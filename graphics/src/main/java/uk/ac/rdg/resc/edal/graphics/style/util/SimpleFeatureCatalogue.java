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

package uk.ac.rdg.resc.edal.graphics.style.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.PlottingDomainParams;

public class SimpleFeatureCatalogue implements FeatureCatalogue {
    private boolean cacheEnabled = false;
    private Map<CacheKey, List<? extends DiscreteFeature<?, ?>>> features = null;
    private Dataset dataset;

    public SimpleFeatureCatalogue(Dataset dataset, boolean cacheEnabled) throws IOException,
            EdalException {
        this.dataset = dataset;
        this.cacheEnabled = cacheEnabled;
        features = new HashMap<>();
    }

    public Dataset getDataset() {
        return dataset;
    }

    @Override
    public FeaturesAndMemberName getFeaturesForLayer(String id, PlottingDomainParams params)
            throws EdalException {
        return new FeaturesAndMemberName(getMapFeature(params, id, cacheEnabled), id);
    }

    private List<? extends DiscreteFeature<?, ?>> getMapFeature(PlottingDomainParams params,
            String varId, boolean cache) throws DataReadingException {
        CacheKey key = new CacheKey(params, varId);
        if (features.containsKey(key)) {
            return features.get(key);
        } else {
            List<? extends DiscreteFeature<?, ?>> extractedFeatures = dataset.extractMapFeatures(
                    CollectionUtils.setOf(varId), params);
            if (cache) {
                features.put(key, extractedFeatures);
            }
            return extractedFeatures;
        }
    }

    private class CacheKey {
        private PlottingDomainParams plottingParams;
        private String varId;

        public CacheKey(PlottingDomainParams plottingParams, String varId) {
            super();
            this.plottingParams = plottingParams;
            this.varId = varId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((plottingParams == null) ? 0 : plottingParams.hashCode());
            result = prime * result + ((varId == null) ? 0 : varId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CacheKey other = (CacheKey) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (plottingParams == null) {
                if (other.plottingParams != null)
                    return false;
            } else if (!plottingParams.equals(other.plottingParams))
                return false;
            if (varId == null) {
                if (other.varId != null)
                    return false;
            } else if (!varId.equals(other.varId))
                return false;
            return true;
        }

        private SimpleFeatureCatalogue getOuterType() {
            return SimpleFeatureCatalogue.this;
        }
    }

}
