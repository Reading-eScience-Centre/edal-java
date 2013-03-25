package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.ArrayList;
import java.util.List;

import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.RangeMeaning;

import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.RegularAxis;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.Polygon;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * A grid which has a border which is of a fixed size in pixels. Specifically
 * this adds a gutter with units the same as the width and height. This is for
 * use in glyph-type plots where the glyphs may be outside the box, but still
 * need to be plotted.
 * 
 * Everything is in terms of the original specifying {@link BoundingBox}, but
 * negative co-ordinates and positions may be returned.
 * 
 * @author Guy G
 * 
 */
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
    
    /**
     * Returns all {@link GridCell2D}s which contain the supplied position (i.e.
     * for cases where the x-axis wraps and the range includes multiple values)
     * 
     * @return
     */
    public final List<GridCell2D> findAllContainingCells(HorizontalPosition pos) {
        CoordinateSystemAxis csXAxis = getCoordinateReferenceSystem().getCoordinateSystem().getAxis(0);
        List<GridCell2D> ret = new ArrayList<GridCell2D>();
        GridCell2D mainCell = findContainingCell(pos.getX(), pos.getY());
        ret.add(mainCell);
        if (mainCell != null && csXAxis != null && csXAxis.getRangeMeaning() == RangeMeaning.WRAPAROUND) {
            RegularAxis xAxis = getXAxis();
            for(int i=0; i<xAxis.size(); i++) {
                /*
                 * The main cell will be the lowest value on the x axis, so we
                 * only need to check x values higher than it.
                 */
                for (double xVal = mainCell.getCentre().getX()+360; xVal <= xAxis.getCoordinateExtent().getHigh(); xVal += 360) {
                    if(xAxis.getCoordinateBounds(i).contains(xVal)){
                        int j = mainCell.getGridCoordinates().getYIndex()+extraPixels;
                        GridCoordinates2D coords = new GridCoordinates2DImpl(i-extraPixels, j-extraPixels);
                        HorizontalPosition centre = this.getGridCellCentreNoBoundsCheck(i, j);
                        Polygon footprint = this.getGridCellFootprintNoBoundsCheck(i, j);
                        ret.add(new GridCell2DImpl(coords, centre, footprint, this));
                    }
                }
            }
        }
        return ret;
    }
}
