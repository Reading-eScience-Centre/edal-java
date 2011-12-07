package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates4D;

public class GridCoordinates4DImpl extends GridCoordinates2DImpl implements GridCoordinates4D {

    private int zIndex;
    private int tIndex;
    
    public GridCoordinates4DImpl(int xIndex, int yIndex, int zIndex, int tIndex) {
        super(xIndex, yIndex);
        this.zIndex = zIndex;
        this.tIndex = tIndex;
    }

    @Override
    public int getTIndex() {
        return tIndex;
    }

    @Override
    public int getZIndex() {
        return zIndex;
    }

}
