/*******************************************************************************
 * Copyright (c) 2012 The University of Reading
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

package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.impl.PixelMap.PixelMapEntry;
import uk.ac.rdg.resc.edal.coverage.impl.PixelMap.Scanline;

/**
 * Strategy for reading data from a {@link GridValuesMatrix}.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public enum DataReadingStrategy {

    PIXEL_BY_PIXEL {
        @Override
        protected void extractCoverageValues(GridValuesMatrix<?> gridValues, PixelMap pixelMap,
                List<Object> values) {
            int[] point = new int[gridValues.getNDim()];
            for (PixelMapEntry pme : pixelMap) {
                point[0] = pme.getSourceGridIIndex();
                point[1] = pme.getSourceGridJIndex();
                Object value = gridValues.readPoint(point);
                for (int index : pme.getTargetGridPoints()) {
                    values.set(index, value);
                }
            }
        }
    },

    SCANLINE {
        @Override
        protected void extractCoverageValues(GridValuesMatrix<?> gridValues, PixelMap pixelMap,
                List<Object> values) {
            Iterator<Scanline> it = pixelMap.scanlineIterator();
            while (it.hasNext()) {
                Scanline scanline = it.next();
                List<PixelMapEntry> entries = scanline.getPixelMapEntries();
                int entriesSize = entries.size();

                int j = scanline.getSourceGridJIndex();
                int imin = entries.get(0).getSourceGridIIndex();
                int imax = entries.get(entriesSize - 1).getSourceGridIIndex();

                int nDim = gridValues.getNDim();
                int[] mins = new int[nDim];
                mins[0] = imin;
                mins[1] = j;
                int[] maxes = new int[nDim];
                maxes[0] = imax;
                maxes[1] = j;
                
                GridValuesMatrix<?> block = gridValues.readBlock(mins, maxes);

                int[] point = new int[nDim];
                for (PixelMapEntry pme : entries) {
                    point[0] = pme.getSourceGridIIndex() - imin;
                    for(int i=1; i<point.length; i++){
                        point[i] = 0;
                    }
                    Object val = block.readPoint(point);
                    for (int p : pme.getTargetGridPoints()) {
                        values.set(p, val);
                    }
                }
                /*
                 * This will probably do nothing, because the result of
                 * readBlock() will be an in-memory structure.
                 */
                block.close();
            }
        }
    },

    BOUNDING_BOX {
        @Override
        protected void extractCoverageValues(GridValuesMatrix<?> gridValues, PixelMap pixelMap,
                List<Object> values) {
            int imin = pixelMap.getMinIIndex();
            int imax = pixelMap.getMaxIIndex();
            int jmin = pixelMap.getMinJIndex();
            int jmax = pixelMap.getMaxJIndex();
            
            int nDim = gridValues.getNDim();
            int[] mins = new int[nDim];
            mins[0] = imin;
            mins[1] = jmin;
            int[] maxes = new int[nDim];
            maxes[0] = imax;
            maxes[1] = jmax;

            GridValuesMatrix<?> block = gridValues.readBlock(mins, maxes);

            int[] point = new int[nDim];
            for (PixelMapEntry pme : pixelMap) {
                point[0] = pme.getSourceGridIIndex() - imin;
                point[1] = pme.getSourceGridJIndex() - jmin;
                Object val = block.readPoint(point);
                for (int targetGridPoint : pme.getTargetGridPoints()) {
                    values.set(targetGridPoint, val);
                }
            }
            /*
             * This will probably do nothing, because the result of readBlock()
             * will be an in-memory structure.
             */
            block.close();
        }
    };

    public List<Object> readValues(GridValuesMatrix<?> gridValuesMatrix,
            HorizontalGrid sourceDomain, HorizontalGrid targetDomain) {

        PixelMap pixelMap = PixelMap.forGrid(sourceDomain, targetDomain);

        List<Object> values = listOfNulls(pixelMap.getTargetDomainSize());

        extractCoverageValues(gridValuesMatrix, pixelMap, values);

        gridValuesMatrix.close();

        return values;
    }

    protected abstract void extractCoverageValues(GridValuesMatrix<?> gridValues,
            PixelMap pixelMap, List<Object> values);

    /**
     * Creates and returns a new mutable list consisting entirely of null
     * values. The values of the list can be altered through set(), but the size
     * of the list cannot be altered.
     * 
     * @param size
     *            The size of the list to create
     * @return a new mutable list consisting entirely of null values.
     */
    private static List<Object> listOfNulls(int size) {
        final Object[] arr = new Object[size];
        Arrays.fill(arr, null);
        return new AbstractList<Object>() {

            @Override
            public Object get(int index) {
                return arr[index];
            }

            @Override
            public Object set(int index, Object newValue) {
                Object oldValue = arr[index];
                arr[index] = newValue;
                return oldValue;
            }

            @Override
            public int size() {
                return arr.length;
            }

        };
    }
}
