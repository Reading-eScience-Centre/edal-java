/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc.edal.wms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.graphics.style.Drawable;
import uk.ac.rdg.resc.edal.graphics.style.Drawable.NameAndRange;
import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.graphics.style.sld.SLDException;
import uk.ac.rdg.resc.edal.graphics.style.sld.StyleSLDParser;
import uk.ac.rdg.resc.edal.graphics.style.util.FeatureCatalogue;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.PlottingDomainParams;
import uk.ac.rdg.resc.edal.wms.exceptions.EdalLayerNotFoundException;
import uk.ac.rdg.resc.edal.wms.util.ContactInfo;
import uk.ac.rdg.resc.edal.wms.util.ServerInfo;
import uk.ac.rdg.resc.edal.wms.util.StyleDef;

/**
 * This class encapsulates the elements needed to implement a specific WMS.
 * Subclasses should extend this to implement their own configuration system.
 * 
 * This includes:
 * 
 * <li>Global server settings
 * 
 * <li>Contact information for the server
 * 
 * <li>Retrieval/generation of layer names from dataset/variable IDs
 * 
 * <li>Defining default plotting parameters for WMS layers
 * 
 * @author Guy Griffiths
 */
public abstract class WmsCatalogue implements FeatureCatalogue {
    private static final Logger log = LoggerFactory.getLogger(WmsCatalogue.class);

    private SortedMap<String, StyleDef> styleDefs = new TreeMap<String, StyleDef>(
            new Comparator<String>() {
                /*
                 * We want the styles to be sorted:
                 * 
                 * Names starting with "default" first (alphabetically ordered
                 * if there are multiple) Then alphabetical order
                 */
                @Override
                public int compare(String o1, String o2) {
                    if (o1.startsWith("default") && !o2.startsWith("default")) {
                        return -1;
                    }
                    if (!o1.startsWith("default") && o2.startsWith("default")) {
                        return 1;
                    }
                    return o1.compareTo(o2);
                }
            });

