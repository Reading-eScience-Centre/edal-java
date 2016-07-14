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
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * Plot confidence interval triangles.
 * 
 */
public class ConfidenceIntervalLayer extends GriddedImageLayer {

    // The name of the variable to use for the lower bounds
    protected String lowerFieldName;
    // The name of the variable to use for the upper bounds
    protected String upperFieldName;
    // The size of the glyphs
    private Integer glyphSize = 8;
    // The colour scheme to use
    protected ColourScheme colourScheme;

    public ConfidenceIntervalLayer(String lowerFieldName, String upperFieldName, int glyphSize,
            ColourScheme colourSceme) {
        this.lowerFieldName = lowerFieldName;
        this.upperFieldName = upperFieldName;
        this.glyphSize = glyphSize;
        this.colourScheme = colourSceme;

        if (glyphSize < 1) {
            throw new IllegalArgumentException("Glyph size must be > 0");
        }
    }

    @Override
    protected void drawIntoImage(BufferedImage image, MapFeatureDataReader dataReader)
            throws EdalException {
        /*
         * The graphics object for drawing
         */
        Graphics2D g = image.createGraphics();

        /*
         * Plot the lower triangles. Start by extracting the data from the
         * catalogue.
         */
        Array2D<Number> lowerValues = dataReader.getDataForLayerName(lowerFieldName);
        Array2D<Number> upperValues = dataReader.getDataForLayerName(upperFieldName);
        for (int i = glyphSize/2; i <= lowerValues.getXSize()-glyphSize/2; i += glyphSize) {
            for (int j = glyphSize/2; j <= lowerValues.getYSize()-glyphSize/2; j += glyphSize) {
                Number lowVal = lowerValues.get(j, i);
                Number highVal = upperValues.get(j, i);
                if ((lowVal != null && !Float.isNaN(lowVal.floatValue()))
                        || (highVal != null && !Float.isNaN(highVal.floatValue()))) {
                    Color lowCol = colourScheme.getColor(lowVal);
                    Color upCol = colourScheme.getColor(highVal);
                    
                    int[] xPoints = { i - glyphSize / 2, i + glyphSize / 2, i + glyphSize / 2 };
                    int[] yPoints = { j + glyphSize / 2, j + glyphSize / 2, j - glyphSize / 2 };
                    g.setColor(lowCol);
                    g.fillPolygon(xPoints, yPoints, 3);
                    
                    xPoints = new int[]{ i - glyphSize / 2, i - glyphSize / 2, i + glyphSize / 2 };
                    yPoints = new int[]{ j + glyphSize / 2, j - glyphSize / 2, j - glyphSize / 2 };
                    g.setColor(upCol);
                    g.fillPolygon(xPoints, yPoints, 3);
                }
            }
        }
    }

    @Override
    public Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new HashSet<Drawable.NameAndRange>();
        ret.add(new NameAndRange(lowerFieldName, Extents.newExtent(colourScheme.getScaleMin(),
                colourScheme.getScaleMax())));
        ret.add(new NameAndRange(upperFieldName, Extents.newExtent(colourScheme.getScaleMin(),
                colourScheme.getScaleMax())));
        return ret;
    }

}
