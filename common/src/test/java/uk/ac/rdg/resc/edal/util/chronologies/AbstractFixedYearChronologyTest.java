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

package uk.ac.rdg.resc.edal.util.chronologies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeZone;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Abstract superclass for testing {@link FixedYearLengthChronology}s.
 * 
 * @author Jon
 */
@Ignore
public abstract class AbstractFixedYearChronologyTest {
    private final Chronology chron;
    private final DateTimeFormatter formatter;
    protected final DateTime sample;

    /** Number of milliseconds in a second */
    protected static final long SECOND = 1000;
    /** Number of milliseconds in a minute */
    protected static final long MINUTE = 60 * SECOND;
    /** Number of milliseconds in an hour */
    protected static final long HOUR = 60 * MINUTE;
    /** Number of milliseconds in a day */
    protected static final long DAY = 24 * HOUR;

    public AbstractFixedYearChronologyTest(Chronology chron) {
        this.chron = chron;
        this.formatter = ISODateTimeFormat.dateTime().withChronology(chron)
                .withZone(DateTimeZone.UTC);
        this.sample = new DateTime(2000, 1, 2, 3, 4, 5, 6, chron);
    }

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
    public void testZeroYear() {
        System.out.println("1st Jan, 0000");
        testDateTime(0, 1, 1, 0, 0, 0, 0);
    }

    @Test
    public void testNegativeYear() {
        System.out.println("Jan 30, -1");
        testDateTime(-1, 1, 30, 0, 0, 0, 0);
    }

    @Test
    public void testVeryNegativeYear() {
        System.out.println("Jan 30, -2000");
        testDateTime(-2000, 1, 30, 0, 0, 0, 0);
    }

