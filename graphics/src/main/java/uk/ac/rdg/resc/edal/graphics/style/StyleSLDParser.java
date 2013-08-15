package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.imageio.ImageIO;
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

import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.FlatOpacity;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.Image;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.InterpolateColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.RasterLayer;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ThresholdColourScheme;

/**
 * Reads in an XML file encoded with Styled Layer Descriptor and Symbology
 * Encoding, validates it against the SLD schema and parses the document
 * to create a corresponding image. Currently only raster symbolizers are
 * handled.
 * 
 * @author Charles Roberts
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
	
	public static void SLDtoXMLString(File xmlFile, File imageFile, GlobalPlottingParams params, Id2FeatureAndMember id2Feature)
			throws ParserConfigurationException, SAXException, FileNotFoundException,
			IOException, XPathExpressionException, IllegalArgumentException {
		
		/*
		 *  Read in and parse an XML file to a Document object. The builder factory is
		 *  configured to be namespace aware and validating. The class SAXErrorHandler
		 *  is used to handle validation errors. The schema is forced to be the SLD
		 *  schema v1.1.0.
		 */
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		builderFactory.setValidating(true);
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
		Document document = builder.parse(new FileInputStream(xmlFile));
		
		/*
		 *  Parse the document using XPath and create a corresponding image
		 */
		XPath xPath =XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(new SLDNamespaceResolver());

		// Instantiate an Image object
		Image image = new Image();

		// Get all the named layers in the document and loop through each one
		NodeList namedLayers = (NodeList) xPath.evaluate(
				"/sld:StyledLayerDescriptor/sld:NamedLayer", document,
				XPathConstants.NODESET);

		if (namedLayers != null) {
			for (int i = 0; i < namedLayers.getLength(); i++) {
				Node layerNode = namedLayers.item(i);
				
				// make sure it is an element node
				if (layerNode.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				
				// get name of data field
				Node nameNode = (Node) xPath.evaluate(
						"./se:Name", layerNode, XPathConstants.NODE);
				if (nameNode == null) {
					continue;
				}
				String name = nameNode.getTextContent();
				if (name.equals("")) {
					continue;
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
					continue;
				}
				
				// get fall back value
				String fallbackValue = (String) xPath.evaluate(
						"./@fallbackValue",
						function, XPathConstants.STRING);
				if (fallbackValue == null) {
					fallbackValue = "";
				}
				
				// parse function specific parts of XML for colour scheme
				ColourScheme colourScheme;
				if (function.getLocalName().equals("Categorize")) {
					// get list of colours
					NodeList colourNodes = (NodeList) xPath.evaluate(
							"./se:Value",
							function, XPathConstants.NODESET);
					if (colourNodes == null) {
						continue;
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
						continue;
					}
					// transform to list of Floats
					ArrayList<Float> thresholds = new ArrayList<Float>();
					for (int j = 0; j < thresholdNodes.getLength(); j++) {
						Node thresholdNode = thresholdNodes.item(j);
						try {
							thresholds.add(Float.parseFloat(thresholdNode.getTextContent()));
						} catch (NumberFormatException nfe) {
							System.err.println("Error: threshold not correctly formated.");
						}
					}
					
					Color noDataColour = decodeColour(fallbackValue);  
					colourScheme = new ThresholdColourScheme(thresholds, colours, noDataColour);
				} else if (function.getLocalName().equals("Interpolate")) {
					// get list of data points
					NodeList pointNodes = (NodeList) xPath.evaluate(
							"./se:InterpolationPoint",
	 						function, XPathConstants.NODESET);
					if (pointNodes == null) {
						continue;
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
							continue;
						}
						Node valueNode = (Node) xPath.evaluate(
								"./se:Value",
								pointNode, XPathConstants.NODE);
						if (valueNode == null) {
							continue;
						}
						try {
							points.add(new InterpolateColourScheme.InterpolationPoint(
									Float.parseFloat(dataNode.getTextContent()),
									decodeColour(valueNode.getTextContent())));
						} catch (NumberFormatException nfe) {
							System.err.println("Error: interpolation data not correctly formated.");
						}
					}
					if (points.size() < 1) {
						continue;
					}
					// create a new InterpolateColourScheme object
					Color noDataColour = decodeColour(fallbackValue);
					colourScheme = new InterpolateColourScheme(points, noDataColour);			
				} else {
					continue;
				}
				
				// instantiate a new raster layer and add it to the image
				RasterLayer rasterLayer = new RasterLayer(name, colourScheme);
				if (!opacity.equals("")) {
					try {
						rasterLayer.setOpacityTransform(new FlatOpacity(Float.parseFloat(opacity)));
					} catch (NumberFormatException nfe) {
						System.err.println("Error: opacity not correctly formatted.");
					}
				}
				image.getLayers().add(rasterLayer);
			}
		}
		
		// write out the image
		if (image.getLayers().size() > 0) {
			ImageIO.write(image.drawImage(params, id2Feature), "png", imageFile);
		} else {
			System.err.println("Error: no image layers have been parsed successfully.");
		}
	}
	
    private static Color decodeColour(String s) {
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

}
