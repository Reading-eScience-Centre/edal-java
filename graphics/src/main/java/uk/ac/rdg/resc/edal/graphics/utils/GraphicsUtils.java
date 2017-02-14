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

package uk.ac.rdg.resc.edal.graphics.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.dataset.AbstractContinuousDomainDataset;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DiscreteFeatureReader;
import uk.ac.rdg.resc.edal.dataset.HorizontallyDiscreteDataset;
import uk.ac.rdg.resc.edal.dataset.PointDataset;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.MapDomain;
import uk.ac.rdg.resc.edal.exceptions.EdalParseException;
import uk.ac.rdg.resc.edal.exceptions.VariableNotFoundException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.graphics.style.RasterLayer;
import uk.ac.rdg.resc.edal.graphics.style.ScaleRange;
import uk.ac.rdg.resc.edal.graphics.style.SegmentColourScheme;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.metadata.Parameter.Category;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * Class containing static utility methods for dealing with graphics
 * 
 * @author Guy Griffiths
 */
public class GraphicsUtils implements Serializable {
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
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Convenience method to extract map features for a single variable in a
     * generic dataset.
     * 
     * @param dataset
     *            The Dataset to extract features from
     * @param varId
     *            The variable ID to extract
     * @param params
     *            The {@link PlottingDomainParams} representing the domain to
     *            extract onto
     * @return A {@link Collection} of {@link DiscreteFeature}s
     */
    public static Collection<? extends DiscreteFeature<?, ?>> extractGeneralMapFeatures(
            Dataset dataset, String varId, PlottingDomainParams params) {
        Collection<? extends DiscreteFeature<?, ?>> mapFeatures = new ArrayList<>();
        if (dataset instanceof HorizontallyDiscreteDataset<?>) {
            HorizontallyDiscreteDataset<?> discreteDataset = (HorizontallyDiscreteDataset<?>) dataset;
            mapFeatures = discreteDataset.extractMapFeatures(
                    CollectionUtils.setOf(varId),
                    new MapDomain(new RegularGridImpl(params.getBbox(), params.getWidth(), params
                            .getHeight()), params.getTargetZ(), params.getTargetT()));
        } else if (dataset instanceof PointDataset<?>) {
            PointDataset<?> pointDataset = (PointDataset<?>) dataset;
            mapFeatures = pointDataset.extractMapFeatures(CollectionUtils.setOf(varId),
                    params.getBbox(), params.getZExtent(), params.getTExtent(),
                    params.getTargetZ(), params.getTargetT());
        }
        return mapFeatures;
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
     *            <li>A named colour. See
     *            {@link GraphicsUtils#getNamedColours()} for a full list
     * 
     * @return A {@link Color} representing the string, or <code>null</code> if
     *         "extend" was supplied
     * @throws EdalParseException
     *             If the format of the string does not fall into one of the
     *             above categories
     */
    public static Color parseColour(String colourString) throws EdalParseException {
        colourString = colourString.trim();
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
            throw new EdalParseException("Invalid format for colour: " + colourString);
        }
    }

    /**
     * @return The available named colours
     */
    public static Collection<String> getNamedColours() {
        return namedColors.keySet();
    }

    /**
     * Converts a {@link Color} to an HTML-like {@link String} (#AARRGGBB), with
     * additional cases for transparent / <code>null</code> values
     * 
     * @param colour
     *            The colour to represent as a {@link String}
     * @return The HTML representation of the {@link Color}, or
     *         <code>null</code> if a <code>null</code> {@link Color} was
     *         supplied.
     */
    public static String colourToString(Color colour) {
        if (colour == null) {
            return "extend";
        } else if (colour.getAlpha() == 0) {
            return "transparent";
        }
        return String.format("#%08X", colour.getRGB());
    }

    /**
     * Converts a {@link Color} to an HTML {@link String} (#RRGGBB), ignoring
     * transparency.
     * 
     * @param colour
     *            The colour to represent as a {@link String}
     * @return The HTML representation of the {@link Color}, or
     *         <code>null</code> if a <code>null</code> {@link Color} was
     *         supplied.
     */
    public static String colourToHtmlString(Color colour) {
        if (colour == null) {
            return null;
        }
        return String.format("#%06X", colour.getRGB() & 0x00FFFFFF);
    }

    /**
     * Estimate the range of values in this layer by reading a sample of data
     * from the default time and elevation.
     * 
     * If the given variable is not found, a default range of 0-100 is returned
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
            log.debug(varId + " not found in range estimation");
            return Extents.newExtent(0f, 100f);
        }
        if (!variableMetadata.isScalar()) {
            /*
             * We have a non-scalar variable. We will attempt to use the first
             * child member to estimate the value range. This may not work in
             * which case we ignore it - worst case scenario is that we end up
             * with a bad scale range set - administrators can just override it.
             */
            try {
                for (VariableMetadata child : variableMetadata.getChildren()) {
                    if (child.isScalar()) {
                        variableMetadata = child;
                        varId = variableMetadata.getId();
                        break;
                    }
                }
            } catch (Exception e) {
                /*
                 * Ignore this error and just generate a (probably) inaccurate
                 * range
                 */
                log.debug("Couldn't get scalar variable for " + varId + " when estimating range");
                return Extents.newExtent(0f, 100f);
            }
        }

