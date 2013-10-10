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

package uk.ac.rdg.resc.godiva.client.handlers;

public interface PaletteSelectionHandler {
    /**
     * Called when the palette has changed in some way
     * 
     * @param layerId
     *            The layer for which the palette has changed
     * @param paletteName
     *            The name of the palette
     * @param style
     *            The name of the style
     * @param nColorBands
     *            The number of colour bands
     */
    public void paletteChanged(String layerId, String paletteName, String style, int nColorBands);

    /**
     * Called when the scale range has been adjusted
     * 
     * @param layerId
     *            The layer for which the scale range has changed
     * @param scaleRange
     *            A string representing the new scale range
     */
    public void scaleRangeChanged(String layerId, String scaleRange);

    /**
     * Called when the linear/log scale setting has been changed
     * 
     * @param layerId
     *            The layer for which the linear/log setting has changed
     * @param newIsLogScale
     *            true if the new state is logarithmic, false if it is linear
     */
    public void logScaleChanged(String layerId, boolean newIsLogScale);

    /**
     * Called when a user wants to automatically adjust the palette range
     * 
     * @param layerId
     *            The layer for which the palette should be adjusted
     */
    public void autoAdjustPalette(String layerId);
    
    /**
     * Called when the opacity of a layer is set
     * 
     * @param layerId
     *            The layer to set the opacity of
     * @param opacity
     *            The opacity as a float ranging from 0-1
     */
    public void setOpacity(String layerId, float opacity);
}
