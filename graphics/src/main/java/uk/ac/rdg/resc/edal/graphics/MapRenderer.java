package uk.ac.rdg.resc.edal.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.Vector2D;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Extents;

public final class MapRenderer {
    private int width;
    private int height;
    private BoundingBox bbox;
    /*
     * This may get used if we have point data. It corresponds to the bounding
     * box in which a plotted point will show up in the image.
     * 
     * i.e. the normal bounding box + e.g. 5 pixels
     */
    private BoundingBox generousBbox = null;
    private Map<TimePosition, Frame> frameData;
    private MapStyleDescriptor style = null;

    private final class DataPoint {
        private final Float value;
        private final HorizontalPosition position;

        public DataPoint(Float value, HorizontalPosition position) {
            super();
            this.value = value;
            this.position = position;
        }

        public Float getValue() {
            return value;
        }

        public HorizontalPosition getPosition() {
            return position;
        }
    }

    protected class Frame {
        private BufferedImage griddedImage = null;
        private List<Float> gridDataMagnitudes = null;
        private List<Float> gridDataDirections = null;
        private BufferedImage pointImage = null;
        private List<DataPoint> pointDataValues = null;
        private String label = null;

        public void setLabel(String label) {
            if (label == null || label.equals(""))
                return;
            if (this.label != null)
                throw new UnsupportedOperationException("Label has already been set for this frame");
            this.label = label;
        }

        public void setGriddedImage(BufferedImage griddedImage) {
            if (gridDataMagnitudes != null || gridDataDirections != null)
                throw new UnsupportedOperationException(
                        "Each frame must contain either an image, or data.  Not both");
            this.griddedImage = griddedImage;
        }

        public void setGridDataMagnitudes(List<Float> gridDataMagnitudes) {
            if (griddedImage != null)
                throw new UnsupportedOperationException(
                        "Each frame must contain either an image, or data.  Not both");
            this.gridDataMagnitudes = gridDataMagnitudes;
        }

        public void setGridDataDirections(List<Float> gridDataDirections) {
            if (griddedImage != null)
                throw new UnsupportedOperationException(
                        "Each frame must contain either an image, or data.  Not both");
            this.gridDataDirections = gridDataDirections;
        }

        public void setPointImage(BufferedImage pointImage) {
            if (pointDataValues != null)
                throw new UnsupportedOperationException(
                        "Each frame must contain either an image, or data.  Not both");
            this.pointImage = pointImage;
        }

        public void addPointDataValue(DataPoint dataPoint) {
            if (pointImage != null)
                throw new UnsupportedOperationException(
                        "Each frame must contain either an image, or data.  Not both");
            if (pointDataValues == null)
                pointDataValues = new ArrayList<DataPoint>();
            pointDataValues.add(dataPoint);
        }

        public BufferedImage getGriddedImage() {
            return griddedImage;
        }

        public List<Float> getGridDataMagnitudes() {
            return gridDataMagnitudes;
        }

        public List<Float> getGridDataDirections() {
            return gridDataDirections;
        }

        public BufferedImage getPointImage() {
            return pointImage;
        }

        public List<DataPoint> getPointDataValues() {
            return pointDataValues;
        }

        public String getLabel() {
            return label;
        }
    }

    public MapRenderer(MapStyleDescriptor style, int width, int height, BoundingBox bbox) {
        this.style = style;
        this.width = width;
        this.height = height;
        this.bbox = bbox;

        frameData = new HashMap<TimePosition, MapRenderer.Frame>();
    }

    public void addData(Feature feature, TimePosition tPos, VerticalPosition zPos, String label) {
        if (feature instanceof GridSeriesFeature<?>) {
            addGriddedFrame((GridSeriesFeature<?>) feature, tPos, zPos, label);
        } else if (feature instanceof PointSeriesFeature<?>) {
            addPointSeriesDataFrame((PointSeriesFeature<?>) feature, tPos, label);
        } else if (feature instanceof ProfileFeature<?>) {
            addProfileDataFrame((ProfileFeature<?>) feature, zPos, label);
        } else {
            throw new UnsupportedOperationException(
                    "Currently this type of feature can not be plotted");
        }
    }

