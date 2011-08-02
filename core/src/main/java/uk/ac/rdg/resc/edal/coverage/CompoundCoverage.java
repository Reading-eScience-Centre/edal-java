/*
 * Copyright (c) 2011 The University of Reading
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

import java.util.Set;

/**
 * <p>A CompoundCoverage contains one or more <i>members</i> ({@literal e.g.}
 * temperature, salinity, velocity etc) and returns {@link Record}s that contain
 * values for each member.<p>
 *
 * @param <P> The type of object used to identify positions within the coverage's domain.
 * This may be a spatial, temporal, or combined spatiotemporal position.
 * @author Jon
 */
public interface CompoundCoverage<P> extends Coverage<P, Record>
{
    /**
     * Returns a Set of unique identifiers, one for each member of the coverage.
     * These identifiers are not (necessarily) intended to be human-readable.
     * @return a Set of unique identifiers, one for each member of the coverage.
     */
    public Set<String> getMemberNames();

    /**
     * Returns a description of the values returned by the coverage (including
     * their units, runtime type and the phenomenon they represent) for a particular
     * member.
     * @param memberName The unique identifier of the member
     * @return a description of the values returned by the coverage for a particular
     * member.
     * @throws IllegalArgumentException if {@code memberName} is not present in
     * the {@link #getMemberNames() set of member names}.
     */
    public RangeMetadata getRangeMetadata(String memberName);

    /**
     * Evaluates the coverage for a specified set of its members.
     * @param pos The position at which the coverage is to be evaluated
     * @param memberNames The set of member names for which the coverage is to
     * be evaluated.
     * @return A {@link Record} containing a value for each of the requested
     * members.  If {@code memberNames} is empty, the Record will be empty.
     * If {@code memberNames} is null, the Record will contain values for every
     * member (i.e. equivalent to evaluate(pos)).  If the coverage is not defined
     * at the given position, this will return null.
     */
    public Record evaluate(P pos, Set<String> memberNames);
}
