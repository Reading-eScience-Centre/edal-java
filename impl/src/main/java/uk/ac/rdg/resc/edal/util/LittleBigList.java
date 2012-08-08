package uk.ac.rdg.resc.edal.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of a BigList which cannot be bigger than a normal
 * list. It is for when you need a BigList, but don't want the hassle of
 * implementing the full BigList type because you aren't going to need the capacity
 * 
 * @author Guy Griffiths
 * 
 * @param <E>
 */
public class LittleBigList<E> extends ArrayList<E> implements BigList<E> {

    private static final long serialVersionUID = 1L;

    @Override
    public E get(long index) {
        return super.get((int) index);
    }

    @Override
    public List<E> getAll(final List<Long> indices) {
        return new AbstractList<E>() {
            @Override
            public E get(int index) {
                return LittleBigList.this.get(index);
            }

            @Override
            public int size() {
                return indices.size();
            }
        };
    }

    @Override
    public List<E> getAll(long fromIndex, long toIndex) {
        return super.subList((int) fromIndex, (int) toIndex);
    }

    @Override
    public long sizeAsLong() {
        return super.size();
    }

}
