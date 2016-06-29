/*
 * Copyright (c) 2011 Applied Science Associates
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 * authors or contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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

/**
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class VectorFactory {

    private static List<Path2D> vectors;

    static {
        vectors = new ArrayList<Path2D>();
        vectors.add(stumpyVector());
        vectors.add(triangleVector());
        vectors.add(lineVector());
        vectors.add(fancyVector());
    }

    public VectorFactory() {
    }

    public static void renderVector(String style, double angle, int i, int j,
            float scale, Graphics2D g) {

        int type = 0;
        if (style.equalsIgnoreCase("STUMPVEC")) {
            type = 0;
        } else if (style.equalsIgnoreCase("TRIVEC")) {
            type = 1;
        } else if (style.equalsIgnoreCase("LINEVEC")) {
            type = 2;
        } else if (style.equalsIgnoreCase("FANCYVEC")) {
            type = 3;
        }

        Path2D ret = (Path2D) vectors.get(type).clone();
        /* Rotate and set position */
        ret.transform(AffineTransform.getRotateInstance(-Math.PI / 2));
        ret.transform(AffineTransform.getRotateInstance(angle));
        ret.transform(AffineTransform.getScaleInstance(scale, scale));
        ret.transform(AffineTransform.getTranslateInstance(i, j));

        // Don't fill the FANCYVEC
        if (type != 3) {
            g.fill(ret);
        }
        g.draw(ret);
    }

    private static Path2D stumpyVector() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0.5);
        path.lineTo(0, -0.5);
        path.lineTo(7, -0.5);
        path.lineTo(4, -4);
        path.lineTo(10, 0);
        path.lineTo(4, 4);
        path.lineTo(7, 0.5);
        path.closePath();
        return path;
    }

    private static Path2D triangleVector() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 4);
        path.lineTo(0, -4);
        path.lineTo(10, 0);
        path.closePath();
        return path;
    }

    private static Path2D lineVector() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.moveTo(10, 0);
        path.lineTo(6, 3);
        path.moveTo(10, 0);
        path.lineTo(6, -3);
        path.closePath();
        return path;
    }

    private static Path2D fancyVector() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(0, -3);
        path.lineTo(5, -2);
        path.lineTo(3, -5);
        path.lineTo(11, -1.5);
        path.lineTo(3, 2);
        path.lineTo(5, -1);
        path.closePath();
        return path;
    }
}