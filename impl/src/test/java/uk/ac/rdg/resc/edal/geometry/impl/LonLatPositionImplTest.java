/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.geometry.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;
import uk.ac.rdg.resc.edal.position.LonLatPosition;
import uk.ac.rdg.resc.edal.position.impl.LonLatPositionImpl;

/**
 * Test case for {@link LonLatPositionImpl}.
 * @author Jon
 */
public class LonLatPositionImplTest {

    private static double longitude = 102.0;
    private static double latitude = 45.0;
    private static LonLatPosition TEST_POS = new LonLatPositionImpl(longitude, latitude);

    /**
     * Tests that the coordinate reference system is correct.
     */
    @Test
    public void simpleTest() {
        assertEquals(TEST_POS.getCoordinateReferenceSystem(), DefaultGeographicCRS.WGS84);
        assertEquals(TEST_POS.getCoordinateReferenceSystem().getCoordinateSystem().getDimension(), 2);
        assertEquals(TEST_POS.getDimension(), 2);
        assertEquals(TEST_POS.getLongitude(), longitude, 0.0);
        assertEquals(TEST_POS.getLatitude(), latitude, 0.0);
        assertEquals(TEST_POS.getLongitude(), TEST_POS.getOrdinate(0), 0.0);
        assertEquals(TEST_POS.getLatitude(), TEST_POS.getOrdinate(1), 0.0);
        assertTrue(Arrays.equals(TEST_POS.getCoordinate(), new double[]{longitude, latitude}));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void outOfBounds1() {
        TEST_POS.getOrdinate(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void outOfBounds2() {
        TEST_POS.getOrdinate(2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testImmutability() {
        TEST_POS.setOrdinate(0, 0.0);
    }

    /** Test creating a LonLatPositionImpl with a longitude outside the range [-180:180] */
    @Test
    public void testLongitudeRange() {
        LonLatPosition pos = new LonLatPositionImpl(678.9, 0.0);
        assertEquals(pos.getLongitude(), -41.1, 0.00001);
        LonLatPosition pos2 = new LonLatPositionImpl(-1023.6, 0.0);
        assertEquals(pos2.getLongitude(), 56.4, 0.00001);
    }

    /**
     * Test method for {@link org.jcsml.impl.LonLatPositionImpl#equals()}.
     */
    @Test
    public void testEquals() {
        LonLatPosition pos1 = new LonLatPositionImpl(longitude, latitude);
        LonLatPosition pos2 = new LonLatPositionImpl(longitude, latitude);
        assertEquals(pos1, pos2);
        LonLatPosition pos3 = new LonLatPositionImpl(longitude, 22.d);

        assertFalse(pos1.equals(pos3));
    }
}
