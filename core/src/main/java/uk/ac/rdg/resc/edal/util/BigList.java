/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.util;

import java.util.List;

/**
 * A {@link List} that provides extra methods to access elements by long integer
 * indices.
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
     * <p>Returns all the elements at the specified positions in this list.</p>
     * <p>This method is provided because BigLists may often be backed by storage
     * on disk or a network.  Reading several elements in one operation may be
     * more efficient than reading elements one at a time.</p>
     * @param indices Indices of the elements to return.
     * @return a List of all the elements at the specified positions in this list.
     * The order of these elements corresponds with the order of the specified
     * positions.
     * @throws IndexOutOfBoundsException if any of the indices is out of range
     * ({@code index < 0 || index >= size()})
     */
    public List<E> getAll(List<Long> indices);
    
    /**
     * Returns the runtime type of the values in the BigList.
     */
    public Class<E> getValueType();
    
    /**
     * Returns the number of elements in the list as a long integer.
     */
    public long sizeAsLong();
    
}
