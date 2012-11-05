package uk.ac.rdg.resc.godiva.client;

import java.util.List;

import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.LonLat;

import uk.ac.rdg.resc.godiva.client.handlers.AviExportHandler;
import uk.ac.rdg.resc.godiva.client.requests.CaseInsensitiveParameterMap;
import uk.ac.rdg.resc.godiva.client.requests.ConnectionException;
import uk.ac.rdg.resc.godiva.client.requests.LayerDetails;
import uk.ac.rdg.resc.godiva.client.requests.LayerMenuItem;
import uk.ac.rdg.resc.godiva.client.requests.LayerTreeJSONParser;
import uk.ac.rdg.resc.godiva.client.widgets.AnimationButton;
import uk.ac.rdg.resc.godiva.client.widgets.CopyrightInfo;
import uk.ac.rdg.resc.godiva.client.widgets.CopyrightInfoIF;
import uk.ac.rdg.resc.godiva.client.widgets.DialogBoxWithCloseButton;
import uk.ac.rdg.resc.godiva.client.widgets.ElevationSelector;
import uk.ac.rdg.resc.godiva.client.widgets.ElevationSelectorIF;
import uk.ac.rdg.resc.godiva.client.widgets.GodivaStateInfo;
import uk.ac.rdg.resc.godiva.client.widgets.Info;
import uk.ac.rdg.resc.godiva.client.widgets.InfoIF;
import uk.ac.rdg.resc.godiva.client.widgets.LayerSelectorCombo;
import uk.ac.rdg.resc.godiva.client.widgets.LayerSelectorIF;
import uk.ac.rdg.resc.godiva.client.widgets.MapArea;
import uk.ac.rdg.resc.godiva.client.widgets.OpacitySelector;
import uk.ac.rdg.resc.godiva.client.widgets.PaletteSelector;
import uk.ac.rdg.resc.godiva.client.widgets.PaletteSelectorIF;
import uk.ac.rdg.resc.godiva.client.widgets.TimeSelector;
import uk.ac.rdg.resc.godiva.client.widgets.TimeSelectorIF;
import uk.ac.rdg.resc.godiva.client.widgets.UnitsInfo;
import uk.ac.rdg.resc.godiva.client.widgets.UnitsInfoIF;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
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

public class Godiva extends BaseWmsClient implements AviExportHandler {
    private static final String WMS_LAYER_ID = "singleLayer";

    private LayerSelectorIF layerSelector;
    private GodivaStateInfo widgetCollection;

    protected Image logo;
    private Image loadingImage;

    protected boolean permalinking;
    protected CaseInsensitiveParameterMap permalinkParamsMap;
    private int zoom = 1;
    private LonLat centre = new LonLat(0.0, 0.0);

    // The link to the Google Earth KMZ
    protected Anchor kmzLink;
    // Link to the current state
    protected Anchor permalink;
    // Email a link to the current state
    protected Anchor email;
    // Link to a screenshot of the current state
    protected Anchor screenshot;
    // Link to the documentation for the system
    private Anchor docLink;

    private OpacitySelector opacitySelector;

    // Button to create animations
    private AnimationButton anim;
    
    private PushButton infoButton;

