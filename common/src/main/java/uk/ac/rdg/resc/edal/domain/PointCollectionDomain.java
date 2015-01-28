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

import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.exceptions.IncorrectDomainException;
import uk.ac.rdg.resc.edal.exceptions.MismatchedCrsException;
import uk.ac.rdg.resc.edal.feature.PointCollectionFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.ImmutableArray1D;

/**
 * The domain of a {@link PointCollectionFeature}: a set of
 * {@link HorizontalPosition}s with a fixed vertical position and time
 * 
 * @author Guy Griffiths
 */
public class PointCollectionDomain implements DiscreteHorizontalDomain<HorizontalPosition> {

    private final Array1D<HorizontalPosition> positions;

    private final BoundingBox bbox;
    private final VerticalPosition zPos;
    private final DateTime time;

    public PointCollectionDomain(List<HorizontalPosition> positions, VerticalPosition zPos,
            DateTime time) throws MismatchedCrsException, IncorrectDomainException {
        this.positions = new ImmutableArray1D<HorizontalPosition>(
                positions.toArray(new HorizontalPosition[0]));
        this.zPos = zPos;
        this.time = time;
        
        CoordinateReferenceSystem commonCrs = null;

        double minx = Double.MAX_VALUE;
        double maxx = -Double.MAX_VALUE;
        double miny = Double.MAX_VALUE;
        double maxy = -Double.MAX_VALUE;

        for (HorizontalPosition hPos : positions) {
            if (commonCrs == null) {
                commonCrs = hPos.getCoordinateReferenceSystem();
            } else {
                if (!commonCrs.equals(hPos.getCoordinateReferenceSystem())) {
                    hPos = GISUtils.transformPosition(hPos, commonCrs);
                }
            }
            if (!Double.isNaN(hPos.getX())) {
                minx = Math.min(minx, hPos.getX());
                maxx = Math.max(maxx, hPos.getX());
            }
            if (!Double.isNaN(hPos.getY())) {
                miny = Math.min(miny, hPos.getY());
                maxy = Math.max(maxy, hPos.getY());
            }

        }
        bbox = new BoundingBoxImpl(minx, miny, maxx, maxy, commonCrs);
    }

    /**
     * @return A {@link BoundingBox} which fully contains this
     *         {@link PointCollectionDomain}
     */
    public BoundingBox getCoordinateBounds() {
        return bbox;
    }

    /**
     * @return The time common to all points in the
     *         {@link PointCollectionDomain} (<code>null</code> if none is
     *         defined)
     * 
     */
    public DateTime getTime() {
        return time;
    }

    /**
     * @return The vertical position common to all points in this
     *         {@link PointCollectionDomain} (<code>null</code> if none is
     *         defined)
     */
    public VerticalPosition getVerticalPosition() {
        return zPos;
    }

    @Override
    public Array1D<HorizontalPosition> getDomainObjects() {
        return positions;
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        if (position == null) {
            return false;
        }
        Iterator<HorizontalPosition> iterator = positions.iterator();
        while (iterator.hasNext()) {
            if (position.equals(iterator.next())) {
                return true;
            }
        }
        return false;
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
    public long size() {
        return positions.size();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bbox == null) ? 0 : bbox.hashCode());
        result = prime * result + ((positions == null) ? 0 : positions.hashCode());
        result = prime * result + ((time == null) ? 0 : time.hashCode());
        result = prime * result + ((zPos == null) ? 0 : zPos.hashCode());
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
        PointCollectionDomain other = (PointCollectionDomain) obj;
        if (bbox == null) {
            if (other.bbox != null)
                return false;
        } else if (!bbox.equals(other.bbox))
            return false;
        if (positions == null) {
            if (other.positions != null)
                return false;
        } else if (!positions.equals(other.positions))
            return false;
        if (time == null) {
            if (other.time != null)
                return false;
        } else if (!time.equals(other.time))
            return false;
        if (zPos == null) {
            if (other.zPos != null)
                return false;
        } else if (!zPos.equals(other.zPos))
            return false;
        return true;
    }
}
