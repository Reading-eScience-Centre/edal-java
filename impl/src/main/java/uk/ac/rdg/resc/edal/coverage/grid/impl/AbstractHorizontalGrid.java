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

import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridExtent;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.geometry.Polygon;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.impl.HorizontalPositionImpl;
import uk.ac.rdg.resc.edal.util.AbstractBigList;
import uk.ac.rdg.resc.edal.util.BigList;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Abstract superclass that partially implements a two-dimensional
 * {@link HorizontalGrid}.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public abstract class AbstractHorizontalGrid extends AbstractGrid implements HorizontalGrid
{

    @Override
    public int getNDim() {
        return 2;
    }

    @Override
    public HorizontalPosition transformCoordinates(GridCoordinates2D coords) {
        return transformCoordinates(coords.getXIndex(), coords.getYIndex());
    }

    @Override
    public HorizontalPosition transformCoordinates(int xIndex, int yIndex) {
        GridExtent gridExtent = this.getGridExtent();
        if (gridExtent.contains(xIndex, yIndex))
        {
            return this.getGridCellCentreNoBoundsCheck(xIndex, yIndex);
        }
        return null;
    }

    
    @Override
    public GridCell2D getGridCell(GridCoordinates2D coords)
    {
        return this.getGridCell(coords.getXIndex(), coords.getYIndex());
    }
    
    @Override
    public GridCell2D getGridCell(int i, int j)
    {
        GridExtent gridExtent = this.getGridExtent();
        if (gridExtent.contains(i, j))
        {
            return this.getGridCellNoBoundsCheck(i, j);
        }
        throw new IndexOutOfBoundsException("No grid cell at " + i + "," + j);
    }

    /**
     * <p>Gets the grid cell at the given grid coordinates without first
     * checking that the grid coordinates are valid for this grid. Use only when
     * you know in advance that the coordinates are valid.</p>
     */
    private GridCell2D getGridCellNoBoundsCheck(int i, int j)
    {
        GridCoordinates2D coords = new GridCoordinates2DImpl(i, j);
        HorizontalPosition centre = this.getGridCellCentreNoBoundsCheck(i, j);
        Polygon footprint = this.getGridCellFootprintNoBoundsCheck(i, j);
        return new GridCell2DImpl(coords, centre, footprint, this);
    }

    /**
     * <p>Transforms grid coordinates into a HorizontalPosition without first
     * checking that the grid coordinates are valid for this grid. Use only when
     * you know in advance that the coordinates are valid.</p>
     */
    protected abstract HorizontalPosition getGridCellCentreNoBoundsCheck(int i, int j);

    /**
     * <p>Gets the footprint of the grid cell at the given grid coordinates without first
     * checking that the grid coordinates are valid for this grid. Use only when
     * you know in advance that the coordinates are valid.</p>
     */
    protected abstract Polygon getGridCellFootprintNoBoundsCheck(int i, int j);
    
    @Override
    public BigList<GridCell2D> getDomainObjects()
    {
        return new AbstractBigList<GridCell2D>()
        {
            @Override
            public GridCell2D get(long index) {
                GridCoordinates2D coords = getCoords(index);
                if (coords == null) {
                    throw new IndexOutOfBoundsException(index + " out of bounds for grid");
                }
                return getGridCellNoBoundsCheck(coords.getXIndex(), coords.getYIndex());
            }

            @Override
            public long sizeAsLong() {
                return AbstractHorizontalGrid.this.size();
            }
        };
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        GridCoordinates2D coords = this.findContainingCell(position);
        return coords != null;
    }

    @Override
    public long findIndexOf(HorizontalPosition pos) {
        GridCoordinates2D coords = this.findContainingCell(pos);
        return coords == null ? -1 : this.getIndex(coords);
    }
    
    @Override
    public final GridCoordinates2D findContainingCell(HorizontalPosition pos) {
        if(pos.getCoordinateReferenceSystem() != getCoordinateReferenceSystem()){
            pos = GISUtils.transformPosition(pos, getCoordinateReferenceSystem());
        }
        return findContainingCell(pos.getX(), pos.getY());
    }
    
    /**
     * <p>Finds the grid cell containing the given position by exhaustive search of
     * all the cells in the grid.  The coordinates of the position must be in
     * this Grid's CRS.</p>
     * <p>This will generally be very inefficient, and so is only recommended
     * for testing purposes.</p>
     * @return the {@link GridCoordinates2D} containing the desired coordinates,
     *         or <code>null</code> if the position is outside of the grid
     */
    protected final GridCoordinates2D findContainingCellExhaustive(double x, double y)
    {
        for (GridCell2D cell : this.getDomainObjects())
        {
            if (cell.getFootprint().contains(new HorizontalPositionImpl(x, y)))
            {
                return cell.getGridCoordinates();
            }
        }
        return null;
    }
    
    /**
     * Finds the grid cell containing the given x,y point, whose coordinates are
     * in the CRS of this grid.  This is called by
     * {@link #findContainingCell(uk.ac.rdg.resc.edal.position.HorizontalPosition)}
     * once the coordinates have been transformed to this CRS.
     * @return the {@link GridCoordinates2D} containing the desired coordinates,
     *         or <code>null</code> if the position is outside of the grid
     */
    protected abstract GridCoordinates2D findContainingCell(double x, double y);

    @Override
    public GridCoordinates2D getCoords(long index) {
        return new GridCoordinates2DImpl(super.getCoords(index));
    }

    @Override
    public GridAxis getAxis(int n) {
        if (n == 0)
            return getXAxis();
        if (n == 1)
            return getYAxis();
        throw new IndexOutOfBoundsException("There are only 2 axes in a horizontal grid");
    }
}
