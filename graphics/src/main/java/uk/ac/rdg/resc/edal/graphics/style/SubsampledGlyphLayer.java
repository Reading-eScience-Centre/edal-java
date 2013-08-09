package uk.ac.rdg.resc.edal.graphics.style;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.util.DataReadingTypes.PlotType;

@XmlType(namespace = Image.NAMESPACE, name = "SubsampledGlyphLayerType")
public class SubsampledGlyphLayer extends GlyphLayer {

    @XmlElement(name = "GlyphSpacing")
	private Float glyphSpacing = 1.5f;
    /*
     * This gets called after being unmarshalled from XML. This reads in the
     * icon for the glyph and sets the glyph spacing as a factor of the icon
     * size.
     */
    void afterUnmarshal( Unmarshaller u, Object parent ) {
    	icon = getIcon(this.glyphIconName);
        
    	if(glyphSpacing < 1.0 || glyphSpacing == null) {
            throw new IllegalArgumentException("Glyph spacing must be non-null and => 1.0");
        }        
    	setXSampleSize((int) (icon.getWidth()*glyphSpacing));
        setYSampleSize((int) (icon.getHeight()*glyphSpacing));
    }

   public SubsampledGlyphLayer() throws InstantiationException {
		super(PlotType.SUBSAMPLE);
	}
	
	public SubsampledGlyphLayer(String dataFieldName, String glyphIconName, float glyphSpacing,
			ColourScheme colourScheme) throws InstantiationException {
		super(PlotType.SUBSAMPLE);
		
		this.dataFieldName = dataFieldName;
		this.glyphIconName = glyphIconName;
		this.glyphSpacing = glyphSpacing;
		this.colourScheme = colourScheme;	
	}

	public float getGlyphSpacing() {
		return glyphSpacing;
	}

}
