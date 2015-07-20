/*******************************************************************************
 * Copyright (c) 2015 The University of Reading
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

package uk.ac.rdg.resc.edal.catalogue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.config.SizeOfPolicyConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.catalogue.jaxb.CacheInfo;
import uk.ac.rdg.resc.edal.catalogue.jaxb.CatalogueConfig;
import uk.ac.rdg.resc.edal.catalogue.jaxb.DatasetConfig;
import uk.ac.rdg.resc.edal.catalogue.jaxb.VariableConfig;
import uk.ac.rdg.resc.edal.catalogue.jaxb.CatalogueConfig.DatasetStorage;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.graphics.exceptions.EdalLayerNotFoundException;
import uk.ac.rdg.resc.edal.graphics.style.util.DatasetCatalogue;
import uk.ac.rdg.resc.edal.graphics.style.util.EnhancedVariableMetadata;
import uk.ac.rdg.resc.edal.graphics.style.util.FeatureCatalogue;
import uk.ac.rdg.resc.edal.graphics.style.util.LayerNameMapper;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.PlottingDomainParams;

/**
 * A catalogues which implements {@link DatasetCatalogue},
 * {@link DatasetStorage}, and {@link FeatureCatalogue}. Given a
 * {@link CacheConfiguration}, this is able to return {@link Dataset}s, and
 * {@link Collection}s of {@link DiscreteFeature}s given a single {@link String}
 * layer identifier.
 * 
 * It also provides a cache of {@link DiscreteFeature}s for speed.
 *
 * @author Guy Griffiths
 */
public class DataCatalogue implements DatasetCatalogue, DatasetStorage, FeatureCatalogue {
    private static final String CACHE_NAME = "featureCache";

    private boolean cachingEnabled = false;
    protected final CacheManager cacheManager;
    private Cache featureCache = null;

    protected final CatalogueConfig config;
    protected Map<String, Dataset> datasets;
    protected final Map<DatasetVariableId, EnhancedVariableMetadata> layerMetadata;

    protected final LayerNameMapper layerNameMapper;

    private DateTime lastUpdateTime = new DateTime();

    public DataCatalogue(CatalogueConfig config, LayerNameMapper layerNameMapper)
            throws IOException {
        /*
         * Initialise the storage for datasets and layer metadata.
         */
        datasets = new HashMap<>();
        layerMetadata = new HashMap<>();

        this.config = config;
        this.config.setDatasetLoadedHandler(this);
        this.config.loadDatasets();

        this.layerNameMapper = layerNameMapper;

        /*
         * We are using an in-memory cache with a configured memory size (as
         * opposed to a configured number of items in memory). This has the
         * advantage that we will get a hard limit on the amount of memory the
         * cache consumes. The disadvantage is that the size of each object
         * needs to be calculated prior to inserting it into the cache.
         * 
         * The maxDepth property specified the maximum number of object
         * references to count before a warning is given (we could also
         * configure it to stop counting once the limit is reached, but this
         * kind of defeats the whole point).
         * 
         * Now, we are generally caching 2 things:
         * 
         * 1) Gridded map features which will generally have 256*256 ~= 65,000
         * values, but could easily be bigger
         * 
         * 2) Collections of point features. A year's worth of EN3 data could
         * typically contain >15,000 features, each with a number of properties
         * 
         * These can need to count a very large number of object references.
         * However, this calculation is actually pretty quick. Setting the max
         * depth to 1,000,000 seems to suppress the vast majority of warnings,
         * and doesn't impact performance noticeably.
         */
        cacheManager = CacheManager.create(new Configuration().name("EDAL-WMS-CacheManager")
                .sizeOfPolicy(new SizeOfPolicyConfiguration().maxDepth(1_000_000)));

        setCache(config.getCacheSettings());
    }

    public CatalogueConfig getConfig() {
        return config;
    }

