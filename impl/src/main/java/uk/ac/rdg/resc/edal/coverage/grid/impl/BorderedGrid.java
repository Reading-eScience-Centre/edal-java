package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;

public class BorderedGrid extends RegularGridImpl {

    private int extraPixels;

    public BorderedGrid(BoundingBox bbox, int width, int height) {
        this(bbox, width, height, 16);
    }
    
    public BorderedGrid(BoundingBox bbox, int width, int height, int extraPixels) {
        super(getLargeBoundingBox(bbox, width, height, extraPixels), width+2*extraPixels, height+2*extraPixels);
        this.extraPixels = extraPixels;
    }
    
    public static BoundingBox getLargeBoundingBox(BoundingBox bbox, int width, int height, int extraPixels){
        double xGrowth = ((double)extraPixels)/width;
        double yGrowth = ((double)extraPixels)/height;
        double xExtra = bbox.getWidth()*xGrowth;
        double yExtra = bbox.getHeight()*yGrowth;
        BoundingBox bboxBordered = new BoundingBoxImpl(new double[] {
                bbox.getMinX() - xExtra, bbox.getMinY() - yExtra, bbox.getMaxX() + xExtra,
                bbox.getMaxY() + yExtra }, bbox.getCoordinateReferenceSystem());
        return bboxBordered;
    }
    
    @Override
    protected GridCell2D findContainingCell(double x, double y) {
        GridCell2D cell = super.findContainingCell(x, y);
        if(cell == null)
            return null;
        int xIndex = cell.getGridCoordinates().getXIndex() - extraPixels;
        int yIndex = cell.getGridCoordinates().getYIndex() - extraPixels;
        return new GridCell2DImpl(new GridCoordinates2DImpl(xIndex, yIndex), cell.getCentre(),
                cell.getFootprint(), cell.getGrid());
    }
}
