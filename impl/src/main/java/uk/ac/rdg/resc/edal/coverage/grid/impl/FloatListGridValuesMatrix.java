package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.List;

import uk.ac.rdg.resc.edal.coverage.grid.Grid;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;

public class FloatListGridValuesMatrix extends InMemoryGridValuesMatrix<Float> {

    private List<Float> values;
    
    public FloatListGridValuesMatrix(Grid grid, List<Float> values) {
        super(grid, Float.class);
        this.values = values;
    }

    @Override
    public Float readPoint(int i, int j) {
        GridCoordinates2D start = getGridExtent().getLow();
        int index = (i-start.getXIndex()) + (j-start.getYIndex()) * getXAxis().size();
        return values.get(index);
    }

}
