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

package uk.ac.rdg.resc.edal.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.exceptions.BadTimeFormatException;

/**
 * Test class for {@link TimeUtils}.
 * 
 * @author Nan
 * 
 */
public class TimeUtilsTest {
    private DateTime start = new DateTime(2000, 1, 1, 0, 0, DateTimeZone.UTC);
    private List<DateTime> datetimes = new ArrayList<DateTime>();
    private final static long MILLIS_PER_SECOND = 1000L;
    private final static long MILLIS_PER_MINUTE = 60L * MILLIS_PER_SECOND;
    private final static long MILLIS_PER_HOUR = 60L * MILLIS_PER_MINUTE;
    private final static long MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR;

    /**
     * Initialising.
     */
    @Before
    public void setUp() {
        for (int i = 0; i < 10; i++)
            datetimes.add(start.plusDays(i));
    }

    /**
     * Test the method of {@link TimeUtils#getPeriodString}.
     */
    @Test
    public void testGetPeriodString() {
        long secondPeriod = 44 * MILLIS_PER_SECOND;
        assertEquals("PT44S", TimeUtils.getPeriodString(secondPeriod));
        long minutePeriod = 15 * MILLIS_PER_MINUTE + secondPeriod;
        assertEquals("PT15M44S", TimeUtils.getPeriodString(minutePeriod));
        long hourPeriod = 20 * MILLIS_PER_HOUR + minutePeriod;
        assertEquals("PT20H15M44S", TimeUtils.getPeriodString(hourPeriod));
        long dayPeriod = 3 * MILLIS_PER_DAY + hourPeriod;
        assertEquals("P3DT20H15M44S", TimeUtils.getPeriodString(dayPeriod));
    }

    /**
     * Test the method of {@link TimeUtils#getTimeStringForCapabilities}.
     */
    @Test
    public void testGetTimeStringForCapabilities() {
        long period = 1L * MILLIS_PER_DAY;
        String oneDayPeriod = TimeUtils.getPeriodString(period);
        DateTime lastDate = datetimes.get(datetimes.size() - 1);
        String expected = TimeUtils.dateTimeToISO8601(start) + "/"
                + TimeUtils.dateTimeToISO8601(lastDate) + "/" + oneDayPeriod;
        assertEquals(expected, TimeUtils.getTimeStringForCapabilities(datetimes));

        datetimes.add(lastDate.plusDays(2));
        lastDate = datetimes.get(datetimes.size() - 1);
        expected = expected + "," + TimeUtils.dateTimeToISO8601(lastDate);
        assertEquals(expected, TimeUtils.getTimeStringForCapabilities(datetimes));

        datetimes.add(lastDate.plusDays(3));
        lastDate = datetimes.get(datetimes.size() - 1);
        expected = expected + "," + TimeUtils.dateTimeToISO8601(lastDate);
        assertEquals(expected, TimeUtils.getTimeStringForCapabilities(datetimes));

        List<DateTime> secondPeriod = new ArrayList<DateTime>();
        for (int i = 1; i < 5; i++)
            secondPeriod.add(start.plusDays(20 * i));
        datetimes.addAll(secondPeriod);
        String twentyDayPeriod = TimeUtils.getPeriodString(20L * period);
        expected = expected + "," + TimeUtils.dateTimeToISO8601(secondPeriod.get(0)) + "/"
                + TimeUtils.dateTimeToISO8601(secondPeriod.get(secondPeriod.size() - 1)) + "/"
                + twentyDayPeriod;
        assertEquals(expected, TimeUtils.getTimeStringForCapabilities(datetimes));
        datetimes.add(start.plusDays(180));
        expected = expected + ","
                + TimeUtils.dateTimeToISO8601(datetimes.get(datetimes.size() - 1));
        assertEquals(expected, TimeUtils.getTimeStringForCapabilities(datetimes));

        List<DateTime> emptyList = new ArrayList<DateTime>();
        assertEquals("", TimeUtils.getTimeStringForCapabilities(emptyList));
    }

