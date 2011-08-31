package uk.ac.rdg.resc.edal.coverage.domain.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.rdg.resc.edal.coverage.domain.ProfileDomain;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.position.impl.VerticalPositionImpl;

public class ProfileDomainImpl implements ProfileDomain {
    
    private VerticalCrs vCrs;
    private List<Double> values;

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
        return values;
    }

    @Override
    public boolean contains(VerticalPosition position) {
        return (position.getZ() >= values.get(0) && position.getZ() <= values.get(values.size()-1));
    }

    @Override
    public int findIndexOf(VerticalPosition position) {
        int index = Collections.binarySearch(values, position.getZ());
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

    @Override
    public List<VerticalPosition> getDomainObjects() {
        List<VerticalPosition> ret = new ArrayList<VerticalPosition>();
        for(Double value : values){
            ret.add(new VerticalPositionImpl(value, vCrs));
        }
        return ret;
    }

    @Override
    public int size() {
        return values.size();
    }

}
