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

import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates4D;

/**
 * Immutable instance of a {@link GridCoordinates4D}
 * 
 * @author Guy Griffiths
 * 
 */
public class GridCoordinates4DImpl extends GridCoordinatesImpl implements GridCoordinates4D {

    public GridCoordinates4DImpl(int xIndex, int yIndex, int zIndex, int tIndex) {
        super(xIndex, yIndex, zIndex, tIndex);
    }

    /**
     * Create a new GridCoordinates2DImpl from an existing
     * {@link GridCoordinates} object, throwing an exception if it is not
     * compatible
     * 
     * @param coords
     *            The {@link GridCoordinates} object to convert
     */
    public GridCoordinates4DImpl(GridCoordinates coords) {
        super(checkCoords(coords.getIndices()));
    }

    /**
     * Creates a new GridCoordinatesImpl with the given coordinates.
     * 
     * @param coords
     *            The coordinates of this position.
     * @throws NullPointerException
     *             if {@code coords == null}
     * @throws IllegalArgumentException
     *             if {@code coords.length == 0}.
     */
    public GridCoordinates4DImpl(int[] coords) {
        super(checkCoords(coords));
    }

    private static int[] checkCoords(int[] coords) {
        if (coords == null)
            throw new NullPointerException();
        if (coords.length != 4)
            throw new IllegalArgumentException("This grid co-ordinates must have 4 dimensions");
        return coords;
    }

    @Override
    public int getXIndex() {
        return getIndex(0);
    }

    @Override
    public int getYIndex() {
        return getIndex(1);
    }

    @Override
    public int getZIndex() {
        return getIndex(2);
    }

    @Override
    public int getTIndex() {
        return getIndex(3);
    }
}
