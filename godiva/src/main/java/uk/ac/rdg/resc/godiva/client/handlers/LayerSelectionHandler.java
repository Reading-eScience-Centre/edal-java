package uk.ac.rdg.resc.godiva.client.handlers;

public interface LayerSelectionHandler {
    public void layerSelected(String wmsUrl, String layerName, boolean autoZoomAndPalette);
    public void layerDeselected(String layerName);
    public void refreshLayerList();
}
