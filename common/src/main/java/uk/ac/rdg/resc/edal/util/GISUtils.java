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

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import org.apache.sis.geometry.DirectPosition2D;
import org.apache.sis.internal.metadata.sql.Initializer;
import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.crs.AbstractCRS;
import org.apache.sis.referencing.cs.AxesConvention;
import org.apache.sis.util.Utilities;
import org.h2.jdbcx.JdbcDataSource;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Matrix;
import org.opengis.util.FactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.HorizontalDomain;
import uk.ac.rdg.resc.edal.domain.SimpleHorizontalDomain;
import uk.ac.rdg.resc.edal.domain.SimpleTemporalDomain;
import uk.ac.rdg.resc.edal.domain.SimpleVerticalDomain;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.domain.VerticalDomain;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.exceptions.InvalidCrsException;
import uk.ac.rdg.resc.edal.exceptions.MismatchedCrsException;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.geometry.LineString;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxisImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;

/**
 * A class containing static methods which are useful for GIS operations.
 * 
 * Also implements {@link ObjectFactory} so that it can be used to provide the
 * {@link JdbcDataSource} for the EPSG database to JNDI.
 *
 * @author Guy
 *
 */
public final class GISUtils implements ObjectFactory {
    public final static double RAD2DEG = 180.0 / Math.PI;
    public final static double DEG2RAD = Math.PI / 180.0;
    private static final Logger log = LoggerFactory.getLogger(GISUtils.class);

    /**
     * Returns the default Lon-Lat geographic CRS. This method guarantees that
     * the returned CRS complies to the following conditions:
     *
     * <ul>
     * <li>is geographic</li>
     * <li>has axes in <var>longitude</var>, <var>latitude</var> order</li>
     * <li>has longitude in degrees increasing toward East</li>
     * <li>has latitude in degrees increasing toward North</li>
     * <li>use the Greenwich prime meridian.</li>
     * </ul>
     *
     * This method makes no guarantees about the geodetic datum. The current
     * implementation delegates to {@link CommonCRS#defaultGeographic()}, which
     * itself delegates to a geographic CRS based on the WGS84 datum. However
     * the default may change in future versions, for example using a spherical
     * CRS instead of WGS84.
     *
     * @return the default geographic CRS with Lon-Lat axes in degrees.
     */
    public static CoordinateReferenceSystem defaultGeographicCRS() {
        return CommonCRS.defaultGeographic();
    }

