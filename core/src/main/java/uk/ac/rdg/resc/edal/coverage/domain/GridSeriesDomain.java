/*
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
 */

package uk.ac.rdg.resc.edal.coverage.domain;

import java.util.List;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell4D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.CalendarSystem;
import uk.ac.rdg.resc.edal.position.VerticalCrs;

/**
 * The domain of a {@link GridSeriesCoverage}, modelled as a composition of a horizontal
 * grid, plus t and z axes.
 * @todo Explain that GridCoordinates are 4D and explain ordering.
 * @author Jon
 */
public interface GridSeriesDomain extends DiscreteDomain<GeoPosition, GridCell4D>
{
    
    public HorizontalGrid getHorizontalGrid();

    public VerticalAxis getVerticalAxis();

    public TimeAxis getTimeAxis();

    public CoordinateReferenceSystem getHorizontalCrs();

    public VerticalCrs getVerticalCrs();

    public CalendarSystem getCalendarSystem();

    public GridCell4D findContainingCell(GeoPosition pos);

    /**
     * {@inheritDoc}
     * <p>Use this method with caution, as it is possible that the number of
     * domain objects (grid cells) will exceed {@link Integer#MAX_VALUE}, meaning
     * that the value of {@code size()} in the returned List may be inaccurate.
     * Prefer the use of {@link #size()} instead, which returns a long integer.</p>
     * @return
     */
    @Override
    public List<GridCell4D> getDomainObjects();

}
