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

package uk.ac.rdg.resc.edal.grid;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * This is an abstract implementation of a {@link HorizontalGrid} for which
 * there exists a mathematical transformation to convert WGS84 to the native
 * CRS.
 * 
 * This transformation is invisible externally - i.e.
 * {@link AbstractTransformedGrid#getCoordinateReferenceSystem()} will return
 * {@link DefaultGeographicCRS#WGS84} and all operations behave as though this
 * is the co-ordinate reference system. However, this class also exposes the
 * method
 * {@link AbstractTransformedGrid#transformNativeHeadingToWgs84(double, double, double, double)}
 * so that vector transforms can be handled.
 */
public abstract class AbstractTransformedGrid implements HorizontalGrid {
    /**
     * Transforms a heading in native grid co-ordinates (degrees clockwise from
     * positive y-direction) into a heading in WGS84 (degrees clockwise from
     * north).
     * 
     * @param xComp
     *            The x-component of the heading
     * @param yComp
     *            The y-component of the heading
     * @param lon
     *            The longitude of the given components
     * @param lat
     *            The latitude of the given components
     * @return The transformed heading
     */
    public abstract double transformNativeHeadingToWgs84(double xComp, double yComp, double lon,
            double lat);

    /**
     * Always returns {@link DefaultGeographicCRS#WGS84}. Transformations
     * between native CRS and WGS84 are done behind the scenes
     */
    @Override
    public final CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return DefaultGeographicCRS.WGS84;
    }
}
