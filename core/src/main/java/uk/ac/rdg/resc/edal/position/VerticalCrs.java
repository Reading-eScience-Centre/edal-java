/*
 * Copyright (c) 2011 The University of Reading
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

package uk.ac.rdg.resc.edal.position;

import uk.ac.rdg.resc.edal.Unit;

/**
 * <p>A vertical coordinate reference system.</p>
 * <p>We don't use GeoAPI's VerticalCRS class here as we need to incorporate
 * pressure and dimensionless coordinates.</p>
 * @todo Datum?
 */
public interface VerticalCrs {


    // Or could use GeoAPI's AxisDirection?
    public enum PositiveDirection { UP, DOWN }

    public Unit getUnits();

    /**
     * Return true if this axis has units of pressure.  If this is true
     * then the positive direction must be DOWN.
     */
    public boolean isPressure();

    /**
     * <p>Return true if this is a dimensionless (e.g. sigma or terrain-following)
     * coordinate system.  If this is true then the units are irrelevant, and
     * isPressure() will return false.</p>
     * <p>Future APIs will need to allow conversions between dimensionless and
     * dimensional coordinates, which will require more information.  (The conversion
     * can be performed using existing routines, e.g. in Java-NetCDF.)  However,
     * the current purpose of EDAL is not to perform the conversion but to provide
     * client code with enough information to decide what to do.</p>
     * @see http://cf-pcmdi.llnl.gov/documents/cf-conventions/1.5/cf-conventions.html#dimensionless-v-coord
     */
    public boolean isDimensionless();

    /**
     * Indicates whether coordinate values increase upward or downward.
     */
    public PositiveDirection getPositiveDirection();

}
