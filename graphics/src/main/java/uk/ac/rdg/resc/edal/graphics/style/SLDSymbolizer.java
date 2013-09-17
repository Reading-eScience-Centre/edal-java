package uk.ac.rdg.resc.edal.graphics.style;

import org.w3c.dom.Node;

import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ImageLayer;

public interface SLDSymbolizer {
	
	Node getLayerNode();
	
	ImageLayer getImageLayer();

}
