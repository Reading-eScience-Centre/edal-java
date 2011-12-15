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

package uk.ac.rdg.resc.edal.feature;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.Record;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

/**
 * Represents data held on a multidimensional grid.
 * 
 * @param <R>
 *            The type of the value returned by the coverage; for a compound
 *            coverage this type will be {@link Record}.
 * @author Jon Blower
 */
public interface GridSeriesFeature<R> extends Feature {

    @Override
    public GridSeriesCoverage<R> getCoverage();

    /**
     * Convenience method to extract a {@link ProfileFeature} for plotting
     * purposes
     * 
     * @param pos
     *            the {@link HorizontalPosition} of the desired
     *            {@link ProfileFeature}
     * @param time
     *            the {@link TimePosition} of the desired {@link ProfileFeature}
     * @return the extracted {@link ProfileFeature}
     */
    public ProfileFeature<R> extractProfileFeature(HorizontalPosition pos, TimePosition time);

    /**
     * Convenience method to extract a {@link PointSeriesFeature} for plotting
     * purposes
     * 
     * @param pos
     *            the {@link HorizontalPosition} of the desired
     *            {@link PointSeriesFeature}
     * @param z
     *            the {@link VerticalPosition} of the desired
     *            {@link PointSeriesFeature}
     * @param tRange
     *            the range of {@link TimePosition}s of the desired
     *            {@link PointSeriesFeature}
     * @return the extracted {@link PointSeriesFeature}
     */
    public PointSeriesFeature<R> extractPointSeriesFeature(HorizontalPosition pos,
            VerticalPosition z, Extent<TimePosition> tRange);

    /**
     * Convenience method to extract a horizontal layer for plotting purposes
     * 
     * @param tindex
     *            The index of the time dimension required
     * @param zindex
     *            The index of the elevation dimension required
     * @param targetDomain
     *            The desired domain of the resultant coverage
     * @return A list of values, with x varying first
     */
    public GridCoverage2D<R> extractHorizontalGrid(int tindex, int zindex,
            HorizontalGrid targetDomain);

    /**
     * Convenience method to extract a horizontal layer for plotting purposes
     * 
     * @param tPos
     *            The time value required
     * @param zPos
     *            The elevation value required
     * @param targetDomain
     *            The desired domain of the resultant coverage
     * @return A list of values, with x varying first
     */
    public GridCoverage2D<R> extractHorizontalGrid(TimePosition tPos, double zPos,
            HorizontalGrid targetDomain);
    
//    public TransectFeature extractTransectFeature(Arguments gohere);
    
}
