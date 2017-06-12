/*******************************************************************************
 * Copyright (c) 2016 The University of Reading
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

package uk.ac.rdg.resc.edal.grid;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.geometry.SimplePolygon;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;

/**
 * A {@link StaggeredHorizontalGrid} which is derived from its original grid.
 * All cell centres and boundaries are calculated from the nodes of the original
 * grid.
 *
 * @author Guy Griffiths
 */
public class DerivedStaggeredGrid extends AbstractHorizontalGrid implements StaggeredHorizontalGrid, Serializable {
    private static final long serialVersionUID = 1L;
    private HorizontalGrid grid;
    private int xSize;
    private int ySize;
    private GridCellArray2D domainObjects;
    private BoundingBoxImpl bbox;
    private SGridPadding xPadding;
    private SGridPadding yPadding;

    /**
     * Create a new {@link DerivedStaggeredGrid}
     * 
     * @param originalGrid
     *            The original {@link HorizontalGrid} which this grid is based
     *            upon
     * @param xPadding
     *            The {@link SGridPadding} to be applied to the x-axis of the
     *            original grid
     * @param yPadding
     *            The {@link SGridPadding} to be applied to the y-axis of the
     *            original grid
     */
    public DerivedStaggeredGrid(HorizontalGrid originalGrid, SGridPadding xPadding,
            SGridPadding yPadding) {
        super(originalGrid.getCoordinateReferenceSystem());
        this.grid = originalGrid;
        this.xPadding = xPadding;
        this.yPadding = yPadding;

        /*
         * Using the domain objects from the original grid, calculate a new set
         * of domain objects which will reflect this staggered grid. They will
         * then be used for the majority of operations on this grid.
         */
        Array2D<GridCell2D> originalDomainObjects = this.grid.getDomainObjects();

        /*
         * First, find the size of the new grid
         */
        int origXSize = originalDomainObjects.getXSize();
        int origYSize = originalDomainObjects.getYSize();
        xSize = origXSize;
        ySize = origYSize;

        if (xPadding == SGridPadding.BOTH) {
            xSize++;
        } else if (xPadding == SGridPadding.NO_PADDING) {
            xSize--;
        }
        if (yPadding == SGridPadding.BOTH) {
            ySize++;
        } else if (yPadding == SGridPadding.NO_PADDING) {
            ySize--;
        }
        domainObjects = new GridCellArray2D(ySize, xSize);

        /*
         * We want to shift the original grid's cells. Both the centres and the
         * boundaries need shifting, so we need to know how many vertices each
         * cell has.
         * 
         * For a 2D grid, I think this is guaranteed to be 4? However, calculate
         * it in case there are edge cases I've not considered.
         */
        Iterator<GridCell2D> iterator = originalDomainObjects.iterator();
        int nVertices = -1;
        while (iterator.hasNext()) {
            GridCell2D cell = iterator.next();
            if (nVertices < 0) {
                nVertices = cell.getFootprint().getVertices().size();
            } else {
                if (nVertices != cell.getFootprint().getVertices().size()) {
                    throw new IllegalArgumentException(
                            "Need the same number of vertices in each grid cell footprint for a staggered grid");
                }
            }
        }

        /*
         * Now stagger each centre and each cell boundary.
         * 
         * At the same time, calculate the bounding box of the grid (based on
         * the cell centres)
         * 
         * TODO - Should we calculate bounding box based on the cell bounds?
         * That would make more sense. Then, the underlying grid can define
         * whether or not to include the edges of the cells...
         */
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                HorizontalPosition staggeredPosition = staggerPosition(x, y,
                        new Array2D<HorizontalPosition>(origYSize, origXSize) {
                            private static final long serialVersionUID = 1L;
                            @Override
                            public HorizontalPosition get(int... coords) {
                                return originalDomainObjects.get(coords).getCentre();
                            }
                        });
                minX = Math.min(minX, staggeredPosition.getX());
                maxX = Math.max(maxX, staggeredPosition.getX());
                minY = Math.min(minY, staggeredPosition.getY());
                maxY = Math.max(maxY, staggeredPosition.getY());
                List<HorizontalPosition> vertices = new ArrayList<>();
                for (int n = 0; n < nVertices; n++) {
                    final int nFinal = n;
                    HorizontalPosition staggeredVertex = staggerPosition(x, y,
                            new Array2D<HorizontalPosition>(origYSize, origXSize) {
                                private static final long serialVersionUID = 1L;
                                @Override
                                public HorizontalPosition get(int... coords) {
                                    return originalDomainObjects.get(coords).getFootprint()
                                            .getVertices().get(nFinal);
                                }
                            });
                    vertices.add(staggeredVertex);
                }

                GridCell2D cell = new GridCell2DImpl(new GridCoordinates2D(x, y),
                        staggeredPosition, new SimplePolygon(vertices), this);
                domainObjects.set(cell, y, x);
            }
        }
        bbox = new BoundingBoxImpl(minX, minY, maxX, maxY, crs);
    }

    /**
     * Helper method to stagger a single position based on an array of the
     * original positions. This calculates an average of the surrounding
     * positions (either 4 or 2 of them, depending on whether this position is
     * on a face or an edge).
     * 
     * @param x
     *            The x index of the position to stagger
     * @param y
     *            The y index of the position to stagger
     * @param positions
     *            The {@link Array2D} of {@link HorizontalPosition}s to stagger
     * @return The staggered position
     */
    @SuppressWarnings("incomplete-switch")
    private HorizontalPosition staggerPosition(int x, int y, Array2D<HorizontalPosition> positions) {
        List<Integer> xPointsToAverage = new ArrayList<>();
        xPointsToAverage.add(x);
        switch (xPadding) {
        case NO_PADDING:
        case HIGH:
            xPointsToAverage.add(x + 1);
            break;
        case LOW:
        case BOTH:
            xPointsToAverage.add(x - 1);
            break;
        }
        List<Integer> yPointsToAverage = new ArrayList<>();
        yPointsToAverage.add(y);
        switch (yPadding) {
        case NO_PADDING:
        case HIGH:
            yPointsToAverage.add(y + 1);
            break;
        case LOW:
        case BOTH:
            yPointsToAverage.add(y - 1);
            break;
        }

        int origXSize = positions.getXSize();
        int origYSize = positions.getYSize();

        double meanX = 0.0;
        double meanY = 0.0;
        double size = xPointsToAverage.size() * yPointsToAverage.size();
        for (int xPoint : xPointsToAverage) {
            for (int yPoint : yPointsToAverage) {
                HorizontalPosition centre;
                if (xPoint == -1) {
                    HorizontalPosition cornerPos;
                    HorizontalPosition innerPos;
                    if (yPoint == -1) {
                        cornerPos = positions.get(0, 0);
                        innerPos = positions.get(1, 1);
                    } else if (yPoint == origYSize) {
                        cornerPos = positions.get(origYSize - 1, 0);
                        innerPos = positions.get(origYSize - 2, 1);
                    } else {
                        cornerPos = positions.get(yPoint, 0);
                        innerPos = positions.get(yPoint, 1);
                    }
                    centre = findOuterPoint(cornerPos, innerPos);
                } else if (xPoint == origXSize) {
                    HorizontalPosition cornerPos;
                    HorizontalPosition innerPos;
                    if (yPoint == -1) {
                        cornerPos = positions.get(0, origXSize - 1);
                        innerPos = positions.get(1, origXSize - 2);
                    } else if (yPoint == origYSize) {
                        cornerPos = positions.get(origYSize - 1, origXSize - 1);
                        innerPos = positions.get(origYSize - 2, origXSize - 2);
                    } else {
                        cornerPos = positions.get(yPoint, origXSize - 1);
                        innerPos = positions.get(yPoint, origXSize - 2);
                    }
                    centre = findOuterPoint(cornerPos, innerPos);
                } else {
                    if (yPoint == -1) {
                        HorizontalPosition cornerPos = positions.get(0, xPoint);
                        HorizontalPosition innerPos = positions.get(1, xPoint);
                        centre = findOuterPoint(cornerPos, innerPos);
                    } else if (yPoint == origYSize) {
                        HorizontalPosition cornerPos = positions.get(origYSize - 1, xPoint);
                        HorizontalPosition innerPos = positions.get(origYSize - 2, xPoint);
                        centre = findOuterPoint(cornerPos, innerPos);
                    } else {
                        centre = positions.get(yPoint, xPoint);
                    }
                }
                meanX += centre.getX() / size;
                meanY += centre.getY() / size;
            }
        }
        return new HorizontalPosition(meanX, meanY, crs);
    }

    /**
     * Helper method. This finds a position outside of grid by extrapolating
     * from the edge point and the first inner point
     * 
     * @param point
     *            The {@link HorizontalPosition} of the edge point
     * @param innerPoint
     *            The {@link HorizontalPosition} of the first inner point
     * @return The {@link HorizontalPosition} of an outer point
     */
    private HorizontalPosition findOuterPoint(HorizontalPosition point,
            HorizontalPosition innerPoint) {
        double x = 2 * point.getX() - innerPoint.getX();
        double y = 2 * point.getY() - innerPoint.getY();
        return new HorizontalPosition(x, y, point.getCoordinateReferenceSystem());
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        return bbox.contains(position);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return bbox;
    }

    @Override
    public GeographicBoundingBox getGeographicBoundingBox() {
        return GISUtils.toGeographicBoundingBox(bbox);
    }

    @Override
    public Array2D<GridCell2D> getDomainObjects() {
        return domainObjects;
    }

    @Override
    public long size() {
        return (long) xSize * (long) ySize;
    }

    @Override
    public int getXSize() {
        return xSize;
    }

    @Override
    public int getYSize() {
        return ySize;
    }

    @Override
    public HorizontalGrid getOriginalGrid() {
        return grid;
    }

    @Override
    public SGridPadding getXPadding() {
        return xPadding;
    }

    @Override
    public SGridPadding getYPadding() {
        return yPadding;
    }

    @Override
    public GridCoordinates2D findIndexOf(HorizontalPosition position) {
        /*
         * First, check the bounding box to see if it's in this grid at all.
         * 
         * Then find the index in the parent grid.
         * 
         * If the corresponding cell in the staggered grid contains it, great!
         * 
         * Otherwise we can check the appropriate nearest neighbours.
         * 
         * If we still haven't found it, it must be in one of the edge
         * extensions to the grid.
         * 
         * Check each edge cell to see which one contains it.
         */
        if (!bbox.contains(position)) {
            /*
             * Not in this grid
             */
            return null;
        }

        GridCoordinates2D testIndex = grid.findIndexOf(position);
        if (testIndex != null) {
            GridCell2D testCell;
            if (testIndex.getX() >= 0 && testIndex.getX() < domainObjects.getXSize()
                    && testIndex.getY() >= 0 && testIndex.getY() < domainObjects.getYSize()) {
                testCell = domainObjects.get(testIndex.getY(), testIndex.getX());
                if (testCell.contains(position)) {
                    /*
                     * Same cell index as the original grid.
                     */
                    return testIndex;
                }
            }

            /*
             * In the original grid, but in a differently-numbered cell. Let's
             * check the (maximum 3) nearest neighbours
             */
            Integer xToTest = null;
            switch (xPadding) {
            case NO_PADDING:
            case HIGH:
                xToTest = testIndex.getX() - 1;
                break;
            case BOTH:
            case LOW:
                xToTest = testIndex.getX() + 1;
                break;
            default:
                break;
            }
            if (xToTest != null && (xToTest < 0 || xToTest >= domainObjects.getXSize())) {
                xToTest = null;
            }

            Integer yToTest = null;
            switch (yPadding) {
            case NO_PADDING:
            case HIGH:
                yToTest = testIndex.getY() - 1;
                break;
            case BOTH:
            case LOW:
                yToTest = testIndex.getY() + 1;
                break;
            default:
                break;
            }
            if (yToTest != null && (yToTest < 0 || yToTest >= domainObjects.getYSize())) {
                yToTest = null;
            }

            if (xToTest != null) {
                testCell = domainObjects.get(testIndex.getY(), xToTest);
                if (testCell.contains(position)) {
                    return new GridCoordinates2D(xToTest, testIndex.getY());
                }
                if (yToTest != null) {
                    testCell = domainObjects.get(yToTest, xToTest);
                    if (testCell.contains(position)) {
                        return new GridCoordinates2D(xToTest, yToTest);
                    }
                    testCell = domainObjects.get(yToTest, testIndex.getX());
                    if (testCell.contains(position)) {
                        return new GridCoordinates2D(testIndex.getX(), yToTest);
                    }
                }
            } else if (yToTest != null) {
                testCell = domainObjects.get(yToTest, testIndex.getX());
                if (testCell.contains(position)) {
                    return new GridCoordinates2D(testIndex.getX(), yToTest);
                }
            }
        } else {
            /*
             * We are outside of the original grid, but in this one. So we need
             * to check all of the appropriate outer cells
             */
            if (xPadding == SGridPadding.LOW || xPadding == SGridPadding.BOTH) {
                /*
                 * Scan the lower x cells
                 */
                for (int y = 0; y < domainObjects.getYSize(); y++) {
                    if (domainObjects.get(y, 0).contains(position)) {
                        return new GridCoordinates2D(0, y);
                    }
                }
            }
            if (xPadding == SGridPadding.HIGH || xPadding == SGridPadding.BOTH) {
                /*
                 * Scan the upper x cells
                 */
                for (int y = 0; y < domainObjects.getYSize(); y++) {
                    if (domainObjects.get(y, xSize - 1).contains(position)) {
                        return new GridCoordinates2D(xSize - 1, y);
                    }
                }
            }
            if (yPadding == SGridPadding.LOW || yPadding == SGridPadding.BOTH) {
                /*
                 * Scan the lower y cells
                 */
                for (int x = 0; x < domainObjects.getXSize(); x++) {
                    if (domainObjects.get(0, x).contains(position)) {
                        return new GridCoordinates2D(x, 0);
                    }
                }
            }
            if (yPadding == SGridPadding.HIGH || yPadding == SGridPadding.BOTH) {
                /*
                 * Scan the upper y cells
                 */
                for (int x = 0; x < domainObjects.getXSize(); x++) {
                    if (domainObjects.get(ySize - 1, x).contains(position)) {
                        return new GridCoordinates2D(x, ySize - 1);
                    }
                }
            }
        }
        /*
         * We only get here when the cell says it does not contain the position,
         * but the bounding box does. This can happen because the
         * GridCell2D.getFootprint().contains() method doesn't always include
         * points on the boundary.
         */
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((bbox == null) ? 0 : bbox.hashCode());
        result = prime * result + ((domainObjects == null) ? 0 : domainObjects.hashCode());
        result = prime * result + ((grid == null) ? 0 : grid.hashCode());
        result = prime * result + ((xPadding == null) ? 0 : xPadding.hashCode());
        result = prime * result + xSize;
        result = prime * result + ((yPadding == null) ? 0 : yPadding.hashCode());
        result = prime * result + ySize;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        DerivedStaggeredGrid other = (DerivedStaggeredGrid) obj;
        if (bbox == null) {
            if (other.bbox != null)
                return false;
        } else if (!bbox.equals(other.bbox))
            return false;
        if (domainObjects == null) {
            if (other.domainObjects != null)
                return false;
        } else if (!domainObjects.equals(other.domainObjects))
            return false;
        if (grid == null) {
            if (other.grid != null)
                return false;
        } else if (!grid.equals(other.grid))
            return false;
        if (xPadding != other.xPadding)
            return false;
        if (xSize != other.xSize)
            return false;
        if (yPadding != other.yPadding)
            return false;
        if (ySize != other.ySize)
            return false;
        return true;
    }

    private class GridCellArray2D extends Array2D<GridCell2D> implements Serializable{
        private static final long serialVersionUID = 1L;
        private GridCell2D[][] data;

        public GridCellArray2D(int ySize, int xSize) {
            super(ySize, xSize);
            data = new GridCell2D[ySize][xSize];
        }

        @Override
        public GridCell2D get(int... coords) {
            return data[coords[Y_IND]][coords[X_IND]];
        }

        @Override
        public void set(GridCell2D value, int... coords) {
            data[coords[Y_IND]][coords[X_IND]] = value;
        }
    }

