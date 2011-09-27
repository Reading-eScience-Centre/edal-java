package uk.ac.rdg.resc.edal.coverage.domain.impl;

import java.util.Collections;
import java.util.List;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.domain.PointSeriesDomain;
import uk.ac.rdg.resc.edal.position.CalendarSystem;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.util.Extents;

public class PointSeriesDomainImpl implements PointSeriesDomain {

    private final List<TimePosition> times;
    private final CalendarSystem calSys;
    
    public PointSeriesDomainImpl(List<TimePosition> times) {
        this.times = times;
        long lastTime = 0L;
        if(times.size() == 0){
            throw new IllegalArgumentException("Must have at least one time value for a PointSeriesDomain");
        }
        for(TimePosition time : times){
            if(time.getValue() < lastTime){
                throw new IllegalArgumentException("List of times must be in ascending order");
            }
            lastTime = time.getValue();
        }
        calSys = times.get(0).getCalendarSystem();
    }
    
    @Override
    public CalendarSystem getCalendarSystem() {
        return calSys;
    }

    @Override
    public List<TimePosition> getDomainObjects() {
        return times;
    }

    @Override
    public Extent<TimePosition> getExtent() {
        return Extents.newExtent(times.get(0), times.get(times.size()-1));
    }

    @Override
    public boolean contains(TimePosition position) {
        return (position.getValue() >= times.get(0).getValue() && position.getValue() <= times.get(times.size()-1).getValue());
    }

    @Override
    public long findIndexOf(TimePosition time) {
        int index = Collections.binarySearch(times, time);
        if(index >= 0){
            return index;
        } else {
            int insertionPoint = -(index+1);
            if(insertionPoint == times.size() || insertionPoint == 0){
                return -1;
            }
            if(Math.abs(times.get(insertionPoint).getValue() - time.getValue()) < 
               Math.abs(times.get(insertionPoint-1).getValue() - time.getValue())){
                return insertionPoint;
            } else {
                return insertionPoint-1;
            }
        }
    }

    @Override
    public long size() {
        return times.size();
    }

    @Override
    public List<TimePosition> getTimes() {
        return times;
    }
}
