package uk.ac.rdg.resc.godiva.client.state;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Interface defining a widget which can store further information about a
 * dataset
 * 
 * @author Guy Griffiths
 * 
 */
public interface InfoIF extends IsWidget {
    /**
     * Sets the information to store
     */
    public void setInfo(String moreInfo);

    /**
     * @return The stored information
     */
    public String getInfo();

    /**
     * Enables/disables the information widget
     * 
     * @param enabled
     */
    public void setEnabled(boolean enabled);

    /**
     * @return <code>true</code> if more information is available
     */
    public boolean hasInfo();
}
