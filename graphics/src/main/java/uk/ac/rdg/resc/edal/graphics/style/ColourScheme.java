package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.rdg.resc.edal.graphics.style.StyleXMLParser.ColorAdapter;

@XmlSeeAlso({ ColourScheme1D.class })
public abstract class ColourScheme {
    // The scale range spanned by this colour scheme
    @XmlElement
    protected Float scaleMin = -50f;
    @XmlElement
    protected Float scaleMax = 50f;
    // The colour to plot for values below the minimum. If null, then use the
    // lowest value in the palette
    @XmlElement
    @XmlJavaTypeAdapter(ColorAdapter.class)
    protected Color belowMinColour = null;
    // The colour to plot for values above the maximum. If null, then use the
    // highest value in the palette
    @XmlElement
    @XmlJavaTypeAdapter(ColorAdapter.class)
    protected Color aboveMaxColour = null;
    // The colour to plot for missing data
    @XmlElement
    @XmlJavaTypeAdapter(ColorAdapter.class)
    protected Color noDataColour = new Color(0, 0, 0, 0);
    // The name of the palette to use.
    @XmlElement
    protected String paletteName = null;
    // The opacity of the color palette
    @XmlElement
    protected int opacity = 100;
    // The number of color bands to use
    @XmlElement
    protected int numColourBands = 254;
    // Whether or not the scale is logarithmic
    @XmlElement
    protected boolean logarithmic = false;

    // Number of values used to generate a colour in this colour scheme.
    // Protected so that subclasses can access it.
    private int numberOfValues;
    
    @SuppressWarnings("unused")
    private ColourScheme(){}

    protected ColourScheme(int n) {
        numberOfValues = n;
    }

    public Color getColor(Number... values) {
        if (values.length != numberOfValues) {
            throw new IllegalArgumentException("Wrong number of values");
        }
        return doGetColor(values);
    }

    /**
     * Get a colour from a varying number of scalar values. The size of values
     * is guaranteed to be equal to the protected variable numberOfValues
     * 
     * @param values
     *            The values which somehow combine to get a colour
     * @return The colour to plot
     */
    public abstract Color doGetColor(Number... values);
}
