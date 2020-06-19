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
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDataset.Enhance;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.dataset.GridDataSource;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.util.Array4D;
import uk.ac.rdg.resc.edal.util.cdm.CdmUtils;

/**
 * Implementation of {@link GridDataSource} using the Unidata Common Data Model
 * for NetCDF
 * 
 * @author Guy Griffiths
 * @author Jon
 */
final class CdmGridDataSource implements GridDataSource {
    private static final Logger log = LoggerFactory.getLogger(CdmGridDataSource.class);

    /*
     * Note that this is the CDM GridDataset, not the EDAL one
     */
    private final GridDataset gridDataset;
    private NetcdfDataset nc;
    private Map<String, RangesList> rangeListCache = new HashMap<>();

    /*
     * This is used to synchronize the actual reading. This is necessary because we
     * have the following model:
     * 
     * There is a single NetcdfDataset object per dataset, which gets cached, and
     * closed when the cache becomes full. This is because the overhead of creating
     * a NetcdfDataset is high.
     * 
     * Each time CdmGridDataset.openGridDataSource() is called, a *new*
     * CdmGridDataSource is created. We can't keep the individual CdmGridDataSource
     * objects in memory because it's not predictable as to when the underlying
     * NetcdfDataset will be closed. The overhead of creating a new
     * CdmGridDataSource is very low compared to the creation of a NetcdfDataset.
     * 
     * When read() is called on separate instances of CdmGridDataSource which refer
     * to the same location, something happens which causes the array indices to be
     * set incorrectly, and we get an ArrayIndexOutOfBoundsException. We can't
     * synchronize on a non-static object, because these are all separate instances.
     * 
     * This could also have been solved by removing NetcdfDataset caching. I think
     * that this would have a bigger effect on the speed, although that may depend
     * on the use case.
     */
    private static Object syncObj = new Object();

    /**
     * Instantiate a {@link CdmGridDataSource} from a {@link NetcdfDataset}
     * 
     * @param nc The {@link NetcdfDataset} to read data from
     * @throws IOException If there is a problem opening the dataset
     */
    public CdmGridDataSource(NetcdfDataset nc) throws IOException {
        this.gridDataset = CdmUtils.getGridDataset(nc);
        this.nc = nc;
    }

    /**
     * Instantiate a {@link CdmGridDataSource} from a {@link NetcdfDataset},
     * manually specifying the {@link RangesList}s to use
     * 
     * @param nc        The {@link NetcdfDataset} to read data from
     * @param rangeList A {@link Map} of variable IDs to {@link RangesList}s. Does
     *                  not need to be complete.
     * @throws IOException If there is a problem opening the dataset
     */
    public CdmGridDataSource(NetcdfDataset nc, Map<String, RangesList> rangeList) throws IOException {
        this.gridDataset = CdmUtils.getGridDataset(nc);
        this.nc = nc;
        /*
         * OK, this is necessary because if we just *use* the supplied rangeList as the
         * rangeListCache it can end up getting shared across multiple instances of this
         * class. And that causes problems.
         * 
         * Previously I had this:
         * 
         * this.rangeListCache = new HashMap<>(rangeList);
         * 
         * But that means that the objects in the cache (the RangesList ones) just get a
         * shallow copy and so problems still happen. This code does a full deep-copy of
         * the RangesList objects which then works.
         * 
         * Note: We could just accept an argument of Map<String, int[]> which would save
         * a little time on initially creating the Map (in CdmGridDatasetFactory) and
         * would be slightly more memory-efficient. But this way is clearer.
         */
        if (rangeList != null) {
            for (Entry<String, RangesList> entry : rangeList.entrySet()) {
                int x = entry.getValue().getXAxisIndex();
                int y = entry.getValue().getYAxisIndex();
                int z = entry.getValue().getZAxisIndex();
                int t = entry.getValue().getTAxisIndex();
                rangeListCache.put(entry.getKey(), new RangesList(x, y, z, t));
            }
        }
    }

