package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.List;

import org.opengis.referencing.cs.CoordinateSystemAxis;

import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Immutable implementation of a {@link ReferenceableAxis}, whose values are not
 * necessarily regularly spaced along the axis.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public class ReferenceableAxisImpl extends AbstractIrregularAxis<Double> {

    
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
    public ReferenceableAxisImpl(String name, Double[] axisValues, boolean isLongitude) {
        super(name, axisValues);
        this.isLongitude = isLongitude;
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
        super(name, axisValues.toArray(new Double[0]));
        this.isLongitude = isLongitude;
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
    public ReferenceableAxisImpl(CoordinateSystemAxis axis, Double[] axisValues, boolean isLongitude) {
        super(axis, axisValues);
        this.isLongitude = isLongitude;
    }

    @Override
    public int findIndexOf(Double position) {
        if (isLongitude) {
            position = GISUtils.getNextEquivalentLongitude(this.getMinimumValue(), position);
        }
        return super.findIndexOf(position);
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
    protected double difference(Double pos1, Double pos2) {
        return pos1 - pos2;
    }
    
    @Override
    protected Double getMidpoint(Double pos1, Double pos2) {
        return 0.5 * (pos1 + pos2);
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
