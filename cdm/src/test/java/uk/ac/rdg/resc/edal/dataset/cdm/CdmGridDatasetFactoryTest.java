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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.cdm.CreateNetCDF;
import uk.ac.rdg.resc.edal.dataset.GridDataset;
import uk.ac.rdg.resc.edal.dataset.plugins.VectorPlugin;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
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
         * The test NetCDF dataset is 4D, and contains 4 variables:
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
        dataset = datasetFactory.createDataset("testdataset", location);
        /*
         * We also add a vector plugin, which will generate magnitudes and
         * directions
         */
        dataset.addVariablePlugin(new VectorPlugin("vLon", "vLat", "Test Vector Field"));
        xSize = 36;
        ySize = 19;
        /*
         * This grid shares the same domain as the source data file
         */
        hGrid = new RegularGridImpl(-180.5, -90.5, 179.5, 90.5, DefaultGeographicCRS.WGS84, xSize,
                ySize);
    }

    @Test
    public void testCorrectData() throws DataReadingException {
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

                /* Derived vars */
                Array2D magValues = mapData.getValues("vLonvLat-mag");
                Array2D dirValues = mapData.getValues("vLonvLat-dir");

                assertArrayEquals(lonValues.getShape(), new int[] { ySize, xSize });
                assertArrayEquals(latValues.getShape(), new int[] { ySize, xSize });
                assertArrayEquals(depthValues.getShape(), new int[] { ySize, xSize });
                assertArrayEquals(timeValues.getShape(), new int[] { ySize, xSize });
                assertArrayEquals(magValues.getShape(), new int[] { ySize, xSize });
                assertArrayEquals(dirValues.getShape(), new int[] { ySize, xSize });
                /*
                 * All arrays should be the same shape, so we can use any of
                 * them
                 */
                for (int i = 0; i < ySize; i++) {
                    float expectedLat = 100f * i / (ySize - 1);
                    for (int j = 0; j < xSize; j++) {
                        float expectedLon = 100f * j / (xSize - 1);

                        double expectedMag = Math.sqrt(expectedLat * expectedLat + expectedLon
                                * expectedLon);
                        double expectedDir = Math.atan2(expectedLat, expectedLon);
                        /*
                         * NetCDF stores these as floats, so 1e-5 is about the
                         * right accuracy.
                         */
                        assertEquals(expectedLon, lonValues.get(i, j).doubleValue(), 1e-5);
                        assertEquals(expectedLat, latValues.get(i, j).doubleValue(), 1e-5);
                        assertEquals(expectedDepth, depthValues.get(i, j).doubleValue(), 1e-5);
                        assertEquals(expectedTime, timeValues.get(i, j).doubleValue(), 1e-5);
                        assertEquals(expectedMag, magValues.get(i, j).doubleValue(), 1e-5);
                        assertEquals(expectedDir, dirValues.get(i, j).doubleValue(), 1e-5);
                    }
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsExceptionForInvalidZ() throws DataReadingException {
        /*
         * The z-value is invalid
         */
        @SuppressWarnings("unused")
        MapFeature mapData = dataset.readMapData(null, hGrid, 999., new DateTime(2000, 01, 01, 00,
                00));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsExceptionForInvalidTime() throws DataReadingException {
        /*
         * The time-value is invalid
         */
        @SuppressWarnings("unused")
        MapFeature mapData = dataset.readMapData(null, hGrid, 0.0, new DateTime(1999, 01, 01, 00,
                00));
    }

    @Test
    public void testVariableMetadataTreeStructure() {
        Set<VariableMetadata> topLevelVariables = dataset.getTopLevelVariables();
        /*
         * This should contain vDepth, vTime, and vLonvLat-group
         * 
         * vLonvLat-group should then contain:
         * 
         * vLat vLon vLonvLatmag vLonvLatdir
         */
        assertEquals(3, topLevelVariables.size());
        VariableMetadata depthMetadata = null;
        VariableMetadata timeMetadata = null;
        VariableMetadata lonLatGroupMetadata = null;
        for (VariableMetadata topMetadata : topLevelVariables) {
            if (topMetadata.getId().equals("vDepth")) {
                depthMetadata = topMetadata;
            }
            if (topMetadata.getId().equals("vTime")) {
                timeMetadata = topMetadata;
            }
            if (topMetadata.getId().equals("vLonvLat-group")) {
                lonLatGroupMetadata = topMetadata;
            }
        }
        assertNotNull(depthMetadata);
        assertNotNull(timeMetadata);
        assertNotNull(lonLatGroupMetadata);

        Set<VariableMetadata> emptyMetadata = Collections.emptySet();
        assertEquals(emptyMetadata, depthMetadata.getChildren());
        assertEquals(emptyMetadata, timeMetadata.getChildren());

        assertEquals(4, lonLatGroupMetadata.getChildren().size());
        VariableMetadata lonMetadata = null;
        VariableMetadata latMetadata = null;
        VariableMetadata magMetadata = null;
        VariableMetadata dirMetadata = null;
        for (VariableMetadata childMetadata : lonLatGroupMetadata.getChildren()) {
            if (childMetadata.getId().equals("vLon")) {
                lonMetadata = childMetadata;
            }
            if (childMetadata.getId().equals("vLat")) {
                latMetadata = childMetadata;
            }
            if (childMetadata.getId().equals("vLonvLat-mag")) {
                magMetadata = childMetadata;
            }
            if (childMetadata.getId().equals("vLonvLat-dir")) {
                dirMetadata = childMetadata;
            }
        }
        assertNotNull(latMetadata);
        assertNotNull(lonMetadata);
        assertNotNull(magMetadata);
        assertNotNull(dirMetadata);
    }
}
