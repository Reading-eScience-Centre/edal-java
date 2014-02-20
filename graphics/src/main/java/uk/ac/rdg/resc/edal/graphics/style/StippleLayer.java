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
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import javax.naming.OperationNotSupportedException;

import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Extents;

public class StippleLayer extends GriddedImageLayer {
    
    private String dataFieldName;
    private DensityMap map;
    
    public StippleLayer(String dataFieldName, DensityMap map) {
        this.dataFieldName = dataFieldName;
        this.map = map;
    }

    @Override
    protected void drawIntoImage(BufferedImage image, MapFeatureDataReader dataReader) throws EdalException {
        int[][] alphas = new int[image.getWidth()][image.getHeight()];
        Array2D<Number> values = dataReader.getDataForLayerName(dataFieldName);
        try {
	        /*
	         * Set the alpha values
	         */
	        for(int i=0; i< image.getWidth();i++) {
	            for(int j=0; j< image.getHeight();j++) {
	                /*
	                 * This is an int between 0 and 255, representing the alpha channel value.
	                 * 
	                 * We use values.get(j, i) because Array2Ds specify the co-ordinates as (y,x) 
	                 */
	                int alpha = Math.round(255 * map.getDensity(values.get(j, i)));
	                alphas[i][j] = alpha;
	            }
	        }
	        /*
	         * Apply black/transparent stippling to the blue image
	         */
	        stippleAlphas(image, alphas, image.getWidth(), image.getHeight());
        } catch (OperationNotSupportedException onse) {
        	throw new EdalException("Problem plotting stipple layer.", onse);
        }
    }
    
    private static int[][] thresholdMap = new int[][]{
        {1,49,13,61,4,52,16,64},
        {33,17,45,29,36,20,48,32},    
        {9,57,5,53,12,60,8,56},    
        {41,25,37,21,44,28,40,24},    
        {3,51,15,63,2,50,14,62},    
        {35,19,47,31,34,18,46,30},    
        {11,59,7,55,10,58,6,54},
        {43,27,39,23,42,26,38,22}
    };
    
    /*
     * This stipples an image into black and transparent pixels depending on an
     * array of alpha values
     * 
     * The thresholdMap above specifies the order that pixels should get
     * switched on to form 65 levels of dithering (incl. endpoints).
     * 
     * This algorithm switches on each pixel according to whether it would be on
     * for an image which consisted entirely of the target alpha value. This is
     * a standard dithering method
     */
    private static void stippleAlphas(BufferedImage image, int[][] alphas, int width, int height) {
        int black = Color.black.getRGB();
        int transparent = new Color(0.0f, 0.0f, 0.0f, 0.0f).getRGB();
        for (int x = 0; x < width; x++) {
            int xmod = x % thresholdMap.length;
            for (int y = 0; y < height; y++) {
                int ymod = y % thresholdMap[0].length;
                int alpha = alphas[x][y];
                if(alpha > 256 * thresholdMap[xmod][ymod] /  ((float) thresholdMap.length * thresholdMap[0].length + 1)){
                    image.setRGB(x, y, black);
                } else {
                    image.setRGB(x, y, transparent);
                }
            }
        }
    }
    
    @Override
    public Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new HashSet<Drawable.NameAndRange>();
        ret.add(new NameAndRange(dataFieldName, Extents.newExtent(map.getMinValue(), map.getMaxValue())));
        return ret;
    }
}
