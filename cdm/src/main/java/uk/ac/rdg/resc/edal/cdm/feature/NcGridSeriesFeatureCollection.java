/*******************************************************************************
 * Copyright (c) 2011 The University of Reading
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
 *******************************************************************************/
package uk.ac.rdg.resc.edal.cdm.feature;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDataset.Gridset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.UnitVocabulary;
import uk.ac.rdg.resc.edal.cdm.coverage.grid.LookUpTableGrid;
import uk.ac.rdg.resc.edal.cdm.coverage.grid.NcGridValuesMatrix4D;
import uk.ac.rdg.resc.edal.cdm.util.CdmUtils;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.domain.impl.GridSeriesDomainImpl;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.coverage.impl.DataReadingStrategy;
import uk.ac.rdg.resc.edal.coverage.impl.GridSeriesCoverageImpl;
import uk.ac.rdg.resc.edal.coverage.plugins.VectorPlugin;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.feature.impl.GridSeriesFeatureImpl;
import uk.ac.rdg.resc.edal.feature.impl.UniqueMembersFeatureCollectionImpl;

/**
 * An implementation of {@link FeatureCollection} which contains
 * {@link GridSeriesFeature} objects which hold {@link Float} data.
 * 
 * @author Guy Griffiths
 * 
 */
public class NcGridSeriesFeatureCollection extends UniqueMembersFeatureCollectionImpl<GridSeriesFeature> {
    
    private final static String NCML_AGGREGATION_NAME = "ncWMS-auto_agg.ncml";

