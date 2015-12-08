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

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.plugins.MeanSDPlugin;
import uk.ac.rdg.resc.edal.dataset.plugins.VariablePlugin;
import uk.ac.rdg.resc.edal.dataset.plugins.VectorPlugin;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.Parameter.Category;
import uk.ac.rdg.resc.edal.util.GraphicsUtils;

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
    @Override
    public Dataset createDataset(String id, String location) throws IOException, EdalException {
        /*
         * Open the NetcdfDataset, using the cache.
         * 
         * We don't close this - it will get closed once the cache determines it
         * needs to be.
         */
        NetcdfDataset nc = NetcdfDatasetAggregator.openAndAggregateDataset(location);

        /*
         * Generate a simple dataset - delegated to subclasses
         */
        Dataset dataset = generateDataset(id, location, nc);

        /*
         * Scans the NetcdfDataset for variables which pair up to make vectors,
         * and adds the appropriate VectorPlugins
         */
        List<VectorPlugin> vectors = processVectors(nc);
        for (VectorPlugin plugin : vectors) {
            dataset.addVariablePlugin(plugin);
        }

        /*
         * Scans the NetcdfDataset for variables which pair up as mean/stddev,
         * and adds the appropriate MeanSDPlugins
         */
        List<MeanSDPlugin> uncerts = processUncertainty(nc);
        for (MeanSDPlugin plugin : uncerts) {
            dataset.addVariablePlugin(plugin);
        }

        return dataset;
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
        String standardName = stdNameAtt != null ? stdNameAtt.getStringValue() : null;

        /*
         * We look to see if this data is categorical, and if so parse the
         * categories to add the to Parameter
         */
        Map<Integer, Category> catMap = null;
        Attribute flagValues = variable.findAttributeIgnoreCase("flag_values");
        Attribute flagMeanings = variable.findAttributeIgnoreCase("flag_meanings");
        Attribute flagColours = variable.findAttributeIgnoreCase("flag_colors");
        if (flagValues != null) {
            /*
             * We have flag values defined. We also need either labels or
             * colours (preferably both...)
             */
            String[] meaningsArray = null;
            if (flagMeanings != null) {
                meaningsArray = ((String) flagMeanings.getValue(0)).split("\\s+");
                if (meaningsArray.length != flagValues.getLength()) {
                    throw new DataReadingException("Categorical data detected, but there are "
                            + flagValues.getLength() + " category values and "
                            + meaningsArray.length + " category meanings.");
                }
            }
            Color[] coloursArray = null;
            if (flagColours != null) {
                String[] split = ((String) flagColours.getValue(0)).split("\\s+");
                if (split.length != flagValues.getLength()) {
                    throw new DataReadingException("Categorical data detected, but there are "
                            + flagValues.getLength() + " category values and " + split.length
                            + " category colours.");
                }
                coloursArray = new Color[split.length];
                for (int i = 0; i < split.length; i++) {
                    coloursArray[i] = GraphicsUtils.parseColour(split[i]);
                }
            }
            /*
             * We have values and meanings and/or colours.
             */
            catMap = new HashMap<>();
            /*
             * Now map the values to the corresponding labels / colours.
             */
            for (int i = 0; i < flagValues.getLength(); i++) {
                String id, label;
                if (meaningsArray != null) {
                    id = meaningsArray[i];
                    label = id.replaceAll("_", " ");
                } else {
                    id = "category_" + flagValues.getNumericValue(i).intValue();
                    label = "Category value: " + flagValues.getNumericValue(i).intValue();
                }
                Color colour = coloursArray == null ? null : coloursArray[i];
                catMap.put(flagValues.getNumericValue(i).intValue(), new Category(id, label,
                        colour, null));
            }
        }

        return new Parameter(varId, name, variable.getDescription(), variable.getUnitsString(),
                standardName, catMap);
    }

    private List<VectorPlugin> processVectors(NetcdfDataset nc) {
        /*
         * Store a map of component names. Key is the compound name, value is a
         * 2-element String array with x, y component IDs
         * 
         * Also store a map of whether these components are really
         * eastward/northward, or whether they are locally u/v
         */
        Map<String, String[]> xyComponentPairs = new HashMap<String, String[]>();
        Map<String, Boolean> xyNameToTrueEN = new HashMap<String, Boolean>();

        for (Variable var : nc.getVariables()) {
            Attribute stdNameAtt = var.findAttributeIgnoreCase("standard_name");
            if (stdNameAtt != null) {
                String varId = var.getFullName();
                String stdName = stdNameAtt.getStringValue();
                /*
                 * Check for vector components
                 */
                if (stdName.contains("eastward_")) {
                    String compoundName = stdName.replaceFirst("eastward_", "");
                    String[] cData;
                    if (!xyComponentPairs.containsKey(compoundName)) {
                        cData = new String[2];
                        xyComponentPairs.put(compoundName, cData);
                        xyNameToTrueEN.put(compoundName, true);
                    }
                    cData = xyComponentPairs.get(compoundName);
                    /*
                     * By doing this, we will end up with the merged coverage
                     */
                    cData[0] = varId;
                } else if (stdName.contains("northward_")) {
                    String compoundName = stdName.replaceFirst("northward_", "");
                    String[] cData;
                    if (!xyComponentPairs.containsKey(compoundName)) {
                        cData = new String[2];
                        xyComponentPairs.put(compoundName, cData);
                        xyNameToTrueEN.put(compoundName, true);
                    }
                    cData = xyComponentPairs.get(compoundName);
                    /*
                     * By doing this, we will end up with the merged coverage
                     */
                    cData[1] = varId;
                } else if (stdName.matches("u-.*component")) {
                    String compoundName = stdName.replaceFirst("u-(.*)component", "$1");
                    String[] cData;
                    if (!xyComponentPairs.containsKey(compoundName)) {
                        cData = new String[2];
                        xyComponentPairs.put(compoundName, cData);
                        xyNameToTrueEN.put(compoundName, false);
                    }
                    cData = xyComponentPairs.get(compoundName);
                    /*
                     * By doing this, we will end up with the merged coverage
                     */
                    cData[0] = varId;
                } else if (stdName.matches("v-.*component")) {
                    String compoundName = stdName.replaceFirst("v-(.*)component", "$1");
                    String[] cData;
                    if (!xyComponentPairs.containsKey(compoundName)) {
                        cData = new String[2];
                        xyComponentPairs.put(compoundName, cData);
                        xyNameToTrueEN.put(compoundName, false);
                    }
                    cData = xyComponentPairs.get(compoundName);
                    /*
                     * By doing this, we will end up with the merged coverage
                     */
                    cData[1] = varId;
                }
                /*
                 * We could potentially add a check for zonal/meridional here if
                 * required.
                 */
            }
        }

        List<VectorPlugin> ret = new ArrayList<>();
        for (Entry<String, String[]> componentData : xyComponentPairs.entrySet()) {
            String commonName = componentData.getKey();
            String[] comps = componentData.getValue();
            if (comps[0] != null && comps[1] != null) {
                ret.add(new VectorPlugin(comps[0], comps[1], commonName, xyNameToTrueEN
                        .get(commonName)));
            }
        }
        return ret;
    }

    private List<MeanSDPlugin> processUncertainty(NetcdfDataset nc) {
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
                    varId2AncillaryVars.put(variable.getFullName(), attr.getStringValue()
                            .split(" "));
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

        List<MeanSDPlugin> ret = new ArrayList<>();
        for (String statsCollectionId : varId2AncillaryVars.keySet()) {
            String[] ids = varId2AncillaryVars.get(statsCollectionId);
            String meanId = null;
            String stddevId = null;
            for (String statsVarIds : ids) {
                String uncertRef = varId2UncertMLRefs.get(statsVarIds);
                if (uncertRef != null
                        && uncertRef.equalsIgnoreCase("http://www.uncertml.org/statistics/mean")) {
                    meanId = statsVarIds;
                }
                if (uncertRef != null
                        && uncertRef
                                .equalsIgnoreCase("http://www.uncertml.org/statistics/standard-deviation")) {
                    stddevId = statsVarIds;
                }
            }
            if (meanId != null && stddevId != null) {
                MeanSDPlugin meanSDPlugin = new MeanSDPlugin(meanId, stddevId,
                        parentVarId2Title.get(statsCollectionId));
                ret.add(meanSDPlugin);
            }
        }
        return ret;
    }

    /**
     * Generate a {@link Dataset} for the given ID, location, and
     * {@link NetcdfDataset}. Subclasses should use this to generate a simple
     * {@link Dataset} - i.e. one with no additional plugins etc. All required
     * {@link VariablePlugin}s will be detected and handled by this class.
     * 
     * @param id
     *            The ID of the {@link Dataset}
     * @param location
     *            The location of the {@link Dataset} (either on disk or online)
     * @param nc
     *            The {@link NetcdfDataset} representing the {@link Dataset}
     * @return A {@link Dataset}
     * @throws IOException
     *             If there is a problem reading the underlying
     *             {@link NetcdfDataset}
     */
    protected abstract Dataset generateDataset(String id, String location, NetcdfDataset nc)
            throws IOException;

    /**
     * @return the phenomenon that the given variable represents.
     * 
     *         This name will be, in order of preference:
     * 
     *         The standard name
     * 
     *         The long name
     * 
     *         The variable name
     */
    protected static String getVariableName(Variable var) {
        Attribute stdNameAtt = var.findAttributeIgnoreCase("standard_name");
        if (stdNameAtt == null || stdNameAtt.getStringValue().trim().equals("")) {
            Attribute longNameAtt = var.findAttributeIgnoreCase("long_name");
            if (longNameAtt == null || longNameAtt.getStringValue().trim().equals("")) {
                return var.getFullName();
            } else {
                return longNameAtt.getStringValue();
            }
        } else {
            return stdNameAtt.getStringValue();
        }
    }
}