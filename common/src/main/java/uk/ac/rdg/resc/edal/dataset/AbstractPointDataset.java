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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.PointFeature;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.util.PlottingDomainParams;

/**
 * An {@link AbstractContinuousDomainDataset} whose map features are
 * {@link PointFeature}s. Subclasses must provide a method to convert from their
 * underlying feature type to a {@link PointFeature}, given a set of
 * {@link PlottingDomainParams}.
 * 
 * @param <F>
 *            The type of {@link DiscreteFeature} which this
 *            {@link AbstractPointDataset} reads natively (i.e. the same type of
 *            {@link DiscreteFeature} which is returned by the
 *            {@link DiscreteFeatureReader} associated with this
 *            {@link AbstractPointDataset}).
 * 
 * @author Guy Griffiths
 */
public abstract class AbstractPointDataset<F extends DiscreteFeature<?, ?>> extends
        AbstractContinuousDomainDataset {

    public AbstractPointDataset(String id, Collection<? extends VariableMetadata> vars,
            FeatureIndexer featureIndexer) {
        super(id, vars, featureIndexer);
    }

    @Override
    public List<PointFeature> extractMapFeatures(Set<String> varIds, PlottingDomainParams params)
            throws DataReadingException {
        List<? extends DiscreteFeature<?, ?>> extractedMapFeatures = super.extractMapFeatures(
                varIds, params);
        List<PointFeature> pointFeatures = new ArrayList<>();
        for (DiscreteFeature<?, ?> feature : extractedMapFeatures) {
            /*
             * This conversion is safe because:
             * 
             * AbstractContinuousDomainDataset reads all features with
             * getFeatureReader().readFeatures()
             * 
             * This class overrides getFeatureReader() to ensure that it returns
             * features of type F
             */
            @SuppressWarnings("unchecked")
            PointFeature pointFeature = convertFeature((F) feature, params);
            if (pointFeature != null) {
                pointFeatures.add(pointFeature);
            }
        }
        return pointFeatures;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Always returns a PointFeature - this is the point of this class. For
     * mixed feature types, extend directly from
     * AbstractContinuousDomainDataset.
     */
    @Override
    public final Class<PointFeature> getMapFeatureType(String variableId) {
        return PointFeature.class;
    }

    /**
     * Convert a {@link DiscreteFeature} of type F to a {@link PointFeature}
     * 
     * @param feature
     *            The feature to convert
     * @param params
     *            The {@link PlottingDomainParams} under which the feature
     *            should be converted
     * @return A {@link PointFeature} ready for plotting, or <code>null</code>
     *         if the supplied {@link PlottingDomainParams} specify a location
     *         where no {@link PointFeature} is present.
     */
    protected abstract PointFeature convertFeature(F feature, PlottingDomainParams params);

    @Override
    public abstract DiscreteFeatureReader<F> getFeatureReader();
}
