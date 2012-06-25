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

import uk.ac.rdg.resc.edal.coverage.grid.Grid;
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.coverage.grid.GridExtent;

/**
 * Partial implementation of a {@link Grid}
 * 
 * @author Guy Griffiths
 * 
 */
public abstract class AbstractGrid implements Grid {

    @Override
    public long size() {
        // We reuse code in GridExtentImpl to calculate the size
        return GridExtentImpl.convert(getGridExtent()).size();
    }

    @Override
    public GridExtent getGridExtent() {
        int[] low = new int[getNDim()];
        int[] high = new int[getNDim()];
        for (int i = 0; i < getNDim(); i++) {
            GridAxis axis = getAxis(i);
            if (axis == null) {
                low[i] = -1;
                high[i] = -1;
            } else {
                low[i] = axis.getIndexExtent().getLow();
                high[i] = axis.getIndexExtent().getHigh();
            }
        }
        return new GridExtentImpl(new GridCoordinatesImpl(low), new GridCoordinatesImpl(high));
    }

    @Override
    public GridCoordinates getCoords(long index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Index must be between 0 and " + size());
        }
        int[] coords = new int[getNDim()];
        for (int i = 0; i < getNDim(); i++) {
            int size = 1;
            if (getAxis(i) != null) {
                size = getAxis(i).size();
            }
            coords[i] = (int) (index % size) + getMin(i);

            index = (index - coords[i]) / getAxisSize(i);
        }
        return new GridCoordinatesImpl(coords);
    }

    @Override
    public long getIndex(GridCoordinates coords) {
        long index = 0;
        for (int i = 0; i < getNDim(); i++) {
            if (!getAxis(i).getIndexExtent().contains(coords.getIndex(i))) {
                throw new IndexOutOfBoundsException("Index out of bounds on axis " + i);
            }

            int thisIndex = coords.getIndex(i) - getMin(i);

            int size = 1;
            for (int j = 0; j < i; j++) {
                size *= getAxisSize(j);
            }
            index += thisIndex * size;
        }
        return index;
    }

    private int getMin(int dim) {
        if (getAxis(dim) == null)
            return 0;
        return this.getAxis(dim).getIndexExtent().getLow();
    }

    private int getAxisSize(int dim) {
        if (getAxis(dim) == null)
            return 1;
        return this.getAxis(dim).size();
    }

    @Override
    public long getIndex(int... indices) {
        return getIndex(new GridCoordinatesImpl(indices));
    }
}
