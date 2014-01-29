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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.graphics.style.util.FeatureCatalogue;
import uk.ac.rdg.resc.edal.graphics.style.util.LegendDataGenerator;
import uk.ac.rdg.resc.edal.util.PlottingDomainParams;

@XmlType(namespace = MapImage.NAMESPACE, name = "ImageType")
@XmlRootElement(namespace = MapImage.NAMESPACE, name = "Image")
public class MapImage extends Drawable {
    /*
     * This is the namespace for the XML.
     * 
     * IF YOU CHANGE IT, YOU NEED TO MODIFY pom.xml AS WELL
     */
    public static final String NAMESPACE = "http://www.resc.reading.ac.uk";

    @XmlElements({
            @XmlElement(name = "Image", type = MapImage.class),
            @XmlElement(name = "ArrowLayer", type = ArrowLayer.class),
            @XmlElement(name = "RasterLayer", type = RasterLayer.class),
            @XmlElement(name = "Raster2DLayer", type = Raster2DLayer.class),
            @XmlElement(name = "StippleLayer", type = StippleLayer.class),
            @XmlElement(name = "ColouredGlyphLayer", type = ColouredGlyphLayer.class),
            @XmlElement(name = "ContourLayer", type = ContourLayer.class) })
    private List<Drawable> layers = new ArrayList<Drawable>();

    public List<Drawable> getLayers() {
        return layers;
    }

