package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opengis.referencing.cs.CoordinateSystemAxis;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.VerticalExtent;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.position.impl.VerticalPositionImpl;

public class VerticalAxisImpl extends AbstractReferenceableAxis<VerticalPosition> implements VerticalAxis {

    private VerticalPosition[] axisValues;
    private boolean reversed = false;
    private VerticalCrs vCrs;
    
    protected VerticalAxisImpl(CoordinateSystemAxis coordSysAxis, VerticalPosition[] values) {
        super(coordSysAxis);
        axisValues = values;
        init(axisValues);
    }
    
    /**
     * Sets all the fields and checks that the axis values ascend or descend
     * monotonically, throwing an IllegalArgumentException if not.
     */
    private void init(VerticalPosition[] axisValues) {
        if (axisValues.length == 0) {
            throw new IllegalArgumentException("Zero-length array");
        }

        if (axisValues.length == 1) {
            this.axisValues = axisValues.clone();
            return;
        }

        reversed = axisValues[1].getZ() < axisValues[0].getZ();
        if (reversed) {
            // Copy out the array in reverse order
            this.axisValues = new VerticalPositionImpl[axisValues.length];
            for (int i = 0; i < axisValues.length; i++) {
                this.axisValues[i] = axisValues[axisValues.length - 1 - i];
            }
        } else {
            this.axisValues = axisValues.clone();
        }

        vCrs = axisValues[0].getCoordinateReferenceSystem();
        
        checkAscending();
    }

    /**
     * Checks that the axis values ascend or descend monotonically, throwing an
     * IllegalArgumentException if not.
     */
    private void checkAscending() {
        VerticalPosition prevVal = axisValues[0];
        for (int i = 1; i < axisValues.length; i++) {
            if (axisValues[i].getZ() <= prevVal.getZ()) {
                throw new IllegalArgumentException("Coordinate values must increase or decrease monotonically");
            }
            prevVal = axisValues[i];
        }
    }
    
    @Override
    public VerticalPosition getCoordinateValue(int index) {
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
    public Extent<VerticalPosition> getCoordinateBounds(int index) {
        int upperIndex = index + 1;
        int lowerIndex = index - 1;
        VerticalPosition lowerBound;
        if (index == 0) {
            lowerBound = getCoordinateExtent().getLow();
        } else {
            lowerBound = new VerticalPositionImpl(0.5 * (axisValues[index].getZ() + axisValues[lowerIndex].getZ()), vCrs);
        }

        VerticalPosition upperBound;
        if (index == size() - 1) {
            upperBound = getCoordinateExtent().getHigh();
        } else {
            upperBound = new VerticalPositionImpl(0.5 * (axisValues[upperIndex].getZ() + axisValues[index].getZ()), vCrs);
        }

        return new VerticalExtent(lowerBound, upperBound);
    }
    
    @Override
    public VerticalCrs getVerticalCrs() {
        return vCrs;
    }

    @Override
    public int findIndexOf(VerticalPosition position) {
        int index = Arrays.binarySearch(axisValues, position);
        return index >= 0 ? maybeReverseIndex(index) : -1;
    }

    @Override
    public Extent<VerticalPosition> getCoordinateExtent() {
        final VerticalPosition min;
        final VerticalPosition max;
        if (size() == 1) {
            min = getMinimumValue();
            max = getMaximumValue();
        } else {
            double val1 = getFirstValue().getZ() - 0.5 * (getCoordinateValue(1).getZ() - getFirstValue().getZ());
            double val2 = getLastValue().getZ() + 0.5 * (getLastValue().getZ() - getCoordinateValue(size() - 2).getZ());
            if (this.isAscending()) {
                min = new VerticalPositionImpl(val1, vCrs);
                max = new VerticalPositionImpl(val2, vCrs);
            } else {
                min = new VerticalPositionImpl(val2, vCrs);
                max = new VerticalPositionImpl(val1, vCrs);
            }
        }
        return new VerticalExtent(min, max);
    }

    @Override
    public boolean contains(VerticalPosition position) {
        Extent<VerticalPosition> extent = getCoordinateExtent();
        return (position.getZ() >= extent.getLow().getZ() && position.getZ() <= extent.getHigh().getZ());
    }

    @Override
    public List<Extent<VerticalPosition>> getDomainObjects() {
        List<Extent<VerticalPosition>> domainObjects = new ArrayList<Extent<VerticalPosition>>();
        for (int i = 0; i < size(); i++) {
            domainObjects.add(getCoordinateBounds(i));
        }
        return domainObjects;
    }


}
