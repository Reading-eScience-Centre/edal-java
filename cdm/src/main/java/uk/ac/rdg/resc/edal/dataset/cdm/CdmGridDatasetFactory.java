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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset.Gridset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ncml.NcMLReader;
import ucar.nc2.units.DateUnit;
import uk.ac.rdg.resc.edal.dataset.DataReadingStrategy;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.GridDataSource;
import uk.ac.rdg.resc.edal.dataset.GriddedDataset;
import uk.ac.rdg.resc.edal.dataset.plugins.MeanSDPlugin;
import uk.ac.rdg.resc.edal.dataset.plugins.VectorPlugin;
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
//    private static final Logger log = LoggerFactory.getLogger(CdmGridDatasetFactory.class);

    private static final int DATASET_CACHE_SIZE = 10;

    private static Map<String, String> ncmlStringCache = new HashMap<>();
    /**
     * A LRU cache of {@link NetcdfDataset}s.
     */
    private static Map<String, NetcdfDataset> datasetCache = new LinkedHashMap<String, NetcdfDataset>(
            DATASET_CACHE_SIZE + 1, 1.0f, true) {
        private static final long serialVersionUID = 1L;

        protected boolean removeEldestEntry(Map.Entry<String, NetcdfDataset> eldest) {
            /*
             * If we are going to remove the eldest entry, we also want to call
             * the close method on it before allowing LinkedHashMap to do the
             * actual removal
             */
            if (super.size() > DATASET_CACHE_SIZE) {
                try {
                    CdmUtils.closeDataset(eldest.getValue());
                } catch (IOException e) {
                    /*
                     * If we can't close it, do not remove it...
                     */
                    return false;
                }
                return true;
            }
            return false;
        };

    };

    @Override
    public GriddedDataset createDataset(String id, String location) throws IOException,
            EdalException {
        NetcdfDataset nc = null;
        /*
         * Open the dataset, using the cache for NcML aggregations
         */
        nc = openAndAggregateDataset(location);

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
        protected GridDataSource openGridDataSource() throws IOException {
            NetcdfDataset nc;
            try {
                nc = openAndAggregateDataset(location);
            } catch (EdalException e) {
                throw new IOException("Problem aggregating datasets", e);
            }
            synchronized (this) {
                /*
                 * If the getGridDataset method runs concurrently on the same
                 * object, we can get a ConcurrentModificationException, so we
                 * synchronise this action to avoid the issue.
                 */
                return new CdmGridDataSource(CdmUtils.getGridDataset(nc));
            }
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
    private static NetcdfDataset openAndAggregateDataset(String location) throws IOException,
            EdalException {
        if (datasetCache.containsKey(location)) {
            return datasetCache.get(location);
        }
        NetcdfDataset nc;
        if (location.startsWith("dods://") || location.startsWith("http://")) {
            /*
             * We have a remote dataset
             */
            nc = CdmUtils.openDataset(location);
        } else {
            /*
             * We have a local dataset
             */
            List<File> files = null;
            try {
                files = CdmUtils.expandGlobExpression(location);
            } catch (NullPointerException e) {
                System.out.println("NPE processing location: " + location);
                throw e;
            }
            if (files.size() == 0) {
                throw new EdalException("The location " + location
                        + " doesn't refer to any existing files.");
            }
            if (files.size() == 1) {
                location = files.get(0).getAbsolutePath();
                nc = CdmUtils.openDataset(location);
            } else {
                /*
                 * We have multiple files in a glob expression. We write some
                 * NcML and use the NetCDF aggregation libs to parse this into
                 * an aggregated dataset.
                 * 
                 * If we have already generated the ncML on a previous call,
                 * just use that.
                 */
                String ncmlString;
                if (ncmlStringCache.containsKey(location)) {
                    ncmlString = ncmlStringCache.get(location);
                } else {
                    /*
                     * Find the name of the time dimension
                     */
                    NetcdfDataset first = openAndAggregateDataset(files.get(0).getAbsolutePath());
                    String timeDimName = null;
                    for (Variable var : first.getVariables()) {
                        if (var.isCoordinateVariable()) {
                            for (Attribute attr : var.getAttributes()) {
                                if (attr.getFullName().equalsIgnoreCase("units")
                                        && attr.getStringValue().contains(" since ")) {
                                    /*
                                     * This is the time dimension. Since this is
                                     * a co-ordinate variable, there is only 1
                                     * dimension
                                     */
                                    Dimension timeDimension = var.getDimension(0);
                                    timeDimName = timeDimension.getFullName();
                                }
                            }
                        }
                    }
                    if (timeDimName == null) {
                        throw new EdalException(
                                "Cannot join multiple files without time dimensions");
                    }
                    
                    /*
                     * Create a Map
                     */
                    Map<Long, Map<String,String>> time2vars2filename = new HashMap<>();
                    for(File file : files) {
                        NetcdfFile ncFile = null;
                        try {
                            ncFile = NetcdfFile.open(file.getAbsolutePath());
                            Variable timeVar = ncFile.findVariable(timeDimName);
                            String unitsString = timeVar.findAttribute("units").getStringValue();
                            String[] unitsParts = unitsString.split(" since ");
                            long time = new DateUnit(timeVar.read().getDouble(0), unitsParts[0], DateUnit.getStandardOrISO(unitsParts[1])).getDate().getTime();
                            if(!time2vars2filename.containsKey(time)) {
                                Map<String, String> vars2filename = new HashMap<>();
                                time2vars2filename.put(time, vars2filename);
                            }
                            List<Variable> variables = ncFile.getVariables();
                            String varNames = "";
                            for(Variable v : variables) {
                                varNames += v.getFullName();
                            }
                            time2vars2filename.get(time).put(varNames, file.getAbsolutePath());
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            ncFile.close();
                        }
                    }
                    
                    List<Long> times = new ArrayList<>(time2vars2filename.keySet());
                    Collections.sort(times);

                    /*
                     * Now create the NcML string and use it to create an
                     * aggregated dataset
                     */
                    StringBuffer ncmlStringBuffer = new StringBuffer();
                    ncmlStringBuffer
                            .append("<netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\">");
                    ncmlStringBuffer.append("<aggregation dimName=\"" + timeDimName
                            + "\" type=\"joinExisting\">");
                    for(Long time : times) {
                        Map<String, String> vars2filename = time2vars2filename.get(time);
                        if(vars2filename.size() == 1) {
                            String filename = vars2filename.values().iterator().next();
                            ncmlStringBuffer.append("<netcdf location=\"" + filename
                                    + "\"/>");
                        } else {
                            ncmlStringBuffer.append("<netcdf><aggregation type=\"union\">");
                            for(Entry<String,String> entry : vars2filename.entrySet()) {
                                ncmlStringBuffer.append("<netcdf location=\""+entry.getValue()+"\"/>");
                            }
                            ncmlStringBuffer.append("</aggregation></netcdf>");
                        }
                    }
                    ncmlStringBuffer.append("</aggregation>");
                    ncmlStringBuffer.append("</netcdf>");

                    ncmlString = ncmlStringBuffer.toString();
                    ncmlStringCache.put(location, ncmlString);
                }
                nc = NcMLReader.readNcML(new StringReader(ncmlString), null);
            }
        }
        datasetCache.put(location, nc);
        return nc;
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