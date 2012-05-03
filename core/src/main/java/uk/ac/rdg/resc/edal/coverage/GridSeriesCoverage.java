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

import java.util.List;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell4D;
import uk.ac.rdg.resc.edal.position.GeoPosition;

/**
 * A multidimensional grid.
 * @author Jon Blower
 */
public interface GridSeriesCoverage extends DiscreteCoverage<GeoPosition, GridCell4D> {

    @Override
    public GridSeriesDomain getDomain();

    /**
     * Get the value of the coverage at a particular point, specified by the
     * integer indices of the coverage
     * 
     * @param tindex
     *            the time index
     * @param zindex
     *            the vertical index
     * @param yindex
     *            the y index
     * @param xindex
     *            the x index
     * @return the value from the coverage
     */
    public Record evaluate(int tindex, int zindex, int yindex, int xindex);

    /**
     * Gets a list of values in the coverage over a range of points
     * 
     * @param tindexExtent
     *            the {@link Extent} of the time axis integers
     * @param zindexExtent
     *            the {@link Extent} of the vertical axis integers
     * @param yindexExtent
     *            the {@link Extent} of the y-axis integers
     * @param xindexExtent
     *            the {@link Extent} of the x-axis integers
     * @return a list of values from the coverage
     */
    public List<Record> evaluate(Extent<Integer> tindexExtent, Extent<Integer> zindexExtent,
            Extent<Integer> yindexExtent, Extent<Integer> xindexExtent);

}
