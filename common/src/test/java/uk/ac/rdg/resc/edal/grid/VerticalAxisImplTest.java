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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.domain.Extent;

import java.util.List;
import java.util.ArrayList;

/**
 * Test class for {@link VerticalAxisImpl} and its ancestor.
 * 
 * @author Nan
 * 
 */
public class VerticalAxisImplTest {
    private VerticalCrs height = new VerticalCrsImpl("meter", false, false, true);
    private VerticalAxis vAxis;
    private double heightStep = 100.0;
    private int numberOfHeightValues = 10;
    private List<Double> heightValues;
    private String vAxisName = "elevation";

    /**
     * Initialising.
     */
    @Before
    public void setUp() {
        heightValues = new ArrayList<Double>();
        for (int i = 0; i <= numberOfHeightValues; i++) {
            heightValues.add(i * heightStep);
        }
        vAxis = new VerticalAxisImpl(vAxisName, heightValues, height);
    }

    /**
     * Test the method of {@link VerticalAxisImpl#contains}.
     */
    @Test
    public void testContains() {
        // pick up the fifth height value in the range of v axis
        Double fifthHeight = heightValues.get(5);
        assertTrue(vAxis.contains(fifthHeight));

        assertFalse(vAxis.contains(null));

        // pick up height value out of the extent of v axis
        Double d = 10000.0;
        assertFalse(vAxis.contains(d));
        // pick up a height value out of the extent of v axis
        d = -200.0;
        assertFalse(vAxis.contains(d));
    }

    /**
     * Test the get methods in {@link VerticalAxis}.
     */
    @Test
    public void testGetMethods() {
        assertEquals(height, vAxis.getVerticalCrs());
        Extent<Double> heightExtent = vAxis.getCoordinateExtent();

        /*
         * Values on the axis are mid-points, the extent of the axis should be
         * extended half of the height step toward two directions.
         */
        Double extentLow = heightValues.get(0) - heightStep / 2;
        Double extentHigh = heightValues.get(numberOfHeightValues) + heightStep / 2;
        Extent<Double> expectedHeightExtent = Extents.newExtent(extentLow, extentHigh);
        assertEquals(expectedHeightExtent, heightExtent);

        Array<Extent<Double>> heights = vAxis.getDomainObjects();
        int heightCounter = 0;
        for (Extent<Double> hExtent : heights) {
            expectedHeightExtent = Extents.newExtent(extentLow + heightCounter * heightStep,
                    extentLow + (++heightCounter) * heightStep);
            assertEquals(expectedHeightExtent, hExtent);
        }

        int expectedIndex = 4;
        // the first date is index 0
        Double fifthHeight = vAxis.getCoordinateValue(expectedIndex);
        assertEquals(heightValues.get(expectedIndex), fifthHeight);

        Extent<Double> expectedFifthHeightBound = Extents.newExtent(fifthHeight - heightStep / 2,
                fifthHeight + heightStep / 2);
        assertEquals(expectedFifthHeightBound, vAxis.getCoordinateBounds(expectedIndex));
        assertEquals(heightValues, vAxis.getCoordinateValues());
        assertEquals(vAxisName, vAxis.getName());
    }

    /**
     * Test the method of {@link VerticalAxisImpl#findIndexOf}.
     */
    @Test
    public void testFindIndexOf() {
        // the first date is index 0
        int expectedIndex = 4;
        Double fiftHeightValue = heightValues.get(expectedIndex);
        assertEquals(expectedIndex, vAxis.findIndexOf(fiftHeightValue));
        int notFoundIndex = -1;
        assertEquals(notFoundIndex, vAxis.findIndexOf(Double.NaN));
    }

    /**
     * Test the method of {@link VerticalAxisImpl#isAscending}.
     */
    @Test
    public void testIsAscending() {
        assertTrue(vAxis.isAscending());

        heightValues = new ArrayList<Double>();
        for (int i = 0; i <= numberOfHeightValues; i++) {
            heightValues.add(-i * heightStep);
        }
        vAxis = new VerticalAxisImpl(vAxisName, heightValues, height);
        assertFalse(vAxis.isAscending());
    }

    /**
     * Test the method of {@link VerticalAxisImpl#size}.
     */
    @Test
    public void testSize() {
        assertEquals(numberOfHeightValues + 1, vAxis.size());
    }
}
