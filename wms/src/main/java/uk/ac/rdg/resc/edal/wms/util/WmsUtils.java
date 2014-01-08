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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.JulianChronology;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.GridDataset;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.exceptions.InvalidCrsException;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.graphics.style.util.PlottingDomainParams;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RegularGrid;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.chronologies.AllLeapChronology;
import uk.ac.rdg.resc.edal.util.chronologies.NoLeapChronology;
import uk.ac.rdg.resc.edal.util.chronologies.ThreeSixtyDayChronology;

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
     * Gets a {@link RegularGrid} representing the image requested by a client
     * in a GetMap operation
     * 
     * @param dr
     *            Object representing a GetMap request
     * @return a RegularGrid representing the requested image
     */
    public static RegularGrid getImageGrid(PlottingDomainParams params) throws InvalidCrsException {
        BoundingBox bbox = params.getBbox();
        return new RegularGridImpl(bbox, params.getWidth(), params.getHeight());
    }

    /**
     * Utility method for getting the dataset ID from the given layer name
     */
    public static String getDatasetId(String layerName) throws EdalException {
        // Find which layer the user is requesting
        String[] layerParts = layerName.split("/");
        if (layerParts.length != 2) {
            throw new EdalException("Layers should be of the form Dataset/Variable");
        }
        return layerParts[0];
    }

    /**
     * Utility method for getting the member name from the given layer name
     */
    public static String getMemberName(String layerName) throws EdalException {
        // Find which layer the user is requesting
        String[] layerParts = layerName.split("/");
        if (layerParts.length != 2) {
            throw new EdalException("Layers should be of the form Dataset/Variable");
        }
        return layerParts[1];
    }

    /*
     * Starting here, we have methods which are only used in the Velocity
     * templates (mostly those defining Capabilities documents)
     * 
     * Don't delete them just because you can't find where they're used in Java
     * code
     */

    public static class StyleInfo {
        private String stylename;
        private String palettename;

        public StyleInfo(String stylename, String palettename) {
            super();
            this.stylename = stylename;
            this.palettename = palettename;
        }

        public String getStylename() {
            return stylename;
        }

        public String getPalettename() {
            return palettename;
        }

        @Override
        public String toString() {
            return stylename + "/" + palettename;
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

    public static HorizontalPosition getPositionFromUrlArgs(String crsCode, String firstCoord,
            String secondCoord, String wmsVersion) throws EdalException {
        final CoordinateReferenceSystem crs = GISUtils.getCrs(crsCode);

        boolean normalOrder = true;
        if (crsCode.equalsIgnoreCase("EPSG:4326") && "1.3.0".equals(wmsVersion)) {
            normalOrder = false;
        }

        double x, y;
        try {
            if (normalOrder) {
                x = Double.parseDouble(firstCoord);
                y = Double.parseDouble(secondCoord);
            } else {
                x = Double.parseDouble(secondCoord);
                y = Double.parseDouble(firstCoord);
            }
        } catch (NumberFormatException nfe) {
            throw new EdalException("Co-ordinates are not properly formatted");
        }
        return new HorizontalPosition(x, y, crs);
    }

    /**
     * Estimate the range of values in this layer by reading a sample of data
     * from the default time and elevation. Works for both Scalar and Vector
     * layers.
     * 
     * @return
     * @throws DataReadingException
     * @throws IOException
     *             if there was an error reading from the source data
     */
    public static Extent<Float> estimateValueRange(Dataset<?> dataset, String varId) {
        if (dataset instanceof GridDataset) {
            GridDataset gridDataset = (GridDataset) dataset;
            VariableMetadata variableMetadata = gridDataset.getVariableMetadata(varId);
            if (!variableMetadata.isScalar()) {
                /*
                 * We have a non-scalar variable. We will attempt to use the
                 * first child member to estimate the value range. This may not
                 * work in which case we ignore it - worst case scenario is that
                 * we end up with a bad scale range set - administrators can
                 * just override it.
                 */
                try {
                    variableMetadata = variableMetadata.getChildren().iterator().next();
                    varId = variableMetadata.getId();
                } catch (Exception e) {
                    /*
                     * Ignore this error and just generate a (probably)
                     * inaccurate range
                     */
                }
            }
            HorizontalGrid hGrid = new RegularGridImpl(variableMetadata.getHorizontalDomain()
                    .getBoundingBox(), 100, 100);
            Double zPos = null;
            if (variableMetadata.getVerticalDomain() != null) {
                zPos = variableMetadata.getVerticalDomain().getExtent().getLow();
            }
            DateTime time = null;
            if (variableMetadata.getTemporalDomain() != null) {
                time = variableMetadata.getTemporalDomain().getExtent().getHigh();
            }
            float min = Float.MAX_VALUE;
            float max = -Float.MAX_VALUE;
            try {
                MapFeature sampleData = gridDataset.readMapData(CollectionUtils.setOf(varId),
                        hGrid, zPos, time);
                Array2D<Number> values = sampleData.getValues(varId);
                if (values != null) {
                    for (Number value : values) {
                        if (value != null) {
                            min = (float) Math.min(value.doubleValue(), min);
                            max = (float) Math.max(value.doubleValue(), max);
                        }
                    }
                }
            } catch (DataReadingException e) {
                log.error(
                        "Problem reading data whilst estimating scale range.  A default value will be used.",
                        e);
            }

            if (max == -Float.MAX_VALUE || min == Float.MAX_VALUE) {
                /*
                 * Defensive - either they are both equal to their start values,
                 * or neither is.
                 * 
                 * Anyway, here we have no data, or can't read it. Pick a range.
                 * I've chosen 0 to 100, but it really doesn't matter.
                 */
                min = 0;
                max = 100;
            } else if (min == max) {
                /*
                 * We've hit an area of uniform data. Make sure that max > min
                 */
                max += 1.0f;
            } else {
                float diff = max - min;
                min -= 0.05 * diff;
                max += 0.05 * diff;
            }

            return Extents.newExtent(min, max);
        } else {
            throw new UnsupportedOperationException("Currently only gridded datasets are supported");
        }
    }

    /** Copies a file */
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
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
}
