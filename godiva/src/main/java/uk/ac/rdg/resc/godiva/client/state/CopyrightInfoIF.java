package uk.ac.rdg.resc.godiva.client.state;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Interface defining a widget to show more copyright information
 * 
 * @author Guy Griffiths
 * 
 */
public interface CopyrightInfoIF extends IsWidget {
    /**
     * Sets the copyright message
     */
    public void setCopyrightInfo(String copyright);

    /**
     * @return The copyright message
     */
    public String getCopyrightInfo();

    /**
     * Enables/disables this widget
     */
    public void setEnabled(boolean enabled);

    /**
     * @return <code>true</code> if copyright information is available
     */
    public boolean hasCopyright();
}
