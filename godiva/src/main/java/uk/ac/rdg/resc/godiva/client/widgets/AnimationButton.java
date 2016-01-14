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

import uk.ac.rdg.resc.godiva.client.handlers.AviExportHandler;
import uk.ac.rdg.resc.godiva.client.handlers.StartEndTimeHandler;
import uk.ac.rdg.resc.godiva.client.state.LayerSelectorIF;
import uk.ac.rdg.resc.godiva.client.state.TimeSelectorIF;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A button which can be used to request, display, and switch off animations
 * 
 * @author Guy Griffiths
 * 
 */
public class AnimationButton extends ToggleButton {
    private MapArea map;
    private StartEndTimePopup popup;
    private String animLayer = null;
    private String jsonProxyUrl;
    private LayerSelectorIF wmsUrlProvider;
    private String currentElevation;
    private String palette;
    private String scaleRange;
    private String aboveMax;
    private String belowMin;
    private String noData;
    private int nColorBands;
    private boolean logScale;
    private Button goButton;
    private Button cancelButton;

    private final TimeSelectorIF currentTimeSelector;

    private VerticalPanel detailsSelectionPanel;
    private ListBox granularitySelector;
    private ListBox fpsSelector;
    private VerticalPanel formatSelector;
    private RadioButton overlayRadioButton;
    private RadioButton aviRadioButton;

    private boolean completed;
    private String style;

    private final AviExportHandler aviExporter;

