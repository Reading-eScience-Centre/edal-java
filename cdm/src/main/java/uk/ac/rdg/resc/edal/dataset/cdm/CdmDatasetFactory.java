/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc.edal.dataset.cdm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import uk.ac.rdg.resc.edal.dataset.DataSource;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.DiscreteLayeredDataset;
import uk.ac.rdg.resc.edal.dataset.plugins.ArbitraryGroupPlugin;
import uk.ac.rdg.resc.edal.dataset.plugins.ValueErrorPlugin;
import uk.ac.rdg.resc.edal.dataset.plugins.VariablePlugin;
import uk.ac.rdg.resc.edal.dataset.plugins.VectorPlugin;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.metadata.DiscreteLayeredVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.Parameter.Category;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;

/**
 * {@link DatasetFactory} that creates {@link Dataset}s representing gridded
 * data read through the Unidata Common Data Model.
 * 
 * Although multiple instances of this {@link DatasetFactory} can be created,
 * all share a common cache of NetcdfDataset objects to speed up operations
 * where the same dataset is accessed multiple times. To avoid excess file
 * handles being open, this is a LRU cache which closes the datasets when they
 * expire.
 * 
 * @author Guy Griffiths
 */
public abstract class CdmDatasetFactory extends DatasetFactory {
    private static final Logger log = LoggerFactory.getLogger(CdmDatasetFactory.class);
    
    @Override
    public DiscreteLayeredDataset<? extends DataSource, ? extends DiscreteLayeredVariableMetadata> createDataset(
            String id, String location) throws IOException, EdalException {
        return createDataset(id, location, false);
    }

    @Override
    public DiscreteLayeredDataset<? extends DataSource, ? extends DiscreteLayeredVariableMetadata> createDataset(
            String id, String location, boolean forceRefresh) throws EdalException {
        NetcdfDataset nc = null;
        try {
            /*
             * Open the NetcdfDataset, using the cache.
             */
            nc = getNetcdfDatasetFromLocation(location, forceRefresh);

            /*
             * Generate a simple dataset - delegated to subclasses
             */
            DiscreteLayeredDataset<? extends DataSource, ? extends DiscreteLayeredVariableMetadata> dataset = generateDataset(
                    id, location, nc);

            /*
             * Scans the NetcdfDataset for variables which pair up to make
             * vectors, and adds the appropriate VectorPlugins
             */
            List<VectorPlugin> vectors = processVectors(dataset);
            for (VectorPlugin plugin : vectors) {
                dataset.addVariablePlugin(plugin);
            }

            /*
             * Scans the NetcdfDataset for variables which pair up as
             * mean/stddev, and adds the appropriate MeanSDPlugins
             */
            List<ValueErrorPlugin> uncerts = processUncertainty(nc);
            for (ValueErrorPlugin plugin : uncerts) {
                dataset.addVariablePlugin(plugin);
            }

            /*
             * Scans the NetcdfDataset for variables which are logically
             * grouped, and adds the grouping plugin
             */
            List<ArbitraryGroupPlugin> groups = processGroups(nc);
            for (ArbitraryGroupPlugin plugin : groups) {
                dataset.addVariablePlugin(plugin);
            }

            /*
             * Release the dataset so that it can be removed from the cache if
             * necessary.
             */
            return dataset;
        } catch (Throwable e) {
            throw new EdalException("Problem creating dataset " + id + " at " + location, e);
        } finally {
            NetcdfDatasetAggregator.releaseDataset(nc);
        }
    }

    protected NetcdfDataset getNetcdfDatasetFromLocation(String location, boolean forceRefresh)
            throws IOException, EdalException {
        return NetcdfDatasetAggregator.getDataset(location, forceRefresh);
    }

