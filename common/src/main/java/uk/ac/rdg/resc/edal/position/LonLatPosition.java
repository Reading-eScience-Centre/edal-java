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

import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * A two-dimensional {@link DirectPosition} that describes a point as longitude
 * and latitude using the WGS84 datum.
 * 
 * @author Jon Blower
 */
public interface LonLatPosition extends HorizontalPosition {

    /**
     * Returns the longitude, in the range [-180:180] degrees.
     * 
     * @return the longitude, in the range [-180:180] degrees.
     */
    @Override
    public double getX();

    /**
     * Returns the geodetic latitude in degrees.
     * 
     * @return the geodetic latitude in degrees.
     */
    @Override
    public double getY();

    /**
     * Returns the longitude, in the range [-180:180] degrees.
     * 
     * @return the longitude, in the range [-180:180] degrees.
     */
    public double getLongitude();

    /**
     * Returns the geodetic latitude in degrees.
     * 
     * @return the geodetic latitude in degrees.
     */
    public double getLatitude();

    /**
     * Returns a two-dimensional coordinate reference system for longitude and
     * latitude using the WGS84 datum ({@literal i.e.} "CRS:84"). The first
     * coordinate in the CRS is the longitude, the second is the geodetic
     * latitude.
     * 
     * @return a two-dimensional coordinate reference system for longitude and
     *         latitude using the WGS84 datum.
     */
    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem();

    /**
     * Returns an array of two coordinates: the first coordinate is the
     * longitude (in the range [-180:180] and the second coordinate is the
     * geodetic latitude.
     */
    @Override
    public double[] getCoordinate();

    /**
     * Returns the ordinate at the specified dimension.
     * 
     * @param dimension
     *            - The dimension in the range 0 to 1 (inclusive)
     * @return The coordinate at the specified dimension (index = 0 gives the
     *         longitude in the range [-180,180], index = 1 gives the geodetic
     *         latitude).
     * @throws IndexOutOfBoundsException
     *             if {@code index < 0 || index > 1}
     */
    @Override
    public double getOrdinate(int index);

}
