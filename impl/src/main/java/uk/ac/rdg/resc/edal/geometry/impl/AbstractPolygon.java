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
