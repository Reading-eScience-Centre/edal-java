/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
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

package uk.ac.rdg.resc.edal.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.joda.time.DateTime;
import org.junit.Test;

import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.grid.VerticalAxisImpl;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalCrsImpl;

public class GISUtilsTest {

    @Test
    public void testGetNextEquivalentLongitude() {
        assertEquals(0, GISUtils.getNextEquivalentLongitude(0, 0), 1e-8);
        assertEquals(180, GISUtils.getNextEquivalentLongitude(0, -180), 1e-8);

        assertEquals(360, GISUtils.getNextEquivalentLongitude(360, 0), 1e-8);
        assertEquals(361, GISUtils.getNextEquivalentLongitude(360, 1), 1e-8);

        assertEquals(360, GISUtils.getNextEquivalentLongitude(360, 360), 1e-8);
        assertEquals(719, GISUtils.getNextEquivalentLongitude(360, 359), 1e-8);
    }

    @Test
    public void testConstrainLongitude180() {
        assertEquals(0, GISUtils.constrainLongitude180(-360), 1e-8);
        assertEquals(90, GISUtils.constrainLongitude180(-270), 1e-8);
        assertEquals(180, GISUtils.constrainLongitude180(-180), 1e-8);
        assertEquals(-90, GISUtils.constrainLongitude180(-90), 1e-8);
        assertEquals(0, GISUtils.constrainLongitude180(-0), 1e-8);
        assertEquals(0, GISUtils.constrainLongitude180(0), 1e-8);
        assertEquals(90, GISUtils.constrainLongitude180(90), 1e-8);
        assertEquals(180, GISUtils.constrainLongitude180(180), 1e-8);
        assertEquals(-90, GISUtils.constrainLongitude180(270), 1e-8);
        assertEquals(0, GISUtils.constrainLongitude180(360), 1e-8);
        assertEquals(90, GISUtils.constrainLongitude180(450), 1e-8);
        assertEquals(180, GISUtils.constrainLongitude180(540), 1e-8);
        assertEquals(-90, GISUtils.constrainLongitude180(630), 1e-8);
        assertEquals(0, GISUtils.constrainLongitude180(720), 1e-8);
    }

    @Test
    public void constrainLongitude360() {
        assertEquals(0, GISUtils.constrainLongitude360(-360), 1e-8);
        assertEquals(90, GISUtils.constrainLongitude360(-270), 1e-8);
        assertEquals(180, GISUtils.constrainLongitude360(-180), 1e-8);
        assertEquals(270, GISUtils.constrainLongitude360(-90), 1e-8);
        assertEquals(0, GISUtils.constrainLongitude360(-0), 1e-8);
        assertEquals(0, GISUtils.constrainLongitude360(0), 1e-8);
        assertEquals(90, GISUtils.constrainLongitude360(90), 1e-8);
        assertEquals(180, GISUtils.constrainLongitude360(180), 1e-8);
        assertEquals(270, GISUtils.constrainLongitude360(270), 1e-8);
        assertEquals(0, GISUtils.constrainLongitude360(360), 1e-8);
        assertEquals(90, GISUtils.constrainLongitude360(450), 1e-8);
        assertEquals(180, GISUtils.constrainLongitude360(540), 1e-8);
        assertEquals(270, GISUtils.constrainLongitude360(630), 1e-8);
        assertEquals(0, GISUtils.constrainLongitude360(720), 1e-8);
    }

    @Test
    public void testGetNearestEquivalentLongitude() {
        assertEquals(0, GISUtils.getNearestEquivalentLongitude(0, 0), 1e-8);
        assertEquals(90, GISUtils.getNearestEquivalentLongitude(0, 90), 1e-8);
        assertEquals(180, GISUtils.getNearestEquivalentLongitude(0, 180), 1e-8);
        assertEquals(-90, GISUtils.getNearestEquivalentLongitude(0, 270), 1e-8);
        assertEquals(0, GISUtils.getNearestEquivalentLongitude(0, 360), 1e-8);

        assertEquals(0, GISUtils.getNearestEquivalentLongitude(90, 0), 1e-8);
        assertEquals(90, GISUtils.getNearestEquivalentLongitude(90, 90), 1e-8);
        assertEquals(180, GISUtils.getNearestEquivalentLongitude(90, 180), 1e-8);
        assertEquals(270, GISUtils.getNearestEquivalentLongitude(90, 270), 1e-8);
        assertEquals(-89, GISUtils.getNearestEquivalentLongitude(90, 271), 1e-8);
        assertEquals(0, GISUtils.getNearestEquivalentLongitude(90, 360), 1e-8);
    }

