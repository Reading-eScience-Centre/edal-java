package uk.ac.rdg.resc.edal.coverage.grid.impl;

import org.opengis.referencing.cs.CoordinateSystemAxis;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.IntegerExtent;
import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableAxis;

/**
 * Abstract superclass for {@link ReferenceableAxis} implementations. Handles
 * the tricky case of searching for longitude values in the axis (longitude
 * values wrap around the globe).
 * 
 * @todo automatically apply the maximum extent -90:90 for latitude axes? Or is
 *       this dangerous, given that some model grid cells are constructed with
 *       latitudes outside this range?
 * @author Jon
 */
public abstract class AbstractReferenceableAxis<P extends Comparable<? super P>> implements ReferenceableAxis<P> {

    private final CoordinateSystemAxis coordSysAxis;
    private final String name;

    /**
     * Creates an axis that is referenceable to the given coordinate system
     * axis. The name of the axis will be set to the name of the given axis.
     * 
     * @throws NullPointerException
     *             if coordSysAxis is null
     */
    protected AbstractReferenceableAxis(CoordinateSystemAxis coordSysAxis) {
        if (coordSysAxis == null)
            throw new NullPointerException("coordSysAxis cannot be null");
        this.name = coordSysAxis.getName().toString();
        this.coordSysAxis = coordSysAxis;
    }

    /**
     * Creates an axis with the given name. The
     * {@link #getCoordinateSystemAxis() coordinate system axis} will be null.
     */
    protected AbstractReferenceableAxis(String name) {
        this.name = name;
        coordSysAxis = null;
    }

    /** Gets the value of the axis at index 0 */
    protected final P getFirstValue() {
        return getCoordinateValue(0);
    }

    /** Gets the value of the axis at index (size - 1) */
    protected final P getLastValue() {
        return getCoordinateValue(this.size() - 1);
    }

    /**
     * Returns the minimum coordinate value of this axis. This will be the first
     * coordinate value if the coordinate values are in ascending order, or the
     * last coordinate value if the coordinate values are in descending order.
     * 
     * @return the minimum coordinate value of this axis
     */
    protected final P getMinimumValue() {
        return isAscending() ? getFirstValue() : getLastValue();
    }

    /**
     * Returns the maximum coordinate value of this axis. This will be the last
     * coordinate value if the coordinate values are in ascending order, or the
     * first coordinate value if the coordinate values are in descending order.
     * 
     * @return the maximum coordinate value of this axis
     */
    protected final P getMaximumValue() {
        return isAscending() ? getLastValue() : getFirstValue();
    }

    @Override
    public CoordinateSystemAxis getCoordinateSystemAxis() {
        return coordSysAxis;
    }

    @Override
    public Extent<Integer> getIndexExtent() {
        /*
         * This implementation returns an exclusive extent ranging from zero up
         * to the size-1 (the maximum index). ReferenceableAxis implementations
         * which do not want this behaviour should override this method
         */
        return new IntegerExtent(0, size() - 1);
    }

    @Override
    public String getName() {
        return name;
    }
}