    @Test
    public void testDateTime() {
        System.out.println("Jan 30, 2000, 13:45:56.789");
        testDateTime(2000, 1, 30, 13, 45, 56, 789);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testMillisOverflow() {
        sample.withMillisOfSecond(1000);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testMillisUnderflow() {
        sample.withMillisOfSecond(-1);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testSecondsOverflow() {
        sample.withSecondOfMinute(60);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testSecondsUnderflow() {
        sample.withSecondOfMinute(-1);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testMinutesOverflow() {
        sample.withMinuteOfHour(60);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testMinutesUnderflow() {
        sample.withMinuteOfHour(-1);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testHoursOverflow() {
        sample.withHourOfDay(24);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testHoursUnderflow() {
        sample.withHourOfDay(-1);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testDayOfMonthUnderflow() {
        sample.withDayOfMonth(0);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testMonthOfYearOverflow() {
        sample.withMonthOfYear(13);
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testMonthOfYearUnderflow() {
        sample.withMonthOfYear(0);
    }

    /** Tests the monthOfYear DateTimeField */
    @Test
    public void testMonthOfYearField() {
        DateTimeField monthField = this.chron.monthOfYear();
        assertEquals(1, monthField.getMinimumValue());
        assertEquals(this.getNumMonthsInYear(), monthField.getMaximumValue());
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
        assertEquals(year, sample.withYear(year).getYear());
        assertEquals(month, sample.withMonthOfYear(month).getMonthOfYear());
        assertEquals(day, sample.withDayOfMonth(day).getDayOfMonth());
        assertEquals(hour, sample.withHourOfDay(hour).getHourOfDay());
        assertEquals(minute, sample.withMinuteOfHour(minute).getMinuteOfHour());
        assertEquals(second, sample.withSecondOfMinute(second).getSecondOfMinute());
        assertEquals(millis, sample.withMillisOfSecond(millis).getMillisOfSecond());
        assertEquals(millis, sample.withMillisOfDay(millis).getMillisOfDay());
    }

    // Tests arithmetic on the day, hour, minute, second and millisecond fields
    @Test
    public void testArithmetic() {
        long millis = sample.getMillis();
        assertEquals(millis + 4 * DAY, sample.dayOfMonth().addToCopy(4).getMillis());
        assertEquals(millis + 4 * HOUR, sample.hourOfDay().addToCopy(4).getMillis());
        assertEquals(millis + 4 * MINUTE, sample.minuteOfHour().addToCopy(4).getMillis());
        assertEquals(millis + 4 * SECOND, sample.secondOfMinute().addToCopy(4).getMillis());
        assertEquals(millis + 4, sample.millisOfSecond().addToCopy(4).getMillis());

        assertEquals(millis - 4 * DAY, sample.dayOfMonth().addToCopy(-4).getMillis());
        assertEquals(millis - 4 * HOUR, sample.hourOfDay().addToCopy(-4).getMillis());
        assertEquals(millis - 4 * MINUTE, sample.minuteOfHour().addToCopy(-4).getMillis());
        assertEquals(millis - 4 * SECOND, sample.secondOfMinute().addToCopy(-4).getMillis());
        assertEquals(millis - 4, sample.millisOfSecond().addToCopy(-4).getMillis());
    }

    @Test
    public void testMonthArithmetic() {
        DateTime dt = new DateTime(2000, 10, 1, 0, 0, 0, 0, this.chron);
        dt = dt.plusMonths(4);
        assertEquals(2001, dt.getYear());
        assertEquals(2, dt.getMonthOfYear());
        assertEquals(1, dt.getDayOfMonth());
        long expected = new DateTime(2001, 2, 1, 0, 0, 0, 0, this.chron).getMillis();
        assertEquals(expected, dt.getMillis());

        dt = new DateTime(2000, 10, 1, 0, 0, 0, 0, this.chron);
        dt = dt.plusMonths(-1);
        System.out.println(dt);
        assertEquals(2000, dt.getYear());
        assertEquals(9, dt.getMonthOfYear());
        assertEquals(1, dt.getDayOfMonth());
        expected = new DateTime(2000, 9, 1, 0, 0, 0, 0, this.chron).getMillis();

        dt = new DateTime(2000, 10, 1, 0, 0, 0, 0, this.chron);
        dt = dt.plusMonths(-11);
        assertEquals(1999, dt.getYear());
        assertEquals(11, dt.getMonthOfYear());
        assertEquals(1, dt.getDayOfMonth());
        expected = new DateTime(1999, 11, 1, 0, 0, 0, 0, this.chron).getMillis();
        assertEquals(expected, dt.getMillis());
    }

    // Tests overflow of all the possible months
    @Test
    public void testDayOfMonthOverflows() {
        for (int i = 1; i <= this.getNumMonthsInYear(); i++) {
            // First check a valid value
            sample.withMonthOfYear(i).withDayOfMonth(this.getNumDaysInMonth(i));
            boolean exThrown = false;
            try {
                sample.withMonthOfYear(i).withDayOfMonth(this.getNumDaysInMonth(i) + 1);
            } catch (IllegalFieldValueException ifve) {
                exThrown = true;
            }
            assertTrue(exThrown);
        }
    }

    /**
     * Creates a DateTime from the given fields and checks that the field values
     * are preserved. Independently checks the calculation of the millisecond
     * offset. Formats the resulting DateTime as an ISO String, then parses the
     * string, checking that the result matches the original DateTime.
     */
    protected final void testDateTime(int year, int monthOfYear, int dayOfMonth, int hourOfDay,
            int minuteOfHour, int secondOfMinute, int millisOfSecond) {
        // Create a DateTime from the given fields
        DateTime dt = new DateTime(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour,
                secondOfMinute, millisOfSecond, this.chron);
        long millis = this.getMillis(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour,
                secondOfMinute, millisOfSecond);
        assertEquals(millis, dt.getMillis());

        // Check that all the fields are the same
        assertEquals(year, dt.getYear());
        assertEquals(monthOfYear, dt.getMonthOfYear());
        assertEquals(dayOfMonth, dt.getDayOfMonth());
        assertEquals(hourOfDay, dt.getHourOfDay());
        assertEquals(minuteOfHour, dt.getMinuteOfHour());
        assertEquals(secondOfMinute, dt.getSecondOfMinute());
        assertEquals(millisOfSecond, dt.getMillisOfSecond());

        // Do a round-trip format and parse
        String isoString = this.formatter.print(dt);
        System.out.println(isoString);
        long parsedMillis = this.formatter.parseMillis(isoString);
        assertEquals(dt.getMillis(), parsedMillis);
    }

    /**
     * Provides an independent calculation of the number of milliseconds since
     * the datum given by these fields.
     */
    protected final long getMillis(int year, int monthOfYear, int dayOfMonth, int hourOfDay,
            int minuteOfHour, int secondOfMinute, int millisOfSecond) {
        int dayOfYear = this.getDayOfYear(monthOfYear, dayOfMonth);
        return (year - 1970) * this.getMillisInYear() + (dayOfYear - 1) * DAY + hourOfDay * HOUR
                + minuteOfHour * MINUTE + secondOfMinute * SECOND + millisOfSecond;
    }

    protected final long getMillisInYear() {
        return this.getNumDaysInYear() * DAY;
    }

    /**
     * Returns the number of days in the year in the chronology under test. This
     * must not use the Chronology itself to provide a figure: it must be
     * independent.
     */
    protected abstract int getNumDaysInYear();

    /**
     * Calculates the (one-based) day of the year from the given month of the
     * year and the day of the month (both one-based)
     */
    protected abstract int getDayOfYear(int monthOfYear, int dayOfMonth);

    protected abstract int getNumDaysInMonth(int monthOfYear);

    protected abstract int getNumMonthsInYear();

}
