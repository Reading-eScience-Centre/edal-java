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

package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.measure.unit.Unit;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.RangeMeaning;

import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.Polygon;
import uk.ac.rdg.resc.edal.geometry.impl.AbstractPolygon;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.impl.HorizontalPositionImpl;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * A horizontal (2D) grid that is defined by explicitly specifying the x and y
 * coordinates of each of its cells.
 * 
 * @todo Probably not ready to use yet - need to check functionality,
 *       particularly to see if longitude wrapping is handled correctly. Will
 *       findContainingCell work correctly?
 * 
 * @author Jon Blower
 * @author Guy Griffiths
 */
public final class CurvilinearGrid extends AbstractHorizontalGrid
{
    private final FloatArrayGridValuesMatrix xCoords;
    private final FloatArrayGridValuesMatrix yCoords;
    private final CoordinateReferenceSystem crs;
    
    private final FloatArrayGridValuesMatrix xCorners;
    private final FloatArrayGridValuesMatrix yCorners;
    private final BoundingBox extent;
    
    public CurvilinearGrid(GridValuesMatrix<Double> xCoords, GridValuesMatrix<Double> yCoords,
            CoordinateReferenceSystem crs)
    {
        // Sanity check
        if (!xCoords.getGridExtent().equals(yCoords.getGridExtent())) {
            throw new IllegalArgumentException("xCoords and yCoords must have the same shape");
        }
        this.crs = crs;
        
        int ni = xCoords.getAxis(0).size();
        int nj = xCoords.getAxis(1).size();
        
        // Detect whether either of the axes is longitude
        boolean xIsLongitude = false;
        boolean yIsLongitude = false;
        if (crs != null) {
            CoordinateSystem cs = crs.getCoordinateSystem();
            CoordinateSystemAxis xAxis = cs.getAxis(0);
            CoordinateSystemAxis yAxis = cs.getAxis(1);
            xIsLongitude = isAxisLongitude(xAxis);
            // There are CRSs (e.g. EPSG:4326) in which y is longitude!
            yIsLongitude = isAxisLongitude(yAxis);
        }
        
        // Initialize the arrays of x and y coords
        this.xCoords = new FloatArrayGridValuesMatrix(ni, nj);
        this.yCoords = new FloatArrayGridValuesMatrix(ni, nj);
        
        // Make sure all longitudes are in the range [-180,180] and find the
        // min and max x and y values
        double minX = Double.NaN;
        double maxX = Double.NaN;
        double minY = Double.NaN;
        double maxY = Double.NaN;
        for (int j = 0; j < nj; j++) {
            for (int i = 0; i < ni; i++) {
                double x = xCoords.readPoint(new int[] { i, j });
                double y = yCoords.readPoint(new int[] { i, j });
                boolean isNaN = Double.isNaN(x) || Double.isNaN(y);
                if (!isNaN) {
                    if (xIsLongitude) {
                        x = GISUtils.constrainLongitude180(x);
                    }
                    if (yIsLongitude) {
                        y = GISUtils.constrainLongitude180(y);
                    }
                    minX = Double.isNaN(minX) ? x : Math.min(minX, x);
                    maxX = Double.isNaN(maxX) ? x : Math.max(maxX, x);
                    minY = Double.isNaN(minY) ? y : Math.min(minY, y);
                    maxY = Double.isNaN(maxY) ? y : Math.max(maxY, y);
                }
                this.xCoords.set(i, j, isNaN ? Float.NaN : (float)x);
                this.yCoords.set(i, j, isNaN ? Float.NaN : (float)y);
            }
        }
        this.extent = new BoundingBoxImpl(minX, minY, maxX, maxY, crs);
        
        this.xCorners = makeCorners(this.xCoords, xIsLongitude);
        this.yCorners = makeCorners(this.yCoords, yIsLongitude);
    }
    
    private boolean isAxisLongitude(CoordinateSystemAxis axis)
    {
        return axis.getRangeMeaning() == RangeMeaning.WRAPAROUND &&
               axis.getUnit().equals(Unit.valueOf("deg"));
    }
    
