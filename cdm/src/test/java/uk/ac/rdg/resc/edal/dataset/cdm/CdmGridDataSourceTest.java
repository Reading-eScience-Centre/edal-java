package uk.ac.rdg.resc.edal.dataset.cdm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import ucar.nc2.dataset.NetcdfDataset;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.util.Array4D;

public class CdmGridDataSourceTest {
    private CdmGridDataSource datasource;
    private static final double delta = 1e-5;

    @Before
    public void setUp() throws Exception {
        URL url = this.getClass().getResource("/rectilinear_test_data.nc");
        String location = url.getPath();
        NetcdfDataset nc = NetcdfDatasetAggregator.getDataset(location);
        datasource = new CdmGridDataSource(nc);
    }

    @Test
    public void readTest() throws IOException, DataReadingException {
        int tmin = 0;
        int tmax = 9;
        int xmin = 0;
        int xmax = 35;
        int ymin = 0;
        int ymax = 18;
        int zmin = 0;
        int zmax = 10;

        Array4D<Number> lonResults = datasource.read("vLon", tmin, tmax, zmin, zmax, ymin, ymax,
                xmin, xmax);
        Array4D<Number> latResults = datasource.read("vLat", tmin, tmax, zmin, zmax, ymin, ymax,
                xmin, xmax);
        Array4D<Number> depthResults = datasource.read("vDepth", tmin, tmax, zmin, zmax, ymin,
                ymax, xmin, xmax);
        Array4D<Number> timeResults = datasource.read("vTime", tmin, tmax, zmin, zmax, ymin, ymax,
                xmin, xmax);

        for (int i = 0; i < tmax; i++) {
            float expectedTime = 100 * i / 9.0f;

            for (int j = 0; j < zmax; j++) {
                float expectedDepth = 10.0f * j;

                for (int k = 0; k < ymax; k++) {
                    float expectedLat = 100.0f * k / ymax;

                    for (int m = 0; m < xmax; m++) {
                        float expectedLon = 100.0f * m / xmax;
                        assertEquals(expectedTime, timeResults.get(i, j, k, m).floatValue(), delta);
                        assertEquals(expectedDepth, depthResults.get(i, j, k, m).floatValue(),
                                delta);
                        assertEquals(expectedLat, latResults.get(i, j, k, m).floatValue(), delta);
                        assertEquals(expectedLon, lonResults.get(i, j, k, m).floatValue(), delta);
                    }
                }
            }
        }
        datasource.close();
    }

    @Test
    public void testReadVariableWithOffset() throws IOException, DataReadingException {
        URL url = this.getClass().getResource("/testOffset.nc");
        NetcdfDataset nc = NetcdfDatasetAggregator.getDataset(url.getPath());

        try (CdmGridDataSource datasource = new CdmGridDataSource(nc)) {
            float[] expectedValues = new float[] {0f, 1.25f, 2.5f, 3.75f, 5f, 6.25f, 7.5f, 8.75f, 10f};

            checkVariable(datasource, "variableWithOffset", expectedValues);
            checkVariable(datasource, "variableWithoutOffset", expectedValues);
        }
    }

    @Test
    public void testReadUnisgnedVariables() throws IOException, DataReadingException {
        URL url = this.getClass().getResource("/testUnsignedTypes.nc");
        NetcdfDataset nc = NetcdfDatasetAggregator.getDataset(url.getPath());

        try (CdmGridDataSource datasource = new CdmGridDataSource(nc)) {
            float[] expectedValues = new float[]{0, 1, 2, 3, 4, 5, 6, 7, 8};

            checkVariable(datasource, "variableUbyte", expectedValues);
            checkVariable(datasource, "variableUshort", expectedValues);
            checkVariable(datasource, "variableUint", expectedValues);
        }
    }

    private void checkVariable(CdmGridDataSource datasource, String variableName, float[] expectedValues)
            throws IOException {
        int xmin = 0;
        int xmax = 2;
        int ymin = 0;
        int ymax = 2;

        Array4D<Number> variable = datasource.read(variableName, 0, 0, 0, 0, ymin, ymax, xmin, xmax);
        int i = 0;

        for (Number number : variable) {
            assertNotNull(number);
            assertEquals(expectedValues[i++], number.floatValue(), delta);
        }
    }
}
