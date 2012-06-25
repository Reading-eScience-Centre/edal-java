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

package uk.ac.rdg.resc.edal.coverage.grid;

/**
 * An N-dimensional grid. This top-level type contains no georeferencing
 * information, and axes are arbitrarily chosen.
 * 
 * @author Jon Blower, Guy Griffiths
 */
public interface Grid {
    /**
     * Gets the nth axis of the grid.
     * 
     * @param n
     *            the desired axis number
     * @return the requested axis
     */
    public GridAxis getAxis(int n);

    /**
     * The extent of the grid in integer coordinates.
     */
    public GridExtent getGridExtent();

    /**
     * Gets the number of axes in the grid
     */
    public int getNDim();

    /**
     * The number of grid cells in this grid (equivalent to
     * {@link GridExtent#size()}).
     */
    public long size();

    /**
     * @throws IndexOutOfBoundsException
     *             if the index is out of range for this grid
     */
    public GridCoordinates getCoords(long index);

    /**
     * @throws IndexOutOfBoundsException
     *             if any of the coords are out of range for this grid
     */
    public long getIndex(GridCoordinates coords);

    /**
     * @throws IndexOutOfBoundsException
     *             if any of the coords are out of range for this grid
     */
    public long getIndex(int... indices);
}