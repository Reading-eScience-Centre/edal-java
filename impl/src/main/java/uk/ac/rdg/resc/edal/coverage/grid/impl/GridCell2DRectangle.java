package uk.ac.rdg.resc.edal.coverage.grid.impl;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.geometry.Polygon;
import uk.ac.rdg.resc.edal.geometry.impl.Rectangle;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.impl.HorizontalPositionImpl;

public class GridCell2DRectangle implements GridCell2D {

    private final HorizontalPosition centre;
    private final Rectangle rectangularRegion;
    private final HorizontalGrid parentGrid;
    private final GridCoordinates2D gridCoords;
    
    public GridCell2DRectangle(GridCoordinates2D gridCoords, HorizontalPosition centre, Double width, Double height,
            CoordinateReferenceSystem crs, HorizontalGrid parentGrid) {
        this.gridCoords = gridCoords;
        this.centre = centre;
        rectangularRegion = new Rectangle(centre.getX() - 0.5 * width, centre.getY() - 0.5 * height,
                                  centre.getX() + 0.5 * width, centre.getY() + 0.5 * height, crs);
        this.parentGrid = parentGrid;
    }

    public GridCell2DRectangle(GridCoordinates2D gridCoords, Double minX, Double minY, Double maxX, Double maxY,
            CoordinateReferenceSystem crs, HorizontalGrid parentGrid) {
        this.gridCoords = gridCoords;
        this.centre = new HorizontalPositionImpl(0.5 * (minX + maxX), 0.5 * (minY + maxY), crs);
        rectangularRegion = new Rectangle(minX, minY, maxX, maxY, crs);
        this.parentGrid = parentGrid;
    }

    @Override
    public GridCoordinates2D getGridCoordinates() {
        return gridCoords;
    }

    @Override
    public HorizontalPosition getCentre() {
        return centre;
    }

    @Override
    public Polygon getFootprint() {
        return rectangularRegion;
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        return rectangularRegion.contains(position);
    }

    @Override
    public HorizontalGrid getGrid() {
        return parentGrid;
    }

}
