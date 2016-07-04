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

package uk.ac.rdg.resc.edal.graphics.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.rdg.resc.edal.dataset.PointDataset;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.HorizontallyDiscreteDataset;
import uk.ac.rdg.resc.edal.domain.MapDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.exceptions.VariableNotFoundException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

public class SimpleFeatureCatalogue<D extends Dataset> implements FeatureCatalogue {
    protected boolean cacheEnabled = false;
    private Map<String, Map<PlottingDomainParams, List<? extends DiscreteFeature<?, ?>>>> features = null;
    private D dataset;

    public SimpleFeatureCatalogue(D dataset, boolean cacheEnabled) throws IOException,
            EdalException {
        this.dataset = dataset;
        this.cacheEnabled = cacheEnabled;
        features = new HashMap<>();
    }

    public D getDataset() {
        return dataset;
    }

    @Override
    public FeaturesAndMemberName getFeaturesForLayer(String id, PlottingDomainParams params)
            throws EdalException {
        return new FeaturesAndMemberName(getMapFeature(params, id, cacheEnabled), id);
    }

    public void expireFromCache(String varId) {
        if (features.containsKey(varId)) {
            features.remove(varId);
        }
    }

    protected List<? extends DiscreteFeature<?, ?>> getMapFeature(PlottingDomainParams params,
            String varId, boolean cache) throws DataReadingException, VariableNotFoundException {
        /*
         * We do caching first by variable ID and then by plotting parameters.
         * This allows us to expire certain variables from the cache on demand
         */
        Map<PlottingDomainParams, List<? extends DiscreteFeature<?, ?>>> varCache;
        if (features.containsKey(varId)) {
            varCache = features.get(varId);
        } else {
            varCache = new HashMap<>();
            features.put(varId, varCache);
        }

        if (varCache.containsKey(params)) {
            return varCache.get(params);
        } else {
            List<? extends DiscreteFeature<?, ?>> extractedFeatures = new ArrayList<>();
            if (dataset instanceof HorizontallyDiscreteDataset<?>) {
                HorizontallyDiscreteDataset<?> discreteDataset = (HorizontallyDiscreteDataset<?>) dataset;
                extractedFeatures = discreteDataset
                        .extractMapFeatures(
                                CollectionUtils.setOf(varId),
                                new MapDomain(new RegularGridImpl(params.getBbox(), params
                                        .getWidth(), params.getHeight()), params.getTargetZ(),
                                        null, params.getTargetT()));
            } else if (dataset instanceof PointDataset<?>) {
                PointDataset<?> pointDataset = (PointDataset<?>) dataset;
                extractedFeatures = pointDataset.extractMapFeatures(CollectionUtils.setOf(varId),
                        params.getBbox(), params.getZExtent(), params.getTExtent(),
                        params.getTargetZ(), params.getTargetT());

            }
            if (cache) {
                varCache.put(params, extractedFeatures);
            }
            return extractedFeatures;
        }
    }
}
