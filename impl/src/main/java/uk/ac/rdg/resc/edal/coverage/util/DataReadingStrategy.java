/*******************************************************************************
 * Copyright (c) 2011 The University of Reading
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
 *******************************************************************************/
package uk.ac.rdg.resc.edal.coverage.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.util.PixelMap.PixelMapEntry;
import uk.ac.rdg.resc.edal.util.Extents;

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
 * {@link PixelMap#getJIndices} and {@link PixelMap#getIIndices}. This minimizes
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
 * {@link PixelMap#getMinIIndex}, {@link PixelMap#getMaxIIndex},
 * {@link PixelMap#getMinJIndex} and {@link PixelMap#getMaxJIndex}. This
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
 * PixelMap using {@link PixelMap#getJIndices}.</li>
 * <li>For each j index, extract data from the minimum to the maximum i index in
 * this row (a "scanline") using {@link PixelMap#getMinIIndexInRow} and
 * {@link PixelMap#getMaxIIndexInRow}. (This assumes that the data are stored
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

        // Stores all the PixelMapEntries at a certain j index
        class Scanline {
            private final int jIndex;
            private final List<PixelMapEntry> pixelMapEntries = new ArrayList<PixelMapEntry>();

            public Scanline(PixelMapEntry pme) {
                this.jIndex = pme.getSourceGridJIndex();
                this.pixelMapEntries.add(pme);
            }
        }

        @Override
        protected <R> int populatePixelArray(List<R> data, PixelMap pixelMap, int tIndex, int zIndex,
                GridSeriesCoverage<R> coverage) throws IOException {
            Iterator<PixelMapEntry> it = pixelMap.iterator();
            if (!it.hasNext())
                return 0;
            PixelMapEntry pme = it.next();
            Scanline scanline = new Scanline(pme);

            int dataPointsRead = 0;
            while (it.hasNext()) {
                pme = it.next();
                int sourceJ = pme.getSourceGridJIndex();
                if (sourceJ == scanline.jIndex) {
                    scanline.pixelMapEntries.add(pme);
                } else {
                    // We have a new scanline.
                    // We read the data for the existing scanline first
                    dataPointsRead += this.readScanline(data, coverage, zIndex, tIndex, scanline);
                    // Now we create a new scanline
                    scanline = new Scanline(pme);
                }
            }

            // We must read the last scanline
            dataPointsRead += this.readScanline(data, coverage, zIndex, tIndex, scanline);

            return dataPointsRead;
        }

        private <R> int readScanline(List<R> data, GridSeriesCoverage<R> coverage, int zIndex,
                int tIndex, Scanline scanline) {
            Extent<Integer> yExtent = Extents.newExtent(scanline.jIndex, scanline.jIndex);
            Extent<Integer> zExtent = Extents.newExtent(zIndex, zIndex);
            Extent<Integer> tExtent = Extents.newExtent(tIndex, tIndex);
            int imin = scanline.pixelMapEntries.get(0).getSourceGridIIndex();
            int imax = scanline.pixelMapEntries.get(scanline.pixelMapEntries.size() - 1)
                    .getSourceGridIIndex();
            Extent<Integer> xExtent = Extents.newExtent(imin, imax);

            // logger.debug(ranges.toString());

            List<R> dataChunk = coverage.evaluate(tExtent, zExtent, yExtent, xExtent);

            // Now copy the scanline's data to the picture array
            for (PixelMapEntry pme : scanline.pixelMapEntries) {
                int xIndex = pme.getSourceGridIIndex() - imin;
                /*
                 * Because we are reading something which only varies in x, we only
                 * need to check the x-index
                 */
                R val = dataChunk.get(xIndex);

                /*
                 * Now we set the value of all the image pixels associated with
                 * this data point.
                 */
                for (int p : pme.getTargetGridPoints()) {
                    data.set(p, val);
                }
            }

            // Return the number of data points read
            return imax - imin + 1;
        }
    },

    /**
     * Reads all data in a single operation, then subsets in memory. Recommended
     * in situations in which individual data reads have a high overhead, e.g.
     * when reading from OPeNDAP datasets or compressed files.
     */
    BOUNDING_BOX {

        @Override
        protected <R> int populatePixelArray(List<R> data, PixelMap pixelMap, int tIndex, int zIndex,
                GridSeriesCoverage<R> coverage) throws IOException {
            // Read the whole chunk of x-y data
            int imin = pixelMap.getMinIIndex();
            int imax = pixelMap.getMaxIIndex();
            int jmin = pixelMap.getMinJIndex();
            int jmax = pixelMap.getMaxJIndex();
            Extent<Integer> tExtent = Extents.newExtent(tIndex, tIndex);
            Extent<Integer> zExtent = Extents.newExtent(zIndex, zIndex);
            Extent<Integer> yExtent = Extents.newExtent(jmin, jmax);
            Extent<Integer> xExtent = Extents.newExtent(imin, imax);

            int xSize = xExtent.getHigh() - xExtent.getLow() + 1;
            int ySize = yExtent.getHigh() - yExtent.getLow() + 1;

            List<R> dataChunk = coverage.evaluate(tExtent, zExtent, yExtent, xExtent);

            for (PixelMapEntry pme : pixelMap) {
                int xIndex = pme.getSourceGridIIndex() - imin;
                int yIndex = pme.getSourceGridJIndex() - jmin;
                int index = getIndex(xIndex, yIndex, 0, 0, xSize, ySize, 1);
                R val = dataChunk.get(index);
                for (int targetGridPoint : pme.getTargetGridPoints()) {
                    data.set(targetGridPoint, val);
                }
            }

            // Return the number of data points read
            return (imax - imin + 1) * (jmax - jmin + 1);
        }
    },

    /**
     * Reads each data point individually. Only efficient if the overhead of
     * reading a single point is not large.
     */
    PIXEL_BY_PIXEL {

        @Override
        protected <R> int populatePixelArray(List<R> data, PixelMap pixelMap, int tIndex, int zIndex,
                GridSeriesCoverage<R> coverage) throws IOException {
            int numDataPointsRead = 0;
            for (PixelMapEntry pme : pixelMap) {
                R val = coverage.evaluate(tIndex, zIndex, pme.getSourceGridJIndex(), pme.getSourceGridIIndex());
                numDataPointsRead++;
                for (int targetGridPoint : pme.getTargetGridPoints()) {
                    data.set(targetGridPoint, val);
                }
            }
            return numDataPointsRead;
        }
    };

    /**
     * Reads data from the given GridDatatype, populating the passed-in array of
     * floats. Returns the number of bytes actually read from the source data
     * files (which may be considerably larger than the size of the data array).
     * @param <R>
     * 
     * @see PixelMap
     */
    public synchronized final <R> int readHorizontalData(int tIndex, int zIndex, GridSeriesCoverage<R> coverage, PixelMap pixelMap,
            List<R> data) throws IOException {
        int dataPointsRead = this.populatePixelArray(data, pixelMap, tIndex, zIndex, coverage);

        // Calculate the number of bytes that we read from the source data
        int bytesPerDataPoint = Float.SIZE/8;
        return dataPointsRead * bytesPerDataPoint;
    }
    
    protected int getIndex(int xIndex, int yIndex, int zIndex, int tIndex, int xSize, int ySize, int zSize) {
        return xIndex + yIndex * xSize + zIndex * zSize * ySize + xSize * ySize * zSize * tIndex;
    }

    /**
     * Reads data from the given variable, populating the given data array
     * @param <R>
     * 
     * @return The number of data points actually read from the source data
     */
    abstract <R> int populatePixelArray(List<R> data, PixelMap pixelMap, int tIndex, int zIndex,
            GridSeriesCoverage<R> coverage) throws IOException;
}