        Double zPos = null;

        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        Collection<? extends DiscreteFeature<?, ?>> mapFeatures = null;
        if ((dataset instanceof HorizontallyDiscreteDataset<?>)) {
            /*
             * Extract map features at a low resolution over the entire domain
             * of the dataset. This will give a good approximation of the values
             * present (albeit at a specific time/depth)
             */
            HorizontallyDiscreteDataset<?> discreteDataset = (HorizontallyDiscreteDataset<?>) dataset;
            if (variableMetadata.getVerticalDomain() != null) {
                zPos = variableMetadata.getVerticalDomain().getExtent().getLow();
            }
            DateTime time = null;
            if (variableMetadata.getTemporalDomain() != null) {
                time = variableMetadata.getTemporalDomain().getExtent().getHigh();
            }
            try {
                long t1 = 0L, t2 = 0L;
                if (log.isDebugEnabled()) {
                    log.debug("Extracting data for range estimation");
                    t1 = System.currentTimeMillis();
                }
                VerticalCrs vCrs = null;
                if (variableMetadata.getVerticalDomain() != null) {
                    vCrs = variableMetadata.getVerticalDomain().getVerticalCrs();
                }
                mapFeatures = discreteDataset.extractMapFeatures(CollectionUtils.setOf(varId),
                        new MapDomain(new RegularGridImpl(variableMetadata.getHorizontalDomain()
                                .getBoundingBox(), 100, 100), zPos, vCrs, time));
                if (log.isDebugEnabled()) {
                    t2 = System.currentTimeMillis();
                    log.debug("Extracted data for range estimation: " + (t2 - t1) + "ms");
                }
            } catch (Exception e) {
                log.error(
                        "Problem reading data whilst estimating scale range for "+varId+".  A default value will be used.",
                        e);
            }
        } else if (dataset instanceof AbstractContinuousDomainDataset) {
            /*
             * We can have any number of features in a dataset with a continuous
             * domain. We can't just extract all features at low resolution
             * because that has no meaning. Instead, we extract a fixed number
             * of features. This is not so accurate since there is no guarantee
             * that the sample will be representative.
             * 
             * However, extracting ALL the features is not a feasible option,
             * and this is only an estimation anyway.
             */
            AbstractContinuousDomainDataset cdDataset = (AbstractContinuousDomainDataset) dataset;
            Set<String> featureIds = cdDataset.getFeatureIds();

            int maxFeatures = featureIds.size() > 100 ? 100 : featureIds.size();
            Set<String> featureIdsToRead = new HashSet<>();
            Iterator<String> iterator = featureIds.iterator();
            int i = 0;
            while (iterator.hasNext() && i < maxFeatures) {
                featureIdsToRead.add(iterator.next());
                i++;
            }
            DiscreteFeatureReader<? extends DiscreteFeature<?, ?>> featureReader = cdDataset
                    .getFeatureReader();
            mapFeatures = featureReader
                    .readFeatures(featureIdsToRead, CollectionUtils.setOf(varId));
        }
        if (mapFeatures != null) {
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
            min -= 0.5f;
            max += 0.5f;
        } else {
            float diff = max - min;
            min -= 0.05 * diff;
            max += 0.05 * diff;
        }

        log.debug("Estimated value range.  Returning");
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

