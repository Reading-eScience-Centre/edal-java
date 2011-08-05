/*
 * Copyright (c) 2008 The University of Reading
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
 * Contains the data values returned by a {@link CompoundCoverage}.
 * @author Jon
 */
public interface Record {
    
    /**
     * <p>Gets the value of the given coverage member..</p>
     * <p><i>This is called locate() in GeoAPI - I don't know why (presumably this
     * reflects the standard, but it seems like an odd name).</i></p>
     * @param memberName The name of a member of this record as provided by
     * the Coverage's {@link CompoundCoverage#getMemberNames() set of member
     * names}.
     * @return the value of the given member. The runtime type of the
     * value is given by the {@link RangeMetadata} object associated with the
     * parent Coverage.
     * @throws IllegalArgumentException if {@code memberName} is not a valid
     * member name
     */
    public Object getValue(String memberName);

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

}
