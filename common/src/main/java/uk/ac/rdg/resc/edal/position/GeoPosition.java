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

package uk.ac.rdg.resc.edal.position;

import org.joda.time.DateTime;

/**
 * <p>
 * Defines the position of a point in four-dimensional space and time.
 * </p>
 * 
 * @author Guy Griffiths
 * @author Jon Blower
 */
public class GeoPosition {

    private final HorizontalPosition hPos;
    private final VerticalPosition zPos;
    private final DateTime time;

    public GeoPosition(HorizontalPosition hPos, VerticalPosition zPos, DateTime time) {
        super();
        this.hPos = hPos;
        this.zPos = zPos;
        this.time = time;
    }

    /**
     * @return the {@link HorizontalPosition} component of this
     *         {@link GeoPosition}
     */
    public HorizontalPosition getHorizontalPosition() {
        return hPos;
    }

    /**
     * @return the {@link VerticalPosition} component of this
     *         {@link GeoPosition}
     */
    public VerticalPosition getVerticalPosition() {
        return zPos;
    }

    /**
     * @return the time component of this {@link GeoPosition}
     */
    public DateTime getTime() {
        return time;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hPos == null) ? 0 : hPos.hashCode());
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
        GeoPosition other = (GeoPosition) obj;
        if (hPos == null) {
            if (other.hPos != null)
                return false;
        } else if (!hPos.equals(other.hPos))
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
