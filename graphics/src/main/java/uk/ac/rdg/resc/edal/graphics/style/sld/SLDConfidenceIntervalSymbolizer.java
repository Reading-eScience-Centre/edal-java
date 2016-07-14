package uk.ac.rdg.resc.edal.graphics.style.sld;

import static uk.ac.rdg.resc.edal.graphics.style.sld.SLDColorMapParser.parseColorMap;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.ConfidenceIntervalLayer;
import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;

public class SLDConfidenceIntervalSymbolizer extends AbstractSLDSymbolizer2D {

	/*
	 * Parse symbolizer using XPath
	 */
	@Override
	protected ImageLayer parseSymbolizer() throws XPathExpressionException, NumberFormatException, SLDException {
		// get the glyph size
		String glyphSizeText = (String) xPath.evaluate(
				"./resc:GlyphSize", symbolizerNode, XPathConstants.STRING);
		Integer glyphSize = 9;
		if (!(glyphSizeText == null) && ! (glyphSizeText.equals(""))) {
			glyphSize = Integer.parseInt(glyphSizeText.trim());
		}

		ColourScheme colourScheme = parseColorMap(xPath, symbolizerNode);
		
		// instantiate a new confidence interval layer and add it to the image
		ConfidenceIntervalLayer confidenceIntervalLayer = new ConfidenceIntervalLayer(xVarName, yVarName, glyphSize, colourScheme);
		return confidenceIntervalLayer;
	}

}
