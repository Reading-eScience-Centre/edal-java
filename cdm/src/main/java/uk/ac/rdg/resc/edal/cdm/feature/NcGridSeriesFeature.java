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
package uk.ac.rdg.resc.edal.cdm.feature;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.cdm.DataReadingStrategy;
import uk.ac.rdg.resc.edal.cdm.FilenameVarIdTimeIndex;
import uk.ac.rdg.resc.edal.cdm.PixelMap;
import uk.ac.rdg.resc.edal.cdm.coverage.NcGridSeriesCoverage;
import uk.ac.rdg.resc.edal.cdm.util.CdmUtils;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.coverage.impl.GridCoverage2DImpl;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.feature.impl.AbstractGridSeriesFeature;
import uk.ac.rdg.resc.edal.position.TimePosition;

/**
 * A {@link GridSeriesFeature} backed by NetCDF files. This can be initialised
 * from a single {@link GridDatatype} (from the UCAR NetCDF libraries), and
 * subsequent grids can be added to create a time aggregated feature
 * 
 * @author Guy Griffiths
 * 
 */
public class NcGridSeriesFeature extends AbstractGridSeriesFeature<Float> {

    private DataReadingStrategy dataReadingStrategy;
    /*
     * A single feature can contain data from multiple files (each with a number
     * of different time values).
     * 
     * We therefore need a map of time positions to the GridDatatype which
     * contains the position, and also the index of each time position within
     * that file.
     */
    private Map<TimePosition, FilenameVarIdTimeIndex> tPosToGridMap = null;
    private boolean noTimeAxis = false;

    /**
     * Instantiate a new {@link NcGridSeriesFeature}
     * 
     * @param grid
     *            the {@link GridDatatype} which contains the data
     * @param hGrid
     *            the {@link HorizontalGrid} which the feature is valid over
     * @param vAxis
     *            the {@link VerticalAxis} which the feature is valid over
     * @param tAxis
     *            the {@link TimeAxis} which the feature is initially valid
     *            over. Extra time points/axes can be merged later
     * @param parentCollection
     *            the {@link FeatureCollection} which holds this {@link Feature}
     * @param dataReadingStrategy
     *            the {@link DataReadingStrategy} which should be used to read
     *            the data
     */
    public NcGridSeriesFeature(String filename, String name, String varId,
            GridSeriesCoverage<Float> coverage,
            FeatureCollection<? extends Feature> parentCollection,
            DataReadingStrategy dataReadingStrategy) {
        super(name, varId, coverage.getDescription(), parentCollection, coverage);
        this.dataReadingStrategy = dataReadingStrategy;

        tPosToGridMap = new HashMap<TimePosition, FilenameVarIdTimeIndex>();
        TimeAxis tAxis = coverage.getDomain().getTimeAxis();
        if (tAxis != null) {
            /*
             * This maps time positions to the grid which contains them, and the
             * index within that grid.
             */
            int tIndex = 0;
            for (TimePosition t : tAxis.getCoordinateValues()) {
                tPosToGridMap.put(t, new FilenameVarIdTimeIndex(filename, varId, tIndex));
                tIndex++;
            }
        } else {
            /*
             * If we have no time axis, we will never merge another GridDatatype
             * into this grid. We use a null key, and set a flag
             */
            noTimeAxis = true;
            tPosToGridMap.put(null, new FilenameVarIdTimeIndex(filename, varId, -1));
        }
    }

    /**
     * Merges a new GridDatatype into the existing feature.
     * 
     * @param newGrid
     *            the {@link GridDatatype} containing the data to be merged
     * @param hGrid
     *            the {@link HorizontalGrid} representing the data in the
     *            {@link GridDatatype}. This must be equivalent to the
     *            {@link HorizontalGrid} in the domain of the feature
     * @param vAxis
     *            the {@link VerticalAxis} representing the data in the
     *            {@link GridDatatype}. This must be equivalent to the
     *            {@link VerticalAxis} in the domain of the feature
     * @param tAxis
     *            the {@link TimeAxis} of the new {@link GridDatatype}
     */
    public void mergeGrid(String filename, String varId, HorizontalGrid hGrid, VerticalAxis vAxis,
            TimeAxis tAxis) {
        if (noTimeAxis == true) {
            throw new UnsupportedOperationException(
                    "The existing feature has no time axis to merge with");
        }
        GridSeriesDomain domain = getCoverage().getDomain();
        if (!domain.getHorizontalGrid().equals(hGrid)
                || (domain.getVerticalAxis() != null && !domain.getVerticalAxis().equals(vAxis))) {
            throw new UnsupportedOperationException(
                    "You cannot merge features with different spatial axes");
        }
        NcGridSeriesCoverage coverage = (NcGridSeriesCoverage) getCoverage();
        /*
         * Merge the coverage.
         */
        coverage.addToCoverage(filename, varId, tAxis);
        /*
         * Add all of the new time values to the map, so that they can be read
         */
        int tIndex = 0;
        for (TimePosition t : tAxis.getCoordinateValues()) {
            tPosToGridMap.put(t, new FilenameVarIdTimeIndex(filename, varId, tIndex));
            tIndex++;
        }
    }

    @Override
    public GridCoverage2D<Float> extractHorizontalGrid(int tindex, int zindex,
            final HorizontalGrid targetDomain) {
        TimePosition tPos = null;
        if (!noTimeAxis) {
            tPos = getCoverage().getDomain().getTimeAxis().getCoordinateValue(tindex);
        }
        HorizontalGrid sourceGrid = getCoverage().getDomain().getHorizontalGrid();
        PixelMap pixelMap = new PixelMap(sourceGrid, targetDomain);

        List<Float> dataList;
        if (pixelMap.isEmpty()) {
            /*
             * There is no overlap between the source data grid and the target
             * domain. Return a list of null values. It's very unlikely that the
             * target domain will be bigger than Integer.MAX_VALUE
             */
            dataList = new AbstractList<Float>() {
                @Override
                public Float get(int index) {
                    if (index < 0 || index >= (int) targetDomain.size())
                        throw new IndexOutOfBoundsException();
                    return null;
                }

                @Override
                public int size() {
                    return (int) targetDomain.size();
                }
            };
        } else {
            float[] data = new float[(int) targetDomain.size()];
            Arrays.fill(data, Float.NaN);
            try {
                /*
                 * Get the grid which refers to this time position. tPos will be
                 * null if we do not have a time axis, in which case this
                 * extracts the only available grid (and the time index in that
                 * grid is exactly equivalent to the time index in the entire
                 * feature)
                 */
                NetcdfDataset nc = CdmUtils.openDataset(tPosToGridMap.get(tPos).getFilename());
                GridDatatype grid = CdmUtils
                        .getGridDatatype(nc, tPosToGridMap.get(tPos).getVarId());
                if (!noTimeAxis) {
                    tindex = tPosToGridMap.get(tPos).getTIndex();
                }
                dataReadingStrategy.readData(tindex, zindex, grid, pixelMap, data);
                CdmUtils.closeDataset(nc);
            } catch (IOException e) {
                // TODO deal with this better
                e.printStackTrace();
            }
            dataList = new ArrayList<Float>();
            for (float d : data) {
                if (Float.isNaN(d)) {
                    dataList.add(null);
                } else {
                    dataList.add(d);
                }
            }
        }
        return new GridCoverage2DImpl<Float>(getCoverage(), targetDomain, dataList);
    }
}
