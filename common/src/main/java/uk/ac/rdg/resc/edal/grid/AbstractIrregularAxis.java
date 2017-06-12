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

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * Partial implementation of an irregular axis.
 * 
 * @author Guy
 * @author Jon
 * 
 * @param <T>
 *            The type of value the axis contains
 */
public abstract class AbstractIrregularAxis<T extends Comparable<? super T>> extends
        AbstractReferenceableAxis<T> {
    private static final long serialVersionUID = 1L;
    /**
     * The axis values, always in ascending numerical order to simplify
     * searching
     */
    protected T[] axisValues;

    /**
     * True if axis values in the above array have been reversed
     */
    protected boolean reversed;

    protected AbstractIrregularAxis(String name, List<T> axisValues) {
        super(name);
        init(axisValues);
    }

    /**
     * Sets all the fields and checks that the axis values ascend or descend
     * monotonically, throwing an IllegalArgumentException if not.
     */
    private void init(List<T> axisValues) {
        if (axisValues.isEmpty()) {
            throw new IllegalArgumentException("Zero-length array");
        }

        if (axisValues.size() == 1) {
            @SuppressWarnings("unchecked")
            T[] array = axisValues.toArray((T[]) new Comparable[0]);
            this.axisValues = array;
            return;
        }

        /*
         * This is not recommended behaviour for Java (hence the
         * SuppressWarnings), but in this case it is a choice between this,
         * unnecessary abstraction, or heavy repetition of code.
         */
        @SuppressWarnings("unchecked")
        T[] vals = (T[]) new Comparable[axisValues.size()];
        this.axisValues = vals;

        reversed = axisValues.get(1).compareTo(axisValues.get(0)) < 0;
        if (reversed) {
            /*
             * Reverse the coordinates so that they are in increasing order
             */
            for (int i = 0; i < axisValues.size(); i++) {
                this.axisValues[i] = axisValues.get(axisValues.size() - 1 - i);
            }
        } else {
            axisValues.toArray(this.axisValues);
        }
        checkAscending();
    }

    /**
     * Checks that the axis values ascend or descend monotonically, throwing an
     * IllegalArgumentException if not.
     */
    protected void checkAscending() {
        T prevVal = axisValues[0];
        for (int i = 1; i < axisValues.length; i++) {
            if (axisValues[i].compareTo(prevVal) <= 0) {
                throw new IllegalArgumentException(
                        "Coordinate values must increase or decrease monotonically");
            }
            prevVal = axisValues[i];
        }
    }

    /*
     * If the array has been reversed, we need to reverse the index
     */
    private int maybeReverseIndex(int index) {
        if (reversed) {
            return axisValues.length - 1 - index;
        } else {
            return index;
        }
    }

    @Override
    public T getCoordinateValue(int index) {
        return axisValues[maybeReverseIndex(index)];
    }

    @Override
    public int findIndexOf(T value) {
        int index = Arrays.binarySearch(axisValues, value);
        if (index >= 0) {
            return maybeReverseIndex(index);
        } else {
            int insertionPoint = -(index + 1);
            if (insertionPoint == 0) {
                if (getCoordinateBounds(0).contains(value)) {
                    return maybeReverseIndex(0);
                } else {
                    return -1;
                }
            }
            if (insertionPoint == axisValues.length) {
                if (getCoordinateBounds(axisValues.length - 1).contains(value)) {
                    return maybeReverseIndex(axisValues.length - 1);
                } else {
                    return -1;
                }
            }
            if (Math.abs(difference(axisValues[insertionPoint], value)) < Math.abs(difference(
                    axisValues[insertionPoint - 1], value))) {
                return maybeReverseIndex(insertionPoint);
            } else {
                return maybeReverseIndex(insertionPoint - 1);
            }
        }
    }

    @Override
    public int size() {
        return axisValues.length;
    }

    @Override
    public boolean isAscending() {
        return !reversed;
    }

    @Override
    public Extent<T> getCoordinateBounds(int index) {
        int upperIndex = index + 1;
        int lowerIndex = index - 1;
        T lowerBound;
        if (index == 0) {
            lowerBound = getCoordinateExtent().getLow();
        } else {
            lowerBound = getMidpoint(axisValues[index], axisValues[lowerIndex]);
        }

        T upperBound;
        if (index == size() - 1) {
            upperBound = getCoordinateExtent().getHigh();
        } else {
            upperBound = getMidpoint(axisValues[upperIndex], axisValues[index]);
        }

        return Extents.newExtent(lowerBound, upperBound);
    }

    /**
     * Returns the midpoint of the supplied values. This is so that certain
     * methods can be abstracted without worrying about non-trivial midpoint
     * calculation (e.g. {@link DateTime}s)
     * 
     * @param pos1
     *            the first value
     * @param pos2
     *            the second value
     * @return the midpoint of the values
     */
    protected abstract T getMidpoint(T pos1, T pos2);

    /**
     * Returns the difference between the supplied values. This is so that
     * certain methods can be abstracted without worrying about non-trivial
     * difference calculation (e.g. {@link DateTime}s)
     * 
     * @param pos1
     *            the first value
     * @param pos2
     *            the second value
     * @return the value corresponding to pos1 - pos2
     */
    protected abstract double difference(T pos1, T pos2);

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(axisValues);
        result = prime * result + (reversed ? 1231 : 1237);
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
        AbstractIrregularAxis<?> other = (AbstractIrregularAxis<?>) obj;
        if (!Arrays.equals(axisValues, other.axisValues))
            return false;
        if (reversed != other.reversed)
            return false;
        return true;
    }
}
