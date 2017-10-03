package uk.ac.rdg.resc.edal.graphics.style.sld;

import javax.xml.xpath.XPathExpressionException;

import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.ColouredTrajectoryLayer;
import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;

public class SLDTrajectorySymbolizer extends AbstractSLDSymbolizer1D {

	/*
	 * Parse symbolizer using XPath
	 */
	@Override
	protected ImageLayer parseSymbolizer() throws XPathExpressionException, NumberFormatException, SLDException {
		ColourScheme colourScheme = SLDColorMapParser.parseColorMap(xPath, symbolizerNode);
		
		// instantiate a new trajectory layer and add it to the image
		ColouredTrajectoryLayer trajectoryLayer = new ColouredTrajectoryLayer(layerName, colourScheme);
		return trajectoryLayer;
	}

}
