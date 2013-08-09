package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = Image.NAMESPACE, propOrder = {}, name = "ColourSchemeType")
public abstract class ColourScheme {
    protected ColourScheme() { }
    
    public abstract Color getColor(Number value);
    
    public abstract Float getScaleMin();
    
    public abstract Float getScaleMax();
}
