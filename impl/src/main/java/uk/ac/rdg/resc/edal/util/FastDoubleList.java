package uk.ac.rdg.resc.edal.util;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

public class FastDoubleList extends AbstractList<Double> {

    double[] data;
    
    /**
     * This is a fast array-backed list.
     * 
     * @param size
     *            the FINAL size of the {@link List}
     */
    public FastDoubleList(int size) {
        data = new double[size];
        Arrays.fill(data, Double.NaN);
    }
    
    @Override
    public Double get(int index) {
        return data[index] == Double.NaN ? null : data[index];
    }

    @Override
    /**
     * This implementation always returns null for speed reasons
     */
    public Double set(int index, Double element) {
        data[index] = element == null ? Double.NaN : (double) element;
        return null;
    }
    
    @Override
    public int size() {
        return data.length;
    }

}
