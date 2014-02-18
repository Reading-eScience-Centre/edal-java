package uk.ac.rdg.resc.edal.graphics.style.sld;

import static uk.ac.rdg.resc.edal.graphics.style.sld.SLDColorMap2DParser.parseColorMap2D;

import javax.xml.xpath.XPathExpressionException;

import uk.ac.rdg.resc.edal.graphics.style.ColourScheme2D;
import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.style.Raster2DLayer;

public class SLDRaster2DSymbolizer extends AbstractSLDSymbolizer2D {

	/*
	 * Parse symbolizer using XPath
	 */
	@Override
	protected ImageLayer parseSymbolizer() throws XPathExpressionException, NumberFormatException, SLDException {
		// parse the colour map
		ColourScheme2D colourScheme2D = parseColorMap2D(xPath, symbolizerNode);
		
		// instantiate a new raster layer and add it to the image
		Raster2DLayer raster2DLayer = new Raster2DLayer(xVarName, yVarName, colourScheme2D);
		return raster2DLayer;
	}

}
