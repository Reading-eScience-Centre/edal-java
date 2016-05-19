/**
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
 */

package uk.ac.rdg.resc.edal.wms.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.Chronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.JulianChronology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.graphics.exceptions.EdalLayerNotFoundException;
import uk.ac.rdg.resc.edal.graphics.style.util.EnhancedVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.util.chronologies.AllLeapChronology;
import uk.ac.rdg.resc.edal.util.chronologies.NoLeapChronology;
import uk.ac.rdg.resc.edal.util.chronologies.ThreeSixtyDayChronology;
import uk.ac.rdg.resc.edal.wms.WmsCatalogue;

/**
 * <p>
 * Collection of static utility methods that are useful in the WMS application.
 * </p>
 * 
 * <p>
 * Through the taglib definition /WEB-INF/taglib/wmsUtils.tld, some of these
 * functions are also available as JSP2.0 functions. For example:
 * </p>
 * <code>
 * <%@taglib uri="/WEB-INF/taglib/wmsUtils" prefix="utils"%>
 * </code>
 * 
 * @author Jon Blower
 * @author Guy Griffiths
 */
public class WmsUtils {
    private static final Logger log = LoggerFactory.getLogger(WmsUtils.class);
    /**
     * The versions of the WMS standard that this server supports
     */
    public static final Set<String> SUPPORTED_VERSIONS = new HashSet<String>();

    static {
        SUPPORTED_VERSIONS.add("1.1.1");
        SUPPORTED_VERSIONS.add("1.3.0");
    }

    /** Private constructor to prevent direct instantiation */
    private WmsUtils() {
        throw new AssertionError();
    }