    /**
     * Test the method of {@link TimeUtils#onSameDay}.
     */
    @Test
    public void testOnSameDay() {
        DateTime dt1;
        dt1 = new DateTime(2013, 2, 27, 14, 34, DateTimeZone.UTC);
        DateTime dt2;
        dt2 = new DateTime(2013, 2, 28, 3, 34, DateTimeZone.forOffsetHours(4));
        assertTrue(TimeUtils.onSameDay(dt1, dt2));

        dt2 = new DateTime(2013, 2, 27, 9, 34, DateTimeZone.forOffsetHours(4));
        assertTrue(TimeUtils.onSameDay(dt1, dt2));

        dt2 = new DateTime(2013, 2, 27, 23, 34, DateTimeZone.forOffsetHours(-2));
        assertFalse(TimeUtils.onSameDay(dt1, dt2));

        dt1 = new DateTime(2013, 2, 28, 14, 34);
        dt2 = new DateTime(2013, 2, 28, 10, 34);
        assertTrue(TimeUtils.onSameDay(dt1, dt2));

        dt1 = new DateTime(2013, 2, 25, 14, 34);
        dt2 = new DateTime(2013, 2, 28, 10, 34);
        assertFalse(TimeUtils.onSameDay(dt1, dt2));
    }

    /**
     * Test the method of {@link TimeUtils#findTimeIndex}.
     */
    @Test
    public void testFindTimeIndex() {
        DateTime dt = new DateTime(1999, 12, 1, 0, 0, DateTimeZone.UTC);
        int index = TimeUtils.findTimeIndex(datetimes, dt);
        assertEquals(index, -1);

        dt = new DateTime(2000, 1, 4, 10, 0, DateTimeZone.UTC);
        index = TimeUtils.findTimeIndex(datetimes, dt);
        assertEquals(index, -5);

        dt = new DateTime(2000, 1, 19, 10, 0, DateTimeZone.UTC);
        index = TimeUtils.findTimeIndex(datetimes, dt);
        assertEquals(index, -11);

        dt = new DateTime(2000, 1, 4, 0, 0, DateTimeZone.UTC);
        index = TimeUtils.findTimeIndex(datetimes, dt);
        assertEquals(index, 3);
    }

    /**
     * Test the method of {@link TimeUtils#getTimeRangeForString}.
     * 
     * @throws BadTimeFormatException
     *             If any of the individual times are incorrectly formatted for
     *             ISO8601
     */
    @Test
    public void testGetTimeRangeForString() throws BadTimeFormatException {
        String timeString = "1990-12-30T12:00:00.000Z,1993-02-15T12:00:00.000Z,"
                + "1998-08-30T12:00:00.000Z,2000-01-01T13:00:00.000Z";
        Chronology isoChronology = ISOChronology.getInstanceUTC();
        DateTime start = TimeUtils.iso8601ToDateTime("1990-12-30T12:00:00.000Z", isoChronology);
        DateTime end = TimeUtils.iso8601ToDateTime("2000-01-01T13:00:00.000Z", isoChronology);
        assertEquals(TimeUtils.getTimeRangeForString(timeString, isoChronology),
                Extents.newExtent(start, end));
        Extent<DateTime> tExent = Extents.newExtent((new DateTime(1990, 12, 30, 12, 0,
                isoChronology)), new DateTime(2000, 01, 01, 13, 0, isoChronology));
        assertEquals(TimeUtils.getTimeRangeForString(timeString, ISOChronology.getInstanceUTC()),
                tExent);

        DateTime dt1 = new DateTime(1990, 2, 3, 14, 25, DateTimeZone.forOffsetHours(-2));
        DateTime dt2 = new DateTime(1995, 12, 3, 23, 25, DateTimeZone.forOffsetHours(-4));
        DateTime dt3 = new DateTime(2010, 1, 31, 12, 25, DateTimeZone.forOffsetHours(8));

        String ts1 = TimeUtils.dateTimeToISO8601(dt1);
        String ts2 = TimeUtils.dateTimeToISO8601(dt2);
        String ts3 = TimeUtils.dateTimeToISO8601(dt3);
        timeString = ts1 + "," + ts2 + "," + ts3;

        assertEquals(
                Extents.newExtent(dt1.toDateTime(DateTimeZone.UTC),
                        dt3.toDateTime(DateTimeZone.UTC)),
                TimeUtils.getTimeRangeForString(timeString, isoChronology));
    }
}
