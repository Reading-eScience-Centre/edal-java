package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.Color;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.graphics.style.ColourPalette;
import uk.ac.rdg.resc.edal.graphics.style.StyleXMLParser.ColorAdapter;

@XmlType(namespace = Image.NAMESPACE, propOrder = {}, name = "ColourSchemeType")
public class ColourScheme {

    @XmlElement(name = "ScaleRange")
    private ColourScale scaleRange = new ColourScale();

    @XmlElement(name = "ColourMap")
    private ColourMap colourPalette = new ColourMap();

    ColourScheme() {}

    public ColourScheme(ColourScale scaleRange, ColourMap colourPalette) {
        super();
        this.scaleRange = scaleRange;
        this.colourPalette = colourPalette;
    }

    public Color getColor(Number value) {
        Float zeroToOne = scaleRange.scaleZeroToOne(value);
        return colourPalette.getColor(zeroToOne);
    }
}
