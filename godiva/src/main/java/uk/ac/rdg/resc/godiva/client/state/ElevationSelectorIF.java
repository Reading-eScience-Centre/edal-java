package uk.ac.rdg.resc.godiva.client.state;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Interface defining a widget for selecting elevation
 * 
 * @author Guy Griffiths
 * 
 */
public interface ElevationSelectorIF extends IsWidget {
    /**
     * Sets the layer ID which this widget is referring to
     */
    public void setId(String id);

    /**
     * Populates the available elevations
     * 
     * @param availableElevations
     *            The available elevations, represented as a {@link List} of
     *            {@link String}s
     */
    public void populateElevations(List<String> availableElevations);

    /**
     * Sets the units and direction of the vertical axis
     * 
     * @param units
     *            A string representing the units
     * @param positive
     *            <code>true</code> if increasing elevation value means
     *            increasing height. <code>false</code> if increasing elevation
     *            value means increasing depth
     */
    public void setUnitsAndDirection(String units, boolean positive);

    /**
     * Gets the currently selected elevation as a {@link String}
     */
    public String getSelectedElevation();

    /**
     * Gets the currently selected elevation range if applicable
     */
    public String getSelectedElevationRange();

    /**
     * Sets the current elevation to the desired value
     */
    public void setSelectedElevation(String currentElevation);

    /**
     * Enables/disables the elevation selector
     */
    public void setEnabled(boolean enabled);

    /**
     * @return The number of available elevations
     */
    public int getNElevations();

    /**
     * @return The units of the vertical axis
     */
    public String getVerticalUnits();

    /**
     * Sets this elevation selector to be continuous
     */
    public void setContinuous(boolean continuous);

    /**
     * @return <code>true</code> if this currently represents a continuous
     *         vertical axis
     */
    public boolean isContinuous();
}
