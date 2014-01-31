/*******************************************************************************
 * Copyright (c) 2014 The University of Reading All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of the
 * University of Reading, nor the names of the authors or contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc.edal.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

/**
 * A class with static methods to read data from the GSHHS binary format to
 * create masks of land data.
 * 
 * Also contains a method to apply such a mask to an image file.
 * 
 * All data assumed to be in EPSG:4326.
 * 
 * Its original purpose was to mask out oceans from satellite images for the
 * MyOcean project.
 * 
 * This doesn't fit nicely into EDAL yet, but it has some useful methods so it
 * is going in the codebase. We may want to modify this for other purposes in
 * the future.
 * 
 * @author Guy
 */
public class CoastlineReader {

    private static final int WATER = Color.white.getRGB();
    private static final int LAND = Color.black.getRGB();

    public static void main(String[] args) throws IOException {
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 2; y++) {
                char xChar = 'X';
                double xMin = Double.NaN;
                char yChar = 'X';
                double yMin = Double.NaN;
                switch (x) {
                case 0:
                    xChar = 'A';
                    xMin = -180.0;
                    break;
                case 1:
                    xChar = 'B';
                    xMin = -90.0;
                    break;
                case 2:
                    xChar = 'C';
                    xMin = -0.0;
                    break;
                case 3:
                    xChar = 'D';
                    xMin = 90.0;
                    break;
                }
                switch (y) {
                case 0:
                    yChar = '1';
                    yMin = 0.0;
                    break;
                case 1:
                    yChar = '2';
                    yMin = -90.0;
                    break;
                }
                String filename = "/home/guy/Data/blue_marble_hires/bm_hires." + xChar + yChar
                        + ".png";
                BufferedImage baseImage = ImageIO.read(new File(filename));
                BufferedImage mask = CoastlineReader.generateMask(new File(
                        "/home/guy/Data/gshhs_bin/gshhs_f.b"), xMin, xMin + 90, yMin, yMin + 90,
                        baseImage.getWidth(), baseImage.getHeight());
                BufferedImage output = CoastlineReader.applyMask(baseImage, mask);
                ImageIO.write(output, "png", new File("/home/guy/Data/blue_marble_hires/bm_masked_"
                        + xChar + yChar + ".png"));
                System.out.println("Image written");
            }
        }
    }

    public static BufferedImage applyMask(BufferedImage original, BufferedImage mask) {
        if (original.getWidth() != mask.getWidth() || original.getHeight() != mask.getHeight()) {
            throw new IllegalArgumentException("Images must be the same size");
        }
        BufferedImage ret = new BufferedImage(original.getWidth(), original.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        ret.createGraphics().drawImage(original, 0, 0, original.getWidth(), original.getHeight(),
                null);

        for (int i = 0; i < ret.getWidth(); i++) {
            if ((i % 100) == 0) {
                System.out.println((100.0 * i / ret.getWidth()) + "% done applying mask");
            }
            for (int j = 0; j < ret.getHeight(); j++) {
                if (mask.getRGB(i, j) == WATER) {
                    ret.setRGB(i, j, 0);
                }
            }
        }
        return ret;
    }

    @SuppressWarnings("unused")
    public static BufferedImage generateMask(File gshhsBinFile, double minLon, double maxLon,
            double minLat, double maxLat, int width, int height) throws IOException {
        FileInputStream inputStream = new FileInputStream(gshhsBinFile);

        BufferedImage world = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = world.createGraphics();
        g.setColor(new Color(WATER));
        g.fillRect(0, 0, width, height);
        while (true) {
            byte[] headerdata = new byte[44];
            if (inputStream.read(headerdata) == -1) {
                break;
            }

            ByteBuffer bb = ByteBuffer.wrap(headerdata);

            int id = bb.getInt();
            int nPoints = bb.getInt();
            int flag = bb.getInt();
            int west = bb.getInt();
            int east = bb.getInt();
            int south = bb.getInt();
            int north = bb.getInt();
            int area = bb.getInt();
            int areaFull = bb.getInt();
            int container = bb.getInt();
            int ancestor = bb.getInt();

            if (container >= 0) {
                /*
                 * This means that we have an outline within another one - e.g.
                 * (i.e.?) a lake
                 */
                g.setColor(new Color(WATER));
            } else {
                g.setColor(new Color(LAND));
            }

            /*
             * Now read the data.
             */
            byte[] points = new byte[nPoints * 2 * 4];
            inputStream.read(points);

            ByteBuffer pointsBuffer = ByteBuffer.wrap(points);
            double[] xPoints = new double[nPoints];
            double[] yPoints = new double[nPoints];

            int[] xImagePoints = new int[nPoints];
            int[] yImagePoints = new int[nPoints];

            /*
             * This code is a bit hacky, but it deals with the dateline, so what
             * do you expect.
             * 
             * Anyway, it works
             */

            double lastX = pointsBuffer.getInt() / 1000000.0;
            while (lastX > 180.0) {
                lastX -= 360.0;
            }
            while (lastX < -180.0) {
                lastX += 360.0;
            }
            xPoints[0] = lastX;
            yPoints[0] = pointsBuffer.getInt() / 1000000.0;

            xImagePoints[0] = getImageX(xPoints[0], minLon, maxLon, width);
            yImagePoints[0] = getImageY(yPoints[0], minLat, maxLat, height);

            boolean startsEastGoesWest = false;
            boolean startsWestGoesEast = false;
            for (int i = 1; i < nPoints; i++) {
                double x = pointsBuffer.getInt() / 1000000.0;
                double y = pointsBuffer.getInt() / 1000000.0;

                /*
                 * Make sure the point is in the range -180-180
                 */
                while (x > 180.0) {
                    x -= 360.0;
                }
                while (x < -180.0) {
                    x += 360.0;
                }

                /*
                 * Check if it crosses the date line. If so, adjust it
                 * accordingly. The point will be off the image, so we set a
                 * flag so that we can draw all of these polygons again
                 */
                if ((lastX - x) > 180.0) {
                    x += 360.0;
                    startsEastGoesWest = true;
                } else if ((x - lastX) > 180.0) {
                    x -= 360.0;
                    startsWestGoesEast = true;
                }

                /*
                 * We're only storing these for the cases where they cross the
                 * dateline
                 */
                xPoints[i] = x;
                yPoints[i] = y;

                xImagePoints[i] = getImageX(x, minLon, maxLon, width);
                yImagePoints[i] = getImageY(y, minLat, maxLat, height);

                lastX = x;
            }
            /*
             * ID 4 is the antarctic. If we fill the outline of its coastline in
             * EPSG:4326 we don't fill the lowest latitudes
             */
            if (id == 4) {
                int[] xP2 = new int[nPoints + 2];
                int[] yP2 = new int[nPoints + 2];
                xP2[0] = getImageX(180, minLon, maxLon, width);
                yP2[0] = getImageY(-90, minLat, maxLat, height);
                xP2[nPoints + 1] = getImageX(-180, minLon, maxLon, width);
                yP2[nPoints + 1] = getImageY(-90, minLat, maxLat, height);
                for (int j = 0; j < nPoints; j++) {
                    xP2[j + 1] = xImagePoints[j];
                    yP2[j + 1] = yImagePoints[j];
                }
                xImagePoints = xP2;
                yImagePoints = yP2;
                nPoints += 2;
            }

            /*
             * Draw the coastline
             */
            g.fillPolygon(xImagePoints, yImagePoints, nPoints);

            /*
             * Any that crossed, draw again but 360 degrees in the appropriate
             * direction
             */
            if (startsEastGoesWest) {
                for (int i = 0; i < xPoints.length; i++) {
                    xImagePoints[i] = getImageX(xPoints[i] - 360.0, minLon, maxLon, width);
                }
                g.fillPolygon(xImagePoints, yImagePoints, nPoints);
            }
            if (startsWestGoesEast) {
                for (int i = 0; i < xPoints.length; i++) {
                    xImagePoints[i] = getImageX(xPoints[i] + 360.0, minLon, maxLon, width);
                }
                g.fillPolygon(xImagePoints, yImagePoints, nPoints);
            }
        }

        inputStream.close();

        return world;
    }

    private static int getImageX(double lon, double minLon, double maxLon, int width) {
        return (int) ((lon - minLon) * width / (maxLon - minLon));
    }

    private static int getImageY(double lat, double minLat, double maxLat, int height) {
        return height - 1 - (int) ((lat - minLat) * height / (maxLat - minLat));
    }
}
