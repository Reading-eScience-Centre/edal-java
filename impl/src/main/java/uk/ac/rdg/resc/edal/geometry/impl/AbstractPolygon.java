/*******************************************************************************
 * Copyright (c) 2012 The University of Reading
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

package uk.ac.rdg.resc.edal.geometry.impl;

import java.awt.geom.Path2D;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.geometry.Polygon;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Partial implementation of a {@link Polygon}, providing default
 * implementations of the {@code contains()}, {@code hashCode()} and
 * {@code equals()} methods.
 * @author Jon
 */
public abstract class AbstractPolygon implements Polygon
{
    
    @Override
    public final boolean contains(HorizontalPosition pos)
    {
        // Convert the position to the CRS of this polygon
        pos = GISUtils.transformPosition(pos, this.getCoordinateReferenceSystem());
        return this.contains(pos.getX(), pos.getY());
    }
    
    /**
     * {@inheritDoc}
     * <p>This default implementation constructs a {@link Path2D} consisting of
     * the vertices of the polygon, and uses this to test for containment.
     * Subclasses may be able to override with a more efficient method.</p>
     */
    public boolean contains(double x, double y)
    {
        Path2D path = this.getBoundaryPath();
        return path.contains(x, y);
    }
    
    private Path2D getBoundaryPath()
    {
        Path2D path = new Path2D.Double();
        boolean firstTime = true;
        for (HorizontalPosition pos : this.getVertices()) {
            // Add the point to the path
            if (firstTime)
                path.moveTo(pos.getX(), pos.getY());
            else
                path.lineTo(pos.getX(), pos.getY());
            firstTime = false;
        }
        path.closePath();
        return path;
    }
    
    @Override
    public int hashCode()
    {
        int hash = this.getVertices().hashCode();
        CoordinateReferenceSystem crs = this.getCoordinateReferenceSystem();
        if (crs != null) {
            hash += crs.hashCode();
        }
        return hash;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (!(obj instanceof Polygon)) return false;
        Polygon other = (Polygon)obj;
        return this.getVertices().equals(other.getVertices());
    }
}
