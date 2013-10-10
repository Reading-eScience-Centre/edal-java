/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.ISODateTimeFormat;

import uk.ac.rdg.resc.edal.domain.Extent;

/**
 * <p>
 * Collection of static utility methods that are useful for dealing with
 * time-related types.
 * </p>
 * 
 * @author Guy
 * @author Jon Blower
 */
public class TimeUtils {
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime()
            .withZone(DateTimeZone.UTC);
    private static final DateTimeFormatter ISO_DATE_FORMATTER = ISODateTimeFormat.date().withZone(
            DateTimeZone.UTC);
    private static final DateTimeFormatter ISO_TIME_FORMATTER = ISODateTimeFormat.time().withZone(
            DateTimeZone.UTC);
    private static final DateTimeFormatter NICE_DATE_TIME_FORMATTER = (new DateTimeFormatterBuilder())
            .appendHourOfDay(2).appendLiteral(":").appendMinuteOfHour(2).appendLiteral(":")
            .appendSecondOfMinute(2).appendLiteral(" ").appendDayOfMonth(1).appendLiteral("-")
            .appendMonthOfYearShortText().appendLiteral("-").appendYear(4, 4).toFormatter()
            .withZone(DateTimeZone.UTC);
    private static final DateTimeFormatter UTC_TIME_FORMATTER = (new DateTimeFormatterBuilder())
            .appendHourOfDay(2).appendLiteral(":").appendMinuteOfHour(2).appendLiteral(":")
            .appendSecondOfMinute(2).toFormatter().withZone(DateTimeZone.UTC);

    /**
     * <p>
     * A {@link Comparator} that compares {@link DateTime} objects based only on
     * their millisecond instant values. This can be used for
     * {@link Collections#sort(java.util.List, java.util.Comparator) sorting} or
     * {@link Collections#binarySearch(java.util.List, java.lang.Object, java.util.Comparator)
     * searching} {@link List}s of {@link DateTime} objects.
     * </p>
     * <p>
     * The ordering defined by this Comparator is <i>inconsistent with
     * equals</i> because it ignores the Chronology of the DateTime instants.
     * </p>
     * <p>
     * <i>(Note: The DateTime object inherits from Comparable, not
     * Comparable&lt;DateTime&gt;, so we can't use the methods in Collections
     * directly. However we can reuse the
     * {@link DateTime#compareTo(java.lang.Object)} method.)</i>
     * </p>
     */
    public static final Comparator<DateTime> TIME_POSITION_COMPARATOR = new Comparator<DateTime>() {
        @Override
        public int compare(DateTime dt1, DateTime dt2) {
            return dt1.compareTo(dt2);
        }
    };

    private static long MILLIS_PER_SECOND = 1000L;
    private static long MILLIS_PER_MINUTE = 60L * MILLIS_PER_SECOND;
    private static long MILLIS_PER_HOUR = 60L * MILLIS_PER_MINUTE;
    private static long MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR;

    /** Private constructor to prevent direct instantiation */
    private TimeUtils() {
        throw new AssertionError();
    }

    /**
     * Converts a {@link DateTime} object into an ISO8601-formatted String.
     */
    public static String dateTimeToISO8601(DateTime dateTime) {
        if (dateTime == null)
            return "";
        return ISO_DATE_TIME_FORMATTER.print(dateTime);
    }

    /**
     * Converts an ISO8601-formatted String into a {@link DateTime} object
     * 
     * @throws ParseException
     * 
     * @throws IllegalArgumentException
     *             if the string is not a valid ISO date-time, or if it is not
     *             valid within the Chronology (e.g. 31st July in a 360-day
     *             calendar).
     */
    public static DateTime iso8601ToDateTime(String isoDateTime, Chronology chronology)
            throws ParseException {
        return ISO_DATE_TIME_FORMATTER.withChronology(chronology).parseDateTime(isoDateTime);
    }

    public static DateTime iso8601ToDate(String isoDate, Chronology chronology)
            throws ParseException {
        return ISO_DATE_FORMATTER.withChronology(chronology).parseDateTime(isoDate);
    }

    /**
     * Searches the given list of timesteps for the specified date-time using
     * the binary search algorithm. Matches are found based only upon the
     * millisecond instant of the target DateTime, not its Chronology.
     * 
     * @param target
     *            The timestep to search for.
     * @return the index of the search key, if it is contained in the list;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>. The
     *         <i>insertion point</i> is defined as the point at which the key
     *         would be inserted into the list: the index of the first element
     *         greater than the key, or <tt>list.size()</tt> if all elements in
     *         the list are less than the specified key. Note that this
     *         guarantees that the return value will be &gt;= 0 if and only if
     *         the key is found. If this Layer does not have a time axis this
     *         method will return -1.
     */
    public static int findTimeIndex(List<DateTime> dtList, DateTime target) {
        return Collections.binarySearch(dtList, target, TIME_POSITION_COMPARATOR);
    }

    /**
     * Formats a DateTime as the time only in ISO format Time zone offset is
     * zero (UTC).
     */
    public static String formatUtcIsoTimeOnly(DateTime dateTime) {
        return ISO_TIME_FORMATTER.print(dateTime);
    }

