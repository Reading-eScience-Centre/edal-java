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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.Projection;
import org.gwtopenmaps.openlayers.client.control.EditingToolbar;
import org.gwtopenmaps.openlayers.client.control.LayerSwitcher;
import org.gwtopenmaps.openlayers.client.control.MousePosition;
import org.gwtopenmaps.openlayers.client.control.WMSGetFeatureInfo;
import org.gwtopenmaps.openlayers.client.control.WMSGetFeatureInfoOptions;
import org.gwtopenmaps.openlayers.client.event.EventHandler;
import org.gwtopenmaps.openlayers.client.event.EventObject;
import org.gwtopenmaps.openlayers.client.event.GetFeatureInfoListener;
import org.gwtopenmaps.openlayers.client.event.LayerLoadCancelListener;
import org.gwtopenmaps.openlayers.client.event.LayerLoadEndListener;
import org.gwtopenmaps.openlayers.client.event.LayerLoadStartListener;
import org.gwtopenmaps.openlayers.client.event.MapBaseLayerChangedListener;
import org.gwtopenmaps.openlayers.client.event.MapLayerChangedListener;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.LineString;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.layer.Image;
import org.gwtopenmaps.openlayers.client.layer.ImageOptions;
import org.gwtopenmaps.openlayers.client.layer.Layer;
import org.gwtopenmaps.openlayers.client.layer.TransitionEffect;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.layer.WMS;
import org.gwtopenmaps.openlayers.client.layer.WMSOptions;
import org.gwtopenmaps.openlayers.client.layer.WMSParams;
import org.gwtopenmaps.openlayers.client.util.JSObject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

import uk.ac.rdg.resc.godiva.client.handlers.GodivaActionsHandler;
import uk.ac.rdg.resc.godiva.client.handlers.OpacitySelectionHandler;
import uk.ac.rdg.resc.godiva.client.handlers.StartEndTimeHandler;
import uk.ac.rdg.resc.godiva.client.util.UnitConverter;
import uk.ac.rdg.resc.godiva.client.widgets.DialogBoxWithCloseButton.CentrePosIF;

/**
 * A widget containing the main OpenLayers map.
 * 
 * @author Guy Griffiths
 */
public class MapArea extends MapWidget implements OpacitySelectionHandler, CentrePosIF {

    private static final String NCWMS_LAYER_NAME = "ncWMS Layer";
    /*
     * We work in CRS:84 rather than EPSG:4326 so that lon-lat order is always
     * correct (regardless of WMS version)
     */
    protected static final Projection CRS84 = new Projection("CRS:84");
    protected static final NumberFormat FORMATTER = NumberFormat.getFormat("###.#####");

    /**
     * A class to store details for a "fixed" layer - i.e. a base map layer, or
     * a static overlay layer. These can be passed in to a new {@link MapArea}
     * to define custom map layers
     *
     * @author Guy Griffiths
     */
    public final static class FixedLayerDetails {
        public final String title;
        public final String wmsUrl;
        public final String layerNames;
        public final String projection;
        public final String imageFormat;
        public final String version;
        public final boolean isOn;
        public final boolean isBaseLayer;

        public FixedLayerDetails(String title, String wmsUrl, String layerNames, String projection,
                String imageFormat, String version, boolean isOn, boolean isBaseLayer) {
            if (title == null || wmsUrl == null || layerNames == null) {
                throw new IllegalArgumentException(
                        "Must provide a non-null title, WMS URL, and layer name");
            }
            this.title = title;
            this.wmsUrl = wmsUrl;
            this.layerNames = layerNames;
            if (projection != null) {
                this.projection = projection;
            } else {
                this.projection = "CRS:84";
            }
            if (imageFormat != null) {
                this.imageFormat = imageFormat;
            } else {
                this.imageFormat = "image/png";
            }
            if (version != null) {
                this.version = version;
            } else {
                this.version = "1.1.1";
            }
            this.isOn = isOn;
            this.isBaseLayer = isBaseLayer;
        }
    }

    /*
     * Class to store a WMS layer along with some other details. This means that
     * all details can be stored together in a java.util.Map
     */
    protected final class WmsDetails {
        public String wmsUrl;
        public final WMS wms;
        public final WMSParams params;
        public final boolean queryable;
        public final boolean downloadable;
        public final boolean multipleElevations;
        public final boolean multipleTimes;

        public WmsDetails(String wmsUrl, WMS wms, WMSParams wmsParameters, boolean queryable,
                boolean downloadable, boolean multipleElevations, boolean multipleTimes) {
            if (wms == null || wmsParameters == null || wmsUrl == null)
                throw new IllegalArgumentException("Cannot provide null parameters");
            this.wmsUrl = wmsUrl;
            this.wms = wms;
            this.params = wmsParameters;
            this.queryable = queryable;
            this.downloadable = downloadable;
            this.multipleElevations = multipleElevations;
            this.multipleTimes = multipleTimes;
        }
    }

    /*
     * The maximum number of features we can request in a GetFeatureInfo
     * request. This is public because classes which use MapArea may want to
     * change this. For example when dealing with older versions of ncWMS which
     * only handle a single feature at a time
     */
    public int maxFeatures = 5;

    protected Map map;
    protected java.util.Map<String, WmsDetails> wmsLayers;
    protected java.util.Map<String, FixedLayerDetails> overLayers;
    protected Image animLayer;
    protected String currentProjection;

    protected String transectLayer = null;

    protected WMSOptions wmsNorthPolarOptions;
    protected WMSOptions wmsSouthPolarOptions;
    protected WMSOptions wmsStandardOptions;
    protected WMSOptions wmsWebMercOptions;

    protected LayerLoadStartListener loadStartListener;
    protected LayerLoadCancelListener loadCancelListener;
    protected LayerLoadEndListener loadEndListener;

    protected GodivaActionsHandler widgetDisabler;

    protected String baseUrlForExport;
    protected String layersForExport;
    protected String overlaysForExport = null;

    protected WMSGetFeatureInfo getFeatureInfo;
    protected EditingToolbar editingToolbar;
    protected String proxyUrl;

    protected float opacity = 1.0f;

    /** Map of unit conversions to be applied to each layer */
    protected java.util.Map<String, UnitConverter> converters;

    /**
     * We keep track of this because animations can be turned off before they've
     * finished loading. This means the loading bar stays present.
     */
    protected boolean animLayerLoading = false;

    public MapArea(int width, int height, final GodivaActionsHandler godivaListener,
            String proxyUrl, FixedLayerDetails... additionalLayers) {
        super(width + "px", height + "px", getDefaultMapOptions());

        if (proxyUrl == null) {
            this.proxyUrl = "";
        } else {
            this.proxyUrl = proxyUrl;
        }

        wmsLayers = new LinkedHashMap<>();
        overLayers = new HashMap<>();
        converters = new HashMap<>();

        /*
         * Define some listeners to handle layer start/end loading events
         */
        loadStartListener = new LayerLoadStartListener() {
            @Override
            public void onLoadStart(LoadStartEvent eventObject) {
                godivaListener.setLoading(true);
            }
        };
        loadCancelListener = new LayerLoadCancelListener() {
            @Override
            public void onLoadCancel(LoadCancelEvent eventObject) {
                godivaListener.setLoading(false);
            }
        };
        loadEndListener = new LayerLoadEndListener() {
            @Override
            public void onLoadEnd(LoadEndEvent eventObject) {
                godivaListener.setLoading(false);
            }
        };
        this.widgetDisabler = godivaListener;
        init(additionalLayers);
        map.addMapMoveListener(godivaListener);
        map.addMapZoomListener(godivaListener);

        wmsStandardOptions = new WMSOptions();
        wmsStandardOptions.setWrapDateLine(true);
        wmsStandardOptions.setTransitionEffect(TransitionEffect.RESIZE);
    }

