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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.io.IOUtils;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.graphics.exceptions.EdalLayerNotFoundException;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.graphics.style.sld.SLDException;
import uk.ac.rdg.resc.edal.graphics.style.sld.StyleSLDParser;
import uk.ac.rdg.resc.edal.graphics.utils.ColourPalette;
import uk.ac.rdg.resc.edal.graphics.utils.EnhancedVariableMetadata;
import uk.ac.rdg.resc.edal.graphics.utils.GraphicsUtils;
import uk.ac.rdg.resc.edal.graphics.utils.PlottingStyleParameters;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.wms.exceptions.EdalUnsupportedOperationException;
import uk.ac.rdg.resc.edal.wms.exceptions.StyleNotSupportedException;
import uk.ac.rdg.resc.edal.wms.util.WmsUtils;

public class GetMapStyleParams {

    private String[] layers;
    private String[] styles;

    private String xmlStyle;

    private boolean transparent = false;
    private Color backgroundColour = new Color(0, true);
    private Color belowMinColour;
    private Color aboveMaxColour;
    /* Opacity of the image in the range [0,100] */
    private int opacity = 100;
    /* Number of colour bands to use in the image */
    private Integer numColourBands = null;
    /* True if we're using a log scale */
    private Boolean logarithmic = null;

    private Extent<Float> colourScaleRange = null;

    /* true if we are using an XML style specification */
    private boolean xmlSpecified = false;

    public GetMapStyleParams(RequestParams params) throws EdalException {
        String layersStr = params.getString("layers");
        if (layersStr == null || layersStr.trim().isEmpty()) {
            layers = null;
        } else {
            layers = layersStr.split(",");
        }

        String stylesStr = params.getString("styles");
        if (stylesStr == null) {
            styles = null;
        } else if (stylesStr.trim().isEmpty()) {
            styles = new String[0];
        } else {
            styles = stylesStr.split(",");
        }

        String xmlLoc = params.getString("sld");
        if (xmlLoc != null) {
            /*
             * We have the SLD parameter, which points to the location of the
             * XML style definition. We should download it
             */
            URL url;
            try {
                url = new URL(xmlLoc);
                InputStream is = url.openStream();
                StringWriter writer = new StringWriter();
                IOUtils.copy(is, writer);
                xmlStyle = writer.toString();
            } catch (IOException e) {
                throw new EdalException(
                        "SLD argument specified, but SLD could not be read from URL: " + xmlLoc, e);
            }
        } else {
            /*
             * No location has been specified for the SLD document, so we check
             * to see if the body of the SLD is included in the request string
             */
            xmlStyle = params.getString("sld_body");
        }

        if (xmlStyle == null) {
            xmlSpecified = false;
            if (layers == null || styles == null) {
                throw new EdalException(
                        "You must specify either SLD, SLD_BODY or LAYERS and STYLES");
            }
            if (styles != null && styles.length != layers.length && styles.length != 0) {
                throw new EdalException("You must request exactly one STYLE per layer, "
                        + "or use the default style for each layer with STYLES=");
            }
        } else {
            xmlSpecified = true;
        }

        String bgcStr = params.getString("bgcolor", "0xffffff");
        backgroundColour = GraphicsUtils.parseColour(bgcStr);

        this.transparent = params.getBoolean("transparent", false);
        if(this.transparent) {
            backgroundColour = new Color(0, true);
        }
        
        String bmcStr = params.getString("belowmincolor");
        if (bmcStr == null) {
            belowMinColour = Color.black;
        } else if (bmcStr.equalsIgnoreCase("extend")) {
            belowMinColour = null;
        } else if (bmcStr.equalsIgnoreCase("transparent")) {
            belowMinColour = new Color(0, 0, 0, 0);
        } else {
            belowMinColour = GraphicsUtils.parseColour(bmcStr);
        }

        String amcStr = params.getString("abovemaxcolor");
        if (amcStr == null) {
            aboveMaxColour = Color.black;
        } else if (amcStr.equalsIgnoreCase("extend")) {
            aboveMaxColour = null;
        } else if (amcStr.equalsIgnoreCase("transparent")) {
            aboveMaxColour = new Color(0, 0, 0, 0);
        } else {
            aboveMaxColour = GraphicsUtils.parseColour(amcStr);
        }

        opacity = params.getPositiveInt("opacity", 100);
        if (opacity > 100) {
            opacity = 100;
        }

        colourScaleRange = getColorScaleRange(params);

        logarithmic = params.getBoolean("logscale", null);

        numColourBands = params.getPositiveInt("numcolorbands", ColourPalette.MAX_NUM_COLOURS);
        if (numColourBands > ColourPalette.MAX_NUM_COLOURS) {
            numColourBands = ColourPalette.MAX_NUM_COLOURS;
        }
    }

