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
 * A two-dimensional grid composed of an X and Y axis. This top-level type
 * contains no georeferencing information, and so the X and Y axes are
 * arbitrarily chosen, however, the x index is usually assumed to vary faster in
 * underlying storage. Hence the natural order of grid coordinates is (x0, y0),
 * (x1, y0), (x2, y0), (x0, y1) etc.
 * 
 * @author Jon Blower
 */
public interface Grid {
    /**
     * Gets the X axis of the grid.
     * 
     * @return the X axis of the grid.
     */
    public GridAxis getXAxis();

    /**
     * Gets the Y axis of the grid.
     * 
     * @return the Y axis of the grid.
     */
    public GridAxis getYAxis();

    /**
     * The extent of the grid in integer coordinates.
     */
    public GridExtent getGridExtent();

    /**
     * The number of grid cells in this grid (equivalent to
     * {@link GridExtent#size()}).
     */
    public long size();
    
    public GridCoordinates2D getCoords(long index);
    public long getIndex(GridCoordinates2D coords);
    public long getIndex(int i, int j);
}