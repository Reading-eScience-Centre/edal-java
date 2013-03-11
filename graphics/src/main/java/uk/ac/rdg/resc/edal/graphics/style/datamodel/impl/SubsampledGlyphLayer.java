package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.DataReadingTypes.PlotType;

@XmlType(namespace = Image.NAMESPACE, name = "SubsampledGlyphLayerType")
public class SubsampledGlyphLayer extends GlyphLayer {

	private float glyphSpacing = 1.5f;
    @XmlElement(name = "GlyphSpacing")
    public void setGlyphSpacing(float glyphSpacing) {
    	this.glyphSpacing = glyphSpacing;
    	setXSampleSize((int) (icon.getWidth()*glyphSpacing));
        setYSampleSize((int) (icon.getHeight()*glyphSpacing));
    }
    
   public SubsampledGlyphLayer() throws InstantiationException {
		super(PlotType.SUBSAMPLE);
	}
	
	public SubsampledGlyphLayer(String dataFieldName, String glyphName, float glyphSpacing,
			ColourScheme colourScheme) throws InstantiationException {
		super(PlotType.SUBSAMPLE);
		
		this.dataFieldName = dataFieldName;
		this.glyphName = glyphName;
		this.glyphSpacing = glyphSpacing;
		this.colourScheme = colourScheme;	
	}

	public float getGlyphSpacing() {
		return glyphSpacing;
	}

}
