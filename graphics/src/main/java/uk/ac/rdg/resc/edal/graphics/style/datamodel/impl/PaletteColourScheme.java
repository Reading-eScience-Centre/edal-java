package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.Color;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = Image.NAMESPACE, propOrder = {}, name = "PaletteColourSchemeType")
public class PaletteColourScheme extends ColourScheme {

    @XmlElement(name = "ScaleRange")
    ColourScale scaleRange = new ColourScale();

    @XmlElement(name = "ColourMap")
    ColourMap colourMap = new ColourMap();

    PaletteColourScheme() {}

    public PaletteColourScheme(ColourScale scaleRange, ColourMap colourMap) {
        super();
        this.scaleRange = scaleRange;
        this.colourMap = colourMap;
    }

    @Override
    public Color getColor(Number value) {
        Float zeroToOne = scaleRange.scaleZeroToOne(value);
        return colourMap.getColor(zeroToOne);
    }

    @Override
    public Float getScaleMin() {
        return scaleRange.getScaleMin();
    }

    @Override
    public Float getScaleMax() {
        return scaleRange.getScaleMax();
    }
}
