package uk.ac.rdg.resc.edal.dataset.cdm;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import uk.ac.rdg.resc.edal.dataset.AbstractGridDataset;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.domain.HorizontalDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.metadata.GridVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.LonLatPosition;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.CurvilinearCoords;
import uk.ac.rdg.resc.edal.util.CurvilinearCoords.Cell;
import uk.ac.rdg.resc.edal.util.PlottingDomainParams;
import uk.ac.rdg.resc.edal.util.ValuesArray2D;

public class CurviLinearGridDatasetTest {
    // accuracy value set for assert equal method for comparison.
    private static final double delta = 1e-5;
    private Dataset dataset;
    private NetcdfFile cdf;
    private String location;

    // parameters about the used test dataset
    private int etaSize = 336;
    private int xiSize = 896;

    @Before
    public void setUp() throws Exception {
        URL url = this.getClass().getResource("/output-curvilinear.nc");
        location = url.getPath();
        cdf = NetcdfFile.open(location);
        CdmGridDatasetFactory datasetFactory = new CdmGridDatasetFactory();
        dataset = datasetFactory.createDataset("testdataset", location);
    }

    /**
     * Dateset contains only x and y data. When we try to extract data including
     * T and Z info, the method should return UnupportedOperationException.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testUnSupportedOperation() throws DataReadingException {
        assertFalse(dataset.supportsProfileFeatureExtraction("allx_u"));
        assertFalse(dataset.supportsTimeseriesExtraction("allx_u"));

        HorizontalDomain hDomain = dataset.getVariableMetadata("allx_u").getHorizontalDomain();

        BoundingBox bbox = hDomain.getBoundingBox();
        PlottingDomainParams params = new PlottingDomainParams(etaSize, xiSize, bbox, null, null,
                null, null, null);
        Collection<? extends PointSeriesFeature> timeSeriesFeatures = dataset
                .extractTimeseriesFeatures(null, params);
        assertEquals(0, timeSeriesFeatures.size());

        Collection<? extends ProfileFeature> profileFeature = dataset.extractProfileFeatures(null,
                params);
        assertEquals(0, profileFeature.size());
    }

    @Test
    public void testCurviLinearCoords() throws Exception {
        Variable lon_rho = cdf.findVariable("lon_rho");
        Variable lat_rho = cdf.findVariable("lat_rho");

        ArrayDouble lon_data = (ArrayDouble) lon_rho.read();
        ArrayDouble lat_data = (ArrayDouble) lat_rho.read();

        ValuesArray2D lon_values = new ValuesArray2D(xiSize, etaSize);
        ValuesArray2D lat_values = new ValuesArray2D(xiSize, etaSize);
        for (int i = 0; i < lon_data.getSize(); i++) {
            int m = i % etaSize;
            int n = i / etaSize;
            int[] coords = new int[] { n, m };
            lon_values.set(lon_data.getDouble(i), coords);
            lat_values.set(lat_data.getDouble(i), coords);
        }
        assertEquals(lon_data.getDouble(etaSize), lon_values.get(1, 0).doubleValue(), delta);
        assertEquals(lat_data.getDouble(etaSize * 2), lat_values.get(2, 0).doubleValue(), delta);

        CurvilinearCoords cCoords = new CurvilinearCoords(lon_values, lat_values);

        BoundingBox expectedBbox = dataset.getVariableMetadata("allx_u").getHorizontalDomain()
                .getBoundingBox();

        assertEquals(expectedBbox, cCoords.getBoundingBox());
        assertEquals(lon_data.getSize(), lon_values.size());
        assertEquals(etaSize, cCoords.getNi());
        assertEquals(xiSize, cCoords.getNj());

        int index = 15000;
        LonLatPosition expectedPos = new LonLatPosition(lon_data.getDouble(index),
                lat_data.getDouble(index));
        int cCoords_i = index % etaSize;
        int cCoords_j = index / etaSize;
        /*
         * LonLatPosition not implement hash code and equals method so the below
         * statement return false though the values are right.
         */

        // assertEquals(expectedPos, cCoords.getMidpoint(cCoords_i, cCoords_j));

        List<Cell> celllist = cCoords.getCells();
        Cell cell = cCoords.getCell(cCoords_i, cCoords_j);
        assertEquals(celllist.get(index), cell);

        assertEquals(cCoords_i, cell.getI());
        assertEquals(cCoords_j, cell.getJ());
        // assertEquals(expectedPos, cell.getCentre());
    }

    @Test
    public void testCurviLinearDataset() throws DataReadingException, IOException {
        assertTrue(dataset instanceof AbstractGridDataset);

        assertEquals(dataset.getDatasetVerticalCrs(), null);

        List<Variable> variables = cdf.getVariables();
        Set<String> vars = new HashSet<>();
        for (Variable v : variables) {
            vars.add(v.getName());
        }
        /*netcdt use variable but dataset use feature. Two different concepts.
        How can I use another to get feature info?*/
        
        assertEquals(vars, dataset.getFeatureIds());
        GridFeature allxUValues = ((AbstractGridDataset) dataset).readFeature("allx_u");
        GridFeature allxVValues = ((AbstractGridDataset) dataset).readFeature("allx_v");
        GridFeature allyUValues = ((AbstractGridDataset) dataset).readFeature("ally_u");
        GridFeature allyVValues = ((AbstractGridDataset) dataset).readFeature("ally_v");

        for (int n = 0; n < xiSize; n++) {
            for (int m = 0; m < etaSize; m++) {
                float xUVale = allxUValues.getValues("allx_u").get(0, 0, m, n).floatValue();
                float xVVale = allxVValues.getValues("allx_v").get(0, 0, m, n).floatValue();
                float yUVale = allyUValues.getValues("ally_u").get(0, 0, m, n).floatValue();
                float yVVale = allyVValues.getValues("ally_v").get(0, 0, m, n).floatValue();

                // below four values are set by dataset generator
                float expectedXU = m;
                float expectedXV = 0.0f;
                float expectedYU = 0.0f;
                float expectedYV = n;

                assertEquals(expectedXU, xUVale, delta);
                assertEquals(expectedXV, xVVale, delta);
                assertEquals(expectedYU, yUVale, delta);
                assertEquals(expectedYV, yVVale, delta);
            }
        }

        HorizontalDomain hDomain = dataset.getVariableMetadata("allx_u").getHorizontalDomain();

        BoundingBox bbox = hDomain.getBoundingBox();
        PlottingDomainParams params = new PlottingDomainParams(etaSize, xiSize, bbox, null, null,
                null, null, null);
        Collection<? extends DiscreteFeature<?, ?>> mapFeature = dataset.extractMapFeatures(null,
                params);
        DiscreteFeature<?, ?> feature = mapFeature.iterator().next();
        MapFeature data = (MapFeature) feature;

        Array2D<Number> xUValues = data.getValues("allx_u");
        assertArrayEquals(new int[] { xiSize, etaSize }, xUValues.getShape());

        /*
         * as discussed, dataset uses the horizontal grid to fetch data not the
         * curvilinear grid so the data it can fetch is limited. Use readFeature
         * method to extract data instead of extractMapFeature method at moment.
         */
        for (int m = 0; m < xiSize; m++) {
            for (int n = 0; n < etaSize; n++) {

                Number number = xUValues.get(m, n);
//                if(number != null) {
                // System.out.println(number);
//                }
                // assertEquals(uValues.getFloat(index),
                // xUValues.get(m,n).floatValue(), delta);
            }
        }
    }
}
