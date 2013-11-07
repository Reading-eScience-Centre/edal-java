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
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import uk.ac.rdg.resc.edal.graphics.style.util.StyleXMLParser.ColorAdapter;

public class ColourPalette {
    /**
     * The name of the default palette that will be used if the user doesn't
     * request a specific palette.
     * 
     * @see DEFAULT_PALETTE
     */
    public static final String DEFAULT_PALETTE_NAME = "rainbow";

    public static final int MAX_NUM_COLOURS = 250;

    /**
     * This is the palette that will be used if no specific palette has been
     * chosen. This palette is taken from the SGT graphics toolkit.
     * 
     * @see DEFAULT_PALETTE_NAME
     */
    private static final Color[] DEFAULT_COLOURS = new Color[] { new Color(0, 0, 143),
            new Color(0, 0, 159), new Color(0, 0, 175), new Color(0, 0, 191), new Color(0, 0, 207),
            new Color(0, 0, 223), new Color(0, 0, 239), new Color(0, 0, 255),
            new Color(0, 11, 255), new Color(0, 27, 255), new Color(0, 43, 255),
            new Color(0, 59, 255), new Color(0, 75, 255), new Color(0, 91, 255),
            new Color(0, 107, 255), new Color(0, 123, 255), new Color(0, 139, 255),
            new Color(0, 155, 255), new Color(0, 171, 255), new Color(0, 187, 255),
            new Color(0, 203, 255), new Color(0, 219, 255), new Color(0, 235, 255),
            new Color(0, 251, 255), new Color(7, 255, 247), new Color(23, 255, 231),
            new Color(39, 255, 215), new Color(55, 255, 199), new Color(71, 255, 183),
            new Color(87, 255, 167), new Color(103, 255, 151), new Color(119, 255, 135),
            new Color(135, 255, 119), new Color(151, 255, 103), new Color(167, 255, 87),
            new Color(183, 255, 71), new Color(199, 255, 55), new Color(215, 255, 39),
            new Color(231, 255, 23), new Color(247, 255, 7), new Color(255, 247, 0),
            new Color(255, 231, 0), new Color(255, 215, 0), new Color(255, 199, 0),
            new Color(255, 183, 0), new Color(255, 167, 0), new Color(255, 151, 0),
            new Color(255, 135, 0), new Color(255, 119, 0), new Color(255, 103, 0),
            new Color(255, 87, 0), new Color(255, 71, 0), new Color(255, 55, 0),
            new Color(255, 39, 0), new Color(255, 23, 0), new Color(255, 7, 0),
            new Color(246, 0, 0), new Color(228, 0, 0), new Color(211, 0, 0), new Color(193, 0, 0),
            new Color(175, 0, 0), new Color(158, 0, 0), new Color(140, 0, 0) };

    private static Map<String, Color[]> loadedColourSets = new HashMap<String, Color[]>();
    private static ColorAdapter cParser;

