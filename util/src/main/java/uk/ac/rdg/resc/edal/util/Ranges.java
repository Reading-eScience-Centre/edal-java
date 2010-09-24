/*
 * Copyright (c) 2010 The University of Reading
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
 */

package uk.ac.rdg.resc.edal.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.joda.time.DateTime;

/**
 * Contains convenience methods for creating {@link Range} objects.  This class
 * is non-instantiable.
 * @author Jon
 */
public final class Ranges
{
    /** Prevents instantiation */
    private Ranges() { throw new AssertionError(); }

    /**
     * Creates a Range whose minimum is the lowest value in the passed collection
     * and whose maximum is the highest value in the passed collection, according
     * to the natural order of its elements.  Null values in the passed collection
     * are ignored; if the collection consists entirely of null values the returned
     * Range will have null minimum and maximum values (an empty range).
     * @param <T> The type of the elements in the collection
     * @param coll A Collection of values, in any order.
     * @return a Range whose minimum is the lowest value in the passed collection
     * and whose maximum is the highest value in the passed collection.
     * @throws NullPointerException if the collection is null
     * @throws NoSuchElementException if the collection is empty
     * @throws ClassCastException if any of the elements in the collection are
     * not {@link Comparable} with any of the others.
     */
    public static <T extends Comparable<? super T>> Range<T> findMinMax(Collection<T> coll)
    {
        // Adapted from Collections.min()
        Iterator<? extends T> i = coll.iterator();
	T minCandidate = i.next();
	T maxCandidate = minCandidate;

        while (i.hasNext()) {
	    T next = i.next();
            if (next != null) {
                if (minCandidate == null || next.compareTo(minCandidate) < 0) {
                    minCandidate = next;
                } if (maxCandidate == null || next.compareTo(maxCandidate) > 0) {
                    maxCandidate = next;
                }
            }
	}

        return new SimpleRange<T>(minCandidate, maxCandidate);
    }

    public static <T extends Object & Comparable<? super T>> Range<T> newRange(T min, T max)
    {
        return new SimpleRange<T>(min, max);
    }

    public static <T> Range<T> newRange(T min, T max, Comparator<? super T> comp)
    {
        return new SimpleRangeWithComparator<T>(min, max, comp);
    }

    public static Range<DateTime> newDateTimeRange(DateTime dt1, DateTime dt2)
    {
        return newRange(dt1, dt2, null);
    }

    public static <T> Range<T> emptyRange()
    {
        return new SimpleRangeWithComparator<T>((T)null, (T)null, null);
    }

    private static abstract class AbstractRange<T> implements Range<T>
    {
        private final T min;
        private final T max;
        protected final Comparator<? super T> comp;

        protected AbstractRange(T min, T max, Comparator<? super T> comp)
        {
            // Not legal for only one of min and max to be null
            if ((min == null && max != null) ||
                (min != null && max == null)) {
                throw new IllegalArgumentException("min and max must both be null or non-null");
            }
            this.min = min;
            this.max = max;
            this.comp = comp;
            if (!this.isEmpty() && this.compare(min, max) > 0) {
                throw new IllegalArgumentException("min must not be greater than max");
            }
        }

        /** Use the natural ordering or the comparator to compare the two given values */
        protected abstract int compare(T val1, T val2);

        @Override
        public final boolean contains(T val) {
            if (this.isEmpty()) return false;
            return this.compare(this.min, val) <= 0 &&
                   this.compare(this.max, val) >= 0;
        }

        @Override
        public final boolean isEmpty() {
            return this.getMinimum() == null &&
                   this.getMaximum() == null;
        }

        @Override
        public final T getMinimum() { return this.min; }

        @Override
        public final T getMaximum() { return this.max; }

        @Override
        public final Comparator<? super T> comparator() { return this.comp; }

        @Override
        public final String toString() {
            return String.format("%s,%s", this.min, this.max);
        }
    }

    private static final class SimpleRange<T extends Comparable<? super T>> extends AbstractRange<T>
    {
        public SimpleRange(T min, T max)
        {
            super(min, max, null);
        }

        @Override
        protected int compare(T val1, T val2) {
            return val1.compareTo(val2);
        }
    }

    private static final class SimpleRangeWithComparator<T> extends AbstractRange<T>
    {
        public SimpleRangeWithComparator(T min, T max, Comparator<? super T> comparator)
        {
            super(min, max, comparator);
        }

        @Override
        protected int compare(T val1, T val2) {
            return this.comp.compare(val1, val2);
        }
    }
}
