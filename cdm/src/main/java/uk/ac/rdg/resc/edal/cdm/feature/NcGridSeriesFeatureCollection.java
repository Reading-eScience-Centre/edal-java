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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDataset.Gridset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.cdm.DataReadingStrategy;
import uk.ac.rdg.resc.edal.cdm.coverage.NcGridSeriesCoverage;
import uk.ac.rdg.resc.edal.cdm.coverage.NcVectorGridSeriesCoverage;
import uk.ac.rdg.resc.edal.cdm.util.CdmUtils;
import uk.ac.rdg.resc.edal.cdm.util.FileUtils;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.Vector2D;

/**
 * An implementation of {@link FeatureCollection} which contains
 * {@link GridSeriesFeature} objects which hold {@link Float} data.
 * 
 * @author Guy Griffiths
 * 
 */
public class NcGridSeriesFeatureCollection implements FeatureCollection<GridSeriesFeature<?>> {

    private String collectionId;
    private String name;
    private Map<String, GridSeriesFeature<?>> id2GridSeriesFeature;

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
        this.collectionId = collectionId;
        this.name = collectionName;

        id2GridSeriesFeature = new HashMap<String, GridSeriesFeature<?>>();
        
        class CompoundData{
            NcGridSeriesCoverage xCoverage;
            String xFilename;
            String xVarId;
            NcGridSeriesCoverage yCoverage;
            String yFilename;
            String yVarId;
            HorizontalGrid xHGrid;
            VerticalAxis xVAxis;
            TimeAxis xTAxis;
            HorizontalGrid yHGrid;
            VerticalAxis yVAxis;
            TimeAxis yTAxis;
            DataReadingStrategy dataReadingStrategy;
        }
        Map<String, CompoundData> compoundsCoverageComponents = new HashMap<String, CompoundData>();

