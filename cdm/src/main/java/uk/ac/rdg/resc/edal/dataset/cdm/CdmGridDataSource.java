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
import java.util.Set;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset.Enhance;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.dataset.GridDataSource;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.util.Array4D;

/**
 * Implementation of {@link GridDataSource} using the Unidata Common Data Model
 * for NetCDF
 * 
 * @author Guy Griffiths
 * @author Jon
 */
final class CdmGridDataSource implements GridDataSource {
    /*
     * Note that this is the CDM GridDataset, not the EDAL one
     */
    private final GridDataset gridDataset;
    private Map<String, RangesList> rangeListCache = new HashMap<>();

    /*
     * This is used to synchronize the actual reading. This is necessary because
     * we have the following model:
     * 
     * There is a single NetcdfDataset object per dataset, which gets cached,
     * and closed when the cache becomes full. This is because the overhead of
     * creating a NetcdfDataset is high.
     * 
     * Each time CdmGridDataset.openGridDataSource() is called, a *new*
     * CdmGridDataSource is created. We can't keep the individual
     * CdmGridDataSource objects in memory because it's not predictable as to
     * when the underlying NetcdfDataset will be closed. The overhead of
     * creating a new CdmGridDataSource is very low compared to the creation of
     * a NetcdfDataset.
     * 
     * When read() is called on separate instances of CdmGridDataSource which
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

    public CdmGridDataSource(GridDataset gridDataset) {
        this.gridDataset = gridDataset;
    }

    @Override
    public Array4D<Number> read(String variableId, int tmin, int tmax, int zmin, int zmax,
            int ymin, int ymax, int xmin, int xmax) throws IOException, DataReadingException {
        /*
         * Get hold of the variable from which we want to read data
         */
        GridDatatype gridDatatype = gridDataset.findGridDatatype(variableId);
        VariableDS var = gridDatatype.getVariable();

        /*
         * Create RangesList object from GridDatatype object This will lead to
         * many RangesList objects being created during data extraction for
         * PIXEL_BY_PIXEL and SCANLINE strategies.
         * 
         * Therefore we cache it - it doesn't give a huge increase in speed, but
         * it is noticeable
         */
        RangesList rangesList;
        if (rangeListCache.containsKey(variableId)) {
            rangesList = rangeListCache.get(variableId);
        } else {
            rangesList = new RangesList(gridDatatype);
            rangeListCache.put(variableId, rangesList);
        }

        /*
         * Set the ranges for t,z,y and x. This can be done without raising
         * exceptions even if some axes are missing.
         */
        rangesList.setTRange(tmin, tmax);
        rangesList.setZRange(zmin, zmax);
        rangesList.setYRange(ymin, ymax);
        rangesList.setXRange(xmin, xmax);

        final Array arr;
        Variable origVar = var.getOriginalVariable();

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
                     * We read from the original variable to avoid enhancing
                     * data values that we won't use
                     */
                    arr = origVar.read(rangesList.getRanges());
                }
            }
        } catch (InvalidRangeException ire) {
            throw new DataReadingException("Cannot read data - invalid range specified", ire);
        } catch (ArrayIndexOutOfBoundsException e) {
//            System.out.println(xmin+" -> "+xmax);
//            System.out.println(ymin+" -> "+ymax);
//            System.out.println(zmin+" -> "+zmax);
//            System.out.println(ymin+" -> "+tmax);
            System.out.println(this + " caused out of bounds");
            throw e;
        }

        /*
         * Decide whether or not we need to enhance any data values we read from
         * this array
         */
        final boolean needsEnhance;
        Set<Enhance> enhanceMode = var.getEnhanceMode();
        if (enhanceMode.contains(Enhance.ScaleMissingDefer)) {
            /* Values read from the array are not enhanced, but need to be */
            needsEnhance = true;
        } else if (enhanceMode.contains(Enhance.ScaleMissing)) {
            /* We only need to enhance if we read data from the plain Variable */
            needsEnhance = origVar != null;
        } else {
            /* Values read from the array will not be enhanced */
            needsEnhance = false;
        }

        /*
         * Returns a 4D array that wraps the Array
         */
        int[] shape = new int[] { (tmax - tmin + 1), (zmax - zmin + 1), (ymax - ymin + 1),
                (xmax - xmin + 1) };
        WrappedArray wrappedArray = new WrappedArray(var, arr, needsEnhance, shape, rangesList);
        return wrappedArray;
    }

    @Override
    public void close() throws DataReadingException {
        /*
         * We do not close this DataSource. The CdmGridDatasetFactory keeps a
         * cache of NetcdfDataset objects and will close them when the cache
         * becomes full.
         * 
         * This is a big speed improvement when the same dataset is accessed
         * multiple times in quick succession
         */
    }

    private static final class WrappedArray extends Array4D<Number> {
        private VariableDS var;
        private Array arr;
        private final int[] shape;
        private final int xAxisIndex;
        private final int yAxisIndex;
        private final int zAxisIndex;
        private final int tAxisIndex;

        public WrappedArray(VariableDS var, Array arr, boolean needsEnhance, int[] shape,
                RangesList rangesList) {
            super(shape[0], shape[1], shape[2], shape[3]);
            this.var = var;
            this.shape = shape;

            if (needsEnhance) {
                this.arr = var.convertScaleOffsetMissing(arr);
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

            /*
             * Create a new index
             */
            Index index = arr.getIndex();
            /*
             * Set the index values
             */
            if (tAxisIndex >= 0)
                index.setDim(tAxisIndex, t);
            if (zAxisIndex >= 0)
                index.setDim(zAxisIndex, z);
            if (yAxisIndex >= 0)
                index.setDim(yAxisIndex, y);
            if (xAxisIndex >= 0)
                index.setDim(xAxisIndex, x);

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
         * Performs the same checks as {@link VariableDS#isMissing(double)}, but
         * allows a tolerance of 1e-7 on the maximum and minimum values. This is
         * because when using aggregations we have no underlying original
         * variable. In these cases, the valid min/max get automatically
         * enhanced as doubles, but the value gets enhanced as its underlying
         * data type. If this is a float, then rounding errors can occur.
         * 
         * e.g. the valid max may be 1.0f, but 0.9999999776482582. The valid max
         * is represented in the double form, but the value is represented in
         * the floating point form is 1.0, which is greater than the valid max,
         * even if in the underlying data they are equal.
         * 
         * @param val
         *            The value to check
         * @return Whether or not this should be considered missing data
         */
        private boolean isMissing(Number num) {
            if (num == null) {
                return true;
            } else {
                double val = num.doubleValue();
                if (var.hasFillValue() && var.isFillValue(val) || var.hasMissingValue()
                        && var.isMissingValue(val) || Double.isNaN(val)) {
                    return true;
                } else if (var.hasInvalidData()) {
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
