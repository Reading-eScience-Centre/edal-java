package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.coverage.grid.GridCell;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;

public abstract class AbstractGridCell<P> implements GridCell<P> {

    private final GridCoordinates gridCoords;
    
    protected AbstractGridCell(GridCoordinates gridCoords) {
        this.gridCoords = gridCoords;
    }
    
    @Override
    public GridCoordinates getGridCoordinates() {
        return gridCoords;
    }
}
