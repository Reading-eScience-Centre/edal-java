/*
 * Copyright (c) 2010 The University of Reading
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
 */

package uk.ac.rdg.resc.edal.time;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import uk.ac.rdg.resc.edal.time.ThreeSixtyDayChronology;
import static org.junit.Assert.*;
import static uk.ac.rdg.resc.edal.time.TimeUtils.*;

/**
 * Tests of the methods in {@link TimeUtils}.
 * @author Jon
 */
public final class TimeUtilsTest
{
    @Test
    public void testParseUdunitsTimeString()
    {
        DateTime dt = parseUdunitsTimeString("1970-1-1");
        assertEquals(0, dt.getMillis());
        assertEquals(DateTimeZone.UTC, dt.getZone());

        dt = parseUdunitsTimeString("1970-1-1 0:0:0.1");
        assertEquals(100, dt.getMillis());
        assertEquals(DateTimeZone.UTC, dt.getZone());

        dt = parseUdunitsTimeString("1992-10-8 15:15:42.5 -6:00");
        assertEquals(DateTimeZone.UTC, dt.getZone());
        assertEquals(1992, dt.getYear());
        assertEquals(10, dt.getMonthOfYear());
        assertEquals(8, dt.getDayOfMonth());
        assertEquals(21, dt.getHourOfDay()); // NOTE: time zone offset applied
        assertEquals(15, dt.getMinuteOfHour());
        assertEquals(42, dt.getSecondOfMinute());
        assertEquals(500, dt.getMillisOfSecond());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testParse360DayTimeStringWithWrongChronology()
    {
        parseUdunitsTimeString("2000-02-30");
    }

    @Test
    public void testParse360DayTimeStringWithCorrectChronology()
    {
        DateTime dt = parseUdunitsTimeString("2000-02-30", ThreeSixtyDayChronology.getInstanceUTC());
        assertEquals(2000, dt.getYear());
        assertEquals(2, dt.getMonthOfYear());
        assertEquals(30, dt.getDayOfMonth());
        assertEquals(0, dt.getHourOfDay());
        assertEquals(0, dt.getMinuteOfHour());
        assertEquals(0, dt.getSecondOfMinute());
        assertEquals(0, dt.getMillisOfSecond());
        assertEquals(DateTimeZone.UTC, dt.getZone());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testParseIllegal360DayTimeString()
    {
        // A date that would be legal in ISO chronology but not in 360-day
        parseUdunitsTimeString("2000-01-31", ThreeSixtyDayChronology.getInstanceUTC());
    }
}
