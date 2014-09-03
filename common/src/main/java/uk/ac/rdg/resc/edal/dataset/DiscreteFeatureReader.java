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

import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;

/**
 * Defines an interface for reading {@link DiscreteFeature}s. This is used for
 * reading features from a collection (usually an
 * {@link AbstractContinuousDomainDataset}) where the primary method of
 * referencing them is by their ID.
 * 
 * @author Guy Griffiths
 * 
 * @param <F>
 *            The concrete type of the {@link DiscreteFeature} to be read by
 *            this {@link DiscreteFeatureReader}
 */
public interface DiscreteFeatureReader<F extends DiscreteFeature<?, ?>> {
    /**
     * Reads a {@link DiscreteFeature} from the underlying data source. This
     * method should be used when only a single feature needs to be read. For
     * multiple features it is recommended to use the
     * {@link DiscreteFeatureReader#readFeatures(Collection, Set)} method since
     * invoking this method multiple times may result in a large amount of
     * overhead re-opening and closing data files.
     * 
     * 
     * @param id
     *            The identifier of the feature within its collection.
     * @param variableIds
     *            The variables which must be included in the feature.
     *            Additional variables can be present if this is more convenient
     *            (i.e. if entire features are already stored in memory there is
     *            no need to subset them to exclude unwanted variables).
     * 
     *            If this argument is <code>null</code> then all available
     *            variables should be included
     * @return A {@link DiscreteFeature} containing measurements for all of the
     *         supplied variables.
     */
    public F readFeature(String id, Set<String> variableIds) throws DataReadingException;

    /**
     * Reads {@link DiscreteFeature}s from the underlying data source.
     * 
     * @param ids
     *            The identifier of the features within their collection.
     * @param variableIds
     *            The variables which must be included in the feature.
     *            Additional variables can be present if this is more convenient
     *            (i.e. if entire features are already stored in memory there is
     *            no need to subset them to exclude unwanted variables).
     * 
     *            If this argument is <code>null</code> then all available
     *            variables should be included
     * @return A {@link Collection} of {@link DiscreteFeature}s containing
     *         measurements for all of the supplied variables.
     */
    public Collection<F> readFeatures(Collection<String> ids, Set<String> variableIds)
            throws DataReadingException;
}
