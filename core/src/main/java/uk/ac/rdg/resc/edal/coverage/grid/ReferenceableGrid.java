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
import uk.ac.rdg.resc.edal.coverage.domain.DiscreteDomain;

/**
 * <p>A {@link Grid} whose points are referenceable to an external coordinate
 * reference system.</p>
 * 
 * <p>Note that the number of dimensions in the external coordinate reference system
 * is not necessarily the same as the number of dimensions in the Grid.  It is
 * perfectly possible to have a two-dimensional grid that samples points from a
 * three-dimensional coordinate system (e.g. a 2D vertical grid in a latitude-longitude-depth
 * coordinate system).</p>
 *
 * <p>The ordering of grid coordinates with respect to real-world coordinates
 * is not defined, since in general the axes of the grid will have no
 * necessary relation to the axes of the real-world coordinate system.
 * Implementations must ensure that they use consistent ordering, which is also
 * respected in the methods of {@link GridValuesMatrix} if this is inherited.
 * (This behaviour is refined in the @link RectilinearGrid} subclass.)</p>
 *
 * <p>Subclasses must implement a method that returns a coordinate reference system
 * (or semantically equivalent object) that is used to reference the positions
 * returned by {@link #transformCoordinates(uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates)}.</p>
 *
 * @param <P> The type of object used to identify real-world positions within
 * this grid
 * @author Jon
 * @todo define getExtent()
 */
public interface ReferenceableGrid<P> extends Grid, DiscreteDomain<P, GridPoint> {

    /**
     * Transforms a grid coordinates to a real-world position.  The returned
     * position is referenced to this Grid's coordinate reference system.
     * @param coords The grid coordinates to transform.
     * @return the "real world" coordinates, or null if the grid coordinates are
     * not contained within the {@link #getGridExtent() envelope of the grid}.
     * @throws IllegalArgumentException if the dimension of the grid coordinates
     * does not match the {@link #getDimension() dimension of the grid}.
     */
    public P transformCoordinates(GridCoordinates coords);

    /**
     * Transforms from a real-world position to the grid coordinates of the
     * corresponding grid point.  No interpolation is performed, i.e. the passed
     * position must exactly match a grid point.  (WATCH!  Standard actually
     * defines nearest-neighbour interpolation for this method, which seems suspect.)
     * @param pos - The "real world" coordinates to transform.
     * @return The coordinates of the corresponding grid point, or null if there
     * is no grid point that corresponds with the direct position.
     */
    public GridCoordinates inverseTransformCoordinates(P pos);

    /**
     * Returns the list of GridPoints that comprise this grid.  Each GridPoint
     * has a {@link Footprint} that defines its extent in an external coordinate
     * reference system.
     * @return
     * @todo Define the order of iteration of the list with respect to the grid
     */
    @Override
    public List<GridPoint> getDomainObjects();

}
