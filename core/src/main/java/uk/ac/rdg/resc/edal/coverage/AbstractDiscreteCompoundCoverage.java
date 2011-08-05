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

import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>Partial implementation of a {@link DiscreteCompoundCoverage}, providing default
 * implementations of some methods.</p>
 * @param <P> The type of object used to identify positions within the coverage's domain.
 * This may be a spatial, temporal, or combined spatiotemporal position.
 * @param <DO> The type of domain object
 * @author Jon
 */
public abstract class AbstractDiscreteCompoundCoverage<P, DO>
        extends AbstractDiscreteCoverage<P, DO, Record>
        implements DiscreteCompoundCoverage<P, DO>
{
    private final class SimpleRecord implements Record {

        private final Map<String, Object> map;

        public SimpleRecord(Map<String, Object> map) {
            this.map = map;
        }

        @Override
        public Object getValue(String memberName) {
            return this.map.get(memberName);
        }

        @Override
        public Set<String> getMemberNames() {
            return AbstractDiscreteCompoundCoverage.this.getMemberNames();
        }

        @Override
        public RangeMetadata getRangeMetadata(String memberName) {
            return AbstractDiscreteCompoundCoverage.this.getRangeMetadata(memberName);
        }
    }

    private final List<Record> recordList = new AbstractList<Record>() {

        @Override
        public Record get(int index) {
            Map<String, Object> map = new HashMap<String, Object>();
            AbstractDiscreteCompoundCoverage<P, DO> cov = AbstractDiscreteCompoundCoverage.this;
            for (String memberName : cov.getMemberNames()) {
                List<?> memberValues = cov.getValues(memberName);
                if (memberValues == null) {
                    throw new IllegalStateException("Values missing for member " + memberName);
                }
                Object value = memberValues.get(index);
                map.put(memberName, value);
            }
            return new SimpleRecord(map);
        }

        @Override
        public int size() { return AbstractDiscreteCompoundCoverage.this.size(); }

    };

    @Override
    public List<Record> getValues() { return this.recordList; }

    @Override
    public Record evaluate(P pos, Set<String> memberNames) {
        int i = this.getDomain().findIndexOf(pos);
        if (i < 0) return null;

        Map<String, Object> map = new HashMap<String, Object>();

        for (String memberName : memberNames) {
            if (!this.getMemberNames().contains(memberName)) {
                // The user has asked for a member that doesn't exist
                // This is an error on the part of the user of this class
                throw new IllegalArgumentException("Coverage member " + memberName + " does not exist");
            }
            List<?> memberValues = this.getValues(memberName);
            if (memberValues == null) {
                // The coverage member is supposed to exist but the programmer
                // hasn't provided it.
                // This is an error on the part of the programmer
                throw new IllegalStateException("Values missing for member " + memberName);
            }
            Object value = memberValues.get(i);
            map.put(memberName, value);
        }
        return new SimpleRecord(map);
    }

    @Override
    public final Class<Record> getRangeType() { return Record.class; }
}
