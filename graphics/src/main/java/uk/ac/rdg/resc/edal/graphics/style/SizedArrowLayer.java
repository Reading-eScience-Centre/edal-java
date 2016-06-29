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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.graphics.style.ArrowLayer.ArrowStyle;
import uk.ac.rdg.resc.edal.graphics.utils.VectorFactory;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;

public class SizedArrowLayer extends GriddedImageLayer {
    private String directionFieldName;
    private String magnitudeFieldName;
    private Color arrowColour = Color.black;

    private int minArrowSize = 4;
    private int maxArrowSize = 12;

    private ArrowStyle arrowStyle = ArrowStyle.UPSTREAM;
    private ScaleRange magnitudeScaleRange;

    public SizedArrowLayer(String directionFieldName, String magnitudeFieldName,
            Integer minArrowSize, Integer maxArrowSize, ScaleRange magnitudeScaleRange,
            Color arrowColour, ArrowStyle arrowStyle) {
        this.directionFieldName = directionFieldName;
        this.magnitudeFieldName = magnitudeFieldName;

        this.minArrowSize = minArrowSize;
        this.maxArrowSize = maxArrowSize;

        this.magnitudeScaleRange = magnitudeScaleRange;

        this.arrowColour = arrowColour;
        this.arrowStyle = arrowStyle;
    }

    @Override
    protected void drawIntoImage(BufferedImage image, MapFeatureDataReader dataReader)
            throws EdalException {
        Array2D<Number> directions = dataReader.getDataForLayerName(directionFieldName);
        Array2D<Number> magnitudes = dataReader.getDataForLayerName(magnitudeFieldName);

        Graphics2D g = image.createGraphics();
        g.setColor(arrowColour);

        int width = image.getWidth();
        int height = image.getHeight();

        /*
         * Calculate the (floating point) number of pixels per arrow. In ideal
         * situations, this will be an integer equal to the sample spacing
         * 
         * For non-ideal situations it means that the arrows will not be evenly
         * spaced (they will be either n or n+1 pixels apart). They will tile
         * perfectly though.
         */
        double xPixelsPerArrow = ((double) width) / (width / (maxArrowSize * 2));
        double yPixelsPerArrow = ((double) height) / (height / (maxArrowSize * 2));
        double xLoc = xPixelsPerArrow / 2;
        double yLoc = yPixelsPerArrow / 2;

        Array<HorizontalPosition> domainObjects = dataReader
                .getMapDomainObjects(directionFieldName);

        for (int j = 0; j < height; j++) {
            if (yLoc > yPixelsPerArrow) {
                yLoc -= yPixelsPerArrow;
                for (int i = 0; i < width; i++) {

                    if (xLoc > xPixelsPerArrow) {
                        xLoc -= xPixelsPerArrow;

                        /*
                         * We are at a point where we need to draw an arrow
                         */
                        Number magnitude = magnitudes.get(j, i);
                        Double angle = GISUtils.transformWgs84Heading(directions.get(j, i),
                                domainObjects.get(j, i));
                        if (magnitude != null && !Float.isNaN(magnitude.floatValue())
                                && angle != null && !Float.isNaN(angle.floatValue())) {

                            double scaleZeroToOne = magnitudeScaleRange.scaleZeroToOne(magnitude);
                            if (scaleZeroToOne < 0) {
                                scaleZeroToOne = 0.0;
                            }
                            if (scaleZeroToOne > 1) {
                                scaleZeroToOne = 1.0;
                            }

                            int arrowSize = (int) (minArrowSize + scaleZeroToOne
                                    * (maxArrowSize - minArrowSize));
                            if (arrowStyle == ArrowStyle.UPSTREAM) {
                                /* Convert from degrees to radians */
                                angle = angle * GISUtils.DEG2RAD;
                                /* Calculate the end point of the arrow */
                                double iEnd = i + arrowSize * Math.sin(angle);
                                /*
                                 * Screen coordinates go down, but north is up,
                                 * hence the minus sign
                                 */
                                double jEnd = j - arrowSize * Math.cos(angle);
                                /* Draw a dot representing the data location */
                                g.fillOval(i - 2, j - 2, 4, 4);
                                /*
                                 * Draw a line representing the vector direction
                                 */
                                g.setStroke(new BasicStroke(1));
                                g.drawLine(i, j, (int) Math.round(iEnd), (int) Math.round(jEnd));
                            } else if (arrowStyle == ArrowStyle.THIN_ARROW) {
                                /*
                                 * The overall arrow size is 10 for things
                                 * returned from the VectorFactory, so we
                                 * multiply the arrow size by 0.1 to get the
                                 * scale factor.
                                 */
                                VectorFactory.renderVector("LINEVEC", angle.doubleValue() * Math.PI
                                        / 180.0, i, j, arrowSize * 0.1f, g);
                            } else if (arrowStyle == ArrowStyle.FAT_ARROW) {
                                VectorFactory.renderVector("STUMPVEC", angle.doubleValue()
                                        * Math.PI / 180.0, i, j, arrowSize * 0.1f, g);
                            } else if (arrowStyle == ArrowStyle.TRI_ARROW) {
                                VectorFactory.renderVector("TRIVEC", angle.doubleValue() * Math.PI
                                        / 180.0, i, j, arrowSize * 0.1f, g);
                            }

                        }
                    }
                    xLoc += 1.0;
                }
            }
            yLoc += 1.0;
        }
    }

    @Override
    public Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new HashSet<Drawable.NameAndRange>();
        ret.add(new NameAndRange(directionFieldName, Extents.newExtent(0f, 360f)));
        return ret;
    }
}