    /**
     * Instantiates a collection of features from one or more NetCDF files.
     * 
     * @param collectionId
     *            The ID of the collection (comparable to the Dataset ID in old
     *            ncWMS)
     * @param collectionName
     *            The name of the collection (comparable to the Dataset name in
     *            old ncWMS)
     * @param location
     *            The location of the NetCDF file(s)
     * @throws IOException
     *             If there is a problem reading the file
     */
    public NcGridSeriesFeatureCollection(String collectionId, String collectionName, String location)
            throws IOException {
        super(collectionId, collectionName);

        List<GridSeriesCoverageImpl> coverages = new ArrayList<GridSeriesCoverageImpl>();

        final class XYVarIDs {
            String xVarId;
            String yVarId;
        }

        DataReadingStrategy dataReadingStrategy = null;
        File file = new File(location);

        if (!file.exists()) {
            File baseFile = new File(location);
            File baseDir = baseFile.getParentFile();
            List<File> files = getNetCdfFilesAt(baseDir);
            if(files.size() == 1) {
                file = files.get(0);
            } else {
                file = aggregate(location);
            }
        }

        String filename = file.getPath();
        NetcdfDataset ncDataset = CdmUtils.openDataset(filename);

        GridDataset gridDS = CdmUtils.getGridDataset(ncDataset);
        dataReadingStrategy = CdmUtils.getOptimumDataReadingStrategy(ncDataset);

        int gridNo = 0;
        for (Gridset gridset : gridDS.getGridsets()) {
            gridNo++;
            /*
             * Get everything from the GridCoordSystem that is needed for making
             * an NcGridSeriesFeature, and keep locally until...
             */
            GridCoordSystem coordSys = gridset.getGeoCoordSystem();
            HorizontalGrid hGrid = CdmUtils.createHorizontalGrid(coordSys);
            if (hGrid instanceof LookUpTableGrid) {
                dataReadingStrategy = DataReadingStrategy.BOUNDING_BOX;
            }
            VerticalAxis vAxis = CdmUtils.createVerticalAxis(coordSys);
            TimeAxis tAxis = null;
            if (coordSys.hasTimeAxis1D()) {
                tAxis = CdmUtils.createTimeAxis(coordSys);
            }

            GridSeriesDomain domain = new GridSeriesDomainImpl(hGrid, vAxis, tAxis);
            // TODO more meaningful description
            GridSeriesCoverageImpl coverage = new GridSeriesCoverageImpl(collectionId + gridNo,
                    domain, dataReadingStrategy);

            Map<String, XYVarIDs> xyComponents = new HashMap<String, XYVarIDs>();

            if (!coverages.contains(coverage)) {
                /*
                 * Coverage doesn't exist.
                 */
                List<GridDatatype> grids = gridset.getGrids();
                /*
                 * Now add all of the variables to the coverage
                 */
                for (GridDatatype gridDT : grids) {
                    VariableDS var = gridDT.getVariable();
                    Phenomenon phenomenon = CdmUtils.getPhenomenon(var);
                    Unit units = Unit.getUnit(gridDT.getUnitsString(), UnitVocabulary.UDUNITS2);
                    String name = phenomenon.getStandardName();
                    String varId = var.getName();
                    String description = var.getDescription();
                    
                    if(description == null || description.equals("")){
                        if(name != null && !name.equals("")){
                            description = name;
                        } else {
                            description = varId;
                        }
                    }
                    

                    GridValuesMatrix<Float> gridValueMatrix = new NcGridValuesMatrix4D(
                            hGrid.getXAxis(), hGrid.getYAxis(), vAxis, tAxis, filename, varId);
                    /*
                     * We want to be able to plot gridded data as boxfill, point, contour, or grid points
                     */
                    coverage.addMember(varId, domain, description, phenomenon, units,
                            gridValueMatrix);

                    /*
                     * Now deal with elements which may be part of a compound
                     * coverage
                     */
                    if(name != null){
                        if (name.contains("eastward")) {
                            String compoundName = name.replaceFirst("eastward_", "");
                            XYVarIDs cData;
                            if (!xyComponents.containsKey(compoundName)) {
                                cData = new XYVarIDs();
                                xyComponents.put(compoundName, cData);
                            }
                            cData = xyComponents.get(compoundName);
                            /*
                             * By doing this, we will end up with the merged
                             * coverage
                             */
                            cData.xVarId = varId;
                        } else if (name.contains("northward")) {
                            String compoundName = name.replaceFirst("northward_", "");
                            XYVarIDs cData;
                            if (!xyComponents.containsKey(compoundName)) {
                                cData = new XYVarIDs();
                                xyComponents.put(compoundName, cData);
                            }
                            cData = xyComponents.get(compoundName);
                            /*
                             * By doing this, we will end up with the merged
                             * coverage
                             */
                            cData.yVarId = varId;
                        }
                    }
                }
                coverages.add(coverage);
            } else {
                throw new IllegalArgumentException("We already have this coverage...");
                /*
                 * This was previously where coverages could be extended, but we
                 * no longer allow this.
                 */
            }

            for (String xyVarIDs : xyComponents.keySet()) {
                XYVarIDs xyData = xyComponents.get(xyVarIDs);
                coverage.getScalarMetadata(xyData.xVarId);
                coverage.getScalarMetadata(xyData.yVarId);
                String description = xyVarIDs.replaceAll("_", " ");
                /*
                 * Some common cases
                 */
                if(description.toLowerCase().contains("current")){
                    description = "Current";
                } else if(description.toLowerCase().contains("wind")){
                    description = "Wind";
                }
                VectorPlugin vectorPlugin = new VectorPlugin(
                        coverage.getScalarMetadata(xyData.xVarId),
                        coverage.getScalarMetadata(xyData.yVarId), xyVarIDs, description);
                coverage.addPlugin(vectorPlugin);
            }
        }

        /*
         * We have now processed all of the files into coverages. Now make them
         * into features
         */
        for (GridSeriesCoverage coverage : coverages) {
            // TODO more meaningful name/ID
            GridSeriesFeature feature = new GridSeriesFeatureImpl(collectionName,
                    coverage.getDescription(), this, coverage);
            addFeature(feature);
        }
    }

