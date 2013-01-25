package uk.ac.rdg.resc.edal.graphics.style.model;

import java.awt.image.BufferedImage;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.graphics.style.Id2FeatureAndMember;

@XmlType(namespace=Image.NAMESPACE)
@XmlSeeAlso({Image.class, ImageLayer.class})
public abstract class DrawableLayer {
    @XmlElement
    public Integer opacity = 100;
    
    public abstract BufferedImage drawImage(GlobalPlottingParams params, Id2FeatureAndMember id2Feature);
}
