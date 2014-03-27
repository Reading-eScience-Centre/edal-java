package uk.ac.rdg.resc.edal.dataset.cdm;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.GridDomain;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxisImpl;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.Array4D;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;
import uk.ac.rdg.resc.edal.util.PlottingDomainParams;

public class RectiLinearGridDatasetTest {
    private Dataset dataset;
    private int xSize = 36;
    private int ySize = 19;
    private int tSize = 10;
    private int zSize = 11;
    private RectilinearGrid rGrid;
    private CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
    private Chronology chrnology;
    private VerticalCrs vCrs;
    private Extent<DateTime> datasetTExtent;
    private Extent<Double> datasetZExtent;
    private double zStep = 10.0;
    private List<DateTime> tAxisValues = new ArrayList<>();
    private List<Double> zAxisValues = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        URL url = this.getClass().getResource("/rectilinear_test_data.nc");
        String location = url.getPath();
        CdmGridDatasetFactory datasetFactory = new CdmGridDatasetFactory();
        dataset = datasetFactory.createDataset("testdataset", location);
        rGrid = new RegularGridImpl(-185.0, -95.0, 175.0, 95.0, crs, xSize, ySize);
        vCrs = new VerticalCrsImpl("m", false, false, false);
        chrnology = ISOChronology.getInstance();
        DateTime start = new DateTime(2000, 01, 01, 00, 00, chrnology);
        DateTime end = new DateTime(2000, 01, 10, 00, 00, chrnology);
        datasetTExtent = Extents.newExtent(start, end);
        datasetZExtent = Extents.newExtent(0.0, 100.0);
        for (int i = 0; i < 10; i++) {
            tAxisValues.add(new DateTime(2000, 01, 01 + i, 00, 00, chrnology));
            zAxisValues.add(10.0 * i);
        }
        zAxisValues.add(100.0);
    }

    @Test
    public void testMetadataInfo() throws Exception {
        assertEquals(dataset.getDatasetChronology(), chrnology);
        assertEquals(dataset.getDatasetVerticalCrs(), vCrs);
        assertEquals(dataset.getMapFeatureType("vLon"), GridFeature.class);

        VariableMetadata metadata = dataset.getVariableMetadata("vLon");

        assertEquals(metadata.getVerticalDomain().getVerticalCrs(), vCrs);
        assertEquals(metadata.getVerticalDomain().getExtent(), datasetZExtent);
        assertEquals(metadata.getHorizontalDomain(), rGrid);
        assertEquals(metadata.getTemporalDomain().getExtent(), datasetTExtent);
        assertTrue(metadata.isScalar());
    }

    @Test
    public void testNullTimeSerieFeatures() throws Exception {
        Array<GridCell2D> cells = rGrid.getDomainObjects();
        DateTime start = new DateTime(1990, 01, 01, 00, 00, chrnology);
        DateTime end = new DateTime(1998, 01, 01, 00, 00, chrnology);
        Extent<DateTime> tExtent = Extents.newExtent(start, end);

        for (Double zPos = 0.0; zPos <= 100; zPos += 10.0) {
            for (GridCell2D cell : cells) {
                HorizontalPosition hPos = cell.getCentre();
                PlottingDomainParams params = new PlottingDomainParams(xSize, ySize,
                        rGrid.getBoundingBox(), null, tExtent, hPos, zPos, null);
                Collection<? extends PointSeriesFeature> timeSeriesFeature = dataset
                        .extractTimeseriesFeatures(null, params);
                // The collection object isn't a null object.
                for (PointSeriesFeature feature : timeSeriesFeature)
                    assertNull(feature);
            }
        }

        start = new DateTime(2010, 01, 01, 00, 00, chrnology);
        end = new DateTime(2012, 01, 01, 00, 00, chrnology);
        tExtent = Extents.newExtent(start, end);
        for (Double zPos = 0.0; zPos <= 100; zPos += 10.0) {
            for (GridCell2D cell : cells) {
                HorizontalPosition hPos = cell.getCentre();
                PlottingDomainParams params = new PlottingDomainParams(xSize, ySize,
                        rGrid.getBoundingBox(), null, tExtent, hPos, zPos, null);
                Collection<? extends PointSeriesFeature> timeSeriesFeature = dataset
                        .extractTimeseriesFeatures(null, params);
                for (PointSeriesFeature feature : timeSeriesFeature)
                    assertNull(feature);
            }
        }

        start = new DateTime(2000, 1, 1, 00, 00, chrnology);
        end = new DateTime(2000, 1, 10, 00, 00, chrnology);
        tExtent = Extents.newExtent(start, end);
        Extent<Double> zExtent = Extents.newExtent(200.0, 500.0);

        for (GridCell2D cell : cells) {
            HorizontalPosition hPos = cell.getCentre();
            PlottingDomainParams params = new PlottingDomainParams(xSize, ySize,
                    rGrid.getBoundingBox(), zExtent, tExtent, hPos, null, null);
            Collection<? extends PointSeriesFeature> timeSeriesFeature = dataset
                    .extractTimeseriesFeatures(null, params);
            for (PointSeriesFeature feature : timeSeriesFeature)
                assertNull(feature);
        }

        zExtent = Extents.newExtent(-200.0, -100.0);
        for (GridCell2D cell : cells) {
            HorizontalPosition hPos = cell.getCentre();
            PlottingDomainParams params = new PlottingDomainParams(xSize, ySize,
                    rGrid.getBoundingBox(), zExtent, tExtent, hPos, null, null);
            Collection<? extends PointSeriesFeature> timeSeriesFeature = dataset
                    .extractTimeseriesFeatures(null, params);
            for (PointSeriesFeature feature : timeSeriesFeature)
                assertNull(feature);
        }
    }

    @Test
    public void testTimeSerieFeatures() throws Exception {
        Array<GridCell2D> cells = rGrid.getDomainObjects();
        TimeAxis tAxis = new TimeAxisImpl("time", tAxisValues);

        for (Double zPos = 0.0; zPos <= 100; zPos += 10.0) {
            for (GridCell2D cell : cells) {
                GridCoordinates2D gCoordinate = cell.getGridCoordinates();
                int xIndex = gCoordinate.getX();
                int yIndex = gCoordinate.getY();
                HorizontalPosition hPos = cell.getCentre();
                PlottingDomainParams params = new PlottingDomainParams(xSize, ySize,
                        rGrid.getBoundingBox(), null, datasetTExtent, hPos, zPos, null);
                Collection<? extends PointSeriesFeature> timeSeriesFeature = dataset
                        .extractTimeseriesFeatures(null, params);
                assertEquals(timeSeriesFeature.size(), 1);
                DiscreteFeature<?, ?> feature = timeSeriesFeature.iterator().next();
                assertTrue(feature instanceof PointSeriesFeature);
                PointSeriesFeature data = (PointSeriesFeature) feature;

                assertEquals(data.getDomain(), tAxis);
                assertEquals(data.getHorizontalPosition(), hPos);
                assertEquals(data.getVerticalPosition().getZ(), zPos, 1e-5);
                Array1D<Number> lonValues = data.getValues("vLon");
                Array1D<Number> latValues = data.getValues("vLat");
                Array1D<Number> depthValues = data.getValues("vDepth");
                Array1D<Number> timeValues = data.getValues("vTime");
                // Array1D<Number> magValues = data.getValues("vLonvLat-mag");
                // Array1D<Number> dirValues = data.getValues("vLonvLat-dir");

                assertArrayEquals(lonValues.getShape(), new int[] { tSize });
                assertArrayEquals(latValues.getShape(), new int[] { tSize });
                assertArrayEquals(depthValues.getShape(), new int[] { tSize });
                assertArrayEquals(timeValues.getShape(), new int[] { tSize });
                // assertArrayEquals(magValues.getShape(), new int[] { tSize });
                // assertArrayEquals(dirValues.getShape(), new int[] { tSize });

                float expectedLon = 100.0f * xIndex / (xSize - 1);
                float expectedLat = 100.0f * yIndex / (ySize - 1);
                float expectedDepth = (float) zPos.doubleValue();

                for (int k = 0; k < tSize; k++) {
                    float expectedTime = 100 * k / 9.0f;
                    assertEquals(lonValues.get(k).floatValue(), expectedLon, 1e-5);
                    assertEquals(latValues.get(k).floatValue(), expectedLat, 1e-5);
                    assertEquals(depthValues.get(k).floatValue(), expectedDepth, 1e-5);
                    assertEquals(expectedTime, timeValues.get(k).floatValue(), 1e-5);
                }
            }
        }
    }

    private int numberOfZValuesUsed(Extent<Double> zExtent) {
        int head = -1;
        int end = -1;
        double low = zExtent.getLow();
        double high = zExtent.getHigh();
        for (int i = 0; i < zSize; i++) {
            if (zAxisValues.get(i) > low || Math.abs(zAxisValues.get(i) - low) < 1e-8) {
                head = i;
                break;
            }
        }
        for (int i = 0; i < zSize; i++) {
            if (zAxisValues.get(zSize - 1 - i) < high
                    || Math.abs(zAxisValues.get(zSize - 1 - i) - high) < 1e-8) {
                end = zSize - 1 - i;
                break;
            }
        }
        return end - head + 1;
    }

    /*
     * The variable offset is for practical usage. The return result is in an
     * array from index 0. However, the expected values are sub-set of the
     * original values. The first value of the return result is mapping onto
     * some one of the original values, e.g., the second one. then the offset
     * value is TWO.
     */

    @Test
    public void testTimeSerieFeaturesPartofExents() throws Exception {
        DateTime start = new DateTime(1999, 12, 25, 15, 00, chrnology);
        DateTime end = new DateTime(2000, 1, 8, 23, 00, chrnology);

        Extent<DateTime> tExtent = Extents.newExtent(start, end);
        Extent<Double> zExtent = Extents.newExtent(4.0, 35.0);
        int noZvaluesUsed = numberOfZValuesUsed(zExtent);

        Map<DateTime, Integer> dateIndexMapping = getDateMapping(tExtent);
        Set<DateTime> dateIndex = dateIndexMapping.keySet();

        int offset = dateIndexMapping.get(Collections.min(dateIndex));

        Array<GridCell2D> cells = rGrid.getDomainObjects();
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();
            HorizontalPosition hPos = cell.getCentre();
            extractTimeSeriesFeature(zExtent, tExtent, hPos, xIndex, yIndex, noZvaluesUsed, offset,
                    dateIndexMapping, dateIndex);
        }

        start = new DateTime(2000, 1, 2, 00, 00, chrnology);
        end = new DateTime(2000, 1, 8, 00, 00, chrnology);

        tExtent = Extents.newExtent(start, end);
        zExtent = Extents.newExtent(10.0, 50.0);
        noZvaluesUsed = numberOfZValuesUsed(zExtent);

        dateIndexMapping = getDateMapping(tExtent);
        dateIndex = dateIndexMapping.keySet();

        offset = dateIndexMapping.get(Collections.min(dateIndex));

        cells = rGrid.getDomainObjects();
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();
            HorizontalPosition hPos = cell.getCentre();
            extractTimeSeriesFeature(zExtent, tExtent, hPos, xIndex, yIndex, noZvaluesUsed, offset,
                    dateIndexMapping, dateIndex);
        }

        start = new DateTime(2000, 1, 3, 15, 00, chrnology);
        end = new DateTime(2000, 1, 10, 23, 00, chrnology);

        tExtent = Extents.newExtent(start, end);
        zExtent = Extents.newExtent(12.0, 78.0);
        noZvaluesUsed = numberOfZValuesUsed(zExtent);

        dateIndexMapping = getDateMapping(tExtent);
        dateIndex = dateIndexMapping.keySet();

        offset = dateIndexMapping.get(Collections.min(dateIndex));

        cells = rGrid.getDomainObjects();
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();
            HorizontalPosition hPos = cell.getCentre();
            extractTimeSeriesFeature(zExtent, tExtent, hPos, xIndex, yIndex, noZvaluesUsed, offset,
                    dateIndexMapping, dateIndex);
        }

        start = new DateTime(2000, 1, 3, 15, 00, chrnology);
        end = new DateTime(2000, 1, 7, 23, 00, chrnology);

        tExtent = Extents.newExtent(start, end);
        zExtent = Extents.newExtent(12.0, 105.0);
        noZvaluesUsed = numberOfZValuesUsed(zExtent);

        dateIndexMapping = getDateMapping(tExtent);
        dateIndex = dateIndexMapping.keySet();

        offset = dateIndexMapping.get(Collections.min(dateIndex));

        cells = rGrid.getDomainObjects();
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();
            HorizontalPosition hPos = cell.getCentre();
            extractTimeSeriesFeature(zExtent, tExtent, hPos, xIndex, yIndex, noZvaluesUsed, offset,
                    dateIndexMapping, dateIndex);
        }

        start = new DateTime(2000, 1, 3, 15, 00, chrnology);
        end = new DateTime(2000, 1, 3, 15, 00, chrnology);

        tExtent = Extents.newExtent(start, end);
        zExtent = Extents.newExtent(12.0, 105.0);
        noZvaluesUsed = numberOfZValuesUsed(zExtent);

        dateIndexMapping = getDateMapping(tExtent);
        dateIndex = dateIndexMapping.keySet();

        offset = dateIndexMapping.get(Collections.min(dateIndex));

        cells = rGrid.getDomainObjects();
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();
            HorizontalPosition hPos = cell.getCentre();
            extractTimeSeriesFeature(zExtent, tExtent, hPos, xIndex, yIndex, noZvaluesUsed, offset,
                    dateIndexMapping, dateIndex);
        }
    }

    private void extractTimeSeriesFeature(Extent<Double> zExtent, Extent<DateTime> tExtent,
            HorizontalPosition hPos, int xIndex, int yIndex, int numberOfZValuesUsed, int offset,
            Map<DateTime, Integer> dateIndexMapping, Set<DateTime> dateIndex) throws Exception {
        PlottingDomainParams params = new PlottingDomainParams(xSize, ySize,
                rGrid.getBoundingBox(), zExtent, tExtent, hPos, null, null);
        Collection<? extends PointSeriesFeature> timeSeriesFeature = dataset
                .extractTimeseriesFeatures(null, params);

        assertEquals(numberOfZValuesUsed, timeSeriesFeature.size());

        for (PointSeriesFeature data : timeSeriesFeature) {
            assertEquals(data.getHorizontalPosition(), hPos);
            Array1D<Number> lonValues = data.getValues("vLon");
            Array1D<Number> latValues = data.getValues("vLat");
            Array1D<Number> depthValues = data.getValues("vDepth");
            Array1D<Number> timeValues = data.getValues("vTime");
            // Array1D<Number> magValues = data.getValues("vLonvLat-mag");
            // Array1D<Number> dirValues = data.getValues("vLonvLat-dir");

            assertArrayEquals(new int[] { dateIndex.size() }, lonValues.getShape());
            assertArrayEquals(new int[] { dateIndex.size() }, latValues.getShape());
            assertArrayEquals(new int[] { dateIndex.size() }, depthValues.getShape());
            assertArrayEquals(new int[] { dateIndex.size() }, timeValues.getShape());
            // assertArrayEquals(magValues.getShape(), new int[] { tSize });
            // assertArrayEquals(dirValues.getShape(), new int[] { tSize });

            float expectedLon = 100.0f * xIndex / (xSize - 1);
            float expectedLat = 100.0f * yIndex / (ySize - 1);
            float expectedDepth = (float) data.getVerticalPosition().getZ();

            for (DateTime dt : dateIndex) {
                int dPos = dateIndexMapping.get(dt);
                float expectedTime = 100 * dPos / 9.0f;
                assertEquals(lonValues.get(dPos - offset).floatValue(), expectedLon, 1e-5);
                assertEquals(latValues.get(dPos - offset).floatValue(), expectedLat, 1e-5);
                assertEquals(depthValues.get(dPos - offset).floatValue(), expectedDepth, 1e-5);
                assertEquals(expectedTime, timeValues.get(dPos - offset).floatValue(), 1e-5);
            }
        }
    }

    @Test
    public void testProfileFeatures() throws Exception {
        DateTime dt = new DateTime(2000, 01, 05, 00, 00);
        List<Double> zAxisValues = new ArrayList<Double>();
        for (int i = 0; i < zSize; i++)
            zAxisValues.add(10.0 * i);
        VerticalAxis vAxis = new VerticalAxisImpl("Vertical Axis", zAxisValues, vCrs);
        float expectedTime = 100 * 4 / 9.0f;

        Array<GridCell2D> cells = rGrid.getDomainObjects();
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();

            HorizontalPosition hPos = cell.getCentre();
            PlottingDomainParams params = new PlottingDomainParams(xSize, ySize, null,
                    datasetZExtent, null, hPos, null, dt);
            Collection<? extends ProfileFeature> profileFeature = dataset.extractProfileFeatures(
                    null, params);
            assertEquals(profileFeature.size(), 1);
            DiscreteFeature<?, ?> feature = profileFeature.iterator().next();
            assertTrue(feature instanceof ProfileFeature);
            ProfileFeature data = (ProfileFeature) feature;

            assertEquals(data.getTime(), dt);
            assertEquals(data.getHorizontalPosition(), hPos);
            assertEquals(data.getDomain(), vAxis);

            Array1D<Number> lonValues = data.getValues("vLon");
            Array1D<Number> latValues = data.getValues("vLat");
            Array1D<Number> depthValues = data.getValues("vDepth");
            Array1D<Number> timeValues = data.getValues("vTime");
            // Array1D<Number> magValues =
            // data.getValues("eastECompeastNComp-mag");
            // Array1D<Number> dirValues =
            // data.getValues("eastECompeastNComp-dir");

            assertArrayEquals(lonValues.getShape(), new int[] { tSize + 1 });
            assertArrayEquals(latValues.getShape(), new int[] { tSize + 1 });
            assertArrayEquals(depthValues.getShape(), new int[] { tSize + 1 });
            assertArrayEquals(timeValues.getShape(), new int[] { tSize + 1 });
            // assertArrayEquals(magValues.getShape(), new int[] { tSize + 1 });
            // assertArrayEquals(dirValues.getShape(), new int[] { tSize + 1 });
            float expectedLon = 100.0f * xIndex / (xSize - 1);
            float expectedLat = 100.0f * yIndex / (ySize - 1);

            for (int k = 0; k < tSize + 1; k++) {
                float expectedDepth = 10.0f * k;
                assertEquals(lonValues.get(k).floatValue(), expectedLon, 1e-5);
                assertEquals(latValues.get(k).floatValue(), expectedLat, 1e-5);
                assertEquals(depthValues.get(k).floatValue(), expectedDepth, 1e-5);
                assertEquals(expectedTime, timeValues.get(k).floatValue(), 1e-5);
            }
        }
    }

    private int numberOfTValuesUsed(Extent<DateTime> tExtent) {
        int head = -1;
        int end = -1;
        DateTime low = tExtent.getLow();
        DateTime high = tExtent.getHigh();
        for (int i = 0; i < tSize; i++) {
            if (tAxisValues.get(i).compareTo(low) > 0 || tAxisValues.get(i).compareTo(low) == 0) {
                head = i;
                break;
            }
        }
        for (int i = 0; i < tSize; i++) {
            if (tAxisValues.get(tSize - 1 - i).compareTo(high) < 0
                    || tAxisValues.get(tSize - 1 - i).compareTo(high) == 0) {
                end = tSize - 1 - i;
                break;
            }
        }
        return end - head + 1;
    }

    @Test
    public void testProfileFeaturesPartOfZExtent() throws Exception {
        Array<GridCell2D> cells = rGrid.getDomainObjects();
        DateTime start = new DateTime(2000, 1, 2, 12, 00, chrnology);
        DateTime end = new DateTime(2000, 1, 4, 15, 00, chrnology);
        Extent<DateTime> tExtent = Extents.newExtent(start, end);
        Extent<Double> zExtent = Extents.newExtent(14.0, 28.0);
        Map<DateTime, Integer> dateIndexMapping = getDateMapping(tExtent);
        Map<Double, Integer> depthIndexMapping = getDepthMapping(zExtent, zStep);
        Set<Double> depthIndex = depthIndexMapping.keySet();
        int offset = depthIndexMapping.get(Collections.min(depthIndex));

        int nunberOfTValuesUsed = numberOfTValuesUsed(tExtent);
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();

            HorizontalPosition hPos = cell.getCentre();
            extractProfileFeatures(zExtent, tExtent, hPos, xIndex, yIndex, depthIndexMapping,
                    dateIndexMapping, nunberOfTValuesUsed, depthIndex, offset);
        }
        
        zExtent = Extents.newExtent(10.0, 30.0);
        depthIndexMapping = getDepthMapping(zExtent, zStep);
        depthIndex = depthIndexMapping.keySet();
        offset = depthIndexMapping.get(Collections.min(depthIndex));

        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();

            HorizontalPosition hPos = cell.getCentre();
            extractProfileFeatures(zExtent, tExtent, hPos, xIndex, yIndex, depthIndexMapping,
                    dateIndexMapping, nunberOfTValuesUsed, depthIndex, offset);
        }
        
        zExtent = Extents.newExtent(12.0, 300.0);
        depthIndexMapping = getDepthMapping(zExtent, zStep);
        depthIndex = depthIndexMapping.keySet();
        offset = depthIndexMapping.get(Collections.min(depthIndex));

        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();

            HorizontalPosition hPos = cell.getCentre();
            extractProfileFeatures(zExtent, tExtent, hPos, xIndex, yIndex, depthIndexMapping,
                    dateIndexMapping, nunberOfTValuesUsed, depthIndex, offset);
        }
        
        zExtent = Extents.newExtent(-100.0, 55.0);
        depthIndexMapping = getDepthMapping(zExtent, zStep);
        depthIndex = depthIndexMapping.keySet();
        offset = depthIndexMapping.get(Collections.min(depthIndex));

        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();

            HorizontalPosition hPos = cell.getCentre();
            extractProfileFeatures(zExtent, tExtent, hPos, xIndex, yIndex, depthIndexMapping,
                    dateIndexMapping, nunberOfTValuesUsed, depthIndex, offset);
        }
    }

    private void extractProfileFeatures(Extent<Double> zExtent, Extent<DateTime> tExtent,
            HorizontalPosition hPos, int xIndex, int yIndex,
            Map<Double, Integer> depthIndexMapping, Map<DateTime, Integer> dateIndexMapping,
            int nunberOfTValuesUsed, Set<Double> depthIndex, int offset) throws Exception {
        
        PlottingDomainParams params = new PlottingDomainParams(xSize, ySize, null, zExtent,
                tExtent, hPos, null, null);
        Collection<? extends ProfileFeature> profileFeature = dataset.extractProfileFeatures(null,
                params);

        assertEquals(profileFeature.size(), nunberOfTValuesUsed);
        
        for (ProfileFeature feature : profileFeature) {
            assertEquals(feature.getHorizontalPosition(), hPos);

            Array1D<Number> lonValues = feature.getValues("vLon");
            Array1D<Number> latValues = feature.getValues("vLat");
            Array1D<Number> depthValues = feature.getValues("vDepth");
            Array1D<Number> timeValues = feature.getValues("vTime");

            assertArrayEquals(lonValues.getShape(), new int[] { depthIndexMapping.size() });
            assertArrayEquals(latValues.getShape(), new int[] { depthIndexMapping.size() });
            assertArrayEquals(depthValues.getShape(), new int[] { depthIndexMapping.size() });
            assertArrayEquals(timeValues.getShape(), new int[] { depthIndexMapping.size() });

            float expectedLon = 100.0f * xIndex / (xSize - 1);
            float expectedLat = 100.0f * yIndex / (ySize - 1);

            int dPos = dateIndexMapping.get(feature.getTime());
            float expectedTime = 100 * (dPos) / 9.0f;
            for (Double d : depthIndex) {
                int zPos = depthIndexMapping.get(d);
                float expectedDepth = 10.0f * (zPos);
                assertEquals(lonValues.get(zPos - offset).floatValue(), expectedLon, 1e-5);
                assertEquals(latValues.get(zPos - offset).floatValue(), expectedLat, 1e-5);
                assertEquals(depthValues.get(zPos - offset).floatValue(), expectedDepth, 1e-5);
                assertEquals(expectedTime, timeValues.get(zPos - offset).floatValue(), 1e-5);
            }
        }
    }

    /*
     * refer the description below the method.
     */
    private Map<DateTime, Integer> getDateMapping(Extent<DateTime> tExtent) {
        int head = -1;
        int end = -1;

        DateTime tLow = tExtent.getLow();
        DateTime tHigh = tExtent.getHigh();
        DateTime datasetTLow = datasetTExtent.getLow();
        DateTime datasetTHigh = datasetTExtent.getHigh();

        DateTime startingPoint;
        if (tLow.compareTo(datasetTHigh) > 0 || tHigh.compareTo(datasetTLow) < 0) {
            return null;
        }
        if (tLow.compareTo(datasetTLow) < 0 || tLow.compareTo(datasetTLow) == 0) {
            head = 0;
            startingPoint = datasetTLow;
        } else {
            Days dayperiod = Days.daysBetween(datasetTLow, tLow);
            head = dayperiod.getDays();
            startingPoint = datasetTLow.plusDays(head);
        }
        if (tHigh.compareTo(datasetTHigh) > 0 || tHigh.compareTo(datasetTHigh) == 0) {
            end = tSize - 1;
        } else {
            Days dayperiod = Days.daysBetween(tHigh, datasetTHigh);
            end = tSize - 1 - dayperiod.getDays();
        }
        TreeMap<DateTime, Integer> result = new TreeMap<>();
        for (int i = 0; i < end - head + 1; i++) {
            result.put(startingPoint.plusDays(i), i + head);
        }
        return result;
    }

    /*
     * Given any depth extent zExtent, find the corresponding extent in the
     * given dataset. for example, the test data's depth extent is from 0.0 to
     * 100.0; if given an extent 4.0 to 18.0, it should return the extent 0.0 to
     * 20.0. if the given extent is outside of the test data depth extent, it
     * should return NULL. if the given extent is overlay with the test data
     * depth extent, it should return the overlay extent, e.g., the given extent
     * is -10.0 to 25.0, it should return an extent from 0.0 to 30.0. zExtent:
     * any extent. step: the step of the given dataset depth extent. in the test
     * data, the value is 10.0.
     * 
     * return a map, the keys of Double is the values on the depth axis. in the
     * test data, they are 0.0, 10.0, to 100.0 the value of Integer is the
     * responding index on the depth axis. in this example, they are 0, 1,...,
     * 10. Why I am interested them because of the expected values are based on
     * these index.
     */
    private Map<Double, Integer> getDepthMapping(Extent<Double> zExtent, double step) {
        int head = -1;
        int end = -1;

        double zLow = zExtent.getLow();
        double zHigh = zExtent.getHigh();
        double datasetZLow = datasetZExtent.getLow();
        double datasetZHigh = datasetZExtent.getHigh();

        double startingPoint;

        if (zLow > datasetZHigh || zHigh < datasetZLow)
            return null;

        if (zLow < datasetZLow || (zLow - datasetZLow) < 1e-10) {
            head = 0;
            startingPoint = datasetZLow;
        } else {
            for (int i = 1; i < zSize; i++) {
                if (zLow < datasetZLow + i * step || (zLow - datasetZLow + i * step) < 1e-10) {
                    head = i - 1;
                    break;
                }
            }
            startingPoint = datasetZLow + head * step;
        }
        if (zHigh > datasetZHigh || Math.abs((zHigh - datasetZHigh)) < 1e-10) {
            end = zSize - 1;
        } else {
            for (int i = 1; i < zSize; i++) {
                if (zHigh > datasetZHigh - i * step
                        || Math.abs((zHigh - datasetZHigh - i * step)) < 1e-10) {
                    end = zSize - i;
                    break;
                }
            }
        }
        Map<Double, Integer> result = new TreeMap<>();
        for (int i = 0; i < end - head + 1; i++) {
            result.put(startingPoint + i * step, i + head);
        }
        return result;
    }

    @Test
    public void testProfileFeaturesPartOfTExtent() throws Exception {
        Array<GridCell2D> cells = rGrid.getDomainObjects();
        DateTime start = new DateTime(2000, 01, 01, 00, 00, chrnology);
        DateTime end = start;

        Extent<DateTime> tExtent = Extents.newExtent(start, end);
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();

            HorizontalPosition hPos = cell.getCentre();
            extractProfileFeatures(datasetZExtent, tExtent, hPos, xIndex, yIndex, zSize);
        }

        tExtent = Extents.newExtent(start.minusDays(100), end.plusDays(5));
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();

            HorizontalPosition hPos = cell.getCentre();
            extractProfileFeatures(datasetZExtent, tExtent, hPos, xIndex, yIndex, zSize);
        }

        tExtent = Extents.newExtent(start.minusDays(100), end.plusDays(50));
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();

            HorizontalPosition hPos = cell.getCentre();
            extractProfileFeatures(datasetZExtent, tExtent, hPos, xIndex, yIndex, zSize);
        }

        tExtent = Extents.newExtent(start.plusDays(3), end.plusDays(500));
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();

            HorizontalPosition hPos = cell.getCentre();
            extractProfileFeatures(datasetZExtent, tExtent, hPos, xIndex, yIndex, zSize);
        }
    }

    private void extractProfileFeatures(Extent<Double> zExtent, Extent<DateTime> tExtent,
            HorizontalPosition hPos, int xIndex, int yIndex, int numberOfZValues) throws Exception {
        PlottingDomainParams params = new PlottingDomainParams(xSize, ySize, null, zExtent,
                tExtent, hPos, null, null);
        Collection<? extends ProfileFeature> profileFeature = dataset.extractProfileFeatures(null,
                params);
        Map<DateTime, Integer> dateMapping = getDateMapping(tExtent);
        assertEquals(profileFeature.size(), dateMapping.size());

        for (ProfileFeature feature : profileFeature) {
            assertTrue(feature instanceof ProfileFeature);
            ProfileFeature data = (ProfileFeature) feature;
            DateTime dt = data.getTime();
            assertEquals(data.getHorizontalPosition(), hPos);

            Array1D<Number> lonValues = data.getValues("vLon");
            Array1D<Number> latValues = data.getValues("vLat");
            Array1D<Number> depthValues = data.getValues("vDepth");
            Array1D<Number> timeValues = data.getValues("vTime");

            assertArrayEquals(latValues.getShape(), new int[] { numberOfZValues });
            assertArrayEquals(depthValues.getShape(), new int[] { numberOfZValues });

            float expectedLon = 100.0f * xIndex / (xSize - 1);
            float expectedLat = 100.0f * yIndex / (ySize - 1);
            int dateIndex = dateMapping.get(dt);
            float expectedTime = 100 * dateIndex / 9.0f;

            for (int k = 0; k < numberOfZValues; k++) {
                float expectedDepth = 10.0f * k;
                assertEquals(lonValues.get(k).floatValue(), expectedLon, 1e-5);
                assertEquals(latValues.get(k).floatValue(), expectedLat, 1e-5);
                assertEquals(depthValues.get(k).floatValue(), expectedDepth, 1e-5);
                assertEquals(expectedTime, timeValues.get(k).floatValue(), 1e-5);
            }
        }
    }

    @Test
    public void testNullProfileFeatures() throws Exception {
        Array<GridCell2D> cells = rGrid.getDomainObjects();
        DateTime start = new DateTime(2011, 01, 15, 00, 00, chrnology);
        DateTime end = new DateTime(2012, 02, 03, 00, 00, chrnology);
        Extent<DateTime> tExtent = Extents.newExtent(start, end);
        for (GridCell2D cell : cells) {
            HorizontalPosition hPos = cell.getCentre();
            PlottingDomainParams params = new PlottingDomainParams(xSize, ySize, null,
                    datasetZExtent, tExtent, hPos, null, null);
            Collection<? extends ProfileFeature> profileFeature = dataset.extractProfileFeatures(
                    null, params);
            for (ProfileFeature feature : profileFeature) {
                assertNull(feature);
            }
        }

        start = new DateTime(1990, 01, 15, 00, 00, chrnology);
        end = new DateTime(1998, 02, 03, 00, 00, chrnology);
        tExtent = Extents.newExtent(start, end);
        for (GridCell2D cell : cells) {
            HorizontalPosition hPos = cell.getCentre();
            PlottingDomainParams params = new PlottingDomainParams(xSize, ySize, null,
                    datasetZExtent, tExtent, hPos, null, null);
            Collection<? extends ProfileFeature> profileFeature = dataset.extractProfileFeatures(
                    null, params);
            for (ProfileFeature feature : profileFeature) {
                assertNull(feature);
            }
        }

        start = new DateTime(2000, 1, 1, 00, 00, chrnology);
        end = new DateTime(2000, 1, 10, 00, 00, chrnology);
        tExtent = Extents.newExtent(start, end);
        Extent<Double> zExtent = Extents.newExtent(200.0, 500.0);
        for (GridCell2D cell : cells) {
            HorizontalPosition hPos = cell.getCentre();
            PlottingDomainParams params = new PlottingDomainParams(xSize, ySize, null, zExtent,
                    tExtent, hPos, null, null);
            Collection<? extends ProfileFeature> profileFeature = dataset.extractProfileFeatures(
                    null, params);
            for (ProfileFeature feature : profileFeature) {
                assertNull(feature);
            }
        }

        start = new DateTime(2000, 1, 1, 00, 00, chrnology);
        end = new DateTime(2000, 1, 10, 00, 00, chrnology);
        tExtent = Extents.newExtent(start, end);
        zExtent = Extents.newExtent(-200.0, -50.0);
        for (GridCell2D cell : cells) {
            HorizontalPosition hPos = cell.getCentre();
            PlottingDomainParams params = new PlottingDomainParams(xSize, ySize, null, zExtent,
                    tExtent, hPos, null, null);
            Collection<? extends ProfileFeature> profileFeature = dataset.extractProfileFeatures(
                    null, params);
            for (ProfileFeature feature : profileFeature) {
                assertNull(feature);
            }
        }
    }
    
    @Test
    public void readFeatureTest() throws Exception{
       assertTrue(dataset.readFeature("vLon") instanceof GridFeature);
       assertFalse(dataset.readFeature("vLat") instanceof PointSeriesFeature);
       assertFalse(dataset.readFeature("vDepth") instanceof ProfileFeature);
       assertFalse(dataset.readFeature("vTime") instanceof TrajectoryFeature);
       assertFalse(dataset.readFeature("vLat") instanceof MapFeature);

       GridFeature feature =(GridFeature) dataset.readFeature("vLon");
       Array4D<Number> values = feature.getValues("vLon");
       int Tsize= values.getTSize();
       int Zsize= values.getZSize();
       int Ysize= values.getYSize();
       int Xsize= values.getXSize();
       
       for(int i=0; i< Tsize; i++){
           for(int j=0; j<Zsize; j++){
               for(int k=0; k< Ysize; k++){
                   for(int m=0; m< Xsize; m++){
                       Number vLonValue =values.get(i,j,k,m);
                       float expectedValue =100.0f * m / (xSize - 1);
                       assertEquals(vLonValue.floatValue(), expectedValue, 1e-5);
                   }
               }
           }
       }
       GridDomain gDomain =feature.getDomain();
       //gDomain.
    }
}
