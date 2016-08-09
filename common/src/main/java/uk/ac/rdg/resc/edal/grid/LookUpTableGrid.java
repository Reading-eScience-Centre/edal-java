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
package uk.ac.rdg.resc.edal.grid;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.CurvilinearCoords;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.CurvilinearCoords.Cell;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;
import uk.ac.rdg.resc.edal.util.LookUpTable;

/**
 * A HorizontalGrid that is created from a "curvilinear" coordinate system,
 * {@literal i.e.} one in which the latitude/longitude coordinates of each grid
 * point are specified using two-dimensional coordinate axes, which explicitly
 * give the lat/lon of each point in the horizontal plane. In these coordinate
 * systems, finding the nearest grid point to a given lat/lon point is complex.
 * Therefore we pre-calculate a {@link LookUpTable "look-up table"} of the
 * nearest i-j indices to a set of lat-lon points. Coordinate conversions using
 * such a look-up table are not precise but may suffice for many applications.
 *
 * @author Guy Griffiths
 * @author Jon Blower
 */
public final class LookUpTableGrid extends AbstractCurvilinearGrid {
    /**
     * In-memory cache of LookUpTableGrid objects to save expensive
     * re-generation of same object
     *
     * @todo The CurvilinearGrid objects can be very big. Really we only need to
     *       key on the arrays of lon and lat: all other quantities can be
     *       calculated from these. This means that we could make other large
     *       objects available for garbage collection.
     */
    private static final Map<CurvilinearCoords, LookUpTableGrid> CACHE = new HashMap<CurvilinearCoords, LookUpTableGrid>();

    private final LookUpTable lut;

    /**
     * The passed-in coordSys must have 2D horizontal coordinate axes.
     */
    public static LookUpTableGrid generate(Array2D<Number> lonVals, Array2D<Number> latVals) {
        CurvilinearCoords curvCoords = new CurvilinearCoords(lonVals, latVals);

        /*
         * We calculate the required resolution of the look-up tables. We want
         * this to be around 3 times the resolution of the grid.
         */
        double minLutResolution = Math.sqrt(curvCoords.getMeanCellArea()) / 3.0;

        synchronized (CACHE) {
            LookUpTableGrid lutGrid = CACHE.get(curvCoords);
            if (lutGrid == null) {
                /* Create a look-up table for this coord sys */
                LookUpTable lut = new LookUpTable(curvCoords, minLutResolution);
                /* Create the LookUpTableGrid */
                lutGrid = new LookUpTableGrid(curvCoords, lut);
                /* Now put this in the cache */
                CACHE.put(curvCoords, lutGrid);
            }
            return lutGrid;
        }
    }

    public static void clearCache() {
        synchronized (CACHE) {
            CACHE.clear();
        }
    }

    /** Private constructor to prevent direct instantiation */
    private LookUpTableGrid(CurvilinearCoords curvGrid, LookUpTable lut) {
        super(curvGrid);
        this.lut = lut;
    }

    @Override
    public GridCoordinates2D findIndexOf(HorizontalPosition position) {
        if(!GISUtils.isWgs84LonLat(position.getCoordinateReferenceSystem())) {
            position = GISUtils.transformPosition(position, GISUtils.defaultGeographicCRS());
        }
        double x = position.getX();
        double y = position.getY();
        /*
         * Find the "first guess" at the containing cell according to the
         * look-up table
         */
        int[] lutCoords = lut.getGridCoordinates(x, y);
        /* Return null if the latLonPoint does not match a valid grid point */
        if (lutCoords == null)
            return null;
        /*
         * Check that this cell really contains this point, if not, check the
         * neighbours
         */
        Cell cell = curvCoords.getCell(lutCoords[0], lutCoords[1]);
        if (cell.contains(x, y)) {
            return new GridCoordinates2D(lutCoords[0], lutCoords[1]);
        }

        /*
         * We do a gradient-descent method to find the true nearest neighbour We
         * store the grid coordinates that we have already examined.
         */
        Set<Cell> examined = new HashSet<Cell>();
        examined.add(cell);
        /*
         * Find the Euclidean distance from the cell centre to the target
         * position
         */
        double shortestDistanceSq = cell.findDistanceSq(x, y);

        boolean found = true;
        /* Prevent the search going on forever */
        int maxIterations = 100;
        for (int i = 0; found && i < maxIterations; i++) {
            found = false;
            for (Cell neighbour : cell.getNeighbours()) {
                if (!examined.contains(neighbour)) {
                    double distanceSq = neighbour.findDistanceSq(x, y);
                    if (distanceSq < shortestDistanceSq) {
                        cell = neighbour;
                        shortestDistanceSq = distanceSq;
                        found = true;
                    }
                    examined.add(neighbour);
                }
            }
        }

        /*
         * We now have the nearest neighbour, but sometimes the position is
         * actually contained within one of the cell's neighbours
         */
        if (cell.contains(x, y)) {
            return new GridCoordinates2D(cell.getI(), cell.getJ());
        }
        for (Cell neighbour : cell.getNeighbours()) {
            if (neighbour.contains(x, y)) {
                return new GridCoordinates2D(neighbour.getI(), neighbour.getJ());
            }
        }

        /*
         * TODO The point is probably on the edge between grid cells and failing
         * the contains() checks. This is probably OK in the middle of a grid,
         * but we might need to be careful at the edges
         */
        return new GridCoordinates2D(cell.getI(), cell.getJ());
    }

    @Override
    public int getXSize() {
        return curvCoords.getNi();
    }

    @Override
    public int getYSize() {
        return curvCoords.getNj();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((lut == null) ? 0 : lut.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        LookUpTableGrid other = (LookUpTableGrid) obj;
        if (lut == null) {
            if (other.lut != null)
                return false;
        } else if (!lut.equals(other.lut))
            return false;
        return true;
    }
}
