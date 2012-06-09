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
public final class BoundingBoxImpl extends AbstractPolygon implements BoundingBox
{
    private final double minx;
    private final double miny;
    private final double maxx;
    private final double maxy;
    private final CoordinateReferenceSystem crs;

    public BoundingBoxImpl(Envelope envelope2d) {
        if (envelope2d.getDimension() != 2) {
            throw new IllegalArgumentException("Envelope dimension must be 2");
        }
        this.crs = envelope2d.getCoordinateReferenceSystem();
        this.minx = envelope2d.getMinimum(0);
        this.maxx = envelope2d.getMaximum(0);
        this.miny = envelope2d.getMinimum(1);
        this.maxy = envelope2d.getMaximum(1);
    }

    public BoundingBoxImpl(Extent<Double> xExtent, Extent<Double> yExtent, CoordinateReferenceSystem crs) {
        this.minx = xExtent.getLow();
        this.maxx = xExtent.getHigh();
        this.miny = yExtent.getLow();
        this.maxy = yExtent.getHigh();
        this.crs = crs;
    }

    /** Constructs a BoundingBox with a null coordinate reference system */
    public BoundingBoxImpl(Extent<Double> xExtent, Extent<Double> yExtent) {
        this(xExtent, yExtent, null);
    }

    /** Creates a BoundingBox from a four-element array [minx, miny, maxx, maxy] */
    public BoundingBoxImpl(double[] bbox, CoordinateReferenceSystem crs) {
        if (bbox == null)
            throw new NullPointerException("Bounding box cannot be null");
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
        this.crs = crs;
    }
    
    /** Creates a BoundingBox */
    public BoundingBoxImpl(HorizontalPosition lowerCorner, HorizontalPosition upperCorner) {
        // TODO: check that CRSs are equal
        this(lowerCorner.getX(), lowerCorner.getY(), upperCorner.getX(),
                upperCorner.getY(), lowerCorner.getCoordinateReferenceSystem());
    }
    
    /** Creates a BoundingBox */
    public BoundingBoxImpl(double minx, double miny, double maxx, double maxy, CoordinateReferenceSystem crs) {
        this.minx = minx;
        this.maxx = maxx;
        this.miny = miny;
        this.maxy = maxy;
        // Check the bounds of the bbox
        if (this.minx > this.maxx || this.miny > this.maxy) {
            throw new IllegalArgumentException("Invalid bounding box specification");
        }
        this.crs = crs;
    }

    /**
     * Creates a BoundingBox from a four-element array [minx, miny, maxx, maxy]
     * with an unknown (null) CRS
     */
    public BoundingBoxImpl(double[] bbox) {
        this(bbox, null);
    }
    
    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return this.crs;
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

    /**
     * Returns a list of vertices in anticlockwise order starting at minx, miny
     */
    @Override
    public List<HorizontalPosition> getVertices() {
        List<HorizontalPosition> positions = new ArrayList<HorizontalPosition>();
        positions.add(new HorizontalPositionImpl(minx, miny, crs));
        positions.add(new HorizontalPositionImpl(maxx, miny, crs));
        positions.add(new HorizontalPositionImpl(maxx, maxy, crs));
        positions.add(new HorizontalPositionImpl(minx, maxy, crs));
        return Collections.unmodifiableList(positions);
    }

    /**
     * Provides a more efficient contains() method than the one in AbstractPolygon
     */
    @Override
    protected boolean contains(double x, double y) {
        return (x >= minx && x <= maxx && y >= miny && y <= maxy);
    }
    
    
    ///// OVERRIDES FROM ENVELOPE INTERFACE /////

    @Override
    public final double getMedian(int i) {
        return (this.getMinimum(i) + this.getMaximum(i)) / 2.0;
    }

    @Override
    public final double getSpan(int i) {
        return this.getMaximum(i) - this.getMinimum(i);
    }
}