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

import uk.ac.rdg.resc.edal.exceptions.EdalException;


public abstract class OpacityTransform extends GriddedImageLayer {

    protected abstract void applyOpacityToImage(BufferedImage image, MapFeatureDataReader dataReader) throws EdalException;

    @Override
    protected final void drawIntoImage(BufferedImage image, MapFeatureDataReader dataReader) throws EdalException {
        applyOpacityToImage(image, dataReader);
    }
    
    /**
     * Applies an alpha value to a pixel.
     * 
     * @param original The original pixel value
     * @param alpha An alpha value (0-255)
     * @return The alpha-blended pixel
     */
    protected int blendPixel(int original, int alpha) {
        // Find the alpha value of the original pixel
        int sourceAlpha = ((original & 0xff000000) >> 24) & 0xff;
        int destAlpha;
        int colour = original & 0x00ffffff;
        // Find the destination alpha value and shift 24 bits to the left
        if(sourceAlpha != 255) {
            // If the original pixel had an alpha value, blend the two... 
            destAlpha = (alpha * sourceAlpha / 255) << 24;
        } else {
            // Otherwise just take the desired alpha...
            destAlpha = alpha << 24;
        }
        // Add the alpha channel and return 
        return colour | destAlpha;
    }
}
