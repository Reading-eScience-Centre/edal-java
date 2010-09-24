/*
 * Copyright (c) 2007 The University of Reading
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

package uk.ac.rdg.resc.edal.coverage.domain.impl;

import uk.ac.rdg.resc.edal.coverage.domain.Extent;
import uk.ac.rdg.resc.edal.geometry.HorizontalPosition;
import uk.ac.rdg.resc.edal.geometry.LonLatPosition;
import java.util.Arrays;
import java.util.List;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import uk.ac.rdg.resc.edal.coverage.domain.DiscretePointDomain;

/**
 * <p>A list of {@link HorizontalPosition}s in a certain coordinate reference system.</p>
 * @author Jon
 */
public final class HorizontalDomain extends AbstractDiscretePointDomain<HorizontalPosition> implements DiscretePointDomain<HorizontalPosition>
{

    private final CoordinateReferenceSystem crs;
    private final List<HorizontalPosition> posList;

    /**
     * Creates a HorizontalDomain from the given List of HorizontalPositions with their
     * coordinate reference system
     * @param posList The x-y points to wrap as a HorizontalDomain
     * @param crs CoordinateReferenceSystem of the points, cannot be null
     * @return a new HorizontalDomain that wraps the given list of positions
     */
    public HorizontalDomain(List<HorizontalPosition> posList, CoordinateReferenceSystem crs) {
        if (crs == null) throw new NullPointerException("Must specify a CRS");
        this.posList = posList;
        this.crs = crs;
    }

    /**
     * Creates a HorizontalDomain containing a single point
     * @param point The HorizontalPosition to wrap
     * @param crs the coordinate reference system of the point
     * @return a new HorizontalDomain that wraps the given point
     */
    public HorizontalDomain(HorizontalPosition point, CoordinateReferenceSystem crs)
    {
        this(Arrays.asList(point), crs);
    }

    /**
     * Creates a HorizontalDomain containing a single point.
     * @param point The HorizontalPosition to wrap
     * @return a new HorizontalDomain that wraps the given point
     */
    public HorizontalDomain(HorizontalPosition point)
    {
        this(point, point.getCoordinateReferenceSystem());
    }

    /**
     * Creates a HorizontalDomain containing a single lon-lat point.
     * @param point The LonLatPosition to wrap
     * @return a new HorizontalDomain that wraps the given lon-lat point
     */
    public HorizontalDomain(LonLatPosition lonLat)
    {
        this(lonLat, DefaultGeographicCRS.WGS84);
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return this.crs;
    }

    @Override
    public List<HorizontalPosition> getDomainObjects() {
        return this.posList;
    }

    public Extent<HorizontalPosition> getExtent() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
