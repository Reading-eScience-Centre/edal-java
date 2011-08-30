package uk.ac.rdg.resc.edal.coverage.domain.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell4D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.coverage.grid.GridExtent;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.coverage.grid.impl.GridCell4DRectangle;
import uk.ac.rdg.resc.edal.coverage.grid.impl.GridCoordinatesImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.GridExtentImpl;
import uk.ac.rdg.resc.edal.position.CalendarSystem;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.impl.GeoPositionImpl;

/**
 * Implementation of {@link GridSeriesDomain} which represents a domain on a 4D
 * grid
 * 
 * The horizontal axes must both be present, but either of the
 * {@link VerticalAxis} or the {@link TimeAxis} may be <code>null</code>
 * 
 * @author Guy Griffiths
 * 
 */
public class GridSeriesDomainImpl implements GridSeriesDomain {

    private HorizontalGrid hGrid;
    private VerticalAxis vAxis;
    private TimeAxis tAxis;

    /**
     * Instantiates a {@link GridSeriesDomainImpl} with all necessary fields
     * 
     * @param hGrid
     *            The horizontal grid of the domain
     * @param vAxis
     *            The vertical axis of the domain
     * @param tAxis
     *            The time axis of the domain
     * @param hCrs
     *            The horizontal {@link CoordinateReferenceSystem}
     * @param vCrs
     *            The vertical {@link CoordinateReferenceSystem}
     * @param calSys
     *            The calendar system in use
     */
    public GridSeriesDomainImpl(HorizontalGrid hGrid, VerticalAxis vAxis, TimeAxis tAxis) {
        super();
        this.hGrid = hGrid;
        this.vAxis = vAxis;
        this.tAxis = tAxis;
    }

    @Override
    public CalendarSystem getCalendarSystem() {
        return tAxis.getCalendarSystem();
    }

    @Override
    public CoordinateReferenceSystem getHorizontalCrs() {
        return hGrid.getCoordinateReferenceSystem();
    }

    @Override
    public HorizontalGrid getHorizontalGrid() {
        return hGrid;
    }

    @Override
    public TimeAxis getTimeAxis() {
        return tAxis;
    }

    @Override
    public VerticalAxis getVerticalAxis() {
        return vAxis;
    }

    @Override
    public VerticalCrs getVerticalCrs() {
        if (vAxis != null)
            return vAxis.getVerticalCrs();
        else
            return null;
    }

    @Override
    public GridCell4D findContainingCell(GeoPosition pos) {
        int vIndex = 0;
        if (vAxis != null) {
            vIndex = vAxis.findIndexOf(pos.getVerticalPosition().getZ());
        }
        int tIndex = 0;
        if (tAxis != null) {
            tAxis.findIndexOf(pos.getTimePosition());
        }
        GridCell2D hCell = hGrid.findContainingCell(pos.getHorizontalPosition());
        Extent<Double> vExtent = vAxis.getCoordinateBounds(vIndex);
        Extent<TimePosition> tExtent = tAxis.getCoordinateBounds(tIndex);

        VerticalCrs vCrs = null;
        if (vAxis != null) {
            vCrs = vAxis.getVerticalCrs();
        }

        return new GridCell4DRectangle(hCell, vExtent, vCrs, vIndex, tExtent, tIndex);
    }

    @Override
    public List<GridCell4D> getDomainObjects() {
        List<GridCell4D> gridCells = new ArrayList<GridCell4D>();

        List<GridCell2D> hCells = hGrid.getDomainObjects();

        VerticalCrs vCrs = null;
        List<Extent<Double>> vExtents;
        if (vAxis != null) {
            vCrs = vAxis.getVerticalCrs();
            vExtents = vAxis.getDomainObjects();
        } else {
            vExtents = Collections.emptyList();
        }
        List<Extent<TimePosition>> tExtents;
        if (tAxis != null) {
            tExtents = tAxis.getDomainObjects();
        } else {
            tExtents = Collections.emptyList();
        }

        Extent<Double> vExtent = null;
        Extent<TimePosition> tExtent = null;
        for (GridCell2D hCell : hCells) {
            for (int i = 0; i < vExtents.size(); i++) {
                vExtent = vExtents.get(i);
                for (int j = 0; j < tExtents.size(); j++) {
                    tExtent = tExtents.get(j);
                    gridCells.add(new GridCell4DRectangle(hCell, vExtent, vCrs, i, tExtent, j));
                }
            }
        }
        return Collections.unmodifiableList(gridCells);
    }

