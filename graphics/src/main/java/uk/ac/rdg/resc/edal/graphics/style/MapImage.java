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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.graphics.style.util.FeatureCatalogue;
import uk.ac.rdg.resc.edal.graphics.style.util.LegendDataGenerator;
import uk.ac.rdg.resc.edal.util.PlottingDomainParams;

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
        return getLegend(componentSize, componentSize, Color.black, Color.white, true);
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
        return getLegend(componentWidth, componentHeight, Color.black, Color.white, true);
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
     * @return An {@link BufferedImage} representing the legend for this
     *         {@link MapImage}
     */
    public BufferedImage getLegend(int componentWidth, int componentHeight, Color textColour,
            Color bgColour, boolean layerNameLabels) throws EdalException {
        return getLegend(componentWidth, componentHeight, textColour, bgColour, layerNameLabels,
                true, 0.1f, 0.1f);
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
            Color bgColour, boolean layerNameLabels, boolean background,
            float extraAmountOutOfRange, float fontProportion) throws EdalException {
        return getLegend(componentWidth, componentHeight, textColour, bgColour, layerNameLabels,
                background, extraAmountOutOfRange, extraAmountOutOfRange, fontProportion);
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
            Color bgColour, boolean layerNameLabels, boolean background,
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
        } else if (noOfIndependentFields == 1) {
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
                    graphics.drawRect(xStart - 2, yStart - 2, borderSize + colourbar2d.getWidth()
                            - 2, borderSize + colourbar2d.getHeight() - 2);
                    graphics.drawRect(xStart - 1, yStart - 1, borderSize + colourbar2d.getWidth()
                            - 2, borderSize + colourbar2d.getHeight() - 2);
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
                            extraAmountOutOfRangeHigh, componentHeight, textColour,
                            layerNameLabels, fontSize);

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

        int textBorder = 4;

        /*
         * Find the values to use for the labels and the minimum difference
         * between adjacent values. The latter and the maximum value are used to
         * calculate the number of significant figures required.
         */
        Float lowVal = nameAndRange.getScaleRange().getLow();
        Float highVal = nameAndRange.getScaleRange().getHigh();
        float vals[] = new float[4];
        for (int i = 0; i < 4; i++) {
            vals[i] = lowVal + (float) i * (highVal - lowVal) / 3.0F;
        }
        float minDiff = Float.POSITIVE_INFINITY;
        for (int i = 1; i < 4; i++) {
            float diff = Math.abs(vals[i] - vals[i - 1]);
            if (diff < minDiff) {
                minDiff = diff;
            }
        }
        // Find the order of magnitude of the minimum difference between adjacent values 
        int oMinDiff = (int) Math.floor(Math.log10(Math.abs(minDiff)));
        // Find the order of magnitude of the maximum value
        int oHighVal = (int) Math.floor(Math.log10(Math.abs(highVal)));
        // Find the number of significant figures required to display the smallest difference 
        int sigfigs = oHighVal - oMinDiff + 1;
        // Convert values to BigDecimals with correct number of significant figures
        BigDecimal[] bds = new BigDecimal[4];
        for (int i = 0; i < 4; i++) {
            bds[i] = new BigDecimal(vals[i], new java.math.MathContext(sigfigs + 1));
        }

        String lowStr = String.valueOf(bds[0].doubleValue());
        String medLowStr = String.valueOf(bds[1].doubleValue());
        String medHighStr = String.valueOf(bds[2].doubleValue());
        String highStr = String.valueOf(bds[3].doubleValue());

        /*
         * Create a temporary image so that we can get some metrics about the
         * font. We can use these to determine the size of the final image.
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
        textFont = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize - 1);

        /*
         * Rotate the font for the labels
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
        int outOfRangeLowOffset = (int) ((componentSize * extraAmountOutOfRangeLow) / (1 + extraAmountOutOfRangeLow + extraAmountOutOfRangeHigh));
        int outOfRangeHighOffset = (int) ((componentSize * extraAmountOutOfRangeHigh) / (1 + extraAmountOutOfRangeLow + extraAmountOutOfRangeHigh));

        int lowYPos = componentSize - outOfRangeLowOffset + textHeightOffset;
        int highYPos = outOfRangeHighOffset + textHeightOffset;
        int medLowYPos = (int) (highYPos + 2.0 * (lowYPos - highYPos) / 3.0);
        int medHighYPos = (int) (highYPos + 1.0 * (lowYPos - highYPos) / 3.0);
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
                    newFieldName.append(fieldName.substring(i * charsPerLine, (i + 1)
                            * charsPerLine)
                            + "-\n-");
                }
            }
            fieldName = newFieldName.toString();
        }

        /*
         * Space needed for labels
         */
        int numberSpace = fontMetrics.stringWidth(lowStr);
        if (fontMetrics.stringWidth(medLowStr) > numberSpace) {
            numberSpace = fontMetrics.stringWidth(medLowStr);
        }
        if (fontMetrics.stringWidth(medHighStr) > numberSpace) {
            numberSpace = fontMetrics.stringWidth(medHighStr);
        }
        if (fontMetrics.stringWidth(highStr) > numberSpace) {
            numberSpace = fontMetrics.stringWidth(highStr);
        }
        /*
         * Total space needed for all text
         */
        int sideSpace = numberSpace + lineHeight * nLines + 2 * textBorder;
        // Dispose of the unused graphics context.
        graphics.dispose();

        BufferedImage ret = new BufferedImage(sideSpace, componentSize, BufferedImage.TYPE_INT_ARGB);
        graphics = ret.createGraphics();
        /*
         * Now draw text for the scale limits
         */
        graphics.setColor(textColor);
        graphics.setFont(textFont);
        graphics.drawString(highStr, textBorder, highYPos);
        graphics.drawString(medHighStr, textBorder, medHighYPos);
        graphics.drawString(medLowStr, textBorder, medLowYPos);
        graphics.drawString(lowStr, textBorder, lowYPos);

        graphics.setFont(sidewaysFont);

        int offset = 0;
        if (layerNameLabels) {
            for (String line : fieldName.split("\n")) {
                graphics.drawString(line, textBorder + numberSpace + lineHeight + offset,
                        componentSize - textBorder);
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
