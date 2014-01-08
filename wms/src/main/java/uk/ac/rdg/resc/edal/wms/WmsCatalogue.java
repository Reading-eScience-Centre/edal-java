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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.GridDataset;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.graphics.style.util.FeatureCatalogue;
import uk.ac.rdg.resc.edal.graphics.style.util.PlottingDomainParams;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.wms.exceptions.WmsLayerNotFoundException;
import uk.ac.rdg.resc.edal.wms.util.ContactInfo;
import uk.ac.rdg.resc.edal.wms.util.ServerInfo;
import uk.ac.rdg.resc.edal.wms.util.StyleDef;
import uk.ac.rdg.resc.edal.wms.util.WmsUtils;

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

    private Map<String, StyleDef> styleDefs = new HashMap<String, StyleDef>();

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
                        StyleDef style = processStyle(matcher.group(1), zip);
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
                        StyleDef style = processStyle(matcher.group(1), new FileInputStream(
                                styleFile));
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
        Dataset<?> dataset = getDatasetFromLayerName(id);
        String variable = getVariableFromId(id);
        if (dataset instanceof GridDataset) {
            GridDataset gridDataset = (GridDataset) dataset;
            TemporalDomain temporalDomain = gridDataset.getVariableMetadata(variable)
                    .getTemporalDomain();
            Chronology chronology = null;
            if (temporalDomain != null) {
                chronology = temporalDomain.getChronology();
            }
            MapFeature mapData = gridDataset.readMapData(CollectionUtils.setOf(variable),
                    WmsUtils.getImageGrid(params), params.getTargetZ(),
                    params.getTargetT(chronology));
            /*
             * TODO Caching probably goes here
             */
            return new FeaturesAndMemberName(CollectionUtils.setOf(mapData), variable);
        } else {
            throw new UnsupportedOperationException("Currently only gridded data is supported");
        }
        /*
         * TODO process other types of Dataset here (i.e. InSituDataset which
         * doesn't yet exist)
         */
    }

    /**
     * Gets the {@link VariableMetadata} object corresponding to a named layer
     * 
     * @param layerName
     *            The name of the WMS layer
     * @return The corresponding {@link VariableMetadata}
     * @throws WmsLayerNotFoundException
     *             If the WMS layer name doesn't map to a variable
     */
    public VariableMetadata getVariableMetadataFromId(String layerName)
            throws WmsLayerNotFoundException {
        Dataset<?> dataset = getDatasetFromLayerName(layerName);
        String variableFromId = getVariableFromId(layerName);
        if (dataset != null && variableFromId != null) {
            return dataset.getVariableMetadata(variableFromId);
        } else {
            throw new WmsLayerNotFoundException("The layer name " + layerName
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
        /*
         * TODO If StyleDef were moved to edal-common, we could put this method
         * in a utility class in edal-common (adding Collection<StyleDef> as an
         * argument).  It's a useful method.
         */
        List<StyleDef> supportedStyles = new ArrayList<StyleDef>();
        /*
         * Loop through all loaded style definitions
         */
        for (StyleDef styleDef : styleDefs.values()) {
            /*
             * Assume the style is supported
             */
            boolean currentStyleSupported = true;
            /*
             * If this style needs the named layer, but it is not scalar (i.e.
             * has no scalar data field which can be read) then it cannot be
             * supported
             */
            if (styleDef.needsNamedLayer() && !variableMetadata.isScalar()) {
                currentStyleSupported = false;
                continue;
            }

            List<String> requiredChildren = styleDef.getRequiredChildren();
            if (requiredChildren != null && !requiredChildren.isEmpty()) {
                for (String requiredChild : requiredChildren) {
                    VariableMetadata childMetadata = variableMetadata
                            .getChildWithRole(requiredChild);
                    if (childMetadata == null || !childMetadata.isScalar()) {
                        /*
                         * We required a child layer which is either missing or
                         * not scalar
                         */
                        currentStyleSupported = false;
                        continue;
                    }
                }
            }
            if (currentStyleSupported) {
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
     * @throws WmsLayerNotFoundException
     *             If the layer specified does not exist on this WMS server
     */
    public Map<String, String> getStyleTemplateLayerNames(String layerName, String styleName)
            throws WmsLayerNotFoundException {
        StyleDef styleDef = styleDefs.get(styleName);
        Map<String, String> layerKeyToLayerName = new HashMap<String, String>();
        if (styleDef.needsNamedLayer()) {
            layerKeyToLayerName.put("layerName", layerName);
        }
        VariableMetadata layerMetadata = getVariableMetadataFromId(layerName);
        for (String childPurpose : styleDef.getRequiredChildren()) {
            layerKeyToLayerName.put(
                    "layerName-" + childPurpose,
                    getLayerName(layerMetadata.getDataset().getId(), layerMetadata
                            .getChildWithRole(childPurpose).getId()));
        }
        return layerKeyToLayerName;
    }

    /**
     * Processes an XML stream representing a style template.
     * 
     * @param name
     *            The name of the style
     * @param inputStream
     *            The input stream containing the XML
     * @return A {@link StyleDef} representing the properties of the style
     * @throws IOException
     *             If there is a problem reading the style file
     * @throws ParserConfigurationException
     *             If there is a problem parsing the XML
     * @throws SAXException
     *             If there is a problem parsing the XML
     */
    private StyleDef processStyle(String name, InputStream inputStream) throws IOException,
            ParserConfigurationException, SAXException {
        /*
         * Get the XML style definition into a NodeList
         */
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);
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

        return new StyleDef(name, requiredChildren, usesPalette, needsNamedLayer, scaledLayer);
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
    public abstract Collection<Dataset<?>> getAllDatasets();

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
    public abstract Dataset<?> getDatasetFromId(String datasetId);

    /**
     * Returns a {@link Dataset} based on a given layer name
     * 
     * @param layerName
     *            The full layer name
     * @return The desired dataset
     */
    public abstract Dataset<?> getDatasetFromLayerName(String layerName)
            throws WmsLayerNotFoundException;

    /**
     * Returns a variable ID based on a given layer name
     * 
     * @param layerName
     *            The full layer name
     * @return The ID of the variable (within its {@link Dataset})
     */
    public abstract String getVariableFromId(String layerName) throws WmsLayerNotFoundException;

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
            throws WmsLayerNotFoundException;

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
