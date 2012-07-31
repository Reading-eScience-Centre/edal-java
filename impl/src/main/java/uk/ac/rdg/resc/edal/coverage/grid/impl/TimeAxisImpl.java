/*******************************************************************************
 * Copyright (c) 2012 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

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

    public TimeAxisImpl(String name, List<TimePosition> axisValues) {
        super(name, axisValues);
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((calSys == null) ? 0 : calSys.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        TimeAxisImpl other = (TimeAxisImpl) obj;
        if (calSys != other.calSys)
            return false;
        return true;
    }
}
