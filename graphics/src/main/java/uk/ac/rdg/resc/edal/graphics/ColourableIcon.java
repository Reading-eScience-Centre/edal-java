package uk.ac.rdg.resc.edal.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

public class ColourableIcon {
    private BufferedImage icon;
    private int height;
    private int width;
    
    public ColourableIcon(BufferedImage icon) {
        this.icon = icon;
        height = icon.getHeight();
        width = icon.getWidth();
    }
    
    public void drawOntoCanvas(int x, int y, Graphics graphics, Color col){
        graphics.drawImage(getColouredIcon(col), x-width/2, y-height/2, null);
    }
    
    public BufferedImage getColouredIcon(Color colour){
        return getColouredIcon(colour.getRed(), colour.getGreen(), colour.getBlue());
    }
    
    private BufferedImage getColouredIcon(int red, int green, int blue) {
        BufferedImage colouredIcon = cloneImage();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int argb = icon.getRGB(i,j);
                int alpha = (argb >>> 24);
                Color currentColour = new Color(argb);
                int r = currentColour.getRed();
                int g = currentColour.getGreen();
                int b = currentColour.getBlue();
                float y = 0.3f*r + 0.59f*g + 0.11f*b;
//                Color final_color = new Color((int)(red+y)/2,(int)(green+y)/2,(int)(blue+y)/2, alpha);
                Color final_color = new Color((int)(3*red+y)/4,(int)(3*green+y)/4,(int)(3*blue+y)/4, alpha);
                colouredIcon.setRGB(i, j, final_color.getRGB());
            }
        }
        return colouredIcon;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    private BufferedImage cloneImage(){
        ColorModel cm = icon.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = icon.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}
