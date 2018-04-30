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
package uk.ac.rdg.resc.edal.grid;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * Test class for {@link RegularAxisImpl} and its ancestor.
 * 
 * @author Nan
 * 
 */
public class RegularAxisImplTest {
    private RegularAxis longitudeAxis;
    private RegularAxis latitudeAxis;
    private static final double SPACE = 1.0 / 3;
    private static final int LONGSIZE = 12;
    private static final int LATSIZE = 24;
    // constant for comparing two doubles accuracy
    private static double delta = 1e-6;

    /**
     * Initialise two regular axis.
     */
    @Before
    public void setUp() {
        longitudeAxis = new RegularAxisImpl("longitude", 100, SPACE, LONGSIZE, true);
        latitudeAxis = new RegularAxisImpl("latitude", 20, SPACE, LATSIZE, false);
    }

    /**
     * Test the method of {@link RegularAxis#getCoordinateValue}.
     */
    @Test
    public void testGetCoordinateValue() {
        /*
         * the expected values are drawn from the definition of
         * "CoordinateValue"
         */
        for (int i = 0; i < LONGSIZE; i++) {
            double longPoint = longitudeAxis.getCoordinateValue(i);
            double expectedValue = 100.0 + i * SPACE;
            assertEquals(expectedValue, longPoint, delta);
        }
        for (int i = 0; i < LATSIZE; i++) {
            double latPoint = latitudeAxis.getCoordinateValue(i);
            double expectedValue = 20.0 + i * SPACE;
            assertEquals(expectedValue, latPoint, delta);
        }
    }

    /**
     * Test the method of {@link RegularAxis#getCoordinateBounds}.
     */
    @Test
    public void testGetCoordinateBounds() {
        /*
         * the expected values are drawn from the definition of
         * "CoordinateBounds"
         */
        for (int i = 0; i < LONGSIZE; i++) {
            Extent<Double> bound = longitudeAxis.getCoordinateBounds(i);
            Extent<Double> expectedBound = Extents.newExtent(100.0 + (i - 0.5) * SPACE, 100.0
                    + (i + 0.5) * SPACE);
            /*
             * Extent class not implement equals method, have to compare them as
             * below.
             */
            assertEquals(expectedBound.getLow(), bound.getLow(), delta);
            assertEquals(expectedBound.getHigh(), bound.getHigh(), delta);
        }
        for (int i = 0; i < LATSIZE; i++) {
            Extent<Double> bound = latitudeAxis.getCoordinateBounds(i);
            Extent<Double> expectedBound = Extents.newExtent(20.0 + (i - 0.5) * SPACE, 20.0
                    + (i + 0.5) * SPACE);
            assertEquals(expectedBound.getLow(), bound.getLow(), delta);
            assertEquals(expectedBound.getHigh(), bound.getHigh(), delta);
        }
    }

    /**
     * Test the method of {@link RegularAxis#findIndexOf}.
     */
    @Test
    public void testFindIndexOf() {
        int expectedIrregularValue = -1;
        /*
         * irregular values are out of the bounds of axis or special values like
         * null
         */
        assertEquals(expectedIrregularValue, longitudeAxis.findIndexOf(0.0));
        assertEquals(expectedIrregularValue, longitudeAxis.findIndexOf(105.0));
        assertEquals(expectedIrregularValue, longitudeAxis.findIndexOf(null));
        assertEquals(expectedIrregularValue, longitudeAxis.findIndexOf(Double.NaN));

        assertEquals(expectedIrregularValue, latitudeAxis.findIndexOf(10.0));
        assertEquals(expectedIrregularValue, latitudeAxis.findIndexOf(30.0));
        assertEquals(expectedIrregularValue, latitudeAxis.findIndexOf(null));
        assertEquals(expectedIrregularValue, latitudeAxis.findIndexOf(Double.NaN));

        double step = 1 / 5.0;
        /*
         * a value is between two indices on the axis, the value's index is the
         * one that the value is more closed to it.
         */
        for (int i = 0; i < LONGSIZE * SPACE / step; i++) {
            double value = 100 + i * step;
            int index = longitudeAxis.findIndexOf(value);
            int expectedIndex = (int) Math.round(i * step / SPACE);
            assertEquals(expectedIndex, index);
        }
        for (int i = 0; i < LATSIZE * SPACE / step; i++) {
            int index = latitudeAxis.findIndexOf(20 + i * step);
            int expectedIndex = (int) Math.round(i * step / SPACE);
            assertEquals(expectedIndex, index);
        }
    }

    /**
     * Test the method of {@link RegularAxis#findIndexOfUnconstrained} by
     * providing strange arguments, which should throw an
     * illegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFindIndexOfUnconstrainedException() {
        // the statement below catches illegalArgumentException
        Exception caughtEx = null;
        try {
            longitudeAxis.findIndexOfUnconstrained(null);
        } catch (IllegalArgumentException e) {
            caughtEx = e;
        }
        assertNotNull(caughtEx);
        // the statement below throws illegalArgumentException
        latitudeAxis.findIndexOfUnconstrained(Double.NaN);
    }

    /**
     * Test the method of {@link RegularAxis#findIndexOfUnconstrained} by
     * providing normal arguments.
     */
    @Test
    public void testFindIndexOfUnconstrained() {
        double step = 1 / 5.0;
        // maximum number of points are tested
        int maxPosNo = 101;
        for (int i = 0; i < maxPosNo; i++) {
            double position = -20.0 + i * step;
            int expectPos = (int) Math.round((position - 20.0) / SPACE);
            assertEquals(expectPos, latitudeAxis.findIndexOfUnconstrained(position));
        }
        for (int i = 0; i < maxPosNo; i++) {
            double position = -20.0 + i * step;
            int expectPos = (int) Math.round((position - 100.0) / SPACE);
            assertEquals(expectPos, longitudeAxis.findIndexOfUnconstrained(position));
        }
        // longitude can be warped, remember -10 and 350 are identical position.
        for (int i = 0; i < maxPosNo; i++) {
            double position = 420.0 + i * step;
            int expectPos = (int) Math.round((position - 360.0 - 100.0) / SPACE);
            assertEquals(expectPos, longitudeAxis.findIndexOfUnconstrained(position));
        }
    }
}
