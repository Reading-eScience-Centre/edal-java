package uk.ac.rdg.resc.edal.coverage.grid.impl;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * Abstract superclass that partially implements a two-dimensional
 * {@link HorizontalGrid}.
 * 
 * @author Jon
 */
public abstract class AbstractHorizontalGrid extends AbstractGrid implements HorizontalGrid {
    private final CoordinateReferenceSystem crs;

    // private final class DomainObjectList extends
    // AbstractList<HorizontalPosition>
    // {
    // private final int iAxisSize =
    // AbstractHorizontalGrid.this.getGridExtent().getSpan(0);
    // private final int jAxisSize =
    // AbstractHorizontalGrid.this.getGridExtent().getSpan(1);
    // private final int size = AbstractHorizontalGrid.this.size();
    //        
    // @Override
    // public HorizontalPosition get(int index) {
    // if (index < 0) {
    // throw new IndexOutOfBoundsException(index + " is out of bounds");
    // }
    // int xi = index % this.iAxisSize;
    // int yi = index / this.iAxisSize;
    // if (yi >= this.jAxisSize) {
    // throw new IndexOutOfBoundsException(index + " is out of bounds");
    // }
    // // We know that the coordinates are valid within the grid so there's
    // // no need to check the bounds again
    // HorizontalPosition pos =
    // AbstractHorizontalGrid.this.transformCoordinatesNoBoundsCheck(xi, yi);
    // if (pos == null) {
    // throw new IndexOutOfBoundsException("Index " + index +
    // " is out of bounds");
    // }
    // return pos;
    // }
    //
    // @Override
    // public int size() {
    // return this.size;
    // }
    // };

    protected AbstractHorizontalGrid(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    @Override
    public final CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return this.crs;
    }

    @Override
    public HorizontalPosition transformCoordinates(GridCoordinates coords) {
        if (coords.getDimension() != 2) {
            throw new IllegalArgumentException("GridCoordinates must be 2D");
        }
        return transformCoordinates(coords.getCoordinateValue(0), coords.getCoordinateValue(1));
    }

    private HorizontalPosition transformCoordinates(int i, int j) {
        GridExtentImpl gridEnv = GridExtentImpl.convert(this.getGridExtent());
        if (!gridEnv.contains(i, j))
            return null;
        return this.transformCoordinatesNoBoundsCheck(i, j);
    }

    /**
     * Transforms grid coordinates into a HorizontalPosition without first
     * checking that the grid coordinates are valid for this grid. Use only when
     * you know in advance that the coordinates are valid.
     */
    protected abstract HorizontalPosition transformCoordinatesNoBoundsCheck(int i, int j);
}
