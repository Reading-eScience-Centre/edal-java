package uk.ac.rdg.resc.edal.util;

import java.util.Iterator;

public abstract class AbstractImmutableArray<T> implements Array<T> {

    private int[] shape;

    public AbstractImmutableArray(int[] shape) {
        this.shape = shape;
    }

    @Override
    public int getNDim() {
        return shape.length;
    }

    @Override
    public int[] getShape() {
        return shape;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int[] indices = new int[getNDim()];

            @Override
            public boolean hasNext() {
                for (int dim = 0; dim < getNDim(); dim++) {
                    if (indices[dim] < shape[dim]) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public T next() {
                T ret = get(indices);
                for (int dim = getNDim(); dim >= 0; dim--) {
                    indices[dim]++;
                    if (indices[dim] > shape[dim]) {
                        indices[dim] = 0;
                    } else {
                        break;
                    }
                }
                return ret;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException(
                        "Cannot remove values - this Array is immutable");
            }
        };
    }

    @Override
    public void set(T value, int... coords) {
        throw new UnsupportedOperationException("Cannot set values - this Array is immutable");
    }

    @Override
    public long size() {
        long size = 1L;
        for (int dim = 0; dim < getNDim(); dim++) {
            size *= shape[dim];
        }
        return size;
    }
}
