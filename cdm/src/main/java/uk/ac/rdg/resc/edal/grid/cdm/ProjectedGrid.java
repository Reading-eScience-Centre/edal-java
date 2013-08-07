/*******************************************************************************
 * Copyright (c) 2011 The University of Reading
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
 *******************************************************************************/
package uk.ac.rdg.resc.edal.grid.cdm;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dt.GridCoordSystem;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.geoloc.projection.RotatedPole;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.cdm.CdmUtils;

/**
 * A two-dimensional {@link HorizontalGrid} that uses a {@link Projection} to
 * convert from lat-lon coordinates to grid coordinates.
 * 
 * @todo Relax restriction that external CRS must be lat-lon. This requires some
 *       translation between CDM Projection objects and GeoAPI
 *       CoordinateReferenceSystem objects.
 * @author Jon Blower
 */
public class ProjectedGrid implements HorizontalGrid {
    private final ProjectionImpl proj;
    private final ReferenceableAxis<Double> xAxis;
    private final ReferenceableAxis<Double> yAxis;
    private final BoundingBox bbox;

    /**
     * The GridCoordSystem must have one-dimensional x and y coordinate axes
     * 
     * @param coordSys
     */
    public ProjectedGrid(GridCoordSystem coordSys) {
        proj = coordSys.getProjection();
        /*
         * If this is a rotated-pole projection then the x axis is longitude and
         * hence wraps at 0/360 degrees.
         */
        boolean xAxisIsLongitude = proj instanceof RotatedPole;
        xAxis = CdmUtils.createReferenceableAxis((CoordinateAxis1D) coordSys.getXHorizAxis(),
                xAxisIsLongitude);
        yAxis = CdmUtils.createReferenceableAxis((CoordinateAxis1D) coordSys.getYHorizAxis());
        bbox = new BoundingBoxImpl(coordSys.getLatLonBoundingBox().getLonMin(), coordSys
                .getLatLonBoundingBox().getLatMin(), coordSys.getLatLonBoundingBox().getLonMax(),
                coordSys.getLatLonBoundingBox().getLatMax(), DefaultGeographicCRS.WGS84);
    }

    @Override
    public ReferenceableAxis<Double> getXAxis() {
        return xAxis;
    }

    @Override
    public ReferenceableAxis<Double> getYAxis() {
        return yAxis;
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        if (GISUtils.crsMatch(getCoordinateReferenceSystem(),
                position.getCoordinateReferenceSystem())) {
            return xAxis.getCoordinateExtent().contains(position.getX())
                    && yAxis.getCoordinateExtent().contains(position.getY());
        } else {
            HorizontalPosition transformedPosition = GISUtils.transformPosition(position,
                    getCoordinateReferenceSystem());
            return xAxis.getCoordinateExtent().contains(transformedPosition.getX())
                    && yAxis.getCoordinateExtent().contains(transformedPosition.getY());
        }
    }

    /**
     * Always returns {@link DefaultGeographicCRS#WGS84}.
     */
    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return DefaultGeographicCRS.WGS84;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return bbox;
    }

    @Override
    public long size() {
        return xAxis.size() * yAxis.size();
    }

    @Override
    public Array<GridCell2D> getDomainObjects() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int[] findIndexOf(HorizontalPosition position) {
        if (GISUtils.crsMatch(getCoordinateReferenceSystem(),
                position.getCoordinateReferenceSystem())) {
            return new int[] { xAxis.findIndexOf(position.getX()),
                    yAxis.findIndexOf(position.getY()) };
        } else {
            HorizontalPosition transformedPosition = GISUtils.transformPosition(position,
                    getCoordinateReferenceSystem());
            return new int[] { xAxis.findIndexOf(transformedPosition.getX()),
                    yAxis.findIndexOf(transformedPosition.getY()) };
        }
    }
}
