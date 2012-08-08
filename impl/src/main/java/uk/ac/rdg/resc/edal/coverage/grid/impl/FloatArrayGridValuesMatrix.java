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

import java.util.Arrays;

import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;

/**
 * A {@link GridValuesMatrix} that stores its values internally in an array of
 * float primitives. Values are mutable.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public final class FloatArrayGridValuesMatrix extends InMemoryGridValuesMatrix<Float> {
    private final float[] vals;
    private final GridAxis xAxis;
    private final GridAxis yAxis;

    public FloatArrayGridValuesMatrix(int ni, int nj) {
        int size = ni * nj;
        if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Grid too large");
        }
        this.vals = new float[size];
        this.xAxis = new GridAxisImpl("x", ni);
        this.yAxis = new GridAxisImpl("y", nj);
    }

    @Override
    public int getNDim() {
        return 2;
    }

    @Override
    public GridAxis doGetAxis(int n) {
        switch (n) {
        case 0:
            return xAxis;
        case 1:
            return yAxis;
        default:
            /*
             * We should never reach this code, because getAxis will already
             * have checked the bounds
             */
            throw new IllegalStateException("Axis index out of bounds");
        }
    }

    @Override
    protected Float doReadPoint(int[] coords) {
        return this.get(coords[0], coords[1]);
    }

    /**
     * Reads a value from the grid as a float primitive (avoids autoboxing)
     */
    public float get(int i, int j) {
        int index = (int) this.getIndex(i, j);
        return this.vals[index];
    }

    /**
     * Reads a value from the grid as a float primitive (avoids autoboxing)
     * 
     * @throws IndexOutOfBoundsException
     *             if the given grid coordinates are out of range
     */
    public void set(int i, int j, float value) {
        int index = (int) this.getIndex(i, j);
        this.vals[index] = value;
    }

    @Override
    public Class<Float> getValueType() {
        return float.class;
    }

    /**
     * Beware that the hashcode will change if the values of the matrix are
     * changed!
     */
    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + this.xAxis.size();
        hash = hash * 31 + this.yAxis.size();
        hash = hash * 31 + Arrays.hashCode(this.vals);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof FloatArrayGridValuesMatrix))
            return false;
        FloatArrayGridValuesMatrix other = (FloatArrayGridValuesMatrix) obj;

        // We only need to use the x axis size and the array of values to check
        // for equality. If the y axis size were different then the arrays
        // would not be the same length.
        return this.xAxis.size() == other.xAxis.size() && Arrays.equals(this.vals, other.vals);
    }

}
