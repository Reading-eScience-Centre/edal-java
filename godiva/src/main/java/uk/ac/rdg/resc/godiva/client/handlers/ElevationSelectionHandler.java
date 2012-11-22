package uk.ac.rdg.resc.godiva.client.handlers;

import uk.ac.rdg.resc.godiva.client.widgets.ElevationSelector;

public interface ElevationSelectionHandler {
    /**
     * Called by the {@link ElevationSelector} when an elevation has been
     * selected
     * 
     * @param layerId
     *            The layer ID for which an elevation was selected
     * @param elevation
     *            The selected elevation
     */
    public void elevationSelected(String layerId, String elevation);
}
