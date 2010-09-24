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

package uk.ac.rdg.resc.edal.cdm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.coverage.grid.GridEnvelope;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dt.GridCoordSystem;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.geoloc.ProjectionPoint;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.coverage.grid.impl.AbstractHorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.GridCoordinatesImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.GridEnvelopeImpl;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.HorizontalPosition;
import uk.ac.rdg.resc.edal.geometry.LonLatPosition;
import uk.ac.rdg.resc.edal.geometry.impl.LonLatPositionImpl;
import uk.ac.rdg.resc.edal.util.Utils;

/**
 * A two-dimensional {@link HorizontalGrid} that uses a {@link Projection} to
 * convert from lat-lon coordinates to grid coordinates.
 * @todo repeats much code from AbstractRectilinearGrid - refactor?
 * @author Jon
 */
class ProjectedGrid extends AbstractHorizontalGrid
{
    private final ProjectionImpl proj;
    private final ReferenceableAxis xAxis;
    private final ReferenceableAxis yAxis;
    private final List<String> axisNames;
    private final GridEnvelopeImpl gridEnvelope;
    private final BoundingBox extent;

    /**
     * The GridCoordSystem must have one-dimensional x and y coordinate axes
     * @param coordSys
     */
    public ProjectedGrid(GridCoordSystem coordSys)
    {
        super(DefaultGeographicCRS.WGS84);
        this.proj = coordSys.getProjection();
        this.xAxis = CdmUtils.createReferenceableAxis((CoordinateAxis1D)coordSys.getXHorizAxis());
        this.yAxis = CdmUtils.createReferenceableAxis((CoordinateAxis1D)coordSys.getYHorizAxis());
        this.axisNames = Collections.unmodifiableList(Arrays.asList(
            this.xAxis.getName(), this.yAxis.getName()));
        this.gridEnvelope = new GridEnvelopeImpl(this.xAxis.getSize() - 1, this.yAxis.getSize() - 1);
        this.extent = Utils.getBoundingBox(CdmUtils.getBbox(coordSys.getLatLonBoundingBox()));
    }

    @Override
    protected LonLatPosition transformCoordinatesNoBoundsCheck(int i, int j) {
        double x = this.xAxis.getCoordinateValue(i);
        double y = this.yAxis.getCoordinateValue(j);
        // Translate this point to lon-lat coordinates
        LatLonPoint latLon;
        synchronized(this.proj) {
            latLon = this.proj.projToLatLon(x, y);
        }
        return new LonLatPositionImpl(latLon.getLongitude(), latLon.getLatitude());
    }

    @Override
    public GridCoordinates findNearestGridPoint(HorizontalPosition pos) {
        ProjectionPoint point = this.getProjectionPoint(pos);
        int i = this.xAxis.getNearestCoordinateIndex(point.getX());
        int j = this.yAxis.getNearestCoordinateIndex(point.getY());
        if (i < 0 || j < 0) return null;
        return new GridCoordinatesImpl(i, j);
    }

    @Override
    public GridCoordinates inverseTransformCoordinates(HorizontalPosition pos) {
        ProjectionPoint point = this.getProjectionPoint(pos);
        int i = this.xAxis.getCoordinateIndex(point.getX());
        int j = this.yAxis.getCoordinateIndex(point.getY());
        if (i < 0 || j < 0) return null;
        return new GridCoordinatesImpl(i, j);
    }

    /** Gets the projection point (in the CRS of this grid's axes) that
     * corresponds with the given horizontal position */
    private ProjectionPoint getProjectionPoint(HorizontalPosition pos) {
        // Translate the point into lat-lon coordinates
        pos = Utils.transformPosition(pos, this.getCoordinateReferenceSystem());
        // Now we go from lon-lat to the coordinate system of the axes.
        // ProjectionImpls are not thread-safe.  Thanks to Marcos
        // Hermida of Meteogalicia for pointing this out!
        synchronized(this.proj) {
            return this.proj.latLonToProj(pos.getY(), pos.getX());
        }
    }

    @Override
    public BoundingBox getExtent() { return this.extent; }

    @Override
    public List<String> getAxisNames() { return this.axisNames; }

    @Override
    public GridEnvelope getGridExtent() { return this.gridEnvelope; }

}
