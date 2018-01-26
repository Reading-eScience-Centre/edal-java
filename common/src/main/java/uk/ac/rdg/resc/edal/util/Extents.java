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

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

import uk.ac.rdg.resc.edal.domain.Extent;

/**
 * Contains convenience methods for creating {@link Extent} objects. This class
 * is non-instantiable.
 * 
 * @author Guy
 * @author Jon
 */
public final class Extents {
    /*
     * Prevents instantiation
     */
    private Extents() {
        throw new AssertionError();
    }

    /**
     * Creates an Extent whose minimum is the lowest value in the passed
     * collection and whose maximum is the highest value in the passed
     * collection, according to the natural order of its elements. Null values
     * and NaNs in the passed collection are ignored; if the collection consists
     * entirely of null values the returned Extent will have null minimum and
     * maximum values (an empty extent).
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
        /*
         * Adapted from Collections.min()
         */
        Iterator<? extends T> i = coll.iterator();
        T minCandidate = i.next();
        T maxCandidate = minCandidate;

        while (i.hasNext()) {
            T next = i.next();
            if (next != null && !next.equals(Float.NaN) && !next.equals(Double.NaN)) {
                if (minCandidate == null || next.compareTo(minCandidate) < 0) {
                    minCandidate = next;
                }
                if (maxCandidate == null || next.compareTo(maxCandidate) > 0
                        || maxCandidate.equals(Float.NaN) || maxCandidate.equals(Double.NaN)) {
                    maxCandidate = next;
                }
            }
        }

        return newExtent(minCandidate, maxCandidate);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Object & Comparable<? super T>> Extent<T> newExtent(T min, T max) {
        /*
         * Check both min and max because one of them could be null
         */
        if (min instanceof DateTime || max instanceof DateTime) {
            return (Extent<T>) new DateTimeExtent((DateTime) min, (DateTime) max);
        }
        return new SimpleExtent<T>(min, max);
    }

    public static <T extends Comparable<? super T>> Extent<T> emptyExtent() {
        return new EmptyExtent<T>();
    }

    /**
     * Special example of an {@link Extent} which contains no values.
     * 
     * @author Guy Griffiths
     *
     * @param <T>
     *            The type of value this {@link EmptyExtent} represents
     */
    private static final class EmptyExtent<T> implements Extent<T> {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean contains(T position) {
            return false;
        }

        @Override
        public T getLow() {
            return null;
        }

        @Override
        public T getHigh() {
            return null;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean intersects(Extent<T> otherExtent) {
            return false;
        }

    }

    private abstract static class AbstractExtent<T extends Comparable<? super T>>
            implements Extent<T> {
        private static final long serialVersionUID = 1L;
        private final T min;
        private final T max;

        protected AbstractExtent(T min, T max) {
            this.min = min;
            this.max = max;
            if (min != null && max != null && compare(min, max) > 0) {
                throw new IllegalArgumentException(
                        String.format("min (%s) must not be greater than max (%s)", min, max));
            }
        }

        /**
         * Use the natural ordering or the comparator to compare the two given
         * values
         */
        protected abstract int compare(T val1, T val2);

        @Override
        public boolean contains(T val) {
            if (val == null
                    || (val instanceof Number && Double.isNaN(((Number) val).doubleValue()))) {
                /*
                 * We specifically exclude NaN values because they are treated
                 * as greater than all other numerical values in the compareTo
                 * method, which is not the behaviour we want.
                 */
                return false;
            } else {
                return compare(this.min, val) <= 0 && compare(this.max, val) >= 0;
            }
        }

        @Override
        public final String toString() {
            if (min == null && max == null) {
                return "";
            } else {
                return String.format("%s,%s", this.min, this.max);
            }
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
            return false;
        }

        @Override
        public boolean intersects(Extent<T> otherExtent) {
            /*
             * The last test will never get run, but included for clarity
             */
            return contains(otherExtent.getLow()) || contains(otherExtent.getHigh())
                    || otherExtent.contains(getLow()) || otherExtent.contains(getHigh());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((max == null) ? 0 : max.hashCode());
            result = prime * result + ((min == null) ? 0 : min.hashCode());
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
            @SuppressWarnings("rawtypes")
            AbstractExtent other = (AbstractExtent) obj;
            if (max == null) {
                if (other.max != null)
                    return false;
            } else if (!max.equals(other.max))
                return false;
            if (min == null) {
                if (other.min != null)
                    return false;
            } else if (!min.equals(other.min))
                return false;
            return true;
        }
    }

    private static final class SimpleExtent<T extends Comparable<? super T>>
            extends AbstractExtent<T> {
        private static final long serialVersionUID = 1L;

        public SimpleExtent(T min, T max) {
            super(min, max);
        }

        @Override
        protected int compare(T val1, T val2) {
            /*
             * In an Extent, we treat null bounds as being open-ended. So
             * whether or not this a non-null value is greater than a null value
             * depends on which bound it is.
             * 
             * We could code for this situation explicitly, but since bounds are
             * inclusive, we can also treat null values as being equal to every
             * other value.
             */
            if (val1 == null || val2 == null) {
                return 0;
            }

            return val1.compareTo(val2);
        }
    }

    /**
     * We treat DateTime extents as a special case because although the compare
     * method works with mixed Chronologies we don't want this to be the case
     * when testing whether an extent contains a value.
     * 
     * @author Guy Griffiths
     */
    private static final class DateTimeExtent extends AbstractExtent<DateTime> {
        private static final long serialVersionUID = 1L;

        public DateTimeExtent(DateTime min, DateTime max) {
            super(min, max);
        }

        @Override
        protected int compare(DateTime val1, DateTime val2) {
            /*
             * In an Extent, we treat null bounds as being open-ended. So
             * whether or not this a non-null value is greater than a null value
             * depends on which bound it is.
             * 
             * We could code for this situation explicitly, but since bounds are
             * inclusive, we can also treat null values as being equal to every
             * other value.
             */
            if (val1 == null || val2 == null) {
                return 0;
            }

            return val1.compareTo(val2);
        }

        @Override
        public boolean contains(DateTime val) {
            Chronology chronology = null;
            /*
             * getLow() and getHigh() cannot both be null:
             * 
             * This class is only instantiated in the Extents.newExtent() method
             * which checks the type of min and max with a call to instanceof.
             * If both are null then we will just get an instance of
             * SimpleExtent, which will work as expected
             */
            if (getLow() != null) {
                chronology = getLow().getChronology();
            } else {
                chronology = getHigh().getChronology();
            }

            if (val == null) {
                return false;
            }
            if (val.getChronology() == null) {
                return chronology == null && super.contains(val);
            }
            if (!val.getChronology().equals(chronology)) {
                if (chronology instanceof ISOChronology
                        && val.getChronology() instanceof ISOChronology) {
                    /*
                     * If they are both ISO Chronologies, we can do a time zone
                     * conversion
                     */
                    return super.contains(val.toDateTime(chronology.getZone()));
                } else {
                    return false;
                }
            }
            return super.contains(val);
        }
    }
}
