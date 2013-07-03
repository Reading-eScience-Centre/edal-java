package uk.ac.rdg.resc.godiva.client.widgets;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uk.ac.rdg.resc.godiva.client.handlers.StartEndTimeHandler;
import uk.ac.rdg.resc.godiva.client.handlers.TimeDateSelectionHandler;
import uk.ac.rdg.resc.godiva.client.requests.ErrorHandler;
import uk.ac.rdg.resc.godiva.client.requests.LayerDetails;
import uk.ac.rdg.resc.godiva.client.requests.LayerRequestBuilder;
import uk.ac.rdg.resc.godiva.client.requests.LayerRequestCallback;
import uk.ac.rdg.resc.godiva.client.requests.TimeRequestBuilder;
import uk.ac.rdg.resc.godiva.client.requests.TimeRequestCallback;
import uk.ac.rdg.resc.godiva.client.state.TimeSelectorIF;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A popup which allows a user to select start and end times. The start time is
 * guaranteed to be earlier or in some exceptional cases, equal to, the end
 * time.
 * 
 * Clients should use the
 * {@link StartEndTimePopup#setTimeSelectionHandler(StartEndTimeHandler)} method
 * to set a callback for when the times have been selected
 * 
 * @author Guy Griffiths
 * 
 */
public class StartEndTimePopup extends DialogBoxWithCloseButton {

    private final String layer;
    private String jsonProxyUrl;
    private String errorMessage;
    private String buttonLabel;
    private Button nextButton;
    private TimeSelector startTimeSelector;
    private TimeSelector endTimeSelector;
    private Map<String, List<String>> availableTimes;
    private StartEndTimeHandler timesHandler;

    private VerticalPanel timeSelectionPanel;
    private Label loadingLabel = new Label("Loading");

    private boolean multiFeature = false;
    private TimeSelectorIF currentTime;
    private List<String> availableDates;

    public StartEndTimePopup(String layer, String baseUrl,
            final TimeSelectorIF currentTimeSelector, final CentrePosIF centrePos,
            final int defaultDateStepsBetweenStartAndEnd) {
        super(centrePos);
        this.jsonProxyUrl = baseUrl;
        this.layer = layer;

        this.currentTime = currentTimeSelector;
        
        init();

        LayerRequestBuilder getLayerDetails = new LayerRequestBuilder(layer, jsonProxyUrl, null);
        getLayerDetails.setCallback(new LayerRequestCallback(layer, new ErrorHandler() {
            @Override
            public void handleError(Throwable e) {
                e.printStackTrace();
            }
        }) {
            @Override
            public void onResponseReceived(Request request, Response response) {
                super.onResponseReceived(request, response);
                LayerDetails layerDetails = getLayerDetails();
                multiFeature = layerDetails.isMultiFeature();

                if(multiFeature){
                    availableDates = TimeSelector.getDatesInRange(layerDetails.getStartTime(),
                            layerDetails.getEndTime());
                } else {
                    availableDates = layerDetails.getAvailableDates();
                }
                if (availableDates == null || availableDates.size() == 0) {
                    handleNoMultipleTimes();
                    return;
                }
                
                availableTimes = new LinkedHashMap<String, List<String>>();
                for (final String date : availableDates) {
                    if(multiFeature) {
                        availableTimes.put(date, Arrays.asList(TimeSelector.allTimes));
                    }
                }
                
                /*
                 * These need to be in date order. They should already be,
                 * but sort to be safe
                 */
                Collections.sort(availableDates);
                startTimeSelector.populateDates(availableDates);
                
                if (currentTime != null
                        && !currentTime.getSelectedDate().equals(
                                availableDates.get(availableDates.size() - 1))) {
                    startTimeSelector.selectDate(currentTimeSelector.getSelectedDate());
                } else {
                    startTimeSelector.selectDate(availableDates.get(0));
                }
                endTimeSelector.populateDates(availableDates);
                /*
                 * Pick a date 5 day-steps ahead.
                 */
                
                if(defaultDateStepsBetweenStartAndEnd > 0 && availableDates.size() > defaultDateStepsBetweenStartAndEnd) {
                    endTimeSelector.selectDate(availableDates.get(defaultDateStepsBetweenStartAndEnd));
                } else {
                    endTimeSelector.selectDate(availableDates.get(availableDates.size() - 1));
                }
                List<String> availableEndTimes = endTimeSelector.getAvailableTimes();
                String lastDateTime = endTimeSelector.getSelectedDate() + "T"
                        + availableEndTimes.get(availableEndTimes.size() - 1);
                endTimeSelector.selectDateTime(lastDateTime);
                
                setTimeSelector();
            }
        });

        try {
            getLayerDetails.send();
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        setAutoHideEnabled(true);
        setModal(true);
        setAnimationEnabled(true);
        setGlassEnabled(true);

        errorMessage = "You need multiple time values to do this";

        setLoading();

        startTimeSelector = new TimeSelector("start_time", "Start time",
                new TimeDateSelectionHandler() {
                    @Override
                    public void dateSelected(String id, final String selectedDate) {
                        final List<String> timesForDate = availableTimes.get(startTimeSelector
                                .getSelectedDate());
                        if (timesForDate != null) {
                            startTimeSelector.populateTimes(timesForDate);
                            checkOrder();
                        } else {
                            TimeRequestBuilder getTimeRequest = new TimeRequestBuilder(
                                    StartEndTimePopup.this.layer, selectedDate, jsonProxyUrl);
                            getTimeRequest.setCallback(new TimeRequestCallback() {
                                @Override
                                public void onResponseReceived(Request request, Response response) {
                                    super.onResponseReceived(request, response);
                                    availableTimes.put(startTimeSelector.getSelectedDate(),
                                            getAvailableTimesteps());
                                    startTimeSelector.populateTimes(getAvailableTimesteps());
                                    checkOrder();
                                }

                                @Override
                                public void onError(Request request, Throwable exception) {
                                }
                            });

                            try {
                                getTimeRequest.send();
                            } catch (RequestException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void datetimeSelected(String id, String selectedStartDateTime) {
                    }

                });

        endTimeSelector = new TimeSelector("end_time", "End time", new TimeDateSelectionHandler() {
            @Override
            public void dateSelected(String id, final String selectedDate) {
                final List<String> timesForDate = availableTimes.get(startTimeSelector
                        .getSelectedDate());
                if (timesForDate != null) {
                    endTimeSelector.populateTimes(timesForDate);
                    checkOrder();
                } else {
                    TimeRequestBuilder getTimeRequest = new TimeRequestBuilder(
                            StartEndTimePopup.this.layer, selectedDate, jsonProxyUrl);
                    getTimeRequest.setCallback(new TimeRequestCallback() {
                        @Override
                        public void onResponseReceived(Request request, Response response) {
                            super.onResponseReceived(request, response);
                            availableTimes.put(endTimeSelector.getSelectedDate(),
                                    getAvailableTimesteps());
                            endTimeSelector.populateTimes(getAvailableTimesteps());
                            checkOrder();
                        }

                        @Override
                        public void onError(Request request, Throwable exception) {
                        }
                    });

                    try {
                        getTimeRequest.send();
                    } catch (RequestException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void datetimeSelected(String id, String selectedTime) {
                /*
                 * Do nothing here - we only check the time when the "Next"
                 * button is clicked
                 */
                checkOrder();
            }
        });
    }
    
    /*
     * This just disables the next button if the start time is later than the end time
     */
    private void checkOrder(){
        if(nextButton == null) {
            return;
        }
        if ((startTimeSelector == null && endTimeSelector == null)
                && startTimeSelector.getSelectedDateTime().compareTo(
                        endTimeSelector.getSelectedDateTime()) >= 0) {
            nextButton.setEnabled(false);
            nextButton.setTitle("Start time must be before end time");
        } else {
            nextButton.setEnabled(true);
            nextButton.setTitle("");
        }
    }

    public void setButtonLabel(String buttonLabel) {
        this.buttonLabel = buttonLabel;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setTimeSelectionHandler(StartEndTimeHandler timesHandler) {
        this.timesHandler = timesHandler;
    }

    private void handleNoMultipleTimes() {
        setText("Multiple times not found");
        Label errorLabel = new Label(errorMessage);
        errorLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_JUSTIFY);
        errorLabel.setSize("350px", "150px");
        Button closeButton = new Button("Close");
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                StartEndTimePopup.this.hide();
                StartEndTimePopup.this.removeFromParent();
            }
        });
        VerticalPanel vP = new VerticalPanel();
        vP.add(errorLabel);
        vP.add(closeButton);
        vP.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_CENTER);
        setWidget(vP);
    }

    private void setTimeSelector() {
        timeSelectionPanel = new VerticalPanel();
        timeSelectionPanel.add(startTimeSelector);
        timeSelectionPanel.add(endTimeSelector);
        timeSelectionPanel.add(getNextButton());
        timeSelectionPanel.setCellHorizontalAlignment(nextButton,
                HasHorizontalAlignment.ALIGN_CENTER);
        setText("Select start and end times");
        timeSelectionPanel.setSize("350px", "150px");
        setWidget(timeSelectionPanel);
    }

    /*
     * These were previously used for filtering the dates, rather than just
     * disabling the next button. They have been left here in case we want to go
     * back to that approach
     */
//    private List<String> getDatesLaterOrEqualTo(String selectedDate, List<String> availableDates) {
//        List<String> laterDates = new ArrayList<String>();
//        for (String date : availableDates) {
//            if (date.compareTo(selectedDate) >= 0) {
//                laterDates.add(date);
//            }
//        }
//        return laterDates;
//    }
//
//    private List<String> getTimesLaterThan(String selectedTime, List<String> availableTimes) {
//        List<String> laterTimes = new ArrayList<String>();
//        for (String time : availableTimes) {
//            if (time.compareTo(selectedTime) > 0) {
//                laterTimes.add(time);
//            }
//        }
//        return laterTimes;
//    }

    private void setLoading() {
        if (loadingLabel == null) {
            loadingLabel = new Label("Loading...");
        }
        loadingLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        loadingLabel.setSize("350px", "150px");
        setText("Loading details");
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(loadingLabel);
        setWidget(verticalPanel);
    }

    private Button getNextButton() {
        if (nextButton == null) {
            if (buttonLabel != null)
                nextButton = new Button(buttonLabel);
            else
                nextButton = new Button("OK");
            nextButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (timesHandler != null) {
                        timesHandler.timesReceived(startTimeSelector.getSelectedDateTime(),
                                endTimeSelector.getSelectedDateTime());
                    }
                }
            });
        }
        return nextButton;
    }
}
