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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;

import uk.ac.rdg.resc.edal.dataset.temporary.RegularAxisImpl;
import uk.ac.rdg.resc.edal.dataset.temporary.RegularGridImpl;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.grid.RegularAxis;
import uk.ac.rdg.resc.edal.grid.RegularGrid;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * <p>
 * Maps real-world points to i and j indices of corresponding points within the
 * source data. A PixelMap is constructed using the following general algorithm:
 * </p>
 * 
 * <pre>
 * For each point in the given targetDomain:
 *    1. Find the grid cell in the source grid that contains this point
 *    2. Let i and j be the coordinates of this grid cell
 *    3. Let p be the index of this point in the target domain
 *    4. Add the mapping (p -> i,j) to the pixel map
 * </pre>
 * 
 * <p>
 * (A more efficient algorithm is used for the special case in which both the
 * requested CRS and the CRS of the data are lat-lon.)
 * </p>
 * 
 * <p>
 * The resulting PixelMap is then used by {@link DataReadingStrategy}s to work
 * out what data to read from the source data files. A variety of strategies are
 * possible for reading these data points, each of which may be optimal in a
 * certain situation.
 * </p>
 * 
 * @author Jon Blower
 * @todo Perhaps we can think of a more appropriate name for this class?
 * 
 * @todo equals() and hashCode(), particularly if we're going to cache instances
 *       of this class.
 * 
 * @todo It may be possible to create an alternative version of this class for
 *       cases where both source and target grids are lat-lon. In this case, the
 *       pixelmap should also be a RectilinearGrid.
 * 
 * @see DataReadingStrategy
 */
final class PixelMap implements Iterable<PixelMap.PixelMapEntry> {
    /*
     * Stores the source grid indices
     */
    private final List<int[]> sourceGridCoords = new ArrayList<int[]>();
    /*
     * Stores the target grid indices
     */
    private final List<int[]> targetGridCoords = new ArrayList<int[]>();

    /**
     * Maps a point in the source grid to corresponding points in the target
     * grid.
     */
    public static interface PixelMapEntry {
        /**
         * Gets the i index of this point in the source grid
         */
        public int getSourceGridIIndex();

        /**
         * Gets the j index of this point in the source grid
         */
        public int getSourceGridJIndex();

        /**
         * Gets the array of all target grid points that correspond with this
         * source grid point. Each grid point is expressed as a length-2 array
         * containing {@code [j, i]}
         */
        public List<int[]> getTargetGridPoints();
    }

    /**
     * Holds all the PixelMapEntries corresponding with a certain j index
     */
    public static interface Scanline {
        /**
         * Gets the j index of this scanline in the source grid
         */
        public int getSourceGridJIndex();

        /**
         * Gets the list of PixelMapEntries associated with the j index, in
         * order of increasing i index
         */
        public List<PixelMapEntry> getPixelMapEntries();
    }

//    private final int sourceGridISize;
//    private final int targetGridISize;

    /*
     * These define the bounding box (in terms of axis indices) of the data to
     * extract from the source files
     */
    private int minIIndex = Integer.MAX_VALUE;
    private int minJIndex = Integer.MAX_VALUE;
    private int maxIIndex = -1;
    private int maxJIndex = -1;
    
