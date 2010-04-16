/*
 * Copyright (c) 2010 The University of Reading
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
 */

package uk.ac.rdg.resc.edal.coverage.domain;

import java.util.List;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * A geospatial/temporal domain: defines the set of points for which a {@link DiscreteCoverage}
 * is defined.  The domain is comprised of a set of unique domain objects in a
 * defined order.  The domain therefore has the semantics of both a {@link Set}
 * and a {@link List} of domain objects.
 * @param <DO> The type of the domain object
 * @author Jon
 */
public interface Domain<DO>
{
    /**
     * Gets the coordinate reference system to which objects in this domain are
     * referenced.  Returns null if the domain objects cannot be referenced
     * to an external coordinate reference system.
     * @return the coordinate reference system to which objects in this domain are
     * referenced, or null if the domain objects are not externally referenced.
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem();

    /**
     * Returns the {@link List} of domain objects that comprise this domain.
     */
    public List<DO> getDomainObjects();

}
