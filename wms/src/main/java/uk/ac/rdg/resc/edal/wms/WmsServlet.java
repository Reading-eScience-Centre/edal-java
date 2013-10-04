package uk.ac.rdg.resc.edal.wms;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.naming.OperationNotSupportedException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.implement.EscapeXmlReference;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.GridDataset;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.domain.VerticalDomain;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.exceptions.MetadataException;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.graphics.formats.ImageFormat;
import uk.ac.rdg.resc.edal.graphics.formats.SimpleFormat;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.graphics.style.util.ColourPalette;
import uk.ac.rdg.resc.edal.graphics.style.util.FeatureCatalogue.MapFeatureAndMember;
import uk.ac.rdg.resc.edal.graphics.style.util.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.grid.RegularGrid;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.TimeUtils;
import uk.ac.rdg.resc.edal.wms.util.WmsUtils;

/**
 * Servlet implementation class WmsServlet
 */
public class WmsServlet extends HttpServlet {
    public static final int TEST = 1;
    private static final long serialVersionUID = 1L;
    private static final String FEATURE_INFO_XML_FORMAT = "text/xml";
    private static final String FEATURE_INFO_PNG_FORMAT = "image/png";
    private static final String[] SUPPORTED_CRS_CODES = new String[] { "EPSG:4326", "CRS:84",
            "EPSG:41001", // Mercator
            "EPSG:27700", // British National Grid
            "EPSG:3408", // NSIDC EASE-Grid North
            "EPSG:3409", // NSIDC EASE-Grid South
            "EPSG:3857", // Google Maps
            "EPSG:32661", // North Polar stereographic
            "EPSG:32761" // South Polar stereographic
    };