    /**
     * Gets the ColorScaleRange object requested by the client
     */
    public static Extent<Float> getColorScaleRange(RequestParams params) throws EdalException {
        String csr = params.getString("colorscalerange");
        if (csr == null || csr.equalsIgnoreCase("default")) {
            /* The client wants the layer's default scale range to be used */
            return Extents.emptyExtent();
        } else if (csr.equalsIgnoreCase("auto")) {
            /*
             * The client wants the image to be scaled according to the image's
             * own min and max values (giving maximum contrast)
             */
            return null;
        } else {
            /* The client has specified an explicit colour scale range */
            String[] scaleEls = csr.split(",");
            if (scaleEls.length == 0) {
                return Extents.emptyExtent();
            }
            Float scaleMin = Float.parseFloat(scaleEls[0]);
            Float scaleMax = Float.parseFloat(scaleEls[1]);
            if (scaleMin > scaleMax)
                throw new EdalException("Min > Max in COLORSCALERANGE");
            return Extents.newExtent(scaleMin, scaleMax);
        }
    }

    /**
     * Gets the object used to generate the map plot with all correct styles and
     * layers set.
     * 
     * @param catalogue
     *            A {@link WmsCatalogue} used to get server-configured default
     *            values for each plotted layer
     * @return A {@link MapImage} object
     * @throws EdalException
     *             If invalid parameters have been supplied, or there are other
     *             issues with generating a {@link MapImage} object
     */
    public MapImage getImageGenerator(WmsCatalogue catalogue) throws EdalException {
        if (xmlStyle != null) {
            try {
                return StyleSLDParser.createImage(xmlStyle);
            } catch (SLDException e) {
                e.printStackTrace();
                throw new EdalException("Problem parsing XML style.  Check logs for stack trace");
            }
        }

        if (layers.length > 1) {
            throw new EdalUnsupportedOperationException("Only 1 layer may be requested");
        }

        String layerName = layers[0];
        EnhancedVariableMetadata layerMetadata = WmsUtils.getLayerMetadata(layerName, catalogue);
        if (catalogue.isDisabled(layerName)) {
            throw new EdalLayerNotFoundException("The layer " + layerName
                    + " is not enabled on this server");
        }

        String style = "default/default";

        if (styles.length != 0 && !"".equals(styles[0])) {
            style = styles[0];
        }

        String[] styleParts = style.split("/");

        String plotStyleName = styleParts[0];

        Collection<String> supportedStyles = WmsUtils.getSupportedStylesForLayer(layerName,
                catalogue);
        if (supportedStyles.size() == 0) {
            /*
             * We have no supported styles for this layer
             */
            throw new StyleNotSupportedException("The layer " + layerName
                    + " cannot be plotted - no styles support it.");
        }
        if ("default".equals(plotStyleName)) {
            /*
             * We want the default style. However, since different layer types
             * support different defaults, there is not a single style name
             * which will fulfil this. To get around this, we add styles of the
             * form default-xxxxx.xml to the styles directory. Then we check
             * here if any of the supported styles for this layer start with
             * "default". If not, then no default style is supported, and we
             * throw an exception.
             */

            boolean supported = false;
            for (String supportedStyle : supportedStyles) {
                if (supportedStyle.startsWith("default")) {
                    plotStyleName = supportedStyle;
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                /*
                 * This type of layer doesn't have a default style.
                 */
                throw new StyleNotSupportedException(
                        "This type of layer has no supported default styles.  Please supply a named style");
            }
        } else {
            /*
             * Check that the requested style is actually supported
             */
            boolean supported = false;
            for (String supportedStyle : supportedStyles) {
                if (supportedStyle.equals(plotStyleName)) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                throw new StyleNotSupportedException("The layer " + layerName
                        + " does not support the style " + plotStyleName);
            }
        }

        PlottingStyleParameters defaults = layerMetadata.getDefaultPlottingParameters();

        /*-
         * Choose the palette name:
         * a) from URL parameter, or failing that
         * b) the server-configured default for this layer, or failing that
         * c) the default colour palette name
         */
        String paletteName;
        if (styleParts.length > 1) {
            paletteName = styleParts[1];
        } else if (defaults.getPalette() != null && !"".equals(defaults.getPalette())) {
            paletteName = defaults.getPalette();
        } else {
            paletteName = ColourPalette.DEFAULT_PALETTE_NAME;
        }

        /*-
         * Choose the colour scale:
         * a) from URL parameter, or failing that
         * b) the server-configured default scale range for this layer, or failing that
         * c) auto-scale
         */
        Extent<Float> colourScaleRange;
        if (this.colourScaleRange == null) {
            /*
             * This is the case where this.colorScaleRange is null and we want
             * auto-scaling
             */
            colourScaleRange = null;
        } else if (this.colourScaleRange.isEmpty()) {
            /*
             * We want to use the default scale range if possible
             */
            Extent<Float> defaultColourScaleRange = defaults.getColorScaleRange();
            if (defaultColourScaleRange == null || defaultColourScaleRange.isEmpty()) {
                /*
                 * We have to auto-scale
                 */
                colourScaleRange = null;
            } else {
                colourScaleRange = defaultColourScaleRange;
            }
        } else {
            /*
             * We have a specified range to use
             */
            colourScaleRange = this.colourScaleRange;
        }
        if (colourScaleRange == null) {
            colourScaleRange = GraphicsUtils.estimateValueRange(WmsUtils.getDatasetFromLayerName(
                    layerName, catalogue), catalogue.getLayerNameMapper()
                    .getVariableIdFromLayerName(layerName));
        }

        /*-
         * Choose whether this is a logarithmic plot:
         * a) from URL parameter, or failing that
         * b) from server-configured default for this layer, or failing that
         * c) not logarithmic
         */
        boolean logarithmic;
        if (this.logarithmic != null) {
            logarithmic = this.logarithmic;
        } else if (defaults.isLogScaling() != null) {
            logarithmic = defaults.isLogScaling();
        } else {
            logarithmic = false;
        }

        /*-
         * Choose how many colour bands to use:
         * a) from URL parameter, or failing that
         * b) from server-configured default for this layer, or failing that
         * c) the maximum
         */
        int numColourBands;
        if (this.numColourBands != null) {
            numColourBands = this.numColourBands;
        } else if (defaults.getNumColorBands() != null) {
            numColourBands = defaults.getNumColorBands();
        } else {
            numColourBands = ColourPalette.MAX_NUM_COLOURS;
        }

        return catalogue.getStyleCatalogue().getMapImageFromStyle(
                plotStyleName,
                new PlottingStyleParameters(colourScaleRange, paletteName, aboveMaxColour,
                        belowMinColour, backgroundColour, logarithmic, numColourBands,
                        opacity / 100f),
                WmsUtils.getVariableMetadataFromLayerName(layerName, catalogue),
                catalogue.getLayerNameMapper());
    }

    public boolean isTransparent() {
        return transparent;
    }

    /**
     * Return the opacity of the image as a percentage
     */
    public int getOpacity() {
        return opacity;
    }

    public int getNumLayers() {
        return layers.length;
    }

    public boolean isXmlDefined() {
        return xmlSpecified;
    }

    public String[] getLayerNames() {
        return layers;
    }

    public String[] getStyleNames() {
        return styles;
    }
}
