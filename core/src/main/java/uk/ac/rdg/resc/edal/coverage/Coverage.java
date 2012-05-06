/*******************************************************************************
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
 *******************************************************************************/

package uk.ac.rdg.resc.edal.coverage;

import java.util.Set;
import uk.ac.rdg.resc.edal.PartialFunction;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;

/**
 * <p>
 * A Coverage associates positions with its {@link #getDomain() domain} to
 * values (its <i>range</i>). It is a partial function because, generally, not
 * all possible positions are associated with values.
 * </p>
 * 
 * @param <P>
 *            The type of object used to identify positions within the
 *            coverage's domain. This may be a spatial, temporal, or combined
 *            spatiotemporal position..
 * @author Jon Blower
 */
public interface Coverage<P> extends PartialFunction<P, Record> {

    /**
     * Returns a human-readable description of this coverage.
     * 
     * @todo Does this belong here or at the Feature level?
     * @return a human-readable description of this coverage.
     */
    public String getDescription();

    /**
     * <p>
     * Returns a Set of unique identifiers, one for
     * each member of the coverage.
     * </p>
     * <p>
     * The identifiers are not (necessarily) intended to be human-readable.
     * </p>
     * 
     * @return a Set of unique identifiers, one for
     *         each member of the coverage
     */
    public Set<String> getMemberNames();
    
    /**
     * Returns the top-level metadata descriptor. This object describes all the
     * members of the Coverage, using nesting where necessary to express semantic
     * relationships between the members.
     * @see the types in the coverage.metadata package
     */
    public RangeMetadata getRangeMetadata();

    /**
     * Returns a description of a particular scalar coverage member.
     * 
     * @param memberName
     *            The unique identifier of the member.
     * @return a description of the values returned by the coverage for a
     *         particular scalar member.
     * @throws IllegalArgumentException
     *             if {@code memberName} is not present in the
     *             {@link #getMemberNames() set of member names}.
     */
    public ScalarMetadata<?> getRangeMetadata(String memberName);
    
    /**
     * Returns a descriptor of the runtime types of the members of the
     * coverage.
     */
    public RecordType getRecordType();

    /**
     * Evaluates the coverage for a specified set of its members.
     * 
     * @param pos
     *            The position at which the coverage is to be evaluated
     * @param memberNames
     *            The set of member names for which the coverage is to be
     *            evaluated.
     * @return a {@link Record} containing a value
     *         for each of the requested members. If {@code memberNames} is
     *         empty, the Record will be empty. If {@code memberNames} is null,
     *         the Record will contain values for every member (i.e. equivalent
     *         to evaluate(pos)).
     */
    public Record evaluate(P pos, Set<String> memberNames);

    /**
     * Evaluates the coverage for all of its members.
     * 
     * @param pos
     *            The position at which the coverage is to be evaluated
     * @return a {@link Record} containing a value
     *         for each of the coverage members.
     */
    @Override
    public Record evaluate(P pos);
    
    /**
     * Evaluates the coverage for one of its members.  Exactly equivalent to
     * evaluate(pos).getValue(memberName);
     * 
     * @param pos
     *            The position at which the coverage is to be evaluated
     * @return the value of the specified member.
     */
    public Object evaluate(P pos, String memberName);

}
