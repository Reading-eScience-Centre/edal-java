package uk.ac.rdg.resc.edal.coverage.domain.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell4D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates4D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.coverage.grid.impl.GridCell4DRectangle;
import uk.ac.rdg.resc.edal.coverage.grid.impl.GridCoordinates4DImpl;
import uk.ac.rdg.resc.edal.position.CalendarSystem;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.position.impl.VerticalPositionImpl;
import uk.ac.rdg.resc.edal.util.Extents;

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

    private final HorizontalGrid hGrid;
    private final VerticalAxis vAxis;
    private final TimeAxis tAxis;

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
        /*
         * Get the extent of the time axis (if available - empty if not)
         */
        int tIndex = 0;
        Extent<TimePosition> tExtent = Extents.emptyExtent(TimePosition.class);
        if (tAxis != null) {
            tAxis.findIndexOf(pos.getTimePosition());
            tExtent = tAxis.getCoordinateBounds(tIndex);
        }

        /*
         * Get the extent of the vertical axis (if available - empty if not)
         */
        int vIndex = 0;
        VerticalCrs vCrs = null;
        Extent<VerticalPosition> vExtent = Extents.emptyExtent(VerticalPosition.class);
        if (vAxis != null) {
            vIndex = vAxis.findIndexOf(pos.getVerticalPosition().getZ());
            vCrs = vAxis.getVerticalCrs();
            Extent<Double> vExtentDouble = vAxis.getCoordinateBounds(vIndex);
            vExtent = Extents.newExtent((VerticalPosition) new VerticalPositionImpl(vExtentDouble.getLow(), vCrs),
                                        (VerticalPosition) new VerticalPositionImpl(vExtentDouble.getHigh(), vCrs));
        }
        
        GridCoordinates2D hCoords = hGrid.findContainingCell(pos.getHorizontalPosition());
        GridCell2D hCell = hGrid.getGridCell(hCoords);
        
        /*
         * Cast is needed here, otherwise an Extent<VerticalPositionImpl> is returned, which is not what we want
         */

        return new GridCell4DRectangle(this, hCell, tExtent, tIndex, vExtent, vIndex);
    }

    @Override
    public List<GridCell4D> getDomainObjects() {
        List<GridCell4D> gridCells = new ArrayList<GridCell4D>();

        List<GridCell2D> hCells = hGrid.getDomainObjects();

        VerticalCrs vCrs = null;
        int vSize = 0;
        if (vAxis != null) {
            vCrs = vAxis.getVerticalCrs();
            vSize = vAxis.size();
        }
        int tSize = 0;
        if (tAxis != null) {
            tSize = tAxis.size();
        }

        for (GridCell2D hCell : hCells) {
            for (int tIndex = 0; tIndex < tSize; tIndex++) {
                Extent<TimePosition> tExtent = null;
                if(tAxis != null){
                    tExtent = tAxis.getCoordinateBounds(tIndex);
                }
                for (int vIndex = 0; vIndex < vSize; vIndex++) {
                    Extent<VerticalPosition> vExtent = null;
                    if(vAxis != null){
                        Extent<Double> vExtentDouble = vAxis.getCoordinateBounds(vIndex);
                        vExtent = Extents.newExtent((VerticalPosition) new VerticalPositionImpl(vExtentDouble.getLow(), vCrs),
                                                    (VerticalPosition) new VerticalPositionImpl(vExtentDouble.getLow(), vCrs));
                    }
                    gridCells.add(new GridCell4DRectangle(this, hCell, tExtent, tIndex, vExtent, vIndex));
                }
            }
        }
        return Collections.unmodifiableList(gridCells);
    }

    @Override
    public long size() {
        int vSize = 1;
        if(vAxis != null){
            vSize = vAxis.size();
        }
        int tSize = 1;
        if(tAxis != null){
            tSize = tAxis.size();
        }
        return (long) (hGrid.size() * vSize * tSize);
    }

    @Override
    public boolean contains(GeoPosition position) {
        boolean containsH = hGrid.contains(position.getHorizontalPosition());
        boolean containsV = false;
        boolean containsT = false;
        if(vAxis != null){
            containsV = vAxis.getCoordinateExtent().contains(position.getVerticalPosition().getZ());
        } else {
            containsV = position.getVerticalPosition() == null;
        }
        if(tAxis != null){
            containsT = tAxis.getCoordinateExtent().contains(position.getTimePosition());
        } else {
            containsT = position.getTimePosition() == null;
        }
        
        return (containsH && containsV && containsT);
    }

    @Override
    public long findIndexOf(GeoPosition position) {
        long hIndex = hGrid.findIndexOf(position.getHorizontalPosition());
        long hSize = hGrid.getGridExtent().size();
        int vIndex = 0;
        int vSize = 1;
        if(vAxis != null){
            vIndex = vAxis.findIndexOf(position.getVerticalPosition().getZ());
            vSize = vAxis.size();
        }
        int tIndex = 0;
        if(tAxis != null){
            tIndex = tAxis.findIndexOf(position.getTimePosition());
        }
        return hIndex + hSize * vIndex + hSize * vSize * tIndex;
    }

    @Override
    public GridCoordinates4D getComponentsOf(long index) {
        int xSize = hGrid.getXAxis().size();
        int ySize = hGrid.getYAxis().size();
        int zSize = 1;
        if(vAxis != null)
            zSize = vAxis.size();
        int tComp = (int) ((index - (index % (xSize*ySize*zSize)))/(xSize*ySize*zSize));
        int zComp = (int) ((index-tComp*xSize*ySize*zSize-(index%(xSize*ySize)))/(xSize*ySize));
        int yComp = (int) ((index-tComp*xSize*ySize*zSize-zComp*xSize*ySize-(index%xSize))/(xSize));
        int xComp = (int) (index-tComp*xSize*ySize*zSize-zComp*xSize*ySize-yComp*xSize);
        return new GridCoordinates4DImpl(xComp, yComp, zComp, tComp);
    }
}
