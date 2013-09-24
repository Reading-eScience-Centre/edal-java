package uk.ac.rdg.resc.edal.graphics.style.sld;

import static uk.ac.rdg.resc.edal.graphics.style.sld.StyleSLDParser.decodeColour;

import java.awt.Color;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ArrowLayer;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.FlatOpacity;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ImageLayer;

public class SLDArrowSymbolizer implements SLDSymbolizer {

	private String layerName;
	private Node symbolizerNode;
	private ImageLayer imageLayer;

	public SLDArrowSymbolizer(String layerName, Node symbolizerNode) throws SLDException {
		try {
			this.layerName = layerName;
			this.symbolizerNode = symbolizerNode;
			imageLayer = parseSymbolizer();
		} catch (Exception e) {
			throw new SLDException(e);
		}
	}

	@Override
	public String getLayerName() {
		return layerName;
	}

	@Override
	public Node getSymbolizerNode() {
		return symbolizerNode;
	}

	@Override
	public ImageLayer getImageLayer() {
		return imageLayer;
	}

	/*
	 * Parse symbolizer using XPath
	 */
	private ImageLayer parseSymbolizer() throws NumberFormatException, XPathExpressionException, SLDException {
		// make sure layer is not null and an element node
		if (symbolizerNode == null || symbolizerNode.getNodeType() != Node.ELEMENT_NODE) {
			throw new SLDException("The symbolizer node cannot be null and must be an element node.");
		}
				
		XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(new SLDNamespaceResolver());
		
		// get opacity element if it exists
		Node opacityNode = (Node) xPath.evaluate(
				"./se:Opacity", symbolizerNode, XPathConstants.NODE);
		String opacity = null;
		if (opacityNode != null) {
			opacity = opacityNode.getTextContent();
		}
		
		// get the arrow properties
		String arrowSizeText = (String) xPath.evaluate(
				"./resc:ArrowSize", symbolizerNode, XPathConstants.STRING);
		Integer arrowSize = 8;
		if (!(arrowSizeText == null) && !(arrowSizeText.equals(""))) {
			arrowSize = Integer.parseInt(arrowSizeText);
		}
		String arrowColourText = (String) xPath.evaluate(
				"./resc:ArrowColour", symbolizerNode, XPathConstants.STRING);
		Color arrowColour = Color.BLACK;
		if (!(arrowColourText == null) && !(arrowColourText.equals(""))) {
			arrowColour = decodeColour(arrowColourText);
		}
				
		// instantiate a new arrow layer and add it to the image
		ArrowLayer arrowLayer = new ArrowLayer(layerName, arrowSize, arrowColour);
		if (!(opacity == null)) {
			arrowLayer.setOpacityTransform(new FlatOpacity(Float.parseFloat(opacity)));
		}
		return arrowLayer;
	}

}
