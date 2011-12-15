package uk.ac.rdg.resc.edal.util;

import java.util.AbstractList;
import java.util.Arrays;

public class ArrayBackedList<R> extends AbstractList<R> {

    private R[] data;
    
    @SuppressWarnings("unchecked")
    public ArrayBackedList(int size) {
        data = (R[]) new Object[size];
        Arrays.fill(data, null);
    }
    
    @Override
    public R get(int index) {
        return data[index];
    }
    
    /**
     * This implementation always returns null for speed reasons
     */
    public R set(int index, R element) {
        data[index] = element;
        return null;
    }

    @Override
    public int size() {
        return data.length;
    }

}
