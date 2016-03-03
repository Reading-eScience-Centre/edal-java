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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
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
    private Map<String, int[]> varId2hztIndices;

    public CdmMeshDataSource(NetcdfDataset nc, Map<String, int[]> varId2hztIndices) {
        this.nc = nc;
        this.varId2hztIndices = varId2hztIndices;
    }

    @Override
    public List<Number> read(String variableId, List<MeshCoordinates3D> coordsToRead)
            throws DataReadingException {
        int[] hztIndices = varId2hztIndices.get(variableId);

        /*
         * First find the range of co-ordinates to read
         */
        int minH = Integer.MAX_VALUE;
        int maxH = -1;
        int minZ = Integer.MAX_VALUE;
        int maxZ = -1;
        int minT = Integer.MAX_VALUE;
        int maxT = -1;
        boolean[] hztRangesSet = new boolean[] { false, false, false };
        for (MeshCoordinates3D coords : coordsToRead) {
            if (hztIndices[0] >= 0) {
                if (coords.h < minH && coords.h >= 0) {
                    minH = coords.h;
                    hztRangesSet[0] = true;
                }
                if (coords.h > maxH) {
                    maxH = coords.h;
                    hztRangesSet[0] = true;
                }
            }
            if (hztIndices[1] >= 0) {
                if (coords.z < minZ && coords.z >= 0) {
                    minZ = coords.z;
                    hztRangesSet[1] = true;
                }
                if (coords.z > maxZ) {
                    maxZ = coords.z;
                    hztRangesSet[1] = true;
                }
            }
            if (hztIndices[2] >= 0) {
                if (coords.t < minT && coords.t >= 0) {
                    minT = coords.t;
                    hztRangesSet[2] = true;
                }
                if (coords.t > maxT) {
                    maxT = coords.t;
                    hztRangesSet[2] = true;
                }
            }
        }

        List<Number> ret = new ArrayList<>();
        boolean rangeSet = (hztIndices[0] < 0 || hztRangesSet[0])
                && (hztIndices[1] < 0 || hztRangesSet[1]) && (hztIndices[2] < 0 || hztRangesSet[2]);
        if (!rangeSet) {
            /*
             * If we have not set a valid range, then it means we don't have any
             * data at all in this area. Fill the return list with nulls
             */
            for (int i = 0; i < coordsToRead.size(); i++) {
                ret.add(null);
            }
        } else {
            /*
             * Find the dimensionality of this variable
             */
            int numDims = 0;
            for (int hztIndex : hztIndices) {
                if (hztIndex >= 0) {
                    numDims++;
                }
            }

            List<Range> ranges = new ArrayList<>();
            for (int i = 0; i < numDims; i++) {
                ranges.add(null);
            }

            try {
                if (hztIndices[0] >= 0) {
                    ranges.set(hztIndices[0], new Range(minH, maxH));
                }

                if (hztIndices[1] >= 0) {
                    ranges.set(hztIndices[1], new Range(minZ, maxZ));
                }

                if (hztIndices[2] >= 0) {
                    ranges.set(hztIndices[2], new Range(minT, maxT));
                }

                /*
                 * See definition of syncObj for explanation of synchronization
                 */
                synchronized (syncObj) {
                    Variable var = nc.findVariable(variableId);
                    Array arr = var.read(ranges);

                    for (MeshCoordinates3D coords : coordsToRead) {
                        if (coords.h < 0 || coords.z < 0 || coords.t < 0) {
                            ret.add(null);
                            continue;
                        }
                        Index index = arr.getIndex();
                        if (hztIndices[2] >= 0) {
                            index.setDim(hztIndices[2], coords.t - minT);
                        }
                        if (hztIndices[1] >= 0) {
                            index.setDim(hztIndices[1], coords.z - minZ);
                        }
                        if (hztIndices[0] >= 0) {
                            try {
                                index.setDim(hztIndices[0], coords.h - minH);
                            } catch (ArrayIndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                        }

                        ret.add(readNumber(arr, index));
                    }
                }
            } catch (ArrayIndexOutOfBoundsException | IOException | InvalidRangeException e) {
                e.printStackTrace();
                throw new DataReadingException("Problem reading data from data source", e);
            }
        }
        return ret;
    }

    private Number readNumber(Array arr, Index index) {
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

    @Override
    public void close() throws DataReadingException {
        NetcdfDatasetAggregator.releaseDataset(nc);
    }
}
