/*******************************************************************************
 * Copyright (c) 2011 The University of Reading
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
 *******************************************************************************/
package uk.ac.rdg.resc.edal.grid;

import org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.AbstractImmutableArray;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.CurvilinearCoords;
import uk.ac.rdg.resc.edal.util.CurvilinearCoords.Cell;

/**
 * Partial implementation of a {@link HorizontalGrid} that is based upon a
 * curvilinear coordinate system ({@literal i.e.} one which is defined by
 * explicitly specifying the latitude and longitude coordinates of each grid
 * point.
 * 
 * @author Guy Griffiths
 * @author Jon Blower
 */
public abstract class AbstractCurvilinearGrid implements HorizontalGrid {
    protected final CurvilinearCoords curvCoords;
    private final BoundingBox latLonBbox;

    protected AbstractCurvilinearGrid(CurvilinearCoords curvCoords) {
        this.curvCoords = curvCoords;
        this.latLonBbox = curvCoords.getBoundingBox();
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return DefaultGeographicCRS.WGS84;
    }

    @Override
    public Array<GridCell2D> getDomainObjects() {
        return new AbstractImmutableArray<GridCell2D>(GridCell2D.class, new int[] {
                curvCoords.getNj(), curvCoords.getNi() }) {
            @Override
            public Class<GridCell2D> getValueClass() {
                return GridCell2D.class;
            }

            @Override
            public GridCell2D get(int... coords) {
                int xIndex = coords[1];
                int yIndex = coords[0];
                Cell cell = curvCoords.getCell(xIndex, yIndex);
                return new GridCell2DImpl(coords, cell.getCentre(),
                        cell.getMinimumBoundingRectangle(), AbstractCurvilinearGrid.this);
            }
        };
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        int[] coords = findIndexOf(position);
        return (coords != null);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return latLonBbox;
    }

    @Override
    public GeographicBoundingBox getGeographicBoundingBox() {
        return new DefaultGeographicBoundingBox(latLonBbox.getMinX(), latLonBbox.getMaxX(),
                latLonBbox.getMinY(), latLonBbox.getMaxY());
    }

    @Override
    public long size() {
        return curvCoords.size();
    }
}