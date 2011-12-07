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
package uk.ac.rdg.resc.edal.cdm.coverage.grid;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridExtent;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.AbstractHorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.GridExtentImpl;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * Partial implementation of a {@link HorizontalGrid} that is based upon a
 * curvilinear coordinate system ({@literal i.e.} one which is defined by
 * explicitly specifying the latitude and longitude coordinates of each grid
 * point.
 * 
 * @author Jon Blower
 */
public abstract class AbstractCurvilinearGrid extends AbstractHorizontalGrid {
    protected final CurvilinearCoords curvGrid;
    private final GridExtent gridExtent;
    private BoundingBox coordinateExtent;

    protected AbstractCurvilinearGrid(CurvilinearCoords curvGrid) {
        // All points will be returned in WGS84 lon-lat
        super(DefaultGeographicCRS.WGS84);
        this.curvGrid = curvGrid;
        this.gridExtent = new GridExtentImpl(curvGrid.getNi()-1, curvGrid.getNj()-1);
        coordinateExtent = curvGrid.getBoundingBox();
    }

    @Override
    protected HorizontalPosition transformCoordinatesNoBoundsCheck(int i, int j) {
        return this.curvGrid.getMidpoint(i, j);
    }

    @Override
    public GridExtent getGridExtent() {
        return this.gridExtent;
    }

    /*
     * TODO Check that this is OK
     */
    @Override
    public GridAxis getXAxis() {
        return new GridAxis() {
            @Override
            public int size() {
                return curvGrid.getNi();
            }

            @Override
            public String getName() {
                return "i";
            }

            @Override
            public Extent<Integer> getIndexExtent() {
                return Extents.newExtent(0, curvGrid.getNi()-1);
            }
        };
    }

    /*
     * TODO Check that this is OK
     */
    @Override
    public GridAxis getYAxis() {
        return new GridAxis() {
            @Override
            public int size() {
                return curvGrid.getNj();
            }

            @Override
            public String getName() {
                return "j";
            }

            @Override
            public Extent<Integer> getIndexExtent() {
                return Extents.newExtent(0, curvGrid.getNj()-1);
            }
        };
    }

    @Override
    public BoundingBox getCoordinateExtent() {
//        return curvGrid.getBoundingBox();
        return coordinateExtent;
    }

    @Override
    public GridCell2D getGridCell(GridCoordinates2D coords) {
        return getGridCell(coords.getXIndex(), coords.getYIndex());
    }
}