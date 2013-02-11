package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;

public class Palette1D implements Palette {

    /**
     * The maximum number of colours a palette can support (254). (One would be
     * hard pushed to distinguish more colours than this in a typical scenario
     * anyway.)
     */
    public static final int MAX_NUM_COLOURS = 254;

    /**
     * The name of the default palette that will be used if the user doesn't
     * request a specific palette.
     * 
     * @see DEFAULT_PALETTE
     */
    public static final String DEFAULT_PALETTE_NAME = "rainbow";

    /**
     * This is the palette that will be used if no specific palette has been
     * chosen. This palette is taken from the SGT graphics toolkit.
     * 
     * @see DEFAULT_PALETTE_NAME
     */
    public static final Color[] DEFAULT_PALETTE = {
        new Color(0, 0, 143), new Color(0, 0, 159), new Color(0, 0, 175),
        new Color(0, 0, 191), new Color(0, 0, 207), new Color(0, 0, 223),
        new Color(0, 0, 239), new Color(0, 0, 255), new Color(0, 11, 255),
        new Color(0, 27, 255), new Color(0, 43, 255), new Color(0, 59, 255),
        new Color(0, 75, 255), new Color(0, 91, 255), new Color(0, 107, 255),
        new Color(0, 123, 255), new Color(0, 139, 255), new Color(0, 155, 255),
        new Color(0, 171, 255), new Color(0, 187, 255), new Color(0, 203, 255),
        new Color(0, 219, 255), new Color(0, 235, 255), new Color(0, 251, 255),
        new Color(7, 255, 247), new Color(23, 255, 231), new Color(39, 255, 215),
        new Color(55, 255, 199), new Color(71, 255, 183), new Color(87, 255, 167),
        new Color(103, 255, 151), new Color(119, 255, 135), new Color(135, 255, 119),
        new Color(151, 255, 103), new Color(167, 255, 87), new Color(183, 255, 71),
        new Color(199, 255, 55), new Color(215, 255, 39), new Color(231, 255, 23),
        new Color(247, 255, 7), new Color(255, 247, 0), new Color(255, 231, 0),
        new Color(255, 215, 0), new Color(255, 199, 0), new Color(255, 183, 0),
        new Color(255, 167, 0), new Color(255, 151, 0), new Color(255, 135, 0),
        new Color(255, 119, 0), new Color(255, 103, 0), new Color(255, 87, 0),
        new Color(255, 71, 0), new Color(255, 55, 0), new Color(255, 39, 0),
        new Color(255, 23, 0), new Color(255, 7, 0), new Color(246, 0, 0),
        new Color(228, 0, 0), new Color(211, 0, 0), new Color(193, 0, 0),
        new Color(175, 0, 0), new Color(158, 0, 0), new Color(140, 0, 0) };

    private final Color[] palette;
    private final String name;

    public Palette1D() {
    	this.name = DEFAULT_PALETTE_NAME;
    	this.palette = DEFAULT_PALETTE;
    }
    
    public Palette1D(int numColorBands) {
    	this.name = DEFAULT_PALETTE_NAME;
    	this.palette = getPalette(numColorBands, DEFAULT_PALETTE);
    }
    
    public Palette1D(String name, Color[] palette) {
        this.name = name;
        this.palette = palette;
    }

    public Palette1D(String name, int numColorBands, Color[] palette) {
        this.name = name;
        this.palette = getPalette(numColorBands, palette);
    }

    /**
     * Gets the number of colours in this palette
     * 
     * @return the number of colours in this palette
     */
    public int getSize() {
        return this.palette.length;
    }
 
