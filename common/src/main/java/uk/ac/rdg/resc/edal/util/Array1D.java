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

public abstract class Array1D implements Array<Number> {

    private int size;

    public Array1D(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Size must be at least 1");
        }
        this.size = size;
    }

    @Override
    public final int getNDim() {
        return 1;
    }

    @Override
    public Iterator<Number> iterator() {
        return new Iterator<Number>() {
            private int counter = 0;

            boolean done = false;

            @Override
            public boolean hasNext() {
                return (!done);
            }

            @Override
            public Number next() {
                Number value = get(counter);
                /*
                 * Increment the counters if necessary, resetting to zero if
                 * necessary
                 */
                counter++;
                if (counter >= size) {
                    done = true;
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
    public Class<Number> getValueClass() {
        return Number.class;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public int[] getShape() {
        return new int[]{size};
    }
}
