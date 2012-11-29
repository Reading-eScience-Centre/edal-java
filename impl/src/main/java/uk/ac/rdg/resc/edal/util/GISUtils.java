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

package uk.ac.rdg.resc.edal.util;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.RangeMeaning;
import org.opengis.referencing.operation.MathTransform;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.DiscreteCoverage;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.TimeAxisImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.VerticalAxisImpl;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.impl.MetadataUtils;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.LonLatPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.position.impl.HorizontalPositionImpl;
import uk.ac.rdg.resc.edal.position.impl.LonLatPositionImpl;
import uk.ac.rdg.resc.edal.position.impl.TimePositionJoda;
import uk.ac.rdg.resc.edal.position.impl.VerticalPositionImpl;

/**
 * A class containing static methods which are useful for GIS operations. One of
 * the main purposes of this class is to provide various feature-specific
 * retrievals (for example getting a time axis of a general feature). This means
 * that if new feature types are added, much of the work to integrate them will
 * be in this class
 * 
 * @author Guy Griffiths
 * 
 */
public final class GISUtils {

    private GISUtils() {}

    /**
     * Converts the given GeographicBoundingBox to a BoundingBox in WGS84
     * longitude-latitude coordinates. This method assumes that the longitude
     * and latitude coordinates in the given GeographicBoundingBox are in the
     * WGS84 system (this is not always true: GeographicBoundingBoxes are often
     * approximate and in no specific CRS).
     */
    public static BoundingBox getBoundingBox(GeographicBoundingBox geoBbox) {
        return new BoundingBoxImpl(new double[] { geoBbox.getWestBoundLongitude(), geoBbox.getSouthBoundLatitude(),
                geoBbox.getEastBoundLongitude(), geoBbox.getNorthBoundLatitude() }, DefaultGeographicCRS.WGS84);
    }

