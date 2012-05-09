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
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import static org.junit.Assert.*;

/**
 * Test of the {@link GridCoordinates2DImpl} class.
 * @todo Test the equals() method
 * @author Jon
 */
public class GridCoordinatesImplTest {

    /** Tests attempt to create GridCoordinates2DImpl with null coordinates */
    @Test(expected=NullPointerException.class)
    public void nullPointerTest() {
        new GridCoordinates2DImpl(null);
    }

    /** Tests attempt to create GridCoordinates2DImpl with zero-length coordinates */
    @Test(expected=IllegalArgumentException.class)
    public void zeroLengthCoordinates() {
        new GridCoordinates2DImpl(new int[0]);
    }

    /** Tests construction of valid objects */
    @Test
    public void testNormalConstruction() {
        GridCoordinates2D gc1 = new GridCoordinates2DImpl(2, 3);
        testNormalConstructionHelper(gc1);
        GridCoordinates2D gc2 = new GridCoordinates2DImpl(new int[]{2, 3});
        testNormalConstructionHelper(gc2);
        assertEquals(gc1, gc2);
    }

    private static void testNormalConstructionHelper(GridCoordinates2D gc) {
        assertEquals(gc.getXIndex(), 2);
        assertEquals(gc.getYIndex(), 3);
    }

    /** Tests hashCode() method */
    @Test
    public void testHashCode() {
        GridCoordinates2D gc = new GridCoordinates2DImpl(5, 6);
        // Hash code of gc is the hash code of its coordinate array
        assertTrue(gc.hashCode() == Arrays.hashCode(new int[]{5, 6}));
    }
    

}