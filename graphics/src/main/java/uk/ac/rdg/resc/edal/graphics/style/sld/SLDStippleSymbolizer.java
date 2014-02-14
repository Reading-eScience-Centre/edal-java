package uk.ac.rdg.resc.edal.graphics.style.sld;

import static uk.ac.rdg.resc.edal.graphics.style.sld.SLDOpacityMapParser.parseOpacityMap;

import javax.xml.xpath.XPathExpressionException;

import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.style.PatternScale;
import uk.ac.rdg.resc.edal.graphics.style.StippleLayer;

public class SLDStippleSymbolizer extends AbstractSLDSymbolizer {

	/*
	 * Parse symbolizer using XPath
	 */
	@Override
	protected ImageLayer parseSymbolizer() throws NumberFormatException, XPathExpressionException, SLDException {
		// create the scale
		PatternScale scale = parseOpacityMap(xPath, symbolizerNode);
				
		// instantiate a new stipple layer and add it to the image
		StippleLayer stippleLayer = new StippleLayer(layerName, scale);
		return stippleLayer;
	}

}