    @Override
    public GeoPosition transformCoordinates(GridCoordinates coords) {
        int xIndex = coords.getCoordinateValue(0);
        int yIndex = coords.getCoordinateValue(1);
        int vIndex = coords.getCoordinateValue(2);
        int tIndex = coords.getCoordinateValue(3);
        Double vPos = null;
        VerticalCrs vCrs = null;
        TimePosition tPos = null;
        if (vAxis != null) {
            vPos = vAxis.getCoordinateValue(vIndex);
            vCrs = vAxis.getVerticalCrs();
        }
        if (tAxis != null) {
            tPos = tAxis.getCoordinateValue(tIndex);
        }
        return new GeoPositionImpl(hGrid.transformCoordinates(new GridCoordinatesImpl(xIndex, yIndex)), vPos, vCrs,
                tPos);
    }

    @Override
    public List<? extends GridAxis> getAxes() {
        List<GridAxis> axes = new ArrayList<GridAxis>();
        axes.addAll(hGrid.getAxes());
        axes.add(vAxis);
        axes.add(tAxis);
        return Collections.unmodifiableList(axes);
    }

    @Override
    public int getDimension() {
        int dim = 2;
        if (vAxis != null)
            dim++;
        if (tAxis != null)
            dim++;
        /*
         * TODO Check whether this should be: 1) The number of non-null
         * dimensions 2) 4 (i.e. the total number of dimensions)
         */
        return dim;
    }

    @Override
    public GridExtent getGridExtent() {
        GridExtent hExtent = hGrid.getGridExtent();
        int vLow = 0;
        int vHigh = 0;
        int tLow = 0;
        int tHigh = 0;
        if (vAxis != null) {
            Extent<Integer> vExtent = vAxis.getIndexExtent();
            vLow = vExtent.getLow();
            vHigh = vExtent.getHigh();
        }
        if (tAxis != null) {
            Extent<Integer> tExtent = tAxis.getIndexExtent();
            tLow = tExtent.getLow();
            tHigh = tExtent.getHigh();
        }

        int hDim = hExtent.getDimension();
        if (hDim != 2) {
            // TODO deal with case where this is not 2D (it should be?)
            throw new IllegalArgumentException("Horizontal grid does not have 2 dimensions");
        }
        GridCoordinates low = new GridCoordinatesImpl(hExtent.getLow().getCoordinateValue(0), hExtent.getLow()
                .getCoordinateValue(1), vLow, tLow);
        GridCoordinates high = new GridCoordinatesImpl(hExtent.getHigh().getCoordinateValue(0), hExtent.getHigh()
                .getCoordinateValue(1), vHigh, tHigh);
        return new GridExtentImpl(low, high);
    }

