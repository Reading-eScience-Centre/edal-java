//package uk.ac.rdg.resc.edal.graphics.style;
//
//import javax.xml.bind.Unmarshaller;
//import javax.xml.bind.annotation.XmlType;
//
//import uk.ac.rdg.resc.edal.graphics.style.util.DataReadingTypes.PlotType;
//
//@XmlType(namespace = Image.NAMESPACE, name = "BasicGlyphLayerType")
//public class BasicGlyphLayer extends GlyphLayer {
//    /*
//     * This gets called after being unmarshalled from XML. This reads in the
//     * icon for the glyph.
//     */
//    void afterUnmarshal( Unmarshaller u, Object parent ) {
//    	icon = getIcon(this.glyphIconName);
//    }
//
//   public BasicGlyphLayer() throws InstantiationException {
//		super(PlotType.GLYPH);
//	}
//	
//	public BasicGlyphLayer(String dataFieldName, String glyphIconName,
//			ColourScheme colourScheme) throws InstantiationException {
//		super(PlotType.GLYPH);
//		
//		this.dataFieldName = dataFieldName;
//		this.glyphIconName = glyphIconName;
//		this.colourScheme = colourScheme;	
//	}
//
//}
