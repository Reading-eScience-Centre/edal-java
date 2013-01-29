package uk.ac.rdg.resc.edal.graphics.style.datamodel.model;

import java.awt.Color;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.rdg.resc.edal.graphics.style.StyleXMLParser.ColorAdapter;

@XmlType(namespace = ImageData.NAMESPACE, propOrder = { "directionFieldName", "arrowSize",
        "arrowColour" }, name = "ArrowLayerType")
public class ArrowData extends DrawableData {
    @XmlElement(name = "DirectionFieldName", required = true)
    private String directionFieldName;
    @XmlElement(name = "ArrowSize")
    private Integer arrowSize = 8;
    @XmlElement(name = "ArrowColour")
    @XmlJavaTypeAdapter(ColorAdapter.class)
    private Color arrowColour = Color.black;
    
    @SuppressWarnings("unused")
    private ArrowData(){}
    
    public ArrowData(String directionFieldName, Integer arrowSize, Color arrowColour) {
        super();
        this.directionFieldName = directionFieldName;
        this.arrowSize = arrowSize;
        this.arrowColour = arrowColour;
    }

    public String getDirectionFieldName() {
        return directionFieldName;
    }

    public Integer getArrowSize() {
        return arrowSize;
    }

    public Color getArrowColour() {
        return arrowColour;
    }
}
