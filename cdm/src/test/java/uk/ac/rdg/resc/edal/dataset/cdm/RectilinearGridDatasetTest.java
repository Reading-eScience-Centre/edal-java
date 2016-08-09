/*******************************************************************************
 * Copyright (c) 2014 The University of Reading
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.dataset.DataSource;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.HorizontallyDiscreteDataset;
import uk.ac.rdg.resc.edal.dataset.plugins.VectorPlugin;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.MapDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.exceptions.IncorrectDomainException;
import uk.ac.rdg.resc.edal.exceptions.VariableNotFoundException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.grid.RegularGrid;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Array4D;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;

/**
 * This test class mainly is for {@link RectiLinearGridDataset} and its ancestor
 * {@link Dataset}. In addition, the {@link VectorPlugin} class is tested as the
 * netCDF library is available in this package.
 *
 * TODO This needs rewriting from scratch. It is badly commented and very
 * difficult to follow.
 *
 * @author Nan Lin
 * */
public class RectilinearGridDatasetTest {
    // the comparison accuracy parameter used by assert equal statement
    private static final double delta = 1e-5;

    private HorizontallyDiscreteDataset<? extends DataSource> dataset;

    /*
     * The below four parameters are about the used test dataset. they're the
     * actual size of rectilinear grid.
     */
    private int xSize = 36;
    private int ySize = 19;
    private int tSize = 10;
    private int zSize = 11;

    private RegularGrid rGrid;
    private CoordinateReferenceSystem crs = GISUtils.defaultGeographicCRS();
    private Chronology chronology;
    private VerticalCrs vCrs;
    private Extent<DateTime> datasetTExtent;
    private Extent<Double> datasetZExtent;
    private TimeAxis tAxis;
//    private VerticalAxis vAxis;

    /*
     * Not every cell in the grid be tested, only a few on the edges and near
     * the centre are chosen and stored in an arraylist below.
     */
    private ArrayList<GridCell2D> samplePoints = new ArrayList<>();

