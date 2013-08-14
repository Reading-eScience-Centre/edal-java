package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.Color;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.rdg.resc.edal.graphics.style.StyleXMLParser.ColorAdapter;

@XmlType(namespace = Image.NAMESPACE, propOrder = {"thresholds", "colours", "noDataColour"}, name = "ThresholdColourSchemeType")
public class ThresholdColourScheme extends ColourScheme {

    /*
     * These hold lists of colours and the value which marks the lower boundary
     * of their threshold, IN REVERSE ORDER OF VALUES. It is supplied in
     * ascending order (because this is the logical way to think of things), but
     * is reversed in the setter.
     */
    @XmlElement(name = "Thresholds", required = true)
    private List<Float> thresholds;
    @XmlElement(name = "Colours", required = true)
    @XmlJavaTypeAdapter(ColorAdapter.class)
    private List<Color> colours;
    
    @XmlElement(name = "MissingDataColour")
    @XmlJavaTypeAdapter(ColorAdapter.class)
    private Color noDataColour = new Color(0f, 0f, 0f, 0f);
    
    /*
     * This gets called after being unmarshalled from XML. This initialises the
     * colours correctly
     */
    void afterUnmarshal( Unmarshaller u, Object parent ) {
        initialiseColours();
    }

    ThresholdColourScheme() {}

    public ThresholdColourScheme(List<Float> thresholds, List<Color> colours, Color noDataColour) {
        super();
        if (noDataColour != null) {
        	this.noDataColour = noDataColour;
        }
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
        return thresholds.get(0);
    }

    @Override
    public Float getScaleMax() {
        return thresholds.get(thresholds.size() - 1);
    }
}
