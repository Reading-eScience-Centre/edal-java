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
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.position.impl.GeoPositionImpl;

public class GridSeriesDomainImpl implements GridSeriesDomain {
    
    private HorizontalGrid hGrid;
    private VerticalAxis vAxis;
    private TimeAxis tAxis;
    
    /**
     * Instantiates a {@link GridSeriesDomainImpl} with all necessary fields
     * @param hGrid The horizontal grid of the domain
     * @param vAxis The vertical axis of the domain
     * @param tAxis The time axis of the domain
     * @param hCrs The horizontal {@link CoordinateReferenceSystem}
     * @param vCrs The vertical {@link CoordinateReferenceSystem}
     * @param calSys The calendar system in use
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
        return vAxis.getVerticalCrs();
    }

    @Override
    public GridCell4D findContainingCell(GeoPosition pos) {
        int vIndex = vAxis.findIndexOf(pos.getVerticalPosition());
        int tIndex = tAxis.findIndexOf(pos.getTimePosition());
        GridCell2D hCell = hGrid.findContainingCell(pos.getHorizontalPosition());
        Extent<VerticalPosition> vExtent = vAxis.getCoordinateBounds(vIndex);
        Extent<TimePosition> tExtent = tAxis.getCoordinateBounds(tIndex);
        
        return new GridCell4DRectangle(hCell, vExtent, vIndex, tExtent, tIndex);
    }

    @Override
    public List<GridCell4D> getDomainObjects() {
        List<GridCell4D> gridCells = new ArrayList<GridCell4D>();
        
        List<GridCell2D> hCells = hGrid.getDomainObjects();
        List<Extent<VerticalPosition>> vExtents = vAxis.getDomainObjects();
        List<Extent<TimePosition>> tExtents = tAxis.getDomainObjects();
        
        for(GridCell2D hCell : hCells){
            for(int i=0; i < vExtents.size(); i++){
                Extent<VerticalPosition> vExtent = vExtents.get(i);
                for(int j=0; j < tExtents.size(); j++){
                    Extent<TimePosition> tExtent = tExtents.get(j);
                    gridCells.add(new GridCell4DRectangle(hCell, vExtent, i, tExtent, j));
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
        return new GeoPositionImpl(hGrid.transformCoordinates(new GridCoordinatesImpl(xIndex, yIndex)),
                                   vAxis.getCoordinateValue(vIndex),
                                   tAxis.getCoordinateValue(tIndex));
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
        return 4;
    }

    @Override
    public GridExtent getGridExtent() {
        GridExtent hExtent = hGrid.getGridExtent();
        Extent<Integer> vExtent = vAxis.getIndexExtent();
        Extent<Integer> tExtent = tAxis.getIndexExtent();
        
        int hDim = hExtent.getDimension();
        if(hDim != 2){
            // TODO deal with case where this is not 2D (it should be?)
        }
        GridCoordinates low = new GridCoordinatesImpl(hExtent.getLow().getCoordinateValue(0),
                                                      hExtent.getLow().getCoordinateValue(1),
                                                      vExtent.getLow(),
                                                      tExtent.getLow());
        GridCoordinates high = new GridCoordinatesImpl(hExtent.getHigh().getCoordinateValue(0),
                                                       hExtent.getHigh().getCoordinateValue(1),
                                                       vExtent.getHigh(),
                                                       tExtent.getHigh());
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
        int vIMin = vAxis.getIndexExtent().getLow();
        // +1 because extents are INCLUSIVE
        int vIMax = vAxis.getIndexExtent().getHigh() + 1;
        int tIMin = tAxis.getIndexExtent().getLow();
        // +1 because extents are INCLUSIVE
        int tIMax = tAxis.getIndexExtent().getHigh() + 1;
        
        for (Integer xIndex = xIMin; xIndex < xIMax; xIndex++) {
            for (Integer yIndex = yIMin; yIndex < yIMax; yIndex++) {
                for (Integer vIndex = vIMin; vIndex < vIMax; vIndex++) {
                    for (Integer tIndex = tIMin; tIndex < tIMax; tIndex++) {
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
        int yRange = hGrid.getGridExtent().getHigh().getCoordinateValue(1) + 1 - hGrid.getGridExtent().getLow().getCoordinateValue(1);
        int vRange = vAxis.getIndexExtent().getHigh() + 1 - vAxis.getIndexExtent().getLow();
        int tRange = tAxis.getIndexExtent().getHigh() + 1 - tAxis.getIndexExtent().getLow();
        return xIndex * yRange * vRange * tRange + yIndex * vRange * tRange + vIndex * tRange + tIndex;
    }

    @Override
    public int size() {
        return hGrid.size()*vAxis.size()*tAxis.size();
    }

    @Override
    public boolean contains(GeoPosition position) {
        return (hGrid.contains(position.getHorizontalPosition()) && 
                vAxis.contains(position.getVerticalPosition()) && 
                tAxis.contains(position.getTimePosition()));
    }

    @Override
    public int findIndexOf(GeoPosition position) {
        // TODO test
        int hIndex = hGrid.findIndexOf(position.getHorizontalPosition());
        int vIndex = vAxis.findIndexOf(position.getVerticalPosition());
        int tIndex = tAxis.findIndexOf(position.getTimePosition());

        int yRange = hGrid.getGridExtent().getHigh().getCoordinateValue(1) + 1 - hGrid.getGridExtent().getLow().getCoordinateValue(1);
        int vRange = vAxis.getIndexExtent().getHigh() + 1 - vAxis.getIndexExtent().getLow();
        int tRange = tAxis.getIndexExtent().getHigh() + 1 - tAxis.getIndexExtent().getLow();
        return hIndex * yRange * vRange * tRange + vIndex * tRange + tIndex;
    }

}
