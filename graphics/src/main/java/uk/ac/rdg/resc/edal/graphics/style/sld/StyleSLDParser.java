package uk.ac.rdg.resc.edal.graphics.style.sld;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uk.ac.rdg.resc.edal.graphics.style.FlatOpacity;
import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Reads in an XML file encoded with Styled Layer Descriptor and Symbology
 * Encoding and parses the document to create a corresponding image.
 */
public class StyleSLDParser {
    public static class FlatOpacityAdapter extends XmlAdapter<String, FlatOpacity> {
        @Override
        public FlatOpacity unmarshal(String v) throws Exception {
            return new FlatOpacity(Float.parseFloat(v.trim()));
        }

        @Override
        public String marshal(FlatOpacity v) throws Exception {
            return v.getOpacity().toString();
        }
    }

    public static final String OUTPUT_ENCODING = "UTF-8";
    public static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    public static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    public static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    public static final String SLD_SCHEMA = "http://schemas.opengis.net/sld/1.1.0/StyledLayerDescriptor.xsd";

    private static Map<String, Class<? extends SLDSymbolizer>> symbolizerList = new HashMap<String, Class<? extends SLDSymbolizer>>();

    static {
        registerSymbolizer("RasterSymbolizer", SLDRasterSymbolizer.class);
        registerSymbolizer("Raster2DSymbolizer", SLDRaster2DSymbolizer.class);
        registerSymbolizer("RasterRGBSymbolizer", SLDRasterRGBSymbolizer.class);
        registerSymbolizer("ContourSymbolizer", SLDContourSymbolizer.class);
        registerSymbolizer("StippleSymbolizer", SLDStippleSymbolizer.class);
        registerSymbolizer("ArrowSymbolizer", SLDArrowSymbolizer.class);
        registerSymbolizer("SizedArrowSymbolizer", SLDSizedArrowSymbolizer.class);
        registerSymbolizer("ColoredSizedArrowSymbolizer", SLDColoredSizedArrowSymbolizer.class);
        registerSymbolizer("ColoredGlyphSymbolizer", SLDColoredGlyphSymbolizer.class);
        //		registerSymbolizer("SubsampledIconSymbolizer", SLDSubsampledIconSymbolizer.class);
        registerSymbolizer("ConfidenceIntervalSymbolizer", SLDConfidenceIntervalSymbolizer.class);
    }

    /**
     * Create an image given an XML file containing an SLD document.
     * 
     * @param xmlFile
     * @return Image
     * @throws FileNotFoundException
     * @throws SLDException
     */
    public static MapImage createImage(File xmlFile) throws FileNotFoundException, SLDException {
        try {
            Document xmlDocument = readXMLFile(xmlFile);
            MapImage image = parseSLD(xmlDocument);
            return image;
        } catch (FileNotFoundException fnfe) {
            throw fnfe;
        } catch (Exception e) {
            throw new SLDException(e);
        }
    }

    /**
     * Create an image given an XML string containing an SLD document
     * 
     * @param xmlString
     * @return Image
     * @throws SLDException
     */
    public static MapImage createImage(String xmlString) throws SLDException {
        try {
            Document xmlDocument = readXMLString(xmlString);
            MapImage image = parseSLD(xmlDocument);
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            throw new SLDException(e);
        }
    }

    /**
     * Register a new symbolizer class on a map of symbolizers.
     * 
     * @param symbolizerTag
     *            - the symbolizer tag as a string.
     * @param symbolizerClass
     *            - the class type of the new symbolizer.
     * @throws IllegalArgumentException
     *             - if either argument is null this exception is thrown.
     */
    public static void registerSymbolizer(String symbolizerTag,
            Class<? extends SLDSymbolizer> symbolizerClass) throws IllegalArgumentException {
        if (symbolizerTag == null) {
            throw new IllegalArgumentException("The symbolizer tag cannot be null.");
        }
        if (symbolizerClass == null) {
            throw new IllegalArgumentException("The symbolizer class cannot be null");
        }
        symbolizerList.put(symbolizerTag, symbolizerClass);
    }

