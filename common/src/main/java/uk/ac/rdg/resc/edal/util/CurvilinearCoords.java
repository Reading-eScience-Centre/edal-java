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

package uk.ac.rdg.resc.edal.util;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * A horizontal (2D) grid that is defined by explicitly specifying the longitude
 * and latitude coordinates of its cells. We assume the WGS84 lat-lon coordinate
 * system.
 * 
 * @author Guy Griffiths
 * @author Jon Blower
 */
public final class CurvilinearCoords {
    /** The number of grid cells in the i direction */
    private final int ni;
    /** The number of grid cells in the j direction */
    private final int nj;

    /**
     * The longitudes of the centres of the grid cells, flattened to a 1D array
     * of size ni*nj
     */
    private final float[] longitudes;
    /**
     * The latitudes of the centres of the grid cells, flattened to a 1D array
     * of size ni*nj
     */
    private final float[] latitudes;

    /** The longitudes of the corners of the grid cells */
    private final Array2D<Number> cornerLons;
    /** The latitudes of the corners of the grid cells */
    private final Array2D<Number> cornerLats;
    /** The lon-lat bounding box of the grid */
    private final BoundingBox lonLatBbox;

    public CurvilinearCoords(Array2D<Number> lonVals, Array2D<Number> latVals) {
        /* Sanity check */
        if (!Arrays.equals(lonVals.getShape(), latVals.getShape())) {
            throw new IllegalArgumentException(String.format(
                    "Lon and Lat axes must have the same shape. Lon: %s; Lat: %s",
                    Arrays.toString(lonVals.getShape()), Arrays.toString(latVals.getShape())));
        }

        ni = lonVals.getShape()[1];
        nj = lonVals.getShape()[0];

        if (ni < 3 || nj < 3) {
            throw new IllegalArgumentException(
                    "Curvilinear coordinates need at least 3 points in each dimension to function correctly");
        }

        longitudes = new float[ni * nj];
        latitudes = new float[ni * nj];

        /*
         * Make sure all longitudes are in the range [-180,180] and find the min
         * and max lat and lon values
         */
        double minLon = 180.0;
        double maxLon = -180.0;
        double minLat = 90.0;
        double maxLat = -90.0;
        int index = 0;
        for (int j = 0; j < nj; j++) {
            for (int i = 0; i < ni; i++) {
                Number lon = lonVals.get(j, i);
                Number lat = latVals.get(j, i);
                if (lon == null || lat == null || Double.isNaN(lon.doubleValue())
                        || Double.isNaN(lat.doubleValue())) {
                    longitudes[index] = Float.NaN;
                    latitudes[index] = Float.NaN;
                } else {
                    lon = GISUtils.constrainLongitude180(lon.floatValue());
                    minLon = Math.min(minLon, lon.doubleValue());
                    maxLon = Math.max(maxLon, lon.doubleValue());
                    minLat = Math.min(minLat, lat.doubleValue());
                    maxLat = Math.max(maxLat, lat.doubleValue());
                    longitudes[index] = lon.floatValue();
                    latitudes[index] = lat.floatValue();
                }
                index++;
            }
        }

        if (maxLon < minLon || maxLat < minLat) {
            throw new IllegalStateException("Invalid bounding box");
        }
        System.out.println("Curvilinear coords "+minLon+"'"+maxLon);

        lonLatBbox = new BoundingBoxImpl(minLon, minLat, maxLon, maxLat, GISUtils.defaultGeographicCRS());

        /* Calculate the corners of the grid cells */
        cornerLons = makeCorners(longitudes, true);
        cornerLats = makeCorners(latitudes, false);
    }