    public static Map<Integer, Color> getColourMapForCategories(Map<Integer, Category> categories) {
        Map<Integer, Color> colours = new HashMap<>();
        Color[] fallback = generateColourSet(CATEGORICAL_COLOUR_SET, categories.size());
        int i = 0;
        for (Entry<Integer, Category> entry : categories.entrySet()) {
            if (entry.getValue().getColour() == null) {
                colours.put(entry.getKey(), fallback[i]);
            } else {
                colours.put(entry.getKey(), parseColour(entry.getValue().getColour()));
            }
            i++;
        }
        return colours;
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
        Map<Integer, Color> colours = getColourMapForCategories(categories);
        for (Integer categoryValue : categories.keySet()) {
            g.setColor(colours.get(categoryValue));
            g.fillRect(GAP, yCoord, SWATH_SIZE, SWATH_SIZE);
            g.setColor(Color.black);
            g.drawRect(GAP, yCoord, SWATH_SIZE, SWATH_SIZE);
            yCoord += SWATH_SIZE;
            g.drawString(categories.get(categoryValue).getLabel(), GAP + SWATH_SIZE + GAP,
                    yCoord - 3);
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

    /**
     * Draws a raster image from the supplied dataset and variable
     * 
     * @param dataset
     *            The dataset containing the data
     * @param varId
     *            The ID of the variable to plot
     * @param width
     *            The desired width of the output image
     * @param height
     *            The desired height of the output image
     * @return A {@link BufferedImage} containing the plot
     */
    public static BufferedImage plotDefaultImage(Dataset dataset, String varId, int width,
            int height) {
        /*
         * Estimate the value range
         */
        ScaleRange scaleRange = new ScaleRange(estimateValueRange(dataset, varId), false);

        /*
         * Use the default colour scheme, transparent background
         */
        ColourScheme colourScheme = new SegmentColourScheme(scaleRange, null, null, new Color(0,
                true), "default", 250);

        MapImage imageGenerator = new MapImage();
        RasterLayer rasterLayer = new RasterLayer(varId, colourScheme);
        imageGenerator.getLayers().add(rasterLayer);

        BoundingBox bbox = new BoundingBoxImpl(dataset.getVariableMetadata(varId)
                .getHorizontalDomain().getGeographicBoundingBox());

        PlottingDomainParams params = PlottingDomainParams.paramsForGriddedDataset(width, height,
                bbox, null, null);

        SimpleFeatureCatalogue<Dataset> featureCatalogue = new SimpleFeatureCatalogue<Dataset>(
                dataset, true);

        return imageGenerator.drawImage(params, featureCatalogue);
    }

    /**
     * A colour set for generating categorical palettes. This is a rainbow
     * colour set, so picking values as spread out as possible from this will
     * generate a reasonable categorical map.
     * 
     * It is preferable to specify the colours manually for categorical data,
     * but this is here for those occasions where that is not possible.
     */
    private static final Color[] CATEGORICAL_COLOUR_SET = new Color[] { new Color(140, 0, 0),
            new Color(158, 0, 0), new Color(175, 0, 0), new Color(193, 0, 0), new Color(211, 0, 0),
            new Color(228, 0, 0), new Color(246, 0, 0), new Color(255, 7, 0),
            new Color(255, 23, 0), new Color(255, 39, 0), new Color(255, 55, 0),
            new Color(255, 71, 0), new Color(255, 87, 0), new Color(255, 103, 0),
            new Color(255, 119, 0), new Color(255, 135, 0), new Color(255, 151, 0),
            new Color(255, 167, 0), new Color(255, 183, 0), new Color(255, 199, 0),
            new Color(255, 215, 0), new Color(255, 231, 0), new Color(255, 247, 0),
            new Color(247, 255, 7), new Color(231, 255, 23), new Color(215, 255, 39),
            new Color(199, 255, 55), new Color(183, 255, 71), new Color(167, 255, 87),
            new Color(151, 255, 103), new Color(135, 255, 119), new Color(119, 255, 135),
            new Color(103, 255, 151), new Color(87, 255, 167), new Color(71, 255, 183),
            new Color(55, 255, 199), new Color(39, 255, 215), new Color(23, 255, 231),
            new Color(7, 255, 247), new Color(0, 251, 255), new Color(0, 235, 255),
            new Color(0, 219, 255), new Color(0, 203, 255), new Color(0, 187, 255),
            new Color(0, 171, 255), new Color(0, 155, 255), new Color(0, 139, 255),
            new Color(0, 123, 255), new Color(0, 107, 255), new Color(0, 91, 255),
            new Color(0, 75, 255), new Color(0, 59, 255), new Color(0, 43, 255),
            new Color(0, 27, 255), new Color(0, 11, 255), new Color(0, 0, 255),
            new Color(0, 0, 239), new Color(0, 0, 223), new Color(0, 0, 207), new Color(0, 0, 191),
            new Color(0, 0, 175), new Color(0, 0, 159), new Color(0, 0, 143) };
}
