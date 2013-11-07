/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc.edal.graphics.style.util;

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
