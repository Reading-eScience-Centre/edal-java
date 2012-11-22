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
     * Gets the WMS URL of the currently selected layer
     * 
     * @return
     */
    public String getWmsUrl();
}
