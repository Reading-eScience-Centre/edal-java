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

import uk.ac.rdg.resc.edal.Extent;
import java.util.List;
import org.junit.Ignore;
import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.coverage.grid.RegularAxis;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import static org.junit.Assert.*;

/**
 * Contains test methods that are applicable to any instance of a
 * {@link ReferenceableAxis}.
 * @author Jon
 */
@Ignore
public abstract class AbstractReferenceableAxisTest
{
    /** Creates and tests the axis as both a RegularAxisImpl and a ReferenceableAxisImpl.
     Returns the RegularAxis */
    protected static RegularAxis createAndTestRegularAxis(double firstValue, double spacing, int size, boolean isLongitude)
    {
        // Create and test a regular axis
        RegularAxis regAxis = new RegularAxisImpl("", firstValue, spacing, size, isLongitude);
        testReferenceableAxis(regAxis, isLongitude);

        // Now create the same axis as a general ReferenceableAxis
        double[] axisVals = new double[size];
        for (int i = 0; i < size; i++)
        {
            axisVals[i] = firstValue + i * spacing;
        }
        ReferenceableAxis refAxis = createAndTestReferenceableAxis(axisVals, isLongitude);

        // Test that the axes are equal
        assertEquals("Axes are not equal", regAxis.getCoordinateValues(), refAxis.getCoordinateValues());

        return regAxis;
    }
    
    /** Creates and tests the axis as both a RegularAxisImpl and a ReferenceableAxisImpl.
     Returns the RegularAxis */
    protected static ReferenceableAxis<Double> createAndTestReferenceableAxis(double[] axisVals, boolean isLongitude)
    {
        double[] clonedAxisVals = axisVals.clone();

        ReferenceableAxis<Double> refAxis = new ReferenceableAxisImpl("", CollectionUtils.listFromDoubleArray(axisVals), isLongitude);
        testReferenceableAxis(refAxis, isLongitude);

        // Test that the axis values have been preserved
        List<Double> coordValues = refAxis.getCoordinateValues();
        assertEquals(clonedAxisVals.length, coordValues.size());
        for (int i = 0; i < clonedAxisVals.length; i++)
        {
            assertEquals(clonedAxisVals[i], coordValues.get(i).doubleValue(), 0.0);
        }

        return refAxis;
    }
    
    protected static void testReferenceableAxis(ReferenceableAxis<Double> axis, boolean isLongitude)
    {
        testBasicProperties(axis);
        boolean reversed = isReversed(axis);
        testMonotonicity(axis, reversed);
        testExtent(axis, reversed);
        testReverseLookup(axis, isLongitude);
    }

    private static void testBasicProperties(ReferenceableAxis<Double> axis)
    {
        List<Double> coordValues = axis.getCoordinateValues();
        assertNotNull(coordValues);
        assertEquals(axis.size(), coordValues.size());
        assertFalse("axis size is zero", axis.size() == 0);
    }
    
    /** Returns true if the coordinate values are in descending order */
    private static boolean isReversed(ReferenceableAxis<Double> axis)
    {
        List<Double> coordValues = axis.getCoordinateValues();
        if (coordValues.size() == 1) return false;
        return coordValues.get(1) < coordValues.get(0);
    }

    private static void testMonotonicity(ReferenceableAxis<Double> axis, boolean reversed)
    {
        List<Double> coordValues = axis.getCoordinateValues();
        double val = coordValues.get(0);
        for (int i = 1; i < coordValues.size(); i++)
        {
            double val2 = coordValues.get(i);
            if (reversed)
            {
                assertTrue("Axis values not in monotonically descending order", val2 < val);
            }
            else
            {
                assertTrue("Axis values not in monotonically ascending order", val2 > val);
            }
            val = val2;
        }
    }
    
    private static void testExtent(ReferenceableAxis<Double> axis, boolean reversed)
    {
        Extent<Double> extent = axis.getCoordinateExtent();
        assertNotNull("extent is null", extent);
        double min = extent.getLow();
        double max = extent.getHigh();
        if (axis.size() == 1)
        {
            assertTrue("Extent min != max for single-valued axis", min == max);
        }
        else
        {
            // Axis has more than one value
            List<Double> coordValues = axis.getCoordinateValues();
            assertTrue("Extent min not less than max", min < max);
            double firstAxisValue = coordValues.get(0);
            double lastAxisValue = coordValues.get(coordValues.size() - 1);
            double minAxisValue = reversed ? lastAxisValue : firstAxisValue;
            double maxAxisValue = reversed ? firstAxisValue : lastAxisValue;
            assertTrue("Extent min not less than min axis value", min < minAxisValue);
            assertTrue("Extent max not greater than max axis value", max > maxAxisValue);
        }
    }

    /** Tests the getting of coordinate values and their indices */
    private static void testReverseLookup(ReferenceableAxis<Double> axis, boolean isLongitude)
    {
        List<Double> coordValues = axis.getCoordinateValues();
        for (int i = 0; i < coordValues.size(); i++) {
            double value = coordValues.get(i);
            assertEquals(value, axis.getCoordinateValue(i), 0.0);
            assertEquals(i, axis.findIndexOf(value));
            assertEquals(i, coordValues.indexOf(value));
            if (isLongitude) {
                assertEquals(i, axis.findIndexOf(value + 720.0));
                assertEquals(i, axis.findIndexOf(value + 360.0));
                assertEquals(i, axis.findIndexOf(value - 360.0));
                assertEquals(i, axis.findIndexOf(value - 720.0));
            }
        }
    }

}
