package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.Color;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.rdg.resc.edal.graphics.style.ColourPalette;
import uk.ac.rdg.resc.edal.graphics.style.StyleXMLParser.ColorAdapter;

@XmlType(namespace = Image.NAMESPACE, name="ColourPaletteType")
public class ColourMap {
    // The colour to plot for values below the minimum. If null, then use the
    // lowest value in the palette
    @XmlElement(name = "BelowMinColour")
    @XmlJavaTypeAdapter(ColorAdapter.class)
    private Color belowMinColour = null;
    // The colour to plot for values above the maximum. If null, then use the
    // highest value in the palette
    @XmlElement(name = "AboveMaxColour")
    @XmlJavaTypeAdapter(ColorAdapter.class)
    private Color aboveMaxColour = null;
    // The colour to plot for missing data
    @XmlElement(name = "MissingDataColour")
    @XmlJavaTypeAdapter(ColorAdapter.class)
    private Color noDataColour = new Color(0, 0, 0, 0);
    
    // The colour to plot for missing data
    @XmlElement(name = "NumberOfColourBands")
    private Integer nColourBands = 254;
    
    @XmlElement(name = "Palette")
    private String paletteString = "default";
    
    private ColourPalette palette = null;
    
    ColourMap(){}
    
    public ColourMap(Color belowMinColour, Color aboveMaxColour, Color noDataColour,
            String palette, Integer nColourBands) {
        super();
        this.belowMinColour = belowMinColour;
        this.aboveMaxColour = aboveMaxColour;
        this.noDataColour = noDataColour;
        this.nColourBands = nColourBands;
        this.paletteString = palette;
    }

    public Color getColor(Number value) {
        if(palette == null) {
            palette = ColourPalette.fromString(paletteString, nColourBands);
        }
        if (value == null || Float.isNaN(value.floatValue())) {
            return noDataColour;
        }
        float val = value.floatValue();
        if (val < 0.0) {
            if(belowMinColour == null) {
                return palette.getColor(0f);
            }
            return belowMinColour;
        }
        if (val > 1.0) {
            if(aboveMaxColour == null) {
                return palette.getColor(1f);
            }
            return aboveMaxColour;
        }
        return palette.getColor(val);
    }
}
