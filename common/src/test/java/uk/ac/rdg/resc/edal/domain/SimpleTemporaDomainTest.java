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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;

import uk.ac.rdg.resc.edal.util.chronologies.*;

/**
 * Test class for {@link SimpleTemporaDomain}. Only
 * {@link SimpleTemporaDomain#contains} is tested.
 * 
 * @author Nan Lin
 */
public class SimpleTemporaDomainTest {
    /**
     * Test {@link SimpleTemporaDomain#contains}. Use various Chronology and
     * dates are inside or outside the temporal domains.
     */
    @Test
    public void testContains() {
        NoLeapChronology nlc = NoLeapChronology.getInstanceUTC();
        DateTime start = new DateTime(2012, 1, 1, 0, 0, nlc);
        DateTime end = new DateTime(2012, 12, 31, 23, 59, nlc);
        SimpleTemporalDomain std = new SimpleTemporalDomain(start, end);
        DateTime dt1 = new DateTime(2012, 2, 29, 12, 10);
        DateTime dt2 = new DateTime(2011, 2, 27, 12, 10, nlc);
        DateTime dt3 = new DateTime(2012, 2, 27, 12, 10, nlc);
        DateTime dt4 = new DateTime(2012, 5, 27, 12, 10);

        assertFalse(std.contains(null));
        // NoLeapChronology doesn't contain the date Feb. 29
        assertFalse(std.contains(dt1));
        // dt2 is outside the temporal domain
        assertFalse(std.contains(dt2));
        assertTrue(std.contains(dt3));
        // dt4 use ISOChronology
        assertFalse(std.contains(dt4));
        assertTrue(std.contains(start));
        assertTrue(std.contains(end));

        ThreeSixtyDayChronology tsdc = ThreeSixtyDayChronology.getInstanceUTC();
        start = new DateTime(2012, 1, 1, 0, 0, tsdc);
        end = new DateTime(2012, 12, 30, 23, 59, tsdc);
        std = new SimpleTemporalDomain(start, end);
        dt1 = new DateTime(2012, 2, 29, 12, 10, tsdc);
        dt2 = new DateTime(2011, 2, 27, 12, 10, tsdc);
        dt3 = new DateTime(2012, 3, 31, 12, 10);
        dt4 = new DateTime(2012, 5, 27, 12, 10, nlc);

        assertTrue(std.contains(start));
        assertTrue(std.contains(end));
        assertTrue(std.contains(dt1));
        // In ThreeSixtyDayChronology, March hasn't 31 days.
        assertFalse(std.contains(dt3));
        assertFalse(std.contains(dt4));

        ISOChronology iso = ISOChronology.getInstance();
        start = new DateTime(2012, 1, 1, 0, 0, iso);
        end = new DateTime(2012, 12, 31, 23, 59, iso);
        std = new SimpleTemporalDomain(start, end);
        dt1 = new DateTime(2012, 2, 29, 12, 10);
        dt2 = new DateTime(2011, 2, 27, 12, 10, nlc);
        dt3 = new DateTime(2012, 12, 31, 23, 59, 36);
        assertTrue(std.contains(dt1));
        //dt2 uses different chronology
        assertFalse(std.contains(dt2));
        // dt3 now is outside this temporal domain though the gap is 36 seconds
        assertFalse(std.contains(dt3));
    }
}
