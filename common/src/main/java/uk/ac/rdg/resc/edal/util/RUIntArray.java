/*******************************************************************************
 * Copyright (c) 2011 The University of Reading
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
 *******************************************************************************/

package uk.ac.rdg.resc.edal.util;

/**
 * <p>
 * A resizeable array of unsigned integers. Data are stored in an array of
 * primitive integers.
 * </p>
 * <p>
 * Instances of this class are not thread safe.
 * </p>
 * 
 * @author Jon Blower
 */
public final class RUIntArray extends RArray {

    /** The maximum value that can be stored in this array */
    public static final long MAX_VALUE = 4294967295L;
    /** The minimum value that can be stored in this array */
    public static final long MIN_VALUE = 0;

    /**
     * Creates an array in which the initial capacity is set the same as the
     * chunk size.
     */
    public RUIntArray(int chunkSize) {
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
    public RUIntArray(int initialCapacity, int chunkSize) {
        super(initialCapacity, chunkSize);
    }

    @Override
    protected int[] makeStorage(int capacity) {
        return new int[capacity];
    }

    /**
     * Returns the <i>i</i>th element of the array.
     * 
     * @param i
     *            The index of the element to return.
     * @return the <i>i</i>th element of the array.
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code i >= size()}
     */
    @Override
    public long getLong(int i) {
        if (i >= this.size) {
            throw new ArrayIndexOutOfBoundsException(i);
        }
        return this.getStorage()[i] & 0xffffffffL;
    }

    @Override
    public int getInt(int i) {
        long val = getLong(i);
        if (val > Integer.MAX_VALUE || val < Integer.MIN_VALUE) {
            throw new ArithmeticException(val + " is too long for an integer");
        }
        return (int) val;
    }

    private int[] getStorage() {
        return (int[]) this.storage;
    }

    @Override
    protected int getStorageLength() {
        return this.getStorage().length;
    }

    @Override
    protected void setElement(int index, long value) {
        this.getStorage()[index] = (int) value;
    }

    @Override
    public void swapElements(int i1, int i2) {
        int[] arr = this.getStorage();
        int temp = arr[i1];
        arr[i1] = arr[i2];
        arr[i2] = temp;
    }

    @Override
    protected long getMinValue() {
        return MIN_VALUE;
    }

    @Override
    protected long getMaxValue() {
        return MAX_VALUE;
    }

}
