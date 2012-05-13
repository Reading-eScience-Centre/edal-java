package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.List;

import uk.ac.rdg.resc.edal.coverage.grid.Grid;
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;

public class FloatListGridValuesMatrix extends InMemoryGridValuesMatrix<Float> {

    private final List<Float> values;
    private final Grid grid;
    
    // TODO: pass in grid axes, or just their sizes and names?
    public FloatListGridValuesMatrix(Grid grid, List<Float> values) {
        super(Float.class);
        this.grid = grid;
        this.values = values;
    }

    @Override
    public Float readPoint(int i, int j) {
        GridCoordinates2D start = getGridExtent().getLow();
        int index = (i-start.getXIndex()) + (j-start.getYIndex()) * getXAxis().size();
        return values.get(index);
    }

    @Override
    public GridAxis getXAxis() {
        return this.grid.getXAxis();
    }

    @Override
    public GridAxis getYAxis() {
        return this.grid.getYAxis();
    }

}
