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

import java.util.Collections;
import java.util.List;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * <p>A one-dimensional axis of a Grid, which maps between integer indices along
 * the axis and real-world coordinates.  It is essentially a one-dimensional
 * special case of a {@link ReferenceableGrid}, although this interface cannot
 * inherit from ReferenceableGrid because ReferenceableGrids by definition have
 * two dimensions or more.</p>
 * @param <T> the type of the coordinate values
 * @author Jon
 */
public interface ReferenceableAxis<T> {

    /**
     * The coordinate values along the axis, in a defined order.  Maps from
     * integer indices to coordinate axes.  Note that the inverse mapping can be
     * found using the {@code indexOf()} method or by
     * {@link Collections#binarySearch(java.util.List, java.lang.Object)}, but
     * convenience methods are provided in this interface, which might be more
     * efficient in some cases.
     * @return the coordinate values along the axis.
     */
    public List<T> getCoordinateValues();

    public T getCoordinateValue(int index);

    public int getCoordinateIndex(T value);

    // public int getNearestCoordinateIndex(int value);

    /** Returns the number of coordinate values on this axis */
    public int getSize();

    /**
     * Returns the {@link CoordinateReferenceSystem} to which the points on the
     * axis are referenced.
     * @return the {@link CoordinateReferenceSystem} to which the points on the
     * axis are referenced.
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem();

}
