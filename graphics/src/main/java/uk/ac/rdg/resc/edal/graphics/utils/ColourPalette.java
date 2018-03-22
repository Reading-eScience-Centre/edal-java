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

package uk.ac.rdg.resc.edal.graphics.utils;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.graphics.utils.GraphicsUtils.ColorAdapter;

public class ColourPalette {
    private static final Logger log = LoggerFactory.getLogger(ColourPalette.class);

    /**
     * The name of the default palette that will be used if the user doesn't
     * request a specific palette.
     * 
     * @see ColourPalette#DEFAULT_COLOURS
     */
    public static final String DEFAULT_PALETTE_NAME = "default";

    public static final int MAX_NUM_COLOURS = 250;

    public static final String INVERSE_SUFFIX = "-inv";

    /**
     * This is the palette that will be used if no specific palette has been
     * chosen. This palette is taken from the SGT graphics toolkit.
     * 
     * Equivalent to the string #081D58,#41B6C4,#FFFFD9
     * 
     * Same as seq-BuYl
     * 
     * @see ColourPalette#DEFAULT_PALETTE_NAME
     */
    private static final Color[] DEFAULT_COLOURS = new Color[] { new Color(8, 29, 88),
            new Color(65, 182, 196), new Color(255, 255, 217) };

    private static Map<String, Color[]> loadedColourSets = new HashMap<>();
    private static ColorAdapter cParser;

