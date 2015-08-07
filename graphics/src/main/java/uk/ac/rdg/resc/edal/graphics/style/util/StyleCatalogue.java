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

import java.util.Collection;
import java.util.List;

import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;

/**
 * A catalogue of supported styles for plotting. This allows for a catalogue of
 * styles to be defined, and provides a method to turn these styles into
 * concrete {@link MapImage} objects ready to be plotted.
 * 
 * @author Guy Griffiths
 */
public interface StyleCatalogue {
    /**
     * Gets the supported styles for a given variable
     * 
     * @param variableMetadata
     *            The {@link VariableMetadata} of the variable to get styles for
     * @return A {@link List} of the supported style names for this variable
     */
    public Collection<String> getSupportedStyles(VariableMetadata variableMetadata);

    /**
     * @param styleName
     *            The style name to test
     * @return <code>true</code> if this style needs a colour palette
     */
    public boolean styleUsesPalette(String styleName);

    /**
     * @param styleName
     *            The style name to test
     * @return The role which the scale range applies to. This is:
     * 
     *         <li><code>null</code> if no scaling takes place
     * 
     *         <li>An empty string if the named layer is scaled
     * 
     *         <li>The child role which scaling is applied to
     */
    public String getScaledRoleForStyle(String styleName);

    /**
     * Returns a concrete {@link MapImage} for a given style
     * 
     * @param styleName
     *            The name of the style
     * @param templateableProperties
     *            The properties which should be injected into the style
     *            template
     * @param metadata
     *            The {@link VariableMetadata} of the variable being plotted. In
     *            the case that multiple variables are used for a single plot,
     *            this should be the parent {@link VariableMetadata} of all
     *            required variables
     * @param layerNameMapper
     *            The associated {@link LayerNameMapper}. This is used to turn
     *            {@link VariableMetadata} objects into concrete layer names for
     *            the style template
     * @return A {@link MapImage}, ready to generate images
     */
    public MapImage getMapImageFromStyle(String styleName,
            PlottingStyleParameters templateableProperties, VariableMetadata metadata,
            LayerNameMapper layerNameMapper);
}
