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
import uk.ac.rdg.resc.edal.cdm.coverage.NcVectorGridSeriesCoverage;
import uk.ac.rdg.resc.edal.cdm.util.CdmUtils;
import uk.ac.rdg.resc.edal.coverage.Coverage;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.coverage.impl.GridCoverage2DImpl;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.impl.AbstractGridSeriesFeature;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.Vector2D;
import uk.ac.rdg.resc.edal.position.impl.Vector2DFloat;

/**
 * A {@link GridSeriesFeature} containing Vector data backed by NetCDF files.
 * This can be initialised from a pair of {@link GridDatatype} objects (from the
 * UCAR NetCDF libraries), and subsequent pairs of grids can be added to create
 * a time aggregated feature
 * 
 * @author Guy Griffiths
 * 
 */
public class NcVectorGridSeriesFeature extends AbstractGridSeriesFeature<Vector2D<Float>> {

    private DataReadingStrategy dataReadingStrategy;
    private Map<TimePosition, FileDataPair> tPosToGridMap = null;
    private boolean noTimeAxis = false;

    /**
     * Initialise the feature
     * 
     * @param name
     *            the name of the feature
     * @param id
     *            the id of the feature
     * @param description
     *            the description of the feature
     * @param parentCollection
     *            the parent {@link FeatureCollection}
     * @param coverage
     *            the {@link Coverage} for the feature
     * @param dataReadingStrategy
     *            the method used to read data
     * @param xData
     *            the {@link GridDatatype} containing the x-components of the
     *            data
     * @param yGrid
     *            the {@link GridDatatype} containing the y-components of the
     *            data
     */
    public NcVectorGridSeriesFeature
//    (String name, String id, String description,
//            FeatureCollection<? extends Feature> parentCollection,
//            GridSeriesCoverage<Vector2D<Float>> coverage, DataReadingStrategy dataReadingStrategy,
//            GridDatatype xGrid, GridDatatype yGrid)
    (String xFilename, String xVarId, String yFilename, String yVarId, String name, String description,
            GridSeriesCoverage<Vector2D<Float>> coverage,
            FeatureCollection<? extends Feature> parentCollection,
            DataReadingStrategy dataReadingStrategy){
        super(name, xVarId, description, parentCollection, coverage);
        this.dataReadingStrategy = dataReadingStrategy;

        TimeAxis tAxis = coverage.getDomain().getTimeAxis();
        tPosToGridMap = new HashMap<TimePosition, FileDataPair>();
        if (tAxis != null) {
            /*
             * This maps time positions to the grid pair which contains them,
             * and the index within that grid pair.
             */
            int tindex = 0;
            for (TimePosition t : tAxis.getCoordinateValues()) {
                FileDataPair filePair = new FileDataPair(new FilenameVarIdTimeIndex(xFilename,
                        xVarId, tindex), new FilenameVarIdTimeIndex(yFilename, yVarId, tindex));
                tPosToGridMap.put(t, filePair);
                tindex++;
            }
        } else {
            /*
             * If we have no time axis, we will never merge another GridDatatype
             * pair into this grid. We use a null key, and set a flag
             */
            tPosToGridMap.put(null, new FileDataPair(new FilenameVarIdTimeIndex(xFilename, xVarId,
                    -1), new FilenameVarIdTimeIndex(yFilename, yVarId, -1)));
            noTimeAxis = true;
        }
    }

