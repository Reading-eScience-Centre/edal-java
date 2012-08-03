package uk.ac.rdg.resc.edal.graphics;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.util.Extents;

public class MapStyleDescriptor {
    private ColorPalette colorPalette = ColorPalette.get(null);
    /*
     * Colour scale range of the picture. An {@link Extent#isEmpty() empty
     * Range} means that the picture will be auto-scaled.
     */
    private Extent<Float> scaleRange = Extents.emptyExtent(Float.class);
    private boolean transparent = true;
    private int opacity = 100;
    private int numColourBands = 254;
    private boolean logarithmic = false;
    private Color bgColor = Color.black;
    private Map<String, ColourableIcon> icons;
    /*
     * The length of arrows in pixels, only used for vector plots
     */
    private float arrowLength = 6.0f;
    
    /*
     * We cache this for speed
     */
    private IndexColorModel indexColorModel = null;

    public MapStyleDescriptor() throws InstantiationException {
        icons = new HashMap<String, ColourableIcon>();

        URL iconUrl;
        BufferedImage iconImage;
        
        /*
         * This will work when the files are packaged as a JAR. For running
         * within an IDE, you may need to add the root directory of the project
         * to the classpath
         */
        try {
            iconUrl = this.getClass().getResource("/img/circle.png");
            iconImage = ImageIO.read(iconUrl);
            icons.put("circle", new ColourableIcon(iconImage));
            
            iconUrl = this.getClass().getResource("/img/square.png");
            iconImage = ImageIO.read(iconUrl);
            icons.put("square", new ColourableIcon(iconImage));
        } catch (IOException e) {
            throw new InstantiationException(
                    "Cannot read required icons.  Ensure that JAR is packaged correctly, or that your project is set up correctly in your IDE");
        }
    }

    public void setColorPalette(String colorPaletteName) {
        ColorPalette pal = ColorPalette.get(colorPaletteName);
        if(pal != null)
            this.colorPalette = pal;
        else
            throw new IllegalArgumentException("The palette "+colorPaletteName+" does not exist");
    }

    public void setScaleRange(Extent<Float> scaleRange) {
        this.scaleRange = scaleRange;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public void setNumColourBands(int numColourBands) {
        this.numColourBands = numColourBands;
    }

    public void setLogarithmic(boolean logarithmic) {
        this.logarithmic = logarithmic;
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    public void setArrowLength(float arrowLength) {
        this.arrowLength = arrowLength;
    }

    public void addIcon(String name, ColourableIcon pointIcon) {
        icons.put(name, pointIcon);
    }

    public float getArrowLength() {
        return arrowLength;
    }

    public Color getColorForValue(Number value){
        return new Color(getColorModel().getRGB(getColourIndex(value)));
    }
    
    public ColourableIcon getIcon(){
        return getIcon("circle");
    }
    
    public ColourableIcon getIcon(String name){
        ColourableIcon ret = null;
        if(name == null){
            ret = icons.get("circle");
        } else {
            ret = icons.get(name.toLowerCase());
        }
        if(ret != null){
            return ret;
        } else {
            return icons.get("circle");
        }
    }
    
    /**
     * @return the colour index that corresponds to the given value
     */
    public int getColourIndex(Number value) {
        if (value == null) {
            return numColourBands; // represents a background pixel
        } else if (!scaleRange.contains(value.floatValue())) {
            return numColourBands + 1; // represents an out-of-range pixel
        } else {
            float scaleMin = scaleRange.getLow().floatValue();
            float scaleMax = scaleRange.getHigh().floatValue();
            double min = logarithmic ? Math.log(scaleMin) : scaleMin;
            double max = logarithmic ? Math.log(scaleMax) : scaleMax;
            double val = logarithmic ? Math.log(value.doubleValue()) : value.doubleValue();
            double frac = (val - min) / (max - min);
            // Compute and return the index of the corresponding colour
            int index = (int) (frac * numColourBands);
            /*
             * For values very close to the maximum value in the range, this
             * index might turn out to be equal to numColourBands due to
             * rounding error. In this case we subtract one from the index to
             * ensure that such pixels are not displayed as background pixels.
             */
            if (index == numColourBands)
                index--;
            return index;
        }
    }
    
    public Extent<Float> getScaleRange(){
        return scaleRange;
    }
    
    public boolean isAutoScale(){
        return scaleRange == null || scaleRange.isEmpty();
    }
    
    public IndexColorModel getColorModel(){
        if(indexColorModel == null)
            indexColorModel = colorPalette.getColorModel(numColourBands, opacity, bgColor, transparent); 
        return indexColorModel;
    }
    
    public BufferedImage getLegend(String title, String units) {
        return colorPalette.createLegend(numColourBands, title, units, logarithmic, scaleRange);
    }
}
