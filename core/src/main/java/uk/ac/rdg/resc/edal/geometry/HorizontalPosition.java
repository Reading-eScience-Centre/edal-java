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

package uk.ac.rdg.resc.edal.geometry;

import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Defines the position of a point in the horizontal plane.
 * @todo May cause confusion for lat-lon coordinate systems (i.e. those with
 * latitude first).  In this case getX() would return latitude, which is
 * counterintuitive.  Do we actually need this interface at all?
 * @author Jon
 */
public interface HorizontalPosition extends DirectPosition {

    /** Returns the x coordinate of this position, equivalent to getOrdinate(0) */
    public double getX();

    /** Returns the y coordinate of this position, equivalent to getOrdinate(1) */
    public double getY();

    /**
     * Returns a two-dimensional coordinate reference system.
     * The first coordinate in the CRS is the {@link #getX() x coordinate};
     * the second is the {@link #getY() y coordinate}.
     * @return a two-dimensional coordinate reference system
     */
    @Override public CoordinateReferenceSystem getCoordinateReferenceSystem();

    /** Returns 2 */
    @Override public int getDimension();

    /**
     * Returns an array of two coordinates [x,y]
     */
    @Override public double[] getCoordinate();

    /**
     * Returns the ordinate at the specified dimension.
     * @param dimension - The dimension in the range 0 to 1 (inclusive)
     * @return The coordinate at the specified dimension (index = 0 gives the
     * x coordinate; index = 1 gives the y coordinate)
     * @throws IndexOutOfBoundsException if {@code index < 0 || index > 1}
     */
    @Override public double getOrdinate(int index);

}