    /**
     * Gets a version of this palette with the given number of color bands,
     * either by subsampling or interpolating the existing palette
     * 
     * @param numColorBands
     *            The number of bands of colour to be used in the new palette
     * @return An array of Colors, with length numColorBands
     * @throws IllegalArgumentException
     *             if the requested number of colour bands is less than one or
     *             greater than {@link #MAX_NUM_COLOURS}.
     */
    private Color[] getPalette(int numColorBands, Color[] palette) {
        if (numColorBands < 1 || numColorBands > MAX_NUM_COLOURS) {
            throw new IllegalArgumentException("numColorBands must be between 1 and "
                    + MAX_NUM_COLOURS);
        }
        Color[] targetPalette;
        if (numColorBands == palette.length) {
            // We can just use the source palette directly
            targetPalette = palette;
        } else {
            // We need to create a new palette
            targetPalette = new Color[numColorBands];
            // We fix the endpoints of the target palette to the endpoints of
            // the source palette
            targetPalette[0] = palette[0];
            targetPalette[targetPalette.length - 1] = palette[palette.length - 1];

            if (targetPalette.length < palette.length) {
                // We only need some of the colours from the source palette
                // We search through the target palette and find the nearest
                // colours
                // in the source palette
                for (int i = 1; i < targetPalette.length - 1; i++) {
                    // Find the nearest index in the source palette
                    // (Multiplying by 1.0f converts integers to floats)
                    int nearestIndex = Math.round(palette.length * i * 1.0f
                            / (targetPalette.length - 1));
                    targetPalette[i] = palette[nearestIndex];
                }
            } else {
                // Transfer all the colours from the source palette into their
                // corresponding
                // positions in the target palette and use interpolation to find
                // the remaining
                // values
                int lastIndex = 0;
                for (int i = 1; i < palette.length - 1; i++) {
                    // Find the nearest index in the target palette
                    int nearestIndex = Math.round(targetPalette.length * i * 1.0f
                            / (palette.length - 1));
                    targetPalette[nearestIndex] = palette[i];
                    // Now interpolate all the values we missed
                    for (int j = lastIndex + 1; j < nearestIndex; j++) {
                        // Work out how much we need from the previous colour
                        // and how much
                        // from the new colour
                        float fracFromThis = (1.0f * j - lastIndex) / (nearestIndex - lastIndex);
                        targetPalette[j] = interpolate(targetPalette[nearestIndex],
                                targetPalette[lastIndex], fracFromThis);
                    }
                    lastIndex = nearestIndex;
                }
                // Now for the last bit of interpolation
                for (int j = lastIndex + 1; j < targetPalette.length - 1; j++) {
                    float fracFromThis = (1.0f * j - lastIndex)
                            / (targetPalette.length - lastIndex);
                    targetPalette[j] = interpolate(targetPalette[targetPalette.length - 1],
                            targetPalette[lastIndex], fracFromThis);
                }
            }
        }
        return targetPalette;
    }

    /**
     * Linearly interpolates between two RGB colours
     * 
     * @param c1
     *            the first colour
     * @param c2
     *            the second colour
     * @param fracFromC1
     *            the fraction of the final colour that will come from c1
     * @return the interpolated Color
     */
    private static Color interpolate(Color c1, Color c2, float fracFromC1) {
        float fracFromC2 = 1.0f - fracFromC1;
        return new Color(Math.round(fracFromC1 * c1.getRed() + fracFromC2 * c2.getRed()),
                Math.round(fracFromC1 * c1.getGreen() + fracFromC2 * c2.getGreen()),
                Math.round(fracFromC1 * c1.getBlue() + fracFromC2 * c2.getBlue()),
                Math.round(fracFromC1 * c1.getAlpha() + fracFromC2 * c2.getAlpha()));
    }

    @Override
	public Color getColor(float value) {
        if (value < 0.0f || value > 1.0f) {
            throw new IllegalArgumentException("value must be between 0 and 1");
        }
        // find the nearest colour in the palette
        int i = (int) (value * this.palette.length);
        // correct in the special case that value = 1 to keep within bounds of array
        if (i == this.palette.length) i--;
		return this.palette[i];
	}

    public static Palette1D fromString(String paletteString) {
        // TODO Auto-generated method stub
        return null;
    }

}