    /**
     * Transforms the given HorizontalPosition to a longitude-latitude position
     * in the WGS84 coordinate reference system.
     * 
     * @param pos
     *            The position to translate
     * @param targetCrs
     *            The CRS to translate into
     * @return a new position in the given CRS, or the same position if the new
     *         CRS is the same as the point's CRS. The returned point's CRS will
     *         be set to {@code targetCrs}.
     * @throws NullPointerException
     *             if {@code pos.getCoordinateReferenceSystem()} is null
     * @todo refactor to share code with above method?
     */
    public static LonLatPosition transformToWgs84LonLat(HorizontalPosition pos) {
        if (pos instanceof LonLatPosition)
            return (LonLatPosition) pos;
        CoordinateReferenceSystem sourceCrs = pos.getCoordinateReferenceSystem();
        if (sourceCrs == null) {
            throw new NullPointerException("Position must have a valid CRS");
        }
        // CRS.findMathTransform() caches recently-used transform objects so
        // we should incur no large penalty for multiple invocations
        try {
            MathTransform transform = CRS.findMathTransform(sourceCrs, DefaultGeographicCRS.WGS84);
            if (transform.isIdentity())
                return new LonLatPositionImpl(pos.getX(), pos.getY());
            double[] point = new double[] { pos.getX(), pos.getY() };
            transform.transform(point, 0, point, 0, 1);
            return new LonLatPositionImpl(point[0], point[1]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
     *         be set to {@code targetCrs}.  If the CRS of the position is null,
     *         the CRS will simply be set to the targetCrs.
     * @throws NullPointerException
     *             if {@code targetCrs} is null.
     * @todo error handling
     */
    public static HorizontalPosition transformPosition(HorizontalPosition pos, CoordinateReferenceSystem targetCrs) {
        if (targetCrs == null) {
            throw new NullPointerException("Target CRS cannot be null");
        }
        CoordinateReferenceSystem sourceCrs = pos.getCoordinateReferenceSystem();
        if (sourceCrs == null) {
            return new HorizontalPositionImpl(pos.getX(), pos.getY(), targetCrs);
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
            return new HorizontalPositionImpl(point[0], point[1], targetCrs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns true if the CRS is a WGS84 longitude-latitude system (with the
     * longitude axis first).
     * 
     * @param crs
     * @return
     */
    public static boolean isWgs84LonLat(CoordinateReferenceSystem crs) {
        try {
            return CRS.findMathTransform(crs, DefaultGeographicCRS.WGS84).isIdentity();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Returns the smallest longitude value that is equivalent to the target
     * value and greater than the reference value. Therefore if {@code reference
     * == 10.0} and {@code target == 5.0} this method will return 365.0.
     */
    public static double getNextEquivalentLongitude(double reference, double target) {
        // Find the clockwise distance from the first value on this axis
        // to the target value. This will be a positive number from 0 to
        // 360 degrees
        double clockDiff = constrainLongitude360(target - reference);
        return reference + clockDiff;
    }

    /**
     * Returns a longitude value in degrees that is equal to the given value but
     * in the range (-180:180]. In this scheme the anti-meridian is represented
     * as +180, not -180.
     */
    public static double constrainLongitude180(double value) {
        double val = constrainLongitude360(value);
        return val > 180.0 ? val - 360.0 : val;
    }

    /**
     * Returns a longitude value in degrees that is equal to the given value but
     * in the range [0:360]
     */
    public static double constrainLongitude360(double value) {
        double val = value % 360.0;
        return val < 0.0 ? val + 360.0 : val;
    }
    
    /**
     * Calculates the magnitude of the vector components given in the provided
     * Lists.  The two lists must be of the same length.  For any element in the
     * component lists, if either east or north is null, the magnitude will also
     * be null.
     * @return a List of the magnitudes calculated from the components.
     */
    public static List<Float> getMagnitudes(List<Float> eastData, List<Float> northData)
    {
        if (eastData == null || northData == null) throw new NullPointerException();
        if (eastData.size() != northData.size())
        {
            throw new IllegalArgumentException("east and north data components must be the same length");
        }
        List<Float> mag = new ArrayList<Float>(eastData.size());
        for (int i = 0; i < eastData.size(); i++)
        {
            Float east = eastData.get(i);
            Float north = northData.get(i);
            Float val = null;
            if (east != null && north != null)
            {
                val = (float)Math.sqrt(east * east + north * north);
            }
            mag.add(val);
        }
        if (mag.size() != eastData.size()) throw new AssertionError();
        return mag;
    }
    
    /**
     * Estimate the range of values in this layer by reading a sample of data
     * from the default time and elevation. Works for both Scalar and Vector
     * layers.
     * 
     * @return
     * @throws IOException
     *             if there was an error reading from the source data
     */
    public static Extent<Float> estimateValueRange(Feature feature, String member) {
        List<Float> dataSample = readDataSample(feature, member);
        return Extents.findMinMax(dataSample);
    }

    private static List<Float> readDataSample(Feature feature, String member) {
        List<Float> ret = new ArrayList<Float>();
        /*
         * This will throw an IllegalArgumentException if this member isn't plottable.
         */
        String scalarMemberName = MetadataUtils.getScalarMemberName(feature, member);
        ScalarMetadata scalarMetadata = (ScalarMetadata) MetadataUtils.getMetadataForFeatureMember(feature, scalarMemberName);
        if(scalarMetadata == null){
            throw new IllegalArgumentException(member+" is not scalar - cannot read data sample");
        }
        Class<?> clazz = scalarMetadata.getValueType();
        if (!Number.class.isAssignableFrom(clazz)) {
            /*
             * TODO a more elegant solution? Some kind of None value for scale
             * ranges?
             * 
             * We want a non-numerical value range. Return whatever you like
             */
            ret.add(0.0f);
            ret.add(100.0f);
            return ret;
        }
        /*
         * Read a low-resolution grid of data covering the entire spatial extent
         */
        List<?> values = null;
        if (feature instanceof GridFeature) {
            GridFeature gridFeature = (GridFeature) feature;
            feature = gridFeature.extractGridFeature(new RegularGridImpl(gridFeature.getCoverage()
                    .getDomain().getCoordinateExtent(), 100, 100), CollectionUtils.setOf(scalarMemberName));
        } else if (feature instanceof GridSeriesFeature) {
            GridSeriesFeature gridSeriesFeature = (GridSeriesFeature) feature;
            feature = gridSeriesFeature.extractGridFeature(
                    new RegularGridImpl(gridSeriesFeature.getCoverage().getDomain()
                            .getHorizontalGrid().getCoordinateExtent(), 100, 100),
                    getClosestElevationToSurface(gridSeriesFeature),
                    getClosestToCurrentTime(gridSeriesFeature.getCoverage().getDomain()
                            .getTimeAxis()), CollectionUtils.setOf(scalarMemberName));
        }

        if (feature.getCoverage() instanceof DiscreteCoverage) {
            DiscreteCoverage<?, ?> discreteCoverage = (DiscreteCoverage<?, ?>) feature
                    .getCoverage();
            values = discreteCoverage.getValues(scalarMemberName);
        } else {
            throw new UnsupportedOperationException("Currently we only support discrete coverages");
        }

        for (Object r : values) {
            Number num = (Number) r;
            if (num == null || num.equals(Float.NaN) || num.equals(Double.NaN)) {
                ret.add(null);
            } else {
                ret.add(num.floatValue());
            }
        }
        return ret;
    }

    public static TimePosition getClosestToCurrentTime(TimeAxis tAxis) {
        return getClosestTimeTo(new TimePositionJoda(), tAxis);
    }

    /**
     * Returns the closest time within a time axis to the given time.
     * 
     * @param targetTime
     *            The target time
     * @param tAxis
     *            The time axis to check
     * @return Either the closest time within that axis, or the closest to the
     *         current time if the target is <code>null</code>, or
     *         <code>null</code> if the time axis is <code>null</code>
     */
    public static TimePosition getClosestTimeTo(TimePosition targetTime, TimeAxis tAxis) {
        if(tAxis == null) {
            return null;
        }
        if (targetTime == null) {
            return getClosestToCurrentTime(tAxis);
        }
        int index = TimeUtils.findTimeIndex(tAxis.getCoordinateValues(), new TimePositionJoda());
        if (index < 0) {
            /*
             * We can calculate the insertion point
             */
            int insertionPoint = -(index + 1);
            /*
             * We set the index to the most recent past time
             */
            if (insertionPoint > 0) {
                /*
                 * The most recent past time
                 */
                index = insertionPoint - 1;
            } else {
                /*
                 * All DateTimes on the axis are in the future, so we take the
                 * earliest
                 */
                index = 0;
            }
        }

        return tAxis.getCoordinateValue(index);
    }

    /**
     * Returns the uppermost elevation of the given feature
     * 
     * @param feature
     *            The feature to test
     * @return The uppermost elevation, or null if the feature doesn't have a
     *         vertical axis
     */
    public static VerticalPosition getClosestElevationToSurface(Feature feature) {
        VerticalAxis vAxis = getVerticalAxis(feature);
        if (vAxis == null) {
            return null;
        }

        double value;
        if (vAxis.getVerticalCrs().isPressure()) {
            /*
             * The vertical axis is pressure. The default (closest to the
             * surface) is therefore the maximum value.
             */
            value = Collections.max(vAxis.getCoordinateValues());
        } else {
            /*
             * The vertical axis represents linear height, so we find which
             * value is closest to zero (the surface), i.e. the smallest
             * absolute value
             */
            value = Collections.min(vAxis.getCoordinateValues(), new Comparator<Double>() {
                @Override
                public int compare(Double d1, Double d2) {
                    return Double.compare(Math.abs(d1), Math.abs(d2));
                }
            });
        }
        return new VerticalPositionImpl(value, vAxis.getVerticalCrs());
    }
    
    /**
     * Gets the closest elevation to the target within the given feature.
     * 
     * @param targetDepth
     *            The target elevation
     * @param feature
     *            The feature containing the domain to be searched
     * @return Either the closest elevation to the target elevation, or the
     *         closest elevation to the surface if the target is
     *         <code>null</code>, or null if the {@link Feature} has no
     *         {@link VerticalAxis}
     */
    public static VerticalPosition getClosestElevationTo(Double targetDepth, Feature feature) {
        if(targetDepth == null) {
            return getClosestElevationToSurface(feature);
        }
        VerticalAxis vAxis = getVerticalAxis(feature);
        if(vAxis == null) {
            return null;
        }
//        if(vAxis.getCoordinateValues().size() == 1){
//            /*
//             * If we only have a single on the axis, return it.
//             */
//            return new VerticalPositionImpl(vAxis.getCoordinateValue(0), vAxis.getVerticalCrs());
//        }
//        if(!vAxis.getCoordinateExtent().contains(targetDepth)){
//            return null;
//        }
        
        List<Double> values = vAxis.getCoordinateValues();
        int index = Collections.binarySearch(values, targetDepth);
        if(index < 0){
            index = -(index + 1);
            if (index == values.size()) {
                index--;
            }
        }
        return new VerticalPositionImpl(values.get(index), vAxis.getVerticalCrs());
    }

    /**
     * Gets the exact elevation required, or <code>null</code> if it is not
     * present in the domain of the given feature
     * 
     * @param elevationStr
     *            The desired elevation
     * @param feature
     *            The feature containing the domain
     * @return The {@link VerticalPosition} required, or <code>null</code>
     */
    public static VerticalPosition getExactElevation(String elevationStr, Feature feature) {
        if(elevationStr == null) {
            return null;
        }
        VerticalAxis vAxis = getVerticalAxis(feature);
        if(vAxis == null){
            return null;
        }
        Double elevation;
        try {
            elevation = Double.parseDouble(elevationStr);
        } catch (Exception e) {
            /*
             * If we have any exception here, it means we cannot parse the
             * string for a double, so we want to return null.
             */
            return null;
        }
        List<Double> values = vAxis.getCoordinateValues();
        int index = Collections.binarySearch(values, elevation);
        if(index < 0){
            return null;
        }
        return new VerticalPositionImpl(vAxis.getCoordinateValue(index), vAxis.getVerticalCrs());
    }

    /**
     * Utility to get the vertical axis of a feature, if it exists
     * 
     * @param feature
     *            the feature to check
     * @return the {@link VerticalAxis}, or <code>null</code> if none exists
     */
    public static VerticalAxis getVerticalAxis(Feature feature) {
        if (feature instanceof GridSeriesFeature) {
            return ((GridSeriesFeature) feature).getCoverage().getDomain().getVerticalAxis();
        } else if (feature instanceof ProfileFeature) {
            ProfileFeature profileFeature = (ProfileFeature) feature;
            return new VerticalAxisImpl("z", profileFeature.getCoverage().getDomain().getZValues(),
                    profileFeature.getCoverage().getDomain().getVerticalCrs());
        } else {
            return null;
        }
    }
    
    /**
     * Utility to get the vertical CRS of a feature, if it exists
     * 
     * @param feature
     *            the feature to check
     * @return the {@link VerticalCrs}, or <code>null</code> if none exists
     */
    public static VerticalCrs getVerticalCrs(Feature feature) {
        if (feature instanceof GridSeriesFeature) {
            return ((GridSeriesFeature) feature).getCoverage().getDomain().getVerticalAxis().getVerticalCrs();
        } else if (feature instanceof ProfileFeature) {
            return ((ProfileFeature) feature).getCoverage().getDomain().getVerticalCrs();
        } else if (feature instanceof PointSeriesFeature) {
            return ((PointSeriesFeature) feature).getVerticalPosition().getCoordinateReferenceSystem();
        } else if (feature instanceof TrajectoryFeature) {
            return ((TrajectoryFeature) feature).getCoverage().getDomain().getVerticalCrs();
        } else {
            return null;
        }
    }
    
    public static HorizontalPosition getClosestHorizontalPositionTo(HorizontalPosition pos,
            Feature feature) {
        if (feature instanceof GridSeriesFeature) {
            GridCell2D cell = ((GridSeriesFeature) feature).getCoverage().getDomain().getHorizontalGrid().findContainingCell(pos);
            if(cell != null){
                return cell.getCentre();
            }
        } else if (feature instanceof GridFeature) {
            GridCell2D cell = ((GridFeature) feature).getCoverage().getDomain().findContainingCell(pos);
            if(cell != null){
                return cell.getCentre();
            }
        } else if (feature instanceof ProfileFeature) {
            return ((ProfileFeature) feature).getHorizontalPosition();
        } else if (feature instanceof PointSeriesFeature) {
            return ((PointSeriesFeature) feature).getHorizontalPosition();
        } else if (feature instanceof TrajectoryFeature) {
            List<GeoPosition> domainObjects = ((TrajectoryFeature) feature).getCoverage().getDomain().getDomainObjects();
            double dist = Double.MAX_VALUE;
            GeoPosition ret = null;
            for(GeoPosition gp : domainObjects) {
                double tempDist = Math.pow(gp.getHorizontalPosition().getX() - pos.getX(), 2)
                        + Math.pow(gp.getHorizontalPosition().getY() - pos.getY(), 2); 
                if(tempDist < dist) {
                    dist = tempDist;
                    ret = gp;
                }
            }
            return ret == null ? null : ret.getHorizontalPosition();
        }
        return null;
    }

    /**
     * Equivalent to {@link GISUtils#getTimeAxis(Feature, boolean)} with the 2nd
     * argument set to false
     */
    public static TimeAxis getTimeAxis(final Feature feature) {
        return getTimeAxis(feature, false);
    }
    
    /**
     * Utility to get the time axis of a feature, if it exists
     * 
     * @param feature
     *            the feature to check
     * @param force
     *            whether to create a fake time axis if the feature has a single
     *            time value
     * @return the {@link TimeAxis}, or <code>null</code> if none exists
     */
    public static TimeAxis getTimeAxis(final Feature feature, boolean force) {
        if (feature instanceof GridSeriesFeature) {
            return ((GridSeriesFeature) feature).getCoverage().getDomain().getTimeAxis();
        } else if (feature instanceof PointSeriesFeature) {
            return new TimeAxisImpl("time", ((PointSeriesFeature) feature).getCoverage()
                    .getDomain().getTimes());
        } else if (feature instanceof TrajectoryFeature) {
            return new TimeAxisImpl("time", new AbstractList<TimePosition>() {
                final List<GeoPosition> positions = ((TrajectoryFeature) feature).getCoverage().getDomain().getDomainObjects();
                @Override
                public TimePosition get(int index) {
                    return positions.get(index).getTimePosition();
                }

                @Override
                public int size() {
                    return positions.size();
                }
            });
        } else {
            if(force){
                if (feature instanceof ProfileFeature) {
                    return new TimeAxisImpl("time", Arrays.asList(((ProfileFeature) feature).getTime()));
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    public static BoundingBox getFeatureHorizontalExtent(Feature feature) {
        if (feature instanceof GridSeriesFeature) {
            return ((GridSeriesFeature) feature).getCoverage().getDomain().getHorizontalGrid()
                    .getCoordinateExtent();
        } else if (feature instanceof GridFeature) {
            return ((GridFeature) feature).getCoverage().getDomain().getCoordinateExtent();
        } else if (feature instanceof ProfileFeature) {
            HorizontalPosition horizontalPosition = ((ProfileFeature) feature)
                    .getHorizontalPosition();
            return new BoundingBoxImpl(horizontalPosition, horizontalPosition);
        } else if (feature instanceof PointSeriesFeature) {
            HorizontalPosition horizontalPosition = ((PointSeriesFeature) feature)
                    .getHorizontalPosition();
            return new BoundingBoxImpl(horizontalPosition, horizontalPosition);
        } else if (feature instanceof TrajectoryFeature) {
            return ((TrajectoryFeature) feature).getCoverage().getDomain().getCoordinateBounds();
        } else {
            return null;
        }
    }

    public static Extent<TimePosition> getFeatureTimeExtent(Feature feature) {
        if (feature instanceof GridSeriesFeature) {
            TimeAxis timeAxis = ((GridSeriesFeature) feature).getCoverage().getDomain().getTimeAxis();
            if(timeAxis != null){
                return timeAxis.getCoordinateExtent();
            } else {
                return null;
            }
        } else if (feature instanceof ProfileFeature) {
            TimePosition time = ((ProfileFeature) feature).getTime();
            return Extents.newExtent(time, time);
        } else if (feature instanceof PointSeriesFeature) {
            return ((PointSeriesFeature) feature).getCoverage().getDomain().getExtent();
        } else if (feature instanceof TrajectoryFeature) {
            return ((TrajectoryFeature) feature).getCoverage().getDomain().getTimeExtent();
        } else {
            return null;
        }
    }

    public static Extent<VerticalPosition> getFeatureVerticalExtent(Feature feature) {
        if (feature instanceof GridSeriesFeature) {
            VerticalAxis verticalAxis = ((GridSeriesFeature) feature).getCoverage().getDomain().getVerticalAxis();
            if(verticalAxis != null){
                Extent<Double> coordinateExtent = verticalAxis.getCoordinateExtent();
                return Extents.newExtent(
                        (VerticalPosition) new VerticalPositionImpl(coordinateExtent.getLow(),
                                getVerticalCrs(feature)),
                                new VerticalPositionImpl(coordinateExtent.getHigh(), getVerticalCrs(feature)));
            } else {
                return null;
            }
        } else if (feature instanceof ProfileFeature) {
            return ((ProfileFeature) feature).getCoverage().getDomain().getVerticalExtent();
        } else if (feature instanceof PointSeriesFeature) {
            VerticalPosition zPos = ((PointSeriesFeature) feature).getVerticalPosition();
            return Extents.newExtent(zPos, zPos);
        } else if (feature instanceof TrajectoryFeature) {
            return ((TrajectoryFeature) feature).getCoverage().getDomain().getVerticalExtent();
        } else {
            return null;
        }
    }

    /**
     * Checks whether a bounding box overlaps with a feature's extent
     * @param bbox The bounding box to check
     * @param feature The feature to check
     * @return
     */
    public static boolean featureOverlapsBoundingBox(BoundingBox bbox, Feature feature) {
        /*
         * Get the feature bounding box
         */
        BoundingBox featureBbox = getFeatureHorizontalExtent(feature);
        
        /*
         * Get 2 corners of the bounding box in the feature CRS
         */
        CoordinateReferenceSystem crs = featureBbox.getCoordinateReferenceSystem();
        HorizontalPosition bLowerCorner = transformPosition(bbox.getLowerCorner(), crs);
        HorizontalPosition bUpperCorner = transformPosition(bbox.getUpperCorner(), crs);

        /*
         * Check the CRS we are now working in to see if either of the axes wraps
         */
        CoordinateSystem coordinateSystem = crs.getCoordinateSystem();
        boolean wrapX = false;
        boolean wrapY = false;
        if(coordinateSystem.getDimension() >= 2){
            if(coordinateSystem.getAxis(0).getRangeMeaning() == RangeMeaning.WRAPAROUND){
                wrapX = true;
            }
            if(coordinateSystem.getAxis(1).getRangeMeaning() == RangeMeaning.WRAPAROUND){
                wrapY = true;
            }
        }
        
        /*
         * Get some x and y bounds for the feature and the bbox
         */
        double xFMin = featureBbox.getMinX();
        double xFMax = featureBbox.getMaxX();
        double xBMin = bLowerCorner.getX();
        double xBMax = bUpperCorner.getX();
        
        double yFMin = featureBbox.getMinY();
        double yFMax = featureBbox.getMaxY();
        double yBMin = bLowerCorner.getY();
        double yBMax = bUpperCorner.getY();
        
        /*
         * Our algorithm checks if the x-extent of the feature overlaps with
         * that of the bounding box, then does the same for the y-extent.
         * 
         * If both overlap, we return true.
         * 
         * We do 3 checks to determine overlap:
         * 
         * The bounding box x-extent contains the minimum value of the feature
         * x-extent
         * 
         * The bounding box x-extent contains the maximum value of the feature
         * x-extent
         * 
         * The feature x-extent contains both values of the bounding box extent
         * (we only need to check one)
         * 
         * However, all of these checks need to ensure that axis wrapping has
         * been taken into account, so we define 3 checking variables, which are
         * wrapped in the context of the extent being checked
         */
        double xMinCheck = wrapX ? GISUtils.getNextEquivalentLongitude(xBMin, xFMin) : xFMin;
        double xMaxCheck = wrapX ? GISUtils.getNextEquivalentLongitude(xBMin, xFMax) : xFMax;
        double xBMinCheck = wrapX ? GISUtils.getNextEquivalentLongitude(xFMin, xBMin) : xBMin;

        if ( !(xBMin <= xMinCheck && xBMax >= xMinCheck)
                && !(xBMin <= xMaxCheck && xBMax >= xMaxCheck)
                && !(xFMin <= xBMinCheck && xFMax >= xBMinCheck)) {
            /*
             * No need to check the y extents, we've already failed
             */
            return false;
        } else {
            double yMinCheck = wrapY ? GISUtils.getNextEquivalentLongitude(yBMin, yFMin) : yFMin;
            double yMaxCheck = wrapY ? GISUtils.getNextEquivalentLongitude(yBMin, yFMax) : yFMax;
            double yBMinCheck = wrapY ? GISUtils.getNextEquivalentLongitude(yFMin, yBMin) : yBMin;
            if ((yBMin <= yMinCheck && yBMax >= yMinCheck)
                    || (yBMin <= yMaxCheck && yBMax >= yMaxCheck)
                    || (yFMin <= yBMinCheck && yFMax >= yBMinCheck)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static boolean timeRangeContains(Extent<TimePosition> tRange, Feature feature) {
        Extent<TimePosition> featureTimeExtent = getFeatureTimeExtent(feature);
        /*
         * We check if the feature extent contains one end (it doesn't matter
         * which) of the desired extent (in case it contains the whole)
         * 
         * Then we check if the desired extent contains either end of the
         * feature extent
         */
        return  featureTimeExtent.contains(tRange.getLow())
                || tRange.contains(featureTimeExtent.getLow())
                || tRange.contains(featureTimeExtent.getHigh());
    }

    public static boolean zRangeContains(Extent<Double> zRange, Feature feature) {
        Extent<VerticalPosition> featureZExtent = getFeatureVerticalExtent(feature);
        /*
         * Similar to the time/bounding box situation, except we check in a
         * different order, since we need to instantiate a new VerticalPosition
         */
        return  zRange.contains(featureZExtent.getLow().getZ())
                || zRange.contains(featureZExtent.getHigh().getZ())
                || featureZExtent.contains(new VerticalPositionImpl(zRange.getLow(), getVerticalCrs(feature)));
    }

    public static GeoPosition getTrajectoryPosition(TrajectoryFeature feature, final HorizontalPosition pos,
            BoundingBox bbox) {
        List<GeoPosition> potentialMatches = new ArrayList<GeoPosition>();
        for(GeoPosition pos4d : feature.getCoverage().getDomain().getDomainObjects()){
            if(bbox.contains(pos4d.getHorizontalPosition())){
                potentialMatches.add(pos4d);
            }
        }
        if(potentialMatches.size() == 0){
            return null;
        }
        if(potentialMatches.size() > 1){
            Collections.sort(potentialMatches, new Comparator<GeoPosition>() {
                @Override
                public int compare(GeoPosition pos1, GeoPosition pos2) {
                    Double dist0 = Math.pow(pos1.getHorizontalPosition().getY() - pos.getY(), 2.0)
                            + Math.pow(pos1.getHorizontalPosition().getX() - pos.getX(), 2.0);
                    Double dist1 = Math.pow(pos2.getHorizontalPosition().getY() - pos.getY(), 2.0)
                            + Math.pow(pos2.getHorizontalPosition().getX() - pos.getX(), 2.0);
                    return dist0.compareTo(dist1);
                }
            });
        }
        return potentialMatches.get(0);
    }
}
