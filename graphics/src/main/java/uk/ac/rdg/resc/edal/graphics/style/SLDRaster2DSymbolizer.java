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

	private Node layerNode;
	private ImageLayer imageLayer;
	
	public SLDRaster2DSymbolizer(Node layerNode) throws SLDException {
		try {
			this.layerNode = layerNode;
			imageLayer = parseSymbolizer();
		} catch (Exception e) {
			throw new SLDException(e);
		}
	}

	@Override
	public Node getLayerNode() {
		return layerNode;
	}

	@Override
	public ImageLayer getImageLayer() {
		return imageLayer;
	}

	/*
	 * Parse symbolizer using XPath
	 */
	private ImageLayer parseSymbolizer() throws XPathExpressionException, NumberFormatException {
		// make sure layer is not null an element node
		if (layerNode == null || layerNode.getNodeType() != Node.ELEMENT_NODE) {
			return null;
		}
				
		XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(new SLDNamespaceResolver());
		
		// get name of x data field
		Node xDataFieldNameNode = (Node) xPath.evaluate(
				"./sld:UserStyle/se:CoverageStyle/se:Rule/resc:Raster2DSymbolizer/se:Geometry/resc:XDataFieldName",
				layerNode, XPathConstants.NODE);
		if (xDataFieldNameNode == null) {
			return null;
		}
		String xDataFieldName = xDataFieldNameNode.getTextContent();
		if (xDataFieldName.equals("")) {
			return null;
		}
		
		// get name of y data field
		Node yDataFieldNameNode = (Node) xPath.evaluate(
				"./sld:UserStyle/se:CoverageStyle/se:Rule/resc:Raster2DSymbolizer/se:Geometry/resc:YDataFieldName",
				layerNode, XPathConstants.NODE);
		if (yDataFieldNameNode == null) {
			return null;
		}
		String yDataFieldName = yDataFieldNameNode.getTextContent();
		if (yDataFieldName.equals("")) {
			return null;
		}
		
		// get opacity element if it exists
		Node opacityNode = (Node) xPath.evaluate(
				"./sld:UserStyle/se:CoverageStyle/se:Rule/resc:Raster2DSymbolizer/se:Opacity",
				layerNode, XPathConstants.NODE);
		String opacity;
		if (opacityNode != null) {
			opacity = opacityNode.getTextContent();
		} else {
			opacity = "";
		}
		
		// get the function defining the colour map
		Node function = (Node) xPath.evaluate(
				"./sld:UserStyle/se:CoverageStyle/se:Rule/resc:Raster2DSymbolizer/se:ColorMap/*",
				layerNode, XPathConstants.NODE);
		if (function == null || function.getNodeType() != Node.ELEMENT_NODE) {
			return null;
		}
		
		// get fall back value
		String fallbackValue = (String) xPath.evaluate(
				"./@fallbackValue",
				function, XPathConstants.STRING);
		Color noDataColour;
		if (fallbackValue == null) {
			noDataColour = null;
		} else {
			noDataColour = decodeColour(fallbackValue);
		}
		
		// parse function specific parts of XML for colour scheme
		ColourScheme2D colourScheme2D;
		if (function.getLocalName().equals("Categorize")) {
			// get list of colours
			NodeList colourNodes = (NodeList) xPath.evaluate(
					"./se:Value",
					function, XPathConstants.NODESET);
			if (colourNodes == null) {
				return null;
			}
			// transform to list of Color objects
			ArrayList<Color> colours = new ArrayList<Color>();
			for (int j = 0; j < colourNodes.getLength(); j++) {
				Node colourNode = colourNodes.item(j);
				colours.add(decodeColour(colourNode.getTextContent()));
			}
			
			//get list of x thresholds
			NodeList xThresholdNodes = (NodeList) xPath.evaluate(
					"./resc:XThreshold",
						function, XPathConstants.NODESET);
			if (xThresholdNodes == null) {
				return null;
			}
			// transform to list of Floats
			ArrayList<Float> xThresholds = new ArrayList<Float>();
			for (int j = 0; j < xThresholdNodes.getLength(); j++) {
				Node thresholdNode = xThresholdNodes.item(j);
				xThresholds.add(Float.parseFloat(thresholdNode.getTextContent()));
			}
			
			//get list of y thresholds
			NodeList yThresholdNodes = (NodeList) xPath.evaluate(
					"./resc:YThreshold",
						function, XPathConstants.NODESET);
			if (yThresholdNodes == null) {
				return null;
			}
			// transform to list of Floats
			ArrayList<Float> yThresholds = new ArrayList<Float>();
			for (int j = 0; j < yThresholdNodes.getLength(); j++) {
				Node thresholdNode = yThresholdNodes.item(j);
				yThresholds.add(Float.parseFloat(thresholdNode.getTextContent()));
			}
			
			colourScheme2D = new ThresholdColourScheme2D(xThresholds, yThresholds, colours, noDataColour);
		} else {
			return null;
		}
		
		// instantiate a new raster layer and add it to the image
		Raster2DLayer raster2DLayer = new Raster2DLayer(xDataFieldName, yDataFieldName, colourScheme2D);
		if (!opacity.equals("")) {
			raster2DLayer.setOpacityTransform(new FlatOpacity(Float.parseFloat(opacity)));
		}
		return raster2DLayer;
	}

}
