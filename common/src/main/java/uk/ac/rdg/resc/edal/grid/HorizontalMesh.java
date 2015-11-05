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
import uk.ac.rdg.resc.edal.geometry.AbstractPolygon;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.Polygon;
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
    private Polygon boundary;

    /**
     * Create a new {@link HorizontalMesh}
     * 
     * @param positions
     *            A {@link List} of {@link HorizontalPosition}s which make up
     *            the vertices of this mesh
     * @param connections
     *            A {@link List} of arrays of <code>int</code>s which define the
     *            connections between the vertices. Each element of the list
     *            should consist of 3 integers - these are the indices of the
     *            points in the {@link HorizontalPosition} {@link List} which
     *            make up each cell of the grid.
     */
    public HorizontalMesh(List<HorizontalPosition> positions, List<int[]> connections) {
        this.positions = positions;
        this.bbox = GISUtils.getBoundingBox(positions);
        /*
         * Calculate the boundary of the grid.
         * 
         * Generate a list of edges and how many times they appear in the grid.
         * By treating 2 edges as equal if they contain the same 2 vertices, we
         * ensure that edges which only appear once are the boundary of the
         * grid.
         */
        Map<Edge, Integer> edgeOccurences = new HashMap<>();
        for (int[] connection : connections) {
//            for(int i=0;i<connection.length-1;i++) {
//                Edge e = new Edge(connection[i], connection[i+1]);
//                if(!edgeOccurences.containsKey(e)) {
//                    edgeOccurences.put(e, 1);
//                } else {
//                    edgeOccurences.put(e, edgeOccurences.get(e)+1);
//                }
//            }
            Edge e1 = new Edge(connection[0], connection[1]);
            Edge e2 = new Edge(connection[1], connection[2]);
            Edge e3 = new Edge(connection[2], connection[0]);
            if (!edgeOccurences.containsKey(e1)) {
                edgeOccurences.put(e1, 1);
            } else {
                edgeOccurences.put(e1, edgeOccurences.get(e1) + 1);
            }
            if (!edgeOccurences.containsKey(e2)) {
                edgeOccurences.put(e2, 1);
            } else {
                edgeOccurences.put(e2, edgeOccurences.get(e2) + 1);
            }
            if (!edgeOccurences.containsKey(e3)) {
                edgeOccurences.put(e3, 1);
            } else {
                edgeOccurences.put(e3, edgeOccurences.get(e3) + 1);
            }
        }
        /*
         * Find the edges which only occur once
         */
        List<Edge> boundaryEdges = new ArrayList<>();
        for (Entry<Edge, Integer> entry : edgeOccurences.entrySet()) {
            if (entry.getValue() == 1) {
                boundaryEdges.add(entry.getKey());
            }
        }

        /*
         * Pick a vertex to start at, and then walk along the edges until the
         * boundary is fully defined
         */
        final List<HorizontalPosition> orderedVertices = new ArrayList<>();
        Edge currentEdge = boundaryEdges.remove(0);
        boolean onI1 = true;
        while (boundaryEdges.size() > 0) {
            int searchVertexIndex;
            if (onI1) {
                orderedVertices.add(positions.get(currentEdge.i1));
                searchVertexIndex = currentEdge.i2;
            } else {
                orderedVertices.add(positions.get(currentEdge.i2));
                searchVertexIndex = currentEdge.i1;
            }

            for (Edge testEdge : boundaryEdges) {
                if (testEdge.i1 == searchVertexIndex) {
                    onI1 = true;
                    currentEdge = testEdge;
                    break;
                }
                if (testEdge.i2 == searchVertexIndex) {
                    onI1 = false;
                    currentEdge = testEdge;
                    break;
                }
            }
            boundaryEdges.remove(currentEdge);

        }

        /*
         * Create the boundary
         */
        this.boundary = new AbstractPolygon() {
            @Override
            public List<HorizontalPosition> getVertices() {
                return orderedVertices;
            }

            @Override
            public CoordinateReferenceSystem getCoordinateReferenceSystem() {
                return bbox.getCoordinateReferenceSystem();
            }
        };
    }

    @Override
    public Array<HorizontalCell> getDomainObjects() {
        return new AbstractImmutableArray<HorizontalCell>(positions.size()) {
            @Override
            public HorizontalCell get(final int... coords) {
                return new HorizontalCell() {
                    @Override
                    public boolean contains(HorizontalPosition position) {
                        throw new UnsupportedOperationException(
                                "Not yet implmented footprints for unstructured grids");
                    }

                    @Override
                    public DiscreteHorizontalDomain<? extends HorizontalCell> getParentDomain() {
                        return HorizontalMesh.this;
                    }

                    @Override
                    public Polygon getFootprint() {
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
        return boundary.contains(position);
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
        if (!boundary.contains(position)) {
            return -1;
        }
        int index = -1;
        double minDistSquared = Double.MAX_VALUE;
        int i = 0;
        for (HorizontalPosition pos : positions) {
            double distSquared = GISUtils.getDistSquared(pos, position);
            if (distSquared < minDistSquared) {
                minDistSquared = distSquared;
                index = i;
            }
            i++;
        }
        return index;
    }

    /**
     * Definition of an edge between 2 vertices.
     *
     * @author Guy Griffiths
     */
    private class Edge {
        int i1, i2;

        public Edge(int i1, int i2) {
            super();
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
            result = prime * result + getOuterType().hashCode();
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
            Edge other = (Edge) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (i1 != other.i1)
                return false;
            if (i2 != other.i2)
                return false;
            return true;
        }

        private HorizontalMesh getOuterType() {
            return HorizontalMesh.this;
        }
    }
}
