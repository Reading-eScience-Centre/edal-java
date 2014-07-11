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

import java.awt.Color;

import uk.ac.rdg.resc.edal.domain.Extent;

/**
 * Interface defining the metadata which provides default (server-configured)
 * values for layer plotting.
 * 
 * URL parameters take precedence, then values returned by this interface, then
 * basic defaults (which are unlikely to be ideal).
 * 
 * This means that it is legal for any of these methods to return
 * <code>null</code> - basic defaults will be used in these cases
 * 
 * @author Guy
 */
public interface WmsLayerMetadata {
    /**
     * @return The title of this layer to be displayed in the menu and the
     *         Capabilities document
     */
    public String getTitle();

    /**
     * @return A brief description of this layer to be displayed in the
     *         Capabilities document
     */
    public String getDescription();

    /**
     * @return The default scale range of this layer, or <code>null</code> if no
     *         scale range is set
     */
    public Extent<Float> getColorScaleRange();

    /**
     * @return The default palette to use for this layer. This can be an
     *         existing palette name, or a palette definition in the form
     *         #[aa]rrggbb,#[aa]rrggbb,#[aa]rrggbb..., where each element is a
     *         hexadecimal value
     */
    public String getPalette();

    /**
     * @return The default colour to use for values which are higher the the
     *         maximum scale value.
     */
    public Color getAboveMaxColour();

    /**
     * @return The default colour to use for values which are lower the the
     *         minimum scale value.
     */
    public Color getBelowMinColour();

    /**
     * @return The default colour to use for values which have no data.
     */
    public Color getNoDataColour();

    /**
     * @return <code>true</code> if this variable is to use logarithmic scaling
     *         by default
     */
    public Boolean isLogScaling();

    /**
     * @returns The default number of colour bands to use for this layer's
     *          palette
     */
    public Integer getNumColorBands();

    /**
     * @return Copyright information about this layer to be displayed be clients
     */
    public String getCopyright();

    /**
     * @return More information about this layer to be displayed be clients
     */
    public String getMoreInfo();

    /**
     * @return Whether or not this layer can be queried with GetFeatureInfo
     *         requests
     */
    public boolean isQueryable();

    /**
     * @return Whether or not this layer is enabled (i.e. visible in
     *         menu/GetCapabilities)
     */
    public boolean isDisabled();
}
