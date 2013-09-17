package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ColourMap;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ColourScale;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.FlatOpacity;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.InterpolateColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.PaletteColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.RasterLayer;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ThresholdColourScheme;
import static uk.ac.rdg.resc.edal.graphics.style.StyleSLDParser.decodeColour;

public class SLDRasterSymbolizer implements SLDSymbolizer {

	private Node layerNode;
	private ImageLayer imageLayer;
	
	public SLDRasterSymbolizer(Node layerNode) throws XPathExpressionException {
		this.layerNode = layerNode;
		imageLayer = parseSymbolizer();
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
	private ImageLayer parseSymbolizer() throws XPathExpressionException {
		// make sure layer is not null an element node
		if (layerNode == null || layerNode.getNodeType() != Node.ELEMENT_NODE) {
			return null;
		}
				
		XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(new SLDNamespaceResolver());
		
		// get name of data field
		Node nameNode = (Node) xPath.evaluate(
				"./se:Name", layerNode, XPathConstants.NODE);
		if (nameNode == null) {
			return null;
		}
		String name = nameNode.getTextContent();
		if (name.equals("")) {
			return null;
		}
		
		// get opacity element if it exists
		Node opacityNode = (Node) xPath.evaluate(
				"./sld:UserStyle/se:CoverageStyle/se:Rule/se:RasterSymbolizer/se:Opacity",
				layerNode, XPathConstants.NODE);
		String opacity;
		if (opacityNode != null) {
			opacity = opacityNode.getTextContent();
		} else {
			opacity = "";
		}
		
		// get the function defining the colour map
		Node function = (Node) xPath.evaluate(
				"./sld:UserStyle/se:CoverageStyle/se:Rule/se:RasterSymbolizer/se:ColorMap/*",
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
		ColourScheme colourScheme;
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
			
			//get list of thresholds
			NodeList thresholdNodes = (NodeList) xPath.evaluate(
					"./se:Threshold",
						function, XPathConstants.NODESET);
			if (thresholdNodes == null) {
				return null;
			}
			// transform to list of Floats
			ArrayList<Float> thresholds = new ArrayList<Float>();
			for (int j = 0; j < thresholdNodes.getLength(); j++) {
				Node thresholdNode = thresholdNodes.item(j);
				thresholds.add(Float.parseFloat(thresholdNode.getTextContent()));
			}
			
			colourScheme = new ThresholdColourScheme(thresholds, colours, noDataColour);
		} else if (function.getLocalName().equals("Interpolate")) {
			// get list of data points
			NodeList pointNodes = (NodeList) xPath.evaluate(
					"./se:InterpolationPoint",
						function, XPathConstants.NODESET);
			if (pointNodes == null) {
				return null;
			}
			// parse into an ArrayList
			ArrayList<InterpolateColourScheme.InterpolationPoint> points =
					new ArrayList<InterpolateColourScheme.InterpolationPoint>();
			for (int j = 0; j < pointNodes.getLength(); j++) {
				Node pointNode = pointNodes.item(j);
				Node dataNode = (Node) xPath.evaluate(
						"./se:Data",
						pointNode, XPathConstants.NODE);
				if (dataNode == null) {
					return null;
				}
				Node valueNode = (Node) xPath.evaluate(
						"./se:Value",
						pointNode, XPathConstants.NODE);
				if (valueNode == null) {
					return null;
				}
				points.add(new InterpolateColourScheme.InterpolationPoint(
						Float.parseFloat(dataNode.getTextContent()),
						decodeColour(valueNode.getTextContent())));
			}
			if (points.size() < 1) {
				return null;
			}
			// create a new InterpolateColourScheme object
			colourScheme = new InterpolateColourScheme(points, noDataColour);			
		} else if (function.getLocalName().equals("Palette")) {
			// Create the colour map
			String paletteDefinition = (String) xPath.evaluate(
					"./resc:PaletteDefinition",
					function, XPathConstants.STRING);
			if (paletteDefinition == null || paletteDefinition.equals("")) {
				return null;
			}
			String nColourBandsText = (String) xPath.evaluate(
					"./resc:NumberOfColorBands",
					function, XPathConstants.STRING);
			Integer nColourBands;
			if (nColourBandsText == null || nColourBandsText.equals("")) {
				nColourBands = 254;
			} else {
				nColourBands = Integer.parseInt(nColourBandsText);
			}
			String belowMinColourText = (String) xPath.evaluate(
					"./resc:BelowMinColor",
					function, XPathConstants.STRING);
			Color belowMinColour = decodeColour(belowMinColourText);
			String aboveMaxColourText = (String) xPath.evaluate(
					"./resc:AboveMaxColor",
					function, XPathConstants.STRING);
			Color aboveMaxColour = decodeColour(aboveMaxColourText);
			ColourMap colourMap = new ColourMap(belowMinColour, aboveMaxColour, noDataColour, paletteDefinition, nColourBands);
			
			// Create the colour scale
			String scaleMinText = (String) xPath.evaluate(
					"./resc:ColorScale/resc:ScaleMin",
					function, XPathConstants.STRING);
			if (scaleMinText == null || scaleMinText.equals("")) {
				return null;
			}
			Float scaleMin = Float.parseFloat(scaleMinText);
			String scaleMaxText = (String) xPath.evaluate(
					"./resc:ColorScale/resc:ScaleMax",
					function, XPathConstants.STRING);
			if (scaleMaxText == null || scaleMaxText.equals("")) {
				return null;
			}
			Float scaleMax = Float.parseFloat(scaleMaxText);
			String logarithmicText = (String) xPath.evaluate(
					"./resc:ColorScale/resc:Logarithmic",
					function, XPathConstants.STRING);
			Boolean logarithmic;
			if (logarithmicText == null || logarithmicText.equals("")) {
				logarithmic = false;
			} else {
				logarithmic = Boolean.parseBoolean(logarithmicText);
			}
			ColourScale colourScale = new ColourScale(scaleMin, scaleMax, logarithmic);
			
			// Create the colour scheme
			colourScheme = new PaletteColourScheme(colourScale, colourMap);
		} else {
			return null;
		}
		
		// instantiate a new raster layer and add it to the image
		RasterLayer rasterLayer = new RasterLayer(name, colourScheme);
		if (!opacity.equals("")) {
			rasterLayer.setOpacityTransform(new FlatOpacity(Float.parseFloat(opacity)));
		}
		return rasterLayer;
	}

}
