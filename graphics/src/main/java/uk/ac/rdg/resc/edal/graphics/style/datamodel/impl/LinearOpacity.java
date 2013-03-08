package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.PlottingDatum;
import uk.ac.rdg.resc.edal.util.Extents;


@XmlType(namespace = Image.NAMESPACE, name="LinearOpacityType")
public class LinearOpacity extends OpacityTransform {
    @XmlElement(name = "DataFieldName", required = true)
    private String dataFieldName;
    @XmlElement(name = "OpaqueValue", required = true)
    private Float opaqueValue;
    @XmlElement(name = "TransparentValue", required = true)
    private Float transparentValue;
//    @XmlElement(name = "MissingDataOpacity")
//    private Float opacityForMissingData = 1.0f;

    LinearOpacity() { }

    public LinearOpacity(String dataFieldName, Float opaqueValue, Float transparentValue
            /*, Float opacityForMissingData*/) {
        super();
        this.dataFieldName = dataFieldName;
        this.opaqueValue = opaqueValue;
        this.transparentValue = transparentValue;
//        this.opacityForMissingData = opacityForMissingData;
    }

    private Float getOpacityForValue(Float value) {
        if (value == null || Float.isNaN(value)) {
            return 1f;
//            return opacityForMissingData;
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
                return 1.0f;
            } else if (value > transparentValue) {
                return 0f;
            } else {
                return 1f - ((value - opaqueValue) / (transparentValue - opaqueValue));
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
    
    @Override
    protected Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new HashSet<Drawable.NameAndRange>();
        if(opaqueValue > transparentValue) {
            ret.add(new NameAndRange(dataFieldName, Extents.newExtent(transparentValue, opaqueValue)));
        } else {
            ret.add(new NameAndRange(dataFieldName, Extents.newExtent(opaqueValue, transparentValue)));
        }
        return ret;
    }
    
    public static void main(String[] args) {
        LinearOpacity o = new LinearOpacity("a", 0f, 1f);
        LinearOpacity o2 = new LinearOpacity("a", 1f, 0f);
        for(float i = -0.01f; i<=1.01f; i += 0.01) {
            System.out.println(i+", "+o.getOpacityForValue(i)+", "+o2.getOpacityForValue(i));
        }
    }
}