    /**
     * Formats a DateTime as the time only in the format "HH:mm:ss", e.g.
     * "14:53:03". Time zone offset is zero (UTC).
     */
    public static String formatUtcTimeOnly(DateTime dateTime) {
        return UTC_TIME_FORMATTER.print(dateTime);
    }

    /**
     * Formats a DateTime in a nice human-readable manner.
     */
    public static String formatUTCHumanReadableDateTime(DateTime dateTime) {
        return NICE_DATE_TIME_FORMATTER.print(dateTime);
    }

    /**
     * Formats a DateTime as the time only in the format "HH:mm:ss", e.g.
     * "14:53:03". Time zone offset is zero (UTC).
     */
    public static String formatUTCDateOnly(DateTime dateTime) {
        return ISO_DATE_FORMATTER.print(dateTime);
    }

    /**
     * <p>
     * Returns a string representing the given List of DateTimes, suitable for
     * inclusion in a Capabilities document. For regularly-spaced data, this
     * will return a string of the form "start/stop/period". For
     * irregularly-spaced data this will return the times as a comma-separated
     * list. For lists with some regular and some irregular spacings, this will
     * use a combination of both approaches.
     * </p>
     * <p>
     * All DateTimes in the provided list are assumed to be in the same
     * {@link Chronology} as the first element of the list. If this is not the
     * case, undefined behaviour may result.
     * </p>
     * 
     * @param times
     *            The List of DateTimes to convert to a String. If this List
     *            contains no entries, an empty string will be returned.
     * @return a string representing the given List of DateTimes, suitable for
     *         inclusion in a Capabilities document.
     * @throws NullPointerException
     *             if {@code times == null}
     */
    public static String getTimeStringForCapabilities(List<DateTime> times) {
        // Take care of some simple cases
        if (times == null)
            throw new NullPointerException();
        if (times.size() == 0)
            return "";
        if (times.size() == 1)
            return dateTimeToISO8601(times.get(0));

        /*
         * We look for sublists that are regularly-spaced This is a simple class
         * that holds the indices of the start and end of these sublists,
         * together with the spacing of the items
         */
        class SubList {
            int first, last;
            long spacing;

            int length() {
                return last - first + 1;
            }
        }

        List<SubList> subLists = new ArrayList<SubList>();
        SubList currentSubList = new SubList();
        currentSubList.first = 0;
        currentSubList.spacing = times.get(1).getMillis() - times.get(0).getMillis();

        for (int i = 1; i < times.size() - 1; i++) {
            long spacing = times.get(i + 1).getMillis() - times.get(i).getMillis();
            if (spacing != currentSubList.spacing) {
                /* Finish off the current sublist and add it to the collection */
                currentSubList.last = i;
                subLists.add(currentSubList);
                /* Create a new sublist, starting at this point */
                currentSubList = new SubList();
                currentSubList.first = i;
                currentSubList.spacing = spacing;
            }
        }

        /* Now add the last time */
        currentSubList.last = times.size() - 1;
        subLists.add(currentSubList);

        /*
         * We now have a collection of sub-lists, each regularly spaced in time.
         * However, we can't simply print these to strings because the some of
         * the times (those on the borders between sublists) would appear twice.
         * For these border times, we need to decide which sublist they belong
         * to. We choose this by attempting to make the longest sublist
         * possible, until there are no more border times to assign.
         */

        /*
         * We must make sure not to deal with the same sublist repeatedly, so we
         * store the indices of the sublists we have dealt with.
         */
        Set<Integer> subListsDone = new HashSet<Integer>(subLists.size());
        boolean done;
        do {
            /* First we find the longest sublist */
            int longestSubListIndex = -1;
            SubList longestSubList = null;
            for (int i = 0; i < subLists.size(); i++) {
                /* Don't look at sublists we've already dealt with */
                if (subListsDone.contains(i))
                    continue;
                SubList subList = subLists.get(i);
                if (longestSubList == null || subList.length() > longestSubList.length()) {
                    longestSubListIndex = i;
                    longestSubList = subList;
                }
            }
            subListsDone.add(longestSubListIndex);

            /*
             * Now we remove the DateTimes at the borders of this sublist from
             * the adjacent sublists. Therefore the longest sublist "claims" the
             * borders from its neighbours.
             */
            if (longestSubListIndex > 0) {
                /* Check the previous sublist */
                SubList prevSubList = subLists.get(longestSubListIndex - 1);
                if (prevSubList.last == longestSubList.first) {
                    prevSubList.last--;
                }
            }
            if (longestSubListIndex < subLists.size() - 1) {
                /* Check the next sublist */
                SubList nextSubList = subLists.get(longestSubListIndex + 1);
                if (nextSubList.first == longestSubList.last) {
                    nextSubList.first++;
                }
            }

            /* Check to see if there are any borders that appear in two sublists */
            done = true;
            for (int i = 1; i < subLists.size() - 1; i++) {
                SubList prev = subLists.get(i - 1);
                SubList cur = subLists.get(i);
                SubList next = subLists.get(i + 1);
                if (prev.last == cur.first || cur.last == next.first) {
                    /* We still have a contested border */
                    done = false;
                    break;
                }
            }
        } while (!done);

        /* Now we can simply print out our sublists, comma-separated */
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < subLists.size(); i++) {
            SubList subList = subLists.get(i);
            List<DateTime> timeList = times.subList(subList.first, subList.last + 1);
            if (timeList.size() > 0) {
                if (i > 0)
                    str.append(",");
                str.append(getRegularlySpacedTimeString(timeList, subList.spacing));
            }
        }

