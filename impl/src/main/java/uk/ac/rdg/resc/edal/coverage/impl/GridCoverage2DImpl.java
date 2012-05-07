package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.ArrayList;
import java.util.List;

import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

public class GridCoverage2DImpl extends AbstractGridCoverage2D {

    private final ScalarMetadata metadata;
    private final String description;
    private final HorizontalGrid grid;
    private final List<T> data;
    
    public GridCoverage2DImpl(ScalarMetadata metadata, String description, HorizontalGrid grid, List<T> data) {
        this.metadata = metadata;
        this.description = description;
        this.grid = grid;
        this.data = data;
    }

    @Override
    public T evaluate(GridCoordinates2D coords) {
        return data.get(gridCoordsToIndex(coords));
    }

    private int gridCoordsToIndex(GridCoordinates2D coords) {
        return coords.getXIndex() + grid.getXAxis().size()*coords.getYIndex();
    }

    @Override
    public List<T> evaluate(List<GridCoordinates2D> coords) {
        List<T> ret = new ArrayList<T>();
        for(GridCoordinates2D coord : coords){
            ret.add(data.get(gridCoordsToIndex(coord)));
        }
        return ret;
    }

    @Override
    public HorizontalGrid getDomain() {
        return grid;
    }

    @Override
    public List<T> getValues() {
        return data;
    }

    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public ScalarMetadata<T> getRangeMetadata() {
        return metadata;
    }
}
