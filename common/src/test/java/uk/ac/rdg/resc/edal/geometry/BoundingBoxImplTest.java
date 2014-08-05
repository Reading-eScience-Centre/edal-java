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

package uk.ac.rdg.resc.edal.geometry;

import static org.junit.Assert.*;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import uk.ac.rdg.resc.edal.exceptions.InvalidCrsException;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Test class for {@link BoundingBox} and {@link AbstractPolygon}. Only methods
 * {@link AbstractPolygon#contains} is tested.
 * 
 * @author Nan
 */
public class BoundingBoxImplTest {
    private BoundingBoxImpl bbox;

    @Before
    public void setUp(){
        bbox = new BoundingBoxImpl(137.47, -20.0, 180.0, 60.0, DefaultGeographicCRS.WGS84);
    }

    /**
     * @throws InvalidCrsException
     *             if a wrong EPSG code is provided.
     * */

    @Test
    public void testContains() throws InvalidCrsException {
        //pick up points on the edges of the grid
        HorizontalPosition hPos = new HorizontalPosition(160.0, -20.0, DefaultGeographicCRS.WGS84);
        assertTrue(bbox.contains(hPos));

        hPos = new HorizontalPosition(150.0, 50.0, DefaultGeographicCRS.WGS84);
        assertTrue(bbox.contains(hPos));

        assertTrue(bbox.contains(137.47, -20.0));
        assertTrue(bbox.contains(137.47, 40.0));
        assertTrue(bbox.contains(137.47, 60.0));
        assertTrue(bbox.contains(150.0, -20.0));

        assertTrue(bbox.contains(180.0, -20.0));
        assertTrue(bbox.contains(180.0, 60.0));
        assertTrue(bbox.contains(180.0, 30.0));
        assertTrue(bbox.contains(160.0, 60.0));

        assertFalse(bbox.contains(190, 0.0));
        assertFalse(bbox.contains(Double.NaN, -30.0));

        assertFalse(bbox.contains(null));

        CoordinateReferenceSystem japanArea = GISUtils.getCrs("EPSG:2450");
        assertTrue(bbox.contains(new HorizontalPosition(17945.194292, 41625.344542, japanArea)));

        CoordinateReferenceSystem chinaArea = GISUtils.getCrs("EPSG:2426");
        assertFalse(bbox.contains(new HorizontalPosition(500000, 4261964.001513, chinaArea)));
    }
}