    /*
     * Parse the document using XPath and create a corresponding image
     */
    private static MapImage parseSLD(Document xmlDocument) throws XPathExpressionException,
            SLDException, InstantiationException, IllegalAccessException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new SLDNamespaceResolver());

        // Instantiate a new MapImage object
        MapImage image = new MapImage();

        // Get all the layers in the document and loop through each one
        NodeList layers = (NodeList) xPath.evaluate(
                "/sld:StyledLayerDescriptor/sld:*", xmlDocument, XPathConstants.NODESET);
        if (!(layers.getLength() > 0)) {
            throw new SLDException("There must be at least one layer in an SLD document.");
        }
        for (int i = 0; i < layers.getLength(); i++) {

            // get each layer node and extract the variable names
            Node layerNode = layers.item(i);
            List<String> varNames = new ArrayList<String>(); 
            if (layerNode.getLocalName().equals("NamedLayer")) {
            	if (layerNode.getNodeType() != Node.ELEMENT_NODE) {
            		throw new SLDException("Named layer is not an element node.");
            	}
                // get the name of the layer
                Node nameNode = (Node) xPath.evaluate("./se:Name", layerNode, XPathConstants.NODE);
                if (nameNode == null) {
                    throw new SLDException("A named layer must be named.");
                }
                String layerName = nameNode.getTextContent();
                if (layerName.equals("")) {
                    throw new SLDException("The name of the layer cannot be empty.");
                }
                varNames.add(layerName);
            } else if (layerNode.getLocalName().equals("UserLayer")) {
            	if (layerNode.getNodeType() != Node.ELEMENT_NODE) {
            		throw new SLDException("User layer is not an element node.");
            	}
            	NodeList nameNodes = (NodeList) xPath.evaluate(
            			"./sld:LayerCoverageConstraints/sld:CoverageConstraint/se:CoverageName",
            			layerNode, XPathConstants.NODESET);
                if (nameNodes == null || nameNodes.getLength() == 0) {
                    throw new SLDException("A user layer must specify the coverage constraints.");
                }
                for(int j = 0; j < nameNodes.getLength(); j++) {
                	String coverageName = nameNodes.item(j).getTextContent();
                    if (coverageName == null || coverageName.equals("")) {
                        throw new SLDException("The name of the coverage must be specified.");
                    }
                    varNames.add(coverageName);
                }
            } else {
            	continue;
            }

            // get the children of the first rule and check that there is exactly one child
            NodeList symbolizers = (NodeList) xPath.evaluate(
            		"./sld:UserStyle/se:CoverageStyle/se:Rule/*", layerNode,
            		XPathConstants.NODESET);
            if (symbolizers.getLength() != 1) {
                throw new SLDException("There must be exactly one symbolizer per layer.");
            }
            Node symbolizerNode = symbolizers.item(0);

            // create the corresponding symbolizer object
            SLDSymbolizer sldSymbolizer = null;
            for (Entry<String, Class<? extends SLDSymbolizer>> entry : symbolizerList.entrySet()) {
                String symbolizerTag = entry.getKey();
                Class<? extends SLDSymbolizer> symbolizerClass = entry.getValue();

                if (symbolizerNode.getLocalName().equals(symbolizerTag)) {
                    sldSymbolizer = (SLDSymbolizer) symbolizerClass.newInstance();
                }
            }
            if (symbolizerNode == null) {
                throw new SLDException("Symbolizer type not recognized.");
            }

            // parse the symbolizer and add the resulting image layer to the image
            ImageLayer imageLayer = sldSymbolizer.getImageLayer(varNames, symbolizerNode);
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

    /**
     * Read in and parse an XML file to a Document object. The builder factory
     * is configured to be namespace aware and validating. The class
     * SAXErrorHandler is used to handle validation errors. The schema is forced
     * to be the SLD schema v1.1.0.
     * 
     * @param xmlFile
     * @return
     * @throws ParserConfigurationException
     * @throws FileNotFoundException
     * @throws SAXException
     * @throws IOException
     * @throws IllegalArgumentException
     */
    private static Document readXMLFile(File xmlFile) throws ParserConfigurationException,
            FileNotFoundException, SAXException, IOException, IllegalArgumentException {
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
        OutputStreamWriter errorWriter = new OutputStreamWriter(System.err, OUTPUT_ENCODING);
        builder.setErrorHandler(new SAXErrorHandler(new PrintWriter(errorWriter, true)));
        Document xmlDocument = builder.parse(new FileInputStream(xmlFile));
        return xmlDocument;
    }

    /**
     * Read in and parse an XML string to a Document object. The builder factory
     * is configured to be namespace aware and validating. The class
     * SAXErrorHandler is used to handle validation errors. The schema is forced
     * to be the SLD schema v1.1.0.
     * 
     * @param xmlString
     * @return
     * @throws ParserConfigurationException
     * @throws FileNotFoundException
     * @throws SAXException
     * @throws IOException
     * @throws IllegalArgumentException
     */
    private static Document readXMLString(String xmlString) throws ParserConfigurationException,
            FileNotFoundException, SAXException, IOException, IllegalArgumentException {
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
        OutputStreamWriter errorWriter = new OutputStreamWriter(System.err, OUTPUT_ENCODING);
        builder.setErrorHandler(new SAXErrorHandler(new PrintWriter(errorWriter, true)));
        Document xmlDocument = builder.parse(new InputSource(new StringReader(xmlString)));
        return xmlDocument;
    }

}
