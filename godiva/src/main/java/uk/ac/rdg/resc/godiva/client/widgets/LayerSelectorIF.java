package uk.ac.rdg.resc.godiva.client.widgets;

import java.util.List;

import uk.ac.rdg.resc.godiva.client.requests.LayerMenuItem;

import com.google.gwt.user.client.ui.IsWidget;

public interface LayerSelectorIF extends IsWidget {
    public void populateLayers(LayerMenuItem topItem);
    public List<String> getSelectedIds();
    public void selectLayer(String id, boolean autoZoomAndPalette);
    public void setEnabled(boolean enabled);
    public List<String> getTitleElements();
    public String getWmsUrl();
}
