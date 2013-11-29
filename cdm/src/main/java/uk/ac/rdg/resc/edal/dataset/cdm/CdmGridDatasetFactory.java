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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset.Gridset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.dataset.AbstractGridDataset;
import uk.ac.rdg.resc.edal.dataset.DataReadingStrategy;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.GridDataSource;
import uk.ac.rdg.resc.edal.dataset.GridDataset;
import uk.ac.rdg.resc.edal.dataset.plugins.MeanSDPlugin;
import uk.ac.rdg.resc.edal.dataset.plugins.VectorPlugin;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.GridFeature;
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
 * @author Guy Griffiths
 * @author Jon
 */
public final class CdmGridDatasetFactory extends DatasetFactory {
    private static final Logger log = LoggerFactory.getLogger(CdmGridDatasetFactory.class);

    @Override
    public GridDataset createDataset(String id, String location) throws IOException, EdalException {
        NetcdfDataset nc = null;
        try {
            /*
             * Open the dataset, using the cache for NcML aggregations
             */
            nc = openDataset(location);

            /*
             * We look for NetCDF-U variables to group mean/standard-deviation.
             * 
             * We need to do this here because we want to subsequently ignore
             * parent variables
             */
            Map<String, String[]> varId2AncillaryVars = new HashMap<String, String[]>();
            for (Variable variable : nc.getVariables()) {
                /*
                 * Just look for parent variables, since these may not have a
                 * grid directly associated with them
                 */
                for (Attribute attr : variable.getAttributes()) {
                    if (attr.getName().equalsIgnoreCase("ancillary_variables")) {
                        varId2AncillaryVars.put(variable.getName(), attr.getStringValue()
                                .split(" "));
                        continue;
                    }
                }
            }

            ucar.nc2.dt.GridDataset gridDataset = CdmUtils.getGridDataset(nc);
            List<GridVariableMetadata> vars = new ArrayList<GridVariableMetadata>();
            /*
             * Store a map of component names. Key is the compound name, value
             * is a 2-element String array with x, y component IDs
             * 
             * Also store a map of whether these components are really
             * eastward/northward, or whether they are locally u/v
             */
            Map<String, String[]> xyComponentPairs = new HashMap<String, String[]>();
            Map<String, Boolean> xyNameToTrueEN = new HashMap<String, Boolean>();
            /*
             * Store a map of variable IDs to UncertML URLs. This will be used
             * to determine which components are mean/std/etc.
             * 
             * TODO implement more than just Mean/SD
             */
            Map<String, String> varId2UncertMLRefs = new HashMap<String, String>();
            /*
             * Here we store the parent variable IDs and their corresponding
             * title.
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
                    String varId = variable.getName();
                    String name = getVariableName(variable);

                    /*
                     * If this is a parent variable for a stats collection, we
                     * don't want it to be a normal variable as well.
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
                        if (attr.getName().equalsIgnoreCase("ref")) {
                            varId2UncertMLRefs.put(varId, attr.getStringValue());
                        }
                    }

                    Parameter parameter = new Parameter(varId, name, variable.getDescription(),
                            variable.getUnitsString());
                    GridVariableMetadata metadata = new GridVariableMetadata(variable.getName(),
                            parameter, hDomain, zDomain, tDomain);
                    vars.add(metadata);

                    if (name != null) {
                        /*
                         * Check for vector components
                         */
                        if (name.contains("eastward")) {
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
                        } else if (name.contains("northward")) {
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
                         * TODO Add zonal/meriodional check
                         */
                    }
                }
            }

            GridDataset cdmGridDataset = new CdmGridDataset(id, location, vars,
                    CdmUtils.getOptimumDataReadingStrategy(nc));
            for (Entry<String, String[]> componentData : xyComponentPairs.entrySet()) {
                String title = componentData.getKey();
                String[] comps = componentData.getValue();
                if (comps[0] != null && comps[1] != null) {
                    cdmGridDataset.addVariablePlugin(new VectorPlugin(comps[0], comps[1], title,
                            xyNameToTrueEN.get(title)));
                }
            }