    @Test
    public void testConstrainBoundingBox() {
        BoundingBox bbox = new BoundingBoxImpl(10, -80, 20, 79, DefaultGeographicCRS.WGS84);
        BoundingBox constrainedBbox = GISUtils.constrainBoundingBox(bbox);
        assertEquals(10, constrainedBbox.getMinX(), 1e-8);
        assertEquals(20, constrainedBbox.getMaxX(), 1e-8);
        assertEquals(-80, constrainedBbox.getMinY(), 1e-8);
        assertEquals(79, constrainedBbox.getMaxY(), 1e-8);

        bbox = new BoundingBoxImpl(370, -80, 380, 79, DefaultGeographicCRS.WGS84);
        constrainedBbox = GISUtils.constrainBoundingBox(bbox);
        assertEquals(10, constrainedBbox.getMinX(), 1e-8);
        assertEquals(20, constrainedBbox.getMaxX(), 1e-8);
        assertEquals(-80, constrainedBbox.getMinY(), 1e-8);
        assertEquals(79, constrainedBbox.getMaxY(), 1e-8);

        bbox = new BoundingBoxImpl(179, -80, 181, 79, DefaultGeographicCRS.WGS84);
        constrainedBbox = GISUtils.constrainBoundingBox(bbox);
        assertEquals(-180, constrainedBbox.getMinX(), 1e-8);
        assertEquals(180, constrainedBbox.getMaxX(), 1e-8);
        assertEquals(-80, constrainedBbox.getMinY(), 1e-8);
        assertEquals(79, constrainedBbox.getMaxY(), 1e-8);
    }

    @Test
    public void testParseBbox() throws EdalException {
        BoundingBox bbox;

        bbox = GISUtils.parseBbox("-1, -2, 3, 4", true, "CRS:84");
        assertEquals(-1, bbox.getMinX(), 1e-8);
        assertEquals(-2, bbox.getMinY(), 1e-8);
        assertEquals(3, bbox.getMaxX(), 1e-8);
        assertEquals(4, bbox.getMaxY(), 1e-8);

        bbox = GISUtils.parseBbox("-1, -2, 3, 4", true, "EPSG:4326");
        assertEquals(-1, bbox.getMinX(), 1e-8);
        assertEquals(-2, bbox.getMinY(), 1e-8);
        assertEquals(3, bbox.getMaxX(), 1e-8);
        assertEquals(4, bbox.getMaxY(), 1e-8);

        bbox = GISUtils.parseBbox("-1, -2, 3, 4", false, "EPSG:4326");
        assertEquals(-1, bbox.getMinY(), 1e-8);
        assertEquals(-2, bbox.getMinX(), 1e-8);
        assertEquals(3, bbox.getMaxY(), 1e-8);
        assertEquals(4, bbox.getMaxX(), 1e-8);
    }

    @Test
    public void testGetClosestTimeTo() {
        List<DateTime> tValues = new ArrayList<DateTime>();
        for (long t = 0L; t <= 100; t += 10) {
            tValues.add(new DateTime(t));
        }
        TimeAxis tAxis = new TimeAxisImpl("time", tValues);
        assertNull(GISUtils.getClosestTimeTo(new DateTime(), null));
        assertEquals(new DateTime(100), GISUtils.getClosestTimeTo(null, tAxis));

        assertEquals(new DateTime(0L), GISUtils.getClosestTimeTo(new DateTime(0L), tAxis));
        assertEquals(new DateTime(0L), GISUtils.getClosestTimeTo(new DateTime(1L), tAxis));
        assertEquals(new DateTime(0L), GISUtils.getClosestTimeTo(new DateTime(2L), tAxis));
        assertEquals(new DateTime(0L), GISUtils.getClosestTimeTo(new DateTime(3L), tAxis));
        assertEquals(new DateTime(0L), GISUtils.getClosestTimeTo(new DateTime(4L), tAxis));
        assertEquals(new DateTime(10L), GISUtils.getClosestTimeTo(new DateTime(5L), tAxis));
        assertEquals(new DateTime(20L), GISUtils.getClosestTimeTo(new DateTime(15L), tAxis));
        assertEquals(new DateTime(0L), GISUtils.getClosestTimeTo(new DateTime(-1000L), tAxis));
        assertEquals(new DateTime(100L), GISUtils.getClosestTimeTo(new DateTime(1000L), tAxis));
    }

    @Test
    public void testGetClosestElevationToSurface() {
        List<Double> values;
        VerticalCrs vCrs;
        VerticalAxisImpl verticalAxis;

        assertNull(GISUtils.getClosestElevationToSurface(null));

        values = Arrays.asList(-10.0, -5.0, 0.0, 5.0, 10.0);
        vCrs = new VerticalCrsImpl("m", false, false, true);
        verticalAxis = new VerticalAxisImpl("z-axis", values, vCrs);
        assertEquals(0.0, GISUtils.getClosestElevationToSurface(verticalAxis), 1e-8);

        values = Arrays.asList(5.0, 6.0, 7.0, 8.0, 9.0, 10.0);
        vCrs = new VerticalCrsImpl("m", false, false, true);
        verticalAxis = new VerticalAxisImpl("z-axis", values, vCrs);
        assertEquals(5.0, GISUtils.getClosestElevationToSurface(verticalAxis), 1e-8);

        values = Arrays.asList(-5.0, -6.0, -7.0, -8.0, -9.0, -10.0);
        vCrs = new VerticalCrsImpl("m", false, false, true);
        verticalAxis = new VerticalAxisImpl("z-axis", values, vCrs);
        assertEquals(-5.0, GISUtils.getClosestElevationToSurface(verticalAxis), 1e-8);
    }
}
