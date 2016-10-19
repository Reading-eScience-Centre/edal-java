/*******************************************************************************
 * Copyright (c) 2014 The University of Reading
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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Test class for {@link GridCell2DImpl}.
 *
 * @author Nan Lin
 *
 */
public class GridCell2DImplTest {
    private RectilinearGrid rGrid;
    private CoordinateReferenceSystem crs = GISUtils.defaultGeographicCRS();

    /**
     * Initialize the rectilinear grid.
     */
    @Before
    public void setUp() {
        double resolution = 1.0 / 2;
        int xSize = 12;
        int ySize = 16;
        ReferenceableAxis<Double> longAxis = new RegularAxisImpl("longitude", 95.0, resolution,
                xSize, true);
        ReferenceableAxis<Double> latAxis = new RegularAxisImpl("latitude", 33.0, resolution,
                ySize, false);
        rGrid = new RectilinearGridImpl(longAxis, latAxis, crs);
    }

    /**
     * Test {@link GridCell2DImpl#contains} and {@link GridCell2DImpl#getGrid}.
     * Pick up positions inside or outside the cell, and a special value "null".
     */
    @Test
    public void testContains() {
        Array<GridCell2D> cells = rGrid.getDomainObjects();
        // pick up a cell of(0,0)
        GridCell2D cell = cells.get(0, 0);
        // create a horizontal position
        HorizontalPosition position = new HorizontalPosition(120.0, 30.0, crs);
        assertFalse(cell.contains(position));
        assertFalse(cell.contains(null));

        cell = cells.get(3, 4);
        position = new HorizontalPosition(97.2, 34.444, crs);
        assertTrue(cell.contains(position));
        assertEquals(rGrid, cell.getParentDomain());
    }

    /**
     * Test {@link GridCell2DImpl#getFootprint}.
     */
    @Test
    public void testGetFootprint() {
        Array<GridCell2D> cells = rGrid.getDomainObjects();
        GridCell2D cell = cells.get(1, 4);
        // expectedBbox is drawn by hand
        BoundingBox expectedBbox = new BoundingBoxImpl(96.75, 33.25, 97.25, 33.75, crs);
        assertEquals(expectedBbox, cell.getFootprint());

        cell = cells.get(3, 4);
        /*
         * as we know the bbox of this cell, pick another bbox which is disjoint
         * the expected one.
         */
        expectedBbox = new BoundingBoxImpl(96.75, 33.6, 97.25, 33.98, crs);
        assertFalse(cell.getFootprint().equals(expectedBbox));
    }
}
