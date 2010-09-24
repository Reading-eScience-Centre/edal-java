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

package uk.ac.rdg.resc.edal.coverage;

import java.util.List;
import java.util.Map;
import uk.ac.rdg.resc.edal.coverage.domain.DiscreteDomain;
import uk.ac.rdg.resc.edal.coverage.grid.GridPoint;

/**
 * <p>A {@link Coverage} whose domain consists of a finite number of domain
 * objects, each of which is associated with a single {@link Record} of measurement
 * values.</p>
 * <p>A DiscreteCoverage is therefore rather like a Map of domain objects to
 * records.  It would be tempting to specify an asMap() method in this interface,
 * but this would not work well for some subclasses, such as the
 * {@link DiscreteGridPointCoverage}, in which the domain objects are {@link
 * GridPoint}s, which are hard for clients to generate.</p>
 * @param <P> The type of object used to identify positions within the domain.
 * @param <DO> The type of domain object
 * @author Jon
 */
public interface DiscreteCoverage<P, DO> extends Coverage<P>
{

    @Override
    public DiscreteDomain<P, DO> getDomain();

    /**
     * Gets the list of objects that comprise this coverage's range.  There
     * will be one entry in the list for each domain object, in the same order.
     * @return the list of objects that comprise this coverage's range.
     */
    public List<Record> getRange();

    /**
     * Gets the list of values for a particular field.  The type of the objects
     * in the returned list must match the runtime class of the field in question,
     * as given by {@link #getRangeType()}.{@link RecordType#getClass(java.lang.String)}.
     * There will be one entry in the list for each domain object, in the same order.
     * @return the list of values for a particular field.
     * @throws IllegalArgumentException if {@code fieldName} is not a valid field
     * name for this coverage.
     */
    public List<?> getRange(String fieldName);

    /**
     * Returns the domain object and its associated Record corresponding with
     * the given position, or null if the position is outside the coverage's
     * domain.
     */
    public Map.Entry<DO, Record> locate(P position);

    /**
     * Gets all the domain-object/record pairs
     * @return
     */
    public List<Map.Entry<DO, Record>> list();

    /**
     * Gets the number of distinct values in this coverage.  (Equivalent to
     * {@code getDomain().size()}.)
     * @return the number of distinct values in this coverage.
     */
    public int size();

}
