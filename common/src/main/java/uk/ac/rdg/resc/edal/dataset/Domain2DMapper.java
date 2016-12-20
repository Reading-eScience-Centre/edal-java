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

package uk.ac.rdg.resc.edal.dataset;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;

/**
 * This is an implementation of a {@link DomainMapper} which maps 2D indices
 * from a source grid onto a pair of integers (as <code>int[]</code>) in the
 * target domain.
 * 
 * The first element of the returned array represents the x-component.
 * 
 * The second element of the returned array represents the y-component.
 * 
 * It also includes a static method
 * {@link Domain2DMapper#forGrid(HorizontalGrid, HorizontalGrid)} which
 * generates a {@link Domain2DMapper} from a source and a target grid, which
 * uses cache of recent grids - creating a {@link Domain2DMapper} is not a
 * particularly efficient operation and generally gets called very regularly for
 * identical grids, particularly in a tiled WMS setting
 * 
 * @author Guy Griffiths
 */
public class Domain2DMapper extends DomainMapper<int[]> {
    private int targetXSize;
    private int targetYSize;



    private Domain2DMapper(HorizontalGrid sourceGrid, int targetXSize, int targetYSize) {
        super(sourceGrid, targetXSize * targetYSize);
        this.targetXSize = targetXSize;
        this.targetYSize = targetYSize;
    }

    @Override
    protected int[] convertIndexToCoordType(int index) {
        /*
         * We're mapping a single int to a pair of co-ordinates, based on the
         * target grid size.
         */
        return new int[] { (index % targetXSize), (index / targetXSize) };
    }

    private int convertCoordsToIndex(int i, int j) {
        return j * targetXSize + i;
    }

    /**
     * Gets the x-size of the target grid
     */
    public int getTargetXSize() {
        return targetXSize;
    }

    /**
     * Gets the y-size of the target grid
     */
    public int getTargetYSize() {
        return targetYSize;
    }

    /**
     * Initialises a {@link Domain2DMapper} from a source and a target grid.
     * 
     * @param sourceGrid
     *            A {@link HorizontalGrid} representing the domain of the source
     *            data
     * @param targetGrid
     *            A {@link HorizontalGrid} representing the domain of the target
     * @return A {@link Domain2DMapper} performing the mapping
     */
    public static Domain2DMapper forGrid(HorizontalGrid sourceGrid, final HorizontalGrid targetGrid) {
        Domain2DMapperCacheKey key = new Domain2DMapperCacheKey(sourceGrid, targetGrid);
        if (domainMapperCache.isKeyInCache(key)) {
            return (Domain2DMapper) domainMapperCache.get(key).getObjectValue();
        }
        Domain2DMapper ret;
        if (sourceGrid instanceof RectilinearGrid
                && targetGrid instanceof RectilinearGrid
                && GISUtils.crsMatch(sourceGrid.getCoordinateReferenceSystem(),
                        targetGrid.getCoordinateReferenceSystem())) {
            /*
             * We can gain efficiency if the source and target grids are both
             * rectilinear grids with the same CRS.
             */
            /*
             * WASTODO: could also be efficient for any matching CRS? But how
             * test for CRS equality, when one CRS will have been created from
             * an EPSG code and the other will have been inferred from the
             * source data file (e.g. NetCDF) TODO: implemented - test that it
             * works when it should!
             */
            ret = forMatchingCrsGrids((RectilinearGrid) sourceGrid, (RectilinearGrid) targetGrid);
        } else {
            /*
             * We can't gain efficiency, so we just initialise for general grids
             */
            ret = forGeneralGrids(sourceGrid, targetGrid);
        }
        domainMapperCache.put(new Element(key, ret));
        return ret;
    }

