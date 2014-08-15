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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RectilinearGridImpl;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.grid.RegularAxisImpl;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxisImpl;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

/**
 * Test class for {@link SimpleGridDomain}. Only
 * {@link SimpleGridDomain#contains} is tested.
 * 
 * @author Nan Lin
 */
public class SimpleGridDomainTest {
    private SimpleGridDomain sgd;
    private CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
    private VerticalCrs height = new VerticalCrsImpl("meter", false, false, true);
    private Chronology chronology = ISOChronology.getInstance();
    private DateTime start = new DateTime(1999, 12, 2, 0, 0, chronology);

    @Before
    public void setUp() throws Exception {
        double resolution = 1.0;
        ReferenceableAxis<Double> longAxis = new RegularAxisImpl("longitude", 65.5, resolution, 11,
                true);
        ReferenceableAxis<Double> latAxis = new RegularAxisImpl("latitude", 44.5, resolution, 16,
                false);
        HorizontalGrid hGrid = new RectilinearGridImpl(longAxis, latAxis, crs);
        // a list contains the values on z axis
        List<Double> vValues = new ArrayList<Double>();
        for (int i = 0; i < 10; i++) {
            vValues.add(1050 + i * 100.0);
        }
        VerticalAxis vAxis = new VerticalAxisImpl("elevation", vValues, height);
        // a list contains a series of times
        List<DateTime> datetimes = new ArrayList<DateTime>();
        for (int i = 0; i < 11; i++) {
            DateTime dt = start.plusDays(2 * i);
            datetimes.add(dt);
        }
        TimeAxis tAxis = new TimeAxisImpl("Sample TimeAxis", datetimes);
        sgd = new SimpleGridDomain(hGrid, vAxis, tAxis);
    }

    @Test
    public void testContains() {
        assertFalse(sgd.contains(null));
        HorizontalPosition hPosition = new HorizontalPosition(65.0, 44.0, crs);
        VerticalPosition vPosition = new VerticalPosition(1000.0, height);
        GeoPosition gPosition = new GeoPosition(hPosition, vPosition, start);
        assertTrue(sgd.contains(gPosition));

        vPosition = new VerticalPosition(950.0, height);
        gPosition = new GeoPosition(hPosition, vPosition, start);
        assertFalse(sgd.contains(gPosition));

        hPosition = new HorizontalPosition(65.0, 60.0, crs);
        vPosition = new VerticalPosition(1100.0, height);
        gPosition = new GeoPosition(hPosition, vPosition, start);
        assertTrue(sgd.contains(gPosition));

        hPosition = new HorizontalPosition(76.0, 44.0, crs);
        vPosition = new VerticalPosition(1300.0, height);
        gPosition = new GeoPosition(hPosition, vPosition, start.plusDays(10));
        assertTrue(sgd.contains(gPosition));

        hPosition = new HorizontalPosition(76.0, 60.0, crs);
        vPosition = new VerticalPosition(1700.0, height);
        gPosition = new GeoPosition(hPosition, vPosition, start.plusDays(15));
        assertTrue(sgd.contains(gPosition));

        vPosition = new VerticalPosition(2000.0, height);
        gPosition = new GeoPosition(hPosition, vPosition, start.plusDays(20));
        assertTrue(sgd.contains(gPosition));

        gPosition = new GeoPosition(hPosition, vPosition, start.plusDays(21));
        assertTrue(sgd.contains(gPosition));

        gPosition = new GeoPosition(hPosition, vPosition, start.plusDays(30));
        assertFalse(sgd.contains(gPosition));
    }
}
