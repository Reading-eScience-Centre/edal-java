package uk.ac.rdg.resc.edal.coverage.grid.impl;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.VerticalExtent;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell4D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.geometry.Polygon;
import uk.ac.rdg.resc.edal.position.CalendarSystem;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.position.impl.TimePositionImpl;
import uk.ac.rdg.resc.edal.position.impl.VerticalPositionImpl;

public final class GridCell4DRectangle extends AbstractGridCell<GeoPosition> implements GridCell4D {

    private final GridCell2D horizGridCell;
    @SuppressWarnings("unused")
    private final VerticalPosition verticalCentre;
    private final Extent<VerticalPosition> verticalExtent;
    private final int verticalIndex;
    private final TimePosition timeCentre;
    private final Extent<TimePosition> timeExtent;
    private final int timeIndex;

    public GridCell4DRectangle(GridCell2D horizGridCell, Extent<VerticalPosition> verticalRange,
                               int verticalIndex, Extent<TimePosition> timeRange, int timeIndex) {
        super(horizGridCell.getGridCoordinates());
        this.horizGridCell = horizGridCell;
        this.verticalCentre = new VerticalPositionImpl(0.5*(verticalRange.getHigh().getZ()+verticalRange.getLow().getZ()));
        this.verticalExtent = verticalRange;
        this.verticalIndex = verticalIndex;
        long meantime = (long) (0.5*(timeRange.getLow().getValue() + timeRange.getHigh().getValue()));
        this.timeCentre = new TimePositionImpl(meantime, timeRange.getLow().getCalendarSystem(), timeRange.getLow().getTimeZoneOffset());
        this.timeExtent = timeRange;
        this.timeIndex = timeIndex;
    }

    public GridCell4DRectangle(GridCell2D hCell, Extent<Double> vExtent, VerticalCrs vCrs, int vIndex,
            Extent<TimePosition> tExtent, int tIndex) {
        this(hCell,
             new VerticalExtent(new VerticalPositionImpl(vExtent.getLow(), vCrs),
                               new VerticalPositionImpl(vExtent.getHigh(), vCrs)),
             vIndex,
             tExtent,
             tIndex);
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
        return timeExtent;
    }

    @Override
    public Extent<VerticalPosition> getVerticalExtent() {
        return verticalExtent;
    }

    @Override
    public boolean contains(GeoPosition position) {
        return (horizGridCell.contains(position.getHorizontalPosition())
                && verticalExtent.contains(position.getVerticalPosition())
                && timeExtent.contains(position.getTimePosition()));
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
