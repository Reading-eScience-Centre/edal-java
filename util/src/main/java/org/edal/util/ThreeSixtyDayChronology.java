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

package org.edal.util;

import org.joda.time.Chronology;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.chrono.BaseChronology;
import org.joda.time.field.MillisDurationField;
import org.joda.time.field.PreciseDateTimeField;
import org.joda.time.field.PreciseDurationDateTimeField;
import org.joda.time.field.PreciseDurationField;
import org.joda.time.field.ZeroIsMaxDateTimeField;

/**
 * <p>A Chronology in which each year has exactly 360 days of 12 equal months
 * ({@literal i.e.} each month has exactly 30 days).  This calendar system is
 * used in many climate simulations.  There are no leap years.</p>
 * <p>In this Chronology, a millisecond instant of zero corresponds with
 * 1970-01-01T00:00:00.000Z and a year has a fixed number of milliseconds
 * (1000*60*60*24*360).</p>
 * <p>There is no concept of an era in this calendar, so all durations and fields
 * relating to this concept are not supported.  Additionally, the concept of a
 * "weekyear" (the year that "owns" a given week) is not implemented.</p>
 * <p>Instances of this class can only be created in {@link DateTimeZone#UTC}.
 * (Support for time zones makes little sense in this chronology).</p>
 * <p>Instances of this class are immutable.</p>
 * <p><i>Note: Much of this code was copied from the package-private
 * BasicChronology.</i></p>
 * @author Jon Blower
 * @see http://cf-pcmdi.llnl.gov/documents/cf-conventions/1.4/cf-conventions.html#calendar
 */
public final class ThreeSixtyDayChronology extends BaseChronology {

    ///// DURATIONS /////

    private static final DurationField millisecondDuration = MillisDurationField.INSTANCE;

    private static final DurationField secondDuration =
        new PreciseDurationField(DurationFieldType.seconds(), DateTimeConstants.MILLIS_PER_SECOND);

    private static final DurationField minuteDuration =
        new PreciseDurationField(DurationFieldType.minutes(), DateTimeConstants.MILLIS_PER_MINUTE);
    
    private static final DurationField hourDuration =
        new PreciseDurationField(DurationFieldType.hours(), DateTimeConstants.MILLIS_PER_HOUR);
    
    private static final DurationField halfdayDuration =
        new PreciseDurationField(DurationFieldType.halfdays(), 12 * DateTimeConstants.MILLIS_PER_HOUR);
    
    private static final DurationField dayDuration =
        new PreciseDurationField(DurationFieldType.days(), 2 * halfdayDuration.getUnitMillis());

    private static final DurationField weekDuration =
        new PreciseDurationField(DurationFieldType.weeks(), 7 * dayDuration.getUnitMillis());
    
    private static final DurationField monthDuration =
        new PreciseDurationField(DurationFieldType.months(), 30 * dayDuration.getUnitMillis());
    
    private static final DurationField yearDuration =
        new PreciseDurationField(DurationFieldType.years(), 12 * monthDuration.getUnitMillis());
    
    private static final DurationField centuryDuration =
        new PreciseDurationField(DurationFieldType.years(), 100 * yearDuration.getUnitMillis());


    ///// DATE-TIME FIELDS /////

    private static final DateTimeField millisOfSecond =
        new PreciseDateTimeField(DateTimeFieldType.millisOfSecond(), millisecondDuration, secondDuration);

    private static final DateTimeField millisOfDay =
        new PreciseDateTimeField(DateTimeFieldType.millisOfDay(), millisecondDuration, dayDuration);

    private static final DateTimeField secondOfMinute =
        new PreciseDateTimeField(DateTimeFieldType.secondOfMinute(), secondDuration, minuteDuration);

    private static final DateTimeField secondOfDay =
        new PreciseDateTimeField(DateTimeFieldType.secondOfDay(), secondDuration, dayDuration);

    private static final DateTimeField minuteOfHour =
        new PreciseDateTimeField(DateTimeFieldType.minuteOfHour(), minuteDuration, hourDuration);

    private static final DateTimeField minuteOfDay =
        new PreciseDateTimeField(DateTimeFieldType.minuteOfDay(), minuteDuration, dayDuration);

    private static final DateTimeField hourOfDay =
        new PreciseDateTimeField(DateTimeFieldType.hourOfDay(), hourDuration, dayDuration);

    private static final DateTimeField hourOfHalfday =
        new PreciseDateTimeField(DateTimeFieldType.hourOfHalfday(), hourDuration, halfdayDuration);

    private static final DateTimeField halfdayOfDay =
        new PreciseDateTimeField(DateTimeFieldType.halfdayOfDay(), halfdayDuration, dayDuration);

    private static final DateTimeField clockhourOfDay =
        new ZeroIsMaxDateTimeField(hourOfDay, DateTimeFieldType.clockhourOfDay());

    private static final DateTimeField clockhourOfHalfday =
        new ZeroIsMaxDateTimeField(hourOfHalfday, DateTimeFieldType.clockhourOfHalfday());

    private static final DateTimeField dayOfWeek =
        new PreciseDateTimeField(DateTimeFieldType.dayOfWeek(), dayDuration, weekDuration);

    /**
     * A DateTimeField whose values start at 1 instead of 0
     * @todo should we use composition instead of inheritance?
     */
    private static final class OneBasedPreciseDateTimeField extends PreciseDateTimeField {

        public OneBasedPreciseDateTimeField(DateTimeFieldType fieldType, DurationField unit, DurationField range) {
            super(fieldType, unit, range);
        }

