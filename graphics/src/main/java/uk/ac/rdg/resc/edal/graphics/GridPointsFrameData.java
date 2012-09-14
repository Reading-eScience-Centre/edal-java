package uk.ac.rdg.resc.edal.graphics;

import java.util.Collection;

import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;

public class GridPointsFrameData extends FrameData {
    private Collection<GridCoordinates2D> pointData;

    public GridPointsFrameData(Collection<GridCoordinates2D> pointData) {
        super(PlotStyle.GRIDPOINT);
        this.pointData = pointData;
    }

    public Collection<GridCoordinates2D> getPointData() {
        return pointData;
    }
}
