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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.exceptions.InvalidCrsException;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;

/**
 * Test class for {@link RectilinearGridImpl}.
 *
 * @author Nan
 *
 */
public class RectilinearGridImplTest {
    // para about the tested grid
    private int xSize = 12;
    private int ySize = 16;
    private RectilinearGrid rGrid;
    private ReferenceableAxis<Double> longAxis;
    private ReferenceableAxis<Double> latAxis;
    private CoordinateReferenceSystem crs = GISUtils.defaultGeographicCRS();

    /**
     * Initialize the RectilinearGrid.
     */
    @Before
    public void setUp() {
        double resolution = 1.0 / 2;
        longAxis = new RegularAxisImpl("longitude", 95.0, resolution, xSize, true);
        latAxis = new RegularAxisImpl("latitude", 33.0, resolution, ySize, false);
        rGrid = new RectilinearGridImpl(longAxis, latAxis, crs);
    }

    /**
     * Test get methods of {@link RectilinearGridImpl}.
     */
    @Test
    public void testGetMethods() {
        assertEquals(xSize, rGrid.getXSize());
        assertEquals(ySize, rGrid.getYSize());
        assertEquals(xSize * ySize, rGrid.size());
        assertEquals(longAxis, rGrid.getXAxis());
        assertEquals(latAxis, rGrid.getYAxis());
        assertEquals(crs, rGrid.getCoordinateReferenceSystem());
        // expectedBbox is drawn by hand
        BoundingBox expectedBbox = new BoundingBoxImpl(94.75, 32.75, 100.75, 40.75, crs);
        assertEquals(expectedBbox, rGrid.getBoundingBox());
        // expectedBbox is drawn by hand
        DefaultGeographicBoundingBox expectedGbbox = new DefaultGeographicBoundingBox(94.75,
                100.75, 32.75, 40.75);
        assertEquals(expectedGbbox, rGrid.getGeographicBoundingBox());

        /*
         * a container contain a series of horizontal positions which are the
         * centres of cells.
         */
        List<HorizontalPosition> hPoss = new ArrayList<>(xSize * ySize);
        for (int i = 0; i < ySize; i++) {
            for (int j = 0; j < xSize; j++) {
                hPoss.add(new HorizontalPosition(95.0 + j * 0.5, 33.0 + i * 0.5, crs));
            }
        }

        Array<GridCell2D> cells = rGrid.getDomainObjects();
        for (GridCell2D cell : cells) {
            int xIndex = cell.getGridCoordinates().getX();
            int yIndex = cell.getGridCoordinates().getY();
            assertEquals(hPoss.get(xIndex + yIndex * xSize), cell.getCentre());
            assertEquals(rGrid, cell.getParentDomain());
        }
    }

    /**
     * Test {@link RectilinearGridImpl#contains}. Pick up positions inside or
     * outside the grid.
     *
     * @throws InvalidCrsException
     *             if a wrong epsg code is provided.
     */
    @Test
    public void testContains() throws InvalidCrsException {
        //points are inside grid. the test should return true.
        HorizontalPosition position = new HorizontalPosition(120, 30, crs);
        assertFalse(rGrid.contains(position));
        position = new HorizontalPosition(96.0, 34.6, crs);
        assertTrue(rGrid.contains(position));
        position = new HorizontalPosition(96.0, 40.7, crs);
        assertTrue(rGrid.contains(position));

        //give "null" as a special argument.
        assertFalse(rGrid.contains(null));

        //a point using different epsg code.
        CoordinateReferenceSystem japanArea = GISUtils.getCrs("EPSG:2450");
        assertFalse(rGrid.contains(new HorizontalPosition(17945.194292, 41625.344542, japanArea)));
    }

    /**
     * Test {@link RectilinearGridImpl#findIndexOf}.
     */
    @Test
    public void testFindIndexOf() {
        HorizontalPosition position = new HorizontalPosition(95.3, 34.35, crs);
        // expectedCoord is drawn by hand.
        GridCoordinates2D expectedCoord = new GridCoordinates2D(1, 3);
        assertEquals(expectedCoord, rGrid.findIndexOf(position));

        position = new HorizontalPosition(100.4, 40.7, crs);
        // pick up a coord which is different from the expected one
        GridCoordinates2D coord = new GridCoordinates2D(11, 15);
        assertEquals(coord, rGrid.findIndexOf(position));
    }
}