    @Override
    public Array4D<Number> read(String variableId, int tmin, int tmax, int zmin, int zmax, int ymin, int ymax, int xmin,
            int xmax) throws IOException, DataReadingException {
        /*
         * Get hold of the variable from which we want to read data
         */
        GridDatatype gridDatatype = gridDataset.findGridDatatype(variableId);
        VariableDS var;
        if (gridDatatype != null) {
            /*
             * This is the ideal option, but in the case of staggered grids, we may not have
             * any grid datatypes
             */
            var = gridDatatype.getVariable();
        } else {
            /*
             * In this case, just find the original variable and either cast it or create a
             * new VariableDS, as required
             */
            Variable origVar = nc.findVariable(variableId);
            if (origVar instanceof VariableDS) {
                var = (VariableDS) origVar;
            } else {
                var = new VariableDS(null, origVar, false);
            }
        }

        /*
         * Create RangesList object from GridDatatype object This will lead to many
         * RangesList objects being created during data extraction for PIXEL_BY_PIXEL
         * and SCANLINE strategies.
         * 
         * Therefore we cache it - it doesn't give a huge increase in speed, but it is
         * noticeable
         */
        RangesList rangesList;
        if (rangeListCache.containsKey(variableId)) {
            rangesList = rangeListCache.get(variableId);
        } else {
            /*
             * TODO What if gridDatatype is null????
             */
            rangesList = new RangesList(gridDatatype);
            rangeListCache.put(variableId, rangesList);
        }

        /*
         * If we are extracting a chunk of data which is 3- or 4-dimensional, there is a
         * good chance that we may have memory issues.
         */
        int tSize = tmax - tmin + 1;
        int zSize = zmax - zmin + 1;
        int ySize = ymax - ymin + 1;
        int xSize = xmax - xmin + 1;

        /*
         * Note that this could be increased by forcing a garbage collection.
         * 
         * However, that completely cripples performance.
         */
        long freeBytes = Runtime.getRuntime().freeMemory();
        /*
         * This is actually the amount of storage needed to store an array of floats *
         * 2. The factor of 2 is to cover additional overheads.
         */
        long requiredBytes = (long) xSize * ySize * zSize * tSize * 4 * 2;

        final Array arr;
        Variable origVar = var.getOriginalVariable();

        /*
         * Set the ranges for t,z,y and x. This can be done without raising exceptions
         * even if some axes are missing.
         */
        rangesList.setTRange(tmin, tmax);
        rangesList.setZRange(zmin, zmax);
        rangesList.setYRange(ymin, ymax);
        rangesList.setXRange(xmin, xmax);

        /*
         * If we have no t or z data, or we definitely have enough memory, read all data
         * at once.
         * 
         * If not, we will read in 2D slices.
         */
        if ((tSize == 1 && zSize == 1) || freeBytes > requiredBytes) {
            try {
                /*
                 * See definition of syncObj for explanation of synchronization
                 */
                if (origVar == null) {
                    synchronized (syncObj) {
                        /* We read from the enhanced variable */
                        arr = var.read(rangesList.getRanges());
                    }
                } else {
                    synchronized (syncObj) {
                        /*
                         * We read from the original variable to avoid enhancing data values that we
                         * won't use
                         */
                        arr = origVar.read(rangesList.getRanges());
                    }
                }
            } catch (InvalidRangeException ire) {
                log.error("Problem reading data - invalid range:\n" + "x: " + xmin + " -> " + xmax + "y: " + ymin
                        + " -> " + ymax + "z: " + zmin + " -> " + zmax + "t: " + tmin + " -> " + tmax);
                throw new DataReadingException("Cannot read data - invalid range specified", ire);
            } catch (ArrayIndexOutOfBoundsException e) {
                log.error(this + " caused out of bounds");
                throw e;
            }
        } else {
            /*
             * Reading this section into memory may cause an OutOfMemoryError. Instead, we
             * will read it in 2D xy slices.
             * 
             * This is actually fine for many use cases (e.g. extracting map data, writing
             * to file), but will become a massive issue if profile / timeseries data is
             * extracted. Warn about this.
             * 
             * When applications know it's fine, they can lower the log threshold for this
             * class.
             */
            log.warn(
                    "Not enough free memory to read entire data structure into memory. Data will be read in 2D x-y slices. "
                            + "This will be very inefficient if you are extracting profiles / timeseries. "
                            + "In that case, consider using a higher-level method to extract the profile / timeseries, or increase the heap size");
            /*
             * Simply setting the array to null will cause the WrappedArray to read 2D
             * slices whenever get() is called. If the same 2D slice is accessed on
             * subsequent calls, it is cached.
             */
            arr = null;
        }

        /*
         * Decide whether or not we need to enhance any data values we read from this
         * array
         */
        final boolean needsEnhance;
        Set<Enhance> enhanceMode = var.getEnhanceMode();
        // ScaleMissingDefer has been removed. It's functionality can be
        // achieved by
        // simply not enhancing with ApplyScaleOffset.
        if (!enhanceMode.contains(Enhance.ApplyScaleOffset)) {
            /*
             * Values read from the array are not enhanced, but need to be
             */
            needsEnhance = true;
            // ScaleMissing has been removed. Its functionality can be achieved
            // by combining
            // ApplyScaleOffset and ConvertMissing.
        } else if (enhanceMode.contains(Enhance.ApplyScaleOffset) && enhanceMode.contains(Enhance.ConvertMissing)) {
            /*
             * We only need to enhance if we read data from the plain Variable
             */
            needsEnhance = origVar != null;
        } else {
            /* Values read from the array will not be enhanced */
            needsEnhance = false;
        }

        /*
         * Returns a 4D array that wraps the Array
         */
        int[] shape = new int[] { tSize, zSize, ySize, xSize };
        WrappedArray wrappedArray = new WrappedArray(var, arr, needsEnhance, shape, rangesList);
        return wrappedArray;
    }

