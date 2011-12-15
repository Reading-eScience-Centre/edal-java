package uk.ac.rdg.resc.edal.util;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

public class FastFloatList extends AbstractList<Float> {

    float[] data;

    /**
     * This is a fast array-backed list.
     * 
     * @param size
     *            the FINAL size of the {@link List}
     */
    public FastFloatList(int size) {
        data = new float[size];
        Arrays.fill(data, Float.NaN);
    }

    @Override
    public Float get(int index) {
        return Float.isNaN(data[index]) ? null : data[index];
    }

    @Override
    /**
     * This implementation always returns null for speed reasons
     */
    public Float set(int index, Float element) {
        data[index] = element == null ? Float.NaN : element.floatValue();
        return null;
    }

    @Override
    public int size() {
        return data.length;
    }
}
