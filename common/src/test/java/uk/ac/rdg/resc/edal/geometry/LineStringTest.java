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

import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.exceptions.InvalidCrsException;
import uk.ac.rdg.resc.edal.exceptions.InvalidLineStringException;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Test class for {@link LineString}.
 *
 * @author Nan
 */
public class LineStringTest {
    private ArrayList<LineString> lineStrings = new ArrayList<LineString>();
    private final CoordinateReferenceSystem crs = GISUtils.defaultGeographicCRS();
    // accuracy constant for comparing double numbers
    private final double delta = 1e-6;

    /**
     * Setting up testing environment: three line strings are put into a
     * container.
     *
     * @throws InvalidCrsException
     *             if a invalid EPSG is provided.
     * @throws InvalidLineStringException
     *             if the line string is not correctly specified.
     * */
    @Before
    public void setUp() throws InvalidCrsException, InvalidLineStringException {
        // the first line string contains 10 lines
        String lineStringSpecOne = "10 20,11 20,12 20,13 20,14 20,15 20,16 20,17 20,18 20,19 20,"
                + "20 20";
        // the second line string contains 6 lines
        String lineStringSpecTwo = "10 20,11 21,12 20,13 21,14 20,15 21,16 20";

        // the third line string contains 6 lines
        String lineStringSpecThree = "10 20,11 21,12 22,13 23,14 24,16 26,20 30";
        lineStrings.add(new LineString(lineStringSpecOne, crs));
        lineStrings.add(new LineString(lineStringSpecTwo, crs));
        lineStrings.add(new LineString(lineStringSpecThree, crs));
    }

    /**
     * Test for {@link LineString#getPointsOnPath} method.
     */
    @Test
    public void testGetPointsOnPath() {
        // pick up five points on the line string one.
        int n = 5;
        List<HorizontalPosition> fivePoints = lineStrings.get(0).getPointsOnPath(n);

        /*
         * only consider x values as all y values are identical (they are on the
         * line)
         */
        List<Double> fivePointsXValues = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            fivePointsXValues.add(fivePoints.get(i).getX());
        }
        // the length of the first line string is 10
        double pathLength = 10.0;
        List<Double> expectedFivePointsXValues = new ArrayList<>();

        // x value for the first control point
        double firstControlPointXValue = lineStrings.get(0).getControlPoints().get(0).getX();
        expectedFivePointsXValues.add(firstControlPointXValue);

        for (int i = 1; i < n - 1; i++) {
            expectedFivePointsXValues.add(firstControlPointXValue + i * pathLength / (n - 1));
        }
        // index for the last control point on the line string one
        int lastOneIndex = 10;

        // x value for the last control point is added
        expectedFivePointsXValues.add(lineStrings.get(0).getControlPoints().get(lastOneIndex)
                .getX());
        assertEquals(expectedFivePointsXValues, fivePointsXValues);

        // pick up three points on the line string two.
        n = 3;
        List<HorizontalPosition> threePoints = lineStrings.get(1).getPointsOnPath(3);

        List<HorizontalPosition> expectedThreePoints = new ArrayList<>();
        /*
         * Add control points at the specified positions (0, 3, 6) according to
         * manually calculation.
         */
        expectedThreePoints.add(lineStrings.get(1).getControlPoints().get(0));
        expectedThreePoints.add(lineStrings.get(1).getControlPoints().get(3));
        expectedThreePoints.add(lineStrings.get(1).getControlPoints().get(6));
        assertEquals(expectedThreePoints, threePoints);

        // pick up four points on the line string three.
        n = 4;
        List<HorizontalPosition> fourPoints = lineStrings.get(2).getPointsOnPath(4);

        List<HorizontalPosition> expectedFourPoints = new ArrayList<>();
        /*
         * Notice, the points (x,y) on the third line string is defined by
         * y=x+10. Apart from the first and last control point, we need to find
         * another two points. These two points are at one third and two third
         * position. By manual calculation, they are at (13.333, 23.333) and
         * (16.667, 26.667). There's a break on the third line string.
         */

        // add the first control point
        expectedFourPoints.add(new HorizontalPosition(10.0, 20.0, crs));

        expectedFourPoints.add(new HorizontalPosition(13.0 + 1.0 / 3.0, 13.0 + 1.0 / 3.0 + 10.0,
                crs));
        expectedFourPoints.add(new HorizontalPosition(16.0 + 2.0 / 3.0, 16.0 + 2.0 / 3.0 + 10.0,
                crs));

        // add the last control point
        expectedFourPoints.add(new HorizontalPosition(20.0, 30.0, crs));

        // the type of fourPoints isn't ArrayList. Have to compare as below:
        for (int i = 0; i < n; i++) {
            assertEquals(expectedFourPoints.get(i).getX(), fourPoints.get(i).getX(), delta);
            assertEquals(expectedFourPoints.get(i).getY(), fourPoints.get(i).getY(), delta);
        }
    }

    /**
     * Test for {@link LineString#getFractionalControlPointDistance} method.
     * */
    @Test
    public void testgGetFractionalControlPointDistance() {
        /*
         * the number should be less than the number of control points on the
         * line string.
         */
        int n = 6; // 10 control points on the line string one.
        double fDistance = lineStrings.get(0).getControlPointDistanceKm(n)
                / lineStrings.get(0).getPathLength();
        ;
        // notice , the length of the first line string is 10
        double expectedFDistance = (double) n / 10;
        assertEquals(expectedFDistance, fDistance, delta);

        n = 5; // 6 control points on the line string two.
        fDistance = lineStrings.get(1).getControlPointDistanceKm(n)
                / lineStrings.get(1).getPathLength();
        // notice , the length of the second line string is 6
        expectedFDistance = n / 6.0;
        assertEquals(expectedFDistance, fDistance, delta);

        n = 3; // 7 control points on the line string three.
        fDistance = lineStrings.get(2).getControlPointDistanceKm(n)
                / lineStrings.get(2).getPathLength();
        /*
         * The length of the third line string is 10 * sqrt(2). This control
         * point's distance to the first one is 3*sqrt(2).
         */
        expectedFDistance = (n * Math.sqrt(2.0)) / (10.0 * Math.sqrt(2.0));
        assertEquals(expectedFDistance, fDistance, delta);
    }
}
