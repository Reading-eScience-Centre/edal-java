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
