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

package uk.ac.rdg.resc.edal.coverage.domain;

import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableGrid;
import uk.ac.rdg.resc.edal.coverage.grid.TemporalAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;

/**
 * <p>The domain of a {@link GridSeriesCoverage}.  It is modelled as a composition
 * of a horizontal gridded domain and optional vertical and temporal axes.  This
 * restricts the use of this interface to cases in which the vertical and temporal
 * axes are everywhere orthogonal to the horizontal grid.</p>
 *
 * <p>Note that it would be possible to model the domain as a four-dimensional
 * {@link ReferenceableGrid}, but this would introduce significant complications
 * mapping from four-dimensional grid points to real-world points, particularly
 * taking into account all the possibilities of different axis ordering.</p>
 * @author Jon
 */
public interface GridSeriesDomain
{

    public HorizontalGrid getHorizontalGrid();

    public VerticalAxis getVerticalAxis();

    public TemporalAxis getTemporalAxis();

    /**
     * Returns the total number of points in the domain, i.e. the product of
     * the sizes of the horizontal grid and the temporal and vertical axes
     */
    public int getSize();

}
