package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.coverage.grid.GridExtent;
import uk.ac.rdg.resc.edal.coverage.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableAxis;
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
    public ReferenceableAxis<Double> getAxis(int index) {
        if (index == 0)
            return getXAxis();
        if (index == 1)
            return getYAxis();
        throw new IndexOutOfBoundsException();
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
    public GridCell2D findContainingCell(HorizontalPosition pos) {
        int xIndex = getXAxis().findIndexOf(pos.getX());
        int yIndex = getYAxis().findIndexOf(pos.getY());
        return new GridCell2DRectangle(new GridCoordinatesImpl(xIndex, yIndex),
                                    getXAxis().getCoordinateBounds(xIndex).getLow(),
                                    getYAxis().getCoordinateBounds(yIndex).getLow(),
                                    getXAxis().getCoordinateBounds(xIndex).getHigh(),
                                    getYAxis().getCoordinateBounds(yIndex).getHigh(),
                                    getCoordinateReferenceSystem());
    }

    @Override
    public List<ReferenceableAxis<Double>> getAxes() {
        List<ReferenceableAxis<Double>> axes = new ArrayList<ReferenceableAxis<Double>>();
        axes.add(getXAxis());
        axes.add(getYAxis());
        return Collections.unmodifiableList(axes);
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
        for (Integer xIndex = xIMin; xIndex < xIMax; xIndex++) {
            for (Integer yIndex = yIMin; yIndex < yIMax; yIndex++) {
                gridCells.add(new GridCell2DRectangle(new GridCoordinatesImpl(xIndex, yIndex),
                                                   getXAxis().getCoordinateBounds(xIndex).getLow(),
                                                   getYAxis().getCoordinateBounds(yIndex).getLow(),
                                                   getXAxis().getCoordinateBounds(xIndex).getHigh(),
                                                   getYAxis().getCoordinateBounds(yIndex).getHigh(),
                                                   getCoordinateReferenceSystem()));
            }
        }
        return Collections.unmodifiableList(gridCells);
    }

    @Override
    public int findIndexOf(HorizontalPosition position) {
        int xIndex = getXAxis().findIndexOf(position.getX());
        int yIndex = getYAxis().findIndexOf(position.getY());
        // +1 because extents are INCLUSIVE
        int yRange = getYAxis().getIndexExtent().getHigh() + 1 - getYAxis().getIndexExtent().getLow();
        return xIndex * yRange + yIndex;
    }

    @Override
    public List<GridCoordinates> getGridPoints() {
        List<GridCoordinates> gridCoords = new ArrayList<GridCoordinates>();
        int xIMin = getXAxis().getIndexExtent().getLow();
        // +1 because extents are INCLUSIVE
        int xIMax = getXAxis().getIndexExtent().getHigh() + 1;
        int yIMin = getYAxis().getIndexExtent().getLow();
        // +1 because extents are INCLUSIVE
        int yIMax = getYAxis().getIndexExtent().getHigh() + 1;
        for (Integer xIndex = xIMin; xIndex < xIMax; xIndex++) {
            for (Integer yIndex = yIMin; yIndex < yIMax; yIndex++) {
                gridCoords.add(new GridCoordinatesImpl(xIndex, yIndex));
            }
        }
        return Collections.unmodifiableList(gridCoords);
    }

    @Override
    public int getOffset(GridCoordinates coords) {
        int xIndex = coords.getCoordinateValue(0);
        int yIndex = coords.getCoordinateValue(1);
        // +1 because extents are INCLUSIVE
        int yRange = getYAxis().getIndexExtent().getHigh() + 1 - getYAxis().getIndexExtent().getLow();
        return xIndex * yRange + yIndex;
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        return (getXAxis().contains(position.getX()) && getYAxis().contains(position.getY()));
    }
    
    @Override
    public BoundingBox getCoordinateExtent() {
        return new BoundingBoxImpl(getXAxis().getCoordinateExtent(), getYAxis().getCoordinateExtent());
    }
}