    /**
     * Initialize the testing environment. First, read the data in. Then create
     * a rectilinear grid object. Next pick up points in the grid as the
     * sampling points. Finally, initialize values for T and Z axis.
     *
     * @throws IOException
     *             if there is a problem when creating the dataset
     * @throws EdalException
     *             if there is a problem when creating the dataset
     * */
    @Before
    public void setUp() throws IOException, EdalException {
        URL url = this.getClass().getResource("/rectilinear_test_data.nc");
        String location = url.getPath();
        CdmGridDatasetFactory datasetFactory = new CdmGridDatasetFactory();
        dataset = datasetFactory.createDataset("testdataset", location);

        /*
         * Set up the grid exactly identical as the test dataset. these values
         * stand for bounding box of the grid.
         */
        rGrid = new RegularGridImpl(-185.0, -95.0, 175.0, 95.0, crs, xSize, ySize);

        /*
         * Initialize the sampling points array. They're on the edges and one in
         * the centre.
         */
        for (GridCell2D cell : rGrid.getDomainObjects()) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();
            // choosing some point on the edges and one in the centre.
            if (xIndex == 0 || xIndex == 15 || xIndex == 35) {
                if (yIndex == 0 || yIndex == 10 || yIndex == 19) {
                    samplePoints.add(cell);
                }
            }
        }
        // m stands for meter
        vCrs = new VerticalCrsImpl("m", false, false, false);
        chronology = ISOChronology.getInstanceUTC();
        DateTime start = new DateTime(2000, 01, 01, 00, 00, chronology);
        DateTime end = new DateTime(2000, 01, 10, 00, 00, chronology);
        datasetTExtent = Extents.newExtent(start, end);
        datasetZExtent = Extents.newExtent(0.0, 100.0);
        List<DateTime> tAxisValues = new ArrayList<>();
        List<Double> zAxisValues = new ArrayList<>();
        for (int i = 0; i < tSize; i++) {
            tAxisValues.add(new DateTime(2000, 01, 01 + i, 00, 00, chronology));
            zAxisValues.add(10.0 * i);
        }
        // add the last value of Z axis
        zAxisValues.add(100.0);
        tAxis = new TimeAxisImpl("time", tAxisValues);
//        vAxis = new VerticalAxisImpl("depth", zAxisValues, vCrs);
    }

    /*
     * Here is the convenience place to test {@link VectorPlugin} since the
     * dependency library netCDF is available here. The method {@link
     * VectorPlugin#processVariableMetadata} is called and then the method in
     * {@link Dataset#addVariablePlugin } is called. As the result, the dataset
     * generates a group of vector plugin of vector magnitude and direction and
     * their metadata. By comparing the results in Vector plugin object and
     * dataset object, we know if the VectorPlugin class exposes expected
     * behaviours.
     */

    /**
     * Test {@link VectorPlugin}.
     *
     * @throws DataReadingExcpetion
     *             if there is a problem reading the underlying data
     * @throws EdalException
     *             if there is a problem adding the plugin or the plugin process
     *             the metadata
     * @throws UnsupportedOperationException
     *             if not all of the requested variables have a time domain
     */
    @Test
    public void testVectorPlugin() throws DataReadingException, EdalException,
            UnsupportedOperationException {
        VectorPlugin vplugin = new VectorPlugin("vLon", "vLat", "Test Vector Field", true);
        dataset.addVariablePlugin(vplugin);

        // test on only one vertical position in order to save times
        double zPos = 20.0;

        for (GridCell2D cell : samplePoints) {
            HorizontalPosition hPos = cell.getCentre();
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();
            // one cell only so both xSize and ySize are 1.
            Collection<? extends PointSeriesFeature> timeSeriesFeatures = dataset
                    .extractTimeseriesFeatures(null, null, null, datasetTExtent, hPos, zPos);
            PointSeriesFeature data = timeSeriesFeatures.iterator().next();
            Array1D<Number> magValues = data.getValues("vLon:vLat-mag");
            Array1D<Number> dirValues = data.getValues("vLon:vLat-dir");

            // two values below are set by test dataset generator
            double expectedLon = 100.0f * xIndex / (xSize - 1);
            double expectedLat = 100.0f * yIndex / (ySize - 1);

            float expectedMag = (float) Math.hypot(expectedLon, expectedLat);
            float expectedDir = (float) (Math.toDegrees(Math.atan2(expectedLon, expectedLat)));
            // only choose one date in the datasetTExtent.
            int dateIndex = 6;
            assertEquals(expectedMag, magValues.get(dateIndex).floatValue(), delta);
            assertEquals(expectedDir, dirValues.get(dateIndex).floatValue(), delta);
        }
    }

    /**
     * Test some get methods (getDatasetChronology, getDatasetVerticalCrs,
     * getMapFeatureType, getVariableMetadata) in {@link Dataset} by evaluating
     * the metadata of the test dataset.
     *
     * @throws VariableNotFoundException
     **/
    @Test
    public void testMetadataInfo() throws VariableNotFoundException {
        assertEquals(MapFeature.class, dataset.getMapFeatureType("vLon"));

        VariableMetadata metadata = dataset.getVariableMetadata("vLon");

        assertEquals(vCrs, metadata.getVerticalDomain().getVerticalCrs());
        assertEquals(datasetZExtent, metadata.getVerticalDomain().getExtent());
        assertEquals(rGrid, metadata.getHorizontalDomain());
        assertEquals(datasetTExtent, metadata.getTemporalDomain().getExtent());
        assertTrue(metadata.isScalar());
    }

    /**
     * Test if the extracted TimeSeriesFeatures method return empty when
     * arguments in the constructor {@link PlottingDomainParams} are set out of
     * the domains of T and Z.
     *
     * @throws DataReadingExcpetion
     *             If there is a problem reading the underlying data
     * @throws UnsupportedOperationException
     *             If not all of the requested variables have a time domain
     * @throws VariableNotFoundException
     */
    @Test
    public void testReturnEmptyTimeSerieFeatures() throws DataReadingException,
            UnsupportedOperationException, VariableNotFoundException {
        DateTime start = new DateTime(1990, 01, 01, 00, 00, chronology);
        DateTime end = new DateTime(1998, 01, 01, 00, 00, chronology);
        Extent<DateTime> tExtent = Extents.newExtent(start, end);

        for (Double zPos = 0.0; zPos <= 100; zPos += 20.0) {
            for (GridCell2D cell : samplePoints) {
                HorizontalPosition hPos = cell.getCentre();
                // tExtent is out scope of dataset tExtent
                Collection<? extends PointSeriesFeature> timeSeriesFeatures = dataset
                        .extractTimeseriesFeatures(null, null, null, tExtent, hPos, zPos);
                // the size of an empty feature is ZERO.
                assertEquals(0, timeSeriesFeatures.size());
            }
        }

        start = new DateTime(2010, 01, 01, 00, 00, chronology);
        end = new DateTime(2012, 01, 01, 00, 00, chronology);
        tExtent = Extents.newExtent(start, end);
        for (Double zPos = 0.0; zPos <= 100; zPos += 30.0) {
            for (GridCell2D cell : samplePoints) {
                HorizontalPosition hPos = cell.getCentre();
                // tExtent is out scope of dataset tExtent
                Collection<? extends PointSeriesFeature> timeSeriesFeatures = dataset
                        .extractTimeseriesFeatures(null, null, null, tExtent, hPos, zPos);
                assertEquals(0, timeSeriesFeatures.size());
            }
        }

        start = new DateTime(2000, 1, 1, 00, 00, chronology);
        end = new DateTime(2000, 1, 10, 00, 00, chronology);
        tExtent = Extents.newExtent(start, end);
        Extent<Double> zExtent = Extents.newExtent(200.0, 500.0);
        for (GridCell2D cell : samplePoints) {
            HorizontalPosition hPos = cell.getCentre();
            // zExtent is out scope of dataset zExtent
            Collection<? extends PointSeriesFeature> timeSeriesFeatures = dataset
                    .extractTimeseriesFeatures(null, null, zExtent, tExtent, hPos, null);
            assertEquals(0, timeSeriesFeatures.size());
        }

        zExtent = Extents.newExtent(-200.0, -100.0);
        for (GridCell2D cell : samplePoints) {
            HorizontalPosition hPos = cell.getCentre();
            // zExtent is out scope of dataset zExtent
            Collection<? extends PointSeriesFeature> timeSeriesFeatures = dataset
                    .extractTimeseriesFeatures(null, null, zExtent, tExtent, hPos, null);
            assertEquals(0, timeSeriesFeatures.size());
        }
    }

    /**
     * Test if it throws IllegalArgumentException when the constructor of
     * {@link PlottingDomainParams} contains meaningless parameters.
     *
     * @throws VariableNotFoundException
     *
     * @throws DataReadingExcpetion
     *             If there is a problem reading the underlying data
     */
    @Test(expected = IncorrectDomainException.class)
    public void testMapFeaturesWithWrongParams() throws DataReadingException,
            VariableNotFoundException {
        // date value is out bound of tAxis
        DateTime tValue = new DateTime(2000, 11, 02, 15, 00, chronology);
        double zPos = 40.0;

        // just pick up one sampling point as we expect an exception
        int i = 5;

        GridCell2D cell = samplePoints.get(i);
        BoundingBox bbox = (BoundingBox) cell.getFootprint();
        // have to use bbox. replace it with null throw NullPointerExcpetion,
        Exception caughtEx = null;
        // the statement below catches an IllegalArgumentException
        try {
            dataset.extractMapFeatures(null, new MapDomain(bbox, 1, 1, zPos, tValue));
        } catch (VariableNotFoundException e) {
            caughtEx = e;
        }
        assertNotNull(caughtEx);
        // depth value is out bound of zAxis
        zPos = 140.0;
        tValue = new DateTime(2000, 01, 02, 15, 00, chronology);
        i = 3;
        cell = samplePoints.get(i);
        bbox = (BoundingBox) cell.getFootprint();
        // the statement below throws a VariableNotFoundException
        dataset.extractMapFeatures(null, new MapDomain(bbox, 1, 1, zPos, tValue));
    }

    /**
     * General test {@link Dataset#extractMapFeatures} method. When the values
     * for para of the constructor {@link PlottingDomainParams} are set
     * properly, it returns expected results.
     *
     * @throws VariableNotFoundException
     *
     * @throws DataReadingExcpetion
     *             If there is a problem reading the underlying data
     */
    @Test
    public void testMapFeatures() throws DataReadingException, VariableNotFoundException {
        // choose one date only
        DateTime tValue = new DateTime(2000, 01, 02, 15, 00, chronology);
        for (Double zPos = 0.0; zPos <= 100; zPos += 20.0) {
            for (GridCell2D cell : samplePoints) {
                GridCoordinates2D gCoordinate = cell.getGridCoordinates();
                int xIndex = gCoordinate.getX();
                int yIndex = gCoordinate.getY();
                BoundingBox bbox = (BoundingBox) cell.getFootprint();
                Collection<? extends DiscreteFeature<?, ?>> mapFeature = dataset
                        .extractMapFeatures(null, new MapDomain(bbox, 1, 1, zPos, tValue));
                // mapFeature responds to one Geo-position
                assertEquals(1, mapFeature.size());

                DiscreteFeature<?, ?> feature = mapFeature.iterator().next();
                MapFeature data = (MapFeature) feature;

                Array2D<Number> lonValues = data.getValues("vLon");
                Array2D<Number> latValues = data.getValues("vLat");
                Array2D<Number> depthValues = data.getValues("vDepth");
                Array2D<Number> timeValues = data.getValues("vTime");

                // for a cell, the shape of values at one Geo-position is (1,1)
                assertArrayEquals(new int[] { 1, 1 }, lonValues.getShape());
                assertArrayEquals(new int[] { 1, 1 }, latValues.getShape());
                assertArrayEquals(new int[] { 1, 1 }, depthValues.getShape());
                assertArrayEquals(new int[] { 1, 1 }, timeValues.getShape());

                /*
                 * below four expected values are set when we generate the
                 * responding values of the test dataset
                 */
                float expectedDepth = (float) zPos.doubleValue();
                float expectedLat = 100.0f * yIndex / (ySize - 1);
                float expectedTime = 100.0f * tAxis.findIndexOf(tValue) / (tSize - 1);
                float expectedLon = 100.0f * xIndex / (xSize - 1);

                assertEquals(expectedLon, lonValues.get(0, 0).floatValue(), delta);
                assertEquals(expectedLat, latValues.get(0, 0).floatValue(), delta);
                assertEquals(expectedDepth, depthValues.get(0, 0).floatValue(), delta);
                assertEquals(expectedTime, timeValues.get(0, 0).floatValue(), delta);
            }
        }
    }

    /**
     * General test {@link Dataset#extractTimeSeriesFeatures} method. When the
     * values for para of the constructor {@link PlottingDomainParams} are set
     * properly, it returns expected results.
     *
     * @throws VariableNotFoundException
     * @throws UnsupportedOperationException
     *
     * @throws DataReadingExcpetion
     *             If there is a problem reading the underlying data
     */
    @Test
    public void testTimeSerieFeatures() throws DataReadingException, UnsupportedOperationException,
            VariableNotFoundException {
        Double zPos = 30.0;

        /*
         * to fetch data at one depth position, BBOX must set to null. otherwise
         * all values inside BBOX be returned as if targetZ is ignored.
         */

        for (GridCell2D cell : samplePoints) {
            HorizontalPosition hPos = cell.getCentre();
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();
            Collection<? extends PointSeriesFeature> timeSeriesFeatures = dataset
                    .extractTimeseriesFeatures(null, null, null, datasetTExtent, hPos, zPos);
            assertEquals(1, timeSeriesFeatures.size());
            PointSeriesFeature data = timeSeriesFeatures.iterator().next();
            Array1D<Number> lonValues = data.getValues("vLon");
            Array1D<Number> latValues = data.getValues("vLat");
            Array1D<Number> depthValues = data.getValues("vDepth");
            Array1D<Number> timeValues = data.getValues("vTime");

            assertArrayEquals(new int[] { tSize }, lonValues.getShape());
            assertArrayEquals(new int[] { tSize }, latValues.getShape());
            assertArrayEquals(new int[] { tSize }, depthValues.getShape());
            assertArrayEquals(new int[] { tSize }, timeValues.getShape());

            /*
             * expectedLon and expectedLat are generated according to the
             * formulas given below.
             */
            float expectedLon = 100.0f * xIndex / (xSize - 1);
            float expectedLat = 100.0f * yIndex / (ySize - 1);
            float expectedDepth = (float) zPos.doubleValue();
            // not at every t value is tested in order to save times
            for (int k = 0; k < tSize; k += 2) {
                /*
                 * expectedTime is defined as the formula below
                 */
                float expectedTime = 100 * k / 9.0f;

                assertEquals(expectedLon, lonValues.get(k).floatValue(), delta);
                assertEquals(expectedLat, latValues.get(k).floatValue(), delta);
                assertEquals(expectedDepth, depthValues.get(k).floatValue(), delta);
                assertEquals(expectedTime, timeValues.get(k).floatValue(), delta);
            }
        }
    }

