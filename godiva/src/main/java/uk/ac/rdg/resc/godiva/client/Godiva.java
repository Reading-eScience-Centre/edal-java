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

package uk.ac.rdg.resc.godiva.client;

import java.util.List;

import org.gwtopenmaps.openlayers.client.Bounds;

import uk.ac.rdg.resc.godiva.client.handlers.AviExportHandler;
import uk.ac.rdg.resc.godiva.client.requests.CaseInsensitiveParameterMap;
import uk.ac.rdg.resc.godiva.client.requests.ConnectionException;
import uk.ac.rdg.resc.godiva.client.requests.LayerDetails;
import uk.ac.rdg.resc.godiva.client.requests.LayerTreeJSONParser;
import uk.ac.rdg.resc.godiva.client.state.CopyrightInfoIF;
import uk.ac.rdg.resc.godiva.client.state.ElevationSelectorIF;
import uk.ac.rdg.resc.godiva.client.state.GodivaStateInfo;
import uk.ac.rdg.resc.godiva.client.state.InfoIF;
import uk.ac.rdg.resc.godiva.client.state.LayerSelectorIF;
import uk.ac.rdg.resc.godiva.client.state.PaletteSelectorIF;
import uk.ac.rdg.resc.godiva.client.state.PaletteSelectorIF.OutOfRangeState;
import uk.ac.rdg.resc.godiva.client.state.TimeSelectorIF;
import uk.ac.rdg.resc.godiva.client.state.UnitsInfoIF;
import uk.ac.rdg.resc.godiva.client.util.GodivaUtils;
import uk.ac.rdg.resc.godiva.client.util.UnitConverter;
import uk.ac.rdg.resc.godiva.client.widgets.AnimationButton;
import uk.ac.rdg.resc.godiva.client.widgets.CopyrightInfo;
import uk.ac.rdg.resc.godiva.client.widgets.DialogBoxWithCloseButton;
import uk.ac.rdg.resc.godiva.client.widgets.DialogBoxWithCloseButton.CentrePosIF;
import uk.ac.rdg.resc.godiva.client.widgets.ElevationSelector;
import uk.ac.rdg.resc.godiva.client.widgets.Info;
import uk.ac.rdg.resc.godiva.client.widgets.LayerSelectorTree;
import uk.ac.rdg.resc.godiva.client.widgets.MapArea;
import uk.ac.rdg.resc.godiva.client.widgets.PaletteSelector;
import uk.ac.rdg.resc.godiva.client.widgets.TimeSelector;
import uk.ac.rdg.resc.godiva.client.widgets.UnitsInfo;
import uk.ac.rdg.resc.godiva.shared.LayerMenuItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This is a concrete implementation of {@link BaseWmsClient} for a client which
 * handles a single WMS layer at a time
 * 
 * @author Guy Griffiths
 * 
 */
public class Godiva extends BaseWmsClient implements AviExportHandler {
    /*
     * A static ID to refer to the WMS layer by. The MapArea object needs an
     * internal reference for each WMS layer. Since this client only handles a
     * single layer anyway, this can be any static string
     */
    private static final String WMS_LAYER_ID = "singleLayer";

    /*
     * State information holders. The LayerSelectorIF is not part of
     * GodivaStateInfo, because it handles layer selection, which may behave in
     * different ways for different clients
     */
    protected LayerSelectorIF layerSelector;
    protected GodivaStateInfo widgetCollection;
    // Button to show further information about the currently selected layer
    protected PushButton infoButton;

    /*
     * Images
     */
    protected Image logo;
    protected Image loadingImage;

    /*
     * These are used for storing and restoring the state so that people can
     * link to a particular state
     */
    protected boolean permalinking;
    protected CaseInsensitiveParameterMap permalinkParamsMap;

    // The link to the Google Earth KMZ
    protected Anchor kmzLink;
    // Link to the current state
    protected Anchor permalink;
    // Email a link to the current state
    protected Anchor email;
    // Link to a screenshot of the current state
    protected Anchor screenshot;
    // Link to the documentation for the system
    protected Anchor docLink;

    // Button to create animations
    protected AnimationButton anim;

    /*
     * Use to store whether timeseries and vertical profiles are supported
     */
    private boolean timeseriesSupported = false;
    private boolean profilesSupported = false;

    /* The extents of the current layer */
    private String currentLayerExtent;
    protected PushButton zoomToLayerExtents;

