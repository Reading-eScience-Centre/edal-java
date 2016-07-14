/*******************************************************************************
 * Copyright (c) 2016 The University of Reading
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

package uk.ac.rdg.resc.edal.examples;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.imageio.ImageIO;

import uk.ac.rdg.resc.edal.graphics.utils.ColourPalette;

/**
 * Example code to draw an image displaying all of the available colour
 * palettes.
 * 
 * This also gets run during a build, and sends the output to the documentation
 * directory for inclusion in the user manual.
 *
 * @author Guy Griffiths
 */
public class DrawPalettes {
    public static void main(String[] args) throws IOException {
        Set<String> paletteNames = ColourPalette.getPredefinedPalettes();
        BufferedImage image = new BufferedImage(700, 30 * paletteNames.size(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        int vOffset = 0;
        for(String paletteName : paletteNames) {
            ColourPalette palette = ColourPalette.fromString(paletteName, 250);
            for(int i=0;i<500;i++) {
                for(int j=0;j<30;j++) {
                    image.setRGB(i, vOffset+j, palette.getColor(i/500f).getRGB());
                }
            }
            g.drawString(paletteName, 510, vOffset + 20);
            vOffset += 30;
        }
        String fileLocation = "./palettes.png";
        if(args != null && args.length > 0) {
            fileLocation = args[0];
        }
        ImageIO.write(image, "png", new File(fileLocation));
    }
}
