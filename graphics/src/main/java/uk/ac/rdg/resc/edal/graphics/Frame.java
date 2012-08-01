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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * Class representing a single map overlay image. This can contain any number of
 * layers containing {@link FrameData}. All data to be plotted is free of
 * geo-referencing by this point.
 * 
 * @author Guy Griffiths
 * 
 */
public class Frame {
    private List<FrameData> layers;
    private int width;
    private int height;
    private String label;
    private ColourableIcon circleIcon = null;
    private ColourableIcon squareIcon = null;

    public Frame(int width, int height, String label) {
        if (width == 0 || height == 0) {
            throw new IllegalArgumentException("You can't make a frame with zero width or height");
        }
        this.width = width;
        this.height = height;
        this.label = label;
        layers = new ArrayList<FrameData>();
    }
    
    public void addGridPoints(List<GridCoordinates2D> coords){
        layers.add(new GridPointsFrameData(coords));
    }

    public void addGriddedData(Number[][] data, PlotStyle style) {
        if (data.length != width) {
            throw new IllegalArgumentException("Can only add data with width " + width);
        }
        if (data[0].length != height) {
            throw new IllegalArgumentException("Can only add data with height " + height);
        }
        layers.add(new GriddedFrameData(style, data));
    }
    
    public void addPointData(Number value, GridCoordinates2D coords, PlotStyle style) {
        layers.add(new PointFrameData(style, coords.getXIndex(), coords.getYIndex(), value));
    }

    public void addMultipointData(List<Number> values, List<GridCoordinates2D> coords,
            PlotStyle plotStyle) {
        layers.add(new MultiPointFrameData(plotStyle, coords, values));
    }

