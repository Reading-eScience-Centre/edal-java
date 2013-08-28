package uk.ac.rdg.resc.edal.wms;

import java.awt.Color;

import javax.xml.bind.JAXBException;

import net.sf.json.JSONException;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.graphics.style.ArrowLayer;
import uk.ac.rdg.resc.edal.graphics.style.ColourMap;
import uk.ac.rdg.resc.edal.graphics.style.ColourScale;
import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.ContourLayer;
import uk.ac.rdg.resc.edal.graphics.style.ContourLayer.ContourLineStyle;
import uk.ac.rdg.resc.edal.graphics.style.Drawable;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.graphics.style.PaletteColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.PatternScale;
import uk.ac.rdg.resc.edal.graphics.style.RasterLayer;
import uk.ac.rdg.resc.edal.graphics.style.StippleLayer;
import uk.ac.rdg.resc.edal.graphics.style.util.ColourPalette;
import uk.ac.rdg.resc.edal.graphics.style.util.StyleJSONParser;
import uk.ac.rdg.resc.edal.graphics.style.util.StyleXMLParser;
import uk.ac.rdg.resc.edal.graphics.style.util.StyleXMLParser.ColorAdapter;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.wms.exceptions.WmsException;

public class GetMapStyleParams {

    private String[] layers;
    private String[] styles;

    private String xmlStyle;

    private boolean transparent = false;
    private Color backgroundColour = Color.white;
    private int opacity = 100; // Opacity of the image in the range [0,100]
    private int numColourBands = 254; // Number of colour bands to use in the
                                      // image
    private boolean logarithmic = false; // True if we're using a log scale

    private Extent<Float> colorScaleRange;
    private boolean autoScale = false; // True if we're using a log scale

    private boolean xmlSpecified = false;

    /*
     * Used for getting server settings. This is something that needs some
     * restructuring.
     */
//    private FeatureCatalogue featureCatalogue;
//    private Map<String, Dataset> datasets;

    private static ColorAdapter cAdapter = new ColorAdapter();

    public GetMapStyleParams(RequestParams params/*
                                                  * , FeatureCatalogue
                                                  * featureCatalogue,
                                                  * Map<String, Dataset>
                                                  * datasets
                                                  */) throws WmsException {
        /*
         * TODO We might want to put the featureCatalogue and datasets back, but
         * hopefully this is not the case...
         * (perhaps add them as arguments to getImageGenerator() if they're really still needed)
         */
//        this.featureCatalogue = featureCatalogue;
//        this.datasets = datasets;

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

        xmlStyle = params.getString("XML_STYLE");

        String jsonStyle = params.getString("JSON_STYLE");
        if (jsonStyle != null && xmlStyle == null) {
            try {
                xmlStyle = StyleJSONParser.JSONtoXMLString(jsonStyle);
            } catch (JSONException e) {
                e.printStackTrace();
                throw new WmsException(
                        "Problem parsing JSON style to XML style.  Check logs for stack trace");
            }
        }

        if (xmlStyle == null) {
            xmlSpecified = false;
            if (layers == null) {
                throw new WmsException(
                        "You must specify either XML_STYLE, JSON_STYLE or LAYERS and STYLES");
            }
            if (styles.length != layers.length && styles.length != 0) {
                throw new WmsException("You must request exactly one STYLE per layer, "
                        + "or use the default style for each layer with STYLES=");
            }
        } else {
            xmlSpecified = true;
        }

        this.transparent = params.getBoolean("transparent", false);

        /*
         * TODO - we don't currently handle the background colour
         */
        try {
            String bgc = params.getString("bgcolor", "0xFFFFFF");
            if ((bgc.length() != 8 && bgc.length() != 10) || !bgc.startsWith("0x"))
                throw new Exception();
            /* Parse the hexadecimal string */
            backgroundColour = cAdapter.unmarshal(bgc);
        } catch (Exception e) {
            throw new WmsException("Invalid format for BGCOLOR");
        }

        opacity = params.getPositiveInt("opacity", 100);
        if (opacity > 100) {
            opacity = 100;
        }

        colorScaleRange = getColorScaleRange(params);
        if (colorScaleRange == null) {
            autoScale = true;
        }

        logarithmic = params.getBoolean("logscale", false);

        numColourBands = params.getPositiveInt("numcolorbands", ColourPalette.MAX_NUM_COLOURS);
        if (numColourBands > ColourPalette.MAX_NUM_COLOURS) {
            numColourBands = ColourPalette.MAX_NUM_COLOURS;
        }
    }

