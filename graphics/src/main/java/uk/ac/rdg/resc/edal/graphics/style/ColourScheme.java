package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;
import java.awt.image.IndexColorModel;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.graphics.ColorPalette;
import uk.ac.rdg.resc.edal.util.Extents;

public abstract class ColourScheme {
	// The scale range spanned by this colour scheme
	protected Extent<Float> scaleRange = Extents.emptyExtent(Float.class);
	// The colour to plot for values below the minimum. If null, then use the
	// lowest value in the palette
	protected Color belowMinColour = null;
	// The colour to plot for values above the maximum. If null, then use the
	// highest value in the palette
	protected Color aboveMaxColour = null;
	// The colour to plot for missing data
	protected Color noDataColour = Color.black;
	// The name of the palette to use.
	protected String paletteName = "null";
	// The opacity of the color palette
	protected int opacity = 100;
	// The number of color bands to use
	protected int numColourBands = 254;
	// Whether or not the scale is logarithmic
	protected boolean logarithmic = false;
	// The palette to use
	protected ColorPalette palette = ColorPalette.get(null);
	// The colour model for the specified palette
	protected IndexColorModel indexColorModel = null;

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
