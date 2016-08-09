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
package uk.ac.rdg.resc.edal.grid;

import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.opengis.metadata.extent.GeographicBoundingBox;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.CurvilinearCoords;
import uk.ac.rdg.resc.edal.util.CurvilinearCoords.Cell;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;

/**
 * Partial implementation of a {@link HorizontalGrid} that is based upon a
 * curvilinear coordinate system ({@literal i.e.} one which is defined by
 * explicitly specifying the latitude and longitude coordinates of each grid
 * point.
 *
 * @author Guy Griffiths
 * @author Jon Blower
 */
public abstract class AbstractCurvilinearGrid extends AbstractTransformedGrid {
    protected final CurvilinearCoords curvCoords;
    private final BoundingBox latLonBbox;
    private Array2D<GridCell2D> domainObjects = null;

    protected AbstractCurvilinearGrid(CurvilinearCoords curvCoords) {
        this.curvCoords = curvCoords;
        this.latLonBbox = curvCoords.getBoundingBox();
    }

    /**
     * Transforms a heading in native grid co-ordinates (degrees clockwise from
     * positive y-direction) into a heading in WGS84 (degrees clockwise from
     * north).
     *
     * @param xComp
     *            The x-component of the heading
     * @param yComp
     *            The y-component of the heading
     * @param lon
     *            The longitude of the given components
     * @param lat
     *            The latitude of the given components
     * @return The transformed heading
     */
    @Override
    public double transformNativeHeadingToWgs84(double xComp, double yComp, double lon, double lat) {
        /*
         * We have a curvilinear grid. There is no analytical way of calculating
         * derivatives, so we use adjacent grid points. This is not very
         * accurate, but it is likely to be sufficient for plotting vector
         * arrows
         */
        GridCoordinates2D posIndex = findIndexOf(new HorizontalPosition(lon, lat,
                GISUtils.defaultGeographicCRS()));
        Array<GridCell2D> curvGridDomain = getDomainObjects();
        int[] shape = curvGridDomain.getShape();
        int gridX = posIndex.getX();
        int gridY = posIndex.getY();

        HorizontalPosition plusXPos;
        HorizontalPosition plusYPos;
        HorizontalPosition centrePos = curvGridDomain.get(gridY, gridX).getCentre();

        /*
         * Calculate the positions directly, or extend the grid if we're at the
         * edge
         */
        if (gridX + 1 < shape[1]) {
            plusXPos = curvGridDomain.get(gridY, gridX + 1).getCentre();
        } else {
            plusXPos = curvGridDomain.get(gridY, gridX - 1).getCentre();
            plusXPos = new HorizontalPosition(2 * centrePos.getX() - plusXPos.getX(), 2
                    * centrePos.getY() - plusXPos.getY(), plusXPos.getCoordinateReferenceSystem());
        }

        if (gridY + 1 < shape[0]) {
            plusYPos = curvGridDomain.get(gridY + 1, gridX).getCentre();
        } else {
            plusYPos = curvGridDomain.get(gridY - 1, gridX).getCentre();
            plusYPos = new HorizontalPosition(2 * centrePos.getX() - plusYPos.getX(), 2
                    * centrePos.getY() - plusYPos.getY(), plusYPos.getCoordinateReferenceSystem());
        }

        /*
         * Calculate the partial derivatives.
         */
        double dXddXs;
        double dYddXs;
        double dXddYs;
        double dYddYs;
        dXddXs = (plusXPos.getX() - centrePos.getX());
        dYddXs = (plusXPos.getY() - centrePos.getY());

        dXddYs = (plusYPos.getX() - centrePos.getX());
        dYddYs = (plusYPos.getY() - centrePos.getY());

        /*
         * Get the new components
         */
        float newX = (float) (dXddXs * xComp + dXddYs * yComp);
        float newY = (float) (dYddXs * xComp + dYddYs * yComp);

        return GISUtils.RAD2DEG * Math.atan2(newX, newY);
    }

    @Override
    public Array2D<GridCell2D> getDomainObjects() {
        if (domainObjects == null) {
            domainObjects = new Array2D<GridCell2D>(curvCoords.getNj(),
                    curvCoords.getNi()) {
                @Override
                public GridCell2D get(int... coords) {
                    int xIndex = coords[1];
                    int yIndex = coords[0];
                    Cell cell = curvCoords.getCell(xIndex, yIndex);
                    return new GridCell2DImpl(new GridCoordinates2D(xIndex, yIndex),
                            cell.getCentre(), cell.getMinimumBoundingRectangle(),
                            AbstractCurvilinearGrid.this);
                }
            };
        }
        return domainObjects;
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        GridCoordinates2D coords = findIndexOf(position);
        return (coords != null);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return latLonBbox;
    }

    @Override
    public GeographicBoundingBox getGeographicBoundingBox() {
        return new DefaultGeographicBoundingBox(latLonBbox.getMinX(), latLonBbox.getMaxX(),
                latLonBbox.getMinY(), latLonBbox.getMaxY());
    }

    @Override
    public long size() {
        return curvCoords.size();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((curvCoords == null) ? 0 : curvCoords.hashCode());
        result = prime * result + ((domainObjects == null) ? 0 : domainObjects.hashCode());
        result = prime * result + ((latLonBbox == null) ? 0 : latLonBbox.hashCode());
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
        AbstractCurvilinearGrid other = (AbstractCurvilinearGrid) obj;
        if (curvCoords == null) {
            if (other.curvCoords != null)
                return false;
        } else if (!curvCoords.equals(other.curvCoords))
            return false;
        if (domainObjects == null) {
            if (other.domainObjects != null)
                return false;
        } else if (!domainObjects.equals(other.domainObjects))
            return false;
        if (latLonBbox == null) {
            if (other.latLonBbox != null)
                return false;
        } else if (!latLonBbox.equals(other.latLonBbox))
            return false;
        return true;
    }
}