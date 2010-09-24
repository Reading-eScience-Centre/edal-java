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

/**
 * A description of a multidimensional grid with integer coordinates.
 * @author Jon
 * @todo implement Iterable&lt;GridCoordinates&gt;?  Would be nice to be able
 * to do {@code for (GridCoordinates coords : grid) ...}.  This method could
 * also be available on the GridEnvelope implementation.
 */
public interface Grid
{

    /**
     * Returns a list containing the names of the grid axes.  The order of these
     * names matches the order of elements in this Grid's {@link GridCoordinates}
     * and {@link #getGridExtent() grid envelope}.
     * @return a list containing the names of the grid axes.
     */
    public List<String> getAxisNames();

    /**
     * Returns the extent of the grid in integer coordinates.
     * @return
     */
    public GridExtent getGridExtent();

    /**
     * Returns a list of the grid points that comprise this grid.  If this is not a
     * {@link ReferenceableGrid}, the footprint of each grid point will be null.
     * @return a list of the grid points that comprise this grid
     * @todo Define the ordering
     */
    public List<GridPoint> getGridPoints();

    /**
     * Returns the dimensionality of the grid. This will be the same as
     * {@link #getAxisNames()}.size() and {@link #getExtent()}.getDimension().
     * @return the dimensionality of the grid.
     */
    public int getDimension();

    /**
     * Returns the number of grid points in the grid.
     * @return the number of grid points in the grid.
     * @todo should return a long?
     */
    public int getSize();
}