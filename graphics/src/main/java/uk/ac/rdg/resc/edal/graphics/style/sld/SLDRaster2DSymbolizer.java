package uk.ac.rdg.resc.edal.graphics.style.sld;

import static uk.ac.rdg.resc.edal.graphics.style.sld.SLDColorMap2DParser.parseColorMap2D;

import javax.xml.xpath.XPathExpressionException;

import uk.ac.rdg.resc.edal.graphics.style.ColourScheme2D;
import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.style.Raster2DLayer;

public class SLDRaster2DSymbolizer extends AbstractSLDSymbolizer {

	/*
	 * Parse symbolizer using XPath
	 */
	@Override
	protected ImageLayer parseSymbolizer() throws XPathExpressionException, NumberFormatException, SLDException {
		// create an object to contain the variable names
		SLDVariables2D variables = new SLDVariables2D();
		
		// parse the colour map
		ColourScheme2D colourScheme2D = parseColorMap2D(xPath, symbolizerNode, variables);
		
		// instantiate a new raster layer and add it to the image
		Raster2DLayer raster2DLayer = new Raster2DLayer(variables.getXVariable(), variables.getYVariable(), colourScheme2D);
		return raster2DLayer;
	}

}
