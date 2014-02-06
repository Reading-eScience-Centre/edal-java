package uk.ac.rdg.resc.ncwms.graphics.style.sld;

import org.w3c.dom.Node;

import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;

public interface SLDSymbolizer {
	
	/**
	 * Parse the symbolizer node of the SLD XML document when given as a parameter as
	 * well as the name of the SLD layer and return an image layer object.
	 * @param layerName
	 * @param symbolizerNode
	 * @return ImageLayer
	 * @throws SLDException
	 */
	ImageLayer getImageLayer(String layerName, Node symbolizerNode) throws SLDException;

}
