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

package uk.ac.rdg.resc.edal.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.UnitVocabulary;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.coverage.grid.impl.TimeAxisImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.VerticalAxisImpl;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalCrs.PositiveDirection;
import uk.ac.rdg.resc.edal.position.impl.TimePositionJoda;
import uk.ac.rdg.resc.edal.position.impl.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.position.impl.VerticalPositionImpl;

/**
 * Test of the {@link GISUtils} class.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public class GISUtilsTest {

    private static final double TOL = 1e-6;

    /**
     * Test the getNextEquivalentLongitude() method
     */
    @Test
    public void testGetNextEquivalentLongitude() {
        assertEquals(365.0, GISUtils.getNextEquivalentLongitude(10.0, 5.0), TOL);
        assertEquals(5.0, GISUtils.getNextEquivalentLongitude(-180.0, 5.0), TOL);
        assertEquals(5.0, GISUtils.getNextEquivalentLongitude(-180.0, 365.0), TOL);
        assertEquals(5.0, GISUtils.getNextEquivalentLongitude(-180.0, -355.0), TOL);
    }

    /** Tests the methods to constrain longitude */
    @Test
    public void testConstrainLongitude() {
        double lon = 0.0;
        assertEquals(lon, GISUtils.constrainLongitude180(lon), TOL);
        assertEquals(lon, GISUtils.constrainLongitude360(lon), TOL);
    
        lon = 0.1;
        assertEquals(lon, GISUtils.constrainLongitude180(lon), TOL);
        assertEquals(lon, GISUtils.constrainLongitude360(lon), TOL);
    
        lon = -0.1;
        assertEquals(lon, GISUtils.constrainLongitude180(lon), TOL);
        assertEquals(359.9, GISUtils.constrainLongitude360(lon), TOL);
    
        lon = 179.9;
        assertEquals(lon, GISUtils.constrainLongitude180(lon), TOL);
        assertEquals(lon, GISUtils.constrainLongitude360(lon), TOL);
    
        lon = 180.0;
        assertEquals(180.0, GISUtils.constrainLongitude180(lon), TOL);
        assertEquals(lon, GISUtils.constrainLongitude360(lon), TOL);
    
        lon = 180.1;
        assertEquals(-179.9, GISUtils.constrainLongitude180(lon), TOL);
        assertEquals(lon, GISUtils.constrainLongitude360(lon), TOL);
    
        lon = -179.9;
        assertEquals(lon, GISUtils.constrainLongitude180(lon), TOL);
        assertEquals(180.1, GISUtils.constrainLongitude360(lon), TOL);
    
        lon = -180.0;
        assertEquals(180.0, GISUtils.constrainLongitude180(lon), TOL);
        assertEquals(180.0, GISUtils.constrainLongitude360(lon), TOL);
    
        lon = -180.1;
        assertEquals(179.9, GISUtils.constrainLongitude180(lon), TOL);
        assertEquals(179.9, GISUtils.constrainLongitude360(lon), TOL);
    
        lon = 359.9;
        assertEquals(-0.1, GISUtils.constrainLongitude180(lon), TOL);
        assertEquals(lon, GISUtils.constrainLongitude360(lon), TOL);
    
        lon = 360.0;
        assertEquals(0.0, GISUtils.constrainLongitude180(lon), TOL);
        assertEquals(0.0, GISUtils.constrainLongitude360(lon), TOL);
    
        lon = 360.1;
        assertEquals(0.1, GISUtils.constrainLongitude180(lon), TOL);
        assertEquals(0.1, GISUtils.constrainLongitude360(lon), TOL);
    
        lon = 720.0;
        assertEquals(0.0, GISUtils.constrainLongitude180(lon), TOL);
        assertEquals(0.0, GISUtils.constrainLongitude360(lon), TOL);
    
        lon = -540.0;
        assertEquals(180.0, GISUtils.constrainLongitude180(lon), TOL);
        assertEquals(180.0, GISUtils.constrainLongitude360(lon), TOL);
    }

    @Test
    public void testGetClosestTimeTo() {
        List<TimePosition> axisValues = new ArrayList<TimePosition>();
        for(long t = 10000; t < 20000; t += 1000){
            /*
             * Axis will have the values 10,000 - 19,000, incrementing by 1000
             * each time
             */
            axisValues.add(new TimePositionJoda(t));
        }
        TimeAxis tAxis = new TimeAxisImpl("testaxis", axisValues);
        
        // Should be null if the axis is null
        assertNull(GISUtils.getClosestTimeTo(new TimePositionJoda(), null));
        // Should be closest to current time if the target is null
        assertEquals(19000L, GISUtils.getClosestTimeTo(null, tAxis).getValue());
        // Test for the correct rounding behaviour
        assertEquals(11000L, GISUtils.getClosestTimeTo(new TimePositionJoda(10500L), tAxis).getValue());
        assertEquals(11000L, GISUtils.getClosestTimeTo(new TimePositionJoda(11499L), tAxis).getValue());
        // Test that values outside the axis return the axis endpoints
        assertEquals(10000L, GISUtils.getClosestTimeTo(new TimePositionJoda(0L), tAxis).getValue());
        assertEquals(19000L, GISUtils.getClosestTimeTo(new TimePositionJoda(10000000L), tAxis).getValue());
    }
    
    @Test
    public void testGetClosestElevationTo() {
        VerticalCrs vCrs = new VerticalCrsImpl(Unit.getUnit("m", UnitVocabulary.UDUNITS2),
                PositiveDirection.DOWN, false); 
        List<Double> axisValues = new ArrayList<Double>();
        for(int v = 0; v < 1000; v += 100){
            /*
             * Axis will have the values 0-900, incrementing by 100
             * each time
             */
            axisValues.add(new Double(v));
        }
        VerticalAxis vAxis = new VerticalAxisImpl("testaxis", axisValues, vCrs);
        
        // Should be null if the axis is null
        assertNull(GISUtils.getClosestElevationTo(1000.0, null));
        // Should be closest to the surface if the target is null
        assertEquals(new VerticalPositionImpl(0, vCrs), GISUtils.getClosestElevationTo(null, vAxis));
        // Test rounding behaviour
        assertEquals(new VerticalPositionImpl(100, vCrs), GISUtils.getClosestElevationTo(50.0, vAxis));
        assertEquals(new VerticalPositionImpl(100, vCrs), GISUtils.getClosestElevationTo(149.9, vAxis));
        // Test values outside the axis return the endpoints
        assertEquals(new VerticalPositionImpl(0, vCrs), GISUtils.getClosestElevationTo(-100.0, vAxis));
        assertEquals(new VerticalPositionImpl(900, vCrs), GISUtils.getClosestElevationTo(10000.0, vAxis));
    }
    
    @Test
    public void testGetExactElevation(){
        /*
         * Gets the exact elevation required, or <code>null</code> if it is not
         * present in the domain of the given feature
         * 
         * @param elevationStr
         *            The desired elevation, as {@link String} representation of a
         *            number (i.e. with no units etc.)
         * @param feature
         *            The feature containing the domain
         * @return The {@link VerticalPosition} required, or <code>null</code>
         */
        VerticalCrs vCrs = new VerticalCrsImpl(Unit.getUnit("m", UnitVocabulary.UDUNITS2),
                PositiveDirection.DOWN, false); 
        List<Double> axisValues = new ArrayList<Double>();
        for(int v = 0; v < 1000; v += 100){
            /*
             * Axis will have the values 0-900, incrementing by 100
             * each time
             */
            axisValues.add(new Double(v));
        }
        VerticalAxis vAxis = new VerticalAxisImpl("testaxis", axisValues, vCrs);
        
        // Different formats of the same thing
        assertEquals(new VerticalPositionImpl(100.0, vCrs), GISUtils.getExactElevation("100", vAxis));
        assertEquals(new VerticalPositionImpl(100.0, vCrs), GISUtils.getExactElevation("100.00", vAxis));
        assertEquals(new VerticalPositionImpl(100.0, vCrs), GISUtils.getExactElevation("00100", vAxis));
        assertEquals(new VerticalPositionImpl(100.0, vCrs), GISUtils.getExactElevation("00100.00", vAxis));
        assertEquals(new VerticalPositionImpl(0.0, vCrs), GISUtils.getExactElevation("-0.00", vAxis));
        // These values are not close enough to any axis values - we expect null
        assertNull(GISUtils.getExactElevation("-100.0", vAxis));
        assertNull(GISUtils.getExactElevation("100.00000001", vAxis));
        assertNull(GISUtils.getExactElevation("99.9999999", vAxis));
    }
    
    @Test
    public void testGetClosestHorizontalPositionTo(){}
    
    @Test
    public void testFeatureOverlapsBoundingBox() {}

}