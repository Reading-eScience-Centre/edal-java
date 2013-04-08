package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.Set;

import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.Record;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.position.impl.GeoPositionImpl;

/**
 * This class allows us to treat a {@link GridSeriesCoverage} as a
 * {@link GridCoverage2D} by providing a fixed {@link VerticalPosition} and
 * {@link TimePosition}
 * 
 * @author guy
 * 
 */
public class GridCoverage2DWrappedGridSeriesCoverage extends
        AbstractMultimemberDiscreteGridCoverage<HorizontalPosition, GridCell2D, HorizontalGrid>
        implements GridCoverage2D {

    private GridSeriesCoverage gridSeriesCoverage;
    private VerticalPosition vPos;
    private TimePosition tPos;

    public GridCoverage2DWrappedGridSeriesCoverage(GridSeriesCoverage gridSeriesCoverage,
            VerticalPosition vPos, TimePosition tPos) {
        super(gridSeriesCoverage.getDescription(), gridSeriesCoverage.getDomain()
                .getHorizontalGrid());
        this.gridSeriesCoverage = gridSeriesCoverage;
        this.vPos = vPos;
        this.tPos = tPos;
    }

    @Override
    public Record evaluate(HorizontalPosition pos, Set<String> memberNames) {
        return gridSeriesCoverage.evaluate(new GeoPositionImpl(pos, vPos, tPos), memberNames);
    }

    @Override
    public Record evaluate(HorizontalPosition pos) {
        return gridSeriesCoverage.evaluate(new GeoPositionImpl(pos, vPos, tPos));
    }

    @Override
    public Object evaluate(HorizontalPosition pos, String memberName) {
        return gridSeriesCoverage.evaluate(new GeoPositionImpl(pos, vPos, tPos), memberName);
    }

    @Override
    public boolean isDefinedAt(HorizontalPosition val) {
        return gridSeriesCoverage.isDefinedAt(new GeoPositionImpl(val, vPos, tPos));
    }

    @Override
    public GridCoverage2D extractGridCoverage(HorizontalGrid targetGrid, Set<String> memberNames) {
        return gridSeriesCoverage.extractGridCoverage(targetGrid, vPos, tPos, memberNames);
    }
}
