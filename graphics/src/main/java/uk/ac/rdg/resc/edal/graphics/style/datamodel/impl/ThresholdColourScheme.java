package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.rdg.resc.edal.graphics.style.StyleXMLParser.ColorAdapter;

@XmlType(namespace = Image.NAMESPACE, propOrder = {"minColour", "colours", "noDataColour"}, name = "ThresholdColourSchemeType")
public class ThresholdColourScheme extends ColourScheme {

    @XmlType(namespace = Image.NAMESPACE, propOrder = {}, name = "ColourAndValueType")
    public static class ColourAndValue {
        @XmlElement(name = "Colour", required = true, nillable = false)
        @XmlJavaTypeAdapter(ColorAdapter.class)
        private Color colour;
        @XmlElement(name = "Value", required = true, nillable = false)
        private Float value;
    }
    
    @XmlElement(name = "MinColour", required = true, nillable = false)
    @XmlJavaTypeAdapter(ColorAdapter.class)
    private Color minColour;
    
    @XmlElement(name = "MissingDataColour")
    @XmlJavaTypeAdapter(ColorAdapter.class)
    private Color noDataColour = new Color(0f, 0f, 0f, 0f);

    /*
     * This holds a list of colours and the value which marks the lower boundary
     * of their threshold, IN REVERSE ORDER OF VALUES. It is supplied in
     * ascending order (because this is the logical way to think of things), but
     * is reversed in the setter.
     */
    @XmlElement(name = "ThresholdValue", required = true)
    private List<ColourAndValue> colours;
    
    /*
     * This gets called after being unmarshalled from XML. This initialises the
     * colours correctly
     */
    void afterUnmarshal( Unmarshaller u, Object parent ) {
        initialiseColours();
    }

    ThresholdColourScheme() {}

    public ThresholdColourScheme(Color minColour, List<ColourAndValue> colours, Color noDataColour) {
        super();
        this.minColour = minColour;
        this.noDataColour = noDataColour;
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
        for(ColourAndValue band : colours) {
            if(value.floatValue() > band.value) {
                return band.colour;
            }
        }
        return minColour;
    }
    
    private void initialiseColours() {
        if(colours == null || colours.size() < 1) {
            throw new IllegalArgumentException("Threshold values must not be null and must have at least one value");
        }
        /*
         * Check that it's correctly ordered.  Then reverse it. 
         */
        Float value = -Float.MAX_VALUE;
        for(ColourAndValue band : colours) {
            if(band.value < value) {
                throw new IllegalArgumentException(
                        "Threshold bands must be in ascending order of value");
            }
            value = band.value;
        }
        Collections.reverse(colours);
    }

    @Override
    public Float getScaleMin() {
        return colours.get(0).value;
    }

    @Override
    public Float getScaleMax() {
        return colours.get(colours.size() - 1).value;
    }
}
