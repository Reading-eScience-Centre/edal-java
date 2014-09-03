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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtopenmaps.openlayers.client.OpenLayers;

import uk.ac.rdg.resc.godiva.client.handlers.ElevationSelectionHandler;
import uk.ac.rdg.resc.godiva.client.handlers.GodivaActionsHandler;
import uk.ac.rdg.resc.godiva.client.handlers.LayerSelectionHandler;
import uk.ac.rdg.resc.godiva.client.handlers.PaletteSelectionHandler;
import uk.ac.rdg.resc.godiva.client.handlers.TimeDateSelectionHandler;
import uk.ac.rdg.resc.godiva.client.requests.ConnectionException;
import uk.ac.rdg.resc.godiva.client.requests.ErrorHandler;
import uk.ac.rdg.resc.godiva.client.requests.LayerDetails;
import uk.ac.rdg.resc.godiva.client.requests.LayerRequestBuilder;
import uk.ac.rdg.resc.godiva.client.requests.LayerRequestCallback;
import uk.ac.rdg.resc.godiva.client.requests.TimeRequestBuilder;
import uk.ac.rdg.resc.godiva.client.requests.TimeRequestCallback;
import uk.ac.rdg.resc.godiva.client.state.GodivaStateInfo;
import uk.ac.rdg.resc.godiva.client.util.UnitConverter;
import uk.ac.rdg.resc.godiva.client.widgets.DialogBoxWithCloseButton;
import uk.ac.rdg.resc.godiva.client.widgets.MapArea;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A class to handle the common operations needed in an EDAL wms client.
 * 
 * This is not dependent on there being only a single WMS layer viewable at a
 * time - i.e. each WMS layer can have associated with it an entire set of
 * widgets, in the form of a {@link GodivaStateInfo} object. For a subclass
 * which uses this to implement a single WMS layer client, see {@link Godiva}
 * 
 * @author Guy Griffiths
 * 
 */
