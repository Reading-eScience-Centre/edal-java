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

package uk.ac.rdg.resc.edal.coverage.domain;

import uk.ac.rdg.resc.edal.Domain;
import java.util.List;
import java.util.Set;
import uk.ac.rdg.resc.edal.coverage.DiscreteCoverage;

/**
 * A {@link Domain} that consists of a finite number of discrete objects.
 * {@link DiscreteCoverage}s use this type of domain.
 * @param <P> The type of object used to identify positions within this domain
 * @param <DO> The type of the domain object
 * @author Jon
 */
public interface DiscreteDomain<P, DO> extends Domain<P>
{

    /**
     * Returns true if at least one of the domain objects contains the given
     * position.
     */
    @Override public boolean contains(P position);

    /**
     * Returns the {@link List} of domain objects that comprise this domain.
     * The domain objects are in a defined order, hence we use a {@link List};
     * however, the domain objects will also each be unique, so the returned list
     * will also have the semantics of a {@link Set}.  (It is impossible in Java
     * to define an object that correctly implements both the List and Set
     * interfaces simultaneously.)
     */
    public List<DO> getDomainObjects();

    /**
     * Returns the index of the first domain object in the {@link #getDomainObjects()
     * list of domain objects} that contains the given position, or -1 if the
     * position is outside the domain.
     */
    public int findIndexOf(P position);

    /**
     * Returns the number of domain objects in the domain (equivalent to
     * {@link #getDomainObjects()}.size()).
     * @return the number of domain objects in the domain
     */
    public int size();

}
