/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
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
 ******************************************************************************/

package uk.ac.rdg.resc.edal.feature;

import uk.ac.rdg.resc.edal.domain.DiscreteDomain;
import uk.ac.rdg.resc.edal.util.Array;

/**
 * <p>
 * A {@link Feature} whose domain consists of a finite number of domain objects,
 * each of which is associated with a single measurement value from each Feature
 * member.
 * </p>
 * 
 * @param <P>
 *            The type of object used to identify positions within the feature's
 *            domain. This may be a spatial, temporal, or combined
 *            spatiotemporal position.
 * @param <DO>
 *            The type of domain object
 * @author Jon Blower
 */
public interface DiscreteFeature<P, DO> extends Feature<P> {
    /**
     * Gets the array of values for the given parameter.  The shape of this array
     * must match the shape of the array of domain objects
     * (from {@link DiscreteDomain#getDomainObjects()}).
     * 
     * @param paramId
     *            The identifier from the {@link #getParameterIds()  set of 
     *            parameter IDs.
     * @return the list of values for the requested member
     */
    public Array<Number> getValues(String paramId);

    /**
     * {@inheritDoc}
     */
    @Override
    public DiscreteDomain<P, DO> getDomain();

    /**
     * Gets the number of distinct values in this coverage. (Equivalent to
     * {@code getDomain().size()}.)
     * 
     * TODO Is there any point in this being a separate method?
     * 
     * @return the number of distinct values in this coverage.
     */
    public long size();

}
