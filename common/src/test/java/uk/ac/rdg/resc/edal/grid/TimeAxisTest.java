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

import java.util.*;

import org.joda.time.*;
import org.joda.time.chrono.ISOChronology;

import uk.ac.rdg.resc.edal.domain.*;
import uk.ac.rdg.resc.edal.util.Extents;

public class TimeAxisTest {
    private TimeAxis tAxis;
    private Chronology chronology;

    @Before
    public void setUp(){
        chronology = ISOChronology.getInstance();
        DateTime start = new DateTime(1999, 12, 1, 0, 0, chronology);
        List<DateTime> datetimes = new ArrayList<DateTime>();
        for (int i = 0; i < 11; i++) {
            DateTime dt = start.plusDays(2 * i);
            datetimes.add(dt);
        }
        tAxis = new TimeAxisImpl("Sample TimeAxis", datetimes);
    }

    @Test
    public void test() {
        Extent<DateTime> dateBound = tAxis.getExtent();
        DateTime firstDate = new DateTime(1999, 12, 1, 0, 0, chronology);
        DateTime lastDate = firstDate.plusDays(20);
        Extent<DateTime> expected = Extents.newExtent(firstDate, lastDate);
        assertEquals(dateBound, expected);

        int n = 4;
        DateTime fifthDate = tAxis.getCoordinateValue(n);
        assertEquals(tAxis.findIndexOf(fifthDate), n);
        assertTrue(tAxis.isAscending());

    }

}
