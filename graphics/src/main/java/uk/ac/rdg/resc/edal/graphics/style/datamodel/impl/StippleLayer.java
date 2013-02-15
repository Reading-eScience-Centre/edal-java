package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.geotoolkit.internal.jaxb.gmd.PT_FreeText;

import uk.ac.rdg.resc.edal.graphics.style.DataReadingTypes.PlotType;
import uk.ac.rdg.resc.edal.graphics.style.PlottingDatum;

@XmlType(namespace = Image.NAMESPACE, name = "PatternLayerType")
public class StippleLayer extends ImageLayer {
    
    @XmlElement(name = "DataFieldName", required = true)
    private String dataFieldName;
    
    @XmlElement(name = "Scale", required = true)
    private PatternScale scale;
    
    private StippleLayer() {
        super(PlotType.RASTER);
    }
    
    
    public StippleLayer(String dataFieldName, PatternScale scale) {
        super(PlotType.RASTER);
        this.dataFieldName = dataFieldName;
        this.scale = scale;
    }

    @Override
    protected void drawIntoImage(BufferedImage image, DataReader dataReader) {
        /*
         * Use the scale object to paint the image in banded shades of blue
         */
        for (PlottingDatum datum : dataReader.getDataForLayerName(dataFieldName)) {
            /*
             * This is an int between 0 and 255.  This corresponds to the RGB values (0,0,0) to (0,0,255).
             */
            int blueness = (int) (256 * (scale.getLevel(datum.getValue()) / (float) (scale.getNLevels()-1)));
            image.setRGB(datum.getGridCoords().getXIndex(), datum.getGridCoords().getYIndex(), blueness);
        }
        /*
         * Apply black/transparent stippling to the blue image
         */
        stippleBlueness(image);
    }
    
    private static int[][] thresholdMap = new int[][]{
        {1,49,13,61,4,52,16,64},
        {33,17,45,29,36,20,48,32},    
        {9,57,5,53,12,60,8,56},    
        {41,25,37,21,44,28,40,24},    
        {3,51,15,63,2,50,14,62},    
        {35,19,47,31,34,18,46,30},    
        {11,59,7,55,10,58,6,54},
        {43,27,39,23,42,26,38,22}
    };
    
    /*
     * This stipples an image into black and transparent pixels depending on the
     * blueness of the pixel.
     * 
     * Blueness is used, because Color.getRGB() returns an int whose final 8
     * bits correspond to the amount of blueness. This means that setting a
     * pixel to a value between 0 and 255 corresponds to blueness, and no other
     * calculation needs to be made
     */
    private static void stippleBlueness(BufferedImage blueImage) {
        int black = Color.black.getRGB();
        int transparent = new Color(0.0f, 0.0f, 0.0f, 0.0f).getRGB();
        for (int x = 0; x < blueImage.getWidth(); x++) {
            int xmod = x % thresholdMap.length;
            for (int y = 0; y < blueImage.getHeight(); y++) {
                int ymod = y % thresholdMap[0].length;
                int blueness = blueImage.getRGB(x,y);
                if(blueness > 256 * thresholdMap[xmod][ymod] /  ((float) thresholdMap.length * thresholdMap[0].length + 1)){
                    blueImage.setRGB(x, y, black);
                } else {
                    blueImage.setRGB(x, y, transparent);
                }
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        int levels = 10;
        int size = 100;
        BufferedImage image = new BufferedImage(levels * size, size, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g = image.createGraphics();
        for(int i = 0; i < levels; i++) {
            int blueness = 255 * i/(levels - 1);
            BufferedImage stipple = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    stipple.setRGB(x, y, blueness);
                }
            }
            stippleBlueness(stipple);
            g.drawImage(stipple, i*size, 0, null);
        }
        ImageIO.write(image, "png", new File("/home/guy/stipples.png"));
    }
}
