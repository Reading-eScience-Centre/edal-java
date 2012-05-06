/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Skeletal class providing a partial implementation of {@link BigList}.
 * @param <E> The type of the elements in the list
 * @author Jon
 */
public abstract class AbstractBigList<E> extends AbstractList<E> implements BigList<E> {
    
    @Override
    public final E get(int index) {
        return this.get((long)index);
    }
    
    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }
    
    /**
     * {@inheritDoc}
     * <p>This default implementation simply calls {@link #get(long)} for each
     * index in the provided List.  Subclasses may wish to provide a more 
     * efficient implementation.</p>
     */
    @Override
    public List<E> getAll(List<Long> indices) {
        List<E> all = new ArrayList<E>(indices.size());
        for (long index : indices) {
            all.add(this.get(index));
        }
        return all;
    }
    
    @Override
    public final int size() {
        // This follows the specification of java.util.List
        long size = this.sizeAsLong();
        if (size < Integer.MAX_VALUE) return (int)size;
        return Integer.MAX_VALUE;
    }
    
    /** We need to define our own iterator class that handles long integer cursors */
    private final class Itr implements Iterator<E> {
        
        private long cursor = 0L;

        @Override
        public boolean hasNext() {
            return cursor < sizeAsLong();
        }

        @Override
        public E next() {
            E next = get(cursor);
            cursor++;
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
    
}
