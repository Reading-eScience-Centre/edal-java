/**
 * Copyright (c) 2010 The University of Reading
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
 */

package uk.ac.rdg.resc.edal.dataset;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.util.RArray;
import uk.ac.rdg.resc.edal.util.RLongArray;
import uk.ac.rdg.resc.edal.util.RUByteArray;
import uk.ac.rdg.resc.edal.util.RUIntArray;
import uk.ac.rdg.resc.edal.util.RUShortArray;

/**
 * <p>
 * Maps real-world points to i and j indices of corresponding points within the
 * source data. A DomainMapper is constructed using the following general
 * algorithm:
 * </p>
 * 
 * <pre>
 * For each point in the given targetDomain:
 *    1. Find the grid cell in the source grid that contains this point
 *    2. Let i and j be the coordinates of this grid cell
 *    3. Let p be the index of this point in the target domain
 *    4. Add the mapping (p to i,j) to the pixel map
 * </pre>
 * 
 * <p>
 * The resulting DomainMapper is then used by {@link DataReadingStrategy}s to
 * work out what data to read from the source data files. A variety of
 * strategies are possible for reading these data points, each of which may be
 * optimal in a certain situation.
 * </p>
 * 
 * @author Jon Blower
 * @author Guy
 * @see DataReadingStrategy
 */
public abstract class DomainMapper<T> implements Iterable<DomainMapper.DomainMapperEntry<T>> {
    protected static Logger log = LoggerFactory.getLogger(DomainMapper.class);

    /* Stores the source grid indices */
    private final RArray sourceGridIndices;
    /* Stores the target grid indices */
    private final RArray targetGridIndices;

    /**
     * Maps a point in the source grid to corresponding points in the target
     * grid.
     */
    public interface DomainMapperEntry<P> {
        /**
         * Gets the i index of this point in the source grid
         */
        public int getSourceGridIIndex();

        /**
         * Gets the j index of this point in the source grid
         */
        public int getSourceGridJIndex();

        /**
         * Gets the array of all target indices that correspond with this source
         * grid point.
         * 
         * The indices can be any type of object, as parameterised by P
         */
        public List<P> getTargetIndices();
    }

    /**
     * Holds all the DomainMapperEntries corresponding with a certain j index
     */
    public static interface Scanline<P> {
        /**
         * Gets the j index of this scanline in the source grid
         */
        public int getSourceGridJIndex();

        /**
         * Gets the list of DomainMapperEntries associated with the j index, in
         * order of increasing i index
         */
        public List<DomainMapperEntry<P>> getPixelMapEntries();
    }

    private final int sourceGridISize;

    private final int targetDomainSize;

    /*
     * These define the bounding box (in terms of axis indices) of the data to
     * extract from the source files
     */
    private int minIIndex = Integer.MAX_VALUE;
    private int minJIndex = Integer.MAX_VALUE;
    private int maxIIndex = -1;
    private int maxJIndex = -1;

    protected DomainMapper(HorizontalGrid sourceGrid, long targetDomainSize) {
        if (targetDomainSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Cannot handle target domains"
                    + " greater than Integer.MAX_VALUE in size");
            /*
             * This is essentially because
             * DomainMapperEntry.getTargetGridPoints() returns a List. Also
             * because the results of extracting data are usually held in a
             * primitive array, which can only be indexed by integer values.
             */
        }

        this.targetDomainSize = (int) targetDomainSize;
        sourceGridISize = sourceGrid.getXSize();

        /*
         * Create an estimate of a suitable chunk size. We don't want this to be
         * too small because we would have to do many array copy operations to
         * grow resizeable arrays. Conversely we don't want it to be too large
         * and lead to wasted space.
         */
        int chunkSize = (int) (targetDomainSize < 1000 ? targetDomainSize : targetDomainSize / 10);

        /*
         * Choose storage for the mappings appropriate to the sizes of the
         * domains
         */
        long maxSourceGridIndex = sourceGrid.size() - 1;
        sourceGridIndices = chooseRArray(maxSourceGridIndex, chunkSize);
        log.debug("Source grid indices (max: {}) stored in a {}", maxSourceGridIndex,
                sourceGridIndices.getClass());

        long maxTargetGridIndex = targetDomainSize - 1;
        targetGridIndices = chooseRArray(maxTargetGridIndex, chunkSize);
        log.debug("Target grid indices (max: {}) stored in a {}", maxTargetGridIndex,
                targetGridIndices.getClass());

