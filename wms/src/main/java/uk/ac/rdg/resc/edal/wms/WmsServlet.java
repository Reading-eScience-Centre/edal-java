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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.naming.OperationNotSupportedException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.covjson.CoverageJsonConverter;
import uk.ac.rdg.resc.edal.covjson.CoverageJsonConverterImpl;
import uk.ac.rdg.resc.edal.dataset.ContinuousDomainDataset;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DiscreteLayeredDataset;
import uk.ac.rdg.resc.edal.dataset.HorizontallyDiscreteDataset;
import uk.ac.rdg.resc.edal.dataset.plugins.VectorPlugin;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.HorizontalDomain;
import uk.ac.rdg.resc.edal.domain.PointCollectionDomain;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.domain.VerticalDomain;
import uk.ac.rdg.resc.edal.exceptions.BadTimeFormatException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.exceptions.IncorrectDomainException;
import uk.ac.rdg.resc.edal.exceptions.MetadataException;
import uk.ac.rdg.resc.edal.exceptions.VariableNotFoundException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.feature.PointCollectionFeature;
import uk.ac.rdg.resc.edal.feature.PointFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.LineString;
import uk.ac.rdg.resc.edal.graphics.Charting;
import uk.ac.rdg.resc.edal.graphics.exceptions.EdalLayerNotFoundException;
import uk.ac.rdg.resc.edal.graphics.formats.ImageFormat;
import uk.ac.rdg.resc.edal.graphics.formats.InvalidFormatException;
import uk.ac.rdg.resc.edal.graphics.formats.KmzFormat;
import uk.ac.rdg.resc.edal.graphics.formats.SimpleFormat;
import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.graphics.style.ScaleRange;
import uk.ac.rdg.resc.edal.graphics.style.SegmentColourScheme;
import uk.ac.rdg.resc.edal.graphics.utils.ColourPalette;
import uk.ac.rdg.resc.edal.graphics.utils.EnhancedVariableMetadata;
import uk.ac.rdg.resc.edal.graphics.utils.FeatureCatalogue.FeaturesAndMemberName;
import uk.ac.rdg.resc.edal.graphics.utils.GraphicsUtils;
import uk.ac.rdg.resc.edal.graphics.utils.LayerNameMapper;
import uk.ac.rdg.resc.edal.graphics.utils.PlottingDomainParams;
import uk.ac.rdg.resc.edal.graphics.utils.PlottingStyleParameters;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.metadata.Parameter.Category;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;
import uk.ac.rdg.resc.edal.util.TimeUtils;
import uk.ac.rdg.resc.edal.wms.exceptions.CurrentUpdateSequence;
import uk.ac.rdg.resc.edal.wms.exceptions.EdalUnsupportedOperationException;
import uk.ac.rdg.resc.edal.wms.exceptions.InvalidUpdateSequence;
import uk.ac.rdg.resc.edal.wms.exceptions.LayerNotQueryableException;
import uk.ac.rdg.resc.edal.wms.util.WmsUtils;

/**
 * The main servlet for all WMS operations, including extended behaviour. This
 * servlet can be used as-is by defining it in the usual way in a web.xml file,
 * and injecting a {@link WmsCatalogue} object by calling the
 * {@link WmsServlet#setCatalogue(WmsCatalogue)}.
 * 
 * If the {@link WmsCatalogue} is not set, behaviour is undefined. It'll fail in
 * all sorts of ways - nothing will work properly. However, the
 * {@link WmsCatalogue} set in {@link WmsServlet#setCatalogue(WmsCatalogue)} is
 * only directly used in
 * {@link WmsServlet#dispatchWmsRequest(String, RequestParams, HttpServletRequest, HttpServletResponse, WmsCatalogue)}
 * , so if it is not set, a subclass can still override that method and inject a
 * {@link WmsCatalogue} on a per-request basis.
 * 
 * The recommended usage is to either subclass this servlet and set a valid
 * instance of a {@link WmsCatalogue} in the constructor/init method or to use
 * Spring to do the wiring for you.
 */
