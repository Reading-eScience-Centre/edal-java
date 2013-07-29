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

package uk.ac.rdg.resc.edal.grid;

import java.util.List;

import uk.ac.rdg.resc.edal.domain.DiscreteDomain;
import uk.ac.rdg.resc.edal.domain.Extent;

/**
 * <p>
 * A one-dimensional axis of a referenceable Grid, which maps between integer
 * indices along the axis and real-world coordinates. It is in spirit a
 * one-dimensional special case of a {@link ReferenceableGrid}.
 * </p>
 * <p>
 * ReferenceableAxes can be composed to form {@link RectilinearGrid}s.
 * </p>
 * <p>
 * If the values of the axis are numeric, the must either increase or decrease
 * monotonically with increasing axis index.
 * </p>
 * 
 * @param <P>
 *            The type of object used to identify positions on this axis
 * @author Jon Blower
 */
public interface ReferenceableAxis<P> extends DiscreteDomain<P, Extent<P>> {
    /**
     * @return The name of the axis.
     */
    public String getName();

    /**
     * Returns the number of points along this axis.
     */
    public int size();

    /**
     * Gets the coordinate value at the given index
     * 
     * @param index
     *            The index of the required position on the axis. Must be within
     *            the axis' {@link #getAxisExtent() extent}.
     * @return the coordinate value at the given index
     * @throws IndexOutOfBoundsException
     *             if the axis' extent does not contain this index.
     */
    public P getCoordinateValue(int index);

    /**
     * Gets all of the coordinate values of the axis
     * 
     * @return A list of coordinate values
     */
    public List<P> getCoordinateValues();

    /**
     * Returns true if the values on the axis are in ascending order according
     * to their natural ordering.
     */
    public boolean isAscending();

    /**
     * Gets the coordinate bounds associated with the point at the given index.
     * In some types of ReferenceableAxis, each point along the axis is
     * associated with a <i>range</i> of positions, rather than a single
     * infinitesimal position. This method returns this range.
     * 
     * @param index
     * @return the coordinate bounds associated with the point at the given
     *         index. Returns an Extent with low=high if the bounds are
     *         infinitesimal. Never returns null.
     */
    public Extent<P> getCoordinateBounds(int index);

    /**
     * <p>
     * Finds the index along this axis that corresponds with the given position.
     * Formally, this returns an index such that
     * getCoordinateBounds(index).contains(position) returns true. Returns -1 if
     * the given position is not associated with any axis index.
     * </p>
     * <p>
     * If this is a longitude axis, this method will handle the case of
     * longitude values wrapping, therefore values of -180 and +180 are treated
     * as equivalent by this method, irrespective of the values in
     * {@link #getCoordinateValue(index)}.
     * </p>
     * 
     * @param position
     * @return the index of the given coordinate value, or -1 if not found.
     */
    public int findIndexOf(P position);

    /**
     * Gets the extent encompassing all the positions on this axis. This will
     * usually be given by the low value of the first set of
     * {@link #getCoordinateBounds(int) coordinate bounds} and the high value of
     * the last set of coordinate bounds (but beware that values along the axis
     * might <i>decrease</i>, not increase).
     */
    public Extent<P> getCoordinateExtent();
}