    /**
     * Creates a directory, throwing an Exception if it could not be created and
     * it does not already exist.
     */
    public static void createDirectory(File dir) throws Exception {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                return;
            } else {
                throw new Exception(dir.getPath() + " already exists but it is a regular file");
            }
        } else {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new Exception("Could not create directory " + dir.getPath());
            }
        }
    }

    /**
     * @return true if the given location represents an OPeNDAP dataset. This
     *         method simply checks to see if the location string starts with
     *         "http://", "https://" or "dods://".
     */
    public static boolean isOpendapLocation(String location) {
        return location.startsWith("http://") || location.startsWith("dods://")
                || location.startsWith("https://");
    }

    /**
     * @return true if the given location represents an NcML aggregation.
     *         dataset. This method simply checks to see if the location string
     *         ends with ".xml" or ".ncml", following the same procedure as the
     *         Java NetCDF library.
     */
    public static boolean isNcmlAggregation(String location) {
        return location.endsWith(".xml") || location.endsWith(".ncml");
    }

    /**
     * Forwards the request to a third party. In this case this server is acting
     * as a proxy.
     * 
     * @param url
     *            The URL to the third party server (e.g.
     *            "http://myhost.com/ncWMS/wms")
     * @param request
     *            Http request object. All query string parameters (except
     *            "&url=") will be copied from this request object to the
     *            request to the third party server.
     * @param response
     *            Http response object
     */
    public static void proxyRequest(String url, HttpServletRequest request,
            HttpServletResponse response) {
        /* Download the data from the remote URL */
        StringBuffer fullURL = new StringBuffer(url);
        boolean firstTime = true;
        for (Object urlParamNameObj : request.getParameterMap().keySet()) {
            fullURL.append(firstTime ? "?" : "&");
            firstTime = false;
            String urlParamName = (String) urlParamNameObj;
            if (!urlParamName.equalsIgnoreCase("url")) {
                fullURL.append(urlParamName + "=" + request.getParameter(urlParamName));
            }
        }
        InputStream in = null;
        OutputStream out = null;
        try {
            /* TODO: better error handling */
            URLConnection conn = new URL(fullURL.toString()).openConnection();
            /* Set header information */
            for (int i = 0; i < conn.getHeaderFields().size(); i++) {
                response.setHeader(conn.getHeaderFieldKey(i), conn.getHeaderField(i));
            }
            in = conn.getInputStream();
            out = response.getOutputStream();
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) >= 0) {
                out.write(buf, 0, len);
            }
        } catch (MalformedURLException e) {
            log.error("Problem proxying request to: " + url, e);
        } catch (IOException e) {
            log.error("Problem proxying request to: " + url, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error(
                            "Problem with closing input stream while proxying request to: " + url,
                            e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("Problem with closing output stream while proxying request to: "
                            + url, e);
                }
            }
        }
    }

    /**
     * <p>
     * Returns the string to be used to display units for the TIME dimension in
     * Capabilities documents. For standard (ISO) chronologies, this will return
     * "ISO8601". For 360-day chronologies this will return "360_day". For other
     * chronologies this will return "unknown".
     * </p>
     */
    public static String getTimeAxisUnits(Chronology chronology) {
        if (chronology instanceof ISOChronology)
            return "ISO8601";
        // The following are the CF names for these calendars
        if (chronology instanceof JulianChronology)
            return "julian";
        if (chronology instanceof ThreeSixtyDayChronology)
            return "360_day";
        if (chronology instanceof NoLeapChronology)
            return "noleap";
        if (chronology instanceof AllLeapChronology)
            return "all_leap";
        return "unknown";
    }

    /**
     * Given a {@link WmsCatalogue}, returns the {@link Dataset} which
     * corresponds to a given layer name
     * 
     * @param layerName
     *            The name of the layer
     * @param catalogue
     *            The {@link WmsCatalogue} which holds the {@link Dataset}
     * @return The requested {@link Dataset}
     * @throws EdalLayerNotFoundException
     *             If the given layer name doesn't map to an available
     *             {@link Dataset}
     */
    public static Dataset getDatasetFromLayerName(String layerName, WmsCatalogue catalogue)
            throws EdalLayerNotFoundException {
        return catalogue.getDatasetFromId(catalogue.getLayerNameMapper().getDatasetIdFromLayerName(
                layerName));
    }

    /**
     * Given a {@link WmsCatalogue}, returns the {@link VariableMetadata} which
     * corresponds to a given layer name
     * 
     * @param layerName
     *            The name of the layer
     * @param catalogue
     *            The {@link WmsCatalogue} which holds the variable
     * @return The requested {@link VariableMetadata}
     * @throws EdalLayerNotFoundException
     *             If the given layer name doesn't map to an available
     *             {@link Dataset} and Variable combination
     */
    public static VariableMetadata getVariableMetadataFromLayerName(String layerName,
            WmsCatalogue catalogue) throws EdalLayerNotFoundException {
        String datasetId = catalogue.getLayerNameMapper().getDatasetIdFromLayerName(layerName);
        String varId = catalogue.getLayerNameMapper().getVariableIdFromLayerName(layerName);
        Dataset dataset = catalogue.getDatasetFromId(datasetId);
        if (dataset == null) {
            throw new EdalLayerNotFoundException("The layer " + layerName
                    + " was not found on this server");
        }
        return dataset.getVariableMetadata(varId);
    }

    /**
     * Given a {@link WmsCatalogue}, returns a {@link List} of styles supported
     * by the given layer name
     * 
     * @param layerName
     *            The name of the layer
     * @param catalogue
     *            The {@link WmsCatalogue} which holds the variable
     * @return A {@link Collection} of the names of the supported styles
     * @throws EdalLayerNotFoundException
     *             If the given layer name doesn't map to an available
     *             {@link Dataset} and Variable combination
     */
    public static Collection<String> getSupportedStylesForLayer(String layerName,
            WmsCatalogue catalogue) throws EdalLayerNotFoundException {
        VariableMetadata variableMetadata = getVariableMetadataFromLayerName(layerName, catalogue);
        return catalogue.getStyleCatalogue().getSupportedStyles(variableMetadata);
    }

    /**
     * Given a named layer and a {@link WmsCatalogue} which contains it, returns
     * the associated {@link EnhancedVariableMetadata}
     * 
     * @param layerName
     *            The name of the layer to get {@link EnhancedVariableMetadata}
     *            about
     * @param catalogue
     *            The {@link WmsCatalogue} containing the layer
     * @return The corresponding {@link EnhancedVariableMetadata}
     * @throws EdalLayerNotFoundException
     *             If the given layer name doesn't map to an available
     *             {@link Dataset} and Variable combination
     */
    public static EnhancedVariableMetadata getLayerMetadata(String layerName, WmsCatalogue catalogue)
            throws EdalLayerNotFoundException {
        return catalogue.getLayerMetadata(getVariableMetadataFromLayerName(layerName, catalogue));
    }
}
