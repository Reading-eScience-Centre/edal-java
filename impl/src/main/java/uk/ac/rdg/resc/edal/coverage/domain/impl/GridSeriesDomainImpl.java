/*******************************************************************************
 * Copyright (c) 2012 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc.edal.coverage.domain.impl;

import java.util.AbstractList;
import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell4D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates4D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.coverage.grid.impl.AbstractGrid;
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
public class GridSeriesDomainImpl extends AbstractGrid implements GridSeriesDomain {

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
            tIndex = tAxis.findIndexOf(pos.getTimePosition());
            if(tIndex < 0)
                return null;
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
            if(vIndex < 0)
                return null;
            vCrs = vAxis.getVerticalCrs();
            Extent<Double> vExtentDouble = vAxis.getCoordinateBounds(vIndex);
            /*
             * Cast is needed here, otherwise an Extent<VerticalPositionImpl> is returned, which is not what we want
             */
            vExtent = Extents.newExtent((VerticalPosition) new VerticalPositionImpl(vExtentDouble.getLow(), vCrs),
                                        (VerticalPosition) new VerticalPositionImpl(vExtentDouble.getHigh(), vCrs));
        }
        
        GridCell2D hCell = hGrid.findContainingCell(pos.getHorizontalPosition());
        if(hCell == null)
            return null;
        
        return new GridCell4DRectangle(this, hCell, tExtent, tIndex, vExtent, vIndex);
    }

    private List<GridCell4D> domainObjs = null;
    
    @Override
    public List<GridCell4D> getDomainObjects() {
        if(domainObjs != null)
            return domainObjs;
        
        final List<GridCell2D> hCells = hGrid.getDomainObjects();

        final VerticalCrs vCrs;
        final int vSize;
        if (vAxis != null) {
            vCrs = vAxis.getVerticalCrs();
            vSize = vAxis.size();
        } else {
            vCrs = null;
            vSize = 1;
        }
        final int tSize;
        if (tAxis != null) {
            tSize = tAxis.size();
        } else {
            tSize = 1;
        }
        
        domainObjs = new AbstractList<GridCell4D>() {
            @Override
            public GridCell4D get(int index) {
                int hIndex = index % hCells.size();
                int vIndex = ((index - hIndex) % vSize)/hCells.size();
                int tIndex = (index - hIndex - vIndex*hCells.size())/(hCells.size()*vSize);
                GridCell2D hCell = hCells.get(hIndex);
                Extent<TimePosition> tExtent = null;
                if(tAxis != null){
                    tExtent = tAxis.getCoordinateBounds(tIndex);
                }
                Extent<VerticalPosition> vExtent = null;
                if(vAxis != null){
                    Extent<Double> vExtentDouble = vAxis.getCoordinateBounds(vIndex);
                    vExtent = Extents.newExtent((VerticalPosition) new VerticalPositionImpl(vExtentDouble.getLow(), vCrs),
                            (VerticalPosition) new VerticalPositionImpl(vExtentDouble.getHigh(), vCrs));
                }
                return new GridCell4DRectangle(GridSeriesDomainImpl.this, hCell, tExtent, tIndex, vExtent, vIndex);
            }

            @Override
            public int size() {
                return hCells.size() * tSize * vSize;
            }
        };
        
        return domainObjs;
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
        return (hGrid.size() * vSize * tSize);
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
            if(vIndex < 0)
                return -1;
            vSize = vAxis.size();
        }
        int tIndex = 0;
        if(tAxis != null){
            tIndex = tAxis.findIndexOf(position.getTimePosition());
            if(tIndex < 0)
                return -1;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hGrid == null) ? 0 : hGrid.hashCode());
        result = prime * result + ((tAxis == null) ? 0 : tAxis.hashCode());
        result = prime * result + ((vAxis == null) ? 0 : vAxis.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GridSeriesDomainImpl other = (GridSeriesDomainImpl) obj;
        if (hGrid == null) {
            if (other.hGrid != null)
                return false;
        } else if (!hGrid.equals(other.hGrid))
            return false;
        if (tAxis == null) {
            if (other.tAxis != null)
                return false;
        } else if (!tAxis.equals(other.tAxis))
            return false;
        if (vAxis == null) {
            if (other.vAxis != null)
                return false;
        } else if (!vAxis.equals(other.vAxis))
            return false;
        return true;
    }

    @Override
    public GridAxis getAxis(int n) {
        if (n == 0)
            return getHorizontalGrid().getXAxis();
        if (n == 1)
            return getHorizontalGrid().getYAxis();
        if (n == 2)
            return getVerticalAxis();
        if (n == 3)
            return getTimeAxis();
        throw new IndexOutOfBoundsException("This GridValuesMatrix has 4 axes");
    }

    @Override
    public int getNDim() {
        return 4;
    }
}
