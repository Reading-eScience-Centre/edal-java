package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.Color;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.graphics.style.Palette1D;
import uk.ac.rdg.resc.edal.graphics.style.StyleXMLParser.ColorAdapter;

@XmlType(namespace = Image.NAMESPACE, propOrder = {}, name = "ColourSchemeType")
public class ColourScheme {

    @XmlElement(name = "ScaleRange")
    private ColourScale scaleRange;

    @XmlElement(name = "ColourPalette")
    private ColourPalette colourPalette;

    ColourScheme() {
    }

    public Color getColor(Number value) {
        Float zeroToOne = scaleRange.scaleZeroToOne(value);
        return colourPalette.getColor(zeroToOne);
    }
}
