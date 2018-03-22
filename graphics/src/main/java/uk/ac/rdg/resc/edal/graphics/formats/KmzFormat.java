/*******************************************************************************
 * Copyright (c) 2011 The University of Reading
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
 *******************************************************************************/

package uk.ac.rdg.resc.edal.graphics.formats;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.joda.time.DateTime;
import org.opengis.metadata.extent.GeographicBoundingBox;

import uk.ac.rdg.resc.edal.util.TimeUtils;

/**
 * Creates KMZ files for importing into Google Earth. Only one instance of this
 * class will ever be created, so this class contains no member variables to
 * ensure thread safety.
 * 
 * TODO Would this be better handled by a velocity template?
 * 
 * @author Jon Blower
 */
public class KmzFormat extends ImageFormat {
    private static final String PICNAME = "frame";
    private static final String PICEXT = "png";
    private static final String COLOUR_SCALE_FILENAME = "legend.png";

    @Override
    public void writeImage(List<BufferedImage> frames, OutputStream out, String name,
            String description, GeographicBoundingBox bbox, List<DateTime> tValues, String zValue,
            BufferedImage legend, Integer frameRate) throws IOException {
        StringBuffer kml = new StringBuffer();
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            if (frameIndex == 0) {
                /*
                 * This is the first frame. Add the KML header and folder
                 * metadata
                 */
                kml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                kml.append(System.getProperty("line.separator"));
                kml.append("<kml xmlns=\"http://earth.google.com/kml/2.0\">");
                kml.append("<Folder>");
                kml.append("<visibility>1</visibility>");
                kml.append("<name>" + name + "</name>");
                kml.append("<description>" + description + "</description>");
                double meanLon = (bbox.getEastBoundLongitude() + bbox.getWestBoundLongitude())
                        / 2.0;
                double meanLat = (bbox.getNorthBoundLatitude() + bbox.getSouthBoundLatitude())
                        / 2.0;
                kml.append("<LookAt><latitude>" + meanLat + "</latitude><longitude>" + meanLon
                        + "</longitude><range>10000000</range></LookAt>");
                /* Add the screen overlay containing the colour scale */
                kml.append("<ScreenOverlay>");
                kml.append("<name>Colour scale</name>");
                kml.append("<Icon><href>" + COLOUR_SCALE_FILENAME + "</href></Icon>");
                kml.append("<overlayXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/>");
                kml.append("<screenXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/>");
                kml.append("<rotationXY x=\"0\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>");
                kml.append("<size x=\"0\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>");
                kml.append("</ScreenOverlay>");
            }

            kml.append("<GroundOverlay>");
            String timestamp = null;
            String z = null;

            if (tValues != null && tValues.get(frameIndex) != null) {
                timestamp = TimeUtils.dateTimeToISO8601(tValues.get(frameIndex));
                kml.append("<TimeStamp><when>" + timestamp + "</when></TimeStamp>");
            }

            if (zValue != null && !zValue.equals("")) {
                z = "";
                if (timestamp != null)
                    z += "<br />";
                z += "Elevation: " + zValue;
            }
            kml.append("<name>");
            if (timestamp == null && z == null) {
                kml.append("Frame " + frameIndex);
            } else {
                kml.append("<![CDATA[");
                if (timestamp != null)
                    kml.append("Time: " + timestamp);
                if (z != null)
                    kml.append(z);
                kml.append("]]>");
            }
            kml.append("</name>");
            kml.append("<visibility>1</visibility>");

            kml.append("<Icon><href>" + getPicFileName(frameIndex) + "</href></Icon>");

            kml.append("<LatLonBox id=\"" + frameIndex + "\">");
            kml.append("<west>" + bbox.getWestBoundLongitude() + "</west>");
            kml.append("<south>" + bbox.getSouthBoundLatitude() + "</south>");
            kml.append("<east>" + bbox.getEastBoundLongitude() + "</east>");
            kml.append("<north>" + bbox.getNorthBoundLatitude() + "</north>");
            kml.append("<rotation>0</rotation>");
            kml.append("</LatLonBox>");
            kml.append("</GroundOverlay>");
        }

        /* Write the footer of the KML file */
        kml.append("</Folder>");
        kml.append("</kml>");

        try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
            /* Write the KML file: todo get filename properly */
            ZipEntry kmlEntry = new ZipEntry(name + ".kml");
            kmlEntry.setTime(System.currentTimeMillis());
            zipOut.putNextEntry(kmlEntry);
            zipOut.write(kml.toString().getBytes());

            /* Now write all the images */
            int frameIndex = 0;
            for (BufferedImage frame : frames) {
                ZipEntry picEntry = new ZipEntry(getPicFileName(frameIndex));
                frameIndex++;
                zipOut.putNextEntry(picEntry);
                ImageIO.write(frame, PICEXT, zipOut);
            }

            /* Finally, write the colour scale */
            ZipEntry scaleEntry = new ZipEntry(COLOUR_SCALE_FILENAME);
            zipOut.putNextEntry(scaleEntry);
            /* Write the colour scale bar to the KMZ file */
            ImageIO.write(legend, PICEXT, zipOut);
        }
    }

    /**
     * @return the name of the picture file with the given index
     */
    private static final String getPicFileName(int frameIndex) {
        return PICNAME + frameIndex + "." + PICEXT;
    }

    @Override
    public String getMimeType() {
        return "application/vnd.google-earth.kmz";
    }

    @Override
    public boolean supportsMultipleFrames() {
        return true;
    }

    @Override
    public boolean supportsFullyTransparentPixels() {
        return true;
    }

    @Override
    public boolean supportsPartiallyTransparentPixels() {
        return true;
    }

    @Override
    public boolean requiresLegend() {
        return true;
    }
}