    /**
     * Adapted from previous ncWMS
     */
    private Array2D<Number> makeCorners(float[] midpoints, boolean isLongitude) {
        Array2D<Number> edges = new ValuesArray2D(nj + 1, ni + 1);

        for (int j = 0; j < nj - 1; j++) {
            for (int i = 0; i < ni - 1; i++) {
                /*
                 * The interior edges are the average of the 4 surrounding
                 * midpoints
                 */
                double midpoint1 = midpoints[getIndex(i, j)];
                double midpoint2 = midpoints[getIndex(i + 1, j)];
                double midpoint3 = midpoints[getIndex(i, j + 1)];
                double midpoint4 = midpoints[getIndex(i + 1, j + 1)];
                if (isLongitude) {
                    /*-
                     * Make sure that all corners are as close together as
                     * possible 
                     * e.g. see whether we need to use -179 or +181.
                     */
                    midpoint2 = GISUtils.getNearestEquivalentLongitude(midpoint1, midpoint2);
                    midpoint3 = GISUtils.getNearestEquivalentLongitude(midpoint1, midpoint3);
                    midpoint4 = GISUtils.getNearestEquivalentLongitude(midpoint1, midpoint4);
                }
                double xval = (midpoint1 + midpoint2 + midpoint3 + midpoint4) / 4.0;
                edges.set(xval, j + 1, i + 1);
            }
            /* Extrapolate to exterior points */
            double val = edges.get(j + 1, 1).doubleValue()
                    - (edges.get(j + 1, 2).doubleValue() - edges.get(j + 1, 1).doubleValue());
            edges.set(val, j + 1, 0);
            val = edges.get(j + 1, ni - 1).doubleValue()
                    + (edges.get(j + 1, ni - 1).doubleValue() - edges.get(j + 1, ni - 2)
                            .doubleValue());
            edges.set(val, j + 1, ni);
        }

        /* Extrapolate to the first and last row */
        for (int x = 0; x < ni + 1; x++) {
            double val = edges.get(1, x).doubleValue()
                    - (edges.get(2, x).doubleValue() - edges.get(1, x).doubleValue());
            edges.set(val, 0, x);
            val = edges.get(nj - 1, x).doubleValue()
                    + (edges.get(nj - 1, x).doubleValue() - edges.get(nj - 2, x).doubleValue());
            edges.set(val, nj, x);
        }

        return edges;
    }

    /**
     * Gets the location of the midpoint of the cell at indices i, j. The
     * {@link HorizontalPosition#getLongitude() longitude coordinate} of the
     * midpoint will be in the range [-180,180].
     * 
     * @throws ArrayIndexOutOfBoundsException
     *             if i and j combine to give a point outside the grid.
     */
    public HorizontalPosition getMidpoint(int i, int j) {
        int index = getIndex(i, j);
        return new HorizontalPosition(longitudes[index], latitudes[index]);
    }

    private int getIndex(int i, int j) {
        return j * ni + i;
    }

    /**
     * Gets the location of the four corners of the cell at indices i, j.
     * 
     * @throws ArrayIndexOutOfBoundsException
     *             if i and j combine to give a point outside the grid.
     */
    private List<HorizontalPosition> getCorners(int i, int j) {
        List<HorizontalPosition> corners = new ArrayList<HorizontalPosition>(4);
        corners.add(getCorner(i, j));
        corners.add(getCorner(i + 1, j));
        corners.add(getCorner(i + 1, j + 1));
        corners.add(getCorner(i, j + 1));
        return corners;
    }

    /**
     * Gets the coordinates of the corner with the given indices <i>in the
     * arrays of corner coordinates</i> (not in the arrays of midpoints).
     */
    private HorizontalPosition getCorner(int cornerI, int cornerJ) {
        return new HorizontalPosition(cornerLons.get(cornerJ, cornerI).doubleValue(), cornerLats.get(
                cornerJ, cornerI).doubleValue());
    }

    /**
     * Gets the [i,j]th cell in this grid.
     * 
     * TODO cache or precompute the cells?
     * 
     * @throws IllegalArgumentException
     *             if i,j is not a valid cell in this grid.
     */
    public Cell getCell(int i, int j) {
        if (i < 0 || j < 0 || i >= ni || j >= nj) {
            throw new IllegalArgumentException(i + ", " + j + " is not a valid cell in this grid");
        }
        return new Cell(i, j);
    }

