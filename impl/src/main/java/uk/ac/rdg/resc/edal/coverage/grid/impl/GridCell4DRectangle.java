package uk.ac.rdg.resc.edal.coverage.grid.impl;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell4D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.geometry.Polygon;
import uk.ac.rdg.resc.edal.position.CalendarSystem;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

public final class GridCell4DRectangle implements GridCell4D {
    
    private final GridSeriesDomain parentGrid;
    private final GridCell2D horizGridCell;
    
    private final Extent<TimePosition> tExtent;
    private final int tIndex;
    private final Extent<VerticalPosition> vExtent;
    private final int vIndex;

    public GridCell4DRectangle(GridSeriesDomain parentGrid, GridCell2D horizGridCell, Extent<TimePosition> tExtent,
            int tIndex, Extent<VerticalPosition> vExtent, int vIndex) {
        this.parentGrid = parentGrid;
        this.horizGridCell = horizGridCell;
        this.tExtent = tExtent;
        this.tIndex = tIndex;
        this.vExtent = vExtent;
        this.vIndex = vIndex;
    }

    @Override
    public boolean contains(GeoPosition position) {
        return (horizGridCell.contains(position.getHorizontalPosition()) && 
                    tExtent.contains(position.getTimePosition()) &&
                    vExtent.contains(position.getVerticalPosition()));
    }

    @Override
    public HorizontalPosition getCentre() {
        return horizGridCell.getCentre();
    }

    @Override
    public Polygon getFootprint() {
        return horizGridCell.getFootprint();
    }

    @Override
    public GridSeriesDomain getGrid() {
        return parentGrid;
    }

    @Override
    public GridCoordinates2D getHorizontalCoordinates() {
        return horizGridCell.getGridCoordinates();
    }

    @Override
    public CoordinateReferenceSystem getHorizontalCrs() {
        return horizGridCell.getCentre().getCoordinateReferenceSystem();
    }

    @Override
    public Extent<TimePosition> getTimeExtent() {
        return tExtent;
    }

    @Override
    public int getTimeIndex() {
        return tIndex;
    }
    
    @Override
    public CalendarSystem getCalendarSystem() {
        return tExtent.getLow().getCalendarSystem();
    }

    @Override
    public Extent<VerticalPosition> getVerticalExtent() {
        return vExtent;
    }

    @Override
    public int getVerticalIndex() {
        return vIndex;
    }
}