//    /**
//     * A helper method to generate {@link PlottingDomainParams} object by using
//     * a given zExtent object. The result is used to evaluating the
//     * {@link Dataset#extractTimeseriesFeatures} behaviour.
//     *
//     * @param zExtent
//     *            which is used as a para of the constructor of
//     *            {@link PlottingDomainParams}in the calling method
//     * @throws DataReadingException
//     *             If there is a problem reading the underlying data
//     * @throws VariableNotFoundException
//     * @throws UnsupportedOperationException
//     */
//    private void getFeatureByZExtent(Extent<Double> zExtent) throws DataReadingException, UnsupportedOperationException, VariableNotFoundException {
//        for (GridCell2D cell : samplePoints) {
//            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
//            int xIndex = gCoordinate.getX();
//            int yIndex = gCoordinate.getY();
//            HorizontalPosition hPos =cell.getCentre();
//            PlottingDomainParams params = new PlottingDomainParams(1, 1, null, zExtent,
//                    datasetTExtent, hPos, null, null);
//            Collection<? extends PointSeriesFeature> timeSeriesFeatures = dataset
//                    .extractTimeseriesFeatures(null, params);
//            /*
//             * if the given zExtent not intersect with dataset zExtent, it
//             * returns empty!
//             */
//            if (!zExtent.intersects(datasetZExtent)) {
//                // timeSeriesFeature should contain nothing
//                assertEquals(0, timeSeriesFeatures.size());
//            } else {
//                PointSeriesFeature data = (PointSeriesFeature) timeSeriesFeatures.iterator().next();
//                verifyTimeSeriesFeature(data, data.getHorizontalPosition(), xIndex, yIndex);
//            }
//        }
//    }
//
//    /**
//     * A helper method which is used to evaluate the returned TimeSeriesFeature
//     * object.
//     *
//     * @param data
//     *            the evaluated TimeSeriesFeature
//     * @param hPos
//     *            the centre of the grid cell
//     * @param xIndex
//     *            the x Index of the grid cell
//     * @param yIndex
//     *            the y Index of the grid cell
//     */
//    private void verifyTimeSeriesFeature(PointSeriesFeature data, HorizontalPosition hPos,
//            int xIndex, int yIndex) {
//        assertEquals(hPos, data.getHorizontalPosition());
//        Array1D<Number> lonValues = data.getValues("vLon");
//        Array1D<Number> latValues = data.getValues("vLat");
//        Array1D<Number> depthValues = data.getValues("vDepth");
//        Array1D<Number> timeValues = data.getValues("vTime");
//
//        assertArrayEquals(new int[] { tSize }, lonValues.getShape());
//        assertArrayEquals(new int[] { tSize }, latValues.getShape());
//        assertArrayEquals(new int[] { tSize }, depthValues.getShape());
//        assertArrayEquals(new int[] { tSize }, timeValues.getShape());
//
//        /*
//         * below three values are set when we generate the responding values of
//         * the test dataset
//         */
//        float expectedLon = 100.0f * xIndex / (xSize - 1);
//        float expectedLat = 100.0f * yIndex / (ySize - 1);
//        float expectedDepth = (float) data.getVerticalPosition().getZ();
//
//        for (int k = 0; k < tSize; k += 2) {
//            // the below value is set by test dataset
//            float expectedTime = 100 * k / 9.0f;
//
//            assertEquals(expectedLon, lonValues.get(k).floatValue(), delta);
//            assertEquals(expectedLat, latValues.get(k).floatValue(), delta);
//            assertEquals(expectedDepth, depthValues.get(k).floatValue(), delta);
//            assertEquals(expectedTime, timeValues.get(k).floatValue(), delta);
//        }
//    }
//
//    /**
//     * A helper method to generate {@link PlottingDomainParams} object by using
//     * a given targetZ object. The result is used to evaluate the returned
//     * {@link Dataset#extractTimeseriesFeatures} behaviour.
//     *
//     * @param targetZ
//     *            Which is used as a para of PlottingDomainParams object in the
//     *            calling method
//     * @throws DataReadingException
//     *             If there is a problem reading the underlying data
//     * @throws VariableNotFoundException
//     * @throws UnsupportedOperationException
//     */
//    private void getFeatureByTargetZ(double targetZ) throws DataReadingException, UnsupportedOperationException, VariableNotFoundException {
//        if (vAxis.contains(targetZ)) {
//            int zIndex = vAxis.findIndexOf(targetZ);
//            for (GridCell2D cell : samplePoints) {
//                HorizontalPosition hPos =cell.getCentre();
//                PlottingDomainParams params = new PlottingDomainParams(1, 1, null, null,
//                        datasetTExtent, hPos, targetZ, null);
//                Collection<? extends PointSeriesFeature> timeSeriesFeatures = dataset
//                        .extractTimeseriesFeatures(null, params);
//                for (PointSeriesFeature data : timeSeriesFeatures) {
//                    Array1D<Number> depthValues = data.getValues("vDepth");
//                    assertArrayEquals(new int[] { tSize }, depthValues.getShape());
//                    // the value below is set by the dataset
//                    float expectedDepth = 10.0f * zIndex;
//                    for (int k = 0; k < tSize; k += 2) {
//                        assertEquals(expectedDepth, depthValues.get(k).floatValue(), delta);
//                    }
//                }
//            }
//        } else {
//            for (GridCell2D cell : samplePoints) {
//                HorizontalPosition hPos =cell.getCentre();
//                PlottingDomainParams params = new PlottingDomainParams(1, 1, null, null,
//                        datasetTExtent, hPos, targetZ, null);
//                Collection<? extends PointSeriesFeature> timeSeriesFeatures = dataset
//                        .extractTimeseriesFeatures(null, params);
//                // targetZ is out bound of zExtent, it returns an empty feature
//                assertEquals(0, timeSeriesFeatures.size());
//            }
//        }
//    }
//
//    /**
//     * In this test, the para of zExtent and targetZ in the constructor
//     * {@link PlottingDomainParams} are set to various values in order to
//     * evaluating returned {@link TimeSeriesFeatures} objects.
//     * @throws VariableNotFoundException
//     * @throws UnsupportedOperationException
//     *
//     * @throws DataReadingExcpetion
//     *             If there is a problem reading the underlying data
//     */
//
//    @Test
//    public void testTimeSerieFeaturesPartofZExents() throws DataReadingException, UnsupportedOperationException, VariableNotFoundException {
//        // a zExtent is inside the zExtent of dataset.
//        Extent<Double> zExtent = Extents.newExtent(18.6, 53.4);
//        getFeatureByZExtent(zExtent);
//
//        // a zExtent is overlay the zExtent of dataset.
//        zExtent = Extents.newExtent(20.0, 153.4);
//        getFeatureByZExtent(zExtent);
//
//        // another zExtent is overlay the zExtent of dataset.
//        zExtent = Extents.newExtent(-120.6, 47.18);
//        getFeatureByZExtent(zExtent);
//
//        // a zExtent is outside the zExtent of dataset.
//        zExtent = Extents.newExtent(-18.6, -10.0);
//        getFeatureByZExtent(zExtent);
//
//        // a targetZ value is inside the zExtent of the dataset
//        double targetZ = 85.8;
//        getFeatureByTargetZ(targetZ);
//
//        // a targetZ value is outside the zExtent of the dataset
//        targetZ = 110.8; // vAxis.contains(targetZ) return false
//        getFeatureByTargetZ(targetZ);
//
//        targetZ = -20.8; // vAxis.contains(targetZ) return false
//        getFeatureByTargetZ(targetZ);
//    }