    private final int targetXSize;
    private final int targetYSize;
    private PixelMap(HorizontalGrid targetGrid) {
        targetXSize = targetGrid.getXAxis().size();
        targetYSize = targetGrid.getYAxis().size();
    }
    public int getTargetXSize() {
        return targetXSize;
    }
    public int getTargetYSize() {
        return targetYSize;
    }

//    private PixelMap(HorizontalGrid sourceGrid, long targetDomainSize) {
//        if (targetDomainSize > Integer.MAX_VALUE) {
//            throw new IllegalArgumentException("Cannot handle target domains"
//                    + " greater than Integer.MAX_VALUE in size");
//            /*
//             * This is essentially because PixelMapEntry.getTargetGridPoints()
//             * returns a List of Integers. Also because the results of
//             * extracting data are usually held in a primitive array, which can
//             * only be indexed by integer values.
//             */
//        }
//
//        sourceGridISize = sourceGrid.getXAxis().size();
//        this.targetDomainSize = (int) targetDomainSize;
//
//        /*
//         * Create an estimate of a suitable chunk size. We don't want this to be
//         * too small because we would have to do many array copy operations to
//         * grow resizeable arrays. Conversely we don't want it to be too large
//         * and lead to wasted space.
//         */
//        int chunkSize = (int) (targetDomainSize < 1000 ? targetDomainSize : targetDomainSize / 10);
//        /*
//         * Choose storage for the mappings appropriate to the sizes of the
//         * domains
//         */
//        long maxSourceGridIndex = sourceGrid.size() - 1;
//        this.sourceGridCoords = chooseRArray(maxSourceGridIndex, chunkSize);
//
//        long maxTargetGridIndex = targetDomainSize - 1;
//        this.targetGridCoords = chooseRArray(maxTargetGridIndex, chunkSize);
//        /* This is just a double-check: shouldn't happen */
//        if (this.targetGridCoords instanceof RLongArray) {
//            throw new IllegalStateException("Can't store target grid indices as"
//                    + " longs: must be integers or smaller");
//        }
//    }

    public static PixelMap forGrid(HorizontalGrid sourceGrid, final HorizontalGrid targetGrid) {
        if (sourceGrid instanceof RectilinearGrid && targetGrid instanceof RectilinearGrid
                && GISUtils.isWgs84LonLat(sourceGrid.getCoordinateReferenceSystem())
                && GISUtils.isWgs84LonLat(targetGrid.getCoordinateReferenceSystem())) {
            /*
             * We can gain efficiency if the source and target grids are both
             * rectilinear lat-lon grids (i.e. they have separable latitude and
             * longitude axes).
             * 
             * TODO: could also be efficient for any matching CRS? But how test
             * for CRS equality, when one CRS will have been created from an
             * EPSG code and the other will have been inferred from the source
             * data file (e.g. NetCDF)
             */
            return forWgs84Grids((RectilinearGrid) sourceGrid, (RectilinearGrid) targetGrid);
        } else {
            /*
             * We can't gain efficiency, so we just treat the target grid as a
             * general grid
             */
            return forGeneralGrids(sourceGrid, targetGrid);
        }
    }

    public static PixelMap forGeneralGrids(HorizontalGrid sourceGrid, HorizontalGrid targetGrid) {
        PixelMap pm = new PixelMap(targetGrid);

        /*
         * Find the nearest grid coordinates to all the points in the domain
         */
        ReferenceableAxis<Double> targetXAxis = targetGrid.getXAxis();
        ReferenceableAxis<Double> targetYAxis = targetGrid.getYAxis();
        ReferenceableAxis<Double> sourceXAxis = sourceGrid.getXAxis();
        ReferenceableAxis<Double> sourceYAxis = sourceGrid.getYAxis();
        for (int i = 0; i < targetXAxis.size(); i++) {
            for (int j = 0; j < targetYAxis.size(); j++) {
                int sourceIIndex = sourceXAxis.findIndexOf(targetXAxis.getCoordinateValue(i));
                int sourceJIndex = sourceYAxis.findIndexOf(targetYAxis.getCoordinateValue(j));
                pm.put(sourceIIndex, sourceJIndex, i, j);
            }
        }

        pm.sortIndices();
        return pm;
    }

