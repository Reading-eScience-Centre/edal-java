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

public class RasterRGBLayer extends GriddedImageLayer {

    private RGBBandColourScheme colourScheme;
    private String rBand;
    private String gBand;
    private String bBand;

    public RasterRGBLayer(String rBand, String gBand, String bBand, RGBBandColourScheme colourScheme) {
        this.rBand = rBand;
        this.gBand = gBand;
        this.bBand = bBand;
        this.colourScheme = colourScheme;
    }

    @Override
    protected void drawIntoImage(BufferedImage image, MapFeatureDataReader dataReader) throws EdalException {
        /*
         * Read fields into arrays
         */
        Array2D<Number> rValues = dataReader.getDataForLayerName(rBand);
        Array2D<Number> gValues = dataReader.getDataForLayerName(gBand);
        Array2D<Number> bValues = dataReader.getDataForLayerName(bBand);
        
        Iterator<Number> rIterator = rValues.iterator();
        Iterator<Number> gIterator = gValues.iterator();
        Iterator<Number> bIterator = bValues.iterator();
        int index = 0;
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        /*
         * Could have done check on any of the iterators - they should be the same size
         * 
         * Get the colours from the RGB values and set the pixel colour
         */
        while(rIterator.hasNext()) {
            pixels[index++] = colourScheme.getColor(rIterator.next(), gIterator.next(), bIterator.next()).getRGB();
        }
        image.setRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
    }

    @Override
    public Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new LinkedHashSet<Drawable.NameAndRange>();
        ret.add(new NameAndRange(rBand,colourScheme.getRedScale()));
        ret.add(new NameAndRange(gBand,colourScheme.getGreenScale()));
        ret.add(new NameAndRange(bBand,colourScheme.getBlueScale()));
        return ret;
    }

}
