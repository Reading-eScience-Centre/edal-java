/*******************************************************************************
 * Copyright (c) 2011 The University of Reading
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
 *******************************************************************************/
package uk.ac.rdg.resc.edal.graphics.formats;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Creates (possibly animated) GIFs. Only one instance of this class will ever
 * be created, so this class contains no member variables to ensure thread
 * safety.
 * 
 * @author Jon Blower
 */
public class GifFormat extends SimpleFormat {

    protected GifFormat() {
    }

    @Override
    public void writeImage(List<BufferedImage> frames, OutputStream out) throws IOException {
        AnimatedGifEncoder e = new AnimatedGifEncoder();
        e.start(out);
        if (frames.size() > 1) {
            // this is an animated GIF. Set to loop infinitely.
            e.setRepeat(0);
            e.setDelay(150); // delay between frames in milliseconds
        }
        boolean sizeSet = false;
        IndexColorModel icm = getGeneralIndexedColorModelWithTransparency();
        byte[] rgbPalette = getRGBPalette(icm);
        for (BufferedImage frame : frames) {
            
            
            BufferedImage gifFrame = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, icm);
            gifFrame.createGraphics().drawImage(frame, 0, 0, null);
            if (!sizeSet) {
                e.setSize(frame.getWidth(), frame.getHeight());
                sizeSet = true;
            }
            // Get the indices of each pixel in the image. We do this after the
            // frames have been created because we might have added a label to
            // the image.
            byte[] indices = ((DataBufferByte) gifFrame.getRaster().getDataBuffer()).getData();
            e.addFrame(rgbPalette, indices, icm.getTransparentPixel());
        }
        e.finish();
    }

    private static IndexColorModel getGeneralIndexedColorModelWithTransparency() {
        /*
         * This was nabbed from the source of BufferedImage. This is the default
         * IndexColorModel generated when we create a BufferedImage of type
         * TYPE_BYTE_INDEXED without specifying an IndexColorModel, with the
         * difference that we add a single transparent pixel.
         */
        
        // Create a 6x6x6 color cube
        int[] cmap = new int[256];
        int i=0;
        cmap[i++] = 255 << 32 | (255 << 16) | (255 << 8) | (255);
        for (int r=0; r < 256; r += 51) {
            for (int g=0; g < 256; g += 51) {
                for (int b=0; b < 256; b += 51) {
                    cmap[i++] = (r<<16)|(g<<8)|b;
                }
            }
        }
        // And populate the rest of the cmap with gray values
        int grayIncr = 256/(256-i);

        // The gray ramp will be between 18 and 252
        int gray = grayIncr*3;
        for (; i < 255; i++) {
            cmap[i] = (gray<<16)|(gray<<8)|gray;
            gray += grayIncr;
        }

        IndexColorModel colorModel = new IndexColorModel(8, 256, cmap, 0, true, 0, DataBuffer.TYPE_BYTE);
        return colorModel;
    }
    
    /**
     * Gets the RGB palette as an array of 256*3 bytes (i.e. 256 colours in RGB
     * order). If the given IndexColorModel contains less than 256 colours the
     * array is padded with zeroes.
     */
    private static byte[] getRGBPalette(IndexColorModel icm) {
        byte[] reds = new byte[icm.getMapSize()];
        byte[] greens = new byte[icm.getMapSize()];
        byte[] blues = new byte[icm.getMapSize()];
        icm.getReds(reds);
        icm.getGreens(greens);
        icm.getBlues(blues);
        byte[] palette = new byte[256 * 3];
        for (int i = 0; i < icm.getMapSize(); i++) {
            palette[i * 3] = reds[i];
            palette[i * 3 + 1] = greens[i];
            palette[i * 3 + 2] = blues[i];
        }
        return palette;
    }

    @Override
    public String getMimeType() {
        return "image/gif";
    }

    @Override
    public boolean supportsMultipleFrames() {
        return true;
    }

    @Override
    public boolean supportsFullyTransparentPixels() {
        return true;
    }

    @Override
    public boolean supportsPartiallyTransparentPixels() {
        return false;
    }
}
