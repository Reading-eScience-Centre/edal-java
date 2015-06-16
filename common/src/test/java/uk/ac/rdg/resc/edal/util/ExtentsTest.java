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

package uk.ac.rdg.resc.edal.util;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.domain.*;
import uk.ac.rdg.resc.edal.util.chronologies.NoLeapChronology;

import java.util.*;

/**
 * Test class for {@link Extents}.
 * 
 * @author Nan
 */
public class ExtentsTest {
    private List<Integer> intList = new ArrayList<Integer>();
    private List<Double> doubleList = new LinkedList<Double>();
    private Set<Float> floatSet = new HashSet<Float>();
    private Vector<Long> longVector = new Vector<Long>();
    private static int SIZE = 10;

    /**
     * Initializing.
     * 
     * @author Nan
     */
    @Before
    public void setUp() {
        for (int i = 0; i < SIZE; i++) {
            intList.add(i);
            doubleList.add(i * 1.0);
            floatSet.add(i * 1.0f);
            longVector.add(i * 1L);
        }
    }

    /**
     * Test the method of {@link Extents#findMinMax}.
     * 
     * @author Nan
     */
    @Test
    public void testFindMinMax() {
        /*
         * the following expected extents are drawn from the initializing
         * process.
         */
        Extent<Integer> expectedIntExtent = Extents.newExtent(0, 9);
        Extent<Double> expectedDoubleExtent = Extents.newExtent(0.0, 9.0);
        Extent<Float> expectedFloatExtent = Extents.newExtent(0.0f, 9.0f);
        Extent<Long> expectedLongExtent = Extents.newExtent(0L, 9L);

        assertEquals(expectedIntExtent, Extents.findMinMax(intList));
        assertEquals(expectedDoubleExtent, Extents.findMinMax(doubleList));
        assertEquals(expectedFloatExtent, Extents.findMinMax(floatSet));
        assertEquals(expectedLongExtent, Extents.findMinMax(longVector));
    }

