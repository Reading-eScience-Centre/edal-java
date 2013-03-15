package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.DataReadingTypes.PlotType;
import uk.ac.rdg.resc.edal.graphics.style.PlottingDatum;
import uk.ac.rdg.resc.edal.util.Extents;

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
        int[][] alphas = new int[image.getWidth()][image.getHeight()];
        /*
         * Use the scale object to paint the image in banded shades of blue
         */
        for (PlottingDatum datum : dataReader.getDataForLayerName(dataFieldName)) {
            /*
             * This is an int between 0 and 255.  This corresponds to the RGB values (0,0,0) to (0,0,255).
             */
            int alpha = (int) (256 * (scale.getLevel(datum.getValue()) / (float) (scale.getNLevels()-1)));
            alphas[datum.getGridCoords().getXIndex()][datum.getGridCoords().getYIndex()] = alpha;
        }
        /*
         * Apply black/transparent stippling to the blue image
         */
        stippleAlphas(image, alphas, image.getWidth(), image.getHeight());
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
    private static void stippleAlphas(BufferedImage image, int[][] alphas, int width, int height) {
        int black = Color.black.getRGB();
        int transparent = new Color(0.0f, 0.0f, 0.0f, 0.0f).getRGB();
        for (int x = 0; x < width; x++) {
            int xmod = x % thresholdMap.length;
            for (int y = 0; y < height; y++) {
                int ymod = y % thresholdMap[0].length;
                int alpha = alphas[x][y];
                if(alpha > 256 * thresholdMap[xmod][ymod] /  ((float) thresholdMap.length * thresholdMap[0].length + 1)){
                    image.setRGB(x, y, black);
                } else {
                    image.setRGB(x, y, transparent);
                }
            }
        }
    }
    
    @Override
    protected Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new HashSet<Drawable.NameAndRange>();
        if (scale.getOpaqueValue() > scale.getTransparentValue()) {
            ret.add(new NameAndRange(dataFieldName, Extents.newExtent(scale.getTransparentValue(),
                    scale.getOpaqueValue())));
        } else {
            ret.add(new NameAndRange(dataFieldName, Extents.newExtent(scale.getOpaqueValue(),
                    scale.getTransparentValue())));
        }
        return ret;
    }
}
