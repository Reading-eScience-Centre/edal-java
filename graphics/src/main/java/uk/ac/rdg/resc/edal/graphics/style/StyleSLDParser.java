package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;
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

import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.Image;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ImageLayer;

/**
 * Reads in an XML file encoded with Styled Layer Descriptor and Symbology
 * Encoding and parses the document to create a corresponding image.
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
				throw new SLDException("There must be exactly one symbolizer per rule");
			}
			Node symbolizerNode = symbolizers.item(0);

			// parse the symbolizer
			SLDSymbolizer sldSymbolizer;
			if (symbolizerNode.getLocalName().equals("RasterSymbolizer")) {
				sldSymbolizer = new SLDRasterSymbolizer(layerName, symbolizerNode);
			} else if (symbolizerNode.getLocalName().equals("Raster2DSymbolizer")) {
				sldSymbolizer = new SLDRaster2DSymbolizer(layerName, symbolizerNode);
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
	 * 
	 * @author Guy Griffiths
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

}
