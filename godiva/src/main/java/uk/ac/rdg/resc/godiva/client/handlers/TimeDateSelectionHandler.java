package uk.ac.rdg.resc.godiva.client.handlers;

public interface TimeDateSelectionHandler {
    /**
     * Called when a date is selected
     * 
     * @param layerId
     *            The layer on which a date has been chosen
     * @param selectedDate
     *            A string representing the selected date
     */
    public void dateSelected(String layerId, String selectedDate);

    /**
     * Called when a time is selected
     * 
     * @param layerId
     *            The layer on which a time has been chosen
     * @param selectedTime
     *            A string representing the selected datetime
     */
    public void datetimeSelected(String layerId, String selectedDatetime);
}
