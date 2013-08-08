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
	
	public static final String OUTPUT_ENCODING = "UTF-8";
	public static final String JAXP_SCHEMA_LANGUAGE =
			"http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	public static final String W3C_XML_SCHEMA =
			"http://www.w3.org/2001/XMLSchema";
	public static final String JAXP_SCHEMA_SOURCE =
			"http://java.sun.com/xml/jaxp/properties/schemaSource";
	public static final String SLD_SCHEMA =
			"http://schemas.opengis.net/sld/1.1.0/StyledLayerDescriptor.xsd";
	public static final String SLD_NAMESPACE =
			"http://www.opengis.net/sld";
	public static final String SE_NAMESPACE =
			"http://www.opengis.net/se";
	
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
		NodeList namedLayers = document.getElementsByTagNameNS(SLD_NAMESPACE, "NamedLayer");
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
			
			// get name of data field
			Node nameNode = layerElement.getElementsByTagNameNS(SE_NAMESPACE, "Name").item(0);
			if (nameNode == null) {
				continue;
			}
			String name = nameNode.getTextContent();
			if (name.equals("")) {
				continue;
			}
			
			// get first RasterSymbolizer element
			Node symbolizerNode = layerElement.getElementsByTagNameNS(SE_NAMESPACE, "RasterSymbolizer").item(0);
			if (symbolizerNode == null || symbolizerNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element symbolizerElement = (Element) symbolizerNode;

			// get opacity element
			Node opacityNode = symbolizerElement.getElementsByTagNameNS(SE_NAMESPACE, "Opacity").item(0);
			String opacity;
			if (opacityNode != null) {
				opacity = opacityNode.getTextContent();
			} else {
				opacity = "";
			}
			
			// get first Categorize element
			Node categorizeNode = symbolizerElement.getElementsByTagNameNS(SE_NAMESPACE, "Categorize").item(0);
			if (categorizeNode == null || categorizeNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element categorizeElement = (Element) categorizeNode;
			
			// get fall back value
			String fallbackValue = categorizeElement.getAttribute("fallbackValue");
			
			// get list of colours
			NodeList colours = categorizeElement.getElementsByTagNameNS(SE_NAMESPACE, "Value");
			if (colours == null) {
				continue;
			}
			
			//get list of thresholds
			NodeList thresholds = categorizeElement.getElementsByTagNameNS(SE_NAMESPACE, "Threshold");
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
		}
		xmlString = xmlString + "</resc:Image>\n";
		return xmlString;
	}
}
