package uk.ac.rdg.resc.edal.graphics.style;

import static uk.ac.rdg.resc.edal.graphics.style.StyleSLDParser.decodeColour;

import java.awt.Color;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ColourScale;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ContourLayer;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ContourLayer.ContourLineStyle;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.FlatOpacity;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ImageLayer;

public class SLDContourSymbolizer implements SLDSymbolizer {

	private String layerName;
	private Node symbolizerNode;
	private ImageLayer imageLayer;

	public SLDContourSymbolizer(String layerName, Node symbolizerNode) throws SLDException {
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
		String autoscaleEnabledText = (String) xPath.evaluate(
				"./resc:AutoscaleEnabled", symbolizerNode, XPathConstants.STRING);
		Boolean autoscaleEnabled;
		if (autoscaleEnabledText == null) {
			autoscaleEnabled = false;
		} else {
			autoscaleEnabled = Boolean.parseBoolean(autoscaleEnabledText);
		}
		ColourScale scale;
		if (autoscaleEnabled.equals(false)) {
			String scaleMinText = (String) xPath.evaluate(
					"./resc:Scale/resc:ScaleMin", symbolizerNode, XPathConstants.STRING);
			if (scaleMinText == null || scaleMinText.equals("")) {
				throw new SLDException("The scale minimum must be specified in a scale.");
			}
			Float scaleMin = Float.parseFloat(scaleMinText);
			String scaleMaxText = (String) xPath.evaluate(
					"./resc:Scale/resc:ScaleMax", symbolizerNode, XPathConstants.STRING);
			if (scaleMaxText == null || scaleMaxText.equals("")) {
				throw new SLDException("The scale maximum must be specified in a scale.");
			}
			Float scaleMax = Float.parseFloat(scaleMaxText);
			String logarithmicText = (String) xPath.evaluate(
					"./resc:Scale/resc:Logarithmic", symbolizerNode, XPathConstants.STRING);
			Boolean logarithmic = Boolean.parseBoolean(logarithmicText);
			scale = new ColourScale(scaleMin, scaleMax, logarithmic);
		} else {
			scale = null;
		}

		// get the contour properties
		String numberOfContoursText = (String) xPath.evaluate(
				"./resc:NumberOfContours", symbolizerNode, XPathConstants.STRING);
		Integer numberOfContours = 10;
		if (!(numberOfContoursText == null)) {
			numberOfContours = Integer.parseInt(numberOfContoursText);
		}
		String contourLineColourText = (String) xPath.evaluate(
				"./resc:ContourLineColour", symbolizerNode, XPathConstants.STRING);
		Color contourLineColour = Color.BLACK;
		if (!(contourLineColourText == null)) {
			contourLineColour = decodeColour(contourLineColourText);
			if (contourLineColour == null) {
				throw new SLDException("Contour line colour incorrectly formatted.");
			}
		}
		String contourLineWidthText = (String) xPath.evaluate(
				"./resc:ContourLineWidth", symbolizerNode, XPathConstants.STRING);
		Integer contourLineWidth = 1;
		if (!(contourLineWidthText == null)) {
			contourLineWidth = Integer.parseInt(contourLineWidthText);
		}
		String contourLineStyleText = (String) xPath.evaluate(
				"./resc:ContourLineStyle", symbolizerNode, XPathConstants.STRING);
		ContourLineStyle contourLineStyle = ContourLineStyle.DASHED;
		if (!(contourLineStyleText == null)) {
			if (contourLineStyleText.equalsIgnoreCase("DASHED")) {
				contourLineStyle = ContourLineStyle.DASHED;
			} else if (contourLineStyleText.equalsIgnoreCase("HEAVY")) {
				contourLineStyle = ContourLineStyle.HEAVY;
			} else if (contourLineStyleText.equalsIgnoreCase("HIGHLIGHT")) {
				contourLineStyle = ContourLineStyle.HIGHLIGHT;
			} else if (contourLineStyleText.equalsIgnoreCase("MARK")) {
				contourLineStyle = ContourLineStyle.MARK;
			} else if (contourLineStyleText.equalsIgnoreCase("MARK_LINE")) {
				contourLineStyle = ContourLineStyle.MARK_LINE;
			} else if (contourLineStyleText.equalsIgnoreCase("SOLID")) {
				contourLineStyle = ContourLineStyle.SOLID;
			} else if (contourLineStyleText.equalsIgnoreCase("STROKE")) {
				contourLineStyle = ContourLineStyle.STROKE;
			} else {
				throw new SLDException("Contour line style not recognized.");
			}
		}
		String labelEnabledText = (String) xPath.evaluate(
				"./resc:Scale/resc:LabelEnabled", symbolizerNode, XPathConstants.STRING);
		Boolean labelEnabled = true;
		if (!(labelEnabledText == null)) {
			Boolean.parseBoolean(labelEnabledText);
		}
		
		// instantiate a new contour layer and add it to the image
		ContourLayer contourLayer = new ContourLayer(layerName, scale, autoscaleEnabled, numberOfContours, contourLineColour, contourLineWidth, contourLineStyle, labelEnabled);
		if (!(opacity == null)) {
			contourLayer.setOpacityTransform(new FlatOpacity(Float.parseFloat(opacity)));
		}
		return contourLayer;
	}

}
