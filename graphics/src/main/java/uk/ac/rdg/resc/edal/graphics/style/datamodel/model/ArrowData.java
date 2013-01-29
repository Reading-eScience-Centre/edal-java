package uk.ac.rdg.resc.edal.graphics.style.datamodel.model;

import java.awt.Color;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.rdg.resc.edal.graphics.style.StyleXMLParser.ColorAdapter;

@XmlType(namespace=ImageData.NAMESPACE, propOrder={"arrowSize", "arrowColor"}, name="ArrowLayerType")
public class ArrowData extends DrawableData {
    @XmlElement
    private Integer arrowSize = 8;
    @XmlElement
    @XmlJavaTypeAdapter(ColorAdapter.class)
    private Color arrowColor = Color.black;
}