    /**
     * Generates a {@link Parameter} object, correctly parsing categorical flags
     * and creating the {@link Category}s associated with the {@link Parameter}.
     * Subclasses should use this method to generate {@link Parameter}s.
     * 
     * @param variable
     *            The {@link Variable} object representing the variable in the
     *            NetCDF file
     * @return The resulting {@link Parameter}
     */
    protected Parameter getParameter(Variable variable) {
        String varId = variable.getFullName();
        String name = getVariableName(variable);

        Attribute stdNameAtt = variable.findAttributeIgnoreCase("standard_name");
        String standardName = null;
        if (stdNameAtt != null) {
            standardName = stdNameAtt.getStringValue();
        } else {
            /*
             * We don't have a standard name defined. Use "long_name" as a
             * backup. This won't be strictly correct, but it may be useful. We
             * don't actually check that standard names are valid anyway.
             */
            Attribute longNameAtt = variable.findAttributeIgnoreCase("long_name");
            if (longNameAtt != null) {
                standardName = longNameAtt.getStringValue();
            }
        }

        /*
         * We look to see if this data is categorical, and if so parse the
         * categories to add the to Parameter
         */
        Map<Integer, Category> catMap = null;
        Attribute flagValues = variable.findAttributeIgnoreCase("flag_values");
        Attribute flagMeanings = variable.findAttributeIgnoreCase("flag_meanings");
        Attribute flagNamespaceAttr = variable.findAttributeIgnoreCase("flag_namespace");
        Attribute flagColours = variable.findAttributeIgnoreCase("flag_colors");
        if (flagValues != null) {
            /*
             * We have flag values defined. We also need either labels or
             * colours (preferably both...)
             */
            String[] meaningsArray = null;
            if (flagMeanings != null && flagMeanings.isString()) {
                meaningsArray = flagMeanings.getStringValue().split("\\s+");
                if (meaningsArray.length != flagValues.getLength()) {
                    throw new DataReadingException("Categorical data detected, but there are "
                            + flagValues.getLength() + " category values and "
                            + meaningsArray.length + " category meanings.");
                }
            }
            String[] coloursArray = null;
            if (flagColours != null) {
                coloursArray = ((String) flagColours.getValue(0)).split("\\s+");
                if (coloursArray.length != flagValues.getLength()) {
                    throw new DataReadingException("Categorical data detected, but there are "
                            + flagValues.getLength() + " category values and " + coloursArray.length
                            + " category colours.");
                }
            }
            /*
             * We have values and meanings and/or colours.
             */
            catMap = new HashMap<>();
            /*
             * Now map the values to the corresponding labels / colours.
             */
            String flagNamespace = null;
            if (flagNamespaceAttr != null) {
                if (flagNamespaceAttr.isString()) {
                    flagNamespace = flagNamespaceAttr.getStringValue();
                }
            }
            for (int i = 0; i < flagValues.getLength(); i++) {
                String id, label;
                if (meaningsArray != null) {
                    id = meaningsArray[i];
                    label = id.replaceAll("_", " ");
                } else {
                    id = "category_" + flagValues.getNumericValue(i).intValue();
                    label = "Category value: " + flagValues.getNumericValue(i).intValue();
                }
                if (flagNamespace != null) {
                    id = flagNamespace + id;
                }
                String colour = coloursArray == null ? null : coloursArray[i];
                catMap.put(flagValues.getNumericValue(i).intValue(),
                        new Category(id, label, colour, null));
            }
        }

        return new Parameter(varId, name, variable.getDescription(), variable.getUnitsString(),
                standardName, catMap);
    }

