package uk.ac.rdg.resc.godiva.client.widgets;

import com.google.gwt.user.client.ui.IsWidget;

public interface InfoIF extends IsWidget {
    public void setInfo(String moreInfo);
    public String getInfo();
    public void setEnabled(boolean enabled);
    public boolean hasInfo();
}
