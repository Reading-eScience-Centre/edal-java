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
    private Integer glyphSize = 9;
	// The colour scheme to use
    protected ColourScheme colourScheme = new PaletteColourScheme();

	public ConfidenceIntervalLayer(String lowerFieldName, String upperFieldName,
			int glyphSize, ColourScheme colourSceme) {
		this.lowerFieldName = lowerFieldName;
		this.upperFieldName = upperFieldName;
		this.glyphSize = glyphSize;
		this.colourScheme = colourSceme;
		
		setSampleSize();
	}
	
	private void setSampleSize() {
	    if(glyphSize < 1 || glyphSize == null) {
	        throw new IllegalArgumentException("Glyph size must be non-null and > 0");
	    }        
	    setXSampleSize(glyphSize);
	    setYSampleSize(glyphSize);
	}

	@Override
	protected void drawIntoImage(BufferedImage image,
			MapFeatureDataReader dataReader) throws EdalException {
        /*
         * The graphics object for drawing
         */
        Graphics2D g = image.createGraphics();
        
		/*
         * Plot the lower triangles. Start by extracting the data from the
         * catalogue. 
         */
        Array2D<Number> values = dataReader.getDataForLayerName(lowerFieldName);
        /*
         * The iterator iterates over the x-dimension first.
         */
        int index = 0;
        for (Number value : values) {
            if (value != null && !Float.isNaN(value.floatValue())) {
            	int j = index/image.getWidth();
            	int i = index - j*image.getWidth();
            	if (j%getYSampleSize() == 0 && i%getXSampleSize() == 0) {
	            	Color color = colourScheme.getColor(value);
	        		int[] xPoints = {i - glyphSize / 2, i + glyphSize / 2, i + glyphSize / 2};
	        		int[] yPoints = {j + glyphSize / 2, j + glyphSize / 2, j - glyphSize / 2};
	        		g.setColor(color);
	            	g.fillPolygon(xPoints, yPoints, 3);	
            	}
            }
        	index++;
        }
        
		/*
         * Plot the upper triangles. Start by extracting the data from the
         * catalogue. 
         */
        values = dataReader.getDataForLayerName(upperFieldName);
        /*
         * The iterator iterates over the x-dimension first.
         */
        index = 0;
        for (Number value : values) {
            if (value != null && !Float.isNaN(value.floatValue())) {
            	int j = index/image.getWidth();
            	int i = index - j*image.getWidth();
            	if (j%getYSampleSize() == 0 && i%getXSampleSize() == 0) {
            		Color color = colourScheme.getColor(value);
            		int[] xPoints = {i - glyphSize / 2, i - glyphSize / 2, i + glyphSize / 2};
            		int[] yPoints = {j + glyphSize / 2, j - glyphSize / 2, j - glyphSize / 2};
            		g.setColor(color);
            		g.fillPolygon(xPoints, yPoints, 3);
            	}
            }
        	index++;
        }
	}

	@Override
	public Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new HashSet<Drawable.NameAndRange>();
        ret.add(new NameAndRange(lowerFieldName, Extents.newExtent(
                colourScheme.getScaleMin(), colourScheme.getScaleMax())));
        ret.add(new NameAndRange(upperFieldName, Extents.newExtent(
                colourScheme.getScaleMin(), colourScheme.getScaleMax())));
        return ret;
	}

}
