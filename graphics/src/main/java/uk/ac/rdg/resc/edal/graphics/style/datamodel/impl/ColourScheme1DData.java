package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.Color;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.graphics.style.Palette1D;
import uk.ac.rdg.resc.edal.graphics.style.StyleXMLParser.ColorAdapter;

@XmlType(namespace = Image.NAMESPACE, propOrder={}, name="ColourSchemeType")
public class ColourScheme1DData {
    // The scale range spanned by this colour scheme
    @XmlElement(required=true, name="ScaleMin")
    private Float scaleMin = -50f;
    @XmlElement(required=true, name="ScaleMax")
    private Float scaleMax = 50f;
    // The colour to plot for values below the minimum. If null, then use the
    // lowest value in the palette
    @XmlElement(name="BelowMinColour")
    @XmlJavaTypeAdapter(ColorAdapter.class)
    private Color belowMinColour = null;
    // The colour to plot for values above the maximum. If null, then use the
    // highest value in the palette
    @XmlElement(name="AboveMaxColour")
    @XmlJavaTypeAdapter(ColorAdapter.class)
    private Color aboveMaxColour = null;
    // The colour to plot for missing data
    @XmlElement(name="MissingDataColour")
    @XmlJavaTypeAdapter(ColorAdapter.class)
    private Color noDataColour = new Color(0, 0, 0, 0);
    // The palette to use.
    @XmlElement(name="Palette")
    private Palette1D palette = new Palette1D();
   // Whether or not the scale is logarithmic
    @XmlElement(name="Logarithmic")
    private Boolean logarithmic = false;

    ColourScheme1DData() {
    }
    
    public ColourScheme1DData(Extent<Float> scaleRange, Color belowMinColour, Color aboveMaxColour, Color noDataColour,
            Palette1D palette, Boolean logarithmic) {
        scaleMin = scaleRange.getLow();
        scaleMax = scaleRange.getHigh();
        
        this.belowMinColour = belowMinColour;
        this.aboveMaxColour = aboveMaxColour;
        this.noDataColour = noDataColour;
        
        this.palette = palette;
        
        this.logarithmic = logarithmic;
    }
    

    public Color getColor(Number value) {
        if (value == null || Float.isNaN(value.floatValue())) {
            return noDataColour; // if no data present return this color
        } else {
            double min = logarithmic ? Math.log(scaleMin) : scaleMin;
            double max = logarithmic ? Math.log(scaleMax) : scaleMax;
            double scaledVal = logarithmic ? Math.log(value.doubleValue()) : value.doubleValue();

            // Handle out of range pixels
            if (scaledVal < min) {
                if (belowMinColour == null) {
                    scaledVal = min;
                } else {
                    return belowMinColour;
                }
            } else if (scaledVal > max) {
                if (aboveMaxColour == null) {
                    scaledVal = max;
                } else {
                    return aboveMaxColour;
                }
            }

            float frac = (float) ((scaledVal - min) / (max - min));
            /*
             * Ensure that frac is not less than 0 or greater then 1
             * due to rounding errors.
             */
            if (frac < 0.0f) frac = 0.0f;
            if (frac > 1.0f) frac = 1.0f;
            return this.palette.getColor(frac);
        }
    }
}
