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

package uk.ac.rdg.resc.edal.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.grid.RegularAxis;
import uk.ac.rdg.resc.edal.grid.RegularAxisImpl;
import uk.ac.rdg.resc.edal.grid.RegularGrid;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;

/**
 * Test class for {@link MapDomain}.
 * 
 * @author Nan Lin
 * */
public class MapDomainTest {
    private MapDomain mapdomain;
    private CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;

    // grid resolution
    double resolution = 1.0 / 2;
    // grid x-side cell number
    int xSize = 6;
    // grid y-side cell number
    int ySize = 8;
    // longitude position of lower left point of the HorizontalGrid
    double lowX = -10.0;
    // latitude position of lower left point of the HorizontalGrid
    double lowY = 51.0;
    private RegularGrid hGrid;

    /**
     * Initialize a map domain.
     * 
     */
    @Before
    public void setUp() {
        // a value for z axis
        double z = 100.0;
        // m stands for meter
        VerticalCrs vCrs = new VerticalCrsImpl("m", false, false, false);
        Chronology chrnology = ISOChronology.getInstance();
        DateTime time = new DateTime(2000, 01, 01, 00, 00, chrnology);
        RegularAxis longAxis = new RegularAxisImpl("longitude", -10.0, resolution,
                xSize, true);
        RegularAxis latAxis = new RegularAxisImpl("latitude", 51.0, resolution,
                ySize, false);
        hGrid = new RegularGridImpl(longAxis, latAxis, crs);
        mapdomain = new MapDomain(hGrid, z, vCrs, time);
    }

    /**
     * Test get methods in {@link MapDomain}.
     * */
    @Test
    public void test() {
        assertEquals(hGrid.getXSize() * hGrid.getYSize(), mapdomain.size());

        HorizontalPosition hPos = new HorizontalPosition(20.3, 60.8, crs);
        assertFalse(mapdomain.contains(hPos));

        hPos = new HorizontalPosition(-9.3, 53.2, crs);
        assertTrue(mapdomain.contains(hPos));

        int expectedXPos = (int) ((hPos.getX() - lowX) / resolution);
        int expectedYPos = (int) ((hPos.getY() - lowY) / resolution);
        GridCoordinates2D expectedCoordinate = new GridCoordinates2D(expectedXPos, expectedYPos);
        assertEquals(expectedCoordinate, mapdomain.findIndexOf(hPos));

        Extent<Double> xExtent = Extents.newExtent(lowX - resolution / 2, lowX - resolution / 2
                + xSize * resolution);
        Extent<Double> yExtent = Extents.newExtent(lowY - resolution / 2, lowY - resolution / 2
                + ySize * resolution);
        BoundingBox expectedBbox = new BoundingBoxImpl(xExtent, yExtent, crs);
        assertEquals(expectedBbox, mapdomain.getBoundingBox());

        Array<GridCell2D> cells = mapdomain.getDomainObjects();
        for (GridCell2D cell : cells) {
            int xIndex = cell.getGridCoordinates().getX();
            int yIndex = cell.getGridCoordinates().getY();
            HorizontalPosition expectedcentre = new HorizontalPosition(lowX + xIndex * resolution,
                    lowY + yIndex * resolution, crs);
            assertEquals(expectedcentre, cell.getCentre());
            assertEquals(hGrid, cell.getParentDomain());
        }
    }
}
