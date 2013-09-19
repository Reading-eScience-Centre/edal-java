package uk.ac.rdg.resc.edal.graphics.style;

import static uk.ac.rdg.resc.edal.graphics.style.StyleSLDParser.decodeColour;

import java.awt.Color;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ColourScheme2D;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.FlatOpacity;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.Raster2DLayer;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ThresholdColourScheme2D;

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
		
		// get opacity element if it exists
		Node opacityNode = (Node) xPath.evaluate(
				"./se:Opacity", symbolizerNode, XPathConstants.NODE);
		String opacity = null;
		if (opacityNode != null) {
			opacity = opacityNode.getTextContent();
		}

		// get the function defining the colour map
		Node function = (Node) xPath.evaluate(
				"./resc:ColorMap2D/*", symbolizerNode, XPathConstants.NODE);
		if (function == null || function.getNodeType() != Node.ELEMENT_NODE) {
			throw new SLDException("The raster symbolizer must contain a function.");
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
		if (!(opacity == null)) {
			raster2DLayer.setOpacityTransform(new FlatOpacity(Float.parseFloat(opacity)));
		}
		return raster2DLayer;
	}

	private ColourScheme2D parseCategorize2D(XPath xPath, Node function,
			Color noDataColour) throws XPathExpressionException, SLDException {
		ColourScheme2D colourScheme2D;

		// get list of colours
		NodeList colourNodes = (NodeList) xPath.evaluate(
				"./se:Value", function, XPathConstants.NODESET);
		if (colourNodes == null) {
			throw new SLDException("The 2D categorize function must contain a list of values.");
		}
		
		// transform to list of Color objects
		ArrayList<Color> colours = new ArrayList<Color>();
		for (int j = 0; j < colourNodes.getLength(); j++) {
			Node colourNode = colourNodes.item(j);
			colours.add(decodeColour(colourNode.getTextContent()));
		}
		
		//get list of x thresholds
		NodeList xThresholdNodes = (NodeList) xPath.evaluate(
				"./resc:XThreshold", function, XPathConstants.NODESET);
		if (xThresholdNodes == null) {
			throw new SLDException("The 2D categorize function must contain a list of x thresholds.");
		}

		// transform to list of Floats
		ArrayList<Float> xThresholds = new ArrayList<Float>();
		for (int j = 0; j < xThresholdNodes.getLength(); j++) {
			Node thresholdNode = xThresholdNodes.item(j);
			xThresholds.add(Float.parseFloat(thresholdNode.getTextContent()));
		}
		
		//get list of y thresholds
		NodeList yThresholdNodes = (NodeList) xPath.evaluate(
				"./resc:YThreshold", function, XPathConstants.NODESET);
		if (yThresholdNodes == null) {
			throw new SLDException("The 2D categorize function must contain a list of y thresholds.");
		}
		// transform to list of Floats
		ArrayList<Float> yThresholds = new ArrayList<Float>();
		for (int j = 0; j < yThresholdNodes.getLength(); j++) {
			Node thresholdNode = yThresholdNodes.item(j);
			yThresholds.add(Float.parseFloat(thresholdNode.getTextContent()));
		}
		
		colourScheme2D = new ThresholdColourScheme2D(xThresholds, yThresholds, colours, noDataColour);
		return colourScheme2D;
	}

}
