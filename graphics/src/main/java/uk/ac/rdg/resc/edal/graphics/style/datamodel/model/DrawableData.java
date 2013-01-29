package uk.ac.rdg.resc.edal.graphics.style.datamodel.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace=ImageData.NAMESPACE, name="Drawable")
public abstract class DrawableData {
    @XmlElements({
        @XmlElement(name="FlatOpacity")
    })
    protected OpacityTransform opacityFunc;
}
