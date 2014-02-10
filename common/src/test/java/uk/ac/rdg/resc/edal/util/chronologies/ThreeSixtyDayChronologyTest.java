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

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test for the {@link ThreeSixtyDayChronology}.
 * 
 * @author Jon
 */
public final class ThreeSixtyDayChronologyTest extends AbstractFixedYearChronologyTest {

    private static Chronology CHRON_360 = ThreeSixtyDayChronology.getInstanceUTC();

    private static final long MONTH = 30 * DAY;
    private static final long YEAR = 12 * MONTH;

    public ThreeSixtyDayChronologyTest() {
        super(CHRON_360);
    }

    @Test
    public void testFeb30_2000() {
        // Only legal in this calendar system
        System.out.println("Feb 30, 2000");
        testDateTime(2000, 2, 30, 0, 0, 0, 0);
    }

    @Test
    public void testYearArithmetic() {
        long millis = sample.getMillis();
        assertEquals(millis + 4 * YEAR, sample.year().addToCopy(4).getMillis());
        assertEquals(millis - 4 * YEAR, sample.year().addToCopy(-4).getMillis());

        DateTime yearOne = sample.withYear(1);
        millis = yearOne.getMillis();
        assertEquals(millis - 4 * YEAR, yearOne.year().addToCopy(-4).getMillis());
        System.out.println(yearOne);
    }

    @Test
    public void testMonthArithmetic2() {
        long millis = sample.getMillis();
        assertEquals(millis + 4 * MONTH, sample.monthOfYear().addToCopy(4).getMillis());
        assertEquals(millis - 4 * MONTH, sample.monthOfYear().addToCopy(-4).getMillis());
    }

    @Override
    protected int getNumDaysInYear() {
        return 360;
    }

    @Override
    protected int getDayOfYear(int monthOfYear, int dayOfMonth) {
        return (monthOfYear - 1) * 30 + dayOfMonth;
    }

    @Override
    protected int getNumDaysInMonth(int monthOfYear) {
        return 30;
    }

    @Override
    protected int getNumMonthsInYear() {
        return 12;
    }
}