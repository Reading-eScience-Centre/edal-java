package uk.ac.rdg.resc.edal.position.impl;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import uk.ac.rdg.resc.edal.position.CalendarSystem;
import uk.ac.rdg.resc.edal.position.TimePeriod;
import uk.ac.rdg.resc.edal.position.TimePosition;

/**
 * Implementation of the {@link TimePosition} interface, backed by a
 * {@link Calendar}
 * 
 * @author Guy Griffiths
 * 
 */
public final class TimePositionImpl implements TimePosition {

    private Calendar cal;
    private CalendarSystem calSys;
    private int utcOffset;

    public TimePositionImpl(){
        this(new Date().getTime());
    }

    public TimePositionImpl(CalendarSystem calSys){
        this(new Date().getTime(), calSys);
    }
    
    /**
     * Instantiates an instance of the class with time in UTC, using the ISO8601
     * calendar system
     * 
     * @param timeInMilliseconds Offset in ms from the epoch (01-01-1970 00:00:00)
     */
    public TimePositionImpl(long timeInMilliseconds){
        this(timeInMilliseconds, CalendarSystem.CAL_ISO_8601);
    }
    
    public TimePositionImpl(long timeInMilliseconds, CalendarSystem calSys){
        this(timeInMilliseconds, calSys, 0);
    }
    
    public TimePositionImpl(long timeInMilliseconds, CalendarSystem calSys, int utcOffset){
        cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMilliseconds);
        
        this.calSys = calSys;
        this.utcOffset = utcOffset;
    }
    
    /**
     * Instantiates an instance of the class with time in UTC, using the ISO8601
     * calendar system
     * 
     * @param year
     *            The year
     * @param month
     *            The month, starting from January=1
     * @param dayOfMonth
     *            The day of the month
     * @param hour
     *            The hour of the day
     * @param minute
     *            The minute of the hour
     * @param second
     *            The second of the minute
     */
    public TimePositionImpl(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        this(year, month, dayOfMonth, hour, minute, second, 0, CalendarSystem.CAL_ISO_8601, 0);
    }

    /**
     * Instantiates an instance of the class calendar system
     * 
     * @param year
     *            The year
     * @param month
     *            The month, starting from January=1
     * @param dayOfMonth
     *            The day of the month
     * @param hour
     *            The hour of the day
     * @param minute
     *            The minute of the hour
     * @param second
     *            The second of the minute
     * @param milliseconds
     *            The millisecond of the second
     * @param calSys
     *            The calendar system being used
     * @param utcOffset
     *            The offset of the time from UTC
     */
    public TimePositionImpl(int year, int month, int dayOfMonth, int hour, int minute, int second, int milliseconds,
            CalendarSystem calSys, int utcOffset) {
        cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month-1, dayOfMonth, hour, minute, second);
        cal.set(Calendar.MILLISECOND, milliseconds);

        this.calSys = calSys;
        this.utcOffset = utcOffset;
    }

    @Override
    public int compareTo(TimePosition t) {
        long thisTime = this.getValue();
        long thatTime = t.getValue();
        if (thisTime < thatTime)
            return -1;
        if (thisTime > thatTime)
            return 1;
        return 0;
    }

    @Override
    public CalendarSystem getCalendarSystem() {
        return calSys;
    }

    @Override
    public int getDayOfMonth() {
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public int getDayOfYear() {
        return cal.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public int getHourOfDay() {
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    @Override
    public int getMillisecondOfSecond() {
        return cal.get(Calendar.MILLISECOND);
    }

    @Override
    public int getMinuteOfHour() {
        return cal.get(Calendar.MINUTE);
    }

    @Override
    /**
     * Returns the month of the year, counting from zero
     */
    public int getMonthOfYear() {
        return cal.get(Calendar.MONTH);
    }

    @Override
    public int getSecondOfMinute() {
        return cal.get(Calendar.SECOND);
    }

    @Override
    public int getTimeZoneOffset() {
        return utcOffset;
    }

    @Override
    public long getValue() {
        return cal.getTimeInMillis();
    }

    @Override
    public int getYear() {
        return cal.get(Calendar.YEAR);
    }
    
    @Override
    public String toString() {
        return DateFormat.getDateTimeInstance().format(cal.getTime());
    }

    @Override
    public TimePosition plus(TimePeriod period) {
        TimePositionImpl ret = new TimePositionImpl(this.getValue(), this.getCalendarSystem());
        ret.cal.add(Calendar.SECOND, period.getSeconds());
        ret.cal.add(Calendar.MINUTE, period.getMinutes());
        ret.cal.add(Calendar.HOUR_OF_DAY, period.getHours());
        ret.cal.add(Calendar.DAY_OF_MONTH, period.getDays());
        ret.cal.add(Calendar.WEEK_OF_MONTH, period.getWeeks());
        ret.cal.add(Calendar.MONTH, period.getMonths());
        ret.cal.add(Calendar.YEAR, period.getYears());
        return ret;
    }
    
    @Override
    public TimePosition minus(TimePeriod period) {
        TimePositionImpl ret = new TimePositionImpl(this.getValue(), this.getCalendarSystem());
        ret.cal.add(Calendar.SECOND, -period.getSeconds());
        ret.cal.add(Calendar.MINUTE, -period.getMinutes());
        ret.cal.add(Calendar.HOUR_OF_DAY, -period.getHours());
        ret.cal.add(Calendar.DAY_OF_MONTH, -period.getDays());
        ret.cal.add(Calendar.WEEK_OF_MONTH, -period.getWeeks());
        ret.cal.add(Calendar.MONTH, -period.getMonths());
        ret.cal.add(Calendar.YEAR, -period.getYears());
        return ret;
    }

    @Override
    public long differenceInMillis(TimePosition time) {
        return (getValue() - time.getValue());
    }

    public static void main(String[] args) {
        TimePosition tp = new TimePositionImpl();
        System.out.println("Time: "+tp);
        System.out.println("Plus 13h: "+tp.plus(new TimePeriodImpl().withHours(13)));
        System.out.println("Plus 1w: "+tp.plus(new TimePeriodImpl().withWeeks(1)));
        System.out.println("Plus 1month: "+tp.plus(new TimePeriodImpl().withMonths(1)));
        System.out.println("Plus 1year: "+tp.plus(new TimePeriodImpl().withYears(1)));
    }
}
