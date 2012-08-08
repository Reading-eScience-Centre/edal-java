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

package uk.ac.rdg.resc.edal.coverage;

import java.util.Set;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell4D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

/**
 * A multidimensional grid.
 * 
 * @author Jon Blower
 */
public interface GridSeriesCoverage extends BaseGridCoverage<GeoPosition, GridCell4D> {

    @Override
    public GridSeriesDomain getDomain();

    /**
     * <p>
     * Extracts a GridCoverage2D whose domain matches the passed-in target grid
     * for the given member names. The returned GridCoverage2D will be
     * memory-resident.
     * </p>
     * <p>
     * The values in the returned Coverage are taken from the source Coverage
     * (i.e. this Coverage) according to the following pseudocode:
     * </p>
     * 
     * <pre>
     * for (GridCell2D targetGridCell : targetGrid.getDomainObjects()) {
     *     Object value = this.evaluate(targetGridCell.getCentre(), memberNames);
     *     addValueToCoverage(value);
     * }
     * </pre>
     * 
     * @return a memory-resident GridCoverage2D whose domain matches the
     *         passed-in target grid and whose range includes the given coverage
     *         members.
     */
    public GridCoverage2D extractGridCoverage(HorizontalGrid targetGrid, VerticalPosition zPos,
            TimePosition tPos, Set<String> memberNames);

    /**
     * Extract a {@link ProfileCoverage} whose vertical domain is the same as
     * the parent grid, and whose 3D (2D spatial, 1D time) position is specified
     * in the arguments
     * 
     * @param pos
     *            the {@link HorizontalPosition} of the profile
     * @param time
     *            the {@link TimePosition} of the profile
     * @param memberNames
     *            the coverage members to extract
     * @return a memory-resident {@link ProfileCoverage}
     */
    public ProfileCoverage extractProfileCoverage(HorizontalPosition pos, TimePosition time,
            Set<String> memberNames);

    /**
     * Extract a {@link PointSeriesCoverage} whose domain is specified by the
     * arguments
     * 
     * @param pos
     *            the {@link HorizontalPosition} of the domain of the
     *            {@link PointSeriesCoverage}
     * @param zPos
     *            the {@link VerticalPosition} of the domain of the
     *            {@link PointSeriesCoverage}
     * @param tExtent
     *            the {@link Extent} of the {@link TimeAxis} required for the
     *            domain of the {@link PointSeriesCoverage}
     * @param memberNames
     *            the coverage members to extract
     * @return a memory-resident {@link PointSeriesCoverage}
     */
    public PointSeriesCoverage extractPointSeriesCoverage(HorizontalPosition pos,
            VerticalPosition zPos, Extent<? extends TimePosition> tExtent, Set<String> memberNames);
}
