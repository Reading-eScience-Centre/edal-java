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

package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.graphics.utils.FeatureCatalogue;
import uk.ac.rdg.resc.edal.graphics.utils.LegendDataGenerator;
import uk.ac.rdg.resc.edal.graphics.utils.PlottingDomainParams;

public class MapImage extends Drawable {
    private List<Drawable> layers = new ArrayList<Drawable>();

    public List<Drawable> getLayers() {
        return layers;
    }

    @Override
    public BufferedImage drawImage(PlottingDomainParams params, FeatureCatalogue catalogue)
            throws EdalException {
        BufferedImage finalImage = new BufferedImage(params.getWidth(), params.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = finalImage.createGraphics();

        for (Drawable drawable : layers) {
            if (drawable != null) {
                BufferedImage drawnImage = drawable.drawImage(params, catalogue);
                OpacityTransform opacityTransform = drawable.getOpacityTransform();
                if (opacityTransform != null) {
                    opacityTransform.drawIntoImage(drawnImage, params, catalogue);
                }
                graphics.drawImage(drawnImage, 0, 0, null);
            }
        }
        if (getOpacityTransform() != null) {
            getOpacityTransform().drawIntoImage(finalImage, params, catalogue);
        }
        return finalImage;
    }

    /**
     * Generate a legend for this {@link MapImage}.
     * 
     * @param componentSize
     *            A single integer specifying the size of each component of the
     *            legend. The final image size will depend upon this number as
     *            well as the number of unique data fields which this
     *            {@link MapImage} depends upon
     * @return An {@link BufferedImage} representing the legend for this
     *         {@link MapImage}
     */
    public BufferedImage getLegend(int componentSize) throws EdalException {
        return getLegend(componentSize, componentSize, Color.black, Color.white, true, false);
    }

    /**
     * Generate a legend for this {@link MapImage}.
     * 
     * @param componentWidth
     *            A single integer specifying the width of each component of the
     *            legend. The final image size will depend upon this number as
     *            well as the number of unique data fields which this
     *            {@link MapImage} depends upon
     * @param componentHeight
     *            A single integer specifying the height of each component of
     *            the legend. The final image size will depend upon this number
     *            as well as the number of unique data fields which this
     *            {@link MapImage} depends upon
     * @return An {@link BufferedImage} representing the legend for this
     *         {@link MapImage}
     */
    public BufferedImage getLegend(int componentWidth, int componentHeight) throws EdalException {
        return getLegend(componentWidth, componentHeight, Color.black, Color.white, true, false);
    }

    /**
     * Generate a legend for this {@link MapImage}.
     * 
     * @param componentWidth
     *            A single integer specifying the width of each component of the
     *            legend. The final image size will depend upon this number as
     *            well as the number of unique data fields which this
     *            {@link MapImage} depends upon
     * @param componentHeight
     *            A single integer specifying the height of each component of
     *            the legend. The final image size will depend upon this number
     *            as well as the number of unique data fields which this
     *            {@link MapImage} depends upon
     * @param force1D
     *            Force this legend to only use the first field.
     * @return An {@link BufferedImage} representing the legend for this
     *         {@link MapImage}
     */
    public BufferedImage getLegend(int componentWidth, int componentHeight, boolean force1D)
            throws EdalException {
        return getLegend(componentWidth, componentHeight, Color.black, Color.white, true, force1D);
    }

    /**
     * Generate a legend for this {@link MapImage}.
     * 
     * @param componentWidth
     *            A single integer specifying the width of each component of the
     *            legend. The final image size will depend upon this number as
     *            well as the number of unique data fields which this
     *            {@link MapImage} depends upon
     * @param componentHeight
     *            A single integer specifying the height of each component of
     *            the legend. The final image size will depend upon this number
     *            as well as the number of unique data fields which this
     *            {@link MapImage} depends upon
     * @param textColour
     *            The {@link Color} of the text
     * @param bgColour
     *            The {@link Color} of the background
     * @param layerNameLabels
     *            Whether or not to plot the ID of the layers on the legend
     * @param force1D
     *            Force this legend to only use the first field.
     * @return An {@link BufferedImage} representing the legend for this
     *         {@link MapImage}
     */
    public BufferedImage getLegend(int componentWidth, int componentHeight, Color textColour,
            Color bgColour, boolean layerNameLabels, boolean force1D) throws EdalException {
        return getLegend(componentWidth, componentHeight, textColour, bgColour, layerNameLabels,
                true, force1D, 0.1f, 0.05f);
    }

    /**
     * Generate a legend for this {@link MapImage}.
     * 
     * @param componentWidth
     *            A single integer specifying the width of each component of the
     *            legend. The final image size will depend upon this number as
     *            well as the number of unique data fields which this
     *            {@link MapImage} depends upon
     * @param componentHeight
     *            A single integer specifying the height of each component of
     *            the legend. The final image size will depend upon this number
     *            as well as the number of unique data fields which this
     *            {@link MapImage} depends upon
     * @param textColour
     *            The {@link Color} of the text
     * @param bgColour
     *            The {@link Color} of the background
     * @param layerNameLabels
     *            Whether or not to plot the ID of the layers on the legend
     * @param width1d
     *            The width of a 1D colourbar
     * @param background
     *            Whether to draw a background map image for 2D legends
     * @param force1D
     *            Force this legend to only use the first field.
     * @param extraAmountOutOfRange
     *            This is the fraction of the colourbar which *gets added* as
     *            out-of-range data.
     * 
     *            i.e. if it's 1, the result would be 1/3 below min, 1/3 in
     *            range, 1/3 above max.
     * @param fontProportion
     *            The proportion of the largest dimension of the main component
     *            which the font height should take up
     * @return An {@link BufferedImage} representing the legend for this
     *         {@link MapImage}
     */
    public BufferedImage getLegend(int componentWidth, int componentHeight, Color textColour,
            Color bgColour, boolean layerNameLabels, boolean background, boolean force1D,
            float extraAmountOutOfRange, float fontProportion) throws EdalException {
        return getLegend(componentWidth, componentHeight, textColour, bgColour, layerNameLabels,
                background, force1D, extraAmountOutOfRange, extraAmountOutOfRange, fontProportion);
    }

    /**
     * Generate a legend for this {@link MapImage}.
     * 
     * @param componentWidth
     *            A single integer specifying the width of each component of the
     *            legend. The final image size will depend upon this number as
     *            well as the number of unique data fields which this
     *            {@link MapImage} depends upon
     * @param componentHeight
     *            A single integer specifying the height of each component of
     *            the legend. The final image size will depend upon this number
     *            as well as the number of unique data fields which this
     *            {@link MapImage} depends upon
     * @param textColour
     *            The {@link Color} of the text
     * @param bgColour
     *            The {@link Color} of the background
     * @param layerNameLabels
     *            Whether or not to plot the ID of the layers on the legend
     * @param width1d
     *            The width of a 1D colourbar
     * @param background
     *            Whether to draw a background map image for 2D legends
     * @param force1D
     *            Force this legend to only use the first field.
     * @param extraAmountOutOfRangeLow
     *            This is the fraction of the colourbar which *gets added* as
     *            out-of-range data below the minimum
     * 
     *            i.e. if it's 1, the result would be 1/3 below min, 1/3 in
     *            range, 1/3 above max.
     * @param extraAmountOutOfRangeHigh
     *            This is the fraction of the colourbar which *gets added* as
     *            out-of-range data above the maximum
     * 
     *            i.e. if it's 1, the result would be 1/3 below min, 1/3 in
     *            range, 1/3 above max.
     * @param fontProportion
     *            The proportion of the largest dimension of the main component
     *            which the font height should take up
     * @return An {@link BufferedImage} representing the legend for this
     *         {@link MapImage}
     */
    public BufferedImage getLegend(int componentWidth, int componentHeight, Color textColour,
            Color bgColour, boolean layerNameLabels, boolean background, boolean force1D,
            float extraAmountOutOfRangeLow, float extraAmountOutOfRangeHigh, float fontProportion)
            throws EdalException {
        BufferedImage finalImage;

        Set<NameAndRange> fieldsWithScales = getFieldsWithScales();
        int noOfIndependentFields = fieldsWithScales.size();

        /*
         * The desired height of the font in pixels
         */
        int fontSize = (int) (fontProportion * Math.max(componentWidth, componentHeight));

        if (noOfIndependentFields == 0) {
            /*
             * Return an empty image - we have no data fields
             */
            return null;
        } else if (noOfIndependentFields == 1 || force1D) {
            /*
             * Case where we have a 1D colour bar
             */

            /*
             * Get the field name and scale range.
             */
            NameAndRange nameAndRange = fieldsWithScales.iterator().next();

            /*
             * Get the data for the colourbar and draw it.
             */
            LegendDataGenerator dataGenerator = new LegendDataGenerator(componentWidth,
                    componentHeight, null, extraAmountOutOfRangeLow, extraAmountOutOfRangeHigh,
                    extraAmountOutOfRangeLow, extraAmountOutOfRangeHigh);

            BufferedImage colourbar = drawImage(dataGenerator.getPlottingDomainParams(),
                    dataGenerator.getFeatureCatalogue(null, nameAndRange));
            Graphics2D graphics = colourbar.createGraphics();
            graphics.setColor(textColour);
            graphics.drawRect(0, 0, colourbar.getWidth() - 1, colourbar.getHeight() - 1);
            graphics.dispose();

            /*
             * Now generate the labels for this legend
             */
            BufferedImage labels = getLegendLabels(nameAndRange, extraAmountOutOfRangeLow,
                    extraAmountOutOfRangeHigh, componentHeight, textColour, layerNameLabels,
                    fontSize);

            /*
             * Now create the correctly-sized final image...
             */
            finalImage = new BufferedImage(componentWidth + labels.getWidth(), componentHeight,
                    BufferedImage.TYPE_INT_ARGB);
            /*
             * ...and draw everything into it
             */
            graphics = finalImage.createGraphics();
            graphics.setColor(bgColour);
            graphics.fill(new Rectangle(finalImage.getWidth(), finalImage.getHeight()));
            graphics.drawImage(colourbar, 0, 0, null);
            graphics.drawImage(labels, componentWidth, 0, null);
        } else {
            /*
             * General case, where we need to generate each possible combination
             * of 2D legends.
             */
            int numberOfImagesInOneDirection = noOfIndependentFields - 1;
            List<NameAndRange> fields = new ArrayList<Drawable.NameAndRange>(fieldsWithScales);

            /*
             * Before we can start this process, we need to calculate how much
             * room to leave for the labels. We can do this by generating all
             * the labels up front
             */
            int borderSize = 0;
            for (int i = 0; i < fields.size(); i++) {
                BufferedImage label = getLegendLabels(fields.get(i), extraAmountOutOfRangeLow,
                        extraAmountOutOfRangeHigh, componentWidth, textColour, layerNameLabels,
                        fontSize);
                if (label.getWidth() > borderSize) {
                    borderSize = label.getWidth() + 8;
                }
            }

            int totalImageWidth = (componentWidth + borderSize) * numberOfImagesInOneDirection;
            int totalImageHeight = (componentHeight + borderSize) * numberOfImagesInOneDirection;

            finalImage = new BufferedImage(totalImageWidth, totalImageHeight,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = finalImage.createGraphics();
            graphics.setColor(bgColour);
            graphics.fillRect(0, 0, totalImageWidth, totalImageHeight);

            /*
             * Load the background image and the data mask
             */
            BufferedImage bg = null;
            BufferedImage bgMask = null;
            if (background) {
                try {
                    bg = ImageIO.read(MapImage.class.getResource("/img/map_bg_200.png"));
                    bgMask = ImageIO.read(MapImage.class.getResource("/img/map_bg_200_mask.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            for (int i = 0; i < fields.size(); i++) {
                int yStart = 2 + (i * (componentHeight + borderSize));
                for (int j = i + 1; j < fields.size(); j++) {
                    int xStart = 2 + ((j - i - 1) * (componentWidth + borderSize));
                    LegendDataGenerator dataGenerator = new LegendDataGenerator(componentWidth,
                            componentHeight, bgMask, extraAmountOutOfRangeLow,
                            extraAmountOutOfRangeHigh, extraAmountOutOfRangeLow,
                            extraAmountOutOfRangeHigh);
                    BufferedImage colourbar2d = drawImage(dataGenerator.getPlottingDomainParams(),
                            dataGenerator.getFeatureCatalogue(fields.get(j), fields.get(i)));
                    if (bg != null) {
                        graphics.drawImage(bg, xStart, yStart, componentWidth, componentHeight,
                                null);
                    }
                    /*
                     * Draw the 2d legend and outline it
                     */
                    graphics.drawImage(colourbar2d, xStart, yStart, null);
                    graphics.setColor(textColour);
                    graphics.drawRect(xStart, yStart, colourbar2d.getWidth() - 1,
                            colourbar2d.getHeight() - 1);
                    graphics.drawRect(xStart - 2, yStart - 2,
                            borderSize + colourbar2d.getWidth() - 2,
                            borderSize + colourbar2d.getHeight() - 2);
                    graphics.drawRect(xStart - 1, yStart - 1,
                            borderSize + colourbar2d.getWidth() - 2,
                            borderSize + colourbar2d.getHeight() - 2);
                    /*
                     * Now draw the labels
                     */
                    AffineTransform at = new AffineTransform();
                    at.translate(xStart + componentWidth, yStart + componentHeight);
                    at.rotate(Math.PI / 2);

                    BufferedImage xLabel = getLegendLabels(fields.get(j), extraAmountOutOfRangeLow,
                            extraAmountOutOfRangeHigh, componentWidth, textColour, layerNameLabels,
                            fontSize);
                    BufferedImage yLabel = getLegendLabels(fields.get(i), extraAmountOutOfRangeLow,
                            extraAmountOutOfRangeHigh, componentHeight, textColour, layerNameLabels,
                            fontSize);

                    graphics.drawImage(xLabel, at, null);
                    graphics.drawImage(yLabel, xStart + componentWidth, yStart, null);
                }
            }
        }
        return finalImage;
    }

    /**
     * This returns an image suitable for plotting next to a vertical colourbar.
     * Rotate it if required.
     * 
     * @param nameAndRange
     * @param extraAmountOutOfRangeLow
     * @param extraAmountOutOfRangeHigh
     * @param componentSize
     * @param textColor
     * @param layerNameLabels
     * @return
     */
    public static BufferedImage getLegendLabels(NameAndRange nameAndRange,
            float extraAmountOutOfRangeLow, float extraAmountOutOfRangeHigh, int componentSize,
            Color textColor, boolean layerNameLabels, int fontHeight) {
        String fieldName = nameAndRange.getFieldLabel();

        List<Float> definedPoints = nameAndRange.getDefinedPoints();
        if (definedPoints != null) {
            /*
             * We are using a scale with defined points (e.g. a threshold colour
             * scheme). We want the labelled values to correspond with the
             * defined points.
             */
            return drawLegendLabels(definedPoints, fieldName, extraAmountOutOfRangeLow,
                    extraAmountOutOfRangeHigh, componentSize, textColor, layerNameLabels,
                    fontHeight);
        } else {
            /*
             * We have a general scale - generate 5 evenly spaced points
             */
            Float lowVal = nameAndRange.getScaleRange().getLow();
            Float highVal = nameAndRange.getScaleRange().getHigh();
            int n = 5;
            List<Float> vals = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                vals.add(lowVal + i * (highVal - lowVal) / (n - 1));
            }
            return drawLegendLabels(vals, fieldName, extraAmountOutOfRangeLow,
                    extraAmountOutOfRangeHigh, componentSize, textColor, layerNameLabels,
                    fontHeight);
        }

    }

    private final static int TEXT_BORDER = 4;

    private static BufferedImage drawLegendLabels(List<Float> values, String fieldName,
            float extraAmountOutOfRangeLow, float extraAmountOutOfRangeHigh, int componentSize,
            Color textColor, boolean layerNameLabels, int fontHeight) {
        Collections.sort(values);
        float[] vals = new float[values.size()];
        float minDiff = Float.MAX_VALUE;
        for (int i = 0; i < vals.length; i++) {
            if (values.get(i) == null) {
                throw new IllegalArgumentException(
                        "Cannot create a legend where one of the labelled values is null");
            }
            vals[i] = values.get(i);
            
            if(i > 0) {
                minDiff = Math.min(minDiff, vals[i] - vals[i-1]);
            }
        }

        /*
         * Find how many times the minimum difference between values divides into the maximum value.
         * This will give the number of significant figures we need to represent all values properly
         */
        float max = Math.max(Math.abs(vals[0]), Math.abs(vals[vals.length - 1]));
        int sigfigs = (int) Math.ceil(Math.log10(max/minDiff));

        /*
         * Generate the text strings for the labels
         */
        String[] strings = new String[vals.length];
        for (int i = 0; i < vals.length; i++) {
            BigDecimal val = new BigDecimal(vals[i], new java.math.MathContext(sigfigs + 1));
            strings[i] = String.valueOf(val.doubleValue());
        }

        /*
         * The following section calculates the required sizes to use, based on
         * the strings we need to render, the desired size of the resulting
         * image, etc.
         * 
         * First, create a temporary image so that we can get some metrics about
         * the font. We can use these to determine the size of the final image.
         */
        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D graphics = tempImage.createGraphics();

        /*
         * Calculate the font size which will fit the font into the given
         * height.
         * 
         * A minimum font size of 6 is set which is just about readable
         */
        int fontSize = 6;
        Font textFont;
        int height = 0;
        while (height < fontHeight) {
            textFont = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize++);
            height = graphics.getFontMetrics(textFont).getHeight();
        }
        /*
         * This is the font we actually want to use
         */
        textFont = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize - 1);

        /*
         * Rotate the font for the field name label
         */
        AffineTransform at = new AffineTransform();
        at.rotate(-Math.PI / 2.0);
        Font sidewaysFont = textFont.deriveFont(at);

        FontMetrics fontMetrics = graphics.getFontMetrics(textFont);
        /*
         * The height of a line of text
         */
        int lineHeight = fontMetrics.getHeight();
        /*
         * The offset needed to account for the fact that the position of text
         * refers to the position of the baseline, not the centre
         */
        int textHeightOffset = lineHeight / 3;

        /*
         * This is how much of an offset we need so that the high/low scale
         * labels are in the right place
         */
        int outOfRangeLowOffset = (int) ((componentSize * extraAmountOutOfRangeLow)
                / (1 + extraAmountOutOfRangeLow + extraAmountOutOfRangeHigh));
        int outOfRangeHighOffset = (int) ((componentSize * extraAmountOutOfRangeHigh)
                / (1 + extraAmountOutOfRangeLow + extraAmountOutOfRangeHigh));

        int fieldLength = 0;
        int nLines = 0;
        if (layerNameLabels) {
            /*
             * The length required to write the field name
             */
            fieldLength = fontMetrics.stringWidth(fieldName);
            /*
             * Number of lines of text needed for field name. The 20 is in there
             * to get around the fact that characters don't take up equal space.
             * It's an empirical value. Feel free to empiricise it more.
             */
            nLines = (int) Math.ceil((double) (fieldLength + 20) / componentSize);
        }
        if (nLines > 1) {
            /*
             * It needs splitting.
             */
            int charsPerLine = 1 + fieldName.length() / nLines;
            StringBuilder newFieldName = new StringBuilder();
            for (int i = 0; i < nLines; i++) {
                /*
                 * Hyphenate
                 */
                if (i == nLines - 1) {
                    newFieldName.append(fieldName.substring(i * charsPerLine));
                } else {
                    newFieldName.append(
                            fieldName.substring(i * charsPerLine, (i + 1) * charsPerLine) + "-\n-");
                }
            }
            fieldName = newFieldName.toString();
        }

        /*
         * Space needed for labels
         */
        int numberSpace = fontMetrics.stringWidth(strings[0]);
        for (int i = 1; i < strings.length; i++) {
            if (fontMetrics.stringWidth(strings[i]) > numberSpace) {
                numberSpace = fontMetrics.stringWidth(strings[i]);
            }
        }

        /*
         * Total space needed for all text
         */
        int sideSpace = numberSpace + lineHeight * nLines + 2 * TEXT_BORDER;
        /*
         * We have now calculated all of the required sizes, and are ready to
         * use this to calculate the position of the text and render it.
         * 
         * Dispose of the unused graphics context.
         */
        graphics.dispose();

        /*
         * Calculate y-positions of text labels
         */
        int[] tPosns = new int[vals.length];

        int lowPos = componentSize - outOfRangeLowOffset + textHeightOffset;
        int highPos = outOfRangeHighOffset + textHeightOffset;
        float minV = vals[0];
        float maxV = vals[vals.length - 1];
        for (int i = 0; i < vals.length; i++) {
            tPosns[i] = (int) (lowPos - ((vals[i] - minV) / (maxV - minV)) * (lowPos - highPos));
        }

        /*
         * All positions calculated, render the final image
         */
        BufferedImage ret = new BufferedImage(sideSpace, componentSize,
                BufferedImage.TYPE_INT_ARGB);
        graphics = ret.createGraphics();
        graphics.setColor(textColor);
        graphics.setFont(textFont);
        for (int i = 0; i < vals.length; i++) {
            graphics.drawString(strings[i], TEXT_BORDER, tPosns[i]);
        }

        /*
         * Use the sideways for for the field label
         */
        graphics.setFont(sidewaysFont);

        int offset = 0;
        if (layerNameLabels) {
            for (String line : fieldName.split("\n")) {
                graphics.drawString(line, TEXT_BORDER + numberSpace + lineHeight + offset,
                        componentSize - TEXT_BORDER);
                offset += lineHeight;
            }
        }

        return ret;
    }

    @Override
    public Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new LinkedHashSet<Drawable.NameAndRange>();
        for (Drawable drawable : layers) {
            if (drawable != null) {
                Set<NameAndRange> fieldsWithScales = drawable.getFieldsWithScales();
                if (drawable.getOpacityTransform() != null) {
                    fieldsWithScales.addAll(drawable.getOpacityTransform().getFieldsWithScales());
                }
                if (fieldsWithScales != null) {
                    ret.addAll(fieldsWithScales);
                }
            }
        }
        if (getOpacityTransform() != null) {
            ret.addAll(getOpacityTransform().getFieldsWithScales());
        }
        return ret;
    }
}
