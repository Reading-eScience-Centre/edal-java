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

package uk.ac.rdg.resc.edal.domain;

import org.joda.time.Chronology;
import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.grid.GridCell4D;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.SimpleGridCell4D;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.Array4D;

/**
 * A {@link GridDomain} which consists of a {@link HorizontalGrid}, a
 * {@link TimeAxis}, and a {@link VerticalAxis}
 * 
 * @author Guy Griffiths
 */
public class SimpleGridDomain implements GridDomain {

    private HorizontalGrid hGrid;
    private VerticalAxis vAxis;
    private TimeAxis tAxis;

    @Override
    public Array<GridCell4D> getDomainObjects() {
        int xSize = hGrid == null ? 1 : hGrid.getXSize();
        int ySize = hGrid == null ? 1 : hGrid.getYSize();
        int zSize = vAxis == null ? 1 : vAxis.size();
        int tSize = tAxis == null ? 1 : tAxis.size();
        return new Array4D<GridCell4D>(tSize, zSize, ySize, xSize) {
            @Override
            public GridCell4D get(int... coords) {
                int x = coords[3];
                int y = coords[2];
                int z = coords[1];
                int t = coords[0];

                GridCell2D gridCell2D = hGrid == null ? null : hGrid.getDomainObjects().get(y, x);
                Extent<Double> vExtent = vAxis == null ? null : vAxis.getCoordinateBounds(z);
                VerticalCrs verticalCrs = vAxis == null ? null : vAxis.getVerticalCrs();
                Extent<DateTime> tExtent = tAxis == null ? null : tAxis.getCoordinateBounds(t);
                Chronology chronology = tAxis == null ? null : tAxis.getChronology();
                return new SimpleGridCell4D(gridCell2D, vExtent, verticalCrs, tExtent, chronology,
                        SimpleGridDomain.this);
            }

            @Override
            public void set(GridCell4D value, int... coords) {
                throw new UnsupportedOperationException("This Array4D is immutable");
            }

            @Override
            public Class<GridCell4D> getValueClass() {
                return GridCell4D.class;
            }
        };
    }

    @Override
    public boolean contains(GeoPosition position) {
        boolean hContains;
        boolean vContains;
        boolean tContains;
        if (position == null) {
            return false;
        }
        if (hGrid == null) {
            if (position.getHorizontalPosition() != null) {
                return false;
            } else {
                hContains = true;
            }
        } else {
            if (position.getHorizontalPosition() == null) {
                return false;
            } else {
                hContains = hGrid.contains(position.getHorizontalPosition());
            }
        }

        if (vAxis == null) {
            if (position.getVerticalPosition() != null) {
                return false;
            } else {
                vContains = true;
            }
        } else {
            if (position.getVerticalPosition() == null) {
                return false;
            } else {
                if (!position.getVerticalPosition().getCoordinateReferenceSystem()
                        .equals(vAxis.getVerticalCrs())) {
                    return false;
                }
                vContains = vAxis.contains(position.getVerticalPosition().getZ());
            }
        }

        if (tAxis == null) {
            if (position.getTime() != null) {
                return false;
            } else {
                tContains = true;
            }
        } else {
            if (position.getTime() == null) {
                return false;
            } else {
                tContains = tAxis.contains(position.getTime());
            }
        }

        return hContains && vContains && tContains;
    }

    @Override
    public HorizontalGrid getHorizontalGrid() {
        return hGrid;
    }

    @Override
    public VerticalAxis getVerticalAxis() {
        return vAxis;
    }

    @Override
    public TimeAxis getTimeAxis() {
        return tAxis;
    }

    @Override
    public long size() {
        long hSize = hGrid == null ? 1 : hGrid.size();
        int vSize = vAxis == null ? 1 : vAxis.size();
        int tSize = tAxis == null ? 1 : tAxis.size();
        return hSize * vSize * tSize;
    }
}
