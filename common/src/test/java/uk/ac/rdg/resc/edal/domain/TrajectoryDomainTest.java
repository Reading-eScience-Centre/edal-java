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

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;
import org.junit.Before;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.joda.time.*;

import uk.ac.rdg.resc.edal.position.*;
import uk.ac.rdg.resc.edal.exceptions.*;

import java.util.*;

/**
 * Test class for {@link TrajectoryDomain}. Only
 * {@link TrajectoryDomain#contains} is tested.
 * 
 * @author Nan
 */
public class TrajectoryDomainTest {
    private final int SIZE = 100;
    private List<GeoPosition> positions = new ArrayList<GeoPosition>(SIZE);
    private CoordinateReferenceSystem crs;
    private VerticalCrs height;
    private DateTime beginDate;
    private TrajectoryDomain tDomain;

    @Before
    public void setUp() throws MismatchedCrsException {
        crs = DefaultGeographicCRS.WGS84;
        height = new VerticalCrsImpl("meter", false, false, true);
        beginDate = new DateTime(1990, 5, 8, 0, 0);
        //iniatialize a series of geo-positions
        for (int i = 0; i < SIZE; i++) {
            HorizontalPosition hPos = new HorizontalPosition(10 + i * 0.1, 20 + i * 0.2, crs);
            VerticalPosition vPos = new VerticalPosition(100 + i * 10.0, height);
            DateTime dt = beginDate.plusDays(i);
            positions.add(new GeoPosition(hPos, vPos, dt));
        }
        tDomain = new TrajectoryDomain(positions);
    }

    @Test
    public void testContains() {
        HorizontalPosition hPos = new HorizontalPosition(11.0, 22.0, crs);
        VerticalPosition vPos = new VerticalPosition(200.0, height);
        DateTime dt = beginDate.plusDays(10);
        assertTrue(tDomain.contains(new GeoPosition(hPos, vPos, dt)));

        hPos = new HorizontalPosition(19.9, 39.8, crs);
        dt = beginDate.plusDays(99);
        vPos = new VerticalPosition(1090.0, height);
        assertTrue(tDomain.contains(new GeoPosition(hPos, vPos, dt)));

        dt = beginDate.plusDays(11);
        assertFalse(tDomain.contains(new GeoPosition(hPos, vPos, dt)));

        assertFalse(tDomain.contains(null));

        hPos = new HorizontalPosition(10.0, 20.0, crs);
        vPos = new VerticalPosition(100.0, height);
        assertTrue(tDomain.contains(new GeoPosition(hPos, vPos, beginDate)));

        hPos = new HorizontalPosition(19.9, 39.8, crs);
        dt = beginDate.plusDays(99);
        vPos = new VerticalPosition(1090.0, height);
        assertTrue(tDomain.contains(new GeoPosition(hPos, vPos, dt)));
    }
}