        @Override public int getMinimumValue() { return super.getMinimumValue() + 1; }
        @Override public int getMaximumValue() { return super.getMaximumValue() + 1; }

        @Override public int get(long instant) {
            return super.get(instant) + 1;
        }

        // We don't need to override set() because set() calls get() to figure
        // out the offset from the current value of this field.  E.g. if we
        // want to set the month to 2 (February), we first figure out what the
        // instant's current month is: this month number will be one-based, so
        // we will automatically calculate the correct offset in ms from this
        // month.
    }

    /** Deals with the fact that dayOfMonth is between 1 and 30 inclusive */
    private static final DateTimeField dayOfMonth =
        new OneBasedPreciseDateTimeField(DateTimeFieldType.dayOfMonth(), dayDuration, monthDuration);

    private static final DateTimeField dayOfYear =
        new OneBasedPreciseDateTimeField(DateTimeFieldType.dayOfYear(), dayDuration, yearDuration);

    private static final DateTimeField monthOfYear =
        new OneBasedPreciseDateTimeField(DateTimeFieldType.monthOfYear(), monthDuration, yearDuration);

    private static final DateTimeField yearOfCentury =
        new PreciseDateTimeField(DateTimeFieldType.yearOfCentury(), yearDuration, centuryDuration);

    // Use PreciseDurationDateTimeField for day, month and year - all have
    // constant offsets (1, 1, and 1970), but different min and max values.
    // Years don't need the RangeDurationField, but days and months might.
    // Do we need to use this for dayOfWeek too?

    private static final DateTimeField year =
        new PreciseDurationDateTimeField(DateTimeFieldType.year(), yearDuration) {

        @Override
        public int get(long instant) {
            // We need to use Math.floor() to deal with negative instants
            return (int)Math.floor(instant * 1.0 / yearDuration.getUnitMillis()) + 1970;
        }

        /** Returns null: the field has no range */
        @Override
        public DurationField getRangeDurationField() { return null; }

        @Override
        public int getMinimumValue() { return this.get(Long.MIN_VALUE); }

        @Override
        // We subtract one to ensure that the whole of this year can be
        // encoded
        public int getMaximumValue() { return this.get(Long.MAX_VALUE) - 1; }
    };
    
    
    ///// CONSTRUCTORS AND FACTORIES /////

    private static final ThreeSixtyDayChronology INSTANCE_UTC =
        new ThreeSixtyDayChronology();

    /** Private constructor to prevent direct instantiation */
    private ThreeSixtyDayChronology() {}

    /** Gets an instance of this Chronology in the UTC time zone */
    public static ThreeSixtyDayChronology getInstanceUTC() {
        return INSTANCE_UTC;
    }
    
    ///// DURATION ACCESSORS /////

    @Override
    public DurationField millis() { return millisecondDuration; }

    @Override
    public DurationField seconds() { return secondDuration; }

    @Override
    public DurationField minutes() { return minuteDuration; }

    @Override
    public DurationField hours() { return hourDuration; }

    @Override
    public DurationField halfdays() { return halfdayDuration; }

    /** Each day has exactly the same length: there is no daylight saving */
    @Override
    public DurationField days() { return dayDuration; }

    /** Each week has 7 days */
    @Override
    public DurationField weeks() { return weekDuration; }

    /** Each month has exactly 30 days */
    @Override
    public DurationField months() { return monthDuration; }

    /** Each year has exactly 360 days */
    @Override
    public DurationField years() { return yearDuration; }

    @Override
    public DurationField centuries() { return centuryDuration; }



    ///// DATE-TIME FIELD ACCESSORS /////

    @Override
    public DateTimeField millisOfSecond() { return millisOfSecond; }

    @Override
    public DateTimeField millisOfDay() { return millisOfDay; }

    @Override
    public DateTimeField secondOfMinute() { return secondOfMinute; }

    @Override
    public DateTimeField secondOfDay() { return secondOfDay; }

    @Override
    public DateTimeField minuteOfHour() { return minuteOfHour; }

    @Override
    public DateTimeField minuteOfDay() { return minuteOfDay; }

    @Override
    public DateTimeField hourOfDay() { return hourOfDay; }

    @Override
    public DateTimeField hourOfHalfday() { return hourOfHalfday; }

    @Override
    public DateTimeField halfdayOfDay() { return halfdayOfDay; }

    @Override
    public DateTimeField clockhourOfDay() { return clockhourOfDay; }

    @Override
    public DateTimeField clockhourOfHalfday() { return clockhourOfHalfday; }

    @Override
    public DateTimeField dayOfWeek() { return dayOfWeek; }

    @Override
    public DateTimeField dayOfMonth() { return dayOfMonth; }

    @Override
    public DateTimeField dayOfYear() { return dayOfYear;}

    @Override
    public DateTimeField monthOfYear() { return monthOfYear; }

    @Override
    public DateTimeField year() { return year; }

    @Override
    public DateTimeField yearOfCentury() { return yearOfCentury; }



    /** Always returns UTC */
    @Override
    public DateTimeZone getZone() { return DateTimeZone.UTC; }

    @Override
    public Chronology withUTC() { return INSTANCE_UTC; }

    /** Throws UnsupportedOperationException unless the time zone is UTC */
    @Override
    public Chronology withZone(DateTimeZone zone) {
        if (zone.equals(DateTimeZone.UTC)) return INSTANCE_UTC;
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
        return "360-day Chronology in UTC";
    }

}
