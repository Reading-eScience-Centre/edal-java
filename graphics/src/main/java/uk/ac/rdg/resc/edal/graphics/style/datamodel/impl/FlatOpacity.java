package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.util.CollectionUtils;

@XmlType(namespace = Image.NAMESPACE, name = "FlatOpacityType")
public class FlatOpacity extends OpacityTransform {
    @XmlElement(name = "Opacity", required = true)
    private Float opacity = 1.0f;

    @SuppressWarnings("unused")
    private FlatOpacity(){}
    
    public FlatOpacity(Float opacity) {
        super();
        this.opacity = opacity;
    }

    @Override
    protected void applyOpacityToImage(BufferedImage image, DataReader dataReader) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] imagePixels = image.getRGB(0, 0, width, height, null, 0, width);

        int alpha = (int) (opacity * 255);
        for (int i=0; i < imagePixels.length; i++) {
            imagePixels[i] = blendPixel(imagePixels[i], alpha);
        }
        image.setRGB(0, 0, width, height, imagePixels, 0, width);
    }

    @Override
    protected Set<NameAndRange> getFieldsWithScales() {
        return Collections.emptySet();
    }
}