    public BufferedImage renderLayers(MapStyleDescriptor style) {
        if (style.isAutoScale()) {
            style.setScaleRange(getAutoRange());
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();

        for (FrameData frameData : layers) {
            BufferedImage frameImage = null;
            switch (frameData.getPlotStyle()) {
            case BOXFILL:
                frameImage = drawGriddedImage(frameData, style);
                break;
            case VECTOR:
                frameImage = drawVectorArrows(frameData, style);
                break;
            case TRAJECTORY:
                frameImage = drawTrajectory(frameData, style);
                break;
            case POINT:
                frameImage = drawPointImage(frameData, style);
                break;
            case CONTOUR:
                throw new UnsupportedOperationException("Contour plots not yet supported");
//                break;
            case GRID_POINTS:
                frameImage = drawGridPoints(frameData);
                break;
            default:
                throw new IllegalArgumentException("Unrecognised plotting style");
            }

            graphics.drawImage(frameImage, 0, 0, null);
        }

        if (label != null && !label.equals("")) {
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

    private BufferedImage drawGriddedImage(FrameData frameData, MapStyleDescriptor style) {
        byte[] pixels = new byte[width*height];
        if (frameData instanceof GriddedFrameData) {
            Number[][] data = ((GriddedFrameData) frameData).getData();
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    Number datum = data[i][j];
                    pixels[i + width * j] = (byte) style.getColourIndex(datum);
                }
            }
            ColorModel colorModel = style.getColorModel();
            
            // Create the Image
            DataBuffer buf = new DataBufferByte(pixels, width*height);
            SampleModel sampleModel = colorModel.createCompatibleSampleModel(width, height);
            WritableRaster raster = Raster.createWritableRaster(sampleModel, buf, null);
            return new BufferedImage(colorModel, raster, false, null);
        } else {
            throw new UnsupportedOperationException("Can only plot gridded images with gridded data");
        }
    }

    private BufferedImage drawVectorArrows(FrameData frameData, MapStyleDescriptor style) {
        if (frameData instanceof GriddedFrameData) {
            Number[][] data = ((GriddedFrameData) frameData).getData();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            // We superimpose direction arrows on top of the background
            // TODO: only do this for lat-lon projections!
            Graphics2D g = image.createGraphics();
            // TODO: control the colour of the arrows with an attribute
            g.setColor(Color.BLACK);

            float arrowLength = style.getArrowLength();
            int minXMod = Integer.MAX_VALUE;
            int minYMod = Integer.MAX_VALUE;
            double spacingX = (int) Math.ceil(arrowLength * 1.5);
            double spacingY = (int) Math.ceil(arrowLength * 1.5);
            for(int s=((int)(arrowLength * 1.5)); s <(arrowLength * 2.5); s++){
                int modX = width % s; 
                if(modX < minXMod) {
                    spacingX = s;
                    minXMod = modX;
                }
                int modY = height % s;
                if(modY < minYMod){
                    spacingY = s;
                    minYMod = modY;
                }
            }
            if(minXMod != 0){
                spacingX = ((double)width)/((int)(width/spacingX)); 
            }
            if(minYMod != 0){
                spacingY = ((double)height)/((int)(height/spacingY)); 
            }
            for (double di = 0.5*spacingX; di < width; di += spacingX) {
                int i = (int) di;
                for (double dj = 0.5*spacingY; dj < height; dj += spacingY) {
                    int j = (int) dj;
                    Number angle = data[i][j];
                    if (angle != null) {
                        // Calculate the end point of the arrow
                        double iEnd = i + arrowLength * Math.cos(angle.doubleValue());
                        // Screen coordinates go down, but north is up, hence
                        // the minus sign
                        double jEnd = j - arrowLength * Math.sin(angle.doubleValue());
                        // Draw a dot representing the data location
                        g.fillOval(i - 2, j - 2, 4, 4);
                        // Draw a line representing the vector direction
                        g.setColor(Color.BLACK);
                        g.setStroke(new BasicStroke(1));
                        g.drawLine(i, j, (int) Math.round(iEnd), (int) Math.round(jEnd));
                    }
                }
            }
            return image;
        } else {
            throw new UnsupportedOperationException("Can only plot vector arrows for gridded data");
        }
    }

    private BufferedImage drawTrajectory(FrameData frameData, MapStyleDescriptor style) {
        if(frameData instanceof MultiPointFrameData){
            MultiPointFrameData multiPointFrameData = (MultiPointFrameData) frameData;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D canvas = image.createGraphics();
            
            canvas.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            double r = 10.0;
            
            PointFrameData lastPointData =  multiPointFrameData.getPointData(0);
            int lastArrowX = lastPointData.getX();
            int lastArrowY = lastPointData.getY();
            
            canvas.setPaint(style.getColorForValue(lastPointData.getValue()));
            for(int i=1; i < multiPointFrameData.size(); i++){
                
                PointFrameData pointData = multiPointFrameData.getPointData(i);
                
                /*
                 * Get image co-ordinates and calculate midpoint of line
                 */
                int midX = (lastPointData.getX()+pointData.getX())/2;
                int midY = (lastPointData.getY()+pointData.getY())/2;
                
                /*
                 * Draw a line from the last point to the mid point, in the colour
                 * representing the last point's value
                 */
                canvas.drawLine(lastPointData.getX(), lastPointData.getY(), midX, midY);

                /*
                 * Set the paint to the colour for the current point
                 */
                canvas.setPaint(style.getColorForValue(pointData.getValue()));
                /*
                 * Draw the line from the midpoint to the end of the line
                 */
                canvas.drawLine(midX, midY, pointData.getX(), pointData.getY());
                
                /*
                 * Puts arrow heads on the line segments
                 */
                double distFromLastArrow = Math.sqrt((pointData.getX() - lastArrowX)
                        * (pointData.getX() - lastArrowX) + (pointData.getY() - lastArrowY)
                        * (pointData.getY() - lastArrowY));
                if(distFromLastArrow > 30){
                    double lineAngle = 2*Math.PI-Math.atan2((pointData.getY() - lastPointData.getY()),
                            (pointData.getX() - lastPointData.getX()));
                    double headAngle1 = lineAngle+0.3;
                    double headAngle2 = lineAngle-0.3;
                    double xh1 = r*Math.cos(headAngle1); 
                    double xh2 = r*Math.cos(headAngle2); 
                    double yh1 = r*Math.sin(headAngle1); 
                    double yh2 = r*Math.sin(headAngle2);
                    canvas.drawLine(-(int)xh1+pointData.getX(), (int)yh1+pointData.getY(), pointData.getX(), pointData.getY());
                    canvas.drawLine(-(int)xh2+pointData.getX(), (int)yh2+pointData.getY(), pointData.getX(), pointData.getY());
                    lastArrowX = pointData.getX();
                    lastArrowY = pointData.getY();
                }
                /*
                 * Store the imageCoords for the next loop
                 */
                lastPointData = pointData;
            }
            return image;
        } else {
            throw new UnsupportedOperationException(
                    "Can only plot trajectories for data with more than one point");
        }
    }

    private BufferedImage drawPointImage(FrameData frameData, MapStyleDescriptor style){
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D canvas = image.createGraphics();
        if(frameData instanceof PointFrameData){
            PointFrameData pointFrameData = (PointFrameData) frameData;
            doPointPlot(pointFrameData, canvas, style, getCircleIcon());
        } else if(frameData instanceof MultiPointFrameData){
            MultiPointFrameData multiPointFrameData = (MultiPointFrameData) frameData;
            for(int i=0; i< multiPointFrameData.size(); i++){
                doPointPlot(multiPointFrameData.getPointData(i), canvas, style, getSquareIcon());
            }
        } else {
            throw new UnsupportedOperationException(
                    "Point images are currently only supported for non-gridded data");
        }
        return image;
    }
    
    private void doPointPlot(PointFrameData pointFrameData, Graphics2D canvas, MapStyleDescriptor style, ColourableIcon pointIcon){
        if(!pointFrameData.getValue().equals(Float.NaN)) {
            Color color = style.getColorForValue(pointFrameData.getValue());
            canvas.drawImage(pointIcon.getColouredIcon(color), pointFrameData.getX()
                    - pointIcon.getWidth() / 2, height - (pointFrameData.getY()
                            - pointIcon.getHeight() / 2) - 1, null);
        }
    }
    
    private ColourableIcon getCircleIcon(){
        if(circleIcon  == null){
            BufferedImage iconImage;
            try {
                /*
                 * This will work when the files are packaged as a JAR. For running
                 * within an IDE, you may need to add the root directory of the project
                 * to the classpath
                 */
                iconImage = ImageIO.read(this.getClass().getResource("/img/circle.png"));
                circleIcon = new ColourableIcon(iconImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return circleIcon;
    }
    
    private ColourableIcon getSquareIcon(){
        if(squareIcon  == null){
            BufferedImage iconImage;
            try {
                /*
                 * This will work when the files are packaged as a JAR. For running
                 * within an IDE, you may need to add the root directory of the project
                 * to the classpath
                 */
                iconImage = ImageIO.read(this.getClass().getResource("/img/square.png"));
                squareIcon = new ColourableIcon(iconImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return squareIcon;
    }
    
    private BufferedImage drawGridPoints(FrameData frameData) {
        if(!(frameData instanceof GridPointsFrameData)){
            throw new IllegalArgumentException("We need grid point data to plot grid points");
        } else {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            GridPointsFrameData gridPointsFrameData = (GridPointsFrameData) frameData;
            int black = Color.BLACK.getRGB();
            for(GridCoordinates2D gridCoord : gridPointsFrameData.getPointData()){
                int x = gridCoord.getXIndex();
                int y = gridCoord.getYIndex();
                if(x >= 0 && x < width && y >= 0 && y < height)
                    image.setRGB(x, y, black);
            }
            return image;
        }
    }

    public Extent<Float> getAutoRange() {
        Float min = Float.MAX_VALUE;
        Float max = Float.MIN_VALUE;
        for (FrameData layer : layers) {
            if (layer instanceof GriddedFrameData) {
                GriddedFrameData griddedFrameData = (GriddedFrameData) layer;
                Number[][] data = griddedFrameData.getData();
                /*
                 * Directional data doesn't need to have the same range as
                 * everything else.
                 * 
                 * If new data types are defined which may be plotted without
                 * worrying about their ranges matching, add them to this list
                 */
                if (layer.getPlotStyle() != PlotStyle.VECTOR) {
                    for (int i = 0; i < data.length; i++) {
                        for (int j = 0; j < data[i].length; j++) {
                            Number value = data[i][j];
                            if (value != null && !value.equals(Float.NaN)
                                    && !value.equals(Double.NaN)) {
                                if (value.floatValue() < min)
                                    min = value.floatValue();
                                if (value.floatValue() > max)
                                    max = value.floatValue();
                            }
                        }
                    }
                }
            } else if (layer instanceof PointFrameData) {
                PointFrameData pointFrameData = (PointFrameData) layer;
                Number value = pointFrameData.getValue();
                if (value != null && !value.equals(Float.NaN) && !value.equals(Double.NaN)) {
                    if (value.floatValue() < min)
                        min = value.floatValue();
                    if (value.floatValue() > max)
                        max = value.floatValue();
                }
            } else if (layer instanceof MultiPointFrameData) {
                MultiPointFrameData multiPointFrameData = (MultiPointFrameData) layer;
                for(int i=0; i< multiPointFrameData.size(); i++){
                    PointFrameData pointFrameData = multiPointFrameData.getPointData(i);
                    Number value = pointFrameData.getValue();
                    if (value != null && !value.equals(Float.NaN) && !value.equals(Double.NaN)) {
                        if (value.floatValue() < min)
                            min = value.floatValue();
                        if (value.floatValue() > max)
                            max = value.floatValue();
                    }
                }
            }
        }
        if (min.equals(Float.MAX_VALUE) || max.equals(Float.MIN_VALUE)) {
            /*
             * We have no data where the ranges matter. Return something anyway
             */
            return Extents.newExtent(0.0f, 1.0f);
        }
        return Extents.newExtent(min, max);
    }
    
    public static void main(String[] args) throws InstantiationException, IOException {
//        Frame f = new Frame(500, 500, null);        
//        List<GridCoordinates2D> coords = new ArrayList<GridCoordinates2D>();
//        for(int xIndex = 0; xIndex < 500; xIndex += 10){
//            for(int yIndex = 0; yIndex < 500; yIndex += 10){
//                coords.add(new GridCoordinates2DImpl(xIndex, yIndex));
//            }
//        }
//        f.addGridPoints(coords);
//        BufferedImage renderLayers = f.renderLayers(new MapStyleDescriptor());
//        ImageIO.write(renderLayers, "png", new File("/home/guy/grid.png"));
        
        BufferedImage image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.drawLine(-10, 250, 490, 250);
        ImageIO.write(image, "png", new File("/home/guy/00line.png"));
    }
}
