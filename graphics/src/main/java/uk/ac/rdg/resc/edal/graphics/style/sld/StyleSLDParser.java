package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ColourMap;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ColourScale;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ColourScheme2D;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.FlatOpacity;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.Image;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.InterpolateColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.LinearOpacity;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.PaletteColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ThresholdColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ThresholdColourScheme2D;

/**
 * Reads in an XML file encoded with Styled Layer Descriptor and Symbology
 * Encoding and parses the document to create a corresponding image.
 */
public class StyleSLDParser {

	public static final String OUTPUT_ENCODING = "UTF-8";
	public static final String JAXP_SCHEMA_LANGUAGE =
			"http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	public static final String W3C_XML_SCHEMA =
			"http://www.w3.org/2001/XMLSchema";
	public static final String JAXP_SCHEMA_SOURCE =
			"http://java.sun.com/xml/jaxp/properties/schemaSource";
	public static final String SLD_SCHEMA =
			"http://schemas.opengis.net/sld/1.1.0/StyledLayerDescriptor.xsd";
	
	/**
	 * Create an image given an XML file containing an SLD document.
	 * @param xmlFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws SLDException
	 */
	public static Image createImage(File xmlFile) throws FileNotFoundException, SLDException {
		try {
			Document xmlDocument = readXMLFile(xmlFile);
			Image image = parseSLD(xmlDocument);
			return image;
		} catch (FileNotFoundException fnfe) {
			throw fnfe;
		} catch (Exception e) {
			throw new SLDException(e);
		}
	}
	
	/*
	 *  Parse the document using XPath and create a corresponding image 
	 */
	private static Image parseSLD(Document xmlDocument) throws XPathExpressionException, SLDException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(new SLDNamespaceResolver());

		// Instantiate a new Image object
		Image image = new Image();

		// Get all the named layers in the document and loop through each one
		NodeList namedLayers = (NodeList) xPath.evaluate(
				"/sld:StyledLayerDescriptor/sld:NamedLayer", xmlDocument,
				XPathConstants.NODESET);
		if (!(namedLayers.getLength() > 0)) {
			throw new SLDException("There must be at least one named layer.");
		}
		for (int i = 0; i < namedLayers.getLength(); i++) {
			
			// get the layer node and check it is an element node
			Node layerNode = namedLayers.item(i);
			if (layerNode.getNodeType() != Node.ELEMENT_NODE) {
				throw new SLDException("Named layer no. " + (i + 1) + " is not an element node.");
			}
			
			// get name of the layer
			Node nameNode = (Node) xPath.evaluate(
					"./se:Name", layerNode, XPathConstants.NODE);
			if (nameNode == null) {
				throw new SLDException("The layer must be named.");
			}
			String layerName = nameNode.getTextContent();
			if (layerName.equals("")) {
				throw new SLDException("The name of the layer cannot be empty.");
			}
			
			// get the children of the first rule and check that there is exactly one child
			NodeList symbolizers = (NodeList) xPath.evaluate(
					"./sld:UserStyle/se:CoverageStyle/se:Rule/*",
					layerNode, XPathConstants.NODESET);
			if (symbolizers.getLength() != 1) {
				throw new SLDException("There must be exactly one symbolizer within a coverage style.");
			}
			Node symbolizerNode = symbolizers.item(0);

			// parse the symbolizer
			SLDSymbolizer sldSymbolizer;
			if (symbolizerNode.getLocalName().equals("RasterSymbolizer")) {
				sldSymbolizer = new SLDRasterSymbolizer(layerName, symbolizerNode);
			} else if (symbolizerNode.getLocalName().equals("Raster2DSymbolizer")) {
				sldSymbolizer = new SLDRaster2DSymbolizer(layerName, symbolizerNode);
			} else if (symbolizerNode.getLocalName().equals("ContourSymbolizer")){
				sldSymbolizer = new SLDContourSymbolizer(layerName, symbolizerNode);
			} else if (symbolizerNode.getLocalName().equals("SmoothedContourSymbolizer")){
				sldSymbolizer = new SLDSmoothedContourSymbolizer(layerName, symbolizerNode);
			} else if (symbolizerNode.getLocalName().equals("StippleSymbolizer")){
				sldSymbolizer = new SLDStippleSymbolizer(layerName, symbolizerNode);
			} else if (symbolizerNode.getLocalName().equals("ArrowSymbolizer")){
				sldSymbolizer = new SLDArrowSymbolizer(layerName, symbolizerNode);
			} else if (symbolizerNode.getLocalName().equals("BasicGlyphSymbolizer")){
				sldSymbolizer = new SLDBasicGlyphSymbolizer(layerName, symbolizerNode);
			} else if (symbolizerNode.getLocalName().equals("SubsampledGlyphSymbolizer")){
				sldSymbolizer = new SLDSubsampledGlyphSymbolizer(layerName, symbolizerNode);
			} else if (symbolizerNode.getLocalName().equals("ConfidenceIntervalSymbolizer")){
				sldSymbolizer = new SLDConfidenceIntervalSymbolizer(layerName, symbolizerNode);
			} else {
				throw new SLDException("Symbolizer type not recognized.");
			}
			
			// add the resulting image layer to the image
			ImageLayer imageLayer = sldSymbolizer.getImageLayer();
			if (imageLayer != null) {
				image.getLayers().add(imageLayer);
			}

		}
		
