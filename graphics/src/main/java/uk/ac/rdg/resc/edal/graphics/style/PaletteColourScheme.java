package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = Image.NAMESPACE, propOrder = {}, name = "PaletteColourSchemeType")
public class PaletteColourScheme extends ColourScheme {

    @XmlElement(name = "ScaleRange")
    ColourScale scaleRange = new ColourScale();

    @XmlElement(name = "ColourMap")
    ColourMap colourPalette = new ColourMap();

    PaletteColourScheme() {}

    public PaletteColourScheme(ColourScale scaleRange, ColourMap colourPalette) {
        super();
        this.scaleRange = scaleRange;
        this.colourPalette = colourPalette;
    }

    @Override
    public Color getColor(Number value) {
        Float zeroToOne = scaleRange.scaleZeroToOne(value);
        return colourPalette.getColor(zeroToOne);
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
