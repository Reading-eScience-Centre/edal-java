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

package uk.ac.rdg.resc.edal.position.impl;

import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

/**
 * <p>
 * Implementation of {@link VerticalPosition} that is immutable provided that
 * the provided {@link VerticalCrs} is also immutable.
 * </p>
 * <p>
 * Although instances of this class are immutable, instances of subclasses may
 * not be.
 * </p>
 * 
 * @author Guy Griffiths
 */
public class VerticalPositionImpl implements VerticalPosition {

    private final Double z;
    private final VerticalCrs crs;

    /**
     * Creates a new VerticalPositionImpl with the given vertical coordinate
     * reference system and the given coordinate.
     * 
     * @param z
     *            The z coordinate (height/depth) of this position
     * @param crs
     *            The vertical coordinate reference system. If this is an
     *            immutable object then this VerticalPositionImpl will also be
     *            immutable. This may be null (in which case the CRS of this
     *            VerticalPosition must be given by some containing object).
     */
    public VerticalPositionImpl(double z, VerticalCrs crs) {
        this.z = z;
        this.crs = crs;
    }

    @Override
    public final VerticalCrs getCoordinateReferenceSystem() {
        return crs;
    }

    /** Returns the vertical coordinate of this position */
    @Override
    public final Double getZ() {
        return z;
    }

    @Override
    public int compareTo(VerticalPosition vPosition) {
        return z.compareTo(vPosition.getZ());
    }

    @Override
    public String toString() {
        return z + (crs != null ? crs.getUnits().getUnitString() : "");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((crs == null) ? 0 : crs.hashCode());
        result = prime * result + ((z == null) ? 0 : z.hashCode());
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
        VerticalPositionImpl other = (VerticalPositionImpl) obj;
        if (crs == null) {
            if (other.crs != null)
                return false;
        } else if (!crs.equals(other.crs))
            return false;
        if (z == null) {
            if (other.z != null)
                return false;
        } else if (!z.equals(other.z))
            return false;
        return true;
    }
}
