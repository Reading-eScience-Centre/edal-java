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

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.domain.Extent;

/**
 * Test class for {@link ReferenceableAxixImpl} and its ancestor.
 * 
 * @author Nan Lin
 * 
 */
public class ReferenceableAxisImplTest {

    private ReferenceableAxisImpl longAxis;
    private ReferenceableAxisImpl latAxis;
    private double[] latValues = { 20.0, 20.5, 20.8, 23.0, 24.0, 24.2, 24.3, 30.0 };
    private double[] longValues = { 50.0, 51.3, 53.9, 55.4, 57.9, 66.9, 74.9, 80.4 };
    // constant for comparing two doubles accuracy
    private static double delta = 1e-10;

    /**
     * Initialize two referenceable axis.
     */
    @Before
    public void setUp() {
        List<Double> longAxisValues = CollectionUtils.listFromDoubleArray(longValues);
        longAxis = new ReferenceableAxisImpl("longitude", longAxisValues, true);
        List<Double> latAxisValues = CollectionUtils.listFromDoubleArray(latValues);
        latAxis = new ReferenceableAxisImpl("latitude", latAxisValues, false);
    }

    /**
     * Test the method of {@link ReferenceableAxixImpl#findIndexOf}.
     */
    @Test
    public void testFindIndexOf() {
        assertEquals(-1, latAxis.findIndexOf(15.0));
        assertEquals(-1, latAxis.findIndexOf(135.0));
        assertEquals(-1, longAxis.findIndexOf(45.0));
        assertEquals(-1, longAxis.findIndexOf(85.0));
        assertEquals(-1, longAxis.findIndexOf(null));
        assertEquals(-1, latAxis.findIndexOf(Double.NaN));

        assertEquals(1, latAxis.findIndexOf(20.4));
        assertEquals(0, latAxis.findIndexOf(20.2));
        assertEquals(3, latAxis.findIndexOf(23.5));

        assertEquals(6, longAxis.findIndexOf(75.0));
        assertEquals(1, longAxis.findIndexOf(51.3));
        assertEquals(4, longAxis.findIndexOf(58.0));
    }

    /**
     * Test the method of {@link ReferenceableAxixImpl#contains}.
     */
    @Test
    public void testContains() {
        assertTrue(longAxis.contains(60.1));
        assertFalse(latAxis.contains(40.2));
        assertFalse(latAxis.contains(null));
    }

    // ExtendLastValue test is ignored as it's similar with this one.
    /**
     * Test the method of {@link ReferenceableAxixImpl#extendFirstValue}.
     */
    @Test
    public void testExtendFirstValue() {
        double first = 23.8;
        double next = 23.6;
        assertEquals(first - (next - first) / 2.0, longAxis.extendFirstValue(first, next), delta);

        next = Double.POSITIVE_INFINITY;
        assertEquals(first - (next - first) / 2.0, longAxis.extendFirstValue(first, next), delta);

        first = Double.MAX_VALUE;
        next = 73.6;
        assertEquals(first - (next - first) / 2.0, latAxis.extendFirstValue(first, next), delta);

        first = Double.NaN;
        next = 53.6;
        assertEquals(first - (next - first) / 2.0, longAxis.extendFirstValue(first, next), delta);
    }

    /**
     * Test the get methods in {@link ReferenceableAxixImpl}.
     */
    @Test
    public void testGetMethods() {
        assertEquals(longValues[0], longAxis.getCoordinateValue(0), delta);

        double spaceHead = longValues[3] - longValues[2];
        double spaceTail = longValues[4] - longValues[3];
        assertEquals(
                Extents.newExtent(longValues[2] + spaceHead / 2.0, longValues[3] + spaceTail / 2.0),
                longAxis.getCoordinateBounds(3));

        double pos1 = 55.8;
        double pos2 = 65.9;
        assertEquals((pos1 + pos2) / 2.0, latAxis.getMidpoint(pos1, pos2), delta);
        assertEquals(Double.NaN, latAxis.difference(25.0, Double.NaN), delta);
        assertEquals(latValues[latValues.length - 1], latAxis.getMaximumValue(), delta);
        assertEquals(longValues[0], longAxis.getMinimumValue(), delta);

        double low = latValues[0] - (latValues[1] - latValues[0]) / 2.0;
        double high = latValues[0] + (latValues[1] - latValues[0]) / 2.0;
        Extent<Double> expectedDomainObject = Extents.newExtent(low, high);
        Extent<Double> realThing = latAxis.getDomainObjects().get(0);
        assertEquals(expectedDomainObject, realThing);

        for (int i = 1; i < latValues.length - 1; i++) {
            double space = latValues[i + 1] - latValues[i];
            low = high;
            high = latValues[i] + space / 2.0;
            expectedDomainObject = Extents.newExtent(low, high);
            realThing = latAxis.getDomainObjects().get(i);
            assertEquals(expectedDomainObject, realThing);
        }
    }

    /**
     * Test the method of {@link ReferenceableAxixImpl#size}.
     */
    @Test
    public void testGetSize() {
        assertEquals(longValues.length, longAxis.size());
        assertEquals(latValues.length, latAxis.size());
    }

    /**
     * Test the method of {@link ReferenceableAxixImpl#isAscending}.
     */
    @Test
    public void testIsAscending() {
        assertTrue(longAxis.isAscending());
        assertTrue(latAxis.isAscending());
    }
}
