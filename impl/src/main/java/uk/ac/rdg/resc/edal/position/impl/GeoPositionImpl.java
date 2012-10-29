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

import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

/**
 * Implementation of {@link GeoPosition} that is immutable if the CRS of the
 * {@link HorizontalPosition} and the {@link VerticalPosition} are also
 * immutable
 * 
 * @author Guy Griffiths
 * 
 */
public class GeoPositionImpl implements GeoPosition {

    private final HorizontalPosition hPos;
    private final VerticalPosition vPos;
    private final TimePosition tPos;

    /**
     * Creates a new GeoPositionImpl with the given 4D coordinates
     * 
     * @param hPos
     *            The horizontal position
     * @param vPos
     *            The vertical position
     * @param tPos
     *            The time
     */
    public GeoPositionImpl(HorizontalPosition hPos, VerticalPosition vPos, TimePosition tPos) {
        this.hPos = hPos;
        this.vPos = vPos;
        this.tPos = tPos;
    }

    public GeoPositionImpl(HorizontalPosition hPos, Double vPos, VerticalCrs vCrs, TimePosition tPos) {
        this.hPos = hPos;
        this.vPos = new VerticalPositionImpl(vPos, vCrs);
        this.tPos = tPos;
    }

    @Override
    public HorizontalPosition getHorizontalPosition() {
        return hPos;
    }

    @Override
    public TimePosition getTimePosition() {
        return tPos;
    }

    @Override
    public VerticalPosition getVerticalPosition() {
        return vPos;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hPos == null) ? 0 : hPos.hashCode());
        result = prime * result + ((tPos == null) ? 0 : tPos.hashCode());
        result = prime * result + ((vPos == null) ? 0 : vPos.hashCode());
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
        GeoPositionImpl other = (GeoPositionImpl) obj;
        if (hPos == null) {
            if (other.hPos != null)
                return false;
        } else if (!hPos.equals(other.hPos))
            return false;
        if (tPos == null) {
            if (other.tPos != null)
                return false;
        } else if (!tPos.equals(other.tPos))
            return false;
        if (vPos == null) {
            if (other.vPos != null)
                return false;
        } else if (!vPos.equals(other.vPos))
            return false;
        return true;
    }
}
