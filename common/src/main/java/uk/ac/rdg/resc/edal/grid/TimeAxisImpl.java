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

package uk.ac.rdg.resc.edal.grid;

import java.util.List;

import org.joda.time.Chronology;
import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * An implementation of a {@link TimeAxis}
 * 
 * @author Guy
 * @author Jon
 * 
 */
public final class TimeAxisImpl extends AbstractIrregularAxis<DateTime> implements TimeAxis {

    private Chronology chronology;

    public TimeAxisImpl(String name, List<DateTime> axisValues) {
        super(name, axisValues);
        chronology = axisValues.get(0).getChronology();
    }

    @Override
    protected DateTime extendFirstValue(DateTime firstVal, DateTime nextVal) {
        long tVal = (long) (firstVal.getMillis() - 0.5 * (nextVal.getMillis() - firstVal
                .getMillis()));
        return new DateTime(tVal);
    }

    @Override
    protected DateTime extendLastValue(DateTime lastVal, DateTime secondLastVal) {
        long tVal = (long) (lastVal.getMillis() + 0.5 * (lastVal.getMillis() - secondLastVal
                .getMillis()));
        return new DateTime(tVal, chronology);
    }

    @Override
    protected double difference(DateTime pos1, DateTime pos2) {
        return pos1.getMillis() - pos2.getMillis();
    }

    @Override
    protected DateTime getMidpoint(DateTime pos1, DateTime pos2) {
        return new DateTime((long) (0.5 * (pos1.getMillis() + pos2.getMillis())));
    }

    @Override
    public Extent<DateTime> getExtent() {
        return Extents.newExtent(getFirstValue(), getLastValue());
    }

    @Override
    public Chronology getChronology() {
        return chronology;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((chronology == null) ? 0 : chronology.hashCode());
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
        if (chronology == null) {
            if (other.chronology != null)
                return false;
        } else if (!chronology.toString().equals(other.chronology.toString()))
            return false;
        return true;
    }

}
