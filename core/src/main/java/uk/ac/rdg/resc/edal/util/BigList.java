/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.util;

import java.util.List;

/**
 * A {@link List} that provides extra methods to access elements by long integer
 * indices.  BigLists may be backed by memory storage or by slower storage such
 * as disk.  However, the get(), getAll() and toArray() methods must return memory-resident
 * structures.
 * @param <E> The type of the elements in the list
 * @author Jon
 * @todo think about subList() and listIterator();
 */
public interface BigList<E> extends List<E> {
    
    /**
     * Returns the element at the specified position in this list.
     * @param index Index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     * ({@code index < 0 || index >= size()})
     */
    public E get(long index);
    
    /**
     * <p>Returns all the elements at the specified positions in this list as
     * a memory-resident List.</p>
     * <p>Note that this should return a new List, <b>not</b> a view onto this List.
     * {@link #subList(int, int) subList()} can be used to provide a view.
     * (TODO: is this too strong a constraint?  Could we simply say that the returned
     * list must be memory-resident?)</p>
     * <p>This method is provided because BigLists may often be backed by storage
     * on disk or a network.  Reading several elements in one operation may be
     * more efficient than reading elements one at a time.</p>
     * @param indices Indices of the elements to return.
     * @return a memory-resident List of all the elements at the specified positions in this list.
     * The order of these elements corresponds with the order of the specified
     * positions.
     * @throws IndexOutOfBoundsException if any of the indices is out of range
     * ({@code index < 0 || index >= size()})
     */
    public List<E> getAll(List<Long> indices);
    
    /**
     * <p>Returns all the elements from {@code fromIndex} (inclusive) to
     * {@code toIndex} (exclusive) as a memory-resident List.</p>
     * <p>Note that this should return a new List, <b>not</b> a view onto this List.
     * {@link #subList(int, int) subList()} can be used to provide a view.
     * (TODO: is this too strong a constraint?  Could we simply say that the returned
     * list must be memory-resident?)</p>
     * <p>This method is provided because BigLists may often be backed by storage
     * on disk or a network.  Reading several elements in one operation may be
     * more efficient than reading elements one at a time.</p>
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex high endpoint (exclusive) of the subList
     * @return a memory-resident List of all the elements from {@code fromIndex}
     * to {@code toIndex}.
     */
    public List<E> getAll(long fromIndex, long toIndex);
    
    /**
     * Returns the runtime type of the values in the BigList.
     */
    public Class<E> getValueType();
    
    /**
     * Returns the number of elements in the list as a long integer.
     */
    public long sizeAsLong();
    
}