    @Override
    public void close() throws DataReadingException {
        NetcdfDatasetAggregator.releaseDataset(nc);
    }

    private static final class WrappedArray extends Array4D<Number> {
        private VariableDS var;
        private Array arr;
        private final int[] shape;
        private final int xAxisIndex;
        private final int yAxisIndex;
        private final int zAxisIndex;
        private final int tAxisIndex;
        private final boolean needsEnhance;
        private final RangesList rangesList;

        /*
         * Used for caching in the case where we read in slices
         */
        private Array cachedArray2D = null;
        private int cachedZ = -1;
        private int cachedT = -1;

        public WrappedArray(VariableDS var, Array arr, boolean needsEnhance, int[] shape, RangesList rangesList) {
            super(shape[0], shape[1], shape[2], shape[3]);
            this.var = var;
            this.shape = shape;
            this.needsEnhance = needsEnhance;
            this.rangesList = rangesList;

            if (needsEnhance && arr != null) {
                // convert(array, convertUnsigned, applyScaleOffset,
                // convertMissing)
                this.arr = var.convert(arr, false, true, true);
            } else {
                this.arr = arr;
            }

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

            Array arrLocal;
            Index index;
            if (this.arr != null) {
                /*
                 * We have already read all of the data into memory. Set the correct indices
                 * ready to read
                 */
                arrLocal = this.arr;
                index = arrLocal.getIndex();
                /*
                 * Set the index values
                 */
                if (tAxisIndex >= 0)
                    index.setDim(tAxisIndex, t);
                if (zAxisIndex >= 0)
                    index.setDim(zAxisIndex, z);
            } else {
                /*
                 * We do not want to read all of the data at once. That means we are going to
                 * read 2D slices on request.
                 */
                if (t == cachedT && z == cachedZ) {
                    /*
                     * If we have already read this 2D slice, use it.
                     */
                    arrLocal = cachedArray2D;
                } else {
                    /*
                     * Need to do a read on the underlying data
                     */
                    rangesList.setTRange(t, t);
                    rangesList.setZRange(z, z);
                    try {
                        arrLocal = var.read(rangesList.getRanges());
                        if (this.needsEnhance) {
                            // convert(array, convertUnsigned, applyScaleOffset,
                            // convertMissing)
                            arrLocal = var.convert(arrLocal, false, true, true);
                        }
                    } catch (IOException | InvalidRangeException e) {
                        log.error("Problem reading underlying data", e);
                        return null;
                    }
                    /*
                     * Store the newly-read slice for subsequent reads.
                     */
                    cachedArray2D = arrLocal;
                    cachedT = t;
                    cachedZ = z;
                }
                /*
                 * Set z/t indices - they will always be 0, since the data is 1D in these
                 * directions.
                 */
                index = arrLocal.getIndex();
                if (tAxisIndex >= 0)
                    index.setDim(tAxisIndex, 0);
                if (zAxisIndex >= 0)
                    index.setDim(zAxisIndex, 0);
            }

            /*
             * Set x/y indices. These are the same whether we're reading from a slice or the
             * whole data chunk.
             */
            if (yAxisIndex >= 0)
                index.setDim(yAxisIndex, y);
            if (xAxisIndex >= 0)
                index.setDim(xAxisIndex, x);

            /*
             * Do the actual data read.
             */
            Number val = null;
            switch (arrLocal.getDataType()) {
            case BYTE:
                val = arrLocal.getByte(index);
                while (val.doubleValue() < var.getValidMin()) {
                    val = val.intValue() + 256;
                }
                break;
            case DOUBLE:
                val = arrLocal.getDouble(index);
                break;
            case FLOAT:
                val = arrLocal.getFloat(index);
                break;
            case INT:
                val = arrLocal.getInt(index);
                break;
            case LONG:
                val = arrLocal.getLong(index);
                break;
            case SHORT:
                val = arrLocal.getShort(index);
                break;
            default:
                break;
            }

            if (isMissing(val)) {
                return null;
            } else {
                return val;
            }
        }