public class WmsServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(WmsServlet.class);

    public static final int AXIS_RESOLUTION = 500;
    private static final long serialVersionUID = 1L;
    protected static final String FEATURE_INFO_XML_FORMAT = "text/xml";
    protected static final String FEATURE_INFO_PLAIN_FORMAT = "text/plain";
    protected static final String FEATURE_INFO_HTML_FORMAT = "text/html";
    protected static final String[] DEFAULT_SUPPORTED_CRS_CODES = new String[] { "EPSG:4326",
            "CRS:84", "EPSG:41001", // Mercator
            "EPSG:27700", // British National Grid
            "EPSG:3408", // NSIDC EASE-Grid North
            "EPSG:3409", // NSIDC EASE-Grid South
            "EPSG:3857", // Google Maps
            "EPSG:5041", // North Polar stereographic
            "EPSG:5042", // South Polar stereographic
            "EPSG:32661", // North Polar stereographic
            "EPSG:32761" // South Polar stereographic
    };

    private WmsCatalogue catalogue = null;
    protected final VelocityEngine velocityEngine;
    private final Set<String> advertisedPalettes = new TreeSet<>();

    private String[] SupportedCrsCodes = DEFAULT_SUPPORTED_CRS_CODES;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public WmsServlet() {
        super();

        advertisedPalettes.add(ColourPalette.DEFAULT_PALETTE_NAME);

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

    /**
     * Sets a {@link WmsCatalogue} to be used globally for all requests.
     * 
     * If no catalogue is set and this {@link Servlet} is used then it will fail
     * with a {@link NullPointerException} on the vast majority of calls.
     * 
     * Note that this {@link WmsCatalogue} is only used in the
     * {@link WmsServlet#dispatchWmsRequest(String, RequestParams, HttpServletRequest, HttpServletResponse)}
     * method, which passes it to all of the worker methods. Thus, a different
     * {@link WmsCatalogue} can be used for each request if required (for
     * example to have dataset/variable defined by the URL) by subclassing
     * {@link WmsServlet} and overriding
     * {@link WmsServlet#dispatchWmsRequest(String, RequestParams, HttpServletRequest, HttpServletResponse)}
     * such that it passes a new catalogue to each of those methods
     * 
     * @param catalogue
     *            The {@link WmsCatalogue} to use.
     */
    public void setCatalogue(WmsCatalogue catalogue) {
        this.catalogue = catalogue;
    }

    /**
     * Gets the {@link WmsCatalogue} used in this servlet. Subclasses may wish
     * to use this method to get direct access to the catalogue if (for example)
     * implementing new, non-WMS extensions.
     * 
     * @return The {@link WmsCatalogue} associated with this servlet.
     */
    protected WmsCatalogue getCatalogue() {
        return this.catalogue;
    }

    /**
     * Sets the palettes to be advertised in the GetCapabilities document.
     * 
     * In the capabilities document, each layer will advertise the available
     * styles.
     * 
     * Since some styles can use palettes, this means that the capabilities
     * document can get very large very quickly with the formula:
     * 
     * (styles which use palettes) x (number of palettes) x (number of layers)
     * 
     * being an approximation of how many Style tags are defined in the
     * document. This is impractical, so we limit the number of advertised
     * palettes. By default, this will only include the default palette name.
     * 
     * This method takes a {@link List} of palette names to advertise alongside
     * the default.
     * 
     * @param paletteNames
     *            The palettes to advertise alongside the default.
     */
    protected void setCapabilitiesAdvertisedPalettes(Collection<String> paletteNames) {
        for (String palette : paletteNames) {
            if (ColourPalette.getPredefinedPalettes().contains(palette)) {
                advertisedPalettes.add(palette);
            }
        }
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
            dispatchWmsRequest(request, params, httpServletRequest, httpServletResponse, catalogue);
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
            log.error("Problem with GET request", e);
            /* An unexpected (internal) error has occurred */
            e.printStackTrace();
            throw new IOException(e);
        }
    }

    /**
     * Sends the HTTP request to the appropriate WMS method
     * 
     * @param request
     *            The URL REQUEST parameter
     * @param params
     *            A map of URL parameters, implemented in a case-insensitive way
     * @param httpServletRequest
     *            The {@link HttpServletRequest} object from the GET request
     * @param httpServletResponse
     *            The {@link HttpServletResponse} object from the GET request
     * @param catalogue
     *            The {@link WmsCatalogue} which should be used to serve
     *            datasets.
     */
    protected void dispatchWmsRequest(String request, RequestParams params,
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            WmsCatalogue catalogue) throws Exception {
        if (catalogue == null) {
            throw new EdalException(
                    "No WMS catalogue has been set to discover datasets.  This is likely to be a programming error.");
        }
        if (request.equals("GetMap")) {
            getMap(params, httpServletResponse, catalogue);
        } else if (request.equals("GetCapabilities")) {
            getCapabilities(params, httpServletResponse,
                    httpServletRequest.getRequestURL().toString(), catalogue);
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
            getFeatureInfo(params, httpServletResponse, catalogue);
        }
        /*
         * The REQUESTs below are non-standard
         */
        else if (request.equals("GetMetadata")) {
            /*
             * This is a request for non-standard metadata.
             */
            getMetadata(params, httpServletResponse, catalogue);
        } else if (request.equals("GetLegendGraphic")) {
            /*
             * This is a request for an image representing the legend for the
             * map parameters
             */
            getLegendGraphic(params, httpServletResponse, catalogue);
        } else if (request.equals("GetTimeseries")) {
            getTimeseries(params, httpServletResponse, catalogue);
        } else if (request.equals("GetTransect")) {
            getTransect(params, httpServletResponse, catalogue);
        } else if (request.equals("GetVerticalProfile")) {
            getVerticalProfile(params, httpServletResponse, catalogue);
            // } else if (request.equals("GetVerticalSection")) {
            // getVerticalSection(params, httpServletResponse);
        } else {
            throw new OperationNotSupportedException(request);
        }
    }

    protected void getMap(RequestParams params, HttpServletResponse httpServletResponse,
            WmsCatalogue catalogue) throws EdalException {
        GetMapParameters getMapParams = new GetMapParameters(params, catalogue);

        PlottingDomainParams plottingParameters = getMapParams.getPlottingDomainParameters();
        GetMapStyleParams styleParameters = getMapParams.getStyleParameters();

        /*
         * If the user has requested the actual data in coverageJSON format...
         */
        if (getMapParams.getFormatString().equalsIgnoreCase("application/prs.coverage+json")
                || getMapParams.getFormatString()
                        .equalsIgnoreCase("application/prs.coverage json")) {
            String[] layerNames = getMapParams.getStyleParameters().getLayerNames();
            LayerNameMapper layerNameMapper = catalogue.getLayerNameMapper();
            List<Feature<?>> features = new ArrayList<>();
            for (String layerName : layerNames) {
                if (!catalogue.isDownloadable(layerName)) {
                    throw new InvalidFormatException(
                            "The format \"application/prs.coverage+json\" is not enabled for this layer.\nIf you think this is an error, please contact the server administrator and get them to enable Download for this dataset");
                }
                Dataset dataset = catalogue
                        .getDatasetFromId(layerNameMapper.getDatasetIdFromLayerName(layerName));
                VariableMetadata metadata = dataset
                        .getVariableMetadata(layerNameMapper.getVariableIdFromLayerName(layerName));
                if (metadata.isScalar()) {
                    Collection<? extends DiscreteFeature<?, ?>> mapFeatures = GraphicsUtils
                            .extractGeneralMapFeatures(dataset,
                                    layerNameMapper.getVariableIdFromLayerName(layerName),
                                    plottingParameters);
                    features.addAll(mapFeatures);
                } else {
                    Set<VariableMetadata> children = metadata.getChildren();
                    for (VariableMetadata child : children) {
                        Collection<? extends DiscreteFeature<?, ?>> mapFeatures = GraphicsUtils
                                .extractGeneralMapFeatures(dataset,
                                        child.getParameter().getVariableId(), plottingParameters);
                        features.addAll(mapFeatures);
                    }
                }
            }

            httpServletResponse.setContentType("application/prs.coverage+json");
            CoverageJsonConverter converter = new CoverageJsonConverterImpl();

            converter.checkFeaturesSupported(features);
            try {
                if (features.size() == 1) {
                    converter.convertFeatureToJson(httpServletResponse.getOutputStream(),
                            features.get(0));
                } else {
                    /*
                     * vectors are currently multiple features each with one
                     * parameter TODO group features with identical domain into
                     * single feature
                     */
                    converter.convertFeaturesToJson(httpServletResponse.getOutputStream(),
                            features);
                }
            } catch (IOException e) {
                log.error("Problem writing CoverageJSON to output stream", e);
            }

            return;
        }

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
                throw new EdalException(
                        "The image format " + getMapParams.getImageFormat().getMimeType()
                                + " does not support fully-transparent pixels");
            }
            if (styleParameters.getOpacity() < 100
                    && !getMapParams.getImageFormat().supportsPartiallyTransparentPixels()) {
                throw new EdalException(
                        "The image format " + getMapParams.getImageFormat().getMimeType()
                                + " does not support partially-transparent pixels");
            }
            if (styleParameters.getNumLayers() > catalogue.getServerInfo()
                    .getMaxSimultaneousLayers()) {
                throw new EdalException(
                        "Only " + catalogue.getServerInfo().getMaxSimultaneousLayers()
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

        /*
         * Set the content type to be what was requested. If anything goes
         * wrong, this will be overwritten to "text/xml" in the
         * handleWmsException method.
         */
        httpServletResponse.setContentType(getMapParams.getFormatString());

        MapImage imageGenerator = styleParameters.getImageGenerator(catalogue);

        List<BufferedImage> frames;
        /*
         * Used for KML format
         */
        List<DateTime> timeValues = new ArrayList<>();
        if (!getMapParams.isAnimation()) {
            frames = Arrays.asList(imageGenerator.drawImage(plottingParameters, catalogue));
            timeValues.add(plottingParameters.getTargetT());
        } else {
            frames = new ArrayList<>();
            for (DateTime timeStep : getMapParams.getAnimationTimesteps()) {
                PlottingDomainParams timestepParameters = new PlottingDomainParams(
                        plottingParameters.getWidth(), plottingParameters.getHeight(),
                        plottingParameters.getBbox(), plottingParameters.getZExtent(), null,
                        plottingParameters.getTargetHorizontalPosition(),
                        plottingParameters.getTargetZ(), timeStep);
                BufferedImage frame = imageGenerator.drawImage(timestepParameters, catalogue);
                Graphics2D g = frame.createGraphics();
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
                g.setColor(Color.white);
                g.drawString(TimeUtils.formatUtcHumanReadableDateTime(timeStep), 9,
                        frame.getHeight() - 9);
                g.drawString(TimeUtils.formatUtcHumanReadableDateTime(timeStep), 9,
                        frame.getHeight() - 11);
                g.drawString(TimeUtils.formatUtcHumanReadableDateTime(timeStep), 11,
                        frame.getHeight() - 11);
                g.drawString(TimeUtils.formatUtcHumanReadableDateTime(timeStep), 11,
                        frame.getHeight() - 9);
                g.setColor(Color.black);
                g.drawString(TimeUtils.formatUtcHumanReadableDateTime(timeStep), 10,
                        frame.getHeight() - 10);
                timeValues.add(timeStep);
                frames.add(frame);
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
                simpleFormat.writeImage(frames, outputStream, getMapParams.getFrameRate());
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
                    httpServletResponse.setHeader("Content-Disposition",
                            "inline; filename=" + layerName.replaceAll("/", "-") + ".kmz");
                }
                EnhancedVariableMetadata layerMetadata = WmsUtils.getLayerMetadata(layerName,
                        catalogue);
                String name = layerMetadata.getTitle();
                String description = layerMetadata.getDescription();
                String zValue = plottingParameters.getTargetZ() == null ? null
                        : plottingParameters.getTargetZ().toString();
                BufferedImage legend = imageGenerator.getLegend(50, 200, true);
                GeographicBoundingBox gbbox = GISUtils
                        .toGeographicBoundingBox(plottingParameters.getBbox());
                imageFormat.writeImage(frames, outputStream, name, description, gbbox, timeValues,
                        zValue, legend, getMapParams.getFrameRate());
            }
            outputStream.close();
        } catch (SocketException e) {
            /*
             * The client can quite often cancel requests when loading tiled
             * maps.
             * 
             * This gives Broken pipe errors which can be ignored.
             */
        } catch (IOException e) {
            if (!(e.getCause() instanceof SocketException)) {
                /*
                 * Same as above - IOException which has a direct cause which is
                 * a SocketException
                 */
                log.error("Problem writing output to stream", e);
            }
        }
    }

    protected void getCapabilities(RequestParams params, HttpServletResponse httpServletResponse,
            String baseUrl, WmsCatalogue catalogue) throws EdalException {
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
            if (updateSequence.isEqual(catalogue.getLastUpdateTime())) {
                throw new CurrentUpdateSequence(updateSeqStr);
            } else if (updateSequence.isAfter(catalogue.getLastUpdateTime())) {
                throw new InvalidUpdateSequence(
                        updateSeqStr + " is later than the current server updatesequence value");
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
            if (catalogue.getServerInfo().allowsGlobalCapabilities()) {
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
        context.put("supportedFeatureInfoFormats", new String[] { FEATURE_INFO_PLAIN_FORMAT,
                FEATURE_INFO_XML_FORMAT, FEATURE_INFO_HTML_FORMAT });
        context.put("supportedCrsCodes", SupportedCrsCodes);
        context.put("GISUtils", GISUtils.class);
        context.put("TimeUtils", TimeUtils.class);
        context.put("WmsUtils", WmsUtils.class);
        context.put("verbose", params.getBoolean("verbose", false));
        context.put("allPalettes", ColourPalette.getPredefinedPalettes());
        context.put("availablePalettes", advertisedPalettes);

        httpServletResponse.setContentType("text/xml");
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
        } finally {
            try {
                httpServletResponse.flushBuffer();
            } catch (IOException e) {
                log.error("Problem flushing output buffer", e);
            }
        }
    }

    protected void getFeatureInfo(RequestParams params, HttpServletResponse httpServletResponse,
            WmsCatalogue catalogue) throws EdalException {
        if (!catalogue.getServerInfo().allowsFeatureInfo()) {
            throw new LayerNotQueryableException(
                    "This server does not allow GetFeatureInfo requests");
        }
        GetFeatureInfoParameters featureInfoParameters = new GetFeatureInfoParameters(params,
                catalogue);
        if (!FEATURE_INFO_XML_FORMAT.equals(featureInfoParameters.getInfoFormat())
                && !FEATURE_INFO_PLAIN_FORMAT.equals(featureInfoParameters.getInfoFormat())
                && !FEATURE_INFO_HTML_FORMAT.equals(featureInfoParameters.getInfoFormat())) {
            throw new EdalUnsupportedOperationException(
                    "Currently the supported feature info types are \"text/html\" \"text/xml\" and \"text/plain\"");
        }
        httpServletResponse.setContentType(featureInfoParameters.getInfoFormat());

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
            if (catalogue.isDisabled(layerName)) {
                throw new EdalLayerNotFoundException(
                        "The layer " + layerName + " is not enabled on this server");
            }
            if (!catalogue.isQueryable(layerName)) {
                throw new LayerNotQueryableException(
                        "The layer " + layerName + " is not queryable");
            }
            Dataset dataset = WmsUtils.getDatasetFromLayerName(layerName, catalogue);
            String variableId = catalogue.getLayerNameMapper()
                    .getVariableIdFromLayerName(layerName);
            VariableMetadata metadata = WmsUtils.getVariableMetadataFromLayerName(layerName,
                    catalogue);
            Set<VariableMetadata> children = metadata.getChildren();

            /*
             * We only want to return a layer name if there are more than one
             */
            String layerNameToSave = layerNames.length < 2 ? null : layerName;

            Collection<? extends DiscreteFeature<?, ?>> mapFeatures;
            if (dataset instanceof HorizontallyDiscreteDataset<?>) {
                /*
                 * This is the simple case - we just want to extract a single
                 * value from the dataset.
                 */
                HorizontallyDiscreteDataset<?> discreteDataset = (HorizontallyDiscreteDataset<?>) dataset;

                Number value = discreteDataset.readSinglePoint(variableId, position,
                        plottingParameters.getTargetZ(), plottingParameters.getTargetT());
                FeatureInfoPoint featureInfoPoint;
                if (value != null) {
                    featureInfoPoint = new FeatureInfoPoint(layerName, variableId, position,
                            TimeUtils.dateTimeToISO8601(plottingParameters.getTargetT()), value,
                            new Properties());
                    featureInfos.add(featureInfoPoint);
                }

                for (VariableMetadata child : children) {
                    /*
                     * Now add the values for every child layer, using the child
                     * variable IDs to identify values.
                     */
                    value = discreteDataset.readSinglePoint(child.getId(), position,
                            plottingParameters.getTargetZ(), plottingParameters.getTargetT());
                    if (value != null) {
                        featureInfoPoint = new FeatureInfoPoint(layerNameToSave, child.getId(),
                                position,
                                TimeUtils.dateTimeToISO8601(plottingParameters.getTargetT()), value,
                                new Properties());
                        featureInfos.add(featureInfoPoint);
                    }
                }
            } else if (dataset instanceof ContinuousDomainDataset) {
                /*
                 * Extract the map features. Because of the way
                 * GetFeatureInfoParameters works, features are searched for in
                 * a 9-pixel box surrounding the clicked position on the map
                 */
                mapFeatures = GraphicsUtils.extractGeneralMapFeatures(dataset, variableId,
                        plottingParameters);
                for (DiscreteFeature<?, ?> feature : mapFeatures) {
                    if (metadata.isScalar()) {
                        /*
                         * If we have a scalar layer, add the value for it
                         * first, using the feature name to identify values.
                         */
                        FeatureInfoPoint featurePoint = getFeatureInfoValuesFromFeature(feature,
                                variableId, plottingParameters, layerNameToSave, feature.getName(),
                                metadata);
                        if (featurePoint != null) {
                            featureInfos.add(featurePoint);
                        }
                    }
                    for (VariableMetadata child : children) {
                        /*
                         * Now add the values for every child layer, using the
                         * child variable IDs to identify values.
                         */
                        String name = catalogue.getLayerMetadata(child).getTitle();
                        FeatureInfoPoint featurePoint = getFeatureInfoValuesFromFeature(feature,
                                child.getId(), plottingParameters, layerNameToSave, name, metadata);
                        if (featurePoint != null) {
                            featureInfos.add(featurePoint);
                        }
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
        Template template;
        if (FEATURE_INFO_XML_FORMAT.equals(featureInfoParameters.getInfoFormat())) {
            template = velocityEngine.getTemplate("templates/featureInfo-xml.vm");
        } else if (FEATURE_INFO_HTML_FORMAT.equals(featureInfoParameters.getInfoFormat())) {
            template = velocityEngine.getTemplate("templates/featureInfo-html.vm");
        } else {
            template = velocityEngine.getTemplate("templates/featureInfo-plain.vm");
        }
        VelocityContext context = new VelocityContext();
        context.put("position",
                GISUtils.transformPosition(position, GISUtils.defaultGeographicCRS()));
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
     * @param metadata
     *            The {@link VariableMetadata} of the layer being queried
     * @return The extracted value and corresponding information collected as a
     *         {@link FeatureInfoPoint}
     */
    protected FeatureInfoPoint getFeatureInfoValuesFromFeature(DiscreteFeature<?, ?> feature,
            String variableId, PlottingDomainParams plottingParameters, String layerName,
            String featureName, VariableMetadata metadata) {
        Object value = null;
        HorizontalPosition position = null;
        String timeStr = null;
        if (feature instanceof MapFeature) {
            MapFeature mapFeature = (MapFeature) feature;
            GridCoordinates2D pointIndex = mapFeature.getDomain()
                    .findIndexOf(plottingParameters.getTargetHorizontalPosition());
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
        } else if (feature instanceof TrajectoryFeature) {
            TrajectoryFeature trajectoryFeature = (TrajectoryFeature) feature;
            HorizontalPosition target = plottingParameters.getTargetHorizontalPosition();
            if (!trajectoryFeature.getDomain().getCoordinateBounds().contains(target)) {
                /*
                 * The clicked position is not within this trajectory's bounds
                 */
                return null;
            }
            double minDist = Double.MAX_VALUE;
            Array1D<GeoPosition> domain = trajectoryFeature.getDomain().getDomainObjects();
            Array1D<Number> values = trajectoryFeature.getValues(variableId);
            for (int i = 0; i < domain.size(); i++) {
                GeoPosition pos = domain.get(i);
                double dist = GISUtils.getDistSquared(pos.getHorizontalPosition(), target);
                if (dist < minDist) {
                    minDist = dist;
                    value = values.get(i);
                    position = pos.getHorizontalPosition();
                    timeStr = TimeUtils.dateTimeToISO8601(pos.getTime());
                }
            }
        } else if (feature instanceof ProfileFeature) {
            /*
             * This shouldn't ever get called now that we are dealing with
             * PointFeatures rather than ProfileFeatures (ProfileFeatures may be
             * the underlying data type but they shouldn't be the map feature).
             * 
             * No harm leaving the code in for future reference.
             */
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
            /*
             * This shouldn't ever get called now that we are dealing with
             * PointFeatures rather than PointSeriesFeatures
             * (PointSeriesFeatures may be the underlying data type but they
             * shouldn't be the map feature).
             * 
             * No harm leaving the code in for future reference.
             */
            PointSeriesFeature pointSeriesFeature = (PointSeriesFeature) feature;
            int index = GISUtils.getIndexOfClosestTimeTo(plottingParameters.getTargetT(),
                    pointSeriesFeature.getDomain());
            if (index >= 0) {
                position = pointSeriesFeature.getHorizontalPosition();
                value = pointSeriesFeature.getValues(variableId).get(index);
                timeStr = TimeUtils.dateTimeToISO8601(
                        pointSeriesFeature.getDomain().getCoordinateValue(index));
            }
        } else {
            log.warn("Info for Feature of type " + feature.getClass()
                    + " requested, but this is not supported");
        }
        if (value != null) {
            /*
             * Change value to the category label if it represents a category
             */
            Map<Integer, Category> categories = metadata.getParameter().getCategories();
            if (categories != null && value instanceof Number
                    && categories.containsKey(((Number) value).intValue())) {
                value = categories.get(((Number) value).intValue()).getLabel();
            }
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
     * @param catalogue2
     * @throws MetadataException
     *             If there are any issues with returning the metadata
     */
    protected void getMetadata(RequestParams params, HttpServletResponse httpServletResponse,
            WmsCatalogue catalogue) throws MetadataException {
        String item = params.getString("item");
        String json = null;
        if (item == null) {
            throw new MetadataException("Must provide an ITEM parameter");
        } else if (item.equals("menu")) {
            json = showMenu(params, catalogue);
        } else if (item.equals("layerDetails")) {
            json = showLayerDetails(params, catalogue);
        } else if (item.equals("timesteps")) {
            json = showTimesteps(params, catalogue);
        } else if (item.equals("minmax")) {
            json = showMinMax(params, catalogue);
        } else if (item.equals("animationTimesteps")) {
            json = showAnimationTimesteps(params, catalogue);
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

    protected String showMenu(RequestParams params, WmsCatalogue catalogue)
            throws MetadataException {
        JSONObject menu = new JSONObject();
        menu.put("label", catalogue.getServerInfo().getName());

        Collection<Dataset> datasets;
        String datasetStr = params.getString("dataset");
        if (datasetStr != null) {
            Dataset dataset = catalogue.getDatasetFromId(datasetStr);
            if (dataset == null) {
                throw new MetadataException("Requested menu for dataset: " + datasetStr
                        + " which does not exist on this server");
            }
            datasets = Arrays.asList(new Dataset[] { dataset });
        } else {
            datasets = catalogue.getAllDatasets();
        }

        JSONArray children = new JSONArray();
        for (Dataset dataset : datasets) {
            String datasetId = dataset.getId();

            Set<VariableMetadata> topLevelVariables = dataset.getTopLevelVariables();
            JSONArray datasetChildren;
            try {
                datasetChildren = addVariablesToArray(topLevelVariables, datasetId, catalogue);
                String datasetLabel = catalogue.getDatasetTitle(datasetId);
                JSONObject datasetJson = new JSONObject();
                datasetJson.put("label", datasetLabel);
                datasetJson.put("children", datasetChildren);
                children.put(datasetJson);
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

    protected JSONArray addVariablesToArray(Set<VariableMetadata> variables, String datasetId,
            WmsCatalogue catalogue) throws EdalLayerNotFoundException {
        JSONArray ret = new JSONArray();
        for (VariableMetadata variable : variables) {
            String id = variable.getId();
            String layerName = catalogue.getLayerNameMapper().getLayerName(datasetId, id);
            EnhancedVariableMetadata layerMetadata = WmsUtils.getLayerMetadata(layerName,
                    catalogue);
            if (catalogue.isDisabled(layerName)) {
                continue;
            }

            JSONObject child = new JSONObject();

            child.put("id", layerName);

            String title = layerMetadata.getTitle();
            child.put("label", title);

            Collection<String> supportedStyles = catalogue.getStyleCatalogue()
                    .getSupportedStyles(variable, catalogue.getLayerNameMapper());
            child.put("plottable", (supportedStyles != null && supportedStyles.size() > 0));

            Set<VariableMetadata> children = variable.getChildren();
            if (children.size() > 0) {
                JSONArray childrenArray = addVariablesToArray(children, datasetId, catalogue);
                child.put("children", childrenArray);
            }

            ret.put(child);
        }
        return ret;
    }

    protected String showLayerDetails(RequestParams params, WmsCatalogue catalogue)
            throws MetadataException {
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
            dataset = WmsUtils.getDatasetFromLayerName(layerName, catalogue);
            variableId = catalogue.getLayerNameMapper().getVariableIdFromLayerName(layerName);
        } catch (EdalLayerNotFoundException e) {
            throw new MetadataException("The layer " + layerName + " does not exist", e);
        }

        EnhancedVariableMetadata layerMetadata;
        try {
            layerMetadata = WmsUtils.getLayerMetadata(layerName, catalogue);
            if (catalogue.isDisabled(layerName)) {
                throw new EdalLayerNotFoundException(
                        "The layer " + layerName + " is not enabled on this server");
            }
        } catch (EdalLayerNotFoundException e) {
            throw new MetadataException("Layer not found", e);
        }
        if (dataset == null || variableId == null || layerMetadata == null) {
            log.error("Layer " + layerName + " doesn't exist - can't get layer details");
            throw new MetadataException("Must supply a valid LAYERNAME to get layer details");
        }

        VariableMetadata variableMetadata;
        try {
            variableMetadata = dataset.getVariableMetadata(variableId);
        } catch (VariableNotFoundException e) {
            throw new MetadataException("Layer not found", e);
        }

        PlottingStyleParameters defaultProperties = layerMetadata.getDefaultPlottingParameters();

        /*
         * Now create local variables containing the relevant details needed
         */
        String units = variableMetadata.getParameter().getUnits();
        /*
         * Converts to CRS:84 and also constrains to (-180:180]
         */
        BoundingBox boundingBox = GISUtils
                .toWGS84BoundingBox(variableMetadata.getHorizontalDomain().getBoundingBox());

        Integer numColorBands = defaultProperties.getNumColorBands();
        if (numColorBands == null) {
            numColorBands = 250;
        }

        Collection<String> supportedStyles = catalogue.getStyleCatalogue()
                .getSupportedStyles(variableMetadata, catalogue.getLayerNameMapper());

        VerticalDomain verticalDomain = variableMetadata.getVerticalDomain();
        TemporalDomain temporalDomain = variableMetadata.getTemporalDomain();

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
        String defaultPalette = defaultProperties.getPalette();
        if (defaultPalette == null) {
            defaultPalette = ColourPalette.DEFAULT_PALETTE_NAME;
        }
        String aboveMaxColour = GraphicsUtils.colourToString(defaultProperties.getAboveMaxColour());
        String belowMinColour = GraphicsUtils.colourToString(defaultProperties.getBelowMinColour());
        String noDataColour = GraphicsUtils.colourToString(defaultProperties.getNoDataColour());

        Boolean logScaling = defaultProperties.isLogScaling();
        if (logScaling == null) {
            logScaling = false;
        }

        /*
         * Now write the layer details out to a JSON object
         */
        JSONObject layerDetails = new JSONObject();

        layerDetails.put("units", units);

        JSONArray bboxJson = new JSONArray();
        bboxJson.put(boundingBox.getMinX());
        bboxJson.put(boundingBox.getMinY());
        bboxJson.put(boundingBox.getMaxX());
        bboxJson.put(boundingBox.getMaxY());
        layerDetails.put("bbox", bboxJson);

        List<Extent<Float>> scaleRanges = defaultProperties.getColorScaleRanges();
        if (scaleRanges == null || scaleRanges.isEmpty()) {
            scaleRanges = new ArrayList<>();
            scaleRanges.add(Extents.emptyExtent());
        }
        int s = 0;
        for (Extent<Float> scaleRange : scaleRanges) {
            /*
             * This writes out the main scaleRange followed by scaleRangeX
             * objects for additional configured default scale ranges. At the
             * time of writing this comment, only one default scale range can be
             * configured, but this may change in future (since multiple scale
             * ranges are permitted on the URL for those layers which support
             * them - currently just uncertainty images)
             */
            JSONArray scaleRangeJson = new JSONArray();
            scaleRangeJson.put(scaleRange.getLow());
            scaleRangeJson.put(scaleRange.getHigh());
            if (s == 0) {
                layerDetails.put("scaleRange", scaleRangeJson);
            } else {
                layerDetails.put("scaleRange" + s, scaleRangeJson);
            }
            s++;
        }

        layerDetails.put("numColorBands", numColorBands);

        JSONArray supportedStylesJson = new JSONArray();
        JSONArray noPaletteStylesJson = new JSONArray();
        for (String supportedStyle : supportedStyles) {
            supportedStylesJson.put(supportedStyle);
            if (!catalogue.getStyleCatalogue().styleUsesPalette(supportedStyle)) {
                noPaletteStylesJson.put(supportedStyle);
            }
        }
        layerDetails.put("supportedStyles", supportedStylesJson);
        layerDetails.put("noPaletteStyles", noPaletteStylesJson);

        layerDetails.put("categorical", variableMetadata.getParameter().getCategories() != null);

        layerDetails.put("queryable",
                catalogue.getServerInfo().allowsFeatureInfo() && catalogue.isQueryable(layerName));
        layerDetails.put("downloadable", catalogue.isDownloadable(layerName));

        /*
         * This can get pretty complicated (specifically when we have a
         * continuous domain containing multiple profile features each of which
         * may have a different axis...) so we factor it out
         */
        JSONObject zAxisJson = getZAxisJson(verticalDomain, dataset, variableId);
        if (zAxisJson != null) {
            layerDetails.put("continuousZ", !(verticalDomain instanceof VerticalAxis));
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
                            daysJson.put(day);
                        }
                        monthsJson.put("" + month, daysJson);
                    }
                    datesWithDataJson.put("" + year, monthsJson);
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
            if (variableMetadata.isScalar()) {
                /*
                 * Only support transects for scalar layers
                 */
                transects = true;
            }
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
        if (moreInfo != null) {
            layerDetails.put("moreInfo", moreInfo);
        }
        if (copyright != null) {
            layerDetails.put("copyright", copyright);
        }
        JSONArray supportedPalettesJson = new JSONArray();
        for (String supportedPalette : supportedPalettes) {
            supportedPalettesJson.put(supportedPalette);
        }
        layerDetails.put("palettes", supportedPalettesJson);
        layerDetails.put("defaultPalette", defaultPalette);
        layerDetails.put("aboveMaxColor", aboveMaxColour.replaceFirst("#", "0x"));
        layerDetails.put("belowMinColor", belowMinColour.replaceFirst("#", "0x"));
        layerDetails.put("noDataColor", noDataColour);
        layerDetails.put("logScaling", logScaling);

        return layerDetails.toString(4);
    }

    private static JSONObject getZAxisJson(VerticalDomain verticalDomain, Dataset dataset,
            String variableId) {
        if (verticalDomain == null) {
            return null;
        }

        JSONObject zAxisJson = new JSONObject();
        zAxisJson.put("units", verticalDomain.getVerticalCrs().getUnits());
        zAxisJson.put("pressure", verticalDomain.getVerticalCrs().isPressure());
        zAxisJson.put("positive", verticalDomain.getVerticalCrs().isPositiveUpwards());
        if (verticalDomain instanceof VerticalAxis) {
            /*
             * We have discrete vertical axis values
             */
            VerticalAxis verticalAxis = (VerticalAxis) verticalDomain;
            List<Double> sortedVals = new ArrayList<>(verticalAxis.getCoordinateValues());
            /*
             * Sort the values smallest to largest.
             */
            Collections.sort(sortedVals);
            if (verticalAxis.getVerticalCrs().isPressure()
                    || (sortedVals.get(0) < 0 && sortedVals.get(sortedVals.size() - 1) < 0)) {
                /*
                 * Largest to smallest if pressure, or all values are negative
                 */
                Collections.reverse(sortedVals);
            }

            JSONArray zValuesJson = new JSONArray();
            for (Double z : sortedVals) {
                zValuesJson.put(z);
            }
            zAxisJson.put("values", zValuesJson);
        } else {
            /*
             * Previously this used a very clever method to read a sample of
             * profile features, and try to space the depth values in such a way
             * that a similar number of features fell within each interval,
             * whilst simultaneously making everything fall on a nice round
             * number.
             * 
             * And when it worked, it worked exceptionally.
             * 
             * But when it didn't, it was slow, sometimes needed special checks
             * to avoid infinite loops, and occasionally generated bad values
             * anyway.
             * 
             * This now just spaces the axis over nice intervals. There's no
             * guarantee that each interval will contain a feature. But it's
             * quick and it doesn't fail
             */
            Extent<Double> zExtent = verticalDomain.getExtent();
            if (zExtent.getLow().equals(zExtent.getHigh())) {
                /*
                 * We have a single depth
                 */
                JSONArray zValuesJson = new JSONArray();
                zValuesJson.put(zExtent.getLow());
                zAxisJson.put("values", zValuesJson);
            } else {
                int nLevels = 20;
                double exactDelta = (zExtent.getHigh() - zExtent.getLow()) / nLevels;
                double delta = 0.0005;
                Stack<Double> deltas = new Stack<>();
                deltas.addAll(Arrays
                        .asList(new Double[] { 10000.0, 5000.0, 1000.0, 500.0, 200.0, 100.0, 50.0,
                                20.0, 10.0, 5.0, 2.0, 1.0, 0.5, 0.1, 0.05, 0.01, 0.005, 0.001 }));

                while (delta < exactDelta && !deltas.isEmpty()) {
                    /*
                     * Find the closest gap
                     */
                    delta = deltas.pop();
                }

                JSONArray zValuesJson = new JSONArray();
                for (double level = 0; level < zExtent.getHigh(); level += delta) {
                    zValuesJson.put(level);
                }
                zAxisJson.put("values", zValuesJson);
            }
        }
        return zAxisJson;
    }

    protected String showTimesteps(RequestParams params, WmsCatalogue catalogue)
            throws MetadataException {
        /*
         * Parse the parameters and get access to the variable and layer
         * metadata
         */
        String layerName = params.getString("layerName");
        if (layerName == null) {
            throw new MetadataException("Must supply a LAYERNAME parameter to get time steps");
        }

        Dataset dataset;
        String variableId;
        try {
            dataset = WmsUtils.getDatasetFromLayerName(layerName, catalogue);
            variableId = catalogue.getLayerNameMapper().getVariableIdFromLayerName(layerName);
            if (catalogue.isDisabled(layerName)) {
                throw new EdalLayerNotFoundException(
                        "The layer " + layerName + " is not enabled on this server");
            }
        } catch (EdalLayerNotFoundException e) {
            throw new MetadataException("The layer " + layerName + " does not exist", e);
        }
        VariableMetadata variableMetadata;
        try {
            variableMetadata = dataset.getVariableMetadata(variableId);
        } catch (VariableNotFoundException e) {
            throw new MetadataException("The layer " + layerName + " does not exist", e);
        }
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
            day = TimeUtils.iso8601ToDateTime(dayStr, temporalDomain.getChronology());
        } catch (BadTimeFormatException e) {
            throw new MetadataException("\"day\" parameter must be an ISO-formatted date");
        }

        if (temporalDomain instanceof TimeAxis) {
            TimeAxis timeAxis = (TimeAxis) temporalDomain;
            for (DateTime time : timeAxis.getCoordinateValues()) {
                if (TimeUtils.onSameDay(day, time)) {
                    timesteps.put(TimeUtils.formatUtcIsoTimeOnly(time));
                }
            }
        } else {
            throw new MetadataException(
                    "timesteps can only be returned for layers with a discrete time domain");
        }
        response.put("timesteps", timesteps);
        return response.toString();
    }

    protected String showMinMax(RequestParams params, WmsCatalogue catalogue)
            throws MetadataException {
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
        if (layerNames.length != 1 || (styleNames != null && styleNames.length > 1)) {
            /*
             * TODO Perhaps relax this restriction and return min/max with layer
             * IDs?
             */
            throw new MetadataException("Can only find min/max for exactly one layer at a time");
        }

        VariableMetadata variableMetadata;
        String datasetId;
        try {
            variableMetadata = WmsUtils.getVariableMetadataFromLayerName(layerNames[0], catalogue);
            datasetId = WmsUtils.getDatasetFromLayerName(layerNames[0], catalogue).getId();
            if (catalogue.isDisabled(layerNames[0])) {
                throw new EdalLayerNotFoundException(
                        "The layer " + layerNames[0] + " is not enabled on this server");
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
        String styleName = null;
        if (styleNames != null && styleNames.length > 0) {
            /*
             * Specified as a URL parameter
             */
            styleName = styleNames[0];
            if (!catalogue.getStyleCatalogue()
                    .getSupportedStyles(variableMetadata, catalogue.getLayerNameMapper())
                    .contains(styleName)) {
                throw new MetadataException("Cannot find min-max for this layer.  The style "
                        + styleName + " is not supported.");
            }
        } else {
            /*
             * The default style
             */
            Collection<String> supportedStyles = catalogue.getStyleCatalogue()
                    .getSupportedStyles(variableMetadata, catalogue.getLayerNameMapper());
            for (String supportedStyle : supportedStyles) {
                if (supportedStyle.startsWith("default")) {
                    styleName = supportedStyle;
                }
            }
            if (styleName == null) {
                throw new MetadataException(
                        "Cannot find min-max for this layer.  No default styles are supported.");
            }
        }

        /*
         * Now find which layer the scale is being applied to
         */
        List<String> scaledLayerRoles = catalogue.getStyleCatalogue()
                .getScaledRoleForStyle(styleName);
        String scaledLayerRole = null;
        if (scaledLayerRoles.size() > 0) {
            scaledLayerRole = scaledLayerRoles.get(0);
        }
        if (scaledLayerRole == null) {
            /*
             * No layer has scaling - we can return anything
             */
            minmax.put("min", 0);
            minmax.put("max", 100);
            return minmax.toString();
        } else if ("".equals(scaledLayerRole)) {
            /*
             * The named (possibly parent) layer is scaled.
             */
            layerName = layerNames[0];
        } else {
            /*
             * A child layer is being scaled. Get the WMS layer name
             * corresponding to this child variable
             */
            String variableId = variableMetadata.getChildWithRole(scaledLayerRole).getId();
            layerName = catalogue.getLayerNameMapper().getLayerName(datasetId, variableId);
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
                    if (value != null && !Double.isNaN(value.doubleValue())) {
                        max = Math.max(max, value.doubleValue());
                        min = Math.min(min, value.doubleValue());
                    }
                }
            } else if (f instanceof PointFeature) {
                PointFeature pointFeature = (PointFeature) f;
                Number value = pointFeature.getValues(featuresAndMember.getMember()).get(0);
                if (value != null) {
                    max = Math.max(max, value.doubleValue());
                    min = Math.min(min, value.doubleValue());
                }
            } else if (f instanceof TrajectoryFeature) {
                TrajectoryFeature trajectoryFeature = (TrajectoryFeature) f;
                Array1D<Number> values = trajectoryFeature.getValues(featuresAndMember.getMember());
                Iterator<Number> it = values.iterator();
                while (it.hasNext()) {
                    Number value = it.next();
                    if (value != null) {
                        max = Math.max(max, value.doubleValue());
                        min = Math.min(min, value.doubleValue());
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
                if (min > max) {
                    double t = min;
                    min = max;
                    max = t;
                }
            }
        }

        /*
         * Limit output to 4s.f
         */
        minmax.put("min", GraphicsUtils.roundToSignificantFigures(min, 4));
        minmax.put("max", GraphicsUtils.roundToSignificantFigures(max, 4));

        return minmax.toString();
    }

    protected String showAnimationTimesteps(RequestParams params, WmsCatalogue catalogue)
            throws MetadataException {
        String layerName = params.getString("layerName");
        if (layerName == null) {
            throw new MetadataException(
                    "Must supply a LAYERNAME parameter to get animation timesteps");
        }

        Dataset dataset;
        String variableId;
        try {
            dataset = WmsUtils.getDatasetFromLayerName(layerName, catalogue);
            variableId = catalogue.getLayerNameMapper().getVariableIdFromLayerName(layerName);
            if (catalogue.isDisabled(layerName)) {
                throw new EdalLayerNotFoundException(
                        "The layer " + layerName + " is not enabled on this server");
            }
        } catch (EdalLayerNotFoundException e) {
            throw new MetadataException("The layer " + layerName + " does not exist", e);
        }
        VariableMetadata variableMetadata;
        try {
            variableMetadata = dataset.getVariableMetadata(variableId);
        } catch (VariableNotFoundException e) {
            throw new MetadataException("The layer " + layerName + " does not exist", e);
        }
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
                startIndex = timeAxis.findIndexOf(
                        TimeUtils.iso8601ToDateTime(startStr, timeAxis.getChronology()));
                endIndex = timeAxis
                        .findIndexOf(TimeUtils.iso8601ToDateTime(endStr, timeAxis.getChronology()));
            } catch (BadTimeFormatException e) {
                throw new MetadataException("Time string is not ISO8601 formatted");
            }
            if (startIndex < 0 || endIndex < 0) {
                throw new MetadataException(
                        "For animation timesteps, both start and end times must be part of the axis");
            }

            JSONObject response = new JSONObject();
            JSONArray timeStrings = new JSONArray();
            List<DateTime> tValues = timeAxis.getCoordinateValues();

            JSONObject timestep = new JSONObject();
            timestep.put("title", "Full (" + (endIndex - startIndex + 1) + " frames)");
            timestep.put("timeString", startStr + "/" + endStr);
            timeStrings.put(timestep);
            addTimeStringToJson(AnimationStep.DAILY, timeStrings, tValues, startIndex, endIndex);
            addTimeStringToJson(AnimationStep.WEEKLY, timeStrings, tValues, startIndex, endIndex);
            addTimeStringToJson(AnimationStep.MONTHLY, timeStrings, tValues, startIndex, endIndex);
            addTimeStringToJson(AnimationStep.BIMONTHLY, timeStrings, tValues, startIndex,
                    endIndex);
            addTimeStringToJson(AnimationStep.BIANNUALLY, timeStrings, tValues, startIndex,
                    endIndex);
            addTimeStringToJson(AnimationStep.YEARLY, timeStrings, tValues, startIndex, endIndex);

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

    /**
     * Enum defining the possibilities for animation timesteps, including the
     * label, the period string for ISO8601, and the joda time period which
     * corresponds to it.
     *
     * @author Guy Griffiths
     */
    private enum AnimationStep {
        DAILY("Daily", "P1D", new Period().withDays(1)),

        WEEKLY("Weekly", "P7D", new Period().withDays(7)),

        MONTHLY("Monthly", "P1M", new Period().withMonths(1)),

        BIMONTHLY("Bi-monthly", "P2M", new Period().withMonths(2)),

        BIANNUALLY("Six-monthly", "P6M", new Period().withMonths(6)),

        YEARLY("Yearly", "P1Y", new Period().withYears(1));

        final String label;
        final String periodString;
        final Period period;

        private AnimationStep(String label, String periodString, Period period) {
            this.label = label;
            this.periodString = periodString;
            this.period = period;
        }
    }

    private static void addTimeStringToJson(AnimationStep step, JSONArray jsonArray,
            List<DateTime> tValues, int startIndex, int endIndex) {
        /*
         * First we calculate how many timesteps would be displayed, so that we
         * can only include ones which would be animated
         */
        int nSteps = 1;
        DateTime thisdt = tValues.get(startIndex);
        DateTime lastdt = tValues.get(startIndex);
        for (int i = startIndex + 1; i <= endIndex; i++) {
            thisdt = tValues.get(i);
            if (!thisdt.isBefore(lastdt.plus(step.period))) {
                nSteps++;
                lastdt = thisdt;
            }
        }
        if (nSteps > 1) {
            String timeString = TimeUtils.dateTimeToISO8601(tValues.get(startIndex)) + "/"
                    + TimeUtils.dateTimeToISO8601(tValues.get(endIndex)) + "/" + step.periodString;
            JSONObject timestep = new JSONObject();
            timestep.put("title", step.label + " (" + nSteps + " frames)");
            timestep.put("timeString", timeString);
            jsonArray.put(timestep);
        }
    }

    protected void getLegendGraphic(RequestParams params, HttpServletResponse httpServletResponse,
            WmsCatalogue catalogue) throws EdalException {
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
            int width = params.getPositiveInt("width", 100);
            int height = params.getPositiveInt("height", 400);
            /*
             * Find the requested colour palette, or use the default if not set
             */
            SegmentColourScheme colourScheme = new SegmentColourScheme(
                    new ScaleRange(Extents.newExtent(0f, 1f), false), Color.black, Color.black,
                    Color.black, paletteName, numColourBands);
            legend = colourScheme.getScaleBar(width, height, 0.0f, vertical, false, null, null);
        } else {
            /*
             * We're creating a legend with supporting text so we need to know
             * the colour scale range and the layer in question
             */
            GetMapStyleParams getMapStyleParameters;
            try {
                getMapStyleParameters = new GetMapStyleParams(params, catalogue);
            } catch (EdalLayerNotFoundException e) {
                throw new MetadataException(
                        "Requested layer is either not present, disabled, or not yet loaded.");
            } catch (Exception e) {
                throw new MetadataException(
                        "You must specify either SLD, SLD_BODY, LAYERS and STYLES, or LAYER and STYLE for a full legend.  You may set COLORBARONLY=true to just generate a colour bar");
            }
            /*
             * Test whether we have categorical data - this will generate a
             * different style of legend
             */
            Map<Integer, Category> categories = null;
            /*
             * We want to treat vector layers as a special case - we don't want
             * to generate a full 2D legend
             */
            boolean isVector = false;
            if (getMapStyleParameters.getNumLayers() == 1) {
                VariableMetadata metadata = WmsUtils.getVariableMetadataFromLayerName(
                        getMapStyleParameters.getLayerNames()[0], catalogue);
                categories = metadata.getParameter().getCategories();
                isVector = metadata.getChildWithRole(VectorPlugin.DIR_ROLE) != null;
            }
            MapImage imageGenerator = getMapStyleParameters.getImageGenerator(catalogue);
            if (categories != null) {
                /*
                 * We have categorical data
                 */
                legend = GraphicsUtils.drawCategoricalLegend(categories);
            } else {
                /*
                 * We have non-categorical data - use the MapImage to generate a
                 * legend
                 */
                int height = params.getPositiveInt("height", 400);
                int width;
                if (imageGenerator.getFieldsWithScales().size() > 1 && !isVector) {
                    width = params.getPositiveInt("width", height);
                } else {
                    width = params.getPositiveInt("width", 50);
                }
                legend = imageGenerator.getLegend(width, height, isVector);
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

    protected void getTimeseries(RequestParams params, HttpServletResponse httpServletResponse,
            WmsCatalogue catalogue) throws EdalException {
        GetPlotParameters getPlotParameters = new GetPlotParameters(params, catalogue);
        PlottingDomainParams plottingParameters = getPlotParameters.getPlottingDomainParameters();
        final HorizontalPosition position = getPlotParameters.getClickedPosition();

        String[] layerNames = getPlotParameters.getLayerNames();
        String outputFormat = getPlotParameters.getInfoFormat();
        if (!"image/png".equalsIgnoreCase(outputFormat)
                && !"image/jpeg".equalsIgnoreCase(outputFormat)
                && !"image/jpg".equalsIgnoreCase(outputFormat)
                && !"text/csv".equalsIgnoreCase(outputFormat)
                && !"text/json".equalsIgnoreCase(outputFormat)
                && !"application/prs.coverage+json".equalsIgnoreCase(outputFormat)
                && !"application/prs.coverage json".equalsIgnoreCase(outputFormat)) {
            throw new InvalidFormatException(
                    outputFormat + " is not a valid output format for a timeseries plot");
        }
        /*
         * Loop over all requested layers
         */
        List<PointSeriesFeature> timeseriesFeatures = new ArrayList<PointSeriesFeature>();
        Map<String, Set<String>> datasets2VariableIds = new HashMap<>();
        /*
         * We want to store the copyright information to output
         */
        StringBuilder copyright = new StringBuilder();
        Map<String, String> varId2Title = new HashMap<>();
        Set<String> copyrights = new LinkedHashSet<>();
        for (String layerName : layerNames) {
            Dataset dataset = WmsUtils.getDatasetFromLayerName(layerName, catalogue);
            VariableMetadata variableMetadata = WmsUtils.getVariableMetadataFromLayerName(layerName,
                    catalogue);

            if (("text/csv".equalsIgnoreCase(outputFormat)
                    || "text/json".equalsIgnoreCase(outputFormat)
                    || "application/prs.coverage+json".equalsIgnoreCase(outputFormat)
                    || "application/prs.coverage json".equalsIgnoreCase(outputFormat))
                    && !catalogue.isDownloadable(layerName)) {
                throw new LayerNotQueryableException(
                        "The layer: " + layerName + " can only be downloaded as an image");
            }
            EnhancedVariableMetadata layerMetadata = catalogue.getLayerMetadata(variableMetadata);
            String layerCopyright = layerMetadata.getCopyright();
            if (layerCopyright != null && !"".equals(layerCopyright)) {
                copyrights.add(layerCopyright);
            }
            if (variableMetadata.isScalar()) {
                varId2Title.put(variableMetadata.getId(), layerMetadata.getTitle());
                if (!datasets2VariableIds.containsKey(dataset.getId())) {
                    datasets2VariableIds.put(dataset.getId(), new LinkedHashSet<String>());
                }
                datasets2VariableIds.get(dataset.getId()).add(variableMetadata.getId());
            } else {
                Set<VariableMetadata> children = variableMetadata.getChildren();
                for (VariableMetadata child : children) {
                    EnhancedVariableMetadata childLayerMetadata = catalogue.getLayerMetadata(child);
                    varId2Title.put(child.getId(), childLayerMetadata.getTitle());
                    if (!datasets2VariableIds.containsKey(dataset.getId())) {
                        datasets2VariableIds.put(dataset.getId(), new LinkedHashSet<String>());
                    }
                    datasets2VariableIds.get(dataset.getId()).add(child.getId());
                }
            }
        }

        for (String layerCopyright : copyrights) {
            copyright.append(layerCopyright);
            copyright.append('\n');
        }
        if (copyright.length() > 0) {
            copyright.deleteCharAt(copyright.length() - 1);
        }

        for (Entry<String, Set<String>> entry : datasets2VariableIds.entrySet()) {
            Dataset dataset = catalogue.getDatasetFromId(entry.getKey());
            List<? extends PointSeriesFeature> extractedTimeseriesFeatures = dataset
                    .extractTimeseriesFeatures(entry.getValue(), plottingParameters.getBbox(),
                            plottingParameters.getZExtent(), plottingParameters.getTExtent(),
                            plottingParameters.getTargetHorizontalPosition(),
                            plottingParameters.getTargetZ());
            timeseriesFeatures.addAll(extractedTimeseriesFeatures);
        }

        while (timeseriesFeatures.size() > getPlotParameters.getFeatureCount()) {
            timeseriesFeatures.remove(timeseriesFeatures.size() - 1);
        }

        httpServletResponse.setContentType(outputFormat);
        if ("text/json".equalsIgnoreCase(outputFormat)
                || "application/prs.coverage+json".equalsIgnoreCase(outputFormat)
                || "application/prs.coverage json".equalsIgnoreCase(outputFormat)) {
            if (timeseriesFeatures.size() > 1) {
                throw new IncorrectDomainException(
                        "JSON export is only supported for gridded data");
            }
            CoverageJsonConverter converter = new CoverageJsonConverterImpl();

            converter.checkFeaturesSupported(timeseriesFeatures);
            try {
                converter.convertFeatureToJson(httpServletResponse.getOutputStream(),
                        timeseriesFeatures.get(0));
            } catch (IOException e) {
                log.error("Cannot write to output stream", e);
                throw new EdalException("Problem writing data to output stream", e);
            }
        } else if ("text/csv".equalsIgnoreCase(outputFormat)) {
            if (timeseriesFeatures.size() > 1) {
                throw new IncorrectDomainException("CSV export is only supported for gridded data");
            }
            try {
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(httpServletResponse.getOutputStream()));
                PointSeriesFeature feature = timeseriesFeatures.get(0);
                Set<String> parameterIds = feature.getVariableIds();
                HorizontalPosition pos = feature.getHorizontalPosition();
                /*
                 * If we have a copyright message, split it at semicolons and
                 * add it as a comment
                 */
                if (copyright.length() > 0) {
                    StringBuilder copyrightMessage = new StringBuilder();
                    String[] copyrightLines = copyright.toString().split(";");
                    for (String copyrightLine : copyrightLines) {
                        copyrightMessage.append("# " + copyrightLine + "\n");
                    }
                    writer.write(copyrightMessage.toString());
                }
                if (GISUtils.isWgs84LonLat(pos.getCoordinateReferenceSystem())) {
                    writer.write("# Latitude: " + pos.getY() + "\n");
                    writer.write("# Longitude: " + pos.getX() + "\n");
                } else {
                    writer.write("# X: " + pos.getX() + "\n");
                    writer.write("# Y: " + pos.getY() + "\n");
                }
                StringBuilder headerLine = new StringBuilder("Time (UTC),");
                StringBuilder filename = new StringBuilder();
                for (String parameterId : parameterIds) {
                    headerLine.append(varId2Title.get(parameterId) + " ("
                            + feature.getParameter(parameterId).getUnits() + "),");
                    filename.append(parameterId + "-");
                }
                filename.append("timeseries.csv");
                httpServletResponse.setHeader("Content-Disposition",
                        "attachment; filename=\"" + filename + "\"");
                writer.write(headerLine.substring(0, headerLine.length() - 1) + "\n");
                TimeAxis axis = feature.getDomain();
                for (int i = 0; i < axis.size(); i++) {
                    StringBuilder dataLine = new StringBuilder(
                            TimeUtils.dateTimeToISO8601(axis.getCoordinateValues().get(i)) + ",");
                    for (String parameterId : parameterIds) {
                        dataLine.append(feature.getValues(parameterId).get(i) + ",");
                    }
                    writer.write(dataLine.substring(0, dataLine.length() - 1) + "\n");
                }
                writer.close();
            } catch (IOException e) {
                log.error("Cannot write to output stream", e);
                throw new EdalException("Problem writing data to output stream", e);
            }
        } else {
            int width = params.getPositiveInt("chartwidth", 700);
            int height = params.getPositiveInt("chartheight", 600);

            /* Now create the vertical profile plot */
            JFreeChart chart = Charting.createTimeSeriesPlot(timeseriesFeatures, position,
                    copyright.toString());
            try {
                if ("image/png".equals(outputFormat)) {
                    ChartUtilities.writeChartAsPNG(httpServletResponse.getOutputStream(), chart,
                            width, height);
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
    }

    protected void getTransect(RequestParams params, HttpServletResponse httpServletResponse,
            WmsCatalogue catalogue) throws EdalException {
        String outputFormat = params.getMandatoryString("format");
        if (!"image/png".equals(outputFormat) && !"image/jpeg".equals(outputFormat)
                && !"image/jpg".equals(outputFormat)) {
            throw new InvalidFormatException(
                    outputFormat + " is not a valid output format for a transect plot");
        }
        String[] layers = params.getMandatoryString("layers").split(",");
        CoordinateReferenceSystem crs = GISUtils.getCrs(params.getMandatoryString("CRS"));
        LineString lineString = new LineString(params.getMandatoryString("linestring"), crs);
        String timeStr = params.getString("time");

        String elevationStr = params.getString("elevation");
        Double zValue = null;
        if (elevationStr != null) {
            zValue = Double.parseDouble(elevationStr);
        }
        StringBuilder copyright = new StringBuilder();
        Map<PointCollectionFeature, String> pointCollectionFeatures2Labels = new HashMap<>();
        /* Do we also want to plot a vertical section plot? */
        boolean verticalSection = false;
        List<HorizontalPosition> verticalSectionHorizontalPositions = new ArrayList<>();
        DiscreteLayeredDataset<?, ?> gridDataset = null;
        String varId = null;
        Set<String> copyrights = new LinkedHashSet<>();
        PlottingStyleParameters defaults = null;
        for (String layerName : layers) {
            Dataset dataset = WmsUtils.getDatasetFromLayerName(layerName, catalogue);
            if (dataset == null) {
                throw new EdalLayerNotFoundException(
                        "The layer " + layerName + " was not found on this server");
            }
            if (dataset instanceof DiscreteLayeredDataset<?, ?>) {
                gridDataset = (DiscreteLayeredDataset<?, ?>) dataset;
                varId = catalogue.getLayerNameMapper().getVariableIdFromLayerName(layerName);
                EnhancedVariableMetadata layerMetadata = WmsUtils.getLayerMetadata(layerName,
                        catalogue);
                String layerCopyright = layerMetadata.getCopyright();
                defaults = layerMetadata.getDefaultPlottingParameters();
                if (layerCopyright != null && !"".equals(layerCopyright)) {
                    copyrights.add(layerCopyright);
                }

                VariableMetadata metadata = gridDataset.getVariableMetadata(varId);
                VerticalDomain verticalDomain = metadata.getVerticalDomain();
                final VerticalPosition zPos;
                if (zValue != null && verticalDomain != null) {
                    zPos = new VerticalPosition(zValue, verticalDomain.getVerticalCrs());
                } else {
                    zPos = null;
                }
                if (verticalDomain != null && layers.length == 1) {
                    verticalSection = true;
                }

                final DateTime time;
                TemporalDomain temporalDomain = metadata.getTemporalDomain();
                if (timeStr != null) {
                    time = TimeUtils.iso8601ToDateTime(timeStr, temporalDomain.getChronology());
                } else {
                    time = null;
                }
                HorizontalDomain hDomain = metadata.getHorizontalDomain();
                final List<HorizontalPosition> transectPoints;
                if (hDomain instanceof HorizontalGrid) {
                    transectPoints = GISUtils.getOptimalTransectPoints((HorizontalGrid) hDomain,
                            lineString);
                } else {
                    transectPoints = lineString.getPointsOnPath(AXIS_RESOLUTION);
                }
                if (verticalSection) {
                    verticalSectionHorizontalPositions = transectPoints;
                }

                PointCollectionDomain pointCollectionDomain = new PointCollectionDomain(
                        transectPoints, zPos, time);

                PointCollectionFeature feature = gridDataset.extractPointCollection(
                        CollectionUtils.setOf(varId), pointCollectionDomain);
                pointCollectionFeatures2Labels.put(feature,
                        catalogue.getLayerMetadata(metadata).getTitle());
            } else {
                throw new EdalUnsupportedOperationException(
                        "Only gridded datasets are supported for transect plots");
            }
        }

        if (defaults == null) {
            defaults = new PlottingStyleParameters(new ArrayList<>(), "default", Color.black,
                    Color.black, new Color(0, true), false, ColourPalette.MAX_NUM_COLOURS, 1f);
        }

        for (String layerCopyright : copyrights) {
            copyright.append(layerCopyright);
            copyright.append('\n');
        }
        if (copyright.length() > 0) {
            copyright.deleteCharAt(copyright.length() - 1);
        }

        JFreeChart chart = Charting.createTransectPlot(pointCollectionFeatures2Labels, lineString,
                false, copyright.toString());// , catalogue.getLayerNameMapper());

        if (verticalSection) {
            /*
             * This can only be true if we have an AbstractGridDataset, so we
             * can use our previous cast
             */
            String paletteName = params.getString("palette", defaults.getPalette());
            int numColourBands = params.getPositiveInt("numcolorbands",
                    defaults.getNumColorBands());

            /*
             * define an extent for the vertical section if parameter present
             */
            String sectionElevationStr = params.getString("section-elevation");
            Extent<Double> zExtent = extractSectionElevation(sectionElevationStr);

            List<ProfileFeature> profileFeatures = new ArrayList<>();
            TemporalDomain temporalDomain = gridDataset.getVariableMetadata(varId)
                    .getTemporalDomain();
            DateTime time = null;
            if (timeStr != null) {
                time = TimeUtils.iso8601ToDateTime(timeStr, temporalDomain.getChronology());
            }
            for (HorizontalPosition pos : verticalSectionHorizontalPositions) {
                PlottingDomainParams plottingParams = new PlottingDomainParams(1, 1, null, null,
                        null, pos, null, time);
                List<? extends ProfileFeature> features = gridDataset.extractProfileFeatures(
                        CollectionUtils.setOf(varId), plottingParams.getBbox(),
                        plottingParams.getZExtent(), plottingParams.getTExtent(),
                        plottingParams.getTargetHorizontalPosition(), plottingParams.getTargetT());
                profileFeatures.addAll(features);
            }

            Extent<Float> scaleRange;
            if (zExtent != null) {
                scaleRange = getExtentOfFeatures(profileFeatures);
            } else {
                List<Extent<Float>> scaleRanges = GetMapStyleParams.getColorScaleRanges(params,
                        defaults.getColorScaleRange());
                if (scaleRanges == null || scaleRanges.isEmpty()) {
                    scaleRange = GraphicsUtils.estimateValueRange(gridDataset, varId);
                } else {
                    scaleRange = scaleRanges.get(0);
                    if (scaleRange == null || scaleRange.isEmpty()) {
                        scaleRange = GraphicsUtils.estimateValueRange(gridDataset, varId);
                    }
                }
            }
            ScaleRange colourScale = new ScaleRange(scaleRange.getLow(), scaleRange.getHigh(),
                    params.getBoolean("logscale", defaults.isLogScaling()));
            ColourScheme colourScheme = new SegmentColourScheme(colourScale,
                    GraphicsUtils.parseColour(params.getString("belowmincolor",
                            GraphicsUtils.colourToString(defaults.getBelowMinColour()))),
                    GraphicsUtils.parseColour(params.getString("abovemaxcolor",
                            GraphicsUtils.colourToString(defaults.getAboveMaxColour()))),
                    GraphicsUtils.parseColour(params.getString("bgcolor",
                            GraphicsUtils.colourToString(defaults.getNoDataColour()))),
                    paletteName, numColourBands);

            JFreeChart verticalSectionChart = Charting.createVerticalSectionChart(profileFeatures,
                    lineString, colourScheme, zValue, zExtent);
            chart = Charting.addVerticalSectionChart(chart, verticalSectionChart);
        }
        int width = params.getPositiveInt("width", 700);
        int height = params.getPositiveInt("height", verticalSection ? 1000 : 600);

        httpServletResponse.setContentType(outputFormat);
        try {
            if ("image/png".equals(outputFormat)) {
                ChartUtilities.writeChartAsPNG(httpServletResponse.getOutputStream(), chart, width,
                        height);
            } else {
                /* Must be a JPEG */
                ChartUtilities.writeChartAsJPEG(httpServletResponse.getOutputStream(), chart, width,
                        height);
            }
        } catch (IOException e) {
            log.error("Cannot write to output stream", e);
            throw new EdalException("Problem writing data to output stream", e);
        }
    }

    protected Extent<Double> extractSectionElevation(String depthString) {
        Extent<Double> zExtent = null;
        if (depthString != null && !depthString.trim().equals("")) {
            String[] depthStrings = depthString.split("/");
            if (depthStrings.length == 2) {
                try {
                    zExtent = Extents.newExtent(Double.parseDouble(depthStrings[0]),
                            Double.parseDouble(depthStrings[1]));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Section elevation format (number/number) is wrong: " + depthString);
                }
            } else {
                throw new IllegalArgumentException(
                        "Section elevation must be a range (number/number)");
            }
        }
        return zExtent;
    }

    private Extent<Float> getExtentOfFeatures(List<ProfileFeature> features) {
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        for (ProfileFeature feature : features) {
            for (String paramId : feature.getVariableIds()) {
                Array1D<Number> values = feature.getValues(paramId);
                int size = (int) values.size();
                for (int i = 0; i < size; i++) {
                    Number number = values.get(i);
                    if (number != null) {
                        if (number.doubleValue() > max) {
                            max = number.floatValue();
                        }
                        if (number.doubleValue() < min) {
                            min = number.floatValue();
                        }
                    }
                }
            }
        }

        return Extents.newExtent(min, max);
    }

    protected void getVerticalProfile(RequestParams params, HttpServletResponse httpServletResponse,
            WmsCatalogue catalogue) throws EdalException {
        GetPlotParameters getPlotParameters = new GetPlotParameters(params, catalogue);
        PlottingDomainParams plottingParameters = getPlotParameters.getPlottingDomainParameters();
        final HorizontalPosition position = getPlotParameters.getClickedPosition();

        String[] layerNames = getPlotParameters.getLayerNames();
        String outputFormat = getPlotParameters.getInfoFormat();
        if (!"image/png".equalsIgnoreCase(outputFormat)
                && !"image/jpeg".equalsIgnoreCase(outputFormat)
                && !"image/jpg".equalsIgnoreCase(outputFormat)
                && !"text/csv".equalsIgnoreCase(outputFormat)
                && !"text/json".equalsIgnoreCase(outputFormat)
                && !"application/prs.coverage+json".equalsIgnoreCase(outputFormat)
                && !"application/prs.coverage json".equalsIgnoreCase(outputFormat)) {
            throw new InvalidFormatException(
                    outputFormat + " is not a valid output format for a profile plot");
        }

        /*
         * Loop over all requested layers
         */
        List<ProfileFeature> profileFeatures = new ArrayList<ProfileFeature>();
        Map<String, Set<String>> datasets2VariableIds = new HashMap<>();
        /*
         * We want to store the copyright information to output
         */
        StringBuilder copyright = new StringBuilder();
        Map<String, String> varId2Title = new HashMap<>();
        Set<String> copyrights = new LinkedHashSet<>();
        String xLabel = null;
        for (String layerName : layerNames) {
            Dataset dataset = WmsUtils.getDatasetFromLayerName(layerName, catalogue);
            VariableMetadata variableMetadata = WmsUtils.getVariableMetadataFromLayerName(layerName,
                    catalogue);
            xLabel = catalogue.getLayerMetadata(variableMetadata).getTitle();

            if (("text/csv".equalsIgnoreCase(outputFormat)
                    || "text/json".equalsIgnoreCase(outputFormat)
                    || "application/prs.coverage+json".equalsIgnoreCase(outputFormat)
                    || "application/prs.coverage json".equalsIgnoreCase(outputFormat))
                    && !catalogue.isDownloadable(layerName)) {
                throw new LayerNotQueryableException(
                        "The layer: " + layerName + " can only be downloaded as an image");
            }
            EnhancedVariableMetadata layerMetadata = catalogue.getLayerMetadata(variableMetadata);
            String layerCopyright = layerMetadata.getCopyright();
            if (layerCopyright != null && !"".equals(layerCopyright)) {
                copyrights.add(layerCopyright);
            }
            if (variableMetadata.isScalar()) {
                varId2Title.put(variableMetadata.getId(), layerMetadata.getTitle());
                if (!datasets2VariableIds.containsKey(dataset.getId())) {
                    datasets2VariableIds.put(dataset.getId(), new LinkedHashSet<String>());
                }
                datasets2VariableIds.get(dataset.getId()).add(variableMetadata.getId());
            } else {
                Set<VariableMetadata> children = variableMetadata.getChildren();
                for (VariableMetadata child : children) {
                    EnhancedVariableMetadata childLayerMetadata = catalogue.getLayerMetadata(child);
                    varId2Title.put(child.getId(), childLayerMetadata.getTitle());
                    if (!datasets2VariableIds.containsKey(dataset.getId())) {
                        datasets2VariableIds.put(dataset.getId(), new LinkedHashSet<String>());
                    }
                    datasets2VariableIds.get(dataset.getId()).add(child.getId());
                }
            }
        }

        for (String layerCopyright : copyrights) {
            copyright.append(layerCopyright);
            copyright.append('\n');
        }
        if (copyright.length() > 0) {
            copyright.deleteCharAt(copyright.length() - 1);
        }

        for (Entry<String, Set<String>> entry : datasets2VariableIds.entrySet()) {
            Dataset dataset = catalogue.getDatasetFromId(entry.getKey());
            List<? extends ProfileFeature> extractedProfileFeatures = dataset
                    .extractProfileFeatures(entry.getValue(), plottingParameters.getBbox(),
                            plottingParameters.getZExtent(), plottingParameters.getTExtent(),
                            plottingParameters.getTargetHorizontalPosition(),
                            plottingParameters.getTargetT());
            profileFeatures.addAll(extractedProfileFeatures);
        }

        while (profileFeatures.size() > getPlotParameters.getFeatureCount()) {
            profileFeatures.remove(profileFeatures.size() - 1);
        }

        httpServletResponse.setContentType(outputFormat);

        if ("text/json".equalsIgnoreCase(outputFormat)
                || "application/prs.coverage+json".equalsIgnoreCase(outputFormat)
                || "application/prs.coverage json".equalsIgnoreCase(outputFormat)) {
            if (profileFeatures.size() > 1) {
                throw new IncorrectDomainException(
                        "JSON export is only supported for gridded data");
            }
            CoverageJsonConverter converter = new CoverageJsonConverterImpl();

            converter.checkFeaturesSupported(profileFeatures);
            try {
                converter.convertFeatureToJson(httpServletResponse.getOutputStream(),
                        profileFeatures.get(0));
            } catch (IOException e) {
                log.error("Cannot write to output stream", e);
                throw new EdalException("Problem writing data to output stream", e);
            }
        } else if ("text/csv".equalsIgnoreCase(outputFormat)) {
            if (profileFeatures.size() > 1) {
                throw new IncorrectDomainException("CSV export is only supported for gridded data");
            }
            try {
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(httpServletResponse.getOutputStream()));
                ProfileFeature feature = profileFeatures.get(0);
                Set<String> parameterIds = feature.getVariableIds();
                HorizontalPosition pos = feature.getHorizontalPosition();
                /*
                 * If we have a copyright message, split it at semicolons and
                 * add it as a comment
                 */
                if (copyright.length() > 0) {
                    StringBuilder copyrightMessage = new StringBuilder();
                    String[] copyrightLines = copyright.toString().split(";");
                    for (String copyrightLine : copyrightLines) {
                        copyrightMessage.append("# " + copyrightLine + "\n");
                    }
                    writer.write(copyrightMessage.toString());
                }
                if (GISUtils.isWgs84LonLat(pos.getCoordinateReferenceSystem())) {
                    writer.write("# Latitude: " + pos.getY() + "\n");
                    writer.write("# Longitude: " + pos.getX() + "\n");
                } else {
                    writer.write("# X: " + pos.getX() + "\n");
                    writer.write("# Y: " + pos.getY() + "\n");
                }
                StringBuilder headerLine = new StringBuilder("Z,");
                StringBuilder filename = new StringBuilder();
                for (String parameterId : parameterIds) {
                    headerLine.append(varId2Title.get(parameterId) + " ("
                            + feature.getParameter(parameterId).getUnits() + "),");
                    filename.append(parameterId + "-");
                }
                filename.append("profile.csv");
                httpServletResponse.setHeader("Content-Disposition",
                        "attachment; filename=\"" + filename + "\"");
                writer.write(headerLine.substring(0, headerLine.length() - 1) + "\n");
                VerticalAxis axis = feature.getDomain();
                for (int i = 0; i < axis.size(); i++) {
                    StringBuilder dataLine = new StringBuilder(
                            axis.getCoordinateValues().get(i) + ",");
                    for (String parameterId : parameterIds) {
                        dataLine.append(feature.getValues(parameterId).get(i) + ",");
                    }
                    writer.write(dataLine.substring(0, dataLine.length() - 1) + "\n");
                }
                writer.close();
            } catch (IOException e) {
                log.error("Cannot write to output stream", e);
                throw new EdalException("Problem writing data to output stream", e);
            }
        } else {
            int width = params.getPositiveInt("chartwidth", 700);
            int height = params.getPositiveInt("chartheight", 600);

            /* Now create the vertical profile plot */
            JFreeChart chart = Charting.createVerticalProfilePlot(profileFeatures, xLabel, position,
                    copyright.toString());

            try {
                if ("image/png".equals(outputFormat)) {
                    ChartUtilities.writeChartAsPNG(httpServletResponse.getOutputStream(), chart,
                            width, height);
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
    protected void handleWmsException(EdalException exception,
            HttpServletResponse httpServletResponse, boolean v130) throws IOException {
        if (exception instanceof EdalLayerNotFoundException) {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        httpServletResponse.setContentType("text/xml");
        StackTraceElement[] stackTrace = exception.getStackTrace();
        StackTraceElement element = stackTrace[0];
        StringBuilder warningMessage = new StringBuilder(
                "Wms Exception caught: \"" + exception.getMessage() + "\" from:"
                        + element.getClassName() + ":" + element.getLineNumber());
        if (exception.getCause() != null) {
            Throwable cause = exception.getCause();
            warningMessage.append(" Cause: " + cause.getMessage());
        }
        log.warn(warningMessage.toString());

        VelocityContext context = new VelocityContext();
        EventCartridge ec = new EventCartridge();
        ec.addEventHandler(new EscapeXmlReference());
        ec.attachToContext(context);

        context.put("exception", exception);
        if (exception.getCause() != null) {
            context.put("cause", exception.getCause());
        }

        Template template;
        if (v130) {
            template = velocityEngine.getTemplate("templates/exception-1.3.0.vm");
        } else {
            template = velocityEngine.getTemplate("templates/exception-1.1.1.vm");
        }
        template.merge(context, httpServletResponse.getWriter());
    }

    public void setCrsCodes(String[] SupportedCrsCodes) {
        this.SupportedCrsCodes = SupportedCrsCodes;
    }

    public String[] getCrsCodes() {
        return SupportedCrsCodes;
    }
}
