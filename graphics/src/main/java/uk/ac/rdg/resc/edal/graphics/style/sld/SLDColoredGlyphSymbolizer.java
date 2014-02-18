package uk.ac.rdg.resc.edal.graphics.style.sld;

import static uk.ac.rdg.resc.edal.graphics.style.sld.SLDColorMapParser.parseColorMap;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.ColouredGlyphLayer;
import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;

public class SLDColoredGlyphSymbolizer extends AbstractSLDSymbolizer1D {

	/*
	 * Parse symbolizer using XPath
	 */
	@Override
	protected ImageLayer parseSymbolizer() throws XPathExpressionException,
			NumberFormatException, SLDException, InstantiationException {
		// get the glyph icon name
		String iconName = (String) xPath.evaluate(
				"./resc:IconName", symbolizerNode, XPathConstants.STRING);
		
		ColourScheme colourScheme = parseColorMap(xPath, symbolizerNode);
		
		// instantiate a new basic glyph layer and add it to the image
		ColouredGlyphLayer glyphLayer = new ColouredGlyphLayer(layerName, iconName, colourScheme);
		return glyphLayer;
	}

}
