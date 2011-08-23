package uk.ac.rdg.resc.edal.coverage.grid.impl;

import org.opengis.referencing.cs.CoordinateSystemAxis;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.grid.RegularAxis;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * Immutable implementation of a {@link RegularAxis}, whose values are regularly
 * spaced along the axis.
 * 
 * @author Jon
 */
public final class RegularAxisImpl extends AbstractReferenceableAxis implements RegularAxis {
    private double firstValue;
    private double spacing; // The axis spacing
    private int size; // The number of points on the axis

    public RegularAxisImpl(CoordinateSystemAxis axis, double firstValue, double spacing, int size, boolean isLongitude) {
        super(axis, isLongitude);
        init(firstValue, spacing, size);
    }

    public RegularAxisImpl(String name, double firstValue, double spacing, int size, boolean isLongitude) {
        super(name, isLongitude);
        init(firstValue, spacing, size);
    }

    private void init(double firstValue, double spacing, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Axis length must not be negative or zero");
        }
        if (spacing == 0.0) {
            throw new IllegalArgumentException("Axis spacing cannot be zero");
        }
        this.firstValue = firstValue;
        this.spacing = spacing;
        this.size = size;
    }

    @Override
    public double getCoordinateSpacing() {
        return spacing;
    }

    @Override
    public Double getCoordinateValue(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(index + " must be between 0 and " + (size - 1));
        }
        return firstValue + index * spacing;
    }

    @Override
    protected int doGetCoordinateIndex(Double value) {
        // This method will generally be faster than an exhaustive search, or
        // even a binary search

        // We find the (non-integer) index of the given value
        Double indexDbl = getIndex(value);

        // We find the nearest integer indices on either side of this and
        // compare
        // the corresponding values with the target value. We do this so that we
        // are not sensitive to rounding errors
        int indexAbove = (int) Math.ceil(indexDbl);
        if (indexMatchesValue(indexAbove, value))
            return indexAbove;

        int indexBelow = (int) Math.floor(indexDbl);
        if (indexMatchesValue(indexBelow, value))
            return indexBelow;

        // Neither of the indices matched the target value
        return -1;
    }

    /**
     * Gets the index of the given value as a double-precision number that is
     * not necessarily an integer.
     */
    private Double getIndex(Double value) {
        return (value - firstValue) / spacing;
    }

    private boolean indexMatchesValue(int index, Double value) {
        if (index < 0 || index >= size)
            return false;
        return value == getCoordinateValue(index);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isAscending() {
        return spacing > 0.0;
    }

    @Override
    public String toString() {
        return String.format("Regular axis: %s, %f, %f, %d", getName(), firstValue, spacing, size);
    }

    @Override
    public Extent<Double> getCoordinateBounds(int index) {
        return Extents.newExtent(firstValue - 0.5 * spacing, firstValue + (size - 0.5) * spacing);
    }
}
