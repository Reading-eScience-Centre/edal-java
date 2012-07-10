/**
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

import java.util.Set;

import uk.ac.rdg.resc.edal.coverage.Coverage;
import uk.ac.rdg.resc.edal.coverage.RecordType;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;

/**
 * <p>
 * Partial implementation of a {@link Coverage}, providing a simpler way for
 * subclasses to provide key items of metadata.
 * </p>
 * 
 * @param <P>
 *            The type of object used to identify positions within the
 *            coverage's domain. This may be a spatial, temporal, or combined
 *            spatiotemporal position.
 * @author Jon
 * @author Guy Griffiths
 */
public abstract class AbstractCoverage<P> implements Coverage<P> {

    @Override
    public boolean isDefinedAt(P pos) {
        return this.getDomain().contains(pos);
    }

    private final RecordType rangeType = new RecordType() {
        @Override
        public Class<?> getValueType(String memberName) {
            checkMemberName(memberName);
            return AbstractCoverage.this.getScalarMetadata(memberName).getValueType();
        }

        @Override
        public Set<String> getMemberNames() {
            return AbstractCoverage.this.getScalarMemberNames();
        }
    };

    @Override
    public RecordType getRangeType() {
        return this.rangeType;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This default implementation returns a "plain" RangeMetadata object
     * containing all the ScalarMetadata objects as direct children in a flat
     * hierarchy. Subclasses should override where appropriate to provide more
     * accurate and expressive metadata relationships using the RangeMetadata
     * subclasses.
     * </p>
     * 
     * @return
     */
    @Override
    public RangeMetadata getRangeMetadata() {
        final AbstractCoverage<P> cov = AbstractCoverage.this;
        return new RangeMetadata() {

            @Override
            public String getName() {
                return "TODO not sure what to put here!";
            }

            @Override
            public String getDescription() {
                return cov.getDescription();
            }

            @Override
            public Set<String> getMemberNames() {
                return cov.getScalarMemberNames();
            }

            @Override
            public ScalarMetadata getMemberMetadata(String name) {
                return cov.getScalarMetadata(name);
            }

            @Override
            public RangeMetadata getParent() {
                // This is the top-level metadata object: there is no parent
                return null;
            }

            @Override
            public RangeMetadata removeMember(String memberName) {
                throw new UnsupportedOperationException("This RangeMetadata is immutable");
            }

            @Override
            public void addMember(RangeMetadata metadata) {
                throw new UnsupportedOperationException("This RangeMetadata is immutable");
            }

            @Override
            public RangeMetadata clone() throws CloneNotSupportedException {
                return AbstractCoverage.this.getRangeMetadata();
            }

            @Override
            public void setParentMetadata(RangeMetadata metadata) {
                throw new UnsupportedOperationException("This is a top-level RangeMetadata object");
            }
        };
    }

    protected void checkMemberName(String memberName) {
        if (!this.getScalarMemberNames().contains(memberName)) {
            throw new IllegalArgumentException("Member name " + memberName
                    + " is not a scalar member of this coverage");
        }
    }

}
