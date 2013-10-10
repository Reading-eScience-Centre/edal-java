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

package uk.ac.rdg.resc.edal.grid.cdm;

//import java.awt.geom.Path2D;
//import java.awt.geom.Point2D;
//import java.util.AbstractList;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.EnumSet;
//import java.util.List;
//import java.util.Set;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import ucar.ma2.ArrayFloat;
//import ucar.nc2.constants.AxisType;
//import ucar.nc2.dataset.CoordinateAxis;
//import ucar.nc2.dataset.CoordinateAxis2D;
//import ucar.nc2.dataset.NetcdfDataset.Enhance;
//import ucar.nc2.dt.GridCoordSystem;
//import uk.ac.rdg.resc.edal.geometry.BoundingBox;
//import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
//import uk.ac.rdg.resc.edal.position.LonLatPosition;
//import uk.ac.rdg.resc.edal.position.impl.LonLatPositionImpl;
//import uk.ac.rdg.resc.edal.util.GISUtils;
//
///**
// * A horizontal (2D) grid that is defined by explicitly specifying the longitude
// * and latitude coordinates of its cells. We assume the WGS84 lat-lon coordinate
// * system. This class holds references to passed-in arrays of longitude and
// * latitude, but does not modify them or provide any public methods to modify
// * them. Modification of these arrays outside this class will cause undefined
// * behaviour.
// * 
// * @author Jon Blower
// */
//public final class CurvilinearCoords {
//    private static final Logger logger = LoggerFactory.getLogger(CurvilinearCoords.class);
//
//    /** The number of grid cells in the i direction */
//    private final int ni;
//    /** The number of grid cells in the j direction */
//    private final int nj;
//
//    // We use floats to store the midpoints and corners to save memory.
//    // The arrays can be very large for large datasets.
//    // TODO: we could probably avoid storing the corners altogether, although
//    // it may be expensive to keep re-creating them (we need the corners to
//    // figure
//    // out whether an arbitrary lat-lon point is within a grid cell).
//
//    /**
//     * The longitudes of the centres of the grid cells, flattened to a 1D array
//     * of size ni*nj
//     */
//    private final float[] longitudes;
//    /**
//     * The latitudes of the centres of the grid cells, flattened to a 1D array
//     * of size ni*nj
//     */
//    private final float[] latitudes;
//
//    /** The longitudes of the corners of the grid cells */
//    private final ArrayFloat.D2 cornerLons;
//    /** The latitudes of the corners of the grid cells */
//    private final ArrayFloat.D2 cornerLats;
//    /** The lon-lat bounding box of the grid */
//    private final BoundingBox lonLatBbox;
//
//    private static final Set<Enhance> SCALE_MISSING = EnumSet.of(Enhance.ScaleMissing);
//
//    /**
//     * Creates a CurvilinearGrid from a GridCoordSystem.
//     * 
//     * @param coordSys
//     *            The GridCoordSystem from which this CurvilinearGrid will be
//     *            created.
//     * @throws IllegalArgumentException
//     *             if the x and y axes of the provided GridCoordSystem are not
//     *             2D coordinate axes of type Lon and Lat respectively
//     */
//    public CurvilinearCoords(GridCoordSystem coordSys) {
//        CoordinateAxis xAxis = coordSys.getXHorizAxis();
//        CoordinateAxis yAxis = coordSys.getYHorizAxis();
//        if (xAxis == null || yAxis == null || !(xAxis instanceof CoordinateAxis2D)
//                || !(yAxis instanceof CoordinateAxis2D) || xAxis.getAxisType() != AxisType.Lon
//                || yAxis.getAxisType() != AxisType.Lat) {
//            throw new IllegalArgumentException("Coordinate system must consist"
//                    + " of two-dimensional latitude and longitude axes");
//        }
//        CoordinateAxis2D lonAxis = (CoordinateAxis2D) coordSys.getXHorizAxis();
//        CoordinateAxis2D latAxis = (CoordinateAxis2D) coordSys.getYHorizAxis();
//
//        // Sanity check
//        if (!Arrays.equals(lonAxis.getShape(), latAxis.getShape())) {
//            throw new IllegalArgumentException(String.format(
//                    "Lon and Lat axes must have the same shape. Lon: %s; Lat: %s",
//                    Arrays.toString(lonAxis.getShape()), Arrays.toString(latAxis.getShape())));
//        }
//
//        // Make sure that scale/offset/missing are processed for the coordinate
//        // axis values
//        lonAxis.enhance(SCALE_MISSING);
//        latAxis.enhance(SCALE_MISSING);
//
//        this.ni = lonAxis.getShape(1);
//        this.nj = lonAxis.getShape(0);
//        this.longitudes = new float[this.ni * this.nj];
//        this.latitudes = new float[this.ni * this.nj];
//
//        // Make sure all longitudes are in the range [-180,180] and find the
//        // min and max lat and lon values
//        double minLon = 180.0;
//        double maxLon = -180.0;
//        double minLat = 90.0;
//        double maxLat = -90.0;
//        int index = 0;
//        for (int j = 0; j < this.nj; j++) {
//            for (int i = 0; i < this.ni; i++) {
//                double lon = lonAxis.getCoordValue(j, i);
//                double lat = latAxis.getCoordValue(j, i);
//                boolean isNaN = Double.isNaN(lon) || Double.isNaN(lat);
//                if (!isNaN) {
//                    lon = GISUtils.constrainLongitude180(lon);
//                    minLon = Math.min(minLon, lon);
//                    maxLon = Math.max(maxLon, lon);
//                    minLat = Math.min(minLat, lat);
//                    maxLat = Math.max(maxLat, lat);
//                }
//                this.longitudes[index] = isNaN ? Float.NaN : (float) lon;
//                this.latitudes[index] = isNaN ? Float.NaN : (float) lat;
//                index++;
//            }
//        }
//
//        if (maxLon < minLon || maxLat < minLat) {
//            throw new IllegalStateException("Invalid bounding box");
//        }
//
//        double[] bbox = new double[] { minLon, minLat, maxLon, maxLat };
//        this.lonLatBbox = new BoundingBoxImpl(bbox);
//        logger.debug("Bounding box = {},{},{},{}", new Object[] { this.lonLatBbox.getMinX(),
//                this.lonLatBbox.getMinY(), this.lonLatBbox.getMaxX(), this.lonLatBbox.getMaxY() });
//
//        // Calculate the corners of the grid cells
//        logger.debug("Making longitude corners");
//        this.cornerLons = makeCorners(this.longitudes, true);
//        logger.debug("Making latitude corners");
//        this.cornerLats = makeCorners(this.latitudes, false);
//        logger.debug("Made curvilinear grid");
//    }
//
//    /**
//     * Gets the location of the midpoint of the cell at indices i, j. The
//     * {@link LonLatPosition#getLongitude() longitude coordinate} of the
//     * midpoint will be in the range [-180,180].
//     * 
//     * @throws ArrayIndexOutOfBoundsException
//     *             if i and j combine to give a point outside the grid.
//     */
//    public LonLatPosition getMidpoint(int i, int j) {
//        int index = this.getIndex(i, j);
//        return new LonLatPositionImpl(this.longitudes[index], this.latitudes[index]);
//    }
//
//    private int getIndex(int i, int j) {
//        return j * this.ni + i;
//    }
//
//    /**
//     * Gets the location of the four corners of the cell at indices i, j.
//     * 
//     * @throws ArrayIndexOutOfBoundsException
//     *             if i and j combine to give a point outside the grid.
//     */
//    private List<LonLatPosition> getCorners(int i, int j) {
//        List<LonLatPosition> corners = new ArrayList<LonLatPosition>(4);
//        corners.add(getCorner(i, j));
//        corners.add(getCorner(i + 1, j));
//        corners.add(getCorner(i + 1, j + 1));
//        corners.add(getCorner(i, j + 1));
//        return corners;
//    }
//
//    /**
//     * Gets the coordinates of the corner with the given indices <i>in the
//     * arrays of corner coordinates</i> (not in the arrays of midpoints).
//     */
//    private LonLatPosition getCorner(int cornerI, int cornerJ) {
//        return new LonLatPositionImpl(this.cornerLons.get(cornerJ, cornerI), this.cornerLats.get(
//                cornerJ, cornerI));
//    }
//
//    /**
//     * Gets the [i,j]th cell in this grid.
//     * 
//     * @todo cache or precompute the cells?
//     * @throws IllegalArgumentException
//     *             if i,j is not a valid cell in this grid.
//     */
//    public Cell getCell(int i, int j) {
//        if (i < 0 || j < 0 || i >= ni || j >= nj) {
//            throw new IllegalArgumentException(i + "," + j + " is not a valid cell in this grid");
//        }
//        return new Cell(i, j);
//    }
//
//    /**
//     * Adapted from {@link CoordinateAxis2D#makeXEdges(ucar.ma2.ArrayDouble.D2)}
//     * , taking into account the wrapping of longitude at +/- 180 degrees
//     */
//    private ArrayFloat.D2 makeCorners(float[] midpoints, boolean isLongitude) {
//        ArrayFloat.D2 edges = new ArrayFloat.D2(nj + 1, ni + 1);
//
//        for (int j = 0; j < nj - 1; j++) {
//            for (int i = 0; i < ni - 1; i++) {
//                // the interior edges are the average of the 4 surrounding
//                // midpoints
//                double midpoint1 = midpoints[this.getIndex(i, j)];
//                double midpoint2 = midpoints[this.getIndex(i + 1, j)];
//                double midpoint3 = midpoints[this.getIndex(i, j + 1)];
//                double midpoint4 = midpoints[this.getIndex(i + 1, j + 1)];
//                if (isLongitude) {
//                    // Make sure that all corners are as close together as
//                    // possible,
//                    // e.g. see whether we need to use -179 or +181.
//                    midpoint2 = harmonizeLongitudes(midpoint1, midpoint2);
//                    midpoint3 = harmonizeLongitudes(midpoint1, midpoint3);
//                    midpoint4 = harmonizeLongitudes(midpoint1, midpoint4);
//                }
//                double xval = (midpoint1 + midpoint2 + midpoint3 + midpoint4) / 4.0;
//                edges.set(j + 1, i + 1, (float) xval);
//            }
//            // extrapolate to exterior points
//            edges.set(j + 1, 0, edges.get(j + 1, 1) - (edges.get(j + 1, 2) - edges.get(j + 1, 1)));
//            edges.set(j + 1, ni,
//                    edges.get(j + 1, ni - 1)
//                            + (edges.get(j + 1, ni - 1) - edges.get(j + 1, ni - 2)));
//        }
//
//        // extrapolate to the first and last row
//        for (int x = 0; x < ni + 1; x++) {
//            edges.set(0, x, edges.get(1, x) - (edges.get(2, x) - edges.get(1, x)));
//            edges.set(nj, x, edges.get(nj - 1, x) + (edges.get(nj - 1, x) - edges.get(nj - 2, x)));
//        }
//
//        return edges;
//    }
//
//    /**
//     * Given a reference longitude and a "test" longitude, this routine returns
//     * a longitude point equivalent to the test longitude such that the
//     * expression {@code abs(ref - test)} is as small as possible.
//     * 
//     * @param ref
//     *            Reference longitude, which must be in the range [-180,180]
//     * @param test
//     *            Test longitude
//     * @return A longitude point equivalent to the test longitude that minimizes
//     *         the expression {@code abs(ref - test)}. This point will not
//     *         necessarily be in the range [-180,180]
//     * @throws IllegalArgumentException
//     *             if the reference longitude is not in the range [-180,180]
//     * @todo unit tests for this
//     * @todo move to Longitude class?
//     */
//    private static double harmonizeLongitudes(double ref, double test) {
//        if (ref < -180.0 || ref > 180.0) {
//            throw new IllegalArgumentException("Reference longitude must be "
//                    + "in the range [-180,180]");
//        }
//        double lon1 = GISUtils.constrainLongitude180(test);
//        double lon2 = ref < 0.0 ? lon1 - 360.0 : lon1 + 360.0;
//        double d1 = Math.abs(ref - lon1);
//        double d2 = Math.abs(ref - lon2);
//        return d1 < d2 ? lon1 : lon2;
//    }
//
//    /**
//     * @return the number of points in the i direction in this grid
//     */
//    public int getNi() {
//        return ni;
//    }
//
//    // TODO: replace this with GridExtents?
//    /**
//     * @return the number of points in the j direction in this grid
//     */
//    public int getNj() {
//        return nj;
//    }
//
//    /** Returns the number of cells in this grid */
//    public int size() {
//        return longitudes.length;
//    }
//
//    public BoundingBox getBoundingBox() {
//        return lonLatBbox;
//    }
//
//    // TODO: could precompute this
//    @Override
//    public int hashCode() {
//        int hashCode = 17;
//        hashCode = 31 * hashCode + ni;
//        hashCode = 31 * hashCode + nj;
//        hashCode = 31 * hashCode + Arrays.hashCode(longitudes);
//        hashCode = 31 * hashCode + Arrays.hashCode(latitudes);
//        return hashCode;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == this)
//            return true;
//        if (!(obj instanceof CurvilinearCoords))
//            return false;
//        CurvilinearCoords other = (CurvilinearCoords) obj;
//        return this.ni == other.ni && this.nj == other.nj
//                && Arrays.equals(this.longitudes, other.longitudes)
//                && Arrays.equals(this.latitudes, other.latitudes);
//    }
//
//    /**
//     * Returns an unmodifiable list of the cells in this grid, with the i
//     * direction varying fastest.
//     */
//    public List<Cell> getCells() {
//        return new CellList();
//    }
//
//    /**
//     * An unmodifiable iterator over the cells in this grid. Not to be confused
//     * with a cellist.
//     */
//    private final class CellList extends AbstractList<Cell> {
//        @Override
//        public Cell get(int index) {
//            int i = index % CurvilinearCoords.this.ni;
//            int j = index / CurvilinearCoords.this.ni;
//            return new Cell(i, j);
//        }
//
//        @Override
//        public int size() {
//            return CurvilinearCoords.this.size();
//        }
//    }
//
//    /**
//     * Returns the area of the quadrilateral defined by the given four vertices.
//     * Uses Bretschneider's Formula,
//     * http://mathworld.wolfram.com/BretschneidersFormula.html
//     */
//    private static double getArea(Point2D p1, Point2D p2, Point2D p3, Point2D p4) {
//        // The squares of the side lengths
//        double a2 = p1.distanceSq(p2);
//        double b2 = p2.distanceSq(p3);
//        double c2 = p3.distanceSq(p4);
//        double d2 = p4.distanceSq(p1);
//        // The squares of the diagonal lengths
//        double f2 = p1.distanceSq(p3);
//        double g2 = p2.distanceSq(p4);
//        // Calculate an intermediate term
//        double term = b2 + d2 - a2 - c2;
//        // Calculate and return the area
//        return Math.sqrt(4 * f2 * g2 - term * term) / 4.0;
//    }
//
//    /**
//     * Gets the mean area of the cells in this grid, in square degrees.
//     */
//    public double getMeanCellArea() {
//        double sumArea = 0.0;
//        int nans = 0;
//        for (Cell cell : this.getCells()) {
//            double cellArea = cell.getArea();
//            // Cell areas can be NaN - see Javadoc for Cell.getArea()
//            if (Double.isNaN(cellArea))
//                nans++;
//            else
//                sumArea += cellArea;
//        }
//        logger.debug("{} cells out of {} had area = NaN", nans, this.size());
//        return sumArea / (this.size() - nans);
//    }
//
//    /**
//     * A cell within this curvilinear grid.
//     */
//    public class Cell {
//        private final int i;
//        private final int j;
//        /*
//         * The minimum bounding rectangle - lazily instantiated
//         */
//        private BoundingBox mbr = null;
//
//        /** Can only be instantiated from the CurvilinearGrid class */
//        private Cell(int i, int j) {
//            this.i = i;
//            this.j = j;
//        }
//
//        /** Gets the i index of this cell within the curvilinear grid */
//        public int getI() {
//            return this.i;
//        }
//
//        /** Gets the j index of this cell within the curvilinear grid */
//        public int getJ() {
//            return this.j;
//        }
//
//        /**
//         * Gets the centre point of this cell. Note that in some grid
//         * formulations, the point could be represented by NaNs (in this case
//         * the cell cannot be used or plotted: it exists in the grid simply for
//         * structural convenience).
//         */
//        public LonLatPosition getCentre() {
//            return CurvilinearCoords.this.getMidpoint(this.i, this.j);
//        }
//
//        /**
//         * <p>
//         * Returns a list of the (four) corners of this cell. The longitude
//         * coordinate (given by {@link Point2D#getX()}) of all the corners will
//         * be as close to the {@link #getCentre() centre} of the cell as
//         * possible. That is to say, if the centre of the cell is at a longitude
//         * of 179 degrees then a corner of the cell would be given with a
//         * longitude of 181 degrees, rather than -179 degrees. This helps with
//         * the plotting of the cell on a plane. This method returns a new List
//         * containing new Points with each invocation.
//         * </p>
//         * <p>
//         * Note that in some grid formulations, the corners could be represented
//         * by NaNs (in this case the cell cannot be used or plotted: it exists
//         * in the grid simply for structural convenience).
//         * </p>
//         */
//        public List<Point2D> getCorners() {
//            List<LonLatPosition> corners = CurvilinearCoords.this.getCorners(this.i, this.j);
//            List<Point2D> cornerPoints = new ArrayList<Point2D>(corners.size());
//            for (LonLatPosition corner : corners) {
//                Point2D cornerPoint = new Point2D.Double(this.harmonizeWithCentre(corner
//                        .getLongitude()), corner.getLatitude());
//                cornerPoints.add(cornerPoint);
//            }
//            return cornerPoints;
//        }
//
//        /**
//         * Gets the neighbours of this cell (up to four) that join this cell
//         * along an edge. The order of the cells in the list is such that the
//         * centres of the cells can be joined to form a polygon in which the
//         * edges do not cross.
//         */
//        public List<Cell> getEdgeNeighbours() {
//            List<Cell> neighbours = new ArrayList<Cell>(4);
//            if (this.i > 0) {
//                neighbours.add(new Cell(this.i - 1, this.j));
//            }
//            if (this.j > 0) {
//                neighbours.add(new Cell(this.i, this.j - 1));
//            }
//            if (this.i < CurvilinearCoords.this.ni - 1) {
//                neighbours.add(new Cell(this.i + 1, this.j));
//            }
//            if (this.j < CurvilinearCoords.this.nj - 1) {
//                neighbours.add(new Cell(this.i, this.j + 1));
//            }
//            return neighbours;
//        }
//
//        /**
//         * Gets the neighbours of this cell (up to four) that join this cell at
//         * a corner. The order of the cells in the list is such that the centres
//         * of the cells can be joined to form a polygon in which the edges do
//         * not cross.
//         */
//        public List<Cell> getCornerNeighbours() {
//            List<Cell> neighbours = new ArrayList<Cell>(4);
//            if (this.i > 0 && this.j > 0) {
//                neighbours.add(new Cell(this.i - 1, this.j - 1));
//            }
//            if (this.i < CurvilinearCoords.this.ni - 1 && this.j > 0) {
//                neighbours.add(new Cell(this.i + 1, this.j - 1));
//            }
//            if (this.i < CurvilinearCoords.this.ni - 1 && this.j < CurvilinearCoords.this.nj - 1) {
//                neighbours.add(new Cell(this.i + 1, this.j + 1));
//            }
//            if (this.i > 0 && this.j < CurvilinearCoords.this.nj - 1) {
//                neighbours.add(new Cell(this.i - 1, this.j + 1));
//            }
//            return neighbours;
//        }
//
//        /**
//         * Gets the neighbours of this cell (up to eight) that join this cell at
//         * an edge or corner.
//         */
//        public List<Cell> getNeighbours() {
//            List<Cell> neighbours = this.getEdgeNeighbours();
//            neighbours.addAll(this.getCornerNeighbours());
//            return neighbours;
//        }
//
//        /**
//         * <p>
//         * Returns the area of this cell in square degrees.
//         * </p>
//         * <p>
//         * Note that in some grid formulations, the area could be NaN (in this
//         * case the cell cannot be used or plotted: it exists in the grid simply
//         * for structural convenience).
//         * </p>
//         */
//        public double getArea() {
//            List<Point2D> corners = this.getCorners();
//            return CurvilinearCoords.getArea(corners.get(0), corners.get(1), corners.get(2),
//                    corners.get(3));
//        }
//
//        /**
//         * Gets a Path2D object representing the boundary of this cell, formed
//         * by joining its {@link #getCorners() corners} by straight lines in
//         * longitude-latitude space. This returns a new Path2D object with each
//         * invocation.
//         */
//        public Path2D getBoundaryPath() {
//            Path2D path = new Path2D.Double();
//            boolean firstTime = true;
//            for (Point2D point : this.getCorners()) {
//                // Add the point to the path
//                if (firstTime)
//                    path.moveTo(point.getX(), point.getY());
//                else
//                    path.lineTo(point.getX(), point.getY());
//                firstTime = false;
//            }
//            path.closePath();
//            return path;
//        }
//
//        public BoundingBox getMinimumBoundingRectangle() {
//            // Lazily instantiated: only needed for rtrees
//            if (this.mbr == null) {
//                List<Point2D> corners = this.getCorners();
//                if (corners.isEmpty())
//                    return null; // Shouldn't happen
//                Point2D corner1 = corners.get(0);
//                double bbox[] = new double[4];
//                bbox[0] = corner1.getX();
//                bbox[2] = bbox[0];
//                bbox[1] = corner1.getY();
//                bbox[3] = bbox[1];
//                for (int ii = 1, size = corners.size(); ii < size; ii++) {
//                    Point2D corner = corners.get(ii);
//                    bbox[0] = Math.min(bbox[0], corner.getX());
//                    bbox[2] = Math.max(bbox[2], corner.getX());
//                    bbox[1] = Math.min(bbox[1], corner.getY());
//                    bbox[3] = Math.max(bbox[3], corner.getY());
//                }
//                this.mbr = new BoundingBoxImpl(bbox);
//            }
//            return this.mbr;
//        }
//
//        /**
//         * Finds the square of the distance between the centre of this cell and
//         * the given LonLatPosition
//         */
//        public double findDistanceSq(double lon, double lat) {
//            LonLatPosition centre = this.getCentre();
//            double dx = lon - centre.getLongitude();
//            double dy = lat - centre.getLatitude();
//            return dx * dx + dy * dy;
//        }
//
//        /**
//         * Returns true if this cell's {@link #getBoundaryPath() boundary}
//         * contains the given longitude-latitude point.
//         * 
//         * @todo what happens if this cell is represented by NaNs?
//         */
//        public boolean contains(double lon, double lat) {
//            lon = this.harmonizeWithCentre(lon);
//            Path2D path = this.getBoundaryPath();
//            return path.contains(lon, lat);
//        }
//
//        /**
//         * Harmonizes the given longitude (in the range [-180:180]) with the
//         * centre of this cell.
//         */
//        private double harmonizeWithCentre(double lon) {
//            return CurvilinearCoords.harmonizeLongitudes(this.getCentre().getLongitude(), lon);
//        }
//
//        @Override
//        public int hashCode() {
//            int hashCode = 17;
//            hashCode = 31 * hashCode + this.i;
//            hashCode = 31 * hashCode + this.j;
//            return hashCode;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (obj == this)
//                return true;
//            if (!(obj instanceof Cell))
//                return false;
//            Cell other = (Cell) obj;
//            return this.i == other.i && this.j == other.j;
//        }
//
//        @Override
//        public String toString() {
//            LonLatPosition centre = this.getCentre();
//            List<Point2D> corners = this.getCorners();
//            return String.format("[%d,%d]: [%f,%f] %s", this.i, this.j, centre.getLongitude(),
//                    centre.getLatitude(), corners);
//        }
//    }
//
//}