    private static PixelMap forWgs84Grids(RectilinearGrid sourceGrid, RectilinearGrid targetGrid) {
        PixelMap pm = new PixelMap(targetGrid);

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
            double lon = targetGridLons.get(i);
            xIndices[i] = sourceGridXAxis.findIndexOf(lon);
        }
        /*
         * Now cycle through the y values in the target grid
         */
        for (int j = 0; j < targetGridYAxis.size(); j++) {
            double lat = targetGridYAxis.getCoordinateValue(j);
            int yIndex = sourceGridYAxis.findIndexOf(lat);
            for (int i = 0; i < xIndices.length; i++) {
                int xIndex = xIndices[i];
                if (xIndex > 0 && yIndex > 0) {
                    pm.put(i, j, xIndices[i], yIndex);
                }
            }
        }

        pm.sortIndices();
        return pm;
    }

    /**
     * Sorts the arrays of source and target indices so that the arrays are in
     * order of increasing source grid index, then increasing target grid index.
     * Uses an in-place quicksort algorithm adapted from
     * http://www.vogella.de/articles/JavaAlgorithmsQuicksort/article.html.
     */
    private void sortIndices() {
        int numElements = sourceGridCoords.size();
        /*
         * Nothing to do if there are only zero or one elements
         */
        if (numElements < 2)
            return;
        quicksort(0, numElements - 1);
    }
    
    private void quicksort(final int low, final int high) {
        int i = low;
        int j = high;
        /*
         * The elements to be sorted are pairs of longs: the first is the source
         * grid index, the second is the target grid index.
         */
        final int[][] pivot = getPair(low + (high - low) / 2);
        /*
         * Divide into two lists
         */
        while (i <= j) {
            while (comparePairs(getPair(i), pivot) < 0) {
                i++;
            }
            while (comparePairs(getPair(j), pivot) > 0) {
                j--;
            }
            if (i <= j) {
                exchange(i, j);
                i++;
                j--;
            }
        }
        /*
         * Recursion
         */
        if (low < j)
            quicksort(low, j);
        if (i < high)
            quicksort(i, high);
    }

    /**
     * Gets the pair of [source, target] grid indices at the given index
     */
    private int[][] getPair(int index) {
        return new int[][] { sourceGridCoords.get(index), targetGridCoords.get(index) };
    }

    /**
     * Returns <0 if pair1 < pair2, 0 if pair1 == pair2, >0 otherwise.
     * Comparisons are performed first on the source grid index, then on the
     * target grid index.
     */
    private int comparePairs(int[][] pair1, int[][] pair2) {
        /*
         * First compare y-index of source grid
         */
        if (pair1[0][1] < pair2[0][1]) {
            return -1;
        }
        if (pair1[0][1] > pair2[0][1]) {
            return 1;
        }

        /*
         * Haven't returned, therefore source grid y-indices are equal.
         * 
         * Compare x-index of source grid
         */
        if (pair1[0][0] < pair2[0][0]) {
            return -1;
        }
        if (pair1[0][0] > pair2[0][0]) {
            return 1;
        }

        /*
         * Source grid indices must be equal, so compare target grid indices
         * 
         * First y-index of target grid
         */
        if (pair1[1][1] < pair2[1][1]) {
            return -1;
        }
        if (pair1[1][1] > pair2[1][1]) {
            return 1;
        }
        
        /*
         * Now x-index of target grid
         */
        if (pair1[1][0] < pair2[1][0]) {
            return -1;
        }
        if (pair1[1][0] > pair2[1][0]) {
            return 1;
        }

        /*
         * Both equal
         */
        return 0;
    }

    /**
     * Exchanges the values in the source and target grid arrays with indices i1
     * and i2
     */
    private void exchange(int i1, int i2) {
        int[] temp = sourceGridCoords.get(i1);
        sourceGridCoords.set(i1, sourceGridCoords.get(i2));
        sourceGridCoords.set(i2, temp);
        
        temp = targetGridCoords.get(i1);
        targetGridCoords.set(i1, targetGridCoords.get(i2));
        targetGridCoords.set(i2, temp);
    }

    /**
     * Adds a new pixel index to this map. Does nothing if either i or j is
     * negative.
     * 
     * @param i
     *            The i index of the point in the source data
     * @param j
     *            The j index of the point in the source data
     * @param targetI
     *            The i index of the corresponding point in the target domain
     * @param targetJ
     *            The j index of the corresponding point in the target domain
     */
    private void put(int i, int j, int targetI, int targetJ) {
        /*
         * If either of the indices are negative there is no data for this
         * target grid point
         */
        if (i < 0 || j < 0)
            return;
        /*
         * Modify the bounding box if necessary
         */
        if (i < minIIndex)
            minIIndex = i;
        if (i > maxIIndex)
            maxIIndex = i;
        if (j < minJIndex)
            minJIndex = j;
        if (j > maxJIndex)
            maxJIndex = j;
        /*
         * Add to the lists holding the mapping
         */
        sourceGridCoords.add(new int[] { i, j });
        targetGridCoords.add(new int[] { targetI, targetJ });
    }

    /**
     * Returns true if this PixelMap does not contain any data: this will happen
     * if there is no intersection between the requested data and the data on
     * disk.
     */
    public boolean isEmpty() {
        return sourceGridCoords.size() == 0;
    }

    public int getTargetDomainSize() {
        return targetGridCoords.size();
    }

    /**
     * Gets the minimum i index in the whole pixel map
     */
    public int getMinIIndex() {
        return minIIndex;
    }

    /**
     * Gets the minimum j index in the whole pixel map
     */
    public int getMinJIndex() {
        return minJIndex;
    }

    /**
     * Gets the maximum i index in the whole pixel map
     */
    public int getMaxIIndex() {
        return maxIIndex;
    }

    /**
     * Gets the maximum j index in the whole pixel map
     */
    public int getMaxJIndex() {
        return maxJIndex;
    }

    /**
     * <p>
     * Gets the number of unique i-j pairs in this pixel map. When combined with
     * the size of the resulting image we can quantify the under- or
     * over-sampling. This is the number of data points that will be extracted
     * by the {@link DataReadingStrategy#PIXEL_BY_PIXEL PIXEL_BY_PIXEL} data
     * reading strategy.
     * </p>
     * <p>
     * This implementation counts the number of unique pairs by cycling through
     * the {@link #iterator()} and so is not a cheap operation. Use sparingly,
     * e.g. for debugging.
     * </p>
     * 
     * @return the number of unique i-j pairs in this pixel map.
     */
    public int getNumUniqueIJPairs() {
        int count = 0;
        for (@SuppressWarnings("unused")
        PixelMapEntry pme : this)
            count++;
        return count;
    }

    /**
     * Gets the size of the i-j bounding box that encompasses all data. This is
     * the number of data points that will be extracted using the
     * {@link DataReadingStrategy#BOUNDING_BOX BOUNDING_BOX} data reading
     * strategy.
     * 
     * @return the size of the i-j bounding box that encompasses all data.
     */
    public long getBoundingBoxSize() {
        return (long) (this.maxIIndex - this.minIIndex + 1) * (this.maxJIndex - this.minJIndex + 1);
    }

    /**
     * Returns an unmodifiable iterator over all the {@link PixelMapEntry}s in
     * this PixelMap.
     */
    @Override
    public Iterator<PixelMapEntry> iterator() {
        return new Iterator<PixelMapEntry>() {
            /** Index in the array of entries */
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < sourceGridCoords.size();
            }

            @Override
            public PixelMapEntry next() {
                final int[] nextSource = sourceGridCoords.get(index);
                final int[] nextTarget = targetGridCoords.get(index);

                final List<int[]> entryTargetCoords = new ArrayList<int[]>();
                entryTargetCoords.add(nextTarget);

                /*
                 * Now find all the other entries that use the same source grid
                 * index
                 */
                boolean done = false;
                index++;
                while (!done && hasNext()) {
                    int[] newSourceEntry = sourceGridCoords.get(index);
                    if (newSourceEntry[0] == nextSource[0] && newSourceEntry[1] == nextSource[1]) {
                        entryTargetCoords.add(targetGridCoords.get(index));
                        index++;
                    } else {
                        done = true;
                    }
                }

                return new PixelMapEntry() {
                    @Override
                    public int getSourceGridIIndex() {
                        return nextSource[0];
                    }

                    @Override
                    public int getSourceGridJIndex() {
                        return nextSource[1];
                    }

                    @Override
                    public List<int[]> getTargetGridPoints() {
                        return entryTargetCoords;
                    }

                };
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    /**
     * Returns an unmodifiable iterator over all the Scanlines in this pixel map
     */
    public Iterator<Scanline> scanlineIterator() {
        return new ScanlineIterator();
    }

    private final class ScanlineIterator implements Iterator<Scanline> {
        final Iterator<PixelMapEntry> it = iterator();
        private Scanline scanline = null;
        private PixelMapEntry pme = null;

        public ScanlineIterator() {
            if (it.hasNext()) {
                pme = it.next();
                scanline = new SimpleScanline(pme);
            }
        }

        @Override
        public boolean hasNext() {
            return scanline != null;
        }

        @Override
        public Scanline next() {
            while (it.hasNext()) {
                pme = it.next();
                int sourceJ = pme.getSourceGridJIndex();
                if (sourceJ == scanline.getSourceGridJIndex()) {
                    /*
                     * This is part of the same scanline
                     */
                    scanline.getPixelMapEntries().add(pme);
                } else {
                    /*
                     * We have a new scanline. We keep a handle to the old one
                     * and create a new one.
                     */
                    Scanline toReturn = scanline;
                    scanline = new SimpleScanline(pme);
                    /*
                     * We return the completed scanline
                     */
                    return toReturn;
                }
            }
            if (scanline == null) {
                throw new NoSuchElementException();
            } else {
                Scanline toReturn = scanline;
                scanline = null;
                return toReturn;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Immutable iterator.");
        }
    }

    private static final class SimpleScanline implements Scanline {
        private final int j;
        private final List<PixelMapEntry> entries = new ArrayList<PixelMapEntry>();

        public SimpleScanline(PixelMapEntry pme) {
            j = pme.getSourceGridJIndex();
            entries.add(pme);
        }

        @Override
        public int getSourceGridJIndex() {
            return j;
        }

        @Override
        public List<PixelMapEntry> getPixelMapEntries() {
            return entries;
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        RegularAxis lonAxis = new RegularAxisImpl("lon", 64.01358, 0.045, 347400, true);
        RegularAxis latAxis = new RegularAxisImpl("lat", 80.12541, -0.045, 35640, false);
        RegularGrid sourceDomain = new RegularGridImpl(lonAxis, latAxis, DefaultGeographicCRS.WGS84);
        RegularGrid targetDomain = new RegularGridImpl(-180,-90,180,90,DefaultGeographicCRS.WGS84,500,500);

        Runtime rt = Runtime.getRuntime();

        long start = System.nanoTime();
        for(int i = 0; i < 100; i++) {
            PixelMap pixelMap = forGrid(sourceDomain, targetDomain);
        }
        long finish = System.nanoTime();

        System.out.println("Built PixelMap in " + ((finish - start) / 1.e6) + " ms");
        //System.out.println("Number of entries " + pixelMap.numEntries + " (" + pixelMap.pixelMapEntries.length + ")");
        //System.out.println("Num unique pairs = " + pixelMap.getNumUniqueIJPairs());
        //System.out.println("Total insert time " + (pixelMap.insertTime / 1.e6));
        //System.out.println("Stuff shifted " + pixelMap.stuffShifted);
        // With compression:    222 ms, 370k   840x400
        // Without compression: 166ms, 2.7M
        // With compression:    222 ms, 370k   512x512
        // Without compression: 140ms, 2.1M
    }
}