        @Override
        public void set(Number val, int... coords) {
            throw new UnsupportedOperationException("Modification not supported.");
        }

        @Override
        public long size() {
            return shape[0] * shape[1] * shape[2] * shape[3];
        }

        /**
         * Performs the same checks as {@link VariableDS#isMissing(double)}, but allows
         * a tolerance of 1e-7 on the maximum and minimum values. This is because when
         * using aggregations we have no underlying original variable. In these cases,
         * the valid min/max get automatically enhanced as doubles, but the value gets
         * enhanced as its underlying data type. If this is a float, then rounding
         * errors can occur.
         * 
         * e.g. the valid max may be 1.0f, but 0.9999999776482582. The valid max is
         * represented in the double form, but the value is represented in the floating
         * point form is 1.0, which is greater than the valid max, even if in the
         * underlying data they are equal.
         * 
         * @param num The value to check
         * @return Whether or not this should be considered missing data
         */
        private boolean isMissing(Number num) {
            if (num == null) {
                return true;
            } else {
                double val = num.doubleValue();
                if (var.hasFillValue() && var.isFillValue(val) || var.hasMissingValue() && var.isMissingValue(val)
                        || Double.isNaN(val)) {
                    return true;
                } else if (var.hasValidData()) {
                    if (var.getValidMax() != -Double.MAX_VALUE) {
                        if (val > var.getValidMax() && (val - var.getValidMax()) > 1e-7) {
                            return true;
                        }
                    }
                    if (var.getValidMin() != Double.MAX_VALUE) {
                        if (val < var.getValidMin() && (var.getValidMin() - val) > 1e-7) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }
    }
}
