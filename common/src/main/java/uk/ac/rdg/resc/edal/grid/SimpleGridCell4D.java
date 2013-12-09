/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
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

package uk.ac.rdg.resc.edal.grid;

import org.joda.time.Chronology;
import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.GridDomain;
import uk.ac.rdg.resc.edal.geometry.Polygon;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;

public class SimpleGridCell4D implements GridCell4D {

    private final GridCell2D hGridCell;
    private final Extent<Double> vExtent;
    private final Extent<DateTime> tExtent;

    private final VerticalCrs vCrs;
    private final Chronology chronology;

    private final GridDomain grid;

    public SimpleGridCell4D(GridCell2D hGridCell, Extent<Double> vExtent, VerticalCrs vCrs,
            Extent<DateTime> tExtent, Chronology chronology, GridDomain grid) {
        this.hGridCell = hGridCell;
        this.vExtent = vExtent;
        this.vCrs = vCrs;
        this.tExtent = tExtent;
        this.chronology = chronology;
        
        this.grid = grid;
    }

    @Override
    public boolean contains(GeoPosition position) {
        boolean hContains;
        boolean vContains;
        boolean tContains;
        if (position == null) {
            return false;
        }
        if (hGridCell == null) {
            if (position.getHorizontalPosition() != null) {
                return false;
            } else {
                hContains = true;
            }
        } else {
            if (position.getHorizontalPosition() == null) {
                return false;
            } else {
                hContains = hGridCell.contains(position.getHorizontalPosition());
            }
        }

        if (vExtent == null) {
            if (position.getVerticalPosition() != null) {
                return false;
            } else {
                vContains = true;
            }
        } else {
            if (position.getVerticalPosition() == null) {
                return false;
            } else {
                if (!position.getVerticalPosition().getCoordinateReferenceSystem().equals(vCrs)) {
                    return false;
                }
                vContains = vExtent.contains(position.getVerticalPosition().getZ());
            }
        }

        if (tExtent == null) {
            if (position.getTime() != null) {
                return false;
            } else {
                tContains = true;
            }
        } else {
            if (position.getTime() == null) {
                return false;
            } else {
                tContains = tExtent.contains(position.getTime());
            }
        }

        return hContains && vContains && tContains;
    }

    @Override
    public HorizontalPosition getCentre() {
        return hGridCell == null ? null : hGridCell.getCentre();
    }

    @Override
    public Polygon getFootprint() {
        return hGridCell == null ? null : hGridCell.getFootprint();
    }

    @Override
    public Extent<DateTime> getTimeExtent() {
        return tExtent;
    }

    @Override
    public Chronology getChronology() {
        return chronology;
    }

    @Override
    public Extent<Double> getVerticalExtent() {
        return vExtent;
    }

    @Override
    public VerticalCrs getVerticalCrs() {
        return vCrs;
    }

    @Override
    public GridDomain getGrid() {
        return grid;
    }
}
