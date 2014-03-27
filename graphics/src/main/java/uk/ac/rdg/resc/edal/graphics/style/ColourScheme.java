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
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import uk.ac.rdg.resc.edal.graphics.style.Drawable.NameAndRange;
import uk.ac.rdg.resc.edal.util.Extents;

public abstract class ColourScheme {
    /**
     * Gets a scale bar for this {@link ColourScheme}
     * 
     * @param width
     *            The desired width of the scale bar
     * @param height
     *            The desired height of the scale bar
     * @param fracOutOfRange
     *            The amount of out of range to show as a fraction of the
     *            coloured part
     * @param vertical
     *            Whether or not the scale bar should be vertical
     * @param labels
     *            Whether to show numerical labels
     * @param textColor
     *            The colour of the text labels
     * @param bgColor
     *            The background colour for the text labels
     * @return The scale bar image
     */
    public BufferedImage getScaleBar(int width, int height, float fracOutOfRange, boolean vertical,
            boolean labels, Color textColor, Color bgColor) {
        int componentSize = vertical ? height : width;
        BufferedImage legendLabels = null;
        if (labels) {
            legendLabels = MapImage.getLegendLabels(
                    new NameAndRange("", Extents.newExtent(getScaleMin(), getScaleMax())),
                    fracOutOfRange, componentSize, textColor, false);
        }
        BufferedImage scaleBar = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        float range = getScaleMax() - getScaleMin();
        float newLow = getScaleMin() - range * fracOutOfRange;
        float newHigh = getScaleMax() + range * fracOutOfRange;
        range = newHigh - newLow;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                float frac;
                if (vertical) {
                    frac = ((float) j) / height;
                } else {
                    frac = ((float) i) / width;
                }

                frac = newLow + frac * range;

                scaleBar.setRGB(i, height - j - 1, getColor(frac).getRGB());
            }
        }

        BufferedImage finalImage;
        if (legendLabels == null) {
            return scaleBar;
        } else {
            if (vertical) {
                finalImage = new BufferedImage(width + legendLabels.getWidth(), height,
                        BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = finalImage.createGraphics();
                g.setColor(bgColor);
                g.fillRect(0, 0, finalImage.getWidth(), finalImage.getHeight());
                g.drawImage(scaleBar, 0, 0, null);
                g.drawImage(legendLabels, scaleBar.getWidth(), 0, null);
            } else {
                finalImage = new BufferedImage(width, height + legendLabels.getWidth(),
                        BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = finalImage.createGraphics();
                g.setColor(bgColor);
                g.fillRect(0, 0, finalImage.getWidth(), finalImage.getHeight());

                AffineTransform at = new AffineTransform();
                at.translate(width, height);
                at.rotate(Math.PI / 2);
                g.drawImage(scaleBar, 0, 0, null);
                g.drawImage(legendLabels, at, null);
            }
        }
        return finalImage;

    }

    /**
     * Returns the colour associated with the given value
     * 
     * @param value
     *            The value to get a colour for
     * @return The {@link Color} according to this {@link ColourScheme}
     */
    public abstract Color getColor(Number value);

    /**
     * @return The minimum value of this colour scale
     */
    public abstract Float getScaleMin();

    /**
     * @return The maximum value of this colour scale
     */
    public abstract Float getScaleMax();
}