//    /**
//     * In this test, the para of tExtent and targetT in the constructor
//     * {@link PlottingDomainParams} are set to various values in order to
//     * evaluating returned {@link TimeSeriesFeatures} objects.
//     * @throws VariableNotFoundException
//     * @throws UnsupportedOperationException
//     *
//     * @throws DataReadingExcpetion
//     *             If there is a problem reading the underlying data
//     */
//    @Test
//    public void testTimeSerieFeaturesPartofTExents() throws DataReadingException, UnsupportedOperationException, VariableNotFoundException {
//        DateTime start = new DateTime(1999, 12, 25, 15, 00, chronology);
//        DateTime end = new DateTime(2000, 1, 8, 23, 00, chronology);
//
//        // a tExtent is intersected with the dataset tExent
//        Extent<DateTime> tExtent = Extents.newExtent(start, end);
//        extractTimeSeriesFeature(tExtent);
//
//        // another tExtent is intersected with the dataset tExent
//        start = new DateTime(2000, 1, 2, 00, 00, chronology);
//        end = new DateTime(2000, 1, 8, 00, 00, chronology);
//        tExtent = Extents.newExtent(start, end);
//        extractTimeSeriesFeature(tExtent);
//
//        // the tExtent is only one point
//        start = new DateTime(2000, 1, 3, 15, 00, chronology);
//        end = new DateTime(2000, 1, 3, 15, 00, chronology);
//        tExtent = Extents.newExtent(start, end);
//        extractTimeSeriesFeature(tExtent);
//
//        // another tExtent is intersected with the dataset tExent
//        start = new DateTime(2000, 1, 3, 15, 00, chronology);
//        end = new DateTime(2000, 5, 3, 15, 00, chronology);
//        tExtent = Extents.newExtent(start, end);
//        extractTimeSeriesFeature(tExtent);
//
//        // a tExtent is outside of the dataset tExent
//        start = new DateTime(2000, 1, 20, 15, 00, chronology);
//        end = new DateTime(2000, 5, 3, 15, 00, chronology);
//        tExtent = Extents.newExtent(start, end);
//        extractTimeSeriesFeature(tExtent);
//
//        // another tExtent is outside of the dataset tExent
//        start = new DateTime(1999, 10, 20, 15, 00, chronology);
//        end = new DateTime(1999, 12, 3, 23, 59, chronology);
//        tExtent = Extents.newExtent(start, end);
//        extractTimeSeriesFeature(tExtent);
//    }
//
//    /**
//     * A helper method is to evaluate TimeSeriesFeatures objects
//     *
//     * @param tExtent
//     *            the para of tExtent in the constructor of
//     *            {@link PlottingDomainParams} object
//     * @throws VariableNotFoundException
//     * @throws UnsupportedOperationException
//     * @throws DataReadingExcpetion
//     *             If there is a problem reading the underlying data
//     */
//    private void extractTimeSeriesFeature(Extent<DateTime> tExtent) throws DataReadingException, UnsupportedOperationException, VariableNotFoundException {
//        for (GridCell2D cell : samplePoints) {
//            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
//            int xIndex = gCoordinate.getX();
//            int yIndex = gCoordinate.getY();
//            HorizontalPosition hPos =cell.getCentre();
//            PlottingDomainParams params = new PlottingDomainParams(1, 1, null, datasetZExtent,
//                    tExtent, hPos, null, null);
//            Collection<? extends PointSeriesFeature> timeSeriesFeatures = dataset
//                    .extractTimeseriesFeatures(null, params);
//            if (tExtent == null || tExtent.intersects(datasetTExtent)) {
//                assertEquals(zSize, timeSeriesFeatures.size());
//                for (PointSeriesFeature feature : timeSeriesFeatures) {
//                    verifyTimeSeriesFeature(feature, hPos, xIndex, yIndex);
//                }
//            } else {
//                assertEquals(0, timeSeriesFeatures.size());
//            }
//        }
//    }

    /**
     * General test to extract profile features by calling
     * {@link Dataset#extraceProfileFeatures} method.
     *
     * @throws VariableNotFoundException
     * @throws UnsupportedOperationException
     *
     * @throws DataReadingExcpetion
     *             If there is a problem reading the underlying data
     */
