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
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.management.ManagementService;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import uk.ac.rdg.resc.edal.cache.EdalCache;
import uk.ac.rdg.resc.edal.catalogue.jaxb.CacheInfo;
import uk.ac.rdg.resc.edal.catalogue.jaxb.CatalogueConfig;
import uk.ac.rdg.resc.edal.catalogue.jaxb.CatalogueConfig.DatasetStorage;
import uk.ac.rdg.resc.edal.catalogue.jaxb.DatasetConfig;
import uk.ac.rdg.resc.edal.catalogue.jaxb.VariableConfig;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.graphics.exceptions.EdalLayerNotFoundException;
import uk.ac.rdg.resc.edal.graphics.utils.DatasetCatalogue;
import uk.ac.rdg.resc.edal.graphics.utils.EnhancedVariableMetadata;
import uk.ac.rdg.resc.edal.graphics.utils.FeatureCatalogue;
import uk.ac.rdg.resc.edal.graphics.utils.GraphicsUtils;
import uk.ac.rdg.resc.edal.graphics.utils.LayerNameMapper;
import uk.ac.rdg.resc.edal.graphics.utils.PlottingDomainParams;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;

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
    private static final Logger log = LoggerFactory.getLogger(DataCatalogue.class);

    private static final String WMS_CACHE_CONFIG = "ehcache.config";
    private static final String CACHE_NAME = "featureCache";
    private static final long CACHE_SIZE_MB = 512;
    private static final int LIFETIME_SECONDS = 0;
    final MemoryStoreEvictionPolicy EVICTION_POLICY = MemoryStoreEvictionPolicy.LFU;
    private static final Strategy PERSISTENCE_STRATEGY = Strategy.NONE;
    private static final TransactionalMode TRANSACTIONAL_MODE = TransactionalMode.OFF;

    private boolean cachingEnabled;
    private Cache featureCache = null;
    private static MBeanServer mBeanServer;
    private static ObjectName cacheManagerObjectName;

    protected final CatalogueConfig config;
    protected Map<String, Dataset> datasets;
    private final Map<DatasetVariableId, EnhancedVariableMetadata> layerMetadata;

    protected final LayerNameMapper layerNameMapper;

    private DateTime lastUpdateTime = new DateTime();

    public DataCatalogue() {
        config = null;
        layerMetadata = null;
        layerNameMapper = null;
    }

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

        this.cachingEnabled = config.getCacheSettings().isEnabled();
        long cacheLifetimeSeconds = (long) (config.getCacheSettings().getElementLifetimeMinutes()
                * 60);

        String ehcache_file = System.getProperty(WMS_CACHE_CONFIG);
        if (ehcache_file != null && !ehcache_file.isEmpty()) {
            /*
             * We want to load the caches from the XML file into the EDAL cache
             * manager
             */
            log.debug("Loading cache definitions from file");
            CacheManager cacheManager = CacheManager.newInstance(System.getProperty(WMS_CACHE_CONFIG));
            for (String cacheName : cacheManager.getCacheNames()) {
                if(EdalCache.cacheManager.cacheExists(cacheName)) {
                    /*
                     * Remove any existing cache
                     */
                    EdalCache.cacheManager.removeCache(cacheName);
                }
                EdalCache.cacheManager.addCache(new Cache(cacheManager.getCache(cacheName).getCacheConfiguration()));
            }
            cacheManager.shutdown();
        }

        if (cachingEnabled) {
            if (EdalCache.cacheManager.cacheExists(CACHE_NAME)) {
                /*
                 * Use parameters for featureCache from ehcache.xml config file
                 * if passed in as JVM parameter wmsCache.config - Update cache
                 * params in NwcmsConfig
                 */
                featureCache = EdalCache.cacheManager.getCache(CACHE_NAME);
                CacheInfo catalogueCacheInfo = config.getCacheSettings();
                CacheConfiguration featureCacheConfiguration = featureCache.getCacheConfiguration();
                catalogueCacheInfo.setInMemorySizeMB(
                        (int) (featureCacheConfiguration.getMaxBytesLocalHeap() / (1024 * 1024)));
                catalogueCacheInfo.setElementLifetimeMinutes(
                        featureCacheConfiguration.getTimeToLiveSeconds() / 60);
                catalogueCacheInfo.setEnabled(true);
            } else {
                /*
                 * Either no ehcache.xml file is available, or it does not
                 * define "featureCache". In this case, configure with values
                 * from config.xml
                 */
                CacheConfiguration cacheConfig = new CacheConfiguration(CACHE_NAME, 0)
                        .eternal(cacheLifetimeSeconds == 0).timeToLiveSeconds(cacheLifetimeSeconds)
                        .maxBytesLocalHeap(config.getCacheSettings().getInMemorySizeMB(),
                                MemoryUnit.MEGABYTES)
                        .memoryStoreEvictionPolicy(EVICTION_POLICY)
                        .persistence(new PersistenceConfiguration().strategy(PERSISTENCE_STRATEGY))
                        .transactionalMode(TRANSACTIONAL_MODE);

                featureCache = new Cache(cacheConfig);
                EdalCache.cacheManager.addCache(featureCache);
            }

            /*
             * Used to gather statistics about Ehcache
             */
            mBeanServer = ManagementFactory.getPlatformMBeanServer();
            try {
                cacheManagerObjectName = new ObjectName("net.sf.ehcache:type=CacheManager,name="
                        + EdalCache.cacheManager.getName());
            } catch (MalformedObjectNameException e) {
                throw new EdalException("unable to form cacheManager ObjectName", e);
            }

            if (!mBeanServer.isRegistered(cacheManagerObjectName)) {
                ManagementService.registerMBeans(EdalCache.cacheManager, mBeanServer, true, true,
                        true, true);
            }
        }
    }

    public CatalogueConfig getConfig() {
        return config;
    }

    public void shutdown() {
        CatalogueConfig.shutdown();
    }

    /**
     * Configures the cache used to store features
     * 
     * @param cacheConfig
     *            The (new) configuration to use for the cache. Must not be
     *            <code>null</code>
     */
    public void setCache(CacheInfo cacheConfig) {
        MemoryStoreEvictionPolicy memoryStoreEviction;
        Strategy persistenceStrategy;
        TransactionalMode transactionalMode;
        long cacheSizeMB;
        long configCacheSizeMB = cacheConfig.getInMemorySizeMB();
        long lifetimeSeconds;
        long configLifetimeSeconds = (long) (cacheConfig.getElementLifetimeMinutes() * 60);

        if (featureCache != null && cachingEnabled == cacheConfig.isEnabled()
                && configCacheSizeMB == featureCache.getCacheConfiguration().getMaxBytesLocalHeap()
                        / (1024 * 1024)
                && configLifetimeSeconds == featureCache.getCacheConfiguration()
                        .getTimeToLiveSeconds()) {
            /*
             * We are not changing anything about the cache.
             */
            return;
        }

        cachingEnabled = cacheConfig.isEnabled();

        if (cachingEnabled) {
            if (EdalCache.cacheManager.cacheExists(CACHE_NAME)) {
                /*
                 * Update cache configuration
                 */
                CacheConfiguration featureCacheConfig = featureCache.getCacheConfiguration();
                featureCacheConfig.setTimeToLiveSeconds(configLifetimeSeconds);
                featureCacheConfig.setMaxBytesLocalHeap(configCacheSizeMB * 1024 * 1024);
            } else {
                /*-
                 * Precedence:
                 * - Admin config
                 * - XML file "ehcache.config"
                 * - Default values
                 */

                /*
                 * Default values
                 */
                cacheSizeMB = CACHE_SIZE_MB;
                lifetimeSeconds = LIFETIME_SECONDS;
                memoryStoreEviction = EVICTION_POLICY;
                persistenceStrategy = PERSISTENCE_STRATEGY;
                transactionalMode = TRANSACTIONAL_MODE;

                /*
                 * XML config
                 */
                String ehcache_file = System.getProperty("ehcache.config");
                if (ehcache_file != null && !ehcache_file.isEmpty()) {
                    Cache tmpfeatureCache = EdalCache.cacheManager.getCache(CACHE_NAME);
                    cacheSizeMB = tmpfeatureCache.getCacheConfiguration().getMaxBytesLocalHeap()
                            / (1024 * 1024);
                    lifetimeSeconds = tmpfeatureCache.getCacheConfiguration()
                            .getTimeToLiveSeconds();
                    memoryStoreEviction = tmpfeatureCache.getCacheConfiguration()
                            .getMemoryStoreEvictionPolicy();
                    persistenceStrategy = tmpfeatureCache.getCacheConfiguration()
                            .getPersistenceConfiguration().getStrategy();
                    transactionalMode = tmpfeatureCache.getCacheConfiguration()
                            .getTransactionalMode();
                }

                /*
                 * Admin
                 */
                if (cacheConfig.getInMemorySizeMB() != 0) {
                    cacheSizeMB = configCacheSizeMB;
                }
                if (cacheConfig.getElementLifetimeMinutes() != 0) {
                    lifetimeSeconds = configLifetimeSeconds;
                }

                /*
                 * Configure and create cache
                 */
                CacheConfiguration config = new CacheConfiguration(CACHE_NAME, 0)
                        .eternal(lifetimeSeconds == 0)
                        .maxBytesLocalHeap(cacheSizeMB, MemoryUnit.MEGABYTES)
                        .timeToLiveSeconds(lifetimeSeconds)
                        .memoryStoreEvictionPolicy(memoryStoreEviction)
                        .persistence(new PersistenceConfiguration().strategy(persistenceStrategy))
                        .transactionalMode(transactionalMode);

                featureCache = new Cache(config);
                EdalCache.cacheManager.addCache(featureCache);
            }
        } else {
            /*
             * Remove existing cache to free up memory
             */
            EdalCache.cacheManager.removeCache(CACHE_NAME);
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
         * If we have any tiles in the cache with this dataset ID, we want to remove them.
         */
        if(cachingEnabled) {
            @SuppressWarnings("rawtypes")
            List keys = featureCache.getKeys();
            for(Object key : keys) {
                CacheKey cacheKey = (CacheKey) key;
                String datasetId = layerNameMapper.getDatasetIdFromLayerName(cacheKey.layerName);
                if(dataset.getId().equals(datasetId)) {
                    featureCache.remove(cacheKey);
                }
            }
        }
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
        try {
            Collections.sort(entryList, new Comparator<Map.Entry<String, Dataset>>() {
                public int compare(Map.Entry<String, Dataset> d1, Map.Entry<String, Dataset> d2) {
                    return config.getDatasetInfo(d1.getKey()).getTitle()
                            .compareTo(config.getDatasetInfo(d2.getKey()).getTitle());
                }
            });
        } catch (NullPointerException e) {
            log.error("Problem when sorting datasets", e);
            /*
             * Sometimes this gives a NullPointerException with remote datasets
             * which are unavailable (?)
             * 
             * It's been seen a couple of times on the issue tracker, but I've
             * been unable to reproduce it. I think it may be that the title is
             * not getting set correctly (or at all?). Perhaps this needs some
             * more robust checking in the CatalogueConfig object?
             * 
             * Since sorting the datasets by title isn't critical, we can ignore
             * the error.
             */
        }

        datasets = new LinkedHashMap<String, Dataset>();
        for (Map.Entry<String, Dataset> entry : entryList) {
            datasets.put(entry.getKey(), entry.getValue());
        }

        /*
         * Now add the layer metadata to a map for future reference
         */
        for (VariableConfig variable : variables) {
            DatasetVariableId id = new DatasetVariableId(variable.getParentDataset().getId(),
                    variable.getId());
            layerMetadata.put(id, variable);
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

    public DatasetConfig getDatasetInfo(String datasetId) {
        return config.getDatasetInfo(datasetId);
    }

    @Override
    public EnhancedVariableMetadata getLayerMetadata(final VariableMetadata variableMetadata)
            throws EdalLayerNotFoundException {
        DatasetVariableId key = new DatasetVariableId(variableMetadata.getDataset().getId(),
                variableMetadata.getId());
        if (layerMetadata.containsKey(key)) {
            return layerMetadata.get(key);
        } else {
            throw new EdalLayerNotFoundException(
                    "No layer exists for the variable: " + variableMetadata.getId()
                            + " in the dataset: " + variableMetadata.getDataset().getId());
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
                mapFeatures = doExtraction(layerName, variable, params);
                try {
                    featureCache.put(new Element(key, mapFeatures));
                } catch (Exception e) {
                    log.error("Problem adding features to cache", e);
                    /*
                     * Just log and carry on - not caching isn't the end of the world
                     */
                }
            }
        } else {
            mapFeatures = doExtraction(layerName, variable, params);
        }
        return new FeaturesAndMemberName(mapFeatures, variable);
    }

    private Collection<? extends DiscreteFeature<?, ?>> doExtraction(String layerName,
            String variable, PlottingDomainParams params) {
        Dataset dataset = getDatasetFromLayerName(layerName);
        return GraphicsUtils.extractGeneralMapFeatures(dataset, variable, params);
    }

    private Dataset getDatasetFromLayerName(String layerName) {
        return getDatasetFromId(layerNameMapper.getDatasetIdFromLayerName(layerName));
    }

    private static class CacheKey implements Serializable {
        private static final long serialVersionUID = 1L;
        final String layerName;
        final PlottingDomainParams params;

        public CacheKey(String layerName, PlottingDomainParams params) {
            super();
            this.layerName = layerName;
            this.params = params;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((layerName == null) ? 0 : layerName.hashCode());
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
            if (layerName == null) {
                if (other.layerName != null)
                    return false;
            } else if (!layerName.equals(other.layerName))
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