    /**
     * Configures the cache used to store features
     * 
     * @param cacheConfig
     *            The (new) configuration to use for the cache. Must not be
     *            <code>null</code>
     */
    public void setCache(CacheInfo cacheConfig) {
        int cacheSizeMB = cacheConfig.getInMemorySizeMB();
        long lifetimeSeconds = (long) (cacheConfig.getElementLifetimeMinutes() * 60);
        if (featureCache != null
                && cachingEnabled == cacheConfig.isEnabled()
                && cacheSizeMB == featureCache.getCacheConfiguration().getMaxBytesLocalHeap()
                        / (1024 * 1024)
                && lifetimeSeconds == featureCache.getCacheConfiguration().getTimeToLiveSeconds()) {
            /*
             * We are not changing anything about the cache.
             */
            return;
        }

        cachingEnabled = cacheConfig.isEnabled();

        /*
         * We are either disabling the cache or changing its size, so remove any
         * existing one.
         */
        if (cacheManager.cacheExists(CACHE_NAME)) {
            cacheManager.removeCache(CACHE_NAME);
        }

        if (cachingEnabled) {
            /*
             * Configure cache
             */
            CacheConfiguration config = new CacheConfiguration(CACHE_NAME, 0)
                    .eternal(lifetimeSeconds == 0)
                    .maxBytesLocalHeap(cacheSizeMB, MemoryUnit.MEGABYTES)
                    .timeToLiveSeconds(lifetimeSeconds)
                    .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
                    .persistence(new PersistenceConfiguration().strategy(Strategy.NONE))
                    .transactionalMode(TransactionalMode.OFF);

            /*
             * If we already have a cache, we can assume that the configuration
             * has changed, so we remove and re-add it.
             */
            featureCache = new Cache(config);
            cacheManager.addCache(featureCache);
        } else {
            /*
             * Nullify any existing cache to free up memory
             */
            featureCache = null;
        }
    }

    /**
     * Removes a dataset from the catalogue. This will also delete any config
     * information about the dataset from the config file.
     * 
     * @param id
     *            The ID of the dataset to remove
     */
    public void removeDataset(String id) {
        datasets.remove(id);
        config.removeDataset(config.getDatasetInfo(id));
    }

    /**
     * Changes a dataset's ID. This will also change the name in the saved
     * config file.
     * 
     * @param oldId
     *            The old ID
     * @param newId
     *            The new ID
     */
    public void changeDatasetId(String oldId, String newId) {
        Dataset dataset = datasets.get(oldId);
        datasets.remove(oldId);
        datasets.put(newId, dataset);
        config.changeDatasetId(config.getDatasetInfo(oldId), newId);
    }

