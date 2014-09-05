/*******************************************************************************
 * Copyright (c) 2014 The University of Reading
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

package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A class representing a colour scheme where each x value is mapped to a
 * different {@link SegmentColourScheme}.
 *
 * @author Guy Griffiths
 */
public class MappedSegmentColorScheme2D extends ColourScheme2D {

    private Map<Number, SegmentColourScheme> colorSchemeMap;
    private Color nonMappedColor;

    private Float xmin = Float.MAX_VALUE;
    private Float xmax = -Float.MAX_VALUE;
    private Float ymin = Float.MAX_VALUE;
    private Float ymax = -Float.MAX_VALUE;

    public MappedSegmentColorScheme2D(Map<Number, SegmentColourScheme> colorSchemeMap,
            Color nonMappedColor) {
        this.colorSchemeMap = colorSchemeMap;
        this.nonMappedColor = nonMappedColor;

        for (Entry<Number, SegmentColourScheme> entry : colorSchemeMap.entrySet()) {
            xmin = Math.min(xmin, entry.getKey().floatValue());
            xmax = Math.min(xmax, entry.getKey().floatValue());

            ymin = Math.min(ymin, entry.getValue().getScaleMin());
            ymax = Math.min(ymax, entry.getValue().getScaleMax());
        }
    }

    @Override
    public Color getColor(Number xValue, Number yValue) {
        if (colorSchemeMap.containsKey(xValue)) {
            return colorSchemeMap.get(xValue).getColor(yValue);
        } else {
            return nonMappedColor;
        }
    }

    @Override
    public Float getScaleMin(int dimension) {
        if (dimension == 0) {
            return xmin;
        } else {
            return ymin;
        }
    }

    @Override
    public Float getScaleMax(int dimension) {
        if (dimension == 0) {
            return xmax;
        } else {
            return ymax;
        }
    }

}
