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

import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * A simple {@link VerticalDomain} containing just an extent and a
 * {@link VerticalCrs}
 * 
 * @author Guy Griffiths
 */
public class SimpleVerticalDomain implements VerticalDomain, Serializable {
    private static final long serialVersionUID = 1L;
    private final Extent<Double> extent;
    private final VerticalCrs crs;

    public SimpleVerticalDomain(Double min, Double max, VerticalCrs crs) {
        this.crs = crs;
        if (min == null || max == null) {
            extent = Extents.emptyExtent(Double.class);
        } else {
            extent = Extents.newExtent(min, max);
        }
    }

    @Override
    public boolean contains(Double position) {
        return extent.contains(position);
    }

    @Override
    public Extent<Double> getExtent() {
        return extent;
    }

    @Override
    public VerticalCrs getVerticalCrs() {
        return crs;
    }

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((crs == null) ? 0 : crs.hashCode());
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
        SimpleVerticalDomain other = (SimpleVerticalDomain) obj;
        if (crs == null) {
            if (other.crs != null)
                return false;
        } else if (!crs.equals(other.crs))
            return false;
        if (extent == null) {
            if (other.extent != null)
                return false;
        } else if (!extent.equals(other.extent))
            return false;
        return true;
    }
}
