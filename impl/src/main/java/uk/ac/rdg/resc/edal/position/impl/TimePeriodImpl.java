package uk.ac.rdg.resc.edal.position.impl;

import uk.ac.rdg.resc.edal.position.TimePeriod;

public class TimePeriodImpl implements TimePeriod {

    private int seconds = 0;
    private int minutes = 0;
    private int hours = 0;
    private int days = 0;
    private int weeks = 0;
    private int months = 0;
    private int years = 0;

    public TimePeriodImpl() {
    }

    @Override
    public TimePeriod withSeconds(int seconds) {
        this.seconds = seconds;
        return this;
    }

    @Override
    public TimePeriod withMinutes(int minutes) {
        this.minutes = minutes;
        return this;
    }

    @Override
    public TimePeriod withHours(int hours) {
        this.hours = hours;
        return this;
    }

    @Override
    public TimePeriodImpl withDays(int days) {
        this.days = days;
        return this;
    }

    @Override
    public TimePeriodImpl withWeeks(int weeks) {
        this.weeks = weeks;
        return this;
    }

    @Override
    public TimePeriodImpl withMonths(int months) {
        this.months = months;
        return this;
    }

    @Override
    public TimePeriodImpl withYears(int years) {
        this.years = years;
        return this;
    }

    @Override
    public int getSeconds() {
        return seconds;
    }

    @Override
    public int getMinutes() {
        return minutes;
    }

    @Override
    public int getHours() {
        return hours;
    }

    @Override
    public int getDays() {
        return days;
    }

    @Override
    public int getWeeks() {
        return weeks;
    }

    @Override
    public int getMonths() {
        return months;
    }

    @Override
    public int getYears() {
        return years;
    }
}