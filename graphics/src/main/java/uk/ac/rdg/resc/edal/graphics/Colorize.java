package uk.ac.rdg.resc.edal.graphics;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

public class Colorize {

    public static final int MAX_COLOR = 256;

    public static final float LUMINANCE_RED = 0.2126f;
    public static final float LUMINANCE_GREEN = 0.7152f;
    public static final float LUMINANCE_BLUE = 0.0722f;

    double hue = 180;
    double saturation = 50;
    double lightness = 0;

    int[] lum_red_lookup;
    int[] lum_green_lookup;
    int[] lum_blue_lookup;

    int[] final_red_lookup;
    int[] final_green_lookup;
    int[] final_blue_lookup;
    
    int red;
    int green;
    int blue;

    public Colorize(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
    
    public Colorize(Color c){
        this.red = c.getRed();
        this.green = c.getGreen();
        this.blue = c.getBlue();
    }

    public BufferedImage doColorize(BufferedImage image) {
        int height = image.getHeight();
        int width;
        ColorModel cm = image.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(null);
        BufferedImage ret = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
        while (height-- != 0) {
            width = image.getWidth();
            while (width-- != 0) {
                int argb = image.getRGB(width, height);
                int oldAlpha = (argb >>> 24);
                Color color = new Color(argb);
                float r = color.getRed()/255f;
                float g = color.getGreen()/255f;
                float b = color.getBlue()/255f;
//                float a = color.getAlpha()/255f;
                float y = 0.3f*r + 0.59f*g + 0.11f*b;
                Color final_color = new Color(0.5f*(red+y), 0.5f*(green+y), 0.5f*(blue+y), oldAlpha/255f);
                

                ret.setRGB(width, height, final_color.getRGB());
            }
        }
        return ret;
    }
}
