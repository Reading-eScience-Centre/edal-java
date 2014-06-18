/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
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

package uk.ac.rdg.resc.edal.domain;

import java.io.Serializable;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

import uk.ac.rdg.resc.edal.util.Extents;

/**
 * A simple {@link TemporalDomain} containing just an extent and a
 * {@link Chronology}
 * 
 * @author Guy Griffiths
 */
public class SimpleTemporalDomain implements TemporalDomain, Serializable {
    private static final long serialVersionUID = 1L;
    private final Extent<DateTime> extent;
    private final Chronology chronology;
    
    public SimpleTemporalDomain(DateTime min, DateTime max) {
        if(min == null || max == null) {
            chronology = ISOChronology.getInstanceUTC();
            extent = Extents.emptyExtent(DateTime.class);
        } else {
            chronology = min.getChronology();
            extent = Extents.newExtent(min, max);
        }
    }
    
    @Override
    public boolean contains(DateTime position) {
        return extent.contains(position);
    }

    @Override
    public Extent<DateTime> getExtent() {
        return extent;
    }

    @Override
    public Chronology getChronology() {
        return chronology;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((chronology == null) ? 0 : chronology.hashCode());
        result = prime * result + ((extent == null) ? 0 : extent.hashCode());
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
        SimpleTemporalDomain other = (SimpleTemporalDomain) obj;
        if (chronology == null) {
            if (other.chronology != null)
                return false;
        } else if (!chronology.toString().equals(other.chronology.toString()))
            return false;
        if (extent == null) {
            if (other.extent != null)
                return false;
        } else if (!extent.equals(other.extent))
            return false;
        return true;
    }


}
