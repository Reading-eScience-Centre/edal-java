package uk.ac.rdg.resc.edal.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        return (Set<T>)SET_OF_SINGLE_NULL_VALUE;
    }

}
