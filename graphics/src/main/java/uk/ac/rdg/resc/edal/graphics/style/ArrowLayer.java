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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.graphics.style.util.StyleXMLParser.ColorAdapter;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;

@XmlType(namespace = MapImage.NAMESPACE, propOrder = { "directionFieldName", "arrowSize",
        "arrowColour" }, name = "ArrowLayerType")
public class ArrowLayer extends GriddedImageLayer {
    @XmlElement(name = "DirectionFieldName", required = true)
    private String directionFieldName;
    @XmlElement(name = "ArrowColour")
    @XmlJavaTypeAdapter(ColorAdapter.class)
    private Color arrowColour = Color.black;

    private Integer arrowSize = 8;

    @SuppressWarnings("unused")
    private ArrowLayer() {
    }

    @XmlElement(name = "ArrowSize")
    public void setArrowSize(Integer arrowSize) {
        this.arrowSize = arrowSize;
        /*
         * This is annotated on the setter, because the it checks for a valid
         * size
         */
        if (arrowSize < 1 || arrowSize == null) {
            throw new IllegalArgumentException("Arrow size must be non-null and > 0");
        }
        setXSampleSize((int) (arrowSize * 1.5));
        setYSampleSize((int) (arrowSize * 1.5));
    }

    public ArrowLayer(String directionFieldName, Integer arrowSize, Color arrowColour) {
        this.directionFieldName = directionFieldName;
        this.arrowColour = arrowColour;
        setArrowSize(arrowSize);
    }

    public String getDirectionFieldName() {
        return directionFieldName;
    }

    public Integer getArrowSize() {
        return arrowSize;
    }

    public Color getArrowColour() {
        return arrowColour;
    }

    @Override
    protected void drawIntoImage(BufferedImage image, MapFeatureDataReader dataReader) throws EdalException {
        Array2D<Number> values = dataReader.getDataForLayerName(directionFieldName);

        Graphics2D g = image.createGraphics();
        g.setColor(arrowColour);

        int width = image.getWidth();
        int height = image.getHeight();

        /*
         * Calculate the (floating point) number of pixels per arrow. In ideal
         * situations, this will be an integer equal to the arrow size * 2
         * 
         * For non-ideal situtations it means that the arrows will not be evenly
         * spaced (they will be either n or n+1 pixels apart). They will tile
         * perfectly though.
         */
        double xPixelsPerArrow = ((double) width) / (width / (arrowSize * 2));
        double yPixelsPerArrow = ((double) height) / (height / (arrowSize * 2));
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
                        Double angle = GISUtils.transformWgs84Heading(values.get(j, i),
                                domainObjects.get(j, i));
                        if (angle != null && !Float.isNaN(angle.floatValue())) {
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
                            /* Draw a line representing the vector direction */
                            g.setStroke(new BasicStroke(1));
                            g.drawLine(i, j, (int) Math.round(iEnd), (int) Math.round(jEnd));
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
        ret.add(new NameAndRange(directionFieldName, Extents.newExtent(0f, new Float(2 * Math.PI))));
        return ret;
    }
}
