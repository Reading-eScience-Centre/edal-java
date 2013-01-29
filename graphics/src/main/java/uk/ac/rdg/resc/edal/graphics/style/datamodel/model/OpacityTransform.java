package uk.ac.rdg.resc.edal.graphics.style.datamodel.model;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace=ImageData.NAMESPACE)
public abstract class OpacityTransform {
    public abstract Float getOpacityForValue(Float value);
}
