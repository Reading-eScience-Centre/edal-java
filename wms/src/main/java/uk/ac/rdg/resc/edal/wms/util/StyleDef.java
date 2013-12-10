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

package uk.ac.rdg.resc.edal.wms.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Definition of a style. This includes properties we need to know to determine
 * whether a particular variable can support this plotting style, and how to
 * advertise it
 * 
 * @author Guy Griffiths
 */
public class StyleDef {
    private String styleName;
    private List<String> requiredChildren;
    private boolean usesPalette;
    private boolean needsNamedLayer;
    private String scaledLayerRole;

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
     */
    public StyleDef(String styleName, Collection<String> requiredChildren, boolean usesPalette,
            boolean needsNamedLayer, String scaledLayerRole) {
        super();
        this.styleName = styleName;
        this.requiredChildren = new ArrayList<String>(requiredChildren);
        this.usesPalette = usesPalette;
        this.needsNamedLayer = needsNamedLayer;
        this.scaledLayerRole = scaledLayerRole;
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
    public List<String> getRequiredChildren() {
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
     * @return The role of the layer which has $scaleMin and $scaleMax applied
     *         to it. This will return an empty string if the parent layer is
     *         the scaled one, and <code>null</code> if no layers use the scale
     *         information.
     */
    public String getScaledLayerRole() {
        return scaledLayerRole;
    }

    @Override
    public String toString() {
        return styleName;
    }
}
