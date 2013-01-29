package uk.ac.rdg.resc.edal.graphics.style.datamodel.model;

import java.awt.Color;
import java.awt.image.IndexColorModel;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.graphics.style.ColorPalette;
import uk.ac.rdg.resc.edal.graphics.style.StyleXMLParser.ColorAdapter;

@XmlType(namespace = ImageData.NAMESPACE, propOrder={}, name="ColourSchemeType")
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
    // The name of the palette to use.
    @XmlElement(name="PaletteName")
    private String paletteName = null;
    // The opacity of the color palette
    @XmlElement(name="Opacity")
    private Float opacity = 1.0f;
    // The number of color bands to use
    @XmlElement(name="NumberOfColourBands")
    private Integer numColourBands = 254;
    // Whether or not the scale is logarithmic
    @XmlElement(name="Logarithmic")
    private Boolean logarithmic = false;

    private ColorPalette palette;
    private IndexColorModel indexColorModel;

    ColourScheme1DData() {
    }
    
    public ColourScheme1DData(Extent<Float> scaleRange, Color belowMinColour, Color aboveMaxColour, Color noDataColour,
            String paletteName, Float opacity, Integer numColourBands, Boolean logarithmic) {
        scaleMin = scaleRange.getLow();
        scaleMax = scaleRange.getHigh();
        
        this.belowMinColour = belowMinColour;
        this.aboveMaxColour = aboveMaxColour;
        this.noDataColour = noDataColour;
        
        this.paletteName = paletteName;
        
        this.opacity = opacity;
        this.numColourBands = numColourBands;
        
        this.logarithmic = logarithmic;
    }
    

    public Color getColor(Number value) {
        if (palette == null || indexColorModel == null) {
            // Set the palette to that specified in paletteName
            palette = ColorPalette.get(paletteName);

            // Get the colour model
            indexColorModel = palette.getColorModel(numColourBands, (int) (100f*opacity));
        }
        /*
         * We can directly access values[0] since values is checked to be of
         * size 1 in the superclass.
         */
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

            double frac = (scaledVal - min) / (max - min);
            // Compute the index of the corresponding colour
            int index = (int) (frac * numColourBands);
            /*
             * For values very close to the maximum value in the range index
             * might turn out to be equal to numColourBands due to rounding
             * error. In this case we subtract one from the index to ensure that
             * these pixels are displayed correctly.
             */
            if (index == numColourBands) {
                index--;
            }

            // return the corresponding colour
            return new Color(indexColorModel.getRGB(index), true);
        }
    }
}