    /*
     * Implemented from interfaces/abstract methods
     */

    @Override
    protected void init() {
        String permalinkString = Window.Location.getParameter("permalinking");
        if (permalinkString != null && Boolean.parseBoolean(permalinkString)) {
            permalinking = true;
            permalinkParamsMap = CaseInsensitiveParameterMap.getMapFromList(Window.Location
                    .getParameterMap());
        }

        /*
         * Initialises links.
         */
        kmzLink = new Anchor("Open in Google Earth");
        kmzLink.setStylePrimaryName("linkStyle");
        kmzLink.setTitle("Open the current view in Google Earth");
        kmzLink.setEnabled(false);

        permalink = new Anchor("Permalink");
        permalink.setStylePrimaryName("linkStyle");
        permalink.setTarget("_blank");
        permalink.setTitle("Permanent link to the current view");
        permalink.setEnabled(false);

        email = new Anchor("Email Link");
        email.setStylePrimaryName("linkStyle");
        email.setTitle("Email a link to the current view");
        email.setEnabled(false);

        screenshot = new Anchor("Export to PNG");
        screenshot.setHref("screenshots/getScreenshot?");
        screenshot.setStylePrimaryName("linkStyle");
        screenshot.setTarget("_blank");
        screenshot.setTitle("Open a downloadable image in a new window - may be slow to load");
        screenshot.setEnabled(false);

        docLink = new Anchor("Documentation", docHref);
        docLink.setStylePrimaryName("linkStyle");
        docLink.setTarget("_blank");
        docLink.setTitle("Open documentation in a new window");

        /*
         * This sets which layer a transect will apply to. Since we have only
         * one WMS layer at a time, this can be set straight away and won't need
         * modifying
         */
        mapArea.setTransectLayerId(WMS_LAYER_ID);

        /*
         * We use a get method here, so that subclasses can override and use
         * their own custom implementations.
         * 
         * Currently only these widgets are initialised in this method, since
         * they are the most complex. The other things such as links can easily
         * be removed and new ones added.
         */
        layerSelector = getLayerSelector();
        ElevationSelectorIF elevationSelector = getElevationSelector();
        TimeSelectorIF timeSelector = getTimeSelector();
        PaletteSelectorIF paletteSelector = getPaletteSelector(layerSelector, mapArea);

        UnitsInfoIF unitsInfo = new UnitsInfo();
        final CopyrightInfoIF copyrightInfo = new CopyrightInfo();
        final InfoIF moreInfo = new Info();

        widgetCollection = new GodivaStateInfo(elevationSelector, timeSelector, paletteSelector,
                unitsInfo, copyrightInfo, moreInfo, layerSelector);

        anim = new AnimationButton(mapArea, proxyUrl, layerSelector, timeSelector, this);
        /*
         * Start this disabled. It gets enabled when layer details are loaded
         * (if appropriate)
         */
        anim.setEnabled(false);

        logo = getLogo();

        loadingImage = new Image(GWT.getModuleBaseURL() + "img/loading.gif");
        loadingImage.setVisible(false);
        loadingImage.setStylePrimaryName("loadingImage");

        infoButton = new PushButton(new Image(GWT.getModuleBaseURL() + "img/info.png"));
        infoButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                DialogBoxWithCloseButton popup = new DialogBoxWithCloseButton(mapArea);
                popup.setGlassEnabled(true);
                popup.setHTML("<b>Information</b>");
                VerticalPanel vPanel = new VerticalPanel();
                if (copyrightInfo.hasCopyright()) {
                    vPanel.add(new HTML("<b>Copyright:</b> " + copyrightInfo.getCopyrightInfo()));
                }
                if (moreInfo.hasInfo()) {
                    vPanel.add(new HTML("<b>Info:</b> " + moreInfo.getInfo()));
                }
                popup.add(vPanel);
                popup.center();
            }
        });
        infoButton.setEnabled(false);
        infoButton.setTitle("More infomation about this dataset");

        zoomToLayerExtents = new PushButton(new Image(GWT.getModuleBaseURL() + "img/extents.png"));
        zoomToLayerExtents.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                mapArea.zoomToExtent(currentLayerExtent);
            }
        });
        zoomToLayerExtents.setEnabled(false);
        zoomToLayerExtents.setTitle("Fit the map to this layer's extents");

        /*
         * Now get the layout and add it to the main window
         */
        RootLayoutPanel mainWindow = RootLayoutPanel.get();
        mainWindow.add(getLayout());

        /*
         * Disable everything because we haven't got any data to plot yet.
         */
        timeSelector.setEnabled(false);
        elevationSelector.setEnabled(false);
        paletteSelector.setEnabled(false);
        unitsInfo.setEnabled(false);
        copyrightInfo.setEnabled(false);
    }

    @Override
    protected GodivaStateInfo getWidgetCollection(String layerId) {
        return widgetCollection;
    }

    /**
     * Requests the layer menu from the server. When the menu is returned,
     * menuLoaded will be called
     */
    @Override
    protected void requestAndPopulateMenu() {
        /*
         * This is where we define the fact that we are working with a single
         * local server
         */
        String path = Window.Location.getHost() + Window.Location.getPath();
        path = path.substring(0, path.lastIndexOf("/"));
        final String wmsUrl = "http://" + path + "/wms";
        final RequestBuilder getMenuRequest = new RequestBuilder(RequestBuilder.GET,
                getUrlFromGetArgs(wmsUrl, "?request=GetMetadata&item=menu"));
        getMenuRequest.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request req, Response response) {
                try {
                    if (response.getStatusCode() != Response.SC_OK) {
                        throw new ConnectionException("Error contacting server");
                    }
                    /*
                     * Once the menu has been received, parse it, and call
                     * menuLoaded()
                     * 
                     * This is a separate method so that subclasses can change
                     * the behaviour once the menu is loaded
                     */
                    JSONValue jsonMap = JSONParser.parseLenient(response.getText());
                    JSONObject parentObj = jsonMap.isObject();
                    LayerMenuItem menuTree = LayerTreeJSONParser.getTreeFromJson(wmsUrl, parentObj);

                    menuLoaded(menuTree);
                } catch (Exception e) {
                    invalidJson(e, response.getText(), getMenuRequest.getUrl());
                } finally {
                    setLoading(false);
                }
            }

            @Override
            public void onError(Request request, Throwable e) {
                setLoading(false);
                handleError(e);
            }
        });

        try {
            setLoading(true);
            getMenuRequest.send();
        } catch (RequestException e) {
            handleError(e);
        }
    }

    @Override
    protected void availableTimesLoaded(String layerId, List<String> availableTimes,
            String nearestTime) {
        widgetCollection.getTimeSelector().populateTimes(availableTimes);
        if (nearestTime != null) {
            widgetCollection.getTimeSelector().selectDateTime(nearestTime);
        }
    }

    @Override
    protected void updateMap(MapArea mapArea, String layerUpdated) {
        /*
         * If we are updating the map having come here with permalink
         * parameters, first zoom to the correct bounds
         */
        if (permalinking) {
            String bbox = permalinkParamsMap.get("bbox");
            if (bbox != null) {
                String[] bboxElems = bbox.split(",");
                if (bboxElems.length == 4) {
                    try {
                        mapArea.getMap().zoomToExtent(
                                new Bounds(Double.parseDouble(bboxElems[0]), Double
                                        .parseDouble(bboxElems[1]), Double
                                        .parseDouble(bboxElems[2]), Double
                                        .parseDouble(bboxElems[3])));
                    } catch (NumberFormatException nfe) {
                        /*
                         * Can't parse one of the bounds. Oh well, not a lot we
                         * can do
                         */
                    }
                }
            }
        }

        /*
         * Get all of the relevant information to update the map
         */
        String currentTime = null;
        String targetTime = null;
        if (widgetCollection.getTimeSelector().isContinuous()) {
            currentTime = widgetCollection.getTimeSelector().getSelectedDateTimeRange();
            targetTime = widgetCollection.getTimeSelector().getSelectedDateTime();
        } else {
            currentTime = widgetCollection.getTimeSelector().getSelectedDateTime();
        }

        String currentElevation = null;
        String targetElevation = null;
        if (widgetCollection.getElevationSelector().isContinuous()) {
            currentElevation = widgetCollection.getElevationSelector().getSelectedElevationRange();
            targetElevation = widgetCollection.getElevationSelector().getSelectedElevation();
        } else {
            currentElevation = widgetCollection.getElevationSelector().getSelectedElevation();
        }

        String currentPalette = widgetCollection.getPaletteSelector().getSelectedPalette();
        String aboveMaxString = widgetCollection.getPaletteSelector().getAboveMaxString();
        String belowMinString = widgetCollection.getPaletteSelector().getBelowMinString();
        String noDataString = widgetCollection.getPaletteSelector().getNoDataColour();

        String currentStyle = widgetCollection.getPaletteSelector().getSelectedStyle();
        String currentScaleRange = widgetCollection.getPaletteSelector().getScaleRange();
        int nColourBands = widgetCollection.getPaletteSelector().getNumColorBands();
        boolean logScale = widgetCollection.getPaletteSelector().isLogScale();

        /*
         * Now call the addLayer method. This will always overwrite an existing
         * WMS layer because we only have a single layer ID (WMS_LAYER_ID)
         */
        mapArea.addLayer(widgetCollection.getWmsUrlProvider().getWmsUrl(), WMS_LAYER_ID,
                layerSelector.getSelectedId(), currentTime, targetTime, currentElevation,
                targetElevation, currentStyle, currentPalette, aboveMaxString, belowMinString,
                noDataString, currentScaleRange, nColourBands, logScale, profilesSupported,
                timeseriesSupported);

        /*
         * Set the opacity after updating the map, otherwise it doesn't work
         */
        if (permalinking) {
            permalinking = false;
            String currentOpacity = permalinkParamsMap.get("opacity");
            if (currentOpacity != null) {
                widgetCollection.getPaletteSelector().setOpacity(Float.parseFloat(currentOpacity));
            }
        }
        updateLinksEtc();
    }

    @Override
    protected void loadingStarted() {
        if (loadingImage != null)
            loadingImage.setVisible(true);
    }

    @Override
    protected void loadingFinished() {
        if (loadingImage != null)
            loadingImage.setVisible(false);
    }

    @Override
    protected String getCurrentTime() {
        return widgetCollection.getTimeSelector().getSelectedDateTime();
    }

    @Override
    public void enableWidgets() {
        layerSelector.setEnabled(true);
        widgetCollection.getTimeSelector().setEnabled(true);
        widgetCollection.getElevationSelector().setEnabled(true);
        widgetCollection.getPaletteSelector().setEnabled(true);
        widgetCollection.getUnitsInfo().setEnabled(true);
    }

    @Override
    public void disableWidgets() {
        layerSelector.setEnabled(false);
        widgetCollection.getTimeSelector().setEnabled(false);
        widgetCollection.getElevationSelector().setEnabled(false);
        widgetCollection.getPaletteSelector().setEnabled(false);
        widgetCollection.getUnitsInfo().setEnabled(false);
    }

    @Override
    public String getAviUrl(String times, String frameRate) {
        PaletteSelectorIF paletteSelector = widgetCollection.getPaletteSelector();

        String urlParams = "dataset=" + widgetCollection.getWmsUrlProvider().getWmsUrl()
                + "&numColorBands=" + paletteSelector.getNumColorBands() + "&logScale="
                + paletteSelector.isLogScale() + "&bbox=" + mapArea.getMap().getExtent();

        ElevationSelectorIF elevationSelector = widgetCollection.getElevationSelector();

        UnitsInfoIF unitsInfo = widgetCollection.getUnitsInfo();

        String currentLayer = null;
        String currentElevation = null;
        String currentPalette = null;
        String currentStyle = null;
        String scaleRange = null;

        urlParams += "&time=" + times;

        String selectedId = layerSelector.getSelectedId();
        if (selectedId != null) {
            currentLayer = selectedId;
            urlParams += "&layer=" + currentLayer;
        }

        if (elevationSelector.getSelectedElevation() != null) {
            currentElevation = elevationSelector.getSelectedElevation();
            urlParams += "&elevation=" + currentElevation;
        }
        if (paletteSelector.getSelectedPalette() != null) {
            currentPalette = paletteSelector.getSelectedPalette();
            urlParams += "&palette=" + currentPalette;
        }
        if (paletteSelector.getSelectedStyle() != null) {
            currentStyle = paletteSelector.getSelectedStyle();
            urlParams += "&style=" + currentStyle;
        }
        if (paletteSelector.getScaleRange() != null) {
            scaleRange = paletteSelector.getScaleRange();
            urlParams += "&scaleRange=" + scaleRange;
        }

        urlParams += "&bbox=" + mapArea.getMap().getExtent().toBBox(6);
        if (layerSelector != null) {
            StringBuilder title = new StringBuilder();
            if (layerSelector.getTitleElements() != null
                    && layerSelector.getTitleElements().size() > 0) {
                for (String element : layerSelector.getTitleElements()) {
                    title.append(element + ",");
                }
                title.deleteCharAt(title.length() - 1);
                urlParams += "&layerTitle=" + title;
            }
        }
        urlParams += "&srs=EPSG:4326";
        urlParams += "&mapHeight=" + mapHeight;
        urlParams += "&mapWidth=" + mapWidth;
        urlParams += "&style=" + currentStyle;
        if (elevationSelector != null)
            urlParams += "&zUnits=" + elevationSelector.getVerticalUnits();
        if (unitsInfo != null)
            urlParams += "&units=" + unitsInfo.getUnits();
        urlParams += "&baseUrl=" + mapArea.getBaseLayerUrl();
        urlParams += "&baseLayers=" + mapArea.getBaseLayerLayers();
        if (frameRate != null)
            urlParams += "&frameRate=" + frameRate;

        return "screenshots/createVideo?" + urlParams;
    }

    @Override
    public void animationStarted(String times, String fps) {
        updateLinksEtc();
        screenshot.setHref(getAviUrl(times, fps));
        screenshot.setText("Export to AVI");
    }

    @Override
    public void animationStopped() {
        updateLinksEtc();
        screenshot.setText("Export to PNG");
    }

    /*
     * Overridden methods for custom additional behaviour
     */

    @Override
    public void setOpacity(String layerId, float opacity) {
        mapArea.setOpacity(layerId, opacity);
        updateLinksEtc();
    }

    @Override
    protected void layerDetailsLoaded(LayerDetails layerDetails, boolean autoUpdate) {
        super.layerDetailsLoaded(layerDetails, autoUpdate);

        currentLayerExtent = layerDetails.getExtents();
        zoomToLayerExtents.setEnabled(true);

        if (layerDetails.isContinuousT()) {
            anim.setEnabled(false);
        } else {
            anim.setEnabled(true);
        }

        timeseriesSupported = layerDetails.supportsTimeseries();
        profilesSupported = layerDetails.supportsProfiles();

        if (widgetCollection.getCopyrightInfo().hasCopyright()
                || widgetCollection.getMoreInfo().hasInfo()) {
            infoButton.setEnabled(true);
        } else {
            infoButton.setEnabled(false);
        }

        /*
         * We populate our widgets here, but in a multi-layer system, we may
         * want to create new widgets here
         */
        if (autoUpdate && !widgetCollection.getPaletteSelector().isLocked()) {
            widgetCollection.getPaletteSelector().selectPalette(layerDetails.getSelectedPalette());
            if (layerDetails.getAboveMaxColour() != null) {
                widgetCollection.getPaletteSelector().setExtraAboveMaxColour(
                        layerDetails.getAboveMaxColour());
            }
            if (layerDetails.getBelowMinColour() != null) {
                widgetCollection.getPaletteSelector().setExtraBelowMinColour(
                        layerDetails.getBelowMinColour());
            }
            if (layerDetails.getNoDataColour() != null) {
                widgetCollection.getPaletteSelector().setNoDataColour(
                        layerDetails.getNoDataColour());
            }
        }

        if (permalinking) {
            String scaleRange = permalinkParamsMap.get("scaleRange");
            String logScaleStr = permalinkParamsMap.get("logScale");
            Boolean logScale = null;
            if (logScaleStr != null) {
                logScale = Boolean.parseBoolean(logScaleStr);
            }
            if (scaleRange != null) {
                widgetCollection.getPaletteSelector().setScaleRange(scaleRange, logScale);
            }

            String numColorBands = permalinkParamsMap.get("numColorBands");
            if (numColorBands != null) {
                widgetCollection.getPaletteSelector().setNumColorBands(
                        Integer.parseInt(numColorBands));
            }

            String aboveMaxString = URL.decodePathSegment(permalinkParamsMap.get("aboveMaxColor"));
            if (aboveMaxString != null) {
                if ("extend".equalsIgnoreCase(aboveMaxString)) {
                    widgetCollection.getPaletteSelector().setAboveMax(OutOfRangeState.EXTEND);
                } else if ("transparent".equalsIgnoreCase(aboveMaxString)) {
                    widgetCollection.getPaletteSelector().setAboveMax(OutOfRangeState.TRANSPARENT);
                } else if ("0x000000".equalsIgnoreCase(aboveMaxString)) {
                    widgetCollection.getPaletteSelector().setAboveMax(OutOfRangeState.BLACK);
                } else {
                    widgetCollection.getPaletteSelector().setExtraAboveMaxColour(aboveMaxString);
                }
            }

            String belowMinString = URL.decodePathSegment(permalinkParamsMap.get("belowMinColor"));
            if (belowMinString != null) {
                if ("extend".equalsIgnoreCase(belowMinString)) {
                    widgetCollection.getPaletteSelector().setBelowMin(OutOfRangeState.EXTEND);
                } else if ("transparent".equalsIgnoreCase(belowMinString)) {
                    widgetCollection.getPaletteSelector().setBelowMin(OutOfRangeState.TRANSPARENT);
                } else if ("0x000000".equalsIgnoreCase(belowMinString)) {
                    widgetCollection.getPaletteSelector().setBelowMin(OutOfRangeState.BLACK);
                } else {
                    widgetCollection.getPaletteSelector().setExtraBelowMinColour(belowMinString);
                }
            }

            String noDataString = URL.decodePathSegment(permalinkParamsMap.get("noDataColor"));
            if (belowMinString != null) {
                widgetCollection.getPaletteSelector().setNoDataColour(noDataString);
            }

            String currentElevation = permalinkParamsMap.get("elevation");
            if (currentElevation != null) {
                widgetCollection.getElevationSelector().setSelectedElevation(currentElevation);
            }

            String currentPalette = permalinkParamsMap.get("palette");
            if (currentPalette != null) {
                widgetCollection.getPaletteSelector().selectPalette(currentPalette);
            } else {
                widgetCollection.getPaletteSelector().selectPalette("default");
            }

            String currentStyle = permalinkParamsMap.get("style");
            if (currentStyle != null) {
                widgetCollection.getPaletteSelector().selectStyle(currentStyle);
            }

            String currentRange = permalinkParamsMap.get("range");
            if (currentRange != null) {
                widgetCollection.getTimeSelector().selectRange(currentRange);
            }
        }
    }

    @Override
    public void onMapMove(MapMoveEvent eventObject) {
        super.onMapMove(eventObject);
        updateLinksEtc();
    }

    @Override
    public void onMapZoom(MapZoomEvent eventObject) {
        super.onMapZoom(eventObject);
        updateLinksEtc();
    }

    @Override
    protected void requestLayerDetails(String wmsUrl, String layerId, String currentTime,
            boolean autoZoomAndPalette) {
        if (permalinking) {
            currentTime = permalinkParamsMap.get("time");
        }
        super.requestLayerDetails(wmsUrl, layerId, currentTime, autoZoomAndPalette);
    }

    /*
     * Godiva-specific methods
     */

    /**
     * Overrides the parent method using the single-WMS layer. Subclasses should
     * use this to set a {@link UnitConverter}
     * 
     * @param converter
     *            The {@link UnitConverter} to use for the WMS layer
     */
    protected void setUnitConverter(UnitConverter converter) {
        super.setUnitConverter(WMS_LAYER_ID, converter);
    }

    /**
     * Updates the links (KMZ, screenshot, email, permalink...)
     */
    @Override
    public void updateLinksEtc() {
        if (kmzLink == null || widgetCollection.getWmsUrlProvider().getWmsUrl() == null) {
            /*
             * This means that we've not yet finished initialising
             */
            return;
        }
        kmzLink.setEnabled(true);
        permalink.setEnabled(true);
        email.setEnabled(true);
        screenshot.setEnabled(true);

        kmzLink.setHref(mapArea.getKMZUrl());

        String baseurl = "http://" + Window.Location.getHost() + Window.Location.getPath()
                + "?permalinking=true&bgmap=" + mapArea.getBackgroundMapName() + "&";

        PaletteSelectorIF paletteSelector = widgetCollection.getPaletteSelector();

        String aboveMax = paletteSelector.getAboveMaxString();
        String belowMin = paletteSelector.getBelowMinString();
        String noData = paletteSelector.getNoDataColour();
        String urlParams = "dataset=" + widgetCollection.getWmsUrlProvider().getWmsUrl()
                + "&numColorBands=" + paletteSelector.getNumColorBands() + "&logScale="
                + paletteSelector.isLogScale() + "&bbox=" + mapArea.getMap().getExtent()
                + "&abovemaxcolor=" + aboveMax + "&belowmincolor=" + belowMin + "&nodatacolor="
                + noData;

        TimeSelectorIF timeSelector = widgetCollection.getTimeSelector();
        ElevationSelectorIF elevationSelector = widgetCollection.getElevationSelector();

        UnitsInfoIF unitsInfo = widgetCollection.getUnitsInfo();

        String currentLayer = null;
        String currentElevation = null;
        String currentPalette = null;
        String currentStyle = null;
        String scaleRange = null;
        int nColorBands = 0;
        boolean logScale = false;

        String selectedId = layerSelector.getSelectedId();
        if (selectedId != null) {
            currentLayer = selectedId;
            urlParams += "&layer=" + currentLayer;
        }

        if (timeSelector.isContinuous()) {
            if (timeSelector.getSelectedDateTimeRange() != null) {
                urlParams += "&time=" + timeSelector.getSelectedDateTimeRange();
            }
            if (timeSelector.getSelectedDateTime() != null) {
                urlParams += "&targettime=" + timeSelector.getSelectedDateTime();
            }
            if (timeSelector.getRange() != null) {
                urlParams += "&range=" + timeSelector.getRange();
            }
        } else {
            if (timeSelector.getSelectedDateTime() != null) {
                urlParams += "&time=" + timeSelector.getSelectedDateTime();
            }
        }
        if (elevationSelector.isContinuous()) {
            if (elevationSelector.getSelectedElevationRange() != null) {
                currentElevation = elevationSelector.getSelectedElevation();
                urlParams += "&targetelevation=" + currentElevation;
            }
            if (elevationSelector.getSelectedElevationRange() != null) {
                currentElevation = elevationSelector.getSelectedElevationRange();
                urlParams += "&elevation=" + currentElevation;
            }
        } else {
            if (elevationSelector.getSelectedElevation() != null) {
                currentElevation = elevationSelector.getSelectedElevation();
                urlParams += "&elevation=" + currentElevation;
            }
        }
        if (paletteSelector.getSelectedPalette() != null) {
            currentPalette = paletteSelector.getSelectedPalette();
            urlParams += "&palette=" + currentPalette;
        }
        if (paletteSelector.getSelectedStyle() != null) {
            currentStyle = paletteSelector.getSelectedStyle();
            urlParams += "&style=" + currentStyle;
        }
        if (paletteSelector.getScaleRange() != null) {
            scaleRange = paletteSelector.getScaleRange();
            urlParams += "&scaleRange=" + scaleRange;
        }
        urlParams += "&opacity=" + paletteSelector.getOpacity();

        anim.updateDetails(currentLayer, currentElevation, currentPalette, currentStyle,
                scaleRange, aboveMax, belowMin, noData, nColorBands, logScale);

        /* Don't need to encode the query string, because we never decode it */
        permalink.setHref(URL.encode(baseurl + urlParams));
        email.setHref("mailto:?subject=MyOcean Data Link&body="
                + URL.encodeQueryString(baseurl + GodivaUtils.encodeQueryString(urlParams)));

        // Screenshot-only stuff
        urlParams += "&bbox=" + mapArea.getMap().getExtent().toBBox(6);
        if (layerSelector != null) {
            StringBuilder title = new StringBuilder();
            if (layerSelector.getTitleElements() != null
                    && layerSelector.getTitleElements().size() > 0) {
                for (String element : layerSelector.getTitleElements()) {
                    title.append(element + ",");
                }
                title.deleteCharAt(title.length() - 1);
                urlParams += "&layerTitle=" + title;
            }
        }
        urlParams += "&crs=" + mapArea.getMap().getProjection();
        urlParams += "&mapHeight=" + mapHeight;
        urlParams += "&mapWidth=" + mapWidth;
        urlParams += "&style=" + currentStyle;
        if (elevationSelector != null)
            urlParams += "&zUnits=" + elevationSelector.getVerticalUnits();
        if (unitsInfo != null)
            urlParams += "&units=" + unitsInfo.getUnits();
        urlParams += "&baseUrl=" + mapArea.getBaseLayerUrl();
        urlParams += "&baseLayers=" + mapArea.getBaseLayerLayers();

        String godivaPath = Window.Location.getPath();
        screenshot.setHref("http://" + Window.Location.getHost() + "/"
                + godivaPath.substring(0, godivaPath.lastIndexOf('/'))
                + "/screenshots/createScreenshot?" + GodivaUtils.encodeQueryString(urlParams));
    }

    /*
     * We define a set of methods here to get the initial widgets needed. That
     * way, a subclass can easily override these methods to replace them with
     * custom implementations, whilst still keeping the Godiva feature set (i.e.
     * one WMS layer at a time)
     */

    protected void menuLoaded(LayerMenuItem menuTree) {
        if (menuTree.isLeaf()) {
            menuTree.addChildItem(new LayerMenuItem("No datasets found!", null, null, false, null));
        }
        layerSelector.populateLayers(menuTree);

        Window.setTitle(menuTree.getTitle());

        if (permalinking) {
            String bgMap = permalinkParamsMap.get("bgmap");
            if (bgMap != null && !"".equals(bgMap)) {
                try {
                    mapArea.setBackgroundMap(bgMap);
                } catch (Exception e) {
                    /*
                     * It doesn't matter if we can't set the background map, so
                     * ignore any exceptions
                     */
                }
            }
            String currentLayer = permalinkParamsMap.get("layer");
            if (currentLayer != null) {
                String datasetUrl = permalinkParamsMap.get("dataset");
                if (datasetUrl == null || "".equals(datasetUrl)) {
                    String path = Window.Location.getHost() + Window.Location.getPath();
                    path = path.substring(0, path.lastIndexOf("/"));
                    final String wmsUrl = "http://" + path + "/wms";
                    layerSelector.selectLayer(currentLayer, wmsUrl, false);
                } else {
                    String currentWms = URL.decode(datasetUrl);
                    layerSelector.selectLayer(currentLayer, currentWms, false);
                }
            }
        }
    }

    /*
     * We define a set of methods here to get the initial widgets needed. That
     * way, a subclass can easily override these methods to replace them with
     * custom implementations, whilst still keeping the Godiva feature set (i.e.
     * one WMS layer at a time)
     */

    /**
     * This is something that will almost certainly be changed for custom
     * clients, so put it in a method which can be overridden
     */
    protected Image getLogo() {
        return new Image(GWT.getModuleBaseURL() + "img/logo.png");
    }

    /**
     * Gets a new palette selector. This method is used in the construction of
     * the Godiva interface and is only called once.
     * 
     * @param wmsUrlProvider
     *            A {@link LayerSelectorIF} which provides the currently
     *            selected WMS URL
     * @return A new instance of a {@link PaletteSelectorIF}
     */
    protected PaletteSelectorIF getPaletteSelector(LayerSelectorIF wmsUrlProvider,
            CentrePosIF localCentre) {
        return new PaletteSelector("mainLayer", getMapHeight(), 30, this, wmsUrlProvider,
                localCentre, true);
    }

    /**
     * Gets a new time selector. This method is used in the construction of the
     * Godiva interface and is only called once.
     * 
     * @return A new instance of a {@link TimeSelectorIF}
     */
    protected TimeSelectorIF getTimeSelector() {
        return new TimeSelector("mainLayer", this);
    }

    /**
     * Gets a new elevation selector. This method is used in the construction of
     * the Godiva interface and is only called once.
     * 
     * @return A new instance of an {@link ElevationSelectorIF}
     */
    protected ElevationSelectorIF getElevationSelector() {
        return new ElevationSelector("mainLayer", "Depth", this);
    }

    private HTML titleLabel;

    /**
     * Gets a new layer selector. This method is used in the construction of the
     * Godiva interface and is only called once.
     * 
     * @return A new instance of a {@link LayerSelectorIF}
     */
    protected LayerSelectorIF getLayerSelector() {
        titleLabel = new HTML();
        return new LayerSelectorTree(this, titleLabel);
    }

    /**
     * Gets the layout. This should return a top-level object which contains the
     * entire page, since this is what gets added to the main window as the
     * layout
     */
    protected Widget getLayout() {
        return LayoutManager.getTraditionalGodiva3Layout(layerSelector, titleLabel,
                widgetCollection.getUnitsInfo(), widgetCollection.getTimeSelector(),
                widgetCollection.getElevationSelector(), widgetCollection.getPaletteSelector(),
                kmzLink, permalink, email, screenshot, logo, mapArea, loadingImage, anim,
                infoButton, zoomToLayerExtents);
        //        return LayoutManager.getCompactGodiva3Layout(layerSelector,
        //                widgetCollection.getUnitsInfo(), widgetCollection.getTimeSelector(),
        //                widgetCollection.getElevationSelector(), widgetCollection.getPaletteSelector(),
        //                kmzLink, permalink, email, screenshot, logo, mapArea, loadingImage, anim,
        //                infoButton);
    }
}
