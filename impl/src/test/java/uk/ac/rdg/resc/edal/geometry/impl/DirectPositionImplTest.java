/*
 * Copyright (c) 2009 The University of Reading
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
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR  CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package uk.ac.rdg.resc.edal.geometry.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.geotoolkit.geometry.DirectPosition2D;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.geotoolkit.referencing.crs.DefaultVerticalCRS;
import org.junit.Test;
import org.opengis.geometry.DirectPosition;
import uk.ac.rdg.resc.edal.position.impl.DirectPositionImpl;

/**
 * Test for the {@link DirectPositionImpl} class.
 * @author Jon
 */
public class DirectPositionImplTest {

    /** Tests attempt to create DirectPositionImpl with null coordinates and
     null CRS*/
    @Test(expected=NullPointerException.class)
    public void nullPointerTest() {
        new DirectPositionImpl(null, null);
    }

    /** Tests attempt to create DirectPositionImpl with null coordinates but
     non-null CRS*/
    @Test(expected=NullPointerException.class)
    public void nullPointerTest2() {
        new DirectPositionImpl(DefaultVerticalCRS.ELLIPSOIDAL_HEIGHT, null);
    }

    /** Tests attempt to create DirectPositionImpl with zero-length coordinates and
     non-null CRS*/
    @Test(expected=IllegalArgumentException.class)
    public void zeroLengthCoordinates() {
        new DirectPositionImpl(DefaultVerticalCRS.ELLIPSOIDAL_HEIGHT, new double[0]);
    }

    /** Tests attempt to create DirectPositionImpl with null coordinates */
    @Test
    public void nullCrsTest() {
        DirectPosition dp = new DirectPositionImpl(null, 1.0, 2.0, 3.0);
        assertNull(dp.getCoordinateReferenceSystem());
        assertTrue(Arrays.equals(dp.getCoordinate(), new double[]{1.0, 2.0, 3.0}));
    }

    /**
     * Tests attempt to create DirectPositionImpl with unmatched CRS dimensions
     * and coordinate array length.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testMismatchedDimensions() {
        new DirectPositionImpl(DefaultVerticalCRS.ELLIPSOIDAL_HEIGHT, 1, 2);
    }

    /** Tests index-based access to coordinate values */
    @Test
    public void testIndexBasedAccess1() {
        DirectPosition dp = new DirectPositionImpl(DefaultGeographicCRS.WGS84_3D, 2.0, 3.1, 4.5);
        testIndexBasedAccessHelper(dp);
    }

    /** Tests index-based access to coordinate values with second constructor */
    @Test
    public void testIndexBasedAccess2() {
        DirectPosition dp = new DirectPositionImpl(DefaultGeographicCRS.WGS84_3D,
            new double[]{2.0, 3.1, 4.5});
        testIndexBasedAccessHelper(dp);
    }

    private static void testIndexBasedAccessHelper(DirectPosition dp) {
        assertEquals(dp.getOrdinate(0), 2.0, 0.0);
        assertEquals(dp.getOrdinate(1), 3.1, 0.0);
        assertEquals(dp.getOrdinate(2), 4.5, 0.0);
    }

    /** Tests immutability (1) */
    @Test(expected=UnsupportedOperationException.class)
    public void testImmutability1() {
        DirectPosition dp = new DirectPositionImpl(null, 1.0, 2.0, 3.0);
        dp.setOrdinate(0, 3.4);
    }

    /** Tests immutability (by modifying arrays) */
    @Test
    public void testImmutability2() {
        double[] coords = new double[]{1.1, 2.2, 3.3};
        double[] clonedCoords = coords.clone();
        DirectPosition dp = new DirectPositionImpl(null, coords);
        // Modify the input array, verify that dp returns same value
        coords[1] = 6.6;
        double[] dpCoords = dp.getCoordinate();
        assertTrue(Arrays.equals(dpCoords, clonedCoords));
        // Modify the output array, verify that dp returns same value
        dpCoords[0] = 12.0;
        assertTrue(Arrays.equals(dp.getCoordinate(), clonedCoords));
    }

    /**
     * Tests immutability (by modifying arrays).  This time we test the
     * other constructor of DirectPositionImpl.
     */
    @Test
    public void testImmutability3() {
        double[] otherCoords = new double[]{2.2, 3.3};
        double[] refCoords = new double[]{1.1, 2.2, 3.3};
        DirectPosition dp = new DirectPositionImpl(null, 1.1, otherCoords);
        // Modify the input array, verify that dp returns same value
        otherCoords[1] = 6.6;
        double[] dpCoords = dp.getCoordinate();
        assertTrue(Arrays.equals(dpCoords, refCoords));
        // Modify the output array, verify that dp returns same value
        dpCoords[0] = 12.0;
        assertTrue(Arrays.equals(dp.getCoordinate(), refCoords));
    }

    /**
     * Tests equals() and hashCode() methods
     */
    @Test
    public void testEqualsAndHashCode() {
        // First test with a null CRS
        DirectPosition dp1 = new DirectPositionImpl(null, 2.0, 3.0);
        DirectPosition dp2 = new DirectPositionImpl(null, new double[]{2.0, 3.0});
        checkEqual(dp1, dp2);

        // Now test with a non-null CRS
        DirectPosition dp3 = new DirectPositionImpl(DefaultGeographicCRS.WGS84, 2.0, 3.0);
        DirectPosition dp4 = new DirectPositionImpl(DefaultGeographicCRS.WGS84, new double[]{2.0, 3.0});
        checkEqual(dp3, dp4);

        // Now test for inequality based on CRS
        DirectPosition dp5 = new DirectPositionImpl(DefaultVerticalCRS.ELLIPSOIDAL_HEIGHT, 2.0);
        DirectPosition dp6 = new DirectPositionImpl(DefaultVerticalCRS.GEOIDAL_HEIGHT, 2.0);
        assertFalse(dp5.equals(dp6));

        // Now test for inequality based on coordinate values
        DirectPosition dp7 = new DirectPositionImpl(DefaultGeographicCRS.WGS84_3D, 4, 5, 6);
        DirectPosition dp8 = new DirectPositionImpl(DefaultGeographicCRS.WGS84_3D, 4, 5, 6.01);
        assertFalse(dp7.equals(dp8));

        // Test another implementation of DirectPosition for symmetry
        DirectPosition dp9 = new DirectPosition2D(null, 2.0, 3.0);
        checkEqual(dp1, dp9);

        DirectPosition dp10 = new DirectPosition2D(DefaultGeographicCRS.WGS84, 2.0, 3.0);
        checkEqual(dp3, dp10);
    }

    private static void checkEqual(DirectPosition dp1, DirectPosition dp2) {
        assertTrue(dp1.equals(dp2));
        assertTrue(dp2.equals(dp1));
        assertEquals(dp1.hashCode(), dp2.hashCode());
    }
}
