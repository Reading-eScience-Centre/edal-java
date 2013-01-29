package uk.ac.rdg.resc.edal.graphics.style.datamodel.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = ImageData.NAMESPACE, name = "RasterLayerType")
public class RasterData extends DrawableData {
    @XmlElement(name = "DataFieldName", required = true)
    private String dataFieldName;
    @XmlElement(name = "ColourScheme")
    private ColourScheme1DData colourScheme = new ColourScheme1DData();
    
    @SuppressWarnings("unused")
    private RasterData() {}
    
    public RasterData(String dataFieldName, ColourScheme1DData colourScheme) {
        super();
        this.dataFieldName = dataFieldName;
        this.colourScheme = colourScheme;
    }

    public String getDataFieldName() {
        return dataFieldName;
    }

    public ColourScheme1DData getColourScheme() {
        return colourScheme;
    }
}
