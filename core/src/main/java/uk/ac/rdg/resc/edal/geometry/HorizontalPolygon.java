/*
 * Copyright (c) 2010 The University of Reading
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

package uk.ac.rdg.resc.edal.geometry;

import java.util.List;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * A polygon in the horizontal plane, defined by a list of vertices in a given
 * coordinate reference system.
 * @author Jon
 * @todo add a contains() method?
 */
public interface HorizontalPolygon {

    /**
     * Returns the two-dimensional horizontal coordinate reference system to
     * which the {@link #getVertices() vertices} are referenced.
     * @return the two-dimensional horizontal coordinate reference system to
     * which the vertices are referenced.
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem();

    /**
     * Returns the list of vertices that define this polygon in the horizontal
     * plane.  The coordinates of the vertices are defined in this object's
     * {@link #getCoordinateReferenceSystem() coordinate reference system}.  The
     * {@link HorizontalPosition}s may have a null CRS or may have the same CRS
     * as this object, but they may not have a non-null CRS that is different from
     * that of this object.
     * @return the list of vertices that define this polygon in the horizontal
     * plane.
     * @todo define whether the polygon is closed, and whether there is a particular
     * order (clockwise or anticlockwise) to the vertices.
     */
    public List<HorizontalPosition> getVertices();

}
