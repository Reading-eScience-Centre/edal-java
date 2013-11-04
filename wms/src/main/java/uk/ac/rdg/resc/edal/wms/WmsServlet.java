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

package uk.ac.rdg.resc.edal.wms;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.SocketException;
import java.util.AbstractList;
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
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.GridDataset;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.HorizontalDomain;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.domain.TrajectoryDomain;
import uk.ac.rdg.resc.edal.domain.VerticalDomain;
import uk.ac.rdg.resc.edal.exceptions.BadTimeFormatException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.exceptions.IncorrectDomainException;
import uk.ac.rdg.resc.edal.exceptions.MetadataException;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.LineString;
import uk.ac.rdg.resc.edal.graphics.Charting;
import uk.ac.rdg.resc.edal.graphics.formats.ImageFormat;
import uk.ac.rdg.resc.edal.graphics.formats.InvalidFormatException;
import uk.ac.rdg.resc.edal.graphics.formats.SimpleFormat;
import uk.ac.rdg.resc.edal.graphics.style.ColourMap;
import uk.ac.rdg.resc.edal.graphics.style.ColourScale;
import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.graphics.style.PaletteColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.util.ColourPalette;
import uk.ac.rdg.resc.edal.graphics.style.util.FeatureCatalogue.MapFeatureAndMember;
import uk.ac.rdg.resc.edal.graphics.style.util.PlottingDomainParams;
import uk.ac.rdg.resc.edal.graphics.style.util.GraphicsUtils;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RegularGrid;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxisImpl;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.TimeUtils;
import uk.ac.rdg.resc.edal.wms.exceptions.WmsLayerNotFoundException;
import uk.ac.rdg.resc.edal.wms.util.StyleDef;
import uk.ac.rdg.resc.edal.wms.util.WmsUtils;

/**
 * Servlet implementation class WmsServlet
 */
