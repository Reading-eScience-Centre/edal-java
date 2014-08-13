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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.naming.OperationNotSupportedException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.implement.EscapeXmlReference;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.chrono.ISOChronology;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.domain.VerticalDomain;
import uk.ac.rdg.resc.edal.exceptions.BadTimeFormatException;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.exceptions.MetadataException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.feature.PointFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.graphics.Charting;
import uk.ac.rdg.resc.edal.graphics.formats.ImageFormat;
import uk.ac.rdg.resc.edal.graphics.formats.InvalidFormatException;
import uk.ac.rdg.resc.edal.graphics.formats.KmzFormat;
import uk.ac.rdg.resc.edal.graphics.formats.SimpleFormat;
import uk.ac.rdg.resc.edal.graphics.style.ColourScale;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.graphics.style.SegmentColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.util.ColourPalette;
import uk.ac.rdg.resc.edal.graphics.style.util.FeatureCatalogue.FeaturesAndMemberName;
import uk.ac.rdg.resc.edal.graphics.style.util.GraphicsUtils;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;
import uk.ac.rdg.resc.edal.util.PlottingDomainParams;
import uk.ac.rdg.resc.edal.util.TimeUtils;
import uk.ac.rdg.resc.edal.wms.exceptions.CurrentUpdateSequence;
import uk.ac.rdg.resc.edal.wms.exceptions.EdalLayerNotFoundException;
import uk.ac.rdg.resc.edal.wms.exceptions.InvalidUpdateSequence;
import uk.ac.rdg.resc.edal.wms.exceptions.LayerNotQueryableException;
import uk.ac.rdg.resc.edal.wms.util.StyleDef;
import uk.ac.rdg.resc.edal.wms.util.WmsUtils;