//    @Test
//    public void testProfileFeatures() throws DataReadingException, UnsupportedOperationException, VariableNotFoundException {
//        DateTime dt = new DateTime(2000, 01, 05, 00, 00);
//        for (GridCell2D cell : samplePoints) {
//            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
//            int xIndex = gCoordinate.getX();
//            int yIndex = gCoordinate.getY();
//
//            HorizontalPosition hPos = cell.getCentre();
//            PlottingDomainParams params = new PlottingDomainParams(1, 1, null, datasetZExtent,
//                    null, hPos, null, dt);
//            Collection<? extends ProfileFeature> profileFeature = dataset.extractProfileFeatures(
//                    null, params);
//            assertEquals(1, profileFeature.size());
//            DiscreteFeature<?, ?> feature = profileFeature.iterator().next();
//            verifyProfileFeature((ProfileFeature) feature, hPos, xIndex, yIndex);
//        }
//    }

    /**
     * Test to extract features by setting the para of BoundingBox in
     * {@link Dataset} extraceXXXFeatures methods.
     *
     * @throws VariableNotFoundException
     * @throws UnsupportedOperationException
     *
     * @throws DataReadingExcpetion
     *             If there is a problem reading the underlying data
     */
    @Test
    public void testFeaturesBBox() throws DataReadingException, UnsupportedOperationException,
            VariableNotFoundException {
        BoundingBox bbox = new BoundingBoxImpl(-124.89, -20.9, 50.004, 25.0, crs);
        double xstep = (rGrid.getXAxis().getCoordinateExtent().getHigh() - rGrid.getXAxis()
                .getCoordinateExtent().getLow())
                / xSize;
        double ystep = (rGrid.getYAxis().getCoordinateExtent().getHigh() - rGrid.getYAxis()
                .getCoordinateExtent().getLow())
                / ySize;
        bboxTest(bbox, xstep, ystep);

        bbox = new BoundingBoxImpl(-134.9, -34.5, 0.8, -31.7, crs);
        bboxTest(bbox, xstep, ystep);

        bbox = new BoundingBoxImpl(-1000.0, -34.5, -759.334, 60.0, crs);
        bboxTest(bbox, xstep, ystep);

        bbox = new BoundingBoxImpl(-400.1, -0.3, 150.8, 58.0, crs);
        bboxTest(bbox, xstep, ystep);

        bbox = new BoundingBoxImpl(11.7, 12.0, 18.9, 70.0, crs);
        bboxTest(bbox, xstep, ystep);

        bbox = new BoundingBoxImpl(11.7, 105.9, 18.9, 190.0, crs);
        bboxTest(bbox, xstep, ystep);

        bbox = new BoundingBoxImpl(11.7, -200.9, 33.3, -140.0, crs);
        bboxTest(bbox, xstep, ystep);

        bbox = new BoundingBoxImpl(11.7, -200.9, 38.3, 10.0, crs);
        bboxTest(bbox, xstep, ystep);

        bbox = new BoundingBoxImpl(11.7, 30.8, 33.3, 150.0, crs);
        bboxTest(bbox, xstep, ystep);
    }

    /**
     * A helper method is to evaluate returned feature objects based on the
     * given bounding box objects.
     *
     * @param bbox
     *            the boundingbox
     * @param xstep
     *            the size of x side of every cell inside the bounding box of
     *            the dataset
     * @param ystep
     *            the size of x side of every cell inside the bounding box of
     *            the dataset
     * @throws VariableNotFoundException
     * @throws UnsupportedOperationException
     * @throws DataReadingExcpetion
     *             If there is a problem reading the underlying data
     */
    private void bboxTest(BoundingBox bbox, double xstep, double ystep)
            throws DataReadingException, UnsupportedOperationException, VariableNotFoundException {
        Collection<? extends ProfileFeature> profileFeatures = dataset.extractProfileFeatures(null,
                bbox, null, null, null, null);

        // y-side need to be considered since x-side can be wrapped up
        Extent<Double> bboxY = Extents.newExtent(bbox.getMinY(), bbox.getMaxY());
        double yLow;
        double yHigh;
        if (bboxY.intersects(rGrid.getYAxis().getCoordinateExtent())) {
            double datasetYLow = rGrid.getYAxis().getCoordinateExtent().getLow();
            if (datasetYLow > bbox.getMinY())
                yLow = datasetYLow;
            else
                yLow = bbox.getMinY();
            double datasetYHigh = rGrid.getYAxis().getCoordinateExtent().getHigh();
            if (datasetYHigh > bbox.getMaxY())
                yHigh = bbox.getMaxY();
            else
                yHigh = datasetYHigh;
            int numberOfYMidPoints = getNumberOfMidPoints(yLow, yHigh, ystep);
            if (numberOfYMidPoints < 0) {
                assertEquals(0, profileFeatures.size());
            } else {
                int numberOfXMidPoints = getNumberOfMidPoints(bbox.getMinX(), bbox.getMaxX(), xstep);
                if (numberOfXMidPoints < 0) {
                    assertEquals(0, profileFeatures.size());
                } else {
                    assertEquals(numberOfXMidPoints * numberOfYMidPoints * tSize,
                            profileFeatures.size());
                }
            }
        }
    }

    /**
     * This only works for lat/lon grid.
     *
     * Decide how many midpoints in a given distance extent
     *
     * @param low
     *            the low point of the given distance extent
     * @param high
     *            the high point of the given distance extent
     * @param step
     *            the step of the distance extent
     * @return: the number of the midpoints in the distance extent
     */
    private int getNumberOfMidPoints(double low, double high, double step) {
        // to deal with longitude values as they can be any values
        if (high - low > 360.0)
            return (int) (360.0 / step);
        int m = (int) Math.ceil(low / step);
        int n = (int) Math.floor(high / step);
        if (n < m)
            return -1;
        else
            return n - m + 1;
    }

    // remember for profile features, targetZ is ignored.