    static {
        /*
         * Make sure these are initialised here (more reliable than relying on
         * them being initialised in file order in case of future refactoring)
         */
        loadedColourSets = new HashMap<String, Color[]>();
        cParser = ColorAdapter.getInstance();

        loadedColourSets.put(DEFAULT_PALETTE_NAME.toLowerCase(), DEFAULT_COLOURS);
        loadedColourSets.put("default", DEFAULT_COLOURS);

        try {
            String[] paletteFileNames = getResourceListing(ColourPalette.class, "palettes/");
            for (String paletteFileName : paletteFileNames) {
                if (paletteFileName.endsWith(".pal")) {
                    try {
                        String paletteName = paletteFileName.substring(0,
                                paletteFileName.lastIndexOf("."));
                        StringBuilder paletteString = new StringBuilder();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(
                                ColourPalette.class.getResource("/palettes/" + paletteFileName)
                                        .openStream()));
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            if (!line.startsWith("%")) {
                                paletteString.append(line);
                                paletteString.append(",");
                            }
                        }
                        paletteString.deleteCharAt(paletteString.length() - 1);
                        Color[] colourSet = colourSetFromString(paletteString.toString());
                        loadedColourSets.put(paletteName, colourSet);
                    } catch (IOException e) {
                        /*
                         * If we can't add this palette, don't add it
                         */
                    }
                }
            }
        } catch (Exception e) {
            /*
             * This catches anything thrown whilst trying to read the palettes
             * directory
             */
        }
    }

    private final Color[] colours;

    private ColourPalette(Color[] palette, int numColorBands) {
        colours = generateColourSet(palette, numColorBands);
    }

    /**
     * Gets a version of this palette with the given number of color bands,
     * either by subsampling or interpolating the existing palette
     * 
     * @param numColorBands
     *            The number of bands of colour to be used in the new palette
     * @return An array of Colors, with length numColorBands
     * @throws IllegalArgumentException
     *             if the requested number of colour bands is less than one or
     *             greater than {@link #MAX_NUM_COLOURS}.
     */
    private static Color[] generateColourSet(Color[] palette, int numColorBands) {
        if (numColorBands < 1 || numColorBands > MAX_NUM_COLOURS) {
            throw new IllegalArgumentException(
                    "numColorBands must be greater than 1 and less than " + MAX_NUM_COLOURS);
        }
        Color[] targetPalette;
        if (numColorBands == palette.length) {
            /* We can just use the source palette directly */
            targetPalette = palette;
        } else {
            /* We need to create a new palette */
            targetPalette = new Color[numColorBands];
            /*
             * We fix the endpoints of the target palette to the endpoints of
             * the source palette
             */
            targetPalette[0] = palette[0];
            targetPalette[targetPalette.length - 1] = palette[palette.length - 1];

            if (targetPalette.length < palette.length) {
                /*
                 * We only need some of the colours from the source palette We
                 * search through the target palette and find the nearest
                 * colours in the source palette
                 */
                for (int i = 1; i < targetPalette.length - 1; i++) {
                    /*
                     * Find the nearest index in the source palette (Multiplying
                     * by 1.0f converts integers to floats)
                     */
                    int nearestIndex = Math.round(palette.length * i * 1.0f
                            / (targetPalette.length - 1));
                    targetPalette[i] = palette[nearestIndex];
                }
            } else {
                /*
                 * Transfer all the colours from the source palette into their
                 * corresponding positions in the target palette and use
                 * interpolation to find the remaining values
                 */
                int lastIndex = 0;
                for (int i = 1; i < palette.length - 1; i++) {
                    /* Find the nearest index in the target palette */
                    int nearestIndex = Math.round(targetPalette.length * i * 1.0f
                            / (palette.length - 1));
                    targetPalette[nearestIndex] = palette[i];
                    /* Now interpolate all the values we missed */
                    for (int j = lastIndex + 1; j < nearestIndex; j++) {
                        /*
                         * Work out how much we need from the previous colour
                         * and how much from the new colour
                         */
                        float fracFromThis = (1.0f * j - lastIndex) / (nearestIndex - lastIndex);
                        targetPalette[j] = interpolate(targetPalette[nearestIndex],
                                targetPalette[lastIndex], fracFromThis);

                    }
                    lastIndex = nearestIndex;
                }
                /* Now for the last bit of interpolation */
                for (int j = lastIndex + 1; j < targetPalette.length - 1; j++) {
                    float fracFromThis = (1.0f * j - lastIndex)
                            / (targetPalette.length - lastIndex);
                    targetPalette[j] = interpolate(targetPalette[targetPalette.length - 1],
                            targetPalette[lastIndex], fracFromThis);
                }
            }
        }
        return targetPalette;
    }

    /**
     * Linearly interpolates between two RGB colours
     * 
     * @param c1
     *            the first colour
     * @param c2
     *            the second colour
     * @param fracFromC1
     *            the fraction of the final colour that will come from c1
     * @return the interpolated Color
     */
    private static Color interpolate(Color c1, Color c2, float fracFromC1) {
        float fracFromC2 = 1.0f - fracFromC1;
        return new Color(Math.round(fracFromC1 * c1.getRed() + fracFromC2 * c2.getRed()),
                Math.round(fracFromC1 * c1.getGreen() + fracFromC2 * c2.getGreen()),
                Math.round(fracFromC1 * c1.getBlue() + fracFromC2 * c2.getBlue()),
                Math.round(fracFromC1 * c1.getAlpha() + fracFromC2 * c2.getAlpha()));
    }

    public Color getColor(float value) {
        if (value < 0.0f || value > 1.0f) {
            throw new IllegalArgumentException("value must be between 0 and 1");
        }
        /* Find the nearest colour in the palette */
        int i = (int) (value * this.colours.length);
        /*
         * Correct in the special case that value = 1 to keep within bounds of
         * array
         */
        if (i == this.colours.length) {
            i--;
        }
        return this.colours[i];
    }

    public static ColourPalette fromString(String paletteString, int nColourBands) {
        if(paletteString == null || "".equals(paletteString)) {
            paletteString = DEFAULT_PALETTE_NAME;
        }
        if (loadedColourSets.containsKey(paletteString.toLowerCase())) {
            return new ColourPalette(loadedColourSets.get(paletteString), nColourBands);
        } else {
            try {
                Color[] colours = colourSetFromString(paletteString);
                return new ColourPalette(colours, nColourBands);
            } catch (Exception e) {
                throw new IllegalArgumentException(paletteString
                        + " is not an existing palette name or a palette definition");
            }
        }
    }

    public static Set<String> getPredefinedPalettes() {
        return loadedColourSets.keySet();
    }

    private static Color[] colourSetFromString(String paletteString) {
        String[] colourStrings = paletteString.split("[,\n]");
        Color[] colours = new Color[colourStrings.length];
        for (int i = 0; i < colourStrings.length; i++) {
            try {
                colours[i] = cParser.unmarshal(colourStrings[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return colours;
    }

    /**
     * This method was taken from the internet, and is used here to extract the
     * palette names from the JAR. This allows us to package palettes with the
     * edal-graphics library
     * 
     * List directory contents for a resource folder. Not recursive. This is
     * basically a brute-force implementation. Works for regular files and also
     * JARs.
     * 
     * Taken from: http://www.uofr.net/~greg/java/get-resource-listing.html
     * 
     * @author Greg Briggs
     * @param clazz
     *            Any java class that lives in the same place as the resources
     *            you want.
     * @param path
     *            Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @throws URISyntaxException
     * @throws IOException
     */
    @SuppressWarnings("rawtypes")
    private static String[] getResourceListing(Class clazz, String path) throws URISyntaxException,
            IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            /* A file path: easy enough */
            return new File(dirURL.toURI()).list();
        }

        if (dirURL == null) {
            /*
             * In case of a jar file, we can't actually find a directory. Have
             * to assume the same jar as clazz.
             */
            String me = clazz.getName().replace(".", "/") + ".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            /* strip out only the JAR file */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            /* gives ALL entries in jar */
            Enumeration<JarEntry> entries = jar.entries();
            Set<String> result = new HashSet<String>();
            /* avoid duplicates in case it is a subdirectory */
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                /* filter according to the path */
                if (name.startsWith(path)) {
                    String entry = name.substring(path.length());
                    int checkSubdir = entry.indexOf("/");
                    if (checkSubdir >= 0) {
                        /*
                         * if it is a subdirectory, we just return the directory
                         * name
                         */
                        entry = entry.substring(0, checkSubdir);
                    }
                    result.add(entry);
                }
            }
            jar.close();
            return result.toArray(new String[result.size()]);
        }

        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }

    public BufferedImage createColourBar(int width, int height, boolean vertical) {
        BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                float frac;
                if (vertical) {
                    frac = ((float) j) / height;
                } else {
                    frac = ((float) i) / width;
                }
                ret.setRGB(i, height - j - 1, getColor(frac).getRGB());
            }
        }
        return ret;
    }
}
