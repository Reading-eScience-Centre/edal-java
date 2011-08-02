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

import uk.ac.rdg.resc.edal.PartialFunction;
import uk.ac.rdg.resc.edal.Domain;

/**
 * <p>A Coverage associates positions with its {@link #getDomain() domain}
 * to values (its <i>range</i>).  It is a partial function because, generally,
 * not all possible positions are associated with values.</p>
 * <p>Coverages may return a single value for each position (in which case they
 * may be modelled as {@link SimpleCoverage}s), or they may return multiple values
 * for each position ({@link CompoundCoverage}s).</p>
 *
 * @param <P> The type of object used to identify positions within the coverage's domain.
 * This may be a spatial, temporal, or combined spatiotemporal position.
 * @param <R> The type of the value returned by the coverage
 * @author Jon
 */
public interface Coverage<P, R> extends PartialFunction<P, R>
{

    /**
     * Returns a human-readable description of this coverage.
     * @todo Does this belong here or at the Feature level?
     * @return a human-readable description of this coverage.
     */
    public String getDescription();

    /**
     * The runtime type of the values of the coverage's range.
     * @todo confusing nomenclature?  ISO standard would have returned a RecordType.
     */
    public Class<R> getRangeType();

}
