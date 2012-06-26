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

package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;

/**
 * Skeletal implementation of a GridValuesMatrix that holds data in memory. All
 * operations can be based on readPoint() without the loss of much efficiency
 * and the close() operation has no effect.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public abstract class InMemoryGridValuesMatrix<E> extends AbstractGridValuesMatrix<E> {
    @Override
    public GridValuesMatrix<E> doReadBlock(final int[] mins, final int[] maxes) {
        int[] sizes = new int[mins.length];
        final GridAxis[] axes = new GridAxis[mins.length];
        for (int i = 0; i < mins.length; i++) {
            sizes[i] = maxes[i] - mins[i] + 1;
            if (getAxis(i) == null)
                axes[i] = null;
            else
                axes[i] = new GridAxisImpl(this.getAxis(i).getName(), sizes[i]);
        }

        // This GridValuesMatrix wraps the parent one, without allocating new
        // storage
        return new InMemoryGridValuesMatrix<E>() {
            @Override
            public E doReadPoint(int[] indices) {
                for (int i = 0; i < indices.length; i++) {
                    indices[i] += mins[i];
                }
                return InMemoryGridValuesMatrix.this.readPoint(indices);
            }

            @Override
            public Class<E> getValueType() {
                return InMemoryGridValuesMatrix.this.getValueType();
            }

            @Override
            protected GridAxis doGetAxis(int n) {
                return axes[n];
            }

            @Override
            public int getNDim() {
                return InMemoryGridValuesMatrix.this.getNDim();
            }
        };
    }
    
    @Override
    public void close() { /* Do nothing */
    }
}