/**
 * The main servlet for all WMS operations, including extended behaviour. This
 * servlet can be used as-is by defining it in the usual way in a web.xml file,
 * and injecting a {@link WmsCatalogue} object by calling the
 * {@link WmsServlet#setCatalogue(WmsCatalogue)}.
 * 
 * If the {@link WmsCatalogue} is not set, behaviour is undefined. It'll fail in
 * all sorts of ways - nothing will work properly.
 * 
 * The recommended usage is to either subclass this servlet and set a valid
 * instance of a {@link WmsCatalogue} in the constructor/init method or to use
 * Spring to do the wiring for you.
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
            "EPSG:5041", // North Polar stereographic
            "EPSG:5042", // South Polar stereographic
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
        velocityEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                "org.apache.velocity.runtime.log.Log4JLogChute");
        velocityEngine.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
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
                 * No version supplied, we'll return the exception in 1.3.0
                 * format
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

    protected void dispatchWmsRequest(String request, RequestParams params,
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
            if (!StringUtils.isBlank(url)) {
                /*
                 * We need to proxy the request if it is on a different server
                 */
                WmsUtils.proxyRequest(url, httpServletRequest, httpServletResponse);
                return;
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
            // } else if (request.equals("GetVerticalSection")) {
            // getVerticalSection(params, httpServletResponse);
        } else {
            throw new OperationNotSupportedException(request);
        }
    }

    private void getMap(RequestParams params, HttpServletResponse httpServletResponse)
            throws EdalException {
        GetMapParameters getMapParams = new GetMapParameters(params, catalogue);

        PlottingDomainParams plottingParameters = getMapParams.getPlottingDomainParameters();
        GetMapStyleParams styleParameters = getMapParams.getStyleParameters();

        if (getMapParams.getImageFormat() instanceof KmzFormat) {
            if (!GISUtils
                    .isWgs84LonLat(plottingParameters.getBbox().getCoordinateReferenceSystem())) {
                throw new EdalException("KMZ files can only be generated from WGS84 projections");
            }
        }

        /*
         * Do some checks on the style parameters.
         * 
         * These only apply to non-XML styles. XML ones are more complex to
         * handle.
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
            if (styleParameters.getNumLayers() > catalogue.getServerInfo()
                    .getMaxSimultaneousLayers()) {
                throw new EdalException("Only "
                        + catalogue.getServerInfo().getMaxSimultaneousLayers()
                        + " layer(s) can be plotted at once");
            }
        }

        /*
         * Check the dimensions of the image
         */
        if (plottingParameters.getHeight() > catalogue.getServerInfo().getMaxImageHeight()
                || plottingParameters.getWidth() > catalogue.getServerInfo().getMaxImageWidth()) {
            throw new EdalException("Requested image size exceeds the maximum of "
                    + catalogue.getServerInfo().getMaxImageWidth() + "x"
                    + catalogue.getServerInfo().getMaxImageHeight());
        }

        MapImage imageGenerator = styleParameters.getImageGenerator(catalogue);

        List<BufferedImage> frames;
        if (!getMapParams.isAnimation()) {
            frames = Arrays.asList(imageGenerator.drawImage(plottingParameters, catalogue));
        } else {
            frames = new ArrayList<>();
            for (DateTime timeStep : getMapParams.getAnimationTimesteps()) {
                PlottingDomainParams timestepParameters = new PlottingDomainParams(
                        plottingParameters.getWidth(), plottingParameters.getHeight(),
                        plottingParameters.getBbox(), plottingParameters.getZExtent(), null,
                        plottingParameters.getTargetHorizontalPosition(),
                        plottingParameters.getTargetZ(), timeStep);
                frames.add(imageGenerator.drawImage(timestepParameters, catalogue));
            }
        }

        ImageFormat imageFormat = getMapParams.getImageFormat();
        try {
            ServletOutputStream outputStream = httpServletResponse.getOutputStream();
            if (imageFormat instanceof SimpleFormat) {
                /*
                 * We have a normal image format
                 */
                SimpleFormat simpleFormat = (SimpleFormat) getMapParams.getImageFormat();
                simpleFormat.writeImage(frames, outputStream, null);
            } else {
                /*
                 * We have KML (or another image format which needs additional
                 * information)
                 */
                String[] layerNames = styleParameters.getLayerNames();
                if (layerNames.length > 1) {
                    throw new EdalException("Exactly 1 layer must be requested for KML ("
                            + layerNames.length + " have been supplied)");
                }
                String layerName = layerNames[0];
                if (imageFormat instanceof KmzFormat) {
                    /*
                     * If this is a KMZ file, give it a sensible filename
                     */
                    httpServletResponse.setHeader("Content-Disposition", "inline; filename="
                            + layerName.replaceAll("/", "-") + ".kmz");
                }
                WmsLayerMetadata layerMetadata = catalogue.getLayerMetadata(layerName);
                String name = layerMetadata.getTitle();
                String description = layerMetadata.getDescription();
                String zValue = plottingParameters.getTargetZ() == null ? null : plottingParameters
                        .getTargetZ().toString();
                List<DateTime> tValues = Arrays.asList(plottingParameters.getTargetT());
                BufferedImage legend = imageGenerator.getLegend(200);
                GeographicBoundingBox gbbox = GISUtils.toGeographicBoundingBox(plottingParameters
                        .getBbox());
                imageFormat.writeImage(frames, outputStream, name, description, gbbox, tValues,
                        zValue, legend, 24);
            }
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
        /*
         * We only advertise text/xml as a GetCapabilities format. The spec says
         * we can return text/xml for unknown formats, so we don't even need to
         * bother retrieving the requested format
         */

        /*
         * Now handle the UPDATESEQUENCE param as per 7.2.3.5 of the WMS spec
         */
        String updateSeqStr = params.getString("updatesequence");
        if (updateSeqStr != null) {
            DateTime updateSequence;
            try {
                updateSequence = TimeUtils.iso8601ToDateTime(updateSeqStr,
                        ISOChronology.getInstanceUTC());
            } catch (IllegalArgumentException iae) {
                throw new InvalidUpdateSequence(updateSeqStr + " is not a valid ISO date-time");
            }
            /*
             * We use isEqual(), which compares dates based on millisecond
             * values only, because we know that the calendar system will be the
             * same in each case (ISO). Comparisons using equals() may return
             * false because updateSequence is read using UTC, whereas
             * lastUpdate is created in the server's time zone, meaning that the
             * Chronologies are different.
             */
            if (updateSequence.isEqual(catalogue.getServerLastUpdate())) {
                throw new CurrentUpdateSequence(updateSeqStr);
            } else if (updateSequence.isAfter(catalogue.getServerLastUpdate())) {
                throw new InvalidUpdateSequence(updateSeqStr
                        + " is later than the current server updatesequence value");
            }
        }

        String wmsVersion = params.getString("version", "1.3.0");
        Template template;
        if ("1.1.1".equals(wmsVersion)) {
            template = velocityEngine.getTemplate("templates/capabilities-1.1.1.vm");
        } else {
            template = velocityEngine.getTemplate("templates/capabilities-1.3.0.vm");
        }

        /*
         * The DATASET parameter is an optional parameter that allows a
         * Capabilities document to be generated for a single dataset only
         */
        String datasetId = params.getString("dataset");

        Collection<Dataset> datasets;
        if (datasetId == null || "".equals(datasetId.trim())) {
            /*
             * No specific dataset has been chosen so we create a Capabilities
             * document including every dataset. First we check to see that the
             * system admin has allowed us to create a global Capabilities doc
             * (this can be VERY large)
             */
            if (catalogue.allowsGlobalCapabilities()) {
                datasets = catalogue.getAllDatasets();
            } else {
                throw new EdalException("Cannot create a Capabilities document "
                        + "that includes all datasets on this server. "
                        + "You must specify a dataset identifier with &amp;DATASET=");
            }
        } else {
            Dataset ds = catalogue.getDatasetFromId(datasetId);
            if (ds == null) {
                throw new EdalException("There is no dataset with ID " + datasetId);
            }
            datasets = new ArrayList<Dataset>();
            datasets.add(ds);
        }

        VelocityContext context = new VelocityContext();
        EventCartridge ec = new EventCartridge();
        ec.addEventHandler(new EscapeXmlReference());
        ec.attachToContext(context);
        context.put("baseUrl", baseUrl);
        context.put("catalogue", catalogue);
        context.put("datasets", datasets);
        context.put("supportedImageFormats", ImageFormat.getSupportedMimeTypes());
        context.put("supportedFeatureInfoFormats", new String[] { FEATURE_INFO_PNG_FORMAT,
                FEATURE_INFO_XML_FORMAT });
        context.put("supportedCrsCodes", SUPPORTED_CRS_CODES);
        context.put("GISUtils", GISUtils.class);
        context.put("TimeUtils", TimeUtils.class);
        context.put("WmsUtils", WmsUtils.class);
        context.put("verbose", params.getBoolean("verbose", false));
        context.put("availablePalettes", ColourPalette.getPredefinedPalettes());

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
        GetFeatureInfoParameters featureInfoParameters = new GetFeatureInfoParameters(params,
                catalogue);
        PlottingDomainParams plottingParameters = featureInfoParameters
                .getPlottingDomainParameters();
        final HorizontalPosition position = featureInfoParameters.getClickedPosition();

        String[] layerNames = featureInfoParameters.getLayerNames();
        /*
         * List of FeatureInfoPoints to be sent to featureInfo velocity template
         */
        List<FeatureInfoPoint> featureInfos = new ArrayList<FeatureInfoPoint>();
        /*
         * Loop over all requested layers
         */
        for (String layerName : layerNames) {
            WmsLayerMetadata layerMetadata = catalogue.getLayerMetadata(layerName);
            if (layerMetadata.isDisabled()) {
                throw new EdalLayerNotFoundException("The layer " + layerName
                        + " is not enabled on this server");
            }
            if (!layerMetadata.isQueryable()) {
                throw new LayerNotQueryableException("The layer " + layerName + " is not queryable");
            }
            Dataset dataset = catalogue.getDatasetFromLayerName(layerName);
            String variableId = catalogue.getVariableFromId(layerName);
            VariableMetadata metadata = catalogue.getVariableMetadataFromId(layerName);
            Set<VariableMetadata> children = metadata.getChildren();
            /*
             * Extract the map features. Because of the way
             * GetFeatureInfoParameters works, features are searched for in a
             * 9-pixel box surrounding the clicked position on the map
             */
            Collection<? extends DiscreteFeature<?, ?>> mapFeatures = dataset.extractMapFeatures(
                    CollectionUtils.setOf(variableId), plottingParameters);

            /*
             * We only want to return a layer name if there are more than one
             */
            String layerNameToSave = layerNames.length < 2 ? null : layerName;
            for (DiscreteFeature<?, ?> feature : mapFeatures) {
                if (metadata.isScalar()) {
                    /*
                     * If we have a scalar layer, add the value for it first,
                     * using the feature name to identify values.
                     */
                    FeatureInfoPoint featurePoint = getFeatureInfoValuesFromFeature(feature,
                            variableId, plottingParameters, layerNameToSave, feature.getName());
                    if (featurePoint != null) {
                        featureInfos.add(featurePoint);
                    }
                }
                for (VariableMetadata child : children) {
                    /*
                     * Now add the values for every child layer, using the child
                     * variable IDs to identify values.
                     * 
                     * TODO perhaps in cases where we have child layers for
                     * multiple features we will want to use a combination of
                     * feature ID + child variable ID.
                     */
                    String name = catalogue.getLayerMetadata(
                            catalogue.getLayerName(dataset.getId(), child.getId())).getTitle();
                    FeatureInfoPoint featurePoint = getFeatureInfoValuesFromFeature(feature,
                            child.getId(), plottingParameters, layerNameToSave, name);
                    if (featurePoint != null) {
                        featureInfos.add(featurePoint);
                    }
                }
            }
        }

        /*
         * Sort the feature info according to how far each position is from the
         * clicked position
         */
        Collections.sort(featureInfos, new Comparator<FeatureInfoPoint>() {
            @Override
            public int compare(FeatureInfoPoint o1, FeatureInfoPoint o2) {
                return Double.compare(GISUtils.getDistSquared(o1.getPosition(), position),
                        GISUtils.getDistSquared(o2.getPosition(), position));
            }
        });

        /*
         * Trim the list down to the number of requested features
         */
        while (featureInfos.size() > featureInfoParameters.getFeatureCount()) {
            featureInfos.remove(featureInfos.size() - 1);
        }

        /*
         * Now render the output XML and send to the output stream
         */
        Template template = velocityEngine.getTemplate("templates/featureInfo.vm");
        VelocityContext context = new VelocityContext();
        context.put("position", position);
        context.put("featureInfo", featureInfos);
        try {
            template.merge(context, httpServletResponse.getWriter());
        } catch (Exception e) {
            log.error("Problem writing FeatureInfo XML", e);
        }
    }

    /**
     * Extracts the target value from a feature
     * 
     * @param feature
     *            The feature to extract the value from
     * @param variableId
     *            The name of the variable within the feature
     * @param plottingParameters
     *            The {@link PlottingDomainParams} defining the target value to
     *            extract
     * @param layerName
     *            The layer name to add to the {@link FeatureInfoPoint}
     * @param featureName
     *            The feature name to add to the {@link FeatureInfoPoint}
     * @return The extracted value and corresponding information collected as a
     *         {@link FeatureInfoPoint}
     */
    private FeatureInfoPoint getFeatureInfoValuesFromFeature(DiscreteFeature<?, ?> feature,
            String variableId, PlottingDomainParams plottingParameters, String layerName,
            String featureName) {
        Number value = null;
        HorizontalPosition position = null;
        String timeStr = null;
        if (feature instanceof MapFeature) {
            MapFeature mapFeature = (MapFeature) feature;
            GridCoordinates2D pointIndex = mapFeature.getDomain().findIndexOf(
                    plottingParameters.getTargetHorizontalPosition());
            if (pointIndex != null) {
                position = mapFeature.getDomain().getDomainObjects()
                        .get(pointIndex.getY(), pointIndex.getX()).getCentre();
                value = mapFeature.getValues(variableId).get(pointIndex.getY(), pointIndex.getX());
                if (mapFeature.getDomain().getTime() != null) {
                    timeStr = TimeUtils.dateTimeToISO8601(mapFeature.getDomain().getTime());
                }
            }
        } else if (feature instanceof PointFeature) {
            PointFeature pointFeature = (PointFeature) feature;
            value = pointFeature.getValue(variableId);
            position = pointFeature.getHorizontalPosition();
            if (pointFeature.getGeoPosition().getTime() != null) {
                timeStr = TimeUtils.dateTimeToISO8601(pointFeature.getGeoPosition().getTime());
            }
        } else if (feature instanceof ProfileFeature) {
            ProfileFeature profileFeature = (ProfileFeature) feature;
            int index = GISUtils.getIndexOfClosestElevationTo(plottingParameters.getTargetZ(),
                    profileFeature.getDomain());
            if (index >= 0) {
                value = profileFeature.getValues(variableId).get(index);
                position = profileFeature.getHorizontalPosition();
                if (profileFeature.getTime() != null) {
                    timeStr = TimeUtils.dateTimeToISO8601(profileFeature.getTime());
                }
            }
        } else if (feature instanceof PointSeriesFeature) {
            PointSeriesFeature pointSeriesFeature = (PointSeriesFeature) feature;
            int index = GISUtils.getIndexOfClosestTimeTo(plottingParameters.getTargetT(),
                    pointSeriesFeature.getDomain());
            if (index >= 0) {
                position = pointSeriesFeature.getHorizontalPosition();
                value = pointSeriesFeature.getValues(variableId).get(index);
                timeStr = TimeUtils.dateTimeToISO8601(pointSeriesFeature.getDomain()
                        .getCoordinateValue(index));
            }
        }
        if (value != null) {
            return new FeatureInfoPoint(layerName, featureName, position, timeStr, value,
                    feature.getFeatureProperties());
        } else {
            return null;
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
                log.error("Problem writing metadata to output stream", e);
                throw new MetadataException("Problem writing JSON to output stream", e);
            }
        } else {
            throw new MetadataException("Invalid value for ITEM parameter");
        }
    }

    private String showMenu(RequestParams params) {
        JSONObject menu = new JSONObject();
        menu.put("label", catalogue.getServerInfo().getName());
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
            } catch (EdalLayerNotFoundException e) {
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

    private JSONArray addVariablesToArray(Set<VariableMetadata> variables, String datasetId)
            throws EdalLayerNotFoundException {
        JSONArray ret = new JSONArray();
        for (VariableMetadata variable : variables) {
            String id = variable.getId();
            String layerName = catalogue.getLayerName(datasetId, id);
            WmsLayerMetadata layerMetadata = catalogue.getLayerMetadata(layerName);
            if (layerMetadata.isDisabled()) {
                continue;
            }

            JSONObject child = new JSONObject();

            child.put("id", layerName);

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
            log.error("Layer " + layerName + " doesn't exist - can't get layer details");
            throw new MetadataException("Must supply a LAYERNAME parameter to get layer details");
        }
        String requestedTime = params.getString("time");

        Dataset dataset;
        String variableId;
        try {
            dataset = catalogue.getDatasetFromLayerName(layerName);
            variableId = catalogue.getVariableFromId(layerName);
        } catch (EdalLayerNotFoundException e) {
            throw new MetadataException("The layer " + layerName + " does not exist", e);
        }

        WmsLayerMetadata layerMetadata;
        try {
            layerMetadata = catalogue.getLayerMetadata(layerName);
            if (layerMetadata.isDisabled()) {
                throw new EdalLayerNotFoundException("The layer " + layerName
                        + " is not enabled on this server");
            }
        } catch (EdalLayerNotFoundException e1) {
            throw new MetadataException("Layer not found", e1);
        }
        if (dataset == null || variableId == null || layerMetadata == null) {
            log.error("Layer " + layerName + " doesn't exist - can't get layer details");
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
        String aboveMaxColour = GraphicsUtils.colourToString(layerMetadata.getAboveMaxColour());
        String belowMinColour = GraphicsUtils.colourToString(layerMetadata.getBelowMinColour());
        String noDataColour = GraphicsUtils.colourToString(layerMetadata.getNoDataColour());

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
            supportedStylesJson.add(supportedStyle.getStyleName());
        }
        layerDetails.put("supportedStyles", supportedStylesJson);

        if (verticalDomain != null) {
            layerDetails.put("continuousZ", !discreteZ);
            JSONObject zAxisJson = new JSONObject();
            zAxisJson.put("units", verticalDomain.getVerticalCrs().getUnits());
            zAxisJson.put("positive", verticalDomain.getVerticalCrs().isPositiveUpwards());
            if (verticalDomain instanceof VerticalAxis) {
                /*
                 * We have discrete vertical axis values
                 */
                VerticalAxis verticalAxis = (VerticalAxis) verticalDomain;
                JSONArray zValuesJson = new JSONArray();
                for (Double z : verticalAxis.getCoordinateValues()) {
                    zValuesJson.add(z);
                }
                zAxisJson.put("values", zValuesJson);
            } else {
                if (!dataset.getFeatureType(variableId).isAssignableFrom(ProfileFeature.class)) {
                    /*
                     * We don't have profile features. Just supply a start and
                     * end elevation. The client can split this however it
                     * wants. Usually this will be a naive split into x levels
                     */
                    zAxisJson.put("startZ", verticalDomain.getExtent().getLow());
                    zAxisJson.put("endZ", verticalDomain.getExtent().getHigh());
                } else {
                    /*
                     * We have profile features. Try and calculate a rough
                     * distribution of depths by reading a sample. Then we can
                     * generate more sensible depth values which
                     * increase/decrease as required.
                     * 
                     * Note that we could do this for any type of feature where
                     * the dataset has a continuous z-domain, but unless each
                     * feature has a discrete vertical domain, we'd need to read
                     * a lot more of them.
                     */

                    /*
                     * First read a sample of depth axis values
                     */
                    Set<String> featureIds = dataset.getFeatureIds();
                    Iterator<String> iterator = featureIds.iterator();
                    List<Double> depthValues = new ArrayList<>();
                    /*
                     * Make sure that the end points are included
                     */
                    depthValues.add(verticalDomain.getExtent().getLow());
                    depthValues.add(verticalDomain.getExtent().getHigh());

                    for (int nFeatures = 0; nFeatures < 50; nFeatures++) {
                        /*
                         * Read up to 50 features
                         */
                        if (iterator.hasNext()) {
                            try {
                                ProfileFeature feature = (ProfileFeature) dataset
                                        .readFeature(iterator.next());
                                if (feature != null) {
                                    depthValues.addAll(feature.getDomain().getCoordinateValues());
                                }
                            } catch (DataReadingException e) {
                                log.error("Problem reading profile feature to test depth levels", e);
                            }
                        } else {
                            break;
                        }
                    }
                    Collections.sort(depthValues);
                    /*
                     * We now have a sorted list of axis values for (up to) 50
                     * features.
                     */
                    if (Math.abs(depthValues.get(0) - depthValues.get(1)) > Math.abs(depthValues
                            .get(depthValues.size() - 1) - depthValues.get(depthValues.size() - 2))) {
                        /*
                         * If the widest increments are at the end of the depth
                         * values list, this will not work, so we just use the
                         * start/end method
                         */
                        zAxisJson.put("startZ", verticalDomain.getExtent().getLow());
                        zAxisJson.put("endZ", verticalDomain.getExtent().getHigh());
                    } else {
                        /*
                         * Next we create a stack of delta-elevation values
                         * which we want to use. These are all round numbers
                         * which will make the axis values nicely human-readable
                         * in the client.
                         * 
                         * Note that because this is a stack, 0.001 will be on
                         * top after the values have been added.
                         * 
                         * We will pop these out until we reach a suitable size.
                         * From that point our elevation values will get further
                         * apart.
                         * 
                         * The aim is to get something like:
                         * 0,10,20,30,40,50,100,200,300,800,1300,2300
                         */

                        Stack<Double> deltas = new Stack<>();
                        deltas.addAll(Arrays.asList(new Double[] { 10000.0, 5000.0, 1000.0, 500.0,
                                200.0, 100.0, 50.0, 20.0, 10.0, 5.0, 2.0, 1.0, 0.5, 0.1, 0.05,
                                0.01, 0.005, 0.001 }));

                        /*
                         * The first level should be the minimum of the extent,
                         * since this was explicitly added to depthValues
                         */
                        List<Double> levels = new ArrayList<>();
                        double lastDeltaStart = depthValues.get(0);
                        levels.add(lastDeltaStart);
                        double delta = 0.0;
                        double lastDelta = 0.0;

                        int nLevels = 25;
                        /*
                         * Split the elevation values into 25 levels.
                         * 
                         * We could just use these elevation values. We'd get a
                         * nice distribution of levels, but they'd be horrible
                         * numbers. So now we get complicated instead...
                         */
                        for (int i = depthValues.size() / nLevels; i < depthValues.size(); i += depthValues
                                .size() / nLevels) {
                            /*
                             * With these nLevels levels, what is the difference
                             * between the two we're considering?
                             */
                            Double lastDeltaEnd = depthValues.get(i);
                            Double testDelta = lastDeltaEnd - lastDeltaStart;

                            /*
                             * Find a nice delta value which is close to this.
                             */
                            if (!deltas.empty()) {
                                while (testDelta > delta) {
                                    lastDelta = delta;
                                    delta = deltas.pop();
                                    /*
                                     * We have a new delta. Keep using the old
                                     * delta until we get a value which is a
                                     * multiple of the current delta. These
                                     * values are much more pleasing (e.g. you
                                     * don't get 110, 210, 310, etc)
                                     */
                                    while (levels.get(levels.size() - 1) % delta != 0) {
                                        levels.add(levels.get(levels.size() - 1) + lastDelta);
                                    }
                                }
                            }

                            /*
                             * Now add levels with this delta until we reach the
                             * next of the 25 levels
                             */
                            while (levels.get(levels.size() - 1) < lastDeltaEnd) {
                                levels.add(levels.get(levels.size() - 1) + delta);
                            }
                            lastDeltaStart = lastDeltaEnd;
                        }
                        /*
                         * Now add the final levels. Keep the final delta value
                         * and keep going until we have exceeded the maximum
                         * value of the extent.
                         */
                        double finalLevels = levels.get(levels.size() - 1);
                        while (finalLevels < verticalDomain.getExtent().getHigh()) {
                            finalLevels += delta;
                            levels.add(finalLevels);
                        }

                        /*
                         * Now we have a nice set of values, serialise them to
                         * JSON.
                         * 
                         * Thanks for reading, and if you're trying to change
                         * this code, I'm sorry. Maybe you should start from
                         * scratch? Or maybe it's simpler than I think and you
                         * understand it perfectly.
                         */
                        JSONArray zValuesJson = new JSONArray();
                        for (Double level : levels) {
                            zValuesJson.add(level);
                        }
                        zAxisJson.put("values", zValuesJson);
                    }
                }

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
                layerDetails.put("startTime",
                        TimeUtils.dateTimeToISO8601(temporalDomain.getExtent().getLow()));
                layerDetails.put("endTime",
                        TimeUtils.dateTimeToISO8601(temporalDomain.getExtent().getHigh()));
            }
            layerDetails.put("timeAxisUnits",
                    WmsUtils.getTimeAxisUnits(temporalDomain.getChronology()));
        }

        /*
         * Set the supported plot types
         */
        boolean timeseries = false;
        boolean profiles = false;
        boolean transects = false;
        Class<? extends DiscreteFeature<?, ?>> underlyingFeatureType = dataset
                .getFeatureType(variableId);
        if (GridFeature.class.isAssignableFrom(underlyingFeatureType)) {
            if (temporalDomain != null) {
                timeseries = true;
            }
            if (verticalDomain != null) {
                profiles = true;
            }
            transects = true;
        } else if (ProfileFeature.class.isAssignableFrom(underlyingFeatureType)) {
            if (verticalDomain != null) {
                profiles = true;
            }
        } else if (PointSeriesFeature.class.isAssignableFrom(underlyingFeatureType)) {
            if (temporalDomain != null) {
                timeseries = true;
            }
        }
        layerDetails.put("supportsTimeseries", timeseries);
        layerDetails.put("supportsProfiles", profiles);
        layerDetails.put("supportsTransects", transects);

        layerDetails.put("nearestTimeIso", TimeUtils.dateTimeToISO8601(nearestTime));
        layerDetails.put("moreInfo", moreInfo);
        layerDetails.put("copyright", copyright);
        JSONArray supportedPalettesJson = new JSONArray();
        for (String supportedPalette : supportedPalettes) {
            supportedPalettesJson.add(supportedPalette);
        }
        layerDetails.put("palettes", supportedPalettesJson);
        layerDetails.put("defaultPalette", defaultPalette);
        layerDetails.put("aboveMaxColor", aboveMaxColour);
        layerDetails.put("belowMinColor", belowMinColour);
        layerDetails.put("noDataColor", noDataColour);
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

        Dataset dataset;
        String variableId;
        try {
            dataset = catalogue.getDatasetFromLayerName(layerName);
            variableId = catalogue.getVariableFromId(layerName);
            if (catalogue.getLayerMetadata(layerName).isDisabled()) {
                throw new EdalLayerNotFoundException("The layer " + layerName
                        + " is not enabled on this server");
            }
        } catch (EdalLayerNotFoundException e) {
            throw new MetadataException("The layer " + layerName + " does not exist", e);
        }
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
        JSONObject minmax = new JSONObject();
        GetMapParameters getMapParams;
        try {
            getMapParams = new GetMapParameters(params, catalogue);
        } catch (EdalException e) {
            e.printStackTrace();
            throw new MetadataException("Problem parsing parameters", e);
        }

        String[] layerNames = getMapParams.getStyleParameters().getLayerNames();
        String[] styleNames = getMapParams.getStyleParameters().getStyleNames();
        if (layerNames.length != 1 || styleNames.length > 1) {
            /*
             * TODO Perhaps relax this restriction and return min/max with layer
             * IDs?
             */
            throw new MetadataException("Can only find min/max for exactly one layer at a time");
        }

        VariableMetadata variableMetadata;
        String datasetId;
        try {
            variableMetadata = catalogue.getVariableMetadataFromId(layerNames[0]);
            datasetId = catalogue.getDatasetFromLayerName(layerNames[0]).getId();
            if (catalogue.getLayerMetadata(layerNames[0]).isDisabled()) {
                throw new EdalLayerNotFoundException("The layer " + layerNames[0]
                        + " is not enabled on this server");
            }
        } catch (EdalLayerNotFoundException e) {
            throw new MetadataException("Layer " + layerNames[0] + " not found on this server", e);
        }

        /*
         * Find out which layer the scaling is being applied to
         */
        String layerName;

        /*
         * First get the style which is applied to this layer
         */
        StyleDef style = null;
        if (styleNames != null && styleNames.length > 0) {
            /*
             * Specified as a URL parameter
             */
            String styleName = styleNames[0];
            style = catalogue.getStyleDefinitionByName(styleName);
            if (style == null) {
                throw new MetadataException("Cannot find min-max for this layer.  The style "
                        + styleName + " is not supported.");
            }
        } else {
            /*
             * The default style
             */
            List<StyleDef> supportedStyles = catalogue.getSupportedStyles(variableMetadata);
            for (StyleDef supportedStyle : supportedStyles) {
                if (supportedStyle.getStyleName().startsWith("default")) {
                    style = supportedStyle;
                }
            }
            if (style == null) {
                throw new MetadataException(
                        "Cannot find min-max for this layer.  No default styles are supported.");
            }
        }

        /*
         * Now find which layer the scale is being applied to
         */
        if (style.getScaledLayerRole() == null) {
            /*
             * No layer has scaling - we can return anything
             */
            minmax.put("min", 0);
            minmax.put("max", 100);
            return minmax.toString();
        } else if ("".equals(style.getScaledLayerRole())) {
            /*
             * The named (possibly parent) layer is scaled.
             */
            layerName = layerNames[0];
        } else {
            /*
             * A child layer is being scaled. Get the WMS layer name
             * corresponding to this child variable
             */
            String variableId = variableMetadata.getChildWithRole(style.getScaledLayerRole())
                    .getId();
            layerName = catalogue.getLayerName(datasetId, variableId);
        }

        /*
         * Now read the required features
         */
        FeaturesAndMemberName featuresAndMember;
        try {
            featuresAndMember = catalogue.getFeaturesForLayer(layerName,
                    getMapParams.getPlottingDomainParameters());
        } catch (EdalException e) {
            log.error("Bad layer name", e);
            throw new MetadataException("Problem reading data", e);
        }

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        Collection<? extends DiscreteFeature<?, ?>> features = featuresAndMember.getFeatures();
        for (DiscreteFeature<?, ?> f : features) {
            if (f instanceof MapFeature) {
                /*
                 * We want to look at all values of the grid feature.
                 */
                Array<Number> values = f.getValues(featuresAndMember.getMember());
                if (values == null) {
                    continue;
                }
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
            } else if (f instanceof PointFeature) {
                PointFeature pointFeature = (PointFeature) f;
                Number value = pointFeature.getValues(featuresAndMember.getMember()).get(0);
                if (value != null) {
                    if (value.doubleValue() > max) {
                        max = value.doubleValue();
                    }
                    if (value.doubleValue() < min) {
                        min = value.doubleValue();
                    }
                }
            } else {
                /*
                 * Would handle other feature types here.
                 */
            }
        }

        if (min == Double.MAX_VALUE || max == -Double.MAX_VALUE) {
            throw new MetadataException("No data in this area - cannot calculate min/max");
        }

        /*
         * No variation in scale.
         */
        if (min == max) {
            if (min == 0.0) {
                min = -0.5;
                max = 0.5;
            } else {
                min *= 0.95;
                max *= 1.05;
            }
        }

        minmax.put("min", min);
        minmax.put("max", max);

        return minmax.toString();
    }

    private String showAnimationTimesteps(RequestParams params) throws MetadataException {
        String layerName = params.getString("layerName");
        if (layerName == null) {
            throw new MetadataException(
                    "Must supply a LAYERNAME parameter to get animation timesteps");
        }

        Dataset dataset;
        String variableId;
        try {
            dataset = catalogue.getDatasetFromLayerName(layerName);
            variableId = catalogue.getVariableFromId(layerName);
            if (catalogue.getLayerMetadata(layerName).isDisabled()) {
                throw new EdalLayerNotFoundException("The layer " + layerName
                        + " is not enabled on this server");
            }
        } catch (EdalLayerNotFoundException e) {
            throw new MetadataException("The layer " + layerName + " does not exist", e);
        }
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
            // Extent<DateTime> extent = temporalDomain.getExtent();
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
        if ("default".equals(paletteName)) {
            paletteName = ColourPalette.DEFAULT_PALETTE_NAME;
        }

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
            SegmentColourScheme colourScheme = new SegmentColourScheme(new ColourScale(
                    Extents.newExtent(0f, 1f), false), Color.black, Color.black, Color.black,
                    paletteName, numColourBands);
            legend = colourScheme.getScaleBar(width, height, 0.0f, vertical, false, null, null);
        } else {
            /*
             * We're creating a legend with supporting text so we need to know
             * the colour scale range and the layer in question
             */
            try {
                GetMapParameters getMapParameters = new GetMapParameters(params, catalogue);
                legend = getMapParameters.getStyleParameters().getImageGenerator(catalogue)
                        .getLegend(200);
            } catch (Exception e) {
                throw new MetadataException(
                        "A full set of GetMap parameters must be provided to generate a full legend.  You can set COLORBARONLY=true to just generate a colour bar");
            }
        }
        httpServletResponse.setContentType("image/png");
        try {
            ImageIO.write(legend, "png", httpServletResponse.getOutputStream());
        } catch (IOException e) {
            log.error("Problem writing legend graphic to output stream", e);
            throw new EdalException("Unable to write legend graphic to output stream", e);
        }
    }

    private void getTimeseries(RequestParams params, HttpServletResponse httpServletResponse)
            throws EdalException {
        GetFeatureInfoParameters featureInfoParameters = new GetFeatureInfoParameters(params,
                catalogue);
        PlottingDomainParams plottingParameters = featureInfoParameters
                .getPlottingDomainParameters();
        final HorizontalPosition position = featureInfoParameters.getClickedPosition();

        String[] layerNames = featureInfoParameters.getLayerNames();
        String outputFormat = featureInfoParameters.getInfoFormat();
        if (!"image/png".equalsIgnoreCase(outputFormat)
                && !"image/jpeg".equalsIgnoreCase(outputFormat)
                && !"image/jpg".equalsIgnoreCase(outputFormat)) {
            throw new InvalidFormatException(outputFormat
                    + " is not a valid output format for a profile plot");
        }
        /*
         * Loop over all requested layers
         */
        List<PointSeriesFeature> pointSeriesFeatures = new ArrayList<PointSeriesFeature>();
        for (String layerName : layerNames) {
            Dataset dataset = catalogue.getDatasetFromLayerName(layerName);
            String variableId = catalogue.getVariableFromId(layerName);
            pointSeriesFeatures.addAll(dataset.extractTimeseriesFeatures(
                    CollectionUtils.setOf(variableId), plottingParameters));
        }

        while (pointSeriesFeatures.size() > featureInfoParameters.getFeatureCount()) {
            pointSeriesFeatures.remove(pointSeriesFeatures.size() - 1);
        }

        int width = 700;
        int height = 600;

        /* Now create the vertical profile plot */
        JFreeChart chart = Charting.createTimeSeriesPlot(pointSeriesFeatures, position);

        httpServletResponse.setContentType(outputFormat);
        try {
            if ("image/png".equals(outputFormat)) {
                ChartUtilities.writeChartAsPNG(httpServletResponse.getOutputStream(), chart, width,
                        height);
            } else {
                /* Must be a JPEG */
                ChartUtilities.writeChartAsJPEG(httpServletResponse.getOutputStream(), chart,
                        width, height);
            }
        } catch (IOException e) {
            log.error("Cannot write to output stream", e);
            throw new EdalException("Problem writing data to output stream", e);
        }
    }

    private void getTransect(RequestParams params, HttpServletResponse httpServletResponse)
            throws EdalException {
        // String outputFormat = params.getMandatoryString("format");
        // if (!"image/png".equals(outputFormat) &&
        // !"image/jpeg".equals(outputFormat)
        // && !"image/jpg".equals(outputFormat)) {
        // throw new InvalidFormatException(outputFormat
        // + " is not a valid output format for a profile plot");
        // }
        // String[] layers = params.getMandatoryString("layers").split(",");
        // CoordinateReferenceSystem crs =
        // GISUtils.getCrs(params.getMandatoryString("CRS"));
        // LineString lineString = new
        // LineString(params.getMandatoryString("linestring"), crs);
        // String timeStr = params.getString("time");
        //
        // String elevationStr = params.getString("elevation");
        // Double zValue = null;
        // if (elevationStr != null) {
        // zValue = Double.parseDouble(elevationStr);
        // }
        // StringBuilder copyright = new StringBuilder();
        // List<TrajectoryFeature> trajectoryFeatures = new
        // ArrayList<TrajectoryFeature>();
        // /* Do we also want to plot a vertical section plot? */
        // boolean verticalSection = false;
        // List<HorizontalPosition> verticalSectionHorizontalPositions = new
        // ArrayList<HorizontalPosition>();
        // for (String layerName : layers) {
        // Dataset dataset = catalogue.getDatasetFromLayerName(layerName);
        // if (dataset instanceof GridDataset) {
        // GridDataset gridDataset = (GridDataset) dataset;
        // String varId = catalogue.getVariableFromId(layerName);
        // String layerCopyright =
        // catalogue.getLayerMetadata(layerName).getCopyright();
        // if (layerCopyright != null && !"".equals(layerCopyright)) {
        // copyright.append(layerCopyright);
        // copyright.append('\n');
        // }
        //
        // VariableMetadata metadata = gridDataset.getVariableMetadata(varId);
        // VerticalDomain verticalDomain = metadata.getVerticalDomain();
        // final VerticalPosition zPos;
        // if (zValue != null && verticalDomain != null) {
        // zPos = new VerticalPosition(zValue, verticalDomain.getVerticalCrs());
        // } else {
        // zPos = null;
        // }
        // if (verticalDomain != null && layers.length == 1) {
        // verticalSection = true;
        // }
        //
        // final DateTime time;
        // TemporalDomain temporalDomain = metadata.getTemporalDomain();
        // if (timeStr != null) {
        // time = TimeUtils.iso8601ToDateTime(timeStr,
        // temporalDomain.getChronology());
        // } else {
        // time = null;
        // }
        // HorizontalDomain hDomain = metadata.getHorizontalDomain();
        // final List<HorizontalPosition> transectPoints;
        // if (hDomain instanceof HorizontalGrid) {
        // transectPoints = GISUtils.getOptimalTransectPoints((HorizontalGrid)
        // hDomain,
        // lineString, zPos, time, AXIS_RESOLUTION / 10);
        // } else {
        // transectPoints = lineString.getPointsOnPath(AXIS_RESOLUTION);
        // }
        // if (verticalSection) {
        // verticalSectionHorizontalPositions = transectPoints;
        // }
        // TrajectoryDomain trajectoryDomain = new TrajectoryDomain(
        // new AbstractList<GeoPosition>() {
        // @Override
        // public GeoPosition get(int index) {
        // return new GeoPosition(transectPoints.get(index), zPos, time);
        // }
        //
        // @Override
        // public int size() {
        // return transectPoints.size();
        // }
        // });
        //
        // TrajectoryFeature feature = gridDataset.readTrajectoryData(
        // CollectionUtils.setOf(varId), trajectoryDomain);
        // trajectoryFeatures.add(feature);
        // } else {
        // throw new UnsupportedOperationException(
        // "Currently only gridded datasets are supported for transect plots");
        // }
        // }
        //
        // if (copyright.length() > 0) {
        // copyright.deleteCharAt(copyright.length() - 1);
        // }
        // JFreeChart chart = Charting.createTransectPlot(trajectoryFeatures,
        // lineString, false,
        // copyright.toString());
        //
        // if (verticalSection) {
        // /*
        // * This can only be true if we have a GridSeriesFeature, so we can
        // * cast
        // */
        // Dataset dataset = catalogue.getDatasetFromLayerName(layers[0]);
        // String varId = catalogue.getVariableFromId(layers[0]);
        // if (dataset instanceof GridDataset) {
        // GridDataset gridDataset = (GridDataset) dataset;
        //
        // String paletteName = params
        // .getString("palette", ColourPalette.DEFAULT_PALETTE_NAME);
        // int numColourBands = params.getPositiveInt("numcolorbands",
        // ColourPalette.MAX_NUM_COLOURS);
        // Extent<Float> scaleRange =
        // GetMapStyleParams.getColorScaleRange(params);
        // if (scaleRange == null || scaleRange.isEmpty()) {
        // scaleRange = Extents.newExtent(270f, 300f);
        // }
        // ColourScale colourScale = new ColourScale(scaleRange.getLow(),
        // scaleRange.getHigh(), params.getBoolean("logscale", false));
        //
        // String bgColourStr = params.getString("bgcolor", "transparent");
        // String amColourStr = params.getString("abovemaxcolor", "0x000000");
        // String bmColourStr = params.getString("belowmincolor", "0x000000");
        // ColourMap palette = new
        // ColourMap(GraphicsUtils.parseColour(bmColourStr),
        // GraphicsUtils.parseColour(amColourStr),
        // GraphicsUtils.parseColour(bgColourStr), paletteName, numColourBands);
        // ColourScheme colourScheme = new PaletteColourScheme(colourScale,
        // palette);
        // List<ProfileFeature> profileFeatures = new
        // ArrayList<ProfileFeature>();
        //
        // VariableMetadata variableMetadata =
        // gridDataset.getVariableMetadata(varId);
        // VerticalDomain verticalDomain = variableMetadata.getVerticalDomain();
        // VerticalAxis vAxis;
        // if (verticalDomain instanceof VerticalAxis) {
        // vAxis = (VerticalAxis) verticalDomain;
        // } else {
        // /*
        // * We don't have a valid vertical axis, so create one
        // */
        // List<Double> values = new ArrayList<Double>();
        // double zMin = verticalDomain.getExtent().getLow();
        // double zMax = verticalDomain.getExtent().getHigh();
        // for (int i = 0; i < AXIS_RESOLUTION; i++) {
        // values.add(zMin + (zMax - zMin) / AXIS_RESOLUTION);
        // }
        // vAxis = new VerticalAxisImpl("Vertical section axis", values,
        // verticalDomain.getVerticalCrs());
        // }
        // TemporalDomain temporalDomain =
        // gridDataset.getVariableMetadata(varId)
        // .getTemporalDomain();
        // DateTime time = null;
        // if (timeStr != null) {
        // time = TimeUtils.iso8601ToDateTime(timeStr,
        // temporalDomain.getChronology());
        // }
        // for (HorizontalPosition pos : verticalSectionHorizontalPositions) {
        // ProfileFeature profileFeature = gridDataset.readProfileData(
        // CollectionUtils.setOf(varId), pos, vAxis, time);
        // profileFeatures.add(profileFeature);
        // }
        // JFreeChart verticalSectionChart =
        // Charting.createVerticalSectionChart(
        // profileFeatures, lineString, colourScheme, zValue);
        // chart = Charting.addVerticalSectionChart(chart,
        // verticalSectionChart);
        // } else {
        // log.error("Vertical section charts not supported for non-grid datasets");
        // }
        // }
        // int width = params.getPositiveInt("width", 700);
        // int height = params.getPositiveInt("height", verticalSection ? 1000 :
        // 600);
        //
        // httpServletResponse.setContentType(outputFormat);
        // try {
        // if ("image/png".equals(outputFormat)) {
        // ChartUtilities.writeChartAsPNG(httpServletResponse.getOutputStream(),
        // chart, width,
        // height);
        // } else {
        // /* Must be a JPEG */
        // ChartUtilities.writeChartAsJPEG(httpServletResponse.getOutputStream(),
        // chart,
        // width, height);
        // }
        // } catch (IOException e) {
        // log.error("Cannot write to output stream", e);
        // throw new EdalException("Problem writing data to output stream", e);
        // }
    }

    private void getVerticalProfile(RequestParams params, HttpServletResponse httpServletResponse)
            throws EdalException {
        GetFeatureInfoParameters featureInfoParameters = new GetFeatureInfoParameters(params,
                catalogue);
        PlottingDomainParams plottingParameters = featureInfoParameters
                .getPlottingDomainParameters();
        final HorizontalPosition position = featureInfoParameters.getClickedPosition();

        String[] layerNames = featureInfoParameters.getLayerNames();
        String outputFormat = featureInfoParameters.getInfoFormat();
        if (!"image/png".equalsIgnoreCase(outputFormat)
                && !"image/jpeg".equalsIgnoreCase(outputFormat)
                && !"image/jpg".equalsIgnoreCase(outputFormat)) {
            throw new InvalidFormatException(outputFormat
                    + " is not a valid output format for a profile plot");
        }

        /*
         * Loop over all requested layers
         */
        List<ProfileFeature> profileFeatures = new ArrayList<ProfileFeature>();
        for (String layerName : layerNames) {
            Dataset dataset = catalogue.getDatasetFromLayerName(layerName);
            String variableId = catalogue.getVariableFromId(layerName);
            profileFeatures.addAll(dataset.extractProfileFeatures(
                    CollectionUtils.setOf(variableId), plottingParameters));
        }

        while (profileFeatures.size() > featureInfoParameters.getFeatureCount()) {
            profileFeatures.remove(profileFeatures.size() - 1);
        }

        int width = 700;
        int height = 600;

        /* Now create the vertical profile plot */
        JFreeChart chart = Charting.createVerticalProfilePlot(profileFeatures, position);

        httpServletResponse.setContentType(outputFormat);
        try {
            if ("image/png".equals(outputFormat)) {
                ChartUtilities.writeChartAsPNG(httpServletResponse.getOutputStream(), chart, width,
                        height);
            } else {
                /* Must be a JPEG */
                ChartUtilities.writeChartAsJPEG(httpServletResponse.getOutputStream(), chart,
                        width, height);
            }
        } catch (IOException e) {
            log.error("Cannot write to output stream", e);
            throw new EdalException("Problem writing data to output stream", e);
        }
    }

    /**
     * Wraps {@link EdalException}s in an XML wrapper and returns them.
     * 
     * @param exception
     *            The exception to handle
     * @param httpServletResponse
     *            The {@link HttpServletResponse} object to write to
     * @param v130
     *            Whether this should be handled as a WMS v1.3.0 exception
     * @throws IOException
     *             If there is a problem writing to the output stream
     */
    void handleWmsException(EdalException exception, HttpServletResponse httpServletResponse,
            boolean v130) throws IOException {
        log.warn("Wms Exception caught: " + exception.getMessage());

        VelocityContext context = new VelocityContext();
        EventCartridge ec = new EventCartridge();
        ec.addEventHandler(new EscapeXmlReference());
        ec.attachToContext(context);

        context.put("exception", exception);

        Template template;
        if (v130) {
            template = velocityEngine.getTemplate("templates/exception-1.3.0.vm");
        } else {
            template = velocityEngine.getTemplate("templates/exception-1.1.1.vm");
        }
        template.merge(context, httpServletResponse.getWriter());
    }
}
