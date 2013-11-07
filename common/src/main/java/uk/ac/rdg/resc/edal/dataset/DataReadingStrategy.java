/**
 * Copyright (c) 2009 The University of Reading
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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.h2.store.DataReader;

import uk.ac.rdg.resc.edal.dataset.DomainMapper.DomainMapperEntry;
import uk.ac.rdg.resc.edal.dataset.DomainMapper.Scanline;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Array4D;
import uk.ac.rdg.resc.edal.util.ValuesArray2D;

/**
 * <p>
 * Defines different strategies for reading data from files. The grid below
 * represents the source data. Black grid squares represent data points that
 * must be read from the source data and will be used to generate the final
 * output (e.g. image):
 * </p>
 * <img src="doc-files/pixelmap_pbp.png">
 * <p>
 * A variety of strategies are possible for reading these data points:
 * </p>
 * 
 * <h3>Strategy 1: read data points one at a time</h3>
 * <p>
 * Read each data point individually by iterating through
 * {@link DomainMapper#getJIndices} and {@link DomainMapper#getIIndices}. This minimizes
 * the memory footprint as the minimum amount of data is read from disk.
 * However, in general this method is inefficient as it maximizes the overhead
 * of the low-level data extraction code by making a large number of small data
 * extractions. This is the {@link #PIXEL_BY_PIXEL pixel-by-pixel} strategy.
 * </p>
 * 
 * <h3>Strategy 2: read all data points in one operation</h3>
 * <p>
 * Read all data in one operation (potentially including lots of data points
 * that are not needed) by finding the overall i-j bounding box with
 * {@link DomainMapper#getMinIIndex}, {@link DomainMapper#getMaxIIndex},
 * {@link DomainMapper#getMinJIndex} and {@link DomainMapper#getMaxJIndex}. This
 * minimizes the number of calls to low-level data extraction code, but may
 * result in a large memory footprint. The {@link DataReader} would then subset
 * this data array in-memory. This is the {@link #BOUNDING_BOX bounding-box}
 * strategy. This approach is recommended for remote datasets (e.g. on an
 * OPeNDAP server) and compressed datasets as it minimizes the overhead
 * associated with the individual data-reading operations.
 * </p>
 * <p>
 * This approach is illustrated in the diagram below. Grey squares represent
 * data points that are read into memory but are discarded because they do not
 * form part of the final image:
 * </p>
 * <img src="doc-files/pixelmap_bbox.png">
 * 
 * <h3>Strategy 3: Read "scanlines" of data</h3>
 * <p>
 * A compromise strategy, which balances memory considerations against the
 * overhead of the low-level data extraction code, works as follows:
 * <ol>
 * <li>Iterate through each row (i.e. each j index) that is represented in the
 * PixelMap using {@link DomainMapper#getJIndices}.</li>
 * <li>For each j index, extract data from the minimum to the maximum i index in
 * this row (a "scanline") using {@link DomainMapper#getMinIIndexInRow} and
 * {@link DomainMapper#getMaxIIndexInRow}. (This assumes that the data are stored
 * with the i dimension varying fastest, meaning that the scanline represents
 * contiguous data in the source files.)</li>
 * </ol>
 * Therefore if there are 25 distinct j indices in the PixelMap there will be 25
 * individual calls to the low-level data extraction code. This algorithm has
 * been found to work well in a variety of situations although it may not always
 * be the most efficient. This is the {@link #SCANLINE scanline} strategy.
 * </p>
 * <p>
 * This approach is illustrated in the diagram below. There is now a much
 * smaller amount of "wasted data" (i.e. grey squares) than in Strategy 2, and
 * there are much fewer individual read operations than in Strategy 1.
 * </p>
 * <img src="doc-files/pixelmap_scanline.png">
 * 
 * @author Jon
 */
