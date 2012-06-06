package uk.ac.rdg.resc.edal.geometry.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.impl.HorizontalPositionImpl;

/**
 * Immutable implementation of a {@link BoundingBox}.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public final class BoundingBoxImpl extends AbstractEnvelope implements BoundingBox
{
    private final double minx;
    private final double miny;
    private final double maxx;
    private final double maxy;

    public BoundingBoxImpl(Envelope envelope2d) {
        super(envelope2d.getCoordinateReferenceSystem());
        if (envelope2d.getDimension() != 2) {
            throw new IllegalArgumentException("Envelope dimension must be 2");
        }
        this.minx = envelope2d.getMinimum(0);
        this.maxx = envelope2d.getMaximum(0);
        this.miny = envelope2d.getMinimum(1);
        this.maxy = envelope2d.getMaximum(1);
    }

    public BoundingBoxImpl(Extent<Double> xExtent, Extent<Double> yExtent, CoordinateReferenceSystem crs) {
        super(crs);

        this.minx = xExtent.getLow();
        this.maxx = xExtent.getHigh();
        this.miny = yExtent.getLow();
        this.maxy = yExtent.getHigh();
    }

    /** Constructs a BoundingBox with a null coordinate reference system */
    public BoundingBoxImpl(Extent<Double> xExtent, Extent<Double> yExtent) {
        this(xExtent, yExtent, null);
    }

    /** Creates a BoundingBox from a four-element array [minx, miny, maxx, maxy] */
    public BoundingBoxImpl(double[] bbox, CoordinateReferenceSystem crs) {
        super(crs);
        if (bbox == null)
            throw new NullPointerException();
        if (bbox.length != 4)
            throw new IllegalArgumentException("Bounding box" + " must have four elements");
        this.minx = bbox[0];
        this.maxx = bbox[2];
        this.miny = bbox[1];
        this.maxy = bbox[3];
        // Check the bounds of the bbox
        if (this.minx > this.maxx || this.miny > this.maxy) {
            throw new IllegalArgumentException("Invalid bounding box specification");
        }
    }
    
    /** Creates a BoundingBox */
    public BoundingBoxImpl(HorizontalPosition lowerCorner, HorizontalPosition upperCorner) {
        // TODO: check that CRSs are equal
        this(lowerCorner.getX(), lowerCorner.getY(), upperCorner.getX(),
                upperCorner.getY(), lowerCorner.getCoordinateReferenceSystem());
    }
    
    /** Creates a BoundingBox */
    public BoundingBoxImpl(double minx, double miny, double maxx, double maxy, CoordinateReferenceSystem crs) {
        super(crs);
        this.minx = minx;
        this.maxx = maxx;
        this.miny = miny;
        this.maxy = maxy;
        // Check the bounds of the bbox
        if (this.minx > this.maxx || this.miny > this.maxy) {
            throw new IllegalArgumentException("Invalid bounding box specification");
        }
    }

    /**
     * Creates a BoundingBox from a four-element array [minx, miny, maxx, maxy]
     * with an unknown (null) CRS
     */
    public BoundingBoxImpl(double[] bbox) {
        this(bbox, null);
    }

    @Override
    public int getDimension() {
        return 2;
    }

    @Override
    public double getMinX() {
        return this.minx;
    }

    @Override
    public double getMaxX() {
        return this.maxx;
    }

    @Override
    public double getMinY() {
        return this.miny;
    }

    @Override
    public double getMaxY() {
        return this.maxy;
    }

    @Override
    public double getMinimum(int i) {
        if (i == 0)
            return this.minx;
        if (i == 1)
            return this.miny;
        throw new IndexOutOfBoundsException();
    }

    @Override
    public double getMaximum(int i) {
        if (i == 0)
            return this.maxx;
        if (i == 1)
            return this.maxy;
        throw new IndexOutOfBoundsException();
    }    
    
    @Override
    public double getWidth()
    {
        return maxx - minx;
    }
    
    @Override
    public double getHeight()
    {
        return maxy - miny;
    }

    @Override
    public HorizontalPosition getLowerCorner() {
        return new HorizontalPositionImpl(this.minx, this.miny, this.crs);
    }

    @Override
    public HorizontalPosition getUpperCorner() {
        return new HorizontalPositionImpl(this.maxx, this.maxy, this.crs);
    }

    @Override
    public String toString() {
        return String.format("%f, %f - %f, %f", this.minx, this.miny, this.maxx, this.maxy);
    }

    @Override
    public List<HorizontalPosition> getVertices() {
        List<HorizontalPosition> positions = new ArrayList<HorizontalPosition>();
        positions.add(new HorizontalPositionImpl(minx, miny, crs));
        positions.add(new HorizontalPositionImpl(maxx, miny, crs));
        positions.add(new HorizontalPositionImpl(maxx, maxy, crs));
        positions.add(new HorizontalPositionImpl(minx, maxy, crs));
        return Collections.unmodifiableList(positions);
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        // TODO: deal with coordinate translation
        if (!equalsWithNull(this.getCoordinateReferenceSystem(), position.getCoordinateReferenceSystem()))
        {
            throw new UnsupportedOperationException("Cannot yet perform contains()"
                + " on position with a different CRS from the bounding box");
        }
        double x = position.getX();
        double y = position.getY();
        return (x >= minx && x <= maxx && y >= miny && y <= maxy);
    }
    
    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + new Double(minx).hashCode();
        hash = hash * 31 + new Double(miny).hashCode();
        hash = hash * 31 + new Double(maxx).hashCode();
        hash = hash * 31 + new Double(maxy).hashCode();
        hash = hash * 31 + (this.crs == null ? this.crs.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof BoundingBoxImpl)) return false;
        BoundingBoxImpl otherBbox = (BoundingBoxImpl)other;
        return equalsDouble(minx, otherBbox.minx) &&
               equalsDouble(miny, otherBbox.miny) &&
               equalsDouble(maxx, otherBbox.maxx) &&
               equalsDouble(maxy, otherBbox.maxy) &&
               equalsWithNull(crs, otherBbox.crs);
    }
    
    private static boolean equalsDouble(double d1, double d2)
    {
        // See the docs for Double.equals() to explain why we don't just do
        // d1 == d2.
        return Double.doubleToLongBits(d1) == Double.doubleToLongBits(d2);
    }
    
    private static boolean equalsWithNull(Object o1, Object o2)
    {
        return o1 == null ? o2 == null : o1.equals(o2);
    }
}