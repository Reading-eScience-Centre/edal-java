package uk.ac.rdg.resc.godiva.client.state;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Interface defining a palette selector widget
 * 
 * @author Guy Griffiths
 * 
 */
public interface PaletteSelectorIF extends IsWidget {
    /**
     * Sets the layer ID which this palette selector is currently active on
     * 
     * @param id
     *            The ID
     */
    public void setId(String id);

    /**
     * Populates the list of available palette names
     */
    public void populatePalettes(List<String> availablePalettes);

    /**
     * @return The name of the currently selected palette
     */
    public String getSelectedPalette();

    /**
     * Select the named palette, if available
     * 
     * @param paletteString
     *            The name of the palette to select
     */
    public void selectPalette(String paletteString);

    /**
     * Populates the list of available style names
     */
    public void populateStyles(List<String> availableStyles);

    /**
     * @return The name of the currently selected style
     */
    public String getSelectedStyle();

    /**
     * Select the named style, if available
     * 
     * @param styleString
     *            The name of the style to select
     */
    public void selectStyle(String styleString);

    /**
     * Sets the scale range
     * 
     * @param scaleRange
     *            The desired scale range, of the form [min],[max]
     * @param logScale
     *            Whether we want this to be a log scale or not
     * @return <code>true</code> if the operation was successful (may not be if
     *         the palette is locked, or we want a negative value in a
     *         logarithmic range)
     */
    public boolean setScaleRange(String scaleRange, Boolean logScale);

    /**
     * @return A {@link String} of the form "[min],[max]" which represents the
     *         current scale range
     */
    public String getScaleRange();

    /**
     * @return The number of colour bands in the currently selected palette
     */
    public int getNumColorBands();

    /**
     * Sets the number of colour bands in the current palette
     */
    public void setNumColorBands(int nBands);

    /**
     * @return Whether or not this is a logarithmic scale
     */
    public boolean isLogScale();

    /**
     * @return Whether or not the palette is locked
     */
    public boolean isLocked();

    /**
     * Enables/disables the palette selector
     */
    public void setEnabled(boolean enabled);

    /**
     * @return Whether or not the current palette selector is enabled
     */
    public boolean isEnabled();

    /**
     * @return The opacity of the current palette as a float ranging from 0-1
     */
    public float getOpacity();

    /**
     * Sets the opacity of the current palette
     * 
     * @param opacity
     *            The opacity, as a float ranging from 0-1
     */
    public void setOpacity(float opacity);
}
