/*******************************************************************************
 * Copyright (c) 2012 The University of Reading
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

package uk.ac.rdg.resc.edal.position.impl;

import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.position.CalendarSystem;
import uk.ac.rdg.resc.edal.position.TimePeriod;
import uk.ac.rdg.resc.edal.position.TimePosition;

public class TimePositionJoda implements TimePosition {
    
    private DateTime dateTime;
    private CalendarSystem calSys;
    
    /**
     * Instantiates this {@link TimePosition} with the current time, and the
     * ISO8601 calendar
     */
    public TimePositionJoda(){
        this(CalendarSystem.CAL_ISO_8601);
    }
    
    /**
     * Instantiates this {@link TimePosition} with the current time, and a
     * specified calendar
     * 
     * @param calSys
     *            the {@link CalendarSystem} to use for this
     *            {@link TimePosition}
     */
    public TimePositionJoda(CalendarSystem calSys){
        dateTime = new DateTime();
        this.calSys = calSys;
    }
    
    /**
     * Instantiates this {@link TimePosition} with a specified time, and the
     * ISO8601 calendar
     * 
     * @param dateInMillis
     *            the time in milliseconds from 01-Jan-1970 00:00:00
     */
    public TimePositionJoda(long dateInMillis){
        this(dateInMillis, CalendarSystem.CAL_ISO_8601);
    }

    /**
     * Instantiates this {@link TimePosition} with a specified time, and a
     * specified calendar
     * 
     * @param dateInMillis
     *            the time in milliseconds from 01-Jan-1970 00:00:00
     * @param calSys
     *            the {@link CalendarSystem} to use for this
     *            {@link TimePosition}
     */
    public TimePositionJoda(long dateInMillis, CalendarSystem calSys){
        this.dateTime = new DateTime(dateInMillis);
        this.calSys = calSys;
    }
    
    /**
     * Instantiates this {@link TimePosition} from a specified time and the
     * ISO8601 calendar
     * 
     * @param dateTime
     *            the required time
     */
    public TimePositionJoda(DateTime dateTime){
        this(dateTime, CalendarSystem.CAL_ISO_8601);
    }
    
    public TimePositionJoda(DateTime dateTime, CalendarSystem calSys){
        this.dateTime = dateTime;
        this.calSys = calSys;
    }
    
    @Override
    public long getValue() {
        return dateTime.getMillis();
    }

    @Override
    public int getYear() {
        return dateTime.getYear();
    }

    @Override
    public int getMonthOfYear() {
        return dateTime.getMonthOfYear() - 1;
    }

    @Override
    public int getDayOfYear() {
        return dateTime.getDayOfYear();
    }

    @Override
    public int getDayOfMonth() {
        return dateTime.getDayOfMonth();
    }

    @Override
    public int getHourOfDay() {
        return dateTime.getHourOfDay();
    }

    @Override
    public int getMinuteOfHour() {
        return dateTime.getHourOfDay();
    }

    @Override
    public int getSecondOfMinute() {
        return dateTime.getSecondOfMinute();
    }

    @Override
    public int getMillisecondOfSecond() {
        return dateTime.getMillisOfSecond();
    }

    @Override
    public int getTimeZoneOffset() {
        return (dateTime.getZone().getOffset(getValue())/60000);
    }

    @Override
    public CalendarSystem getCalendarSystem() {
        return calSys;
    }

    @Override
    public int compareTo(TimePosition t) {
        if(t == null){
            return 1;
        }
        return new Long(getValue()).compareTo(t.getValue());
    }
    
    @Override
    public boolean equals(Object arg0) {
        if(arg0 instanceof TimePosition){
            TimePosition timePosition = (TimePosition) arg0;
            if(timePosition.getValue() == getValue() && timePosition.getCalendarSystem().equals(calSys)){
                return true;
            }
        }
        return false;
    }

    @Override
    public TimePosition plus(TimePeriod period) {
        return new TimePositionJoda(dateTime.plusYears(period.getYears()).plusMonths(period.getMonths())
                .plusWeeks(period.getWeeks()).plusDays(period.getDays())
                .plusHours(period.getHours()).plusMinutes(period.getMinutes())
                .plusSeconds(period.getSeconds()), calSys);
    }

    @Override
    public TimePosition minus(TimePeriod period) {
        return new TimePositionJoda(dateTime.minusYears(period.getYears()).minusMonths(period.getMonths())
                .minusWeeks(period.getWeeks()).minusDays(period.getDays())
                .minusHours(period.getHours()).minusMinutes(period.getMinutes())
                .minusSeconds(period.getSeconds()), calSys);
    }

    @Override
    public long differenceInMillis(TimePosition time) {
        return getValue() - time.getValue();
    }
    
    @Override
    public String toString() {
        return dateTime.toString();
    }
}