    /**
     * Calculates the positions of the corners of the grid
     */
    private FloatArrayGridValuesMatrix makeCorners(FloatArrayGridValuesMatrix centres,
            boolean isLongitude)
    {
        int ni = centres.getAxis(0).size();
        int nj = centres.getAxis(1).size();
        
        FloatArrayGridValuesMatrix edges = new FloatArrayGridValuesMatrix(ni + 1, nj + 1);

        for (int j = 0; j < nj - 1; j++) {
            for (int i = 0; i < ni - 1; i++) {
                // the interior edges are the average of the 4 surrounding
                // midpoints
                double midpoint1 = centres.readPoint(new int[] { i, j });
                double midpoint2 = centres.readPoint(new int[] { i + 1, j });
                double midpoint3 = centres.readPoint(new int[] { i, j + 1 });
                double midpoint4 = centres.readPoint(new int[] { i + 1, j + 1 });
                if (isLongitude) {
                    // Make sure that all corners are as close together as
                    // possible,
                    // e.g. see whether we need to use -179 or +181.
                    midpoint2 = harmonizeLongitudes(midpoint1, midpoint2);
                    midpoint3 = harmonizeLongitudes(midpoint1, midpoint3);
                    midpoint4 = harmonizeLongitudes(midpoint1, midpoint4);
                }
                double xval = (midpoint1 + midpoint2 + midpoint3 + midpoint4) / 4.0;
                edges.set(i + 1, j + 1, (float) xval);
            }
            // extrapolate to exterior points
            edges.set(0, j + 1, edges.get(1, j + 1) - (edges.get(2, j + 1) - edges.get(1, j + 1)));
            edges.set(ni, j + 1,
                    edges.get(ni - 1, j + 1)
                            + (edges.get(ni - 1, j + 1) - edges.get(ni - 2, j + 1)));
        }

        // extrapolate to the first and last row
        for (int x = 0; x < ni + 1; x++) {
            edges.set(x, 0, edges.get(x, 1) - (edges.get(x, 2) - edges.get(x, 1)));
            edges.set(x, nj, edges.get(x, nj - 1) + (edges.get(x, nj - 1) - edges.get(x, nj - 2)));
        }

        return edges;
    }
    
    /**
     * Given a reference longitude and a "test" longitude, this routine returns
     * a longitude point equivalent to the test longitude such that the
     * expression {@code abs(ref - test)} is as small as possible.
     * 
     * @param ref
     *            Reference longitude, which must be in the range [-180,180]
     * @param test
     *            Test longitude
     * @return A longitude point equivalent to the test longitude that minimizes
     *         the expression {@code abs(ref - test)}. This point will not
     *         necessarily be in the range [-180,180]
     * @throws IllegalArgumentException
     *             if the reference longitude is not in the range [-180,180]
     * @todo unit tests for this
     * @todo move to Longitude class?
     */
    private static double harmonizeLongitudes(double ref, double test) {
        if (ref < -180.0 || ref > 180.0) {
            throw new IllegalArgumentException("Reference longitude must be "
                    + "in the range [-180,180]");
        }
        double lon1 = GISUtils.constrainLongitude180(test);
        double lon2 = ref < 0.0 ? lon1 - 360.0 : lon1 + 360.0;
        double d1 = Math.abs(ref - lon1);
        double d2 = Math.abs(ref - lon2);
        return d1 < d2 ? lon1 : lon2;
    }
    

    @Override
    protected HorizontalPosition getGridCellCentreNoBoundsCheck(int i, int j) {
        double x = this.xCoords.readPoint(new int[] { i, j });
        double y = this.yCoords.readPoint(new int[] { i, j });
        return new HorizontalPositionImpl(x, y, this.crs);
    }

    @Override
    protected Polygon getGridCellFootprintNoBoundsCheck(int i, int j)
    {
        List<HorizontalPosition> corners = new ArrayList<HorizontalPosition>(4);
        corners.add(getCorner(i, j));
        corners.add(getCorner(i + 1, j));
        corners.add(getCorner(i + 1, j + 1));
        corners.add(getCorner(i, j + 1));
        final List<HorizontalPosition> uCorners = Collections.unmodifiableList(corners);
        
        return new AbstractPolygon()
        {
            @Override
            public List<HorizontalPosition> getVertices() {
                return uCorners;
            }
            
            @Override
            public CoordinateReferenceSystem getCoordinateReferenceSystem() {
                return CurvilinearGrid.this.getCoordinateReferenceSystem();
            }
        };
    }
    
    private HorizontalPosition getCorner(int i, int j)
    {
        double x = this.xCorners.readPoint(new int[] { i, j });
        double y = this.yCorners.readPoint(new int[] { i, j });
        return new HorizontalPositionImpl(x, y, this.getCoordinateReferenceSystem());
    }

    @Override
    protected GridCell2D findContainingCell(double x, double y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BoundingBox getCoordinateExtent() {
        return this.extent;
    }

    @Override
    public GridAxis getXAxis() {
        return this.xCoords.getAxis(0);
    }

    @Override
    public GridAxis getYAxis() {
        return this.xCoords.getAxis(1);
    }
    
    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return this.crs;
    }
    
    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + this.xCoords.hashCode();
        hash = hash * 31 + this.yCoords.hashCode();
        if (this.crs != null) {
            hash = hash * 31 + this.crs.hashCode();
        }
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof CurvilinearGrid)) return false;
        CurvilinearGrid other = (CurvilinearGrid)obj;
        return this.xCoords.equals(other.xCoords) &&
               this.yCoords.equals(other.yCoords) &&
               this.crs == null ? other.crs == null : this.crs.equals(other.crs);
    }

}
