package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlType(namespace = Image.NAMESPACE, name="LinearOpacityType")
public class LinearOpacity extends OpacityTransform {
    @XmlElement(name = "OpaqueValue", required = true)
    private Float opaqueValue;
    @XmlElement(name = "TransparentValue", required = true)
    private Float transparentValue;
    @XmlElement(name = "MissingDataOpacity")
    private Float opacityForMissingData = 0.0f;

    @Override
    public Float getOpacityForValue(Float value) {
        if (value == null || Float.isNaN(value)) {
            return opacityForMissingData;
        }

        boolean highOpaque = opaqueValue > transparentValue;

        if (highOpaque) {
            if (value > opaqueValue) {
                return opaqueValue;
            } else if (value < transparentValue) {
                return transparentValue;
            } else {
                return (value - transparentValue) / (opaqueValue - transparentValue);
            }
        } else {
            if (value < opaqueValue) {
                return opaqueValue;
            } else if (value > transparentValue) {
                return transparentValue;
            } else {
                return (value - opaqueValue) / (transparentValue - opaqueValue);
            }
        }
    }

}
