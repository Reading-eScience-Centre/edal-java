/*
 * Copyright (c) 2010 The University of Reading
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
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR  CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package uk.ac.rdg.resc.edal.coverage.grid.impl;

import org.opengis.referencing.cs.CoordinateSystemAxis;
import uk.ac.rdg.resc.edal.coverage.grid.RegularAxis;

/**
 * Immutable implementation of a {@link RegularAxis}, whose values are regularly
 * spaced along the axis.
 * @author Jon
 */
public final class RegularAxisImpl extends AbstractReferenceableAxis
        implements RegularAxis
{
    private double firstValue;
    private double spacing; // The axis spacing
    private int size; // The number of points on the axis

    public RegularAxisImpl(CoordinateSystemAxis axis, double firstValue,
            double spacing, int size, boolean isLongitude)
    {
        super(axis, isLongitude);
        this.setParams(firstValue, spacing, size);
    }

    public RegularAxisImpl(String name, double firstValue,
            double spacing, int size, boolean isLongitude)
    {
        super(name, isLongitude);
        this.setParams(firstValue, spacing, size);
    }

    private void setParams(double firstValue, double spacing, int size)
    {
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
    public double getCoordinateSpacing() { return this.spacing; }

    @Override
    public double getCoordinateValue(int index) {
        if (index < 0 || index >= this.size) {
            throw new IndexOutOfBoundsException(index + " must be between 0 and "
                + (this.size - 1));
        }
        return this.firstValue + index * this.spacing;
    }

    @Override
    protected int doGetCoordinateIndex(double value) {
        // This method will generally be faster than an exhaustive search, or
        // even a binary search
        
        // We find the (non-integer) index of the given value
        double indexDbl = getIndex(value);

        // We find the nearest integer indices on either side of this and compare
        // the corresponding values with the target value.  We do this so that we
        // are not sensitive to rounding errors
        {
            int indexAbove = (int)Math.ceil(indexDbl);
            if (indexMatchesValue(indexAbove, value)) return indexAbove;
        }

        {
            int indexBelow = (int)Math.floor(indexDbl);
            if (indexMatchesValue(indexBelow, value)) return indexBelow;
        }

        // Neither of the indices matched the target value
        return -1;
    }

    /**
     * Gets the index of the given value as a double-precision number that is
     * not necessarily an integer.
     */
    private double getIndex(double value) {
        return (value - this.firstValue) / this.spacing;
    }

    private boolean indexMatchesValue(int index, double value) {
        if (index < 0 || index >= this.size) return false;
        return Double.compare(value, this.getCoordinateValue(index)) == 0;
    }

    @Override
    protected int doGetNearestCoordinateIndex(double value) {
        // We find the (non-integer) index of the given value
        double indexDbl = getIndex(value);
        // We round to the nearest integer
        int index = (int)Math.round(indexDbl);
        // Check the extremes (probably not strictly necessary?)
        if (index < 0) return 0;
        if (index >= this.size) return this.size - 1;
        return index;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    protected boolean isAscending() {
        return this.spacing > 0.0;
    }

    @Override
    public String toString() {
        return String.format("Regular axis: %s, %f, %f, %d", this.getName(),
                this.firstValue, this.spacing, this.size);
    }

}