    @Override
    public List<GridCoordinates> getGridPoints() {
        // TODO write test (with getOffset)
        List<GridCoordinates> gridCoords = new ArrayList<GridCoordinates>();
        int xIMin = hGrid.getGridExtent().getLow().getCoordinateValue(0);
        // +1 because extents are INCLUSIVE
        int xIMax = hGrid.getGridExtent().getHigh().getCoordinateValue(0) + 1;
        int yIMin = hGrid.getGridExtent().getLow().getCoordinateValue(1);
        // +1 because extents are INCLUSIVE
        int yIMax = hGrid.getGridExtent().getHigh().getCoordinateValue(1) + 1;
        
        int vIMin = 0;
        int vIMax = 0;
        if(vAxis != null){
            vIMin = vAxis.getIndexExtent().getLow();
            // +1 because extents are INCLUSIVE
            vIMax = vAxis.getIndexExtent().getHigh() + 1;
        }
        
        int tIMin = 0;
        int tIMax = 0;
        if(tAxis != null){
            tIMin = tAxis.getIndexExtent().getLow();
            // +1 because extents are INCLUSItE
            tIMax = tAxis.getIndexExtent().getHigh() + 1;
        }

        for (Integer tIndex = tIMin; tIndex < tIMax; tIndex++) {
            for (Integer vIndex = vIMin; vIndex < vIMax; vIndex++) {
                for (Integer yIndex = yIMin; yIndex < yIMax; yIndex++) {
                    for (Integer xIndex = xIMin; xIndex < xIMax; xIndex++) {
                        gridCoords.add(new GridCoordinatesImpl(xIndex, yIndex, vIndex, tIndex));
                    }
                }
            }
        }
        return gridCoords;
    }

    @Override
    public int getOffset(GridCoordinates coords) {
        // TODO write test (with getGridPoints)
        int xIndex = coords.getCoordinateValue(0);
        int yIndex = coords.getCoordinateValue(1);
        int vIndex = coords.getCoordinateValue(2);
        int tIndex = coords.getCoordinateValue(3);
        int xRange = hGrid.getGridExtent().getHigh().getCoordinateValue(0) + 1
                - hGrid.getGridExtent().getLow().getCoordinateValue(0);
        int yRange = hGrid.getGridExtent().getHigh().getCoordinateValue(1) + 1
                - hGrid.getGridExtent().getLow().getCoordinateValue(1);
        int vRange = 0;
        if(vAxis != null)
            vRange = vAxis.getIndexExtent().getHigh() + 1 - vAxis.getIndexExtent().getLow();
        return xIndex + yIndex * xRange + vIndex * xRange * yRange + tIndex * xRange * yRange * vRange;
    }

    @Override
    public int size() {
        int vSize = 1;
        if(vAxis != null){
            vSize = vAxis.size();
        }
        int tSize = 1;
        if(tAxis != null){
            tSize = tAxis.size();
        }
        return hGrid.size() * vSize * tSize;
    }

    @Override
    public boolean contains(GeoPosition position) {
        boolean containsH = hGrid.contains(position.getHorizontalPosition());
        boolean containsV = false;
        boolean containsT = false;
        if(vAxis != null){
            containsV = vAxis.contains(position.getVerticalPosition().getZ());
        } else {
            containsV = position.getVerticalPosition() == null;
        }
        if(tAxis != null){
            containsT = tAxis.contains(position.getTimePosition());
        } else {
            containsT = position.getTimePosition() == null;
        }
        
        return (containsH && containsV && containsT);
    }

    @Override
    public int findIndexOf(GeoPosition position) {
        // TODO test
        int hIndex = hGrid.findIndexOf(position.getHorizontalPosition());
        int vIndex = 0;
        int vRange = 1;
        if(vAxis != null){
            vIndex = vAxis.findIndexOf(position.getVerticalPosition().getZ());
            vRange = vAxis.getIndexExtent().getHigh() + 1 - vAxis.getIndexExtent().getLow();
        }
        int tIndex = 0;
        int tRange = 1;
        if(tAxis != null){
            tIndex = tAxis.findIndexOf(position.getTimePosition());
            tRange = tAxis.getIndexExtent().getHigh() + 1 - tAxis.getIndexExtent().getLow();
        }
        int yRange = hGrid.getGridExtent().getHigh().getCoordinateValue(1) + 1
                - hGrid.getGridExtent().getLow().getCoordinateValue(1);
        return hIndex * yRange * vRange * tRange + vIndex * tRange + tIndex;
    }

}