        /*
         * This is just a double-check: shouldn't happen
         */
        if (targetGridIndices instanceof RLongArray) {
            throw new IllegalStateException("Can't store target grid indices as"
                    + " longs: must be integers or smaller");
        }
    }

    /*
     * Creates and returns a resizable array for holding values up to and
     * including maxElementValue. For example, an unsigned short array may be
     * used if the array will only hold values up to 65535.
     */
    private static RArray chooseRArray(long maxElementValue, int chunkSize) {
        if (maxElementValue <= RUByteArray.MAX_VALUE)
            return new RUByteArray(chunkSize);
        if (maxElementValue <= RUShortArray.MAX_VALUE)
            return new RUShortArray(chunkSize);
        if (maxElementValue <= RUIntArray.MAX_VALUE)
            return new RUIntArray(chunkSize);
        return new RLongArray(chunkSize);
    }

    /**
     * Sorts the arrays of source and target indices so that the arrays are in
     * order of increasing source grid index, then increasing target grid index.
     * Uses an in-place quicksort algorithm adapted from
     * http://www.vogella.de/articles/JavaAlgorithmsQuicksort/article.html.
     */
    protected void sortIndices() {
        int numElements = sourceGridIndices.size();
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
        final long[] pivot = getPair(low + (high - low) / 2);

        /* Divide into two lists */
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
        /* Recursion */
        if (low < j) {
            quicksort(low, j);
        }
        if (i < high) {
            quicksort(i, high);
        }
    }

    /*
     * Gets the pair of [source, target] grid indices at the given index
     */
    private long[] getPair(int index) {
        return new long[] { sourceGridIndices.getLong(index), targetGridIndices.getLong(index) };
    }

    /**
     * Returns <0 if pair1 < pair2, 0 if pair1 == pair2, >0 otherwise.
     * Comparisons are performed first on the source grid index, then on the
     * target grid index.
     */
    private int comparePairs(long[] pair1, long[] pair2) {
        if (pair1[0] < pair2[0])
            return -1;
        if (pair1[0] > pair2[0])
            return 1;
        /*
         * Source grid indices must be equal, so compare target grid indices
         */
        if (pair1[1] < pair2[1])
            return -1;
        if (pair1[1] > pair2[1])
            return 1;
        /*
         * Both equal
         */
        return 0;
    }

    /*
     * Exchanges the values in the source and target grid arrays with indices i1
     * and i2
     */
    private void exchange(int i1, int i2) {
        sourceGridIndices.swapElements(i1, i2);
        targetGridIndices.swapElements(i1, i2);
    }

    /**
     * Adds a new pixel index to this map. Does nothing if either i or j is
     * negative.
     * 
     * @param i
     *            The i index of the point in the source data
     * @param j
     *            The j index of the point in the source data
     * @param targetGridIndex
     *            The index of the corresponding point in the target domain
     */
    protected void put(int i, int j, int targetGridIndex) {
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
         * Calculate a single integer representing this grid point in the source
         * grid
         */
        long sourceGridIndex = (long) j * sourceGridISize + i;

        /*
         * Add to the arrays holding the mapping
         */
        sourceGridIndices.append(sourceGridIndex);
        targetGridIndices.append(targetGridIndex);
    }

    /**
     * Returns true if this DomainMapper does not contain any data: this will
     * happen if there is no intersection between the requested data and the
     * data on disk.
     */
    public boolean isEmpty() {
        return sourceGridIndices.size() == 0;
    }

    /**
     * Returns the size of the target domain
     */
    public int getTargetDomainSize() {
        return targetDomainSize;
    }

    /**
     * Gets the minimum i index in the whole domain mapper
     */
    public int getMinIIndex() {
        return minIIndex;
    }

    /**
     * Gets the minimum j index in the whole domain mapper
     */
    public int getMinJIndex() {
        return minJIndex;
    }

    /**
     * Gets the maximum i index in the whole domain mapper
     */
    public int getMaxIIndex() {
        return maxIIndex;
    }

    /**
     * Gets the maximum j index in the whole domain mapper
     */
    public int getMaxJIndex() {
        return maxJIndex;
    }

    /**
     * <p>
     * Gets the number of unique i-j pairs in this pixel map. When combined with
     * the size of the resulting image we can quantify the under- or
     * over-sampling. This is the number of data points that will be extracted
     * by the {@link DataReadingStrategy#PIXEL_BY_PIXEL} data reading strategy.
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
        DomainMapperEntry<T> pme : this)
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
        return (maxIIndex - minIIndex + 1) * (maxJIndex - minJIndex + 1);
    }

    /**
     * Returns an unmodifiable iterator over all the {@link DomainMapperEntry}s
     * in this PixelMap.
     */
    @Override
    public Iterator<DomainMapperEntry<T>> iterator() {
        return new Iterator<DomainMapperEntry<T>>() {
            /* Index in the array of entries */
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < sourceGridIndices.size();
            }

            @Override
            public DomainMapperEntry<T> next() {
                final long entrySourceIndex = sourceGridIndices.getLong(index);

                final List<Integer> entryTargetIndices = new ArrayList<Integer>();
                entryTargetIndices.add(targetGridIndices.getInt(index));
                index++;

                /*
                 * Now find all the other entries that use the same source grid
                 * index
                 */
                while (hasNext()) {
                    long newSourceIndex = sourceGridIndices.getLong(index);
                    if (newSourceIndex == entrySourceIndex) {
                        entryTargetIndices.add(targetGridIndices.getInt(index));
                        index++;
                    } else {
                        break;
                    }
                }

                return new DomainMapperEntry<T>() {
                    @Override
                    public int getSourceGridIIndex() {
                        return (int) (entrySourceIndex % sourceGridISize);
                    }

                    @Override
                    public int getSourceGridJIndex() {
                        return (int) (entrySourceIndex / sourceGridISize);
                    }

                    @Override
                    public List<T> getTargetIndices() {
                        /*
                         * Here we convert the target index into whatever object
                         * is desired. Subclasses will override the
                         * convertIndexToCoordType method to do the work of this
                         * operation.
                         */
                        return new AbstractList<T>() {
                            @Override
                            public T get(int index) {
                                return DomainMapper.this.convertIndexToCoordType(entryTargetIndices
                                        .get(index));
                            }

                            @Override
                            public int size() {
                                return entryTargetIndices.size();
                            }
                        };
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
     * This performs the conversion from a single long index into whatever
     * coordinate type is required. The simplest example would be to simply
     * return the index for a 1D array (see {@link Domain1DMapper} for this).
     * 
     * TODO This currently takes an int. Do we want to support target domains
     * which are bigger than Integer.MAX_VALUE?
     * 
     * @param index
     *            The index to convert to a co-ordinate
     * @return The co-ordinate represented by the index
     * 
     */
    protected abstract T convertIndexToCoordType(int index);

    /**
     * Returns an unmodifiable iterator over all the Scanlines in this pixel map
     */
    public Iterator<Scanline<T>> scanlineIterator() {
        return new ScanlineIterator();
    }

    private final class ScanlineIterator implements Iterator<Scanline<T>> {
        final Iterator<DomainMapperEntry<T>> it = iterator();
        private Scanline<T> scanline = null;
        private DomainMapperEntry<T> pme = null;

        public ScanlineIterator() {
            if (it.hasNext()) {
                pme = it.next();
                scanline = new SimpleScanline<T>(pme);
            }
        }

        @Override
        public boolean hasNext() {
            return scanline != null;
        }

        @Override
        public Scanline<T> next() {
            while (it.hasNext()) {
                pme = it.next();
                int sourceJ = pme.getSourceGridJIndex();
                if (sourceJ == scanline.getSourceGridJIndex()) {
                    /* This is part of the same scanline */
                    scanline.getPixelMapEntries().add(pme);
                } else {
                    /*
                     * We have a new scanline. We keep a handle to the old one
                     * and create a new one.
                     */
                    Scanline<T> toReturn = scanline;
                    scanline = new SimpleScanline<T>(pme);
                    /* We return the completed scanline */
                    return toReturn;
                }
            }
            if (scanline == null) {
                throw new NoSuchElementException();
            } else {
                Scanline<T> toReturn = scanline;
                scanline = null;
                return toReturn;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Immutable iterator.");
        }
    }

    private static final class SimpleScanline<P> implements Scanline<P> {
        private final int j;
        private final List<DomainMapperEntry<P>> entries = new ArrayList<DomainMapperEntry<P>>();

        public SimpleScanline(DomainMapperEntry<P> pme) {
            j = pme.getSourceGridJIndex();
            entries.add(pme);
        }

        @Override
        public int getSourceGridJIndex() {
            return j;
        }

        @Override
        public List<DomainMapperEntry<P>> getPixelMapEntries() {
            return entries;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + maxIIndex;
        result = prime * result + maxJIndex;
        result = prime * result + minIIndex;
        result = prime * result + minJIndex;
        result = prime * result + sourceGridISize;
        result = prime * result + ((sourceGridIndices == null) ? 0 : sourceGridIndices.hashCode());
        result = prime * result + targetDomainSize;
        result = prime * result + ((targetGridIndices == null) ? 0 : targetGridIndices.hashCode());
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
        @SuppressWarnings("rawtypes")
        DomainMapper other = (DomainMapper) obj;
        if (maxIIndex != other.maxIIndex)
            return false;
        if (maxJIndex != other.maxJIndex)
            return false;
        if (minIIndex != other.minIIndex)
            return false;
        if (minJIndex != other.minJIndex)
            return false;
        if (sourceGridISize != other.sourceGridISize)
            return false;
        if (sourceGridIndices == null) {
            if (other.sourceGridIndices != null)
                return false;
        } else if (!sourceGridIndices.equals(other.sourceGridIndices))
            return false;
        if (targetDomainSize != other.targetDomainSize)
            return false;
        if (targetGridIndices == null) {
            if (other.targetGridIndices != null)
                return false;
        } else if (!targetGridIndices.equals(other.targetGridIndices))
            return false;
        return true;
    }
}
