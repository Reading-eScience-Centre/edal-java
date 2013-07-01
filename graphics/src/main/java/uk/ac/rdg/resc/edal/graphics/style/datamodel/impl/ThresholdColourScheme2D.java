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

@XmlType(namespace = Image.NAMESPACE, propOrder = {"xThresholds", "yThresholds", "colours", "noDataColour"}, name = "ThresholdColourScheme2DType")
public class ThresholdColourScheme2D extends ColourScheme2D {

    @XmlElement(name = "MissingDataColour")
    @XmlJavaTypeAdapter(ColorAdapter.class)
    private Color noDataColour = new Color(0f, 0f, 0f, 0f);

    /*
     * Thresholds (their lower boundaries) and colours are supplied in ascending order,
     * because that is the logical way to think about things, but then reversed in order.
     */
    @XmlElement(name = "XThresholds", required = true)
    private List<Float> xThresholds;
    @XmlElement(name = "YThresholds", required = true)
    private List<Float> yThresholds;
    @XmlElement(name = "Colours", required = true)
    @XmlJavaTypeAdapter(ColorAdapter.class)
    private List<Color> colours;

    /*
     * This gets called after being unmarshalled from XML. This initialises the
     * colours correctly
     */
    void afterUnmarshal( Unmarshaller u, Object parent ) {
        initialiseColours();
    }

    ThresholdColourScheme2D() {}

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
				return xThresholds.get(0);
			case 2:
				return yThresholds.get(0);
			default:
				throw new IllegalArgumentException("Dimension must be either 1 or 2.");
		}
	}

	@Override
	public Float getScaleMax(int dimension) {
		switch (dimension) {
			case 1:
				return xThresholds.get(xThresholds.size() - 1);
			case 2:
				return yThresholds.get(yThresholds.size() - 1);
			default:
				throw new IllegalArgumentException("Dimension must be either 1 or 2.");
		}	
	}

}