    public WmsCatalogue() {
        /*
         * We want the catalogue to read all possible XML templates for
         * supported styles. This includes 2 parts:
         * 
         * 1. Reading the styles which are packaged with the edal-wms module
         * (i.e. from the JAR file)
         * 
         * 2. Reading any additional styles defined by a user (i.e. from the
         * classpath)
         */
        NoAutoCloseZipInputStream zip = null;
        try {
            /*
             * This reads all styles from the JAR file
             */
            Pattern styleXmlPath = Pattern.compile("^styles/(.*)\\.xml$");
            CodeSource src = WmsCatalogue.class.getProtectionDomain().getCodeSource();
            if (src != null) {
                URL jar = src.getLocation();
                zip = new NoAutoCloseZipInputStream(jar.openStream());
                while (true) {
                    ZipEntry e = zip.getNextEntry();
                    if (e == null) {
                        break;
                    }
                    String name = e.getName();
                    Matcher matcher = styleXmlPath.matcher(name);
                    if (matcher.matches()) {
                        String xmlString = IOUtils.toString(zip);
                        StyleDef style = processStyle(matcher.group(1), xmlString);
                        if (style != null) {
                            styleDefs.put(style.getStyleName(), style);
                        }
                    }
                }
            } else {
                /* Fail... */
            }
        } catch (Exception e) {
            log.error("Problem processing styles in edal-wms module", e);
        } finally {
            if (zip != null) {
                zip.allowToBeClosed();
                try {
                    zip.close();
                } catch (IOException e) {
                    /*
                     * Ignore this error, we can't do anything about it
                     */
                }
            }
        }
        try {
            /*
             * This reads all styles from the WEB-INF/classes/styles directory
             */
            Pattern styleXmlPath = Pattern.compile("^(.*)\\.xml$");
            File stylesDir = new File(getClass().getResource("/styles/").toURI());
            if (stylesDir.isDirectory()) {
                for (File styleFile : stylesDir.listFiles()) {
                    Matcher matcher = styleXmlPath.matcher(styleFile.getName());
                    if (matcher.matches()) {
                        StyleDef style = processStyle(matcher.group(1),
                                IOUtils.toString(new FileInputStream(styleFile)));
                        if (style != null) {
                            styleDefs.put(style.getStyleName(), style);
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            /*
             * We ignore this exception since it just means that the styles
             * directory is missing from the classpath
             */
            if (!e.getMessage().contains("URI is not hierarchical")) {
                throw e;
            }
        } catch (Exception e) {
            log.error("Problem processing styles on classpath", e);
        }
    }

    @Override
    public FeaturesAndMemberName getFeaturesForLayer(String id, PlottingDomainParams params)
            throws EdalException {
        Dataset dataset = getDatasetFromLayerName(id);
        String variable = getVariableFromId(id);
        Collection<? extends DiscreteFeature<?, ?>> mapFeatures = dataset.extractMapFeatures(
                CollectionUtils.setOf(variable), params);
        return new FeaturesAndMemberName(mapFeatures, variable);
    }

    /**
     * Gets the {@link VariableMetadata} object corresponding to a named layer
     * 
     * @param layerName
     *            The name of the WMS layer
     * @return The corresponding {@link VariableMetadata}
     * @throws EdalLayerNotFoundException
     *             If the WMS layer name doesn't map to a variable
     */
    public VariableMetadata getVariableMetadataFromId(String layerName)
            throws EdalLayerNotFoundException {
        Dataset dataset = getDatasetFromLayerName(layerName);
        String variableFromId = getVariableFromId(layerName);
        if (dataset != null && variableFromId != null) {
            return dataset.getVariableMetadata(variableFromId);
        } else {
            throw new EdalLayerNotFoundException("The layer name " + layerName
                    + " doesn't map to a variable");
        }
    }

    /**
     * Gets a {@link List} of styles which are supported for the supplied
     * variable
     * 
     * @param variableMetadata
     *            The {@link VariableMetadata} representing the variable to
     *            check
     * @return A {@link List} containing all supported WMS styles which can plot
     *         the supplied variable.
     */
    public List<StyleDef> getSupportedStyles(VariableMetadata variableMetadata) {
        List<StyleDef> supportedStyles = new ArrayList<StyleDef>();
        /*
         * Loop through all loaded style definitions
         */
        for (StyleDef styleDef : styleDefs.values()) {
            if (styleDef.supportedBy(variableMetadata)) {
                supportedStyles.add(styleDef);
            }
        }

        return supportedStyles;
    }

    /**
     * Returns the a {@link Map} of keys used in XML templates to the concrete
     * WMS layer names which they represent.
     * 
     * @param layerName
     *            The WMS layer name to plot
     * @param styleName
     *            The style name to be plotted
     * @return A {@link Map} of keys used in XML templates to the concrete WMS
     *         layer names which they represent.
     * @throws EdalLayerNotFoundException
     *             If the layer specified does not exist on this WMS server
     */
    public Map<String, String> getStyleTemplateLayerNames(String layerName, String styleName)
            throws EdalLayerNotFoundException {
        StyleDef styleDef = styleDefs.get(styleName);
        Map<String, String> layerKeyToLayerName = new HashMap<String, String>();
        if (styleDef.needsNamedLayer()) {
            layerKeyToLayerName.put("layerName", layerName);
        }
        VariableMetadata layerMetadata = getVariableMetadataFromId(layerName);
        for (String childPurpose : styleDef.getRequiredChildRoles()) {
            layerKeyToLayerName.put(
                    "layerName-" + childPurpose,
                    getLayerName(layerMetadata.getDataset().getId(), layerMetadata
                            .getChildWithRole(childPurpose).getId()));
        }
        return layerKeyToLayerName;
    }

    /**
     * Processes an XML string representing a style template.
     * 
     * @param name
     *            The name of the style
     * @param xmlString
     *            The input stream containing the XML
     * @return A {@link StyleDef} representing the properties of the style
     * @throws IOException
     *             If there is a problem reading the style file
     * @throws ParserConfigurationException
     *             If there is a problem parsing the XML
     * @throws SAXException
     *             If there is a problem parsing the XML
     */
    private StyleDef processStyle(String name, String xmlString) throws IOException,
            ParserConfigurationException, SAXException {
        /*
         * Get the XML style definition into a NodeList
         */
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(IOUtils.toInputStream(xmlString));
        NodeList xmlNodes = doc.getChildNodes();

        String scaledLayer = null;
        boolean usesPalette = false;
        boolean needsNamedLayer = false;
        Set<String> requiredChildren = new HashSet<String>();

        /*
         * Find the layer name which uses the scaling URL arguments (if any)
         */
        Node scaleMinNode = findScaleMinNode(xmlNodes);
        if (scaleMinNode != null) {
            scaledLayer = getScaledLayerName(scaleMinNode);
        }
        /*
         * Find out whether this style uses a palette
         */
        usesPalette = styleContainsTag(xmlNodes, "^\\$paletteName$");
        /*
         * Find out whether this style uses the named layer (if not it will use
         * child layers of the named layer)
         */
        needsNamedLayer = styleContainsTag(xmlNodes, "^\\$layerName$");
        /*
         * Find which child layers are used by this style
         */
        populateStyleChildRoles(xmlNodes, requiredChildren);

        /*
         * This was the simpler bit. We now need to know what types of feature
         * are supported by the XML template.
         * 
         * This essentially means that we need to parse the XML into the
         * MapImage object, loop through the layers in the MapImage, and call
         * the acceptsFeatureTypes() method on each.
         * 
         * To parse the XML successfully, we need to replace all of the valid
         * identifiers which may be constrained (i.e. those which don't allow
         * arbitrary text) with real values (for example, if we try to parse the
         * XML fragment <scaleMin>$scaleMin</scaleMin> we should get an error,
         * since the <scaleMin> tag needs a numerical value.
         */

        xmlString = xmlString.replaceAll("\\$scaleMin", "0");
        xmlString = xmlString.replaceAll("\\$scaleMax", "10");
        xmlString = xmlString.replaceAll("\\$logarithmic", "linear");
        xmlString = xmlString.replaceAll("\\$numColorBands", "10");
        xmlString = xmlString.replaceAll("\\$bgColor", "#000000");
        xmlString = xmlString.replaceAll("\\$belowMinColor", "#000000");
        xmlString = xmlString.replaceAll("\\$aboveMaxColor", "#000000");

        /*
         * Java generics at its finest ;-)
         * 
         * To test if a style is supported by a particular variable, we need to
         * know:
         * 
         * For EVERY layer in the image (e.g. RasterLayer, GlyphLayer, etc) what
         * roles are supported, and whether they the variables having these
         * roles are one of the (possibly multiple) feature types supported by
         * this layer.
         * 
         * So the first Collection separates the layers.
         * 
         * The Map maps role names to the (2nd) Collection of Feature types, one
         * of which the variable with that role must be a type of.
         */
        Collection<Map<String, Collection<Class<? extends Feature<?>>>>> roles2FeatureType = new ArrayList<Map<String, Collection<Class<? extends Feature<?>>>>>();
        try {
            MapImage mapImage = StyleSLDParser.createImage(xmlString);
            for (Drawable layer : mapImage.getLayers()) {
                Map<String, Collection<Class<? extends Feature<?>>>> role2FeatureType = new HashMap<String, Collection<Class<? extends Feature<?>>>>();
                if (layer instanceof ImageLayer) {
                    ImageLayer imageLayer = (ImageLayer) layer;
                    Collection<Class<? extends Feature<?>>> supportedFeatureTypes = imageLayer
                            .supportedFeatureTypes();
                    /*
                     * Add the supported feature types to a Map of Collections
                     * of Features, using the getFieldsWithScales to determine
                     * the roles to use as a key
                     */
                    Set<NameAndRange> fieldsWithScales = imageLayer.getFieldsWithScales();
                    for (NameAndRange field : fieldsWithScales) {
                        String layerName = field.getFieldLabel();
                        if (layerName.startsWith("$layerName")) {
                            /*
                             * This should always be the case
                             */
                            String role;
                            if (layerName.equals("$layerName")) {
                                role = "";
                            } else {
                                role = layerName.substring(layerName.indexOf("-"));
                            }
                            role2FeatureType.put(role, supportedFeatureTypes);
                        }
                    }
                }
                roles2FeatureType.add(role2FeatureType);
            }
        } catch (SLDException e) {
            log.error("Problem parsing style XML", e);
        }

        return new StyleDef(name, requiredChildren, usesPalette, needsNamedLayer, scaledLayer,
                roles2FeatureType);
    }

    /**
     * Recursively searches a {@link NodeList} until it finds one with the text
     * content "$scaleMin".
     * 
     * @param nodes
     *            The {@link NodeList} to search
     * @return The {@link Node} containing "$scaleMin", or <code>null</code> if
     *         none exists
     */
    private Node findScaleMinNode(NodeList nodes) {
        /*
         * Recursively search through the tree for the tag "$scaleMin"
         */
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.hasChildNodes()) {
                /*
                 * Search all child nodes first
                 */
                Node scaleMinNode = findScaleMinNode(node.getChildNodes());
                if (scaleMinNode != null) {
                    return scaleMinNode;
                }
            }
            /*
             * Check this node's text
             */
            String nodeText = node.getTextContent();
            if (nodeText != null && nodeText.matches(".*\\$scaleMin.*")) {
                return node;
            }
        }
        /*
         * Nothing found in any of this NodeList, return null
         */
        return null;
    }

    /**
     * Finds the name of the layer which contains the scale min node. This works
     * by going up the document tree and checking all siblings for
     * "layerName-xxx" until one is found.
     * 
     * @param scaleMinNode
     *            The {@link Node} containing "$scaleMin"
     * @return The name of the layer to which "$scaleMin" applies, or
     *         <code>null</code> if there are none.
     */
    private String getScaledLayerName(Node scaleMinNode) {
        /*
         * Get the parent node and then check all of its children for a
         * $layerName[-xxxx] tag.
         * 
         * If there are none, go up the tree one level and try again.
         * 
         * Although we end up checking some parts of the tree several times, if
         * there are multiple layer names in the style definition we will get
         * the one which is most closely related to the scaleMin tag.
         */
        Node parentNode = scaleMinNode.getParentNode();
        if (parentNode == null) {
            return null;
        } else {
            String layerName = recursivelyCheckChildrenForLayerName(parentNode);
            if (layerName == null) {
                return getScaledLayerName(parentNode);
            } else {
                return layerName;
            }
        }
    }

    /**
     * This method takes a node and searches the entire tree below it for the
     * string "$layerName-xxx", and returns the "xxx" bit
     * 
     * @param parentNode
     *            A {@link Node} to test
     * @return The role of the child node, "" for the parent layer, or
     *         <code>null</code> if no such layers exist (e.g. for a fixed layer
     *         name style which uses the scale URL arguments)
     */
    private String recursivelyCheckChildrenForLayerName(Node parentNode) {
        /*
         * This could probably be factored out for more efficiency, but this
         * part of the code is really not a bottleneck (and only gets called at
         * initialisation)
         */
        Pattern pattern = Pattern.compile(".*\\$layerName-?(\\w*).*");
        /*
         * Recursively search for the child layer name, returning it when found
         */
        NodeList nodes = parentNode.getChildNodes();
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Node childNode = nodes.item(i);
                if (childNode.hasChildNodes()) {
                    String layerName = recursivelyCheckChildrenForLayerName(childNode);
                    if (layerName != null) {
                        return layerName;
                    }
                }
                String nodeText = childNode.getTextContent();
                Matcher matcher = pattern.matcher(nodeText);
                if (matcher.matches()) {
                    String childSuffix = matcher.group(1);
                    return childSuffix;
                }
            }
        }
        return null;
    }

