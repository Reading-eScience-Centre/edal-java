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

package uk.ac.rdg.resc.edal.graphics.formats;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.monte.media.Format;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.VideoFormatKeys;
import org.monte.media.avi.AVIWriter;
import org.monte.media.math.Rational;

/**
 * "Image" format for outputting to AVI.  Uses the
 * <a href="http://www.randelshofer.ch/monte/">Monte Media Library</a>
 * by Werner Randelshofer to do the rendering to AVI.
 * 
 * @author Guy Griffiths
 *
 */
public class AviFormat extends SimpleFormat {
    
    @Override
    public void writeImage(List<BufferedImage> frames, OutputStream out, Integer frameRate) throws IOException {
        if (frames == null || frames.size() == 0) {
            throw new IllegalArgumentException("Cannot create an animation with no images");
        }
        try{
        int width = frames.get(0).getWidth();
        int height = frames.get(0).getHeight();
        Format format = new Format(VideoFormatKeys.EncodingKey, VideoFormatKeys.ENCODING_AVI_DIB,
                VideoFormatKeys.DepthKey, 24, VideoFormatKeys.MediaTypeKey, MediaType.VIDEO,
                VideoFormatKeys.FrameRateKey, new Rational(frameRate), VideoFormatKeys.WidthKey, width,
                VideoFormatKeys.HeightKey, height);

        AVIWriter writer = new AVIWriter(new MemoryCacheImageOutputStream(out));
        writer.addTrack(format);
        writer.setPalette(0, frames.get(0).getColorModel());
        
        for(BufferedImage frame : frames){
            writer.write(0, frame, 1);
        }
        writer.write(0, frames.get(frames.size()-1), 1);
        writer.close();
        
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public String getMimeType() {
        return "video/avi";
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
        return true;
    }

}
