package uk.ac.rdg.resc.edal.graphics.style.sld;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.FlatOpacity;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.PatternScale;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.StippleLayer;

public class SLDStippleSymbolizer implements SLDSymbolizer {

	private String layerName;
	private Node symbolizerNode;
	private ImageLayer imageLayer;

	public SLDStippleSymbolizer(String layerName, Node symbolizerNode) throws SLDException {
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
		
		// create the scale
		String patternBandsText = (String) xPath.evaluate(
				"./resc:PatternScale/resc:PatternBands", symbolizerNode, XPathConstants.STRING);
		Integer patternBands = 10;
		if (!(patternBandsText == null) && !(patternBandsText.equals(""))) {
			patternBands = Integer.parseInt(patternBandsText);
		}
		String transparentValueText = (String) xPath.evaluate(
				"./resc:PatternScale/resc:TransparentValue", symbolizerNode, XPathConstants.STRING);
		if (transparentValueText == null || transparentValueText.equals("")) {
			throw new SLDException("The transparent value must be specified in a pattern scale.");
		}
		Float transparentValue = Float.parseFloat(transparentValueText);
		String opaqueValueText = (String) xPath.evaluate(
				"./resc:PatternScale/resc:OpaqueValue", symbolizerNode, XPathConstants.STRING);
		if (opaqueValueText == null || opaqueValueText.equals("")) {
			throw new SLDException("The opaque value must be specified in a pattern scale.");
		}
		Float opaqueValue = Float.parseFloat(opaqueValueText);
		String logarithmicText = (String) xPath.evaluate(
				"./resc:PatternScale/resc:Logarithmic", symbolizerNode, XPathConstants.STRING);
		Boolean logarithmic = Boolean.parseBoolean(logarithmicText);
		PatternScale scale = new PatternScale(patternBands, transparentValue, opaqueValue, logarithmic);
				
		// instantiate a new stipple layer and add it to the image
		StippleLayer stippleLayer = new StippleLayer(layerName, scale);
		if (!(opacity == null)) {
			stippleLayer.setOpacityTransform(new FlatOpacity(Float.parseFloat(opacity)));
		}
		return stippleLayer;
	}

}
