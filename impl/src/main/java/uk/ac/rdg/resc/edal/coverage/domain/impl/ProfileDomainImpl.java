package uk.ac.rdg.resc.edal.coverage.domain.impl;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

import uk.ac.rdg.resc.edal.coverage.domain.ProfileDomain;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.position.impl.VerticalPositionImpl;

public class ProfileDomainImpl implements ProfileDomain {
    
    private VerticalCrs vCrs;
    private List<Double> values;
    private boolean reversed = false;

    public ProfileDomainImpl(List<Double> values, VerticalCrs vCrs) {
        this.vCrs = vCrs;
        if(values.size() == 0){
            throw new IllegalArgumentException("Must have at least one elevation value for a ProfileDomain");
        }
        /*
         * Reverse the list if it is (presumed) descending
         */
        if(values.size() >= 2){
            if(values.get(0) > values.get(1)){
                reversed = true;
                Collections.reverse(values);
            }
        }
        Double lastVal = Double.NEGATIVE_INFINITY; 
        for(Double elevation : values){
            if(elevation < lastVal){
                throw new IllegalArgumentException("List of values must be in ascending or descending order");
            }
            lastVal = elevation;
        }
        this.values = values;
    }

    @Override
    public VerticalCrs getVerticalCrs() {
        return vCrs;
    }

    @Override
    public List<Double> getZValues() {
        List<Double> ret = new AbstractList<Double>() {
            @Override
            public Double get(int index) {
                return values.get(reverseIndex(index));
            }

            @Override
            public int size() {
                return values.size();
            }
        };
        return ret;
    }

    @Override
    public boolean contains(VerticalPosition position) {
        return (position.getZ() >= values.get(0) && position.getZ() <= values.get(values.size()-1));
    }

    @Override
    public long findIndexOf(VerticalPosition position) {
        int index = Collections.binarySearch(values, position.getZ());
        index = reverseIndex(index);
        if(index >= 0){
            return index;
        } else {
            int insertionPoint = -(index+1);
            if(insertionPoint == values.size() || insertionPoint == 0){
                return -1;
            }
            if(Math.abs(values.get(insertionPoint) - position.getZ()) < 
               Math.abs(values.get(insertionPoint-1) - position.getZ())){
                return insertionPoint;
            } else {
                return insertionPoint-1;
            }
        }
    }

    private int reverseIndex(int index) {
        if (reversed)
            return values.size() - 1 - index;
        else
            return index;
    }

    @Override
    public List<VerticalPosition> getDomainObjects() {
        List<VerticalPosition> ret = new AbstractList<VerticalPosition>() {
            @Override
            public VerticalPosition get(int index) {
                return new VerticalPositionImpl(values.get(reverseIndex(index)), vCrs);
            }

            @Override
            public int size() {
                return values.size();
            }
        };
        return ret;
    }

    @Override
    public long size() {
        return values.size();
    }
}
