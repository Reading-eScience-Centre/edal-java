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

package uk.ac.rdg.resc.edal.graphics.style.util;

import java.awt.Color;
import java.util.Collection;

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
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.PlottingDomainParams;

/**
 * Class containing static utility methods for dealing with graphics
 * 
 * @author Guy Griffiths
 */
public class GraphicsUtils {
    private static final Logger log = LoggerFactory.getLogger(GraphicsUtils.class);

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
}
