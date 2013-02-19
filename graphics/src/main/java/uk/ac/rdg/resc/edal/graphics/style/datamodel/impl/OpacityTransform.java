package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.image.BufferedImage;

import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.DataReadingTypes.PlotType;


@XmlType(namespace=Image.NAMESPACE, name="OpacityTransformType")
public abstract class OpacityTransform extends ImageLayer {
    
    public OpacityTransform() {
        super(PlotType.RASTER);
    }

    protected abstract void applyOpacityToImage(BufferedImage image, DataReader dataReader);

    @Override
    protected final void drawIntoImage(BufferedImage image, DataReader dataReader) {
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