    /**
     * Tests {@link Extent#contains} for int, double, open-ended (at lower end,
     * upper end, and both ends), datetime, and empty extents.
     * 
     * @author Guy Griffiths
     */
    @Test
    public void testContains() {
        /*
         * Test Integer Extent
         */
        Extent<Integer> intExtent = Extents.newExtent(-10, 10);
        assertTrue(intExtent.contains(-10));
        assertTrue(intExtent.contains(-5));
        assertTrue(intExtent.contains(0));
        assertTrue(intExtent.contains(5));
        assertTrue(intExtent.contains(10));

        assertFalse(intExtent.contains(-11));
        assertFalse(intExtent.contains(11));
        assertFalse(intExtent.contains(null));

        /*
         * Check Double Extent
         */
        Extent<Double> doubleExtent = Extents.newExtent(-10.0, 10.0);
        assertTrue(doubleExtent.contains(-10.0));
        assertTrue(doubleExtent.contains(-5.0));
        assertTrue(doubleExtent.contains(0.0));
        assertTrue(doubleExtent.contains(5.0));
        assertTrue(doubleExtent.contains(10.0));

        assertFalse(doubleExtent.contains(-11.0));
        assertFalse(doubleExtent.contains(11.0));
        assertFalse(doubleExtent.contains(-10.0000000001));
        assertFalse(doubleExtent.contains(10.0000000001));
        assertFalse(doubleExtent.contains(null));
        assertFalse(doubleExtent.contains(Double.NaN));
        assertFalse(doubleExtent.contains(Double.POSITIVE_INFINITY));
        assertFalse(doubleExtent.contains(Double.NEGATIVE_INFINITY));

        /*
         * Check Extent with no lower bound
         */
        Extent<Double> openLowerExtent = Extents.newExtent(null, 10.0);
        for (double i = 10.0; i > -100.0; i -= 0.5) {
            assertTrue(openLowerExtent.contains(i));
        }
        assertFalse(openLowerExtent.contains(11.0));
        assertFalse(openLowerExtent.contains(10.0000000001));
        assertFalse(openLowerExtent.contains(Double.NaN));
        assertFalse(openLowerExtent.contains(Double.POSITIVE_INFINITY));
        assertTrue(openLowerExtent.contains(Double.NEGATIVE_INFINITY));

        /*
         * Check Extent with no upper bound
         */
        Extent<Double> openUpperExtent = Extents.newExtent(-10.0, null);
        for (double i = -10.0; i < 100.0; i += 0.5) {
            assertTrue(openUpperExtent.contains(i));
        }
        assertFalse(openUpperExtent.contains(-11.0));
        assertFalse(openUpperExtent.contains(-10.0000000001));
        assertFalse(openUpperExtent.contains(Double.NaN));
        assertFalse(openUpperExtent.contains(Double.NEGATIVE_INFINITY));
        assertTrue(openUpperExtent.contains(Double.POSITIVE_INFINITY));

        /*
         * Check Extent with no bounds
         */
        Extent<Double> openExtent = Extents.newExtent(null, null);
        for (double i = -100.0; i < 100.0; i += 0.5) {
            assertTrue(openExtent.contains(i));
        }
        assertFalse(openExtent.contains(Double.NaN));
        assertTrue(openExtent.contains(Double.NEGATIVE_INFINITY));
        assertTrue(openExtent.contains(Double.POSITIVE_INFINITY));

        /*
         * Check DateTime Extents
         */
        Extent<DateTime> datetimeExtent = Extents.newExtent(new DateTime(2015, 5, 1, 0, 0),
                new DateTime(2015, 6, 1, 0, 0));
        assertTrue(datetimeExtent.contains(new DateTime(2015, 5, 1, 0, 0)));
        assertTrue(datetimeExtent.contains(new DateTime(2015, 5, 15, 0, 0)));
        assertTrue(datetimeExtent.contains(new DateTime(2015, 6, 1, 0, 0)));
        assertFalse(datetimeExtent.contains(new DateTime(2015, 4, 30, 23, 59, 59, 999)));
        assertFalse(datetimeExtent.contains(new DateTime(2015, 6, 1, 0, 1)));
        assertFalse(datetimeExtent.contains(new DateTime(2015, 5, 15, 0, 0, NoLeapChronology
                .getInstanceUTC())));

        Extent<DateTime> datetimeOpenHighExtent = Extents.newExtent(new DateTime(2015, 5, 1, 0, 0),
                null);
        assertTrue(datetimeOpenHighExtent.contains(new DateTime(2015, 5, 1, 0, 0)));
        assertTrue(datetimeOpenHighExtent.contains(new DateTime(2015, 5, 15, 0, 0)));
        assertTrue(datetimeOpenHighExtent.contains(new DateTime(2015, 6, 1, 0, 0)));
        assertFalse(datetimeOpenHighExtent.contains(new DateTime(2015, 4, 30, 23, 59, 59, 999)));
        assertFalse(datetimeOpenHighExtent.contains(new DateTime(2015, 5, 15, 0, 0,
                NoLeapChronology.getInstanceUTC())));
        DateTime testDate = new DateTime(2015, 6, 1, 0, 0);
        for (int i = 0; i < 1000; i++) {
            /*
             * Gets geometrically later, which is fine
             */
            testDate = testDate.plusDays(i);
            assertTrue(datetimeOpenHighExtent.contains(testDate));
        }

        Extent<DateTime> datetimeOpenLowExtent = Extents.newExtent(null, new DateTime(2015, 6, 1,
                0, 0));
        assertTrue(datetimeOpenLowExtent.contains(new DateTime(2015, 5, 1, 0, 0)));
        assertTrue(datetimeOpenLowExtent.contains(new DateTime(2015, 5, 15, 0, 0)));
        assertTrue(datetimeOpenLowExtent.contains(new DateTime(2015, 6, 1, 0, 0)));
        assertFalse(datetimeOpenLowExtent.contains(new DateTime(2015, 5, 15, 0, 0, NoLeapChronology
                .getInstanceUTC())));
        assertFalse(datetimeExtent.contains(new DateTime(2015, 6, 1, 0, 1)));
        testDate = new DateTime(2015, 5, 1, 0, 0);
        for (int i = 0; i < 1000; i++) {
            /*
             * Gets geometrically earlier, which is fine
             */
            testDate = testDate.minusDays(i);
            assertTrue(datetimeOpenLowExtent.contains(testDate));
        }
        
        Extent<DateTime> datetimeOpenExtent = Extents.newExtent(null, null);
        for (int i = 0; i < 3000; i++) {
            testDate = new DateTime(i, 1, 1, 0, 0);
            assertTrue(datetimeOpenExtent.contains(testDate));
        }

        /*
         * Check empty Extents
         */
        Extent<Double> emptyDoubleExtent = Extents.emptyExtent();
        Random r = new Random();
        for (int i = 0; i < 100; i++) {
            assertFalse(emptyDoubleExtent.contains(r.nextDouble()));
        }
        Extent<Integer> emptyIntegerExtent = Extents.emptyExtent();
        for (int i = 0; i < 100; i++) {
            assertFalse(emptyIntegerExtent.contains(r.nextInt()));
        }
    }
}
