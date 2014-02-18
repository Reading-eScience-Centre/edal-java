package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.util.List;

import org.w3c.dom.Node;

import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;

public interface SLDSymbolizer {
	
	/**
	 * Parse the symbolizer node of the SLD XML document when given as a parameter as
	 * well as the list of variable names and return an image layer object.
	 * @param varNames
	 * @param symbolizerNode
	 * @return ImageLayer
	 * @throws SLDException
	 */
	ImageLayer getImageLayer(List<String> varNames, Node symbolizerNode) throws SLDException;

}
