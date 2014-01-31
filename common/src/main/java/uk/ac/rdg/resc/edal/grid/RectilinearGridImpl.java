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

package uk.ac.rdg.resc.edal.grid;

import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.AbstractImmutableArray;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;

/**
 * Immutable implementation of a {@link RectilinearGrid} using {@link Double}s.
 * 
 * @author Guy Griffiths
 */
public class RectilinearGridImpl extends AbstractHorizontalGrid implements RectilinearGrid {
    protected ReferenceableAxis<Double> xAxis;
    protected ReferenceableAxis<Double> yAxis;
    private Array<GridCell2D> domainObjects = null;

    /**
     * Single-argument constructor for subclasses who would rather create their
     * axes from constructor arguments (see {@link RegularGridImpl} for an
     * example)
     */
    protected RectilinearGridImpl(CoordinateReferenceSystem crs) {
        super(crs);
    }

    /**
     * Instantiates a new rectilinear grid from the given axes
     * 
     * @param xAxis
     *            the x-axis
     * @param yAxis
     *            the y-axis
     * @param crs
     *            the {@link CoordinateReferenceSystem}
     */
    public RectilinearGridImpl(ReferenceableAxis<Double> xAxis, ReferenceableAxis<Double> yAxis,
            CoordinateReferenceSystem crs) {
        super(crs);
        this.xAxis = xAxis;
        this.yAxis = yAxis;
    }

    @Override
    public Array<GridCell2D> getDomainObjects() {
        if (domainObjects == null) {
            domainObjects = new AbstractImmutableArray<GridCell2D>(new int[] {
                    yAxis.size(), xAxis.size() }) {
                @Override
                public GridCell2D get(int... coords) {
                    int xIndex = coords[1];
                    int yIndex = coords[0];
                    return new GridCell2DImpl(new GridCoordinates2D(xIndex, yIndex),
                            new HorizontalPosition(xAxis.getCoordinateValue(xIndex), yAxis
                                    .getCoordinateValue(yIndex), crs), new BoundingBoxImpl(
                                    xAxis.getCoordinateBounds(xIndex),
                                    yAxis.getCoordinateBounds(yIndex), crs),
                            RectilinearGridImpl.this);
                }
            };
        }
        return domainObjects;
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        if (GISUtils.crsMatch(crs, position.getCoordinateReferenceSystem())) {
            return xAxis.contains(position.getX()) && yAxis.contains(position.getY());
        } else {
            HorizontalPosition transformedPos = GISUtils.transformPosition(position, crs);
            return xAxis.contains(transformedPos.getX()) && yAxis.contains(transformedPos.getY());
        }
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBoxImpl(xAxis.getCoordinateExtent().getLow(), yAxis
                .getCoordinateExtent().getLow(), xAxis.getCoordinateExtent().getHigh(), yAxis
                .getCoordinateExtent().getHigh(), crs);
    }

    @Override
    public GeographicBoundingBox getGeographicBoundingBox() {
        return GISUtils.toGeographicBoundingBox(getBoundingBox());
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
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return this.crs;
    }

    @Override
    public long size() {
        return xAxis.size() * yAxis.size();
    }

    @Override
    public GridCoordinates2D findIndexOf(HorizontalPosition position) {
        int x = xAxis.findIndexOf(position.getX());
        int y = yAxis.findIndexOf(position.getY());
        if (x >= 0 && y >= 0) {
            return new GridCoordinates2D(x, y);
        } else {
            return null;
        }
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
        RectilinearGridImpl other = (RectilinearGridImpl) obj;
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

    @Override
    public int getXSize() {
        return xAxis.size();
    }

    @Override
    public int getYSize() {
        return yAxis.size();
    }

}
