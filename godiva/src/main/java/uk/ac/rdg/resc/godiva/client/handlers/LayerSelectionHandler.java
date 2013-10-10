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

public interface LayerSelectionHandler {
    /**
     * Called when a layer is selected
     * 
     * @param wmsUrl
     *            The WMS URL where the layer is present
     * @param layerId
     *            The ID of the layer on the WMS server
     * @param autoZoomAndPalette
     *            Whether we want to automatically zoom to the layer extents and
     *            adjust the palette. Note that the palette will not be adjusted
     *            if a range has been set on the server
     */
    public void layerSelected(String wmsUrl, String layerId, boolean autoZoomAndPalette);

    /**
     * Called when a layer is deselected. This is only likely to happen on
     * multi-layer systems
     * 
     * TODO perhaps this will need a wmsUrl to uniquely locate the layer?
     * Currently we have no multi-layer systems on which to test this, so it's
     * not really important. This comment should be removed when one gets
     * implemented
     * 
     * @param layerId
     *            The ID of the layer which has been deselected
     * 
     */
    public void layerDeselected(String layerId);

    /**
     * Called when the user would like the list of layers to be refreshed
     */
    public void refreshLayerList();
}
