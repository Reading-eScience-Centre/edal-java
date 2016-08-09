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

import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Test class for {@link RegularGridImpl} and its ancestor.
 *
 * @author Nan
 *
 */
public class RegularGridImplTest {

    private CoordinateReferenceSystem crs = GISUtils.defaultGeographicCRS();

    /**
     * Test {@link RegularGrid#getXAxis} and {@link RegularGrid#getYAxis}.
     */
    @Test
    public void regularGridImplTest() {
        BoundingBox bbox = new BoundingBoxImpl(100.0, 20.0, 120.0, 50.0, crs);
        RegularGrid grid = new RegularGridImpl(bbox, 20, 30);

        double resolutionX = (bbox.getMaxX() - bbox.getMinX()) / grid.getXSize();
        double resolutionY = (bbox.getMaxY() - bbox.getMinY()) / grid.getYSize();
        /*
         * the values of 100.5 and 20.5 are drawn by the relationship between
         * the axis origins and their bounding box.
         */
        RegularAxis longside = new RegularAxisImpl("Geodetic longitude", 100.5, resolutionX,
                grid.getXSize(), true);
        RegularAxis latside = new RegularAxisImpl("Geodetic latitude", 20.5, resolutionY,
                grid.getYSize(), false);

        assertTrue(grid.getXAxis().equals(longside));
        assertTrue(grid.getYAxis().equals(latside));
    }
}