    /**
     * Returns the number of files in the given location
     * @param location
     * @return
     */
    private List<File> getNetCdfFilesAt(File baseDir) {
        List<File> ret = new ArrayList<File>();
        
        if(baseDir != null) {
            File[] files = baseDir.listFiles();
            if (files != null) {
                for (File subFile : files) {
                    if (subFile.getName().toLowerCase().endsWith(".nc")) {
                        ret.add(subFile);
                    } else if(subFile.isDirectory()) {
                        ret.addAll(getNetCdfFilesAt(subFile));
                    }
                }
            }
        }
        return ret;
    }

    private File aggregate(String location) throws IOException{
        /*
         * The file doesn't exist. The user is *probably* trying to perform
         * an aggregation of all NetCDF files along the time axis. Let's see
         * if that seems to be the case, and then create an ncml file for
         * them.
         */
        /*
         * First get the components of the path
         */
        String[] pathElements = location.split(File.separator);
        String filePart = pathElements[pathElements.length - 1].toLowerCase();
        int finalPathIndex = pathElements.length - 1;
        /*
         * Are we searching for all NetCDF files?
         */
        if (filePart.equals("*.nc")) {
            /*
             * Are we searching recursively?
             */
            boolean recurse = false;
            if (pathElements.length > 1 && pathElements[pathElements.length - 2].equals("**")) {
                recurse = true;
                finalPathIndex--;
            }
            /*
             * Get the base path we're searching in
             */
            StringBuilder basePath = new StringBuilder();
            for (int i = 0; i < finalPathIndex; i++) {
                basePath.append(pathElements[i] + File.separator);
            }
            /*
             * Now find an example file to open, so that we can determine
             * the name of the time dimension
             */
            File basePathFile = new File(basePath.toString());
            
            List<File> netCdfFilesAt = getNetCdfFilesAt(basePathFile);
            if(netCdfFilesAt == null || netCdfFilesAt.size() == 0) {
                throw new FileNotFoundException("No NetCDF files in the location: " + location);
            }
            String timeDimensionName = getTimeDimensionName(netCdfFilesAt.get(0));
            if(timeDimensionName == null){
                throw new IllegalArgumentException(
                        "You have specified wildcards in the path, but the NetCDF files don't all have time axes.  We can only automatically aggregate along time axes.");
            }
            /*
             * Now that we have the name of the time dimension, write an
             * ncml file, and set the location to that
             */
            File ncmlFile = new File(basePathFile, NCML_AGGREGATION_NAME);
            if(!ncmlFile.exists()){
                /*
                 * If it already exists, we won't overwrite it
                 */
                BufferedWriter writer = new BufferedWriter(new FileWriter(ncmlFile));
                writer.write("<netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\">\n");
                writer.write("<aggregation dimName=\""+timeDimensionName+"\" type=\"joinExisting\" recheckEvery=\"5 min\">\n");
                writer.write("<scan location=\"./\" suffix=\".nc\" subdirs=\""+recurse+"\"/>\n");
                writer.write("</aggregation>\n");
                writer.write("</netcdf>\n");
                writer.close();
            }
            return ncmlFile;
        } else {
            throw new FileNotFoundException("Cannot process the location: " + location);
        }
    }
    
    /**
     * Finds the name of the time dimension in the given NetCDF file
     * 
     * @param ncFile
     *            A NetCDF file
     * @return A {@link String} containing the name of the time dimension
     * @throws IOException
     */
    private String getTimeDimensionName(File ncFile) throws IOException {
        NetcdfDataset ncDataset = CdmUtils.openDataset(ncFile.getAbsolutePath());
        GridDataset gridDS = CdmUtils.getGridDataset(ncDataset);
        List<Gridset> gridsets = gridDS.getGridsets();
        if(gridsets != null && gridsets.size() > 0){
            Gridset gridset = gridsets.get(0);
            GridCoordSystem geoCoordSystem = gridset.getGeoCoordSystem();
            if(geoCoordSystem.hasTimeAxis()){
                CdmUtils.closeDataset(ncDataset);
                return geoCoordSystem.getTimeAxis().getName();
            } else {
                CdmUtils.closeDataset(ncDataset);
                return null;
            }
        } else {
            CdmUtils.closeDataset(ncDataset);
            return null;
        }
    }
}
