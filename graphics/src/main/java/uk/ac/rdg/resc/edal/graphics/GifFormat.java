package uk.ac.rdg.resc.edal.graphics;

import java.awt.image.BufferedImage;
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
 * @author Jon Blower $Revision$ $Date$ $Log$
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
        byte[] rgbPalette = null;
        IndexColorModel icm = null;
        for (BufferedImage frame : frames) {
            if (rgbPalette == null) {
                // This is the first frame
                e.setSize(frame.getWidth(), frame.getHeight());
                // Get the colour palette. We assume that we have used an
                // IndexColorModel that is the same for all frames
                icm = (IndexColorModel) frame.getColorModel();
                rgbPalette = getRGBPalette(icm);
            }
            // Get the indices of each pixel in the image. We do this after the
            // frames have been created because we might have added a label to
            // the image.
            byte[] indices = ((DataBufferByte) frame.getRaster().getDataBuffer()).getData();
            e.addFrame(rgbPalette, indices, icm.getTransparentPixel());
        }
        e.finish();
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
