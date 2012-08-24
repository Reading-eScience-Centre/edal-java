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
 * to do the rendering to AVI.
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
