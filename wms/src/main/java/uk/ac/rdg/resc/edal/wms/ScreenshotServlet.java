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
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.exceptions.EdalException;

/**
 * Implementation of a servlet which generates images suitable for use in
 * publications. These are aimed at being an improvement over simply taking a
 * screenshot of a WMS client.
 * 
 * The parameters this request accepts are (optional unless specified):
 * 
 * image: true/false. Whether to generate an image or just an HTML page
 * containing the image. This is generally not included.
 * 
 * baseUrl: The WMS URL of the background map
 * 
 * baseLayers: The layers to display from the background map
 * 
 * bbox: The bounding box defining the viewport (mandatory)
 * 
 * crs: The co-ordinate reference system code (mandatory)
 * 
 * mapHeight: The desired height of the map
 * 
 * mapWidth: The desired width of the map
 * 
 * layerTitle: A comma-separated list of strings which make up the title. Each
 * line represents the child of the NEXT one and is rendered as such. For
 * example "TMP,FOAM one degree" will be similarly to:
 * 
 * FOAM one degree > TMP
 * 
 * elevation: A string representing the elevation
 * 
 * time: A string representing the time
 * 
 * server: The URL of the WMS server
 * 
 * layer: The WMS layer name of the layer on the above server
 * 
 * style: The base name of the style
 * 
 * palette: The name of the palette to use
 * 
 * scaleRange: Comma-separated colour scale range
 * 
 * numColorBands: The number of colour bands to use
 * 
 * targetElevation: The target elevation (for in-situ data)
 * 
 * targetTime: The target time (for in-situ data)
 * 
 * TODO Error handling is not implemented very well
 */
