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

import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset.Gridset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
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

        List<File> files = FileUtils.expandGlobExpression(location);
        for (File file : files) {
            NetcdfDataset ncDataset = openDataset(file.getPath());

            FeatureDataset featureDS = FeatureDatasetFactoryManager.wrap(FeatureType.GRID,
                    ncDataset, null, null);
            if (featureDS == null) {
                throw new IOException("No grid datasets found in file: " + file.getPath());
            }
            FeatureType fType = featureDS.getFeatureType();
            assert (fType == FeatureType.GRID);
            GridDataset gridDS = (GridDataset) featureDS;

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

                Map<String, GridDatatype[]> compoundsCoverageComponents = new HashMap<String, GridDatatype[]>();

                List<GridDatatype> grids = gridset.getGrids();
                for (GridDatatype gridDT : grids) {
                    /*
                     * ...here, where we can get each variable and construct the
                     * NcGridSeriesFeature add add it to the collection
                     */
                    VariableDS var = gridDT.getVariable();
                    String name = CdmUtils.getVariableTitle(var);
                    String id = gridDT.getName();
//                    String description = gridDT.getDescription();

                    if (name.contains("eastward")) {
                        String compoundName = name.replaceFirst("eastward_", "");
                        if (compoundsCoverageComponents.containsKey(compoundName)) {
                            compoundsCoverageComponents.get(compoundName)[0] = gridDT;
                        } else {
                            GridDatatype[] compoundArray = new GridDatatype[2];
                            compoundArray[0] = gridDT;
                            compoundsCoverageComponents.put(compoundName, compoundArray);
                        }
                    } else if (name.contains("northward")) {
                        String compoundName = name.replaceFirst("northward_", "");
                        if (compoundsCoverageComponents.containsKey(compoundName)) {
                            compoundsCoverageComponents.get(compoundName)[1] = gridDT;
                        } else {
                            GridDatatype[] compoundArray = new GridDatatype[2];
                            compoundArray[1] = gridDT;
                            compoundsCoverageComponents.put(compoundName, compoundArray);
                        }
                    }
                    
                    if(id2GridSeriesFeature.containsKey(id)){
                        ((NcGridSeriesFeature)id2GridSeriesFeature.get(id)).mergeGrid(gridDT, hGrid, vAxis, tAxis);
                    } else {
                        NcGridSeriesFeature feature = new NcGridSeriesFeature(gridDT, hGrid, vAxis, tAxis, this, dataReadingStrategy);
                        id2GridSeriesFeature.put(id, feature);
                    }
                }

                for (String compoundVar : compoundsCoverageComponents.keySet()) {
                    GridDatatype[] gridDTList = compoundsCoverageComponents.get(compoundVar);
                    if (gridDTList.length != 2 || gridDTList[0] == null || gridDTList[1] == null) {
                        throw new UnsupportedOperationException("Can only make 2 variables into a compound var");
                    }
                    GridDatatype gridX = gridDTList[0];
                    GridDatatype gridY = gridDTList[1];
                    VariableDS varX = gridX.getVariable();
                    VariableDS varY = gridY.getVariable();
                    String id = varX.getName() + varY.getName();
                    String xDesc = varX.getDescription();
                    int xIndex = xDesc.indexOf("-component of");
                    String description = xDesc.substring(xIndex + 14);
                    NcGridSeriesCoverage covX = new NcGridSeriesCoverage(varX, hGrid, vAxis, tAxis);
                    NcGridSeriesCoverage covY = new NcGridSeriesCoverage(varY, hGrid, vAxis, tAxis);

                    /*
                     * TODO we need to aggregate variables with different time values into the same coverage
                     */
                    if(id2GridSeriesFeature.containsKey(id)){
                        ((NcVectorGridSeriesFeature)id2GridSeriesFeature.get(id)).mergeGrids(gridX, gridY, hGrid, vAxis, tAxis);
                    } else {
                        try{
                            GridSeriesCoverage<Vector2D<Float>> coverage = new NcVectorGridSeriesCoverage(
                                    covX, covY);
                            GridSeriesFeature<Vector2D<Float>> feature = new NcVectorGridSeriesFeature(
                                    compoundVar, id, description, this, coverage, dataReadingStrategy,
                                    gridX, gridY);
                            id2GridSeriesFeature.put(id, feature);
                        } catch (InstantiationException e) {
                            /*
                             * If we get this error, it means that the components do
                             * not match properly, and can't make a Vector coverage.
                             */
                            // TODO log the error
                        }
                    }
                }
            }
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
     * @return a {@link NetcdfDataset} object for accessing the data at the
     *         given location.
     * @throws IOException
     *             if there was an error reading from the data source.
     */
    private static NetcdfDataset openDataset(String location) throws IOException {
        NetcdfDataset nc;
        if (location.endsWith(".xml") || location.endsWith(".ncml")) {
            // We use the cache of NetcdfDatasets to read NcML aggregations
            // as they can be time-consuming to put together. If the underlying
            // data can change we rely on the server admin setting the
            // "recheckEvery" parameter in the aggregation file.
            nc = NetcdfDataset.acquireDataset(location, null);
        } else {
            // For local single files and OPeNDAP datasets we don't use the
            // cache, to ensure that we are always reading the most up-to-date
            // data. There is a small possibility that the dataset cache will
            // have swallowed up all available file handles, in which case
            // the server admin will need to increase the number of available
            // handles on the server.
            nc = NetcdfDataset.openDataset(location);
        }
        return nc;
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
        NcGridSeriesFeatureCollection nc = new NcGridSeriesFeatureCollection("testId", "testName", "/home/guy/MIPe2e/makassar/*.nc");
        for(String feature:nc.getFeatureIds())
            System.out.println(feature);
        NcGridSeriesFeature feature = (NcGridSeriesFeature) nc.getFeatureById("sozowind");
        for(TimePosition tp:feature.getCoverage().getDomain().getTimeAxis().getCoordinateValues()){
            System.out.println(tp);
        }
    }
}
