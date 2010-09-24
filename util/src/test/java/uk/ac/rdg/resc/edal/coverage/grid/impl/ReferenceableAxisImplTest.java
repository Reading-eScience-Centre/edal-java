/*
 * Copyright (c) 2010 The University of Reading
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

import org.junit.Test;
import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableAxis;
import static org.junit.Assert.*;

/**
 * Test of the {@link ReferenceableAxisImpl} class.
 * @author Jon
 */
public class ReferenceableAxisImplTest extends AbstractReferenceableAxisTest {

    private static double[] NON_MONOTONIC_ARRAY = new double[]{
        1.0, 2.0, 3.0, 2.5, 3.5, 4.5
    };

    private static double[] NON_MONOTONIC_REVERSE_ARRAY = new double[]{
        5.0, 4.0, 3.0, 3.5, 2.0, 1.0
    };

    /** Tests the enforcement of strict monotonicity in axis values */
    @Test(expected=IllegalArgumentException.class)
    public void testMonotonicityArray() {
        new ReferenceableAxisImpl("", NON_MONOTONIC_ARRAY, false);
    }

    /** Tests the enforcement of strict monotonicity in axis values */
    @Test(expected=IllegalArgumentException.class)
    public void testMonotonicityArray2() {
        new ReferenceableAxisImpl("", NON_MONOTONIC_REVERSE_ARRAY, false);
    }

    /** Tests the reverse lookup of all values in the list of coordinate values */
    @Test
    public void testReverseLookup() {
        double[] axisVals = new double[100];

        for (int i = 0; i < axisVals.length; i++) {
            axisVals[i] = -56.45 + i * 2.65;
        }
        createAndTestReferenceableAxis(axisVals, false);

        // Now an axis in reverse order
        for (int i = 0; i < axisVals.length; i++) {
            axisVals[i] = 85.5 - i * 0.5;
        }
        createAndTestReferenceableAxis(axisVals, false);
    }

    /** Test finding nearest coordinate values */
    @Test
    public void testFindNearestCoordValues() {
        ReferenceableAxis axis = createAndTestReferenceableAxis(
             new double[] {0.0, 1.5, 3.5, 6.0, 10.0, 15.0, 25.0, 50.0, 100.0},
             false);

        assertEquals(-1, axis.getNearestCoordinateIndex(-0.76));
        assertEquals(0, axis.getNearestCoordinateIndex(-0.749));
        assertEquals(0, axis.getNearestCoordinateIndex(-0.001));
        assertEquals(0, axis.getNearestCoordinateIndex(0.001));
        assertEquals(0, axis.getNearestCoordinateIndex(0.749));
        assertEquals(1, axis.getNearestCoordinateIndex(0.751));
        assertEquals(7, axis.getNearestCoordinateIndex(74.9));
        assertEquals(8, axis.getNearestCoordinateIndex(75.1));
        assertEquals(8, axis.getNearestCoordinateIndex(99.99));
        assertEquals(8, axis.getNearestCoordinateIndex(100.01));
        assertEquals(8, axis.getNearestCoordinateIndex(124.99));
        assertEquals(-1, axis.getNearestCoordinateIndex(125.01));
    }

    /** Test finding nearest coordinate values */
    @Test
    public void testFindNearestCoordValuesReversedAxis() {
        ReferenceableAxis axis = createAndTestReferenceableAxis(
             new double[] {80, 75, 70, 60, 50, 30, 10, 5},
             false);

        assertEquals(-1, axis.getNearestCoordinateIndex(85));
        assertEquals(-1, axis.getNearestCoordinateIndex(82.51));
        assertEquals(0, axis.getNearestCoordinateIndex(82.49));
        assertEquals(0, axis.getNearestCoordinateIndex(79));
        assertEquals(0, axis.getNearestCoordinateIndex(77.51));
        assertEquals(1, axis.getNearestCoordinateIndex(77.49));
        assertEquals(4, axis.getNearestCoordinateIndex(51));
        assertEquals(5, axis.getNearestCoordinateIndex(39.9));
        assertEquals(6, axis.getNearestCoordinateIndex(11));
        assertEquals(6, axis.getNearestCoordinateIndex(7.51));
        assertEquals(7, axis.getNearestCoordinateIndex(7.49));
        assertEquals(7, axis.getNearestCoordinateIndex(2.51));
        assertEquals(-1, axis.getNearestCoordinateIndex(2.49));
    }


}