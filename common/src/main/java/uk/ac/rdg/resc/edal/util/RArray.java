/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
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

package uk.ac.rdg.resc.edal.util;

/**
 * <p>
 * Abstract superclass for resizeable integer arrays. Although values are
 * retrieved and set using long integers, the underlying storage may be any data
 * type.
 * </p>
 * <p>
 * GNU Trove implements similar classes with more sophisticated features, but
 * the internal array doubles in capacity with each resize operation. This class
 * grows the internal array linearly, by a constant amount each time. Hence the
 * number of elements in the internal storage array is never greater than
 * {@code size() + chunkSize - 1}.
 * </p>
 * 
 * @author Jon Blower
 */
public abstract class RArray {

    /*
     * Number of used entries in the array
     */
    protected int size = 0;
    /*
     * The amount by which the array should grow on each resize
     */
    protected final int chunkSize;
    /*
     * Must be an array
     */
    protected Object storage;

    /**
     * Creates an array in which the initial capacity is set the same as the
     * chunk size.
     */
    protected RArray(int chunkSize) {
        this(chunkSize, chunkSize);
    }

    /**
     * Creates an array with the given initial capacity and chunk size.
     * 
     * @param initialCapacity
     *            The number of elements in the storage array
     * @param chunkSize
     *            The number of storage elements that will be added each time
     *            the storage array grows.
     */
    protected RArray(int initialCapacity, int chunkSize) {
        if (initialCapacity < 0 || chunkSize <= 0) {
            throw new IllegalArgumentException("Initial capacity must be >=0"
                    + " and chunk size must be > 0");
        }
        this.chunkSize = chunkSize;
        this.storage = this.makeStorage(initialCapacity);
    }

    protected abstract Object makeStorage(int capacity);

    protected abstract int getStorageLength();

    protected abstract void setElement(int index, long value);

    protected abstract long getMinValue();

    protected abstract long getMaxValue();

    public abstract void swapElements(int i1, int i2);

    /**
     * Returns the <i>i</i>th element of the array as a long integer,
     * irrespective of the underlying storage type.
     * 
     * @param i
     *            The index of the element to return.
     * @return the <i>i</i>th element of the array.
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code i >= size()}
     */
    public abstract long getLong(int i);

    /**
     * Returns the <i>i</i>th element of the array as a 4-byte integer,
     * irrespective of the underlying storage type.
     * 
     * @param i
     *            The index of the element to return.
     * @return the <i>i</i>th element of the array.
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code i >= size()}
     * @throws ArithmeticError
     *             if the element is too large or small to be represented as a
     *             4-byte integer.
     */
    public abstract int getInt(int i);

    /**
     * Appends an integer to the end of the array
     * 
     * @param i
     *            The integer to append
     * @throws ArithmeticException
     *             if {@code i} is too large or small to be stored in the
     *             underlying storage array
     */
    public final void append(long i) {
        if (i < this.getMinValue() || i > this.getMaxValue()) {
            throw new ArithmeticException(i + " cannot be stored in this array");
        }
        int storageLength = this.getStorageLength();
        if (this.size >= storageLength) {
            /*
             * Grow the array by chunkSize elements
             */
            Object newArray = this.makeStorage(storageLength + this.chunkSize);
            System.arraycopy(this.storage, 0, newArray, 0, storageLength);
            this.storage = newArray;
        }
        this.setElement(this.size, i);
        this.size++;
    }

    public final int size() {
        return this.size;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + chunkSize;
        result = prime * result + size;
        result = prime * result + ((storage == null) ? 0 : storage.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RArray other = (RArray) obj;
        if (chunkSize != other.chunkSize)
            return false;
        if (size != other.size)
            return false;
        if (storage == null) {
            if (other.storage != null)
                return false;
        } else if (!storage.equals(other.storage))
            return false;
        return true;
    }
}
