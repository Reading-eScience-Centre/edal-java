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
package uk.ac.rdg.resc.edal.cdm.coverage.grid;

import java.util.List;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;

import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dt.GridCoordSystem;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.geoloc.ProjectionPoint;
import ucar.unidata.geoloc.projection.RotatedPole;
import uk.ac.rdg.resc.edal.cdm.util.CdmUtils;
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.coverage.grid.impl.AbstractHorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.GridCoordinatesImpl;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.LonLatPosition;
import uk.ac.rdg.resc.edal.position.impl.LonLatPositionImpl;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * A two-dimensional {@link HorizontalGrid} that uses a {@link Projection} to
 * convert from lat-lon coordinates to grid coordinates.
 * 
 * @todo repeats much code from AbstractRectilinearGrid - refactor?
 * @author Jon Blower
 */
public class ProjectedGrid extends AbstractHorizontalGrid {
    private final ProjectionImpl proj;
    private final ReferenceableAxis<Double> xAxis;
    private final ReferenceableAxis<Double> yAxis;
    private final BoundingBox extent;

    /**
     * The GridCoordSystem must have one-dimensional x and y coordinate axes
     * 
     * @param coordSys
     */
    public ProjectedGrid(GridCoordSystem coordSys) {
        super(DefaultGeographicCRS.WGS84);
        this.proj = coordSys.getProjection();
        // If this is a rotated-pole projection then the x axis is longitude and
        // hence wraps at 0/360 degrees.
        boolean xAxisIsLongitude = this.proj instanceof RotatedPole;
        this.xAxis = CdmUtils.createReferenceableAxis((CoordinateAxis1D) coordSys.getXHorizAxis(),
                xAxisIsLongitude);
        this.yAxis = CdmUtils.createReferenceableAxis((CoordinateAxis1D) coordSys.getYHorizAxis());
        double[] bbox = new double[4];
        bbox[0] = coordSys.getLatLonBoundingBox().getLonMin();
        bbox[1] = coordSys.getLatLonBoundingBox().getLatMin();
        bbox[2] = coordSys.getLatLonBoundingBox().getLonMax();
        bbox[3] = coordSys.getLatLonBoundingBox().getLatMax();
        this.extent = new BoundingBoxImpl(bbox);
    }

    @Override
    protected LonLatPosition transformCoordinatesNoBoundsCheck(int i, int j) {
        double x = this.xAxis.getCoordinateValue(i);
        double y = this.yAxis.getCoordinateValue(j);
        // Translate this point to lon-lat coordinates
        LatLonPoint latLon;
        synchronized (this.proj) {
            latLon = this.proj.projToLatLon(x, y);
        }
        return new LonLatPositionImpl(latLon.getLongitude(), latLon.getLatitude());
    }

    @Override
    public GridCoordinates2D findContainingCell(HorizontalPosition pos) {
        ProjectionPoint point = this.getProjectionPoint(pos);
        int i = this.xAxis.findIndexOf(point.getX());
        int j = this.yAxis.findIndexOf(point.getY());
        if (i < 0 || j < 0)
            return null;
        return new GridCoordinatesImpl(i, j);
    }

    /**
     * Gets the projection point (in the CRS of this grid's axes) that
     * corresponds with the given horizontal position
     */
    private ProjectionPoint getProjectionPoint(HorizontalPosition pos) {
        // Translate the point into lat-lon coordinates
        pos = GISUtils.transformPosition(pos, this.getCoordinateReferenceSystem());
        // Now we go from lon-lat to the coordinate system of the axes.
        // ProjectionImpls are not thread-safe. Thanks to Marcos
        // Hermida of Meteogalicia for pointing this out!
        synchronized (this.proj) {
            return this.proj.latLonToProj(pos.getY(), pos.getX());
        }
    }

    @Override
    public GridAxis getXAxis() {
        return xAxis;
    }

    @Override
    public GridAxis getYAxis() {
        return yAxis;
    }

    @Override
    public List<GridCell2D> getDomainObjects() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BoundingBox getCoordinateExtent() {
        return extent;
    }

    @Override
    public GridCell2D getGridCell(GridCoordinates2D coords) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GridCell2D getGridCell(int xIndex, int yIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        return xAxis.getCoordinateExtent().contains(position.getX())
                && yAxis.getCoordinateExtent().contains(position.getY());
    }

    @Override
    public long findIndexOf(HorizontalPosition pos) {
        GridCoordinates2D gridCoords = findContainingCell(pos);
        return gridCoords.getXIndex() + gridCoords.getYIndex()*xAxis.size();
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ProjectedGrid){
            ProjectedGrid grid = (ProjectedGrid) obj;
            return grid.xAxis.equals(xAxis) && grid.yAxis.equals(yAxis) && super.equals(obj);
        } else {
            return false;
        }
    }
}
