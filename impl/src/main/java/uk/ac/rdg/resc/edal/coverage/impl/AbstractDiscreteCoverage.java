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

import java.util.AbstractList;
import java.util.List;

import uk.ac.rdg.resc.edal.coverage.DiscreteCoverage;
import uk.ac.rdg.resc.edal.coverage.DomainObjectValuePair;
import uk.ac.rdg.resc.edal.coverage.Record;
import uk.ac.rdg.resc.edal.coverage.domain.DiscreteDomain;

/**
 * <p>Partial implementation of a {@link DiscreteCoverage}, providing default
 * implementations of some methods.  Subclasses should usually inherit from
 * {@link AbstractDiscreteCompoundCoverage} or {@link AbstractDiscreteSimpleCoverage}
 * as appropriate.</p>
 * @param <P> The type of object used to identify positions within the coverage's domain.
 * This may be a spatial, temporal, or combined spatiotemporal position.
 * @param <DO> The type of domain object
 * @param <R> The type of the value returned by the coverage; for a compound
 * coverage this type will be {@link Record}.
 * @author Jon
 */
public abstract class AbstractDiscreteCoverage<P, DO, R> implements DiscreteCoverage<P, DO, R>
{

    private final List<DomainObjectValuePair<DO, R>> dovpList =
            new AbstractList<DomainObjectValuePair<DO, R>>() {

        @Override
        public DomainObjectValuePair<DO, R> get(int i) {
            if (i < 0 || i >= this.size()) {
                throw new IndexOutOfBoundsException("Index " + i + " out of bounds");
            }
            return AbstractDiscreteCoverage.this.getDvp(i);
        }

        @Override
        public int size() { return (int)AbstractDiscreteCoverage.this.size(); }
        
    };
    
    private DomainObjectValuePair<DO, R> getDvp(int i) {
        final DO domainObject = this.getDomain().getDomainObjects().get(i);
        final R value = this.getValues().get(i);

        return new DomainObjectValuePair<DO, R>() {
            @Override public DO getDomainObject() { return domainObject; }
            @Override public R getValue() { return value; }
        };
    }

    @Override
    public DomainObjectValuePair<DO, R> locate(P pos) {
        DiscreteDomain<P, DO> domain = this.getDomain();
        int i = (int)domain.findIndexOf(pos);
        if (i < 0) return null;
        return this.getDvp(i);
    }

    @Override
    public List<DomainObjectValuePair<DO, R>> list() {
        return this.dovpList;
    }

    @Override
    public long size() { return this.getDomain().size(); }

    @Override
    public boolean isDefinedAt(P pos) {
        return this.getDomain().contains(pos);
    }

    @Override
    public R evaluate(P pos) {
        int i = (int)this.getDomain().findIndexOf(pos);
        if (i < 0 || this.getValues().isEmpty()) return null;
        return this.getValues().get(i);
    }

}
