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

package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.rdg.resc.edal.coverage.DiscreteCoverage;
import uk.ac.rdg.resc.edal.coverage.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.Record;

/**
 * <p>Partial implementation of a simple {@link DiscreteCoverage}, providing default
 * implementations of some methods.</p>
 * @param <P> The type of object used to identify positions within the coverage's domain.
 * This may be a spatial, temporal, or combined spatiotemporal position.
 * @param <DO> The type of domain object
 * @param <R> The type of the value returned by the coverage; for a compound
 * coverage this type will be {@link Record}.
 * @author Jon
 */
public abstract class AbstractDiscreteSimpleCoverage<P, DO, R>
        extends AbstractDiscreteCoverage<P, DO, R>
{

    private static final Set<String> MEMBER_NAMES;

    static {
        Set<String> members = new HashSet<String>(1);
        members.add(null);
        MEMBER_NAMES = Collections.unmodifiableSet(members);
    }

    @Override
    public final boolean isCompound() { return false; }

    @Override
    public final List<?> getValues(String memberName) {
        if (memberName == null) return this.getValues();
        throw new IllegalArgumentException("For a simple coverage, memberName must be null");
    }

    @Override
    public final Set<String> getMemberNames() { return MEMBER_NAMES; }

    @Override
    public final RangeMetadata getRangeMetadata(String memberName) {
        if (memberName == null) return this.getRangeMetadata();
        throw new IllegalArgumentException("For a simple coverage, memberName must be null");
    }

    protected abstract RangeMetadata getRangeMetadata();

    @Override
    public final R evaluate(P pos, Set<String> memberNames) {
        if (memberNames == null || memberNames.equals(MEMBER_NAMES)) {
            return this.evaluate(pos);
        }
        throw new IllegalArgumentException("For a simple coverage, memberNames "
                + "must be null or a set containing a single null element");
    }

}
