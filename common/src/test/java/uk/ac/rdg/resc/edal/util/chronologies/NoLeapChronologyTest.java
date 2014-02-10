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
import org.joda.time.DateTimeField;
import org.joda.time.IllegalFieldValueException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test for the {@link NoLeapChronology}.
 * 
 * @author Jon
 */
public final class NoLeapChronologyTest extends AbstractFixedYearVariableMonthChronologyTest {

    private static Chronology CHRON_NOLEAP = NoLeapChronology.getInstanceUTC();

    private static final int[] DAYS_IN_MONTH = new int[] { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31,
            30, 31 };

    public NoLeapChronologyTest() {
        super(CHRON_NOLEAP);
    }

    /** Tests the dayOfMonth DateTimeField */
    @Test
    public void testDayOfMonthField() {
        DateTimeField dayOfMonthField = CHRON_NOLEAP.dayOfMonth();
        assertEquals(1, dayOfMonthField.getMinimumValue());
        assertEquals(31, dayOfMonthField.getMaximumValue());
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testFeb29_2004() {
        /*
         * This would be a legal date in the 360day, AllLeap or Gregorian
         * calendars
         */
        this.testDateTime(2004, 2, 29, 0, 0, 0, 0);
    }

    @Override
    protected int getNumDaysInYear() {
        return 365;
    }

    @Override
    protected int[] getDaysInMonth() {
        return DAYS_IN_MONTH.clone();
    }

}