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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.domain.Extent;

@XmlType(namespace = MapImage.NAMESPACE, propOrder={}, name="ColourScaleType")
public class ColourScale {
    // The scale range spanned by this colour scheme
    @XmlElement(required=true, name="ScaleMin")
    private Float scaleMin = -50f;
    @XmlElement(required=true, name="ScaleMax")
    private Float scaleMax = 50f;
    // Whether or not the scale is logarithmic
    @XmlElement(name="Logarithmic")
    private Boolean logarithmic = false;
    
    ColourScale(){}
    
    public ColourScale(Extent<Float> scaleRange, Boolean logarithmic) {
        this.scaleMin = scaleRange.getLow();
        this.scaleMax = scaleRange.getHigh();
        this.logarithmic = logarithmic;
    }
    
    public ColourScale(Float scaleMin, Float scaleMax, Boolean logarithmic) {
        this.scaleMin = scaleMin;
        this.scaleMax = scaleMax;
        this.logarithmic = logarithmic;
    }
    
    
    public Float getScaleMin() {
        return scaleMin;
    }

    public Float getScaleMax() {
        return scaleMax;
    }

    public Boolean isLogarithmic() {
        return logarithmic;
    }

    /**
     * Scales an input number to the range 0-1. Will return a number outside
     * this range if necessary, but the result can ONLY be interpreted as
     * "out-of-range" (i.e. the amount by which it is out-of-range should not be
     * used)
     * 
     * @param input
     *            The input number
     * @return A number from 0-1 if in range, a number outside 0-1 if
     *         out-of-range, and null if null/NaN
     */
    public Float scaleZeroToOne(Number input) {
        if(input == null || Float.isNaN(input.floatValue())) {
            return null;
        }
        
        if(logarithmic) {
            if(scaleMin <= 0.0 || scaleMax <= 0.0) {
                throw new IllegalArgumentException("Cannot log-scale zero/negative numbers");
            }
            if(input.floatValue() <= 0.0f) {
                /*
                 * Below min scale, but logarithmic so this would cause an
                 * error. Just need to return a number which is outside the 0-1
                 * range.
                 */
                return -1f;
            }
            return (float) ((Math.log(input.doubleValue()) - Math.log(scaleMin)) / (Math.log(scaleMax) - Math.log(scaleMin)));
        } else {
            return ((input.floatValue() - scaleMin) / (scaleMax - scaleMin));
        }
    }
}
