package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import javax.xml.bind.annotation.XmlType;


@XmlType(namespace=Image.NAMESPACE, name="OpacityTransformType")
public abstract class OpacityTransform {
    public abstract Float getOpacityForValue(Float value);
}
