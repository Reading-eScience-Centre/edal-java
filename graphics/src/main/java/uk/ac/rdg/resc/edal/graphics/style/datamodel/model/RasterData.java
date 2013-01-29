package uk.ac.rdg.resc.edal.graphics.style.datamodel.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace=ImageData.NAMESPACE, name="RasterLayerType")
public class RasterData extends DrawableData {
    @XmlElement(name="ColourScheme")
    private ColourScheme1DData colourScheme = new ColourScheme1DData();
}
