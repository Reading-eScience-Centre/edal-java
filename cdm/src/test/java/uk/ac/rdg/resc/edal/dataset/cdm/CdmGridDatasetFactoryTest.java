package uk.ac.rdg.resc.edal.dataset.cdm;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.cdm.CreateNetCDF;
import uk.ac.rdg.resc.edal.dataset.GridDataset;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.util.Array2D;

/**
 * These tests perform various operations on a test dataset (originally
 * generated from {@link CreateNetCDF}). These involve reading map data from the
 * NetCDF file and checking that the values obtained are those expected
 * 
 * @author Guy
 */
public class CdmGridDatasetFactoryTest {

    private GridDataset dataset;
    private int xSize;
    private int ySize;
    private HorizontalGrid hGrid;

    @Before
    public void setup() throws IOException {
        /*-
         * The test dataset is 4D, and contains 4 variables:
         * 
         * vLon
         * vLat
         * vDepth
         * vTime
         * 
         * each of which covers [0,100] along one single dimension.
         * 
         * The latitude covers [-180,170] at a resolution of 10 degrees
         * The longitude covers [-90, 90] at a resolution of 10 degrees
         * The depth covers [0,100] at a resolution of 10m
         * The time covers 10 days starting from 01/01/2000 00:00:00
         */
        URL url = this.getClass().getResource("/test.nc");
        String location = url.getPath();
        CdmGridDatasetFactory datasetFactory = new CdmGridDatasetFactory();
        dataset = datasetFactory.createDataset(location);
        xSize = 36;
        ySize = 19;
        /*
         * This grid shares the same domain as the source data file
         */
        hGrid = new RegularGridImpl(-180.5, -90.5, 179.5, 90.5, DefaultGeographicCRS.WGS84, xSize,
                ySize);
    }

    @Test
    public void testCorrectData() throws IOException {
        /*- 
         * This test covers:
         * Reading the data from the location
         * Extracting each variable at every depth and time onto its native grid
         * Checking that each variable has the expected value
         */
        for (Double zPos = 0.0; zPos <= 100; zPos += 10.0) {
            double expectedDepth = zPos;
            for (int daysFromStart = 0; daysFromStart < 10; daysFromStart++) {
                DateTime time = new DateTime(2000, 01, 01 + daysFromStart, 00, 00);
                float expectedTime = 100 * daysFromStart / 9.0f;

                MapFeature mapData = dataset.readMapData(null, hGrid, zPos, time);

                Array2D lonValues = mapData.getValues("vLon");
                Array2D latValues = mapData.getValues("vLat");
                Array2D depthValues = mapData.getValues("vDepth");
                Array2D timeValues = mapData.getValues("vTime");
                assertArrayEquals(lonValues.getShape(), new int[] { ySize, xSize });
                assertArrayEquals(latValues.getShape(), new int[] { ySize, xSize });
                assertArrayEquals(depthValues.getShape(), new int[] { ySize, xSize });
                assertArrayEquals(timeValues.getShape(), new int[] { ySize, xSize });
                /*
                 * All arrays should be the same shape, so we can use any of
                 * them
                 */
                for (int i = 0; i < ySize; i++) {
                    float expectedLat = 100f * i / (ySize - 1);
                    for (int j = 0; j < xSize; j++) {
                        float expectedLon = 100f * j / (xSize - 1);
                        /*
                         * NetCDF stores these as floats, so 1e-5 is about the
                         * right accuracy.
                         */
                        assertEquals(expectedLon, lonValues.get(i, j).doubleValue(), 1e-5);
                        assertEquals(expectedLat, latValues.get(i, j).doubleValue(), 1e-5);
                        assertEquals(expectedDepth, depthValues.get(i, j).doubleValue(), 1e-5);
                        assertEquals(expectedTime, timeValues.get(i, j).doubleValue(), 1e-5);
                    }
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsExceptionForInvalidZ() throws IOException {
        /*
         * The z-value is invalid
         */
        @SuppressWarnings("unused")
        MapFeature mapData = dataset.readMapData(null, hGrid, 999., new DateTime(2000, 01, 01, 00,
                00));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsExceptionForInvalidTime() throws IOException {
        /*
         * The time-value is invalid
         */
        @SuppressWarnings("unused")
        MapFeature mapData = dataset.readMapData(null, hGrid, 0.0, new DateTime(1999, 01, 01, 00,
                00));
    }

}