    /**
     * Adds an animation layer to the map
     * 
     * @param wmsUrl
     *            The WMS URL
     * @param layerId
     *            The WMS layer ID
     * @param timeList
     *            A comma separated list of times for the animation
     * @param currentElevation
     *            The elevation for the animation
     * @param palette
     *            The palette name
     * @param style
     *            The style name
     * @param aboveMaxString
     *            A string representing the colour for data above the max scale
     *            range
     * @param belowMinString
     *            A string representing the colour for data below min scale
     *            range
     * @param noDataString
     *            A string representing the colour when there is no data
     * @param scaleRange
     *            The scale range, of the form "[min],[max]"
     * @param nColorBands
     *            The number of colour bands to use
     * @param logScale
     *            Whether to use a logarithmic colour scale
     * @param frameRate
     *            The frame rate of the final animation
     */
    public void addAnimationLayer(String wmsUrl, String layerId, String timeList,
            String currentElevation, String palette, String style, String scaleRange,
            String aboveMaxString, String belowMinString, String noDataString, int nColorBands,
            boolean logScale, String frameRate) {
        StringBuilder url = new StringBuilder("service=WMS&request=GetMap&version=1.1.1");
        url.append("&format=image/gif" + "&transparent=true" + "&styles=" + style + "/" + palette
                + "&layers=" + layerId + "&time=" + timeList + "&logscale=" + logScale + "&srs="
                + currentProjection + "&bbox=" + map.getExtent().toBBox(6) + "&width="
                + ((int) map.getSize().getWidth()) + "&height=" + ((int) map.getSize().getHeight())
                + "&animation=true");
        if (scaleRange != null)
            url.append("&colorscalerange=" + scaleRange);
        if (currentElevation != null)
            url.append("&elevation=" + currentElevation.toString());
        if (nColorBands > 0)
            url.append("&numcolorbands=" + nColorBands);
        if (frameRate != null)
            url.append("&frameRate=" + frameRate);
        if (aboveMaxString != null) {
            url.append("&ABOVEMAXCOLOR=" + aboveMaxString);
        }
        if (belowMinString != null) {
            url.append("&BELOWMINCOLOR=" + belowMinString);
        }
        if (noDataString != null) {
            url.append("&BGCOLOR=" + noDataString);
        }
        ImageOptions opts = new ImageOptions();
        opts.setAlwaysInRange(true);
        /*
         * Because the animation is just an animated GIF, we use Image for our
         * OpenLayers layer
         */
        animLayer = new Image("Animation Layer", wmsUrl + "?" + url.toString(), map.getExtent(),
                map.getSize(), opts);
        animLayer.addLayerLoadStartListener(new LayerLoadStartListener() {
            @Override
            public void onLoadStart(LoadStartEvent eventObject) {
                animLayerLoading = true;
                loadStartListener.onLoadStart(eventObject);
            }
        });
        animLayer.addLayerLoadEndListener(new LayerLoadEndListener() {
            @Override
            public void onLoadEnd(LoadEndEvent eventObject) {
                /*
                 * Only fire the load end event if we are still WAITING for the
                 * animation layer (i.e. it hasn't been cancelled)
                 */
                if (animLayerLoading) {
                    animLayerLoading = false;
                    loadEndListener.onLoadEnd(eventObject);
                }
            }
        });
        animLayer.setIsBaseLayer(false);
        animLayer.setDisplayInLayerSwitcher(false);
        animLayer.setOpacity(opacity);

        /*
         * Out of all visible layers, we choose the most transparent and set the
         * animation layer transparency to that.
         */
        float opacity = 1.0f;
        for (WmsDetails wmsDetails : wmsLayers.values()) {
            float currentOpacity = wmsDetails.wms.getOpacity();
            wmsDetails.wms.setIsVisible(false);
            if (currentOpacity < opacity)
                opacity = currentOpacity;
        }
        animLayer.setOpacity(opacity);

        map.addLayer(animLayer);
        widgetDisabler.disableWidgets();
    }

    public void stopAnimation() {
        /*
         * This stops and removes the animation.
         * 
         * TODO We may want a pause method...
         */
        widgetDisabler.enableWidgets();
        if (animLayer != null) {
            /*
             * Because the animation is just an animated GIF, we use Image for
             * our OpenLayers layer
             */
            if (animLayerLoading) {
                /*
                 * The layer is still loading, but is no longer required. Cancel
                 * it.
                 */
                animLayerLoading = false;
                loadEndListener.onLoadEnd(null);
            }
            map.removeLayer(animLayer);
            animLayer = null;
        }
        for (WmsDetails wmsDetails : wmsLayers.values()) {
            wmsDetails.wms.setIsVisible(true);
        }
    }

    /**
     * Adds a WMS layer to the map
     * 
     * @param wmsUrl
     *            The WMS URL
     * @param internalLayerId
     *            An internal layer ID. This allows us to add multiple WMS
     *            layers to this map
     * @param wmsLayerName
     *            The WMS layer ID
     * @param time
     *            The time (or time range) at which we want data
     * @param targetTime
     *            If we have a continuous time axis, what time should we try and
     *            colour the data's value at
     * @param elevation
     *            The elevation (or elevation range) at which we want data
     * @param targetElevation
     *            If we have a continuous elevation axis, what elevation should
     *            we try and colour the data's value at
     * @param style
     *            The style name for this layer
     * @param palette
     *            The palette name for this layer
     * @param aboveMaxString
     *            The string defining the colour to display when values are
     *            above the max
     * @param belowMinString
     *            The string defining the colour to display when values are
     *            below the min
     * @param scaleRange
     *            The scale range (as a string: "[min],[max]")
     * @param nColourBands
     *            The number of colour bands in the palette
     * @param logScale
     *            Whether to use a logarithmic scale
     * @param multipleElevations
     *            Whether we have multiple elevations available
     * @param multipleTimes
     *            Whether we have multiple times available
     */
    public void addLayer(String wmsUrl, String internalLayerId, String wmsLayerName, String time,
            String targetTime, String elevation, String targetElevation, String style,
            String palette, String aboveMaxString, String belowMinString, String noDataString,
            String scaleRange, int nColourBands, boolean logScale, boolean queryable,
            boolean downloadable, boolean multipleElevations, boolean multipleTimes) {
        WMSParams params = new WMSParams();
        params.setFormat("image/png");
        params.setTransparent(true);
        params.setStyles(style + "/" + palette);
        params.setLayers(wmsLayerName);
        if (time != null) {
            params.setParameter("TIME", time);
        }
        if (targetTime != null) {
            params.setParameter("TARGETTIME", targetTime);
        }
        if (elevation != null) {
            params.setParameter("ELEVATION", elevation);
        }
        if (targetElevation != null) {
            params.setParameter("TARGETELEVATION", targetElevation);
        }
        if (scaleRange != null) {
            params.setParameter("COLORSCALERANGE", scaleRange);
        }
        if (nColourBands > 0) {
            params.setParameter("NUMCOLORBANDS", nColourBands + "");
        }
        if (aboveMaxString != null) {
            params.setParameter("ABOVEMAXCOLOR", aboveMaxString);
        }
        if (belowMinString != null) {
            params.setParameter("BELOWMINCOLOR", belowMinString);
        }
        if (noDataString != null) {
            params.setParameter("BGCOLOR", noDataString);
        }
        params.setParameter("LOGSCALE", logScale + "");

        WMSOptions options = getOptionsForCurrentProjection();

        doAddingOfLayer(wmsUrl, internalLayerId, params, options, queryable, downloadable,
                multipleElevations, multipleTimes);
    }