            for (String statsCollectionId : varId2AncillaryVars.keySet()) {
                String[] ids = varId2AncillaryVars.get(statsCollectionId);
                String meanId = null;
                String stddevId = null;
                for (String statsVarIds : ids) {
                    String uncertRef = varId2UncertMLRefs.get(statsVarIds);
                    if (uncertRef != null
                            && uncertRef
                                    .equalsIgnoreCase("http://www.uncertml.org/statistics/mean")) {
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
        } finally {
            closeDataset(nc);
        }
    }

    private static final class CdmGridDataset extends AbstractGridDataset {
        private final String location;
        private final DataReadingStrategy dataReadingStrategy;

        public CdmGridDataset(String id, String location, Collection<GridVariableMetadata> vars,
                DataReadingStrategy dataReadingStrategy) {
            super(id, vars);
            this.location = location;
            this.dataReadingStrategy = dataReadingStrategy;
        }

        @Override
        public GridFeature readFeature(String featureId) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected GridDataSource openGridDataSource() throws IOException {
            NetcdfDataset nc = openDataset(location);
            return new CdmGridDataSource(CdmUtils.getGridDataset(nc));
        }

        @Override
        protected DataReadingStrategy getDataReadingStrategy() {
            return dataReadingStrategy;
        }
    }

    /**
     * Opens the NetCDF dataset at the given location, using the dataset cache
     * if {@code location} represents an NcML aggregation. We cannot use the
     * cache for OPeNDAP or single NetCDF files because the underlying data may
     * have changed and the NetcdfDataset cache may cache a dataset forever. In
     * the case of NcML we rely on the fact that server administrators ought to
     * have set a "recheckEvery" parameter for NcML aggregations that may change
     * with time. It is desirable to use the dataset cache for NcML aggregations
     * because they can be time-consuming to assemble and we don't want to do
     * this every time a map is drawn.
     * 
     * @param location
     *            The location of the data: a local NetCDF file, an NcML
     *            aggregation file or an OPeNDAP location, {@literal i.e.}
     *            anything that can be passed to
     *            NetcdfDataset.openDataset(location).
     * 
     * @return a {@link NetcdfDataset} object for accessing the data at the
     *         given location.
     * 
     * @throws IOException
     *             if there was an error reading from the data source.
     */
    private static NetcdfDataset openDataset(String location) throws IOException {
        boolean usedCache = false;
        NetcdfDataset nc;
        long start = System.nanoTime();
        if (CdmUtils.isNcmlAggregation(location)) {
            /*
             * We use the cache of NetcdfDatasets to read NcML aggregations as
             * they can be time-consuming to put together. If the underlying
             * data can change we rely on the server admin setting the
             * "recheckEvery" parameter in the aggregation file.
             */
            nc = NetcdfDataset.acquireDataset(location, null);
            usedCache = true;
        } else {
            /*
             * For local single files and OPeNDAP datasets we don't use the
             * cache, to ensure that we are always reading the most up-to-date
             * data. There is a small possibility that the dataset cache will
             * have swallowed up all available file handles, in which case the
             * server admin will need to increase the number of available
             * handles on the server.
             */
            nc = NetcdfDataset.openDataset(location);
        }
        long openedDS = System.nanoTime();
        String verb = usedCache ? "Acquired" : "Opened";
        log.debug(verb + " NetcdfDataset in {} milliseconds", (openedDS - start) / 1.e6);
        return nc;
    }

    /*
     * Closes the given dataset, logging any exceptions at debug level
     */
    private static void closeDataset(NetcdfDataset nc) {
        if (nc == null)
            return;
        try {
            nc.close();
            log.debug("NetCDF file closed");
        } catch (IOException ex) {
            log.error("IOException closing " + nc.getLocation(), ex);
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
                return var.getName();
            } else {
                return longNameAtt.getStringValue();
            }
        } else {
            return stdNameAtt.getStringValue();
        }
    }
}