public abstract class BaseWmsClient implements EntryPoint, ErrorHandler, GodivaActionsHandler,
        LayerSelectionHandler, ElevationSelectionHandler, TimeDateSelectionHandler,
        PaletteSelectionHandler {

    /*
     * State variables.
     */
    protected int mapHeight;
    protected int mapWidth;
    protected String proxyUrl;
    protected String docHref;

    /*
     * We need this because the call to layerDetails (where we receive this
     * time) is separate to the call where we discover what actual times (as
     * opposed to dates) are available
     */
    protected String nearestTime;

    /*
     * Map widget
     */
    protected MapArea mapArea;

    /*
     * A count of how many items we are currently waiting to load.
     */
    private int loadingCount;

    /*
     * These 3 booleans are used so that we only update the map when all
     * required data have been loaded
     */
    protected boolean layerDetailsLoaded;
    protected boolean dateTimeDetailsLoaded;
    protected boolean minMaxDetailsLoaded;

    /**
     * This is the entry point for GWT.
     * 
     * Queries a config servlet and sets some global fields. If the config is
     * not present, or there is an error, sets some defaults, and calls the
     * initialisation method.
     */
    @Override
    public void onModuleLoad() {
        /*
         * Set the path for OpenLayers images. This means that we can package
         * them with this GWT source code, keeping everything nicely separated
         */
        setImagePath(GWT.getModuleBaseURL() + "/js/img/");

        /*
         * The location of the config servlet is hard-coded. If it is not found,
         * we use some default options
         */
        RequestBuilder getConfig = new RequestBuilder(RequestBuilder.GET, "getconfig");
        getConfig.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                try {
                    JSONValue jsonMap = JSONParser.parseLenient(response.getText());
                    JSONObject parentObj = jsonMap.isObject();
                    proxyUrl = parentObj.get("proxy").isString().stringValue();
                    docHref = parentObj.get("docLocation").isString().stringValue();
                    mapHeight = Integer.parseInt(parentObj.get("mapHeight").isString()
                            .stringValue());
                    mapWidth = Integer.parseInt(parentObj.get("mapWidth").isString().stringValue());
                    initBaseWms();
                    /*
                     * Handle any user-specific configuration parameters
                     */
                    handleCustomParams(parentObj);
                } catch (Exception e) {
                    /*
                     * Catching a plain Exception - not something that should
                     * generally be done.
                     * 
                     * However...
                     * 
                     * We explicitly *do* want to handle *all* possible runtime
                     * exceptions in the same manner.
                     */
                    initWithDefaults();
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
                /*
                 * If we have a problem contacting the config servlet, use some
                 * default options
                 */
                initWithDefaults();
            }
        });

        try {
            getConfig.send();
        } catch (RequestException e) {
            initWithDefaults();
        }
    }

    /**
     * Uses native Javascript to set the image path for OpenLayers
     * 
     * @param imagepath
     *            The path where OpenLayers images are stored
     */
    private static native void setImagePath(String imagepath)/*-{
                                                             $wnd.OpenLayers.ImgPath = imagepath;
                                                             }-*/;

    /**
     * This is called after all other parameters have been received from a
     * config servlet, and subclasses can use it to handle custom configuration
     * options from the ConfigServlet.
     * 
     * @param parentObj
     */
    protected void handleCustomParams(JSONObject parentObj) {
        /*
         * Does nothing, but if a custom client adds to the config options, this
         * can be overridden to handle them
         */
    }

    /**
     * Initializes the WMS client with some default settings.
     * 
     * Subclasses can override this to define new defaults
     */
    protected void initWithDefaults() {
        mapHeight = 400;
        mapWidth = 512;
        /*
         * No proxy by default, because by default we run on the same server as
         * ncWMS
         */
        proxyUrl = "";
        docHref = "http://www.resc.rdg.ac.uk/trac/ncWMS/wiki/GodivaTwoUserGuide";
        initBaseWms();
    }

    /**
     * Initialises the necessary elements and then passes to a subclass method
     * for layout and initialisation of other widgets
     */
    private void initBaseWms() {
        loadingCount = 0;
        mapArea = getMapArea();

        /*
         * Call the subclass initialisation
         */
        init();

        /*
         * Set this at the last possible moment, so that subclasses can set it
         * if they like
         */
        OpenLayers.setProxyHost(proxyUrl);

        /*
         * Now request the menu from the ncWMS server
         */
        requestAndPopulateMenu();
    }

    /**
     * @return A new {@link MapArea}. This will be called once. Subclasses can
     *         override this method to use specialised subclasses of
     *         {@link MapArea}
     */
    protected MapArea getMapArea() {
        return new MapArea(mapWidth, mapHeight, this, proxyUrl);
    }

    /**
     * Builds a URL given the request name and a {@link Map} of parameters
     * 
     * @param request
     *            the request name (e.g. GetMap)
     * @param parameters
     *            a {@link Map} of parameters and their values
     * @return the URL of the request
     */
    protected String getWmsRequestUrl(String wmsUrl, String request, Map<String, String> parameters) {
        StringBuilder url = new StringBuilder();
        url.append("?request=" + request);
        for (String key : parameters.keySet()) {
            url.append("&" + key + "=" + parameters.get(key));
        }
        return getUrlFromGetArgs(wmsUrl, url.toString());
    }

    /**
     * Encodes the URL, including proxy and base WMS URL
     * 
     * @param url
     *            the part of the URL representing the GET arguments
     * @return the encoded URL
     */
    protected String getUrlFromGetArgs(String wmsUrl, String url) {
        return URL.encode(proxyUrl + wmsUrl + url);
    }

    /**
     * Gets the height of the map in the map widget
     * 
     * @return the height in pixels
     */
    protected int getMapHeight() {
        return mapHeight;
    }

    /**
     * Gets the width of the map in the map widget
     * 
     * @return the width in pixels
     */
    protected int getMapWidth() {
        return mapWidth;
    }

    /**
     * Request details about a particular layer. Once loaded, layerDetailsLoaded
     * will be called.
     * 
     * @param wmsUrl
     *            the base URL of the WMS server containing the layer details
     * @param layerId
     *            the ID of the layer whose details are desired
     * @param currentTime
     *            the time we want to know the closest available time to. Can be
     *            null
     * @param autoZoomAndPalette
     *            true if we want to zoom to extents and possibly auto-adjust
     *            palette once the details have been loaded. Note that this will
     *            only auto-adjust the palette if the conditions are right
     */
    protected void requestLayerDetails(final String wmsUrl, final String layerId,
            String currentTime, final boolean autoZoomAndPalette) {
        if (layerId == null) {
            /*
             * We have no variables defined in the selected layer
             * 
             * Return here. We are already dealing with the case where there are
             * no layers present.
             */
            return;
        }
        /*
         * The map should only get updated once all details are loaded
         */
        layerDetailsLoaded = false;
        dateTimeDetailsLoaded = false;
        minMaxDetailsLoaded = false;

        final LayerRequestBuilder getLayerDetailsRequest = new LayerRequestBuilder(layerId,
                proxyUrl + wmsUrl, currentTime);

        getLayerDetailsRequest.setCallback(new LayerRequestCallback(layerId, this) {
            @Override
            public void onResponseReceived(Request req, Response response) {
                try {
                    super.onResponseReceived(req, response);
                    if (response.getStatusCode() != Response.SC_OK) {
                        throw new ConnectionException("Error contacting server");
                    }
                    /*
                     * This will make a call to populateWidgets, and may create
                     * extra widgets if needed (e.g. for multi-layer clients)
                     */
                    layerDetailsLoaded(getLayerDetails(), autoZoomAndPalette);

                    /*
                     * Zoom to extents and possible auto-adjust palette
                     */
                    if (autoZoomAndPalette) {
                        try {
                            mapArea.zoomToExtents(getLayerDetails().getExtents());
                        } catch (Exception e) {
                            handleError(e);
                        }
                        /*
                         * We request an auto-range. Since force=false, this
                         * will only request the auto-range if the server-side
                         * value has not been configured
                         */
                        maybeRequestAutoRange(getLayerDetails().getId(), false);
                    } else {
                        minMaxDetailsLoaded = true;
                    }
                    layerDetailsLoaded = true;
                    /*
                     * Once we have done everything else we update the map. Note
                     * that this will only actually update the map if all three
                     * flags (layerDetailsLoaded, minMaxDetailsLoaded, and
                     * dateTimeDetailsLoaded) are set to true.
                     */
                    updateMapBase(layerId);
                } catch (Exception e) {
                    invalidJson(e, response.getText(), getLayerDetailsRequest.getUrl());
                } finally {
                    /*
                     * Indicate that we have finished this loading operation
                     */
                    setLoading(false);
                }
            }

            @Override
            public void onError(Request request, Throwable e) {
                /*
                 * We have an error. We set the state variables correctly and
                 * update the map, then handle the error that occurred
                 */
                setLoading(false);
                layerDetailsLoaded = true;
                minMaxDetailsLoaded = true;
                updateMapBase(layerId);
                handleError(e);
            }
        });

        try {
            /*
             * Register that we are loading something, then send the request
             */
            setLoading(true);
            getLayerDetailsRequest.send();
        } catch (RequestException e) {
            /*
             * If this fails, set the loading state
             */
            setLoading(false);
            layerDetailsLoaded = true;
            handleError(e);
        }
    }

    /**
     * Possibly requests the auto-detected scale range. This will make the
     * request if {@code force} is true, or we have a default scale range set on
     * the server
     * 
     * @param layerId
     *            the ID of the layer to request the scale range for
     * @param force
     *            whether to perform even if a scale has been set on the server
     */
    protected void maybeRequestAutoRange(final String layerId, boolean force) {
        GodivaStateInfo widgetCollection = getWidgetCollection(layerId);

        /*
         * If the palette is disabled, we don't want to get an auto-range
         */
        if (!widgetCollection.getPaletteSelector().isEnabled()) {
            minMaxDetailsLoaded = true;
            return;
        }

        /*
         * If we have default values for the scale range or force=true, then
         * continue with the request, otherwise return.
         * 
         * Note that the current default scale range is [-50, 50]. This should
         * probably be signalled in a more appropriate manner
         */
        String[] scaleRangeSplit = widgetCollection.getPaletteSelector().getScaleRange().split(",");
        if (!force
                && (Double.parseDouble(scaleRangeSplit[0]) != -50 || Double
                        .parseDouble(scaleRangeSplit[1]) != 50)) {
            minMaxDetailsLoaded = true;
            return;
        }

        minMaxDetailsLoaded = false;

        Map<String, String> parameters = new HashMap<String, String>();
        /*
         * We are making a general GetMetadata request, so we need to set the
         * item to "minmax"
         */
        parameters.put("item", "minmax");
        parameters.put("layers", layerId);

        parameters.put("styles", widgetCollection.getPaletteSelector().getSelectedStyle());

        /*
         * We use 1.1.1 here, because if getMap().getProjection() returns
         * EPSG:4326, getMap().getExtent().toBBox(4) will still return in
         * lon-lat order
         */
        parameters.put("version", "1.1.1");
        parameters.put("bbox", mapArea.getMap().getExtent().toBBox(4));
        /*
         * We put crs as well as srs, because some older versions of THREDDS
         * seem to still want "CRS" even with 1.1.1...
         */
        parameters.put("srs", mapArea.getMap().getProjection());
        parameters.put("crs", mapArea.getMap().getProjection());

        if (widgetCollection.getTimeSelector().isContinuous()) {
            /*
             * Continuous time ranges need both a "time" (a range) and a
             * "targettime" (single value) from the time selector
             */
            if (widgetCollection.getTimeSelector().getSelectedDateTime() != null) {
                parameters.put("TARGETTIME", widgetCollection.getTimeSelector()
                        .getSelectedDateTime());
            }
            if (widgetCollection.getTimeSelector().getSelectedDateTimeRange() != null) {
                parameters.put("time", widgetCollection.getTimeSelector()
                        .getSelectedDateTimeRange());
            }
        } else {
            /*
             * Discrete time ranges just need a single time
             */
            if (widgetCollection.getTimeSelector().getSelectedDateTime() != null) {
                parameters.put("time", widgetCollection.getTimeSelector().getSelectedDateTime());
            }
        }

        if (widgetCollection.getElevationSelector().isContinuous()) {
            /*
             * Continuous depth ranges need both an "elevation" (a range) and a
             * "colorby/depth" (single value) from the elevation selector
             */
            if (widgetCollection.getElevationSelector().getSelectedElevation() != null) {
                parameters.put("TARGETELEVATION", widgetCollection.getElevationSelector()
                        .getSelectedElevation());
            }
            if (widgetCollection.getElevationSelector().getSelectedElevationRange() != null) {
                parameters.put("elevation", widgetCollection.getElevationSelector()
                        .getSelectedElevationRange());
            }
        } else {
            /*
             * Discrete elevation ranges just need a single elevation
             */
            if (widgetCollection.getElevationSelector().getSelectedElevation() != null) {
                parameters.put("elevation", widgetCollection.getElevationSelector()
                        .getSelectedElevation());
            }
        }
        /*
         * We just request a 100x100 pixel subsample of the data for getting the
         * auto-range. The bigger this grid, the more accurate the result, but
         * the longer it will take.
         * 
         * Note that for in-situ datasets, this size is irrelevant, since
         * reprojection of data will never subsample it.
         */
        parameters.put("height", "100");
        parameters.put("width", "100");
        if (nearestTime != null) {
            parameters.put("time", nearestTime);
        }

        /*
         * Assemble the mix-max request
         */
        final RequestBuilder getMinMaxRequest = new RequestBuilder(RequestBuilder.GET,
                getWmsRequestUrl(widgetCollection.getWmsUrlProvider().getWmsUrl(), "GetMetadata",
                        parameters));
        getMinMaxRequest.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request req, Response response) {
                /*
                 * Extract the min and max values from the JSON, handling any
                 * errors
                 */
                try {
                    if (response.getText() != null && !response.getText().isEmpty()) {
                        JSONValue jsonMap = JSONParser.parseLenient(response.getText());
                        JSONObject parentObj = jsonMap.isObject();
                        JSONNumber minNum = parentObj.get("min").isNumber();
                        JSONNumber maxNum = parentObj.get("max").isNumber();

                        double min = minNum == null ? -50 : minNum.doubleValue();
                        double max = maxNum == null ? 50 : maxNum.doubleValue();
                        /*
                         * Call the rangeLoaded method. All this does it set the
                         * range on the appropriate widget, but subclasses may
                         * want to add additional behaviour, so it is a separate
                         * method
                         */
                        rangeLoaded(layerId, min, max);
                    }
                } catch (Exception e) {
                    invalidJson(e, response.getText(), getMinMaxRequest.getUrl());
                } finally {
                    /*
                     * Set the state correctly and update the map, regardless of
                     * whether we succeeded or failed
                     */
                    minMaxDetailsLoaded = true;
                    updateMapBase(layerId);
                    setLoading(false);
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
                /*
                 * Set the state correctly and update the map, regardless of
                 * whether we succeeded or failed
                 */
                setLoading(false);
                minMaxDetailsLoaded = true;
                updateMapBase(layerId);
                handleError(exception);
            }
        });
        setLoading(true);
        try {
            getMinMaxRequest.send();
        } catch (RequestException e) {
            setLoading(false);
            minMaxDetailsLoaded = true;
            updateMapBase(layerId);
            handleError(e);
        }
    }

    /**
     * This is called when an auto scale range has been loaded. It can be
     * assumed that by this point we want to update the scale.
     * 
     * @param layerId
     *            the layer for which the scale range has been loaded
     * @param min
     *            the minimum scale value
     * @param max
     *            the maximum scale value
     */
    protected void rangeLoaded(String layerId, double min, double max) {
        getWidgetCollection(layerId).getPaletteSelector().setScaleRange(min + "," + max, null);
    }

    /**
     * This is called once a layer's details have been loaded. By default this
     * will just populate the widgets associated with this layer, but subclasses
     * may want to override this method to implement custom behaviour
     * before/after populating the widgets
     * 
     * @param layerDetails
     *            the details received from the server
     * @param autoUpdate
     *            whether or not we want to auto update palette and zoom
     */
    protected void layerDetailsLoaded(LayerDetails layerDetails, boolean autoUpdate) {
        /*
         * We want to populate the widgets associated with this layer. Any other
         * actions which are needed should be implemented by the subclass
         */
        populateWidgets(layerDetails);
    }

    /**
     * Populates the set of widgets associated with this layer. The ID of the
     * layer is taken from the {@link LayerDetails} and the associated widgets
     * are retrieved and populated
     * 
     * @param layerDetails
     *            a {@link LayerDetails} object containing the layer details.
     *            This gets returned when layer details are loaded
     */
    protected void populateWidgets(LayerDetails layerDetails) {
        if (layerDetails == null) {
            handleError(new NullPointerException("Null layer details"));
        }

        GodivaStateInfo widgetCollection = getWidgetCollection(layerDetails.getId());

        if (widgetCollection == null) {
            handleError(new NullPointerException("Null widget collection"));
        }

        /*
         * GodivaStateInfo.get*() cannot return null, so all
         * widgetCollection.get* methods are NullPointerException safe
         */

        /*
         * Set the layer ID for the widgets that need it
         */
        widgetCollection.getElevationSelector().setId(layerDetails.getId());
        widgetCollection.getTimeSelector().setId(layerDetails.getId());
        widgetCollection.getPaletteSelector().setId(layerDetails.getId());

        /*
         * Set some static information
         */
        widgetCollection.getUnitsInfo().setUnits(layerDetails.getUnits());
        widgetCollection.getCopyrightInfo().setCopyrightInfo(layerDetails.getCopyright());
        widgetCollection.getMoreInfo().setInfo(layerDetails.getMoreInfo());
        widgetCollection.getElevationSelector().setUnitsAndDirection(layerDetails.getZUnits(),
                layerDetails.isZPositive());

        /*
         * Populate the palette options
         */
        widgetCollection.getPaletteSelector().populatePalettes(layerDetails.getAvailablePalettes());
        widgetCollection.getPaletteSelector().populateStyles(layerDetails.getSupportedStyles());
        if (!widgetCollection.getPaletteSelector().isLocked()) {
            widgetCollection.getPaletteSelector().setScaleRange(layerDetails.getScaleRange(),
                    layerDetails.isLogScale());
            widgetCollection.getPaletteSelector().setNumColorBands(layerDetails.getNumColorBands());
        }

        /*
         * Set things dependent on whether we have a multi-feature layer (i.e.
         * one with continuous depth/time axes which can show data from multiple
         * features. This usually corresponds to in-situ data, but not
         * necessarily)
         */
        widgetCollection.getTimeSelector().setContinuous(layerDetails.isContinuousT());
        widgetCollection.getElevationSelector().setContinuous(layerDetails.isContinuousZ());
        mapArea.setAllowTransects(layerDetails.supportsTransects());

        if (layerDetails.isContinuousT()) {
            /*
             * Set all options which depend on this being a layer with a
             * continuous t-axis
             */
            if (layerDetails.getStartTime().equals(layerDetails.getEndTime())) {
                /*
                 * We have a continuous axis with one value. We can treat it as
                 * non-continuous...
                 */
                widgetCollection.getTimeSelector().setContinuous(false);
                String[] split = layerDetails.getStartTime().split("T");
                List<String> date = new ArrayList<String>();
                date.add(split[0]);
                widgetCollection.getTimeSelector().populateDates(date);
                List<String> time = new ArrayList<String>();
                time.add(split[1]);
                widgetCollection.getTimeSelector().populateTimes(time);
                dateTimeDetailsLoaded = true;
            } else {
                List<String> startEndDates = new ArrayList<String>();
                startEndDates.add(layerDetails.getStartTime());
                startEndDates.add(layerDetails.getEndTime());
                widgetCollection.getTimeSelector().populateDates(startEndDates);
            }
            if (layerDetails.getNearestDateTime() != null) {
                widgetCollection.getTimeSelector()
                        .selectDateTime(layerDetails.getNearestDateTime());
            } else {
                dateTimeDetailsLoaded = true;
            }
        } else {
            /*
             * Set all options which depend on this having a discrete t-axis
             */
            widgetCollection.getTimeSelector().populateDates(layerDetails.getAvailableDates());
            if (layerDetails.getNearestDateTime() != null) {
                nearestTime = layerDetails.getNearestDateTime();
                widgetCollection.getTimeSelector().selectDate(layerDetails.getNearestDate());
            } else {
                dateTimeDetailsLoaded = true;
            }
        }
        if (layerDetails.isContinuousZ()) {
            /*
             * Set all options which depend on this being a layer with a
             * continuous z-axis
             */
            if (layerDetails.getStartZ() != null && layerDetails.getEndZ() != null) {
                if (layerDetails.getStartZ().equals(layerDetails.getEndZ())) {
                    widgetCollection.getElevationSelector().populateElevations(null);
                } else {
                    List<String> startEndZs = new ArrayList<String>();
                    startEndZs.add(layerDetails.getStartZ());
                    startEndZs.add(layerDetails.getEndZ());
                    widgetCollection.getElevationSelector().populateElevations(startEndZs);
                }
            } else if (layerDetails.getAvailableZs() != null) {
                widgetCollection.getElevationSelector().populateElevations(
                        layerDetails.getAvailableZs());
            } else {
                /*
                 * We have either the start or end z being null
                 */
                widgetCollection.getElevationSelector().populateElevations(null);
            }
        } else {
            /*
             * Set all options which depend on this having a discrete z-axis
             */
            widgetCollection.getElevationSelector().populateElevations(
                    layerDetails.getAvailableZs());
        }
    }

    /**
     * Checks that all the required details are loaded (or do not need to be)
     * before calling the subclass method
     */
    protected void updateMapBase(String layerUpdated) {
        if (layerDetailsLoaded && dateTimeDetailsLoaded && minMaxDetailsLoaded) {
            mapArea.updatePos();
            updateMap(mapArea, layerUpdated);
        }
    }

    /**
     * Handles the case where we are unable to parse JSON data returned from the
     * server, or where non-JSON data is returned when we expected JSON data.
     * This covers a number of situations and so needs to be quite a general
     * method.
     * 
     * @param e
     *            The exception which was caught
     * @param response
     *            The message text
     * @param url
     *            The URL which caused the exception
     */
    protected void invalidJson(Exception e, String response, String url) {
        e.printStackTrace();
        final DialogBoxWithCloseButton popup = new DialogBoxWithCloseButton(mapArea);
        VerticalPanel v = new VerticalPanel();
        if (e instanceof ConnectionException) {
            v.add(new Label(e.getMessage()));
        } else {
            v.add(new Label("The server has experienced an error"));
            v.add(new Label("Please try again in a short while"));
            v.add(new Label("The URL which behaved unexpectedly was:"));
            v.add(new Label(url));
            v.add(new Label("The response from the server was:"));
            v.add(new Label(response));
        }
        popup.setHTML("Server Error");
        popup.setWidget(v);
        popup.center();
    }

    /*
     * Methods which are part of the interfaces we implement
     */

    /*
     * Methods from LayerSelectionHandler
     */

    @Override
    public void layerSelected(String wmsUrl, String layerId, boolean autoZoomAndPalette) {
        requestLayerDetails(wmsUrl, layerId, getCurrentTime(), autoZoomAndPalette);
    }

    @Override
    public void layerDeselected(String layerId) {
        mapArea.removeLayer(layerId);
    }

    @Override
    public void refreshLayerList() {
        requestAndPopulateMenu();
    }

    /*
     * From ElevationSelectionHandler
     */

    @Override
    public void elevationSelected(String layerId, String elevation) {
        updateMapBase(layerId);
    }

    /*
     * From PaletteSelectionHandler
     */

    @Override
    public void paletteChanged(String layerId, String paletteName, String style, int nColorBands) {
        updateMapBase(layerId);
    }

    @Override
    public void scaleRangeChanged(String layerId, String scaleRange) {
        updateMapBase(layerId);
    }

    @Override
    public void logScaleChanged(String layerId, boolean newIsLogScale) {
        updateMapBase(layerId);
    }

    @Override
    public void autoAdjustPalette(String layerId) {
        maybeRequestAutoRange(layerId, true);
    }

    /*
     * Methods from TimeDateSelectionHandler
     */

    @Override
    public void dateSelected(final String layerId, String selectedDate) {
        if (selectedDate == null) {
            dateTimeDetailsLoaded = true;
            updateMapBase(layerId);
            return;
        }
        dateTimeDetailsLoaded = false;
        final TimeRequestBuilder getTimeRequest = new TimeRequestBuilder(layerId, selectedDate,
                proxyUrl + getWidgetCollection(layerId).getWmsUrlProvider().getWmsUrl());
        getTimeRequest.setCallback(new TimeRequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                try {
                    super.onResponseReceived(request, response);
                    if (response.getStatusCode() != Response.SC_OK) {
                        throw new ConnectionException("Error contacting server");
                    }
                    availableTimesLoaded(layerId, getAvailableTimesteps(), nearestTime);
                    datetimeSelected(layerId, getWidgetCollection(layerId).getTimeSelector()
                            .getSelectedDateTime());
                } catch (Exception e) {
                    invalidJson(e, response.getText(), getTimeRequest.getUrl());
                } finally {
                    setLoading(false);
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
                setLoading(false);
                dateTimeDetailsLoaded = true;
                updateMapBase(layerId);
                handleError(exception);
            }
        });

        try {
            setLoading(true);
            getTimeRequest.send();
        } catch (RequestException e) {
            handleError(e);
        }
    }

    @Override
    public void datetimeSelected(String layerId, String selectedTime) {
        dateTimeDetailsLoaded = true;
        nearestTime = null;
        updateMapBase(layerId);
    }

    /**
     * Allows the client to use different units to those specified on the server
     * (e.g. convert celcius to kelvin)
     * 
     * @param layerId
     *            The ID of the layer to convert units for
     * @param converter
     *            The {@link UnitConverter} to use for conversion
     */
    protected void setUnitConverter(String layerId, UnitConverter converter) {
        /*
         * Set the unit converter for all required components (palette selector
         * for colour scales, and map area for GetFeatureInfo conversion)
         */
        getWidgetCollection(layerId).getPaletteSelector().setUnitConverter(converter);
        mapArea.setUnitConverter(layerId, converter);
    }

    /*
     * Methods from GodivaActionsHandler (others are delegated to subclasses)
     */

    @Override
    public void setLoading(boolean loading) {
        /*
         * Adjust the loading count, and call the loadingStarted() or
         * loadingFinished() methods as appropriate
         */
        if (loading) {
            loadingCount++;
            if (loadingCount == 1) {
                loadingStarted();
            }
        } else {
            loadingCount--;
            if (loadingCount == 0) {
                loadingFinished();
            }
        }
    }

    @Override
    public void onMapMove(MapMoveEvent eventObject) {
        /*
         * Do nothing. Subclasses may or may not want to.
         * 
         * We implement this here so that we receive the events and subclasses
         * don't *have* to bother with implementing and registering the callback
         */
    }

    @Override
    public void onMapZoom(MapZoomEvent eventObject) {
        /*
         * Do nothing. Subclasses may or may not want to.
         * 
         * We implement this here so that we receive the events and subclasses
         * don't *have* to bother with implementing and registering the callback
         */
    }

    /*
     * Method from ErrorHandler
     */

    @Override
    public void handleError(Throwable e) {
        /*
         * This is not ideal, but there is little we can do about some of these
         * exceptions. This at least logs the error for debugging.
         */
        e.printStackTrace();
    }

    /*
     * Abstract methods to be implemented by a subclass
     */

    /**
     * This gets called once the page has loaded. Subclasses should use for
     * initializing any widgets, and setting the layout. If this is not
     * implemented, a blank page will be displayed
     */
    protected abstract void init();

    /**
     * Gets the {@link GodivaStateInfo} for the specified layer
     * 
     * @param layerId
     *            The WMS layer ID
     * @return The state information, usually in the form of widgets
     */
    protected abstract GodivaStateInfo getWidgetCollection(String layerId);

    /**
     * This is called at initialisation, and is used to populate the layer
     * selection menu(s). Subclasses should use this to request any data and
     * then populate the appropriate widget(s)
     */
    protected abstract void requestAndPopulateMenu();

    /**
     * This is called once a list of available times has been loaded
     * 
     * @param layerId
     *            the layer for which times have been loaded
     * @param availableTimes
     *            a {@link List} of available times
     * @param nearestTime
     *            the nearest time to the current time (for e.g. auto selection)
     */
    protected abstract void availableTimesLoaded(String layerId, List<String> availableTimes,
            String nearestTime);

    /**
     * This is where the map should be updated. It gets called when all details
     * have been loaded and we actually want to update the map. Clients should
     * handle this in the appropriate way, which will almost certainly involve a
     * call to mapArea.addLayer(...)
     * 
     * @param mapArea
     *            The map area which can be updated
     * @param layerUpdated
     *            The ID of the layer which we are viewing
     */
    protected abstract void updateMap(MapArea mapArea, String layerUpdated);

    /**
     * This is called when a loading process starts
     */
    protected abstract void loadingStarted();

    /**
     * This is called when all loading processes have finished
     */
    protected abstract void loadingFinished();

    /**
     * Returns the "current" time. The definition of current may depend on
     * exactly what the client does. This is the time used when requesting layer
     * details from the server - i.e. the time to which the "nearestTime" will
     * refer
     * 
     * @return A string representation of the current time
     */
    protected abstract String getCurrentTime();
}
