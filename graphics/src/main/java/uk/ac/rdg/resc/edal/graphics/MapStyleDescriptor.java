package uk.ac.rdg.resc.edal.graphics;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.util.Extents;

public class MapStyleDescriptor {
    public static enum Style {
        BOXFILL, VECTOR, POINT, TRAJECTORY, DEFAULT
    };

    public Style getStyle() {
        return style;
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

    public void setStyle(Style style) {
        this.style = style;
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

    public void setPointIcon(ColourableIcon pointIcon) {
        this.pointIcon = pointIcon;
    }

    public float getArrowLength() {
        return arrowLength;
    }

    private ColorPalette colorPalette = ColorPalette.get(null);
    /*
     * Colour scale range of the picture. An {@link Extent#isEmpty() empty
     * Range} means that the picture will be auto-scaled.
     */
    private Extent<Float> scaleRange = Extents.emptyExtent(Float.class);
    private Style style;
    private boolean transparent = true;
    private int opacity = 100;
    private int numColourBands = 254;
    private boolean logarithmic = false;
    private Color bgColor = Color.black;

    /*
     * The length of arrows in pixels, only used for vector plots
     */
    private float arrowLength = 10.0f;
    /*
     * The icon to use for point data
     */
    private ColourableIcon pointIcon = null;
    
    public Color getColorForValue(Float value){
        return new Color(getColorModel().getRGB(getColourIndex(value)));
    }
    
    public ColourableIcon getIcon(){
        if(pointIcon == null){
            BufferedImage iconImage;
            try {
                /*
                 * This will work when the files are packaged as a JAR. For running
                 * within an IDE, you may need to add the root directory of the project
                 * to the classpath
                 */
                URL iconUrl = this.getClass().getResource("/img/data_point3.png");
                iconImage = ImageIO.read(iconUrl);
                pointIcon = new ColourableIcon(iconImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return pointIcon;
    }
    
    /**
     * @return the colour index that corresponds to the given value
     */
    public int getColourIndex(Float value) {
        if (value == null) {
            return numColourBands; // represents a background pixel
        } else if (!scaleRange.contains(value)) {
            return numColourBands + 1; // represents an out-of-range pixel
        } else {
            float scaleMin = scaleRange.getLow().floatValue();
            float scaleMax = scaleRange.getHigh().floatValue();
            double min = logarithmic ? Math.log(scaleMin) : scaleMin;
            double max = logarithmic ? Math.log(scaleMax) : scaleMax;
            double val = logarithmic ? Math.log(value) : value;
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
    
    public boolean isAutoScale(){
        return scaleRange == null || scaleRange.isEmpty();
    }
    
    public IndexColorModel getColorModel(){
        return colorPalette.getColorModel(numColourBands, opacity, bgColor, transparent);
    }
    
    public BufferedImage getLegend(String title, String units) {
        return colorPalette.createLegend(numColourBands, title, units, logarithmic, scaleRange);
    }
}