    /**
     * Gets the ColorScaleRange object requested by the client
     */
    private Extent<Float> getColorScaleRange(RequestParams params) throws WmsException {
        String csr = params.getString("colorscalerange");
        if (csr == null || csr.equalsIgnoreCase("default")) {
            // The client wants the layer's default scale range to be used
            return null;
        } else if (csr.equalsIgnoreCase("auto")) {
            // The client wants the image to be scaled according to the image's
            // own min and max values (giving maximum contrast)
            return Extents.emptyExtent(Float.class);
        } else {
            // The client has specified an explicit colour scale range
            String[] scaleEls = csr.split(",");
            if (scaleEls.length == 0) {
                return Extents.emptyExtent(Float.class);
            }
            Float scaleMin = Float.parseFloat(scaleEls[0]);
            Float scaleMax = Float.parseFloat(scaleEls[1]);
            if (scaleMin > scaleMax)
                throw new WmsException("Min > Max in COLORSCALERANGE");
            return Extents.newExtent(scaleMin, scaleMax);
        }
    }

    public MapImage getImageGenerator() throws WmsException {
        if (xmlStyle != null) {
            try {
                return StyleXMLParser.deserialise(xmlStyle);
            } catch (JAXBException e) {
                e.printStackTrace();
                throw new WmsException("Problem parsing XML style.  Check logs for stack trace");
            }
        }

        if (layers.length > 1) {
            throw new WmsException("Only 1 layer may be requested");
        }

        String layerName = layers[0];
        String style = "default/default";

        if (styles.length != 0) {
            style = styles[0];
        }

        String[] styleParts = style.split("/");
        if (styleParts.length == 0) {
            throw new WmsException("Style should be of the form STYLE/PALETTE ()");
        }
        String plotStyleName = styleParts[0];
        String paletteName = styleParts.length > 1 ? styleParts[1] : "default";

        MapImage image = new MapImage();

        Drawable layer = null;

        /*
         * TODO Handling of default styles...
         * 
         * Currently we just treat default as boxfill, and don't bother about
         * hierarchy.
         */
//        if(plotStyleName.toLowerCase().startsWith("default")) {
//            /*
//             * Check for parent layers which can't be directly plotted
//             */
//            FeatureCollectionAndMemberName featureAndMemberName = featureCatalogue.getFeatureAndMemberName(layerName);
//            String memberName = featureAndMemberName.getMemberName();
//            String datasetName = featureAndMemberName.getFeatureCollection().getId();
//            /*
//             * Only one member supplied = max 1 feature returned
//             */
//            @SuppressWarnings("unchecked")
//            Collection<Feature> findFeatures = (Collection<Feature>) featureAndMemberName.getFeatureCollection().findFeatures(null, null, null,
//                    CollectionUtils.setOf(memberName));
//            Feature feature = null;
//            for(Feature f :findFeatures) {
//                feature = f;
//            }
//            if(feature == null) {
//                throw new WmsException("No features fit this description");
//            }
//            RangeMetadata topMetadata = feature.getCoverage().getRangeMetadata();
//            RangeMetadata memberMetadata = MetadataUtils.getDescendantMetadata(topMetadata, memberName);
//            /*
//             * Default plotting info
//             */
//            Dataset dataset = datasets.get(featureAndMemberName.getFeatureCollection().getId());
//            if(memberMetadata instanceof ScalarMetadata) {
//                /*
//                 * We don't have a parent layer.  This makes things somewhat easier.
//                 */
//                /*
//                 * TODO For the time being, we assume that "boxfill" is the default,
//                 * but this really depends on the feature type.
//                 */
//                plotStyleName = "boxfill";
//            } else if(memberMetadata instanceof VectorMetadata) {
//                VectorMetadata vectorMetadata = (VectorMetadata) memberMetadata;
//                List<ScalarMetadata> representativeChildren = vectorMetadata.getRepresentativeChildren();
//                String magnitudeFieldName = null;
//                String directionFieldName = null;
//                for(ScalarMetadata m : representativeChildren) {
//                    if(m instanceof VectorComponent) {
//                        VectorComponent vectorComponent = (VectorComponent) m;
//                        if(vectorComponent.getComponentType() == VectorComponentType.MAGNITUDE) {
//                            magnitudeFieldName = vectorComponent.getName();
//                        } else if(vectorComponent.getComponentType() == VectorComponentType.DIRECTION) {
//                            directionFieldName = vectorComponent.getName();
//                        }
//                    } else {
//                        /*
//                         * Won't get thrown unless code changes, but best to be safe.
//                         */
//                        throw new WmsException("Vector Metadata must contain vector components");
//                    }
//                }
//                if(magnitudeFieldName == null || directionFieldName == null) {
//                    throw new WmsException("Vector Metadata must contain magnitude and direction");
//                }
//                /*
//                 * Treat parameters as params for magnitude field, and plot arrow layer on top with default values
//                 */
//                /*
//                 * Generate a RasterLayer
//                 */
//                ColourScale scaleRange = new ColourScale(colorScaleRange.getLow(),
//                        colorScaleRange.getHigh(), logarithmic);
//                ColourMap colourPalette = new ColourMap(Color.black, Color.black, new Color(0, true),
//                        paletteName, numColourBands);
//                ColourScheme colourScheme = new PaletteColourScheme(scaleRange, colourPalette);
//                
//                layer = new RasterLayer(datasetName+"/"+magnitudeFieldName, colourScheme);
//                image.getLayers().add(layer);
//                
//                layer = new ArrowLayer(datasetName+"/"+directionFieldName, 8, Color.black);
//                image.getLayers().add(layer);
//                return image;
//            } else if(memberMetadata instanceof StatisticsCollection) {
//                StatisticsCollection statisticsCollection = (StatisticsCollection) memberMetadata;
//                List<ScalarMetadata> children = statisticsCollection.getRepresentativeChildren();
//                String meanFieldName = null;
//                String stddevFieldName = null;
//                String lowerFieldName = null;
//                String upperFieldName = null;
//                for(String childName : statisticsCollection.getMemberNames()) {
//                    ScalarMetadata m = (ScalarMetadata) statisticsCollection.getMemberMetadata(childName);
//                    if(m instanceof Statistic) {
//                        Statistic statistic = (Statistic) m;
//                        if(statistic.getStatisticType() == StatisticType.MEAN) {
//                            meanFieldName = statistic.getName();
//                        } else if(statistic.getStatisticType() == StatisticType.STANDARD_DEVIATION) {
//                            stddevFieldName = statistic.getName();
//                        } else if(statistic.getStatisticType() == StatisticType.LOWER_CONFIDENCE_BOUND) {
//                            lowerFieldName = statistic.getName();
//                        } else if(statistic.getStatisticType() == StatisticType.UPPER_CONFIDENCE_BOUND) {
//                            upperFieldName = statistic.getName();
//                        }
//                    } else {
//                        /*
//                         * Won't get thrown unless code changes, but best to be safe.
//                         */
//                        throw new WmsException("Statistics must (currently) contain mean and std dev");
//                    }
//                }
//                if(meanFieldName == null || stddevFieldName == null || lowerFieldName == null || upperFieldName == null) {
//                    throw new WmsException("Statistics must (currently) contain mean and std dev");
//                }
//                /*
//                 * Treat parameters as params for magnitude field, and plot
//                 * contour layer on top with default values taken from server
//                 * (which should have been approximately auto-scaled
//                 */
//                /*
//                 * Generate a RasterLayer
//                 */
//                ColourScale scaleRange = new ColourScale(colorScaleRange.getLow(),
//                        colorScaleRange.getHigh(), logarithmic);
//                ColourMap colourPalette = new ColourMap(Color.black, Color.black, new Color(0, true),
//                        paletteName, numColourBands);
//                ColourScheme colourScheme = new PaletteColourScheme(scaleRange, colourPalette);
//                
//                layer = new RasterLayer(datasetName+"/"+meanFieldName, colourScheme);
//                image.getLayers().add(layer);
//                
//                FeaturePlottingMetadata stddevPlottingMetadata = dataset.getPlottingMetadataMap().get(stddevFieldName);
//                Extent<Float> sdRange = stddevPlottingMetadata.getColorScaleRange();
//                
//                
//                if(plotStyleName.toLowerCase().endsWith("contour") || plotStyleName.equalsIgnoreCase("default")) {
//                    layer = new ContourLayer(datasetName + "/" + stddevFieldName, new ColourScale(
//                            sdRange.getLow(), sdRange.getHigh(), logarithmic), autoScale, 8,
//                            Color.black, 1, ContourLineStyle.SOLID, true);
//                } else if(plotStyleName.toLowerCase().endsWith("smooth")) {
//                    layer = new SmoothedContourLayer(datasetName + "/" + stddevFieldName, new ColourScale(
//                            sdRange.getLow(), sdRange.getHigh(), logarithmic), autoScale, 8,
//                            Color.black, 1, ContourLineStyle.SOLID, true);
//                } else if(plotStyleName.toLowerCase().endsWith("stipple")) {
//                    float range = sdRange.getHigh() - sdRange.getLow();
//                    float low = sdRange.getLow() + 0.1f * range;
//                    float high = sdRange.getHigh() - 0.1f * range;
//                    layer = new StippleLayer(datasetName + "/" + stddevFieldName, new PatternScale(
//                            5, low, high, false));
//                } else if(plotStyleName.toLowerCase().endsWith("confidence")) {
//                    image.getLayers().remove(0);
//                    layer = new ConfidenceIntervalLayer(datasetName + "/" + lowerFieldName,
//                            datasetName + "/" + upperFieldName, 8, colourScheme);
//                } else if (plotStyleName.toLowerCase().endsWith("fade_black")) {
//                    layer = new RasterLayer(datasetName + "/" + stddevFieldName,
//                            new PaletteColourScheme(new ColourScale(sdRange.getLow(),
//                                    sdRange.getHigh(), logarithmic), new ColourMap(new Color(0,
//                                    true), Color.black, new Color(0, true), "#00000000,#ff000000", 15)));
//                } else if (plotStyleName.toLowerCase().endsWith("fade_white")) {
//                    layer = new RasterLayer(datasetName + "/" + stddevFieldName,
//                            new PaletteColourScheme(new ColourScale(sdRange.getLow(),
//                                    sdRange.getHigh(), logarithmic), new ColourMap(new Color(0,
//                                    true), Color.white, new Color(0, true), "#00ffffff,#ffffffff", 15)));
//                }
//                image.getLayers().add(layer);
//                return image;
//            }
//        }

        if ("default".equalsIgnoreCase(plotStyleName) || "boxfill".equalsIgnoreCase(plotStyleName)) {
            /*
             * Generate a RasterLayer
             */
            ColourScale scaleRange = new ColourScale(colorScaleRange.getLow(),
                    colorScaleRange.getHigh(), logarithmic);
            ColourMap colourPalette = new ColourMap(Color.black, Color.black, new Color(0, true),
                    paletteName, numColourBands);
            ColourScheme colourScheme = new PaletteColourScheme(scaleRange, colourPalette);
            layer = new RasterLayer(layerName, colourScheme);
        } else if (plotStyleName.equalsIgnoreCase("contour")) {
            layer = new ContourLayer(layerName, new ColourScale(colorScaleRange.getLow(),
                    colorScaleRange.getHigh(), logarithmic), autoScale, numColourBands,
                    Color.black, 1, ContourLineStyle.SOLID, true);
        } else if (plotStyleName.equalsIgnoreCase("stipple")) {
            PatternScale scale = new PatternScale(numColourBands, colorScaleRange.getLow(),
                    colorScaleRange.getHigh(), logarithmic);
            layer = new StippleLayer(layerName, scale);
        } else if (plotStyleName.equalsIgnoreCase("arrow")) {
            layer = new ArrowLayer(layerName, 8, Color.black);
        }

        if (layer == null) {
            throw new WmsException("Do not know how to plot the style: " + plotStyleName);
        }

        image.getLayers().add(layer);

        return image;
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
}
