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
import java.util.HashMap;
import java.util.Map;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import uk.ac.rdg.resc.edal.dataset.GridDataSource;
import uk.ac.rdg.resc.edal.dataset.HZTDataSource;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;

/**
 * Implementation of {@link GridDataSource} using the Unidata Common Data Model
 * for NetCDF
 * 
 * @author Guy Griffiths
 * @author Jon
 */
final class CdmMeshDataSource implements HZTDataSource {
    public static Integer instances = 0;
    public int instance = 0;
    /*
     * This is used to synchronize the actual reading. This is necessary because
     * we have the following model:
     * 
     * There is a single NetcdfDataset object per dataset, which gets cached,
     * and closed when the cache becomes full. This is because the overhead of
     * creating a NetcdfDataset is high.
     * 
     * Each time CfHorizontalMesh4dDataset.openGridDataSource() is called, a
     * *new* CdmMeshDataSource is created. We can't keep the individual
     * CdmMeshDataSource objects in memory because it's not predictable as to
     * when the underlying NetcdfDataset will be closed. The overhead of
     * creating a new CdmMeshDataSource is very low compared to the creation of
     * a NetcdfDataset.
     * 
     * When read() is called on separate instances of CdmMeshDataSource which
     * refer to the same location, something happens which causes the array
     * indices to be set incorrectly, and we get an
     * ArrayIndexOutOfBoundsException. We can't synchronize on a non-static
     * object, because these are all separate instances.
     * 
     * This could also have been solved by removing NetcdfDataset caching. I
     * think that this would have a bigger effect on the speed, although that
     * may depend on the use case.
     */
    private static Object syncObj = new Object();
    private NetcdfDataset nc;
    private Map<String, Array> cachedArrays = new HashMap<>();
    private Map<String, int[]> varId2hztIndices;

    public CdmMeshDataSource(NetcdfDataset nc, Map<String, int[]> varId2hztIndices) {
        this.nc = nc;
        synchronized (instances) {
            this.instance = instances++;
        }
        this.varId2hztIndices = varId2hztIndices;
    }

    @Override
    public Number read(String variableId, int tIndex, int zIndex, int hIndex)
            throws DataReadingException {
        int[] hztIndices = varId2hztIndices.get(variableId);
        if ((hIndex < 0 && hztIndices[0] >= 0) || (hztIndices[2] >= 0 && tIndex < 0)
                || (hztIndices[1] >= 0 && zIndex < 0)) {
            return null;
        }
        try {
            /*
             * See definition of syncObj for explanation of synchronization
             */
            synchronized (syncObj) {
                Array arr;
                if (!cachedArrays.containsKey(variableId)) {
                    Variable var = nc.findVariable(variableId);
                    arr = var.read();
                    cachedArrays.put(variableId, arr);
                } else {
                    arr = cachedArrays.get(variableId);
                }

                Index index = arr.getIndex();
                if (hztIndices[2] >= 0) {
                    index.setDim(hztIndices[2], tIndex);
                }
                if (hztIndices[1] >= 0) {
                    index.setDim(hztIndices[1], zIndex);
                }
                if (hztIndices[0] >= 0) {
                    index.setDim(hztIndices[0], hIndex);
                }

                Number val = null;
                switch (arr.getDataType()) {
                case BYTE:
                    val = arr.getByte(index);
                    break;
                case DOUBLE:
                    val = arr.getDouble(index);
                    break;
                case FLOAT:
                    val = arr.getFloat(index);
                    break;
                case INT:
                    val = arr.getInt(index);
                    break;
                case LONG:
                    val = arr.getLong(index);
                    break;
                case SHORT:
                    val = arr.getShort(index);
                    break;
                default:
                    break;
                }

                return val;
            }
        } catch (ArrayIndexOutOfBoundsException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void close() throws DataReadingException {
        NetcdfDatasetAggregator.releaseDataset(nc);
    }

}