    private List<VectorPlugin> processVectors(Dataset ds) {
        /*
         * Store a map of component names. Key is the compound name, value is a
         * 2-element String array with x, y component IDs
         * 
         * Also store a map of whether these components are really
         * eastward/northward, or whether they are locally u/v
         */
        Map<String, String[]> xyComponentPairs = new HashMap<String, String[]>();
        Map<String, Boolean> xyNameToTrueEN = new HashMap<String, Boolean>();

        Set<String> variableIds = ds.getVariableIds();
        
        /*
         * Include a check to see if multiple vector variables have the same standard name
         */
        Map<String, Integer> stdNameToCount = new HashMap<>();
        for (String varId : variableIds) {
            VariableMetadata metadata = ds.getVariableMetadata(varId);
            String stdName = metadata.getParameter().getStandardName();
            if (stdName != null) {
                /*
                 * Check for vector components
                 */
                IdComponentEastNorth vectorInfo = determineVectorIdAndComponent(stdName);
                if (vectorInfo != null) {
                    String[] cData;
                    if (!xyComponentPairs.containsKey(vectorInfo.id)) {
                        cData = new String[2];
                        xyComponentPairs.put(vectorInfo.id, cData);
                        xyNameToTrueEN.put(vectorInfo.id, vectorInfo.isEastNorth);
                        
                        stdNameToCount.put(vectorInfo.id, 0);
                    }
                    cData = xyComponentPairs.get(vectorInfo.id);
                    /*
                     * This checks whether we've used the same standard name more than twice
                     */
                    stdNameToCount.put(vectorInfo.id, stdNameToCount.get(vectorInfo.id) + 1);
                    /*
                     * By doing this, we will end up with the merged coverage
                     */
                    if (vectorInfo.isX) {
                        cData[0] = varId;
                    } else {
                        cData[1] = varId;
                    }
                }
            }
        }
        
        for(Entry<String, Integer> entry : stdNameToCount.entrySet()) {
            if(entry.getValue() > 2) {
                /*
                 * This standard name root has been used more than once
                 */
                String stdRoot = entry.getKey();
                xyComponentPairs.remove(stdRoot);
                StringSimilarityService similar = new StringSimilarityServiceImpl(new JaroWinklerStrategy());
                List<String> xVars = new ArrayList<>();
                List<Boolean> xVarIndexedTrueEN = new ArrayList<>();
                List<String> yVars = new ArrayList<>();
                for (String varId : variableIds) {
                    VariableMetadata metadata = ds.getVariableMetadata(varId);
                    String stdName = metadata.getParameter().getStandardName();
                    if (stdName != null && stdName.contains(stdRoot)) {
                        IdComponentEastNorth vectorInfo = determineVectorIdAndComponent(stdName);
                        if (vectorInfo == null) {
                          continue;
                        }
                        if(vectorInfo.isX) {
                            xVars.add(varId);
                            xVarIndexedTrueEN.add(vectorInfo.isEastNorth);
                        } else {
                            yVars.add(varId);
                        }
                    }
                }
                for(String xVar : xVars) {
                    String closest = yVars.get(0);
                    double score = 0;
                    for(String yVar : yVars) {
                        double currentScore = similar.score(xVar, yVar);
                        if(currentScore > score) {
                            closest = yVar;
                            score = currentScore;
                        }
                    }
                    yVars.remove(closest);
                    String commonName = stdRoot + " ("+xVar+","+closest+")";
                    xyComponentPairs.put(commonName, new String[2]);
                    xyComponentPairs.get(commonName)[0] = xVar;
                    xyComponentPairs.get(commonName)[1] = closest;
                    xyNameToTrueEN.put(commonName, xVarIndexedTrueEN.get(xVars.indexOf(xVar)));
                }
            }
        }

        List<VectorPlugin> ret = new ArrayList<>();
        for (Entry<String, String[]> componentData : xyComponentPairs.entrySet()) {
            String commonName = componentData.getKey();
            String[] comps = componentData.getValue();
            if (comps[0] != null && comps[1] != null) {
                ret.add(new VectorPlugin(comps[0], comps[1], commonName,
                        xyNameToTrueEN.get(commonName)));
            }
        }
        return ret;
    }

