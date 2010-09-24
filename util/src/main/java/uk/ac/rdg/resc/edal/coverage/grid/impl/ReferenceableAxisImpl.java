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

import java.util.Arrays;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableAxis;

/**
 * Immutable implementation of a {@link ReferenceableAxis}, whose values are
 * not necessarily regularly spaced along the axis.
 * @author Jon
 */
public final class ReferenceableAxisImpl extends AbstractReferenceableAxis
{
    /** The axis values, always in ascending numerical order to simplify searching */
    private double[] axisValues;
    
    /** True if axis values in the above array have been reversed */
    private boolean reversed = false;


    /**
     * Creates a ReferenceableAxis from the given array of axis values.  The
     * axis values are copied to internal data structures, therefore subsequent
     * modifications to the array of axis values have no effect on this object.
     * @param name The name of the axis
     * @param axisValues Array of axis values; must be in strictly ascending
     * numerical order
     * @param isLongitude True if this is a longitude axis in degrees (hence
     * values of 0 and 360 are equivalent).
     * @throws NullPointerException if {@code axisValues} is null
     * @throws IllegalArgumentException if the axis values are not in strictly
     * ascending numerical order, or if the array of axis values is empty
     */
    public ReferenceableAxisImpl(String name, double[] axisValues, boolean isLongitude)
    {
        super(name, isLongitude);
        this.setup(axisValues);
    }

    /**
     * Creates a ReferenceableAxis from the given array of axis values.  The
     * axis values are copied to internal data structures, therefore subsequent
     * modifications to the array of axis values have no effect on this object.
     * @param axis The coordinate system axis to which values on this axis
     * are referenceable.
     * @param axisValues Array of axis values; must be in strictly ascending
     * numerical order
     * @param isLongitude True if this is a longitude axis in degrees (hence
     * values of 0 and 360 are equivalent).
     * @throws NullPointerException if {@code axisValues} is null
     * @throws IllegalArgumentException if the axis values are not in strictly
     * ascending numerical order, or if the array of axis values is empty
     */
    public ReferenceableAxisImpl(CoordinateSystemAxis axis, double[] axisValues, boolean isLongitude)
    {
        super(axis, isLongitude);
        this.setup(axisValues);
    }

    /**
     * Sets all the fields and checks that the axis values ascend or descend
     * monotonically, throwing an IllegalArgumentException if not.
     */
    private void setup(double[] axisValues)
    {
        if (axisValues.length == 0) {
            throw new IllegalArgumentException("Zero-length array");
        }

        if (axisValues.length == 1) {
            this.axisValues = axisValues.clone();
            return;
        }

        this.reversed = axisValues[1] < axisValues[0];
        if (this.reversed) {
            // Copy out the array in reverse order
            this.axisValues = new double[axisValues.length];
            for (int i = 0; i < axisValues.length; i++) {
                this.axisValues[i] = axisValues[axisValues.length - 1 - i];
            }
        } else {
            this.axisValues = axisValues.clone();
        }

        this.checkAscending();
    }

    /**
     * Checks that the axis values ascend or descend
     * monotonically, throwing an IllegalArgumentException if not.
     */
    private void checkAscending()
    {
        double prevVal = axisValues[0];
        for (int i = 1; i < this.axisValues.length; i++)
        {
            if (this.axisValues[i] <= prevVal)
            {
                throw new IllegalArgumentException("Coordinate values must increase or decrease monotonically");
            }
            prevVal = this.axisValues[i];
        }
    }

    @Override
    public double getCoordinateValue(int index) {
        return this.axisValues[this.maybeReverseIndex(index)];
    }

    /** If the array has been reversed, we need to reverse the index */
    private int maybeReverseIndex(int index) {
        if (this.reversed) return this.axisValues.length - 1 - index;
        else return index;
    }

    @Override
    protected int doGetCoordinateIndex(double value) {
        // Do a binary search for the coordinate value
        int index = Arrays.binarySearch(this.axisValues, value);
        return index >= 0 ? this.maybeReverseIndex(index) : -1;
    }

    @Override
    protected int doGetNearestCoordinateIndex(double value) {
        // The axis values are in ascending order so we can use a binary search
        int index = Arrays.binarySearch(this.axisValues, value);

        if (index < 0)
        {
            // No exact match, but we have the insertion point, i.e. the index of
            // the first element that is greater than the target value
            int insertionPoint = -(index + 1);

            // Deal with the extremes
            if (insertionPoint == 0) {
                index = 0;
            } else if (insertionPoint == this.axisValues.length) {
                index = this.axisValues.length - 1;
            } else {
                // We need to work out which index is closer: insertionPoint or
                // (insertionPoint - 1)
                double d1 = Math.abs(value - this.axisValues[insertionPoint]);
                double d2 = Math.abs(value - this.axisValues[insertionPoint - 1]);
                if (d1 < d2) index = insertionPoint;
                else index = insertionPoint - 1;
            }
        }

        return this.maybeReverseIndex(index);
    }

    @Override
    public int getSize() {
        return this.axisValues.length;
    }

    @Override
    protected boolean isAscending() {
        return !this.reversed;
    }

}
