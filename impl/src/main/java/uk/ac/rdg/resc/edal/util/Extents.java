package uk.ac.rdg.resc.edal.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import uk.ac.rdg.resc.edal.Extent;

/**
 * Contains convenience methods for creating {@link Extent} objects. This class
 * is non-instantiable.
 * 
 * @author Jon
 */
public final class Extents {
    /** Prevents instantiation */
    private Extents() {
        throw new AssertionError();
    }

    /**
     * Creates an Extent whose minimum is the lowest value in the passed
     * collection and whose maximum is the highest value in the passed
     * collection, according to the natural order of its elements. Null values
     * in the passed collection are ignored; if the collection consists entirely
     * of null values the returned Extent will have null minimum and maximum
     * values (an empty extent).
     * 
     * @param <T>
     *            The type of the elements in the collection
     * @param coll
     *            A Collection of values, in any order.
     * @return an Extent whose minimum is the lowest value in the passed
     *         collection and whose maximum is the highest value in the passed
     *         collection.
     * @throws NullPointerException
     *             if the collection is null
     * @throws NoSuchElementException
     *             if the collection is empty
     * @throws ClassCastException
     *             if any of the elements in the collection are not
     *             {@link Comparable} with any of the others.
     */
    public static <T extends Comparable<? super T>> Extent<T> findMinMax(Collection<T> coll) {
        // Adapted from Collections.min()
        Iterator<? extends T> i = coll.iterator();
        T minCandidate = i.next();
        T maxCandidate = minCandidate;

        while (i.hasNext()) {
            T next = i.next();
            if (next != null) {
                if (minCandidate == null || next.compareTo(minCandidate) < 0) {
                    minCandidate = next;
                }
                if (maxCandidate == null || next.compareTo(maxCandidate) > 0) {
                    maxCandidate = next;
                }
            }
        }

        return new SimpleExtent<T>(minCandidate, maxCandidate);
    }

    public static <T extends Object & Comparable<? super T>> Extent<T> newExtent(T min, T max) {
        return new SimpleExtent<T>(min, max);
    }

    public static <T extends Comparable<? super T>> Extent<T> newExtent(T min, T max, Comparator<? super T> comp) {
        return new SimpleExtentWithComparator<T>(min, max, comp);
    }

    public static <T extends Comparable<? super T>> Extent<T> emptyExtent() {
        return new SimpleExtentWithComparator<T>((T) null, (T) null, null);
    }

    private abstract static class AbstractExtent<T extends Comparable<? super T>> implements Extent<T> {
        private final T min;
        private final T max;
        protected final Comparator<? super T> comp;

        protected AbstractExtent(T min, T max, Comparator<? super T> comp) {
            // Not legal for only one of min and max to be null
            if ((min == null && max != null) || (min != null && max == null)) {
                throw new IllegalArgumentException("min and max must both be null or non-null");
            }
            this.min = min;
            this.max = max;
            this.comp = comp;
            if (min != null && max != null && this.compare(min, max) > 0) {
                throw new IllegalArgumentException(String
                        .format("min (%s) must not be greater than max (%s)", min, max));
            }
        }

        /**
         * Use the natural ordering or the comparator to compare the two given
         * values
         */
        protected abstract int compare(T val1, T val2);

        @Override
        public final boolean contains(T val) {
            return this.compare(this.min, val) <= 0 && this.compare(this.max, val) >= 0;
        }

        @Override
        public final String toString() {
            return String.format("%s,%s", this.min, this.max);
        }

        @Override
        public T getHigh() {
            return max;
        }

        @Override
        public T getLow() {
            return min;
        }

        @Override
        public boolean isEmpty() {
            return (min == null && max == null);
        }
    }

    private static final class SimpleExtent<T extends Comparable<? super T>> extends AbstractExtent<T> {
        public SimpleExtent(T min, T max) {
            super(min, max, null);
        }

        @Override
        protected int compare(T val1, T val2) {
            return val1.compareTo(val2);
        }
    }

    private static final class SimpleExtentWithComparator<T extends Comparable<? super T>> extends AbstractExtent<T> {
        public SimpleExtentWithComparator(T min, T max, Comparator<? super T> comparator) {
            super(min, max, comparator);
        }

        @Override
        protected int compare(T val1, T val2) {
            return this.comp.compare(val1, val2);
        }
    }
}
