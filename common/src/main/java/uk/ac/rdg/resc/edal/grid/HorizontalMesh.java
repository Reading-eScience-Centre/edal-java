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

package uk.ac.rdg.resc.edal.grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.domain.DiscreteHorizontalDomain;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.Polygon;
import uk.ac.rdg.resc.edal.geometry.SimplePolygon;
import uk.ac.rdg.resc.edal.grid.kdtree.KDTree;
import uk.ac.rdg.resc.edal.grid.kdtree.Point;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.AbstractImmutableArray;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * An unstructured mesh in the horizontal plane.
 * 
 * @author Guy Griffiths
 */
public class HorizontalMesh implements DiscreteHorizontalDomain<HorizontalCell> {
    private final List<HorizontalPosition> positions;
    private final BoundingBox bbox;
    private List<NestedBoundary> topLevelBoundaries;
    private KDTree kdTree;
    private List<Polygon> cellBounds;

    /**
     * Create a new {@link HorizontalMesh}
     * 
     * @param positions
     *            A {@link List} of {@link HorizontalPosition}s which make up
     *            the vertices of this mesh
     * @param connections
     *            A {@link List} of arrays of <code>int</code>s which define the
     *            connections between the vertices. Each element of the list
     *            should consist of at least 3 integers - these are the indices
     *            of the points in the {@link HorizontalPosition} {@link List}
     *            which make up each cell of the grid.
     * @param connectionsStartFrom
     *            In the list of connections, what number refers to the first
     *            vertex in the positions list? This is almost always either 0
     *            or 1
     */
    public static HorizontalMesh fromConnections(List<HorizontalPosition> positions,
            List<int[]> connections, int connectionsStartFrom) {
        HorizontalMesh mesh = new HorizontalMesh(positions);

        /*
         * Calculate the boundary of the grid.
         * 
         * Generate a list of edges and how many times they appear in the grid.
         * By treating 2 edges as equal if they contain the same 2 vertices, we
         * ensure that edges which only appear once are the boundary of the
         * grid.
         */
        Map<IndexEdge, Integer> edgeOccurences = new HashMap<>();
        for (int[] connection : connections) {
            for (int e = 0; e < connection.length - 1; e++) {
                IndexEdge edge = new IndexEdge(connection[e], connection[e + 1]);
                if (!edgeOccurences.containsKey(edge)) {
                    edgeOccurences.put(edge, 1);
                } else {
                    edgeOccurences.put(edge, edgeOccurences.get(edge) + 1);
                }
            }
            IndexEdge edge = new IndexEdge(connection[connection.length - 1], connection[0]);
            if (!edgeOccurences.containsKey(edge)) {
                edgeOccurences.put(edge, 1);
            } else {
                edgeOccurences.put(edge, edgeOccurences.get(edge) + 1);
            }
        }
        /*
         * Find the edges which only occur once
         */
        List<IndexEdge> boundaryEdges = new ArrayList<>();
        for (Entry<IndexEdge, Integer> entry : edgeOccurences.entrySet()) {
            if (entry.getValue() == 1) {
                boundaryEdges.add(entry.getKey());
            }
        }

        /*
         * We can now construct the boundaries of the mesh. There will be
         * multiple boundaries if:
         * 
         * We have separate distinct areas in the mesh
         * 
         * We have cut-outs of the mesh (possibly with other meshes inside...)
         * 
         * We first construct a list of boundaries and then process them to get
         * a list of NestedBoundarys which can be used to determine whether a
         * point is truly within the mesh.
         */
        List<NestedBoundary> meshBoundaries = new ArrayList<>();
        /*
         * We remove edges from the list once a complete boundary has been
         * found. Eventually we will have removed them all.
         */
        while (boundaryEdges.size() > 0) {
            /*
             * Pick a vertex to start at, and then walk along the edges until
             * the boundary is fully defined
             */
            final List<HorizontalPosition> orderedVertices = new ArrayList<>();
            IndexEdge currentEdge = boundaryEdges.remove(0);
            boolean onI1 = true;
            boolean foundEdge = true;
            /*
             * We keep searching the current path until no more edges are found
             */
            while (foundEdge) {
                int searchVertexIndex;
                if (onI1) {
                    orderedVertices.add(positions.get(currentEdge.i1 - connectionsStartFrom));
                    searchVertexIndex = currentEdge.i2;
                } else {
                    orderedVertices.add(positions.get(currentEdge.i2 - connectionsStartFrom));
                    searchVertexIndex = currentEdge.i1;
                }

                /*
                 * By removing edges once they've been used, this loop gets
                 * smaller each time
                 */
                foundEdge = false;
                for (IndexEdge testEdge : boundaryEdges) {
                    if (testEdge.i1 == searchVertexIndex) {
                        onI1 = true;
                        currentEdge = testEdge;
                        foundEdge = true;
                        break;
                    }
                    if (testEdge.i2 == searchVertexIndex) {
                        onI1 = false;
                        currentEdge = testEdge;
                        foundEdge = true;
                        break;
                    }
                }
                boundaryEdges.remove(currentEdge);
            }
            Polygon boundary = new SimplePolygon(orderedVertices);
            meshBoundaries.add(new NestedBoundary(boundary));
        }
        /*
         * meshBoundaries now contains all of the boundaries of this mesh. Now
         * find how (and if) they are nested.
         */
        for (NestedBoundary bound : meshBoundaries) {
            for (NestedBoundary testBound : meshBoundaries) {
                if (!bound.equals(testBound)) {
                    if (boundFullyContains(bound.boundary, testBound.boundary)) {
                        bound.addChildPolygon(testBound);
                    }
                }
            }
        }

        /*
         * Now find the top-level boundaries - i.e. those with no parent bounds
         */
        List<NestedBoundary> hasParent = new ArrayList<>();
        for (NestedBoundary bound : meshBoundaries) {
            for (NestedBoundary childBound : bound.childBounds) {
                hasParent.add(childBound);
            }
        }
        mesh.topLevelBoundaries = new ArrayList<>();
        for (NestedBoundary bound : meshBoundaries) {
            if (!hasParent.contains(bound)) {
                mesh.topLevelBoundaries.add(bound);
            }
        }

        /*
         * Now remove duplicates - at this point parent boundaries will have all
         * the boundaries they contain as children, even those which are also
         * contained by their direct children
         */
        for (NestedBoundary bound : mesh.topLevelBoundaries) {
            removeLowerBounds(bound);
        }
        return mesh;
    }

