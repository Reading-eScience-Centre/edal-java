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
