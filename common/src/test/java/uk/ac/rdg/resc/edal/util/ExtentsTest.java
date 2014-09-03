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

import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.domain.*;

import java.util.*;

/**
 * Test class for {@link Extents}.
 * 
 * @author Nan
 * 
 */
public class ExtentsTest {
    private List<Integer> intList = new ArrayList<Integer>();
    private List<Double> doubleList = new LinkedList<Double>();
    private Set<Float> floatSet = new HashSet<Float>();
    private Vector<Long> longVector = new Vector<Long>();
    private static int SIZE = 10;

    /**
     * Initializing.
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
     * Test the method of {@link Extents#contains}. Pick up special values like
     * Double.NaN, or points on the border.
     */
    @Test
    public void testContains() {
        Extent<Integer> intExtent = Extents.findMinMax(intList);
        assertFalse(intExtent.contains(null));
        assertFalse(intExtent.contains(30));
        assertTrue(intExtent.contains(0));
        assertTrue(intExtent.contains(9));

        Extent<Double> doubleExtent = Extents.findMinMax(doubleList);
        assertFalse(doubleExtent.contains(null));
        assertFalse(doubleExtent.contains(Double.NaN));
        assertFalse(doubleExtent.contains(30.0));
        assertTrue(doubleExtent.contains(0.0));
        assertFalse(doubleExtent.contains(-0.0));
        assertTrue(doubleExtent.contains(9.0));
    }
}
