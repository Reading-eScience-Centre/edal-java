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

package uk.ac.rdg.resc.edal.dataset;

import java.util.List;

import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;

/**
 * This is an implementation of a {@link DomainMapper} which maps 2D indices
 * from a source grid onto a pair of integers (as <code>int[]</code>) in the
 * target domain.
 * 
 * The first element of the returned array represents the x-component.
 * 
 * The second element of the returned array represents the y-component.
 * 
 * It also includes a static method
 * {@link Domain2DMapper#forGrid(HorizontalGrid, HorizontalGrid)} which
 * generates a {@link Domain2DMapper} from a source and a target grid.
 * 
 * @author Guy
 */
public class Domain2DMapper extends DomainMapper<int[]> {
    private int targetXSize;
    private int targetYSize;

    protected Domain2DMapper(HorizontalGrid sourceGrid, int targetXSize, int targetYSize) {
        super(sourceGrid, targetXSize * targetYSize);
        this.targetXSize = targetXSize;
        this.targetYSize = targetYSize;
    }

    @Override
    protected int[] convertIndexToCoordType(int index) {
        /*
         * We're mapping a single int to a pair of co-ordinates, based on the
         * target grid size.
         */
        int[] ret = new int[] { (index % targetXSize), (index / targetXSize) };
        return ret;
    }

    private int convertCoordsToIndex(int i, int j) {
        return j * targetXSize + i;
    }

    /**
     * Gets the x-size of the target grid
     */
    public int getTargetXSize() {
        return targetXSize;
    }

    /**
     * Gets the y-size of the target grid
     */
    public int getTargetYSize() {
        return targetYSize;
    }

    /**
     * Initialises a {@link Domain2DMapper} from a source and a target grid.
     * 
     * @param sourceGrid
     *            A {@link HorizontalGrid} representing the domain of the source
     *            data
     * @param targetGrid
     *            A {@link HorizontalGrid} representing the domain of the target
     * @return A {@link Domain2DMapper} performing the mapping
     */
    public static Domain2DMapper forGrid(HorizontalGrid sourceGrid, final HorizontalGrid targetGrid) {
        if (sourceGrid instanceof RectilinearGrid
                && targetGrid instanceof RectilinearGrid
                && GISUtils.crsMatch(sourceGrid.getCoordinateReferenceSystem(),
                        targetGrid.getCoordinateReferenceSystem())) {
            /*
             * We can gain efficiency if the source and target grids are both
             * rectilinear grids with the same CRS.
             */
            /*
             * WASTODO: could also be efficient for any matching CRS? But how
             * test for CRS equality, when one CRS will have been created from
             * an EPSG code and the other will have been inferred from the
             * source data file (e.g. NetCDF) TODO: implemented - test that it
             * works when it should!
             */
            return forMatchingCrsGrids((RectilinearGrid) sourceGrid, (RectilinearGrid) targetGrid);
        } else {
            /*
             * We can't gain efficiency, so we just initialise for general grids
             */
            return forGeneralGrids(sourceGrid, targetGrid);
        }
    }

    /*-
     * Initialise the Domain2DMapper for 2 grids which:
     * 
     * a) Have matching CRSs
     * b) Are rectilinear
     * 
     * This is the optimised method
     */
    private static Domain2DMapper forMatchingCrsGrids(RectilinearGrid sourceGrid,
            RectilinearGrid targetGrid) {
        Domain2DMapper mapper = new Domain2DMapper(sourceGrid, targetGrid.getXAxis().size(),
                targetGrid.getYAxis().size());

        log.debug("Using optimized method for coordinates with orthogonal 1D axes in the same CRS");

        ReferenceableAxis<Double> sourceGridXAxis = sourceGrid.getXAxis();
        ReferenceableAxis<Double> sourceGridYAxis = sourceGrid.getYAxis();

        ReferenceableAxis<Double> targetGridXAxis = targetGrid.getXAxis();
        ReferenceableAxis<Double> targetGridYAxis = targetGrid.getYAxis();

        /*
         * Calculate the indices along the x axis
         */
        int[] xIndices = new int[targetGridXAxis.size()];
        List<Double> targetGridLons = targetGridXAxis.getCoordinateValues();
        for (int i = 0; i < targetGridLons.size(); i++) {
            double targetX = targetGridLons.get(i);
            xIndices[i] = sourceGridXAxis.findIndexOf(targetX);
        }

        /*
         * Now cycle through the y-values in the target grid
         */
        for (int j = 0; j < targetGridYAxis.size(); j++) {
            double targetY = targetGridYAxis.getCoordinateValue(j);
            int yIndex = sourceGridYAxis.findIndexOf(targetY);
            if (yIndex >= 0) {
                for (int i = 0; i < xIndices.length; i++) {
                    mapper.put(xIndices[i], yIndex, mapper.convertCoordsToIndex(i, j));
                }
            }
        }

        mapper.sortIndices();
        return mapper;
    }

    /*
     * Initialise the Domain2DMapper for general HorizontalGrids
     */
    private static Domain2DMapper forGeneralGrids(HorizontalGrid sourceGrid,
            final HorizontalGrid targetGrid) {
        Domain2DMapper mapper = new Domain2DMapper(sourceGrid, targetGrid.getXSize(),
                targetGrid.getYSize());
        /*
         * Find the nearest grid coordinates to all the points in the domain
         */
        Array<GridCell2D> targetDomainObjects = targetGrid.getDomainObjects();
        for (int j = 0; j < targetGrid.getYSize(); j++) {
            for (int i = 0; i < targetGrid.getXSize(); i++) {
                targetGrid.getDomainObjects().get(j, i).getCentre();
                HorizontalPosition transformedPosition = GISUtils.transformPosition(targetDomainObjects.get(j,i).getCentre(), sourceGrid
                        .getCoordinateReferenceSystem());
                GridCoordinates2D indices = sourceGrid.findIndexOf(transformedPosition);
                if(indices != null) {
                    mapper.put(indices.getX(), indices.getY(), mapper.convertCoordsToIndex(i, j));
                }
            }
        }

        mapper.sortIndices();
        return mapper;
    }
}
