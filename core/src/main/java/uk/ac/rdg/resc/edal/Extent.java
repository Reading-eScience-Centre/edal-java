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

package uk.ac.rdg.resc.edal;

/**
 * <p>
 * Defines a contiguous domain that is defined by "low" and "high" values. Any
 * value between or including these values is considered part of the domain.
 * </p>
 * <p>If the low value is null, then any value up to or including the high value
 * will be considered part of the domain.  If the high value is null, then any
 * value equal to or exceeding the low value will be considered part of the 
 * domain.  If both values are null then the domain contains all values (i.e.
 * the domain is <i>total</i>).</p>
 * 
 * @param <A>
 *            The type of object used to identify values within this extent.
 * @author Jon Blower
 */
public interface Extent<A extends Comparable<? super A>> extends Domain<A> {
    
    /**
     * Returns true if the given value is considered part of this Extent.
     * If the low value is null, then any value up to or including the high value
     * will be considered part of the domain.  If the high value is null, then any
     * value equal to or exceeding the low value will be considered part of the 
     * domain.  If both values are null then the domain contains all values (i.e.
     * the domain is <i>total</i>).
     */
    @Override
    public boolean contains(A val);
    
    /**
     * @return the low value of this {@link Extent}, or null if the extent is
     * unbounded at the low end.
     */
    public A getLow();

    /**
     * @return the high value of this {@link Extent}, or null if the extent is
     * unbounded at the high end.
     */
    public A getHigh();
    
    /**
     * We define the hashcode of an Extent to be calculated according to the
     * following formula:
     * <pre>
     * int hash = 17;
     * if (low != null) {
     *     hash = hash * 31 + low.hashCode();
     * }
     * if (high != null) {
     *     hash = hash * 31 + high.hashCode();
     * }
     * </pre>
     * This allows Extents to be compared for equality across implementations.
     */
    @Override
    public int hashCode();
    
    /**
     * Two Extents are equal if their low and high values are equal.
     */
    @Override
    public boolean equals(Object obj);
}
