/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.Arrays;
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;

/**
 * A {@link GridValuesMatrix} that stores its values internally in an array
 * of float primitives.  Values are mutable th
 * @author Jon
 */
public final class FloatArrayGridValuesMatrix extends InMemoryGridValuesMatrix<Float>
{
    private final float[] vals;
    private final GridAxis xAxis;
    private final GridAxis yAxis;

    public FloatArrayGridValuesMatrix(int ni, int nj)
    {
        int size = ni * nj;
        if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Grid too large");
        }
        this.vals = new float[size];
        this.xAxis = new GridAxisImpl("x", ni);
        this.yAxis = new GridAxisImpl("y", nj);
    }

    @Override
    public GridAxis getXAxis() {
        return this.xAxis;
    }

    @Override
    public GridAxis getYAxis() {
        return this.yAxis;
    }

    @Override
    public Float readPoint(int i, int j) {
        return this.get(i, j);
    }
    
    /**
     * Reads a value from the grid as a float primitive (avoids autoboxing)
     */
    public float get(int i, int j) {
        int index = (int)this.getIndex(i, j);
        return this.vals[index];
    }
    
    /**
     * Reads a value from the grid as a float primitive (avoids autoboxing)
     * @throws IndexOutOfBoundsException if the given grid coordinates are
     * out of range
     */
    public void set(int i, int j, float value) {
        int index = (int)this.getIndex(i, j);
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
        if (obj == this) return true;
        if (!(obj instanceof FloatArrayGridValuesMatrix)) return false;
        FloatArrayGridValuesMatrix other = (FloatArrayGridValuesMatrix)obj;
        
        // We only need to use the x axis size and the array of values to check
        // for equality.  If the y axis size were different then the arrays
        // would not be the same length.
        return this.xAxis.size() == other.xAxis.size() &&
               Arrays.equals(this.vals, other.vals);
    }

}