public class ScreenshotServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(ScreenshotServlet.class);

    /**
     * Handles a GET request. This will generate a screenshot and write it to
     * the output stream, assuming
     * 
     * This should be mapped to the correct URL in the web.xml
     * 
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fullRequest = request.getRequestURL().toString();
        String servletUrl = fullRequest.substring(0,
                fullRequest.indexOf(request.getServletPath()) + 1);

        RequestParams params = new RequestParams(request.getParameterMap());

        if (params.getString("image") != null
                && params.getString("image").equalsIgnoreCase("true")) {
            BufferedImage image;
            try {
                image = drawScreenshot(params, servletUrl);

                response.setContentType("image/png");
                OutputStream output = response.getOutputStream();
                ImageIO.write(image, "png", output);
            } catch (EdalException e) {
                /*
                 * TODO Handle this properly - maybe you should look at
                 * different Velocity template methods.
                 * 
                 * Perhaps this should return an image explaining there is a
                 * problem?
                 */
                e.printStackTrace();
                log.error("Problem with generating screenshot", e);
            }
        } else {
            PrintWriter out = response.getWriter();
            String url = request.getRequestURL() + "?" + request.getQueryString() + "&image=true";
            out.println("<html><body>To save the image, right click and select \"Save As\"<br>"
                    + "Note: The image may take a long time to appear, depending on the speed of the data servers<br>"
                    + "<img src=\"" + url + "\"</body></html>");
        }
    }

    private BufferedImage drawScreenshot(RequestParams params, String servletUrl)
            throws EdalException, IOException {
        /*
         * Retrieve some URL parameters and store for later
         */
        String baseLayerUrl = params.getString("baseUrl");
        String baseLayerNames = params.getMandatoryString("baseLayers");
        if (baseLayerNames != null) {
            baseLayerUrl += "&LAYERS=" + baseLayerNames;
        }

        String[] bboxStr = params.getString("bbox").split(",");
        Float minLon = Float.parseFloat(bboxStr[0]);
        Float maxLon = Float.parseFloat(bboxStr[2]);
        Float minLat = Float.parseFloat(bboxStr[1]);
        Float maxLat = Float.parseFloat(bboxStr[3]);
        Float lonRange = maxLon - minLon;

        String crs = params.getString("crs");

        if (baseLayerUrl == null) {
            /*
             * Set the background map URL if it was blank
             */
            if (crs.equalsIgnoreCase("EPSG:5041")) {
                baseLayerUrl = "http://godiva.rdg.ac.uk/geoserver/ReSC/wms?LAYERS=bluemarble-np";
            } else if (crs.equalsIgnoreCase("EPSG:5042")) {
                baseLayerUrl = "http://godiva.rdg.ac.uk/geoserver/ReSC/wms?LAYERS=bluemarble-sp";
            } else {
                baseLayerUrl = "http://godiva.rdg.ac.uk/geoserver/ReSC/wms?LAYERS=bluemarble";
            }
        }

        int mapHeight = params.getPositiveInt("mapHeight", 384);
        int mapWidth = params.getPositiveInt("mapWidth", 512);

        String time = params.getString("time");

        /*
         * Get the legend first so that we know how big the final image needs to
         * be
         */
        BufferedImage colorBar = null;
        URL url = createWmsUrl(params, false, minLon, minLat, maxLon, maxLat, 50, mapHeight,
                servletUrl, time, true);
        if (url != null) {
            InputStream in = null;
            try {
                URLConnection conn = url.openConnection();
                in = conn.getInputStream();
                colorBar = ImageIO.read(in);
            } finally {
                if (in != null)
                    in.close();
            }
        }

        /*
         * Calculate the space needed for text above the image
         */
        int textSpace = 25;
        if (params.getString("layerTitle") != null) {
            String[] titleElements = params.getString("layerTitle").split(",");
            textSpace += 15 * titleElements.length;
        }
        if (time != null) {
            textSpace += 20;
        }
        if (params.getString("elevation") != null) {
            textSpace += 20;
        }

        /*
         * Calculate the total size needed for the image
         */
        int totalWidth = mapWidth + colorBar.getWidth() + 20;
        int totalHeight;
        if (mapHeight > colorBar.getHeight()) {
            totalHeight = mapHeight + textSpace;
        } else {
            totalHeight = colorBar.getHeight() + textSpace;
        }

        /*
         * Create the final image, fill it white, and set some rendering
         * defaults
         */
        BufferedImage image = new BufferedImage(totalWidth, totalHeight,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setPaint(Color.white);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setBackground(Color.white);

        /* Draw labels on the image for the layer name, time, elevation */
        Font font = new Font("SansSerif", Font.BOLD, 16);
        g.setPaint(Color.black);
        g.setFont(font);
        int vPos = 20;
        int hPos = 10;

        String title = params.getString("layerTitle");
        if (title != null) {
            int indent = 0;
            String[] titleElements = title.split(",");
            if (titleElements.length >= 2) {
                for (int i = titleElements.length - 1; i > 0; i--) {
                    g.drawString(titleElements[i], hPos + indent, vPos);
                    vPos += 15;
                    g.drawString("\u21b3", hPos + indent + 8, vPos - 2);
                    indent += 20;
                    font = new Font("SansSerif", Font.BOLD, 14);
                    g.setFont(font);
                }
            }
            if (titleElements.length > 0) {
                g.drawString(titleElements[0], hPos + indent, vPos);
                vPos += 15;
            }
        }
        vPos += 5;

        font = new Font("SansSerif", Font.BOLD, 14);
        g.setFont(font);
        if (time != null) {
            g.drawString("Time: " + time, hPos, vPos);
            vPos += 20;
        }

        String el = params.getString("elevation");
        if (el != null) {
            String depth = el;
            String u = params.getString("zUnits");
            String units = "";
            if (u != null)
                units = u;
            if (depth.startsWith("-"))
                g.drawString("Depth: " + el.substring(1) + units, hPos, vPos);
            else
                g.drawString("Elevation: " + el + units, hPos, vPos);
            vPos += 30;
        }

        /*
         * Text drawing done.
         * 
         * Now draw the background image
         */
        if ((!crs.equalsIgnoreCase("EPSG:4326") && !crs.equalsIgnoreCase("CRS:84"))
                || (minLon >= -180 && maxLon <= 180)) {
            BufferedImage im = getImage(params, minLon, minLat, maxLon, maxLat, mapWidth, mapHeight,
                    baseLayerUrl, time);
            g.drawImage(im, 0, textSpace, null);
        } else if (minLon < -180 && maxLon <= 180) {
            int lefWidth = (int) (mapWidth * (-180 - minLon) / (lonRange));
            BufferedImage im = getImage(params, minLon + 360, minLat, 180f, maxLat, lefWidth,
                    mapHeight, baseLayerUrl, time);
            g.drawImage(im, 0, textSpace, null);
            im = getImage(params, -180f, minLat, maxLon, maxLat, mapWidth - lefWidth, mapHeight,
                    baseLayerUrl, time);
            g.drawImage(im, lefWidth, textSpace, null);
        } else if (minLon >= -180 && maxLon > 180) {
            int rightWidth = (int) (mapWidth * (maxLon - 180f) / (lonRange));
            BufferedImage im = getImage(params, minLon, minLat, 180f, maxLat, mapWidth - rightWidth,
                    mapHeight, baseLayerUrl, time);
            g.drawImage(im, 0, textSpace, null);
            im = getImage(params, -180f, minLat, maxLon - 360, maxLat, rightWidth, mapHeight,
                    baseLayerUrl, time);
            g.drawImage(im, mapWidth - rightWidth, textSpace, null);
        } else if (minLon < -180 && maxLon > 180) {
            int leftWidth = (int) (mapWidth * (-180 - minLon) / (lonRange));
            BufferedImage im = getImage(params, minLon + 360, minLat, 180f, maxLat, leftWidth,
                    mapHeight, baseLayerUrl, time);
            g.drawImage(im, 0, textSpace, null);

            int rightWidth = (int) (mapWidth * (maxLon - 180f) / (lonRange));
            im = getImage(params, -180f, minLat, maxLon - 360, maxLat, rightWidth, mapHeight,
                    baseLayerUrl, time);
            g.drawImage(im, mapWidth - rightWidth, textSpace, null);

            im = getImage(params, -180f, minLat, 180f, maxLat, mapWidth - leftWidth - rightWidth,
                    mapHeight, baseLayerUrl, time);
            g.drawImage(im, leftWidth, textSpace, null);
        }

        url = createWmsUrl(params, false, minLon, minLat, maxLon, maxLat, mapWidth, mapHeight,
                servletUrl, time, false);
        BufferedImage wmsLayer;
        if (url != null) {
            wmsLayer = ImageIO.read(url);
            g.drawImage(wmsLayer, 0, textSpace, null);
        }

        String overlaysStr = params.getString("overlays");
        if (overlaysStr != null) {
            /*
             * Draw any overlay layers on top of the WMS one
             */
            String[] overlayers = overlaysStr.split(",");
            for (String overlayer : overlayers) {
                URL overlayWmsUrl = createWmsUrl(params, true, minLon, minLat, maxLon, maxLat,
                        mapWidth, mapHeight, overlayer, time, false);
                if (overlayWmsUrl != null) {
                    wmsLayer = ImageIO.read(overlayWmsUrl);
                    g.drawImage(wmsLayer, 0, textSpace, null);
                }
            }
        }

        if (colorBar != null) {
            g.drawImage(colorBar, mapWidth + 10, textSpace + (mapHeight - colorBar.getHeight()) / 2,
                    mapWidth + 10 + colorBar.getWidth(),
                    textSpace + (mapHeight + colorBar.getHeight()) / 2, 0, 0, colorBar.getWidth(),
                    colorBar.getHeight(), null);
        }

        return image;
    }

    private BufferedImage getImage(RequestParams params, Float minLon, Float minLat, Float maxLon,
            Float maxLat, int width, int height, String bgUrl, String time) throws IOException {
        BufferedImage image = null;
        URL baseUrl = createWmsUrl(params, true, minLon, minLat, maxLon, maxLat, width, height,
                bgUrl, time, false);
        try {
            image = ImageIO.read(baseUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }
        return image;
    }

    private URL createWmsUrl(RequestParams params, boolean baseLayer, Float minLon, Float minLat,
            Float maxLon, Float maxLat, int width, int height, String baseWmsUrl, String time,
            boolean colorbar) throws UnsupportedEncodingException {
        StringBuilder url = new StringBuilder();
        if (baseLayer) {
            Pattern p = Pattern.compile(" ");
            if (baseWmsUrl != null) {
                Matcher m = p.matcher(baseWmsUrl);
                if (m != null)
                    baseWmsUrl = m.replaceAll("%20");
            }
            url.append(baseWmsUrl);
            url.append("&STYLES=");
        } else {
            String server = params.getString("server");
            if (server == null || server.equalsIgnoreCase("null")) {
                /*
                 * If we don't have a server, we can't plot a layer
                 */
                return null;
            }
            url.append(server + "?SERVICE=WMS&LAYERS=" + params.getString("layer"));
            String style = params.getString("style");
            String palette = params.getString("palette");
            if (style != null && palette != null) {
                url.append("&STYLES=" + style + "/" + palette);
            } else {
                url.append("&STYLES=");
            }
            String scaleRange = params.getString("scaleRange");
            if (scaleRange != null)
                url.append("&COLORSCALERANGE=" + scaleRange);
            String aboveMaxColor = params.getString("aboveMaxColor");
            if (aboveMaxColor != null)
                url.append("&ABOVEMAXCOLOR=" + URLEncoder.encode(aboveMaxColor, "UTF-8"));
            String belowMinColor = params.getString("belowMinColor");
            if (belowMinColor != null)
                url.append("&BELOWMINCOLOR=" + URLEncoder.encode(belowMinColor, "UTF-8"));
            String noDataColor = params.getString("noDataColor");
            if (noDataColor != null)
                url.append("&BGCOLOR=" + URLEncoder.encode(noDataColor, "UTF-8"));
            String numColorBands = params.getString("numColorBands");
            if (numColorBands != null)
                url.append("&NUMCOLORBANDS=" + numColorBands);
            String logscale = params.getString("logscale");
            if (logscale != null)
                url.append("&LOGSCALE=" + logscale);
            if (time != null)
                /*
                 * Yuck. But it seems to be necessary, otherwise "+" gets
                 * interpreted wrongly
                 */
                url.append("&TIME=" + URLEncoder.encode(time, "UTF-8"));
            String elevation = params.getString("elevation");
            if (elevation != null)
                url.append("&ELEVATION=" + elevation);
            String targetElevation = params.getString("targetelevation");
            if (targetElevation != null)
                url.append("&TARGETELEVATION=" + targetElevation);
            String targetTime = params.getString("targettime");
            if (targetTime != null)
                /*
                 * Yuck. But it seems to be necessary, otherwise "+" gets
                 * interpreted wrongly
                 */
                url.append("&TARGETTIME=" + URLEncoder.encode(targetTime, "UTF-8"));
        }

        url.append("&TRANSPARENT=true");
        url.append("&VERSION=1.1.1&SERVICE=WMS&REQUEST=");
        if (colorbar) {
            url.append("GetLegendGraphic");
        } else {
            /*
             * Don't specify a width for a colourbar. A different default will
             * be used depending on whether we have a 1D or 2D variable
             */
            url.append("GetMap&WIDTH=" + width);
        }
        url.append("&FORMAT=image/png&HEIGHT=" + height);
        url.append("&BBOX=" + minLon + "," + minLat + "," + maxLon + "," + maxLat);
        String crs = params.getString("crs");
        if (crs.equalsIgnoreCase("CRS:84")) {
            crs = "EPSG:4326";
        }
        url.append("&SRS=" + crs);
        try {
            return new URL(url.toString().replaceAll(" ", "%20"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
