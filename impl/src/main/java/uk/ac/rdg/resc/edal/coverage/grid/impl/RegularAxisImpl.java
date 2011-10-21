package uk.ac.rdg.resc.edal.coverage.grid.impl;

import org.opengis.referencing.cs.CoordinateSystemAxis;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.grid.RegularAxis;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Immutable implementation of a {@link RegularAxis}, whose values are regularly
 * spaced along the axis.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public final class RegularAxisImpl extends AbstractReferenceableAxis<Double> implements RegularAxis {
    private double firstValue;
    private double spacing; // The axis spacing
    private int size; // The number of points on the axis
    private final boolean isLongitude;

    public RegularAxisImpl(CoordinateSystemAxis axis, double firstValue, double spacing, int size, boolean isLongitude) {
        super(axis);
        this.isLongitude = isLongitude;
        init(firstValue, spacing, size);
    }

    public RegularAxisImpl(String name, double firstValue, double spacing, int size, boolean isLongitude) {
        super(name);
        this.isLongitude = isLongitude;
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

    /**
     * Gets the index of the given value as a double-precision number that is
     * not necessarily an integer.
     */
    private Double getIndex(Double value) {
        return (value - firstValue) / spacing;
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
        double centre = getCoordinateValue(index);
        return Extents.newExtent(centre - 0.5 * spacing, centre + 0.5 * spacing);
    }

    @Override
    public int findIndexOf(Double position) {
        if (isLongitude) {
            position = GISUtils.getNextEquivalentLongitude(this.getCoordinateExtent().getLow(), position);
        }
        // This method will generally be faster than an exhaustive search, or
        // even a binary search

        // We find the (non-integer) index of the given value
        Double indexDbl = getIndex(position);
        // We round to the nearest integer
        int index = (int)Math.round(indexDbl);
        // Check the extremes (probably not strictly necessary?)
//        if (index < 0) return 0;
//        if (index == this.size) return this.size - 1;
        if (index < 0 || index >= size)
            return -1;
        return index;
    }

    @Override
    protected Double extendFirstValue(Double firstVal, Double nextVal) {
        return firstVal - 0.5 * (nextVal - firstVal);
    }

    @Override
    protected Double extendLastValue(Double lastVal, Double secondLastVal) {
        return lastVal + 0.5 * (lastVal - secondLastVal);
    }
}
