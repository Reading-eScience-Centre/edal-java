package uk.ac.rdg.resc.edal.dataset.cdm;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
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
    private static final double delta = 1e-5;
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
    private TimeAxis tAxis;
    private VerticalAxis vAxis;

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
        List<DateTime> tAxisValues = new ArrayList<>();
        List<Double> zAxisValues = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tAxisValues.add(new DateTime(2000, 01, 01 + i, 00, 00, chrnology));
            zAxisValues.add(10.0 * i);
        }
        zAxisValues.add(100.0);
        tAxis = new TimeAxisImpl("time", tAxisValues);
        vAxis = new VerticalAxisImpl("depth", zAxisValues, vCrs);
    }

    @Test
    public void testMetadataInfo() throws Exception {
        assertEquals(chrnology, dataset.getDatasetChronology());
        assertEquals(vCrs, dataset.getDatasetVerticalCrs());
        assertEquals(MapFeature.class, dataset.getMapFeatureType("vLon"));

        VariableMetadata metadata = dataset.getVariableMetadata("vLon");

        assertEquals(vCrs, metadata.getVerticalDomain().getVerticalCrs());
        assertEquals(datasetZExtent, metadata.getVerticalDomain().getExtent());
        assertEquals(rGrid, metadata.getHorizontalDomain());
        assertEquals(datasetTExtent, metadata.getTemporalDomain().getExtent());
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

        for (Double zPos = 0.0; zPos <= 100; zPos += 10.0) {
            for (GridCell2D cell : cells) {
                GridCoordinates2D gCoordinate = cell.getGridCoordinates();
                int xIndex = gCoordinate.getX();
                int yIndex = gCoordinate.getY();
                HorizontalPosition hPos = cell.getCentre();
                // to fetch data at one depth position, BBOX must set to null.
                // otherwise all values inside BBOX be returned as if targetZ
                // is ignored.
                PlottingDomainParams params = new PlottingDomainParams(xSize, ySize, null, null,
                        datasetTExtent, hPos, zPos, null);
                Collection<? extends PointSeriesFeature> timeSeriesFeature = dataset
                        .extractTimeseriesFeatures(null, params);
                assertEquals(1, timeSeriesFeature.size());
                DiscreteFeature<?, ?> feature = timeSeriesFeature.iterator().next();
                assertTrue(feature instanceof PointSeriesFeature);
                PointSeriesFeature data = (PointSeriesFeature) feature;

                assertEquals(tAxis, data.getDomain());
                assertEquals(hPos, data.getHorizontalPosition());
                assertEquals(zPos, data.getVerticalPosition().getZ(), delta);

                Array1D<Number> lonValues = data.getValues("vLon");
                Array1D<Number> latValues = data.getValues("vLat");
                Array1D<Number> depthValues = data.getValues("vDepth");
                Array1D<Number> timeValues = data.getValues("vTime");

                assertArrayEquals(new int[] { tSize }, lonValues.getShape());
                assertArrayEquals(new int[] { tSize }, latValues.getShape());
                assertArrayEquals(new int[] { tSize }, depthValues.getShape());
                assertArrayEquals(new int[] { tSize }, timeValues.getShape());

                float expectedLon = 100.0f * xIndex / (xSize - 1);
                float expectedLat = 100.0f * yIndex / (ySize - 1);
                float expectedDepth = (float) zPos.doubleValue();

                for (int k = 0; k < tSize; k++) {
                    float expectedTime = 100 * k / 9.0f;
                    assertEquals(expectedLon, lonValues.get(k).floatValue(), delta);
                    assertEquals(expectedLat, latValues.get(k).floatValue(), delta);
                    assertEquals(expectedDepth, depthValues.get(k).floatValue(), 1e-5);
                    assertEquals(expectedTime, timeValues.get(k).floatValue(), 1e-5);
                }
            }
        }
    }

    @Test
    public void testTimeSerieFeaturesPartofZExents() throws Exception {
        Extent<Double> zExtent = Extents.newExtent(18.6, 53.4);
        Array<GridCell2D> cells = rGrid.getDomainObjects();
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();
            HorizontalPosition hPos = cell.getCentre();
            PlottingDomainParams params = new PlottingDomainParams(xSize, ySize, null, zExtent,
                    datasetTExtent, hPos, null, null);
            Collection<? extends PointSeriesFeature> timeSeriesFeatures = dataset
                    .extractTimeseriesFeatures(null, params);
            for (PointSeriesFeature data : timeSeriesFeatures)
                verifyTimeSeriesFeature(data, hPos, xIndex, yIndex);
        }

        zExtent = Extents.newExtent(20.0, 153.4);
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();
            HorizontalPosition hPos = cell.getCentre();
            PlottingDomainParams params = new PlottingDomainParams(xSize, ySize, null, zExtent,
                    datasetTExtent, hPos, null, null);
            Collection<? extends PointSeriesFeature> timeSeriesFeatures = dataset
                    .extractTimeseriesFeatures(null, params);
            for (PointSeriesFeature data : timeSeriesFeatures)
                verifyTimeSeriesFeature(data, hPos, xIndex, yIndex);
        }

        zExtent = Extents.newExtent(-120.6, 47.18);
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();
            HorizontalPosition hPos = cell.getCentre();
            PlottingDomainParams params = new PlottingDomainParams(xSize, ySize, null, zExtent,
                    datasetTExtent, hPos, null, null);
            Collection<? extends PointSeriesFeature> timeSeriesFeatures = dataset
                    .extractTimeseriesFeatures(null, params);
            for (PointSeriesFeature data : timeSeriesFeatures)
                verifyTimeSeriesFeature(data, hPos, xIndex, yIndex);
        }

        zExtent = Extents.newExtent(-18.6, -10.0);
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();
            HorizontalPosition hPos = cell.getCentre();
            PlottingDomainParams params = new PlottingDomainParams(xSize, ySize, null, zExtent,
                    datasetTExtent, hPos, null, null);
            Collection<? extends PointSeriesFeature> timeSeriesFeatures = dataset
                    .extractTimeseriesFeatures(null, params);
            for (PointSeriesFeature data : timeSeriesFeatures)
                verifyTimeSeriesFeature(data, hPos, xIndex, yIndex);
        }

        double targetZ = 85.8;
        int zIndex = -1;
        if (vAxis.contains(targetZ)) {
            zIndex = vAxis.findIndexOf(targetZ);
            for (GridCell2D cell : cells) {
                HorizontalPosition hPos = cell.getCentre();
                PlottingDomainParams params = new PlottingDomainParams(xSize, ySize, null, null,
                        datasetTExtent, hPos, targetZ, null);
                Collection<? extends PointSeriesFeature> timeSeriesFeatures = dataset
                        .extractTimeseriesFeatures(null, params);

                for (PointSeriesFeature data : timeSeriesFeatures) {
                    Array1D<Number> depthValues = data.getValues("vDepth");
                    assertArrayEquals(new int[] { tSize }, depthValues.getShape());

                    float expectedDepth = 10.0f * zIndex;
                    for (int k = 0; k < tSize; k++) {
                        assertEquals(expectedDepth, depthValues.get(k).floatValue(), delta);
                    }
                }
            }
        }

        targetZ = 110.8; // vAxis.contains(targetZ) return false
        for (GridCell2D cell : cells) {
            HorizontalPosition hPos = cell.getCentre();
            PlottingDomainParams params = new PlottingDomainParams(xSize, ySize, null, null,
                    datasetTExtent, hPos, targetZ, null);
            Collection<? extends PointSeriesFeature> timeSeriesFeatures = dataset
                    .extractTimeseriesFeatures(null, params);
            assertEquals(0, timeSeriesFeatures.size());
        }

        targetZ = -20.8; // vAxis.contains(targetZ) return false
        for (GridCell2D cell : cells) {
            HorizontalPosition hPos = cell.getCentre();
            PlottingDomainParams params = new PlottingDomainParams(xSize, ySize, null, null,
                    datasetTExtent, hPos, targetZ, null);
            Collection<? extends PointSeriesFeature> timeSeriesFeatures = dataset
                    .extractTimeseriesFeatures(null, params);
            assertEquals(0, timeSeriesFeatures.size());
        }
    }

    @Test
    public void testTimeSerieFeaturesPartofTExents() throws Exception {
        DateTime start = new DateTime(1999, 12, 25, 15, 00, chrnology);
        DateTime end = new DateTime(2000, 1, 8, 23, 00, chrnology);

        Extent<DateTime> tExtent = Extents.newExtent(start, end);

        Array<GridCell2D> cells = rGrid.getDomainObjects();
        for (GridCell2D cell : cells) {
            extractTimeSeriesFeature(tExtent, cell);
        }

        start = new DateTime(2000, 1, 2, 00, 00, chrnology);
        end = new DateTime(2000, 1, 8, 00, 00, chrnology);
        tExtent = Extents.newExtent(start, end);
        for (GridCell2D cell : cells) {
            extractTimeSeriesFeature(tExtent, cell);
        }

        start = new DateTime(2000, 1, 3, 15, 00, chrnology);
        end = new DateTime(2000, 1, 3, 15, 00, chrnology);
        tExtent = Extents.newExtent(start, end);
        for (GridCell2D cell : cells) {
            extractTimeSeriesFeature(tExtent, cell);
        }

        start = new DateTime(2000, 1, 3, 15, 00, chrnology);
        end = new DateTime(2000, 5, 3, 15, 00, chrnology);
        tExtent = Extents.newExtent(start, end);
        for (GridCell2D cell : cells) {
            extractTimeSeriesFeature(tExtent, cell);
        }

        start = new DateTime(2000, 1, 20, 15, 00, chrnology);
        end = new DateTime(2000, 5, 3, 15, 00, chrnology);
        tExtent = Extents.newExtent(start, end);
        for (GridCell2D cell : cells) {
            extractTimeSeriesFeature(tExtent, cell);
        }

        start = new DateTime(1999, 10, 20, 15, 00, chrnology);
        end = new DateTime(1999, 12, 3, 23, 59, chrnology);
        tExtent = Extents.newExtent(start, end);
        for (GridCell2D cell : cells) {
            extractTimeSeriesFeature(tExtent, cell);
        }
    }

    private void extractTimeSeriesFeature(Extent<DateTime> tExtent, GridCell2D cell)
            throws Exception {
        GridCoordinates2D gCoordinate = cell.getGridCoordinates();
        int xIndex = gCoordinate.getX();
        int yIndex = gCoordinate.getY();
        HorizontalPosition hPos = cell.getCentre();
        PlottingDomainParams params = new PlottingDomainParams(xSize, ySize, null, datasetZExtent,
                tExtent, hPos, null, null);
        Collection<? extends PointSeriesFeature> timeSeriesFeature = dataset
                .extractTimeseriesFeatures(null, params);

        if (tExtent == null || tExtent.intersects(datasetTExtent)) {
            assertEquals(zSize, timeSeriesFeature.size());

            for (PointSeriesFeature feature : timeSeriesFeature) {
                verifyTimeSeriesFeature(feature, hPos, xIndex, yIndex);
            }
        } else {
            assertEquals(0, timeSeriesFeature.size());
        }
    }

    private void verifyTimeSeriesFeature(PointSeriesFeature data, HorizontalPosition hPos,
            int xIndex, int yIndex) {
        assertEquals(hPos, data.getHorizontalPosition());
        Array1D<Number> lonValues = data.getValues("vLon");
        Array1D<Number> latValues = data.getValues("vLat");
        Array1D<Number> depthValues = data.getValues("vDepth");
        Array1D<Number> timeValues = data.getValues("vTime");

        assertArrayEquals(new int[] { tSize }, lonValues.getShape());
        assertArrayEquals(new int[] { tSize }, latValues.getShape());
        assertArrayEquals(new int[] { tSize }, depthValues.getShape());
        assertArrayEquals(new int[] { tSize }, timeValues.getShape());

        float expectedLon = 100.0f * xIndex / (xSize - 1);
        float expectedLat = 100.0f * yIndex / (ySize - 1);
        float expectedDepth = (float) data.getVerticalPosition().getZ();

        for (int k = 0; k < tSize; k++) {
            float expectedTime = 100 * k / 9.0f;
            assertEquals(expectedLon, lonValues.get(k).floatValue(), delta);
            assertEquals(expectedLat, latValues.get(k).floatValue(), delta);
            assertEquals(expectedDepth, depthValues.get(k).floatValue(), delta);
            assertEquals(expectedTime, timeValues.get(k).floatValue(), delta);
        }
    }

    // General test to extract profile features
    @Test
    public void testProfileFeatures() throws Exception {
        DateTime dt = new DateTime(2000, 01, 05, 00, 00);

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
            assertEquals(1, profileFeature.size());
            DiscreteFeature<?, ?> feature = profileFeature.iterator().next();
            assertTrue(feature instanceof ProfileFeature);
            verifyProfileFeature((ProfileFeature) feature, hPos, xIndex, yIndex);
        }
    }

    @Test
    public void testFeaturesBBox() throws Exception {
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

    private void bboxTest(BoundingBox bbox, double xstep, double ystep) throws Exception {
        PlottingDomainParams params = new PlottingDomainParams(xSize, ySize, bbox, null, null,
                null, null, null);
        Collection<? extends ProfileFeature> profileFeatures = dataset.extractProfileFeatures(null,
                params);

        params = new PlottingDomainParams(xSize, ySize, bbox, null, null, null, null, null);
        Collection<? extends PointSeriesFeature> timeSeriesFeatures = dataset
                .extractTimeseriesFeatures(null, params);

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
                assertEquals(0, timeSeriesFeatures.size());
                assertEquals(0, profileFeatures.size());
            } else {
                int numberOfXMidPoints = getNumberOfMidPoints(bbox.getMinX(), bbox.getMaxX(), xstep);
                if (numberOfXMidPoints < 0) {
                    assertEquals(0, timeSeriesFeatures.size());
                    assertEquals(0, profileFeatures.size());
                }
                else{
                    assertEquals(numberOfXMidPoints * numberOfYMidPoints * zSize,
                            timeSeriesFeatures.size());
                    assertEquals(numberOfXMidPoints * numberOfYMidPoints * tSize,
                            profileFeatures.size());
                }
            }
        }
    }

    private int getNumberOfMidPoints(double low, double high, double step) {
        if (high - low > 360.0)
            return (int) (360.0 / step);
        int m = (int) Math.ceil(low / step);
        int n = (int) Math.floor(high / step);
        if (n < m)
            return -1;
        else
            return n - m + 1;
    }

    // for profile features, targetZ is ignored.
    private void zExtentCaseForProfileFeatures(Extent<Double> zExtent, GridCell2D cell)
            throws Exception {
        HorizontalPosition hPos = cell.getCentre();
        PlottingDomainParams params = new PlottingDomainParams(xSize, ySize, null, zExtent, null,
                hPos, null, null);
        Collection<? extends ProfileFeature> profileFeature = dataset.extractProfileFeatures(null,
                params);
        if (zExtent == null || zExtent.intersects(datasetZExtent)) {
            assertEquals(tSize, profileFeature.size());
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();
            for (ProfileFeature feature : profileFeature) {
                verifyProfileFeature(feature, hPos, xIndex, yIndex);
            }
        } else {
            assertEquals(0, profileFeature.size());
        }
    }

    @Test
    public void testProfileFeaturesPartOfZExtent() throws Exception {
        Array<GridCell2D> cells = rGrid.getDomainObjects();

        // test case 1
        Extent<Double> zExtent = Extents.newExtent(1220.0, 4401.0);
        for (GridCell2D cell : cells) {
            zExtentCaseForProfileFeatures(zExtent, cell);
        }

        // test case 2, the given zExtent intersect with the one of the data set
        // it should return all profile features the data set contain
        zExtent = Extents.newExtent(25.0, 300.0);
        for (GridCell2D cell : cells) {
            zExtentCaseForProfileFeatures(zExtent, cell);
        }

        // test case 3: a given zExtent is in the range of of the zExtent of the
        // dataset
        // all profile features return
        zExtent = Extents.newExtent(38.0, 55.0);
        for (GridCell2D cell : cells) {
            zExtentCaseForProfileFeatures(zExtent, cell);
        }

        // test case 4: another example a given zExtent intersects with the
        // zExtent of the data set.
        zExtent = Extents.newExtent(-100.0, 95.0);
        for (GridCell2D cell : cells) {
            zExtentCaseForProfileFeatures(zExtent, cell);
        }

        zExtent = Extents.newExtent(-100.0, -5.0);
        for (GridCell2D cell : cells) {
            zExtentCaseForProfileFeatures(zExtent, cell);
        }

        for (GridCell2D cell : cells) {
            zExtentCaseForProfileFeatures(null, cell);
        }
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
            tExentCaseForProfileFeatures(tExtent, null, hPos, xIndex, yIndex);
        }

        tExtent = Extents.newExtent(start.minusDays(100), end.plusDays(5));
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();
            HorizontalPosition hPos = cell.getCentre();
            tExentCaseForProfileFeatures(tExtent, null, hPos, xIndex, yIndex);
        }

        tExtent = Extents.newExtent(start.minusDays(100), end.plusDays(50));
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();
            HorizontalPosition hPos = cell.getCentre();
            tExentCaseForProfileFeatures(tExtent, null, hPos, xIndex, yIndex);
        }

        tExtent = Extents.newExtent(start.plusDays(3), end.plusDays(500));
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();

            HorizontalPosition hPos = cell.getCentre();
            tExentCaseForProfileFeatures(tExtent, null, hPos, xIndex, yIndex);
        }
        
        DateTime targetT = new DateTime(2000, 01, 05, 00, 00, chrnology);
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();

            HorizontalPosition hPos = cell.getCentre();
            tExentCaseForProfileFeatures(null, targetT, hPos, xIndex, yIndex);
        }
        
        targetT = new DateTime(2000, 01, 05, 10, 50, chrnology);
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();

            HorizontalPosition hPos = cell.getCentre();
            tExentCaseForProfileFeatures(null, targetT, hPos, xIndex, yIndex);
        }
        
        targetT = new DateTime(1999, 01, 05, 10, 50, chrnology);
        for (GridCell2D cell : cells) {
            GridCoordinates2D gCoordinate = cell.getGridCoordinates();
            int xIndex = gCoordinate.getX();
            int yIndex = gCoordinate.getY();

            HorizontalPosition hPos = cell.getCentre();
            tExentCaseForProfileFeatures(null, targetT, hPos, xIndex, yIndex);
        }
    }

    private void tExentCaseForProfileFeatures(Extent<DateTime> tExtent, DateTime targetT,
            HorizontalPosition hPos, int xIndex, int yIndex) throws Exception {
        PlottingDomainParams params = new PlottingDomainParams(xSize, ySize, null, datasetZExtent,
                tExtent, hPos, null, targetT);
        Collection<? extends ProfileFeature> profileFeature = dataset.extractProfileFeatures(null,
                params);
        if (tExtent == null && targetT == null) {
            assertEquals(tSize, profileFeature.size());
            for (ProfileFeature feature : profileFeature) {
                verifyProfileFeature(feature, hPos, xIndex, yIndex);
            }
        } else if (tExtent == null && targetT != null) {
            if (tAxis.contains(targetT)) {
                assertEquals(1, profileFeature.size());
                for (ProfileFeature feature : profileFeature) {
                    verifyProfileFeature(feature, hPos, xIndex, yIndex);
                }
            } else {
                assertEquals(0, profileFeature.size());
            }
        } else if (tExtent != null && tExtent.intersects(datasetTExtent)) {
            for (ProfileFeature feature : profileFeature) {
                verifyProfileFeature(feature, hPos, xIndex, yIndex);
            }
        } else if (tExtent != null && !tExtent.intersects(datasetTExtent)) {
            assertEquals(0, profileFeature.size());
        }
    }

    private void verifyProfileFeature(ProfileFeature data, HorizontalPosition hPos, int xIndex,
            int yIndex) {
        assertEquals(hPos, data.getHorizontalPosition());

        Array1D<Number> lonValues = data.getValues("vLon");
        Array1D<Number> latValues = data.getValues("vLat");
        Array1D<Number> depthValues = data.getValues("vDepth");
        Array1D<Number> timeValues = data.getValues("vTime");

        assertArrayEquals(new int[] { zSize }, latValues.getShape());
        assertArrayEquals(new int[] { zSize }, depthValues.getShape());

        float expectedLon = 100.0f * xIndex / (xSize - 1);
        float expectedLat = 100.0f * yIndex / (ySize - 1);
        int dateIndex = tAxis.findIndexOf(data.getTime());
        float expectedTime = 100 * dateIndex / 9.0f;

        for (int k = 0; k < zSize; k++) {
            float expectedDepth = 10.0f * k;
            assertEquals(expectedLon, lonValues.get(k).floatValue(), delta);
            assertEquals(expectedLat, latValues.get(k).floatValue(), delta);
            assertEquals(expectedDepth, depthValues.get(k).floatValue(), delta);
            assertEquals(expectedTime, timeValues.get(k).floatValue(), delta);
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
    public void readFeatureTest() throws Exception {
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
                        float expectedValue = 100.0f * m / (xSize - 1);
                        assertEquals(expectedValue, vLonValue.floatValue(), delta);
                    }
                }
            }
        }
    }
}
