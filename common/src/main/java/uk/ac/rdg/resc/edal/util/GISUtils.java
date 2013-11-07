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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geotoolkit.factory.Hints;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.geotoolkit.referencing.factory.epsg.EpsgInstaller;
import org.h2.jdbcx.JdbcDataSource;
import org.joda.time.DateTime;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.util.FactoryException;

import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.domain.VerticalDomain;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.exceptions.InvalidCrsException;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.geometry.LineString;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

/**
 * A class containing static methods which are useful for GIS operations.
 * 
 * @author Guy
 * 
 */
public final class GISUtils {

    private GISUtils() {
    }

    /**
     * Tests if a coordinate reference system is equivalent to WGS84 Lon-Lat
     * 
     * @param coordinateReferenceSystem
     *            The {@link CoordinateReferenceSystem} to test
     * @return <code>true</code> if the supplied
     *         {@link CoordinateReferenceSystem} is equivalent to WGS84
     */
    public static boolean isWgs84LonLat(CoordinateReferenceSystem coordinateReferenceSystem) {
        try {
            return CRS.findMathTransform(coordinateReferenceSystem, DefaultGeographicCRS.WGS84)
                    .isIdentity();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Finds the next longitude which is greater than the reference longitude
     * and equivalent to the target longitude
     * 
     * @param reference
     *            The reference longitude
     * @param target
     *            The target longitude
     * @return A longitude which is equivalent to the target and greater than
     *         the reference
     */
    public static double getNextEquivalentLongitude(double reference, double target) {
        /*
         * Find the clockwise distance from the first value on this axis to the
         * target value. This will be a positive number from 0 to 360 degrees
         */
        double clockDiff = constrainLongitude360(target - reference);
        return reference + clockDiff;
    }

    /**
     * Returns a longitude value in degrees that is equal to the given value but
     * in the range (-180:180]
     */
    public static double constrainLongitude180(double value) {
        double val = constrainLongitude360(value);
        return val > 180.0 ? val - 360.0 : val;
    }

    /**
     * Returns a longitude value in degrees that is equal to the given value but
     * in the range [0:360)
     */
    public static double constrainLongitude360(double value) {
        double val = value % 360.0;
        return val < 0.0 ? val + 360.0 : val;
    }

    /**
     * Given a target longitude and a longitude, this returns the longitude
     * value which is nearest to the target, taking wrapping into account
     * 
     * @param target
     *            The longitude which we are aiming to be nearest to, in the
     *            range (-180:180]
     * @param longitude
     *            The longitude which we want to be nearest to the target
     * @return A longitude value which is equivalent to <code>longitude</code>
     */
    public static double getNearestEquivalentLongitude(double target, double longitude) {
        if (target <= -180.0 || target > 180.0) {
            throw new IllegalArgumentException(
                    "Reference longitude must be in the range (-180,180]");
        }
        double lon1 = constrainLongitude180(longitude);
        double lon2 = target < 0.0 ? lon1 - 360.0 : lon1 + 360.0;
        double d1 = Math.abs(target - lon1);
        double d2 = Math.abs(target - lon2);
        return d1 < d2 ? lon1 : lon2;
    }

    /**
     * Constrains a lat-lon bounding box to have all longitude values in the
     * range [-180:180]
     * 
     * @param bbox
     *            The {@link BoundingBox} to constrain
     * @return The constrained {@link BoundingBox}. If the {@link BoundingBox}
     *         crosses the date line, a {@link BoundingBox} which spans the
     *         entire range (-180:180] will be returned
     */
    public static BoundingBox constrainBoundingBox(BoundingBox bbox) {
        if (isWgs84LonLat(bbox.getCoordinateReferenceSystem())) {
            if (bbox.getMaxX() > 180.0) {
                if (bbox.getMinX() < 180.0) {
                    /*
                     * Bounding box crosses date line
                     */
                    return new BoundingBoxImpl(-180, bbox.getMinY(), 180.0, bbox.getMaxY(),
                            bbox.getCoordinateReferenceSystem());
                } else {
                    /*
                     * Bounding box doesn't cross date line, but is all above
                     * 360
                     */
                    return new BoundingBoxImpl(constrainLongitude180(bbox.getMinX()),
                            bbox.getMinY(), constrainLongitude180(bbox.getMaxX()), bbox.getMaxY(),
                            bbox.getCoordinateReferenceSystem());
                }
            }
        }
        return bbox;
    }

    /**
     * Transforms the given HorizontalPosition to a new position in the given
     * coordinate reference system.
     * 
     * @param pos
     *            The position to translate.
     * @param targetCrs
     *            The CRS to translate into
     * @return a new position in the given CRS, or the same position if the new
     *         CRS is the same as the point's CRS. The returned point's CRS will
     *         be set to {@code targetCrs}. If the CRS of the position is null,
     *         the CRS will simply be set to the targetCrs.
     * @throws NullPointerException
     *             if {@code targetCrs} is null.
     */
    public static HorizontalPosition transformPosition(HorizontalPosition pos,
            CoordinateReferenceSystem targetCrs) {
        if (targetCrs == null) {
            throw new NullPointerException("Target CRS cannot be null");
        }
        CoordinateReferenceSystem sourceCrs = pos.getCoordinateReferenceSystem();
        if (sourceCrs == null) {
            return new HorizontalPosition(pos.getX(), pos.getY(), targetCrs);
        }
        /*
         * CRS.findMathTransform() caches recently-used transform objects so we
         * should incur no large penalty for multiple invocations
         */
        try {
            MathTransform transform = CRS.findMathTransform(sourceCrs, targetCrs);
            if (transform.isIdentity())
                return pos;
            double[] point = new double[] { pos.getX(), pos.getY() };
            transform.transform(point, 0, point, 0, 1);
            return new HorizontalPosition(point[0], point[1], targetCrs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests whether 2 {@link CoordinateReferenceSystem}s are equivalent
     * 
     * @param sourceCrs
     *            The first {@link CoordinateReferenceSystem} to test
     * @param targetCrs
     *            The second {@link CoordinateReferenceSystem} to test
     */
    public static boolean crsMatch(CoordinateReferenceSystem sourceCrs,
            CoordinateReferenceSystem targetCrs) {
        MathTransform transform;
        try {
            transform = CRS.findMathTransform(sourceCrs, targetCrs);
            return transform.isIdentity();
        } catch (FactoryException e) {
            /*
             * There is a problem performing the transfer. Say that these CRSs
             * do not match (since we can't be sure)
             */
            return false;
        }
    }

    /**
     * Finds a {@link CoordinateReferenceSystem} with the given code, forcing
     * longitude-first axis order.
     * 
     * @param crsCode
     *            The code for the CRS
     * @return a coordinate reference system with the longitude axis first
     * @throws InvalidCrsException
     *             if a CRS matching the code cannot be found
     * @throws NullPointerException
     *             if {@code crsCode} is null
     */
    public static CoordinateReferenceSystem getCrs(String crsCode) throws InvalidCrsException {
        if (crsCode == null)
            throw new NullPointerException("CRS code cannot be null");
        try {
            /* The "true" means "force longitude first" */
            return CRS.decode(crsCode, true);
        } catch (Exception e) {
            throw new InvalidCrsException(crsCode);
        }
    }

    /**
     * Converts a string of the form "a1,b1,a2,b2" into a {@link BoundingBox}
     * 
     * @param bboxStr
     *            A string of the form "a1,b1,a2,b2". If xFirst is
     *            <code>true</code>, then a1, a2 represent the x-coordinates and
     *            b1,b2 represent the y-coordinates, otherwise it is the other
     *            way around
     * @param xFirst
     *            Whether the x-coordinates are first or second in the list
     * @param crs
     *            A string representing the {@link CoordinateReferenceSystem} of
     *            the {@link BoundingBox}
     * 
     * @throws EdalException
     *             if the format of the bounding box is invalid
     */
    public static BoundingBox parseBbox(String bboxStr, boolean xFirst, String crs)
            throws EdalException {
        String[] bboxEls = bboxStr.split(",");
        /* Check the validity of the bounding box */
        if (bboxEls.length != 4) {
            throw new EdalException("Invalid bounding box format: need four elements");
        }
        double minx, miny, maxx, maxy;
        try {
            if (xFirst) {
                minx = Double.parseDouble(bboxEls[0]);
                miny = Double.parseDouble(bboxEls[1]);
                maxx = Double.parseDouble(bboxEls[2]);
                maxy = Double.parseDouble(bboxEls[3]);
            } else {
                minx = Double.parseDouble(bboxEls[1]);
                miny = Double.parseDouble(bboxEls[0]);
                maxx = Double.parseDouble(bboxEls[3]);
                maxy = Double.parseDouble(bboxEls[2]);
            }
        } catch (NumberFormatException nfe) {
            throw new EdalException("Invalid bounding box format: all elements must be numeric");
        }
        if (minx >= maxx || miny >= maxy) {
            throw new EdalException("Invalid bounding box format");
        }
        return new BoundingBoxImpl(minx, miny, maxx, maxy, getCrs(crs));
    }

    /**
     * Tests whether a {@link TemporalDomain} is discrete. Used when generating
     * Capabilities document in WMS
     */
    public static boolean isTemporalDomainTimeAxis(TemporalDomain domain) {
        return domain instanceof TimeAxis;
    }

    /**
     * Tests whether a {@link VerticalDomain} is discrete. Used when generating
     * Capabilities document in WMS
     */
    public static boolean isVerticalDomainVerticalAxis(VerticalDomain domain) {
        return domain instanceof VerticalAxis;
    }

    /**
     * Returns the closest time to the current time from a list of values
     * 
     * @param tValues
     *            The list of times to check
     * @return The closest from the list to the current time.
     */
    public static DateTime getClosestToCurrentTime(List<DateTime> tValues) {
        return getClosestTimeTo(new DateTime(), tValues);
    }

    /**
     * Returns the closest time within a time axis to the given time.
     * 
     * @param targetTime
     *            The target time
     * @param tValues
     *            The time values to check
     * @return Either the closest time within that axis, or the closest to the
     *         current time if the target is <code>null</code>, or
     *         <code>null</code> if the list of times is <code>null</code>
     */
    public static DateTime getClosestTimeTo(DateTime targetTime, List<DateTime> tValues) {
        if (tValues == null) {
            return null;
        }
        if (targetTime == null) {
            return getClosestToCurrentTime(tValues);
        }
        int index = TimeUtils.findTimeIndex(tValues, targetTime);
        if (index < 0) {
            /*
             * We can calculate the insertion point
             */
            int insertionPoint = -(index + 1);
            /*
             * We set the index to the most recent past time
             */
            if (insertionPoint == tValues.size()) {
                index = insertionPoint - 1;
            } else if (insertionPoint > 0) {
                /*
                 * We need to find which of the two possibilities is the closest
                 * time
                 */
                long t1 = tValues.get(insertionPoint - 1).getMillis();
                long t2 = tValues.get(insertionPoint).getMillis();

                if ((t2 - targetTime.getMillis()) <= (targetTime.getMillis() - t1)) {
                    index = insertionPoint;
                } else {
                    index = insertionPoint - 1;
                }
            } else {
                /*
                 * All DateTimes on the axis are in the future, so we take the
                 * earliest
                 */
                index = 0;
            }
        }

        return tValues.get(index);
    }

    /**
     * Returns the closest elevation to the surface of the given
     * {@link VerticalAxis}
     * 
     * @param vAxis
     *            The {@link VerticalAxis} to test
     * @return The uppermost elevation, or null if no {@link VerticalAxis} is
     *         provided
     */
    public static Double getClosestElevationToSurface(VerticalAxis vAxis) {
        if (vAxis == null) {
            return null;
        }

        if (vAxis.getVerticalCrs().isPressure()) {
            /*
             * The vertical axis is pressure. The default (closest to the
             * surface) is therefore the maximum value.
             */
            return Collections.max(vAxis.getCoordinateValues());
        } else {
            /*
             * The vertical axis represents linear height, so we find which
             * value is closest to zero (the surface), i.e. the smallest
             * absolute value
             */
            return Collections.min(vAxis.getCoordinateValues(), new Comparator<Double>() {
                @Override
                public int compare(Double d1, Double d2) {
                    return Double.compare(Math.abs(d1), Math.abs(d2));
                }
            });
        }
    }

    /**
     * Gets a HorizontalDomain that contains (near) the minimum necessary number
     * of points to sample a layer's source grid of data. That is to say,
     * creating a HorizontalDomain at higher resolution would not result in
     * sampling significantly more points in the layer's source grid.
     * 
     * @param feature
     *            The feature for which the transect will be generated
     * @param transect
     *            The transect as specified in the request
     * @return a HorizontalDomain that contains (near) the minimum necessary
     *         number of points to sample a layer's source grid of data.
     */
    public static List<HorizontalPosition> getOptimalTransectPoints(HorizontalGrid hGrid,
            LineString transect, VerticalPosition zPos, DateTime time, int dataGridPoints) {
        /*
         * We need to work out how many points we need to include in order to
         * completely sample the data grid (i.e. we need the resolution of the
         * points to be higher than that of the data grid). It's hard to work
         * this out neatly (data grids can be irregular) but we can estimate
         * this by creating transects at progressively higher resolution, and
         * working out how many grid points will be sampled.
         */
        int lastNumUniqueGridPointsSampled = -1;
        int dataGridIncrease = dataGridPoints;
        List<HorizontalPosition> pointList = null;
        while (true) {
            /*
             * Create a transect with the required number of points,
             * interpolating between the control points in the line string
             */
            List<HorizontalPosition> points = transect.getPointsOnPath(dataGridPoints);

            /*
             * Work out how many grid points will be sampled by this transect
             */
            Set<GridCoordinates2D> gridCoords = new HashSet<GridCoordinates2D>();
            for (HorizontalPosition pos : points) {
                GridCoordinates2D gridCoord = hGrid.findIndexOf(pos);
                if (gridCoord != null) {
                    gridCoords.add(gridCoord);
                }
            }

            int numUniqueGridPointsSampled = gridCoords.size();

            /*
             * If this increase in resolution results in at least 10% more
             * points being sampled we'll go around the loop again
             */
            if (numUniqueGridPointsSampled > lastNumUniqueGridPointsSampled * 1.1) {
                /* We need to increase the transect resolution and try again */
                lastNumUniqueGridPointsSampled = numUniqueGridPointsSampled;
                dataGridPoints += dataGridIncrease;
                pointList = points;
            } else {
                /* We've gained little advantage by the last resolution increase */
                return pointList;
            }
        }
    }

//    /**
//     * Finds the distance between 2 positions in units of the CRS.
//     * 
//     * @param pos1
//     *            The first position
//     * @param pos2
//     *            The second position
//     * @return The distance between the 2 positions in units of the CRS.
//     */
//    public static double getDistance(HorizontalPosition pos1, HorizontalPosition pos2) {
//        if (pos1 == null || pos2 == null) {
//            return Double.NaN;
//        }
//        if (!crsMatch(pos1.getCoordinateReferenceSystem(), pos2.getCoordinateReferenceSystem())) {
//            pos2 = transformPosition(pos2, pos1.getCoordinateReferenceSystem());
//        }
//        double x1 = pos1.getX();
//        double x2 = pos2.getX();
//        double y1 = pos1.getY();
//        double y2 = pos2.getY();
//        if (isWgs84LonLat(pos1.getCoordinateReferenceSystem())) {
//            x2 = getNearestEquivalentLongitude(x1, x2);
//        }
//        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
//    }

    static {
        /*
         * Initialise the EPSG database if necessary
         */
        try {
            Class.forName("org.h2.Driver");
            JdbcDataSource dataSource = new JdbcDataSource();
            dataSource.setURL("jdbc:h2:.h2/epsg.db");
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(true);
            Hints.putSystemDefault(Hints.EPSG_DATA_SOURCE, dataSource);
            EpsgInstaller i = new EpsgInstaller();
            i.setDatabase(conn);
            if (!i.exists()) {
                i.call();
            }
        } catch (FactoryException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