    private IdComponentEastNorth determineVectorIdAndComponent(String stdName) {
        /*
         * We could potentially add a check for zonal/meridional here if
         * required.
         */
        if (stdName.contains("eastward_")) {
            return new IdComponentEastNorth(stdName.replaceFirst("eastward_", ""), true, true);
        } else if (stdName.contains("northward_")) {
            return new IdComponentEastNorth(stdName.replaceFirst("northward_", ""), false, true);
        } else if (stdName.matches("u-.*component")) {
            return new IdComponentEastNorth(stdName.replaceFirst("u-(.*)component", "$1"), true,
                    false);
        } else if (stdName.matches("v-.*component")) {
            return new IdComponentEastNorth(stdName.replaceFirst("v-(.*)component", "$1"), false,
                    false);
        } else if (stdName.startsWith("u-component")) {
        	return new IdComponentEastNorth(stdName.replaceFirst("u-component", ""), true,
        			false);
        } else if (stdName.startsWith("v-component")) {
        	return new IdComponentEastNorth(stdName.replaceFirst("v-component", ""), false,
        			false);
        } else if (stdName.matches(".*x_.*velocity")) {
            return new IdComponentEastNorth(stdName.replaceFirst("(.*)x_(.*velocity)", "$1$2"),
                    true, false);
        } else if (stdName.matches(".*y_.*velocity")) {
            return new IdComponentEastNorth(stdName.replaceFirst("(.*)y_(.*velocity)", "$1$2"),
                    false, false);
        }
        return null;
    }

    private class IdComponentEastNorth {
        String id;
        boolean isX;
        boolean isEastNorth;

        public IdComponentEastNorth(String id, boolean isX, boolean isEastNorth) {
            this.id = id;
            this.isX = isX;
            this.isEastNorth = isEastNorth;
        }
    }

    private List<ValueErrorPlugin> processUncertainty(NetcdfDataset nc) {
        /*
         * We look for NetCDF-U variables to group mean/standard-deviation.
         * 
         * We need to do this here because we want to subsequently ignore parent
         * variables
         */
        Map<String, String[]> varId2AncillaryVars = new HashMap<String, String[]>();
        for (Variable variable : nc.getVariables()) {
            /*
             * Just look for parent variables, since these may not have a grid
             * directly associated with them
             */
            for (Attribute attr : variable.getAttributes()) {
                if (attr.getFullName().equalsIgnoreCase("ancillary_variables")) {
                    varId2AncillaryVars.put(variable.getFullName(),
                            attr.getStringValue().split(" "));
                    continue;
                }
            }
        }

        /*
         * Store a map of variable IDs to UncertML URLs. This will be used to
         * determine which components are mean/std/etc.
         * 
         * TODO implement more than just Mean/SD
         */
        Map<String, String> varId2UncertMLRefs = new HashMap<String, String>();
        /*
         * Here we store the parent variable IDs and their corresponding title.
         */
        Map<String, String> parentVarId2Title = new HashMap<String, String>();
        for (Variable var : nc.getVariables()) {
            String varId = var.getFullName();
            /*
             * If this is a parent variable for a stats collection, we don't
             * want it to be a normal variable as well.
             */
            if (varId2AncillaryVars.containsKey(varId)) {
                parentVarId2Title.put(varId, getVariableName(var));
                continue;
            }

            /*
             * If it is a child variable is (potentially) referenced by
             * UncertML, store its ID and the (possible) UncertML URI
             */
            for (Attribute attr : var.getAttributes()) {
                if (attr.getFullName().equalsIgnoreCase("ref")) {
                    varId2UncertMLRefs.put(varId, attr.getStringValue());
                }
            }
        }

        List<ValueErrorPlugin> ret = new ArrayList<>();
        for (String statsCollectionId : varId2AncillaryVars.keySet()) {
            String[] ids = varId2AncillaryVars.get(statsCollectionId);
            String valueId = null;
            String errorId = null;
            for (String statsVarIds : ids) {
                String uncertRef = varId2UncertMLRefs.get(statsVarIds);
                if (valueId == null && ("http://www.uncertml.org/statistics/mean"
                        .equalsIgnoreCase(uncertRef)
                        || "http://www.uncertml.org/statistics/median".equalsIgnoreCase(uncertRef)
                        || "http://www.uncertml.org/statistics/mode".equalsIgnoreCase(uncertRef)
                        || "http://www.uncertml.org/statistics/moment"
                                .equalsIgnoreCase(uncertRef))) {
                    valueId = statsVarIds;
                }
                if (errorId == null && ("http://www.uncertml.org/statistics/standard-deviation"
                        .equalsIgnoreCase(uncertRef)
                        || "http://www.uncertml.org/statistics/variance"
                                .equalsIgnoreCase(uncertRef))) {
                    errorId = statsVarIds;
                }
            }
            if (valueId != null && errorId != null) {
                ValueErrorPlugin meanSDPlugin = new ValueErrorPlugin(valueId, errorId,
                        parentVarId2Title.get(statsCollectionId));
                ret.add(meanSDPlugin);
            }
        }
        return ret;
    }

