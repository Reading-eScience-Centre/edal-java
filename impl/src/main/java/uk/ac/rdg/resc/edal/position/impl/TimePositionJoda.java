package uk.ac.rdg.resc.edal.position.impl;

import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.position.CalendarSystem;
import uk.ac.rdg.resc.edal.position.TimePeriod;
import uk.ac.rdg.resc.edal.position.TimePosition;

public class TimePositionJoda implements TimePosition {
    
    private DateTime dateTime;
    private CalendarSystem calSys;
    
    public TimePositionJoda(){
        this(CalendarSystem.CAL_ISO_8601);
    }
    
    public TimePositionJoda(CalendarSystem calSys){
        dateTime = new DateTime();
        this.calSys = calSys;
    }
    
    public TimePositionJoda(long dateInMillis){
        this(dateInMillis, CalendarSystem.CAL_ISO_8601);
    }

    public TimePositionJoda(long dateInMillis, CalendarSystem calSys){
        this.dateTime = new DateTime(dateInMillis);
        this.calSys = calSys;
    }
    
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
        return new Long(getValue()).compareTo(t.getValue());
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
}
