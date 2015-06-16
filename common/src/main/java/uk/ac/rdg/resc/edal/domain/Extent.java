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

package uk.ac.rdg.resc.edal.domain;

import java.io.Serializable;

/**
 * <p>
 * Defines a contiguous domain that is defined by "low" and "high" bounds. Any
 * value (inclusively) between these values is considered part of the domain. A
 * null value for "low" or "high" indicates that the extent is unbounded at that
 * end. If both values are null and the Extent is not empty (i.e.
 * {@link Extent#isEmpty()} returns <code>true</code>), then the Extent includes
 * all possible values of P, with the exception of NaN values where P is
 * numerical.
 * </p>
 * 
 * @param P
 *            The type of object used to identify positions within this extent.
 * 
 * @author Jon Blower
 * @author Guy
 */
public interface Extent<P> extends Domain<P>, Serializable {
    /**
     * @return The low bound of this {@link Extent}
     */
    public P getLow();

    /**
     * @return The high bound of this {@link Extent}
     */
    public P getHigh();

    /**
     * @return Whether or not this is an empty {@link Extent} - empty
     *         {@link Extent}s are defined as containing no values and
     *         overlapping no other {@link Extent}s, and <code>null</code> will
     *         be returnedfor both {@link Extent#getHigh()} and
     *         {@link Extent#getLow()}
     */
    public boolean isEmpty();

    /**
     * Tests whether this extent overlaps with another
     * 
     * @param otherExtent
     *            The other {@link Extent} to test
     * @return <code>true</code> if the supplied {@link Extent} is wholly or
     *         partially contained within this one
     */
    public boolean intersects(Extent<P> otherExtent);

    @Override
    public boolean equals(Object obj);

    @Override
    public int hashCode();
}
