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

package uk.ac.rdg.resc.edal.coverage.grid;

import java.util.List;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import uk.ac.rdg.resc.edal.coverage.domain.DiscretePointDomain;
import uk.ac.rdg.resc.edal.coverage.domain.Extent;

/**
 * <p>A one-dimensional axis of a Grid, which maps between integer indices along
 * the axis and real-world coordinates.  It is in spirit a one-dimensional
 * special case of a {@link ReferenceableGrid}.</p>
 * <p>Coordinate values along the axis must either increase or decrease
 * monotonically.</p>
 * @param <P> The type of object used to identify positions on this axis
 * @author Jon
 */
public interface ReferenceableAxis<P> extends DiscretePointDomain<P> {

    /**
     * The name of the axis.
     */
    public String getName();

    /**
     * <p>The coordinate values along the axis, in ascending order.  Maps from
     * integer indices to coordinate values.  Note that the inverse mapping can be
     * found using the {@code indexOf()} method, although this method does not
     * take into account the wrapping of longitude values in a longitude axis:
     * use {@link #getCoordinateIndex(double)} or
     * {@link #getNearestCoordinateIndex(double)} for this.</p>
     * <p>The coordinate values must vary monotonically, i.e. either always
     * increasing or always decreasing.</p>
     * @return the coordinate values along the axis.
     */
    public List<Double> getCoordinateValues();

    /**
     * Gets the coordinate value at the given index.
     * @param index The index of the required coordinate value
     * @return the coordinate value at the given index
     * @throws IndexOutOfBoundsException if {@code index &lt; 0} or
     * {@code index &gt;= getSize()}
     */
    public P getCoordinateValue(int index);

    /**
     * Gets the number of coordinate values on this axis, always at least 1.
     * @return the number of coordinate values on this axis
     */
    @Override
    public int size();

    /**
     * Finds the index of the given coordinate value.  If this is a longitude
     * axis, this method will handle the case of longitude values wrapping,
     * therefore values of -180 and +180 are treated as equivalent by this method,
     * irrespective of the values in {@link #getCoordinateValues()}.
     * @param value
     * @return the index of the given coordinate value, or -1 if not found.
     */
    @Override
    public int findIndexOf(P position);

    /**
     * <p>Finds the nearest coordinate index to the given value. If this is a longitude
     * axis, this method will handle the case of longitude values wrapping.
     * So values of -180 and +180 are treated as equivalent by this method,
     * irrespective of the values in {@link #getCoordinateValues()}.</p>
     * @return the nearest coordinate index to the given value, or -1 if the
     * value is outside the {@link #getExtent() extent} of this axis.
     */
    public int findNearestIndexOf(P position);

    /**
     * <p>Gets the extent of this axis, which may be wider than the minimum and
     * maximum coordinate values.</p>
     */
    @Override
    public Extent<P> getExtent();

    /**
     * Returns the {@link CoordinateSystemAxis} to which the points on the
     * axis are referenced.  May be null if unknown.
     * @return the {@link CoordinateSystemAxis} to which the points on the
     * axis are referenced.
     */
    public CoordinateSystemAxis getCoordinateSystemAxis();

}
