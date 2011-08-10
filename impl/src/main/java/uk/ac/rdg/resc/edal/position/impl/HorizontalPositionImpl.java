package uk.ac.rdg.resc.edal.position.impl;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * <p>
 * Implementation of {@link HorizontalPosition} that is immutable provided that
 * the provided {@link CoordinateReferenceSystem} is also immutable.
 * </p>
 * <p>
 * Although instances of this class are immutable, instances of subclasses may
 * not be.
 * </p>
 * 
 * @author Jon
 */
public class HorizontalPositionImpl extends DirectPositionImpl implements HorizontalPosition {

    /** Returns the x coordinate of this position */
    @Override
    public final double getX() {
        return this.getOrdinate(0);
    }

    /** Returns the y coordinate of this position */
    @Override
    public final double getY() {
        return this.getOrdinate(1);
    };

    /**
     * Creates a new HorizontalPositionImpl with the given coordinate reference
     * system and the given coordinates.
     * 
     * @param crs
     *            The coordinate reference system. If this is an immutable
     *            object then this HorizontalPositionImpl will also be
     *            immutable. This may be null (in which case the CRS of this
     *            HorizontalPosition must be given by some containing object).
     * @param x
     *            The x coordinate of this position
     * @param y
     *            The y coordinate of this position
     * @throws IllegalArgumentException
     *             if the CRS is non-null and does not have two dimensions
     */
    public HorizontalPositionImpl(double x, double y, CoordinateReferenceSystem crs) {
        super(crs, x, y);
    }

    /**
     * Creates a new HorizontalPositionImpl without a specified
     * CoordinateReferenceSystem. This object may only be interpreted in the
     * context of a containing or supporting object that contains the CRS.
     * 
     * @param x
     *            The x coordinate of this position
     * @param y
     *            The y coordinate of this position
     */
    public HorizontalPositionImpl(double x, double y) {
        this(x, y, null);
    }

}
