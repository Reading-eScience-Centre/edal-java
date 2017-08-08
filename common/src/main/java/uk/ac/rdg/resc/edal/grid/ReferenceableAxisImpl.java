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

package uk.ac.rdg.resc.edal.grid;

import java.util.List;

import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Immutable implementation of a {@link ReferenceableAxis}, whose values are not
 * necessarily regularly spaced along the axis.
 * 
 * @author Guy
 * @author Jon
 */
public class ReferenceableAxisImpl extends AbstractIrregularAxis<Double> {

    private static final long serialVersionUID = 1L;
    private final boolean isLongitude;

    /**
     * Creates a ReferenceableAxis from the given array of axis values. The axis
     * values are copied to internal data structures, therefore subsequent
     * modifications to the array of axis values have no effect on this object.
     * 
     * @param name
     *            The name of the axis
     * @param axisValues
     *            List of axis values; must ascend or descend monotonically
     * @param isLongitude
     *            True if this is a longitude axis in degrees (hence values of 0
     *            and 360 are equivalent).
     * @throws NullPointerException
     *             if {@code axisValues} is null
     * @throws IllegalArgumentException
     *             if the axis values are not in strictly ascending numerical
     *             order, or if the array of axis values is empty
     */
    public ReferenceableAxisImpl(String name, List<Double> axisValues, boolean isLongitude) {
        super(name, axisValues);
        this.isLongitude = isLongitude;
    }
    
    @Override
    public boolean wraps() {
        return this.isLongitude;
    }

    @Override
    public int findIndexOf(Double position) {
        if (position == null || Double.isNaN(position))
            return -1;
        if (isLongitude) {
            position = GISUtils.getNextEquivalentLongitude(getCoordinateExtent().getLow(), position);
        }
        return super.findIndexOf(position);
    }

    @Override
    protected Double extendFirstValue(Double firstVal, Double nextVal) {
        return firstVal - 0.5 * (nextVal - firstVal);
    }

    @Override
    protected Double extendLastValue(Double lastVal, Double secondLastVal) {
        return lastVal + 0.5 * (lastVal - secondLastVal);
    }

    @Override
    protected double difference(Double pos1, Double pos2) {
        return pos1 - pos2;
    }

    @Override
    protected Double getMidpoint(Double pos1, Double pos2) {
        return 0.5 * (pos1 + pos2);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (isLongitude ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReferenceableAxisImpl other = (ReferenceableAxisImpl) obj;
        if (isLongitude != other.isLongitude)
            return false;
        return true;
    }
}
