package uk.ac.rdg.resc.edal.graphics.style;

import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;

public class PlottingDatum {
    private GridCoordinates2D gridCoords;
    private Number value;

    public PlottingDatum(GridCoordinates2D gridCoords, Number value) {
        super();
        this.gridCoords = gridCoords;
        this.value = value;
    }

    public GridCoordinates2D getGridCoords() {
        return gridCoords;
    }

    public Number getValue() {
        return value;
    }
}
