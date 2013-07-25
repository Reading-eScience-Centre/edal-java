/*
 * Copyright (c) 2013 Reading e-Science Centre, University of Reading, UK
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of Reading e-Science Centre, University of Reading, UK, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.rdg.resc.edal.dataset.cdm;

import java.io.IOException;
import java.util.Iterator;
import ucar.ma2.Index;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.dataset.GridDataSource;
import uk.ac.rdg.resc.edal.dataset.GridMetadata;
import uk.ac.rdg.resc.edal.util.Array;

/**
 *
 * @author Jon
 */
final class CdmGridDataSource implements GridDataSource 
{
    // Note that this is the CDM GridDataset, not the EDAL one
    private GridDataset gd;

    @Override
    public Array<Float> read(GridMetadata gridMetadata, int tIndex,
        int zIndex, int ymin, int ymax, int xmin, int xmax) throws IOException
    {
        // Create RangesList object from GridMetadata object
        // This will lead to many RangesList objects being created during data
        // extraction for PIXEL_BY_PIXEL and SCANLINE strategies.  Should profile
        // to make sure this won't be a problem.
        RangesList rangesList = new RangesList(gridMetadata);
        // Set the ranges for t,z,y and x.  This can be done without raising
        // exceptions even if some axes are missing.
        rangesList.setTRange(tIndex, tIndex);
        rangesList.setZRange(zIndex, zIndex);
        rangesList.setYRange(ymin, ymax);
        rangesList.setXRange(xmin, xmax);
        
        // Get hold of the variable from which we want to read data
        GridDatatype gridDatatype = this.gd.findGridDatatype(gridMetadata.getId());
        VariableDS var = gridDatatype.getVariable();
        
        // Read data, then convert or wrap as Array 
        DataChunk data = DataChunk.readDataChunk(var, rangesList);
        
        // Returns a 2D array that wraps the DataChunk
        // Calculate the shape of the wrapped array, x 
        int[] shape = new int[]{ (ymax - ymin + 1), (xmax - xmin + 1) };
        return new WrappedArray(gridMetadata, data, shape);
    }
    
    private static final class WrappedArray implements Array<Float>
    {
        private final GridMetadata gm;
        private final DataChunk dataChunk;
        private final int[] shape;
        
        public WrappedArray(GridMetadata gm, DataChunk dataChunk, int[] shape) {
            this.gm = gm;
            this.dataChunk = dataChunk;
            this.shape = shape;
        }

        @Override
        public int getNDim() { return 2; }

        @Override
        public int[] getShape() { return this.shape; }

        @Override
        public Class<Float> getValueClass() { return Float.class; }
        
        @Override
        public Float get(int... coords)
        {
            if (coords == null || coords.length != 2)
            {
                throw new IllegalArgumentException("Invalid coordinates: " + coords);
            }
            // The convention is that the x coordinate varies fastest
            int x = coords[1];
            int y = coords[0];
            
            // Create a new index
            Index index = this.dataChunk.getIndex();
            // Initialize the index (this may be unnecessary)
            index.set(new int[index.getRank()]);
            // Set the x and y index values
            index.setDim(this.gm.getYAxisIndex(), y);
            index.setDim(this.gm.getXAxisIndex(), x);
            
            // Now read the data, converting missing values to null if necessary
            float val = this.dataChunk.readFloatValue(index);
            return Float.isNaN(val) ? null : val;
        }

        @Override
        public Iterator<Float> iterator() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long size() {
            return ((long)this.shape[0] * this.shape[1]);
        }
        
    }

    @Override
    public void close() throws IOException {
        // TODO
    }
    
}
