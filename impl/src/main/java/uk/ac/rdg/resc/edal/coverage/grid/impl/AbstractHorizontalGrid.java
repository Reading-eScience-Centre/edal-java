package uk.ac.rdg.resc.edal.coverage.grid.impl;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridExtent;
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
            return this.transformCoordinatesNoBoundsCheck(xIndex, yIndex);
        }
        return null;
    }

    /**
     * Transforms grid coordinates into a HorizontalPosition without first
     * checking that the grid coordinates are valid for this grid. Use only when
     * you know in advance that the coordinates are valid.
     */
    protected abstract HorizontalPosition transformCoordinatesNoBoundsCheck(int i, int j);
    
}
