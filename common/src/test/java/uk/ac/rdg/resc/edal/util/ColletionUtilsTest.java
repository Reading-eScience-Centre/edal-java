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

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.util.CollectionUtils;

/**
 * Test class for {@link CollectionUtils}
 * 
 * @author Nan
 * 
 */
public class ColletionUtilsTest {
    private static int SIZE = 100;
    private float[] floatData = new float[SIZE];
    private Float[] floatDataAsObject = new Float[SIZE];
    private double[] doubleData = new double[SIZE];
    private Double[] doubleDataAsObject = new Double[SIZE];
    private Set<Double> doubleDataSet = new HashSet<Double>();
    private Set<Float> floatDataSet = new HashSet<Float>();
    // constant for assert equal comparing two doubles
    private double delta = 1e-6;

    /**
     * Initialising.
     */
    @Before
    public void setUp() {
        for (int i = 0; i < SIZE - 10; i++) {
            floatData[i] = 1.0f * i + 1.0f;
            doubleData[i] = 1.0 * i + 1.0;
            floatDataAsObject[i] = 1.0f * i + 1.0f;
            doubleDataAsObject[i] = 1.0 * i + 1.0;
            floatDataSet.add(1.0f * i + 1.0f);
            doubleDataSet.add(1.0 * i + 1.0);
        }
        floatDataSet.add(null);
        doubleDataSet.add(null);
    }

    /**
     * Test the method of{@link CollectionUtils#listFromFloatArray}.
     */
    @Test
    public void testListFromFloatArray() {
        List<Float> floatDataAsList = CollectionUtils.listFromFloatArray(floatData);
        assertEquals(floatData.length, floatDataAsList.size());
        /*
         * the array and the list should map to each other according to the
         * index.
         */
        for (int i = 0; i < floatData.length; i++) {
            assertEquals(floatData[i], floatDataAsList.get(i), delta);
        }
    }

    /**
     * Test the method of{@link CollectionUtils#listFromDoubleArray}.
     */
    @Test
    public void testListFromDoubleArray() {
        List<Double> doubleDataAsList = CollectionUtils.listFromDoubleArray(doubleData);
        assertEquals(doubleData.length, doubleDataAsList.size());
        for (int i = 0; i < doubleData.length; i++) {
            assertEquals(doubleData[i], doubleDataAsList.get(i), delta);
        }
    }

    /**
     * Test the method of{@link CollectionUtils#setOf}.
     */
    @Test
    public void testSetOf() {
        Set<Float> floatSet = CollectionUtils.setOf(floatDataAsObject);
        Set<Double> doubleSet = CollectionUtils.setOf(doubleDataAsObject);
        assertEquals(floatDataSet, floatSet);
        assertEquals(doubleDataSet, doubleSet);
    }

    @Test(expected = NullPointerException.class)
    public void testNull() {
        // the statement below catches NullPointerException
        try {
            CollectionUtils.listFromFloatArray(null);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
        // the statement below throws NullPointerException
        CollectionUtils.listFromDoubleArray(null);
    }
}
