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

package uk.ac.rdg.resc.edal.dataset.cdm;

import java.io.IOException;
import java.util.Iterator;

import ucar.ma2.Index;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.dataset.GridDataSource;
import uk.ac.rdg.resc.edal.util.Array4D;

/**
 * Implementation of {@link GridDataSource} using the Unidata Common Data Model
 * for NetCDF
 * 
 * @author Jon
 * @author Guy
 */
final class CdmGridDataSource implements GridDataSource {
    /*
     * Note that this is the CDM GridDataset, not the EDAL one
     */
    private final GridDataset gridDataset;
    
    public CdmGridDataSource(GridDataset gridDataset) {
        this.gridDataset = gridDataset;
    }

    @Override
    public Array4D<Number> read(String variableId, int tmin, int tmax, int zmin, int zmax, int ymin,
            int ymax, int xmin, int xmax) throws IOException {

        /*
         * Get hold of the variable from which we want to read data
         */
        GridDatatype gridDatatype = gridDataset.findGridDatatype(variableId);
        VariableDS var = gridDatatype.getVariable();

        /*
         * Create RangesList object from GridDatatype object This will lead to
         * many RangesList objects being created during data extraction for
         * PIXEL_BY_PIXEL and SCANLINE strategies. Should profile to make sure
         * this won't be a problem.
         * 
         * TODO test overhead and cache if required
         */
        RangesList rangesList = new RangesList(gridDatatype);
        /*
         * Set the ranges for t,z,y and x. This can be done without raising
         * exceptions even if some axes are missing.
         */
        rangesList.setTRange(tmin, tmax);
        rangesList.setZRange(zmin, zmax);
        rangesList.setYRange(ymin, ymax);
        rangesList.setXRange(xmin, xmax);

        /*
         * Read data, then convert or wrap as Array
         */
        DataChunk data = DataChunk.readDataChunk(var, rangesList);

        /*
         * Returns a 4D array that wraps the DataChunk
         * 
         * TODO Hmm. This is not an in-memory object. Perhaps that doesn't
         * matter, but we need to be aware of it when using this class to create
         * features
         */
        int[] shape = new int[] { (tmax - tmin + 1), (zmax - zmin + 1), (ymax - ymin + 1), (xmax - xmin + 1) };
        return new WrappedArray(rangesList, data, shape);
    }

    @Override
    public void close() throws IOException {
        gridDataset.close();
    }

    private static final class WrappedArray extends Array4D<Number> {
        private final DataChunk dataChunk;
        private final int[] shape;
        private final int xAxisIndex;
        private final int yAxisIndex;
        private final int zAxisIndex;
        private final int tAxisIndex;

        public WrappedArray(RangesList rangesList, DataChunk dataChunk, int[] shape) {
            super(shape[0], shape[1], shape[2], shape[3]);
            this.dataChunk = dataChunk;
            this.shape = shape;
            xAxisIndex = rangesList.getXAxisIndex();
            yAxisIndex = rangesList.getYAxisIndex();
            zAxisIndex = rangesList.getZAxisIndex();
            tAxisIndex = rangesList.getTAxisIndex();
        }

        @Override
        public int[] getShape() {
            return shape;
        }

        @Override
        public Number get(int... coords) {
            /*
             * The convention is that the x coordinate varies fastest
             */
            int x = coords[3];
            int y = coords[2];
            int z = coords[1];
            int t = coords[0];

            /*
             * Create a new index
             */
            Index index = dataChunk.getIndex();
            /*
             * Initialize the index (this may be unnecessary)
             */
            index.set(new int[index.getRank()]);
            /*
             * Set the x and y index values
             */
            if (tAxisIndex > 0)
                index.setDim(tAxisIndex, t);
            if (zAxisIndex > 0)
                index.setDim(zAxisIndex, z);
            if (yAxisIndex > 0)
                index.setDim(yAxisIndex, y);
            if (xAxisIndex > 0)
                index.setDim(xAxisIndex, x);

            /*
             * Now read the data, converting missing values to null if necessary
             */
            float val = dataChunk.readFloatValue(index);
            return Float.isNaN(val) ? null : val;
        }

        @Override
        public void set(Number val, int... coords) {
            throw new UnsupportedOperationException("Modification not supported.");
        }

        @Override
        public Iterator<Number> iterator() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long size() {
            return shape[0] * shape[1] * shape[2] * shape[3];
        }

        @Override
        public Class<Number> getValueClass() {
            return Number.class;
        }
    }
}
