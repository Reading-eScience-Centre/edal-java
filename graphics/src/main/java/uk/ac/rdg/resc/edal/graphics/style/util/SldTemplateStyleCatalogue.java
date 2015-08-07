/*******************************************************************************
 * Copyright (c) 2015 The University of Reading
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

package uk.ac.rdg.resc.edal.graphics.style.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.graphics.style.Drawable;
import uk.ac.rdg.resc.edal.graphics.style.Drawable.NameAndRange;
import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.graphics.style.sld.SLDException;
import uk.ac.rdg.resc.edal.graphics.style.sld.StyleSLDParser;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;

/**
 * An implementation of a {@link StyleCatalogue} which is based on having
 * resources containing Xml templates of SLD documents.
 * 
 * This uses the singleton pattern and the object is obtained with
 * {@link SldTemplateStyleCatalogue#getStyleCatalogue()}
 *
 * @author Guy Griffiths
 */
public class SldTemplateStyleCatalogue implements StyleCatalogue {
    /*
     * Because we will generally set the external style directory separately to
     * where we use this catalogue, make it a singleton
     */
    private final static SldTemplateStyleCatalogue INSTANCE = new SldTemplateStyleCatalogue();
    private static final Logger log = LoggerFactory.getLogger(StyleCatalogue.class);
    /* Velocity templating engine used for reading fixed styles */
    private VelocityEngine velocityEngine;

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

    /**
     * @return An instance of the {@link SldTemplateStyleCatalogue}
     */
    public static SldTemplateStyleCatalogue getStyleCatalogue() {
        return INSTANCE;
    }