    /**
     * Tests if a coordinate reference system is equivalent to the default
     * geographic CRS.
     *
     * @param coordinateReferenceSystem
     *            The {@link CoordinateReferenceSystem} to test
     * @return <code>true</code> if the supplied
     *         {@link CoordinateReferenceSystem} is equivalent to
     *         {@link #defaultGeographicCRS()}
     */
    public static boolean isDefaultGeographicCRS(
            CoordinateReferenceSystem coordinateReferenceSystem) {
        return Utilities.equalsIgnoreMetadata(defaultGeographicCRS(), coordinateReferenceSystem);
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
            return CRS
                    .findOperation(coordinateReferenceSystem,
                            CommonCRS.WGS84.normalizedGeographic(), null)
                    .getMathTransform().isIdentity();
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
     *            The longitude which we are aiming to be nearest to
     * @param longitude
     *            The longitude which we want to be nearest to the target
     * @return A longitude value which is equivalent to <code>longitude</code>
     */
    public static double getNearestEquivalentLongitude(double target, double longitude) {
        double lon1 = getNextEquivalentLongitude(target, longitude);
        double lon2 = lon1 - 360.0;
        double d1 = Math.abs(target - lon1);
        double d2 = Math.abs(target - lon2);
        return d1 <= d2 ? lon1 : lon2;
    }

    /**
     * Constrains a lat-lon bounding box to have all longitude values in the
     * range (-180:180]
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
        if (pos == null) {
            return null;
        }
        CoordinateReferenceSystem sourceCrs = pos.getCoordinateReferenceSystem();
        if (sourceCrs == null) {
            return new HorizontalPosition(pos.getX(), pos.getY(), targetCrs);
        }
        if (targetCrs == null) {
            throw new NullPointerException("Target CRS cannot be null");
        }
        /*
         * CRS.findMathTransform() caches recently-used transform objects so we
         * should incur no large penalty for multiple invocations
         */
        try {
            MathTransform transform = CRS.findOperation(sourceCrs, targetCrs, null)
                    .getMathTransform();
            if (transform.isIdentity())
                return pos;
            double[] point = new double[] { pos.getX(), pos.getY(), 0 };
            transform.transform(point, 0, point, 0, 1);
            return new HorizontalPosition(point[0], point[1], targetCrs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Transforms the given lat-lon heading to a different
     * {@link CoordinateReferenceSystem}
     *
     * @param heading
     *            The heading in degrees
     * @param position
     *            The {@link HorizontalPosition} at which to transform the
     *            heading. The {@link CoordinateReferenceSystem} returned by
     *            {@link HorizontalPosition#getCoordinateReferenceSystem()} will
     *            be the {@link CoordinateReferenceSystem} in which the heading
     *            is valid.
     *
     * @return The heading, in degrees clockwise from "upwards" (i.e. y-positive
     *         in the target CRS)
     */
    public static Double transformWgs84Heading(Number heading, HorizontalPosition position) {
        if (position.getCoordinateReferenceSystem() == null) {
            throw new NullPointerException("Target CRS cannot be null");
        }
        if (heading == null || Double.isNaN(heading.doubleValue())) {
            return null;
        }
        /*
         * CRS.findMathTransform() caches recently-used transform objects so we
         * should incur no large penalty for multiple invocations
         */
        try {
            MathTransform wgs2crs = CRS.findOperation(CommonCRS.WGS84.normalizedGeographic(),
                    position.getCoordinateReferenceSystem(), null).getMathTransform();
            if (wgs2crs.isIdentity())
                return heading.doubleValue();
            heading = heading.doubleValue() * DEG2RAD;

            /*
             * Find the position in WGS84
             */
            double[] point = new double[] { position.getX(), position.getY() };
            wgs2crs.inverse().transform(point, 0, point, 0, 1);

            /*
             * Now find the derivative at that position.
             */
            Matrix derivative = wgs2crs.derivative(new DirectPosition2D(point[0], point[1]));

            /*
             * Use the derivative to find the new heading
             */
            double x = Math.sin(heading.doubleValue());
            double y = Math.cos(heading.doubleValue());

            double newX = derivative.getElement(0, 0) * x + derivative.getElement(0, 1) * y;
            double newY = derivative.getElement(1, 0) * x + derivative.getElement(1, 1) * y;

            return RAD2DEG * Math.atan2(newX, newY);
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
        if (sourceCrs == null) {
            if (targetCrs == null) {
                return true;
            } else {
                return false;
            }
        } else if (targetCrs == null) {
            return false;
        }

        MathTransform transform;
        try {
            transform = CRS.findOperation(sourceCrs, targetCrs, null).getMathTransform();
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
            CoordinateReferenceSystem crs = CRS.forCode(crsCode);
            crs = AbstractCRS.castOrCopy(crs).forConvention(AxesConvention.RIGHT_HANDED);
            return crs;
        } catch (Exception e) {
            log.error("Problem getting CRS", e);
            throw new InvalidCrsException(crsCode, e);
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
        if (minx > maxx || miny > maxy) {
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
     * @param tDomain
     *            The list of times to check
     * @return The closest from the list to the current time.
     */
    public static DateTime getClosestToCurrentTime(TemporalDomain tDomain) {
        return getClosestTimeTo(new DateTime(), tDomain);
    }

    /**
     * Returns the closest time within a temporal domain to the given time.
     *
     * @param targetTime
     *            The target time
     * @param tDomain
     *            The {@link TemporalDomain} to check
     * @return Either the closest time within that axis, or the closest to the
     *         current time if the target is <code>null</code>, or
     *         <code>null</code> if the list of times is <code>null</code>
     */
    public static DateTime getClosestTimeTo(DateTime targetTime, TemporalDomain tDomain) {
        if (tDomain == null) {
            return null;
        }
        if (targetTime == null) {
            targetTime = new DateTime();
        }
        if (tDomain instanceof TimeAxis) {
            TimeAxis timeAxis = (TimeAxis) tDomain;
            int index = getIndexOfClosestTimeTo(targetTime, timeAxis);
            return timeAxis.getCoordinateValue(index);
        } else {
            /*
             * We just have a domain representing an extent. If it contains the
             * target time, return the target time, otherwise return either the
             * upper or lower bound, as appropriate.
             */
            if (tDomain.contains(targetTime)) {
                return targetTime;
            } else if (targetTime.isBefore(tDomain.getExtent().getLow())) {
                return tDomain.getExtent().getLow();
            } else {
                return tDomain.getExtent().getHigh();
            }
        }
    }

    public static int getIndexOfClosestTimeTo(DateTime targetTime, TimeAxis timeAxis) {
        List<DateTime> tValues = timeAxis.getCoordinateValues();
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
        return index;
    }

    /**
     * Returns the closest elevation to the surface of the given
     * {@link VerticalDomain}
     *
     * @param vDomain
     *            The {@link VerticalDomain} to test
     * @return The uppermost elevation, or null if no {@link VerticalDomain} is
     *         provided
     */
    public static Double getClosestElevationToSurface(VerticalDomain vDomain) {
        if (vDomain == null) {
            return null;
        }
        if (vDomain instanceof VerticalAxis) {
            VerticalAxis vAxis = (VerticalAxis) vDomain;
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
        } else {
            if (vDomain.getVerticalCrs().isPressure()) {
                /*
                 * The vertical domain is pressure. The default (closest to the
                 * surface) is therefore the maximum value.
                 */
                return vDomain.getExtent().getHigh();
            } else {
                /*
                 * The vertical domain represents linear height, so if includes
                 * 0.0, this is the closest to the surface, otherwise the lowest
                 * value is closest to the surface
                 */
                if (vDomain.getExtent().contains(0.0)) {
                    return 0.0;
                } else {
                    return vDomain.getExtent().getLow();
                }
            }
        }
    }

    /**
     * Returns the closest elevation within a vertical domain to the given
     * elevation.
     *
     * @param targetZ
     *            The target elevation
     * @param zDomain
     *            The {@link VerticalDomain} to check
     * @return Either the closest elevation within that axis, or the closest to
     *         the surface if the target is <code>null</code>, or
     *         <code>null</code> if the {@link VerticalDomain} is
     *         <code>null</code>
     */
    public static Double getClosestElevationTo(Double targetZ, VerticalDomain zDomain) {
        if (zDomain == null) {
            return null;
        }
        if (targetZ == null) {
            return getClosestElevationToSurface(zDomain);
        }
        if (zDomain instanceof VerticalAxis) {
            VerticalAxis zAxis = (VerticalAxis) zDomain;
            int index = getIndexOfClosestElevationTo(targetZ, zAxis);
            return zAxis.getCoordinateValue(index);
        } else {
            /*
             * We just have a domain representing an extent. If it contains the
             * target elevation, return the target elevation, otherwise return
             * either the upper or lower bound, as appropriate.
             */
            if (zDomain.contains(targetZ)) {
                return targetZ;
            } else if (targetZ < (zDomain.getExtent().getLow())) {
                return zDomain.getExtent().getLow();
            } else {
                return zDomain.getExtent().getHigh();
            }
        }
    }

    public static int getIndexOfClosestElevationTo(Double target, VerticalAxis zAxis) {
        if (zAxis == null || target == null) {
            return -1;
        }
        List<Double> zVals = zAxis.getCoordinateValues();
        int zIndex = Collections.binarySearch(zVals, target);
        if (zIndex < 0) {
            /*
             * We can calculate the insertion point
             */
            int insertionPoint = -(zIndex + 1);
            /*
             * We set the index to the most recent past time
             */
            if (insertionPoint == zVals.size()) {
                zIndex = insertionPoint - 1;
            } else if (insertionPoint > 0) {
                /*
                 * We need to find which of the two possibilities is the closest
                 * time
                 */
                double z1 = zVals.get(insertionPoint - 1);
                double z2 = zVals.get(insertionPoint);

                if ((z2 - target) <= (target - z1)) {
                    zIndex = insertionPoint;
                } else {
                    zIndex = insertionPoint - 1;
                }
            } else {
                zIndex = 0;
            }
        }
        return zIndex;
    }

    /**
     * Gets a HorizontalDomain that contains (near) the minimum necessary number
     * of points to sample a layer's source grid of data. That is to say,
     * creating a HorizontalDomain at higher resolution would not result in
     * sampling significantly more points in the layer's source grid.
     *
     * @param hGrid
     *            The {@link HorizontalGrid} to find transect point on
     * @param transect
     *            The transect as specified in the request
     * @return a HorizontalDomain that contains (near) the minimum necessary
     *         number of points to sample a layer's source grid of data.
     */
    public static List<HorizontalPosition> getOptimalTransectPoints(HorizontalGrid hGrid,
            LineString transect) {
        /*
         * We need to work out how many points we need to include in order to
         * completely sample the data grid (i.e. we need the resolution of the
         * points to be higher than that of the data grid). It's hard to work
         * this out neatly (data grids can be irregular) but we can estimate
         * this by creating transects at progressively higher resolution, and
         * working out how many grid points will be sampled.
         */
        int lastNumUniqueGridPointsSampled = -1;
        int dataGridPoints = 64;
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
                /*
                 * We've gained little advantage by the last resolution increase
                 */
                return pointList;
            }
        }
    }

    /**
     * Converts a {@link BoundingBox} into a {@link BoundingBox} using the
     * {@link GISUtils#defaultGeographicCRS()}
     *
     * This method is not guaranteed to be exact. Its aim is to choose bounding
     * boxes which contain all of the data - there is no requirement that the
     * bounding box is a tight fit.
     *
     * @param bbox
     *            The bounding box
     * @return A {@link BoundingBox} in the
     *         {@link GISUtils#defaultGeographicCRS()} CRS which contains the
     *         supplied {@link BoundingBox}
     */
    public static BoundingBox toWGS84BoundingBox(BoundingBox bbox) {
        double minx = Double.MAX_VALUE;
        double maxx = -Double.MAX_VALUE;
        double miny = Double.MAX_VALUE;
        double maxy = -Double.MAX_VALUE;
        if (isWgs84LonLat(bbox.getCoordinateReferenceSystem())) {
            bbox = constrainBoundingBox(bbox);
            minx = bbox.getMinX();
            maxx = bbox.getMaxX();
            miny = bbox.getMinY();
            maxy = bbox.getMaxY();
        } else {

            /*
             * Check for north/south polar stereographic - then we know that a
             * pole is included which checking the border will not pick up, as
             * well as all longitude values
             */
            ReferenceIdentifier crsId = bbox.getCoordinateReferenceSystem().getName();

            if ("EPSG".equalsIgnoreCase(crsId.getCodeSpace())) {
                if ("5041".equals(crsId.getCode()) || "32661".equals(crsId.getCode())
                        || crsId.getCode().contains("UPS North")) {
                    /*
                     * North polar stereographic. We only need to find the min y
                     */
                    maxy = 90;
                    maxx = 180;
                    minx = -180;
                } else if ("5042".equals(crsId.getCode()) || "32761".equals(crsId.getCode())
                        || crsId.getCode().contains("UPS South")) {
                    /*
                     * South polar stereographic. We only need to find the max y
                     */
                    miny = -90;
                    maxx = 180;
                    minx = -180;
                }
            }
            /*
             * There is no simple mapping from an arbitrary bounding box to a
             * lat-lon one. We scan around the edge of the bounding box (10
             * points per side) transforming each position and find the bounding
             * box of these points
             */
            for (double x = bbox.getMinX(); x <= bbox.getMaxX(); x += (bbox.getWidth() / 10.0)) {
                /*
                 * Top and bottom sides of bbox
                 */
                double y = bbox.getMinY();
                HorizontalPosition transformPosition = transformPosition(
                        new HorizontalPosition(x, y, bbox.getCoordinateReferenceSystem()),
                        defaultGeographicCRS());
                minx = Math.min(transformPosition.getX(), minx);
                maxx = Math.max(transformPosition.getX(), maxx);
                miny = Math.min(transformPosition.getY(), miny);
                maxy = Math.max(transformPosition.getY(), maxy);

                y = bbox.getMaxY();
                transformPosition = transformPosition(
                        new HorizontalPosition(x, y, bbox.getCoordinateReferenceSystem()),
                        defaultGeographicCRS());
                minx = Math.min(transformPosition.getX(), minx);
                maxx = Math.max(transformPosition.getX(), maxx);
                miny = Math.min(transformPosition.getY(), miny);
                maxy = Math.max(transformPosition.getY(), maxy);
            }
            for (double y = bbox.getMinY(); y <= bbox.getMaxY(); y += (bbox.getHeight() / 10.0)) {
                /*
                 * Sides of bbox
                 */
                double x = bbox.getMinX();
                HorizontalPosition transformPosition = transformPosition(
                        new HorizontalPosition(x, y, bbox.getCoordinateReferenceSystem()),
                        defaultGeographicCRS());
                minx = Math.min(transformPosition.getX(), minx);
                maxx = Math.max(transformPosition.getX(), maxx);
                miny = Math.min(transformPosition.getY(), miny);
                maxy = Math.max(transformPosition.getY(), maxy);

                x = bbox.getMaxX();
                transformPosition = transformPosition(
                        new HorizontalPosition(x, y, bbox.getCoordinateReferenceSystem()),
                        defaultGeographicCRS());
                minx = Math.min(transformPosition.getX(), minx);
                maxx = Math.max(transformPosition.getX(), maxx);
                miny = Math.min(transformPosition.getY(), miny);
                maxy = Math.max(transformPosition.getY(), maxy);
            }
        }
        /*
         * Geographic bounding boxes cannot extend outside of the ranges
         * -180,180;-90,90
         */
        if (minx < -180) {
            minx = -180;
        }
        if (maxx > 180) {
            maxx = 180;
        }
        if (miny < -90) {
            miny = -90;
        }
        if (maxy > 90) {
            maxy = 90;
        }
        return new BoundingBoxImpl(minx, miny, maxx, maxy, defaultGeographicCRS());
    }

    /**
     * Converts a {@link BoundingBox} into a {@link GeographicBoundingBox} (i.e.
     * one which is in lat/lon WGS84).
     *
     * This method is not guaranteed to be exact. Its aim is to choose bounding
     * boxes which contain all of the data - there is no requirement that the
     * bounding box is a tight fit.
     *
     * @param bbox
     *            The bounding box
     * @return A {@link GeographicBoundingBox} which contains the supplied
     *         {@link BoundingBox}
     */
    public static GeographicBoundingBox toGeographicBoundingBox(BoundingBox bbox) {
        BoundingBox wgs84BoundingBox = toWGS84BoundingBox(bbox);
        return new DefaultGeographicBoundingBox(wgs84BoundingBox.getMinX(),
                wgs84BoundingBox.getMaxX(), wgs84BoundingBox.getMinY(), wgs84BoundingBox.getMaxY());
    }

    /**
     * Increases the size of a {@link BoundingBox} by a given factor
     *
     * @param bbox
     *            The {@link BoundingBox} to increase the size of
     * @param percentageIncrease
     *            The percentage increase
     * @return A larger {@link BoundingBox} with the same centre
     */
    public static BoundingBox getLargeBoundingBox(BoundingBox bbox, double percentageIncrease) {
        /*
         * Divide by 200 because we these values get used twice (once on each
         * side)
         */
        double xExtra = bbox.getWidth() * (percentageIncrease / 200.0);
        double yExtra = bbox.getHeight() * (percentageIncrease / 200.0);
        BoundingBox bboxBordered = new BoundingBoxImpl(bbox.getMinX() - xExtra,
                bbox.getMinY() - yExtra, bbox.getMaxX() + xExtra, bbox.getMaxY() + yExtra,
                bbox.getCoordinateReferenceSystem());
        return bboxBordered;
    }

    /**
     * Calculates the {@link BoundingBox} of a set of
     * {@link HorizontalPosition}s.
     * 
     * @param positions
     *            a {@link List} of {@link HorizontalPosition}s, which must all
     *            share the same CRS
     * @return The minimum bounding rectangle of the supplied positions
     * @throws MismatchedCrsException
     *             if not all positions share the same CRS
     */
    public static BoundingBox getBoundingBox(List<HorizontalPosition> positions) {
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        CoordinateReferenceSystem crs = null;
        for (HorizontalPosition pos : positions) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            if (crs == null) {
                crs = pos.getCoordinateReferenceSystem();
            } else {
                if (!crsMatch(crs, pos.getCoordinateReferenceSystem())) {
                    throw new MismatchedCrsException(
                            "All positions must have the same CRS to get bounding box");
                }
            }

        }
        BoundingBox bbox = new BoundingBoxImpl(minX, minY, maxX, maxY, crs);
        return constrainBoundingBox(bbox);
    }

    /**
     * Calculates the {@link BoundingBox} of a set of {@link BoundingBox}es -
     * i.e. the minimum {@link BoundingBox} which will encompass them all
     * 
     * @param bboxes
     *            a {@link List} of {@link BoundingBox}es, which must all share
     *            the same CRS
     * @return The minimum bounding rectangle of the supplied
     *         {@link BoundingBox}es
     * @throws MismatchedCrsException
     *             if not all positions share the same CRS
     */
    public static BoundingBox getBoundingBoxOfBoxes(List<BoundingBox> bboxes) {
        List<HorizontalPosition> corners = new ArrayList<>();
        for (BoundingBox bbox : bboxes) {
            corners.add(bbox.getLowerCorner());
            corners.add(bbox.getUpperCorner());
        }
        return getBoundingBox(corners);
    }

    /**
     * Gets the intersection of a number of {@link HorizontalDomain}s
     *
     * @param domains
     *            The {@link HorizontalDomain}s to find a intersection of
     * @return A new {@link HorizontalDomain} whose {@link BoundingBox}
     *         represents the area where valid values can be found in all the
     *         supplied {@link HorizontalDomain}s. The
     *         {@link CoordinateReferenceSystem} of the returned
     *         {@link HorizontalDomain} will be the same as the supplied
     *         {@link HorizontalDomain}s if they match, otherwise it will be
     *         WGS84. If all of the supplied {@link HorizontalDomain}s have a
     *         <code>null</code> CRS, they are assumed to have the same
     *         (undefined) CRS. If there is a mixture of <code>null</code> and
     *         non-<code>null</code> CRSes, there is no intersection.
     */
    public static HorizontalDomain getIntersectionOfHorizontalDomains(HorizontalDomain... domains) {
        if (domains.length == 0) {
            throw new IllegalArgumentException("Must provide multiple domains to get a union");
        }
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;

        boolean sameCrs = true;
        HorizontalDomain comparison = domains[0];
        for (int i = 1; i < domains.length; i++) {
            HorizontalDomain domain = domains[i];
            if (comparison.getCoordinateReferenceSystem() == null) {
                /*
                 * If our comparison CRS is null, then all others must be too,
                 * otherwise we cannot find an intersection.
                 */
                if (domain.getCoordinateReferenceSystem() != null) {
                    return null;
                }
            } else {
                /*
                 * If we have a null CRS, we cannot find an intersection, since
                 * our comparison CRS is non-null
                 */
                if (domain.getCoordinateReferenceSystem() == null) {
                    return null;
                }
                if (!comparison.getCoordinateReferenceSystem()
                        .equals(domain.getCoordinateReferenceSystem())) {
                    sameCrs = false;
                }
            }
        }

        boolean allEqual = true;
        for (HorizontalDomain domain : domains) {
            /*
             * If one of the domains is null, their intersection is null
             */
            if (domain == null) {
                return null;
            }
            if (!domain.equals(comparison)) {
                allEqual = false;
            }
            if (sameCrs) {
                BoundingBox bbox = domain.getBoundingBox();
                if (bbox.getMaxX() > maxX) {
                    maxX = bbox.getMaxX();
                }
                if (bbox.getMinX() < minX) {
                    minX = bbox.getMinX();
                }
                if (bbox.getMaxY() > maxY) {
                    maxY = bbox.getMaxY();
                }
                if (bbox.getMinY() < minY) {
                    minY = bbox.getMinY();
                }
            } else {
                GeographicBoundingBox gbbox = domain.getGeographicBoundingBox();
                if (gbbox.getEastBoundLongitude() > maxX) {
                    maxX = gbbox.getEastBoundLongitude();
                }
                if (gbbox.getWestBoundLongitude() < minX) {
                    minX = gbbox.getWestBoundLongitude();
                }
                if (gbbox.getNorthBoundLatitude() > maxY) {
                    maxY = gbbox.getNorthBoundLatitude();
                }
                if (gbbox.getSouthBoundLatitude() < minY) {
                    minY = gbbox.getSouthBoundLatitude();
                }
            }
        }
        if (allEqual) {
            return comparison;
        }
        CoordinateReferenceSystem crs = sameCrs ? comparison.getCoordinateReferenceSystem()
                : defaultGeographicCRS();
        return new SimpleHorizontalDomain(minX, minY, maxX, maxY, crs);
    }

    /**
     * Gets the intersection of a number of {@link VerticalDomain}s
     *
     * @param domains
     *            The {@link VerticalDomain}s to find a intersection of. They
     *            must all share the same {@link VerticalCrs}
     * @return A new {@link VerticalDomain} whose extent represents the range
     *         where valid values can be found in all the supplied
     *         {@link VerticalDomain}s
     */
    public static VerticalDomain getIntersectionOfVerticalDomains(VerticalDomain... domains) {
        if (domains.length == 0) {
            throw new IllegalArgumentException(
                    "Must provide multiple domains to get an intersection");
        }
        if (domains[0] == null) {
            return null;
        }
        VerticalCrs verticalCrs = domains[0].getVerticalCrs();
        Double min = -Double.MAX_VALUE;
        Double max = Double.MAX_VALUE;
        boolean allVerticalAxes = true;
        Set<Double> axisVals = new HashSet<Double>();

        boolean allEqual = true;
        VerticalDomain comparison = domains[0];
        for (VerticalDomain domain : domains) {
            /*
             * If one of the domains is null, their intersection is null
             */
            if (domain == null) {
                return null;
            }
            if ((domain.getVerticalCrs() == null && verticalCrs != null)
                    || !domain.getVerticalCrs().equals(verticalCrs)) {
                throw new IllegalArgumentException(
                        "Vertical domain CRSs must match to calculate their union");
            }

            if (!domain.equals(comparison)) {
                allEqual = false;
            }

            if (!(domain instanceof VerticalAxis)) {
                /*
                 * Not all of our domains are vertical axes
                 */
                allVerticalAxes = false;
            }
            if (allVerticalAxes) {
                /*
                 * If we still think we have all vertical axes, add the axis
                 * values to the list
                 */
                axisVals.addAll(((VerticalAxis) domain).getCoordinateValues());
            }

            if (domain.getExtent().getLow() > min) {
                min = domain.getExtent().getLow();
            }
            if (domain.getExtent().getHigh() < max) {
                max = domain.getExtent().getHigh();
            }
        }

        if (allEqual) {
            return comparison;
        }

        if (allVerticalAxes) {
            /*
             * All of our domains were vertical axes, so we create a new axis
             * out of the intersection of all their points. Often it's the case
             * that all domains are the same.
             */
            List<Double> values = new ArrayList<Double>(axisVals);
            Collections.sort(values);
            return new VerticalAxisImpl("Derived vertical axis", values, verticalCrs);
        } else {
            return new SimpleVerticalDomain(min, max, verticalCrs);
        }
    }

    /**
     * Gets the intersection of a number of {@link TemporalDomain}s
     *
     * @param domains
     *            The {@link TemporalDomain}s to find a intersection of
     * @return A new {@link TemporalDomain} whose extent represents the range
     *         where valid values can be found in all the supplied
     *         {@link TemporalDomain}s
     */
    public static TemporalDomain getIntersectionOfTemporalDomains(TemporalDomain... domains) {
        if (domains.length == 0) {
            throw new IllegalArgumentException("Must provide multiple domains to get a union");
        }
        if (domains[0] == null) {
            return null;
        }
        Chronology chronology = domains[0].getChronology();
        DateTime min = new DateTime(0L, chronology);
        DateTime max = new DateTime(Long.MAX_VALUE, chronology);
        boolean allTimeAxes = true;
        Set<DateTime> axisVals = new HashSet<DateTime>();

        boolean allEqual = true;
        TemporalDomain comparison = domains[0];
        for (TemporalDomain domain : domains) {
            /*
             * If one of the domains is null, their intersection is null
             */
            if (domain == null) {
                return null;
            }

            if (!domain.equals(comparison)) {
                allEqual = false;
            }
            if (!(domain instanceof TimeAxis)) {
                /*
                 * Not all of our domains are time axes
                 */
                allTimeAxes = false;
            }
            if (allTimeAxes) {
                /*
                 * If we still think we have all time axes, add the axis values
                 * to the list, ensuring they are in the same chronology.
                 */
                for (DateTime time : ((TimeAxis) domain).getCoordinateValues()) {
                    axisVals.add(time.toDateTime(chronology));
                }
            }
            if (domain.getExtent().getLow().isAfter(min)) {
                min = domain.getExtent().getLow();
            }
            if (domain.getExtent().getHigh().isBefore(max)) {
                max = domain.getExtent().getHigh();
            }
        }

        if (allEqual) {
            return comparison;
        }
        if (allTimeAxes) {
            /*
             * All of our domains were time axes, so we create a new axis out of
             * the intersection of all their points. Often it's the case that
             * all domains are the same.
             */
            List<DateTime> values = new ArrayList<DateTime>(axisVals);
            Collections.sort(values);
            return new TimeAxisImpl("Derived time axis", values);
        } else {
            /*
             * There is no intersection between these TemporalDomain
             */
            if (max.isBefore(min)) {
                return null;
            }
            return new SimpleTemporalDomain(min, max);
        }
    }

    /**
     * Performs Pythagoras on two distances to calculate the distance squared.
     * Useful for sorting lists according to distance from a point. The result
     * shouldn't be used for anything critical, since two results may not
     * accurately reflect the true distance (e.g. two points near the pole may
     * incorrectly report a smaller distance apart than two near the equator).
     *
     * No check that the positions have the same CRS is performed (for speed).
     *
     * @param pos1
     *            The first position
     * @param pos2
     *            The second position
     * @return A number related to how far apart the positions are, or
     *         {@link Double#MAX_VALUE} if either is <code>null</code>
     */
    public static double getDistSquared(HorizontalPosition pos1, HorizontalPosition pos2) {
        if (pos1 == null || pos2 == null) {
            return Double.MAX_VALUE;
        }
        return (pos1.getX() - pos2.getX()) * (pos1.getX() - pos2.getX())
                + (pos1.getY() - pos2.getY()) * (pos1.getY() - pos2.getY());

    }

    /**
     * Limits a z-axis to include a range as tightly as possible
     *
     * @param axis
     *            The axis to limit
     * @param limits
     *            The range to limit to
     * @return A new {@link VerticalAxis} which will extend by at most one point
     *         over each of the bounds provided by limits, or the original axis
     *         if limits is <code>null</code>
     */
    public static VerticalAxis limitZAxis(VerticalAxis axis, Extent<Double> limits) {
        if (limits == null) {
            return axis;
        }
        if (limits.getHigh() < axis.getCoordinateExtent().getLow()
                || limits.getLow() > axis.getCoordinateExtent().getHigh()) {
            return null;
        }

        int lowIndex = 0;
        for (int i = 0; i < axis.size(); i++) {
            Double axisValue = axis.getCoordinateValue(i);
            if (axisValue <= limits.getLow()) {
                lowIndex = i;
            } else {
                break;
            }
        }
        int highIndex = axis.size() - 1;
        for (int i = axis.size() - 1; i >= 0; i--) {
            Double axisValue = axis.getCoordinateValue(i);
            if (axisValue >= limits.getHigh()) {
                highIndex = i;
            } else {
                break;
            }
        }

        List<Double> values = new ArrayList<Double>();
        for (int i = lowIndex; i <= highIndex; i++) {
            values.add(axis.getCoordinateValue(i));
        }
        return new VerticalAxisImpl(axis.getName(), values, axis.getVerticalCrs());
    }

    /**
     * Limits a t-axis to include a range as tightly as possible
     *
     * @param axis
     *            The axis to limit
     * @param limits
     *            The range to limit to
     * @return A new {@link TimeAxis} which will extend by at most one point
     *         over each of the bounds provided by limits, or the original axis
     *         if limits is <code>null</code>
     */
    public static TimeAxis limitTAxis(TimeAxis axis, Extent<DateTime> limits) {
        if (limits == null) {
            return axis;
        }
        if (limits.getHigh().isBefore(axis.getCoordinateExtent().getLow())
                || limits.getLow().isAfter(axis.getCoordinateExtent().getHigh())) {
            return null;
        }
        int lowIndex = 0;
        for (int i = 0; i < axis.size(); i++) {
            DateTime axisValue = axis.getCoordinateValue(i);
            if (axisValue.isBefore(limits.getLow()) || axisValue.isEqual(limits.getLow())) {
                lowIndex = i;
            } else {
                break;
            }
        }
        int highIndex = axis.size() - 1;
        for (int i = axis.size() - 1; i >= 0; i--) {
            DateTime axisValue = axis.getCoordinateValue(i);
            if (axisValue.isAfter(limits.getHigh()) || axisValue.isEqual(limits.getHigh())) {
                highIndex = i;
            } else {
                break;
            }
        }
        List<DateTime> values = new ArrayList<DateTime>();
        for (int i = lowIndex; i <= highIndex; i++) {
            values.add(axis.getCoordinateValue(i));
        }
        return new TimeAxisImpl(axis.getName(), values);
    }

    /**
     * Contains a single static flag. This can be used to set the path for the
     * EPSG database directory. If not set, the system's temporary directory
     * will be used
     *
     * This is in a separate class because the static{} block in GISUtils will
     * get executed before the class can be used, so setting something like
     * GISUtils.DB_PATH would have no effect - it gets set too late.
     */
    public static class EpsgDatabasePath {
        public static String DB_PATH = null;
    }

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
            Hashtable<?, ?> environment) throws Exception {
        return dataSource;
    }

    private static JdbcDataSource dataSource = new JdbcDataSource();
    private static Connection conn;
    static {
        /*
         * Initialise the EPSG database if necessary
         */
        try {
            log.debug("Creating EPSG database");
            Class.forName("org.h2.Driver");
            String path;
            if (EpsgDatabasePath.DB_PATH == null) {
                path = System.getProperty("java.io.tmpdir");
            } else {
                path = EpsgDatabasePath.DB_PATH;
            }
            // Ensure path is suitable for a URI, especially on Windows machines
            File fp = new File(path);
            path = fp.toURI().toString();
            /*
             * AUTO_SERVER=TRUE means that this DB will run in embedded mode on
             * the first JVM. However, if a second JVM tries to access it, it
             * will then start server mode.
             */
            String dbUrl = "jdbc:h2:" + path + "/.h2/epsg.db;AUTO_SERVER=TRUE";
            log.debug("Attempting to create EPSG datbase: " + dbUrl);
            dataSource.setURL(dbUrl);

            conn = dataSource.getConnection();
            conn.setAutoCommit(true);

            log.debug("EPSG database created successfully");
            /*
             * Install the standalone JNDI context. This will only get
             * registered if no other JNDI handler exists. In the case that this
             * class is within a webapp (e.g. ncWMS) inside a servlet container
             * (e.g. Tomcat), this will not do anything.
             */
//            JNDI.install(dataSource);

            /*
             * WARNING - THIS IS A *HORRIBLE* HACK
             * 
             * Previously we were using JNDI to declare the data source, either
             * by defining this class as the factory class for the DataSource in
             * the webapp context, or by using the small JNDI context when run
             * outside a webapp (itself a hack, although not a very horrible
             * one).
             * 
             * The trouble is that in ncWMS we suggest that people override the
             * context.xml to provide a config directory location which will
             * persist across application redeploys. Doing this overwrites the
             * supplied context, meaning that no database is created.
             * 
             * There is no satisfactory way around this.
             * 
             * So instead, we use reflection to set the private field on
             * Initializer (the Apache SIS class). THIS IS NOT A LONG TERM
             * SOLUTION and should be fixed as soon as possible. Fixes in order
             * of preference:
             * 
             * Get Apache to include a setter on Initializer, rather than
             * mandating JNDI
             * 
             * Find a way of binding the JNDI resource programmatically /
             * outside META-INF/context.xml
             * 
             * Move back to Geotoolkit (it's a little slower, but it doesn't
             * require hacks like this).
             */
            Field f = Initializer.class.getDeclaredField("source");
            f.setAccessible(true);
            f.set(null, dataSource);
            f.setAccessible(false);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            log.error("Problem creating EPSG database.  Reprojection will not work", e);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Problem creating EPSG database.  Reprojection will not work", e);
        }
    }

    public static void releaseEpsgDatabase() {
        try {
            conn.close();
        } catch (SQLException e) {
            log.error("Problem closing EPSG database", e);
        }
    }

    public static boolean isPressureUnits(String units) {
        if (units.equalsIgnoreCase("bar") || units.equalsIgnoreCase("standard_atmosphere")
                || units.equalsIgnoreCase("technical_atmosphere")
                || units.equalsIgnoreCase("inch_H2O_39F") || units.equalsIgnoreCase("inch_H2O_60F")
                || units.equalsIgnoreCase("inch_Hg_32F") || units.equalsIgnoreCase("inch_Hg_60F")
                || units.equalsIgnoreCase("millimeter_Hg_0C") || units.equalsIgnoreCase("footH2O")
                || units.equalsIgnoreCase("cmHg") || units.equalsIgnoreCase("cmH2O")
                || units.equalsIgnoreCase("Pa") || units.equalsIgnoreCase("inch_Hg")
                || units.equalsIgnoreCase("inch_hg") || units.equalsIgnoreCase("inHg")
                || units.equalsIgnoreCase("in_Hg") || units.equalsIgnoreCase("in_hg")
                || units.equalsIgnoreCase("millimeter_Hg") || units.equalsIgnoreCase("mmHg")
                || units.equalsIgnoreCase("mm_Hg") || units.equalsIgnoreCase("mm_hg")
                || units.equalsIgnoreCase("torr") || units.equalsIgnoreCase("foot_H2O")
                || units.equalsIgnoreCase("ftH2O") || units.equalsIgnoreCase("psi")
                || units.equalsIgnoreCase("ksi") || units.equalsIgnoreCase("barie")
                || units.equalsIgnoreCase("at") || units.equalsIgnoreCase("atmosphere")
                || units.equalsIgnoreCase("atm") || units.equalsIgnoreCase("barye")) {
            return true;
        }
        return false;
    }

    public static boolean isLatitudeUnits(String units) {
        if (units.equalsIgnoreCase("degrees_north") || units.equalsIgnoreCase("degree_north")
                || units.equalsIgnoreCase("degrees_N") || units.equalsIgnoreCase("degree_N")
                || units.equalsIgnoreCase("degreesN") || units.equalsIgnoreCase("degreeN"))
            return true;
        return false;
    }

    public static boolean isLongitudeUnits(String units) {
        if (units.equalsIgnoreCase("degrees_east") || units.equalsIgnoreCase("degree_east")
                || units.equalsIgnoreCase("degrees_E") || units.equalsIgnoreCase("degree_E")
                || units.equalsIgnoreCase("degreesE") || units.equalsIgnoreCase("degreeE"))
            return true;
        return false;
    }
}