        List<File> files = FileUtils.expandGlobExpression(location);
        for (File file : files) {
            String filename = file.getPath();
            NetcdfDataset ncDataset = CdmUtils.openDataset(filename);

            GridDataset gridDS = CdmUtils.getGridDataset(ncDataset);
            DataReadingStrategy dataReadingStrategy = CdmUtils
                    .getOptimumDataReadingStrategy(ncDataset);

            for (Gridset gridset : gridDS.getGridsets()) {
                /*
                 * Get everything from the GridCoordSystem that is needed for
                 * making an NcGridSeriesFeature, and keep locally until...
                 */
                GridCoordSystem coordSys = gridset.getGeoCoordSystem();
                HorizontalGrid hGrid = CdmUtils.createHorizontalGrid(coordSys);
                VerticalAxis vAxis = CdmUtils.createVerticalAxis(coordSys);
                TimeAxis tAxis = null;
                if (coordSys.hasTimeAxis1D()) {
                    tAxis = CdmUtils.createTimeAxis(coordSys);
                }

                List<GridDatatype> grids = gridset.getGrids();
                for (GridDatatype gridDT : grids) {
                    /*
                     * ...here, where we can get each variable and construct the
                     * NcGridSeriesFeature add add it to the collection
                     */
                    VariableDS var = gridDT.getVariable();
                    String name = CdmUtils.getVariableTitle(var);
                    String varId = var.getName();
                    String description = var.getDescription();

                    NcGridSeriesCoverage coverage = new NcGridSeriesCoverage(filename, varId,
                            hGrid, vAxis, tAxis, description, var.getUnitsString());
                    if (id2GridSeriesFeature.containsKey(varId)) {
                        ((NcGridSeriesFeature) id2GridSeriesFeature.get(varId)).mergeGrid(filename,
                                varId, hGrid, vAxis, tAxis);
                    } else {
                        NcGridSeriesFeature feature = new NcGridSeriesFeature(filename,
                                CdmUtils.getVariableTitle(var), varId, coverage, this,
                                dataReadingStrategy);
                        id2GridSeriesFeature.put(varId, feature);
                    }
                    
                    /*
                     * Now deal with elements which may be part of a compound coverage
                     */
                    if (name.contains("eastward")) {
                        String compoundName = name.replaceFirst("eastward_", "");
                        CompoundData cData;
                        if (!compoundsCoverageComponents.containsKey(compoundName)) {
                            cData = new CompoundData();
                            compoundsCoverageComponents.put(compoundName, cData);
                        }
                        cData = compoundsCoverageComponents.get(compoundName);
                        cData.xCoverage = coverage;
                        cData.xFilename = filename;
                        cData.xVarId = varId;
                        cData.xHGrid = hGrid;
                        cData.xVAxis = vAxis;
                        cData.xTAxis = tAxis;
                        /*
                         * This should be the same for both, and so we can set it in either place
                         */
                        cData.dataReadingStrategy = dataReadingStrategy;
                    } else if (name.contains("northward")) {
                        String compoundName = name.replaceFirst("northward_", "");
                        CompoundData cData;
                        if (!compoundsCoverageComponents.containsKey(compoundName)) {
                            cData = new CompoundData();
                            compoundsCoverageComponents.put(compoundName, cData);
                        }
                        cData = compoundsCoverageComponents.get(compoundName);
                        cData.yCoverage = coverage;
                        cData.yFilename = filename;
                        cData.yVarId = varId;
                        cData.yHGrid = hGrid;
                        cData.yVAxis = vAxis;
                        cData.yTAxis = tAxis;
                    }

                }

            }
        }
        for (String compoundVar : compoundsCoverageComponents.keySet()) {
            CompoundData cData = compoundsCoverageComponents.get(compoundVar);
            if (!cData.xHGrid.equals(cData.yHGrid)
                    || (cData.xVAxis != null && !cData.xVAxis.equals(cData.yVAxis))
                    || (cData.xTAxis != null && !cData.xTAxis.equals(cData.yTAxis))) {
                // TODO Log this better
                System.out.println("Cannot merge data from different grids");
                continue;
            }
            
            String id = cData.xVarId+cData.yVarId;
            String xDesc = cData.xCoverage.getDescription();
            int xIndex = xDesc.indexOf("-component of");
            String description = xDesc.substring(xIndex + 14);
            if (id2GridSeriesFeature.containsKey(id)) {
                ((NcVectorGridSeriesFeature) id2GridSeriesFeature.get(id)).mergeGrids(
                        cData.xFilename, cData.xVarId, cData.yFilename, cData.yVarId, cData.xHGrid,
                        cData.xVAxis, cData.xTAxis);
            } else {
                try {
                    GridSeriesCoverage<Vector2D<Float>> coverage = new NcVectorGridSeriesCoverage(
                            cData.xCoverage, cData.yCoverage);
                    GridSeriesFeature<Vector2D<Float>> feature = new NcVectorGridSeriesFeature(
                            cData.xFilename, cData.xVarId, cData.yFilename, cData.yVarId, id,
                            description, coverage, this, cData.dataReadingStrategy);
                    id2GridSeriesFeature.put(id, feature);
                } catch (InstantiationException e) {
                    /*
                     * If we get this error, it means that the components do not
                     * match properly, and can't make a Vector coverage.
                     */
                    // TODO log the error
                    System.out.println("Cannot merge data from different grids");
                    continue;
                }
            }
        }
    }

    @Override
    public GridSeriesFeature<?> getFeatureById(String id) {
        return id2GridSeriesFeature.get(id);
    }

    @Override
    public Set<String> getFeatureIds() {
        return id2GridSeriesFeature.keySet();
    }

    @Override
    public Collection<GridSeriesFeature<?>> getFeatures() {
        return id2GridSeriesFeature.values();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<GridSeriesFeature<?>> getFeatureType() {
        // TODO check this with usage examples
        return (Class<GridSeriesFeature<?>>) (Class<?>) GridSeriesFeature.class;
    }

    @Override
    public String getId() {
        return collectionId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Iterator<GridSeriesFeature<?>> iterator() {
        /*
         * We cannot simply use:
         * 
         * return id2GridSeriesFeature.values().iterator()
         * 
         * because this will be an iterator of the wrong type
         * 
         * TODO IS THIS STILL TRUE?
         */
        return new Iterator<GridSeriesFeature<?>>() {
            @Override
            public boolean hasNext() {
                return id2GridSeriesFeature.values().iterator().hasNext();
            }

            @Override
            public GridSeriesFeature<?> next() {
                return id2GridSeriesFeature.values().iterator().next();
            }

            @Override
            public void remove() {
                id2GridSeriesFeature.values().iterator().remove();
            }
        };
    }

    public static void main(String[] args) throws IOException {
        NcGridSeriesFeatureCollection nc = new NcGridSeriesFeatureCollection("testId", "testName",
                "/home/guy/Data/OSTIA/*.nc");
        for (String feature : nc.getFeatureIds())
            System.out.println(feature);
        NcGridSeriesFeature feature = (NcGridSeriesFeature) nc.getFeatureById("sea_ice_fraction");
//        NcGridSeriesFeature feature = (NcGridSeriesFeature) nc.getFeatureById("sozowind");
        for (TimePosition tp : feature.getCoverage().getDomain().getTimeAxis()
                .getCoordinateValues()) {
            System.out.println(tp);
        }
    }
}