    private SldTemplateStyleCatalogue() {
        /*
         * Initialise the velocity engine to be able to generate MapImages from
         * XML style templates
         */
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                "org.apache.velocity.runtime.log.Log4JLogChute");
        velocityEngine.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
        /*
         * We add a classpath resource loader and a file resource loader. The
         * file resource loader has no path until it is added with the
         * addStylesInDirectory() method
         */
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "class,file");
        velocityEngine.setProperty("class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        /*
         * We want the catalogue to read all possible XML templates for
         * supported styles. This includes 2 parts:
         * 
         * 1. Reading the styles which are packaged with the edal-wms module
         * (i.e. from the JAR file)
         * 
         * 2. Reading any additional styles defined by a user (i.e. from the
         * classpath)
         * 
         * Styles in an arbitrary directory can be added with the
         * addStylesInDirectory() method
         */
        NoAutoCloseZipInputStream zip = null;
        try {
            /*
             * This reads all styles from the JAR file
             */
            Pattern styleXmlPath = Pattern.compile("^styles/(.*)\\.xml$");
            CodeSource src = StyleCatalogue.class.getProtectionDomain().getCodeSource();
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
                            styleDefs.put(style.styleName, style);
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
            File stylesDir = new File(SldTemplateStyleCatalogue.class.getResource("/styles/")
                    .toURI());
            addStylesInDirectory(stylesDir);
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
    public Collection<String> getSupportedStyles(VariableMetadata variableMetadata) {
        List<String> supportedStyles = new ArrayList<>();
        /*
         * Loop through all loaded style definitions
         */
        for (StyleDef styleDef : styleDefs.values()) {
            if (styleDef.supportedBy(variableMetadata)) {
                supportedStyles.add(styleDef.styleName);
            }
        }

        return supportedStyles;
    }

    @Override
    public boolean styleUsesPalette(String styleName) {
        return styleDefs.get(styleName).usesPalette;
    }

    @Override
    public String getScaledRoleForStyle(String styleName) {
        return styleDefs.get(styleName).scaledLayerRole;
    }

    @Override
    public MapImage getMapImageFromStyle(String styleName,
            PlottingStyleParameters templateProperties, VariableMetadata metadata,
            LayerNameMapper layerNameMapper) {
        /*
         * We first try and find a resource in the styles directory - this will
         * be the case for any styles defined on the classpath / in the JAR.
         * 
         * If that fails, try without a directory prefix - this will be the case
         * for all styles in a user-added directory.
         */
        String resourceName = "styles/" + styleName.toLowerCase() + ".xml";
        if (!velocityEngine.resourceExists(resourceName)) {
            resourceName = styleName.toLowerCase() + ".xml";
        }
        Template template = velocityEngine.getTemplate(resourceName);

        /*
         * Set all of the variables for replacing in the template
         */
        VelocityContext context = new VelocityContext();
        context.put("paletteName", templateProperties.getPalette());
        Extent<Float> colourScaleRange = templateProperties.getColorScaleRange();
        context.put("scaleMin", colourScaleRange.getLow());
        context.put("scaleMax", colourScaleRange.getHigh());
        context.put("logarithmic", templateProperties.isLogScaling() ? "logarithmic" : "linear");
        context.put("numColorBands", templateProperties.getNumColorBands());
        context.put("bgColor", GraphicsUtils.colourToString(templateProperties.getNoDataColour()));
        context.put("belowMinColor",
                GraphicsUtils.colourToString(templateProperties.getBelowMinColour()));
        context.put("aboveMaxColor",
                GraphicsUtils.colourToString(templateProperties.getAboveMaxColour()));

        /*
         * Now deal with the layer names
         */
        Map<String, VariableMetadata> layerKeysToLayerNames = getStyleTemplateLayerNames(metadata,
                styleName);
        for (Entry<String, VariableMetadata> keyToLayerName : layerKeysToLayerNames.entrySet()) {
            context.put(keyToLayerName.getKey(),
                    layerNameMapper.getLayerName(metadata.getDataset().getId(), metadata.getId()));
        }

        /*
         * Process the template, replacing all parameters with their supplied
         * values
         */
        StringWriter xmlStringWriter = new StringWriter();
        template.merge(context, xmlStringWriter);
        try {
            /*
             * We now have an XML description of the style for this request.
             * Parse it into a MapImage and return the result.
             */
            return StyleSLDParser.createImage(xmlStringWriter.toString());
        } catch (SLDException e) {
            e.printStackTrace();
            /*
             * There is a problem parsing the XML
             */
            throw new EdalException("Problem parsing XML template for style " + styleName);
        }
    }

    /**
     * Adds an external directory containing styles.
     * 
     * @param stylesDir
     *            The location of the directory to add.
     * @throws FileNotFoundException
     *             If the supplied path is not a directory
     */
    public void addStylesInDirectory(File stylesDir) throws FileNotFoundException {
        Pattern styleXmlPath = Pattern.compile("^(.*)\\.xml$");
        if (stylesDir.isDirectory()) {
            for (File styleFile : stylesDir.listFiles()) {
                Matcher matcher = styleXmlPath.matcher(styleFile.getName());
                if (matcher.matches()) {
                    StyleDef style = null;
                    try {
                        style = processStyle(matcher.group(1),
                                IOUtils.toString(new FileInputStream(styleFile)));
                    } catch (IOException | ParserConfigurationException | SAXException e) {
                        log.error("Problem processing styles on classpath", e);
                    }
                    if (style != null) {
                        styleDefs.put(style.styleName, style);
                    }
                }
            }
            velocityEngine.addProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH,
                    stylesDir.getAbsolutePath());
        } else {
            log.error("User tried to add a styles directory which was not a directory: "
                    + stylesDir.getAbsolutePath());
            throw new FileNotFoundException("The path " + stylesDir.getAbsolutePath()
                    + " is not a directory");
        }
    }

    /**
     * Returns a {@link Map} of keys used in style templates to the specific
     * {@link VariableMetadata} objects which they represent. Since a style
     * template may refer to child layers, we need a way to map the keys for
     * child layers to the concrete variables which they represent
     * 
     * This implementation takes the {@link VariableMetadata} of the data layer
     * referred to as <code>$layerName</code> in the template and finds the
     * necessary child {@link VariableMetadata} objects which are required for
     * the given style name.
     * 
     * For example, if <code>$layerName</code> refers to a parent layer which
     * groups vector components, and the style template defines
     * <code>$layerName-mag</code> and <code>$layerName-dir</code> this method
     * will return a {@link Map} of <code>$layerName-mag</code> and
     * <code>$layerName-dir</code> to the {@link VariableMetadata} objects
     * representing those quantities.
     * 
     * @param namedMetadata
     *            The {@link VariableMetadata} of the main layer being requested
     * @param styleName
     *            The style name to be plotted
     * @return A {@link Map} of keys used in style templates to the
     *         {@link VariableMetadata} objects they represent.
     */
    private Map<String, VariableMetadata> getStyleTemplateLayerNames(
            VariableMetadata namedMetadata, String styleName) {
        StyleDef styleDef = styleDefs.get(styleName);
        Map<String, VariableMetadata> layerKeyToLayerName = new HashMap<>();
        if (styleDef.needsNamedLayer) {
            layerKeyToLayerName.put("layerName", namedMetadata);
        }
        for (String childPurpose : styleDef.requiredChildRoles) {
            VariableMetadata childWithRole = namedMetadata.getChildWithRole(childPurpose);
            if (childWithRole != null) {
                layerKeyToLayerName.put("layerName-" + childPurpose, childWithRole);
            } else {
                /*
                 * TODO throw exception here
                 */
            }
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
    private static StyleDef processStyle(String name, String xmlString) throws IOException,
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
    private static Node findScaleMinNode(NodeList nodes) {
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
    private static String getScaledLayerName(Node scaleMinNode) {
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
    private static String recursivelyCheckChildrenForLayerName(Node parentNode) {
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
    private static boolean styleContainsTag(NodeList nodes, String regexp) {
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
    private static void populateStyleChildRoles(NodeList nodes, Set<String> childRoles) {
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
     * Definition of a style. This includes properties we need to know to
     * determine whether a particular variable can support this plotting style,
     * and how to advertise it.
     * 
     * @author Guy Griffiths
     */
    private static class StyleDef {
        /**
         * @return The name of this style
         */
        private String styleName;
        /**
         * @return A {@link List} of the roles which the named layer needs its
         *         children to have.
         */
        private List<String> requiredChildRoles;
        /**
         * @return Whether this style uses a palette
         */
        private boolean usesPalette;
        /**
         * @return Whether this style needs the named layer to be scalar (if not
         *         it just uses children of the named layer)
         */
        private boolean needsNamedLayer;
        /**
         * @return The role of the layer which has <code>$scaleMin</code> and
         *         <code>$scaleMax</code> applied to it. This will return an
         *         empty string if the parent layer is the scaled one, and
         *         <code>null</code> if no layers use the scale information.
         */
        private String scaledLayerRole;
        private Collection<Map<String, Collection<Class<? extends Feature<?>>>>> roles2FeatureType;

        /**
         * Instantiate a new {@link StyleDef}
         * 
         * @param styleName
         *            The name of the style
         * @param requiredChildren
         *            A {@link Set} of child layers which this style needs (e.g.
         *            a vector style will need "mag" and "dir" children")
         * @param usesPalette
         *            Whether or not this style uses a named palette
         * @param needsNamedLayer
         *            Whether this style needs the requested layer to plot. For
         *            example, a vector style <i>only</i> needs child members to
         *            plot, so this would be <code>false</code>
         * @param roles2FeatureType
         *            A {@link Collection} where each item in it represents a
         *            layer in the {@link MapImage} which this style represents.
         * 
         *            Each layer requires a {@link Map} of role name to
         *            supported feature types, so that we can check whether a
         *            variable which fulfils a given role can produce one of the
         *            supported feature types.
         */
        public StyleDef(String styleName, Collection<String> requiredChildren, boolean usesPalette,
                boolean needsNamedLayer, String scaledLayerRole,
                Collection<Map<String, Collection<Class<? extends Feature<?>>>>> roles2FeatureType) {
            super();
            this.styleName = styleName;
            this.requiredChildRoles = new ArrayList<String>(requiredChildren);
            this.usesPalette = usesPalette;
            this.needsNamedLayer = needsNamedLayer;
            this.scaledLayerRole = scaledLayerRole;
            this.roles2FeatureType = roles2FeatureType;
        }

        @Override
        public String toString() {
            return styleName;
        }

        /**
         * Tests whether this style is supported by a given variable
         * 
         * @param variableMetadata
         *            The {@link VariableMetadata} representing the variable to
         *            test
         * @return <code>true</code> if the style can be supported
         */
        public boolean supportedBy(VariableMetadata variableMetadata) {
            if (variableMetadata == null) {
                return false;
            }
            if (needsNamedLayer) {
                /*
                 * If this style needs the named layer, but it is not scalar
                 * (i.e. has no scalar data field which can be read) then it
                 * cannot be supported.
                 */
                if (!variableMetadata.isScalar()) {
                    return false;
                }
                /*
                 * We check that the feature type supported by this
                 * VariableMetadata is compatible with the layers which the
                 * named layer is required in.
                 */
                if (!styleSupportsRoleAndFeatureType("", variableMetadata.getDataset()
                        .getMapFeatureType(variableMetadata.getId()))) {
                    return false;
                }
            }

            if (requiredChildRoles != null && !requiredChildRoles.isEmpty()) {
                for (String requiredRole : requiredChildRoles) {
                    VariableMetadata childMetadata = variableMetadata
                            .getChildWithRole(requiredRole);
                    if (childMetadata == null || !childMetadata.isScalar()) {
                        /*
                         * We required a child layer which is either missing or
                         * not scalar. This style is not supported by the
                         * supplied metadata.
                         */
                        return false;
                    }
                    try {
                        if (!styleSupportsRoleAndFeatureType(requiredRole, childMetadata
                                .getDataset().getMapFeatureType(childMetadata.getId()))) {
                            /*
                             * We need the child metadata to support a feature
                             * type which it does not.
                             */
                            return false;
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
            return true;
        }

        /**
         * This method checks to see whether a particular combination of role
         * and feature type are supported by this style
         * 
         * @param role
         *            The name of the role to test ("" or <code>null</code> for
         *            parent layer)
         * @param featureType
         *            The {@link Class} of the corresponding feature type
         *            supported by this role
         * @return <code>true</code> if the given role/feature type are
         *         supported
         */
        private boolean styleSupportsRoleAndFeatureType(String role,
                Class<? extends Feature<?>> featureType) {

            for (Map<String, Collection<Class<? extends Feature<?>>>> role2FeatureTypes : roles2FeatureType) {
                Collection<Class<? extends Feature<?>>> supportedFeatures = role2FeatureTypes
                        .get(role);
                if (supportedFeatures != null) {
                    /*
                     * This role is required in the current image layer.
                     * 
                     * Therefore it needs to map to one of the feature types
                     * supported by this image layer.
                     */
                    boolean supportsFeatureType = false;
                    for (Class<? extends Feature<?>> clazz : supportedFeatures) {
                        if (clazz.isAssignableFrom(featureType)) {
                            /*
                             * We have found that the feature type associated
                             * with the given role is compatible with one of the
                             * feature types supported by the layer
                             */
                            supportsFeatureType = true;
                            break;
                        }
                    }

                    /*
                     * We have checked all possible supported feature types. If
                     * none are supported, this role has failed in the current
                     * image layer. This is enough to mark it as a failure.
                     * 
                     * To mark it as a success, we need to continue checking all
                     * layers in the image.
                     */
                    if (!supportsFeatureType) {
                        return false;
                    }
                }
                /*
                 * Implied:
                 * 
                 * else {
                 * 
                 * This role is not required in the current layer of the image.
                 * 
                 * Continue checking the rest of the image layers
                 * 
                 * }
                 */
            }
            return true;
        }
    }

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
    private static class NoAutoCloseZipInputStream extends ZipInputStream {
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