    /**
     * Remove child {@link NestedBoundary}s which also occur further down the
     * tree. Calls itself recursively.
     * 
     * @param bound
     *            The {@link NestedBoundary} to start checking at.
     */
    private static void removeLowerBounds(NestedBoundary bound) {
        List<NestedBoundary> remove = new ArrayList<>();
        for (NestedBoundary child : bound.childBounds) {
            boolean containedByChild = false;
            for (NestedBoundary grandchild : bound.childBounds) {
                if (grandchild.childBounds.contains(child)) {
                    containedByChild = true;
                    break;
                }
            }
            if (containedByChild) {
                remove.add(child);
            }
        }
        for (NestedBoundary rb : remove) {
            bound.childBounds.remove(rb);
        }

        for (NestedBoundary child : bound.childBounds) {
            removeLowerBounds(child);
        }
    }

    /**
     * Finds whether a {@link Polygon} is entirely contained within another
     * {@link Polygon}
     * 
     * @param container
     *            The {@link Polygon} to test as a container
     * @param candidate
     *            The {@link Polygon} to test as being contained
     * @return <code>true</code> if the canditate is entirely contained within
     *         the container
     */
    private static boolean boundFullyContains(Polygon container, Polygon candidate) {
        for (HorizontalPosition point : candidate.getVertices()) {
            if (!container.contains(point)) {
                return false;
            }
        }
        return true;
    }

