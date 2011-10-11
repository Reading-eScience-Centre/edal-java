package uk.ac.rdg.resc.edal.cdm.feature;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.GridDataset.Gridset;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import uk.ac.rdg.resc.edal.cdm.DataReadingStrategy;
import uk.ac.rdg.resc.edal.cdm.coverage.NcGridSeriesCoverage;
import uk.ac.rdg.resc.edal.cdm.util.CdmUtils;
import uk.ac.rdg.resc.edal.cdm.util.FileUtils;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;

/**
 * An implementation of {@link FeatureCollection} which contains
 * {@link GridSeriesFeature} objects which hold {@link Float} data.
 * 
 * @author Guy Griffiths
 * 
 */
public class NcGridSeriesFeatureCollection implements FeatureCollection<GridSeriesFeature<Float>> {

    private String collectionId;
    private String name;
    private Map<String, GridSeriesFeature<Float>> id2GridSeriesFeature;

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

        id2GridSeriesFeature = new HashMap<String, GridSeriesFeature<Float>>();

        List<File> files = FileUtils.expandGlobExpression(location);
        for (File file : files) {
            NetcdfDataset ncDataset = openDataset(file.getPath());

            FeatureDataset featureDS = FeatureDatasetFactoryManager.wrap(FeatureType.GRID, ncDataset, null, null);
            if (featureDS == null) {
                throw new IOException("No grid datasets found in file: " + file.getPath());
            }
            FeatureType fType = featureDS.getFeatureType();
            assert (fType == FeatureType.GRID);
            GridDataset gridDS = (GridDataset) featureDS;

            DataReadingStrategy dataReadingStrategy = CdmUtils.getOptimumDataReadingStrategy(ncDataset);
            
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
                    String id = var.getName();
                    String description = var.getDescription();

                    GridSeriesCoverage<Float> coverage = new NcGridSeriesCoverage(var, hGrid, vAxis, tAxis);
                    GridSeriesFeature<Float> feature = new NcGridSeriesFeature(name, id, description, this, coverage, dataReadingStrategy, gridDT);

                    id2GridSeriesFeature.put(id.toLowerCase(), feature);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        // NcGridSeriesFeatureCollection fs = new
        // NcGridSeriesFeatureCollection("testcollection", "Test Collection",
        // "/home/guy/Data/FOAM_ONE/FOAM_one.ncml");
        NcGridSeriesFeatureCollection fs = new NcGridSeriesFeatureCollection("testcollection", "Test Collection",
                "/home/guy/Data/FOAM_ONE/FOAM_20100130.0.nc");
        
        for(String fId : fs.getFeatureIds()){
//            fs.getFeatureById(fId).getCoverage().extractHorizontalLayer(0, 0);
            System.out.println(fs.getFeatureById(fId).getDescription()+","+fs.getFeatureById(fId).getName());
        }
        
        String var = "V";

        System.out.println(fs.getFeatureById(var).getCoverage().getRangeMetadata(null).getDescription());
        System.out.println(fs.getFeatureById(var).getCoverage().getRangeMetadata(null).getUnits().getUnitString());
        System.out.println(fs.getFeatureById(var).getCoverage().getRangeMetadata(null).getParameter().getStandardName());

        List<Float> vals = fs.getFeatureById(var).getCoverage().getValues();

        GridSeriesDomain domain = fs.getFeatureById(var).getCoverage().getDomain(); 
        int xSize = domain.getHorizontalGrid().getXAxis().size();
        int ySize = domain.getHorizontalGrid().getYAxis().size();
        int zSize = domain.getVerticalAxis().size();
        int tSize = domain.getTimeAxis().size();
        
        double err = 0.0;
        System.out.println(xSize + "," + ySize + "," + zSize + "," + tSize);
        for (int t = 0; t < tSize; t++) {
            for (int z = 0; z < zSize; z++) {
                for (int y = 0; y < ySize; y++) {
                    for (int x = 0; x < xSize; x++) {
                        Float evVal = fs.getFeatureById(var).getCoverage().evaluate(t, z, y, x);
                        int index = x + y * xSize + z * ySize * xSize + t * zSize * ySize * xSize;
                        Float vaVal = vals.get(index);

                        // System.out.println(x+","+y+","+z+","+t);
                        if (evVal.isNaN() && vaVal.isNaN()) {
                            // Ignore
                        } else {
                            // System.out.println(evVal + "," + vaVal);
                            if (evVal.isNaN() || vaVal.isNaN()) {
                                System.out.println("One is NaN, but the other isn't:" + evVal + "," + vaVal);
                            } else {
                                err += evVal - vaVal;
                            }
                        }
                    }
                }
            }
            System.out.println("Total error:" + err);
        }

        // NcGridSeriesFeatureCollection fs = new
        // NcGridSeriesFeatureCollection("testcollection", "Test Collection",
        // "/home/guy/Data/FOAM_ONE/*.nc");
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
    public GridSeriesFeature<Float> getFeatureById(String id) {
        return id2GridSeriesFeature.get(id.toLowerCase());
    }

    @Override
    public Set<String> getFeatureIds() {
        return id2GridSeriesFeature.keySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<GridSeriesFeature<Float>> getFeatureType() {
        // TODO check this with usage examples
        return (Class<GridSeriesFeature<Float>>) (Class<?>) GridSeriesFeature.class;
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
    public Iterator<GridSeriesFeature<Float>> iterator() {
        /*
         * We cannot simply use:
         * 
         * return id2GridSeriesFeature.values().iterator()
         * 
         * because this will be an iterator of the wrong type
         * 
         * TODO IS THIS STILL TRUE?
         */
        return new Iterator<GridSeriesFeature<Float>>() {
            @Override
            public boolean hasNext() {
                return id2GridSeriesFeature.values().iterator().hasNext();
            }

            @Override
            public GridSeriesFeature<Float> next() {
                return id2GridSeriesFeature.values().iterator().next();
            }

            @Override
            public void remove() {
                id2GridSeriesFeature.values().iterator().remove();
            }
        };
    }

}
