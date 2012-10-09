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
 * The coordinates of a grid cell within a {@link Grid}. It is usually
 * assumed that the x index varies faster than the y index in underlying
 * storage, hence we define
 * {@link #compareTo(uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates)
 * compareTo()} so that coordinates may be sorted in a manner that reflects
 * this.
 * 
 * @author Jon Blower
 */
public interface GridCoordinates extends Comparable<GridCoordinates> {

    /**
     * The index of the point on an axis
     * 
     * @param dim
     *            The axis number to return the index for
     */
    public int getIndex(int dim);

    /**
     * All indices of the {@link GridCoordinates}
     * 
     * @return
     */
    public int[] getIndices();

    /**
     * The number of dimensions in this {@link GridCoordinates}
     * 
     * @return the number of dimensions
     */
    public int getNDim();

    /**
     * Compares first on the basis of the highest dimension's index, then
     * decreasing through the dimensions. For a 4D case, this means we compare
     * t, z, y, then x. Hence the coordinates (x=1, y=2, z=2, t=2) are taken to
     * be "less than" (x=2, y=1, z=2, t=2).
     * 
     * @param coords
     * @return a negative number if the current {@link GridCoordinates} is
     *         "less than" the supplied one, 0 if they are equivalent, and a
     *         positive nubmer if the current {@link GridCoordinates} is
     *         "more than" the supplied one
     */
    @Override
    public int compareTo(GridCoordinates coords);

}
