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

import uk.ac.rdg.resc.edal.domain.DiscreteHorizontalDomain;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;

/**
 * A two-dimensional grid in the horizontal plane that is referenced to a 2D
 * horizontal coordinate reference system. If each individual grid axis is
 * aligned with the axes of the CRS, the {@link RectilinearGrid} class can be
 * used.
 * 
 * @author Jon Blower
 */
public interface HorizontalGrid extends DiscreteHorizontalDomain<GridCell2D> {
    
    @Override
    public Array2D<GridCell2D> getDomainObjects();

    /**
     * Returns the size of this domain. Equivalent to
     * <code>getXSize() * getYSize()</code>
     */
    @Override
    public long size();

    /**
     * @return The size of the x-axis
     */
    public int getXSize();

    /**
     * @return The size of the y-axis
     */
    public int getYSize();

    /**
     * Finds the index of the specified position within this domain
     * 
     * @param position
     *            The position to find the index of
     * @return A {@link GridCoordinates2D} object representing the index of the
     *         position within the domain, or <code>null</code> if the position
     *         is outside the domain.
     */
    public GridCoordinates2D findIndexOf(HorizontalPosition position);

    @Override
    public boolean equals(Object obj);
    
    @Override
    public int hashCode();
}
