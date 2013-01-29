package uk.ac.rdg.resc.edal.graphics.style.datamodel.model;

import javax.xml.bind.annotation.XmlElement;

public class FlatOpacity extends OpacityTransform {
    @XmlElement(name="Opacity")
    private Float opacity = 1.0f;
    
    @Override
    public Float getOpacityForValue(Float value) {
        return opacity;
    }
}
