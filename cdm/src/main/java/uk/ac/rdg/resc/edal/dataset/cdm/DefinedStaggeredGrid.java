/*******************************************************************************
 * Copyright (c) 2016 The University of Reading
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

package uk.ac.rdg.resc.edal.dataset.cdm;

import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;

/**
 * A {@link StaggeredHorizontalGrid} which tracks the original grid and the x/y
 * paddings used, but defers to an explictly-defined grid for referencing
 *
 * @author Guy Griffiths
 */
public class DefinedStaggeredGrid implements StaggeredHorizontalGrid {
    private HorizontalGrid originalGrid;
    private HorizontalGrid staggeredGrid;
    private SGridPadding xPadding;
    private SGridPadding yPadding;

    /**
     * @param staggeredGrid
     *            The staggered grid to use
     * @param originalGrid
     *            The original grid which the staggered one is relative to
     * @param xPadding
     *            The padding on the x-axis to transform from the original grid
     *            to the staggered one
     * @param yPadding
     *            The padding on the y-axis to transform from the original grid
     *            to the staggered one
     */
    public DefinedStaggeredGrid(HorizontalGrid staggeredGrid, HorizontalGrid originalGrid,
            SGridPadding xPadding, SGridPadding yPadding) {
        this.staggeredGrid = staggeredGrid;
        this.originalGrid = originalGrid;
        this.xPadding = xPadding;
        this.yPadding = yPadding;
    }

    @Override
    public Array2D<GridCell2D> getDomainObjects() {
        return staggeredGrid.getDomainObjects();
    }

    @Override
    public long size() {
        return staggeredGrid.size();
    }

    @Override
    public int getXSize() {
        return staggeredGrid.getXSize();
    }

    @Override
    public int getYSize() {
        return staggeredGrid.getYSize();
    }

    @Override
    public GridCoordinates2D findIndexOf(HorizontalPosition position) {
        return staggeredGrid.findIndexOf(position);
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        return staggeredGrid.contains(position);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return staggeredGrid.getBoundingBox();
    }

    @Override
    public GeographicBoundingBox getGeographicBoundingBox() {
        return staggeredGrid.getGeographicBoundingBox();
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return staggeredGrid.getCoordinateReferenceSystem();
    }

    @Override
    public HorizontalGrid getOriginalGrid() {
        return originalGrid;
    }

    @Override
    public SGridPadding getXPadding() {
        return xPadding;
    }

    @Override
    public SGridPadding getYPadding() {
        return yPadding;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((originalGrid == null) ? 0 : originalGrid.hashCode());
        result = prime * result + ((staggeredGrid == null) ? 0 : staggeredGrid.hashCode());
        result = prime * result + ((xPadding == null) ? 0 : xPadding.hashCode());
        result = prime * result + ((yPadding == null) ? 0 : yPadding.hashCode());
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
        DefinedStaggeredGrid other = (DefinedStaggeredGrid) obj;
        if (originalGrid == null) {
            if (other.originalGrid != null)
                return false;
        } else if (!originalGrid.equals(other.originalGrid))
            return false;
        if (staggeredGrid == null) {
            if (other.staggeredGrid != null)
                return false;
        } else if (!staggeredGrid.equals(other.staggeredGrid))
            return false;
        if (xPadding != other.xPadding)
            return false;
        if (yPadding != other.yPadding)
            return false;
        return true;
    }
}
