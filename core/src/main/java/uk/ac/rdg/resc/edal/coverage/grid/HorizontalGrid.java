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
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import uk.ac.rdg.resc.edal.coverage.domain.DiscretePointDomain;
import uk.ac.rdg.resc.edal.coverage.domain.Extent;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.HorizontalPosition;

/**
 * A two-dimensional {@link ReferenceableGrid} in the horizontal plane.
 * @author Jon
 */
public interface HorizontalGrid extends ReferenceableGrid<HorizontalPosition>
{
    /**
     * <p>Transforms a grid coordinates to a direct position.  The returned
     * position's {@link DirectPosition#getCoordinateReferenceSystem() coordinate
     * reference system} will match the {@link #getCoordinateReferenceSystem()
     * coordinate reference system associated with this object}.</p>
     *
     * <p>This is a convenience method, which is exactly equivalent to calling
     * {@link #transformCoordinates(uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates)}
     * with grid coordinates [i,j]</p>
     * @param i The i coordinate within the grid
     * @param j The j coordinate within the grid
     * @return the "real world" coordinates, or null if the grid coordinates are
     * not contained within the {@link #getGridExtent() envelope of the grid}.
     */
    public HorizontalPosition transformCoordinates(int i, int j);

    /**
     * Returns a two-dimensional horizontal coordinate reference system.
     * @return a two-dimensional horizontal coordinate reference system.
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem();

    /** Returns 2 */
    @Override
    public int getDimension();
    
    /**
     * Finds the nearest (two-dimensional) grid point to the given position.
     * @return the nearest grid point to the given position, or null if the
     * position is outside the {@link BoundingBox bounding box} of the grid.
     */
    public GridCoordinates findNearestGridPoint(HorizontalPosition pos);
        
    /**
     * Finds the nearest grid points to each of the positions in the given list
     * in a single operation.  The results of this method will be exactly
     * equivalent to calling {@link #findNearestGridPoint(uk.ac.rdg.resc.edal.position.HorizontalPosition)}
     * for each point within the domain.  The order of the grid coordinates in
     * the returned list will be the same as the order of the positions within
     * the domain.
     */
    public List<GridCoordinates> findNearestGridPoints(DiscretePointDomain<HorizontalPosition> positions);

    /**
     * Gets the 2D bounding box of the grid in the grid's
     * {@link #getCoordinateReferenceSystem() coordinate reference system}.
     */
    @Override
    public Extent<HorizontalPosition> getExtent();
}