    /**
     * Merges new data into the feature
     * 
     * @param gridX
     *            the {@link GridDatatype} of the x-components of the data
     * @param gridY
     *            the {@link GridDatatype} of the y-components of the data
     * @param hGrid
     *            the {@link HorizontalGrid} representing the data in the
     *            {@link GridDatatype}s. This must be equivalent to the
     *            {@link HorizontalGrid} in the domain of the feature
     * @param vAxis
     *            the {@link VerticalAxis} representing the data in the
     *            {@link GridDatatype}s. This must be equivalent to the
     *            {@link VerticalAxis} in the domain of the feature
     * @param tAxis
     *            the {@link TimeAxis} of the new {@link GridDatatype}s
     */
    public void mergeGrids(String xFilename, String xVarId, String yFilename, String yVarId,
            HorizontalGrid hGrid, VerticalAxis vAxis, TimeAxis tAxis) {
        if (noTimeAxis) {
            throw new UnsupportedOperationException(
                    "The existing feature has no time axis to merge with");
        }
        GridSeriesDomain domain = getCoverage().getDomain();
        if (!domain.getHorizontalGrid().equals(hGrid)
                || (domain.getVerticalAxis() != null && !domain.getVerticalAxis().equals(vAxis))) {
            throw new UnsupportedOperationException(
                    "You cannot merge features with different spatial axes");
        }
        /*
         * Merge the coverage
         */
        NcVectorGridSeriesCoverage coverage = (NcVectorGridSeriesCoverage) getCoverage();
        coverage.addToCoverage(xFilename, xVarId, yFilename, yVarId, tAxis);
        /*
         * Add all of the new time values to the map
         */
        int tindex = 0;
        for (TimePosition t : tAxis.getCoordinateValues()) {
            FileDataPair gridPair = new FileDataPair(new FilenameVarIdTimeIndex(xFilename, xVarId,
                    tindex), new FilenameVarIdTimeIndex(yFilename, yVarId, tindex));
            tPosToGridMap.put(t, gridPair);
            tindex++;
        }
    }

    @Override
    public GridCoverage2D<Vector2D<Float>> extractHorizontalGrid(int tindex, int zindex,
            final HorizontalGrid targetDomain) {
        TimePosition tPos = null;
        if (!noTimeAxis) {
            tPos = getCoverage().getDomain().getTimeAxis().getCoordinateValue(tindex);
        }
        HorizontalGrid sourceGrid = getCoverage().getDomain().getHorizontalGrid();
        PixelMap pixelMap = new PixelMap(sourceGrid, targetDomain);

        List<Vector2D<Float>> dataList;
        if (pixelMap.isEmpty()) {
            /*
             * There is no overlap between the source data grid and the target
             * domain. Return a list of null values. It's very unlikely that the
             * target domain will be bigger than Integer.MAX_VALUE
             */
            dataList = new AbstractList<Vector2D<Float>>() {
                @Override
                public Vector2D<Float> get(int index) {
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
            float[] xData = new float[(int) targetDomain.size()];
            float[] yData = new float[(int) targetDomain.size()];
            Arrays.fill(xData, Float.NaN);
            Arrays.fill(yData, Float.NaN);
            try {
                if (!noTimeAxis) {
                    /*
                     * Both x and y should have the same time axes, and so we can pick either tindex
                     */
                    tindex = tPosToGridMap.get(tPos).getXData().getTIndex();
                }
                NetcdfDataset xNc = CdmUtils.openDataset(tPosToGridMap.get(tPos).getXData().getFilename());
                GridDatatype xGrid = CdmUtils
                        .getGridDatatype(xNc, tPosToGridMap.get(tPos).getXData().getVarId());
                NetcdfDataset yNc = CdmUtils.openDataset(tPosToGridMap.get(tPos).getYData().getFilename());
                GridDatatype yGrid = CdmUtils
                        .getGridDatatype(yNc, tPosToGridMap.get(tPos).getYData().getVarId());
                dataReadingStrategy.readData(tindex, zindex, xGrid, pixelMap, xData);
                dataReadingStrategy.readData(tindex, zindex, yGrid, pixelMap, yData);
                CdmUtils.closeDataset(xNc);
                CdmUtils.closeDataset(yNc);
            } catch (IOException e) {
                // TODO deal with this better
                e.printStackTrace();
            }
            dataList = new ArrayList<Vector2D<Float>>();
            for (int i = 0; i < xData.length; i++) {
                if (Float.isNaN(xData[i]) || Float.isNaN(yData[i])) {
                    dataList.add(null);
                } else {
                    dataList.add(new Vector2DFloat(xData[i], yData[i]));
                }
            }
        }
        return new GridCoverage2DImpl<Vector2D<Float>>(getCoverage(), targetDomain, dataList);
    }

    /*
     * A simple class to hold pairs of GridDatatypes and the time index within
     * them of the desired value
     */
    private class FileDataPair {
        private final FilenameVarIdTimeIndex xData;
        private final FilenameVarIdTimeIndex yData;

        public FileDataPair(FilenameVarIdTimeIndex xData, FilenameVarIdTimeIndex yData) {
            super();
            this.xData = xData;
            this.yData = yData;
        }

        public FilenameVarIdTimeIndex getXData() {
            return xData;
        }

        public FilenameVarIdTimeIndex getYData() {
            return yData;
        }
    }
}
