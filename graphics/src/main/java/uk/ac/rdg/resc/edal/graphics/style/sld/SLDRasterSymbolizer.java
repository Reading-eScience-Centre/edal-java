package uk.ac.rdg.resc.edal.graphics.style.sld;

import javax.xml.xpath.XPathExpressionException;

import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.style.RasterLayer;

public class SLDRasterSymbolizer extends AbstractSLDSymbolizer1D {

	/*
	 * Parse symbolizer using XPath
	 */
	@Override
	protected ImageLayer parseSymbolizer() throws XPathExpressionException, NumberFormatException, SLDException {
		ColourScheme colourScheme = SLDColorMapParser.parseColorMap(xPath, symbolizerNode);
		
		// instantiate a new raster layer and add it to the image
		RasterLayer rasterLayer = new RasterLayer(layerName, colourScheme);
		return rasterLayer;
	}

}
