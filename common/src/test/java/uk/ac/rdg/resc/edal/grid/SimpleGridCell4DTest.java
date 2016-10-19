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

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.SimpleGridDomain;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.chronologies.NoLeapChronology;

/**
 * Test class for {@link SimpleGridCell4D}.
 *
 * @author Nan
 *
 */
public class SimpleGridCell4DTest {
    private SimpleGridDomain sgd;
    private Extent<Double> vExtent;
    private Extent<DateTime> tExtent;
    private CoordinateReferenceSystem crs = GISUtils.defaultGeographicCRS();
    private VerticalCrs depth = new VerticalCrsImpl("meter", false, false, false);
    private Chronology chronology = ISOChronology.getInstance();

    /**
     * Initialise the testing environment: a simple grid domain with its X Y Z T
     * axis values.
     */
    @Before
    public void setUp() {
        double resolution = 1.0;
        ReferenceableAxis<Double> longAxis = new RegularAxisImpl("longitude", -9.5, resolution, 10,
                true);
        ReferenceableAxis<Double> latAxis = new RegularAxisImpl("latitude", 50.5, resolution, 8,
                false);
        RectilinearGrid rGrid = new RectilinearGridImpl(longAxis, latAxis, crs);

        vExtent = Extents.newExtent(0.0, 1000.0);
        DateTime begin = new DateTime(2000, 1, 1, 0, 0, chronology);
        // there are 10 dates on the t axis
        tExtent = Extents.newExtent(begin, begin.plusDays(10));

        List<Double> vValues = new ArrayList<Double>();
        // there are 11 values on the Z axis
        for (int i = 0; i < 11; i++) {
            vValues.add(i * 100.0);
        }
        VerticalAxis vAxis = new VerticalAxisImpl("depth", vValues, depth);

        List<DateTime> datetimes = new ArrayList<DateTime>();
        for (int i = 0; i < 11; i++) {
            DateTime dt = begin.plusDays(i);
            datetimes.add(dt);
        }
        TimeAxis tAxis = new TimeAxisImpl("Sample TimeAxis", datetimes);
        sgd = new SimpleGridDomain(rGrid, vAxis, tAxis);
    }

    /**
     * Test {@link SimpleGridCell4D#contains} method.
     */
    @Test
    public void testContains() {
        /*
         * choose positions inside or outside of X Y Z T axis of the simple grid
         * cell 4D. use various chronology.
         */
        HorizontalPosition hPos = new HorizontalPosition(-6.5, 52.7, crs);
        VerticalPosition vPos = new VerticalPosition(100.9, depth);
        DateTime dt = new DateTime(2000, 1, 2, 12, 45, chronology);

        Array<GridCell2D> cells = sgd.getHorizontalGrid().getDomainObjects();
        // get a cell index with i=2 j=3 of the grid
        GridCell2D cell = cells.get(2, 3);
        SimpleGridCell4D sgc = new SimpleGridCell4D(cell, vExtent, depth, tExtent, chronology, sgd);

        GeoPosition gPos = new GeoPosition(hPos, vPos, dt);
        assertTrue(sgc.contains(gPos));
        assertFalse(sgc.contains(null));

        Chronology noleap = NoLeapChronology.getInstanceUTC();
        dt = new DateTime(2000, 1, 2, 12, 45, noleap);
        gPos = new GeoPosition(hPos, vPos, dt);
        assertFalse(sgc.contains(gPos));

        vPos = new VerticalPosition(1000.9, depth);
        dt = new DateTime(2000, 1, 2, 12, 45, chronology);
        gPos = new GeoPosition(hPos, vPos, dt);
        assertFalse(sgc.contains(gPos));

        vPos = new VerticalPosition(800.9, depth);
        dt = new DateTime(2000, 11, 12, 2, 45, chronology);
        gPos = new GeoPosition(hPos, vPos, dt);
        assertFalse(sgc.contains(gPos));

        hPos = new HorizontalPosition(-8.5, 53.7, crs);
        vPos = new VerticalPosition(100.9, depth);
        dt = new DateTime(2000, 1, 2, 12, 45, chronology);
        gPos = new GeoPosition(hPos, vPos, dt);
        assertFalse(sgc.contains(gPos));

        cell = null;
        sgc = new SimpleGridCell4D(cell, vExtent, depth, tExtent, chronology, sgd);
        assertFalse(sgc.contains(gPos));
    }
}
