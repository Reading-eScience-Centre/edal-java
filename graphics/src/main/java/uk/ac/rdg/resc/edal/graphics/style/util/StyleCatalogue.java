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

package uk.ac.rdg.resc.edal.graphics.style.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;

/**
 * @author Guy Griffiths
 */
public interface StyleCatalogue {
    public List<StyleDef> getSupportedStyles(VariableMetadata variableMetadata);

    /**
     * Returns a {@link Map} of keys used in XML templates to the specific
     * {@link VariableMetadata} objects which they represent. Specifically this
     * takes the {@link VariableMetadata} of the data layer referred to as
     * <code>"$layerName"</code> in the XML and finds the necessary child
     * {@link VariableMetadata} objects which are required for the given style
     * name.
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
     * @return A {@link Map} of keys used in XML templates to the
     *         {@link VariableMetadata} objects they represent.
     */
    public Map<String, VariableMetadata> getStyleTemplateLayerNames(VariableMetadata namedMetadata,
            String styleName);

    /**
     * Returns the {@link StyleDef} object with the given name
     * 
     * @param styleName
     *            The name of the style
     * @return A {@link StyleDef} object representing the style, or
     *         <code>null</code> if it doesn't exist
     */
    public StyleDef getStyleDefinitionByName(String styleName);
    
    /**
     * Definition of a style. This includes properties we need to know to determine
     * whether a particular variable can support this plotting style, and how to
     * advertise it.
     * 
     * @author Guy Griffiths
     */
    public class StyleDef {
        private String styleName;
        private List<String> requiredChildren;
        private boolean usesPalette;
        private boolean needsNamedLayer;
        private String scaledLayerRole;
        private Collection<Map<String, Collection<Class<? extends Feature<?>>>>> roles2FeatureType;

        /**
         * Instantiate a new {@link StyleDef}
         * 
         * @param styleName
         *            The name of the style
         * @param requiredChildren
         *            A {@link Set} of child layers which this style needs (e.g. a
         *            vector style will need "mag" and "dir" children")
         * @param usesPalette
         *            Whether or not this style uses a named palette
         * @param needsNamedLayer
         *            Whether this style needs the requested layer to plot. For
         *            example, a vector style <i>only</i> needs child members to
         *            plot, so this would be <code>false</code>
         * @param roles2FeatureType
         *            A {@link Collection} where each item in it represents a layer
         *            in the {@link MapImage} which this style represents.
         * 
         *            Each layer requires a {@link Map} of role name to supported
         *            feature types, so that we can check whether a variable which
         *            fulfils a given role can produce one of the supported feature
         *            types.
         */
        public StyleDef(String styleName, Collection<String> requiredChildren, boolean usesPalette,
                boolean needsNamedLayer, String scaledLayerRole,
                Collection<Map<String, Collection<Class<? extends Feature<?>>>>> roles2FeatureType) {
            super();
            this.styleName = styleName;
            this.requiredChildren = new ArrayList<String>(requiredChildren);
            this.usesPalette = usesPalette;
            this.needsNamedLayer = needsNamedLayer;
            this.scaledLayerRole = scaledLayerRole;
            this.roles2FeatureType = roles2FeatureType;
        }

        /**
         * @return The name of this style
         */
        public String getStyleName() {
            return styleName;
        }

        /**
         * @return A {@link List} of the roles which the named layer needs its
         *         children to have.
         */
        public List<String> getRequiredChildRoles() {
            return requiredChildren;
        }

        /**
         * @return Whether this style uses a palette
         */
        public boolean usesPalette() {
            return usesPalette;
        }

        /**
         * @return Whether this style needs the named layer to be scalar (if not it
         *         just uses children of the named layer)
         */
        public boolean needsNamedLayer() {
            return needsNamedLayer;
        }

        /**
         * @return The role of the layer which has <code>$scaleMin</code> and
         *         <code>$scaleMax</code> applied to it. This will return an empty
         *         string if the parent layer is the scaled one, and
         *         <code>null</code> if no layers use the scale information.
         */
        public String getScaledLayerRole() {
            return scaledLayerRole;
        }

        @Override
        public String toString() {
            return styleName;
        }

        /**
         * Tests whether this style is supported by a given variable
         * 
         * @param variableMetadata
         *            The {@link VariableMetadata} representing the variable to test
         * @return <code>true</code> if the style can be supported
         */
        public boolean supportedBy(VariableMetadata variableMetadata) {
            if (variableMetadata == null) {
                return false;
            }
            if (needsNamedLayer()) {
                /*
                 * If this style needs the named layer, but it is not scalar (i.e.
                 * has no scalar data field which can be read) then it cannot be
                 * supported.
                 */
                if (!variableMetadata.isScalar()) {
                    return false;
                }
                /*
                 * We check that the feature type supported by this VariableMetadata
                 * is compatible with the layers which the named layer is required
                 * in.
                 */
                if (!styleSupportsRoleAndFeatureType("", variableMetadata.getDataset()
                        .getMapFeatureType(variableMetadata.getId()))) {
                    return false;
                }
            }

            List<String> requiredChildRoles = getRequiredChildRoles();
            if (requiredChildRoles != null && !requiredChildRoles.isEmpty()) {
                for (String requiredRole : requiredChildRoles) {
                    VariableMetadata childMetadata = variableMetadata.getChildWithRole(requiredRole);
                    if (childMetadata == null || !childMetadata.isScalar()) {
                        /*
                         * We required a child layer which is either missing or not
                         * scalar. This style is not supported by the supplied
                         * metadata.
                         */
                        return false;
                    }
                    try {
                        if (!styleSupportsRoleAndFeatureType(requiredRole, childMetadata.getDataset()
                                .getMapFeatureType(childMetadata.getId()))) {
                            /*
                             * We need the child metadata to support a feature type
                             * which it does not.
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
         * This method checks to see whether a particular combination of role and
         * feature type are supported by this style
         * 
         * @param role
         *            The name of the role to test ("" or <code>null</code> for
         *            parent layer)
         * @param featureType
         *            The {@link Class} of the corresponding feature type supported
         *            by this role
         * @return <code>true</code> if the given role/feature type are supported
         */
        private boolean styleSupportsRoleAndFeatureType(String role,
                Class<? extends Feature<?>> featureType) {

            for (Map<String, Collection<Class<? extends Feature<?>>>> role2FeatureTypes : roles2FeatureType) {
                Collection<Class<? extends Feature<?>>> supportedFeatures = role2FeatureTypes.get(role);
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
                             * We have found that the feature type associated with
                             * the given role is compatible with one of the feature
                             * types supported by the layer
                             */
                            supportsFeatureType = true;
                            break;
                        }
                    }

                    /*
                     * We have checked all possible supported feature types. If none
                     * are supported, this role has failed in the current image
                     * layer. This is enough to mark it as a failure.
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

}
