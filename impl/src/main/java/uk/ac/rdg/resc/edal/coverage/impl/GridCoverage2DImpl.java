package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.ArrayList;
import java.util.List;

import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

public class GridCoverage2DImpl extends AbstractDiscreteSimpleCoverage<HorizontalPosition, GridCell2D, Float> implements
        GridCoverage2D<Float> {

    private final RangeMetadata metadata;
    private final String description;
    private final HorizontalGrid grid;
    private final List<Float> data;
    
    public GridCoverage2DImpl(GridSeriesCoverage<Float> fullCoverage, HorizontalGrid grid, List<Float> data) {
        metadata = fullCoverage.getRangeMetadata(null);
        description = fullCoverage.getDescription();
        this.grid = grid;
        this.data = data;
    }

    @Override
    public Float evaluate(GridCoordinates2D coords) {
        return data.get(gridCoordsToIndex(coords));
    }

    private int gridCoordsToIndex(GridCoordinates2D coords) {
        return coords.getXIndex() + grid.getXAxis().size()*coords.getYIndex();
    }

    @Override
    public List<Float> evaluate(List<GridCoordinates2D> coords) {
        List<Float> ret = new ArrayList<Float>();
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
    public List<Float> getValues() {
        return data;
    }

    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    protected RangeMetadata getRangeMetadata() {
        return metadata;
    }
}
