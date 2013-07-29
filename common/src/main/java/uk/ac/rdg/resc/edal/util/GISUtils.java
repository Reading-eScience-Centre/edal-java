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

package uk.ac.rdg.resc.edal.util;

import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * A class containing static methods which are useful for GIS operations.
 * 
 * @author Guy
 * 
 */
public final class GISUtils {

    private GISUtils() {
    }

    public static boolean isWgs84LonLat(CoordinateReferenceSystem coordinateReferenceSystem) {
        try {
            return CRS.findMathTransform(coordinateReferenceSystem, DefaultGeographicCRS.WGS84)
                    .isIdentity();
        } catch (Exception e) {
            return false;
        }
    }
    
    public static double getNextEquivalentLongitude(double reference, double target) {
        // Find the clockwise distance from the first value on this axis
        // to the target value. This will be a positive number from 0 to
        // 360 degrees
        double clockDiff = constrainLongitude360(target - reference);
        return reference + clockDiff;
    }
    
    public static double constrainLongitude180(double value) {
        double val = constrainLongitude360(value);
        return val > 180.0 ? val - 360.0 : val;
    }

    /**
     * Returns a longitude value in degrees that is equal to the given value but
     * in the range [0:360]
     */
    public static double constrainLongitude360(double value) {
        double val = value % 360.0;
        return val < 0.0 ? val + 360.0 : val;
    }
}
