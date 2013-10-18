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

import uk.ac.rdg.resc.edal.exceptions.EdalParseException;

/**
 * Class containing static utility methods for dealing with graphics
 * 
 * @author Guy Griffiths
 */
public class GraphicsUtils {
    /**
     * Parses a string to obtain a {@link Color}.
     * 
     * @param colourString
     *            A string of one of the forms:
     * 
     *            <li>0xRRGGBB
     * 
     *            <li>0xAARRGGBB
     * 
     *            <li>#RRGGBB
     * 
     *            <li>#AARRGGBB
     * 
     *            <li>"transparent"
     * 
     *            <li>"extend"
     * 
     * @return A {@link Color} representing the string, or <code>null</code> if
     *         "extend" was supplied
     * @throws EdalParseException
     *             If the format of the string does not fall into one of the
     *             above categories
     */
    public static Color parseColour(String colourString) throws EdalParseException {
        if ("transparent".equalsIgnoreCase(colourString)) {
            return new Color(0, true);
        }
        if ("extend".equalsIgnoreCase(colourString)) {
            /*
             * null represents extending the colour
             */
            return null;
        }
        if (!colourString.startsWith("0x") && !colourString.startsWith("#")) {
            throw new EdalParseException("Invalid format for colour");
        }
        if (colourString.length() == 7 || colourString.length() == 8) {
            /*
             * We have #RRGGBB or 0xRRGGBB. Color.decode can handle these
             */
            return Color.decode(colourString);
        } else if (colourString.length() == 9) {
            /*
             * We have #AARRGGBB
             */
            Color color = Color.decode("#" + colourString.substring(3));
            int alpha = Integer.parseInt(colourString.substring(1, 3), 16);
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        } else if (colourString.length() == 10) {
            /*
             * We have 0xAARRGGBB
             */
            Color color = Color.decode("0x" + colourString.substring(4));
            int alpha = Integer.parseInt(colourString.substring(2, 4), 16);
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        } else {
            throw new EdalParseException("Invalid format for colour");
        }
    }

    public static String colourToString(Color colour) {
        return String.format("#%08X", colour.getRGB());
    }
}
