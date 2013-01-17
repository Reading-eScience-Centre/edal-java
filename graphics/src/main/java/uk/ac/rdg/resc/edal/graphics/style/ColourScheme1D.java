package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.graphics.ColorPalette;

public class ColourScheme1D extends ColourScheme {

	public ColourScheme1D(Extent<Float> scaleRange, Color belowMinColor,
			Color aboveMaxColor, Color noDataColor, String paletteName,
			int opacity, int numColourBands, boolean logarithmic) {
		super(1);

		// Set variables in constructor for now
		this.scaleRange = scaleRange;
		this.opacity = opacity;
		this.numColourBands = numColourBands;
		this.logarithmic = logarithmic;
		this.belowMinColour = belowMinColor;
		this.aboveMaxColour = aboveMaxColor;
		this.noDataColour = noDataColor;
		this.paletteName = paletteName;

		// Set the palette to that specified in paletteName
		palette = ColorPalette.get(paletteName);

		// Get the colour model
		indexColorModel = palette.getColorModel(numColourBands, opacity);

	}

	@Override
	public Color doGetColor(Number... values) {
		/*
		 * We can directly access values[0] since values is checked to be of
		 * size 1 in the superclass.
		 */
		if (values[0] == null || Float.isNaN(values[0].floatValue())) {
			return noDataColour; // if no data present return this color
		} else {
			float scaleMin = scaleRange.getLow().floatValue();
			float scaleMax = scaleRange.getHigh().floatValue();
			double min = logarithmic ? Math.log(scaleMin) : scaleMin;
			double max = logarithmic ? Math.log(scaleMax) : scaleMax;
			double value = logarithmic ? Math.log(values[0].doubleValue())
					: values[0].doubleValue();
			
			// Handle out of range pixels
			if (value < min) {
				if (belowMinColour == null) {
					value = min;
				} else {
					return belowMinColour;
				}
			} else if (value > max) {
				if (aboveMaxColour == null) {
					value = max;
				} else {
					return aboveMaxColour;
				}
			}

			double frac = (value - min) / (max - min);
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
			return new Color(indexColorModel.getRGB(index));
		}
	}
}
