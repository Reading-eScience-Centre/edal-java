package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.Arrays;

import org.opengis.referencing.cs.CoordinateSystemAxis;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.util.Extents;

public abstract class AbstractIrregularAxis<T extends Comparable<? super T>> extends
        AbstractReferenceableAxis<T> {
    /**
     * The axis values, always in ascending numerical order to simplify
     * searching
     */
    protected T[] axisValues;

    /**
     * True if axis values in the above array have been reversed
     */
    protected boolean reversed;

    protected AbstractIrregularAxis(CoordinateSystemAxis coordSysAxis, T[] axisValues) {
        super(coordSysAxis);
        init(axisValues);
    }

    protected AbstractIrregularAxis(String name, T[] axisValues) {
        super(name);
        init(axisValues);
    }

    /**
     * Sets all the fields and checks that the axis values ascend or descend
     * monotonically, throwing an IllegalArgumentException if not.
     */
    @SuppressWarnings("unchecked")
    protected void init(T[] axisValues) {
        if (axisValues.length == 0) {
            throw new IllegalArgumentException("Zero-length array");
        }

        if (axisValues.length == 1) {
            this.axisValues = axisValues.clone();
            return;
        }

        reversed = axisValues[1].compareTo(axisValues[0]) < 0;
        if (reversed) {
            /*
             * This is not recommended behaviour for Java (hence the
             * SuppressWarnings), but in this case it is a choice between this,
             * unnecessary abstraction, or heavy repetition of code.
             */
            this.axisValues = (T[]) new Object[axisValues.length];
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
    protected void checkAscending() {
        T prevVal = axisValues[0];
        for (int i = 1; i < axisValues.length; i++) {
            if (axisValues[i].compareTo(prevVal) <= 0) {
                throw new IllegalArgumentException(
                        "Coordinate values must increase or decrease monotonically");
            }
            prevVal = axisValues[i];
        }
    }

    /*
     * If the array has been reversed, we need to reverse the index
     */
    private int maybeReverseIndex(int index) {
        if (reversed)
            return axisValues.length - 1 - index;
        else
            return index;
    }

    @Override
    public T getCoordinateValue(int index) {
        return axisValues[maybeReverseIndex(index)];
    }

    @Override
    public int findIndexOf(T value) {
        int index = Arrays.binarySearch(axisValues, value);
        if (index >= 0) {
            return maybeReverseIndex(index);
        } else {
            int insertionPoint = -(index + 1);
            if (insertionPoint == 0) {
                if (getCoordinateBounds(0).contains(value)) {
                    return 0;
                } else {
                    return -1;
                }
            }
            if (insertionPoint == axisValues.length) {
                if (getCoordinateBounds(axisValues.length - 1).contains(value)) {
                    return axisValues.length - 1;
                } else {
                    return -1;
                }
            }
            if (Math.abs(difference(axisValues[insertionPoint], value)) < Math.abs(difference(
                    axisValues[insertionPoint - 1], value))) {
                return maybeReverseIndex(insertionPoint);
            } else {
                return maybeReverseIndex(insertionPoint - 1);
            }
        }
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
    public Extent<T> getCoordinateBounds(int index) {
        int upperIndex = index + 1;
        int lowerIndex = index - 1;
        T lowerBound;
        if (index == 0) {
            lowerBound = getCoordinateExtent().getLow();
        } else {
            lowerBound = getMidpoint(axisValues[index], axisValues[lowerIndex]);
        }

        T upperBound;
        if (index == size() - 1) {
            upperBound = getCoordinateExtent().getHigh();
        } else {
            upperBound = getMidpoint(axisValues[upperIndex], axisValues[index]);
        }

        return Extents.newExtent(lowerBound, upperBound);
    }

    /**
     * Returns the midpoint of the supplied values. This is so that certain
     * methods can be abstracted without worrying about non-trivial midpoint
     * calculation (e.g. {@link TimePosition}s)
     * 
     * @param pos1
     *            the first value
     * @param pos2
     *            the second value
     * @return the midpoint of the values
     */
    protected abstract T getMidpoint(T pos1, T pos2);

    /**
     * Returns the difference between the supplied values. This is so that
     * certain methods can be abstracted without worrying about non-trivial
     * difference calculation (e.g. {@link TimePosition}s)
     * 
     * @param pos1
     *            the first value
     * @param pos2
     *            the second value
     * @return the value corresponding to pos1 - pos2
     */
    protected abstract double difference(T pos1, T pos2);
}
