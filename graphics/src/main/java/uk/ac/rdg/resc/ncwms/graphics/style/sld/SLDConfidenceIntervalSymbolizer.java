package uk.ac.rdg.resc.ncwms.graphics.style.sld;

import static uk.ac.rdg.resc.ncwms.graphics.style.sld.SLDColorMapParser.parseColorMap;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.ConfidenceIntervalLayer;
import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;

public class SLDConfidenceIntervalSymbolizer extends AbstractSLDSymbolizer {

	/*
	 * Parse symbolizer using XPath
	 */
	@Override
	protected ImageLayer parseSymbolizer() throws XPathExpressionException, NumberFormatException, SLDException {
		// get name of upper data field
		Node upperDataFieldNameNode = (Node) xPath.evaluate(
				"./resc:UpperDataFieldName", symbolizerNode, XPathConstants.NODE);
		if (upperDataFieldNameNode == null) {
			throw new SLDException("The upper data field name must be specified.");
		}
		String upperDataFieldName = upperDataFieldNameNode.getTextContent();
		if (upperDataFieldName.equals("")) {
			throw new SLDException("The upper data field name cannot be empty.");
		}
		
		// get name of lower data field
		Node lowerDataFieldNameNode = (Node) xPath.evaluate(
				"./resc:LowerDataFieldName", symbolizerNode, XPathConstants.NODE);
		if (lowerDataFieldNameNode == null) {
			throw new SLDException("The lower data field name must be specified.");
		}
		String lowerDataFieldName = lowerDataFieldNameNode.getTextContent();
		if (lowerDataFieldName.equals("")) {
			throw new SLDException("The lower data field name cannot be empty.");
		}
		
		// get the glyph size
		String glyphSizeText = (String) xPath.evaluate(
				"./resc:GlyphSize", symbolizerNode, XPathConstants.STRING);
		Integer glyphSize = 9;
		if (!(glyphSizeText == null) && ! (glyphSizeText.equals(""))) {
			glyphSize = Integer.parseInt(glyphSizeText);
		}

		ColourScheme colourScheme = parseColorMap(xPath, symbolizerNode);
		
		// instantiate a new confidence interval layer and add it to the image
		ConfidenceIntervalLayer confidenceIntervalLayer = new ConfidenceIntervalLayer(lowerDataFieldName, upperDataFieldName, glyphSize, colourScheme);
		return confidenceIntervalLayer;
	}

}