    static {
        /*
         * Make sure these are initialised here (more reliable than relying on
         * them being initialised in file order in case of future refactoring)
         */
        loadedColourSets = new TreeMap<String, Color[]>();
        cParser = ColorAdapter.getInstance();

        loadedColourSets.put(DEFAULT_PALETTE_NAME, DEFAULT_COLOURS);
        Color[] invColourSet = (Color[]) ArrayUtils.clone(DEFAULT_COLOURS);
        ArrayUtils.reverse(invColourSet);
        loadedColourSets.put(DEFAULT_PALETTE_NAME + INVERSE_SUFFIX, invColourSet);

        try {
            String[] paletteFileNames = getResourceListing(ColourPalette.class, "palettes/");
            for (String paletteFileName : paletteFileNames) {
                if (paletteFileName.endsWith(".pal")) {
                    String paletteName = paletteFileName.substring(0,
                            paletteFileName.lastIndexOf("."));
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(ColourPalette.class
                                    .getResource("/palettes/" + paletteFileName).openStream()))) {
                        addPaletteFile(paletteName, reader);
                    } catch (IOException e) {
                        /*
                         * If we can't add this palette, don't add it
                         */
                        log.warn("Couldn't add palette: " + paletteName, e);
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

    /**
     * Overrides the default palette
     * 
     * @param paletteStr
     *            The palette (pre-defined or otherwise) to use as the default
     * @return Whether or not the operation was successful.
     */
    public static boolean setDefaultPalette(String paletteStr) {
        Color[] colourSet = null;
        if (getPredefinedPalettes().contains(paletteStr)) {
            colourSet = loadedColourSets.get(paletteStr);
        } else {
            colourSet = colourSetFromString(paletteStr);
        }
        if (colourSet != null) {
            loadedColourSets.put(DEFAULT_PALETTE_NAME, colourSet);
            Color[] invColourSet = (Color[]) ArrayUtils.clone(colourSet);
            ArrayUtils.reverse(invColourSet);
            loadedColourSets.put(DEFAULT_PALETTE_NAME + INVERSE_SUFFIX, invColourSet);
            return true;
        }
        return false;
    }

    public static void addPaletteDirectory(File paletteDir) throws FileNotFoundException {
        if (paletteDir.isDirectory()) {
            for (String paletteFileName : paletteDir.list()) {
                if (paletteFileName.endsWith(".pal")) {
                    String paletteName = paletteFileName.substring(0,
                            paletteFileName.lastIndexOf("."));
                    try (BufferedReader reader = new BufferedReader(new FileReader(
                            new File(paletteDir.getAbsolutePath() + "/" + paletteFileName)))) {
                        addPaletteFile(paletteName, reader);
                    } catch (IOException e) {
                        /*
                         * If we can't add this palette, don't add it
                         */
                        log.warn("Couldn't add palette: " + paletteName, e);
                    }
                }
            }
        } else {
            throw new FileNotFoundException(paletteDir.getAbsolutePath() + " is not a directory");
        }
    }

    private static void addPaletteFile(String paletteName, BufferedReader reader)
            throws IOException {
        StringBuilder paletteString = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("%")) {
                paletteString.append(line);
                paletteString.append(",");
            }
        }
        paletteString.deleteCharAt(paletteString.length() - 1);
        Color[] colourSet = colourSetFromString(paletteString.toString());
        if (colourSet != null) {
            loadedColourSets.put(paletteName, colourSet);
            Color[] invColourSet = (Color[]) ArrayUtils.clone(colourSet);
            ArrayUtils.reverse(invColourSet);
            loadedColourSets.put(paletteName + INVERSE_SUFFIX, invColourSet);
        }
    }

    private final Color[] colours;

    public ColourPalette(Color[] palette, int numColorBands) {
        if (numColorBands < 1 || numColorBands > MAX_NUM_COLOURS) {
            throw new IllegalArgumentException(
                    "numColorBands must be greater than 1 and less than " + MAX_NUM_COLOURS);
        }
        colours = GraphicsUtils.generateColourSet(palette, numColorBands);
    }

    /**
     * Gets the colour corresponding to a fractional point along the palette
     * 
     * @param value
     *            The fraction along the palette of the colour
     * @return The desired colour
     */
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

    /**
     * Gets a {@link ColourPalette} from a string representation of it
     * 
     * @param paletteString
     *            Either the name of a predefined palette, or a string defining
     *            a palette. This is a comma, colon, or newline separated list
     *            of colours (see {@link GraphicsUtils#parseColour(String)} for
     *            valid colour formats)
     * @param nColourBands
     *            The number of colour bands to use in the palette.
     * @return The desired {@link ColourPalette}
     */
    public static ColourPalette fromString(String paletteString, int nColourBands) {
        if (paletteString == null || "".equals(paletteString)) {
            paletteString = DEFAULT_PALETTE_NAME;
        }
        if (loadedColourSets.containsKey(paletteString)) {
            return new ColourPalette(loadedColourSets.get(paletteString), nColourBands);
        } else {
            Color[] colours = colourSetFromString(paletteString);
            if (colours == null) {
                throw new EdalException(
                        paletteString + " is not an existing palette name or a palette definition");
            }
            return new ColourPalette(colours, nColourBands);
        }
    }

    public static Set<String> getPredefinedPalettes() {
        return loadedColourSets.keySet();
    }

    private static Color[] colourSetFromString(String paletteString) {
        String[] colourStrings = paletteString.split("[,\n:]");
        Color[] colours = new Color[colourStrings.length];
        for (int i = 0; i < colourStrings.length; i++) {
            colours[i] = cParser.unmarshal(colourStrings[i]);
            if (colours[i] == null) {
                /*
                 * Problem parsing the colour
                 */
                return null;
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
    private static String[] getResourceListing(Class clazz, String path)
            throws URISyntaxException, IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        /*
         * GG Modification:
         * 
         * We want to return both file path entries *AND* those in a JAR
         */
        String[] fileList = new String[0];
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            /* A file path: easy enough */
            fileList = new File(dirURL.toURI()).list();
        }

        /*
         * In case of a jar file, we can't actually find a directory. Have to
         * assume the same jar as clazz.
         */
        String me = clazz.getName().replace(".", "/") + ".class";
        dirURL = clazz.getClassLoader().getResource(me);

        String[] jarList = new String[0];
        if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            /* strip out only the JAR file */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
            try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
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
                             * if it is a subdirectory, we just return the
                             * directory name
                             */
                            entry = entry.substring(0, checkSubdir);
                        }
                        result.add(entry);
                    }
                }
                jarList = result.toArray(new String[result.size()]);
            }
        }

        String[] retList = new String[fileList.length + jarList.length];
        int rlCount = 0;
        for (String fileEntry : fileList) {
            retList[rlCount++] = fileEntry;
        }
        for (String jarEntry : jarList) {
            retList[rlCount++] = jarEntry;
        }
        return retList;
    }
}
