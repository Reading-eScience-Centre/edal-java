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
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.Arrays;
import org.junit.Test;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import static org.junit.Assert.*;

/**
 * Test of the {@link GridCoordinatesImpl} class.
 * @todo Test the equals() method
 * @author Jon
 */
public class GridCoordinatesImplTest {

    /** Tests attempt to create GridCoordinatesImpl with null coordinates */
    @Test(expected=NullPointerException.class)
    public void nullPointerTest() {
        new GridCoordinatesImpl(null);
    }

    /** Tests attempt to create GridCoordinatesImpl with zero-length coordinates */
    @Test(expected=IllegalArgumentException.class)
    public void zeroLengthCoordinates() {
        new GridCoordinatesImpl(new int[0]);
    }

    /** Tests construction of valid objects */
    @Test
    public void testNormalConstruction() {
        GridCoordinates gc1 = new GridCoordinatesImpl(2, 3, 4);
        testNormalConstructionHelper(gc1);
        GridCoordinates gc2 = new GridCoordinatesImpl(new int[]{2, 3, 4});
        testNormalConstructionHelper(gc2);
        assertEquals(gc1, gc2);
    }

    private static void testNormalConstructionHelper(GridCoordinates gc) {
        assertTrue(gc.getDimension() == 3);
        assertEquals(gc.getCoordinateValue(0), 2);
        assertEquals(gc.getCoordinateValue(1), 3);
        assertEquals(gc.getCoordinateValue(2), 4);
    }

    /** Tests hashCode() method */
    @Test
    public void testHashCode() {
        GridCoordinates gc = new GridCoordinatesImpl(5, 6, 7, 8);
        // Hash code of gc is the hash code of its coordinate array
        assertTrue(gc.hashCode() == Arrays.hashCode(new int[]{5, 6, 7, 8}));
    }

    /** Tests immutability (1) */
    @Test(expected=UnsupportedOperationException.class)
    public void testImmutability1() {
        GridCoordinates gc = new GridCoordinatesImpl(5, 6, 7, 8);
        gc.setCoordinateValue(1, 4);
    }

    /** Tests immutability (by modifying arrays) */
    @Test
    public void testImmutability2() {
        int[] coords = new int[]{1, 2, 3};
        int[] clonedCoords = coords.clone();
        GridCoordinates gc = new GridCoordinatesImpl(coords);
        // Modify the input array, verify that dp returns same value
        coords[1] = 6;
        int[] gcCoords = gc.getCoordinateValues();
        assertTrue(Arrays.equals(gcCoords, clonedCoords));
        // Modify the output array, verify that dp returns same value
        gcCoords[0] = 12;
        assertTrue(Arrays.equals(gc.getCoordinateValues(), clonedCoords));
    }

    /**
     * Tests immutability (by modifying arrays).  This time we test the
     * other constructor of DirectPositionImpl.
     */
    @Test
    public void testImmutability3() {
        int[] otherCoords = new int[]{2, 3};
        int[] refCoords = new int[]{1, 2, 3};
        GridCoordinates gc = new GridCoordinatesImpl(1, otherCoords);
        // Modify the input array, verify that dp returns same value
        otherCoords[1] = 6;
        int[] gcCoords = gc.getCoordinateValues();
        assertTrue(Arrays.equals(gcCoords, refCoords));
        // Modify the output array, verify that dp returns same value
        gcCoords[0] = 12;
        assertTrue(Arrays.equals(gc.getCoordinateValues(), refCoords));
    }

}