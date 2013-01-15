package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;

import uk.ac.rdg.resc.edal.Extent;

public abstract class ColourScheme {
    // The scale range spanned by this colour scheme
    Extent<Float> scaleRange;
    // The colour to plot for values below the minimum. If null, then use the
    // lowest value in the palette
    Color belowMinColor;
    // The colour to plot for values above the maximum. If null, then use the
    // highest value in the palette
    Color aboveMaxColor;
    // The colour to plot for missing data
    Color noDataColor;
    // The palette to use.
    Palette palette;

    boolean log = false;

    // Number of values used to generate a colour in this colour scheme.
    // Protected so that subclasses can access it.
    protected final int numberOfValues;

    public ColourScheme(int n) {
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
