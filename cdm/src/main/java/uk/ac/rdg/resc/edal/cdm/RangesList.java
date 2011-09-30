package uk.ac.rdg.resc.edal.cdm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dt.GridDatatype;

/**
 * <p>
 * Wraps a List of {@link Range}s, providing methods to safely set ranges for x,
 * y, z and t. Call {@link #getRanges()} to get a List of Range objects that can
 * be passed directly to {@link Variable#read(java.util.List)}.
 * </p>
 * <p>
 * The setXRange() methods and its cousins do nothing if there is no
 * corresponding axis, hence there is no need to worry about
 * InvalidRangeExceptions for axes that do not exist.
 * </p>
 * 
 * @author Jon Blower
 * @author Guy Griffiths
 */

final class RangesList {
    private static final Logger log = LoggerFactory.getLogger(RangesList.class);

    private static final Range ZERO_RANGE;

    private final List<Range> ranges;

    // The indices of these axes in the *physical* data arrays.
    private int xAxisIndex = -1;
    private int yAxisIndex = -1;
    private int zAxisIndex = -1;
    private int tAxisIndex = -1;

    static {
        try {
            ZERO_RANGE = new Range(0, 0);
        } catch (InvalidRangeException ire) {
            throw new ExceptionInInitializerError(ire);
        }
    }

    public RangesList(GridDatatype grid) {
        Variable var = grid.getVariable();
        int rank = var.getRank();
        this.ranges = new ArrayList<Range>(rank);
        for (int i = 0; i < rank; i++) {
            this.ranges.add(ZERO_RANGE);
        }

        // We need to find the indices of the four axes in the *physical* data
        // arrays. Note that GridDatatype.getXDimensionIndex() and its cousins
        // return the index in *canonical* (tzyx) order and therefore can't be
        // used.
        String xDimName = grid.getXDimension() == null ? null : grid.getXDimension().getName();
        String yDimName = grid.getYDimension() == null ? null : grid.getYDimension().getName();
        String zDimName = grid.getZDimension() == null ? null : grid.getZDimension().getName();
        String tDimName = grid.getTimeDimension() == null ? null : grid.getTimeDimension().getName();

        // The dimensions in this list are in physical order
        List<Dimension> dims = var.getDimensions();
        // Look through the list, looking for the x,y,z,t dimensions based on
        // name
        for (int i = 0; i < dims.size(); i++) {
            Dimension dim = dims.get(i);
            if (dim.getName().equals(xDimName))
                this.xAxisIndex = i;
            else if (dim.getName().equals(yDimName))
                this.yAxisIndex = i;
            else if (dim.getName().equals(zDimName))
                this.zAxisIndex = i;
            else if (dim.getName().equals(tDimName))
                this.tAxisIndex = i;
        }

        log.debug("Created RangesList: Shape = {}", Arrays.toString(grid.getShape()));
        log.debug("            ....    Rank = {}, x = {}, y = {}, z = {}, t = {}", new Object[] { rank,
                this.xAxisIndex, this.yAxisIndex, this.zAxisIndex, this.tAxisIndex });
    }

    public void setXRange(int xmin, int xmax) {
        this.setRange(this.xAxisIndex, xmin, xmax);
    }

    public void setYRange(int ymin, int ymax) {
        this.setRange(this.yAxisIndex, ymin, ymax);
    }

    public void setZRange(int zmin, int zmax) {
        this.setRange(this.zAxisIndex, zmin, zmax);
    }

    public void setTRange(int tmin, int tmax) {
        this.setRange(this.tAxisIndex, tmin, tmax);
    }

    private void setRange(int index, int min, int max) {
        if (index >= 0 && min >= 0 && max >= 0) // TODO: silent failure?
        {
            try {
                this.ranges.set(index, new Range(min, max));
            } catch (InvalidRangeException ire) {
                // This is a programming error, so is wrapped as a runtime
                // exception
                throw new IllegalArgumentException(ire);
            }
        }
    }

    private Range getRange(int index) {
        if (index >= 0)
            return this.ranges.get(index);
        else
            return null;
    }

    /**
     * Gets the index of the x axis within the {@link #getRanges() list of
     * ranges}.
     */
    public int getXAxisIndex() {
        return this.xAxisIndex;
    }

    /**
     * Gets the index of the y axis within the {@link #getRanges() list of
     * ranges}.
     */
    public int getYAxisIndex() {
        return this.yAxisIndex;
    }

    /**
     * Gets the index of the z axis within the {@link #getRanges() list of
     * ranges}.
     */
    public int getZAxisIndex() {
        return this.zAxisIndex;
    }

    /**
     * Gets the index of the t axis within the {@link #getRanges() list of
     * ranges}.
     */
    public int getTAxisIndex() {
        return this.tAxisIndex;
    }

    public List<Range> getRanges() {
        return this.ranges;
    }

    @Override
    public String toString() {
        Range tRange = this.getRange(this.tAxisIndex);
        Range zRange = this.getRange(this.zAxisIndex);
        Range yRange = this.getRange(this.yAxisIndex);
        Range xRange = this.getRange(this.xAxisIndex);
        return String.format("tRange(%d): %s, zRange(%d): %s, yRange(%d): %s, xRange(%d): %s", this.tAxisIndex, tRange,
                this.zAxisIndex, zRange, this.yAxisIndex, yRange, this.xAxisIndex, xRange);
    }
}
