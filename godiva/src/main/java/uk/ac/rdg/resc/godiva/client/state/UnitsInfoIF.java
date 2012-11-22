package uk.ac.rdg.resc.godiva.client.state;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Interface defining a widget to display units information
 * 
 * @author Guy Griffiths
 * 
 */
public interface UnitsInfoIF extends IsWidget {
    /**
     * Set the current units
     * 
     * @param units
     *            A string representing the unit type
     */
    public void setUnits(String units);

    /**
     * Enable/disable this widget
     * 
     * @param enabled
     */
    public void setEnabled(boolean enabled);

    /**
     * Get the current units
     */
    public String getUnits();
}
