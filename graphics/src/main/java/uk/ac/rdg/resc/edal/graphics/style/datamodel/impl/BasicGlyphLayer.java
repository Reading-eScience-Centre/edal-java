package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.DataReadingTypes.PlotType;

@XmlType(namespace = Image.NAMESPACE, name = "BasicGlyphLayerType")
public class BasicGlyphLayer extends GlyphLayer {

   public BasicGlyphLayer() throws InstantiationException {
		super(PlotType.GLYPH);
	}
	
	public BasicGlyphLayer(String dataFieldName, String glyphName,
			ColourScheme colourScheme) throws InstantiationException {
		super(PlotType.GLYPH);
		
		this.dataFieldName = dataFieldName;
		this.glyphName = glyphName;
		this.colourScheme = colourScheme;	
	}

}
