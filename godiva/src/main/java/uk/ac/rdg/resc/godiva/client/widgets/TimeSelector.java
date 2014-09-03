/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc.godiva.client.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import uk.ac.rdg.resc.godiva.client.handlers.TimeDateSelectionHandler;
import uk.ac.rdg.resc.godiva.client.state.TimeSelectorIF;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Implementation of {@link TimeSelectorIF} which presents dates and times (and
 * ranges where applicable) as separate drop-down boxes
 * 
 * @author Guy Griffiths
 * 
 */
public class TimeSelector extends BaseSelector implements TimeSelectorIF {
    protected static final String[] allTimes = new String[24];

    static {
        /*
         * allTimes used for continuous axis
         */
        NumberFormat format = NumberFormat.getFormat("00");
        for (int i = 0; i < 24; i++) {
            allTimes[i] = format.format(i) + ":00:00.000Z";
        }
    }

    private ListBox dates;
    private ListBox times;
    private String id;
    private TimeDateSelectionHandler handler;

    /*
     * These are used when we have a continuous time axis
     */
    private boolean continuous = false;
    private ListBox range;
    private Label rangeLabel;
    /*
     * These are the start time (on the first date) and end time (on the last
     * date) so that correct limits can be set
     */
    private String startTime;
    private String endTime;
    private static final DateTimeFormat datePrinter = DateTimeFormat.getFormat("yyyy-MM-dd");;

    public TimeSelector(String id, final TimeDateSelectionHandler handler) {
        this(id, "Time", handler);
    }

    public TimeSelector(String id, String label, final TimeDateSelectionHandler handler) {
        super(label);
        this.id = id;
        this.handler = handler;
        initDiscrete();
    }

