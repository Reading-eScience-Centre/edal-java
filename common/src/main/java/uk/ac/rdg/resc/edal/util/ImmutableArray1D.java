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

import uk.ac.rdg.resc.edal.exceptions.ArrayAccessException;

public class ImmutableArray1D<T> extends Array1D<T> {
    private static final long serialVersionUID = 1L;
    private final T[] data;

    /**
     * Create a new in-memory array.
     * 
     * @param values
     *            An array containing the data.
     */
    public ImmutableArray1D(T[] values) {
        super(values.length);
        this.data = values;
    }

    @Override
    public T get(int... coords) {
        if (coords.length != 1) {
            throw new ArrayAccessException("This Array is 1-dimensional, but you have asked for "
                    + coords.length + " dimensions");
        }
        return data[coords[0]];
        
        /*
         * General method for nD arrays
         */
//        for (int i = coords.length - 1; i >= 0; i--) {
//            int stepIndex = coords[i];
//            int multiplier = 1;
//            for (int j = i + 1; j < coords.length; j++) {
//                multiplier *= shape[j];
//            }
//            index += stepIndex * multiplier;
//        }
//
//        return data[index];
    }

    @Override
    public void set(T value, int... coords) {
        throw new UnsupportedOperationException("This Array1D is immutable.");
    }
}