    public AnimationButton(final MapArea map, final String jsonProxyUrl,
            final LayerSelectorIF wmsUrlProvider, final TimeSelectorIF currentTimeSelector,
            AviExportHandler aviExporter) {
        super(new Image(GWT.getModuleBaseURL() + "img/film.png"), new Image(GWT.getModuleBaseURL()
                + "img/stop.png"));
        super.setTitle("Open the animation wizard");

        this.map = map;
        this.jsonProxyUrl = jsonProxyUrl;
        this.wmsUrlProvider = wmsUrlProvider;
        this.currentTimeSelector = currentTimeSelector;
        this.aviExporter = aviExporter;

        this.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (AnimationButton.this.isDown()) {
                    completed = false;
                    /*
                     * The first action on clicking is to get a wizard to select
                     * start/end times, frame rate, etc.
                     */
                    popup = getWizard();
                    popup.center();
                    AnimationButton.this.setTitle("Stop the animation");
                } else {
                    AnimationButton.this.setTitle("Open the animation wizard");
                    map.stopAnimation();
                    AnimationButton.this.aviExporter.animationStopped();
                }
            }
        });

        /*
         * Define all of the constant widgets ready for when they get used
         */
        formatSelector = new VerticalPanel();
        overlayRadioButton = new RadioButton("formatSelector", "Overlay");
        overlayRadioButton.setValue(true);
        aviRadioButton = new RadioButton("formatSelector", "Export to AVI");
        formatSelector.add(overlayRadioButton);
        formatSelector.add(aviRadioButton);

        fpsSelector = new ListBox();
        fpsSelector.addItem("1fps", "1");
        fpsSelector.addItem("2fps", "2");
        fpsSelector.addItem("5fps", "5");
        fpsSelector.addItem("10fps", "10");
        fpsSelector.addItem("15fps", "15");
        fpsSelector.addItem("24fps", "24");
        fpsSelector.addItem("30fps", "30");
        fpsSelector.setSelectedIndex(3);

        /*
         * We disable the animation button until we have a layer selected
         */
        setEnabled(false);
    }

    /**
     * Update the current details so that the animation requested is the correct
     * one
     * 
     * @param layer
     *            The layer ID for the animation
     * @param currentElevation
     *            The elevation for the animation
     * @param palette
     *            The palette name used for the animation
     * @param style
     *            The style for the animation
     * @param scaleRange
     *            The scale range used for the animation
     * @param nColorBands
     *            The number of colour bands in the palette for the animation
     * @param logScale
     *            Whether the palette should be logarithmic
     */
    public void updateDetails(String layer, String currentElevation, String palette, String style,
            String scaleRange, String aboveMax, String belowMin, String noData, int nColorBands,
            boolean logScale) {
        this.animLayer = layer;
        this.currentElevation = currentElevation;
        this.palette = palette;
        this.style = style;
        this.scaleRange = scaleRange;
        this.aboveMax = aboveMax;
        this.belowMin = belowMin;
        this.noData = noData;
        this.nColorBands = nColorBands;
        this.logScale = logScale;
    }

    /*
     * Removes all popups and adds the animation to the map or downloads it.
     */
    private void startAnimation(String times, String frameRate, boolean overlay) {
        if (overlay) {
            map.addAnimationLayer(wmsUrlProvider.getWmsUrl(), animLayer, times, currentElevation,
                    palette, style, scaleRange, aboveMax, belowMin, noData, nColorBands, logScale,
                    frameRate);
            this.setTitle("Stop animation");
            aviExporter.animationStarted(times, frameRate);
        } else {
            /*
             * If we want to open an AVI, we also don't need the button selected
             * any more
             */
            Window.open(aviExporter.getAviUrl(times, frameRate), null, null);
            this.setDown(false);
        }
        popup.removeFromParent();
        popup = null;
        detailsSelectionPanel = null;
        granularitySelector = null;
        goButton = null;
    }

    /*
     * Gets the wizard for selecting animation parameters
     */
    private StartEndTimePopup getWizard() {
        /*
         * First a start/end time popup
         */
        popup = new StartEndTimePopup(animLayer, jsonProxyUrl + wmsUrlProvider.getWmsUrl(),
                currentTimeSelector, map, 5);
        popup.setErrorMessage("Can only create animation where there are multiple times");
        popup.setButtonLabel("Next >");
        popup.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                if (!completed)
                    AnimationButton.this.setDown(false);
            }
        });

        if (animLayer == null) {
            setNoAnimationPossible("Please select a layer before trying to create an animation");
            return popup;
        }

        popup.setTimeSelectionHandler(new StartEndTimeHandler() {
            @Override
            public void timesReceived(String startDateTime, String endDateTime) {
                /*
                 * Once the times have been loaded, we make a new request to the
                 * server to generate some recommended timesteps for us
                 */
                VerticalPanel loadingPanel = new VerticalPanel();
                loadingPanel.add(new Label("Getting details from server..."));
                loadingPanel.add(new Label("Please wait"));
                loadingPanel.setSize("350px", "150px");
                popup.setWidget(loadingPanel);
                String url = URL.encode(jsonProxyUrl + wmsUrlProvider.getWmsUrl()
                        + "?request=GetMetadata&item=animationTimesteps&layerName=" + animLayer
                        + "&start=" + startDateTime + "&end=" + endDateTime);
                RequestBuilder getAnimTimestepsRequest = new RequestBuilder(RequestBuilder.GET, url);
                getAnimTimestepsRequest.setCallback(new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        /*
                         * Once we have received how many timesteps are
                         * available, we populate a selector and then set it to
                         * gather further details
                         */
                        JSONValue jsonMap = JSONParser.parseLenient(response.getText());
                        JSONObject parentObj = jsonMap.isObject();

                        JSONValue timesJson = parentObj.get("timeStrings");

                        granularitySelector = new ListBox();
                        if (timesJson != null) {
                            JSONArray timesArr = timesJson.isArray();
                            for (int i = 0; i < timesArr.size(); i++) {
                                JSONObject timeObj = timesArr.get(i).isObject();
                                String title = timeObj.get("title").isString().stringValue();
                                String value = timeObj.get("timeString").isString().stringValue();
                                granularitySelector.addItem(title, value);
                            }
                        }

                        setDetailsSelector();
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                        exception.printStackTrace();
                    }
                });
                try {
                    getAnimTimestepsRequest.send();
                } catch (RequestException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        return popup;
    }

    /*
     * Adds the widgets to select the frame rate, granularity, etc. and puts
     * them in the popup
     */
    private void setDetailsSelector() {
        detailsSelectionPanel = new VerticalPanel();
        Label infoLabel = new Label(
                "The more frames you choose the longer your animation will take to load."
                        + " Please choose the smallest number you think you need!");
        detailsSelectionPanel.add(infoLabel);

        HorizontalPanel granPan = new HorizontalPanel();
        Label granLabel = new Label("Granularity:");
        granPan.add(granLabel);
        granPan.add(granularitySelector);
        granPan.setCellWidth(granLabel, "40%");
        granPan.setCellHorizontalAlignment(granLabel, HasHorizontalAlignment.ALIGN_RIGHT);
        granPan.setCellHorizontalAlignment(granularitySelector, HasHorizontalAlignment.ALIGN_LEFT);
        granPan.setWidth("100%");
        detailsSelectionPanel.add(granPan);

//        HorizontalPanel formatPan = new HorizontalPanel();
//        Label formatLabel = new Label("Type of animation:");
//        formatPan.add(formatLabel);
//        formatPan.add(formatSelector);
//        formatPan.setCellWidth(formatLabel, "40%");
//        formatPan.setCellVerticalAlignment(formatLabel, HasVerticalAlignment.ALIGN_MIDDLE);
//        formatPan.setCellHorizontalAlignment(formatLabel, HasHorizontalAlignment.ALIGN_RIGHT);
//        formatPan.setCellHorizontalAlignment(formatSelector, HasHorizontalAlignment.ALIGN_LEFT);
//        formatPan.setWidth("100%");
//        detailsSelectionPanel.add(formatPan);

        HorizontalPanel fpsPan = new HorizontalPanel();
        Label fpsLabel = new Label("Frame Rate:");
        fpsPan.add(fpsLabel);
        fpsPan.add(fpsSelector);
        fpsPan.setCellWidth(fpsLabel, "40%");
        fpsPan.setCellHorizontalAlignment(fpsLabel, HasHorizontalAlignment.ALIGN_RIGHT);
        fpsPan.setCellHorizontalAlignment(fpsSelector, HasHorizontalAlignment.ALIGN_LEFT);
        fpsPan.setWidth("100%");
        detailsSelectionPanel.add(fpsPan);

        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.add(getCancelButton());
        buttonPanel.add(getGoButton());
        detailsSelectionPanel.add(buttonPanel);
        detailsSelectionPanel.setCellHorizontalAlignment(buttonPanel,
                HasHorizontalAlignment.ALIGN_CENTER);
        detailsSelectionPanel.setSize("350px", "150px");
        popup.setText("Select the time resolution");
        popup.setWidget(detailsSelectionPanel);
    }

    protected Button getGoButton() {
        if (goButton == null) {
            goButton = new Button("Go");
            goButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    completed = true;
                    String times = granularitySelector.getValue(granularitySelector
                            .getSelectedIndex());
                    String frameRate = fpsSelector.getValue(fpsSelector.getSelectedIndex());
                    startAnimation(times, frameRate, overlayRadioButton.getValue());
                }
            });
        }
        return goButton;
    }

    protected Button getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new Button("Cancel");
            cancelButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    popup.hide();
                }
            });
        }
        return cancelButton;
    }

    private void setNoAnimationPossible(String message) {
        popup.setText("Cannot create animation");
        Label errorLabel = new Label(message);
        errorLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_JUSTIFY);
        errorLabel.setSize("350px", "150px");
        Button closeButton = new Button("Close");
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                completed = false;
                popup.hide();
                popup.removeFromParent();
            }
        });
        VerticalPanel vP = new VerticalPanel();
        vP.add(errorLabel);
        vP.add(closeButton);
        vP.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_CENTER);
        popup.setWidget(vP);
    }
}
