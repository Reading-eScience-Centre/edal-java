package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.coverage.grid.GridCell;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

public abstract class AbstractGridCell implements GridCell<HorizontalPosition> {

    private final GridCoordinates gridCoords;
    
    protected AbstractGridCell(GridCoordinates gridCoords) {
        this.gridCoords = gridCoords;
    }
    
    @Override
    public GridCoordinates getGridCoordinates() {
        return gridCoords;
    }
}
