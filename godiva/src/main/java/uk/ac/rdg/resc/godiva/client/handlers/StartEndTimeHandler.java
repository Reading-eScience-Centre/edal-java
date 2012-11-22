package uk.ac.rdg.resc.godiva.client.handlers;

import uk.ac.rdg.resc.godiva.client.widgets.StartEndTimePopup;

public interface StartEndTimeHandler {
    /**
     * Called when the start and end times have been set up a
     * {@link StartEndTimePopup}
     * 
     * @param startDateTime
     *            A string representing the starting datetime
     * @param endDateTime
     *            A string representing the end datetime
     */
    public void timesReceived(String startDateTime, String endDateTime);
}
