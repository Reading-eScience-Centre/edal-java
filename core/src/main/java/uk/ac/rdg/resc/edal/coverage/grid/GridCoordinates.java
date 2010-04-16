/*
 * Copyright (c) 2009 The University of Reading
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

import java.util.Arrays;

/**
 * <p>The coordinates of a point within a {@link Grid}.  This interface inherits
 * from GeoAPI's GridCoordinates, defining contracts for the equals() and hashCode()
 * methods.  This permits different implementations of <i>this interface</i> to be
 * mixed in collections such as Sets and Maps.  Note that it is not generally safe to mix
 * different implementations of GeoAPI's GridCoordinates
 * in the same collection, as they might implement equals() and hashCode() differently.
 * If GeoAPI's GridCoordinates interface is modified to define equals() and
 * hashCode(), this interface may become redundant.</p>
 * <p>This interface also defines a {@link #compareTo(uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates) 
 * natural ordering} for GridCoordinates.</p>
 * @see org.opengis.coverage.grid.GridCoordinates
 * @author Jon
 */
public interface GridCoordinates extends org.opengis.coverage.grid.GridCoordinates,
    Comparable<GridCoordinates> {

    /**
     * The hash code is defined as the hash code of the integer array returned
     * by {@link #getCoordinateValues()}, as calculated by {@link Arrays#hashCode(int[])}.
     * @return
     */
    @Override public int hashCode();

    /**
     * <p>This returns true if all the following conditions are met, false otherwise:</p>
     * <ul>
     *   <li>{@code obj} is an instance of {@link GridCoordinates} (i.e. <i>this
     * interface</i>)</li>
     *   <li>{@code Arrays.equals(this.getCoordinateValues(), other.getCoordinateValues())}</li>
     * </ul>
     * @param obj The object to compare for equality.
     * @return true if {@code obj} is equal to this object, as defined above.
     */
    @Override public boolean equals(Object obj);

    /**
     * <p>Compares this object with the specified object for order. We define
     * this ordering as follows:</p>
     * <ul>
     *   <li>First compare the dimensions of the objects.  If they are different,
     * the object with the greater number of dimensions is considered greater. *</li>
     *   <li>Next compare each individual coordinate from each object, starting
     * with {@link #getCoordinateValue(int) getCoordinateValue}(0).  If they are different, the
     * object with the higher coordinate is considered greater.</li>
     * </ul>
     * <p>This defines a natural ordering that is <i>consistent with equals</i>.</p>
     * <p>* It is unlikely to be useful in reality to compare GridCoordinates
     * objects with different numbers of dimensions, but we specify this for
     * completeness.</p>
     * <p>This ordering ensures that collections of GridCoordinates objects
     * will be ordered with the last coordinate varying fastest.</p>
     * @param other The coordinates to be compared with this object
     * @return a negative integer, zero, or a positive integer as this object is
     * less than, equal to, or greater than the specified object.
     */
    @Override public int compareTo(GridCoordinates other);

}
