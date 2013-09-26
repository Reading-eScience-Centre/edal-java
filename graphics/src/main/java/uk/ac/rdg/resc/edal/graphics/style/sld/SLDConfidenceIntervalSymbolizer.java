package uk.ac.rdg.resc.edal.graphics.style.sld;

import static uk.ac.rdg.resc.edal.graphics.style.sld.StyleSLDParser.addOpacity;
import static uk.ac.rdg.resc.edal.graphics.style.sld.StyleSLDParser.decodeColour;
import static uk.ac.rdg.resc.edal.graphics.style.sld.StyleSLDParser.parseCategorize;
import static uk.ac.rdg.resc.edal.graphics.style.sld.StyleSLDParser.parseInterpolate;
import static uk.ac.rdg.resc.edal.graphics.style.sld.StyleSLDParser.parsePalette;

import java.awt.Color;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ConfidenceIntervalLayer;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ImageLayer;

public class SLDConfidenceIntervalSymbolizer implements SLDSymbolizer {

	private String layerName;
	private Node symbolizerNode;
	private ImageLayer imageLayer;
	
	public SLDConfidenceIntervalSymbolizer(String layerName, Node symbolizerNode) throws SLDException {
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
		// make sure layer is not null and an element node
		if (symbolizerNode == null || symbolizerNode.getNodeType() != Node.ELEMENT_NODE) {
			throw new SLDException("The symbolizer node cannot be null and must be an element node.");
		}
				
		XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(new SLDNamespaceResolver());
		
		// get name of upper data field
		Node upperDataFieldNameNode = (Node) xPath.evaluate(
				"./se:Geometry/resc:UpperDataFieldName", symbolizerNode, XPathConstants.NODE);
		if (upperDataFieldNameNode == null) {
			throw new SLDException("The upper data field name must be specified in a geometry tag.");
		}
		String upperDataFieldName = upperDataFieldNameNode.getTextContent();
		if (upperDataFieldName.equals("")) {
			throw new SLDException("The upper data field name cannot be empty.");
		}
		
		// get name of lower data field
		Node lowerDataFieldNameNode = (Node) xPath.evaluate(
				"./se:Geometry/resc:LowerDataFieldName", symbolizerNode, XPathConstants.NODE);
		if (lowerDataFieldNameNode == null) {
			throw new SLDException("The lower data field name must be specified in a geometry tag.");
		}
		String lowerDataFieldName = lowerDataFieldNameNode.getTextContent();
		if (lowerDataFieldName.equals("")) {
			throw new SLDException("The lower data field name cannot be empty.");
		}
		
		// get the glyph size
		String glyphSizeText = (String) xPath.evaluate(
				"./resc:GlyphSize", symbolizerNode, XPathConstants.STRING);
		Integer glyphSize = 9;
		if (!(glyphSizeText == null) && ! (glyphSizeText.equals(""))) {
			glyphSize = Integer.parseInt(glyphSizeText);
		}

		// get the function defining the colour map
		Node function = (Node) xPath.evaluate(
				"./se:ColorMap/*", symbolizerNode, XPathConstants.NODE);
		if (function == null || function.getNodeType() != Node.ELEMENT_NODE) {
			throw new SLDException("The color map must contain a function.");
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
		ColourScheme colourScheme;
		if (function.getLocalName().equals("Categorize")) {
			colourScheme = parseCategorize(xPath, function, noDataColour);
		} else if (function.getLocalName().equals("Interpolate")) {
			colourScheme = parseInterpolate(xPath, function, noDataColour);			
		} else if (function.getLocalName().equals("Palette")) {
			colourScheme = parsePalette(xPath, function, noDataColour);
		} else {
			throw new SLDException("The function must be one of Categorize, Interpolate or Palette.");
		}
		
		// instantiate a new confidence interval layer and add it to the image
		ConfidenceIntervalLayer confidenceIntervalLayer = new ConfidenceIntervalLayer(lowerDataFieldName, upperDataFieldName, glyphSize, colourScheme);
		addOpacity(xPath, symbolizerNode, confidenceIntervalLayer);
		return confidenceIntervalLayer;
	}

}
