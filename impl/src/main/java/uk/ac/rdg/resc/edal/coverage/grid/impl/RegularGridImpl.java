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

package uk.ac.rdg.resc.edal.coverage.grid.impl;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.RangeMeaning;

import uk.ac.rdg.resc.edal.coverage.grid.RegularAxis;
import uk.ac.rdg.resc.edal.coverage.grid.RegularGrid;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;

/**
 * Immutable implementation of a {@link RegularGrid}.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public class RegularGridImpl extends AbstractRectilinearGrid implements RegularGrid
{
    private final RegularAxis xAxis;
    private final RegularAxis yAxis;
    private final CoordinateReferenceSystem crs;

    public RegularGridImpl(RegularAxis xAxis, RegularAxis yAxis, CoordinateReferenceSystem crs) {
        if (xAxis == null || yAxis == null) {
            throw new NullPointerException("Axes cannot be null");
        }
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.crs = crs;
    }

    /**
     * Constructs a RegularGrid with a null coordinate reference system
     */
    public RegularGridImpl(RegularAxis xAxis, RegularAxis yAxis) {
        this(xAxis, yAxis, null);
    }

    /**
     * <p>
     * Constructs a RegularGrid from the given bounding box, with the given
     * width and height. Note that the bounding box represents the edges of the
     * grid, whereas grid coordinates represent the centre of the grid points.
     * </p>
     * 
     * <p>
     * The coordinate reference system of the returned grid will be taken from
     * the CRS of the bounding box. If this is null, the names of the axes will
     * be set to "Unknown X axis" and "Unknown Y axis".
     * </p>
     * 
     * @param bbox
     *            The bounding box of the grid
     * @param width
     *            the number of grid points in the x direction
     * @param height
     *            the number of grid points in the y direction
     */
    public RegularGridImpl(BoundingBox bbox, int width, int height) {
        this(bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY(), bbox
                .getCoordinateReferenceSystem(), width, height);
    }

    /**
     * Constructs a RegularGrid from the given bounding box, with the given
     * width and height. Note that the bounding box represents the edges of the
     * grid, whereas grid coordinates represent the centre of the grid points.
     * 
     * @param bbox
     *            the bounding box, in the form: [minx, miny, maxx, maxy]
     * @param crs
     *            the {@link CoordinateReferenceSystem} of the bounding box
     * @param width
     *            the number of grid points in the x direction
     * @param height
     *            the number of grid points in the y direction
     */
    public RegularGridImpl(double[] bbox, CoordinateReferenceSystem crs, int width, int height) {
        this(bbox[0], bbox[1], bbox[2], bbox[3], crs, width, height);
    }

    /**
     * Constructs a RegularGrid from the given bounding box, with the given
     * width and height. Note that the bounding box represents the edges of the
     * grid, whereas grid coordinates represent the centre of the grid points.
     * 
     * @param minx
     *            the minimum x value of the bounding box
     * @param miny
     *            the minimum y value of the bounding box
     * @param maxx
     *            the maximum x value of the bounding box
     * @param maxy
     *            the maximum y value of the bounding box
     * @param crs
     *            the {@link CoordinateReferenceSystem}
     * @param width
     *            the number of grid points in the x-direction
     * @param height
     *            the number of grid points in the y-direction
     */
    public RegularGridImpl(double minx, double miny, double maxx, double maxy,
            CoordinateReferenceSystem crs, int width, int height) {
        if (maxx < minx || maxy < miny) {
            throw new IllegalArgumentException("Invalid bounding box");
        }
        
        this.crs = crs;

        double xSpacing = (maxx - minx) / width;
        double ySpacing = (maxy - miny) / height;

        // The axis values represent the centres of the grid points
        double firstXAxisValue = minx + (0.5 * xSpacing);
        double firstYAxisValue = miny + (0.5 * ySpacing);

        if (crs == null) {
            /*
             * If we don't have a crs, we can't tell if an axis is longitude
             */
            xAxis = new RegularAxisImpl("Unknown X axis", firstXAxisValue, xSpacing, width,
                    false);
            yAxis = new RegularAxisImpl("Unknown Y axis", firstYAxisValue, ySpacing, height, false);
        } else {
            /*
             * If we do have a crs, we can use rangeMeaning==WRAPAROUND to
             * determine whether the axis is longitude
             * 
             * TODO There may be wrapped axes where this is inappropriate.
             * Perhaps change isLongitude to wraps in RegularAxisImpl, and set a
             * wrap value?
             */
            CoordinateSystem cs = crs.getCoordinateSystem();
            xAxis = new RegularAxisImpl(cs.getAxis(0), firstXAxisValue, xSpacing, width,
                    (cs.getAxis(0).getRangeMeaning()==RangeMeaning.WRAPAROUND));
            // y axis is very unlikely to be longitude
            yAxis = new RegularAxisImpl(cs.getAxis(1), firstYAxisValue, ySpacing, height, (cs
                    .getAxis(1).getRangeMeaning() == RangeMeaning.WRAPAROUND));
        }
    }

    @Override
    public RegularAxis getXAxis() {
        return xAxis;
    }

    @Override
    public RegularAxis getYAxis() {
        return yAxis;
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return this.crs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((xAxis == null) ? 0 : xAxis.hashCode());
        result = prime * result + ((yAxis == null) ? 0 : yAxis.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RegularGridImpl other = (RegularGridImpl) obj;
        if (xAxis == null) {
            if (other.xAxis != null)
                return false;
        } else if (!xAxis.equals(other.xAxis))
            return false;
        if (yAxis == null) {
            if (other.yAxis != null)
                return false;
        } else if (!yAxis.equals(other.yAxis))
            return false;
        return true;
    }

}
