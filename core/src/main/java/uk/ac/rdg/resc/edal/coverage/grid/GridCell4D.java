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

package uk.ac.rdg.resc.edal.coverage.grid;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import uk.ac.rdg.resc.edal.Domain;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.geometry.Polygon;
import uk.ac.rdg.resc.edal.position.CalendarSystem;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

/**
 * A cell in a {@link GridSeriesDomain}, which can have up to four dimensions.
 * 
 * @author Jon Blower
 */
public interface GridCell4D extends Domain<GeoPosition> {

    public GridCoordinates2D getHorizontalCoordinates();

    /**
     * @return the time index of the grid cell in the parent grid
     */
    public int getTimeIndex();

    /**
     * @return the z index of the grid cell in the parent grid
     */
    public int getVerticalIndex();

    /**
     * @return the centre of the grid cell in horizontal space
     */
    public HorizontalPosition getCentre();

    /**
     * @return the footprint of this grid cell in horizontal space.
     */
    public Polygon getFootprint();

    /**
     * @return the {@link CoordinateReferenceSystem} of the horizontal component
     *         of the grid cell
     */
    public CoordinateReferenceSystem getHorizontalCrs();

    /**
     * @return the range of valid positions in the time axis of parent grid,
     * or null if there is no time axis.
     */
    public Extent<TimePosition> getTimeExtent();

    /**
     * @return the range of valid positions in the vertical axis of parent grid,
     * or null if there is no vertical axis
     */
    public Extent<VerticalPosition> getVerticalExtent();

    /**
     * @return the {@link CalendarSystem} used in this grid cell
     */
    public CalendarSystem getCalendarSystem();

    /**
     * @return the parent grid of this grid cell
     */
    public GridSeriesDomain getGrid();

}
