/*******************************************************************************
 * Copyright (c) 2011 The University of Reading
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
 *******************************************************************************/
package uk.ac.rdg.resc.edal.dataset.cdm;

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
 * @author Guy
 */

final class RangesList {
    private static final Logger log = LoggerFactory.getLogger(RangesList.class);

    private static final Range ZERO_RANGE;

    private final List<Range> ranges;

    /*
     * The indices of these axes in the *physical* data arrays.
     */
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
        ranges = new ArrayList<Range>(rank);
        for (int i = 0; i < rank; i++) {
            ranges.add(ZERO_RANGE);
        }

        /*
         * We need to find the indices of the four axes in the *physical* data
         * arrays. Note that GridDatatype.getXDimensionIndex() and its cousins
         * return the index in *canonical* (tzyx) order and therefore can't be
         * used.
         */
        String xDimName = grid.getXDimension() == null ? null : grid.getXDimension().getFullName();
        String yDimName = grid.getYDimension() == null ? null : grid.getYDimension().getFullName();
        String zDimName = grid.getZDimension() == null ? null : grid.getZDimension().getFullName();
        String tDimName = grid.getTimeDimension() == null ? null : grid.getTimeDimension()
                .getFullName();

        /*
         * The dimensions in this list are in physical order
         */
        List<Dimension> dims = var.getDimensions();
        /*
         * Look through the list, looking for the x,y,z,t dimensions based on
         * name
         */
        for (int i = 0; i < dims.size(); i++) {
            Dimension dim = dims.get(i);
            if (dim.getFullName().equals(xDimName))
                xAxisIndex = i;
            else if (dim.getFullName().equals(yDimName))
                yAxisIndex = i;
            else if (dim.getFullName().equals(zDimName))
                zAxisIndex = i;
            else if (dim.getFullName().equals(tDimName))
                tAxisIndex = i;
        }

        log.debug("Created RangesList: Shape = {}", Arrays.toString(grid.getShape()));
        log.debug("            ....    Rank = {}, x = {}, y = {}, z = {}, t = {}", new Object[] {
                rank, xAxisIndex, yAxisIndex, zAxisIndex, tAxisIndex });
    }

    public void setXRange(int xmin, int xmax) {
        setRange(xAxisIndex, xmin, xmax);
    }

    public void setYRange(int ymin, int ymax) {
        setRange(yAxisIndex, ymin, ymax);
    }

    public void setZRange(int zmin, int zmax) {
        setRange(zAxisIndex, zmin, zmax);
    }

    public void setTRange(int tmin, int tmax) {
        setRange(tAxisIndex, tmin, tmax);
    }

    private void setRange(int index, int min, int max) {
        if (index >= 0 && min >= 0 && max >= 0) {
            try {
                ranges.set(index, new Range(min, max));
            } catch (InvalidRangeException ire) {
                /*
                 * This is a programming error, so is wrapped as a runtime
                 * exception
                 */
                throw new IllegalArgumentException(ire);
            }
        }
    }

    private Range getRange(int index) {
        if (index >= 0) {
            return ranges.get(index);
        } else {
            return null;
        }
    }

    /**
     * Gets the index of the x axis within the {@link #getRanges() list of
     * ranges}.
     */
    public int getXAxisIndex() {
        return xAxisIndex;
    }

    /**
     * Gets the index of the y axis within the {@link #getRanges() list of
     * ranges}.
     */
    public int getYAxisIndex() {
        return yAxisIndex;
    }

    /**
     * Gets the index of the z axis within the {@link #getRanges() list of
     * ranges}.
     */
    public int getZAxisIndex() {
        return zAxisIndex;
    }

    /**
     * Gets the index of the t axis within the {@link #getRanges() list of
     * ranges}.
     */
    public int getTAxisIndex() {
        return tAxisIndex;
    }

    public List<Range> getRanges() {
        return ranges;
    }

    @Override
    public String toString() {
        Range tRange = getRange(tAxisIndex);
        Range zRange = getRange(zAxisIndex);
        Range yRange = getRange(yAxisIndex);
        Range xRange = getRange(xAxisIndex);
        return String.format("tRange(%d): %s, zRange(%d): %s, yRange(%d): %s, xRange(%d): %s",
                tAxisIndex, tRange, zAxisIndex, zRange, yAxisIndex, yRange, xAxisIndex, xRange);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ranges == null) ? 0 : ranges.hashCode());
        result = prime * result + tAxisIndex;
        result = prime * result + xAxisIndex;
        result = prime * result + yAxisIndex;
        result = prime * result + zAxisIndex;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RangesList other = (RangesList) obj;
        if (ranges == null) {
            if (other.ranges != null)
                return false;
        } else if (!ranges.equals(other.ranges))
            return false;
        if (tAxisIndex != other.tAxisIndex)
            return false;
        if (xAxisIndex != other.xAxisIndex)
            return false;
        if (yAxisIndex != other.yAxisIndex)
            return false;
        if (zAxisIndex != other.zAxisIndex)
            return false;
        return true;
    }
}
