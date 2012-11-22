package uk.ac.rdg.resc.godiva.client.state;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Interface defining a time selector
 * 
 * @author Guy Griffiths
 * 
 */
public interface TimeSelectorIF extends IsWidget {
    /**
     * Sets the ID of the layer which this time selector applies to
     * 
     * @param id
     */
    public void setId(String id);

    /**
     * Populate the available dates
     * 
     * @param availableDates
     *            The available dates, as a {@link List} of {@link String}s
     */
    public void populateDates(List<String> availableDates);

    /**
     * Return the available dates
     * 
     * @return A {@link List} of {@link String}s representing the available
     *         dates
     */
    public List<String> getAvailableDates();

    /**
     * Populate the available times. This may change as the date changes
     * 
     * @param availableTimes
     *            The available times, as a {@link List} of {@link String}s
     */
    public void populateTimes(List<String> availableTimes);

    /**
     * Return the available times
     * 
     * @return A {@link List} of {@link String}s representing the available
     *         times for the selected date
     */
    public List<String> getAvailableTimes();

    /**
     * @return A {@link String} representation of the currently selected date
     */
    public String getSelectedDate();

    /**
     * @return A {@link String} representation of the currently selected
     *         datetime
     */
    public String getSelectedDateTime();

    /**
     * @return A {@link String} representation of the currently selected time
     */
    public String getSelectedTime();

    /**
     * @return A {@link String} representation of the currently selected
     *         datetime range. This may be <code>null</code> if isContinous()
     *         returns false
     */
    public String getSelectedDateTimeRange();

    /**
     * Choose a specified date
     * 
     * @param dateString
     *            The desired date
     * @return <code>true</code> if the operation was successfull
     */
    public boolean selectDate(String dateString);

    /**
     * Choose a specified datetime
     * 
     * @param dateString
     *            The desired datetime
     * @return <code>true</code> if the operation was successfull
     */
    public boolean selectDateTime(String timeString);

    /**
     * Enable/disable the widget
     * 
     * @param enabled
     *            <code>true</code> to enable the widget, <code>false</code> to
     *            disable it
     */
    public void setEnabled(boolean enabled);

    /**
     * @return <code>true</code> if this time selector can represent multiple
     *         datetimes
     */
    public boolean hasMultipleTimes();

    /**
     * Sets whether this {@link TimeSelectorIF} needs to represent discrete or
     * continous time
     * 
     * @param continuous
     */
    public void setContinuous(boolean continuous);

    /**
     * @return <code>true</code> if this {@link TimeSelectorIF} is currently
     *         representing a continuous time range, <code>false</code> if it is
     *         a discrete one
     */
    public boolean isContinuous();

    /**
     * Sets the range (i.e. +/- how much) for a continuous axis
     * 
     * @param currentRange
     */
    public void selectRange(String currentRange);

    /**
     * Gets the currently selected range (i.e. +/- how much) for a continuous
     * axis
     * 
     * @return
     */
    public String getRange();
}
