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

package uk.ac.rdg.resc.edal.coverage.domain.impl;

import java.util.Collections;
import java.util.List;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.domain.PointSeriesDomain;
import uk.ac.rdg.resc.edal.position.CalendarSystem;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * This is a {@link List} backed implementation of a {@link PointSeriesDomain}
 * 
 * @author Guy Griffiths
 * 
 */
public class PointSeriesDomainImpl implements PointSeriesDomain {

    private final List<TimePosition> times;
    private final CalendarSystem calSys;

    /**
     * Instantiates a new {@link PointSeriesDomain} from a {@link List} of
     * {@link TimePosition}s
     * 
     * @param times
     *            the {@link List} of {@link TimePosition}s
     */
    public PointSeriesDomainImpl(List<TimePosition> times) {
        if(times != null){
            this.times = times;
            long lastTime = 0L;
            if (times.size() == 0) {
                throw new IllegalArgumentException(
                        "Must have at least one time value for a PointSeriesDomain");
            }
            for (TimePosition time : times) {
                if (time.getValue() < lastTime) {
                    throw new IllegalArgumentException("List of times must be in ascending order");
                }
                lastTime = time.getValue();
            }
            calSys = times.get(0).getCalendarSystem();
        } else {
            /*
             * We may want an empty point series domain
             */
            this.times = Collections.emptyList();
            calSys = null;
        }
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
        return Extents.newExtent(times.get(0), times.get(times.size() - 1));
    }

    @Override
    public boolean contains(TimePosition position) {
        return (position.getValue() >= times.get(0).getValue() && position.getValue() <= times.get(
                times.size() - 1).getValue());
    }

    @Override
    public long findIndexOf(TimePosition time) {
        int index = Collections.binarySearch(times, time);
        if (index >= 0) {
            return index;
        } else {
            int insertionPoint = -(index + 1);
            if (insertionPoint == times.size() || insertionPoint == 0) {
                return -1;
            }
            if (Math.abs(times.get(insertionPoint).getValue() - time.getValue()) < Math.abs(times
                    .get(insertionPoint - 1).getValue() - time.getValue())) {
                return insertionPoint;
            } else {
                return insertionPoint - 1;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((calSys == null) ? 0 : calSys.hashCode());
        result = prime * result + ((times == null) ? 0 : times.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PointSeriesDomainImpl other = (PointSeriesDomainImpl) obj;
        if (calSys != other.calSys)
            return false;
        if (times == null) {
            if (other.times != null)
                return false;
        } else if (!times.equals(other.times))
            return false;
        return true;
    }
}
