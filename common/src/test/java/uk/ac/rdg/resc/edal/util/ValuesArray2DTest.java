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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Test class for {@link ValueArray2D}.
 * 
 * @author Nan
 * 
 */
public class ValuesArray2DTest {

    private ValuesArray2D data;

    private static final int XSIZE = 5;
    private static final int YSIZE = 7;

    /**
     * Initialising the 2D values array.
     */
    @Before
    public void setUp() {
        data = new ValuesArray2D(YSIZE, XSIZE);
        for (int i = 0; i < YSIZE; i++) {
            for (int j = 0; j < XSIZE; j++) {
                data.set(1 * i + j, i, j);
            }
        }
    }

    /**
     * Test the method of {@link ValueArray2D#iterator}.
     */
    @Test
    public void testIterator() {
        // Expected values are drawn via the initializing process.
        List<Number> expected = new ArrayList<Number>();
        for (int i = 0; i < YSIZE; i++) {
            for (int j = 0; j < XSIZE; j++) {
                expected.add(1 * i + j);
            }
        }

        Iterator<Number> iterator = data.iterator();
        int index = 0;
        while (iterator.hasNext() && index < data.size()) {
            assertEquals(expected.get(index), iterator.next());
            index++;
        }
    }

    /**
     * Test the methods of {@link ValueArray2D#get} and {@link ValueArray2D#set}
     * .
     */
    @Test
    public void testGetSet() {
        /*
         * Set values at given positions then compare two values. The type of
         * values is Number, so we try all primary types like float, double,
         * byte, short, etc.
         */
        float f = 12.0f;
        data.set(f, 2, 3);
        assertEquals(f, data.get(2, 3).doubleValue(), 1e-6);

        long longInt = 123456789999999999L;
        data.set(longInt, 1, 3);
        assertEquals(longInt, data.get(1, 3).longValue());

        int i = 5;
        data.set(i, 2, 2);
        assertEquals(i, data.get(2, 2).longValue());

        short s = (short) 100000;
        data.set(s, 1, 2);
        assertEquals(s, data.get(1, 2));

        byte b = 120;
        data.set(b, 1, 4);
        assertEquals(b, data.get(1, 4));

        double d = 129.9998;
        data.set(d, 2, 4);
        assertEquals(d, data.get(2, 4).doubleValue(), 1e-6);
        
        d = Double.NaN;
        data.set(d, 2, 4);
        assertEquals(d, data.get(2, 4).doubleValue(), 1e-6);

        data.set(null, 6, 4);
        assertEquals(null, data.get(6, 4));
    }
}
