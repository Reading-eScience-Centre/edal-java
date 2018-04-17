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

package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ThresholdColourScheme extends EnumeratedColourScheme {
    /*
     * These hold lists of colours and the value which marks the lower boundary
     * of their threshold, IN REVERSE ORDER OF VALUES. It is supplied in
     * ascending order (because this is the logical way to think of things), but
     * is reversed in the setter.
     */
    private List<Float> thresholds;
    private List<Color> colours;
    
    private Color noDataColour = new Color(0f, 0f, 0f, 0f);
    
    public ThresholdColourScheme(List<Float> thresholds, List<Color> colours, Color noDataColour) {
        super();
        this.noDataColour = noDataColour;
        this.thresholds = thresholds;
        this.colours = colours;
        initialiseColours();
    }

    @Override
    public Color getColor(Number value) {
        if(value == null || Float.isNaN(value.floatValue())) {
            return noDataColour;
        }
        /*
         * Remember: THIS LIST IS IN REVERSE ORDER.
         */
        Iterator<Color> colourIterator = colours.iterator();
		Color colour = colourIterator.next();
        for(Float band : thresholds) {
            if(value.floatValue() > band) {
                return colour;
            }
            colour = colourIterator.next();
        }
        return colour;
    }
    
    private void initialiseColours() {
        if(thresholds == null || thresholds.size() < 1) {
            throw new IllegalArgumentException("Threshold values must not be null and must have at least one value");
        }
        /*
         * Check that there are the correct number of colours.
         */
        if (colours == null || colours.size() != (thresholds.size() + 1)) {
        	throw new IllegalArgumentException("Colours must not be null and must be in the correct number to match the thresholds.");
        }
        /*
         * Check that thresholds are correctly ordered.  Then reverse lists. 
         */
        Float value = -Float.MAX_VALUE;
        for(Float band : thresholds) {
            if(band < value) {
                throw new IllegalArgumentException(
                        "Threshold bands must be in ascending order of value");
            }
            value = band;
        }
        Collections.reverse(thresholds);
        
        Collections.reverse(colours);
    }

    @Override
    public Float getScaleMin() {
        return thresholds.get(thresholds.size() - 1);
    }

    @Override
    public Float getScaleMax() {
        return thresholds.get(0);
    }
    
    @Override
    public List<Float> getEnumeratedPoints() {
        return thresholds;
    }
}
