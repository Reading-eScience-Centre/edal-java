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

package uk.ac.rdg.resc.edal.coverage.grid;

import uk.ac.rdg.resc.edal.util.BigList;

/**
 * A {@link Grid} that contains values
 * 
 * @param <E>
 *            The type of values in the matrix
 * @author Jon
 */
public interface GridValuesMatrix<E> extends Grid {

    /**
     * Reads a single point from the grid. For disk-based GridValuesMatrixes it
     * will usually be more efficient to call readBlock() to read data in bulk.
     * 
     * @throws IndexOutOfBoundsException
     *             any index is out of bounds
     */
    public E readPoint(int[] coords);

    /**
     * Returns an in-memory GridValuesMatrix holding values from the given
     * subset of this object.
     */
    public GridValuesMatrix<E> readBlock(int[] mins, int[] maxes);

    /**
     * Returns a representation of the values in this object as a BigList. For
     * disk-based GridValuesMatrixes, it will not usually be efficient to use
     * BigList.get() to read values. Instead use BigList.getAll() or
     * this.readBlock().getValues() to get an in-memory structure.
     */
    public BigList<E> getValues();

    /**
     * @return the type of the values which this {@link GridValuesMatrix}
     *         contains.
     */
    public Class<E> getValueType();

    /**
     * Frees any resources associated with the grid.
     * <p>
     * For disk-based data storage, this will close any open file handles, after
     * which the GridValuesMatrix will no longer return any values. The user
     * must then retrieve a new GridValuesMatrix from the coverage in question.
     * </p>
     * <p>
     * For in-memory data storage, the call to close() may be ignored.
     * </p>
     */
    public void close();
}
