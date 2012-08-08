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

package uk.ac.rdg.resc.edal.coverage.domain.impl;

import java.util.Arrays;
import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.Domain;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * A list of {@link HorizontalPosition}s. Each will have a
 * {@link CoordinateReferenceSystem} associated with it. Generally, all
 * positions within the {@link Domain} will belong to the same
 * {@link CoordinateReferenceSystem}
 * 
 * @author Guy Griffiths
 */
public final class HorizontalDomain implements Domain<HorizontalPosition> {

    private final List<HorizontalPosition> posList;

    /**
     * Creates a HorizontalDomain from the given List of HorizontalPositions
     * with their coordinate reference system
     * 
     * @param list
     *            The x-y points to wrap as a HorizontalDomain
     * @return a new HorizontalDomain that wraps the given list of projection
     *         points
     */
    public HorizontalDomain(List<HorizontalPosition> posList) {
        this.posList = posList;
    }

    /**
     * Creates a HorizontalDomain containing a single point
     * 
     * @param point
     *            The HorizontalPosition to wrap
     * @return a new HorizontalDomain that wraps the given point
     */
    public HorizontalDomain(HorizontalPosition point) {
        this(Arrays.asList(point));
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        return posList.contains(position);
    }

    /**
     * Gets a list of all objects in the domain
     * 
     * @return a {@link List} of {@link HorizontalPosition}s
     */
    public List<HorizontalPosition> getDomainObjects() {
        return posList;
    }
}
