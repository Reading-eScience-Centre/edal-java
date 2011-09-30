package uk.ac.rdg.resc.edal.graphics;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;

/**
 * Abstract superclass for simple image formats that do not require information
 * about the layer, time values, bounding box etc to render an image.
 * 
 * @author Jon Blower $Revision$ $Date$ $Log$
 */
public abstract class SimpleFormat extends ImageFormat {
    /**
     * Returns false: simple formats do not require a lagend.
     */
    @Override
    public final boolean requiresLegend() {
        return false;
    }

    /**
     * Delegates to writeImage(frames, out), ignoring most of the parameters.
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
    @Override
    public void writeImage(List<BufferedImage> frames, OutputStream out, GridSeriesFeature<?> feature, BufferedImage legend)
            throws IOException {
        this.writeImage(frames, out);
    }

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
     * @throws IOException
     *             if there was an error writing to the output stream
     * @throws IllegalArgumentException
     *             if this ImageFormat cannot render all of the given
     *             BufferedImages.
     */
    public abstract void writeImage(List<BufferedImage> frames, OutputStream out) throws IOException;
}
