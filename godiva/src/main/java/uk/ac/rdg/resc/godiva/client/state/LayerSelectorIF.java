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

package uk.ac.rdg.resc.godiva.client.state;

import java.util.List;

import uk.ac.rdg.resc.godiva.shared.LayerMenuItem;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Interface defining a single layer selection widget. This is not part of the
 * {@link GodivaStateInfo} group, and is probably not useful for a multi-layer
 * system
 * 
 * @author Guy Griffiths
 * 
 */
public interface LayerSelectorIF extends IsWidget {
    /**
     * Populate the menu tree
     * 
     * @param topItem
     *            A {@link LayerMenuItem} representing the top level menu item
     */
    public void populateLayers(LayerMenuItem topItem);

    /**
     * @return The currently selected layer ID
     */
    public String getSelectedId();

    /**
     * Sets the selected layer
     * 
     * @param id
     *            The layer ID
     * @param wmsUrl
     *            The corresponding WMS URL
     * @param autoZoomAndPalette
     *            Whether or not to automatically zoom and set the palette after
     *            selecting the layer
     */
    public void selectLayer(String id, String wmsUrl, boolean autoZoomAndPalette);

    /**
     * Enables/disables the layer selector
     */
    public void setEnabled(boolean enabled);

    /**
     * @return A list of string elements which define where we are in the menu
     *         tree. This is used for generating titles
     */
    public List<String> getTitleElements();

    /**
     * @return The WMS URL of the currently selected layer
     */
    public String getWmsUrl();
}
