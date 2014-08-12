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
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.joda.time.DateTime;
import org.opengis.metadata.extent.GeographicBoundingBox;

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
        /*
         * Prevent the use of a disk cache when creating images using ImageIO.
         * Using the disk cache can cause problems if a suitable temporary
         * directory can't be found, causing the rendering to fail under certain
         * systems.
         */
        ImageIO.setUseCache(false);
        // We pre-create all the ImageFormat objects
        for (ImageFormat format : new ImageFormat[] { new PngFormat(), new Png32Format(),
                new GifFormat(), new JpegFormat(), new KmzFormat() }) {
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
            throw new InvalidFormatException("The image format " + mimeType
                    + " is not supported");
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
     * Writes the given list of frames to the supplied output stream. If there
     * are multiple frames, the image format must support animations, otherwise
     * an exception is thrown.
     * 
     * @param frames
     *            A {@link List} of {@link BufferedImage}s to plot
     * @param out
     *            The {@link OutputStream} to write the resulting image to
     * @param name
     *            The name of the feature being plotted (for KML)
     * @param description
     *            A description of what's being plotted (for KML)
     * @param bbox
     *            The bounding box of the data being plotted
     * @param tValues
     *            The time values corresponding to the frames of data
     * @param zValue
     *            A string representing the elevation of the plotted data
     * @param legend
     *            An image representing the legend
     * @param frameRate
     *            The frame rate to render an animation at
     * @throws IOException
     *             If there is a problem writing the data to the
     *             {@link OutputStream}
     */
    public abstract void writeImage(List<BufferedImage> frames, OutputStream out, String name,
            String description, GeographicBoundingBox bbox, List<DateTime> tValues, String zValue,
            BufferedImage legend, Integer frameRate) throws IOException;

}
