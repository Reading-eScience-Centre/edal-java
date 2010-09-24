/*
 * Copyright (c) 2009 The University of Reading
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

package uk.ac.rdg.resc.edal.util.position;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import uk.ac.rdg.resc.edal.geometry.HorizontalPosition;

/**
 * <p>Implementation of {@link HorizontalPosition} that is immutable provided that
 * the provided {@link CoordinateReferenceSystem} is also immutable.</p>
 * <p>Although instances of this class are immutable, instances of subclasses may
 * not be.</p>
 * @author Jon
 */
public class HorizontalPositionImpl extends DirectPositionImpl implements HorizontalPosition {

    /** Returns the x coordinate of this position */
    @Override public final double getX() { return this.getOrdinate(0); }

    /** Returns the y coordinate of this position */
    @Override public final double getY() { return this.getOrdinate(1); };

    /**
     * Creates a new HorizontalPositionImpl with the given coordinate reference
     * system and the given coordinates.
     * @param crs The coordinate reference system.  If this is an immutable
     * object then this HorizontalPositionImpl will also be immutable.  This
     * may be null (in which case the CRS of this HorizontalPosition must be given
     * by some containing object).
     * @param x The x coordinate of this position
     * @param y The y coordinate of this position
     * @throws IllegalArgumentException if the CRS is non-null and does not have
     * two dimensions
     */
    public HorizontalPositionImpl(CoordinateReferenceSystem crs, double x, double y) {
        super(crs, x, y);
    }

    /**
     * Creates a new HorizontalPositionImpl without a specified CoordinateReferenceSystem.  This object
     * may only be interpreted in the context of a containing or supporting object
     * that contains the CRS.
     * @param x The x coordinate of this position
     * @param y The y coordinate of this position
     */
    public HorizontalPositionImpl(double x, double y) {
        super(null, x, y);
    }

}