    /**
     * Finds whether a particular tag exists in a tree
     * 
     * @param nodes
     *            A {@link NodeList} representing the part of the tree to check
     * @param regexp
     *            A regular expression defining the tag to search for
     * @return <code>true</code> if the regular expression matches
     */
    private boolean styleContainsTag(NodeList nodes, String regexp) {
        if (nodes == null) {
            return false;
        }
        /*
         * Recursively search all nodes and their children until the named tag
         * is found
         */
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String textContent = node.getTextContent();
            if (textContent != null) {
                if (textContent.matches(regexp)) {
                    return true;
                }
            }
            if (styleContainsTag(node.getChildNodes(), regexp)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This searches a tree for tags of the form "$layerName-xxxx" and adds the
     * "xxxx" parts to a {@link Set}
     * 
     * @param nodes
     *            The {@link NodeList} to start searching from
     * @param childRoles
     *            A {@link Set} to add the child role names to
     */
    private void populateStyleChildRoles(NodeList nodes, Set<String> childRoles) {
        Pattern pattern = Pattern.compile("^\\$layerName-?(\\w*)$");
        if (nodes == null) {
            return;
        }
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String textContent = node.getTextContent();
            if (textContent != null) {
                Matcher matcher = pattern.matcher(textContent);
                if (matcher.matches()) {
                    String role = matcher.group(1);
                    if (!role.isEmpty()) {
                        childRoles.add(role);
                    }
                }
            }
            populateStyleChildRoles(node.getChildNodes(), childRoles);
        }
    }

    /**
     * Returns the {@link StyleDef} object with the given name
     * 
     * @param styleName
     *            The name of the style
     * @return A {@link StyleDef} object representing the style, or
     *         <code>null</code> if it doesn't exist
     */
    public StyleDef getStyleDefinitionByName(String styleName) {
        return styleDefs.get(styleName);
    }

    /**
     * @return The main server metadata for this server
     */
    public abstract ServerInfo getServerInfo();

    /**
     * @return The main contact information for this server
     */
    public abstract ContactInfo getContactInfo();

    /**
     * @return <code>true</code> if this server allows capabilities documents to
     *         be generated for all datasets
     */
    public abstract boolean allowsGlobalCapabilities();

    /**
     * @return The last time that data on this server was updated
     */
    public abstract DateTime getServerLastUpdate();

    /**
     * @return All available {@link Dataset}s on this server
     */
    public abstract Collection<Dataset> getAllDatasets();

    /**
     * @param datasetId
     *            The ID of the dataset
     * @return The server-configured title of this dataset
     */
    public abstract String getDatasetTitle(String datasetId);

    /*
     * End of global server settings
     */

    /**
     * Returns a {@link Dataset} from its ID
     * 
     * @param datasetId
     *            The ID of the dataset
     * @return The desired dataset
     */
    public abstract Dataset getDatasetFromId(String datasetId);

    /**
     * Returns a {@link Dataset} based on a given layer name
     * 
     * @param layerName
     *            The full layer name
     * @return The desired dataset
     */
    public abstract Dataset getDatasetFromLayerName(String layerName)
            throws EdalLayerNotFoundException;

    /**
     * Returns a variable ID based on a given layer name
     * 
     * @param layerName
     *            The full layer name
     * @return The ID of the variable (within its {@link Dataset})
     */
    public abstract String getVariableFromId(String layerName) throws EdalLayerNotFoundException;

    /**
     * Returns the layer name based on the dataset and variable ID
     * 
     * @param dataset
     *            The ID of dataset containing the layer
     * @param variableId
     *            The ID of the variable within the dataset
     * @return The WMS layer name of this variable
     */
    public abstract String getLayerName(String datasetId, String variableId);

    /**
     * Returns server-configured metadata for a given layer
     * 
     * @param layerName
     *            The full layer name
     * @return Default metadata values for the desired layer
     */
    public abstract WmsLayerMetadata getLayerMetadata(String layerName)
            throws EdalLayerNotFoundException;

    /**
     * This is an {@link ZipInputStream} which only gets closed once it's been
     * specifically told to. This is because otherwise when parsing XML using
     * the {@link DocumentBuilder}, it will close the {@link InputStream}.
     * 
     * Since {@link ZipInputStream}s need to stay open to process everything
     * within the zip file, this is not what we want. The ideal solution would
     * be that {@link DocumentBuilder} didn't close {@link InputStream}s which
     * it didn't create, but that's out of our control.
     * 
     * See http://stackoverflow.com/questions/20020982/java-create-inputstream-
     * from-zipinputstream-entry
     * 
     * @author Guy Griffiths
     */
    private class NoAutoCloseZipInputStream extends ZipInputStream {
        private boolean canBeClosed = false;

        public NoAutoCloseZipInputStream(InputStream is) {
            super(is);
        }

        @Override
        public void close() throws IOException {
            if (canBeClosed)
                super.close();
        }

        public void allowToBeClosed() {
            canBeClosed = true;
        }
    }
}
