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

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Extents;

public class Raster2DLayer extends GriddedImageLayer {

    private String xDataFieldName;
    private String yDataFieldName;
    private ColourScheme2D colourScheme;
    
    public Raster2DLayer(String xDataFieldName, String yDataFieldName, ColourScheme2D colourScheme) {
        this.xDataFieldName = xDataFieldName;
        this.yDataFieldName = yDataFieldName;
        this.colourScheme = colourScheme;
    }

    @Override
    protected void drawIntoImage(BufferedImage image, MapFeatureDataReader dataReader) throws EdalException {
        /*
         * Read fields into arrays
         */
        Array2D<Number> xValues = dataReader.getDataForLayerName(xDataFieldName);
        Array2D<Number> yValues = dataReader.getDataForLayerName(yDataFieldName);
        
        Iterator<Number> xIterator = xValues.iterator();
        Iterator<Number> yIterator = yValues.iterator();
        int index = 0;
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        /*
         * Could have done check on either iterator - they should be the same size
         * 
         * Get the colours from the 2 values and set the pixel colour
         */
        while(xIterator.hasNext()) {
            pixels[index++] = colourScheme.getColor(xIterator.next(), yIterator.next()).getRGB();
        }
        image.setRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
    }

    @Override
    public Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new LinkedHashSet<Drawable.NameAndRange>();
        ret.add(new NameAndRange(xDataFieldName, Extents.newExtent(colourScheme.getScaleMin(1),
                colourScheme.getScaleMax(1))));
        ret.add(new NameAndRange(yDataFieldName, Extents.newExtent(colourScheme.getScaleMin(2),
                colourScheme.getScaleMax(2))));
        return ret;
    }

}
