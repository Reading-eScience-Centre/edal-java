/*******************************************************************************
 * Copyright (c) 2012 The University of Reading
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

package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.Arrays;

import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;

/**
 * Immutable implementation of {@link GridCoordinates}.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public class GridCoordinatesImpl implements GridCoordinates {

    private final int[] indices;

    /**
     * Creates a new GridCoordinatesImpl with the given coordinates.
     * 
     * @param xIndex
     *            The first coordinate
     * @param otherCoords
     *            The remaining coordinates
     */
    public GridCoordinatesImpl(int... indices) {
        if (indices.length < 2) {
            throw new IllegalArgumentException("A grid must have at least 2 dimensions");
        }
        this.indices = indices;
    }

    /**
     * <p>
     * Creates a new GridCoordinatesImpl from the given GridCoordinates object.
     * This may be used to convert a GridCoordinates object of unknown type to
     * one of this type, perhaps to make an object that is guaranteed immutable.
     * </p>
     * <p>
     * If {@code gridCoordinates} is already an instance of GridCoordinatesImpl,
     * this method simply returns it: no new objects are created. This method is
     * therefore different from {@link #clone()}, which <i>always</i> returns a
     * new object.
     * </p>
     * 
     * @param gridCoordinates
     *            The GridCoordinates2D to convert.
     * @return a GridCoordinatesImpl object containing the same information as
     *         the provided GridCoordinates2D object.
     * @see #clone()
     */
    public static GridCoordinatesImpl convert(GridCoordinates gridCoordinates) {
        if (gridCoordinates instanceof GridCoordinatesImpl) {
            return (GridCoordinatesImpl) gridCoordinates;
        }
        return new GridCoordinatesImpl(gridCoordinates.getIndices());
    }

    /**
     * Returns a GridCoordinatesImpl object in which all coordinates are zero.
     * 
     * @param dimension
     *            the required number of dimensions
     * @return a GridCoordinatesImpl object in which all coordinates are zero.
     * @throws IllegalArgumentException
     *             if {@code dimension <= 0}
     */
    public static GridCoordinatesImpl zero(int size) {
        return new GridCoordinatesImpl(new int[size]);
    }

    @Override
    public int getIndex(int dim) {
        return indices[dim];
    }

    @Override
    public int[] getIndices() {
        return indices;
    }

    @Override
    public int getNDim() {
        return indices.length;
    }

    @Override
    public int compareTo(GridCoordinates other) {
        return GridCoordinatesComparator.INSTANCE.compare(this, other);
    }

    /**
     * <p>
     * Returns a new {@link GridCoordinatesImpl} object that is a copy of this
     * object. There is generally no need to call this method as
     * {@link GridCoordinatesImpl} objects are immutable and can be reused
     * freely.
     * </p>
     * <p>
     * Note that GeoAPI should not have specified that {@code
     * org.opengis.coverage.grid.GridCoordinates} extends {@code
     * org.opengis.util.Cloneable}: this should have been left as a mixin for
     * concrete implementations.
     * </p>
     * 
     * @return a new {@link GridCoordinatesImpl} object that is a copy of this
     *         object.
     * @see #convert(org.opengis.coverage.grid.GridCoordinates)
     */
    @Override
    public Object clone() {
        return new GridCoordinatesImpl(indices);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(indices);
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
        GridCoordinatesImpl other = (GridCoordinatesImpl) obj;
        if (!Arrays.equals(indices, other.indices))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder gcString = new StringBuilder();
        for (int index : indices) {
            gcString.append(index + ",");
        }
        gcString.deleteCharAt(gcString.length() - 1);
        return gcString.toString();
    }
}
