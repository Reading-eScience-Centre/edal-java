package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.Color;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = Image.NAMESPACE, propOrder = {}, name = "ColourSchemeType")
public class ColourScheme {

    @XmlElement(name = "ScaleRange")
    ColourScale scaleRange = new ColourScale();

    @XmlElement(name = "ColourMap")
    ColourMap colourPalette = new ColourMap();

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
