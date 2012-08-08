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
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.util.AbstractBigList;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * Skeletal implementation of {@link GridValuesMatrix}.
 * 
 * @param <E>
 *            The type of the values contained in the grid
 * @author Jon
 * @author Guy Griffiths
 */
public abstract class AbstractGridValuesMatrix<E> extends AbstractGrid implements
        GridValuesMatrix<E> {

    @Override
    public final GridAxis getAxis(int n) {
        if (n >= getNDim() || n < 0) {
            throw new IllegalArgumentException("We only have " + getNDim()
                    + " axes, so you may only request from 0 to " + (getNDim() - 1));
        }
        return doGetAxis(n);
    }

    protected abstract GridAxis doGetAxis(int n);

    @Override
    public final E readPoint(int[] coords) {
        if (coords.length != getNDim()) {
            throw new IllegalArgumentException("Number of co-ordinates supplied (" + coords.length
                    + ") must be equal to the number of dimensions (" + getNDim() + ")");
        }
        return doReadPoint(coords);
    }

    protected abstract E doReadPoint(int[] coords);

    @Override
    public final GridValuesMatrix<E> readBlock(int[] mins, int[] maxes) {
        if (mins.length != getNDim() || mins.length != maxes.length) {
            throw new IllegalArgumentException("Number of minimum (" + mins.length
                    + ") and maximum (" + maxes.length
                    + ") values must both be equal to the number of dimensions (" + getNDim() + ")");
        }
        return doReadBlock(mins, maxes);
    }

    protected abstract GridValuesMatrix<E> doReadBlock(int[] mins, int[] maxes);

    /**
     * Returns a BigList that uses this GridValuesMatrix to obtain values. Note
     * that none of the methods in this BigList close the parent
     * GridValuesMatrix, so users must be careful to close the GridValuesMatrix
     * when the BigList is no longer required.
     * 
     * @todo implement getAll() based on something more efficient than repeated
     *       calls to get().
     * @return
     */
    @Override
    public BigList<E> getValues() {
        return new AbstractBigList<E>() {
            @Override
            public E get(long index) {
                GridCoordinates coords = getCoords(index);
                E value = readPoint(coords.getIndices());
                return value;
            }

            @Override
            public long sizeAsLong() {
                return AbstractGridValuesMatrix.this.size();
            }
        };
    }

}
