/**
 * Copyright (c) 2011 Applied Science Associates
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
 */

package uk.ac.rdg.resc.edal.graphics.utils;

import java.awt.Graphics2D;
import java.util.List;
import java.util.ArrayList;
import java.awt.geom.Path2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.Arrays;

/**
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class BarbFactory {
    private static List<Path2D> windBarbs;

    static {
        windBarbs = new ArrayList<Path2D>();
        windBarbs.add(barb_0_4());
        windBarbs.add(barb_5_9());
        windBarbs.add(barb_10_14());
        windBarbs.add(barb_15_19());
        windBarbs.add(barb_20_24());
        windBarbs.add(barb_25_29());
        windBarbs.add(barb_30_34());
        windBarbs.add(barb_35_39());
        windBarbs.add(barb_40_44());
        windBarbs.add(barb_45_49());
        windBarbs.add(barb_50_54());
        windBarbs.add(barb_55_59());
        windBarbs.add(barb_60_64());
        windBarbs.add(barb_65_69());
        windBarbs.add(barb_70_74());
        windBarbs.add(barb_75_79());
        windBarbs.add(barb_80_84());
        windBarbs.add(barb_85_89());
        windBarbs.add(barb_90_94());
        windBarbs.add(barb_95_99());
        windBarbs.add(barb_100());
    }

    public BarbFactory() {
    }

    public static void drawWindBarbForSpeed(double speed, double angle, int i, int j, String units,
            float scale, Graphics2D g) {

        /* Convert to knots */
        if (units.equalsIgnoreCase("m/s")) {
            speed = speed * 1.94384449;
        } else if (units.equalsIgnoreCase("cm/s")) {
            speed = speed * 0.0194384449;
        } else if (units.trim().equalsIgnoreCase("mm/s")) {
            speed = speed * 0.00194384449;
        }
        int knots = (Double.valueOf(speed)).intValue();
        int pennants = knots / 50;
        int fullLines = (knots - (pennants * 50)) / 10;
        int halfLines = (knots - (pennants * 50) - (fullLines * 10)) / 5;

        int barbLength = 18;
        int fullLineLength = 10;
        int halfLineLength = fullLineLength / 2;
        int basePennantLength = 4;
        int distanceBetweenItems = 3;
        int currentPosOnBaseBarb = 0;
        int hasPennants = 0;
        // Base barb
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(barbLength, 0);
        // Drawing pennants...
        if (pennants > 0) {
            int contPennants = 0;
            hasPennants = 1;
            while (contPennants < pennants) {
                currentPosOnBaseBarb = barbLength - (contPennants * basePennantLength);
                path.moveTo(currentPosOnBaseBarb, 0);
                path.lineTo(currentPosOnBaseBarb - (basePennantLength / 2), fullLineLength);
                path.lineTo(currentPosOnBaseBarb - basePennantLength, 0);
                path.closePath();
                currentPosOnBaseBarb = 0;
                contPennants++;
            }
        }
        // Drawing full lines...
        if (fullLines > 0) {
            int contFullLines = 0;
            currentPosOnBaseBarb = barbLength - (pennants * basePennantLength)
                    - distanceBetweenItems * hasPennants;
            while (contFullLines < fullLines) {
                path.moveTo(currentPosOnBaseBarb, 0);
                path.lineTo(currentPosOnBaseBarb + basePennantLength / 2, fullLineLength);
                contFullLines++;
                currentPosOnBaseBarb -= distanceBetweenItems;
            }
        }
        // half line...
        if (halfLines > 0) {
            if (pennants == 0 && fullLines == 0) {
                currentPosOnBaseBarb = barbLength - 5;
                path.moveTo(currentPosOnBaseBarb, 0);
                path.lineTo(currentPosOnBaseBarb + basePennantLength / 4, halfLineLength);
            } else {
                currentPosOnBaseBarb = barbLength - (pennants * basePennantLength)
                        - distanceBetweenItems * (fullLines + hasPennants);
                path.moveTo(currentPosOnBaseBarb, 0);
                path.lineTo(currentPosOnBaseBarb + basePennantLength / 4, halfLineLength);
            }
        }

        path.transform(AffineTransform.getRotateInstance(-Math.PI / 2));
        path.transform(AffineTransform.getRotateInstance(angle));
        path.transform(AffineTransform.getScaleInstance(scale, scale));
        path.transform(AffineTransform.getTranslateInstance(i, j));
        g.draw(path);

        // Iterating the transformed path to get the right coordinates for the
        // polygons
        PathIterator pi = path.getPathIterator(null);
        double[] coordinates = new double[6];
        // double arrays for polygon points coordinates (PathIterator works with
        // double)
        double[] xcoords = new double[4];
        double[] ycoords = new double[4];
        // int arrays for polygon points coordinates -> fill method works with
        // int
        int[] iXcoords = new int[4];
        int[] iYcoords = new int[4];
        int prevType = pi.currentSegment(coordinates);
        g.fillOval(Double.valueOf(coordinates[0]).intValue() - 2, Double.valueOf(coordinates[1])
                .intValue() - 2, 4, 4);
        // Filling the pennants
        if (hasPennants == 1) {

            double[] prevCoordinates = Arrays.copyOf(coordinates, coordinates.length);
            int type = prevType;
            int cont = 0;
            boolean isPolygon = false;
            while (pi.isDone() == false) {

                if (isPolygon) {
                    xcoords[cont] = coordinates[0];
                    ycoords[cont] = coordinates[1];
                    cont++;
                }
                prevType = type;
                prevCoordinates = Arrays.copyOf(coordinates, coordinates.length);
                pi.next();
                type = pi.currentSegment(coordinates);
                if (prevType == PathIterator.SEG_MOVETO && type == PathIterator.SEG_LINETO) {
                    isPolygon = true;
                    cont = 0;
                    xcoords[cont] = prevCoordinates[0];
                    ycoords[cont] = prevCoordinates[1];
                    cont++;
                }
                if (type == PathIterator.SEG_CLOSE) {
                    isPolygon = false;
                    cont = 0;
                    // copy doubles into ints
                    for (int k = 0; k < 3; k++) {
                        iXcoords[k] = Double.valueOf(xcoords[k]).intValue();
                        iYcoords[k] = Double.valueOf(ycoords[k]).intValue();
                    }
                    iXcoords[iXcoords.length - 1] = iXcoords[0];
                    iYcoords[iYcoords.length - 1] = iYcoords[0];
                    g.fillPolygon(iXcoords, iYcoords, 4);
                }
            }
        }
    }

    public static void renderWindBarbForSpeed(double speed, double angle, int i, int j,
            String units, float scale, boolean southern_hemisphere, Graphics2D g) {
        /* Convert to knots */
        if (units.trim().equalsIgnoreCase("m/s")) {
            speed = speed * 1.94384449;
        } else if (units.trim().equalsIgnoreCase("cm/s")) {
            speed = speed * 0.0194384449;
        } else if (units.trim().equalsIgnoreCase("mm/s")) {
            speed = speed * 0.00194384449;
        }

        /* Get index into windBarbs array */
        int rank = (int) (speed / 5) + 1;
        if (rank < 0) {
            rank = 0;
        } else if (rank >= windBarbs.size()) {
            rank = windBarbs.size() - 1;
        }

        Path2D ret = (Path2D) windBarbs.get(rank).clone();
        /*
         * Rotate so the Barb represents 0 from degrees.
         * 
         * Barbs are initially drawn like this:
         * 
         * *-------- || ||
         * 
         * Wind is the "From" direction, so we need to rotate the barb by 1 *
         * (Math.PI / 2).
         * 
         * 
         * * | | | ----| ----|
         */
        /*
         * Southern Hemisphere barbs need to be flipped so they point in the
         * anti-clockwise direction
         */
        if (!southern_hemisphere) {
            ret.transform(AffineTransform.getScaleInstance(1.0, -1.0));
        }
        ret.transform(AffineTransform.getRotateInstance(1 * (Math.PI / 2)));
        // Now rotate by the correct angle, clockwise
        ret.transform(AffineTransform.getRotateInstance(angle));
        // Scale the image
        /*
         * The main line length is 18. We want this line length to be equal to
         * the supplied scale.
         */
        scale /= 18;
        ret.transform(AffineTransform.getScaleInstance(scale, scale));
        // Place the image
        ret.transform(AffineTransform.getTranslateInstance(i, j));
        g.draw(ret);
    }

    private static Path2D barb_0_4() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(18, 0);
        return path;
    }

    private static Path2D barb_5_9() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(18, 0);
        path.moveTo(15, 0);
        path.lineTo(17, -8);
        return path;
    }

    private static Path2D barb_10_14() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(18, 0);
        path.lineTo(22, -16);
        return path;
    }

    private static Path2D barb_15_19() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(18, 0);
        path.lineTo(22, -16);
        path.moveTo(15, 0);
        path.lineTo(17, -8);
        return path;
    }

    private static Path2D barb_20_24() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(18, 0);
        path.lineTo(22, -16);
        path.moveTo(15, 0);
        path.lineTo(19, -16);
        return path;
    }

    /* CONTINUE */
    private static Path2D barb_25_29() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(18, 0);
        path.lineTo(22, -16);
        path.moveTo(15, 0);
        path.lineTo(19, -16);
        path.moveTo(12, 0);
        path.lineTo(14, -8);
        return path;
    }

    private static Path2D barb_30_34() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(18, 0);
        path.lineTo(22, -16);
        path.moveTo(15, 0);
        path.lineTo(19, -16);
        path.moveTo(12, 0);
        path.lineTo(16, -16);
        return path;
    }

    private static Path2D barb_35_39() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(18, 0);
        path.lineTo(22, -16);
        path.moveTo(15, 0);
        path.lineTo(19, -16);
        path.moveTo(12, 0);
        path.lineTo(16, -16);
        path.moveTo(9, 0);
        path.lineTo(11, -8);
        return path;
    }

    private static Path2D barb_40_44() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(18, 0);
        path.lineTo(22, -16);
        path.moveTo(15, 0);
        path.lineTo(19, -16);
        path.moveTo(12, 0);
        path.lineTo(16, -16);
        path.moveTo(9, 0);
        path.lineTo(13, -16);
        return path;
    }

    private static Path2D barb_45_49() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(18, 0);
        path.lineTo(22, -16);
        path.moveTo(15, 0);
        path.lineTo(19, -16);
        path.moveTo(12, 0);
        path.lineTo(16, -16);
        path.moveTo(9, 0);
        path.lineTo(13, -16);
        path.moveTo(6, 0);
        path.lineTo(8, -8);
        return path;
    }

    private static Path2D barb_50_54() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(22, 0);
        path.lineTo(22, -16);
        path.lineTo(18, 0);
        return path;
    }

    private static Path2D barb_55_59() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(22, 0);
        path.lineTo(22, -16);
        path.lineTo(18, 0);
        path.moveTo(15, 0);
        path.lineTo(17, -8);
        return path;
    }

    private static Path2D barb_60_64() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(22, 0);
        path.lineTo(22, -16);
        path.lineTo(18, 0);
        path.moveTo(15, 0);
        path.lineTo(19, -16);
        return path;
    }

    private static Path2D barb_65_69() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(22, 0);
        path.lineTo(22, -16);
        path.lineTo(18, 0);
        path.moveTo(15, 0);
        path.lineTo(19, -16);
        path.moveTo(12, 0);
        path.lineTo(14, -8);
        return path;
    }

    private static Path2D barb_70_74() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(22, 0);
        path.lineTo(22, -16);
        path.lineTo(18, 0);
        path.moveTo(15, 0);
        path.lineTo(19, -16);
        path.moveTo(12, 0);
        path.lineTo(16, -16);
        return path;
    }

    private static Path2D barb_75_79() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(22, 0);
        path.lineTo(22, -16);
        path.lineTo(18, 0);
        path.moveTo(15, 0);
        path.lineTo(19, -16);
        path.moveTo(12, 0);
        path.lineTo(16, -16);
        path.moveTo(9, 0);
        path.lineTo(11, -8);
        return path;
    }

    private static Path2D barb_80_84() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(22, 0);
        path.lineTo(22, -16);
        path.lineTo(18, 0);
        path.moveTo(15, 0);
        path.lineTo(19, -16);
        path.moveTo(12, 0);
        path.lineTo(16, -16);
        path.moveTo(9, 0);
        path.lineTo(13, -16);
        return path;
    }

    private static Path2D barb_85_89() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(22, 0);
        path.lineTo(22, -16);
        path.lineTo(18, 0);
        path.moveTo(15, 0);
        path.lineTo(19, -16);
        path.moveTo(12, 0);
        path.lineTo(16, -16);
        path.moveTo(9, 0);
        path.lineTo(13, -16);
        path.moveTo(6, 0);
        path.lineTo(8, -8);
        return path;
    }

    private static Path2D barb_90_94() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(22, 0);
        path.lineTo(22, -16);
        path.lineTo(18, 0);
        path.moveTo(15, 0);
        path.lineTo(19, -16);
        path.moveTo(12, 0);
        path.lineTo(16, -16);
        path.moveTo(9, 0);
        path.lineTo(13, -16);
        path.moveTo(6, 0);
        path.lineTo(10, -16);
        return path;
    }

    private static Path2D barb_95_99() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(22, 0);
        path.lineTo(22, -16);
        path.lineTo(18, 0);
        path.moveTo(15, 0);
        path.lineTo(19, -16);
        path.moveTo(12, 0);
        path.lineTo(16, -16);
        path.moveTo(9, 0);
        path.lineTo(13, -16);
        path.moveTo(6, 0);
        path.lineTo(10, -16);
        path.moveTo(3, 0);
        path.lineTo(5, -8);
        return path;
    }

    private static Path2D barb_100() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.quadTo(-2, 2, -4, 0);
        path.quadTo(-2, -2, 0, 0);
        path.lineTo(22, 0);
        path.lineTo(22, -16);
        path.lineTo(14, -16);
        path.lineTo(14, 0);
        return path;
    }
}
