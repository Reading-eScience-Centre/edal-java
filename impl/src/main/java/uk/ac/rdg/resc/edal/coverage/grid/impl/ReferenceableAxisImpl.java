package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opengis.referencing.cs.CoordinateSystemAxis;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.GeneralExtent;
import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.util.Utils;

/**
 * Immutable implementation of a {@link ReferenceableAxis}, whose values are not
 * necessarily regularly spaced along the axis.
 * 
 * @author Jon
 */
public class ReferenceableAxisImpl extends AbstractReferenceableAxis<Double> {
    /**
     * The axis values, always in ascending numerical order to simplify
     * searching
     */
    private double[] axisValues;
    private final boolean isLongitude;

    /** True if axis values in the above array have been reversed */
    private boolean reversed = false;

    /**
     * Creates a ReferenceableAxis from the given array of axis values. The axis
     * values are copied to internal data structures, therefore subsequent
     * modifications to the array of axis values have no effect on this object.
     * 
     * @param name
     *            The name of the axis
     * @param axisValues
     *            Array of axis values; must be in strictly ascending numerical
     *            order
     * @param isLongitude
     *            True if this is a longitude axis in degrees (hence values of 0
     *            and 360 are equivalent).
     * @throws NullPointerException
     *             if {@code axisValues} is null
     * @throws IllegalArgumentException
     *             if the axis values are not in strictly ascending numerical
     *             order, or if the array of axis values is empty
     */
    public ReferenceableAxisImpl(String name, double[] axisValues, boolean isLongitude) {
        super(name);
        this.isLongitude = isLongitude;
        init(axisValues);
    }

    /**
     * Creates a ReferenceableAxis from the given array of axis values. The axis
     * values are copied to internal data structures, therefore subsequent
     * modifications to the array of axis values have no effect on this object.
     * 
     * @param axis
     *            The coordinate system axis to which values on this axis are
     *            referenceable
     *            .http://www.guardian.co.uk/commentisfree/2011/aug/
     *            09/uk-riots-psychology-of-looting
     * @param axisValues
     *            Array of axis values; must be in strictly ascending numerical
     *            order
     * @param isLongitude
     *            True if this is a longitude axis in degrees (hence values of 0
     *            and 360 are equivalent).
     * @throws NullPointerException
     *             if {@code axisValues} is null
     * @throws IllegalArgumentException
     *             if the axis values are not in strictly ascending numerical
     *             order, or if the array of axis values is empty
     */
    public ReferenceableAxisImpl(CoordinateSystemAxis axis, double[] axisValues, boolean isLongitude) {
        super(axis);
        this.isLongitude = isLongitude;
        init(axisValues);
    }

    /**
     * Sets all the fields and checks that the axis values ascend or descend
     * monotonically, throwing an IllegalArgumentException if not.
     */
    private void init(double[] axisValues) {
        if (axisValues.length == 0) {
            throw new IllegalArgumentException("Zero-length array");
        }

        if (axisValues.length == 1) {
            this.axisValues = axisValues.clone();
            return;
        }

        reversed = axisValues[1] < axisValues[0];
        if (reversed) {
            // Copy out the array in reverse order
            this.axisValues = new double[axisValues.length];
            for (int i = 0; i < axisValues.length; i++) {
                this.axisValues[i] = axisValues[axisValues.length - 1 - i];
            }
        } else {
            this.axisValues = axisValues.clone();
        }

        checkAscending();
    }

    /**
     * Checks that the axis values ascend or descend monotonically, throwing an
     * IllegalArgumentException if not.
     */
    private void checkAscending() {
        double prevVal = axisValues[0];
        for (int i = 1; i < axisValues.length; i++) {
            if (axisValues[i] <= prevVal) {
                throw new IllegalArgumentException("Coordinate values must increase or decrease monotonically");
            }
            prevVal = axisValues[i];
        }
    }

    @Override
    public Double getCoordinateValue(int index) {
        return axisValues[maybeReverseIndex(index)];
    }

    /** If the array has been reversed, we need to reverse the index */
    private int maybeReverseIndex(int index) {
        if (this.reversed)
            return axisValues.length - 1 - index;
        else
            return index;
    }

    @Override
    public int size() {
        return axisValues.length;
    }

    @Override
    public boolean isAscending() {
        return !reversed;
    }

    @Override
    public Extent<Double> getCoordinateBounds(int index) {
        int upperIndex = index + 1;
        int lowerIndex = index - 1;
        Double lowerBound;
        if (index == 0) {
            lowerBound = getCoordinateExtent().getLow();
        } else {
            lowerBound = 0.5 * (axisValues[index] + axisValues[lowerIndex]);
        }

        Double upperBound;
        if (index == size() - 1) {
            upperBound = getCoordinateExtent().getHigh();
        } else {
            upperBound = 0.5 * (axisValues[upperIndex] + axisValues[index]);
        }

        return new GeneralExtent(lowerBound, upperBound);
    }


    @Override
    public int findIndexOf(Double position) {
        if (isLongitude) {
            position = Utils.getNextEquivalentLongitude(this.getMinimumValue(), position);
        }
        int index = Arrays.binarySearch(axisValues, position);
        return index >= 0 ? maybeReverseIndex(index) : -1;
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
        return new GeneralExtent(min, max);
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