public class WmsServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(WmsServlet.class);
    
    public static final int AXIS_RESOLUTION = 500;
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
            boolean v130;
            try {
                v130 = "1.3.0".equals(params.getMandatoryWmsVersion());
            } catch (EdalException e) {
                /*
                 * No version supplied, we'll return the exception in 1.3.0 format
                 */
                v130 = true;
            }
            handleWmsException(wmse, httpServletResponse, v130);
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
                return;
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
//        } else if (request.equals("GetVerticalSection")) {
//            getVerticalSection(params, httpServletResponse);
        } else {
            throw new OperationNotSupportedException(request);
        }
    }

    private void getMap(RequestParams params, HttpServletResponse httpServletResponse)
            throws EdalException {
        GetMapParameters getMapParams = new GetMapParameters(params);

        PlottingDomainParams plottingParameters = getMapParams.getPlottingParameters();
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
             */
            log.error("Problem writing output to stream", e);
        }
    }

    private void getCapabilities(RequestParams params, HttpServletResponse httpServletResponse,
            String baseUrl) throws EdalException {
        VelocityContext context = new VelocityContext();
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
        context.put("availablePalettes", ColourPalette.getPredefinedPalettes());

        String wmsVersion = params.getMandatoryWmsVersion();
        Template template;
        if("1.1.1".equals(wmsVersion)) {
            template = velocityEngine.getTemplate("templates/capabilities-1.1.1.vm");
        } else {
            template = velocityEngine.getTemplate("templates/capabilities-1.3.0.vm");
        }
        try {
            template.merge(context, httpServletResponse.getWriter());
        } catch (ResourceNotFoundException e) {
            log.error("Cannot find capabilities template", e);
        } catch (ParseErrorException e) {
            log.error("Cannot parse capabilities template", e);
        } catch (MethodInvocationException e) {
            log.error("Capabilities template has incorrect method", e);
        } catch (IOException e) {
            log.error("Problem writing output to stream", e);
        }
    }

    private void getFeatureInfo(RequestParams params, HttpServletResponse httpServletResponse)
            throws EdalException {
        GetFeatureInfoParameters featureInfoParameters = new GetFeatureInfoParameters(params);
        PlottingDomainParams plottingParameters = featureInfoParameters.getPlottingParameters();
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
                TemporalDomain temporalDomain = gridDataset.getVariableMetadata(variableId).getTemporalDomain();
                Chronology chronology = null;
                if(temporalDomain != null) {
                    chronology = temporalDomain.getChronology();
                }
                Number value;
                value = gridDataset.readSinglePoint(variableId, position,
                        plottingParameters.getTargetZ(), plottingParameters.getTargetT(chronology));
                featureInfos.add(new FeatureInfoPoint(layerName, position, value));
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
            JSONArray datasetChildren;
            try {
                datasetChildren = addVariablesToArray(topLevelVariables, datasetId);
                String datasetLabel = catalogue.getDatasetTitle(datasetId);
                JSONObject datasetJson = new JSONObject();
                datasetJson.put("label", datasetLabel);
                datasetJson.put("children", datasetChildren);
                children.add(datasetJson);
            } catch (WmsLayerNotFoundException e) {
                /*
                 * This shouldn't happen - it means that we've failed to get
                 * layer metadata for a layer which definitely exists.
                 * 
                 * If it does happen, we just miss this bit out of the menu and
                 * log the message. That'll lead back to here, and the debugging
                 * can begin!
                 */
                log.error("Failed to get layer metadata", e);
            }
        }

        menu.put("children", children);
        return menu.toString(4);
    }

    private JSONArray addVariablesToArray(Set<VariableMetadata> variables, String datasetId) throws WmsLayerNotFoundException {
        JSONArray ret = new JSONArray();
        for (VariableMetadata variable : variables) {
            JSONObject child = new JSONObject();

            String id = variable.getId();
            String layerName = catalogue.getLayerName(datasetId, id);

            child.put("id", layerName);

            WmsLayerMetadata layerMetadata = catalogue.getLayerMetadata(layerName);
            String title = layerMetadata.getTitle();
            child.put("label", title);

            List<StyleDef> supportedStyles = catalogue.getSupportedStyles(variable);
            child.put("plottable", (supportedStyles != null && supportedStyles.size() > 0));

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
            log.error("Layer "+layerName+" doesn't exist - can't get layer details");
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
        
        WmsLayerMetadata layerMetadata;
        try {
            layerMetadata = catalogue.getLayerMetadata(layerName);
        } catch (WmsLayerNotFoundException e1) {
            throw new MetadataException("Layer not found", e1);
        }
        if (dataset == null || variableId == null || layerMetadata == null) {
            log.error("Layer "+layerName+" doesn't exist - can't get layer details");
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

        List<StyleDef> supportedStyles = catalogue.getSupportedStyles(variableMetadata);

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
                } catch (BadTimeFormatException e) {
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
        for (StyleDef supportedStyle : supportedStyles) {
            supportedStylesJson.add(supportedStyle.getStyleName() );
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
                layerDetails.put("startTime", TimeUtils.dateTimeToISO8601(temporalDomain.getExtent().getLow()));
                layerDetails.put("endTime", TimeUtils.dateTimeToISO8601(temporalDomain.getExtent().getHigh()));
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
        } catch (BadTimeFormatException e) {
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

        MapFeatureAndMember featureAndMember;
        try {
            featureAndMember = catalogue.getFeatureAndMemberName(layerNames[0],
                    getMapParams.getPlottingParameters());
        } catch (BadTimeFormatException e) {
            log.error("Bad time format", e);
            throw new MetadataException("Bad time format", e);
        }
        MapFeature mapFeature = featureAndMember.getMapFeature();
        Array2D<Number> values = mapFeature.getValues(featureAndMember.getMember());
        if(values == null) {
            throw new MetadataException("Cannot find min/max - this is not a scalar layer");
        }

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
            } catch (BadTimeFormatException e) {
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
//            Extent<DateTime> extent = temporalDomain.getExtent();
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

    private void getTimeseries(RequestParams params, HttpServletResponse httpServletResponse) throws EdalException {
        String outputFormat = params.getMandatoryString("format");
        if (!"image/png".equals(outputFormat) && !"image/jpeg".equals(outputFormat)
                && !"image/jpg".equals(outputFormat)) {
            throw new InvalidFormatException(outputFormat + " is not a valid output format for a time series plot");
        }
        
        String[] layers = params.getMandatoryString("layers").split(",");

        String[] lonLat = params.getMandatoryString("point").split(" +");
        HorizontalPosition hPos;
        try{
            double lon = Double.parseDouble(lonLat[0]);
            double lat = Double.parseDouble(lonLat[1]);
            hPos = new HorizontalPosition(lon, lat, DefaultGeographicCRS.WGS84);
        } catch (NumberFormatException nfe) {
            log.error("Badly formed co-ordinates for time series", nfe);
            throw new EdalException("POINT is not well-formed");
        }
        
        String timeStr = params.getMandatoryString("time");
        
        List<PointSeriesFeature> pointSeriesFeatures = new ArrayList<PointSeriesFeature>();
        for(String layerName : layers) {
            Dataset dataset = catalogue.getDatasetFromId(layerName);
            if(dataset instanceof GridDataset) {
                GridDataset gridDataset = (GridDataset) dataset;
                String varId = catalogue.getVariableFromId(layerName);
                VariableMetadata variableMetadata = catalogue.getVariableMetadataFromId(layerName);
                TemporalDomain temporalDomain = variableMetadata.getTemporalDomain();
                VerticalDomain verticalDomain = variableMetadata.getVerticalDomain();
                if(temporalDomain == null) {
                    throw new IncorrectDomainException("Variable must have a temporal domain to plot a time series");
                }
                Extent<DateTime> timeRange = TimeUtils.getTimeRangeForString(timeStr, temporalDomain.getChronology());

                VerticalPosition zPos = null;
                String elevationStr = params.getString("elevation");
                if(elevationStr != null && verticalDomain != null) {
                    zPos = new VerticalPosition(Double.parseDouble(elevationStr), verticalDomain.getVerticalCrs());
                }
                
                List<DateTime> axisValues = new ArrayList<DateTime>();
                if(temporalDomain instanceof TimeAxis) {
                    TimeAxis varTimeAxis = (TimeAxis) temporalDomain;
                    for (DateTime time : varTimeAxis.getCoordinateValues()) {
                        if ((time.isAfter(timeRange.getLow()) || time.isEqual(timeRange.getLow()) )
                                && (time.isBefore(timeRange.getHigh()) || time.isEqual(timeRange
                                        .getHigh()))) {
                            axisValues.add(time);
                        }
                    }
                } else {
                    long min = timeRange.getLow().getMillis();
                    long max = timeRange.getHigh().getMillis();
                    for(int i=0; i < AXIS_RESOLUTION; i++) {
                        axisValues.add(new DateTime(min + (max - min) * ((double) i) / AXIS_RESOLUTION));
                    }
                }
                TimeAxis timeAxis = new TimeAxisImpl("Artifical time-axis", axisValues);    
                PointSeriesFeature feature = gridDataset.readTimeSeriesData(CollectionUtils.setOf(varId), hPos, zPos, timeAxis);
                pointSeriesFeatures.add(feature);
            } else {
                throw new UnsupportedOperationException(
                        "Currently only gridded datasets are supported for time series plots");
            }
        }

        int width = params.getPositiveInt("width", 700);
        int height = params.getPositiveInt("height", 600);

        /* Now create the time series plot */
        JFreeChart chart = Charting.createTimeSeriesPlot(pointSeriesFeatures, hPos);

        httpServletResponse.setContentType(outputFormat);
        try {
            if ("image/png".equals(outputFormat)) {
                ChartUtilities.writeChartAsPNG(httpServletResponse.getOutputStream(), chart, width, height);
            } else {
                /* Must be a JPEG */
                ChartUtilities.writeChartAsJPEG(httpServletResponse.getOutputStream(), chart, width, height);
            }
        } catch (IOException e) {
            log.error("Cannot write to output stream", e);
            throw new EdalException("Problem writing data to output stream", e);
        }
    }

    private void getTransect(RequestParams params, HttpServletResponse httpServletResponse) throws EdalException {
        String outputFormat = params.getMandatoryString("format");
        if (!"image/png".equals(outputFormat) && !"image/jpeg".equals(outputFormat)
                && !"image/jpg".equals(outputFormat)) {
            throw new InvalidFormatException(outputFormat + " is not a valid output format for a profile plot");
        }
        String[] layers = params.getMandatoryString("layers").split(",");
        CoordinateReferenceSystem crs = GISUtils.getCrs(params.getMandatoryString("CRS"));
        LineString lineString = new LineString(params.getMandatoryString("linestring"), crs);
        String timeStr = params.getString("time");
        
        String elevationStr = params.getString("elevation");
        Double zValue = null;
        if(elevationStr != null) {
            zValue = Double.parseDouble(elevationStr);
        }
        StringBuilder copyright = new StringBuilder();
        List<TrajectoryFeature> trajectoryFeatures = new ArrayList<TrajectoryFeature>();
        /* Do we also want to plot a vertical section plot? */
        boolean verticalSection = false;
        List<HorizontalPosition> verticalSectionHorizontalPositions = new ArrayList<HorizontalPosition>();
        for(String layerName : layers) {
            Dataset dataset = catalogue.getDatasetFromId(layerName);
            if(dataset instanceof GridDataset) {
                GridDataset gridDataset = (GridDataset) dataset;
                String varId = catalogue.getVariableFromId(layerName);
                String layerCopyright = catalogue.getLayerMetadata(layerName).getCopyright();
                if(layerCopyright != null && !"".equals(layerCopyright)) {
                    copyright.append(layerCopyright);
                    copyright.append('\n');
                }
                
                VariableMetadata metadata = gridDataset.getVariableMetadata(varId);
                VerticalDomain verticalDomain = metadata.getVerticalDomain();
                final VerticalPosition zPos;
                if(zValue != null && verticalDomain != null) {
                    zPos = new VerticalPosition(zValue, verticalDomain.getVerticalCrs());
                } else {
                    zPos = null;
                }
                if(verticalDomain != null && layers.length == 1) {
                    verticalSection = true;
                }
                
                final DateTime time;
                TemporalDomain temporalDomain = metadata.getTemporalDomain();
                if(timeStr != null) {
                    time = TimeUtils.iso8601ToDateTime(timeStr, temporalDomain.getChronology());
                } else {
                    time = null;
                }
                HorizontalDomain hDomain = metadata.getHorizontalDomain();
                final List<HorizontalPosition> transectPoints;
                if(hDomain instanceof HorizontalGrid) {
                    transectPoints = GISUtils.getOptimalTransectPoints(
                            (HorizontalGrid) hDomain, lineString, zPos, time, AXIS_RESOLUTION/10);
                } else {
                    transectPoints = lineString.getPointsOnPath(AXIS_RESOLUTION);
                }
                if(verticalSection) {
                    verticalSectionHorizontalPositions = transectPoints;
                }
                TrajectoryDomain trajectoryDomain = new TrajectoryDomain(new AbstractList<GeoPosition>() {
                    @Override
                    public GeoPosition get(int index) {
                        return new GeoPosition(transectPoints.get(index), zPos, time);
                    }

                    @Override
                    public int size() {
                        return transectPoints.size();
                    }
                });
                
                TrajectoryFeature feature = gridDataset.readTrajectoryData(CollectionUtils.setOf(varId), trajectoryDomain);
                trajectoryFeatures.add(feature);
            } else {
                throw new UnsupportedOperationException(
                        "Currently only gridded datasets are supported for transect plots");
            }
        }
        
        copyright.deleteCharAt(copyright.length() - 1);
        JFreeChart chart = Charting.createTransectPlot(trajectoryFeatures, lineString, false, copyright.toString());

        
        if (verticalSection) {
            /*
             * This can only be true if we have a GridSeriesFeature, so we can
             * cast
             */
            Dataset dataset = catalogue.getDatasetFromId(layers[0]);
            String varId = catalogue.getVariableFromId(layers[0]);
            if(dataset instanceof GridDataset) {
                GridDataset gridDataset = (GridDataset) dataset;
                
                String paletteName = params.getString("palette", ColourPalette.DEFAULT_PALETTE_NAME);
                int numColourBands = params.getPositiveInt("numcolorbands", ColourPalette.MAX_NUM_COLOURS);
                Extent<Float> scaleRange = GetMapStyleParams.getColorScaleRange(params);
                if(scaleRange == null || scaleRange.isEmpty()) {
                    scaleRange = Extents.newExtent(270f, 300f);
                }
                ColourScale colourScale = new ColourScale(scaleRange.getLow(), scaleRange.getHigh(), params.getBoolean("logscale", false));

                String bgColourStr = params.getString("bgcolor", "transparent");
                String amColourStr = params.getString("abovemaxcolor", "0x000000");
                String bmColourStr = params.getString("belowmincolor", "0x000000");
                ColourMap palette = new ColourMap(GraphicsUtils.parseColour(bmColourStr),
                        GraphicsUtils.parseColour(amColourStr),
                        GraphicsUtils.parseColour(bgColourStr), paletteName, numColourBands);
                ColourScheme colourScheme = new PaletteColourScheme(colourScale, palette);
                List<ProfileFeature> profileFeatures = new ArrayList<ProfileFeature>();
                
                VariableMetadata variableMetadata = gridDataset.getVariableMetadata(varId);
                VerticalDomain verticalDomain = variableMetadata.getVerticalDomain();
                VerticalAxis vAxis;
                if(verticalDomain instanceof VerticalAxis) {
                    vAxis = (VerticalAxis) verticalDomain;
                } else {
                    /*
                     * We don't have a valid vertical axis, so create one
                     */
                    List<Double> values = new ArrayList<Double>();
                    double zMin = verticalDomain.getExtent().getLow();
                    double zMax = verticalDomain.getExtent().getHigh();
                    for(int i=0; i<AXIS_RESOLUTION; i++) {
                        values.add(zMin + (zMax - zMin)/AXIS_RESOLUTION);
                    }
                    vAxis = new VerticalAxisImpl("Vertical section axis", values, verticalDomain.getVerticalCrs());
                }
                TemporalDomain temporalDomain = gridDataset.getVariableMetadata(varId).getTemporalDomain();
                DateTime time = null;
                if(timeStr != null) {
                    time = TimeUtils.iso8601ToDateTime(timeStr, temporalDomain.getChronology());
                }
                for (HorizontalPosition pos : verticalSectionHorizontalPositions) {
                    ProfileFeature profileFeature = gridDataset.readProfileData(
                            CollectionUtils.setOf(varId), pos,
                            vAxis, time);
                    profileFeatures.add(profileFeature);
                }
                JFreeChart verticalSectionChart = Charting.createVerticalSectionChart(profileFeatures,
                        lineString, colourScheme, zValue);
                chart = Charting.addVerticalSectionChart(chart, verticalSectionChart);
            } else {
                log.error("Vertical section charts not supported for non-grid datasets");
            }
        }
        int width = params.getPositiveInt("width", 700);
        int height = params.getPositiveInt("height", verticalSection ? 1000 : 600);
        
        httpServletResponse.setContentType(outputFormat);
        try {
            if ("image/png".equals(outputFormat)) {
                ChartUtilities.writeChartAsPNG(httpServletResponse.getOutputStream(), chart, width, height);
            } else {
                /* Must be a JPEG */
                ChartUtilities.writeChartAsJPEG(httpServletResponse.getOutputStream(), chart, width, height);
            }
        } catch (IOException e) {
            log.error("Cannot write to output stream", e);
            throw new EdalException("Problem writing data to output stream", e);
        }
    }

    private void getVerticalProfile(RequestParams params, HttpServletResponse httpServletResponse) throws EdalException {
        String outputFormat = params.getMandatoryString("format");
        if (!"image/png".equals(outputFormat) && !"image/jpeg".equals(outputFormat)
                && !"image/jpg".equals(outputFormat)) {
            throw new InvalidFormatException(outputFormat + " is not a valid output format for a profile plot");
        }
        
        String[] layers = params.getMandatoryString("layers").split(",");

        String[] lonLat = params.getMandatoryString("point").split(" +");
        HorizontalPosition hPos;
        try{
            double lon = Double.parseDouble(lonLat[0]);
            double lat = Double.parseDouble(lonLat[1]);
            hPos = new HorizontalPosition(lon, lat, DefaultGeographicCRS.WGS84);
        } catch (NumberFormatException nfe) {
            log.error("Badly formed co-ordinates for vertical profile", nfe);
            throw new EdalException("POINT is not well-formed");
        }
        
        DateTime time = null;
        String timeStr = params.getString("time");
        List<ProfileFeature> profileFeatures = new ArrayList<ProfileFeature>();
        for(String layerName : layers) {
            Dataset dataset = catalogue.getDatasetFromId(layerName);
            if(dataset instanceof GridDataset) {
                GridDataset gridDataset = (GridDataset) dataset;
                String varId = catalogue.getVariableFromId(layerName);
                VerticalDomain verticalDomain = gridDataset.getVariableMetadata(varId).getVerticalDomain();
                VerticalAxis zAxis;
                if(verticalDomain instanceof VerticalAxis) {
                    zAxis = (VerticalAxis) verticalDomain;
                } else {
                    List<Double> values = new ArrayList<Double>();
                    Double min = verticalDomain.getExtent().getLow();
                    Double max = verticalDomain.getExtent().getHigh();
                    if(min == null || max == null) {
                        log.error("Cannot plot profile for "+layerName+" - vertical domain is not well-defined");
                        continue;
                    }
                    for(int i=0; i < AXIS_RESOLUTION; i++) {
                        values.add(min + (max - min) * ((double) i) / AXIS_RESOLUTION);
                    }
                    TemporalDomain temporalDomain = gridDataset.getVariableMetadata(varId).getTemporalDomain();
                    if(timeStr != null) {
                        time = TimeUtils.iso8601ToDateTime(timeStr, temporalDomain.getChronology());
                    }
                    zAxis = new VerticalAxisImpl("Artificial z-axis", values, verticalDomain.getVerticalCrs());
                }
                ProfileFeature feature = gridDataset.readProfileData(CollectionUtils.setOf(varId), hPos, zAxis, time);
                profileFeatures.add(feature);
            } else {
                throw new UnsupportedOperationException(
                        "Currently only gridded datasets are supported for vertical profile plots");
            }
        }

        int width = params.getPositiveInt("width", 700);
        int height = params.getPositiveInt("height", 600);

        /* Now create the vertical profile plot */
        JFreeChart chart = Charting.createVerticalProfilePlot(profileFeatures, hPos);

        httpServletResponse.setContentType(outputFormat);
        try {
            if ("image/png".equals(outputFormat)) {
                ChartUtilities.writeChartAsPNG(httpServletResponse.getOutputStream(), chart, width, height);
            } else {
                /* Must be a JPEG */
                ChartUtilities.writeChartAsJPEG(httpServletResponse.getOutputStream(), chart, width, height);
            }
        } catch (IOException e) {
            log.error("Cannot write to output stream", e);
            throw new EdalException("Problem writing data to output stream", e);
        }
    }
    
    private void handleWmsException(EdalException wmse, HttpServletResponse httpServletResponse, boolean v130)
            throws IOException {
        VelocityContext context = new VelocityContext();
        EventCartridge ec = new EventCartridge();
        ec.addEventHandler(new EscapeXmlReference());
        ec.attachToContext(context);
        
        context.put("exception", wmse);
        
        Template template;
        if(v130) {
            template = velocityEngine.getTemplate("templates/exception-1.3.0.vm");
        } else {
            template = velocityEngine.getTemplate("templates/exception-1.1.1.vm");
        }
        template.merge(context, httpServletResponse.getWriter());
    }
}