    /*
     * Sets up widget for discrete use. Called whenever we change from
     * continuous to discrete
     */
    private void initDiscrete() {
        dates = new ListBox();
        dates.setName("date_selector");
        dates.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                dateSelected();
            }
        });
        dates.setTitle("Adjust the date");
        add(dates);

        times = new ListBox();
        times.setName("time_selector");
        times.setTitle("Adjust the time");
        times.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                handler.datetimeSelected(id, getSelectedDateTime());
            }
        });
        add(times);
    }

    private void dateSelected() {
        if (continuous) {
            /*
             * If we have a continuous axis, selecting the date should lead to a
             * map update
             */

            String targetTime = null;
            if (times.getItemCount() > 0) {
                targetTime = times.getValue(times.getSelectedIndex());
            }
            String targetRange = null;
            if (range.getItemCount() > 0) {
                targetRange = range.getValue(range.getSelectedIndex());
            }
            times.clear();
            if (dates.getSelectedIndex() == 0) {
                for (String item : allTimes) {
                    if (item.compareTo(startTime) >= 0)
                        times.addItem(item);
                }
            } else if (dates.getSelectedIndex() == (dates.getItemCount() - 1)) {
                for (String item : allTimes) {
                    if (item.compareTo(endTime) <= 0)
                        times.addItem(item);
                }
            } else {
                for (String item : allTimes) {
                    times.addItem(item);
                }
            }

            for (int i = 0; i < times.getItemCount(); i++) {
                if (times.getValue(i).equals(targetTime)) {
                    times.setSelectedIndex(i);
                }
            }

            for (int i = 0; i < range.getItemCount(); i++) {
                if (range.getValue(i).equals(targetRange)) {
                    range.setSelectedIndex(i);
                }
            }

            handler.datetimeSelected(id, getSelectedDateTime());
        } else {
            /*
             * Otherwise we need to request the available timesteps
             */
            handler.dateSelected(id, getSelectedDate());
        }
    }

    /*
     * Sets up the widget for continuous use.
     */
    private void initContinuous() {
        /*
         * We need all of the same widgets as for the discrete case
         */
        initDiscrete();

        /*
         * Then we also need a time range
         */
        rangeLabel = new Label("+/-");
        add(rangeLabel);
        range = new ListBox();
        range.setName("range_selector");
        range.setTitle("Choose time window to display");
        range.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                handler.datetimeSelected(id, getSelectedDateTime());
            }
        });
        range.addItem("1 hour", "" + 1000L * 60 * 60);
        range.addItem("1 day", "" + 1000L * 60 * 60 * 24);
        range.addItem("1 week", "" + 1000L * 60 * 60 * 24 * 7);
        range.addItem("1 month", "" + 1000L * 60 * 60 * 24 * 31);
        range.addItem("6 months", "" + 1000L * 60 * 60 * 24 * 31 * 6);
        range.addItem("1 year", "" + 1000L * 60 * 60 * 24 * 365);
        range.setEnabled(true);
        range.setSelectedIndex(1);
        add(range);
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void populateDates(List<String> availableDatetimes) {
        dates.clear();
        times.clear();
        if (availableDatetimes == null || availableDatetimes.size() == 0) {
            dates.setEnabled(false);
            times.setEnabled(false);
            label.addStyleDependentName("inactive");
        } else {
            if (continuous) {
                /*
                 * For a continuous time axis, we only need the start and end
                 * times
                 */
                if (availableDatetimes.size() != 2) {
                    throw new IllegalArgumentException(
                            "For a continuous time axis, you must provide exactly 2 dates");
                }

                /*
                 * Datetimes will be in ISO8601 format, so this bit just
                 * extracts the time string
                 */
                startTime = availableDatetimes.get(0).substring(11);
                endTime = availableDatetimes.get(1).substring(11);

                availableDatetimes = getDatesInRange(availableDatetimes.get(0),
                        availableDatetimes.get(1));

                int i = 0;
                int selectDate = 0;
                String nowString = datePrinter.format(new Date());
                for (String item : availableDatetimes) {
                    if (item.compareTo(nowString) < 0) {
                        selectDate = i;
                    }
                    i++;
                    dates.addItem(item);
                }
                dates.setEnabled(true);
                dates.setSelectedIndex(selectDate);

                label.removeStyleDependentName("inactive");
                /*
                 * Now fire a change event to populate the times
                 */
                DomEvent.fireNativeEvent(Document.get().createChangeEvent(), dates);
            } else {
                Collections.sort(availableDatetimes);
                for (String item : availableDatetimes) {
                    dates.addItem(item, URL.encodePathSegment(item));
                }
                dates.setEnabled(true);
                label.removeStyleDependentName("inactive");
            }
        }
    }

    @Override
    public void populateTimes(List<String> availableTimes) {
        times.clear();

        if (availableTimes == null || availableTimes.size() == 0 || !dates.isEnabled()) {
            times.setEnabled(false);
        } else {
            Collections.sort(availableTimes);
            for (String item : availableTimes) {
                times.addItem(item, URL.encodePathSegment(item));
            }
            if (availableTimes.size() > 1) {
                times.setEnabled(true);
            } else {
                times.setEnabled(false);
            }
        }
    }

    @Override
    public String getSelectedDate() {
        int i = dates.getSelectedIndex();
        if (i != -1) {
            return dates.getValue(i);
        } else {
            return null;
        }
    }

    @Override
    public String getSelectedDateTime() {
        int i = dates.getSelectedIndex();
        int j = times.getSelectedIndex();
        // TODO Look at this more carefully for case when no times are present
        if (i != -1 && j != -1) {
            /*
             * TODO Maybe the Z will cause issues?
             */
            return dates.getValue(i) + "T" + times.getValue(j);
        } else {
            return null;
        }
    }

    @Override
    public String getSelectedDateTimeRange() {
        if (!continuous) {
            return null;
        } else {
            int i = dates.getSelectedIndex();
            int j = times.getSelectedIndex();
            int k = range.getSelectedIndex();
            // TODO Look at this more carefully for case when no times are
            // present
            if (i != -1 && j != -1 && k != -1) {
                return getRangeString(getSelectedDateTime(), range.getValue(k));
            } else {
                return null;
            }
        }
    }

    private String getRangeString(String datetime, String rangeStr) {
        DateTimeFormat parser = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
        Date centreDate = parser.parse(datetime);
        long range = Long.parseLong(rangeStr);
        Date startDate = new Date(centreDate.getTime() - range);
        Date endDate = new Date(centreDate.getTime() + range);
        return parser.format(startDate) + "/" + parser.format(endDate);
    }

    @Override
    public String getSelectedTime() {
        int i = times.getSelectedIndex();
        // TODO Look at this more carefully for case when no times are present
        if (i != -1) {
            return times.getValue(i);
        } else {
            return null;
        }
    }

    @Override
    public boolean selectDate(String dateString) {
        for (int i = 0; i < dates.getItemCount(); i++) {
            if (dates.getValue(i).equals(dateString)) {
                dates.setSelectedIndex(i);
                dateSelected();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean selectDateTime(String timeString) {
        boolean dateValid = false;
        for (int i = 0; i < dates.getItemCount(); i++) {
            if (dates.getValue(i).equals(timeString.substring(0, 10))) {
                dates.setSelectedIndex(i);
                dateValid = true;
            }
        }
        if (dateValid) {
            String time = URL.encodePathSegment(timeString.substring(11));
            for (int i = 0; i < times.getItemCount(); i++) {
                if (times.getValue(i).equals(time)) {
                    times.setSelectedIndex(i);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (times.getItemCount() > 1)
            times.setEnabled(enabled);
        else
            times.setEnabled(false);
        if (dates.getItemCount() > 1)
            dates.setEnabled(enabled);
        else
            dates.setEnabled(false);

        if (!times.isEnabled() && !dates.isEnabled()) {
            label.addStyleDependentName("inactive");
        } else {
            label.removeStyleDependentName("inactive");
        }
    }

    @Override
    public boolean hasMultipleTimes() {
        return (dates.getItemCount() > 1) || (times.getItemCount() > 1);
    }

    @Override
    public void setContinuous(boolean continuous) {
        if (continuous != this.continuous) {
            this.continuous = continuous;
            if (dates != null)
                remove(dates);
            if (times != null)
                remove(times);
            dates = null;
            times = null;

            if (range != null)
                remove(range);
            if (rangeLabel != null)
                remove(rangeLabel);
            range = null;
            rangeLabel = null;
            if (continuous) {
                initContinuous();
            } else {
                initDiscrete();
            }
        }
    }

    /*
     * Deprecated methods are fine here, because we are compiling everything
     * with GWT which handles them properly
     */
    @SuppressWarnings("deprecation")
    public static List<String> getDatesInRange(String startDateTimeStr, String endDateTimeStr) {
        DateTimeFormat parser = DateTimeFormat.getFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");

        Date startDate = parser.parse(startDateTimeStr);
        Date endDate = parser.parse(endDateTimeStr);
        startDate.setMinutes(0);
        startDate.setSeconds(0);

        endDate.setHours(endDate.getHours() + 1);
        endDate.setMinutes(0);
        endDate.setSeconds(0);

        List<String> dates = new ArrayList<String>();
        while (startDate.getTime() <= endDate.getTime()) {
            dates.add(datePrinter.format(startDate));
            startDate.setDate(startDate.getDate() + 1);
        }
        return dates;
    }

    @Override
    public boolean isContinuous() {
        return continuous;
    }

    @Override
    public List<String> getAvailableDates() {
        List<String> allDates = new ArrayList<String>();
        for (int i = 0; i < dates.getItemCount(); i++) {
            allDates.add(dates.getValue(i));
        }
        return allDates;
    }

    @Override
    public List<String> getAvailableTimes() {
        List<String> allTimes = new ArrayList<String>();
        for (int i = 0; i < times.getItemCount(); i++) {
            allTimes.add(times.getValue(i));
        }
        return allTimes;
    }

    @Override
    public void selectRange(String currentRange) {
        if (continuous) {
            for (int i = 0; i < range.getItemCount(); i++) {
                if (range.getValue(i).equals(currentRange)) {
                    range.setSelectedIndex(i);
                }
            }
        }
    }

    @Override
    public String getRange() {
        if (continuous && range != null)
            return range.getValue(range.getSelectedIndex());
        return null;
    }

    /**
     * @return The index of the selected date. Used in {@link StartEndTimePopup}
     */
    public int getSelectedDateIndex() {
        return dates.getSelectedIndex();
    }
}
