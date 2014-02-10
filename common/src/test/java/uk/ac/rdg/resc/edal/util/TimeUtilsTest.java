package uk.ac.rdg.resc.edal.util;

import static org.junit.Assert.*;

import org.joda.time.*;

import uk.ac.rdg.resc.edal.util.TimeUtils;
import uk.ac.rdg.resc.edal.util.chronologies.AllLeapChronology;
import uk.ac.rdg.resc.edal.exceptions.BadTimeFormatException;
import java.util.*;
import org.joda.time.chrono.*;
import org.junit.Before;
import org.junit.Test;

public class TimeUtilsTest {
    private DateTime start = new DateTime(2000, 1, 1, 0, 0, DateTimeZone.UTC);
    private List<DateTime> datetimes = new ArrayList<DateTime>();
    private final static long MILLIS_PER_SECOND = 1000L;
    private final static long MILLIS_PER_MINUTE = 60L * MILLIS_PER_SECOND;
    private final static long MILLIS_PER_HOUR = 60L * MILLIS_PER_MINUTE;
    private final static long MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR;

    @Before
    public void setUp() throws Exception {
        for (int i = 0; i < 10; i++)
            datetimes.add(start.plusDays(i));
    }

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

    @Test
    public void testGetTimeRangeForString() throws BadTimeFormatException {
        String timeString = "1990-12-30T12:00:00.000Z,1993-02-15T12:00:00.000Z,"
                + "1998-08-30T12:00:00.000Z,2000-01-01T13:00:00.000Z";
        Chronology isoChronology = ISOChronology.getInstanceUTC();
        DateTime start = TimeUtils.iso8601ToDateTime("1990-12-30T12:00:00.000Z", isoChronology);
        DateTime end = TimeUtils.iso8601ToDateTime("2000-01-01T13:00:00.000Z", isoChronology);
        assertEquals(Extents.newExtent(start, end),
                TimeUtils.getTimeRangeForString(timeString, ISOChronology.getInstanceUTC()));
    }

}
