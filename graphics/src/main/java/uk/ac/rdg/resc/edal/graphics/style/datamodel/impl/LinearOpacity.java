package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.image.BufferedImage;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.PlottingDatum;


@XmlType(namespace = Image.NAMESPACE, name="LinearOpacityType")
public class LinearOpacity extends OpacityTransform {
    @XmlElement(name = "DataFieldName", required = true)
    private String dataFieldName;
    @XmlElement(name = "OpaqueValue", required = true)
    private Float opaqueValue;
    @XmlElement(name = "TransparentValue", required = true)
    private Float transparentValue;
    @XmlElement(name = "MissingDataOpacity")
    private Float opacityForMissingData = 0.0f;

    private Float getOpacityForValue(Float value) {
        if (value == null || Float.isNaN(value)) {
            return opacityForMissingData;
        }

        boolean highOpaque = opaqueValue > transparentValue;

        if (highOpaque) {
            if (value > opaqueValue) {
                return 1.0f;
            } else if (value < transparentValue) {
                return 0f;
            } else {
                return (value - transparentValue) / (opaqueValue - transparentValue);
            }
        } else {
            if (value < opaqueValue) {
                return 1f;
            } else if (value > transparentValue) {
                return 0f;
            } else {
                return (value - opaqueValue) / (opaqueValue - transparentValue);
            }
        }
    }

    @Override
    protected void applyOpacityToImage(BufferedImage image, DataReader dataReader) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] imagePixels = image.getRGB(0, 0, width, height, null, 0, width);

        for(PlottingDatum datum : dataReader.getDataForLayerName(dataFieldName)){
            int xIndex = datum.getGridCoords().getXIndex();
            int yIndex = datum.getGridCoords().getYIndex();
            int imageIndex = xIndex + yIndex * width;
            int alpha = ((int) (getOpacityForValue(datum.getValue().floatValue()) * 255));
            imagePixels[imageIndex] = blendPixel(imagePixels[imageIndex], alpha);
        }
        
        image.setRGB(0, 0, width, height, imagePixels, 0, width);
    }
}