    public static HorizontalMesh fromBounds(List<HorizontalPosition> positions,
            List<Polygon> boundaries) {
        HorizontalMesh mesh = new HorizontalMesh(positions);

        Map<HorizontalEdge, Integer> edgeOccurences = new HashMap<>();
        for (Polygon cellBound : boundaries) {
            List<HorizontalPosition> vertices = cellBound.getVertices();
            for (int e = 0; e < vertices.size() - 1; e++) {
                HorizontalEdge edge = new HorizontalEdge(vertices.get(e), vertices.get(e + 1));
                if (!edgeOccurences.containsKey(edge)) {
                    edgeOccurences.put(edge, 1);
                } else {
                    edgeOccurences.put(edge, edgeOccurences.get(edge) + 1);
                }
            }
            HorizontalEdge edge = new HorizontalEdge(vertices.get(vertices.size() - 1),
                    vertices.get(0));
            if (!edgeOccurences.containsKey(edge)) {
                edgeOccurences.put(edge, 1);
            } else {
                edgeOccurences.put(edge, edgeOccurences.get(edge) + 1);
            }
        }
        /*
         * Find the edges which only occur once
         */
        List<HorizontalEdge> boundaryEdges = new ArrayList<>();
        for (Entry<HorizontalEdge, Integer> entry : edgeOccurences.entrySet()) {
            if (entry.getValue() == 1) {
                boundaryEdges.add(entry.getKey());
            }
        }
        /*
         * We can now construct the boundaries of the mesh. There will be
         * multiple boundaries if:
         * 
         * We have separate distinct areas in the mesh
         * 
         * We have cut-outs of the mesh (possibly with other meshes inside...)
         * 
         * We first construct a list of boundaries and then process them to get
         * a list of NestedBoundarys which can be used to determine whether a
         * point is truly within the mesh.
         */
        List<NestedBoundary> meshBoundaries = new ArrayList<>();
        /*
         * We remove edges from the list once a complete boundary has been
         * found. Eventually we will have removed them all.
         */
        while (boundaryEdges.size() > 0) {
            /*
             * Pick a vertex to start at, and then walk along the edges until
             * the boundary is fully defined
             */
            final List<HorizontalPosition> orderedVertices = new ArrayList<>();
            HorizontalEdge currentEdge = boundaryEdges.remove(0);
            boolean onI1 = true;
            boolean foundEdge = true;
            /*
             * We keep searching the current path until no more edges are found
             */
            while (foundEdge) {
                HorizontalPosition searchVertexIndex;
                if (onI1) {
                    orderedVertices.add(currentEdge.p1);
                    searchVertexIndex = currentEdge.p2;
                } else {
                    orderedVertices.add(currentEdge.p2);
                    searchVertexIndex = currentEdge.p1;
                }

                /*
                 * By removing edges once they've been used, this loop gets
                 * smaller each time
                 */
                foundEdge = false;
                for (HorizontalEdge testEdge : boundaryEdges) {
                    if (testEdge.p1.equals(searchVertexIndex)) {
                        onI1 = true;
                        currentEdge = testEdge;
                        foundEdge = true;
                        break;
                    }
                    if (testEdge.p2.equals(searchVertexIndex)) {
                        onI1 = false;
                        currentEdge = testEdge;
                        foundEdge = true;
                        break;
                    }
                }
                boundaryEdges.remove(currentEdge);
            }
            Polygon boundary = new SimplePolygon(orderedVertices);
            meshBoundaries.add(new NestedBoundary(boundary));
        }
        /*
         * meshBoundaries now contains all of the boundaries of this mesh. Now
         * find how (and if) they are nested.
         */
        for (NestedBoundary bound : meshBoundaries) {
            for (NestedBoundary testBound : meshBoundaries) {
                if (!bound.equals(testBound)) {
                    if (boundFullyContains(bound.boundary, testBound.boundary)) {
                        bound.addChildPolygon(testBound);
                    }
                }
            }
        }

        /*
         * Now find the top-level boundaries - i.e. those with no parent bounds
         */
        List<NestedBoundary> hasParent = new ArrayList<>();
        for (NestedBoundary bound : meshBoundaries) {
            for (NestedBoundary childBound : bound.childBounds) {
                hasParent.add(childBound);
            }
        }
        mesh.topLevelBoundaries = new ArrayList<>();
        for (NestedBoundary bound : meshBoundaries) {
            if (!hasParent.contains(bound)) {
                mesh.topLevelBoundaries.add(bound);
            }
        }

        /*
         * Now remove duplicates - at this point parent boundaries will have all
         * the boundaries they contain as children, even those which are also
         * contained by their direct children
         */
        for (NestedBoundary bound : mesh.topLevelBoundaries) {
            removeLowerBounds(bound);
        }

//        /*
//         * The set of boundaries seem to use slightly different values for
//         * coincident edges in the test data I had.
//         * 
//         * This means that there is no way to calculate the actual boundary of
//         * the data.
//         */
//        mesh.topLevelBoundaries = new ArrayList<>();
//        mesh.topLevelBoundaries.add(new NestedBoundary(mesh.bbox));
        mesh.cellBounds = boundaries;
        return mesh;
    }

    private HorizontalMesh(List<HorizontalPosition> positions) {
        this.kdTree = new KDTree(positions);
        this.kdTree.buildTree();
        this.positions = positions;
        this.bbox = GISUtils.getBoundingBox(positions);
    }

