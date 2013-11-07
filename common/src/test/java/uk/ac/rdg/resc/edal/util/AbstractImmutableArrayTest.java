/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class AbstractImmutableArrayTest {

    private Array<Float> array;

    private static final int XSIZE = 5;
    private static final int YSIZE = 7;

    @Before
    public void setUp() throws Exception {
        array = new AbstractImmutableArray<Float>(Float.class, YSIZE, XSIZE) {
            @Override
            public Float get(int... coords) {
                int x = coords[1];
                int y = coords[0];
                return y + 0.1f * x;
            }

            @Override
            public Class<Float> getValueClass() {
                return Float.class;
            }
        };

    }

    @Test
    public void testIterator() {
        /*
         * This tests that the iterator for an AbstractImmutableArray behaves
         * properly (correct values, and varies the final dimension fastest)
         */
        List<Float> expected = new ArrayList<Float>();
        for (int j = 0; j < YSIZE; j++) {
            for (int i = 0; i < XSIZE; i++) {
                expected.add(j + 0.1f * i);
            }
        }

        Iterator<Float> iterator = array.iterator();
        int index = 0;
        while (iterator.hasNext() && index < 1000) {
            assertEquals(expected.get(index), iterator.next());
            index++;
        }
    }
    
    @Test
    public void testGetValueClass() {
        assertEquals(Float.class, array.getValueClass());
    }
    
    @Test
    public void testGet() {
        /*
         * This tests that the get method returns the expected values
         */
        for (int j = 0; j < YSIZE; j++) {
            for (int i = 0; i < XSIZE; i++) {
                Float expected = j + 0.1f * i;
                assertEquals(expected, array.get(j, i));
            }
        }
    }
}
