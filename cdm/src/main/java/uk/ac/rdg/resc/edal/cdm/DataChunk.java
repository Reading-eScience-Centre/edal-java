package uk.ac.rdg.resc.edal.cdm;

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
class DataChunk {
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
            // We read from the enhanced variable
            arr = readVariable(var, ranges);
        } else {
            // We read from the original variable to avoid enhancing data
            // values that we won't use
            arr = readVariable(origVar, ranges);
        }

        // Decide whether or not we need to enhance any data values we
        // read from this array
        final boolean needsEnhance;
        Set<Enhance> enhanceMode = var.getEnhanceMode();
        if (enhanceMode.contains(Enhance.ScaleMissingDefer)) {
            // Values read from the array are not enhanced, but need to be
            needsEnhance = true;
        } else if (enhanceMode.contains(Enhance.ScaleMissing)) {
            // We only need to enhance if we read data from the plain Variable
            needsEnhance = origVar != null;
        } else {
            // Values read from the array will not be enhanced
            needsEnhance = false;
        }

        return new DataChunk(var, arr, needsEnhance);
    }

    /**
     * Reads from the variable, converting any InvalidRangeExceptions to
     * IllegalArgumentExceptions (they are really run time errors and so should
     * not be checked exceptions).
     */
    private static final Array readVariable(Variable var, RangesList ranges) throws IOException {
        try {
            log.debug("Reading from variable {} with ranges {}", var.getName(), ranges.toString());
            return var.read(ranges.getRanges());
        } catch (InvalidRangeException ire) {
            throw new IllegalArgumentException(ire);
        }
    }

    /** Gets an Index for the underlying Array */
    public Index getIndex() {
        return this.arr.getIndex();
    }

    /**
     * Reads a data value as a float, applying scale/offset if required.
     * 
     * @return the data value, or {@link Float#NaN} if this is a missing value
     */
    public float readFloatValue(Index index) {
        double val = arr.getFloat(index);
        if (this.needsEnhance) {
            val = this.var.convertScaleOffsetMissing(val);
        }
        if (this.var.isMissing(val))
            return Float.NaN;
        else
            return (float) val;
    }
}