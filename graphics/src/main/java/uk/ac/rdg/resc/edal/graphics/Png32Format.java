package uk.ac.rdg.resc.edal.graphics;

import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes 32-bit (ARGB) PNG images using the ImageIO class. Only one instance of
 * this class will ever be created, so this class contains no member variables
 * to ensure thread safety. Some browsers have problems with {@link PngFormat
 * indexed PNGs}, and some clients find it easier to merge 32-bit images with
 * others.
 * 
 * @author jdb
 */
public class Png32Format extends PngFormat {
    /**
     * Protected default constructor to prevent direct instantiation.
     */
    protected Png32Format() {
    }

    @Override
    public String getMimeType() {
        return "image/png;mode=32bit";
    }

    @Override
    public void writeImage(List<BufferedImage> frames, OutputStream out) throws IOException {
        List<BufferedImage> frames32bit = new ArrayList<BufferedImage>(frames.size());
        for (BufferedImage source : frames) {
            frames32bit.add(convertARGB(source));
        }
        super.writeImage(frames32bit, out);
    }

    /**
     * Converts the source image to 32-bit colour (ARGB).
     * 
     * @param source
     *            the source image to convert
     * @return a copy of the source image with a 32-bit (ARGB) colour depth
     */
    private static BufferedImage convertARGB(BufferedImage source) {
        BufferedImage dest = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        ColorConvertOp convertOp = new ColorConvertOp(source.getColorModel().getColorSpace(), dest.getColorModel()
                .getColorSpace(), null);
        convertOp.filter(source, dest);
        return dest;
    }
}