    private void addGriddedFrame(GridSeriesFeature<?> feature, TimePosition tPos,
            VerticalPosition zPos, String label) {
        /*
         * First, we map the feature to a coverage of the correct size. This
         * means that we will have a value in the coverage for each pixel in the
         * image
         */
        HorizontalGrid targetDomain = new RegularGridImpl(bbox, width, height);
        final GridCoverage2D<?> coverage = feature.extractHorizontalGrid(tPos, zPos, targetDomain);

        Class<?> clazz = coverage.getRangeMetadata(null).getValueType();
        if (clazz != Vector2D.class && !Number.class.isAssignableFrom(clazz)) {
            throw new UnsupportedOperationException(
                    "Can only add frames from coverages which are either numbers or vectors");
        }

        List<Float> magnitudes;
        List<Float> directions = null;

        if (clazz == Vector2D.class) {
            magnitudes = new AbstractList<Float>() {
                @Override
                public Float get(int index) {
                    Vector2D<Float> vec = ((Vector2D<Float>) coverage.getValues().get(index));
                    return vec == null ? null : vec.getMagnitude();
                }

                @Override
                public int size() {
                    return coverage.getValues().size();
                }
            };
            directions = new AbstractList<Float>() {
                @Override
                public Float get(int index) {
                    Vector2D<Float> vec = ((Vector2D<Float>) coverage.getValues().get(index));
                    return vec == null ? null : vec.getDirection();
                }

                @Override
                public int size() {
                    return coverage.getValues().size();
                }
            };
        } else {
            magnitudes = (List<Float>) coverage.getValues();
        }

        Frame currentFrameData;
        if (frameData.containsKey(tPos)) {
            currentFrameData = frameData.get(tPos);
        } else {
            currentFrameData = new Frame();
        }

        if (style.isAutoScale()) {
            currentFrameData.setGridDataMagnitudes(magnitudes);
            currentFrameData.setGridDataDirections(directions);
        } else {
            currentFrameData.setGriddedImage(createGriddedImage(magnitudes, directions));
        }
        currentFrameData.setLabel(label);

        frameData.put(tPos, currentFrameData);
    }

    private void addPointSeriesDataFrame(PointSeriesFeature<?> feature, TimePosition tPos,
            String label) {
        if (pointEntirelyOutsideBox(feature.getHorizontalPosition()))
            return;

        Class<?> clazz = feature.getCoverage().getRangeMetadata(null).getValueType();
        Float value;
        if (clazz == Float.class) {
            value = (Float) feature.getCoverage().evaluate(tPos);
        } else if (clazz == Vector2D.class) {
            Vector2D<Float> vec = ((Vector2D<Float>) feature.getCoverage().evaluate(tPos));
            value = vec == null ? null : vec.getMagnitude();
        } else {
            throw new UnsupportedOperationException("Feature value type should be Float or Vector");
        }

        DataPoint data = new DataPoint(value, feature.getHorizontalPosition());
        addPointData(data, tPos, label);
    }

    private void addProfileDataFrame(ProfileFeature<?> feature, VerticalPosition zPos, String label) {
        if (pointEntirelyOutsideBox(feature.getHorizontalPosition()))
            return;

        Class<?> clazz = feature.getCoverage().getRangeMetadata(null).getValueType();
        Float value;
        if (clazz == Float.class) {
            value = (Float) feature.getCoverage().evaluate(zPos);
        } else if (clazz == Vector2D.class) {
            Vector2D<Float> vec = ((Vector2D<Float>) feature.getCoverage().evaluate(zPos));
            value = vec == null ? null : vec.getMagnitude();
        } else {
            throw new UnsupportedOperationException("Feature value type should be Float or Vector");
        }

        DataPoint data = new DataPoint(value, feature.getHorizontalPosition());
        addPointData(data, feature.getTime(), label);
    }

    private void addPointData(DataPoint data, TimePosition tPos, String label) {
        Frame currentFrameData;
        if (frameData.containsKey(tPos)) {
            currentFrameData = frameData.get(tPos);
        } else {
            currentFrameData = new Frame();
        }

        if (style.isAutoScale()) {
            currentFrameData.addPointDataValue(data);
        } else {
            BufferedImage currentImage = currentFrameData.getPointImage();
            addToPointImage(data, currentImage);
            currentFrameData.setPointImage(currentImage);
        }
        if (currentFrameData.getLabel() == null)
            currentFrameData.setLabel(label);

        frameData.put(tPos, currentFrameData);
    }

    private boolean pointEntirelyOutsideBox(HorizontalPosition pos) {
        if (generousBbox == null) {
            initGenerousBBox();
        }
        double x = pos.getX();
        double y = pos.getY();
        if (x < generousBbox.getMinX() || x > generousBbox.getMaxX() || y < generousBbox.getMinY()
                || y > generousBbox.getMaxY())
            return true;

        return false;
    }

