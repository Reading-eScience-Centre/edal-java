package uk.ac.rdg.resc.edal.graphics;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.graphics.exceptions.InvalidFormatException;

/**
 * Abstract superclass for all image formats. Only one instance of each subclass
 * will be created so subclasses must be thread safe. Subclasses should provide
 * protected default constructors so that they cannot be instantiated directly.
 * 
 * @author Jon Blower
 * @author Guy Griffiths
 */
public abstract class ImageFormat {
    private static final Map<String, ImageFormat> formats = new LinkedHashMap<String, ImageFormat>();

    static {
        // Prevent the use of a disk cache when creating images using ImageIO.
        // Using the disk cache can cause problems if a suitable temporary
        // directory can't be found, causing the rendering to fail under certain
        // systems.
        ImageIO.setUseCache(false);
        // We pre-create all the ImageFormat objects
        for (ImageFormat format : new ImageFormat[] { new PngFormat(), new Png32Format(), new GifFormat(),
                new JpegFormat(), new KmzFormat() }) {
            formats.put(format.getMimeType(), format);
        }
    }

    /**
     * Gets the image MIME types that are supported
     * 
     * @return the MIME types as a Set of Strings
     */
    public static Set<String> getSupportedMimeTypes() {
        return formats.keySet();
    }

    /**
     * Gets an ImageFormat object corresponding with the given MIME type. Note
     * that this does not create a new object with each invocation: only one
     * ImageFormat object is created for each MIME type.
     * 
     * @param mimeType
     *            The MIME type of the requested format
     * @return An ImageFormat object capable of rendering images in the given
     *         MIME type
     * @throws InvalidFormatException
     *             if the given MIME type is not supported
     */
    public static ImageFormat get(String mimeType) throws InvalidFormatException {
        ImageFormat format = formats.get(mimeType);
        if (format == null) {
            throw new InvalidFormatException("The image format " + mimeType + " is not supported by this server");
        }
        return format;
    }

    /**
     * Returns the MIME type that is supported by this ImageFormat object.
     */
    public abstract String getMimeType();

    /**
     * Returns true if this image format supports multi-frame animations.
     */
    public abstract boolean supportsMultipleFrames();

    /**
     * Returns true if this image format supports fully-transparent pixels.
     */
    public abstract boolean supportsFullyTransparentPixels();

    /**
     * Returns true if this image format supports partially-transparent pixels.
     * If this is true then supportsFullyTransparentPixels() should also be
     * true.
     */
    public abstract boolean supportsPartiallyTransparentPixels();

    /**
     * Returns true if this image format needs an accompanying legend. This
     * default implementation returns false, but subclasses can override.
     * 
     * @see KmzFormat
     */
    public abstract boolean requiresLegend();

    /**
     * Writes the given list of {@link java.awt.BufferedImage}s to the given
     * OutputStream. If this ImageFormat doesn't support animations then the
     * given list of frames should only contain one entry, otherwise an
     * IllegalArgumentException will be thrown.
     * 
     * @param frames
     *            List of BufferedImages to render into an image
     * @param out
     *            The OutputStream to which the image will be written
     * @param featureCollection
     *            the {@link FeatureCollection} to which the feature belongs.
     *            This is used for metadata and axes for {@link KmzFormat}
     * @param featureId
     *            The ID of the feature within its collection The bounding box
     *            of the image(s)
     * @param legend
     *            A legend image (this will be null unless this.requiresLegend()
     *            returns true.
     * @throws IOException
     *             if there was an error writing to the output stream
     * @throws IllegalArgumentException
     *             if this ImageFormat cannot render all of the given
     *             BufferedImages.
     */
    public abstract void writeImage(List<BufferedImage> frames, OutputStream out,
            FeatureCollection<Feature> featureCollection, String featureId, BufferedImage legend) throws IOException;
}
