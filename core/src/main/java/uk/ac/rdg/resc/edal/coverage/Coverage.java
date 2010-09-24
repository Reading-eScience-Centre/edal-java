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

package uk.ac.rdg.resc.edal.coverage;

import java.util.Collection;
import uk.ac.rdg.resc.edal.coverage.domain.Domain;

/**
 * <p>A Coverage associates positions with its {@link #getDomain() domain}
 * to values (its <i>range</i>).  It is essentially a function.</p>
 * @param <P> The type of object used to identify positions within the coverage's domain.
 * This may be a spatial, temporal, or combined spatiotemporal position.
 * @author Jon
 */
public interface Coverage<P>
{

    /**
     * Returns an object that describes the values that are returned by this
     * coverage.
     * @return an object that describes the values that are returned by this
     * coverage.
     */
    public RecordType getRangeType();

    /**
     * Returns an object that describes the domain of the coverage.
     */
    public Domain<P> getDomain();

    /**
     * This calculates and returns the value of the coverage
     * for a point in space and/or time.
     * @param position an indicator of the position within this coverage's domain
     * @param fieldNames a List of for which the coverage is to be evaluated
     * (each of these must be represented in the {@link #getRangeType() range type}.
     * If this is null, this method returns a Record containing values for all
     * fields.  If this is non-null but empty this method returns an empty
     * record.
     * @return A Record containing the values of the requested fields, or null
     * if the coverage is not defined at the given position.
     */
    public Record evaluate(P position, Collection<String> fieldNames);

}
