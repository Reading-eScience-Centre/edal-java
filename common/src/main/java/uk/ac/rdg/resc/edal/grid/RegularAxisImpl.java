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

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Immutable implementation of a {@link RegularAxis}, whose values are regularly
 * spaced along the axis.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public final class RegularAxisImpl extends AbstractReferenceableAxis<Double> implements RegularAxis {
    private double firstValue;
    /*
     * The axis spacing
     */
    private double spacing;
    /*
     * The number of points on the axis
     */
    private int size;
    private final boolean isLongitude;

    public RegularAxisImpl(String name, double firstValue, double spacing, int size,
            boolean isLongitude) {
        super(name);
        this.isLongitude = isLongitude;
        init(firstValue, spacing, size);
    }
    
    private void init(double firstValue, double spacing, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Axis length must not be negative or zero");
        }
        if (spacing == 0.0) {
            throw new IllegalArgumentException("Axis spacing cannot be zero");
        }
        this.firstValue = firstValue;
        this.spacing = spacing;
        this.size = size;
    }

    @Override
    public double getCoordinateSpacing() {
        return spacing;
    }

    @Override
    public Double getCoordinateValue(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(index + " must be between 0 and " + (size - 1));
        }
        return firstValue + index * spacing;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isAscending() {
        return spacing > 0.0;
    }

    @Override
    public String toString() {
        return String.format("Regular axis: %s, %f, %f, %d", getName(), firstValue, spacing, size);
    }

    @Override
    public Extent<Double> getCoordinateBounds(int index) {
        double centre = getCoordinateValue(index);
        return Extents.newExtent(centre - 0.5 * spacing, centre + 0.5 * spacing);
    }

    @Override
    public int findIndexOf(Double position) {
        if (isLongitude) {
            position = GISUtils.getNextEquivalentLongitude(getCoordinateExtent().getLow(),
                    position);
        }
        /*
         * This method will generally be faster than an exhaustive search, or
         * even a binary search
         */

        /* We find the (non-integer) index of the given value */
        Double indexDbl = getIndex(position);
        /* We round to the nearest integer */
        int index = (int) Math.round(indexDbl);
        if (index < 0 || index >= size)
            return -1;
        return index;
    }

    /*
     * Gets the index of the given value as a double-precision number that is
     * not necessarily an integer.
     */
    private Double getIndex(Double value) {
        return (value - firstValue) / spacing;
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
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(firstValue);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + (isLongitude ? 1231 : 1237);
        result = prime * result + size;
        temp = Double.doubleToLongBits(spacing);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        RegularAxisImpl other = (RegularAxisImpl) obj;
        if (Double.doubleToLongBits(firstValue) != Double.doubleToLongBits(other.firstValue))
            return false;
        if (isLongitude != other.isLongitude)
            return false;
        if (size != other.size)
            return false;
        if (Double.doubleToLongBits(spacing) != Double.doubleToLongBits(other.spacing))
            return false;
        return true;
    }
}
