package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.ArrayList;
import java.util.List;

import org.opengis.referencing.cs.CoordinateSystemAxis;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.Utils;

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
public abstract class AbstractReferenceableAxis implements ReferenceableAxis<Double> {

    private final CoordinateSystemAxis coordSysAxis;
    private final String name;
    private final boolean isLongitude;

    /**
     * Creates an axis that is referenceable to the given coordinate system
     * axis. The name of the axis will be set to the name of the given axis.
     * 
     * @throws NullPointerException
     *             if coordSysAxis is null
     */
    protected AbstractReferenceableAxis(CoordinateSystemAxis coordSysAxis, boolean isLongitude) {
        if (coordSysAxis == null)
            throw new NullPointerException("coordSysAxis cannot be null");
        this.name = coordSysAxis.getName().toString();
        this.coordSysAxis = coordSysAxis;
        this.isLongitude = isLongitude;
    }

    /**
     * Creates an axis with the given name. The
     * {@link #getCoordinateSystemAxis() coordinate system axis} will be null.
     */
    protected AbstractReferenceableAxis(String name, boolean isLongitude) {
        this.name = name;
        coordSysAxis = null;
        this.isLongitude = isLongitude;
    }

    @Override
    public int findIndexOf(Double position) {
        if (isLongitude) {
            position = Utils.getNextEquivalentLongitude(this.getMinimumValue(), position);
        }
        return doGetCoordinateIndex(position);
    }

    /**
     * <p>
     * Gets the index of the given coordinate value, ignoring the possibility of
     * longitude axis wrapping. Returns -1 if the value is not a coordinate
     * value of this axis.
     * </p>
     * <p>
     * Subclasses should make this implementation as efficient as possible,
     * since the implementation is reused in the {@code indexOf} method of the
     * {@link #findIndexOf() list of coordinate values}.
     * </p>
     */
    protected abstract int doGetCoordinateIndex(Double value);

    /** Gets the value of the axis at index 0 */
    private final double getFirstValue() {
        return getCoordinateValue(0);
    }

    /** Gets the value of the axis at index (size - 1) */
    private final double getLastValue() {
        return getCoordinateValue(this.size() - 1);
    }

    /**
     * Returns the minimum coordinate value of this axis. This will be the first
     * coordinate value if the coordinate values are in ascending order, or the
     * last coordinate value if the coordinate values are in descending order.
     * 
     * @return the minimum coordinate value of this axis
     */
    private final double getMinimumValue() {
        return isAscending() ? getFirstValue() : getLastValue();
    }

    /**
     * Returns the maximum coordinate value of this axis. This will be the last
     * coordinate value if the coordinate values are in ascending order, or the
     * first coordinate value if the coordinate values are in descending order.
     * 
     * @return the maximum coordinate value of this axis
     */
    private final double getMaximumValue() {
        return isAscending() ? getLastValue() : getFirstValue();
    }

    @Override
    public Extent<Double> getCoordinateExtent() {
        final double min;
        final double max;
        if (size() == 1) {
            min = getMinimumValue();
            max = getMaximumValue();
        } else {
            double val1 = getFirstValue() - 0.5 * (getCoordinateValue(1) - getFirstValue());
            double val2 = getLastValue() + 0.5 * (getLastValue() - getCoordinateValue(size() - 2));
            if (this.isAscending()) {
                min = val1;
                max = val2;
            } else {
                min = val2;
                max = val1;
            }
        }
        return Extents.newExtent(min, max);
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
        return Extents.newExtent(0, size() - 1);
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean contains(Double position) {
        /*
         * We can simply find out whether the position falls within the extent
         * of the axis.
         * 
         * Special behaviour (e.g. discontinuous axes) should be implemented in
         * a subclass
         */
        Extent<Double> extent = getCoordinateExtent();
        return (position >= extent.getLow() && position <= extent.getHigh());
    }
    

    @Override
    public List<Extent<Double>> getDomainObjects() {
        List<Extent<Double>> domainObjects = new ArrayList<Extent<Double>>();
        for (int i = 0; i < size(); i++) {
            domainObjects.add(getCoordinateBounds(i));
        }
        return domainObjects;
    }
}
