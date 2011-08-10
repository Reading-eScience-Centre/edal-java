package uk.ac.rdg.resc.edal.coverage.grid.impl;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell4D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.geometry.Polygon;
import uk.ac.rdg.resc.edal.position.CalendarSystem;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

public final class GridCell4DRectangle extends AbstractGridCell<GeoPosition> implements GridCell4D {

    private final GridCell2D horizGridCell;
    @SuppressWarnings("unused")
    private final VerticalPosition verticalCentre;
    private final Extent<VerticalPosition> verticalRange;
    private final int verticalIndex;
    private final TimePosition timeCentre;
    private final Extent<TimePosition> timeRange;
    private final int timeIndex;

    public GridCell4DRectangle(GridCell2D horizGridCell, VerticalPosition verticalCentre,
            Extent<VerticalPosition> verticalRange, int verticalIndex, TimePosition timeCentre,
            Extent<TimePosition> timeRange, int timeIndex) {
        super(horizGridCell.getGridCoordinates());
        this.horizGridCell = horizGridCell;
        this.verticalCentre = verticalCentre;
        this.verticalRange = verticalRange;
        this.verticalIndex = verticalIndex;
        this.timeCentre = timeCentre;
        this.timeRange = timeRange;
        this.timeIndex = timeIndex;
    }

    @Override
    public CalendarSystem getCalendarSystem() {
        return timeCentre.getCalendarSystem();
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
    public CoordinateReferenceSystem getHorizontalCrs() {
        return horizGridCell.getHorizontalCrs();
    }

    @Override
    public Extent<TimePosition> getTimeExtent() {
        return timeRange;
    }

    @Override
    public Extent<VerticalPosition> getVerticalExtent() {
        return verticalRange;
    }

    @Override
    public boolean contains(GeoPosition position) {
        return (horizGridCell.contains(position.getHorizontalPosition())
                && verticalRange.contains(position.getVerticalPosition())
                && timeRange.contains(position.getTimePosition()));
    }

    @Override
    public GridCoordinates getGridCoordinates() {
        int[] xy = horizGridCell.getGridCoordinates().getCoordinateValues();
        if (xy.length != 2) {
            throw new UnsupportedOperationException("Horizontal position must consist of 2 axes");
        }
        return new GridCoordinatesImpl(xy[0], xy[1], verticalIndex, timeIndex);
    }

}
