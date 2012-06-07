package uk.ac.rdg.resc.edal.coverage.grid.impl;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridExtent;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.geometry.Polygon;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.AbstractBigList;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * Abstract superclass that partially implements a two-dimensional
 * {@link HorizontalGrid}.
 * 
 * @author Jon
 */
public abstract class AbstractHorizontalGrid extends AbstractGrid implements HorizontalGrid
{
    private final CoordinateReferenceSystem crs;

    protected AbstractHorizontalGrid(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    @Override
    public final CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
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
    
}