		// check that the image has layers and if so return it
		if (image.getLayers().size() > 0) {
			return image;
		} else {
			throw new SLDException("No image layers have been parsed successfully.");
		}
	}
	
	/*
	 *  Read in and parse an XML file to a Document object. The builder factory is
	 *  configured to be namespace aware and validating. The class SAXErrorHandler
	 *  is used to handle validation errors. The schema is forced to be the SLD
	 *  schema v1.1.0.
	 */
	private static Document readXMLFile(File xmlFile) throws ParserConfigurationException,
			FileNotFoundException, SAXException, IOException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
	//	Uncomment to turn on schema validation
	//	builderFactory.setValidating(true);
		try {
			builderFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
		} catch (IllegalArgumentException iae) {
			throw new IllegalArgumentException("Error: JAXP DocumentBuilderFactory "
					+ "attribute not recognized: " + JAXP_SCHEMA_LANGUAGE + "\n"
					+ "Check to see if parser conforms to JAXP spec.");
		}
		builderFactory.setAttribute(JAXP_SCHEMA_SOURCE, SLD_SCHEMA);
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		OutputStreamWriter errorWriter = new OutputStreamWriter(System.err,
				OUTPUT_ENCODING);
		builder.setErrorHandler(new SAXErrorHandler(new PrintWriter(errorWriter, true)));
		Document xmlDocument = builder.parse(new FileInputStream(xmlFile));
		return xmlDocument;
	}

	/**
	 * Decode a string representing a colour in both the case when there is an opacity or not
	 * @param s
	 * @return
	 */
	public static Color decodeColour(String s) {
        if (s.length() == 7) {
            return Color.decode(s);
        } else if (s.length() == 9) {
            Color color = Color.decode("#"+s.substring(3));
            int alpha = Integer.parseInt(s.substring(1,3), 16);
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        } else {
            return null;
        }
    }

	public static ColourScheme parseCategorize(XPath xPath, Node function,
			Color noDataColour) throws XPathExpressionException, NumberFormatException, SLDException {
		ColourScheme colourScheme;

		// get list of colours
		NodeList colourNodes = (NodeList) xPath.evaluate(
				"./se:Value", function, XPathConstants.NODESET);
		if (colourNodes == null) {
			throw new SLDException("The categorize function must contain a list ov values.");
		}
		
		// transform to list of Color objects
		ArrayList<Color> colours = new ArrayList<Color>();
		for (int j = 0; j < colourNodes.getLength(); j++) {
			Node colourNode = colourNodes.item(j);
			colours.add(decodeColour(colourNode.getTextContent()));
		}
		
		//get list of thresholds
		NodeList thresholdNodes = (NodeList) xPath.evaluate(
				"./se:Threshold", function, XPathConstants.NODESET);
		if (thresholdNodes == null) {
			throw new SLDException("The categorize function must contain a list of thresholds.");
		}
		
		// transform to list of Floats
		ArrayList<Float> thresholds = new ArrayList<Float>();
		for (int j = 0; j < thresholdNodes.getLength(); j++) {
			Node thresholdNode = thresholdNodes.item(j);
			thresholds.add(Float.parseFloat(thresholdNode.getTextContent()));
		}
		
		colourScheme = new ThresholdColourScheme(thresholds, colours, noDataColour);
		return colourScheme;
	}

	public static ColourScheme parseInterpolate(XPath xPath, Node function,
			Color noDataColour) throws XPathExpressionException, NumberFormatException, SLDException {
		ColourScheme colourScheme;

		// get list of data points
		NodeList pointNodes = (NodeList) xPath.evaluate(
				"./se:InterpolationPoint", function, XPathConstants.NODESET);
		if (pointNodes == null) {
			throw new SLDException("The interpolate function must contain a list of interpolation points.");
		}
		
		// parse into an ArrayList
		ArrayList<InterpolateColourScheme.InterpolationPoint> points =
				new ArrayList<InterpolateColourScheme.InterpolationPoint>();
		for (int j = 0; j < pointNodes.getLength(); j++) {
			Node pointNode = pointNodes.item(j);
			Node dataNode = (Node) xPath.evaluate(
					"./se:Data", pointNode, XPathConstants.NODE);
			if (dataNode == null) {
				throw new SLDException("Each interpolation point must contain a data element.");
			}
			Node valueNode = (Node) xPath.evaluate(
					"./se:Value", pointNode, XPathConstants.NODE);
			if (valueNode == null) {
				throw new SLDException("Each interpolation point must contain a value element.");
			}
			points.add(new InterpolateColourScheme.InterpolationPoint(
					Float.parseFloat(dataNode.getTextContent()),
					decodeColour(valueNode.getTextContent())));
		}

		// create a new InterpolateColourScheme object
		colourScheme = new InterpolateColourScheme(points, noDataColour);
		return colourScheme;
	}

	public static ColourScheme parsePalette(XPath xPath, Node function,
			Color noDataColour) throws XPathExpressionException, NumberFormatException, SLDException {
		ColourScheme colourScheme;

		// Create the colour map
		String paletteDefinition = (String) xPath.evaluate(
				"./resc:PaletteDefinition", function, XPathConstants.STRING);
		if (paletteDefinition == null || paletteDefinition.equals("")) {
			throw new SLDException("The palette function must contain a palette definition.");
		}
		String nColourBandsText = (String) xPath.evaluate(
				"./resc:NumberOfColorBands", function, XPathConstants.STRING);
		Integer nColourBands;
		if (nColourBandsText == null || nColourBandsText.equals("")) {
			nColourBands = 254;
		} else {
			nColourBands = Integer.parseInt(nColourBandsText);
		}
		String belowMinColourText = (String) xPath.evaluate(
				"./resc:BelowMinColor", function, XPathConstants.STRING);
		Color belowMinColour = decodeColour(belowMinColourText);
		String aboveMaxColourText = (String) xPath.evaluate(
				"./resc:AboveMaxColor", function, XPathConstants.STRING);
		Color aboveMaxColour = decodeColour(aboveMaxColourText);
		ColourMap colourMap = new ColourMap(belowMinColour, aboveMaxColour, noDataColour, paletteDefinition, nColourBands);
		
		// Create the colour scale
		String scaleMinText = (String) xPath.evaluate(
				"./resc:ColorScale/resc:ScaleMin", function, XPathConstants.STRING);
		if (scaleMinText == null || scaleMinText.equals("")) {
			throw new SLDException("The scale minimum must be specified in a colour scale.");
		}
		Float scaleMin = Float.parseFloat(scaleMinText);
		String scaleMaxText = (String) xPath.evaluate(
				"./resc:ColorScale/resc:ScaleMax", function, XPathConstants.STRING);
		if (scaleMaxText == null || scaleMaxText.equals("")) {
			throw new SLDException("The scale maximum must be specified in a colour scale.");
		}
		Float scaleMax = Float.parseFloat(scaleMaxText);
		String logarithmicText = (String) xPath.evaluate(
				"./resc:ColorScale/resc:Logarithmic", function, XPathConstants.STRING);
		Boolean logarithmic = Boolean.parseBoolean(logarithmicText);
		ColourScale colourScale = new ColourScale(scaleMin, scaleMax, logarithmic);
		
		// Create the colour scheme
		colourScheme = new PaletteColourScheme(colourScale, colourMap);
		return colourScheme;
	}

	public static ColourScheme2D parseCategorize2D(XPath xPath, Node function,
			Color noDataColour) throws XPathExpressionException, NumberFormatException, SLDException {
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

	/**
	 * Adds an opacity transform parsed from a symbolizer node to an image layer.
	 * @param xPath
	 * @param symbolizerNode
	 * @param imageLayer
	 * @throws XPathExpressionException
	 * @throws NumberFormatException
	 * @throws SLDException
	 */
	public static void addOpacity(XPath xPath, Node symbolizerNode, ImageLayer imageLayer)
			throws XPathExpressionException, NumberFormatException, SLDException {
		Node opacityNode = (Node) xPath.evaluate(
				"./se:Opacity", symbolizerNode, XPathConstants.NODE);
		Node linearNode = (Node) xPath.evaluate(
				"./resc:LinearOpacityTransform", symbolizerNode, XPathConstants.NODE);
		
		if (opacityNode != null) {
			if (linearNode != null) {
				throw new SLDException("A symbolizer can only contain one opacity transform");
			}
			String opacity = opacityNode.getTextContent();
			imageLayer.setOpacityTransform(new FlatOpacity(Float.parseFloat(opacity)));
			return;
		}
		
		if (linearNode != null) {
			String dataFieldName = (String) xPath.evaluate(
					"./resc:DataFieldName", linearNode, XPathConstants.STRING);
			if (dataFieldName == null || dataFieldName.equals("")) {
				throw new SLDException("A linear opacity transform must contain a data field name.");
			}
			String opaqueValue = (String) xPath.evaluate(
					"./resc:OpaqueValue", linearNode, XPathConstants.STRING);
			if (opaqueValue == null || opaqueValue.equals("")) {
				throw new SLDException("A linear opacity transform must contain an opaque value.");
			}
			String transparentValue = (String) xPath.evaluate(
					"./resc:TransparentValue", linearNode, XPathConstants.STRING);
			if (transparentValue == null || transparentValue.equals("")) {
				throw new SLDException("A linear opacity transform must contain a transparent value.");
			}
			imageLayer.setOpacityTransform(new LinearOpacity(dataFieldName,
					Float.parseFloat(opaqueValue),
					Float.parseFloat(transparentValue)));
			return;
		}
		
	}

}
