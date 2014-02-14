package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.awt.Color;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import uk.ac.rdg.resc.edal.graphics.style.ArrowLayer;
import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;

public class SLDArrowSymbolizer extends AbstractSLDSymbolizer {

	/*
	 * Parse symbolizer using XPath
	 */
	@Override
	protected ImageLayer parseSymbolizer() throws NumberFormatException, XPathExpressionException, SLDException {
		// get the arrow properties
		String arrowSizeText = (String) xPath.evaluate(
				"./resc:ArrowSize", symbolizerNode, XPathConstants.STRING);
		Integer arrowSize = 8;
		if (!(arrowSizeText == null) && !(arrowSizeText.equals(""))) {
			arrowSize = Integer.parseInt(arrowSizeText);
		}
		String arrowColourText = (String) xPath.evaluate(
				"./resc:ArrowColour", symbolizerNode, XPathConstants.STRING);
		Color arrowColour = Color.BLACK;
		if (!(arrowColourText == null) && !(arrowColourText.equals(""))) {
			arrowColour = decodeColour(arrowColourText);
		}
				
		// instantiate a new arrow layer and add it to the image
		ArrowLayer arrowLayer = new ArrowLayer(layerName, arrowSize, arrowColour);
		return arrowLayer;
	}

}