    /*
     * Does the work of actually adding the layer to the map
     */
    protected void doAddingOfLayer(String wmsUrl, String internalLayerId, WMSParams params,
            WMSOptions options, boolean queryable, boolean downloadable, boolean multipleElevations,
            boolean multipleTimes) {
        WmsDetails wmsAndParams = wmsLayers.get(internalLayerId);
        WMS wmsLayer;

        if (wmsAndParams != null) {
            /*
             * If we already have an existing layer, we remove it and re-add it.
             * 
             * New parameters can be merged, but this can cause problems if the
             * new layer doesn't require some parameters which the old layer
             * had, so this is simpler.
             */
            map.removeLayer(wmsLayers.get(internalLayerId).wms);
        }

        wmsLayer = new WMS(NCWMS_LAYER_NAME, wmsUrl, params, options);
        wmsLayer.addLayerLoadStartListener(loadStartListener);
        wmsLayer.addLayerLoadCancelListener(loadCancelListener);
        wmsLayer.addLayerLoadEndListener(loadEndListener);
        wmsLayer.setIsBaseLayer(false);
        wmsLayer.setOpacity(opacity);
        map.addLayer(wmsLayer);
        /*
         * Set the WMS layer to be first in the list of overlays.
         * 
         * User-defined overlays are intended to overlay (well, duh) the data
         */
        map.setLayerIndex(wmsLayer, 0);

        WmsDetails newWmsAndParams = new WmsDetails(wmsUrl, wmsLayer, params, queryable,
                downloadable, multipleElevations, multipleTimes);
        wmsLayers.put(internalLayerId, newWmsAndParams);

        /*
         * This is a little weird and should be unnecessary. However, it's not
         * unnecessary.
         * 
         * Take it out and try if you really want.
         */
        if (getFeatureInfo != null) {
            getFeatureInfo.deactivate();
            map.removeControl(getFeatureInfo);
            getFeatureInfo = null;
        }
        if (queryable) {
            setGetFeatureInfoDetails(wmsUrl, multipleElevations, multipleTimes, downloadable,
                    internalLayerId);
        }
        if (animLayer != null) {
            animLayer.setIsVisible(false);
        }
    }

    public void removeLayer(String layerId) {
        if (wmsLayers.containsKey(layerId)) {
            map.removeLayer(wmsLayers.get(layerId).wms);
            wmsLayers.remove(layerId);
        }
    }

