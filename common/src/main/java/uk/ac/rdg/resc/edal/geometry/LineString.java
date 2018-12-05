/*******************************************************************************
 * Copyright (c) 2012 The University of Reading
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.sis.distance.DistanceUtils;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.exceptions.InvalidCrsException;
import uk.ac.rdg.resc.edal.exceptions.InvalidLineStringException;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * Represents a path through a coordinate system. The path consists of a set of
 * linear path elements, drawn between <i>control points</i>.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public final class LineString {

    private final List<HorizontalPosition> controlPoints;
    private final double[] controlPointDistances;
    private double pathLength;
    private final CoordinateReferenceSystem crs;

    /**
     * Constructs a {@link LineString} from a line string in the form.
     * 
     * @param lineStringSpec
     *            the line string as specified in the form "x1 y1, x2 y2, x3
     *            y3".
     * @param crs
     *            The coordinate reference system for the line string's
     *            coordinates
     * @throws InvalidLineStringException
     *             if the line string is not correctly specified.
     * @throws InvalidCrsException
     * @throws NullPointerException
     *             if crsHelper == null
     */
    public LineString(String lineStringSpec, CoordinateReferenceSystem crs)
            throws InvalidLineStringException, InvalidCrsException {
        String[] pointsStr = lineStringSpec.split(",");
        if (pointsStr.length < 2) {
            throw new InvalidLineStringException(
                    "At least two points are required to generate a line string");
        }
        if (crs == null) {
            throw new NullPointerException("CRS cannot be null");
        }
        this.crs = crs;

        /*
         * The control points along the transect as specified by the line string
         */
        final List<HorizontalPosition> ctlPoints = new ArrayList<HorizontalPosition>();
        for (String s : pointsStr) {
            /*
             * Allows one or more spaces to be used as a delimiter
             */
            String[] coords = s.trim().split(" +");
            if (coords.length != 2) {
                throw new InvalidLineStringException("Coordinates format error");
            }
            try {
                ctlPoints.add(new HorizontalPosition(Double.parseDouble(coords[0].trim()),
                        Double.parseDouble(coords[1].trim()), crs));
            } catch (NumberFormatException nfe) {
                throw new InvalidLineStringException("Coordinates format error");
            }
        }
        controlPoints = Collections.unmodifiableList(ctlPoints);

        /*
         * Calculate the total length of the path in units of the CRS. While
         * we're doing this we'll calculate the total length of the path up to
         * each waypoint
         */
        controlPointDistances = new double[controlPoints.size()];
        pathLength = 0.0;
        controlPointDistances[0] = pathLength;
        for (int i = 1; i < this.controlPoints.size(); i++) {
            HorizontalPosition p1 = controlPoints.get(i - 1);
            HorizontalPosition p2 = controlPoints.get(i);
            pathLength += DistanceUtils.getHaversineDistance(p1.getY(), p1.getX(), p2.getY(),
                    p2.getX());
            controlPointDistances[i] = pathLength;
        }
    }

    /**
     * Returns the list of control points along this line string.
     * 
     * @return an unmodifiable list of control points.
     */
    public List<HorizontalPosition> getControlPoints() {
        return controlPoints;
    }

    /**
     * Returns the fractional distance along the line string to the control
     * point with the given index.
     * 
     * @param index
     *            The index of the control point. An index of zero represents
     *            the start of the line string.
     * @return the distance in km along the whole line string to the control
     *         point
     * @throws IndexOutOfBoundsException
     *             if {@code index < 0 || index >= number of control points}
     */
    public double getControlPointDistanceKm(int index) {
        if (index < 0 || index >= controlPointDistances.length) {
            throw new IndexOutOfBoundsException();
        }
        return controlPointDistances[index];
    }

    /**
     * @return The total path length, in km
     */
    public double getPathLength() {
        return pathLength;
    }

    /**
     * Returns a list of <i>n</i> points along the path defined by this line
     * string. The first point will be the first control point; the last point
     * will be the last control point. Intermediate points are linearly
     * interpolated between the control points. Note that it is not guaranteed
     * that the intermediate control points will be contained in this list.
     * 
     * TODO Add the control points to this list, in the correct location.
     * 
     * @param n
     *            The number of points to return
     * @return an unmodifiable list of {@code n} points that lie on this path.
     * @throws IllegalArgumentException
     *             if {@code numPoints < 2}
     * 
     */
    public List<HorizontalPosition> getPointsOnPath(int n) {
        if (n < 2) {
            throw new IllegalArgumentException("Must request at least 2 points");
        }
        final List<HorizontalPosition> points = new ArrayList<HorizontalPosition>(n);
        /* The first point is the first control point */
        points.add(controlPoints.get(0));
        /* Now for the points in the middle */
        for (int i = 1; i < n - 1; i++) {
            /* Calculate the distance along the path */
            double s = pathLength * i / (n - 1);
            /* Calculate an interpolated point along the path */
            points.add(interpolatePoint(s));
        }
        /* The last point is the last waypoint */
        points.add(controlPoints.get(controlPoints.size() - 1));

        return Collections.unmodifiableList(points);
    }

    /**
     * Given a length <i>s</i> along the path defined by this line string, this
     * method returns a {@link HorizontalPosition} that represents this point on
     * the path.
     * 
     * @param s
     *            the distance along the path
     * @return a HorizontalPosition representing this point on the path.
     * @throws IllegalArgumentException
     *             if s < 0 or s > pathLength
     */
    private HorizontalPosition interpolatePoint(double s) {
        if (s < 0.0 || s > pathLength) {
            throw new IllegalArgumentException("s does not lie on the path");
        }
        /* Find the index of the last control point we passed */
        int i = this.getPreviousControlPointIndex(s);
        /* Find the distance from the last control point */
        double dlast = s - controlPointDistances[i];
        /* Find the distance to the next control point */
        double dnext = controlPointDistances[i + 1] - s;
        /*
         * Find the fraction of the distance between the last and next control
         * points
         */
        double dfrac = dlast / (dlast + dnext);

        /* Find the x and y coordinates of the interpolated point */
        HorizontalPosition cplast = controlPoints.get(i);
        HorizontalPosition cpnext = controlPoints.get(i + 1);
        double x = (1.0 - dfrac) * cplast.getX() + dfrac * cpnext.getX();
        double y = (1.0 - dfrac) * cplast.getY() + dfrac * cpnext.getY();

        return new HorizontalPosition(x, y, crs);
    }

    /**
     * If we walk a distance <i>s</i> along the path defined by this line
     * string, this method returns the index <i>i</i> of the last control point
     * we passed. I.e. the point at distance <i>s</i> lies between control point
     * <i>i</i> and control point <i>i</i> + 1.
     * 
     * @param s
     *            The distance along the path, which must lie between 0 and
     *            pathLength (checked before this method is called).
     * @return the index of the previous control point, where 0 <= i <
     *         numControlPoints - 1.
     */
    private int getPreviousControlPointIndex(double s) {
        for (int i = 1; i < controlPointDistances.length; i++) {
            if (controlPointDistances[i] > s)
                return i - 1;
        }
        /* Shouldn't get here */
        throw new AssertionError();
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

}
