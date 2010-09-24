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

import java.util.Comparator;

/**
 * <p>Describes a range of values of a certain type.  The range is considered to
 * be inclusive of both its minimum and maximum values.  The minimum and maximum
 * values may be equal, in which case the Range will contain a single value.</p>
 * <p>If both the minimum and maximum values in this Range are null, this range
 * is considered to be empty, i.e. it contains no values.  It is not legal for
 * only one of the minimum and maximum values to be null.</p>
 * @param <T> The type of the values in the range
 * @todo Allow for discretized Ranges (e.g. TIME dimensions).  Would alter the
 * semantics of contains().
 * @todo Allow for half-open Ranges, in which one or other of min or max is null.
 * @author Jon
 */
public interface Range<T>
{
    /**
     * Returns true if the given value is contained within this range.  Note
     * that the range is considered to be inclusive of both its
     * {@link #getMinimum() minimum} and {@link #getMaximum() maximum} value.
     * @return true if the given value is contained within this range.
     */
    public boolean contains(T val);

    /**
     * Returns the minimum value in this range.  Note that the range is
     * considered to be inclusive of this minimum value.
     * @return the minimum value in this range, or null if this Range contains
     * no values.
     */
    public T getMinimum();

    /**
     * Returns the maximum value in this range.  Note that the range is
     * considered to be inclusive of this maximum value.
     * @return the maximum value in this range, or null if this Range contains
     * no values.
     */
    public T getMaximum();

    /**
     * Returns true if this Range contains no values, in which case both
     * {@link #getMinimum()} and {@link #getMaximum()} will be null.
     * @return true if this Range contains no values.
     */
    public boolean isEmpty();

    /**
     * Returns the comparator used to compare the elements in this range, or
     * @code null} if this range uses the {@link Comparable natural ordering}
     * of its elements.
     * @return the comparator used to order the elements in this set, or
     * @code null} if this range uses the {@link Comparable natural ordering}
     * of its elements.
     */
    public Comparator<? super T> comparator();

}
