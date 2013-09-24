package uk.ac.rdg.resc.edal.graphics.style.sld;

import org.w3c.dom.Node;

import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ImageLayer;

public interface SLDSymbolizer {
	
	String getLayerName();
	
	Node getSymbolizerNode();
	
	ImageLayer getImageLayer();

}
