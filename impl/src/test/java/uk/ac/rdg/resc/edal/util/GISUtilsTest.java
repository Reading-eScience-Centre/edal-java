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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test of the {@link Utils} class.
 * @author Jon
 */
public class GISUtilsTest {

    private static final double TOL = 1e-6;

    /** Tests the methods to constrain longitude */
    @Test
    public void testConstrainLongitude()
    {
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

}