//    /**
//     * A helper method is to evaluate returned ProfileFeature objects based on
//     * given zExtent values.
//     *
//     * @param zExtent
//     *            the corresponding param of zExtent in PlottingDomainParams
//     *            object
//     * @throws VariableNotFoundException
//     * @throws UnsupportedOperationException
//     * @throws DataReadingExcpetion
//     *             If there is a problem reading the underlying data
//     */
//    private void zExtentCaseForProfileFeatures(Extent<Double> zExtent) throws DataReadingException, UnsupportedOperationException, VariableNotFoundException {
//        for (GridCell2D cell : samplePoints) {
//            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
//            int xIndex = gCoordinate.getX();
//            int yIndex = gCoordinate.getY();
//            HorizontalPosition hPos = cell.getCentre();
//            PlottingDomainParams params = new PlottingDomainParams(1, 1, null, zExtent, null, hPos,
//                    null, null);
//            Collection<? extends ProfileFeature> profileFeature = dataset.extractProfileFeatures(
//                    null, params);
//            if (zExtent == null || zExtent.intersects(datasetZExtent)) {
//                assertEquals(tSize, profileFeature.size());
//
//                for (ProfileFeature feature : profileFeature) {
//                    verifyProfileFeature(feature, hPos, xIndex, yIndex);
//                }
//            } else {
//                assertEquals(0, profileFeature.size());
//            }
//        }
//    }
//
//    /**
//     * Test {@link Dataset#extractProfileFeatures} method by setting various
//     * values for zExtent
//     * @throws VariableNotFoundException
//     * @throws UnsupportedOperationException
//     *
//     * @throws DataReadingExcpetion
//     *             If there is a problem reading the underlying data
//     */
//    @Test
//    public void testProfileFeaturesPartOfZExtent() throws DataReadingException, UnsupportedOperationException, VariableNotFoundException {
//        // test case 1
//        Extent<Double> zExtent = Extents.newExtent(1220.0, 4401.0);
//        zExtentCaseForProfileFeatures(zExtent);
//
//        /*
//         * test case 2, the given zExtent intersect with the one of the data set
//         * it should return all profile features the data set contains.
//         */
//
//        zExtent = Extents.newExtent(25.0, 300.0);
//        zExtentCaseForProfileFeatures(zExtent);
//
//        /*
//         * test case 3: a given zExtent is in the range of of the zExtent of the
//         * dataset, all profile features return.
//         */
//        zExtent = Extents.newExtent(38.0, 55.0);
//        zExtentCaseForProfileFeatures(zExtent);
//
//        /*
//         * test case 4: another example a given zExtent intersects with the
//         * zExtent of the data set.
//         */
//        zExtent = Extents.newExtent(-100.0, 95.0);
//        zExtentCaseForProfileFeatures(zExtent);
//
//        zExtent = Extents.newExtent(-100.0, -5.0);
//        zExtentCaseForProfileFeatures(zExtent);
//
//        zExtentCaseForProfileFeatures(null);
//        zExtentCaseForProfileFeatures(zExtent);
//    }

    /**
     * Test {@link Dataset#extractProfileFeatures} method by setting various
     * values of tExtent and targetT for the constructor
     * {@link PlottingDomainParams}.
     *
     * @throws VariableNotFoundException
     * @throws UnsupportedOperationException
     *
     * @throws DataReadingExcpetion
     *             If there is a problem reading the underlying data
     */
    @Test
    public void testProfileFeaturesPartOfTExtent() throws DataReadingException,
            UnsupportedOperationException, VariableNotFoundException {
        DateTime start = new DateTime(2000, 01, 01, 00, 00, chronology);
        DateTime end = start;
        Extent<DateTime> tExtent = Extents.newExtent(start, end);
        DateTime targetT = null;

        // tExent is one point, targetT is null
        tExentCaseForProfileFeatures(tExtent, null);

        // a tExent is overlay (left side) of the dataset tExent
        tExtent = Extents.newExtent(start.minusDays(100), end.plusDays(5));
        tExentCaseForProfileFeatures(tExtent, null);

        // a tExent includes the whole dataset tExent
        tExtent = Extents.newExtent(start.minusDays(100), end.plusDays(50));
        tExentCaseForProfileFeatures(tExtent, null);

        // a tExent is overlay (right side) of the dataset tExent
        tExtent = Extents.newExtent(start.plusDays(3), end.plusDays(500));
        tExentCaseForProfileFeatures(tExtent, null);

        // a targetT is exactly on the tAxis point
        targetT = new DateTime(2000, 01, 05, 00, 00, chronology);
        tExentCaseForProfileFeatures(tExtent, targetT);

        // a targetT is on the tAxis
        targetT = new DateTime(2000, 01, 05, 10, 50, chronology);
        tExentCaseForProfileFeatures(tExtent, targetT);

        // a targetT is outside of the tAxis
        targetT = new DateTime(1999, 01, 05, 10, 50, chronology);
        tExentCaseForProfileFeatures(tExtent, targetT);
    }

    /**
     * A helper method is to evaluate ProfileFeatures objects.
     *
     * @param tExtent
     *            the corresponding param tExtent in PlottingDomainParams
     * @param targetT
     *            the corresponding param targetT in PlottingDomainParams
     * @throws VariableNotFoundException
     * @throws UnsupportedOperationException
     * @throws DataReadingExcpetion
     *             If there is a problem reading the underlying data
     */
    private void tExentCaseForProfileFeatures(Extent<DateTime> tExtent, DateTime targetT)
            throws DataReadingException, UnsupportedOperationException, VariableNotFoundException {
        for (GridCell2D cell : samplePoints) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();
            HorizontalPosition hPos = cell.getCentre();
            Collection<? extends ProfileFeature> profileFeatures = dataset.extractProfileFeatures(
                    null, null, datasetZExtent, tExtent, hPos, targetT);
            if (tExtent == null && targetT == null) {
                assertEquals(tSize, profileFeatures.size());
                for (ProfileFeature feature : profileFeatures) {
                    verifyProfileFeature(feature, hPos, xIndex, yIndex);
                }
            } else if (tExtent == null && targetT != null) {
                if (tAxis.contains(targetT)) {
                    assertEquals(1, profileFeatures.size());
                    for (ProfileFeature feature : profileFeatures) {
                        verifyProfileFeature(feature, hPos, xIndex, yIndex);
                    }
                } else {
                    assertEquals(0, profileFeatures.size());
                }
            } else if (tExtent != null && tExtent.intersects(datasetTExtent)) {
                for (ProfileFeature feature : profileFeatures) {
                    verifyProfileFeature(feature, hPos, xIndex, yIndex);
                }
            } else if (tExtent != null && !tExtent.intersects(datasetTExtent)) {
                assertEquals(0, profileFeatures.size());
            }
        }

    }

    /**
     * A helper method to evaluate ProfileFeatures object.
     *
     * @param data
     *            the extracted Profilefeature
     * @param hPos
     *            the centre of the grid cell
     * @param xIndex
     *            the x Index of the grid cell
     * @param yIndex
     *            the y Index of the grid cell
     */
    private void verifyProfileFeature(ProfileFeature data, HorizontalPosition hPos, int xIndex,
            int yIndex) {
        Array1D<Number> lonValues = data.getValues("vLon");
        Array1D<Number> latValues = data.getValues("vLat");
        Array1D<Number> depthValues = data.getValues("vDepth");
        Array1D<Number> timeValues = data.getValues("vTime");

        assertArrayEquals(new int[] { zSize }, latValues.getShape());
        assertArrayEquals(new int[] { zSize }, depthValues.getShape());

        /*
         * below two values are used purposedly when we generate the responding
         * values of the test dataset
         */
        float expectedLon = 100.0f * xIndex / (xSize - 1);
        float expectedLat = 100.0f * yIndex / (ySize - 1);

        int dateIndex = tAxis.findIndexOf(data.getTime());
        float expectedTime = 100 * dateIndex / 9.0f;

        for (int k = 0; k < zSize; k += 2) {
            // the below value is used by the test dataset generator
            float expectedDepth = 10.0f * k;

            assertEquals(expectedLon, lonValues.get(k).floatValue(), delta);
            assertEquals(expectedLat, latValues.get(k).floatValue(), delta);
            assertEquals(expectedDepth, depthValues.get(k).floatValue(), delta);
            assertEquals(expectedTime, timeValues.get(k).floatValue(), delta);
        }
    }

    /**
     * Test {@link Dataset#extractProfileFeatures} method return empty when
     * values in the constructor of {@link PlottingDomainParams} are out of the
     * scopes of X, Y, Z, T domains.
     *
     * @throws VariableNotFoundException
     * @throws UnsupportedOperationException
     *
     * @throws DataReadingExcpetion
     *             If there is a problem reading the underlying data
     */

    @Test
    public void testReturnEmptyProfileFeatures() throws DataReadingException,
            UnsupportedOperationException, VariableNotFoundException {
        DateTime start = new DateTime(2011, 01, 15, 00, 00, chronology);
        DateTime end = new DateTime(2012, 02, 03, 00, 00, chronology);
        Extent<DateTime> tExtent = Extents.newExtent(start, end);
        Collection<? extends ProfileFeature> profileFeatures = dataset.extractProfileFeatures(null,
                null, datasetZExtent, tExtent, null, null);
        assertEquals(0, profileFeatures.size());

        start = new DateTime(1990, 01, 15, 00, 00, chronology);
        end = new DateTime(1998, 02, 03, 00, 00, chronology);
        tExtent = Extents.newExtent(start, end);
        profileFeatures = dataset.extractProfileFeatures(null, null, datasetZExtent, tExtent, null, null);
        assertEquals(0, profileFeatures.size());

        start = new DateTime(2000, 1, 1, 00, 00, chronology);
        end = new DateTime(2000, 1, 10, 00, 00, chronology);
        tExtent = Extents.newExtent(start, end);
        Extent<Double> zExtent = Extents.newExtent(200.0, 500.0);
        profileFeatures = dataset.extractProfileFeatures(null, null, zExtent, tExtent, null, null);
        assertEquals(0, profileFeatures.size());

        start = new DateTime(2000, 1, 1, 00, 00, chronology);
        end = new DateTime(2000, 1, 10, 00, 00, chronology);
        tExtent = Extents.newExtent(start, end);
        zExtent = Extents.newExtent(-200.0, -50.0);
        profileFeatures = dataset.extractProfileFeatures(null, null, zExtent, tExtent, null, null);
        assertEquals(0, profileFeatures.size());
    }

    /**
     * By testing {@link Dataset#readFeature}, two classes {@link GridFeature}
     * and {@link MapFeature} are tested.
     *
     * @throws VariableNotFoundException
     *
     * @throws DataReadingExcpetion
     *             If there is a problem reading the underlying data
     */
    @Test
    public void readFeatureTest() throws DataReadingException, VariableNotFoundException {
        assertTrue(dataset.readFeature("vLon") instanceof GridFeature);
        assertFalse(dataset.readFeature("vLat") instanceof PointSeriesFeature);
        assertFalse(dataset.readFeature("vDepth") instanceof ProfileFeature);
        assertFalse(dataset.readFeature("vTime") instanceof TrajectoryFeature);
        assertFalse(dataset.readFeature("vLat") instanceof MapFeature);

        GridFeature feature = (GridFeature) dataset.readFeature("vLon");
        Array4D<Number> values = feature.getValues("vLon");
        int Tsize = values.getTSize();
        int Zsize = values.getZSize();
        int Ysize = values.getYSize();
        int Xsize = values.getXSize();

        for (int i = 0; i < Tsize; i++) {
            for (int j = 0; j < Zsize; j++) {
                for (int k = 0; k < Ysize; k++) {
                    for (int m = 0; m < Xsize; m++) {
                        Number vLonValue = values.get(i, j, k, m);

                        // the below value is used by dataset generator
                        float expectedValue = 100.0f * m / (xSize - 1);

                        assertEquals(expectedValue, vLonValue.floatValue(), delta);
                    }
                }
            }
        }

        // The following test MapFeaure interface
        Set<String> ids = new HashSet<>();
        ids.add("vLon");

        // T and Z values are fixed
        double zValue = 60.0;
        DateTime tValue = new DateTime(2000, 01, 01, 00, 00, chronology);
        MapDomain mapdomain = new MapDomain(rGrid, zValue, vCrs, tValue);

        MapFeature mapfeature = feature.extractMapFeature(ids, rGrid, zValue, tValue);
        assertEquals(mapdomain, mapfeature.getDomain());
        assertEquals(zValue, mapfeature.getDomain().getZ().doubleValue(), delta);
        assertEquals(tValue, mapfeature.getDomain().getTime());

        for (int k = 0; k < Ysize; k++) {
            for (int m = 0; m < Xsize; m++) {
                // the below value is used by the test dataset generator
                float expectedLonValue = 100.0f * m / (xSize - 1);

                assertEquals(expectedLonValue, mapfeature.getValues("vLon").get(k, m).floatValue(),
                        delta);
            }
        }
    }
}
