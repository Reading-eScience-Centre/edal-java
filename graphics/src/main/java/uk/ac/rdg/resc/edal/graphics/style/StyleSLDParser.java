package uk.ac.rdg.resc.edal.graphics.style;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

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
	
	public static String SLDtoXMLString(File file)
			throws ParserConfigurationException, SAXException, FileNotFoundException,
			IOException, XPathExpressionException {
		
		/*
		 *  Read in and parse an XML file to a Document object. The builder factory is
		 *  configured to be namespace aware and validating. The class SAXErrorHandler
		 *  is used to handle validation errors. The schema is forced to be the SLD
		 *  schema.
		 */
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		builderFactory.setValidating(true);
		try {
			builderFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
		} catch (IllegalArgumentException iae) {
			System.err.println("Error: JAXP DocumentBuilderFactory attribute "
					+ "not recognized: " + JAXP_SCHEMA_LANGUAGE);
			System.err.println("Check to see if parser conforms to JAXP spec.");
			System.exit(1);
		}
		builderFactory.setAttribute(JAXP_SCHEMA_SOURCE, SLD_SCHEMA);
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		OutputStreamWriter errorWriter = new OutputStreamWriter(System.err,
				OUTPUT_ENCODING);
		builder.setErrorHandler(new SAXErrorHandler(new PrintWriter(errorWriter, true)));
		Document document = builder.parse(new FileInputStream(file));
		
		// Parse the document using XPath
		XPath xPath =XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(new SLDNamespaceResolver());

		// Get all the named layers in the document and loop through each one
		NodeList namedLayers = (NodeList) xPath.evaluate(
				"/sld:StyledLayerDescriptor/sld:NamedLayer", document,
				XPathConstants.NODESET);
		if (namedLayers == null) {
			return "";
		}
		String xmlString = "";
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
			
			// get opacity element
			Node opacityNode = (Node) xPath.evaluate(
					"./sld:UserStyle/se:CoverageStyle/se:Rule/se:RasterSymbolizer/se:Opacity",
					layerNode, XPathConstants.NODE);
			String opacity;
			if (opacityNode != null) {
				opacity = opacityNode.getTextContent();
			} else {
				opacity = "";
			}
			
			// get fall back value
			String fallbackValue = (String) xPath.evaluate(
					"./sld:UserStyle/se:CoverageStyle/se:Rule/se:RasterSymbolizer/se:ColorMap/se:Categorize/@fallbackValue",
					layerNode, XPathConstants.STRING);
			
			// get list of colours
			NodeList colours = (NodeList) xPath.evaluate(
					"./sld:UserStyle/se:CoverageStyle/se:Rule/se:RasterSymbolizer/se:ColorMap/se:Categorize/se:Value",
					layerNode, XPathConstants.NODESET);
			if (colours == null) {
				continue;
			}
			
			//get list of thresholds
			NodeList thresholds = (NodeList) xPath.evaluate(
					"./sld:UserStyle/se:CoverageStyle/se:Rule/se:RasterSymbolizer/se:ColorMap/se:Categorize/se:Threshold",
					layerNode, XPathConstants.NODESET);
			if (thresholds == null) {
				continue;
			}

			// write out XML to string
			xmlString = xmlString +
					"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
					+ "<resc:Image xmlns:resc='http://www.resc.reading.ac.uk'>\n";
			xmlString = xmlString + "    <RasterLayer>\n";
			if (!opacity.equals("")) {
				xmlString = xmlString + "        <FlatOpacity>" + opacity + "</FlatOpacity>\n";
			}
			xmlString = xmlString + "        <DataFieldName>" + name + "</DataFieldName>\n";
			xmlString = xmlString + "        <ThresholdColourScheme>\n";
			for (int j = 0; j < colours.getLength(); j++) {
				String colour = colours.item(j).getTextContent();
				xmlString = xmlString + "            <Colours>" + colour + "</Colours>\n";
			}
			for (int j = 0; j < thresholds.getLength(); j++) {
				String threshold = thresholds.item(j).getTextContent();
				xmlString = xmlString + "            <Thresholds>" + threshold + "</Thresholds>\n";					
			}
			if (!fallbackValue.equals("")) {
				xmlString = xmlString + "            <MissingDataColour>" + fallbackValue + "</MissingDataColour>\n";
			}
			xmlString = xmlString + "        </ThresholdColourScheme>\n";
			xmlString = xmlString + "    </RasterLayer>\n";
			xmlString = xmlString + "</resc:Image>\n";
		}
		return xmlString;
	}
}
