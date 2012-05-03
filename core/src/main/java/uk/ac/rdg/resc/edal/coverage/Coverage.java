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

/**
 * <p>
 * A Coverage associates positions with its {@link #getDomain() domain} to
 * values (its <i>range</i>). It is a partial function because, generally, not
 * all possible positions are associated with values.
 * </p>
 * <p>
 * Coverages may return a single value for each position (in which case they are
 * <i>simple</i> coveraages), or they may return multiple values for each
 * position in {@link Record} objects (in which case they are <i>compound</i>
 * coverage).
 * </p>
 * 
 * @param <P>
 *            The type of object used to identify positions within the
 *            coverage's domain. This may be a spatial, temporal, or combined
 *            spatiotemporal position.
 * @param <R>
 *            The type of the value returned by the coverage; for a compound
 *            coverage this type will be {@link Record}.
 * @author Jon Blower
 */
public interface Coverage<P, R> extends PartialFunction<P, R> {

    /**
     * Returns true if this is a compound coverage. For compound coverages the
     * type {@code R} will be {@link Record}.
     * 
     * @return true if this is a compound coverage
     */
    public boolean isCompound();

    /**
     * Returns a human-readable description of this coverage.
     * 
     * @todo Does this belong here or at the Feature level?
     * @return a human-readable description of this coverage.
     */
    public String getDescription();

    /**
     * <p>
     * For a compound coverage, returns a Set of unique identifiers, one for
     * each member of the coverage; for a simple coverage, returns a Set
     * containing a single null member. This allows the following code to work
     * for both simple and compound coverages:
     * </p>
     * 
     * <pre>
     * for (String member : coverage.getMemberNames()) {
     *     RangeMetadata meta = coverage.getRangeMetadata(member);
     *     ... // do something useful
     * }
     * </pre>
     * <p>
     * (and similarly for {@link DiscreteCoverage#getValues(java.lang.String)})
     * </p>
     * <p>
     * The identifiers are not (necessarily) intended to be human-readable.
     * </p>
     * 
     * @return Compound coverages return a Set of unique identifiers, one for
     *         each member of the coverage; simple coverages return a Set
     *         containing a single null value.
     */
    public Set<String> getMemberNames();
    
    /**
     * Returns the top-level metadata descriptor. This object describes all the
     * members of the Coverage, using nesting to express semantic relationships
     * between the members.
     */
    public RangeMetadata getRangeMetadata();

    /**
     * Returns a description of a particular coverage member.
     * 
     * @param memberName
     *            The unique identifier of the member.  For simple coverages,
     *            only null is allowed.
     * @return a description of the values returned by the coverage for a
     *         particular member.
     * @throws IllegalArgumentException
     *             if {@code memberName} is not present in the
     *             {@link #getMemberNames() set of member names}.
     */
    public RangeMetadata getRangeMetadata(String memberName);

    /**
     * Evaluates the coverage for a specified set of its members.
     * 
     * @param pos
     *            The position at which the coverage is to be evaluated
     * @param memberNames
     *            The set of member names for which the coverage is to be
     *            evaluated. For a simple coverage, {@code memberNames} can be
     *            null, or can be a Set containing a single null member.
     * @return A compound coverage returns a {@link Record} containing a value
     *         for each of the requested members. If {@code memberNames} is
     *         empty, the Record will be empty. If {@code memberNames} is null,
     *         the Record will contain values for every member (i.e. equivalent
     *         to evaluate(pos)). A simple coverage returns a value of the
     *         appropriate type {@code R}. If the coverage is not defined at the
     *         given position, this will return null.
     */
    public R evaluate(P pos, Set<String> memberNames);

    /**
     * Evaluates the coverage for all of its members.
     * 
     * @param pos
     *            The position at which the coverage is to be evaluated
     * @return A compound coverage returns a {@link Record} containing a value
     *         for all of its members. A simple coverage returns a value of the
     *         appropriate type {@code R}. If the coverage is not defined at the
     *         given position, this will return null.
     */
    @Override
    public R evaluate(P pos);

}
