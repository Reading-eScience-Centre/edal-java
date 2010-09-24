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

package uk.ac.rdg.resc.edal.util;

import java.util.Collections;
import java.util.List;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.HorizontalPosition;
import uk.ac.rdg.resc.edal.geometry.HorizontalPositionList;
import uk.ac.rdg.resc.edal.geometry.LonLatPosition;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.geometry.impl.HorizontalPositionImpl;
import uk.ac.rdg.resc.edal.geometry.impl.LonLatPositionImpl;

/**
 * Contains some useful utility methods.
 * @author Jon
 */
public final class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    /** Prevents direct instantiation */
    private Utils() { throw new AssertionError(); }

    /**
     * Returns a longitude value in degrees that is equal to the given value
     * but in the range (-180:180].  In this scheme the anti-meridian is
     * represented as +180, not -180.
     */
    public static double constrainLongitude180(double value)
    {
        double val = constrainLongitude360(value);
        return val > 180.0 ? val - 360.0 : val;
    }

    /**
     * Returns a longitude value in degrees that is equal to the given value
     * but in the range [0:360]
     */
    public static double constrainLongitude360(double value)
    {
        double val = value % 360.0;
        return val < 0.0 ? val + 360.0 : val;
    }

    /**
     * Returns the smallest longitude value that is equivalent to the target
     * value and greater than the reference value.  Therefore if
     * {@code reference == 10.0} and {@code target == 5.0} this method will
     * return 365.0.
     */
    public static double getNextEquivalentLongitude(double reference, double target)
    {
        // Find the clockwise distance from the first value on this axis
        // to the target value.  This will be a positive number from 0 to
        // 360 degrees
        double clockDiff = Utils.constrainLongitude360(target - reference);
        return reference + clockDiff;
    }

    /**
     * Transforms the given HorizontalPosition to a new position in the given
     * coordinate reference system.
     * @param pos The position to translate
     * @param targetCrs The CRS to translate into
     * @return a new position in the given CRS, or the same position if the
     * new CRS is the same as the point's CRS.  The returned point's CRS will be
     * set to {@code targetCrs}.
     * @throws NullPointerException if {@code pos.getCoordinateReferenceSystem()}
     * is null, or if {@code targetCrs} is null.
     * @todo error handling
     */
    public static HorizontalPosition transformPosition(HorizontalPosition pos,
            CoordinateReferenceSystem targetCrs)
    {
        CoordinateReferenceSystem sourceCrs = pos.getCoordinateReferenceSystem();
        if (sourceCrs == null)
        {
            throw new NullPointerException("Position must have a valid CRS");
        }
        if (targetCrs == null)
        {
            throw new NullPointerException("Target CRS cannot be null");
        }
        // CRS.findMathTransform() caches recently-used transform objects so
        // we should incur no large penalty for multiple invocations
        try
        {
            MathTransform transform = CRS.findMathTransform(sourceCrs, targetCrs);
            if (transform.isIdentity()) return pos;
            double[] point = new double[]{pos.getX(), pos.getY()};
            transform.transform(point, 0, point, 0, 1);
            return new HorizontalPositionImpl(point[0], point[1], targetCrs);
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Transforms all the points in the given domain into a list of horizontal
     * positions in the required coordinate reference system, in the same order
     * as the positions within the domain
     * @param posList The list of positions to transform
     * @param targetCrs The CRS to translate into
     * @return a list of new positions in the given CRS.  The returned points'
     * CRS will be set to {@code targetCrs}.
     * @throws NullPointerException if {@code domain} is null, if
     * {@code pos.getCoordinateReferenceSystem()} is null, or if {@code targetCrs} is null.
     * @todo error handling
     * @todo perhaps we should change the return type to domain so that
     * we can simply return the source domain if the transform is the
     * identity transform?
     */
    public static List<HorizontalPosition> transformPositions(HorizontalPositionList posList,
            CoordinateReferenceSystem targetCrs)
    {
        logger.debug("Transforming {} points from {} to {}", new Object[]{
            posList.getPositions().size(),
            posList.getCoordinateReferenceSystem().getName(),
            targetCrs.getName()
        });
        
        CoordinateReferenceSystem sourceCrs = posList.getCoordinateReferenceSystem();
        if (posList == null) throw new NullPointerException("Domain cannot be null");
        if (sourceCrs == null) throw new NullPointerException("Position must have a valid CRS");
        if (targetCrs == null) throw new NullPointerException("Target CRS cannot be null");

        List<HorizontalPosition> domainObjects = posList.getPositions();

        // CRS.findMathTransform() caches recently-used transform objects so
        // we should incur no large penalty for multiple invocations
        try
        {
            MathTransform transform = CRS.findMathTransform(sourceCrs, targetCrs);
            // Convert the points from the domain into an array of doubles so
            // that we can transform them in a single operation
            double[] points = new double[domainObjects.size() * 2];
            int i = 0;
            for (HorizontalPosition pos : domainObjects)
            {
                points[i] = pos.getX();
                points[i+1] = pos.getY();
                i += 2;
            }
            // transform the points in-place
            transform.transform(points, 0, points, 0, domainObjects.size());

            // Create a new list of horizontal positions in the new CRS
            List<HorizontalPosition> newPosList = CollectionUtils.newArrayList();
            for (i = 0; i < points.length; i += 2)
            {
                newPosList.add(new HorizontalPositionImpl(points[i], points[i+1], targetCrs));
            }
            return Collections.unmodifiableList(newPosList);
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Transforms the given HorizontalPosition to a longitude-latitude position
     * in the WGS84 coordinate reference system.
     * @param pos The position to translate
     * @param targetCrs The CRS to translate into
     * @return a new position in the given CRS, or the same position if the
     * new CRS is the same as the point's CRS.  The returned point's CRS will be
     * set to {@code targetCrs}.
     * @throws NullPointerException if {@code pos.getCoordinateReferenceSystem()}
     * is null
     * @todo refactor to share code with above method?
     */
    public static LonLatPosition transformToWgs84LonLat(HorizontalPosition pos)
    {
        if (pos instanceof LonLatPosition) return (LonLatPosition)pos;
        CoordinateReferenceSystem sourceCrs = pos.getCoordinateReferenceSystem();
        if (sourceCrs == null)
        {
            throw new NullPointerException("Position must have a valid CRS");
        }
        // CRS.findMathTransform() caches recently-used transform objects so
        // we should incur no large penalty for multiple invocations
        try
        {
            MathTransform transform = CRS.findMathTransform(sourceCrs, DefaultGeographicCRS.WGS84);
            if (transform.isIdentity()) return new LonLatPositionImpl(pos.getX(), pos.getY());
            double[] point = new double[]{pos.getX(), pos.getY()};
            transform.transform(point, 0, point, 0, 1);
            return new LonLatPositionImpl(point[0], point[1]);
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns true if the CRS is a WGS84 longitude-latitude system (with the
     * longitude axis first).
     * @param crs
     * @return
     */
    public static boolean isWgs84LonLat(CoordinateReferenceSystem crs)
    {
        try
        {
            return CRS.findMathTransform(crs, DefaultGeographicCRS.WGS84).isIdentity();
        }
        catch(FactoryException fe)
        {
            return false;
        }
    }

    /**
     * Converts the given GeographicBoundingBox to a BoundingBox in WGS84
     * longitude-latitude coordinates.  This method assumes that the longitude
     * and latitude coordinates in the given GeographicBoundingBox are in the
     * WGS84 system (this is not always true: GeographicBoundingBoxes are
     * often approximate and in no specific CRS).
     */
    public static BoundingBox getBoundingBox(GeographicBoundingBox geoBbox)
    {
        return new BoundingBoxImpl(new double[]{
            geoBbox.getWestBoundLongitude(),
            geoBbox.getSouthBoundLatitude(),
            geoBbox.getEastBoundLongitude(),
            geoBbox.getNorthBoundLatitude()},
            DefaultGeographicCRS.WGS84
        );
    }

//    public static void main(String[] args) throws Exception
//    {
//        CoordinateReferenceSystem nPolar = CRS.decode("EPSG:32661", true);
//        double x = 2000000;
//        double y = 2000000;
//        HorizontalPosition pos = new HorizontalPositionImpl(x/4, y, nPolar);
//        HorizontalPosition pos84 = transformPosition(pos, DefaultGeographicCRS.WGS84);
//        System.out.printf("%f,%f%n", pos84.getX(), pos84.getY());
//
//        System.out.println(nPolar);
//
//
//        CrsHelper helper = CrsHelper.fromCrs(CRS.decode("EPSG:32661", true));
//        LonLatPosition lonLat = helper.crsToLonLat(x/4, y);
//        System.out.printf("%f,%f%n", lonLat.getLongitude(), lonLat.getLatitude());
//    }

}
