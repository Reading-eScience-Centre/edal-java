/*******************************************************************************
 * Copyright (c) 2015 The University of Reading
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

package uk.ac.rdg.resc.edal.geometry;

import java.awt.geom.Path2D;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.exceptions.MismatchedCrsException;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Implementation of a {@link Polygon} based on an ordered list of vertices
 * 
 * @author Guy Griffiths
 */
public class SimplePolygon extends AbstractPolygon {

    private List<HorizontalPosition> vertices;
    private CoordinateReferenceSystem crs = null;
    private List<BoundingBoxImpl> inBounds = new ArrayList<>();

    /**
     * Construct a new {@link SimplePolygon}
     * 
     * @param vertices
     *            An ordered list of vertices. Must all be in the same
     *            {@link CoordinateReferenceSystem}
     */
    public SimplePolygon(List<HorizontalPosition> vertices) {
        for (HorizontalPosition vertex : vertices) {
            if (crs == null) {
                crs = vertex.getCoordinateReferenceSystem();
            } else {
                if (!GISUtils.crsMatch(vertex.getCoordinateReferenceSystem(), crs)) {
                    throw new MismatchedCrsException(
                            "All positions in a polygon must have the same CRS");
                }
            }
        }

        if (GISUtils.isWgs84LonLat(crs)) {
            /*
             * If we're working with lat-lon, deal with polygons crossing the
             * date line
             */
            this.vertices = new ArrayList<>();
            HorizontalPosition last = null;
            for (HorizontalPosition vertex : vertices) {
                if (last != null) {
                    vertex = new HorizontalPosition(GISUtils.getNearestEquivalentLongitude(
                            last.getX(), vertex.getX()), vertex.getY());
                    last = vertex;
                }
                this.vertices.add(vertex);
            }
        } else {
            this.vertices = vertices;
        }

        /*
         * This is an optimisation for the contains() method.
         * 
         * For large complex polygons, the contains method can take quite a long
         * time. Hence we only perform this optimisation for fairly large
         * polygons. We pick 20 vertices arbitrarily - in fact this is rather on
         * the low side, but the optimisation doesn't take too long, and it will
         * not slow down the contains method once the inBounds have been
         * generated
         * 
         * Here, we create a 4x4 grid of points and start growing a rectangle
         * from each one. Once an edge hits the boundary of the polygon we stop
         * growing in that direction.
         * 
         * We then end up with up to 16 rectangles which give a (greatly
         * simplified) approximation of the boundary of this polygon. Our
         * contains() method can then check these rectangles first - if a point
         * is within them then it is definitely within this polygon, otherwise a
         * full check needs to be done.
         */
        if (vertices.size() > 20) {
            int n = 4;
            double dx = getBoundingBox().getWidth() / n;
            double dy = getBoundingBox().getHeight() / n;
            Path2D boundary = getBoundaryPath();
            for (double startX = 0.5 * dx + getBoundingBox().getMinX(); startX < getBoundingBox()
                    .getMaxX(); startX += dx) {
                for (double startY = 0.5 * dy + getBoundingBox().getMinY(); startY < getBoundingBox()
                        .getMaxY(); startY += dy) {
                    if (super.contains(startX, startY)) {
                        /*
                         * Continue incrementing / decrementing X / Y
                         */
                        boolean incX = true;
                        boolean incY = true;
                        boolean decX = true;
                        boolean decY = true;
                        /*
                         * Upper left coords & width
                         */
                        double ulX = startX;
                        double ulY = startY;
                        double width = Double.MIN_VALUE;
                        double height = Double.MIN_VALUE;

                        /*
                         * We'll grow each rectangle by 1% of the total bbox
                         * width / height at each step.
                         */
                        double deltaX = getBoundingBox().getWidth() / 100.0;
                        double deltaY = getBoundingBox().getHeight() / 100.0;
                        while (incX || incY || decX || decY) {
                            /*
                             * Test decreasing the minimum X
                             */
                            if (decX
                                    && boundary.contains(ulX - deltaX, ulY, width + deltaX, height)) {
                                ulX -= deltaX;
                                width += deltaX;
                            } else {
                                decX = false;
                            }
                            /*
                             * Test decreasing the minimum Y
                             */
                            if (decY
                                    && boundary.contains(ulX, ulY - deltaY, width, height + deltaY)) {
                                ulY -= deltaY;
                                height += deltaY;
                            } else {
                                decY = false;
                            }
                            /*
                             * Test increasing the maximum X
                             */
                            if (incX && boundary.contains(ulX, ulY, width + deltaX, height)) {
                                width += deltaX;
                            } else {
                                incX = false;
                            }
                            /*
                             * Test increasing the maximum Y
                             */
                            if (incY && boundary.contains(ulX, ulY, width, height + deltaY)) {
                                height += deltaY;
                            } else {
                                incY = false;
                            }
                        }
                        /*
                         * Add the new rectangle to the list of bounds
                         */
                        inBounds.add(new BoundingBoxImpl(ulX, ulY, ulX + width, ulY + height, crs));
                    }
                }
            }
            /*
             * Sort with largest area rectangles first, since these are most
             * likely to contain a point
             */
            Collections.sort(inBounds, new Comparator<BoundingBoxImpl>() {
                @Override
                public int compare(BoundingBoxImpl bb1, BoundingBoxImpl bb2) {
                    double area1 = bb1.getWidth() * bb1.getHeight();
                    double area2 = bb2.getWidth() * bb2.getHeight();
                    if (area1 > area2) {
                        /*
                         * This says that bb1 is "less than" bb2. That's fine
                         * because we want to sort in DESCENDING order.
                         * 
                         * The "proper" thing to do (i.e. such that the
                         * Comparator behaves less confusingly) would be to sort
                         * in ascending order and reverse the list.
                         * 
                         * But we don't need to do that because this Comparator
                         * is only used once and has this massive comment
                         * explaining the situation.
                         */
                        return -1;
                    } else if (area1 < area2) {
                        return 1;
                    }
                    return 0;
                }
            });
        } else if (vertices.size() == 4) {
            if ((vertices.get(0).getX() == vertices.get(1).getX()
                    && vertices.get(1).getY() == vertices.get(2).getY()
                    && vertices.get(2).getX() == vertices.get(3).getX() && vertices.get(3).getY() == vertices
                    .get(0).getY())
                    || (vertices.get(0).getY() == vertices.get(1).getY()
                            && vertices.get(1).getX() == vertices.get(2).getX()
                            && vertices.get(2).getY() == vertices.get(3).getY() && vertices.get(3)
                            .getX() == vertices.get(0).getX())) {
                /*
                 * If this is the case, we have a rectangular polygon aligned to
                 * the axes of its CRS. i.e. a simple bounding box.
                 */
                List<Double> xs = new AbstractList<Double>() {
                    @Override
                    public Double get(int index) {
                        return vertices.get(index).getX();
                    }

                    @Override
                    public int size() {
                        return vertices.size();
                    }
                };
                List<Double> ys = new AbstractList<Double>() {
                    @Override
                    public Double get(int index) {
                        return vertices.get(index).getY();
                    }

                    @Override
                    public int size() {
                        return vertices.size();
                    }
                };
                inBounds.add(new BoundingBoxImpl(Collections.min(xs), Collections.min(ys),
                        Collections.max(xs), Collections.max(ys), crs));
            }
        }
    }

    @Override
    public boolean contains(double x, double y) {
        /*
         * First check the rectangles which approximate this polygon. If it's in
         * one of them, it's in the Polygon
         */
        for (BoundingBoxImpl inBound : inBounds) {
            if (inBound.contains(x, y)) {
                return true;
            }
        }
        /*
         * Need to do a full check on the boundary path
         */
        return super.contains(x, y);
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    @Override
    public List<HorizontalPosition> getVertices() {
        return vertices;
    }

    @Override
    public String toString() {
        return Arrays.toString(vertices.toArray());
    }
}
