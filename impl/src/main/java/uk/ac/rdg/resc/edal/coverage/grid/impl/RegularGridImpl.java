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
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR  CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package uk.ac.rdg.resc.edal.coverage.grid.impl;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import uk.ac.rdg.resc.edal.coverage.grid.RegularAxis;
import uk.ac.rdg.resc.edal.coverage.grid.RegularGrid;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;

/**
 * Immutable implementation of a {@link RegularGrid}.
 * @author Jon
 */
public final class RegularGridImpl extends AbstractRectilinearGrid implements RegularGrid
{
    private final RegularAxis xAxis;
    private final RegularAxis yAxis;

    public RegularGridImpl(RegularAxis xAxis, RegularAxis yAxis,
            CoordinateReferenceSystem crs)
    {
        super(crs);
        if (xAxis == null || yAxis == null) {
            throw new NullPointerException("Axes cannot be null");
        }
        this.xAxis = xAxis;
        this.yAxis = yAxis;
    }

    /**
     * Constructs a RegularGrid with a null coordinate reference system
     */
    public RegularGridImpl(RegularAxis xAxis, RegularAxis yAxis)
    {
        this(xAxis, yAxis, null);
    }

    /**
     * <p>Constructs a RegularGrid from the given geographic bounding box, with the given
     * width and height.  Note that the bounding box represents the edges of the
     * grid, whereas grid coordinates represent the centre of the grid points.</p>
     *
     * <p>The coordinate reference system of the returned grid will be
     * WGS84 longitude-latitude.</p>
     *
     * @param bbox The bounding box of the grid
     * @param width the number of grid points in the x direction
     * @param height the number of grid points in the y direction
     */
    public RegularGridImpl(GeographicBoundingBox bbox, int width, int height)
    {
        this(
            bbox.getWestBoundLongitude(),
            bbox.getSouthBoundLatitude(),
            bbox.getEastBoundLongitude(),
            bbox.getNorthBoundLatitude(),
            DefaultGeographicCRS.WGS84,
            width,
            height
        );
    }

    /**
     * <p>Constructs a RegularGrid from the given bounding box, with the given
     * width and height.  Note that the bounding box represents the edges of the
     * grid, whereas grid coordinates represent the centre of the grid points.</p>
     *
     * <p>The coordinate reference system of the returned grid will be taken
     * from the CRS of the bounding box.  If this is null, the names of the
     * axes will be set to "Unknown X axis" and "Unknown Y axis".</p>
     *
     * @param bbox The bounding box of the grid
     * @param width the number of grid points in the x direction
     * @param height the number of grid points in the y direction
     */
    public RegularGridImpl(BoundingBox bbox, int width, int height)
    {
        this(
            bbox.getMinX(),
            bbox.getMinY(),
            bbox.getMaxX(),
            bbox.getMaxY(),
            bbox.getCoordinateReferenceSystem(),
            width,
            height
        );
    }

    /**
     * @param bbox [minx, miny, maxx, maxy]
     * @param crs
     * @param width
     * @param height
     */
    public RegularGridImpl(double[] bbox, CoordinateReferenceSystem crs, int width, int height)
    {
        this(bbox[0], bbox[1], bbox[2], bbox[3], crs, width, height);
    }

    public RegularGridImpl(double minx, double miny, double maxx, double maxy,
            CoordinateReferenceSystem crs, int width, int height)
    {
        super(crs);
        if (maxx < minx || maxy < miny)
        {
            throw new IllegalArgumentException("Invalid bounding box");
        }

        double xSpacing = (maxx - minx) / width;
        double ySpacing = (maxy - miny) / height;

        // The axis values represent the centres of the grid points
        double firstXAxisValue = minx + (0.5 * xSpacing);
        double firstYAxisValue = miny + (0.5 * ySpacing);

        // TODO: identify whether the axis is longitude
        // Can we use axis.rangemeaning == WRAPS for this?  Do we also have
        // to check that the units of the axis are correct (degrees rather than
        // radians) and that the axis is really longitude?
        boolean isLongitude = false;

        if (crs == null)
        {
            this.xAxis = new RegularAxisImpl(
                "Unknown X axis",
                firstXAxisValue,
                xSpacing,
                width,
                isLongitude
            );
            this.yAxis = new RegularAxisImpl(
                "Unknown Y axis",
                firstYAxisValue,
                ySpacing,
                height,
                false // y axis is very unlikely to be longitude
            );
        }
        else
        {
            CoordinateSystem cs = crs.getCoordinateSystem();
            this.xAxis = new RegularAxisImpl(
                cs.getAxis(0),
                firstXAxisValue,
                xSpacing,
                width,
                isLongitude
            );
            this.yAxis = new RegularAxisImpl(
                cs.getAxis(1),
                firstYAxisValue,
                ySpacing,
                height,
                false // y axis is very unlikely to be longitude
            );
        }
    }

    @Override
    public RegularAxis getAxis(int index) {
        if (index == 0) return this.xAxis;
        if (index == 1) return this.yAxis;
        throw new IndexOutOfBoundsException();
    }

    @Override
    public RegularAxis getXAxis() { return this.xAxis; }

    @Override
    public RegularAxis getYAxis() { return this.yAxis; }
}
