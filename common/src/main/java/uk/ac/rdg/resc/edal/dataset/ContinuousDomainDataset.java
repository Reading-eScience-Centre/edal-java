/*******************************************************************************
 * Copyright (c) 2014 The University of Reading
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

package uk.ac.rdg.resc.edal.dataset;

import java.util.Collection;
import java.util.Set;

import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;

public interface ContinuousDomainDataset<F extends DiscreteFeature<?, ?>> extends Dataset<F> {
    /**
     * Extracts a {@link Collection} of {@link DiscreteFeature}s from this
     * {@link Dataset}
     * 
     * @param varIds
     *            The variables to extract. If this is <code>null</code>, all
     *            available variables (determined by the result of
     *            {@link ContinuousDomainDataset#getVariableIds()}) will be read
     * @param hExtent
     *            The target {@link BoundingBox}. If <code>null</code> no
     *            constraint should be put on the horizontal domain.
     * @param zExtent
     *            The vertical range to extract data from. Any features which
     *            have values within this vertical range (as well as the
     *            specified {@link BoundingBox} and time range) will be read. If
     *            <code>null</code> no constraint should be put on the vertical
     *            domain.
     * @param tExtent
     *            The time range to extract data from. Any features which have
     *            values within this time range (as well as the specified
     *            {@link BoundingBox} and time range) will be read. If
     *            <code>null</code> no constraint should be put on the temporal
     *            domain.
     * @return The extracted {@link DiscreteFeature}s
     * @throws DataReadingException
     *             If the underlying data cannot be read for any reason
     */
    public Collection<F> extractFeatures(Set<String> varIds, BoundingBox hExtent,
            Extent<Double> zExtent, Extent<DateTime> tExtent) throws DataReadingException;
}
