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

import java.util.Collection;

import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * This is an implementation of a {@link DomainMapper} which maps 2D indices
 * from a source grid onto a single integer in the target domain.
 * 
 * It also includes a static method
 * {@link Domain1DMapper#forList(HorizontalGrid, Collection)} which generates a
 * {@link Domain1DMapper} from a source grid and a list of target positions.
 * 
 * @author Guy
 */
public class Domain1DMapper extends DomainMapper<Integer> {
    protected Domain1DMapper(HorizontalGrid sourceGrid, long targetDomainSize) {
        super(sourceGrid, targetDomainSize);
    }

    @Override
    protected Integer convertIndexToCoordType(int index) {
        /*
         * We're mapping from an int to an int, so we just return it.
         */
        return index;
    }

    /**
     * Creates a {@link Domain1DMapper} from a {@link HorizontalGrid} source and
     * a {@link Collection} of {@link HorizontalPosition}s for the target
     * 
     * @param sourceGrid
     *            A {@link HorizontalGrid} on which the source data is defined
     * @param targetPositions
     *            A {@link Collection} of {@link HorizontalPosition}s defining
     *            the target domain.
     * @return A {@link Domain1DMapper} which performs the mapping
     */
    public static Domain1DMapper forList(HorizontalGrid sourceGrid,
            Collection<HorizontalPosition> targetPositions) {
        log.debug("Creating DomainMapper from a 1D list of points");

        long start = System.currentTimeMillis();
        Domain1DMapper mapper = new Domain1DMapper(sourceGrid, targetPositions.size());
        int pixelIndex = 0;
        /*
         * Find the nearest grid coordinates to all the points in the domain
         */
        for (HorizontalPosition pos : targetPositions) {
            /*
             * Find the index of the cell containing this position
             */
            int[] indices = sourceGrid.findIndexOf(pos);
            mapper.put(indices[0], indices[1], pixelIndex);
            pixelIndex++;
        }
        long end = System.currentTimeMillis();
        mapper.sortIndices();
        log.debug("DomainMapper created in " + (end - start) + "ms");
        return mapper;
    }
}
