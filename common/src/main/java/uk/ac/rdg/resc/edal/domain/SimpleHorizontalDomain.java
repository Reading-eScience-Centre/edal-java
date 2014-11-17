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

package uk.ac.rdg.resc.edal.domain;

import java.io.Serializable;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * A simple implementation of a {@link HorizontalDomain}
 * 
 * @author Guy Griffiths
 */
public class SimpleHorizontalDomain implements HorizontalDomain, Serializable {
    private static final long serialVersionUID = 1L;
    private final BoundingBox bbox;

    /**
     * Create a {@link HorizontalDomain} based on a WGS84 bounding box
     * 
     * @param minLon
     *            The minimum longitude
     * @param minLat
     *            The minimum latitude
     * @param maxLon
     *            The maximum longitude
     * @param maxLat
     *            The maximum latitude
     */
    public SimpleHorizontalDomain(double minLon, double minLat, double maxLon, double maxLat) {
        bbox = new BoundingBoxImpl(minLon, minLat, maxLon, maxLat, DefaultGeographicCRS.WGS84);
    }

    /**
     * Create a {@link HorizontalDomain}
     * 
     * @param minX
     *            The minimum x value
     * @param minY
     *            The minimum y value
     * @param maxX
     *            The maximum x value
     * @param maxY
     *            The maximum y value
     * @param crs
     *            The {@link CoordinateReferenceSystem} of the domain
     */
    public SimpleHorizontalDomain(double minX, double minY, double maxX, double maxY,
            CoordinateReferenceSystem crs) {
        bbox = new BoundingBoxImpl(minX, minY, maxX, maxY, crs);
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        return bbox.contains(position);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return bbox;
    }

    @Override
    public GeographicBoundingBox getGeographicBoundingBox() {
        return GISUtils.toGeographicBoundingBox(bbox);
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return bbox.getCoordinateReferenceSystem();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bbox == null) ? 0 : bbox.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleHorizontalDomain other = (SimpleHorizontalDomain) obj;
        if (bbox == null) {
            if (other.bbox != null)
                return false;
        } else if (!bbox.equals(other.bbox))
            return false;
        return true;
    }
}