    private WmsCatalogue catalogue;
    private final VelocityEngine velocityEngine;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public WmsServlet() {
        super();
        /*
         * Initialise the velocity templating engine ready for use in
         * GetFeatureInfo and GetCapabilities
         */
        Properties props = new Properties();
        props.put("resource.loader", "class");
        props.put("class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine = new VelocityEngine();
        velocityEngine.init(props);
    }

    public void setCatalogue(WmsCatalogue catalogue) {
        this.catalogue = catalogue;
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    protected void doGet(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException, IOException {

        /*
         * Create an object that allows request parameters to be retrieved in a
         * way that is not sensitive to the case of the parameter NAMES (but is
         * sensitive to the case of the parameter VALUES).
         */
        RequestParams params = new RequestParams(httpServletRequest.getParameterMap());

        try {
            /*
             * Check the REQUEST parameter to see if we're producing a
             * capabilities document, a map or a FeatureInfo
             */
            String request = params.getMandatoryString("request");
            dispatchWmsRequest(request, params, httpServletRequest, httpServletResponse);
        } catch (EdalException wmse) {
            handleWmsException(wmse, httpServletResponse);
        } catch (SocketException se) {
            /*
             * SocketExceptions usually happen when the client has aborted the
             * connection, so there's nothing we can do here
             */
        } catch (IOException ioe) {
            /*
             * Filter out Tomcat ClientAbortExceptions, which for some reason
             * don't inherit from SocketException. We check the class name to
             * avoid a compile-time dependency on the Tomcat libraries
             */
            if (ioe.getClass().getName()
                    .equals("org.apache.catalina.connector.ClientAbortException")) {
            }
            /*
             * Other types of IOException are potentially interesting and must
             * be rethrown to avoid hiding errors (maybe they represent internal
             * errors when reading data for instance).
             */
            throw ioe;
        } catch (Exception e) {
            e.printStackTrace();
            /* An unexpected (internal) error has occurred */
            throw new IOException(e);
        }
    }

    private void dispatchWmsRequest(String request, RequestParams params,
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws Exception {
        if (request.equals("GetMap")) {
            getMap(params, httpServletResponse);
        } else if (request.equals("GetCapabilities")) {
            getCapabilities(params, httpServletResponse, httpServletRequest.getRequestURL()
                    .toString());
        } else if (request.equals("GetFeatureInfo")) {
            /* Look to see if we're requesting data from a remote server */
            String url = params.getString("url");
            if (url != null && !url.trim().equals("")) {
                /*
                 * TODO We need to proxy the request if it is on a different
                 * server
                 */
//                NcwmsMetadataController.proxyRequest(url, httpServletRequest, httpServletResponse);
//                return;
            }
            getFeatureInfo(params, httpServletResponse);
        }
        /*
         * The REQUESTs below are non-standard
         */
        else if (request.equals("GetMetadata")) {
            /*
             * This is a request for non-standard metadata.
             */
            getMetadata(params, httpServletResponse);
        } else if (request.equals("GetLegendGraphic")) {
            /*
             * This is a request for an image representing the legend for the
             * map parameters
             */
            getLegendGraphic(params, httpServletResponse);
        } else if (request.equals("GetTimeseries")) {
            getTimeseries(params, httpServletResponse);
        } else if (request.equals("GetTransect")) {
            getTransect(params, httpServletResponse);
        } else if (request.equals("GetVerticalProfile")) {
            getVerticalProfile(params, httpServletResponse);
        } else if (request.equals("GetVerticalSection")) {
            getVerticalSection(params, httpServletResponse);
        } else {
            throw new OperationNotSupportedException(request);
        }
    }

    private void getMap(RequestParams params, HttpServletResponse httpServletResponse)
            throws EdalException {
        GetMapParameters getMapParams = new GetMapParameters(params);

        GlobalPlottingParams plottingParameters = getMapParams.getPlottingParameters();
        GetMapStyleParams styleParameters = getMapParams.getStyleParameters();
        if (!(getMapParams.getImageFormat() instanceof SimpleFormat)) {
            throw new EdalException("Currently KML is not supported.");
            /*
             * TODO Support KML
             */
        }
        SimpleFormat simpleFormat = (SimpleFormat) getMapParams.getImageFormat();

        /*
         * Do some checks on the style parameters.
         * 
         * These only apply to non-XML styles. XML ones are more complex to
         * handle.
         * 
         * TODO sort out some checks on XML styles.
         */
        if (!styleParameters.isXmlDefined()) {
            if (styleParameters.isTransparent()
                    && !getMapParams.getImageFormat().supportsFullyTransparentPixels()) {
                throw new EdalException("The image format "
                        + getMapParams.getImageFormat().getMimeType()
                        + " does not support fully-transparent pixels");
            }
            if (styleParameters.getOpacity() < 100
                    && !getMapParams.getImageFormat().supportsPartiallyTransparentPixels()) {
                throw new EdalException("The image format "
                        + getMapParams.getImageFormat().getMimeType()
                        + " does not support partially-transparent pixels");
            }
            if (styleParameters.getNumLayers() > catalogue.getMaxSimultaneousLayers()) {
                throw new EdalException("Only " + catalogue.getMaxSimultaneousLayers()
                        + " layer(s) can be plotted at once");
            }
        }

        /*
         * Check the dimensions of the image
         */
        if (plottingParameters.getHeight() > catalogue.getMaxImageHeight()
                || plottingParameters.getWidth() > catalogue.getMaxImageWidth()) {
            throw new EdalException("Requested image size exceeds the maximum of "
                    + catalogue.getMaxImageWidth() + "x" + catalogue.getMaxImageHeight());
        }

        MapImage imageGenerator = styleParameters.getImageGenerator(catalogue);

        List<BufferedImage> frames;
        if (!getMapParams.isAnimation()) {
            frames = Arrays.asList(imageGenerator.drawImage(plottingParameters, catalogue));
        } else {
            throw new UnsupportedOperationException("Animations are not yet supported");
        }

        try {
            ServletOutputStream outputStream = httpServletResponse.getOutputStream();
            simpleFormat.writeImage(frames, outputStream, null);
            outputStream.close();
        } catch (IOException e) {
            /*
             * The client can quite often cancel requests when loading tiled
             * maps.
             * 
             * This gives Broken pipe errors which can be ignored.
             * 
             * TODO what about other exceptions?
             */
        }
    }

    private void getCapabilities(RequestParams params, HttpServletResponse httpServletResponse,
            String baseUrl) throws EdalException {
//        ToolManager toolManager = new ToolManager();
//        toolManager.autoConfigure(true);
        VelocityContext context = new VelocityContext();// toolManager.createContext());
        EventCartridge ec = new EventCartridge();
        ec.addEventHandler(new EscapeXmlReference());
        ec.attachToContext(context);
        context.put("baseUrl", baseUrl);
        context.put("catalogue", catalogue);
        context.put("supportedImageFormats", ImageFormat.getSupportedMimeTypes());
        context.put("supportedFeatureInfoFormats", new String[] { FEATURE_INFO_PNG_FORMAT,
                FEATURE_INFO_XML_FORMAT });
        context.put("supportedCrsCodes", SUPPORTED_CRS_CODES);
        context.put("GISUtils", GISUtils.class);
        context.put("TimeUtils", TimeUtils.class);
        context.put("WmsUtils", WmsUtils.class);
        context.put("verbose", params.getBoolean("verbose", false));

        Template template = velocityEngine.getTemplate("templates/capabilities-1.3.0.vm");
        try {
            template.merge(context, httpServletResponse.getWriter());
        } catch (ResourceNotFoundException e) {
            // TODO Add logging
            e.printStackTrace();
        } catch (ParseErrorException e) {
            // TODO Add logging
            e.printStackTrace();
        } catch (MethodInvocationException e) {
            // TODO Add logging
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Add logging
            e.printStackTrace();
        }
    }

    private void getFeatureInfo(RequestParams params, HttpServletResponse httpServletResponse)
            throws EdalException {
        GetFeatureInfoParameters featureInfoParameters = new GetFeatureInfoParameters(params);
        GlobalPlottingParams plottingParameters = featureInfoParameters.getPlottingParameters();
        RegularGrid imageGrid = WmsUtils.getImageGrid(plottingParameters);
        Double xVal = imageGrid.getXAxis().getCoordinateValue(featureInfoParameters.getI());
        Double yVal = imageGrid.getYAxis().getCoordinateValue(
                imageGrid.getYAxis().size() - 1 - featureInfoParameters.getJ());
        HorizontalPosition position = new HorizontalPosition(xVal, yVal,
                imageGrid.getCoordinateReferenceSystem());

        String[] layerNames = featureInfoParameters.getLayerNames();
        List<FeatureInfoPoint> featureInfos = new ArrayList<FeatureInfoPoint>();
        for (String layerName : layerNames) {
            Dataset dataset = catalogue.getDatasetFromId(layerName);
            String variableId = catalogue.getVariableFromId(layerName);

            if (dataset instanceof GridDataset) {
                GridDataset gridDataset = (GridDataset) dataset;
                Number value;
                try {
                    value = gridDataset.readSinglePoint(variableId, position,
                            plottingParameters.getTargetZ(), plottingParameters.getTargetT());
                    featureInfos.add(new FeatureInfoPoint(layerName, position, value));
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new EdalException("Problem reading data", e);
                }
            } else {
                throw new UnsupportedOperationException(
                        "GetFeatureInfo not supported for non-gridded features yet");
            }
        }

        Template template = velocityEngine.getTemplate("templates/featureInfo.vm");
        VelocityContext context = new VelocityContext();
        context.put("position", position);
        context.put("featureInfo", featureInfos);
        try {
            template.merge(context, httpServletResponse.getWriter());
        } catch (ResourceNotFoundException e) {
            // TODO Add logging
            e.printStackTrace();
        } catch (ParseErrorException e) {
            // TODO Add logging
            e.printStackTrace();
        } catch (MethodInvocationException e) {
            // TODO Add logging
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Add logging
            e.printStackTrace();
        }
    }

    /**
     * Handles returning metadata about a requested layer.
     * 
     * @param params
     *            The URL parameters
     * @param httpServletResponse
     *            The response object to write out to
     * @throws MetadataException
     *             If there are any issues with returning the metadata
     */
    protected void getMetadata(RequestParams params, HttpServletResponse httpServletResponse)
            throws MetadataException {
        String item = params.getString("item");
        String json = null;
        if (item == null) {
            throw new MetadataException("Must provide an ITEM parameter");
        } else if (item.equals("menu")) {
            json = showMenu(params);
        } else if (item.equals("layerDetails")) {
            json = showLayerDetails(params);
        } else if (item.equals("timesteps")) {
            json = showTimesteps(params);
        } else if (item.equals("minmax")) {
            json = showMinMax(params);
        } else if (item.equals("animationTimesteps")) {
            json = showAnimationTimesteps(params);
        }
        if (json != null) {
            httpServletResponse.setContentType("application/json");
            try {
                httpServletResponse.getWriter().write(json);
            } catch (IOException e) {
                /*
                 * TODO needs a log message, since this might not get back to
                 * the user
                 */
                throw new MetadataException("Problem writing JSON to output stream");
            }
        } else {
            throw new MetadataException("Invalid value for ITEM parameter");
        }
    }

    private String showMenu(RequestParams params) {
        JSONObject menu = new JSONObject();
        menu.put("label", catalogue.getServerName());
        JSONArray children = new JSONArray();
        for (Dataset dataset : catalogue.getAllDatasets()) {
            String datasetId = dataset.getId();

            Set<VariableMetadata> topLevelVariables = dataset.getTopLevelVariables();
            JSONArray datasetChildren = addVariablesToArray(topLevelVariables, datasetId);
            String datasetLabel = catalogue.getDatasetTitle(datasetId);
            JSONObject datasetJson = new JSONObject();
            datasetJson.put("label", datasetLabel);
            datasetJson.put("children", datasetChildren);
            children.add(datasetJson);
        }

        menu.put("children", children);
        return menu.toString(4);
    }

    private JSONArray addVariablesToArray(Set<VariableMetadata> variables, String datasetId) {
        JSONArray ret = new JSONArray();
        for (VariableMetadata variable : variables) {
            JSONObject child = new JSONObject();

            String id = variable.getId();
            String layerName = catalogue.getLayerName(datasetId, id);

            child.put("id", layerName);

            WmsLayerMetadata layerMetadata = catalogue.getLayerMetadata(layerName);
            String title = layerMetadata.getTitle();
            child.put("label", title);

            boolean plottable = variable.isPlottable();
            child.put("plottable", plottable);

            Set<VariableMetadata> children = variable.getChildren();
            if (children.size() > 0) {
                JSONArray childrenArray = addVariablesToArray(children, datasetId);
                child.put("children", childrenArray);
            }

            ret.add(child);
        }
        return ret;
    }

    private String showLayerDetails(RequestParams params) throws MetadataException {
        /*
         * Parse the parameters and get access to the variable and layer
         * metadata
         */
        String layerName = params.getString("layerName");
        if (layerName == null) {
            throw new MetadataException("Must supply a LAYERNAME parameter to get layer details");
        }
        String requestedTime = params.getString("time");

        /*
         * TODO Check that this is safe enough - do we need to catch any other
         * exceptions?
         * 
         * Yes, but they're not in place yet...
         */
        Dataset dataset = catalogue.getDatasetFromId(layerName);
        String variableId = catalogue.getVariableFromId(layerName);
        WmsLayerMetadata layerMetadata = catalogue.getLayerMetadata(layerName);
        if (dataset == null || variableId == null || layerMetadata == null) {
            throw new MetadataException("Must supply a valid LAYERNAME to get layer details");
        }

        VariableMetadata variableMetadata = dataset.getVariableMetadata(variableId);

        /*
         * Now create local variables containing the relevant details needed
         */
        String units = variableMetadata.getParameter().getUnits();
        BoundingBox boundingBox = GISUtils.constrainBoundingBox(variableMetadata
                .getHorizontalDomain().getBoundingBox());
        Extent<Float> scaleRange = layerMetadata.getColorScaleRange();
        Integer numColorBands = layerMetadata.getNumColorBands();
        /*
         * TODO Supported styles?
         */
        List<String> supportedStyles = Arrays.asList("default", "boxfill");

        VerticalDomain verticalDomain = variableMetadata.getVerticalDomain();
        TemporalDomain temporalDomain = variableMetadata.getTemporalDomain();

        boolean discreteZ = (verticalDomain instanceof VerticalAxis);
        boolean discreteT = (temporalDomain instanceof TimeAxis);

        DateTime targetTime = null;
        DateTime nearestTime = null;
        if (temporalDomain != null) {
            if (requestedTime != null) {
                try {
                    targetTime = TimeUtils.iso8601ToDateTime(requestedTime,
                            temporalDomain.getChronology());
                } catch (ParseException e) {
                    throw new MetadataException("Requested time has an invalid format", e);
                }
            } else {
                targetTime = new DateTime(temporalDomain.getChronology());
            }
            if (temporalDomain instanceof TimeAxis) {
                TimeAxis timeAxis = (TimeAxis) temporalDomain;
                long minDeltaT = Long.MAX_VALUE;
                for (DateTime time : timeAxis.getCoordinateValues()) {
                    long dT = Math.abs(time.getMillis() - targetTime.getMillis());
                    if (dT < minDeltaT) {
                        minDeltaT = dT;
                        nearestTime = time;
                    }
                }

            } else {
                /*
                 * If we have a continuous time axis, the nearest time will
                 * either be the start time, the end time, or the target time
                 */
                if (targetTime.isAfter(temporalDomain.getExtent().getHigh().getMillis())) {
                    nearestTime = temporalDomain.getExtent().getHigh();
                } else if (targetTime.isBefore(temporalDomain.getExtent().getLow().getMillis())) {
                    nearestTime = temporalDomain.getExtent().getLow();
                } else {
                    nearestTime = targetTime;
                }
            }
        }

        String moreInfo = layerMetadata.getMoreInfo();
        String copyright = layerMetadata.getCopyright();

        Set<String> supportedPalettes = ColourPalette.getPredefinedPalettes();
        String defaultPalette = layerMetadata.getPalette();
        Boolean logScaling = layerMetadata.isLogScaling();

        /*
         * Now write the layer details out to a JSON object
         */
        JSONObject layerDetails = new JSONObject();

        layerDetails.put("units", units);

        JSONArray bboxJson = new JSONArray();
        bboxJson.add(boundingBox.getMinX());
        bboxJson.add(boundingBox.getMinY());
        bboxJson.add(boundingBox.getMaxX());
        bboxJson.add(boundingBox.getMaxY());
        layerDetails.put("bbox", bboxJson);

        JSONArray scaleRangeJson = new JSONArray();
        scaleRangeJson.add(scaleRange.getLow());
        scaleRangeJson.add(scaleRange.getHigh());
        layerDetails.put("scaleRange", scaleRangeJson);

        layerDetails.put("numColorBands", numColorBands);

        JSONArray supportedStylesJson = new JSONArray();
        for (String supportedStyle : supportedStyles) {
            supportedStylesJson.add(supportedStyle);
        }
        layerDetails.put("supportedStyles", supportedStylesJson);

        if (verticalDomain != null) {
            layerDetails.put("continuousZ", !discreteZ);
            /*
             * TODO This changes the format slightly for continuous z-axis.
             * Remove this todo once godiva has been tested with continuous
             * z-axes
             */
            JSONObject zAxisJson = new JSONObject();
            zAxisJson.put("units", verticalDomain.getVerticalCrs().getUnits());
            zAxisJson.put("positive", verticalDomain.getVerticalCrs().isPositiveUpwards());
            if (verticalDomain instanceof VerticalAxis) {
                VerticalAxis verticalAxis = (VerticalAxis) verticalDomain;
                JSONArray zValuesJson = new JSONArray();
                for (Double z : verticalAxis.getCoordinateValues()) {
                    zValuesJson.add(z);
                }
                zAxisJson.put("values", zValuesJson);
            } else {
                zAxisJson.put("startZ", verticalDomain.getExtent().getLow());
                zAxisJson.put("endZ", verticalDomain.getExtent().getHigh());
            }
            layerDetails.put("zaxis", zAxisJson);
        }

        if (temporalDomain != null) {
            layerDetails.put("continuousT", !discreteT);
            if (temporalDomain instanceof TimeAxis) {
                TimeAxis timeAxis = (TimeAxis) temporalDomain;
                Map<Integer, Map<Integer, List<Integer>>> datesWithData = new LinkedHashMap<Integer, Map<Integer, List<Integer>>>();
                for (DateTime dateTime : timeAxis.getCoordinateValues()) {
                    /*
                     * We must make sure that dateTime() is in UTC or
                     * getDayOfMonth() etc might return unexpected results
                     */
                    dateTime = dateTime.withZone(DateTimeZone.UTC);
                    /*
                     * See whether this dateTime is closer to the target
                     * dateTime than the current closest value
                     */
                    int year = dateTime.getYear();
                    Map<Integer, List<Integer>> months = datesWithData.get(year);
                    if (months == null) {
                        months = new LinkedHashMap<Integer, List<Integer>>();
                        datesWithData.put(year, months);
                    }
                    /*
                     * We need to subtract 1 from the month number as Javascript
                     * months are 0-based (Joda-time months are 1-based). This
                     * retains compatibility with previous behaviour.
                     */
                    int month = dateTime.getMonthOfYear() - 1;
                    List<Integer> days = months.get(month);
                    if (days == null) {
                        days = new ArrayList<Integer>();
                        months.put(month, days);
                    }
                    int day = dateTime.getDayOfMonth();
                    if (!days.contains(day)) {
                        days.add(day);
                    }
                }
                JSONObject datesWithDataJson = new JSONObject();
                for (Integer year : datesWithData.keySet()) {
                    Map<Integer, List<Integer>> months = datesWithData.get(year);
                    JSONObject monthsJson = new JSONObject();
                    for (Integer month : months.keySet()) {
                        JSONArray daysJson = new JSONArray();
                        List<Integer> days = months.get(month);
                        for (Integer day : days) {
                            daysJson.add(day);
                        }
                        monthsJson.put(month, daysJson);
                    }
                    datesWithDataJson.put(year, monthsJson);
                }
                layerDetails.put("datesWithData", datesWithDataJson);
            } else {
                layerDetails.put("startTime", temporalDomain.getExtent().getLow());
                layerDetails.put("endTime", temporalDomain.getExtent().getHigh());
            }
            layerDetails.put("timeAxisUnits",
                    WmsUtils.getTimeAxisUnits(temporalDomain.getChronology()));
        }

        layerDetails.put("nearestTimeIso", TimeUtils.dateTimeToISO8601(nearestTime));
        layerDetails.put("moreInfo", moreInfo);
        layerDetails.put("copyright", copyright);
        JSONArray supportedPalettesJson = new JSONArray();
        for (String supportedPalette : supportedPalettes) {
            supportedPalettesJson.add(supportedPalette);
        }
        layerDetails.put("palettes", supportedPalettesJson);
        layerDetails.put("defaultPalette", defaultPalette);
        layerDetails.put("logScaling", logScaling);

        return layerDetails.toString(4);
    }

    private String showTimesteps(RequestParams params) throws MetadataException {
        /*
         * Parse the parameters and get access to the variable and layer
         * metadata
         */
        String layerName = params.getString("layerName");
        if (layerName == null) {
            throw new MetadataException("Must supply a LAYERNAME parameter to get layer details");
        }

        Dataset dataset = catalogue.getDatasetFromId(layerName);
        String variableId = catalogue.getVariableFromId(layerName);
        VariableMetadata variableMetadata = dataset.getVariableMetadata(variableId);
        TemporalDomain temporalDomain = variableMetadata.getTemporalDomain();

        JSONObject response = new JSONObject();
        JSONArray timesteps = new JSONArray();

        String dayStr = params.getString("day");
        if (dayStr == null) {
            throw new MetadataException(
                    "Must provide the \"day\" parameter for a valid timesteps request");
        }
        DateTime day;
        try {
            day = TimeUtils.iso8601ToDate(dayStr, temporalDomain.getChronology());
        } catch (ParseException e) {
            throw new MetadataException("\"day\" parameter must be an ISO-formatted date");
        }

        if (temporalDomain instanceof TimeAxis) {
            TimeAxis timeAxis = (TimeAxis) temporalDomain;
            for (DateTime time : timeAxis.getCoordinateValues()) {
                if (TimeUtils.onSameDay(day, time)) {
                    timesteps.add(TimeUtils.formatUtcIsoTimeOnly(time));
                }
            }
        } else {
            throw new MetadataException(
                    "timesteps can only be returned for layers with a discrete time domain");
        }
        response.put("timesteps", timesteps);
        return response.toString();
    }

    private String showMinMax(RequestParams params) throws MetadataException {
        GetMapParameters getMapParams;
        try {
            getMapParams = new GetMapParameters(params);
        } catch (EdalException e) {
            e.printStackTrace();
            throw new MetadataException("Problem parsing parameters", e);
        }

        String[] layerNames = getMapParams.getStyleParameters().getLayerNames();
        if (layerNames.length != 1) {
            /*
             * TODO Perhaps relax this restriction and return min/max with layer
             * IDs?
             */
            throw new MetadataException("Can only find min/max for exactly one layer at a time");
        }

        MapFeatureAndMember featureAndMember = catalogue.getFeatureAndMemberName(layerNames[0],
                getMapParams.getPlottingParameters());
        MapFeature mapFeature = featureAndMember.getMapFeature();
        Array2D values = mapFeature.getValues(featureAndMember.getMember());

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        Iterator<Number> iterator = values.iterator();
        while (iterator.hasNext()) {
            Number value = iterator.next();
            if (value != null) {
                if (value.doubleValue() > max) {
                    max = value.doubleValue();
                }
                if (value.doubleValue() < min) {
                    min = value.doubleValue();
                }
            }
        }

        JSONObject minmax = new JSONObject();
        minmax.put("min", min);
        minmax.put("max", max);

        return minmax.toString();
    }

    private String showAnimationTimesteps(RequestParams params) throws MetadataException {
        String layerName = params.getString("layerName");
        if (layerName == null) {
            throw new MetadataException("Must supply a LAYERNAME parameter to get layer details");
        }

        Dataset dataset = catalogue.getDatasetFromId(layerName);
        String variableId = catalogue.getVariableFromId(layerName);
        VariableMetadata variableMetadata = dataset.getVariableMetadata(variableId);
        TemporalDomain temporalDomain = variableMetadata.getTemporalDomain();

        String startStr = params.getString("start");
        String endStr = params.getString("end");
        if (startStr == null || endStr == null) {
            throw new MetadataException("Must provide values for start and end");
        }

        if (temporalDomain instanceof TimeAxis) {
            TimeAxis timeAxis = (TimeAxis) temporalDomain;
            int startIndex;
            int endIndex;
            try {
                startIndex = timeAxis.findIndexOf(TimeUtils.iso8601ToDateTime(startStr,
                        timeAxis.getChronology()));
                endIndex = timeAxis.findIndexOf(TimeUtils.iso8601ToDateTime(endStr,
                        timeAxis.getChronology()));
            } catch (ParseException e) {
                throw new MetadataException("Time string is not ISO8601 formatted");
            }
            if (startIndex < 0 || endIndex < 0) {
                /*
                 * TODO This was previous behavious in ncWMS, but there's no
                 * strong reason for it
                 */
                throw new MetadataException(
                        "For animation timesteps, both start and end times must be part of the axis");
            }

            JSONObject response = new JSONObject();
            JSONArray timeStrings = new JSONArray();
            List<DateTime> tValues = timeAxis.getCoordinateValues();

            JSONObject timestep = new JSONObject();
            timestep.put("title", "Full (" + (endIndex - startIndex + 1) + " frames)");
            timestep.put("timeString", startStr + "/" + endStr);
            timeStrings.add(timestep);
            addTimeStringToJson("Daily", timeStrings, tValues, startIndex, endIndex,
                    new Period().withDays(1));
            addTimeStringToJson("Weekly", timeStrings, tValues, startIndex, endIndex,
                    new Period().withWeeks(1));
            addTimeStringToJson("Monthly", timeStrings, tValues, startIndex, endIndex,
                    new Period().withMonths(1));
            addTimeStringToJson("Bi-monthly", timeStrings, tValues, startIndex, endIndex,
                    new Period().withMonths(2));
            addTimeStringToJson("Twice-yearly", timeStrings, tValues, startIndex, endIndex,
                    new Period().withMonths(6));
            addTimeStringToJson("Yearly", timeStrings, tValues, startIndex, endIndex,
                    new Period().withYears(1));

            response.put("timeStrings", timeStrings);
            return response.toString();
        } else {
            Extent<DateTime> extent = temporalDomain.getExtent();
            /*
             * TODO How do we deal with timesteps for a continuous time domain?
             */
            throw new MetadataException(
                    "Animation timesteps can only be returned for layers with a discrete time domain");
        }
    }

    private static void addTimeStringToJson(String label, JSONArray jsonArray,
            List<DateTime> tValues, int startIndex, int endIndex, Period resolution) {
        List<DateTime> timesteps = new ArrayList<DateTime>();
        timesteps.add(tValues.get(startIndex));
        for (int i = startIndex + 1; i <= endIndex; i++) {
            DateTime lastdt = timesteps.get(timesteps.size() - 1);
            DateTime thisdt = tValues.get(i);
            if (!thisdt.isBefore(lastdt.plus(resolution))) {
                timesteps.add(thisdt);
            }
        }
        /* We filter out all the animations with less than 2 timesteps */
        if (timesteps.size() > 1) {
            String timeString = getTimeString(timesteps);
            JSONObject timestep = new JSONObject();
            timestep.put("title", label + " (" + timesteps.size() + " frames)");
            timestep.put("timeString", timeString);
            jsonArray.add(timestep);
        }
    }

    private static String getTimeString(List<DateTime> timesteps) {
        if (timesteps.size() == 0)
            return "";
        StringBuilder builder = new StringBuilder();
        for (DateTime timestep : timesteps) {
            builder.append(TimeUtils.dateTimeToISO8601(timestep) + ",");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    private void getLegendGraphic(RequestParams params, HttpServletResponse httpServletResponse)
            throws EdalException {
        BufferedImage legend;

        /* numColourBands defaults to ColorPalette.MAX_NUM_COLOURS if not set */
        int numColourBands = params.getPositiveInt("numcolorbands", ColourPalette.MAX_NUM_COLOURS);

        String paletteName = params.getString("palette", ColourPalette.DEFAULT_PALETTE_NAME);

        /* Find out if we just want the colour bar with no supporting text */
        String colorBarOnly = params.getString("colorbaronly", "false");
        boolean vertical = params.getBoolean("vertical", true);
        if (colorBarOnly.equalsIgnoreCase("true")) {
            /*
             * We're only creating the colour bar so we need to know a width and
             * height
             */
            int width = params.getPositiveInt("width", 50);
            int height = params.getPositiveInt("height", 200);
            /* Find the requested colour palette, or use the default if not set */
            ColourPalette palette = ColourPalette.fromString(paletteName, numColourBands);
            legend = palette.createColourBar(width, height, vertical);
        } else {
            /*
             * We're creating a legend with supporting text so we need to know
             * the colour scale range and the layer in question
             */
            /*
             * TODO This is relatively straightforward if full GetMap request
             * parameters have been sent...
             */
            throw new MetadataException("Can only produce colourbars at present - not full legends");
        }
        httpServletResponse.setContentType("image/png");
        try {
            ImageIO.write(legend, "png", httpServletResponse.getOutputStream());
        } catch (IOException e) {
            /*
             * TODO log message here
             */
            throw new EdalException("Unable to write legend graphic to output stream", e);
        }
    }

    private void getTimeseries(RequestParams params, HttpServletResponse httpServletResponse) {
        // TODO Auto-generated method stub

    }

    private void getTransect(RequestParams params, HttpServletResponse httpServletResponse) {
        // TODO Auto-generated method stub

    }

    private void getVerticalProfile(RequestParams params, HttpServletResponse httpServletResponse) {
        // TODO Auto-generated method stub

    }

    private void getVerticalSection(RequestParams params, HttpServletResponse httpServletResponse) {
        // TODO Auto-generated method stub

    }

    private void handleWmsException(EdalException wmse, HttpServletResponse httpServletResponse)
            throws IOException {
        /*
         * TODO this should return the exception as XML or potentially an image
         */
        PrintWriter writer = httpServletResponse.getWriter();
        wmse.printStackTrace(writer);
    }
}
