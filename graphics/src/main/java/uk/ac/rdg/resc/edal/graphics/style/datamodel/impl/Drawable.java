package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.image.BufferedImage;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.graphics.style.Id2FeatureAndMember;

@XmlType(namespace=Image.NAMESPACE, name="Drawable")
public abstract class Drawable {
    @XmlElements({
        @XmlElement(name="FlatOpacity", type = FlatOpacity.class),
        @XmlElement(name="LinearOpacity", type = LinearOpacity.class)
    })
    private OpacityTransform opacityTransform;
    
    public OpacityTransform getOpacityTransform() {
        return opacityTransform;
    }

    @XmlTransient
    public void setOpacityTransform(OpacityTransform opacityTransform) {
        this.opacityTransform = opacityTransform;
    }

    public abstract BufferedImage drawImage(GlobalPlottingParams params, Id2FeatureAndMember id2Feature);
}
