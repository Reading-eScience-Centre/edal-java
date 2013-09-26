package uk.ac.rdg.resc.edal.graphics.style.sld;

import static uk.ac.rdg.resc.edal.graphics.style.sld.StyleSLDParser.addOpacity;
import static uk.ac.rdg.resc.edal.graphics.style.sld.StyleSLDParser.decodeColour;
import static uk.ac.rdg.resc.edal.graphics.style.sld.StyleSLDParser.parseCategorize2D;

import java.awt.Color;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ColourScheme2D;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.Raster2DLayer;

public class SLDRaster2DSymbolizer implements SLDSymbolizer {

	private String layerName;
	private Node symbolizerNode;
	private ImageLayer imageLayer;
	
	public SLDRaster2DSymbolizer(String layerName, Node symbolizerNode) throws SLDException {
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
	private ImageLayer parseSymbolizer() throws XPathExpressionException, NumberFormatException, SLDException {
		// make sure layer is not null an element node
		if (symbolizerNode == null || symbolizerNode.getNodeType() != Node.ELEMENT_NODE) {
			throw new SLDException("The symbolizer node cannot be null and must be an element node.");
		}
				
		XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(new SLDNamespaceResolver());
		
		// get name of x data field
		Node xDataFieldNameNode = (Node) xPath.evaluate(
				"./se:Geometry/resc:XDataFieldName", symbolizerNode, XPathConstants.NODE);
		if (xDataFieldNameNode == null) {
			throw new SLDException("The x data field name must be specified in a geometry tag.");
		}
		String xDataFieldName = xDataFieldNameNode.getTextContent();
		if (xDataFieldName.equals("")) {
			throw new SLDException("The x data field name cannot be empty.");
		}
		
		// get name of y data field
		Node yDataFieldNameNode = (Node) xPath.evaluate(
				"./se:Geometry/resc:YDataFieldName", symbolizerNode, XPathConstants.NODE);
		if (yDataFieldNameNode == null) {
			throw new SLDException("The y data field name must be specified in a geometry tag.");
		}
		String yDataFieldName = yDataFieldNameNode.getTextContent();
		if (yDataFieldName.equals("")) {
			throw new SLDException("The y data field name cannot be empty.");
		}
		
		// get the function defining the colour map
		Node function = (Node) xPath.evaluate(
				"./resc:ColorMap2D/*", symbolizerNode, XPathConstants.NODE);
		if (function == null || function.getNodeType() != Node.ELEMENT_NODE) {
			throw new SLDException("The 2D color map must contain a function.");
		}
		
		// get fall back value
		String fallbackValue = (String) xPath.evaluate(
				"./@fallbackValue", function, XPathConstants.STRING);
		Color noDataColour;
		if (fallbackValue == null) {
			noDataColour = null;
		} else {
			noDataColour = decodeColour(fallbackValue);
		}
		
		// parse function specific parts of XML for colour scheme
		ColourScheme2D colourScheme2D;
		if (function.getLocalName().equals("Categorize2D")) {
			colourScheme2D = parseCategorize2D(xPath, function, noDataColour);
		} else {
			throw new SLDException("The function must be Categorize2D.");
		}
		
		// instantiate a new raster layer and add it to the image
		Raster2DLayer raster2DLayer = new Raster2DLayer(xDataFieldName, yDataFieldName, colourScheme2D);
		addOpacity(xPath, symbolizerNode, raster2DLayer);
		return raster2DLayer;
	}

}
