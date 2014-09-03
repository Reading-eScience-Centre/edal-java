/*******************************************************************************
 * Copyright (c) 2014 The University of Reading
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

package uk.ac.rdg.resc.edal.grid;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * Test class for {@link TimeAxisImpl} and its ancestors.
 * 
 * @author Nan
 * 
 */
public class TimeAxisImplTest {
    private TimeAxis tAxis;
    private Chronology chronology = ISOChronology.getInstance();
    private String tAxisName = "Sample TimeAxis";
    // the number not include the first one on the axis
    private int numberOfDate = 10;
    // first date on the t axis
    private DateTime start;
    // contains the values on the t axis; the date step is one day.
    private List<DateTime> datetimes;

    /**
     * Initialising.
     */
    @Before
    public void setUp() {
        start = new DateTime(1999, 12, 1, 10, 0, chronology);

        datetimes = new ArrayList<DateTime>();
        for (int i = 0; i < numberOfDate + 1; i++) {
            datetimes.add(start.plusDays(i));
        }
        tAxis = new TimeAxisImpl(tAxisName, datetimes);
    }

    /**
     * Test the method of {@link TimeAxisImpl#contains}.
     */
    @Test
    public void testContains() {
        // pick up the fifth date in the range of t axis
        DateTime dt = start.plusDays(5);
        assertTrue(tAxis.contains(dt));

        assertFalse(tAxis.contains(null));

        // pick up a date out of the range of t axis
        dt = start.plusDays(25);
        assertFalse(tAxis.contains(dt));
        // pick up a date out of the range of t axis
        dt = start.minusDays(5);
        assertFalse(tAxis.contains(dt));
    }

    /**
     * Test the get methods in {@link TimeAxisImpl}.
     */
    @Test
    public void testGetMethods() {
        assertEquals(chronology, tAxis.getChronology());
        Extent<DateTime> dateExtent = tAxis.getCoordinateExtent();
        /*
         * the date value start from the first date value minus half of the date
         * step, which is 12 hours
         */
        DateTime from = start.minusHours(12);
        /*
         * the date value end after the last date value plus half of the date
         * step, which is 12 hours
         */
        DateTime to = start.plusDays(numberOfDate).plusHours(12);
        Extent<DateTime> expectedDateExtent = Extents.newExtent(from, to);
        assertEquals(expectedDateExtent, dateExtent);

        Array<Extent<DateTime>> dates = tAxis.getDomainObjects();
        int dateCounter = 0;
        for (Extent<DateTime> tExtent : dates) {
            Extent<DateTime> expectedExtent = Extents.newExtent(from.plusDays(dateCounter),
                    from.plusDays(++dateCounter));
            assertEquals(expectedExtent, tExtent);
        }

        int expectedIndex = 4;
        // the first date is index 0
        DateTime fifthDate = tAxis.getCoordinateValue(expectedIndex);
        assertEquals(start.plusDays(expectedIndex), fifthDate);

        Extent<DateTime> expectedFifthDateBound = Extents.newExtent(fifthDate.minusHours(12),
                fifthDate.plusHours(12));
        assertEquals(expectedFifthDateBound, tAxis.getCoordinateBounds(expectedIndex));
        assertEquals(datetimes, tAxis.getCoordinateValues());
        assertEquals(tAxisName, tAxis.getName());
    }

    /**
     * Test the method of {@link TimeAxisImpl#findIndexOf}.
     */
    @Test
    public void testFindIndexOf() {
        // the first date is index 0
        int expectedIndex = 4;
        DateTime fifthDate = tAxis.getCoordinateValue(expectedIndex);
        assertEquals(expectedIndex, tAxis.findIndexOf(fifthDate));
        int notFoundIndex = -1;
        //a date is outside t axis.
        assertEquals(notFoundIndex, tAxis.findIndexOf(start.plusDays(25)));
    }

    /**
     * Test the method of {@link TimeAxisImpl#isAscending}.
     */
    @Test
    public void testIsAscending() {
        assertTrue(tAxis.isAscending());
        
        ArrayList<DateTime> dts = new ArrayList<>();
        for (int i = 0; i < numberOfDate + 1; i++) {
            dts.add(start.minusDays(i));
        }
        TimeAxis ta = new TimeAxisImpl(tAxisName, dts);
        assertFalse(ta.isAscending());
    }

    /**
     * Test the method of {@link TimeAxisImpl#size}.
     */
    @Test
    public void testSize() {
        assertEquals(numberOfDate + 1, tAxis.size());
    }
}