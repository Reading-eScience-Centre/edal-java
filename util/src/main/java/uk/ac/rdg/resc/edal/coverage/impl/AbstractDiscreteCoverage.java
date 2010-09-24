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

package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.AbstractList;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.rdg.resc.edal.coverage.DiscreteCoverage;
import uk.ac.rdg.resc.edal.coverage.Record;
import uk.ac.rdg.resc.edal.util.CollectionUtils;


/**
 * Skeletal implementation of a {@link DiscreteCoverage}.  Subclasses provide
 * implementations of {@link #getRange(java.lang.String)} for each field name
 * and this implementation automatically generates {@link Record}s.
 * @param <P> The type of object used to identify positions within the domain.
 * @param <DO> The type of the domain objects
 * @author Jon
 */
public abstract class AbstractDiscreteCoverage<P, DO> implements DiscreteCoverage<P, DO> {

    @Override
    public List<Record> getRange() {
        return new AbstractList<Record>() {

            @Override
            public Record get(int index) {
                return AbstractDiscreteCoverage.this.getRecord(index, null);
            }

            @Override
            public int size() {
                return AbstractDiscreteCoverage.this.size();
            }

        };
    }

    /**
     * Returns the domain object and its associated Record corresponding with
     * the given position, or null if the position is outside the coverage's
     * domain.
     */
    public Entry<DO, Record> locate(P position)
    {
        int domainObjectIndex = this.getDomain().findIndexOf(position);
        if (domainObjectIndex < 0) return null;
        DO domainObject = this.getDomain().getDomainObjects().get(domainObjectIndex);
        Record record = this.getRecord(domainObjectIndex, null);
        return new SimpleEntry<DO, Record>(domainObject, record);
    }

    @Override
    public List<Entry<DO, Record>> list() {
        return new AbstractList<Entry<DO, Record>>() {

            @Override
            public Entry<DO, Record> get(int index) {
                DO domainObject = AbstractDiscreteCoverage.this.getDomain().getDomainObjects().get(index);
                Record record = AbstractDiscreteCoverage.this.getRecord(index, null);
                return new SimpleEntry<DO, Record>(domainObject, record);
            }

            @Override
            public int size() {
                return AbstractDiscreteCoverage.this.size();
            }

        };
    }

    /**
     * Gets the record corresponding with the domain object at the given index
     */
    protected Record getRecord(int index, Collection<String> fieldNames) {
        // Check the value of index
        if (index < 0 || index >= this.size()) {
            throw new IndexOutOfBoundsException("" + index);
        }
        // If not specified, use all the field names
        if (fieldNames == null) fieldNames = this.getRangeType().getFieldNames();
        // Build up the Record object
        Map<String, Object> values = CollectionUtils.newHashMap();
        for (String field : fieldNames) {
            Object value = AbstractDiscreteCoverage.this.getRange(field).get(index);
            values.put(field, value);
        }
        return new SimpleRecord(values, this.getRangeType());
    }

    /**
     * {@inheritDoc}
     * <p>This implementation delegates to this.getDomain().size()</p>
     */
    @Override
    public int size() {
        return this.getDomain().size();
    }

    @Override
    public Record evaluate(P position, Collection<String> fieldNames)
    {
        int domainObjectIndex = this.getDomain().findIndexOf(position);
        if (domainObjectIndex < 0) return null;
        return this.getRecord(domainObjectIndex, fieldNames);
    }


}
