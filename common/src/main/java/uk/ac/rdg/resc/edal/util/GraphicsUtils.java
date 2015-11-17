/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc.edal.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalParseException;
import uk.ac.rdg.resc.edal.exceptions.VariableNotFoundException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.metadata.Parameter.Category;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;

/**
 * Class containing static utility methods for dealing with graphics
 * 
 * @author Guy Griffiths
 */
public class GraphicsUtils {
    private static final Logger log = LoggerFactory.getLogger(GraphicsUtils.class);
    private static Map<String, Color> namedColors = new HashMap<>();

    static {
        try {
            URL resource = GraphicsUtils.class.getResource("/colors.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                namedColors.put(
                        split[0],
                        new Color(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer
                                .parseInt(split[3])));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parses a string to obtain a {@link Color}.
     * 
     * @param colourString
     *            A string of one of the forms:
     * 
     *            <li>0xRRGGBB
     * 
     *            <li>0xAARRGGBB
     * 
     *            <li>#RRGGBB
     * 
     *            <li>#AARRGGBB
     * 
     *            <li>"transparent"
     * 
     *            <li>"extend"
     * 
     * @return A {@link Color} representing the string, or <code>null</code> if
     *         "extend" was supplied
     * @throws EdalParseException
     *             If the format of the string does not fall into one of the
     *             above categories
     */
    public static Color parseColour(String colourString) throws EdalParseException {
        if ("transparent".equalsIgnoreCase(colourString)) {
            return new Color(0, true);
        }
        if ("extend".equalsIgnoreCase(colourString)) {
            /*
             * In the context of palette out-of-range values, null represents
             * extending the colour. In other cases it doesn't represent a
             * value, so null is not a disaster.
             */
            return null;
        }
        if (namedColors.containsKey(colourString)) {
            return namedColors.get(colourString);
        }
        if (!colourString.toLowerCase().startsWith("0x") && !colourString.startsWith("#")) {
            throw new EdalParseException("Invalid format for colour: " + colourString);
        }
        if (colourString.length() == 7 || colourString.length() == 8) {
            /*
             * We have #RRGGBB or 0xRRGGBB. Color.decode can handle these
             */
            return Color.decode(colourString);
        } else if (colourString.length() == 9) {
            /*
             * We have #AARRGGBB
             */
            Color color = Color.decode("#" + colourString.substring(3));
            int alpha = Integer.parseInt(colourString.substring(1, 3), 16);
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        } else if (colourString.length() == 10) {
            /*
             * We have 0xAARRGGBB
             */
            Color color = Color.decode("0x" + colourString.substring(4));
            int alpha = Integer.parseInt(colourString.substring(2, 4), 16);
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        } else {
            throw new EdalParseException("Invalid format for colour");
        }
    }

    public static String colourToString(Color colour) {
        if (colour == null) {
            return "extend";
        } else if (colour.getAlpha() == 0) {
            return "transparent";
        }
        return String.format("#%08X", colour.getRGB());
    }

    /**
     * Estimate the range of values in this layer by reading a sample of data
     * from the default time and elevation.
     * 
     * If the given variable is found, a default range of 0-100 is returned
     * 
     * @param dataset
     *            The dataset containing the variable to estimate
     * @param varId
     *            The ID of the variable to estimate
     * @return An approximate value range
     */
    public static Extent<Float> estimateValueRange(Dataset dataset, String varId) {
        VariableMetadata variableMetadata;
        try {
            variableMetadata = dataset.getVariableMetadata(varId);
        } catch (VariableNotFoundException e1) {
            /*
             * Variable doesn't exist, any range is fine
             */
            return Extents.newExtent(0f, 100f);
        }
        if (!variableMetadata.isScalar()) {
            return Extents.newExtent(0f, 100f);
            /*
             * We have a non-scalar variable. We will attempt to use the first
             * child member to estimate the value range. This may not work in
             * which case we ignore it - worst case scenario is that we end up
             * with a bad scale range set - administrators can just override it.
             */
//            try {
//                variableMetadata = variableMetadata.getChildren().iterator().next();
//                varId = variableMetadata.getId();
//            } catch (Exception e) {
//                /*
//                 * Ignore this error and just generate a (probably) inaccurate
//                 * range
//                 */
//            }
        }

        Double zPos = null;
        Extent<Double> zExtent = null;
        /*
         * TODO SLOW! This will be problematic if we are using
         * AbstractContinuousDomainDatasets, because we will end up extracting
         * every single feature. On the plus side, the value range returned will
         * be absolutely accurate...
         */
        if (variableMetadata.getVerticalDomain() != null) {
            zPos = variableMetadata.getVerticalDomain().getExtent().getLow();
            zExtent = variableMetadata.getVerticalDomain().getExtent();
        }
        DateTime time = null;
        Extent<DateTime> tExtent = null;
        if (variableMetadata.getTemporalDomain() != null) {
            time = variableMetadata.getTemporalDomain().getExtent().getHigh();
            tExtent = variableMetadata.getTemporalDomain().getExtent();
        }
        PlottingDomainParams params = new PlottingDomainParams(100, 100, variableMetadata
                .getHorizontalDomain().getBoundingBox(), zExtent, tExtent, null, zPos, time);
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        try {
            Collection<? extends DiscreteFeature<?, ?>> mapFeatures = dataset.extractMapFeatures(
                    CollectionUtils.setOf(varId), params);
            for (DiscreteFeature<?, ?> feature : mapFeatures) {
                Array<Number> values = feature.getValues(varId);
                if (values != null) {
                    for (Number value : values) {
                        if (value != null && !Double.isNaN(value.doubleValue())) {
                            min = (float) Math.min(value.doubleValue(), min);
                            max = (float) Math.max(value.doubleValue(), max);
                        }
                    }
                }
            }
        } catch (DataReadingException | VariableNotFoundException e) {
            log.error(
                    "Problem reading data whilst estimating scale range.  A default value will be used.",
                    e);
        }

        if (max == -Float.MAX_VALUE || min == Float.MAX_VALUE) {
            /*
             * Defensive - either they are both equal to their start values, or
             * neither is.
             * 
             * Anyway, here we have no data, or can't read it. Pick a range.
             * I've chosen 0 to 100, but it really doesn't matter.
             */
            min = 0;
            max = 100;
        } else if (min == max) {
            /*
             * We've hit an area of uniform data. Make sure that max > min
             */
            max += 1.0f;
        } else {
            float diff = max - min;
            min -= 0.05 * diff;
            max += 0.05 * diff;
        }

        return Extents.newExtent((float) roundToSignificantFigures(min, 4),
                (float) roundToSignificantFigures(max, 4));
    }

    /**
     * Rounds a double to a number of significant figures
     * 
     * Taken from:
     * http://stackoverflow.com/questions/202302/rounding-to-an-arbitrary
     * -number-of-significant-digits
     * 
     * @param num
     *            The number to round
     * @param n
     *            The number of significant figures
     * @return The rounded number
     */
    public static double roundToSignificantFigures(double num, int n) {
        if (num == 0) {
            return 0;
        }

        final double d = Math.ceil(Math.log10(num < 0 ? -num : num));
        final int power = n - (int) d;

        final double magnitude = Math.pow(10, power);
        final long shifted = Math.round(num * magnitude);
        return shifted / magnitude;
    }

    /**
     * Gets a version of this palette with the given number of color bands,
     * either by subsampling or interpolating the existing palette
     * 
     * @param numColorBands
     *            The number of bands of colour to be used in the new palette
     * @return An array of Colors, with length numColorBands
     */
    public static Color[] generateColourSet(Color[] palette, int numColorBands) {
        Color[] targetPalette;
        if (numColorBands == palette.length) {
            /* We can just use the source palette directly */
            targetPalette = palette;
        } else {
            /* We need to create a new palette */
            targetPalette = new Color[numColorBands];
            /*
             * We fix the endpoints of the target palette to the endpoints of
             * the source palette
             */
            targetPalette[0] = palette[0];
            targetPalette[targetPalette.length - 1] = palette[palette.length - 1];

            if (targetPalette.length < palette.length) {
                /*
                 * We only need some of the colours from the source palette We
                 * search through the target palette and find the nearest
                 * colours in the source palette
                 */
                for (int i = 1; i < targetPalette.length - 1; i++) {
                    /*
                     * Find the nearest index in the source palette (Multiplying
                     * by 1.0f converts integers to floats)
                     */
                    int nearestIndex = Math.round(palette.length * i * 1.0f
                            / (targetPalette.length - 1));
                    targetPalette[i] = palette[nearestIndex];
                }
            } else {
                /*
                 * Transfer all the colours from the source palette into their
                 * corresponding positions in the target palette and use
                 * interpolation to find the remaining values
                 */
                int lastIndex = 0;
                for (int i = 1; i < palette.length - 1; i++) {
                    /* Find the nearest index in the target palette */
                    int nearestIndex = Math.round(targetPalette.length * i * 1.0f
                            / (palette.length - 1));
                    targetPalette[nearestIndex] = palette[i];
                    /* Now interpolate all the values we missed */
                    for (int j = lastIndex + 1; j < nearestIndex; j++) {
                        /*
                         * Work out how much we need from the previous colour
                         * and how much from the new colour
                         */
                        float fracFromThis = (1.0f * j - lastIndex) / (nearestIndex - lastIndex);
                        targetPalette[j] = interpolate(targetPalette[nearestIndex],
                                targetPalette[lastIndex], fracFromThis);

                    }
                    lastIndex = nearestIndex;
                }
                /* Now for the last bit of interpolation */
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

    public static class ColorAdapter extends XmlAdapter<String, Color> {
        private ColorAdapter() {
        }

        @Override
        public Color unmarshal(String s) {
            try {
                return GraphicsUtils.parseColour(s);
            } catch (EdalParseException e) {
                return null;
            }
        }

        @Override
        public String marshal(Color c) {
            return GraphicsUtils.colourToString(c);
        }

        private static ColorAdapter adapter = new ColorAdapter();

        public static ColorAdapter getInstance() {
            return adapter;
        }
    }

    /**
     * Renders a legend for categorical data
     * 
     * @param categories
     *            The categories to draw a legend for
     * @return The resulting BufferedImage
     */
    public static BufferedImage drawCategoricalLegend(Map<Integer, Category> categories) {
        /*
         * Make a very large canvas to draw all of the category labels onto.
         * This can then be trimmed later
         */
        int WIDTH = 1000;
        int HEIGHT = 10000;
        BufferedImage canvas = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        final int GAP = 5;
        final int SWATH_SIZE = 16;
        int yCoord = GAP;
        for (Category category : categories.values()) {
            g.setColor(category.getColour());
            g.fillRect(GAP, yCoord, SWATH_SIZE, SWATH_SIZE);
            g.setColor(Color.black);
            g.drawRect(GAP, yCoord, SWATH_SIZE, SWATH_SIZE);
            yCoord += SWATH_SIZE;
            g.drawString(category.getLabel(), GAP + SWATH_SIZE + GAP, yCoord - 3);
            yCoord += GAP;
        }
        int x = canvas.getWidth() - 1;
        for (; x >= 0; x--) {
            boolean hitStuff = false;
            for (int j = 0; j < yCoord; j++) {
                if (canvas.getRGB(x, j) != 0) {
                    hitStuff = true;
                }
            }
            if (hitStuff) {
                break;
            }
        }
        BufferedImage ret = new BufferedImage(x + GAP, yCoord, BufferedImage.TYPE_INT_ARGB);

        g = ret.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, ret.getWidth(), ret.getHeight());
        g.drawImage(canvas, 0, 0, null);
        return ret;
    }
}
