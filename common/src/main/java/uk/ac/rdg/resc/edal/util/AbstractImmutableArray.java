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

import java.util.Iterator;

public abstract class AbstractImmutableArray<T> implements Array<T> {

    private Class<T> clazz;
    protected int[] shape;

    /**
     * Instantiate a new {@link AbstractImmutableArray}
     * 
     * @param clazz
     *            The class of the objects in this {@link Array}. Because of
     *            type erasure we need to supply it to the constructor. The
     *            other ways to do it are to either allow every subclass to
     *            implement {@link Array#getValueClass()} itself (more code), or
     *            use some rather complicated reflection (overkill)
     * @param shape
     *            The shape of the {@link Array}. The final dimension varies
     *            fastest. In GIS applications, the standard order for
     *            co-ordinates is T, Z, Y, X
     */
    public AbstractImmutableArray(Class<T> clazz, int... shape) {
        this.clazz = clazz;
        this.shape = shape;
    }

    @Override
    public int getNDim() {
        return shape.length;
    }

    @Override
    public int[] getShape() {
        return shape;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            boolean itemsRemaining = true;
            int[] indices = new int[getNDim()];

            @Override
            public boolean hasNext() {
                return itemsRemaining;
            }

            @Override
            public T next() {
                T ret = get(indices);
                for (int dim = getNDim() - 1; dim >= 0; dim--) {
                    indices[dim]++;
                    if (indices[dim] >= shape[dim]) {
                        indices[dim] = 0;
                        if (dim == 0) {
                            itemsRemaining = false;
                        }
                    } else {
                        break;
                    }
                }
                return ret;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException(
                        "Cannot remove values - this Array is immutable");
            }
        };
    }

    @Override
    public void set(T value, int... coords) {
        throw new UnsupportedOperationException("Cannot set values - this Array is immutable");
    }

    @Override
    public long size() {
        long size = 1L;
        for (int dim = 0; dim < getNDim(); dim++) {
            size *= shape[dim];
        }
        return size;
    }

    @Override
    public Class<T> getValueClass() {
        return clazz;
    }
}
