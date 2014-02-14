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

public class ThresholdColourScheme2D extends ColourScheme2D {
    private Color noDataColour = new Color(0f, 0f, 0f, 0f);

    /*
     * Thresholds (their lower boundaries) and colours are supplied in ascending order,
     * because that is the logical way to think about things, but then reversed in order.
     */
    private List<Float> xThresholds;
    private List<Float> yThresholds;
    private List<Color> colours;

    public ThresholdColourScheme2D(List<Float> xThresholds, List<Float> yThresholds,
    		List<Color> colours, Color noDataColour) {
        super();
        this.noDataColour = noDataColour;
        this.xThresholds = xThresholds;
        this.yThresholds = yThresholds;
        this.colours = colours;
        initialiseColours();
    }
    
	@Override
	public Color getColor(Number xValue, Number yValue) {
		if(xValue == null || Float.isNaN(xValue.floatValue())
				|| yValue == null || Float.isNaN(yValue.floatValue())) {
            return noDataColour;
        }
        /*
         * Remember: THESE LISTS ARE IN REVERSE ORDER.
         */
		Iterator<Color> colourIterator = colours.iterator();
		Color colour = colourIterator.next();
        for(Float yBand: yThresholds) {
        	for(Float xBand: xThresholds) {
        		if(yValue.floatValue() > yBand && xValue.floatValue() > xBand) {
        			return colour;
        		}
        		colour = colourIterator.next();
        	}
        	if(yValue.floatValue() > yBand) {
    			return colour;
        	}
        	colour = colourIterator.next();
        }
        for(Float xBand: xThresholds) {
    		if(xValue.floatValue() > xBand) {
    			return colour;
    		}
    		colour = colourIterator.next();
    	}
        return colour;
	}

    private void initialiseColours() {
        if(xThresholds == null || xThresholds.size() < 1) {
            throw new IllegalArgumentException("X threshold values must not be null and must have at least one value");
        }
        if(yThresholds == null || yThresholds.size() < 1) {
            throw new IllegalArgumentException("Y threshold values must not be null and must have at least one value");
        }
        /*
         * Check that there are the correct number of colours.
         */
        if (colours == null || colours.size() != (xThresholds.size() + 1)*(yThresholds.size() + 1)) {
        	throw new IllegalArgumentException("Colours must not be null and must be in the correct number to match the x and y thresholds.");
        }
        /*
         * Check that thresholds are correctly ordered.  Then reverse lists. 
         */
        Float value = -Float.MAX_VALUE;
        for(Float band : xThresholds) {
            if(band < value) {
                throw new IllegalArgumentException(
                        "Threshold bands must be in ascending order of value");
            }
            value = band;
        }
        if(yThresholds != xThresholds) {
        	Collections.reverse(xThresholds);
        }
        
        value = -Float.MAX_VALUE;
        for(Float band : yThresholds) {
            if(band < value) {
                throw new IllegalArgumentException(
                        "Threshold bands must be in ascending order of value");
            }
            value = band;
        }
        Collections.reverse(yThresholds);
        
        Collections.reverse(colours);
    }

	@Override
	public Float getScaleMin(int dimension) {
		switch (dimension) {
			case 1:
				return xThresholds.get(xThresholds.size() - 1);
			case 2:
				return yThresholds.get(yThresholds.size() - 1);
			default:
				throw new IllegalArgumentException("Dimension must be either 1 or 2.");
		}
	}

	@Override
	public Float getScaleMax(int dimension) {
		switch (dimension) {
			case 1:
				return xThresholds.get(0);
			case 2:
				return yThresholds.get(0);
			default:
				throw new IllegalArgumentException("Dimension must be either 1 or 2.");
		}	
	}

}
