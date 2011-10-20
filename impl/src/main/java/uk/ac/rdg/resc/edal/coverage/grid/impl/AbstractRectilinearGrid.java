package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridExtent;
import uk.ac.rdg.resc.edal.coverage.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.impl.HorizontalPositionImpl;

/**
 * Abstract superclass that partially implements a two-dimensional
 * {@link RectilinearGrid}.
 * 
 * @author Guy Griffiths
 */
public abstract class AbstractRectilinearGrid extends AbstractHorizontalGrid implements RectilinearGrid {

    protected AbstractRectilinearGrid(CoordinateReferenceSystem crs) {
        super(crs);
    }

    @Override
    public GridExtent getGridExtent() {
        return new GridExtentImpl(
            getXAxis().size() - 1,
            getYAxis().size() - 1
        );
    }

    @Override
    protected final HorizontalPosition transformCoordinatesNoBoundsCheck(int i, int j) {
        double x = getXAxis().getCoordinateValue(i);
        double y = getYAxis().getCoordinateValue(j);
        return new HorizontalPositionImpl(x, y, getCoordinateReferenceSystem());
    }

    @Override
    public GridCoordinates2D findContainingCell(HorizontalPosition pos) {
        int xIndex = getXAxis().findIndexOf(pos.getX());
        int yIndex = getYAxis().findIndexOf(pos.getY());
        return new GridCoordinatesImpl(xIndex, yIndex);
    }

    @Override
    public List<GridCell2D> getDomainObjects() {
        List<GridCell2D> gridCells = new ArrayList<GridCell2D>();
        int xIMin = getXAxis().getIndexExtent().getLow();
        // +1 because extents are INCLUSIVE
        int xIMax = getXAxis().getIndexExtent().getHigh() + 1;
        int yIMin = getYAxis().getIndexExtent().getLow();
        // +1 because extents are INCLUSIVE
        int yIMax = getYAxis().getIndexExtent().getHigh() + 1;
        for (Integer yIndex = yIMin; yIndex < yIMax; yIndex++) {
            for (Integer xIndex = xIMin; xIndex < xIMax; xIndex++) {
                gridCells.add(new GridCell2DRectangle(new GridCoordinatesImpl(xIndex, yIndex),
                                                   getXAxis().getCoordinateBounds(xIndex).getLow(),
                                                   getYAxis().getCoordinateBounds(yIndex).getLow(),
                                                   getXAxis().getCoordinateBounds(xIndex).getHigh(),
                                                   getYAxis().getCoordinateBounds(yIndex).getHigh(),
                                                   getCoordinateReferenceSystem(),this));
            }
        }
        return Collections.unmodifiableList(gridCells);
    }

    @Override
    public long findIndexOf(HorizontalPosition position) {
        int xIndex = getXAxis().findIndexOf(position.getX());
        int yIndex = getYAxis().findIndexOf(position.getY());
        if(xIndex < 0 || yIndex < 0){
            return -1;
        }
        // +1 because extents are INCLUSIVE
        int xRange = getXAxis().getIndexExtent().getHigh() + 1 - getXAxis().getIndexExtent().getLow();
        return xIndex + xRange * yIndex;
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        return (getXAxis().getCoordinateExtent().contains(position.getX()) && 
                    getYAxis().getCoordinateExtent().contains(position.getY()));
    }
    
    @Override
    public BoundingBox getCoordinateExtent() {
        return new BoundingBoxImpl(getXAxis().getCoordinateExtent(), getYAxis().getCoordinateExtent(), getCoordinateReferenceSystem());
    }
    
    @Override
    public GridCell2D getGridCell(GridCoordinates2D coords) {
        return getGridCell(coords.getXIndex(), coords.getYIndex());
    }

    @Override
    public GridCell2D getGridCell(int xIndex, int yIndex) {
        GridCoordinates2D gridCoords = new GridCoordinatesImpl(xIndex,yIndex);
        Extent<Double> xExtents = getXAxis().getCoordinateBounds(xIndex);
        Extent<Double> yExtents = getYAxis().getCoordinateBounds(yIndex);
        return new GridCell2DRectangle(gridCoords, xExtents.getLow(), yExtents.getLow(),
                                                   xExtents.getHigh(), yExtents.getHigh(),
                                                   getCoordinateReferenceSystem(), this);
    }
}