    @Override
    public BufferedImage drawImage(PlottingDomainParams params, FeatureCatalogue catalogue) throws EdalException {
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
    private static final int COLOURBAR_WIDTH = 50;

    public BufferedImage getLegend(int componentSize) throws EdalException {
        /*
         * TODO componentSize doesn't actually specify the total size of a
         * component, just the data bit of it. We *may* want to fix this
         */
        BufferedImage finalImage;
        Set<NameAndRange> fieldsWithScales = getFieldsWithScales();
        int noOfIndependentFields = fieldsWithScales.size();
        Color fgColour = Color.black;
        Color bgColour = Color.white;
        /*
         * This is the fraction of the colourbar which *gets added* as
         * out-of-range data.
         * 
         * i.e. if it's 1, the result would be 1/3 below min, 1/3 in range, 1/3
         * above max.
         */
        float extraAmountOutOfRange = 0.1f;

        if (noOfIndependentFields == 0) {
            /*
             * TODO Return an empty image - we have no data fields
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
            LegendDataGenerator dataGenerator = new LegendDataGenerator(fieldsWithScales,
                    COLOURBAR_WIDTH, componentSize, null, extraAmountOutOfRange);
            BufferedImage colourbar = drawImage(dataGenerator.getGlobalParams(),
                    dataGenerator.getFeatureCatalogue(null, nameAndRange.getFieldLabel()));
            Graphics2D graphics = colourbar.createGraphics();
            graphics.setColor(fgColour);
            graphics.drawRect(0, 0, colourbar.getWidth() - 1, colourbar.getHeight() - 1);
            graphics.dispose();

            /*
             * Now generate the labels for this legend
             */
            BufferedImage labels = getLabels(nameAndRange, extraAmountOutOfRange, componentSize,
                    fgColour);

            /*
             * Now create the correctly-sized final image...
             */
            finalImage = new BufferedImage(COLOURBAR_WIDTH + labels.getWidth(), componentSize,
                    BufferedImage.TYPE_INT_ARGB);
            /*
             * ...and draw everything into it
             */
            graphics = finalImage.createGraphics();
            graphics.setColor(bgColour);
            graphics.fill(new Rectangle(finalImage.getWidth(), finalImage.getHeight()));
            graphics.drawImage(colourbar, 0, 0, null);
            graphics.drawImage(labels, COLOURBAR_WIDTH, 0, null);
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
            BufferedImage[] labels = new BufferedImage[fields.size()];
            int borderSize = 0;
            for (int i = 0; i < fields.size(); i++) {
                labels[i] = getLabels(fields.get(i), extraAmountOutOfRange, componentSize, fgColour);
                if (labels[i].getWidth() > borderSize) {
                    borderSize = labels[i].getWidth() + 8;
                }
            }

            int totalImageSize = (componentSize + borderSize) * numberOfImagesInOneDirection;

            finalImage = new BufferedImage(totalImageSize, totalImageSize,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = finalImage.createGraphics();
            graphics.setColor(bgColour);
            graphics.fillRect(0, 0, totalImageSize, totalImageSize);

            /*
             * Load the background image and the data mask
             */
            BufferedImage bg = null;
            BufferedImage bgMask = null;
            try {
                bg = ImageIO.read(MapImage.class.getResource("/img/map_bg_200.png"));
                bgMask = ImageIO.read(MapImage.class.getResource("/img/map_bg_200_mask.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < fields.size(); i++) {
                String yName = fields.get(i).getFieldLabel();
                int yStart = 2 + (i * (componentSize + borderSize));
                for (int j = i + 1; j < fields.size(); j++) {
                    int xStart = 2 + ((j - i - 1) * (componentSize + borderSize));
                    String xName = fields.get(j).getFieldLabel();
                    LegendDataGenerator dataGenerator = new LegendDataGenerator(fieldsWithScales,
                            componentSize, componentSize, bgMask, 0.1f);
                    BufferedImage colourbar2d = drawImage(dataGenerator.getGlobalParams(),
                            dataGenerator.getFeatureCatalogue(xName, yName));
                    if (bg != null) {
                        graphics.drawImage(bg, xStart, yStart, componentSize, componentSize, null);
                    }
                    /*
                     * Draw the 2d legend and outline it
                     */
                    graphics.drawImage(colourbar2d, xStart, yStart, null);
                    graphics.setColor(fgColour);
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
                    at.translate(xStart + componentSize, yStart + componentSize);
                    at.rotate(Math.PI / 2);
                    graphics.drawImage(labels[j], at, null);
                    graphics.drawImage(labels[i], xStart + componentSize, yStart, null);
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
     * @param extraAmountOutOfRange
     * @param componentSize
     * @return
     */
    private static BufferedImage getLabels(NameAndRange nameAndRange, float extraAmountOutOfRange,
            int componentSize, Color textColor) {
        String fieldName = nameAndRange.getFieldLabel();

        Font textFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        AffineTransform at = new AffineTransform();
        at.rotate(-Math.PI / 2.0);
        Font sidewaysFont = textFont.deriveFont(at);
        DecimalFormat formatter = new DecimalFormat("0.###E0");

        int textBorder = 4;

        Float lowVal = nameAndRange.getScaleRange().getLow();
        Float highVal = nameAndRange.getScaleRange().getHigh();
        String lowStr = formatter.format(lowVal);
        String medLowStr = formatter.format(lowVal + (highVal - lowVal) / 3.0);
        String medHighStr = formatter.format(lowVal + 2.0 * (highVal - lowVal) / 3.0);
        String highStr = formatter.format(highVal);

        /*
         * Create a temporary image so that we can get some metrics about the
         * font. We can use these to determine the size of the final image.
         */
        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D graphics = tempImage.createGraphics();
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
        int outOfRangeOffset = (int) (componentSize * extraAmountOutOfRange / (1 + 2 * extraAmountOutOfRange));

        int lowYPos = componentSize - outOfRangeOffset + textHeightOffset;
        int highYPos = outOfRangeOffset + textHeightOffset;
        int medLowYPos = (int) (highYPos + 2.0 * (lowYPos - highYPos) / 3.0);
        int medHighYPos = (int) (highYPos + 1.0 * (lowYPos - highYPos) / 3.0);
        /*
         * The length required to write the field name
         */
        int fieldLength = fontMetrics.stringWidth(fieldName);
        /*
         * Number of lines of text needed for field name. The 20 is in there to
         * get around the fact that characters don't take up equal space. It's
         * an empirical value. Feel free to empiricise it more.
         */
        int nLines = (int) Math.ceil((double) (fieldLength + 20) / componentSize);
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
        for (String line : fieldName.split("\n")) {
            graphics.drawString(line, textBorder + numberSpace + lineHeight + offset, componentSize
                    - textBorder);
            offset += lineHeight;
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
