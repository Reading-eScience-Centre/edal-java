package uk.ac.rdg.resc.edal.graphics.style.datamodel.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace=ImageData.NAMESPACE, name="FlatOpacityType")
public class FlatOpacity extends OpacityTransform {
    @XmlElement(name="Opacity", required=true)
    private Float opacity = 1.0f;
    
    @Override
    public Float getOpacityForValue(Float value) {
        return opacity;
    }
}
