package uk.ac.rdg.resc.edal.dataset;

/**
 * Test Class for {@link PRTreeFeatureIndexer}.
 * Only for {@link PRTreeFeatureIndexer#getgetAllFeatureIds} and
 * {@link PRTreeFeatureIndexer#findFeatureIds} methods as others are
 * simple.
 */
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Extents;

public class PRTreeFeatureIndexerTest {
    // points number on the longitude side
    private static final int xSize = 350;
    // points number on the latitude side
    private static final int ySize = 80;
    private static Chronology chrnology = ISOChronology.getInstance();
    // time starting point for the testing dataset
    private static DateTime dt = new DateTime(200, 01, 01, 00, 00, chrnology);
    // lower left point of the grid in lat lon format
    private static double x_origin = 5.0;
    private static double y_origin = 10.0;

    private Extent<Double> verticalExtent;
    private Extent<DateTime> timeExtent;
    private TreeSet<Double> longitudePoints = new TreeSet<>();
    private TreeSet<Double> latitudePoints = new TreeSet<>();
    private CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;

    private Collection<String> varIDs;
    private ArrayList<FeatureIndexer.FeatureBounds> features = new ArrayList<>();
    private PRTreeFeatureIndexer featureindexer = new PRTreeFeatureIndexer();

    @Before
    public void setUp() {
        // initialize the horizontal grid with depth and time extents
        verticalExtent = Extents.newExtent(0.0, 90.0);
        timeExtent = Extents.newExtent(dt, dt.plusDays(10));

        // grid points are not evenly distributed
        for (int i = 0; i < xSize; i++) {
            longitudePoints.add(x_origin + (i + 1) * i * 0.01 / 2.0);
        }
        for (int i = 0; i < ySize; i++) {
            latitudePoints.add(y_origin + (i + 1) * i * 0.02 / 2.0);
        }
        // variables available for the test dataset
        varIDs = new HashSet<>();
        varIDs.add("temperature");
        varIDs.add("allx_u");
        varIDs.add("allx_v");

        Iterator<Double> yIndex = latitudePoints.iterator();
        Iterator<Double> xIndex = longitudePoints.iterator();
        int counterX = 0;
        int counterY = 0;
        while (yIndex.hasNext()) {
            double yvalue = yIndex.next();
            while (xIndex.hasNext()) {
                String featureID = "x" + (new Integer(counterX++)).toString() + "y"
                        + (new Integer(counterY)).toString();
                HorizontalPosition hPos = new HorizontalPosition(xIndex.next(), yvalue, crs);
                FeatureIndexer.FeatureBounds featurebounds = new FeatureIndexer.FeatureBounds(
                        featureID, hPos, verticalExtent, timeExtent, varIDs);
                features.add(featurebounds);
            }
            xIndex = longitudePoints.iterator();
            counterY++;
            counterX = 0;
        }
        featureindexer.addFeatures(features);
    }

    @Test
    public void testgetAllFeatureIds() {
        HashSet<String> expectedIDs = new HashSet<>();
        for (int i = 0; i < ySize; i++) {
            for (int j = 0; j < xSize; j++) {
                String featureID = "x" + (new Integer(j)).toString() + "y"
                        + (new Integer(i)).toString();
                expectedIDs.add(featureID);
            }
        }
        assertEquals(expectedIDs, featureindexer.getAllFeatureIds());
    }

    @Test
    public void testFindFeatureIds() {
        // variables that we are interested
        HashSet<String> fIDs = new HashSet<>();
        fIDs.add("allx_u");
        fIDs.add("allx_v");

        // general test
        Extent<Double> xExtent = Extents.newExtent(100.0, 102.5);
        Extent<Double> yExtent = Extents.newExtent(15.0, 25.0);
        BoundingBox bbox = new BoundingBoxImpl(xExtent, yExtent, crs);
        findFeatureIds(bbox, verticalExtent, timeExtent, fIDs);

        // verticalExtent set to null
        findFeatureIds(bbox, null, timeExtent, fIDs);

        // timeExtent set to null
        findFeatureIds(bbox, verticalExtent, null, fIDs);

        // both verticalExtent and timeExtent set to null
        findFeatureIds(bbox, null, null, fIDs);

        // a verticalExtent intersect with the verticalExtent of the test
        // dataset
        Extent<Double> vExtent = Extents.newExtent(-20.4, 102.5);

        // a timeExtent intersect with the timeExtent of the test dataset
        Extent<DateTime> tExtent = Extents.newExtent(dt.minusDays(3), dt.plusDays(3));

        findFeatureIds(bbox, vExtent, timeExtent, fIDs);
        findFeatureIds(bbox, verticalExtent, tExtent, fIDs);

        // a bounding box of which longitude is greater than 180.0
        xExtent = Extents.newExtent(390.0, 392.0);
        yExtent = Extents.newExtent(70.0, 72.0);
        bbox = new BoundingBoxImpl(xExtent, yExtent, crs);
        findFeatureIds(bbox, verticalExtent, timeExtent, fIDs);
    }

    /**
     * Help method to do the real business of testing findFeatureIds
     * 
     * @param bbox
     *            Bounding box of the feature that users are interested
     * @param verticalExtent
     *            verticalExtent of the feature that users are interested
     * @param timeExtent
     *            timeExtent of the feature that users are interested
     * @param variableIds
     *            names of the features that users are interested
     * */
    private void findFeatureIds(BoundingBox bbox, Extent<Double> verticalExtent,
            Extent<DateTime> timeExtent, Collection<String> variableIds) {
        Collection<String> results = featureindexer.findFeatureIds(bbox, verticalExtent,
                timeExtent, variableIds);
        /*
         * results are implemented in ArrayList. Change its form to hashset so
         * can be applied to assertEqual to compare.
         */
        Collection<String> resultInHashSetForm = new HashSet<>();
        for (String s : results) {
            resultInHashSetForm.add(s);
        }
        int xIndexStartFrom = longitudePoints.headSet(bbox.getMinX()).size();
        int xIndexEndAt = longitudePoints.headSet(bbox.getMaxX()).size();

        int yIndexStartFrom = latitudePoints.headSet(bbox.getMinY()).size();
        int yIndexEndAt = latitudePoints.headSet(bbox.getMaxY()).size();

        Collection<String> expectedResults = new HashSet<>();
        for (int x = xIndexStartFrom; x < xIndexEndAt; x++) {
            for (int y = yIndexStartFrom; y < yIndexEndAt; y++) {
                String s = "x" + (new Integer(x)).toString() + "y" + (new Integer(y)).toString();
                expectedResults.add(s);
            }
        }
        /*
         * the given bounding box may respond to two searching boxes in the test
         * dataset
         */
        if ((bbox.getMinX() - 360.0) > x_origin) {
            xIndexStartFrom = longitudePoints.headSet(bbox.getMinX() - 360.0).size();
            xIndexEndAt = longitudePoints.headSet(bbox.getMaxX() - 360.0).size();
            for (int x = xIndexStartFrom; x < xIndexEndAt; x++) {
                for (int y = yIndexStartFrom; y < yIndexEndAt; y++) {
                    String s = "x" + (new Integer(x)).toString() + "y"
                            + (new Integer(y)).toString();
                    expectedResults.add(s);
                }
            }
        }
        assertEquals(expectedResults, resultInHashSetForm);
    }
}
