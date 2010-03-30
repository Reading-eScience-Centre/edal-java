package org.edal.util;

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



import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test for the {@link ThreeSixtyDayChronology}.
 * @author Jon
 */
public final class ThreeSixtyDayChronologyTest {

    private static Chronology CHRON_360 = ThreeSixtyDayChronology.getInstanceUTC();

    private static DateTimeFormatter FORMATTER = ISODateTimeFormat.dateTime()
            .withChronology(CHRON_360)
            .withZone(DateTimeZone.UTC);

    private static final DateTime SAMPLE = new DateTime(2000, 1, 2, 3, 4, 5, 6, CHRON_360);

    private static final long SECOND = 1000;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR   = 60 * MINUTE;
    private static final long DAY    = 24 * HOUR;
    private static final long MONTH  = 30 * DAY;
    private static final long YEAR   = 12 * MONTH;

    /**
     * Test of zero millisecond offset (1970-01-01)
     */
    @Test
    public void test1970() {
        System.out.println("1970");
        testDateTime(1970, 1, 1, 0, 0, 0, 0);
    }

    /**
     * Test of one year's worth of millisecond offset (1971-01-01)
     */
    @Test
    public void test1971() {
        System.out.println("1971");
        testDateTime(1971, 1, 1, 0, 0, 0, 0);
    }

    @Test
    public void test1969() {
        System.out.println("1969");
        testDateTime(1969, 1, 1, 0, 0, 0, 0);
    }

    @Test
    public void test1969AndAMillisecond() {
        System.out.println("1969 + 1ms");
        testDateTime(1969, 1, 1, 0, 0, 0, 1);
    }

    @Test
    public void testFeb30_2000() {
        System.out.println("Feb 30, 2000");
        testDateTime(2000, 2, 30, 0, 0, 0, 0);
    }

    @Test
    public void testZeroYear() {
        System.out.println("1st Jan, 0000");
        testDateTime(0, 1, 1, 0, 0, 0, 0);
    }

    @Test
    public void testNegativeYear() {
        System.out.println("Feb 30, -1");
        testDateTime(-1, 2, 30, 0, 0, 0, 0);
    }

    @Test
    public void testVeryNegativeYear() {
        System.out.println("Feb 30, -2000");
        testDateTime(-2000, 2, 30, 0, 0, 0, 0);
    }

    @Test
    public void testDateTime() {
        System.out.println("Feb 30, 2000, 13:45:56.789");
        testDateTime(2000, 2, 30, 13, 45, 56, 789);
    }

    @Test(expected=IllegalFieldValueException.class)
    public void testMillisOverflow() {
        SAMPLE.withMillisOfSecond(1000);
    }

    @Test(expected=IllegalFieldValueException.class)
    public void testMillisUnderflow() {
        SAMPLE.withMillisOfSecond(-1);
    }

    @Test(expected=IllegalFieldValueException.class)
    public void testSecondsOverflow() {
        SAMPLE.withSecondOfMinute(60);
    }

    @Test(expected=IllegalFieldValueException.class)
    public void testSecondsUnderflow() {
        SAMPLE.withSecondOfMinute(-1);
    }

    @Test(expected=IllegalFieldValueException.class)
    public void testMinutesOverflow() {
        SAMPLE.withMinuteOfHour(60);
    }

    @Test(expected=IllegalFieldValueException.class)
    public void testMinutesUnderflow() {
        SAMPLE.withMinuteOfHour(-1);
    }

    @Test(expected=IllegalFieldValueException.class)
    public void testHoursOverflow() {
        SAMPLE.withHourOfDay(24);
    }

    @Test(expected=IllegalFieldValueException.class)
    public void testHoursUnderflow() {
        SAMPLE.withHourOfDay(-1);
    }

    @Test(expected=IllegalFieldValueException.class)
    public void testDayOfMonthOverflow() {
        SAMPLE.withMonthOfYear(2).withDayOfMonth(31);
    }

    @Test(expected=IllegalFieldValueException.class)
    public void testDayOfMonthOverflow2() {
        // Pick a month that usually has 31 days
        SAMPLE.withMonthOfYear(1).withDayOfMonth(31);
    }

    @Test(expected=IllegalFieldValueException.class)
    public void testDayOfMonthUnderflow() {
        SAMPLE.withDayOfMonth(0);
    }

    @Test(expected=IllegalFieldValueException.class)
    public void testMonthOfYearOverflow() {
        SAMPLE.withMonthOfYear(13);
    }

    @Test(expected=IllegalFieldValueException.class)
    public void testMonthOfYearUnderflow() {
        SAMPLE.withMonthOfYear(0);
    }