    /*-
     * Initialise the Domain2DMapper for 2 grids which:
     * 
     * a) Have matching CRSs
     * b) Are rectilinear
     * 
     * This is the optimised method
     */
    private static Domain2DMapper forMatchingCrsGrids(RectilinearGrid sourceGrid,
            RectilinearGrid targetGrid) {
        Domain2DMapper mapper = new Domain2DMapper(sourceGrid, targetGrid.getXAxis().size(),
                targetGrid.getYAxis().size());

        log.debug("Using optimized method for coordinates with orthogonal 1D axes in the same CRS");

        ReferenceableAxis<Double> sourceGridXAxis = sourceGrid.getXAxis();
        ReferenceableAxis<Double> sourceGridYAxis = sourceGrid.getYAxis();

        ReferenceableAxis<Double> targetGridXAxis = targetGrid.getXAxis();
        ReferenceableAxis<Double> targetGridYAxis = targetGrid.getYAxis();

        /*
         * Calculate the indices along the x axis
         */
        int[] xIndices = new int[targetGridXAxis.size()];
        List<Double> targetGridLons = targetGridXAxis.getCoordinateValues();
        for (int i = 0; i < targetGridLons.size(); i++) {
            double targetX = targetGridLons.get(i);
            xIndices[i] = sourceGridXAxis.findIndexOf(targetX);
        }

        /*
         * Now cycle through the y-values in the target grid
         */
        for (int j = 0; j < targetGridYAxis.size(); j++) {
            double targetY = targetGridYAxis.getCoordinateValue(j);
            int yIndex = sourceGridYAxis.findIndexOf(targetY);
            if (yIndex >= 0) {
                for (int i = 0; i < xIndices.length; i++) {
                    mapper.put(xIndices[i], yIndex, mapper.convertCoordsToIndex(i, j));
                }
            }
        }

        mapper.sortIndices();
        return mapper;
    }

    /*
     * Initialise the Domain2DMapper for general HorizontalGrids
     */
    private static Domain2DMapper forGeneralGrids(HorizontalGrid sourceGrid,
            final HorizontalGrid targetGrid) {
        Domain2DMapper mapper = new Domain2DMapper(sourceGrid, targetGrid.getXSize(),
                targetGrid.getYSize());
        /*
         * Find the nearest grid coordinates to all the points in the domain
         */
        Array<GridCell2D> targetDomainObjects = targetGrid.getDomainObjects();
        for (int j = 0; j < targetGrid.getYSize(); j++) {
            for (int i = 0; i < targetGrid.getXSize(); i++) {
                targetGrid.getDomainObjects().get(j, i).getCentre();
                HorizontalPosition transformedPosition = GISUtils.transformPosition(
                        targetDomainObjects.get(j, i).getCentre(),
                        sourceGrid.getCoordinateReferenceSystem());
                GridCoordinates2D indices = sourceGrid.findIndexOf(transformedPosition);
                if (indices != null) {
                    mapper.put(indices.getX(), indices.getY(), mapper.convertCoordsToIndex(i, j));
                }
            }
        }

        mapper.sortIndices();
        return mapper;
    }

    /*
     * Cache management
     */
    private static final String CACHE_NAME = "domainMapperCache";
    private static final String CACHE_MANAGER = "EDAL-CacheManager";
    private static final int MAX_HEAP_ENTRIES = 100;
    private static final MemoryStoreEvictionPolicy EVICTION_POLICY = MemoryStoreEvictionPolicy.LFU;
    private static final Strategy PERSISTENCE_STRATEGY = Strategy.NONE;
    private static final TransactionalMode TRANSACTIONAL_MODE = TransactionalMode.OFF;
    private static Cache domainMapperCache;
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
            domainMapperCache = new Cache(config);
            cacheManager.addCache(domainMapperCache);
        } else {
            domainMapperCache = cacheManager.getCache(CACHE_NAME);
        }

    }

    public static class Domain2DMapperCacheKey {
        private HorizontalGrid source;
        private HorizontalGrid target;

        public Domain2DMapperCacheKey(HorizontalGrid source, HorizontalGrid target) {
            super();
            this.source = source;
            this.target = target;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((source == null) ? 0 : source.hashCode());
            result = prime * result + ((target == null) ? 0 : target.hashCode());
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
            Domain2DMapperCacheKey other = (Domain2DMapperCacheKey) obj;
            if (source == null) {
                if (other.source != null)
                    return false;
            } else if (!source.equals(other.source))
                return false;
            if (target == null) {
                if (other.target != null)
                    return false;
            } else if (!target.equals(other.target))
                return false;
            return true;
        }
    }
}