    /*
     * Sets the GetFeatureInfo details and what to do when we receive GFI data
     */
    protected void setGetFeatureInfoDetails(final String wmsUrl, final boolean multipleElevations,
            final boolean multipleTimes, final boolean downloadable, final String layerId) {
        WMSGetFeatureInfoOptions getFeatureInfoOptions = new WMSGetFeatureInfoOptions();
        getFeatureInfoOptions.setQueryVisible(true);
        getFeatureInfoOptions.setInfoFormat("text/xml");
        getFeatureInfoOptions.setMaxFeaturess(maxFeatures);

        final WMS[] layers = new WMS[wmsLayers.size()];
        Iterator<WmsDetails> it = wmsLayers.values().iterator();
        int i = 0;
        while (it.hasNext()) {
            layers[i] = it.next().wms;
            i++;
        }
        getFeatureInfoOptions.setLayers(layers);

        JSObject vendorParams = JSObject.createJSObject();
        final String timeStr = wmsLayers.get(layerId).params.getJSObject()
                .getPropertyAsString("TIME");
        if (timeStr != null) {
            vendorParams.setProperty("TIME", timeStr);
        }
        final String targetTimeStr = wmsLayers.get(layerId).params.getJSObject()
                .getPropertyAsString("TARGETTIME");
        if (targetTimeStr != null) {
            vendorParams.setProperty("TARGETTIME", targetTimeStr);
        }
        final String elevationStr = wmsLayers.get(layerId).params.getJSObject()
                .getPropertyAsString("ELEVATION");
        if (elevationStr != null) {
            vendorParams.setProperty("ELEVATION", elevationStr);
        }
        final String targetElevationStr = wmsLayers.get(layerId).params.getJSObject()
                .getPropertyAsString("TARGETELEVATION");
        if (targetElevationStr != null) {
            vendorParams.setProperty("TARGETELEVATION", targetElevationStr);
        }

        getFeatureInfo = new WMSGetFeatureInfo(getFeatureInfoOptions);

        getFeatureInfo.addGetFeatureListener(new GetFeatureInfoListener() {
            @Override
            public void onGetFeatureInfo(GetFeatureInfoEvent eventObject) {
                String pixels[] = eventObject.getJSObject().getProperty("xy").toString().split(",");

                /*
                 * Parse as float and then cast to int. This is because if a
                 * browser-based zoom is applied, we can get floating point
                 * values for pixels due to rounding errors
                 */
                final int mapXClick = (int) Float.parseFloat(pixels[0].substring(2));
                final int mapYClick = (int) Float.parseFloat(pixels[1].substring(2));

                int x = mapXClick + MapArea.this.getAbsoluteLeft();
                int y = mapYClick + MapArea.this.getAbsoluteTop();

                FeatureInfoMessageAndFeatureIds featureInfo = null;
                final DialogBox pop = new DialogBoxWithCloseButton(MapArea.this);
                pop.setPopupPosition(x, y);
                try {
                    featureInfo = processFeatureInfo(eventObject.getText(),
                            converters.get(layerId));
                } catch (Exception e) {
                    GWT.log(e.getMessage());
                    /*
                     * Something is wrong with the GFI response. It may just be
                     * that there is no data here.
                     */
                    pop.setHTML("Feature Info Unavailable");
                    pop.add(new HTML("No Feature information is available at this location"));
                    pop.show();
                    return;
                }

                String message = featureInfo.message;

                pop.setHTML("Feature Info");

                VerticalPanel panel = new VerticalPanel();

                HTML html = new HTML("<div class=\"getFeatureInfo\">" + message + "</div>");
                panel.add(html);

                StringBuilder layerNames = new StringBuilder();

                /*
                 * The FeatureInfo has returned feature IDs to query
                 */
                for (String layerName : featureInfo.featureIds) {
                    layerNames.append(layerName + ",");
                }
                if (layerNames.length() > 0) {
                    // Remove the final comma
                    layerNames.deleteCharAt(layerNames.length() - 1);
                }

                final String layer = wmsLayers.get(layerId).wms.getParams().getLayers();

                if (multipleElevations && layerNames.length() > 0) {
                    /*
                     * If we have multiple depths, we can plot a vertical
                     * profile here
                     */
                    final String parameters = "REQUEST=GetVerticalProfile" + "&LAYERS=" + layer
                            + "&QUERY_LAYERS=" + layer + "&BBOX=" + map.getExtent().toBBox(4)
                            + "&SRS=" + currentProjection + "&FEATURE_COUNT=5" + "&HEIGHT="
                            + ((int) map.getSize().getHeight()) + "&WIDTH="
                            + ((int) map.getSize().getWidth()) + "&X=" + mapXClick + "&Y="
                            + mapYClick + "&STYLES=default/default"
                            + ((targetTimeStr != null)
                                    ? ("&TARGETTIME=" + URL.encodeQueryString(targetTimeStr))
                                    : "")
                            + ((timeStr != null) ? ("&TIME=" + URL.encodeQueryString(timeStr)) : "")
                            + "&VERSION=1.1.1";
                    Anchor profilePlot = new Anchor("Vertical Profile Plot");
                    profilePlot.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            displayImagePopup(
                                    proxyUrl + wmsUrl + "?" + parameters + "&INFO_FORMAT=image/png",
                                    "Vertical Profile");
                            pop.hide();
                        }
                    });
                    panel.add(profilePlot);

                    if (downloadable) {
                        Anchor profileDownload = new Anchor("Vertical Profile Download");
                        profileDownload.addClickHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                Window.open(
                                        proxyUrl + wmsUrl + "?" + parameters
                                                + "&INFO_FORMAT=text/csv",
                                        "Vertical Profile Data", null);
                                pop.hide();
                            }
                        });
                        panel.add(profileDownload);
                    }
                }

                if (multipleTimes && layerNames.length() > 0) {
                    /*
                     * If we have multiple times, we can plot a time series here
                     */
                    String eS = elevationStr;
                    if (targetElevationStr != null) {
                        eS = targetElevationStr;
                    }
                    final String parameters = "REQUEST=GetTimeseries" + "&LAYERS=" + layer
                            + "&QUERY_LAYERS=" + layer + "&BBOX=" + map.getExtent().toBBox(4)
                            + "&SRS=" + currentProjection + "&FEATURE_COUNT=5" + "&HEIGHT="
                            + ((int) map.getSize().getHeight()) + "&WIDTH="
                            + ((int) map.getSize().getWidth()) + "&X=" + mapXClick + "&Y="
                            + mapYClick + "&STYLES=default/default"
                            + ((eS != null) ? ("&ELEVATION=" + eS) : "") + "&VERSION=1.1.1";
                    Anchor timeseriesPlot = new Anchor("Time Series Plot");
                    timeseriesPlot.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            String wmsLayer = wmsLayers.get(layerId).wms.getParams().getLayers()
                                    .split(",")[0];
                            final StartEndTimePopup timeSelector = new StartEndTimePopup(wmsLayer,
                                    proxyUrl + wmsUrl, null, MapArea.this, -1);
                            timeSelector.setButtonLabel("Plot");
                            timeSelector.setErrorMessage(
                                    "You can only plot a time series when you have multiple times available");
                            timeSelector.setHTML("Select range for time series");
                            timeSelector.setTimeSelectionHandler(new StartEndTimeHandler() {
                                @Override
                                public void timesReceived(String startDateTime,
                                        String endDateTime) {
                                    displayImagePopup(proxyUrl + wmsUrl + "?" + parameters
                                            + "&TIME=" + startDateTime + "/" + endDateTime
                                            + "&INFO_FORMAT=image/png", "Time series");
                                    timeSelector.hide();
                                }
                            });
                            pop.hide();
                            timeSelector.center();
                        }
                    });
                    panel.add(timeseriesPlot);

                    if (downloadable) {
                        /*
                         * If we have multiple times, we can download a time
                         * series here
                         */
                        Anchor timeseriesDownload = new Anchor("Time Series Download");
                        timeseriesDownload.addClickHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                String wmsLayer = wmsLayers.get(layerId).wms.getParams().getLayers()
                                        .split(",")[0];
                                final StartEndTimePopup timeSelector = new StartEndTimePopup(
                                        wmsLayer, proxyUrl + wmsUrl, null, MapArea.this, -1);
                                timeSelector.setButtonLabel("Download");
                                timeSelector.setErrorMessage(
                                        "You can only download a time series when you have multiple times available");
                                timeSelector.setHTML("Select range for time series");
                                timeSelector.setTimeSelectionHandler(new StartEndTimeHandler() {
                                    @Override
                                    public void timesReceived(String startDateTime,
                                            String endDateTime) {
                                        Window.open(
                                                proxyUrl + wmsUrl + "?" + parameters + "&TIME="
                                                        + startDateTime + "/" + endDateTime
                                                        + "&INFO_FORMAT=text/csv",
                                                "Time series data", null);
                                        timeSelector.hide();
                                    }
                                });
                                pop.hide();
                                timeSelector.center();
                            }
                        });
                        panel.add(timeseriesDownload);
                    }
                }
                pop.add(panel);
                pop.setPopupPosition(x, y);
                pop.setAutoHideEnabled(true);
                pop.show();
            }
        });
        getFeatureInfo.setAutoActivate(true);
        map.addControl(getFeatureInfo);

        getFeatureInfo.getJSObject().setProperty("vendorParams", vendorParams);
    }

    /**
     * Used to display an image in a new window. This is used for showing
     * vertical profiles or time series plots
     * 
     * @param url
     *            The absolute URL of the image
     * @param title
     *            The title of the popup box
     */
    protected void displayImagePopup(String url, String title) {
        /*
         * Window.open treats relative URLs in a browser-dependent manner. So we
         * have to use absolute ones here.
         */
        Window.open(url, title, null);

        /*
         * This is how we can display the image in a popup box.
         */
        // final DialogBoxWithCloseButton popup = new
        // DialogBoxWithCloseButton(this);
        // final com.google.gwt.user.client.ui.Image image = new
        // com.google.gwt.user.client.ui.Image(url);
        // image.addLoadHandler(new LoadHandler() {
        // @Override
        // public void onLoad(LoadEvent event) {
        // popup.center();
        // }
        // });
        // /*
        // * NOTTODO this doesn't seem to appear on Chromium...
        // */
        // image.setAltText("Image loading...");
        // if(title != null){
        // popup.setHTML(title);
        // }
        // popup.add(image);
        // popup.center();
    }

    public void zoomToExtent(String extents) {
        if (currentProjection.equalsIgnoreCase("EPSG:32661")
                || currentProjection.equalsIgnoreCase("EPSG:32761")
                || currentProjection.equalsIgnoreCase("EPSG:5041")
                || currentProjection.equalsIgnoreCase("EPSG:5042")
                || currentProjection.equalsIgnoreCase("EPSG:3857")
                || currentProjection.equalsIgnoreCase("EPSG:900913")) {
            /*
             * If we have a polar projection, the extents will be wrong. In this
             * case, just ignore the zoom to extents.
             * 
             * This is acceptable behaviour, since if we are looking at polar
             * data, we never want to zoom to the extents of the data
             */
            return;
        }
        try {
            String[] bboxStr = extents.split(",");
            double lowerLeftX = Double.parseDouble(bboxStr[0]);
            double lowerLeftY = Double.parseDouble(bboxStr[1]);
            double upperRightX = Double.parseDouble(bboxStr[2]);
            double upperRightY = Double.parseDouble(bboxStr[3]);
            Bounds bounds = new Bounds(lowerLeftX, lowerLeftY, upperRightX, upperRightY);
            /*
             * For some reason, the first time we do this with the bounds
             * -180,-90,180,90 it actually zooms to
             * -165.46,-129.26,165.46,129.26.
             * 
             * The second time it zooms out properly. This is using
             * OpenLayers-2.11 and gwt-openlayers 1.0. If those versions have
             * changed and you're reading this, try removing one of the calls
             * and seeing if it works. Otherwise keep them both in.
             */
            map.zoomToExtent(bounds);
            map.zoomToExtent(bounds);
        } catch (Exception e) {
            /*
             * Ignore this - it most likely means that badly formatted bounds
             * were specified, but it's preferable to ignore it rather than
             * giving the user a message
             */
        }
    }

    /**
     * @return The URL used to get a KMZ of the currently displayed layer
     */
    public String getKMZUrl() {
        String url;
        WmsDetails wmsAndParams = wmsLayers.get(getTransectLayerId());
        if (wmsAndParams != null) {
            if (animLayer != null) {
                url = animLayer.getUrl();
                url = url.replaceAll("image/gif", "application/vnd.google-earth.kmz");
                url = url.replaceAll("image%2Fgif", "application%2Fvnd.google-earth.kmz");
            } else {
                url = wmsAndParams.wms.getFullRequestString(wmsAndParams.params, null);
                url = url + "&height=1024&width=1024";
                if (currentProjection.equals("EPSG:5041")) {
                    /*
                     * North polar stereographic.
                     * 
                     * We can't just do a transform on the Bounds object - it
                     * seems to just to a very naive transform which doesn't
                     * work for polar stereographic.
                     */
                    Bounds extent = map.getExtent();
                    Point lowerLeft = new Point(extent.getLowerLeftX(), extent.getLowerLeftY());
                    Point upperRight = new Point(extent.getUpperRightX(), extent.getUpperRightY());

                    lowerLeft.transform(new Projection("EPSG:5041"), new Projection("CRS:84"));
                    upperRight.transform(new Projection("EPSG:5041"), new Projection("CRS:84"));

                    double lowLat = lowerLeft.getY() < upperRight.getY() ? lowerLeft.getY()
                            : upperRight.getY();

                    url = url + "&bbox=-180," + lowLat + ",180,90";

                    url = url.replaceAll("EPSG:5041", "CRS:84");
                    url = url.replaceAll("EPSG%3A5041", "CRS%3A84");
                } else if (currentProjection.equals("EPSG:5042")) {
                    /*
                     * South polar stereographic.
                     * 
                     * We can't just do a transform on the Bounds object - it
                     * seems to just to a very naive transform which doesn't
                     * work for polar stereographic.
                     */
                    Bounds extent = map.getExtent();
                    Point lowerLeft = new Point(extent.getLowerLeftX(), extent.getLowerLeftY());
                    Point upperRight = new Point(extent.getUpperRightX(), extent.getUpperRightY());

                    lowerLeft.transform(new Projection("EPSG:5042"), new Projection("CRS:84"));
                    upperRight.transform(new Projection("EPSG:5042"), new Projection("CRS:84"));

                    double highLat = lowerLeft.getY() > upperRight.getY() ? lowerLeft.getY()
                            : upperRight.getY();

                    url = url + "&bbox=-180,-90,180," + highLat;

                    url = url.replaceAll("EPSG:5042", "CRS:84");
                    url = url.replaceAll("EPSG%3A5042", "CRS%3A84");
                } else {
                    Bounds extent = map.getExtent();
                    double yMin = extent.getLowerLeftY();
                    if (yMin < -90) {
                        yMin = -90;
                    }
                    double yMax = extent.getUpperRightY();
                    if (yMax > 90) {
                        yMax = 90;
                    }

                    url = url + "&bbox=" + extent.getLowerLeftX() + "," + yMin + ","
                            + extent.getUpperRightX() + "," + yMax;
                    url = url.replaceAll("EPSG:4326", "CRS:84");
                    url = url.replaceAll("EPSG%3A4326", "CRS%3A84");
                    /*
                     * Since map.getExtent().toBBox() always returns x-first, we
                     * must make sure that we're not using the EPSG:4326
                     * projection, which is y-first in 1.3.0 and x-first in
                     * 1.1.1
                     * 
                     * Replace it with CRS:84 since this is equivalent and
                     * always x-first
                     */
                }
                url = url.replaceAll("image/png", "application/vnd.google-earth.kmz");
                url = url.replaceAll("image%2Fpng", "application%2Fvnd.google-earth.kmz");
            }
        } else {
            url = null;
        }
        return url;
    }

    protected static MapOptions getDefaultMapOptions() {
        MapOptions mapOptions = new MapOptions();
        mapOptions.setProjection("CRS:84");
        mapOptions.setDisplayProjection(CRS84);
        JSObject vendorParams = JSObject.createJSObject();
        vendorParams.setProperty("theme", GWT.getModuleBaseURL() + "theme/default/style.css");
        mapOptions.setJSObject(vendorParams);
        return mapOptions;
    }

    protected void init(FixedLayerDetails[] additionalLayers) {
        this.setStylePrimaryName("mapStyle");
        map = this.getMap();
        addBaseLayers(additionalLayers);

        currentProjection = map.getProjection();
        map.addControl(new LayerSwitcher());
        map.addControl(new MousePosition());
        addDrawingLayer();
        map.setMaxExtent(new Bounds(-180, -90, 180, 90));
        map.setCenter(new LonLat(0.0, 0.0), 2);
        map.setFractionalZoom(true);
    }

    public void setOpacity(String layerId, float opacity) {
        this.opacity = opacity;
        for (WmsDetails wmsDetails : wmsLayers.values()) {
            String layersStr = wmsDetails.params.getLayers();
            String[] layers = layersStr.split(",");
            for (String layer : layers) {
                if (layer.equalsIgnoreCase(layerId)) {
                    wmsDetails.wms.setOpacity(opacity);
                }
            }
        }
        if (animLayer != null) {
            animLayer.setOpacity(opacity);
        }
    }

    /**
     * Sets a {@link UnitConverter} for a specified layer. This will allow
     * GetFeatureInfo requests to display the correct value
     * 
     * @param layerId
     *            The internal layer to apply the conversion to
     * @param converter
     *            The {@link UnitConverter} to use
     */
    public void setUnitConverter(String layerId, UnitConverter converter) {
        converters.put(layerId, converter);
    }

    public String getBaseLayerUrl() {
        return baseUrlForExport;
    }

    public String getBaseLayerLayers() {
        return layersForExport;
    }

    public String getOverlaysForExport() {
        return overlaysForExport;
    }

    public String getBackgroundMapName() {
        return map.getBaseLayer().getName();
    }

    public void setBackgroundMap(String layerName) {
        /*
         * map.getLayerByName() doesn't appear to actually work...
         */
        Layer layer = null;
        for (Layer l : map.getLayers()) {
            if (l.getName().equals(layerName)) {
                layer = l;
                break;
            }
        }
        map.setBaseLayer(layer);
        baseLayerChanged(layer);
    }

    protected void addBaseLayers(FixedLayerDetails[] additionalLayers) {
        /*
         * Adds the base layers to the map.
         * 
         * Note that this section has some unnecessary code duplication.
         * Specifically, we redefine WMSOptions and WMSParams each time we use
         * them, when in reality, the same WMSOptions object can be used for
         * many of the layers.
         * 
         * IT'S FOR CLARITY AND A HASSLE-FREE FUTURE, PLEASE DON'T SCREW AROUND
         * WITH IT.
         */

        WMSParams wmsParams;
        WMSOptions wmsOptions;

        /*
         * Bounds and options for Web Mercator. Not actually used, but we make
         * them available for custom layers
         */
        Bounds webMercExtent = new Bounds(-20037508.34, -20037508.34, 20037508.34, 20037508.34);
        float webMercMaxResolution = (float) ((webMercExtent.getUpperRightX()
                - webMercExtent.getLowerLeftX()) / 512.0);
        wmsWebMercOptions = new WMSOptions();
        wmsWebMercOptions.setProjection("EPSG:3857");
        wmsWebMercOptions.setWrapDateLine(true);
        wmsWebMercOptions.setTransitionEffect(TransitionEffect.RESIZE);
        wmsWebMercOptions.setMaxExtent(webMercExtent);
        wmsWebMercOptions.setMaxResolution(webMercMaxResolution);

        /*
         * NASA Basemap
         * 
         * This doesn't currently work - if you request a tile which spans the
         * dateline, it fetches the wrong area. It's probably a bug in their
         * code. I've told them about it, so this may be useful if/when they fix
         * it.
         */
        WMS nasaBlueMarble;
        wmsOptions = new WMSOptions();
        wmsOptions.setProjection("CRS:84");
        wmsOptions.setWrapDateLine(true);
        wmsOptions.setTransitionEffect(TransitionEffect.MAP_RESIZE);
        wmsParams = new WMSParams();
        wmsParams.setLayers("BlueMarbleNG-TB");
        wmsParams.setFormat("image/png");
        wmsParams.setParameter("version", "1.3.0");

        String nasaMapServerUrl = "http://neowms.sci.gsfc.nasa.gov/wms/wms?";
        nasaBlueMarble = new WMS("NASA Blue Marble WMS", nasaMapServerUrl, wmsParams, wmsOptions);
        nasaBlueMarble.addLayerLoadStartListener(loadStartListener);
        nasaBlueMarble.addLayerLoadEndListener(loadEndListener);
        nasaBlueMarble.setIsBaseLayer(true);

        /*
         * Basemaps hosted at ReSC
         */

        /*
         * ReSC naturalearth
         */
        String rescMapServerUrl = "http://godiva.reading.ac.uk/geoserver/ReSC/wms?";
        wmsOptions = new WMSOptions();
        wmsOptions.setProjection("CRS:84");
        wmsOptions.setWrapDateLine(true);
        wmsOptions.setTransitionEffect(TransitionEffect.MAP_RESIZE);
        wmsParams = new WMSParams();
        wmsParams.setLayers("naturalearth");
        wmsParams.setFormat("image/png");
        wmsParams.setParameter("version", "1.3.0");
        WMS naturalEarth = new WMS("NaturalEarth WMS", rescMapServerUrl, wmsParams, wmsOptions);
        naturalEarth.addLayerLoadStartListener(loadStartListener);
        naturalEarth.addLayerLoadEndListener(loadEndListener);
        naturalEarth.setIsBaseLayer(true);

        /*
         * ReSC Bluemarble
         */
        wmsOptions = new WMSOptions();
        wmsOptions.setProjection("CRS:84");
        wmsOptions.setWrapDateLine(true);
        wmsOptions.setTransitionEffect(TransitionEffect.MAP_RESIZE);
        wmsParams = new WMSParams();
        wmsParams.setLayers("bluemarble");
        wmsParams.setFormat("image/png");
        WMS blueMarble = new WMS("BlueMarble WMS", rescMapServerUrl, wmsParams, wmsOptions);
        blueMarble.addLayerLoadStartListener(loadStartListener);
        blueMarble.addLayerLoadEndListener(loadEndListener);
        blueMarble.setIsBaseLayer(true);

        /*
         * These are the bounds of ALL OF the polar layers (i.e. North AND
         * South) on the ReSC server
         */
        Bounds polarMaxExtent = new Bounds(-4000000, -4000000, 8000000, 8000000);
        float polarMaxResolution = (float) ((polarMaxExtent.getUpperRightX()
                - polarMaxExtent.getLowerLeftX()) / 512.0);

        /*
         * ReSC Natural Earth North Pole
         */
        wmsNorthPolarOptions = new WMSOptions();
        wmsNorthPolarOptions.setProjection("EPSG:5041");
        wmsNorthPolarOptions.setMaxExtent(polarMaxExtent);
        wmsNorthPolarOptions.setMaxResolution(polarMaxResolution);
        wmsNorthPolarOptions.setTransitionEffect(TransitionEffect.RESIZE);
        wmsNorthPolarOptions.setWrapDateLine(false);

        wmsParams = new WMSParams();
        wmsParams.setLayers("naturalearth-np");
        wmsParams.setFormat("image/png");
        wmsParams.setParameter("version", "1.3.0");
        WMS naturalEarthNP = new WMS("North polar stereographic (NaturalEarth)", rescMapServerUrl,
                wmsParams, wmsNorthPolarOptions);
        naturalEarthNP.setIsBaseLayer(true);

        /*
         * ReSC Blue Marble North Pole
         */
        wmsParams = new WMSParams();
        wmsParams.setLayers("bluemarble-np");
        wmsParams.setFormat("image/png");
        wmsParams.setParameter("version", "1.3.0");
        WMS blueMarbleNP = new WMS("North polar stereographic (BlueMarble)", rescMapServerUrl,
                wmsParams, wmsNorthPolarOptions);
        blueMarbleNP.setIsBaseLayer(true);
        blueMarbleNP.setSingleTile(true);

        /*
         * ReSC Natural Earth South Pole
         */
        wmsSouthPolarOptions = new WMSOptions();
        wmsSouthPolarOptions.setProjection("EPSG:5042");
        wmsSouthPolarOptions.setMaxExtent(polarMaxExtent);
        wmsSouthPolarOptions.setMaxResolution(polarMaxResolution);
        wmsSouthPolarOptions.setTransitionEffect(TransitionEffect.RESIZE);
        wmsSouthPolarOptions.setWrapDateLine(false);

        wmsParams = new WMSParams();
        wmsParams.setLayers("naturalearth-sp");
        wmsParams.setFormat("image/png");
        wmsParams.setParameter("version", "1.3.0");
        WMS naturalEarthSP = new WMS("South polar stereographic (NaturalEarth)", rescMapServerUrl,
                wmsParams, wmsSouthPolarOptions);
        naturalEarthSP.setIsBaseLayer(true);

        /*
         * ReSC Blue Marble South Pole
         */
        wmsParams = new WMSParams();
        wmsParams.setLayers("bluemarble-sp");
        wmsParams.setFormat("image/png");
        wmsParams.setParameter("version", "1.3.0");
        WMS blueMarbleSP = new WMS("South polar stereographic (BlueMarble)", rescMapServerUrl,
                wmsParams, wmsSouthPolarOptions);
        blueMarbleSP.setIsBaseLayer(true);

        /*
         * Basemap from demis.nl
         */
        wmsOptions = new WMSOptions();
        wmsOptions.setProjection("EPSG:4326");
        wmsOptions.setWrapDateLine(true);
        wmsOptions.setTransitionEffect(TransitionEffect.MAP_RESIZE);
        wmsParams = new WMSParams();
        wmsParams.setLayers("Countries,Bathymetry,Topography,Hillshading,Coastlines,Builtup+areas,"
                + "Waterbodies,Rivers,Streams,Railroads,Highways,Roads,Trails,Borders,Cities,Airports");
        wmsParams.setFormat("image/png");
        WMS demis = new WMS("Demis WMS", "http://www2.demis.nl/wms/wms.ashx?WMS=WorldMap",
                wmsParams, wmsOptions);
        demis.setIsBaseLayer(true);
        demis.addLayerLoadStartListener(loadStartListener);
        demis.addLayerLoadEndListener(loadEndListener);

        /*
         * Basemaps from GEBCO. Note that these need a special copyright message
         */
        wmsOptions = new WMSOptions();
        wmsOptions.setProjection("EPSG:4326");
        wmsOptions.setWrapDateLine(true);
        wmsOptions.setTransitionEffect(TransitionEffect.MAP_RESIZE);
        wmsParams = new WMSParams();
        wmsParams.setLayers("GEBCO_LATEST");
        wmsParams.setFormat("image/png");
        WMS gebco = new WMS(
                "GEBCO WMS - Imagery reproduced from the GEBCO_2014 Grid, version 20150318, www.gebco.net",
                "http://www.gebco.net/data_and_products/gebco_web_services/web_map_service/mapserv?",
                wmsParams, wmsOptions);
        gebco.setIsBaseLayer(true);
        gebco.addLayerLoadStartListener(loadStartListener);
        gebco.addLayerLoadEndListener(loadEndListener);

        /*
         * Add all of the layers in the order we want them displayed
         */
        map.addLayer(naturalEarth);
        map.addLayer(blueMarble);
        map.addLayer(nasaBlueMarble);
        map.addLayer(demis);
        map.addLayer(gebco);
        map.addLayer(naturalEarthNP);
        map.addLayer(blueMarbleNP);
        map.addLayer(naturalEarthSP);
        map.addLayer(blueMarbleSP);

        /*
         * Now global setup stuff. Store the current projection, add the layer
         * change listener and set the default layer.
         */
        currentProjection = map.getProjection();

        /*
         * Technically we should be able to leave these vars, add the base layer
         * change listener, and *then* do map.setBaseLayer() to trigger
         * baseLayerChanged which will set the vars.
         * 
         * But it doesn't work, probably due to something weird. Meh, this works
         * too.
         */
        baseUrlForExport = rescMapServerUrl;
        layersForExport = "naturalearth";
        map.setBaseLayer(naturalEarth);

        map.addMapBaseLayerChangedListener(new MapBaseLayerChangedListener() {
            @Override
            public void onBaseLayerChanged(MapBaseLayerChangedEvent eventObject) {
                baseLayerChanged(eventObject.getLayer());
            }
        });

        map.addMapLayerChangedListener(new MapLayerChangedListener() {
            /*
             * This is to change the screenshot servlet link when an overlay is
             * switched on or off
             */
            @Override
            public void onLayerChanged(MapLayerChangedEvent eventObject) {
                /*
                 * First check that we're dealing with an overlay, otherwise do
                 * nothing
                 */
                if (overLayers.containsKey(eventObject.getLayer().getId())) {
                    /*
                     * Now construct the overlaysForExport String from all
                     * visible overlays
                     */
                    Layer[] layers = map.getLayers();
                    StringBuilder sb = new StringBuilder();
                    for (Layer layer : layers) {
                        if (!layer.isBaseLayer() && layer.isVisible()) {
                            FixedLayerDetails wmsDetails = overLayers.get(layer.getId());
                            if (wmsDetails != null) {
                                sb.append(wmsDetails.wmsUrl + "LAYERS=" + wmsDetails.layerNames
                                        + ",");
                            }
                        }
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    /*
                     * If after all this, the string has changed (which it
                     * should have done?), reassign it and trigger the link
                     * update.
                     */
                    String newOverlaysExportStr = sb.toString();
                    if (!newOverlaysExportStr.equals(overlaysForExport)) {
                        overlaysForExport = newOverlaysExportStr;
                        widgetDisabler.updateLinksEtc();
                    }
                }
            }
        });

        /*
         * Now add any additional user-defined layers
         */
        for (FixedLayerDetails layer : additionalLayers) {
            try {
                wmsOptions = new WMSOptions();
                wmsOptions.setProjection(layer.projection);
                wmsOptions.setWrapDateLine(true);
                wmsOptions.setTransitionEffect(TransitionEffect.MAP_RESIZE);
                wmsOptions.setIsBaseLayer(layer.isBaseLayer);
                if (layer.projection.equalsIgnoreCase("EPSG:3857")
                        || layer.projection.equalsIgnoreCase("EPSG:900913")) {
                    wmsOptions.setMaxExtent(webMercExtent);
                    wmsOptions.setMaxResolution(webMercMaxResolution);
                }
                wmsParams = new WMSParams();
                wmsParams.setLayers(layer.layerNames);
                wmsParams.setFormat(layer.imageFormat);
                wmsParams.setTransparent(true);
                wmsParams.setParameter("version", layer.version);
                WMS userLayer = new WMS(layer.title, layer.wmsUrl, wmsParams, wmsOptions);
                overLayers.put(userLayer.getId(), layer);
                userLayer.setIsVisible(false);
                map.addLayer(userLayer);
                if (!layer.isBaseLayer && layer.isOn) {
                    userLayer.setIsVisible(true);
                } else {
                }
                if (layer.isBaseLayer && layer.isOn) {
                    map.setBaseLayer(userLayer);
                }
            } catch (Exception e) {
                GWT.log("Problem adding custom map layer.  Ignoring this layer: " + layer.title, e);
            }
        }
    }

    private void baseLayerChanged(Layer layer) {
        GWT.log("Base layer changed");
        String url = layer.getJSObject().getPropertyAsString("url");
        String layers = layer.getJSObject().getPropertyAsArray("params")[0]
                .getPropertyAsString("LAYERS");
        baseUrlForExport = url + (url.contains("?") ? "&" : "?");
        layersForExport = layers;
        if (!map.getProjection().equals(currentProjection)) {
            currentProjection = map.getProjection();
            for (String internalLayerId : wmsLayers.keySet()) {
                WmsDetails wmsAndParams = wmsLayers.get(internalLayerId);
                if (wmsAndParams != null) {
                    removeLayer(internalLayerId);
                    doAddingOfLayer(wmsAndParams.wmsUrl, internalLayerId, wmsAndParams.params,
                            getOptionsForCurrentProjection(), wmsAndParams.queryable,
                            wmsAndParams.downloadable, wmsAndParams.multipleElevations,
                            wmsAndParams.multipleTimes);
                }
            }
            map.zoomToMaxExtent();
        }
        widgetDisabler.updateLinksEtc();
    }

    protected WMSOptions getOptionsForCurrentProjection() {
        if (currentProjection.equalsIgnoreCase("EPSG:32661")
                || currentProjection.equalsIgnoreCase("EPSG:5041")) {
            return wmsNorthPolarOptions;
        } else if (currentProjection.equalsIgnoreCase("EPSG:32761")
                || currentProjection.equalsIgnoreCase("EPSG:5042")) {
            return wmsSouthPolarOptions;
        } else if (currentProjection.equalsIgnoreCase("EPSG:3857")
                || currentProjection.equalsIgnoreCase("EPSG:900913")) {
            return wmsWebMercOptions;
        } else {
            return wmsStandardOptions;
        }
    }

    protected class FeatureInfoMessageAndFeatureIds {
        public String message;
        public String[] featureIds;

        public FeatureInfoMessageAndFeatureIds(String message, String[] featureIds) {
            super();
            this.message = message;
            this.featureIds = featureIds;
        }
    }

    protected FeatureInfoMessageAndFeatureIds processFeatureInfo(String text,
            UnitConverter converter) {
        Document featureInfo = XMLParser.parse(text);
        double lon = Double.parseDouble(featureInfo.getElementsByTagName("longitude").item(0)
                .getChildNodes().item(0).getNodeValue());
        double lat = Double.parseDouble(featureInfo.getElementsByTagName("latitude").item(0)
                .getChildNodes().item(0).getNodeValue());

        StringBuffer html = new StringBuffer("<table>");
        html.append("<tr><td><b>Clicked:</b></td></tr>");
        html.append("<tr><td><b>Longitude:</b></td><td>" + FORMATTER.format(lon) + "</td></tr>");
        html.append("<tr><td><b>Latitude:</b></td><td>" + FORMATTER.format(lat) + "</td></tr>");
        html.append("<tr><td>&nbsp;</td></tr>");

        NodeList feature = featureInfo.getElementsByTagName("Feature");
        int length = feature.getLength();
        String[] layerNames = new String[length];
        for (int i = 0; i < length; i++) {
            /*
             * For each feature...
             */
            Node item = feature.item(i);
            NodeList childNodes = item.getChildNodes();

            String layerName = null;
            Double actualX = null;
            Double actualY = null;
            NodeList featureInfoNode = null;
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node child = childNodes.item(j);
                if (child.getNodeName().equalsIgnoreCase("layer")) {
                    layerName = child.getFirstChild().getNodeValue();
                } else if (child.getNodeName().equalsIgnoreCase("actualX")) {
                    actualX = Double.parseDouble(child.getFirstChild().getNodeValue());
                } else if (child.getNodeName().equalsIgnoreCase("actualY")) {
                    actualY = Double.parseDouble(child.getFirstChild().getNodeValue());
                } else if (child.getNodeName().equalsIgnoreCase("FeatureInfo")) {
                    featureInfoNode = child.getChildNodes();
                }
            }

            if (layerName != null) {
                layerNames[i] = layerName;
                html.append("<tr><td><b>Layer:</b></td><td>" + layerName);
            }
            if (actualX != null && actualY != null)
                html.append(
                        " (" + FORMATTER.format(actualX) + "," + FORMATTER.format(actualY) + ")");
            html.append("</td></tr>");
            if (featureInfoNode != null) {
                String id = null;
                String time = null;
                String valueStr = null;
                for (int j = 0; j < featureInfoNode.getLength(); j++) {
                    Node child = featureInfoNode.item(j);
                    if (child.getNodeName().equalsIgnoreCase("time")) {
                        Node firstChild = child.getFirstChild();
                        if (firstChild != null) {
                            time = firstChild.getNodeValue();
                        }
                    } else if (child.getNodeName().equalsIgnoreCase("value")) {
                        valueStr = child.getFirstChild().getNodeValue();
                        /*
                         * This is probably a number. Parse it as if it is, but
                         * ignore any errors
                         */
                        try {
                            float value = Float.parseFloat(valueStr);
                            if (converter != null) {
                                value = converter.convertToDisplayUnit(value);
                            }
                            valueStr = FORMATTER.format(value);
                        } catch (Exception e) {
                            /*
                             * Ignore, we'll just use the string value as-is.
                             */
                        }
                    } else if (child.getNodeName().equalsIgnoreCase("id")) {
                        id = child.getFirstChild().getNodeValue();
                    }
                }
                if (valueStr != null) {
                    if (id != null) {
                        html.append("<tr><td><b>Feature:</b></td><td>" + id);
                    }
                    if (time != null) {
                        html.append("<tr><td><b>Time:</b></td><td>" + time + "</td></tr>");
                    }
                    valueStr = valueStr.replaceAll(";", "<br/>");
                    html.append("<tr><td><b>Value:</b></td><td>" + valueStr + "</td></tr>");
                }
                /*
                 * Now process any arbitrary properties
                 */
                for (int j = 0; j < featureInfoNode.getLength(); j++) {
                    Node child = featureInfoNode.item(j);
                    if (child.getNodeName().equalsIgnoreCase("property")) {
                        String propertyName = child.getAttributes().getNamedItem("name")
                                .getNodeValue();
                        String propertyValue = child.getFirstChild().getNodeValue();
                        html.append("<tr><td><b>" + propertyName + ":</b></td><td>" + propertyValue
                                + "</td></tr>");
                    }
                }
            }
            html.append("<tr><td>&nbsp;</td></tr>");
        }
        html.append("</table>");
        return new FeatureInfoMessageAndFeatureIds(html.toString(), layerNames);
    }

    protected void addDrawingLayer() {
        Vector drawingLayer = new Vector("Drawing");
        drawingLayer.getEvents().register("featureadded", drawingLayer, new EventHandler() {
            @Override
            public void onHandle(EventObject eventObject) {
                WmsDetails wmsAndParams = wmsLayers.get(getTransectLayerId());
                if (wmsAndParams != null) {
                    WMS wmsLayer = wmsAndParams.wms;
                    JSObject featureJs = eventObject.getJSObject().getProperty("feature");
                    JSObject lineStringJs = VectorFeature.narrowToVectorFeature(featureJs)
                            .getGeometry().getJSObject();
                    LineString line = LineString.narrowToLineString(lineStringJs);
                    Point[] points = line.getComponents();
                    StringBuilder lineStringBuilder = new StringBuilder();
                    for (int i = 0; i < points.length - 1; i++) {
                        lineStringBuilder.append(points[i].getX() + " " + points[i].getY() + ", ");
                    }
                    lineStringBuilder.append(points[points.length - 1].getX() + " "
                            + points[points.length - 1].getY());
                    String projection = currentProjection;
                    if ("EPSG:4326".equals(projection)) {
                        /*
                         * We are using x,y order for the co-ords, so ensure
                         * that the projection will match this
                         */
                        projection = "CRS:84";
                    }
                    String transectParams = "REQUEST=GetTransect" + "&LAYERS="
                            + wmsLayer.getParams().getLayers() + "&CRS=" + projection
                            + "&LINESTRING=" + lineStringBuilder + "&FORMAT=image/png";

                    transectParams = addParameterValue(wmsLayer, transectParams, "ELEVATION");
                    transectParams = addParameterValue(wmsLayer, transectParams, "TARGETELEVATION");
                    transectParams = addParameterValue(wmsLayer, transectParams, "TIME");
                    transectParams = addParameterValue(wmsLayer, transectParams, "TARGETTIME");
                    transectParams = addParameterValue(wmsLayer, transectParams, "COLORSCALERANGE");
                    transectParams = addParameterValue(wmsLayer, transectParams, "NUMCOLORBANDS");
                    transectParams = addParameterValue(wmsLayer, transectParams, "LOGSCALE");
                    transectParams = addParameterValue(wmsLayer, transectParams, "ABOVEMAXCOLOR");
                    transectParams = addParameterValue(wmsLayer, transectParams, "BELOWMINCOLOR");
                    transectParams = addParameterValue(wmsLayer, transectParams, "BGCOLOR");

                    String stylesStr = wmsLayer.getParams().getStyles();
                    if (stylesStr != null && !stylesStr.equals("")) {
                        String[] styles = stylesStr.split(",");
                        String palette = null;
                        String[] styleParts = styles[0].split("/");
                        if (styleParts.length == 2) {
                            palette = styleParts[1];
                        }
                        if (palette != null) {
                            transectParams += "&PALETTE=" + palette;
                        }
                    }
                    /*
                     * Yes, this is peculiar. Yes, it is also necessary.
                     * 
                     * Without this, the GetFeatureInfo functionality stops
                     * working after a transect graph has been plotted.
                     * 
                     * Please feel free to play with it for hours trying to get
                     * it to work another way - and Good Luck!
                     */
                    if (getFeatureInfo != null) {
                        getFeatureInfo.deactivate();
                        getFeatureInfo.activate();
                    }
                    displayImagePopup(wmsAndParams.wmsUrl + "?" + transectParams, "Transect");
                }
            }

            /*
             * Adds the value of the requested parameter to the URL string if it
             * exists on the WMS layer
             */
            private String addParameterValue(WMS wmsLayer, String transectUrl,
                    String parameterName) {
                String parameterValue = wmsLayer.getParams().getJSObject()
                        .getPropertyAsString(parameterName);
                if (parameterValue != null && !parameterValue.equals("")) {
                    return transectUrl + "&" + parameterName + "=" + parameterValue;
                } else {
                    return transectUrl;
                }
            }
        });
        editingToolbar = new EditingToolbar(drawingLayer);
        map.addControl(editingToolbar);
    }

    /*
     * Gets the ID of the layer to be used for transects + KML
     * 
     * If it hasn't been set, pick a random layer. Failing that, return null
     */
    protected String getTransectLayerId() {
        if (transectLayer != null) {
            return transectLayer;
        } else {
            return wmsLayers.keySet().isEmpty() ? null : wmsLayers.keySet().iterator().next();
        }
    }

    public void setTransectLayerId(String transectLayer) {
        this.transectLayer = transectLayer;
    }

    public void setAllowTransects(boolean allowTransects) {
        if (allowTransects) {
            editingToolbar.activate();
        } else {
            editingToolbar.deactivate();
        }
    }

    @Override
    public ScreenPosition getCentre() {
        return new ScreenPosition(getAbsoluteLeft() + getOffsetWidth() / 2,
                getAbsoluteTop() + getOffsetHeight() / 2);
    }

    /**
     * This should be called when the position/size of the map has changed.
     * Failing to do so will lead to e.g. incorrect positions for GetFeatureInfo
     */
    public void updatePos() {
        map.updateSize();
    }
}
