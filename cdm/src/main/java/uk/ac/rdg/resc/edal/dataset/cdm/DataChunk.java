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

package uk.ac.rdg.resc.edal.dataset.cdm;

import java.io.IOException;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset.Enhance;
import ucar.nc2.dataset.VariableDS;

/**
 * Wraps an {@link Array}, providing a method to read data with enhancement
 * applied if necessary
 */
final class DataChunk {
    private static final Logger log = LoggerFactory.getLogger(DataChunk.class);

    private final VariableDS var;
    private final Array arr;
    private final boolean needsEnhance;

    private DataChunk(VariableDS var, Array arr, boolean needsEnhance) {
        this.var = var;
        this.arr = arr;
        this.needsEnhance = needsEnhance;
    }

    /** Creates a DataChunk by reading from the given variable */
    public static DataChunk readDataChunk(VariableDS var, RangesList ranges) throws IOException {
        final Array arr;
        Variable origVar = var.getOriginalVariable();
        if (origVar == null) {
            /* We read from the enhanced variable */
            arr = readVariable(var, ranges);
        } else {
            /*
             * We read from the original variable to avoid enhancing data values
             * that we won't use
             */
            arr = readVariable(origVar, ranges);
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

        return new DataChunk(var, arr, needsEnhance);
    }

    /**
     * Reads from the variable, converting any InvalidRangeExceptions to
     * IllegalArgumentExceptions (they are really run time errors and so should
     * not be checked exceptions).
     */
    private static Array readVariable(Variable var, RangesList ranges) throws IOException {
        try {
            log.debug("Reading from variable {} with ranges {}", var.getFullName(), ranges.toString());
            return var.read(ranges.getRanges());
        } catch (InvalidRangeException ire) {
            throw new IllegalArgumentException(ire);
        }
    }

    /** Gets an Index for the underlying Array */
    public Index getIndex() {
        return arr.getIndex();
    }

    public long size() {
        return arr.getSize();
    }

    /**
     * Reads a data value as a float, applying scale/offset if required.
     * 
     * @return the data value, or {@link Float#NaN} if this is a missing value
     */
    public float readFloatValue(Index index) {
        double val = arr.getFloat(index);
        if (needsEnhance) {
            val = var.convertScaleOffsetMissing(val);
        }
        if (var.isMissing(val))
            return Float.NaN;
        else
            return (float) val;
    }
}