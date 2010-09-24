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

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import uk.ac.rdg.resc.edal.coverage.grid.RegularAxis;
import static org.junit.Assert.*;

/**
 * Test of the {@link RegularAxisImpl} class.
 * @author Jon
 */
public class RegularAxisImplTest extends AbstractReferenceableAxisTest {

    /** Tests the creation of the List of coordinate values */
    @Test
    public void testListGeneration() {
        RegularAxis regAxis = new RegularAxisImpl("", 0.0, 1.0, 10, false);
        testReferenceableAxis(regAxis, false);
        List<Double> testList = Arrays.asList(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0);
        assertEquals(testList, regAxis.getCoordinateValues());
    }

    /** Tests the reverse lookup of all values in the list of coordinate values */
    @Test
    public void testSomeAxes() {
        // First an axis with a positive spacing
        createAndTestRegularAxis(-25.4, 0.851, 100, false);
        // Now an axis with a negative spacing
        createAndTestRegularAxis(-80.0, -0.5, 320, false);
    }

    /** Test the behaviour for longitude axes */
    @Test
    public void testLongitudeAxes() {

        RegularAxis regAxis = createAndTestRegularAxis(0.0, 1.0, 360, true);
        assertEquals(-1, regAxis.getCoordinateIndex(0.5));

        regAxis = createAndTestRegularAxis(-180.0, 0.5, 720, true);
        assertEquals(-1, regAxis.getCoordinateIndex(0.25));
    }

    /** Tests the use of getNearestCoordinateIndex() */
    @Test
    public void testGetNearestCoordinateIndex() {
        RegularAxis regAxis = createAndTestRegularAxis(0.0, 1.0, 10, false);
        assertEquals(-1, regAxis.getNearestCoordinateIndex(-0.51));
        assertEquals(0, regAxis.getNearestCoordinateIndex(-0.49));
        assertEquals(0, regAxis.getNearestCoordinateIndex(-0.01));
        assertEquals(0, regAxis.getNearestCoordinateIndex(0.0));
        assertEquals(0, regAxis.getNearestCoordinateIndex(0.01));
        assertEquals(0, regAxis.getNearestCoordinateIndex(0.499));
        assertEquals(1, regAxis.getNearestCoordinateIndex(0.501));
        assertEquals(1, regAxis.getNearestCoordinateIndex(1.499));
        assertEquals(2, regAxis.getNearestCoordinateIndex(1.501));
        assertEquals(8, regAxis.getNearestCoordinateIndex(8.499));
        assertEquals(9, regAxis.getNearestCoordinateIndex(8.501));
        assertEquals(9, regAxis.getNearestCoordinateIndex(9.499));
        assertEquals(-1, regAxis.getNearestCoordinateIndex(9.501));
    }

    /** Tests the use of getNearestCoordinateIndex() */
    @Test
    public void testGetNearestCoordinateIndexNegativeSpacing() {
        RegularAxis regAxis = createAndTestRegularAxis(90.0, -1.0, 181, false);
        assertEquals(-1, regAxis.getNearestCoordinateIndex(90.51));
        assertEquals(0, regAxis.getNearestCoordinateIndex(90.49));
        assertEquals(0, regAxis.getNearestCoordinateIndex(89.51));
        assertEquals(1, regAxis.getNearestCoordinateIndex(89.49));
        assertEquals(89, regAxis.getNearestCoordinateIndex(0.51));
        assertEquals(90, regAxis.getNearestCoordinateIndex(0.49));
        assertEquals(90, regAxis.getNearestCoordinateIndex(0.0));
        assertEquals(90, regAxis.getNearestCoordinateIndex(-0.49));
        assertEquals(91, regAxis.getNearestCoordinateIndex(-0.51));
        assertEquals(180, regAxis.getNearestCoordinateIndex(-90.49));
        assertEquals(-1, regAxis.getNearestCoordinateIndex(-90.51));
    }

    /** Tests the use of getNearestCoordinateIndex() for longitude axes */
    @Test
    public void testGetNearestCoordinateIndexLonAxis() {
        // An axis that spans the globe
        RegularAxis regAxis = createAndTestRegularAxis(0.0, 1.0, 360, true);
        assertEquals(359, regAxis.getNearestCoordinateIndex(-0.51));
        assertEquals(0, regAxis.getNearestCoordinateIndex(-0.49));
        assertEquals(0, regAxis.getNearestCoordinateIndex(-0.01));
        assertEquals(0, regAxis.getNearestCoordinateIndex(0.0));
        assertEquals(0, regAxis.getNearestCoordinateIndex(0.01));
        assertEquals(0, regAxis.getNearestCoordinateIndex(0.499));
        assertEquals(2, regAxis.getNearestCoordinateIndex(1.501));
        assertEquals(359, regAxis.getNearestCoordinateIndex(359.499));
        assertEquals(0, regAxis.getNearestCoordinateIndex(359.501));
        assertEquals(180, regAxis.getNearestCoordinateIndex(180.0));
        assertEquals(180, regAxis.getNearestCoordinateIndex(-180.0));
        assertEquals(180, regAxis.getNearestCoordinateIndex(540.0));
        assertEquals(180, regAxis.getNearestCoordinateIndex(-540.0));
    }

}