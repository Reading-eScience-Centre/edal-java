package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = Image.NAMESPACE, propOrder={}, name="ColourScaleType")
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
