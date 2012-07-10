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
import java.util.ArrayList;
import java.util.List;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.util.Extents;

public class Frame {
    private List<DataStylePair> layers;
    private int width;
    private int height;
    private String label;

    private class DataStylePair{
        private PlotStyle plotStyle;
        private Number[] data;
        
        public DataStylePair(PlotStyle plotStyle, Number[] data) {
            this.plotStyle = plotStyle;
            this.data = data;
        }
    }
    
    public Frame(int width, int height, String label) {
        this.width = width;
        this.height = height;
        this.label = label;
        layers = new ArrayList<DataStylePair>();
    }

    public void addData(Number[] data, PlotStyle style) {
        if (data.length != width * height) {
            throw new IllegalArgumentException("Can only add data with size " + (width * height));
        }
        layers.add(new DataStylePair(style, data));
    }

    public BufferedImage renderLayers(MapStyleDescriptor style) {
        if (style.isAutoScale()) {
            style.setScaleRange(getAutoRange());
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();

        for (DataStylePair dataStylePair : layers) {
            BufferedImage frameImage = null;
            switch (dataStylePair.plotStyle) {
            case BOXFILL:
                frameImage = drawGriddedImage(dataStylePair.data, style);
                break;
            case VECTOR:
                frameImage = drawVectorArrows(dataStylePair.data, style);
                break;
            case TRAJECTORY:
                throw new IllegalArgumentException("Trajectory plots not yet supported");
//                break;
            case POINT:
                throw new IllegalArgumentException("Point plots not yet supported");
//                break;
            case CONTOUR:
                throw new IllegalArgumentException("Contour plots not yet supported");
//                break;
            default:
                throw new IllegalArgumentException("Unrecognised plotting style");
            }

            graphics.drawImage(frameImage, 0, 0, null);
        }
        
        if(label != null && !label.equals("")){
            Graphics2D gfx = (Graphics2D) image.getGraphics();
            gfx.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
            gfx.setPaint(new Color(0, 0, 0));
            gfx.fillRect(1, image.getHeight() - 19, image.getWidth() - 2, 18);
            gfx.setPaint(new Color(255, 255, 255));
            gfx.drawRect(0, image.getHeight() - 20, image.getWidth() - 1, 19);
            gfx.drawString(label, 10, image.getHeight() - 5);
        }
        
        return image;
    }
    
    private BufferedImage drawGriddedImage(Number[] data, MapStyleDescriptor style){
        byte[] pixels = new byte[width * height];
        for (int i = 0; i < pixels.length; i++) {
            Number datum = data[i];
            pixels[i] = (byte) style.getColourIndex(datum);
        }
        // Create a ColorModel for the image
        ColorModel colorModel = style.getColorModel();

        // Create the Image
        DataBuffer buf = new DataBufferByte(pixels, pixels.length);
        SampleModel sampleModel = colorModel.createCompatibleSampleModel(width, height);
        WritableRaster raster = Raster.createWritableRaster(sampleModel, buf, null);
        return new BufferedImage(colorModel, raster, false, null);
    }
    
    private BufferedImage drawVectorArrows(Number[] data, MapStyleDescriptor style){
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        // We superimpose direction arrows on top of the background
        // TODO: only do this for lat-lon projections!
        Graphics2D g = image.createGraphics();
        // TODO: control the colour of the arrows with an attribute
        g.setColor(Color.BLACK);

        float arrowLength = style.getArrowLength();
        for (int i = 0; i < width; i += Math.ceil(arrowLength * 1.2)) {
            for (int j = 0; j < height; j += Math.ceil(arrowLength * 1.2)) {
                int dataIndex = j * width + i;
                Number angle = data[dataIndex];
                if (angle != null) {
                    // Calculate the end point of the arrow
                    double iEnd = i + arrowLength * Math.cos(angle.doubleValue());
                    // Screen coordinates go down, but north is up, hence
                    // the minus sign
                    double jEnd = j - arrowLength * Math.sin(angle.doubleValue());
                    // Draw a dot representing the data location
                    g.fillOval(i - 2, j - 2, 4, 4);
                    // Draw a line representing the vector direction and
                    // magnitude
                    g.setStroke(new BasicStroke(1));
                    g.drawLine(i, j, (int) Math.round(iEnd), (int) Math.round(jEnd));
                }
            }
        }
        return image;
    }

    public Extent<Float> getAutoRange() {
        Float min = Float.MAX_VALUE;
        Float max = Float.MIN_VALUE;
        for (DataStylePair layer : layers) {
            /*
             * Directional data doesn't need to have the same range as
             * everything else.
             * 
             * If new data types are defined which may be plotted without
             * worrying about their ranges matching, add them to this list
             */
            if(layer.plotStyle != PlotStyle.VECTOR){
                for (Number f : layer.data) {
                    if (f != null && !f.equals(Float.NaN) && !f.equals(Double.NaN)) {
                        if (f.floatValue() < min)
                            min = f.floatValue();
                        if (f.floatValue() > max)
                            max = f.floatValue();
                    }
                }
            }
        }
        if(min.equals(Float.MAX_VALUE) || max.equals(Float.MIN_VALUE)){
            /*
             * We have no data where the ranges matter.  Return something anyway
             */
            return Extents.newExtent(0.0f, 1.0f);
        }
        return Extents.newExtent(min, max);
    }
}