    private List<ArbitraryGroupPlugin> processGroups(NetcdfDataset nc) {
        /*
         * We look for the tag "logical_group" and group variables with a common
         * group together
         */
        Map<String, List<String>> groupId2Vars = new HashMap<>();
        for (Variable variable : nc.getVariables()) {
            /*
             * Just look for parent variables, since these may not have a grid
             * directly associated with them
             */
            for (Attribute attr : variable.getAttributes()) {
                if (attr.getFullName().equalsIgnoreCase("logical_group")) {
                    String groupName = attr.getStringValue();
                    if (!groupId2Vars.containsKey(groupName)) {
                        groupId2Vars.put(groupName, new ArrayList<>());
                    }
                    groupId2Vars.get(groupName).add(variable.getFullName());
                }
            }
        }

        List<ArbitraryGroupPlugin> ret = new ArrayList<>();
        for (Entry<String, List<String>> entry : groupId2Vars.entrySet()) {
            List<String> groupedVars = entry.getValue();
            if (groupedVars.size() < 2) {
                log.warn("The group: "+entry.getKey()+" has fewer than 2 variables.  It will not be added");
            } else {
                ret.add(new ArbitraryGroupPlugin(entry.getKey(), groupedVars.toArray(new String[0])));
            }
        }
        return ret;
    }

    /**
     * Generate a {@link DiscreteLayeredDataset} for the given ID, location, and
     * {@link NetcdfDataset}. Subclasses should use this to generate a simple
     * {@link DiscreteLayeredDataset} - i.e. one with no additional plugins etc.
     * All required {@link VariablePlugin}s will be detected and handled by this
     * class.
     * 
     * @param id
     *            The ID of the {@link Dataset}
     * @param location
     *            The location of the {@link Dataset} (either on disk or online)
     * @param nc
     *            The {@link NetcdfDataset} representing the {@link Dataset}
     * @return A {@link DiscreteLayeredDataset}
     * @throws IOException
     *             If there is a problem reading the underlying
     *             {@link NetcdfDataset}
     */
    protected abstract DiscreteLayeredDataset<? extends DataSource, ? extends DiscreteLayeredVariableMetadata> generateDataset(
            String id, String location, NetcdfDataset nc) throws IOException;

    /**
     * @return the name of the phenomenon that the given variable represents.
     * 
     *         This name will be, in order of preference:
     * 
     *         The long name
     * 
     *         The standard name
     * 
     *         The variable name
     */
    protected static String getVariableName(Variable var) {
        Attribute longNameAtt = var.findAttributeIgnoreCase("long_name");
        if (longNameAtt == null || longNameAtt.getStringValue().trim().equals("")) {
            Attribute stdlongNameAtt = var.findAttributeIgnoreCase("standard_name");
            if (stdlongNameAtt == null || stdlongNameAtt.getStringValue().trim().equals("")) {
                return var.getFullName();
            } else {
                return stdlongNameAtt.getStringValue();
            }
        } else {
            return longNameAtt.getStringValue();
        }
    }
}