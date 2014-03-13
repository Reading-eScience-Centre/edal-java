package uk.ac.rdg.resc.edal.graphics.style.sld;

import static uk.ac.rdg.resc.edal.graphics.style.sld.SLDDensityMapParser.parseDensityMap;

import javax.xml.xpath.XPathExpressionException;

import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.style.DensityMap;
import uk.ac.rdg.resc.edal.graphics.style.StippleLayer;

public class SLDStippleSymbolizer extends AbstractSLDSymbolizer1D {

	/*
	 * Parse symbolizer using XPath
	 */
	@Override
	protected ImageLayer parseSymbolizer() throws NumberFormatException, XPathExpressionException, SLDException {
		// create the scale
		DensityMap map = parseDensityMap(xPath, symbolizerNode);
				
		// instantiate a new stipple layer and add it to the image
		StippleLayer stippleLayer = new StippleLayer(layerName, map);
		return stippleLayer;
	}

}
