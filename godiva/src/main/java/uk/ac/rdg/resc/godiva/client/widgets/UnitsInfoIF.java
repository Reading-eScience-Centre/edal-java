package uk.ac.rdg.resc.godiva.client.widgets;

import com.google.gwt.user.client.ui.IsWidget;

public interface UnitsInfoIF extends IsWidget {
    public void setUnits(String units);
    public void setEnabled(boolean enabled);
    public String getUnits();
}