        return str.toString();
    }

    /**
     * <p>
     * Creates a time string, suitable for a Capabilities document, that
     * represents the given list of DateTimes, which have previously been
     * calculated to be regularly-spaced according to the given period.
     * </p>
     * <p>
     * <i>"Package-private" rather than "private" to enable visibility in unit
     * tests</i>
     * </p>
     * 
     * @param times
     *            The list of DateTimes, which have been previously calculated
     *            to be regularly-spaced. This can have one or more elements; if
     *            it only has one or two elements then the {@code period} is
     *            ignored.
     * @param period
     *            The interval between the dateTimes, in milliseconds
     * @throws IllegalArgumentException
     *             if {@code times} has zero elements.
     */
    static StringBuilder getRegularlySpacedTimeString(List<DateTime> times, long period) {
        if (times.size() == 0)
            throw new IllegalArgumentException();
        StringBuilder str = new StringBuilder();
        str.append(dateTimeToISO8601(times.get(0)));
        if (times.size() == 2) {
            /* No point in specifying the interval, just write the two times */
            str.append(",");
            str.append(dateTimeToISO8601(times.get(1)));
        } else if (times.size() > 2) {
            str.append("/");
            str.append(dateTimeToISO8601(times.get(times.size() - 1)));
            str.append("/");
            str.append(getPeriodString(period));
        }
        return str;
    }

    /**
     * <p>
     * Gets a representation of the given period as an ISO8601 string, e.g.
     * "P1D" for one day, "PT3.5S" for 3.5s.
     * </p>
     * <p>
     * For safety, this will only express periods in days, hours, minutes and
     * seconds. These are the only durations that are constant in their
     * millisecond length across different Chronologies. (Years and months can
     * be different lengths between and within Chronologies.)
     * </p>
     * 
     * @param period
     *            The period in milliseconds
     * @return a representation of the given period as an ISO8601 string
     */
    public static String getPeriodString(long period) {
        StringBuilder str = new StringBuilder("P");
        long days = period / MILLIS_PER_DAY;
        if (days > 0) {
            str.append(days + "D");
            period -= days * MILLIS_PER_DAY;
        }
        if (period > 0)
            str.append("T");
        long hours = period / MILLIS_PER_HOUR;
        if (hours > 0) {
            str.append(hours + "H");
            period -= hours * MILLIS_PER_HOUR;
        }
        long minutes = period / MILLIS_PER_MINUTE;
        if (minutes > 0) {
            str.append(minutes + "M");
            period -= minutes * MILLIS_PER_MINUTE;
        }
        /* Now the period represents the number of milliseconds */
        if (period > 0) {
            long seconds = period / MILLIS_PER_SECOND;
            long millis = period % MILLIS_PER_SECOND;
            str.append(seconds);
            if (millis > 0)
                str.append("." + addOrRemoveZeros(millis));
            str.append("S");
        }

        return str.toString();
    }

    /**
     * Adds leading zeros and removes trailing zeros as appropriate, to make the
     * given number of milliseconds suitable for placing after a decimal point
     * (e.g. 500 becomes 5, 5 becomes 005).
     */
    private static String addOrRemoveZeros(long millis) {
        if (millis == 0)
            return "";
        String s = Long.toString(millis);
        if (millis < 10)
            return "00" + s;

        if (millis < 100)
            s = "0" + s;
        /* Now remove all trailing zeros */
        while (s.endsWith("0")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public static Extent<DateTime> getTimeRangeForString(String timeString, Chronology chronology)
            throws ParseException {
        if (timeString == null) {
            return null;
        }
        List<DateTime> times = new ArrayList<DateTime>();
        String[] elements = timeString.split(",");
        for (String element : elements) {
            String[] startStop = element.split("/");
            for (String time : startStop) {
                if (time.equalsIgnoreCase("current")) {
                    times.add(new DateTime());
                } else {
                    times.add(iso8601ToDateTime(time, chronology));
                }
            }
        }
        return Extents.findMinMax(times);
    }

    /**
     * @return true if the two given DateTimes fall on the same day.
     */
    public static boolean onSameDay(DateTime dt1, DateTime dt2) {
        /*
         * We must make sure that the DateTimes are both in UTC or the field
         * comparisons will not do what we expect
         */
        dt1 = dt1.withZone(DateTimeZone.UTC);
        dt2 = dt2.withZone(DateTimeZone.UTC);
        boolean onSameDay = dt1.getYear() == dt2.getYear()
                && dt1.getMonthOfYear() == dt2.getMonthOfYear()
                && dt1.getDayOfMonth() == dt2.getDayOfMonth();
        return onSameDay;
    }
}