public enum DataReadingStrategy {
    /**
     * Reads "scanlines" of data, leading to a smaller memory footprint than the
     * {@link #BOUNDING_BOX bounding-box} strategy, but a larger number of
     * individual data-reading operations. Recommended for use when the overhead
     * of a data-reading operation is low, e.g. for local, uncompressed files.
     */
    SCANLINE {
        @Override
        public Array2D<Number> readMapData(GridDataSource dataSource, String varId, int tIndex,
                int zIndex, Domain2DMapper pixelMap) throws IOException {
            Array2D<Number> ret = new ValuesArray2D(pixelMap.getTargetYSize(),
                    pixelMap.getTargetXSize());

            Iterator<Scanline<int[]>> it = pixelMap.scanlineIterator();
            while (it.hasNext()) {
                Scanline<int[]> scanline = it.next();
                List<DomainMapperEntry<int[]>> entries = scanline.getPixelMapEntries();

                int j = scanline.getSourceGridJIndex();
                int imin = entries.get(0).getSourceGridIIndex();
                int imax = entries.get(entries.size() - 1).getSourceGridIIndex();

                Array4D<Number> data = dataSource.read(varId, tIndex, tIndex, zIndex, zIndex, j, j,
                        imin, imax);

                for (DomainMapperEntry<int[]> dme : entries) {
                    List<int[]> targetGridPoints = dme.getTargetIndices();
                    for (int[] targetPoint : targetGridPoints) {
                        ret.set(data.get(0, 0, 0, dme.getSourceGridIIndex() - imin), new int[] {
                                targetPoint[1], targetPoint[0] });
                    }
                }
            }
            return ret;
        }
    },

    /**
     * Reads all data in a single operation, then subsets in memory. Recommended
     * in situations in which individual data reads have a high overhead, e.g.
     * when reading from OPeNDAP datasets or compressed files.
     */
    BOUNDING_BOX {
        @Override
        public Array2D<Number> readMapData(GridDataSource dataSource, String varId, int tIndex,
                int zIndex, Domain2DMapper pixelMap) throws IOException {
            Array2D<Number> ret = new ValuesArray2D(pixelMap.getTargetYSize(),
                    pixelMap.getTargetXSize());
            int imin = pixelMap.getMinIIndex();
            int imax = pixelMap.getMaxIIndex();
            int jmin = pixelMap.getMinJIndex();
            int jmax = pixelMap.getMaxJIndex();
            Array4D<Number> data = dataSource.read(varId, tIndex, tIndex, zIndex, zIndex, jmin, jmax,
                    imin, imax);
            for (DomainMapperEntry<int[]> pme : pixelMap) {
                List<int[]> targetGridPoints = pme.getTargetIndices();
                for (int[] targetPoint : targetGridPoints) {
                    ret.set(data.get(0, 0, pme.getSourceGridJIndex() - jmin,
                            pme.getSourceGridIIndex() - imin), new int[] { targetPoint[1],
                            targetPoint[0] });
                }
            }
            return ret;
        }
    },

    /**
     * Reads each data point individually. Only efficient if the overhead of
     * reading a single point is not large.
     */
    PIXEL_BY_PIXEL {
        @Override
        public Array2D<Number> readMapData(GridDataSource dataSource, String varId, int tIndex,
                int zIndex, Domain2DMapper pixelMap) throws IOException {
            Array2D<Number> ret = new ValuesArray2D(pixelMap.getTargetYSize(),
                    pixelMap.getTargetXSize());
            for (DomainMapperEntry<int[]> pme : pixelMap) {
                Array4D<Number> data = dataSource.read(varId, tIndex, tIndex, zIndex, zIndex,
                        pme.getSourceGridJIndex(), pme.getSourceGridJIndex(),
                        pme.getSourceGridIIndex(), pme.getSourceGridIIndex());
                List<int[]> targetGridPoints = pme.getTargetIndices();
                for (int[] targetPoint : targetGridPoints) {
                    ret.set(data.get(0, 0, 0, 0), new int[] { targetPoint[1], targetPoint[0] });
                }
            }
            return ret;
        }
    };

    abstract public Array2D<Number> readMapData(GridDataSource dataSource, String varId, int tIndex,
            int zIndex, Domain2DMapper pixelMap) throws IOException;
}
