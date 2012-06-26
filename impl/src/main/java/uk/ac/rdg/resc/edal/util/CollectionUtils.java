/*******************************************************************************
 * Copyright (c) 2012 The University of Reading
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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * Contains some useful utility methods for working with Collections.
 * 
 * @author Jon
 */
public final class CollectionUtils {

    private static final Set SET_OF_SINGLE_NULL_VALUE;

    static {
        Set set = new HashSet(1);
        set.add(null);
        SET_OF_SINGLE_NULL_VALUE = Collections.unmodifiableSet(set);
    }

    /** Prevents direct instantiation */
    private CollectionUtils() {
        throw new AssertionError();
    }

    /**
     * Returns a new empty ArrayList for objects of a certain type
     */
    public static <T> ArrayList<T> newArrayList() {
        return new ArrayList<T>();
    }

    /**
     * Returns a new empty HashSet for objects of a certain type
     */
    public static <T> HashSet<T> newHashSet() {
        return new HashSet<T>();
    }

    /**
     * Returns a new empty LinkedHashSet for objects of a certain type
     */
    public static <T> LinkedHashSet<T> newLinkedHashSet() {
        return new LinkedHashSet<T>();
    }

    /**
     * Returns a new empty HashMap for objects of a certain type
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<K, V>();
    }

    /**
     * Returns a new empty LinkedHashMap for objects of a certain type
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap<K, V>();
    }

    /**
     * Returns a new empty TreeMap for objects of a certain type
     */
    public static <K, V> TreeMap<K, V> newTreeMap() {
        return new TreeMap<K, V>();
    }

    /**
     * Creates and returns an unmodifiable List that wraps the given array.
     * Changes to the array will be reflected in the List.
     * 
     * @param arr
     *            The array to wrap as a List
     * @return an unmodifiable List that wraps the array
     * @throws NullPointerException
     *             if the array is null
     */
    public static List<Float> listFromFloatArray(final float[] arr) {
        if (arr == null)
            throw new NullPointerException("array cannot be null");
        return new AbstractList<Float>() {
            @Override
            public Float get(int index) {
                return arr[index];
            }

            @Override
            public int size() {
                return arr.length;
            }
        };
    }

    /**
     * Creates and returns an unmodifiable List that wraps the given array.
     * Changes to the array will be reflected in the List.
     * 
     * @param arr
     *            The array to wrap as a List
     * @param fillValue
     *            The value to use in place of Float.NaNs
     * @return an unmodifiable List that wraps the array
     * @throws NullPointerException
     *             if the array is null
     */
    public static List<Float> listFromFloatArray(final float[] arr, final Float fillValue) {
        if (arr == null)
            throw new NullPointerException("array cannot be null");
        return new AbstractList<Float>() {
            @Override
            public Float get(int index) {
                float val = arr[index];
                if (Float.isNaN(val)) {
                    return fillValue;
                } else {
                    return val;
                }
                // return Float.isNaN(val) ? fillValue : val;
            }

            @Override
            public int size() {
                return arr.length;
            }
        };
    }

    /**
     * Creates and returns an unmodifiable List that wraps the given array.
     * Changes to the array will be reflected in the List.
     * 
     * @param arr
     *            The array to wrap as a List
     * @return an unmodifiable List that wraps the array
     * @throws NullPointerException
     *             if the array is null
     */
    public static List<Double> listFromDoubleArray(final double[] arr) {
        if (arr == null)
            throw new NullPointerException("array cannot be null");
        return new AbstractList<Double>() {
            @Override
            public Double get(int index) {
                return arr[index];
            }

            @Override
            public int size() {
                return arr.length;
            }
        };
    }

    /**
     * Returns a new Set containing the given values
     */
    public static <T> Set<T> setOf(T... values) {
        Set<T> set = newHashSet();
        set.addAll(Arrays.asList(values));
        return set;
    }
    
    /**
     * Returns an unmodifiable Set containing a single null value
     */
    public static <T> Set<T> setOfSingleNullValue() {
        return SET_OF_SINGLE_NULL_VALUE;
    }
    
    /**
     * Wraps a List as a BigList
     */
    public static <T> BigList<T> wrap(final List<T> list)
    {
        return new AbstractBigList<T>()
        {
            @Override
            public T get(long index) {
                if (index < 0 || index >= list.size()) {
                    throw new IndexOutOfBoundsException(index +
                            " out of bounds for list of size " + list.size());
                }
                return list.get((int)index);
            }

            @Override
            public long sizeAsLong() {
                return list.size();
            }
        };
    }

    
    
    public static Double minIgnoringNullsAndNans(Collection<Number> coll){
        Comparator<Number> minC = new Comparator<Number>() {
            @Override
            public int compare(Number arg0, Number arg1) {
                if(arg0 == null)
                    return 1;
                if(arg1 == null)
                    return -1;
                Float f1 = arg0.floatValue();
                Float f2 = arg1.floatValue();
                if(f1.equals(Float.NaN))
                    return 1;
                if(f2.equals(Float.NaN))
                    return -1;
                return f1.compareTo(f2);
            }
        };
        return Collections.min(coll, minC).doubleValue();
    }
    
    public static Double maxIgnoringNullsAndNans(Collection<Number> coll){
        Comparator<Number> maxC = new Comparator<Number>() {
            @Override
            public int compare(Number arg0, Number arg1) {
                if(arg0 == null)
                    return -1;
                if(arg1 == null)
                    return 1;
                Float f1 = arg0.floatValue();
                Float f2 = arg1.floatValue();
                if(f1.equals(Float.NaN))
                    return -1;
                if(f2.equals(Float.NaN))
                    return 1;
                return f1.compareTo(f2);
            }
        };
        return Collections.max(coll, maxC).doubleValue();
    }
}
