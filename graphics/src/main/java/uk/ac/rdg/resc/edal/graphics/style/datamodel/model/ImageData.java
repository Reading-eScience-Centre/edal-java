package uk.ac.rdg.resc.edal.graphics.style.datamodel.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace=ImageData.NAMESPACE, name="ImageType")
@XmlRootElement(namespace=ImageData.NAMESPACE, name="Image")
public class ImageData extends DrawableData {
    /*
     * This is the namespace for the XML.
     * 
     * IF YOU CHANGE IT, YOU NEED TO MODIFY pom.xml AS WELL
     */
    public static final String NAMESPACE="http://www.resc.reading.ac.uk";
    
    @XmlElements({
        @XmlElement(name="Image", type = ImageData.class),
        @XmlElement(name="ArrowLayer", type = ArrowData.class),
        @XmlElement(name="RasterLayer", type = RasterData.class)
    })
    private List<DrawableData> layers = new ArrayList<DrawableData>();

    public void addLayer(DrawableData layer) {
        layers.add(layer);
    }
}