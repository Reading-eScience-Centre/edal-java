package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.List;

import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.position.CalendarSystem;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.impl.TimePositionJoda;

/**
 * An implementation of a {@link TimeAxis}
 * 
 * @author Jon Blower
 * @author Guy Griffiths
 * 
 */
public final class TimeAxisImpl extends AbstractIrregularAxis<TimePosition> implements TimeAxis {

    private final CalendarSystem calSys;

    public TimeAxisImpl(String name, TimePosition[] axisValues) {
        super(name, axisValues);
        this.calSys = axisValues[0].getCalendarSystem();
    }

    public TimeAxisImpl(String name, List<TimePosition> axisValues) {
        super(name, axisValues.toArray(new TimePosition[0]));
        this.calSys = axisValues.get(0).getCalendarSystem();
    }

    @Override
    protected TimePosition extendFirstValue(TimePosition firstVal, TimePosition nextVal) {
        long tVal = (long) (firstVal.getValue() - 0.5 * (nextVal.getValue() - firstVal.getValue()));
        return new TimePositionJoda(tVal);
    }

    @Override
    protected TimePosition extendLastValue(TimePosition lastVal, TimePosition secondLastVal) {
        long tVal = (long) (lastVal.getValue() + 0.5 * (lastVal.getValue() - secondLastVal
                .getValue()));
        return new TimePositionJoda(tVal);
    }

    @Override
    public CalendarSystem getCalendarSystem() {
        return calSys;
    }


    @Override
    protected double difference(TimePosition pos1, TimePosition pos2) {
        return pos1.getValue() - pos2.getValue();
    }

    @Override
    protected TimePosition getMidpoint(TimePosition pos1, TimePosition pos2) {
        return new TimePositionJoda((long) (0.5 * (pos1.getValue() + pos2.getValue())));
    }
}
