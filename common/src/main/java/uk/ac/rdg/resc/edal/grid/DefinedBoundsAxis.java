/*******************************************************************************
 * Copyright (c) 2016 The University of Reading
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

package uk.ac.rdg.resc.edal.grid;

import java.util.List;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * Defines a {@link ReferenceableAxis} where the bounds between cells are
 * explicitly specified.
 *
 * @author Guy Griffiths
 */
public class DefinedBoundsAxis extends ReferenceableAxisImpl implements ReferenceableAxis<Double> {
    private static final long serialVersionUID = 1L;
    private List<Extent<Double>> bounds;
    private Extent<Double> axisExtent;

    public DefinedBoundsAxis(String name, List<Double> axisValues, List<Extent<Double>> axisBounds,
            boolean isLongitude) {
        super(name, axisValues, isLongitude);
        if (axisValues.size() != axisBounds.size()
                ) {
            throw new IllegalArgumentException(
                    "To specify the cell boundaries of an axis, you must provide the same number of bounds as axis values");
        }
        bounds = axisBounds;
        Double low = axisBounds.get(0).getLow(); 
        Double high = axisBounds.get(axisBounds.size()-1).getHigh();
        if(low < high) {
            axisExtent = Extents.newExtent(low, high);
        } else {
            /*
             * Descending value axis
             */
            axisExtent = Extents.newExtent(high, low);
        }
    }

    @Override
    public Extent<Double> getCoordinateBounds(int index) {
        return bounds.get(index);
    }

    @Override
    public Extent<Double> getCoordinateExtent() {
        return axisExtent;
    }

    @Override
    protected Double getMidpoint(Double pos1, Double pos2) {
        /*
         * This method is only used for calculating the coordinate bounds of a
         * particular cell.
         * 
         * Because this is specified exactly, it is never used.
         */
        throw new UnsupportedOperationException(
                "This method should never be used.  If you see this message, report it as a bug.");
    }

    @Override
    protected Double extendFirstValue(Double firstVal, Double nextVal) {
        /*
         * This method is only used for calculating the coordinate bounds of the
         * entire axis.
         * 
         * Because this is specified exactly, it is never used.
         */
        throw new UnsupportedOperationException(
                "This method should never be used.  If you see this message, report it as a bug.");
    }

    @Override
    protected Double extendLastValue(Double lastVal, Double secondLastVal) {
        /*
         * This method is only used for calculating the coordinate bounds of the
         * entire axis.
         * 
         * Because this is specified exactly, it is never used.
         */
        throw new UnsupportedOperationException(
                "This method should never be used.  If you see this message, report it as a bug.");
    }
}
