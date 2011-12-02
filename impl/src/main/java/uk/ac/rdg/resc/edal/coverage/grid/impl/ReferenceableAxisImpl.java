package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.Arrays;
import java.util.List;

import org.opengis.referencing.cs.CoordinateSystemAxis;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Immutable implementation of a {@link ReferenceableAxis}, whose values are not
 * necessarily regularly spaced along the axis.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public class ReferenceableAxisImpl extends AbstractReferenceableAxis<Double> {
    /**
     * The axis values, always in ascending numerical order to simplify
     * searching
     */
    private double[] axisValues;

    /** True if axis values in the above array have been reversed */
    private boolean reversed = false;
    
    private final boolean isLongitude;

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
     * @param name
     *            The name of the axis
     * @param axisValues
     *            List of axis values; must be in strictly ascending numerical
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
    public ReferenceableAxisImpl(String name, List<Double> axisValues, boolean isLongitude) {
        super(name);
        this.isLongitude = isLongitude;
        double[] vals = new double[axisValues.size()];
        for(int i=0; i<vals.length; i++){
            vals[i] = axisValues.get(i);
        }
        init(vals);
    }

    /**
     * Creates a ReferenceableAxis from the given array of axis values. The axis
     * values are copied to internal data structures, therefore subsequent
     * modifications to the array of axis values have no effect on this object.
     * 
     * @param axis
     *            The coordinate system axis to which values on this axis are
     *            referenceable
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
    protected void init(double[] axisValues) {
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
        if (reversed)
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
    public int findIndexOf(Double position) {
        if (isLongitude) {
            position = GISUtils.getNextEquivalentLongitude(this.getMinimumValue(), position);
        }
        int index = Arrays.binarySearch(axisValues, position);
        if(index >= 0){
            return maybeReverseIndex(index);
        } else {
            int insertionPoint = -(index+1);
            if(insertionPoint == axisValues.length || insertionPoint == 0){
                return -1;
            }
            if(Math.abs(axisValues[insertionPoint] - position) < 
               Math.abs(axisValues[insertionPoint-1] - position)){
                return maybeReverseIndex(insertionPoint);
            } else {
                return maybeReverseIndex(insertionPoint-1);
            }
        }
    }
    

    @Override
    protected Double extendFirstValue(Double firstVal, Double nextVal) {
        return firstVal - 0.5 * (nextVal - firstVal);
    }

    @Override
    protected Double extendLastValue(Double lastVal, Double secondLastVal) {
        return lastVal + 0.5 * (lastVal - secondLastVal);
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

        return Extents.newExtent(lowerBound, upperBound);
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ReferenceableAxisImpl){
            ReferenceableAxisImpl axis = (ReferenceableAxisImpl) obj;
            return super.equals(obj) && (axis.getCoordinateValues().equals(getCoordinateValues()))
                    && (reversed == axis.reversed) && (isLongitude == axis.isLongitude);
        } else {
            return false;
        }
    }
}
