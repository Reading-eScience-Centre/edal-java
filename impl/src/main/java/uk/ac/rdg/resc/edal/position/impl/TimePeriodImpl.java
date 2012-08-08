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

import uk.ac.rdg.resc.edal.position.TimePeriod;

public class TimePeriodImpl implements TimePeriod {

    private int seconds = 0;
    private int minutes = 0;
    private int hours = 0;
    private int days = 0;
    private int weeks = 0;
    private int months = 0;
    private int years = 0;

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