    @Override
    public void init() {
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
        kmzLink.setStylePrimaryName("labelStyle");
        kmzLink.setTitle("Open the current view in Google Earth");

        permalink = new Anchor("Permalink");
        permalink.setStylePrimaryName("labelStyle");
        permalink.setTarget("_blank");
        permalink.setTitle("Permanent link to the current view");

        email = new Anchor("Email Link");
        email.setStylePrimaryName("labelStyle");
        email.setTitle("Email a link to the current view");

        screenshot = new Anchor("Export to PNG");
        screenshot.setHref("/screenshots/getScreenshot?");
        screenshot.setStylePrimaryName("labelStyle");
        screenshot.setTarget("_blank");
        screenshot.setTitle("Open a downloadable image in a new window - may be slow to load");

        docLink = new Anchor("Documentation", docHref);
        docLink.setTarget("_blank");
        docLink.setTitle("Open documentation in a new window");

        mapArea.setTransectLayerId(WMS_LAYER_ID);


        /*
         * We use a get method here, so that subclasses can override and use
         * their own custom implementations.
         * 
         * Currently only these widgets are initialised in this method, since
         * they are the most complex. The other things such as links can easily
         * be removed.
         * 
         * TODO This should probably be extended to the animation button and
         * opacity selector at the very least, but first they need to be
         * interfaceised.  Yes that is a word.
         */
        layerSelector = getLayerSelector();
        ElevationSelectorIF elevationSelector = getElevationSelector();
        TimeSelectorIF timeSelector = getTimeSelector();
        PaletteSelectorIF paletteSelector = getPaletteSelector(layerSelector);

        UnitsInfoIF unitsInfo = new UnitsInfo();
        final CopyrightInfoIF copyrightInfo = new CopyrightInfo();
        final InfoIF moreInfo = new Info();

        widgetCollection = new GodivaStateInfo(elevationSelector, timeSelector, paletteSelector,
                unitsInfo, copyrightInfo, moreInfo, layerSelector);

        opacitySelector = new OpacitySelector(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                setOpacity();
            }
        });
        anim = new AnimationButton(mapArea, proxyUrl, layerSelector, timeSelector, this);

        logo = getLogo();

        loadingImage = new Image(GWT.getModuleBaseURL()+"img/loading.gif");
        loadingImage.setVisible(false);
        loadingImage.setStylePrimaryName("loadingImage");

        
        infoButton = new PushButton(new Image(GWT.getModuleBaseURL()+"img/info.png"));
        infoButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                DialogBoxWithCloseButton popup = new DialogBoxWithCloseButton();
                popup.setHTML("<b>Information</b>");
                VerticalPanel vPanel = new VerticalPanel();
                if(copyrightInfo.hasCopyright()){
                    vPanel.add(new HTML("<b>Copyright:</b> "+copyrightInfo.getCopyrightInfo()));
                }
                if(moreInfo.hasInfo()){
                    vPanel.add(new HTML("<b>Info:</b> "+moreInfo.getInfo()));
                }
                popup.add(vPanel);
                popup.center();
            }
        });
        infoButton.setEnabled(false);

        RootLayoutPanel mainWindow = RootLayoutPanel.get();

        mainWindow.add(LayoutManager.getGodiva3Layout(layerSelector, unitsInfo, timeSelector,
                elevationSelector, paletteSelector, kmzLink, permalink, email, screenshot, logo,
                mapArea, loadingImage, anim, opacitySelector, infoButton));
        
        timeSelector.setEnabled(false);
        elevationSelector.setEnabled(false);
        paletteSelector.setEnabled(false);
        unitsInfo.setEnabled(false);
        copyrightInfo.setEnabled(false);
        opacitySelector.setEnabled(false);
    }
    
    /**
     * This is something that will almost certainly be changed for custom
     * clients, so put it in a method which can be overridden
     */
    protected Image getLogo() {
        return new Image(GWT.getModuleBaseURL()+"img/logo.png");
    }

    private void setOpacity() {
        mapArea.setOpacity(WMS_LAYER_ID, opacitySelector.getOpacity());
        updateLinksEtc();
    }

    /**
     * Requests the layer menu from the server. When the menu is returned,
     * menuLoaded will be called
     */
    @Override
    protected void requestAndPopulateMenu() {
        /*
         * This is where we define the fact that we are working with a single local server
         */
        final String wmsUrl = "wms";
        RequestBuilder getMenuRequest = new RequestBuilder(RequestBuilder.GET,
                getUrlFromGetArgs(wmsUrl, "?request=GetMetadata&item=menu"));
        getMenuRequest.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request req, Response response) {
                try {
                    if (response.getStatusCode() != Response.SC_OK) {
                        throw new ConnectionException("Error contacting server");
                    }
                    JSONValue jsonMap = JSONParser.parseLenient(response.getText());
                    JSONObject parentObj = jsonMap.isObject();
                    LayerMenuItem menuTree = LayerTreeJSONParser.getTreeFromJson(wmsUrl, parentObj);

                    menuLoaded(menuTree);
                } catch (Exception e) {
                    invalidJson(e);
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
    
    protected void menuLoaded(LayerMenuItem menuTree) {
        if (menuTree.isLeaf()) {
            menuTree.addChildItem(new LayerMenuItem("No georeferencing data found!", null, false, null));
        }
        layerSelector.populateLayers(menuTree);

        Window.setTitle(menuTree.getTitle());

        if (permalinking) {
            String currentLayer = permalinkParamsMap.get("layer");
            if (currentLayer != null) {
                layerSelector.selectLayer(currentLayer, false);
            }
        }
    }

    @Override
    public void layerDetailsLoaded(LayerDetails layerDetails, boolean autoUpdate) {
        populateWidgets(layerDetails, widgetCollection);
        
        if(widgetCollection.getCopyrightInfo().hasCopyright() || widgetCollection.getMoreInfo().hasInfo()){
            infoButton.setEnabled(true);
        } else {
            infoButton.setEnabled(false);
        }
        
        /*
         * We populate our widgets here, but in a multi-layer system, we may
         * want to create new widgets here
         */
        if (autoUpdate) {
            widgetCollection.getPaletteSelector().selectPalette(layerDetails.getSelectedPalette());
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


            String currentElevation = permalinkParamsMap.get("elevation");
            if (currentElevation != null) {
                widgetCollection.getElevationSelector().setSelectedElevation(currentElevation);
            }

            String currentPalette = permalinkParamsMap.get("palette");
            if (currentPalette != null) {
                widgetCollection.getPaletteSelector().selectPalette(currentPalette);
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
    public void loadingStarted() {
        loadingImage.setVisible(true);
    }

    @Override
    public void loadingFinished() {
        loadingImage.setVisible(false);
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
    public void availableTimesLoaded(String layerId, List<String> availableTimes, String nearestTime) {
        widgetCollection.getTimeSelector().populateTimes(availableTimes);
        if (nearestTime != null) {
            widgetCollection.getTimeSelector().selectDateTime(nearestTime);
        }
    }

    @Override
    public void rangeLoaded(String layerId, double min, double max) {
        widgetCollection.getPaletteSelector().setScaleRange(min + "," + max, null);
    }

    @Override
    public GodivaStateInfo getWidgetCollection(String layerId) {
        return widgetCollection;
    }

    @Override
    public void updateMap(MapArea mapArea, String layerUpdated) {
        List<String> selectedIds = layerSelector.getSelectedIds();
        if (selectedIds == null || selectedIds.size() == 0) {
            /*
             * This should never happen here, but let's be safe
             */
            throw new IllegalArgumentException("No layers selected");
        }
        if (permalinking) {
            String centre = permalinkParamsMap.get("centre");
            if (centre != null) {
                String zoom = permalinkParamsMap.get("zoom");
                if (zoom != null) {
                    mapArea.getMap().setCenter(
                            new LonLat(Double.parseDouble(centre.split(",")[0]),
                                    Double.parseDouble(centre.split(",")[1])),
                            Integer.parseInt(zoom));
                } else {
                    mapArea.getMap().setCenter(
                            new LonLat(Double.parseDouble(centre.split(",")[0]), Double
                                    .parseDouble(centre.split(",")[1])));
                }
            } else {
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
                             * Can't parse one of the bounds. Oh well, not a lot
                             * we can do
                             */
                        }
                    }
                }
            }
        }
        
        String currentTime = null;
        String colorbyTime = null;
        if(widgetCollection.getTimeSelector().isContinuous()){
            currentTime = widgetCollection.getTimeSelector().getSelectedDateTimeRange();
            colorbyTime = widgetCollection.getTimeSelector().getSelectedDateTime();
        } else {
            currentTime = widgetCollection.getTimeSelector().getSelectedDateTime();
        }
        
        String currentElevation = null;
        String colorbyElevation = null;
        if(widgetCollection.getElevationSelector().isContinuous()){
            currentElevation = widgetCollection.getElevationSelector().getSelectedElevationRange();
            colorbyElevation = widgetCollection.getElevationSelector().getSelectedElevation();
        } else {
            currentElevation = widgetCollection.getElevationSelector().getSelectedElevation();
        }

        String currentPalette = widgetCollection.getPaletteSelector().getSelectedPalette();
        String currentStyle = widgetCollection.getPaletteSelector().getSelectedStyle();
        String currentScaleRange = widgetCollection.getPaletteSelector().getScaleRange();
        int nColourBands = widgetCollection.getPaletteSelector().getNumColorBands();
        boolean logScale = widgetCollection.getPaletteSelector().isLogScale();
        mapArea.addLayer(widgetCollection.getWmsUrlProvider().getWmsUrl(), WMS_LAYER_ID, layerSelector.getSelectedIds().get(0), currentTime, colorbyTime,
                currentElevation, colorbyElevation, currentStyle, currentPalette, currentScaleRange, nColourBands,
                logScale, widgetCollection.getElevationSelector().getNElevations() > 1,
                widgetCollection.getTimeSelector().hasMultipleTimes());
        
        /*
         * Set the opacity after updating the map, otherwise it doesn't work
         */
        if(permalinking){
            permalinking = false;
            String currentOpacity = permalinkParamsMap.get("opacity");
            if (currentOpacity != null) {
                opacitySelector.setOpacity(Float.parseFloat(currentOpacity));
                setOpacity();
            }
        }
        updateLinksEtc();

        /*
         * This doesn't get automatically enabled by the base client (because it
         * is Godiva3 specific)
         */
        opacitySelector.setEnabled(true);
    }

    @Override
    public String getCurrentTime() {
        return widgetCollection.getTimeSelector().getSelectedDateTime();
    }

    @Override
    public void onMapMove(MapMoveEvent eventObject) {
        super.onMapMove(eventObject);
        centre = mapArea.getMap().getCenter();
        updateLinksEtc();
    }

    @Override
    public void onMapZoom(MapZoomEvent eventObject) {
        super.onMapZoom(eventObject);
        zoom = mapArea.getMap().getZoom();
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

    /**
     * Updates the links (KMZ, screenshot, email, permalink...)
     */
    private void updateLinksEtc() {
        kmzLink.setHref(mapArea.getKMZUrl());

        String baseurl = "http://" + Window.Location.getHost() + Window.Location.getPath()
                + "?permalinking=true&";

        PaletteSelectorIF paletteSelector = widgetCollection.getPaletteSelector();

        String urlParams = "dataset=" + widgetCollection.getWmsUrlProvider().getWmsUrl() + "&numColorBands="
                + paletteSelector.getNumColorBands() + "&logScale=" + paletteSelector.isLogScale()
                + "&zoom=" + zoom + "&centre=" + centre.lon() + "," + centre.lat();

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

        List<String> selectedIds = layerSelector.getSelectedIds();
        if (selectedIds != null && selectedIds.size() > 0) {
            currentLayer = selectedIds.get(0);
            urlParams += "&layer=" + currentLayer;
        }

        if (timeSelector.getSelectedDateTime() != null) {
            urlParams += "&time=" + timeSelector.getSelectedDateTime();
        }
        if (timeSelector.getRange() != null) {
            urlParams += "&range=" + timeSelector.getRange();
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
        urlParams += "&opacity=" + opacitySelector.getOpacity();

        anim.updateDetails(currentLayer, currentElevation, currentPalette, currentStyle,
                scaleRange, nColorBands, logScale);

        permalink.setHref(URL.encode(baseurl + urlParams));
        email.setHref("mailto:?subject=MyOcean Data Link&body="
                + URL.encodeQueryString(baseurl + urlParams));

        // Screenshot-only stuff
        urlParams += "&bbox=" + mapArea.getMap().getExtent().toBBox(6);
        if(layerSelector != null) {
            StringBuilder title = new StringBuilder();
            if (layerSelector.getTitleElements() != null
                    && layerSelector.getTitleElements().size() > 0) {
                for (String element : layerSelector.getTitleElements()) {
                    title.append(element + ",");
                }
                title.deleteCharAt(title.length()-1);
                urlParams += "&layerTitle=" + title;
            }
        }
        urlParams += "&crs=EPSG:4326";
        urlParams += "&mapHeight=" + mapHeight;
        urlParams += "&mapWidth=" + mapWidth;
        urlParams += "&style=" + currentStyle;
        if(elevationSelector != null)
            urlParams += "&zUnits=" + elevationSelector.getVerticalUnits();
        if (unitsInfo != null)
            urlParams += "&units=" + unitsInfo.getUnits();
        urlParams += "&baseUrl="+mapArea.getBaseLayerUrl();
        urlParams += "&baseLayers="+mapArea.getBaseLayerLayers();
        screenshot.setHref("screenshots/createScreenshot?" + urlParams);
    }

    @Override
    public String getAviUrl(String times, String frameRate) {
        PaletteSelectorIF paletteSelector = widgetCollection.getPaletteSelector();

        String urlParams = "dataset=" + widgetCollection.getWmsUrlProvider().getWmsUrl()
                + "&numColorBands=" + paletteSelector.getNumColorBands() + "&logScale="
                + paletteSelector.isLogScale() + "&zoom=" + zoom + "&centre=" + centre.lon() + "," + centre.lat();

        ElevationSelectorIF elevationSelector = widgetCollection.getElevationSelector();
        
        UnitsInfoIF unitsInfo = widgetCollection.getUnitsInfo();

        String currentLayer = null;
        String currentElevation = null;
        String currentPalette = null;
        String currentStyle = null;
        String scaleRange = null;

        urlParams += "&time=" + times;
        
        List<String> selectedIds = layerSelector.getSelectedIds();
        if (selectedIds != null && selectedIds.size() > 0) {
            currentLayer = selectedIds.get(0);
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
        if(layerSelector != null) {
            StringBuilder title = new StringBuilder();
            if(layerSelector.getTitleElements() != null && layerSelector.getTitleElements().size() > 0) {
                for(String element : layerSelector.getTitleElements()){
                    title.append(element+",");
                }
                title.deleteCharAt(title.length()-1);
                urlParams += "&layerTitle=" + title;
            }
        }
        urlParams += "&srs=EPSG:4326";
        urlParams += "&mapHeight=" + mapHeight;
        urlParams += "&mapWidth=" + mapWidth;
        urlParams += "&style=" + currentStyle;
        if(elevationSelector != null)
            urlParams += "&zUnits=" + elevationSelector.getVerticalUnits();
        if (unitsInfo != null)
            urlParams += "&units=" + unitsInfo.getUnits();
        urlParams += "&baseUrl="+mapArea.getBaseLayerUrl();
        urlParams += "&baseLayers="+mapArea.getBaseLayerLayers();
        if(frameRate != null)
            urlParams += "&frameRate="+frameRate;
        
        return "screenshots/createVideo?"+urlParams;
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
     * We define a set of methods here to get the initial widgets needed. That
     * way, a subclass can easily override these methods to replace them with
     * custom implementations, whilst still keeping the Godiva feature set (i.e.
     * one WMS layer at a time)
     */
    protected PaletteSelectorIF getPaletteSelector(LayerSelectorIF wmsUrlProvider) {
        return new PaletteSelector("mainLayer", getMapHeight(), 30,
                this, layerSelector, true);
    }

    protected TimeSelectorIF getTimeSelector() {
        return new TimeSelector("mainLayer", this);
    }

    protected ElevationSelectorIF getElevationSelector() {
        return new ElevationSelector("mainLayer", "Depth", this);
    }
    

    protected LayerSelectorIF getLayerSelector() {
        return new LayerSelectorCombo(this);
    }
}
