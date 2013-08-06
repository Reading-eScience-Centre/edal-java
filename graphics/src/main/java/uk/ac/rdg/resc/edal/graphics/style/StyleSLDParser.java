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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class StyleSLDParser {
	
	private static final String OUTPUT_ENCODING = "UTF-8";
	private static final String JAXP_SCHEMA_LANGUAGE =
			"http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	private static final String W3C_XML_SCHEMA =
			"http://www.w3.org/2001/XMLSchema";
	private static final String JAXP_SCHEMA_SOURCE =
			"http://java.sun.com/xml/jaxp/properties/schemaSource";
	private static final String SLD_SCHEMA =
			"http://schemas.opengis.net/sld/1.1.0/StyledLayerDescriptor.xsd";
	
	public static String SLDtoXMLString(File file)
			throws ParserConfigurationException, SAXException, FileNotFoundException,
			IOException {
		
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
		
		// Parse the XML document using DOM
		NodeList namedLayers = document.getElementsByTagName("NamedLayer");
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
			Element layerElement = (Element) layerNode;
			
			String name = layerElement.getElementsByTagName("se:Name").item(0).getTextContent();
			if (name == null) {
				continue;
			}
			
			// get first CoverageStyle element
			Node csNode = layerElement.getElementsByTagName("se:CoverageStyle").item(0);
			if (csNode == null || csNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element csElement = (Element) csNode;
			
			// get first Rule element
			Node ruleNode = csElement.getElementsByTagName("se:Rule").item(0);
			if (ruleNode == null || ruleNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element ruleElement = (Element) ruleNode;
			
			// get first RasterSymbolizer element
			Node rsNode = ruleElement.getElementsByTagName("se:RasterSymbolizer").item(0);
			if (rsNode == null || rsNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element rsElement = (Element) rsNode;

			// get opacity attribute
			String opacity = layerElement.getElementsByTagName("se:Opacity").item(0).getTextContent();
			
			// get first ColorMap element
			Node cmNode = rsElement.getElementsByTagName("se:ColorMap").item(0);
			if (cmNode == null || cmNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element cmElement = (Element) cmNode;

			// get first Categorize element
			Node catNode = cmElement.getElementsByTagName("se:Categorize").item(0);
			if (catNode == null || catNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element catElement = (Element) catNode;
			
			// get fall back value
			String fallbackValue = catElement.getAttribute("fallbackValue");
			
			// get list of colours
			NodeList colours = catElement.getElementsByTagName("se:Value");
			if (colours == null) {
				continue;
			}
			
			//get list of thresholds
			NodeList thresholds = catElement.getElementsByTagName("se:Threshold");
			if (thresholds == null) {
				continue;
			}

			// write out XML to string
			xmlString = xmlString +
					"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
					+ "<resc:Image xmlns:resc='http://www.resc.reading.ac.uk'>\n";
			xmlString = xmlString + "    <RasterLayer>\n";
			if (opacity != null) {
				xmlString = xmlString + "        <FlatOpacity>" + opacity +
						"</FlatOpacity>\n";
			}
			xmlString = xmlString + "        <DataFieldName>" + name + "</DataFieldName>\n";
			for (int j = 0; j < colours.getLength(); j++) {
				String colour = colours.item(j).getTextContent();
				if (colour != null) {
					xmlString = xmlString + "        <Colours>" + colour +
							"</Colours>\n";					
				}
			}
			for (int j = 0; j < thresholds.getLength(); j++) {
				String threshold = thresholds.item(j).getTextContent();
				if (threshold != null) {
					xmlString = xmlString + "        <Thresholds>" + threshold +
							"</Thesholds>\n";					
				}
			}
			if (fallbackValue != null) {
				xmlString = xmlString + "        <MissingDataColour>" + fallbackValue +
						"</MissingDataColour>\n";
			}
			xmlString = xmlString + "    </RasterLayer>\n";
		}
		xmlString = xmlString + "</resc:Image>\n";
		return xmlString;
	}
}
