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

package uk.ac.rdg.resc.edal.domain;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.exceptions.InvalidCrsException;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Test class for {@link SimpleHorizontalDomain}. Only
 * {@link SimpleHorizontalDomain#contains} is tested.
 *
 * @author Nan
 */
public class SimpleHorizontalDomainTest {

    /**
     * Test {@link SimpleHorizontalDomain#contains}. Choose test positions
     * inside or outside SimpeHorizontalDomain. In particular, some of these
     * positions are on the edge of the domain; some use different from
     * CoordinateReferenceSystem.
     *
     * @throws InvalidCrsException
     *             if a unknown epsg code is provide.
     *
     */
    @Test
    public void testContains() throws InvalidCrsException {
        SimpleHorizontalDomain sdm = new SimpleHorizontalDomain(120, 30, 150, 40);
        HorizontalPosition position = new HorizontalPosition(125, 32, GISUtils.defaultGeographicCRS());
        assertTrue(sdm.contains(position));

        // positions below are on the edge
        position = new HorizontalPosition(120, 30, GISUtils.defaultGeographicCRS());
        assertTrue(sdm.contains(position));

        position = new HorizontalPosition(120, 40, GISUtils.defaultGeographicCRS());
        assertTrue(sdm.contains(position));

        position = new HorizontalPosition(150, 30, GISUtils.defaultGeographicCRS());
        assertTrue(sdm.contains(position));

        position = new HorizontalPosition(150, 40, GISUtils.defaultGeographicCRS());
        assertTrue(sdm.contains(position));

        position = new HorizontalPosition(150, 35, GISUtils.defaultGeographicCRS());
        assertTrue(sdm.contains(position));

        // position is inside domain
        position = new HorizontalPosition(130, 35, GISUtils.defaultGeographicCRS());
        assertTrue(sdm.contains(position));

        // positions are outside domain
        position = new HorizontalPosition(125, 42, GISUtils.defaultGeographicCRS());
        assertFalse(sdm.contains(position));

        position = new HorizontalPosition(305, 32, GISUtils.defaultGeographicCRS());
        assertFalse(sdm.contains(position));

        position = new HorizontalPosition(295, -20, GISUtils.defaultGeographicCRS());
        assertFalse(sdm.contains(position));

        position = new HorizontalPosition(-60, 20.7, GISUtils.defaultGeographicCRS());
        assertFalse(sdm.contains(position));

        // position using different epsg code is outside domain
        CoordinateReferenceSystem chinaArea = GISUtils.getCrs("EPSG:2426");
        assertFalse(sdm.contains(new HorizontalPosition(500000, 4261964.001513, chinaArea)));

        // position using different epsg code is inside domain
        CoordinateReferenceSystem japanArea = GISUtils.getCrs("EPSG:2450");
        assertTrue(sdm.contains(new HorizontalPosition(17945.194292, 41625.344542, japanArea)));
    }
}
