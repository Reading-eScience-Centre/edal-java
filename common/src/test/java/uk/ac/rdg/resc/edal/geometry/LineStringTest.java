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

package uk.ac.rdg.resc.edal.geometry;

import static org.junit.Assert.*;

import java.util.*;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.exceptions.InvalidCrsException;
import uk.ac.rdg.resc.edal.exceptions.InvalidLineStringException;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * Test class for {@link LineString}.
 * 
 * @author Nan
 */
public class LineStringTest {
    private ArrayList<LineString> lineStrings = new ArrayList<LineString>();
    private final CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
    // accuracy constant for comparing double numbers
    private final double delta = 1e-6;

    /**
     * Setting up testing environment: three line strings are put into a
     * container.
     * 
     * @throws InvalidCrsException
     *             if a invalid EPSG is provided.
     * @throws InvalidLineStringException
     *             the line string is not correctly specified.
     * */
    @Before
    public void setUp() throws InvalidCrsException, InvalidLineStringException {
        // the first line string contains 10 lines
        String lineStringSpecOne = "10 20,11 20,12 20,13 20,14 20,15 20,16 20,17 20,18 20,19 20,"
                + "20 20";
        // the second line string contains 6 lines
        String lineStringSpecTwo = "10 20,11 21,12 20,13 21,14 20,15 21,16 20";
        // the third line string contains 6 lines
        String lineStringSpecThree = "10 20,11 21,12 22,13 23,14 24,15 25,16 26";
        lineStrings.add(new LineString(lineStringSpecOne, crs));
        lineStrings.add(new LineString(lineStringSpecTwo, crs));
        lineStrings.add(new LineString(lineStringSpecThree, crs));
    }

    /**
     * Test for {@link LineString#getPointsOnPath} method. The expected lin
     * */
    @Test
    public void testGetPointsOnPath() {
        // pick up points on the line string
        int n = 5;
        List<HorizontalPosition> fivePoints = lineStrings.get(0).getPointsOnPath(n);
        HorizontalPosition midPoint = fivePoints.get(1);
        double xValue = midPoint.getX();

        // the length of the first line string is 10
        double pathLength = 10.0;
        double expectedMidPointXValue = fivePoints.get(0).getX() + pathLength / (n - 1);
        assertEquals(expectedMidPointXValue, xValue, delta);

        n = 3;
        List<HorizontalPosition> threePoints = lineStrings.get(1).getPointsOnPath(n);
        midPoint = threePoints.get(1);
        xValue = midPoint.getX();
        // manually get the mid point x value of the second line string
        expectedMidPointXValue = 13.0;
        assertEquals(expectedMidPointXValue, xValue, delta);

        threePoints = lineStrings.get(2).getPointsOnPath(3);
        midPoint = threePoints.get(1);
        xValue = midPoint.getX();
        // manually get the mid point x value of the second line string
        expectedMidPointXValue = 13.0;
        assertEquals(expectedMidPointXValue, xValue, delta);
    }

    /**
     * Test for {@link LineString#getFractionalControlPointDistance} method.
     * */
    @Test
    public void testgGetFractionalControlPointDistance() {
        int n = 6;
        double fDistance = lineStrings.get(0).getFractionalControlPointDistance(n);
        // notice , the length of the first line string is 10
        double expectedFDistance = (double) n / 10;
        assertEquals(expectedFDistance, fDistance, delta);

        n = 5;
        fDistance = lineStrings.get(1).getFractionalControlPointDistance(n);
        // notice , 6 lines in the second line string
        expectedFDistance = n / 6.0;
        assertEquals(expectedFDistance, fDistance, delta);

        n = 3;
        fDistance = lineStrings.get(2).getFractionalControlPointDistance(n);
        // notice , 6 lines in the third line string
        expectedFDistance = n / 6.0;
        assertEquals(expectedFDistance, fDistance, delta);
    }
}
