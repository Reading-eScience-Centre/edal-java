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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.rdg.resc.edal.coverage.DiscreteCoverage;
import uk.ac.rdg.resc.edal.coverage.DomainObjectValuePair;
import uk.ac.rdg.resc.edal.coverage.Record;
import uk.ac.rdg.resc.edal.coverage.domain.DiscreteDomain;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.util.AbstractBigList;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * <p>
 * Partial implementation of a {@link DiscreteCoverage}, providing default
 * implementations of some methods.
 * </p>
 * 
 * @param <P>
 *            The type of object used to identify positions within the
 *            coverage's domain. This may be a spatial, temporal, or combined
 *            spatiotemporal position.
 * @param <DO>
 *            The type of domain object
 * @author Jon
 * @author Guy Griffiths
 */
public abstract class AbstractDiscreteCoverage<P, DO> extends AbstractCoverage<P> implements
        DiscreteCoverage<P, DO> {

    /**
     * Partial implementation of {@link BigList}, which can be used by
     * subclasses of AbstractDiscreteCoverage. Takes its size from the size of
     * the Coverage (which in turn takes it from the size of the domain).
     */
    protected abstract class AbstractBigList2<E> extends AbstractBigList<E> {
        @Override
        public long sizeAsLong() {
            return AbstractDiscreteCoverage.this.size();
        }
    }

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
            return this.map.keySet();
        }

        @Override
        public ScalarMetadata getRangeMetadata(String memberName) {
            return AbstractDiscreteCoverage.this.getRangeMetadata(memberName);
        }
    }

    private final BigList<Record> recordList = new AbstractBigList2<Record>() {
        @Override
        public Record get(long index) {
            return getRecord(index, getMemberNames());
        }
    };

    @Override
    public BigList<Record> getValues() {
        return this.recordList;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Some Coverages can get very large, so we specialize to a {@link BigList}.
     * Subclasses can use the {@link AbstractBigList2} abstract class to provide
     * an easier way to implement this.
     * </p>
     */
    @Override
    public abstract BigList<?> getValues(String memberName);

    private final BigList<DomainObjectValuePair<DO>> dovpList = new AbstractBigList2<DomainObjectValuePair<DO>>() {
        @Override
        public DomainObjectValuePair<DO> get(long i) {
            if (i < 0 || i >= this.size()) {
                throw new IndexOutOfBoundsException("Index " + i + " out of bounds");
            }
            return AbstractDiscreteCoverage.this.getDvp(i);
        }
    };

    @Override
    public BigList<DomainObjectValuePair<DO>> list() {
        return this.dovpList;
    }

    private DomainObjectValuePair<DO> getDvp(long i) {
        List<DO> domainObjects = this.getDomain().getDomainObjects();
        final DO domainObject;
        // Some domains can be big and are specialized to BigList
        if (domainObjects instanceof BigList) {
            BigList<DO> bigList = (BigList<DO>) domainObjects;
            domainObject = bigList.get(i);
        } else {
            domainObject = domainObjects.get((int) i);
        }
        final Record value = this.getValues().get(i);

        return new DomainObjectValuePair<DO>() {
            @Override
            public DO getDomainObject() {
                return domainObject;
            }

            @Override
            public Record getValue() {
                return value;
            }
        };
    }

    @Override
    public DomainObjectValuePair<DO> locate(P pos) {
        DiscreteDomain<P, DO> domain = this.getDomain();
        int i = (int) domain.findIndexOf(pos);
        if (i < 0)
            return null;
        return this.getDvp(i);
    }

    @Override
    public long size() {
        return this.getDomain().size();
    }

    @Override
    public Record evaluate(P pos) {
        return this.evaluate(pos, this.getMemberNames());
    }

    @Override
    public Object evaluate(P pos, String memberName) {
        this.checkMemberName(memberName);
        long i = this.getDomain().findIndexOf(pos);
        if (i < 0)
            return null;
        return getMemberValue(i, memberName);
    }

    @Override
    public Record evaluate(P pos, Set<String> memberNames) {
        long i = this.getDomain().findIndexOf(pos);
        if (i < 0)
            return null;
        return this.getRecord(i, memberNames);
    }

    private Object getMemberValue(long index, String memberName) {
        BigList<?> memberValues = this.getValues(memberName);
        return memberValues.get(index);
    }

    private Record getRecord(long index, Set<String> memberNames) {
        Map<String, Object> map = new HashMap<String, Object>();

        for (String memberName : memberNames) {
            this.checkMemberName(memberName);
            BigList<?> memberValues = this.getValues(memberName);
            System.out.println(memberName + "," + index);
            Object value = memberValues.get(index);
            map.put(memberName, value);
        }
        return new SimpleRecord(map);
    }

}