    private void initGenerousBBox() {
        double geoWidth = bbox.getSpan(0);
        double geoHeight = bbox.getSpan(1);
        int iconWidth = style.getIcon().getWidth();
        int iconHeight = style.getIcon().getHeight();
        double xExtra = geoWidth * ((iconWidth + width) / (double) width - 1);
        double yExtra = geoHeight * ((iconHeight + height) / (double) height - 1);
        generousBbox = new BoundingBoxImpl(bbox.getMinX() - xExtra, bbox.getMinY() - yExtra,
                bbox.getMaxX() + xExtra, bbox.getMaxY() + yExtra,
                bbox.getCoordinateReferenceSystem());
    }

    /**
     * Gets the frames as BufferedImages, ready to be turned into a picture or
     * animation. This is called just before the picture is due to be created,
     * so subclasses can delay creating the BufferedImages until all the data
     * has been extracted (for example, if we are auto-scaling an animation, we
     * can't create each individual frame until we have data for all the frames)
     * 
     * @return List of BufferedImages
     */
    public List<BufferedImage> getRenderedFrames() {
        List<BufferedImage> images = new ArrayList<BufferedImage>();

        if (style.isAutoScale()) {
            float min = Float.MAX_VALUE;
            float max = Float.MIN_VALUE;
            for (Frame frame : frameData.values()) {
                List<Float> magnitudes = frame.getGridDataMagnitudes();
                if (magnitudes != null) {
                    float minCurrent = Float.MAX_VALUE;
                    float maxCurrent = Float.MIN_VALUE;
                    for (Float val : magnitudes) {
                        if (val != null) {
                            if (val > maxCurrent)
                                maxCurrent = val;
                            if (val < minCurrent)
                                minCurrent = val;
                        }
                    }
                    if (minCurrent < min)
                        min = minCurrent;
                    if (maxCurrent > max)
                        max = maxCurrent;
                }
                List<DataPoint> pointvals = frame.getPointDataValues();
                if (pointvals != null) {
                    float minCurrent = Float.MAX_VALUE;
                    float maxCurrent = Float.MIN_VALUE;
                    for (DataPoint datum : pointvals) {
                        if (datum != null) {
                            Float val = datum.getValue();
                            if (val != null) {
                                if (val > maxCurrent)
                                    maxCurrent = val;
                                if (val < minCurrent)
                                    minCurrent = val;
                            }
                        }
                    }
                    if (minCurrent < min)
                        min = minCurrent;
                    if (maxCurrent > max)
                        max = maxCurrent;
                }
            }
            if (min > max) {
                images.add(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
                return images;
            }
            style.setScaleRange(Extents.newExtent(min, max));
        }

        List<TimePosition> times = new ArrayList<TimePosition>(frameData.keySet());
        Collections.sort(times);
        for (TimePosition time : times) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = image.getGraphics();

            Frame currentFrame = frameData.get(time);

            /*
             * Render the gridded image (if needed)
             */
            BufferedImage griddedImage = currentFrame.getGriddedImage();
            if (griddedImage == null) {
                if (currentFrame.getGridDataMagnitudes() != null) {
                    /*
                     * We have no gridded image, but we do have gridded data
                     */
                    griddedImage = createGriddedImage(currentFrame.getGridDataMagnitudes(),
                            currentFrame.getGridDataDirections());
                    graphics.drawImage(griddedImage, 0, 0, null);
                }
            } else {
                graphics.drawImage(griddedImage, 0, 0, null);
            }

            /*
             * Render the point image (if needed)
             */
            BufferedImage pointImage = currentFrame.getPointImage();
            if (pointImage == null) {
                if (currentFrame.getPointDataValues() != null) {
                    pointImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    addToPointImage(currentFrame.getPointDataValues(), pointImage);
                    graphics.drawImage(pointImage, 0, 0, null);
                }
            } else {
                graphics.drawImage(pointImage, 0, 0, null);
            }

            /*
             * Render the label (if needed)
             */
            if (currentFrame.getLabel() != null) {
                Graphics2D gfx = (Graphics2D) image.getGraphics();
                gfx.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
                gfx.setPaint(new Color(0, 0, 0));
                gfx.fillRect(1, image.getHeight() - 19, image.getWidth() - 2, 18);
                gfx.setPaint(new Color(255, 255, 255));
                gfx.drawRect(0, image.getHeight() - 20, image.getWidth() - 1, 19);
                gfx.drawString(currentFrame.getLabel(), 10, image.getHeight() - 5);
            }

            images.add(image);
            frameData.remove(time);
        }
        return images;
    }