    /**
     * @return the number of points in the i direction in this grid
     */
    public int getNi() {
        return ni;
    }

    /**
     * @return the number of points in the j direction in this grid
     */
    public int getNj() {
        return nj;
    }

    /** Returns the number of cells in this grid */
    public int size() {
        return longitudes.length;
    }

    public BoundingBox getBoundingBox() {
        return lonLatBbox;
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode = 31 * hashCode + ni;
        hashCode = 31 * hashCode + nj;
        hashCode = 31 * hashCode + Arrays.hashCode(longitudes);
        hashCode = 31 * hashCode + Arrays.hashCode(latitudes);
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof CurvilinearCoords))
            return false;
        CurvilinearCoords other = (CurvilinearCoords) obj;
        return ni == other.ni && nj == other.nj && Arrays.equals(longitudes, other.longitudes)
                && Arrays.equals(latitudes, other.latitudes);
    }

    /**
     * Returns an unmodifiable list of the cells in this grid, with the i
     * direction varying fastest.
     */
    public List<Cell> getCells() {
        return new CellList();
    }

    /**
     * An unmodifiable iterator over the cells in this grid. Not to be confused
     * with a cellist.
     */
    private final class CellList extends AbstractList<Cell> {
        @Override
        public Cell get(int index) {
            int i = index % CurvilinearCoords.this.ni;
            int j = index / CurvilinearCoords.this.ni;
            return new Cell(i, j);
        }

        @Override
        public int size() {
            return CurvilinearCoords.this.size();
        }
    }

    /**
     * Returns the area of the quadrilateral defined by the given four vertices.
     * Uses Bretschneider's Formula,
     * http://mathworld.wolfram.com/BretschneidersFormula.html
     */
    private static double getArea(Point2D p1, Point2D p2, Point2D p3, Point2D p4) {
        /* The squares of the side lengths */
        double a2 = p1.distanceSq(p2);
        double b2 = p2.distanceSq(p3);
        double c2 = p3.distanceSq(p4);
        double d2 = p4.distanceSq(p1);
        /* The squares of the diagonal lengths */
        double f2 = p1.distanceSq(p3);
        double g2 = p2.distanceSq(p4);
        /* Calculate an intermediate term */
        double term = b2 + d2 - a2 - c2;
        /* Calculate and return the area */
        return Math.sqrt(4 * f2 * g2 - term * term) / 4.0;
    }

    /**
     * Gets the mean area of the cells in this grid, in square degrees.
     */
    public double getMeanCellArea() {
        double sumArea = 0.0;
        int nans = 0;
        for (Cell cell : getCells()) {
            double cellArea = cell.getArea();
            /* Cell areas can be NaN - see Javadoc for Cell.getArea() */
            if (Double.isNaN(cellArea)) {
                nans++;
            } else {
                sumArea += cellArea;
            }
        }
        return sumArea / (size() - nans);
    }

    /**
     * A cell within this curvilinear grid.
     */
    public class Cell {
        private final int i;
        private final int j;
        /*
         * The minimum bounding rectangle - lazily instantiated
         */
        private BoundingBox mbr = null;

        /** Can only be instantiated from the CurvilinearGrid class */
        private Cell(int i, int j) {
            this.i = i;
            this.j = j;
        }

        public int getI() {
            return i;
        }

        public int getJ() {
            return j;
        }

        /**
         * Gets the centre point of this cell. Note that in some grid
         * formulations, the point could be represented by NaNs (in this case
         * the cell cannot be used or plotted: it exists in the grid simply for
         * structural convenience).
         */
        public HorizontalPosition getCentre() {
            return CurvilinearCoords.this.getMidpoint(i, j);
        }

        /**
         * <p>
         * Returns a list of the (four) corners of this cell. The longitude
         * coordinate (given by {@link Point2D#getX()}) of all the corners will
         * be as close to the {@link #getCentre() centre} of the cell as
         * possible. That is to say, if the centre of the cell is at a longitude
         * of 179 degrees then a corner of the cell would be given with a
         * longitude of 181 degrees, rather than -179 degrees. This helps with
         * the plotting of the cell on a plane. This method returns a new List
         * containing new Points with each invocation.
         * </p>
         * <p>
         * Note that in some grid formulations, the corners could be represented
         * by NaNs (in this case the cell cannot be used or plotted: it exists
         * in the grid simply for structural convenience).
         * </p>
         */
        public List<Point2D> getCorners() {
            List<HorizontalPosition> corners = CurvilinearCoords.this.getCorners(i, j);
            List<Point2D> cornerPoints = new ArrayList<Point2D>(corners.size());
            for (HorizontalPosition corner : corners) {
                Point2D cornerPoint = new Point2D.Double(
                        harmonizeWithCentre(corner.getX()), corner.getY());
                cornerPoints.add(cornerPoint);
            }
            return cornerPoints;
        }

        /**
         * Gets the neighbours of this cell (up to four) that join this cell
         * along an edge. The order of the cells in the list is such that the
         * centres of the cells can be joined to form a polygon in which the
         * edges do not cross.
         */
        public List<Cell> getEdgeNeighbours() {
            List<Cell> neighbours = new ArrayList<Cell>(4);
            if (i > 0) {
                neighbours.add(new Cell(i - 1, j));
            }
            if (j > 0) {
                neighbours.add(new Cell(i, j - 1));
            }
            if (i < CurvilinearCoords.this.ni - 1) {
                neighbours.add(new Cell(i + 1, j));
            }
            if (j < CurvilinearCoords.this.nj - 1) {
                neighbours.add(new Cell(i, j + 1));
            }
            return neighbours;
        }

        /**
         * Gets the neighbours of this cell (up to four) that join this cell at
         * a corner. The order of the cells in the list is such that the centres
         * of the cells can be joined to form a polygon in which the edges do
         * not cross.
         */
        public List<Cell> getCornerNeighbours() {
            List<Cell> neighbours = new ArrayList<Cell>(4);
            if (i > 0 && j > 0) {
                neighbours.add(new Cell(i - 1, j - 1));
            }
            if (i < CurvilinearCoords.this.ni - 1 && j > 0) {
                neighbours.add(new Cell(i + 1, j - 1));
            }
            if (i < CurvilinearCoords.this.ni - 1 && j < CurvilinearCoords.this.nj - 1) {
                neighbours.add(new Cell(i + 1, j + 1));
            }
            if (i > 0 && j < CurvilinearCoords.this.nj - 1) {
                neighbours.add(new Cell(i - 1, j + 1));
            }
            return neighbours;
        }

        /**
         * Gets the neighbours of this cell (up to eight) that join this cell at
         * an edge or corner.
         */
        public List<Cell> getNeighbours() {
            List<Cell> neighbours = getEdgeNeighbours();
            neighbours.addAll(getCornerNeighbours());
            return neighbours;
        }

        /**
         * <p>
         * Returns the area of this cell in square degrees.
         * </p>
         * <p>
         * Note that in some grid formulations, the area could be NaN (in this
         * case the cell cannot be used or plotted: it exists in the grid simply
         * for structural convenience).
         * </p>
         */
        public double getArea() {
            List<Point2D> corners = this.getCorners();
            return CurvilinearCoords.getArea(corners.get(0), corners.get(1), corners.get(2),
                    corners.get(3));
        }

        /**
         * Gets a Path2D object representing the boundary of this cell, formed
         * by joining its {@link #getCorners() corners} by straight lines in
         * longitude-latitude space. This returns a new Path2D object with each
         * invocation.
         */
        public Path2D getBoundaryPath() {
            Path2D path = new Path2D.Double();
            boolean firstTime = true;
            for (Point2D point : getCorners()) {
                /* Add the point to the path */
                if (firstTime)
                    path.moveTo(point.getX(), point.getY());
                else
                    path.lineTo(point.getX(), point.getY());
                firstTime = false;
            }
            path.closePath();
            return path;
        }

        public BoundingBox getMinimumBoundingRectangle() {
            /* Lazily instantiated: only needed for rtrees */
            if (mbr == null) {
                List<Point2D> corners = getCorners();
                if (corners.isEmpty()) {
                    /* Shouldn't happen */
                    return null;
                }
                Point2D corner1 = corners.get(0);
                double minX = corner1.getX();
                double maxX = minX;
                double minY = corner1.getY();
                double maxY = minY;
                for (int ii = 1, size = corners.size(); ii < size; ii++) {
                    Point2D corner = corners.get(ii);
                    minX = Math.min(minX, corner.getX());
                    maxX = Math.max(maxX, corner.getX());
                    minY = Math.min(minY, corner.getY());
                    maxY = Math.max(maxY, corner.getY());
                }
                mbr = new BoundingBoxImpl(minX, minY, maxX, maxY, GISUtils.defaultGeographicCRS());
            }
            return mbr;
        }

        /**
         * Finds the square of the distance between the centre of this cell and
         * the given HorizontalPosition
         */
        public double findDistanceSq(double lon, double lat) {
            HorizontalPosition centre = this.getCentre();
            lon = harmonizeWithCentre(lon);
            double dx = lon - centre.getX();
            double dy = lat - centre.getY();
            return dx * dx + dy * dy;
        }

        /**
         * Returns true if this cell's {@link #getBoundaryPath() boundary}
         * contains the given longitude-latitude point.
         * 
         * TODO what happens if this cell is represented by NaNs?
         */
        public boolean contains(double lon, double lat) {
            lon = harmonizeWithCentre(lon);
            Path2D path = getBoundaryPath();
            return path.contains(lon, lat);
        }

        /**
         * Harmonizes the given longitude (in the range [-180:180]) with the
         * centre of this cell.
         */
        private double harmonizeWithCentre(double lon) {
            return GISUtils.getNearestEquivalentLongitude(getCentre().getX(), lon);
        }

        @Override
        public int hashCode() {
            int hashCode = 17;
            hashCode = 31 * hashCode + i;
            hashCode = 31 * hashCode + j;
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof Cell))
                return false;
            Cell other = (Cell) obj;
            return i == other.i && j == other.j;
        }

        @Override
        public String toString() {
            HorizontalPosition centre = getCentre();
            List<Point2D> corners = getCorners();
            return String.format("[%d,%d]: [%f,%f] %s", i, j, centre.getX(),
                    centre.getY(), corners);
        }
    }

    public static void main(String[] args) {
        Array2D<Number> latVals = new ValuesArray2D(3, 3);
        Array2D<Number> lonVals = new ValuesArray2D(3, 3);
        latVals.set(0.0, 0, 0);
        latVals.set(0.0, 0, 1);
        latVals.set(0.0, 0, 2);

        latVals.set(1.0, 1, 0);
        latVals.set(1.0, 1, 1);
        latVals.set(1.0, 1, 2);

        latVals.set(2.0, 2, 0);
        latVals.set(2.0, 2, 1);
        latVals.set(2.0, 2, 2);

        lonVals.set(173.0, 0, 0);
        lonVals.set(177.0, 0, 1);
        lonVals.set(-179.0, 0, 2);

        lonVals.set(173.0, 1, 0);
        lonVals.set(177.0, 1, 1);
        lonVals.set(-179.0, 1, 2);

        lonVals.set(173.0, 2, 0);
        lonVals.set(177.0, 2, 1);
        lonVals.set(-179.0, 2, 2);

        Iterator<Number> iterator = latVals.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
        iterator = lonVals.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
        CurvilinearCoords test1 = new CurvilinearCoords(lonVals, latVals);
        for (Cell cell : test1.getCells()) {
            for(Point2D p : cell.getCorners()) {
                System.out.println(p);
            }
            System.out.println();
        }
    }
}
