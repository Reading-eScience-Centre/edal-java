package uk.ac.rdg.resc.edal.coverage.grid.impl;


import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.geometry.Polygon;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * Simple immutable implementation of a GridCell2D.
 * @author Jon
 */
public final class GridCell2DImpl implements GridCell2D
{

    private final GridCoordinates2D gridCoords;
    private final HorizontalPosition centre;
    private final Polygon footprint;
    private final HorizontalGrid parentGrid;
    
    /**
     * @todo check that CRSs of centre, footprint and parentGrid all match?
     */
    public GridCell2DImpl(GridCoordinates2D gridCoords, HorizontalPosition centre, Polygon footprint,
            HorizontalGrid parentGrid) {
        this.gridCoords = gridCoords;
        this.centre = centre;
        this.footprint = footprint;
        this.parentGrid = parentGrid;
    }
    
    @Override
    public GridCoordinates2D getGridCoordinates() {
        return gridCoords;
    }

    @Override
    public HorizontalPosition getCentre() {
        return centre;
    }

    @Override
    public Polygon getFootprint() {
        return footprint;
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        return footprint.contains(position);
    }

    @Override
    public HorizontalGrid getGrid() {
        return parentGrid;
    }

    @Override
    public String toString() {
        return centre.getX()+","+centre.getY();
    }
}
