package uk.ac.rdg.resc.edal.coverage.grid.impl;


import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridExtent;
import uk.ac.rdg.resc.edal.coverage.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.impl.HorizontalPositionImpl;
import uk.ac.rdg.resc.edal.util.GISUtils;

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
    protected final HorizontalPosition getGridCellCentreNoBoundsCheck(int i, int j) {
        double x = getXAxis().getCoordinateValue(i);
        double y = getYAxis().getCoordinateValue(j);
        return new HorizontalPositionImpl(x, y, getCoordinateReferenceSystem());
    }

    @Override
    public GridCoordinates2D findContainingCell(HorizontalPosition pos) {
        if(pos.getCoordinateReferenceSystem() != getCoordinateReferenceSystem()){
            pos = GISUtils.transformPosition(pos, getCoordinateReferenceSystem());
        }
        int xIndex = getXAxis().findIndexOf(pos.getX());
        int yIndex = getYAxis().findIndexOf(pos.getY());
        if(xIndex < 0 || yIndex < 0){
            return null;
        }
        return new GridCoordinates2DImpl(xIndex, yIndex);
    }
    
    @Override
    public BoundingBox getCoordinateExtent() {
        return new BoundingBoxImpl(getXAxis().getCoordinateExtent(),
            getYAxis().getCoordinateExtent(), getCoordinateReferenceSystem());
    }

    @Override
    protected BoundingBox getGridCellFootprintNoBoundsCheck(int xIndex, int yIndex) {
        Extent<Double> xExtents = getXAxis().getCoordinateBounds(xIndex);
        Extent<Double> yExtents = getYAxis().getCoordinateBounds(yIndex);
        return new BoundingBoxImpl(xExtents, yExtents);
    }
}
