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

package uk.ac.rdg.resc.edal.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.io.Serializable;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import uk.ac.rdg.resc.edal.dataset.HZTDataSource.MeshCoordinates3D;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.VariableNotFoundException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.HorizontalMesh;
import uk.ac.rdg.resc.edal.metadata.HorizontalMesh4dVariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;
import uk.ac.rdg.resc.edal.util.ValuesArray1D;
import uk.ac.rdg.resc.edal.util.ValuesArray2D;

/**
 * Partial implementation of a {@link Dataset} where the horizontal layers are
 * based on an unstructured mesh, and the vertical / time dimensions are
 * discrete.
 *
 * @author Guy Griffiths
 */
public abstract class HorizontalMesh4dDataset extends
        DiscreteLayeredDataset<HZTDataSource, HorizontalMesh4dVariableMetadata> implements Serializable {

    public HorizontalMesh4dDataset(String id, Collection<HorizontalMesh4dVariableMetadata> vars) {
        super(id, vars);
    }

    @Override
    public Class<? extends DiscreteFeature<?, ?>> getFeatureType(String variableId) {
        /*
         * Whilst feature reading is not yet supported, getFeatureType needs to
         * return a class for the WMS classes (in edal-wms) to work correctly.
         * 
         * TODO Ideally we should create a new HorizontalMesh4dFeature type
         * which shares a parent with GridFeature, and have the WMS check for
         * that parent feature type. Until feature reading is implemented this
         * is extremely low priority.
         */
        return GridFeature.class;
    }

    @Override
    public Feature<?> readFeature(String featureId) throws DataReadingException,
            VariableNotFoundException {
        throw new UnsupportedOperationException("Feature reading is not yet supported");
    }

//    @SuppressWarnings("unchecked")
    @Override
    protected Array2D<Number> extractHorizontalData(HorizontalMesh4dVariableMetadata metadata,
            int tIndex, int zIndex, HorizontalGrid targetGrid, HZTDataSource dataSource)
            throws DataReadingException {
        HorizontalMesh grid = metadata.getHorizontalDomain();

        /*
         * Create a list of coordinates to read to extract all of the horizontal
         * data, and a corresponding list of 2D coordinates in which to store
         * the output data
         */
        List<GridCoordinates2D> outputCoords;
        List<MeshCoordinates3D> coordsToRead;
        MeshDatasetCacheElement meshDatasetCacheElement;
        if (meshDatasetCache.isKeyInCache(targetGrid)) {
            meshDatasetCacheElement = (MeshDatasetCacheElement) meshDatasetCache.get(targetGrid).getObjectValue();
            outputCoords = meshDatasetCacheElement.getOutputCoords();
            coordsToRead = meshDatasetCacheElement.getCoordsToRead();
        } else {
            outputCoords = new ArrayList<>();
            coordsToRead = new ArrayList<>();
            for (GridCell2D cell : targetGrid.getDomainObjects()) {
                HorizontalPosition centre = cell.getCentre();
                GridCoordinates2D coordinates = cell.getGridCoordinates();
                int hIndex = grid.findIndexOf(centre);
                MeshCoordinates3D meshCoords = new MeshCoordinates3D(hIndex, zIndex, tIndex);
                outputCoords.add(coordinates);
                coordsToRead.add(meshCoords);
            }
            meshDatasetCacheElement = new MeshDatasetCacheElement(outputCoords, coordsToRead);
            meshDatasetCache.put(new Element(targetGrid, meshDatasetCacheElement));
        }

        /*
         * Now perform the actual read
         */
        List<Number> dataVals = dataSource.read(metadata.getId(), coordsToRead);

        /*
         * And finally populate the output array with the read values
         */
        Array2D<Number> data = new ValuesArray2D(targetGrid.getYSize(), targetGrid.getXSize());
        for (int i = 0; i < dataVals.size(); i++) {
            GridCoordinates2D outputCoord = outputCoords.get(i);
            data.set(dataVals.get(i), outputCoord.getY(), outputCoord.getX());
        }
        return data;
    }

    @Override
    protected Array1D<Number> extractProfileData(HorizontalMesh4dVariableMetadata metadata,
            List<Integer> zs, int tIndex, HorizontalPosition hPos, HZTDataSource dataSource)
            throws DataReadingException {
        HorizontalMesh hDomain = metadata.getHorizontalDomain();
        int hIndex = hDomain.findIndexOf(hPos);

        /*
         * Populate the list of coordinates to read
         */
        List<MeshCoordinates3D> coordsToRead = new ArrayList<>();
        for (Integer z : zs) {
            coordsToRead.add(new MeshCoordinates3D(hIndex, z, tIndex));
        }

        /*
         * Do the reading
         */
        List<Number> dataVals = dataSource.read(metadata.getId(), coordsToRead);

        /*
         * Populate the output array
         */
        int i = 0;
        Array1D<Number> data = new ValuesArray1D(zs.size());
        for (Number value : dataVals) {
            data.set(value, new int[] { i++ });
        }
        return data;
    }

    @Override
    protected Array1D<Number> extractTimeseriesData(HorizontalMesh4dVariableMetadata metadata,
            List<Integer> ts, int zIndex, HorizontalPosition hPos, HZTDataSource dataSource)
            throws DataReadingException {
        HorizontalMesh hDomain = metadata.getHorizontalDomain();
        int hIndex = hDomain.findIndexOf(hPos);

        /*
         * Populate the list of coordinates to read
         */
        List<MeshCoordinates3D> coordsToRead = new ArrayList<>();
        for (Integer t : ts) {
            coordsToRead.add(new MeshCoordinates3D(hIndex, zIndex, t));
        }

        /*
         * Do the reading
         */
        List<Number> dataVals = dataSource.read(metadata.getId(), coordsToRead);

        /*
         * Populate the output array
         */
        int i = 0;
        Array1D<Number> data = new ValuesArray1D(ts.size());
        for (Number value : dataVals) {
            data.set(value, new int[] { i++ });
        }
        return data;
    }

    @Override
    protected Number extractPoint(HorizontalMesh4dVariableMetadata metadata, int t, int z,
            HorizontalPosition hPos, HZTDataSource dataSource) throws DataReadingException {
        HorizontalMesh hGrid = metadata.getHorizontalDomain();
        int hIndex = hGrid.findIndexOf(hPos);
        if (hIndex == -1) {
            return null;
        }
        return dataSource.read(metadata.getId(),
                Collections.singletonList(new MeshCoordinates3D(hIndex, z, t))).get(0);
    }

    /*
     * Cache management
     * - 50 maps of in-out coordinate mappings
     */
    private static final String CACHE_NAME ="meshDatasetCache";
    private static final String CACHE_MANAGER = "EDAL-CacheManager";
    private static final int MAX_HEAP_ENTRIES = 50;
    private static final MemoryStoreEvictionPolicy EVICTION_POLICY = MemoryStoreEvictionPolicy.LFU;
    private static final Strategy PERSISTENCE_STRATEGY = Strategy.NONE;
    private static final TransactionalMode TRANSACTIONAL_MODE = TransactionalMode.OFF;
    private static Cache meshDatasetCache = null;
    protected static final CacheManager cacheManager = CacheManager.create(new Configuration().name(CACHE_MANAGER));

    static {
        if (cacheManager.cacheExists(CACHE_NAME) == false) {
            /*
             * Configure cache
             */
            CacheConfiguration config = new CacheConfiguration(CACHE_NAME, MAX_HEAP_ENTRIES)
                    .eternal(true)
                    .memoryStoreEvictionPolicy(EVICTION_POLICY)
                    .persistence(new PersistenceConfiguration().strategy(PERSISTENCE_STRATEGY))
                    .transactionalMode(TRANSACTIONAL_MODE);
            meshDatasetCache = new Cache(config);
            cacheManager.addCache(meshDatasetCache);
        } else {
            meshDatasetCache = cacheManager.getCache(CACHE_NAME);
        }
    }
}