//    public static void main(String[] args) throws IOException {
//        CoordinateReferenceSystem crs = GISUtils.getCrs("EPSG:32661");
//        RegularGridImpl grid = new RegularGridImpl(100, 100, 900, 900, crs, 10, 10);
//        DerivedStaggeredGrid sgrid = new DerivedStaggeredGrid(grid, SGridPadding.NO_PADDING,
//                SGridPadding.NO_OFFSET);
//        BufferedImage image = plotGrids(grid, sgrid);
//        ImageIO.write(image, "png", new File("/home/guy/grids.png"));
//    }

    /**
     * This was useful during testing. It plots the original grid and the
     * staggered grid on top of one another. It also checks which cell on the
     * staggered grid each pixel falls into and colours it accordingly.
     */
    @SuppressWarnings("unused")
    private static BufferedImage plotGrids(HorizontalGrid grid, HorizontalGrid sgrid) {
        CoordinateReferenceSystem crs = grid.getCoordinateReferenceSystem();
        Array2D<GridCell2D> gridObjs = grid.getDomainObjects();

        Array2D<GridCell2D> staggeredObjs = sgrid.getDomainObjects();

        BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                GridCoordinates2D index = sgrid.findIndexOf(new HorizontalPosition(x, y, crs));
                if (index != null) {
                    int col = index.getX() + index.getY();
                    col *= 20;
                    col %= 230;
                    col += 23;
                    Color color = new Color(0, col, col, 255);
                    image.setRGB(x, y, color.getRGB());
                }
            }
        }

        for (int x = 0; x < grid.getXSize(); x++) {
            for (int y = 0; y < grid.getYSize(); y++) {
                g.setColor(Color.black);
                GridCell2D cell = gridObjs.get(y, x);
                g.fillOval((int) cell.getCentre().getX() - 5, (int) cell.getCentre().getY() - 5,
                        10, 10);
                g.drawString(x + "," + y, (int) cell.getCentre().getX(), (int) cell.getCentre()
                        .getY());

                List<HorizontalPosition> vertices = cell.getFootprint().getVertices();
                for (int i = 0; i < vertices.size() - 1; i++) {
                    HorizontalPosition v1 = vertices.get(i);
                    HorizontalPosition v2 = vertices.get(i + 1);
                    g.drawLine((int) v1.getX(), (int) v1.getY(), (int) v2.getX(), (int) v2.getY());
                }
                HorizontalPosition v1 = vertices.get(vertices.size() - 1);
                HorizontalPosition v2 = vertices.get(0);
                g.drawLine((int) v1.getX(), (int) v1.getY(), (int) v2.getX(), (int) v2.getY());
            }
        }
        for (int x = 0; x < sgrid.getXSize(); x++) {
            for (int y = 0; y < sgrid.getYSize(); y++) {
                g.setColor(Color.red);
                GridCell2D staggeredCell = null;
                staggeredCell = staggeredObjs.get(y, x);
                g.fillOval((int) staggeredCell.getCentre().getX() - 2, (int) staggeredCell
                        .getCentre().getY() - 2, 4, 4);
                g.drawString(x + "," + y, (int) staggeredCell.getCentre().getX(),
                        (int) staggeredCell.getCentre().getY());

                List<HorizontalPosition> vertices = staggeredCell.getFootprint().getVertices();
                for (int i = 0; i < vertices.size() - 1; i++) {
                    HorizontalPosition v1 = vertices.get(i);
                    HorizontalPosition v2 = vertices.get(i + 1);
                    g.drawLine((int) v1.getX(), (int) v1.getY(), (int) v2.getX(), (int) v2.getY());
                }
                HorizontalPosition v1 = vertices.get(vertices.size() - 1);
                HorizontalPosition v2 = vertices.get(0);
                g.drawLine((int) v1.getX(), (int) v1.getY(), (int) v2.getX(), (int) v2.getY());
            }
        }
        return image;
    }
}