    @Test
    public void setFields() {
        int year = 1;
        int month = 12;
        int day = 30;
        int hour = 23;
        int minute = 59;
        int second = 58;
        int millis = 999;
        assertEquals(year, SAMPLE.withYear(year).getYear());
        assertEquals(month, SAMPLE.withMonthOfYear(month).getMonthOfYear());
        assertEquals(day, SAMPLE.withDayOfMonth(day).getDayOfMonth());
        assertEquals(hour, SAMPLE.withHourOfDay(hour).getHourOfDay());
        assertEquals(minute, SAMPLE.withMinuteOfHour(minute).getMinuteOfHour());
        assertEquals(second, SAMPLE.withSecondOfMinute(second).getSecondOfMinute());
        assertEquals(millis, SAMPLE.withMillisOfSecond(millis).getMillisOfSecond());
        assertEquals(millis, SAMPLE.withMillisOfDay(millis).getMillisOfDay());
    }

    @Test
    public void testArithmetic() {
        long millis = SAMPLE.getMillis();
        assertEquals(millis + 4 * YEAR, SAMPLE.year().addToCopy(4).getMillis());
        assertEquals(millis + 4 * MONTH, SAMPLE.monthOfYear().addToCopy(4).getMillis());
        assertEquals(millis + 4 * DAY, SAMPLE.dayOfMonth().addToCopy(4).getMillis());
        assertEquals(millis + 4 * HOUR, SAMPLE.hourOfDay().addToCopy(4).getMillis());
        assertEquals(millis + 4 * MINUTE, SAMPLE.minuteOfHour().addToCopy(4).getMillis());
        assertEquals(millis + 4 * SECOND, SAMPLE.secondOfMinute().addToCopy(4).getMillis());
        assertEquals(millis + 4 , SAMPLE.millisOfSecond().addToCopy(4).getMillis());

        DateTime yearOne = SAMPLE.withYear(1);
        millis = yearOne.getMillis();
        assertEquals(millis - 4 * YEAR, yearOne.year().addToCopy(-4).getMillis());
        System.out.println(yearOne);

    }
    
    /** Performs a round-trip test on creating DateTimes in the Chronology */
    private static final DateTime testDateTime(int year, int monthOfYear, int dayOfMonth,
        int hourOfDay, int minuteOfHour, int secondOfMinute, int millisOfSecond)
    {
        // Create a DateTime from the given fields and check that the millisecond
        // offset is correct
        long millis = getMillis(year, monthOfYear, dayOfMonth, hourOfDay,
            minuteOfHour, secondOfMinute, millisOfSecond);
        DateTime dt = new DateTime(year, monthOfYear, dayOfMonth, hourOfDay,
            minuteOfHour, secondOfMinute, millisOfSecond, CHRON_360);
        System.out.println("year = " + dt.getYear());
        assertEquals(millis, dt.getMillis());
        
        // Now create a new DateTime from the millisecond offset and check that
        // the fields are correct
        dt = new DateTime(millis, CHRON_360);
        assertEquals(year,           dt.getYear());
        assertEquals(monthOfYear,    dt.getMonthOfYear());
        assertEquals(dayOfMonth,     dt.getDayOfMonth());
        assertEquals(hourOfDay,      dt.getHourOfDay());
        assertEquals(minuteOfHour,   dt.getMinuteOfHour());
        assertEquals(secondOfMinute, dt.getSecondOfMinute());
        assertEquals(millisOfSecond, dt.getMillisOfSecond());

        int dayOfYear = (monthOfYear - 1) * 30 + dayOfMonth;
        int minuteOfDay = hourOfDay * 60 + minuteOfHour;
        int secondOfDay = minuteOfDay * 60 + secondOfMinute;
        int millisOfDay = secondOfDay * 1000 + millisOfSecond;
        
        assertEquals(dayOfYear,   dt.getDayOfYear());
        assertEquals(minuteOfDay, dt.getMinuteOfDay());
        assertEquals(secondOfDay, dt.getSecondOfDay());
        assertEquals(millisOfDay, dt.getMillisOfDay());

        // Do a round-trip format and parse
        String isoString = FORMATTER.print(millis);
        System.out.println(isoString);
        long parsedMillis = FORMATTER.parseMillis(isoString);
        assertEquals(millis, parsedMillis);

        return dt;
    }

    /** Gets the millisecond offset since 1970-01-01, given a TestDateTime */
    private static long getMillis(int year, int monthOfYear, int dayOfMonth,
            int hourOfDay, int minuteOfHour, int secondOfMinute, int millisOfSecond)
    {
        return (year - 1970)     * YEAR   +
               (monthOfYear - 1) * MONTH  +
               (dayOfMonth - 1)  * DAY    +
               hourOfDay         * HOUR   +
               minuteOfHour      * MINUTE +
               secondOfMinute    * SECOND +
               millisOfSecond;
    }

}