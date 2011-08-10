package uk.ac.rdg.resc.edal.coverage.grid.impl;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.geometry.Polygon;
import uk.ac.rdg.resc.edal.geometry.impl.Rectangle;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.impl.HorizontalPositionImpl;

public class GridCell2DRectangle extends AbstractGridCell<HorizontalPosition> implements GridCell2D {

    private final HorizontalPosition centre;
    private final Rectangle rectangularRegion;
    private final CoordinateReferenceSystem crs;

    public GridCell2DRectangle(GridCoordinates gridCoords, HorizontalPosition centre, Double width, Double height,
            CoordinateReferenceSystem crs) {
        super(gridCoords);
        this.centre = centre;
        rectangularRegion = new Rectangle(centre.getX() - 0.5 * width, centre.getY() - 0.5 * height,
                                  centre.getX() + 0.5 * width, centre.getY() + 0.5 * height, crs);
        this.crs = crs;
    }

    public GridCell2DRectangle(GridCoordinates gridCoords, Double minX, Double minY, Double maxX, Double maxY,
            CoordinateReferenceSystem crs) {
        super(gridCoords);
        this.centre = new HorizontalPositionImpl(0.5 * (minX + maxX), 0.5 * (minY + maxY));
        rectangularRegion = new Rectangle(minX, minY, maxX, maxY, crs);
        this.crs = crs;
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
    public CoordinateReferenceSystem getHorizontalCrs() {
        return crs;
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        return rectangularRegion.contains(position);
    }

}
