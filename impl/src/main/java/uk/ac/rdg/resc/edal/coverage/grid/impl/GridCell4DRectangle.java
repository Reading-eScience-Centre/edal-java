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
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        if(horizGridCell != null){
            s.append(horizGridCell.toString());
        }
        if(tExtent != null){
            s.append(", Time: "+tExtent.toString());
        }
        if(vExtent != null){
            s.append(", Vertical: "+vExtent.toString());
        }
        return s.toString();
    }
}
