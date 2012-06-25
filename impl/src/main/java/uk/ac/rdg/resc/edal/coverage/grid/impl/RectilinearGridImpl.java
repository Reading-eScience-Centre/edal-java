/*******************************************************************************
 * Copyright (c) 2012 The University of Reading
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

package uk.ac.rdg.resc.edal.coverage.grid.impl;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.coverage.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableAxis;

/**
 * Immutable implementation of a {@link RectilinearGrid} using {@link Double}s.
 * 
 * @author Guy Griffiths
 */
public final class RectilinearGridImpl extends AbstractRectilinearGrid
{
    private final ReferenceableAxis<Double> xAxis;
    private final ReferenceableAxis<Double> yAxis;
    private final CoordinateReferenceSystem crs;

    /**
     * Instantiates a new rectilinear grid from the given axes
     * 
     * @param xAxis
     *            the x-axis
     * @param yAxis
     *            the y-axis
     * @param crs
     *            the {@link CoordinateReferenceSystem}
     */
    public RectilinearGridImpl(ReferenceableAxis<Double> xAxis, ReferenceableAxis<Double> yAxis,
            CoordinateReferenceSystem crs) {
        if (xAxis == null || yAxis == null) {
            throw new NullPointerException("Axes cannot be null");
        }
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.crs = crs;
    }

    /**
     * Instantiates a new rectilinear grid from the given axes, with no
     * {@link CoordinateReferenceSystem}
     * 
     * @param xAxis
     *            the x-axis
     * @param yAxis
     *            the y-axis
     */
    public RectilinearGridImpl(ReferenceableAxis<Double> xAxis, ReferenceableAxis<Double> yAxis) {
        this(xAxis, yAxis, null);
    }

    @Override
    public ReferenceableAxis<Double> getXAxis() {
        return xAxis;
    }

    @Override
    public ReferenceableAxis<Double> getYAxis() {
        return yAxis;
    }
    
    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return this.crs;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RectilinearGridImpl) {
            RectilinearGridImpl grid = (RectilinearGridImpl) obj;
            return grid.xAxis.equals(xAxis) && grid.yAxis.equals(yAxis) && super.equals(obj);
        } else {
            return false;
        }
    }
}