    @Override
    public synchronized void datasetLoaded(Dataset dataset, Collection<VariableConfig> variables) {
        /*
         * If we already have a dataset with this ID, it will be replaced. This
         * is exactly what we want.
         */
        datasets.put(dataset.getId(), dataset);

        /*
         * Re-sort the datasets map according to the titles of the datasets, so
         * that they appear in the menu in this order.
         */
        List<Map.Entry<String, Dataset>> entryList = new ArrayList<Map.Entry<String, Dataset>>(
                datasets.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<String, Dataset>>() {
            public int compare(Map.Entry<String, Dataset> d1, Map.Entry<String, Dataset> d2) {
                return config.getDatasetInfo(d1.getKey()).getTitle()
                        .compareTo(config.getDatasetInfo(d2.getKey()).getTitle());
            }
        });

        datasets = new LinkedHashMap<String, Dataset>();
        for (Map.Entry<String, Dataset> entry : entryList) {
            datasets.put(entry.getKey(), entry.getValue());
        }

        /*
         * Now add the layer metadata to a map for future reference
         */
        for (VariableConfig ncwmsVariable : variables) {
            DatasetVariableId id = new DatasetVariableId(ncwmsVariable.getParentDataset().getId(),
                    ncwmsVariable.getId());
            layerMetadata.put(id, ncwmsVariable);
        }
        lastUpdateTime = new DateTime();

        /*
         * The config has changed, so we save it.
         */
        try {
            config.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    @Override
    public Collection<Dataset> getAllDatasets() {
        /*
         * This catalogue stores all possible datasets, but this method must
         * only return those which are available (i.e. not disabled and ready to
         * go)
         */
        List<Dataset> allDatasets = new ArrayList<Dataset>();
        for (Dataset dataset : datasets.values()) {
            DatasetConfig datasetInfo = config.getDatasetInfo(dataset.getId());
            if (datasetInfo != null && !datasetInfo.isDisabled() && datasetInfo.isReady()) {
                allDatasets.add(dataset);
            }
        }
        return allDatasets;
    }

    @Override
    public Dataset getDatasetFromId(String datasetId) {
        if (datasets.containsKey(datasetId)) {
            return datasets.get(datasetId);
        } else {
            return null;
        }
    }

    @Override
    public EnhancedVariableMetadata getLayerMetadata(final VariableMetadata variableMetadata)
            throws EdalLayerNotFoundException {
        DatasetVariableId key = new DatasetVariableId(variableMetadata.getDataset().getId(),
                variableMetadata.getId());
        if (layerMetadata.containsKey(key)) {
            return layerMetadata.get(key);
        } else {
            throw new EdalLayerNotFoundException("No layer exists for the variable: "
                    + variableMetadata.getId() + " in the dataset: "
                    + variableMetadata.getDataset().getId());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public FeaturesAndMemberName getFeaturesForLayer(String layerName, PlottingDomainParams params)
            throws EdalException {
        String variable = layerNameMapper.getVariableIdFromLayerName(layerName);
        Collection<? extends DiscreteFeature<?, ?>> mapFeatures;
        if (cachingEnabled) {
            CacheKey key = new CacheKey(layerName, params);
            Element element = featureCache.get(key);
            if (element != null && element.getObjectValue() != null) {
                /*
                 * This is why we added the SuppressWarnings("unchecked").
                 */
                mapFeatures = (Collection<? extends DiscreteFeature<?, ?>>) element
                        .getObjectValue();
            } else {
                Dataset dataset = getDatasetFromLayerName(layerName);
                mapFeatures = dataset.extractMapFeatures(CollectionUtils.setOf(variable), params);
                featureCache.put(new Element(key, mapFeatures));
            }
        } else {
            Dataset dataset = getDatasetFromLayerName(layerName);
            mapFeatures = dataset.extractMapFeatures(CollectionUtils.setOf(variable), params);
        }
        return new FeaturesAndMemberName(mapFeatures, variable);
    }

    private Dataset getDatasetFromLayerName(String layerName) {
        return getDatasetFromId(layerNameMapper.getDatasetIdFromLayerName(layerName));
    }

    private static class CacheKey {
        final String id;
        final PlottingDomainParams params;

        public CacheKey(String id, PlottingDomainParams params) {
            super();
            this.id = id;
            this.params = params;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result + ((params == null) ? 0 : params.hashCode());
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
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            if (params == null) {
                if (other.params != null)
                    return false;
            } else if (!params.equals(other.params))
                return false;
            return true;
        }
    }

    private class DatasetVariableId {
        String datasetId;
        String variableId;

        public DatasetVariableId(String datasetId, String variableId) {
            super();
            this.datasetId = datasetId;
            this.variableId = variableId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((datasetId == null) ? 0 : datasetId.hashCode());
            result = prime * result + ((variableId == null) ? 0 : variableId.hashCode());
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
            DatasetVariableId other = (DatasetVariableId) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (datasetId == null) {
                if (other.datasetId != null)
                    return false;
            } else if (!datasetId.equals(other.datasetId))
                return false;
            if (variableId == null) {
                if (other.variableId != null)
                    return false;
            } else if (!variableId.equals(other.variableId))
                return false;
            return true;
        }

        private DataCatalogue getOuterType() {
            return DataCatalogue.this;
        }
    }
}
