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

public abstract class Array4D<T> implements Array<T> {

    private int[] shape = new int[4];

    protected static final int X_IND = 3;
    protected static final int Y_IND = 2;
    protected static final int Z_IND = 1;
    protected static final int T_IND = 0;

    public Array4D(int tSize, int zSize, int ySize, int xSize) {
        if (xSize < 1 || ySize < 1 || zSize < 1 || tSize < 1) {
            throw new IllegalArgumentException("All dimension sizes must be at least 1");
        }
        shape[X_IND] = xSize;
        shape[Y_IND] = ySize;
        shape[Z_IND] = zSize;
        shape[T_IND] = tSize;
    }

    @Override
    public final int getNDim() {
        return 4;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int xCounter = 0;
            private int yCounter = 0;
            private int zCounter = 0;
            private int tCounter = 0;

            boolean done = false;

            @Override
            public boolean hasNext() {
                return (!done);
            }

            @Override
            public T next() {
                T value = get(tCounter, zCounter, yCounter, xCounter);
                /*
                 * Increment the counters, resetting to zero if necessary
                 */
                if (++xCounter >= shape[X_IND]) {
                    xCounter = 0;
                    if (++yCounter >= shape[Y_IND]) {
                        yCounter = 0;
                        if (++zCounter >= shape[Z_IND]) {
                            zCounter = 0;
                            if (++tCounter >= shape[T_IND]) {
                                tCounter = 0;
                                done = true;
                            }
                        }
                    }
                }
                return value;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove is not supported for this iterator");
            }
        };
    }

    @Override
    public long size() {
        return shape[X_IND] * shape[Y_IND] * shape[Z_IND] * shape[T_IND];
    }

    @Override
    /**
     * Returns the shape of the array.  The elements correspond to:
     * 
     * getShape()[0] = Size along time dimension
     * getShape()[1] = Size along z dimension
     * getShape()[2] = Size along y dimension
     * getShape()[3] = Size along x dimension
     */
    public int[] getShape() {
        return shape;
    }

    /**
     * Convenience method to get the size in the x-direction
     */
    public int getXSize() {
        return shape[X_IND];
    }

    /**
     * Convenience method to get the size in the y-direction
     */
    public int getYSize() {
        return shape[Y_IND];
    }

    /**
     * Convenience method to get the size in the z-direction
     */
    public int getZSize() {
        return shape[Z_IND];
    }

    /**
     * Convenience method to get the size in the t-direction
     */
    public int getTSize() {
        return shape[T_IND];
    }
}
