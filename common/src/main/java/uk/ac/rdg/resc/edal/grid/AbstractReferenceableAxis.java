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

import java.util.AbstractList;
import java.util.List;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.util.AbstractImmutableArray;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * Abstract superclass for {@link ReferenceableAxis} implementations. Handles
 * the tricky case of searching for longitude values in the axis (longitude
 * values wrap around the globe).
 * 
 * @author Jon
 * @author Guy
 */
public abstract class AbstractReferenceableAxis<T extends Comparable<? super T>> implements
        ReferenceableAxis<T> {

    private final String name;

    /**
     * Creates an axis with the given name.
     */
    protected AbstractReferenceableAxis(String name) {
        this.name = name;
    }

    /** Gets the value of the axis at index 0 */
    protected final T getFirstValue() {
        return getCoordinateValue(0);
    }

    /**
     * Gets the value of the axis at index (size - 1)
     */
    protected final T getLastValue() {
        return getCoordinateValue(this.size() - 1);
    }

    /**
     * Returns the minimum coordinate value of this axis. This will be the first
     * coordinate value if the coordinate values are in ascending order, or the
     * last coordinate value if the coordinate values are in descending order.
     * 
     * @return the minimum coordinate value of this axis
     */
    protected final T getMinimumValue() {
        return isAscending() ? getFirstValue() : getLastValue();
    }

    /**
     * Returns the maximum coordinate value of this axis. This will be the last
     * coordinate value if the coordinate values are in ascending order, or the
     * first coordinate value if the coordinate values are in descending order.
     * 
     * @return the maximum coordinate value of this axis
     */
    protected final T getMaximumValue() {
        return isAscending() ? getLastValue() : getFirstValue();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Extent<T> getCoordinateExtent() {
        final T min;
        final T max;
        if (size() == 1) {
            min = getMinimumValue();
            max = getMaximumValue();
        } else {
            T val1 = extendFirstValue(getFirstValue(), getCoordinateValue(1));
            T val2 = extendLastValue(getLastValue(), getCoordinateValue(size() - 2));
            if (this.isAscending()) {
                min = val1;
                max = val2;
            } else {
                min = val2;
                max = val1;
            }
        }
        return Extents.newExtent(min, max);
    }

    @Override
    public List<T> getCoordinateValues() {
        List<T> ret = new AbstractList<T>() {
            @Override
            public T get(int i) {
                return getCoordinateValue(i);
            }

            @Override
            public int size() {
                return AbstractReferenceableAxis.this.size();
            }
        };
        return ret;
    }
    

    @SuppressWarnings("unchecked")
    @Override
    public Array<Extent<T>> getDomainObjects() {
        return new AbstractImmutableArray<Extent<T>>((Class<Extent<T>>) (Class<?>) Extent.class, new int[] { size() }) {
            @Override
            public Extent<T> get(int... coords) {
                /*
                 * coords is 1D (shape set in constructor), so this is fine
                 */
                return getCoordinateBounds(coords[0]);
            }
        };
    }
    
    @Override
    public boolean contains(T position) {
        for(Extent<T> extent : getDomainObjects()) {
            if(extent.contains(position)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This should return the lower bound of the first value of the axis, based
     * on the first and second values. This will generally be equivalent to:
     * <p>
     * firstVal - (nextVal-firstVal)/2
     * 
     * @param firstVal
     * @param nextVal
     * @return
     */
    protected abstract T extendFirstValue(T firstVal, T nextVal);

    /**
     * This should return the upper bound of the last value of the axis, based
     * on the last two values. This will generally be equivalent to:
     * <p>
     * lastVal + (lastVal-secondLastVal)/2
     * 
     * @param firstVal
     * @param nextVal
     * @return
     */
    protected abstract T extendLastValue(T lastVal, T secondLastVal);

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return true;
    }
}