    private BufferedImage createGriddedImage(List<Float> gridDataMagnitudes,
            List<Float> gridDataDirections) {
        // Create the pixel array for the frame
        byte[] pixels = new byte[width * height];
        for (int i = 0; i < pixels.length; i++) {
            /*
             * The image coordinate system has the vertical axis increasing
             * downward, but the data's coordinate system has the vertical axis
             * increasing upwards. The method below flips the axis
             */
            int dataIndex = getDataIndex(i);
            pixels[i] = (byte) style.getColourIndex(gridDataMagnitudes.get(dataIndex));
        }

        // Create a ColorModel for the image
        ColorModel colorModel = style.getColorModel();

        // Create the Image
        DataBuffer buf = new DataBufferByte(pixels, pixels.length);
        SampleModel sampleModel = colorModel.createCompatibleSampleModel(width, height);
        WritableRaster raster = Raster.createWritableRaster(sampleModel, buf, null);
        BufferedImage image = new BufferedImage(colorModel, raster, false, null);

        /*
         * If no style has been specified, DEFAULT will be selected. In the case
         * of default style, we want to plot direction lines for vector
         * components
         */
        if (gridDataDirections != null) {// && (style == Style.VECTOR || style
                                         // == Style.DEFAULT)) {
            // We superimpose direction arrows on top of the background
            // TODO: only do this for lat-lon projections!
            Graphics2D g = image.createGraphics();
            // TODO: control the colour of the arrows with an attribute
            // Must be part of the colour palette (here we use the colour
            // for out-of-range values)
            g.setColor(Color.BLACK);
            float arrowLength = style.getArrowLength();
            for (int i = 0; i < width; i += Math.ceil(arrowLength * 1.2)) {
                for (int j = 0; j < height; j += Math.ceil(arrowLength * 1.2)) {
                    int dataIndex = getDataIndex(i, j);
                    Float angle = gridDataDirections.get(dataIndex);
                    if (angle != null) {
                        // Calculate the end point of the arrow
                        double iEnd = i + arrowLength * Math.cos(angle);
                        // Screen coordinates go down, but north is up, hence
                        // the minus sign
                        double jEnd = j - arrowLength * Math.sin(angle);
                        // Draw a dot representing the data location
                        g.fillOval(i - 2, j - 2, 4, 4);
                        // Draw a line representing the vector direction and
                        // magnitude
                        g.setStroke(new BasicStroke(1));
                        g.drawLine(i, j, (int) Math.round(iEnd), (int) Math.round(jEnd));
                    }
                }
            }
        }

        return image;
    }

    /**
     * Calculates the index of the data point in a data array that corresponds
     * with the given index in the image array, taking into account that the
     * vertical axis is flipped.
     */
    private int getDataIndex(int imageIndex) {
        int imageI = imageIndex % width;
        int imageJ = imageIndex / width;
        return getDataIndex(imageI, imageJ);
    }

    /**
     * Calculates the index of the data point in a data array that corresponds
     * with the given index in the image array, taking into account that the
     * vertical axis is flipped.
     */
    private int getDataIndex(int imageI, int imageJ) {
        int dataJ = height - imageJ - 1;
        int dataIndex = dataJ * width + imageI;
        return dataIndex;
    }

    private void addToPointImage(List<DataPoint> pointDataValues, BufferedImage pointImage) {
        for (DataPoint dataPoint : pointDataValues) {
            addToPointImage(dataPoint, pointImage);
        }
    }

    private void addToPointImage(DataPoint pointDataValue, BufferedImage pointImage) {
        if (pointImage == null) {
            pointImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }
        int[] coords = transformPosition(pointDataValue.getPosition());
        Color color = style.getColorForValue(pointDataValue.getValue());
        ColourableIcon icon = style.getIcon();
        icon.drawOntoCanvas(coords[0], coords[1], pointImage.getGraphics(), color);
    }

    private int[] transformPosition(HorizontalPosition pos) {
        double xFrac = (pos.getX() - bbox.getMinX()) / (bbox.getMaxX() - bbox.getMinX());
        /*
         * Y appears the wrong way around because vertical co-ords are switched
         * in images
         */
        double yFrac = (bbox.getMaxY() - pos.getY()) / (bbox.getMaxY() - bbox.getMinY());
        int[] pictureCoords = new int[2];

        pictureCoords[0] = (int) (xFrac * this.width);
        pictureCoords[1] = (int) (yFrac * this.height);

        return pictureCoords;
    }
}
