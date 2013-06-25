package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.Color;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = Image.NAMESPACE, propOrder = {}, name = "ColourScheme2DType")
public abstract class ColourScheme2D {
    protected ColourScheme2D() { }
    
    public abstract Color getColor(Number xValue, Number yValue);
    
    public abstract Float getScaleMin(int dimension);
    
    public abstract Float getScaleMax(int dimension);
}