    @Override
    public Array<HorizontalCell> getDomainObjects() {
        return new AbstractImmutableArray<HorizontalCell>(positions.size()) {
            @Override
            public HorizontalCell get(final int... coords) {
                return new HorizontalCell() {
                    @Override
                    public boolean contains(HorizontalPosition position) {
                        if (cellBounds != null) {
                            return cellBounds.get(coords[0]).contains(position);
                        }
                        throw new UnsupportedOperationException(
                                "Not yet implemented footprints for unstructured grids");
                    }

                    @Override
                    public DiscreteHorizontalDomain<? extends HorizontalCell> getParentDomain() {
                        return HorizontalMesh.this;
                    }

                    @Override
                    public Polygon getFootprint() {
                        if (cellBounds != null) {
                            return cellBounds.get(coords[0]);
                        }
                        throw new UnsupportedOperationException(
                                "Not yet implmented footprints for unstructured grids");
                    }

                    @Override
                    public HorizontalPosition getCentre() {
                        return positions.get(coords[0]);
                    }
                };
            }
        };
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        for (NestedBoundary boundary : topLevelBoundaries) {
            if (boundary.contains(position)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return bbox;
    }

    @Override
    public GeographicBoundingBox getGeographicBoundingBox() {
        return GISUtils.toGeographicBoundingBox(getBoundingBox());
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return bbox.getCoordinateReferenceSystem();
    }

    @Override
    public long size() {
        return positions.size();
    }

    /**
     * Finds the index of the closest mesh vertex to the supplied position
     * 
     * @param position
     *            The {@link HorizontalPosition} to test
     * @return The index in the original position list, or -1 if the supplied
     *         position is outside the boundary of this {@link HorizontalMesh}
     */
    public int findIndexOf(HorizontalPosition position) {
        if (!bbox.contains(position)) {
            return -1;
        }
        if (!contains(position)) {
            return -1;
        }

        Point nearestNeighbour = kdTree.nearestNeighbour(position);

        int index = nearestNeighbour.getIndex();

        if (cellBounds == null) {
            /*
             * If we have no cell bounds, we just want the nearest neighbour
             * within the mesh bounds
             */
            return index;
        } else {
            /*
             * If we have cell bounds, check to see if the nearest neighbour
             * really contains the position
             */
            if (cellBounds.get(index).contains(position)) {
                return index;
            } else {
                /*
                 * If not, find all cells which are nearby (arbitrarily within a
                 * bounding box which is 5x the size of the MBR of the cell but
                 * centred on the same point)
                 */
                ArrayList<Point> possibles = kdTree.rangeQuery(GISUtils.getLargeBoundingBox(
                        cellBounds.get(index).getBoundingBox(), 500));
                /*
                 * Now check all of these cells to see if the position is
                 * contained in one of them
                 */
                for (Point p : possibles) {
                    int pIndex = p.getIndex();
                    if (cellBounds.get(pIndex).contains(position)) {
                        return pIndex;
                    }
                }
                /*
                 * Position is not in the nearest neighbour cell bounds, or any
                 * of the nearby cells...
                 * 
                 * This is unlikely to happen, but could be the case if there
                 * are gaps between cells.
                 */
                return -1;
            }
        }

//        /*
//         * Linear search method. Not considerably slower than our KDTree
//         * implementation for the datasets I've tried it on.
//         */
//        int index = -1;
//        double minDistSquared = Double.MAX_VALUE;
//        int i = 0;
//        for (HorizontalPosition pos : positions) {
//            double distSquared = GISUtils.getDistSquared(pos, position);
//            if (distSquared < minDistSquared) {
//                minDistSquared = distSquared;
//                index = i;
//            }
//            i++;
//        }
//        return index;
    }

    /**
     * Definition of an edge between 2 vertices.
     *
     * @author Guy Griffiths
     */
    private static class IndexEdge {
        int i1, i2;

        public IndexEdge(int i1, int i2) {
            /*
             * Order the points so that equivalent edges create identical
             * objects
             */
            if (i1 < i2) {
                this.i1 = i1;
                this.i2 = i2;
            } else {
                this.i1 = i2;
                this.i2 = i1;
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + i1;
            result = prime * result + i2;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            IndexEdge other = (IndexEdge) obj;
            if (i1 != other.i1)
                return false;
            if (i2 != other.i2)
                return false;
            return true;
        }

        @Override
        public String toString() {
            return i1 + "\t" + i2;
        }
    }

    /**
     * Definition of an edge between 2 vertices.
     *
     * @author Guy Griffiths
     */
    private static class HorizontalEdge {
        HorizontalPosition p1, p2;

        public HorizontalEdge(HorizontalPosition i1, HorizontalPosition i2) {
            /*
             * Order the points so that equivalent edges create identical
             * objects
             */
            if (i1.getX() < i2.getX() || (i1.getX() == i2.getX() && i1.getY() < i2.getY())) {
                this.p1 = i1;
                this.p2 = i2;
            } else {
                this.p1 = i2;
                this.p2 = i1;
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((p1 == null) ? 0 : p1.hashCode());
            result = prime * result + ((p2 == null) ? 0 : p2.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            HorizontalEdge other = (HorizontalEdge) obj;
            if (p1 == null) {
                if (other.p1 != null)
                    return false;
            } else if (!p1.equals(other.p1))
                return false;
            if (p2 == null) {
                if (other.p2 != null)
                    return false;
            } else if (!p2.equals(other.p2))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return p1.toString() + "\t" + p2.toString();
        }
    }

    private static class NestedBoundary {
        private Polygon boundary;
        private List<NestedBoundary> childBounds = new ArrayList<>();

        public NestedBoundary(Polygon boundary) {
            super();
            this.boundary = boundary;
        }

        public void addChildPolygon(NestedBoundary child) {
            childBounds.add(child);
        }

        public boolean contains(HorizontalPosition position) {
            boolean in = false;
            return recurseContains(position, in);
        }

        private boolean recurseContains(HorizontalPosition position, boolean in) {
            if (boundary.contains(position)) {
                in = !in;
                for (NestedBoundary child : childBounds) {
                    boolean newIn = child.recurseContains(position, in);
                    if (newIn != in) {
                        return newIn;
                    }
                }
            }
            return in;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((boundary == null) ? 0 : boundary.hashCode());
            result = prime * result + ((childBounds == null) ? 0 : childBounds.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            NestedBoundary other = (NestedBoundary) obj;
            if (boundary == null) {
                if (other.boundary != null)
                    return false;
            } else if (!boundary.equals(other.boundary))
                return false;
            if (childBounds == null) {
                if (other.childBounds != null)
                    return false;
            } else if (!childBounds.equals(other.childBounds))
                return false;
            return true;
        }
    }

    /*
     * Useful code for testing this method. It plots out all of the boundaries
     * in an image.
     */
//    private static void plotBounds(HorizontalMesh mesh, File outFile, int width, int height)
//            throws IOException {
//        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//        Graphics2D g = image.createGraphics();
//        g.fillRect(0, 0, width, height);
//        g.setColor(Color.black);
//        RegularGridImpl grid = new RegularGridImpl(mesh.bbox, width, height);
//        for (NestedBoundary bounds : mesh.topLevelBoundaries) {
//            plotBoundsRecursive(bounds, g, grid);
//        }
//        ImageIO.write(image, "png", outFile);
//    }
//
//    private static void plotBoundsRecursive(NestedBoundary bounds, Graphics2D g,
//            RegularGridImpl grid) {
//        List<HorizontalPosition> vertices = bounds.boundary.getVertices();
//        for (int i = 0; i < vertices.size() - 1; i++) {
//            GridCoordinates2D index1 = grid.findIndexOf(vertices.get(i));
//            GridCoordinates2D index2 = grid.findIndexOf(vertices.get(i + 1));
//            if (index1 != null && index2 != null)
//                g.drawLine(index1.getX(), index1.getY(), index2.getX(), index2.getY());
//        }
//        GridCoordinates2D index1 = grid.findIndexOf(vertices.get(0));
//        GridCoordinates2D index2 = grid.findIndexOf(vertices.get(vertices.size() - 1));
//        if (index1 != null && index2 != null)
//            g.drawLine(index1.getX(), index1.getY(), index2.getX(), index2.getY());
//
//        for (BoundingBox bbox : ((SimplePolygon) bounds.boundary).inBounds) {
//            vertices = bbox.getVertices();
//            for (int i = 0; i < vertices.size() - 1; i++) {
//                index1 = grid.findIndexOf(vertices.get(i));
//                index2 = grid.findIndexOf(vertices.get(i + 1));
//                if (index1 != null && index2 != null)
//                    g.drawLine(index1.getX(), index1.getY(), index2.getX(), index2.getY());
//            }
//            index1 = grid.findIndexOf(vertices.get(0));
//            index2 = grid.findIndexOf(vertices.get(vertices.size() - 1));
//            if (index1 != null && index2 != null)
//                g.drawLine(index1.getX(), index1.getY(), index2.getX(), index2.getY());
//        }
//        for (NestedBoundary child : bounds.childBounds) {
//            plotBoundsRecursive(child, g, grid);
//        }
//    }
}
