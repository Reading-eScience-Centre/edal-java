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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset.Gridset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.dataset.DataReadingStrategy;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.GridDataSource;
import uk.ac.rdg.resc.edal.dataset.GriddedDataset;
import uk.ac.rdg.resc.edal.dataset.plugins.MeanSDPlugin;
import uk.ac.rdg.resc.edal.dataset.plugins.VectorPlugin;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.metadata.GridVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.util.cdm.CdmUtils;

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
 * @author Jon
 */
public final class CdmGridDatasetFactory extends DatasetFactory {
    @Override
    public GriddedDataset createDataset(String id, String location) throws IOException,
            EdalException {
        /*
         * Open the dataset, using the cache for NcML aggregations
         */
        NetcdfDataset nc = NetcdfDatasetAggregator.openAndAggregateDataset(location);

        /*-
         * We may in future be able to use forecast model run collection aggregations for
         * dealing with the case of overlapping time axes.  To do this the code will look
         * something like this:
         * 
         * StringBuilder sb = new StringBuilder(); 
         * Formatter formatter = new Formatter(sb, Locale.UK);
         * Fmrc f = Fmrc.open(location, formatter);
         * 
         * in openAndAggregateDataset.  It will need to build up an NcML document which
         * does this.  It should look something like:
         * 
         *  <netcdf xmlns="http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2" enhance="true">
         *      <aggregation dimName="run" type="forecastModelRunCollection" timeUnitsChange="true">
         *           <!-- scanFmrc actually works, but what we want is something like the following bit -->
         *           <scanFmrc location="/home/guy/Data/POLCOMS_IRISH/" regExp=".*\.nc"/>
         *           <netcdf location="/home/guy/Data/POLCOMS_IRISH/polcoms_irish_hourly_20090320.nc" coordValue="2009-03-20T00:00:00Z" enhance="true" />
         *           <netcdf location="/home/guy/Data/POLCOMS_IRISH/polcoms_irish_hourly_20090321.nc" coordValue="2009-03-21T00:00:00Z" enhance="true" />
         *           <netcdf location="/home/guy/Data/POLCOMS_IRISH/polcoms_irish_hourly_20090322.nc" coordValue="2009-03-22T00:00:00Z" enhance="true" />
         *      </aggregation>
         *  </netcdf>
         * 
         * For more documentation see: 
         * http://mailman.unidata.ucar.edu/software/thredds/current/netcdf-java/ncml/FmrcAggregation.html
         * 
         * We then can do stuff like:
         * 
         * ucar.nc2.dt.GridDataset gridDataset = f.getDatasetBest();
         * 
         * To get the single best aggregation of the overlapping time axis
         * 
         * Then we need to work with GridDatasets in place of NetcdfDatasets.  Stuff like:
         * 
         * for(Variable variable : gridDataset.getNetcdfFile().getVariables()) {
         *    // blah blah
         * }
         * 
         * will be necessary.  We need to check that that works with remote datasets too
         */

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

        ucar.nc2.dt.GridDataset gridDataset = CdmUtils.getGridDataset(nc);
        List<GridVariableMetadata> vars = new ArrayList<GridVariableMetadata>();
        /*
         * Store a map of component names. Key is the compound name, value is a
         * 2-element String array with x, y component IDs
         * 
         * Also store a map of whether these components are really
         * eastward/northward, or whether they are locally u/v
         */
        Map<String, String[]> xyComponentPairs = new HashMap<String, String[]>();
        Map<String, Boolean> xyNameToTrueEN = new HashMap<String, Boolean>();
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
        for (Gridset gridset : gridDataset.getGridsets()) {
            GridCoordSystem coordSys = gridset.getGeoCoordSystem();
            HorizontalGrid hDomain = CdmUtils.createHorizontalGrid(coordSys);
            VerticalAxis zDomain = CdmUtils.createVerticalAxis(coordSys);
            TimeAxis tDomain = CdmUtils.createTimeAxis(coordSys);

            /*
             * Create a VariableMetadata object for each GridDatatype
             */
            for (GridDatatype grid : gridset.getGrids()) {
                VariableDS variable = grid.getVariable();
                String varId = variable.getFullName();
                String name = getVariableName(variable);

                Attribute stdNameAtt = variable.findAttributeIgnoreCase("standard_name");
                String standardName = stdNameAtt != null ? stdNameAtt.getStringValue() : null;

                /*
                 * If this is a parent variable for a stats collection, we don't
                 * want it to be a normal variable as well.
                 */
                if (varId2AncillaryVars.containsKey(varId)) {
                    parentVarId2Title.put(varId, name);
                    continue;
                }

                /*
                 * If it is a child variable is (potentially) referenced by
                 * UncertML, store its ID and the (possible) UncertML URI
                 */
                for (Attribute attr : variable.getAttributes()) {
                    if (attr.getFullName().equalsIgnoreCase("ref")) {
                        varId2UncertMLRefs.put(varId, attr.getStringValue());
                    }
                }

                Parameter parameter = new Parameter(varId, name, variable.getDescription(),
                        variable.getUnitsString(), standardName);
                GridVariableMetadata metadata = new GridVariableMetadata(parameter, hDomain,
                        zDomain, tDomain, true);
                vars.add(metadata);

                if (name != null) {
                    /*
                     * Check for vector components
                     */
                    if (name.contains("eastward_")) {
                        String compoundName = name.replaceFirst("eastward_", "");
                        String[] cData;
                        if (!xyComponentPairs.containsKey(compoundName)) {
                            cData = new String[2];
                            xyComponentPairs.put(compoundName, cData);
                            xyNameToTrueEN.put(compoundName, true);
                        }
                        cData = xyComponentPairs.get(compoundName);
                        /*
                         * By doing this, we will end up with the merged
                         * coverage
                         */
                        cData[0] = varId;
                    } else if (name.contains("northward_")) {
                        String compoundName = name.replaceFirst("northward_", "");
                        String[] cData;
                        if (!xyComponentPairs.containsKey(compoundName)) {
                            cData = new String[2];
                            xyComponentPairs.put(compoundName, cData);
                            xyNameToTrueEN.put(compoundName, true);
                        }
                        cData = xyComponentPairs.get(compoundName);
                        /*
                         * By doing this, we will end up with the merged
                         * coverage
                         */
                        cData[1] = varId;
                    } else if (name.matches("u-.*component")) {
                        String compoundName = name.replaceFirst("u-(.*)component", "$1");
                        String[] cData;
                        if (!xyComponentPairs.containsKey(compoundName)) {
                            cData = new String[2];
                            xyComponentPairs.put(compoundName, cData);
                            xyNameToTrueEN.put(compoundName, false);
                        }
                        cData = xyComponentPairs.get(compoundName);
                        /*
                         * By doing this, we will end up with the merged
                         * coverage
                         */
                        cData[0] = varId;
                    } else if (name.matches("v-.*component")) {
                        String compoundName = name.replaceFirst("v-(.*)component", "$1");
                        String[] cData;
                        if (!xyComponentPairs.containsKey(compoundName)) {
                            cData = new String[2];
                            xyComponentPairs.put(compoundName, cData);
                            xyNameToTrueEN.put(compoundName, false);
                        }
                        cData = xyComponentPairs.get(compoundName);
                        /*
                         * By doing this, we will end up with the merged
                         * coverage
                         */
                        cData[1] = varId;
                    }
                    /*
                     * We could potentially add a check for zonal/meridional
                     * here if required.
                     */
                }
            }
        }

        CdmGridDataset cdmGridDataset = new CdmGridDataset(id, location, vars,
                CdmUtils.getOptimumDataReadingStrategy(nc));
        for (Entry<String, String[]> componentData : xyComponentPairs.entrySet()) {
            String commonName = componentData.getKey();
            String[] comps = componentData.getValue();
            if (comps[0] != null && comps[1] != null) {
                cdmGridDataset.addVariablePlugin(new VectorPlugin(comps[0], comps[1], commonName,
                        xyNameToTrueEN.get(commonName)));
            }
        }

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
                cdmGridDataset.addVariablePlugin(meanSDPlugin);
            }
        }

        return cdmGridDataset;
    }

    private final class CdmGridDataset extends GriddedDataset {
        private final String location;
        private final DataReadingStrategy dataReadingStrategy;

        public CdmGridDataset(String id, String location, Collection<GridVariableMetadata> vars,
                DataReadingStrategy dataReadingStrategy) {
            super(id, vars);
            this.location = location;
            this.dataReadingStrategy = dataReadingStrategy;
        }

        @Override
        protected GridDataSource openDataSource() throws DataReadingException {
            NetcdfDataset nc;
            try {
                nc = NetcdfDatasetAggregator.openAndAggregateDataset(location);
                synchronized (this) {
                    /*
                     * If the getGridDataset method runs concurrently on the
                     * same object, we can get a
                     * ConcurrentModificationException, so we synchronise this
                     * action to avoid the issue.
                     */
                    return new CdmGridDataSource(CdmUtils.getGridDataset(nc));
                }
            } catch (EdalException | IOException e) {
                throw new DataReadingException("Problem aggregating datasets", e);
            }
        }

        @Override
        protected DataReadingStrategy getDataReadingStrategy() {
            return dataReadingStrategy;
        }
    }

    /**
     * Returns the phenomenon that the given variable represents.
     * 
     * This name will be, in order of preference:
     * 
     * The standard name
     * 
     * The long name
     * 
     * The variable name
     */
    private static String getVariableName(Variable var) {
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