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

package uk.ac.rdg.resc.edal.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.domain.DiscreteHorizontalDomain;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.MapDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.VariableNotFoundException;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.HorizontalCell;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.metadata.DiscreteLayeredVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * A partial implementation of an {@link HorizontallyDiscreteDataset} based on
 * a 4D dataset where the z- and t-dimensions are discrete axes. The horizontal
 * dimension is not necessarily separable into 2 axes. This class is a parent
 * class for both {@link GriddedDataset} (a full 4d grid) and
 * {@link HorizontalMesh4dDataset} (a 4d grid where the horizontal layers are
 * unstructured)
 * 
 * @param <DS>
 *            The type of {@link DataSource} which will read the underlying data
 * @param <VM>
 *            The type of {@link DiscreteLayeredVariableMetadata} which will
 *            describe the non-derived variables in this {@link Dataset}
 * 
 * @author Guy Griffiths
 * @author Jon Blower
 */
public abstract class DiscreteLayeredDataset<DS extends DataSource, VM extends DiscreteLayeredVariableMetadata>
        extends HorizontallyDiscreteDataset<DS> {
    public DiscreteLayeredDataset(String id, Collection<VM> vars) {
        super(id, vars);
    }

    @Override
    protected final Array2D<Number> readUnderlyingHorizontalData(String varId,
            MapDomain domain, DS dataSource)
            throws DataReadingException, VariableNotFoundException {
        VM metadata = getNonDerivedVariableMetadata(varId);

        /*
         * Get the z/t domains
         */
        VerticalAxis zAxis = metadata.getVerticalDomain();
        TimeAxis tAxis = metadata.getTemporalDomain();

        /*
         * Use these objects to convert natural coordinates to grid indices
         */
        int tIndex = getTimeIndex(domain.getTime(), tAxis, varId);
        int zIndex = getVerticalIndex(domain.getZ(), zAxis, varId);

        return extractHorizontalData(metadata, tIndex, zIndex, domain, dataSource);
    }

    @Override
    protected final Map<ProfileLocation, Array1D<Number>> readUnderlyingVerticalData(String varId,
            VerticalAxis zAxis, BoundingBox bbox, DateTime targetT, Extent<DateTime> tExtent,
            DS dataSource) throws DataReadingException, VariableNotFoundException {
        VM metadata = getNonDerivedVariableMetadata(varId);

        /*
         * Get the z/t domains
         */
        TimeAxis tAxis = metadata.getTemporalDomain();
        VerticalAxis variableZAxis = metadata.getVerticalDomain();

        /*
         * Find all of the horizontal positions which should be included
         */
        List<HorizontalPosition> horizontalPositions = getHorizontalPositionsToExtract(bbox,
                metadata);

        /*
         * Find all of the times which should be included
         */
        List<DateTime> times = new ArrayList<DateTime>();
        if (tAxis != null) {
            if (tExtent != null) {
                for (DateTime time : tAxis.getCoordinateValues()) {
                    if (tExtent.contains(time)) {
                        times.add(time);
                    }
                }
            } else if (targetT != null) {
                if (tAxis.contains(targetT)) {
                    int tIndex = GISUtils.getIndexOfClosestTimeTo(targetT, tAxis);
                    times.add(tAxis.getCoordinateValue(tIndex));
                }
            } else {
                times = tAxis.getCoordinateValues();
            }
        } else {
            times.add(null);
        }

        /*
         * Now read the data for each unique profile location.
         */
        Map<ProfileLocation, Array1D<Number>> ret = new HashMap<ProfileLocation, Array1D<Number>>();
        for (HorizontalPosition hPos : horizontalPositions) {
            for (DateTime time : times) {
                ProfileLocation location = new ProfileLocation(hPos, time);

                /*
                 * We only want times which exactly match
                 */
                int tIndex = 0;
                if (tAxis != null) {
                    tIndex = tAxis.getCoordinateValues().indexOf(time);
                }
                if (tIndex < 0) {
                    continue;
                }

                /*
                 * Now read the z-limits
                 */
                if (variableZAxis == null) {
                    throw new IllegalArgumentException("The variable " + varId
                            + " has no vertical axis, so a vertical profile cannot be read.");
                }
                if (!variableZAxis.getVerticalCrs().equals(zAxis.getVerticalCrs())) {
                    throw new IllegalArgumentException("The vertical CRS of the variable " + varId
                            + " must match that of the domain you are trying to read.");
                }

                int zSize = zAxis.size();

                List<Integer> zIndices = new ArrayList<>();
                for (int i = 0; i < zSize; i++) {
                    Double zVal = zAxis.getCoordinateValue(i);
                    int zIndex = variableZAxis.findIndexOf(zVal);
                    if (zIndex < 0) {
                        throw new IllegalArgumentException("The z-axis for the variable " + varId
                                + " does not contain the position " + zVal
                                + " which was requested.");
                    }
                    zIndices.add(zIndex);
                }

                Array1D<Number> data = extractProfileData(metadata, zIndices, tIndex, hPos,
                        dataSource);
                ret.put(location, data);
            }
        }

        return ret;
    }

    @Override
    protected final Map<PointSeriesLocation, Array1D<Number>> readUnderlyingTemporalData(
            String varId, TimeAxis tAxis, BoundingBox bbox, Double targetZ, Extent<Double> zExtent,
            DS dataSource) throws DataReadingException, VariableNotFoundException {
        VM metadata = getNonDerivedVariableMetadata(varId);

        /*
         * Get the z/t domains
         */
        VerticalAxis zAxis = metadata.getVerticalDomain();
        TimeAxis variableTAxis = metadata.getTemporalDomain();

        /*
         * Find all of the horizontal positions which should be included
         */
        List<HorizontalPosition> horizontalPositions = getHorizontalPositionsToExtract(bbox,
                metadata);

        /*
         * Find all of the elevations which should be included
         */
        List<Double> zVals = new ArrayList<Double>();
        if (zAxis != null) {
            if (zExtent != null) {
                for (Double zVal : zAxis.getCoordinateValues()) {
                    if (zExtent.contains(zVal)) {
                        zVals.add(zVal);
                    }
                }
            } else if (targetZ != null) {
                if (zAxis.contains(targetZ)) {
                    int zIndex = GISUtils.getIndexOfClosestElevationTo(targetZ, zAxis);
                    zVals.add(zAxis.getCoordinateValue(zIndex));
                }
            } else {
                zVals = zAxis.getCoordinateValues();
            }
        } else {
            zVals.add(null);
        }

        /*
         * Now read the data for each unique time series location.
         */
        Map<PointSeriesLocation, Array1D<Number>> ret = new HashMap<PointSeriesLocation, Array1D<Number>>();
        for (HorizontalPosition hPos : horizontalPositions) {
            for (Double zVal : zVals) {
                VerticalPosition zPos = null;
                if (zVal != null) {
                    zPos = new VerticalPosition(zVal, zAxis.getVerticalCrs());
                }
                PointSeriesLocation location = new PointSeriesLocation(hPos, zPos);

                /*
                 * We only want co-ordinate values which match exactly
                 */
                int zIndex = 0;
                if (zAxis != null) {
                    zIndex = zAxis.getCoordinateValues().indexOf(zVal);
                }
                if (zIndex < 0) {
                    continue;
                }

                /*
                 * Now read the t-limits
                 */
                if (variableTAxis == null) {
                    throw new IllegalArgumentException("The variable " + varId
                            + " has no time axis, so a timeseries cannot be read.");
                }
                if (!variableTAxis.getChronology().equals(tAxis.getChronology())) {
                    throw new IllegalArgumentException("The Chronology of the variable " + varId
                            + " must match that of the domain you are trying to read.");
                }
                int tSize = tAxis.size();

                List<Integer> tIndices = new ArrayList<>();
                for (int i = 0; i < tSize; i++) {
                    DateTime time = tAxis.getCoordinateValue(i);
                    int tIndex = variableTAxis.findIndexOf(time);
                    if (tIndex < 0) {
                        throw new IllegalArgumentException("The time-axis for the variable "
                                + varId + " does not contain the time " + time
                                + " which was requested.");
                    }
                    tIndices.add(tIndex);
                }

                Array1D<Number> data = extractTimeseriesData(metadata, tIndices, zIndex, hPos,
                        dataSource);
                ret.put(location, data);
            }
        }

        return ret;
    }

    @Override
    protected final Number readUnderlyingPointData(String varId, HorizontalPosition hPos,
            Double zVal, DateTime time, DS dataSource) throws DataReadingException,
            VariableNotFoundException {
        VM metadata = getNonDerivedVariableMetadata(varId);

        VerticalAxis verticalDomain = metadata.getVerticalDomain();
        int z = getVerticalIndex(zVal, verticalDomain, varId);

        TimeAxis temporalDomain = metadata.getTemporalDomain();
        int t = getTimeIndex(time, temporalDomain, varId);

        return extractPoint(metadata, t, z, hPos, dataSource);
    }

    /**
     * @param bbox
     *            The {@link BoundingBox} in which to select
     *            {@link HorizontalPosition}s
     * @param metadata
     *            {@link DiscreteLayeredVariableMetadata} object of the variable
     *            in question
     * @return A {@link List} of {@link HorizontalPosition}s
     */
    private List<HorizontalPosition> getHorizontalPositionsToExtract(BoundingBox bbox, VM metadata) {
        DiscreteHorizontalDomain<? extends HorizontalCell> hGrid = metadata
                .getHorizontalDomain();
        /*
         * Find all of the horizontal positions which should be included
         */
        List<HorizontalPosition> horizontalPositions = new ArrayList<HorizontalPosition>();
        if (bbox == null) {
            bbox = hGrid.getBoundingBox();
        }
        if (bbox.getLowerCorner().equals(bbox.getUpperCorner())) {
            /*
             * We have a single position
             */
            horizontalPositions.add(bbox.getLowerCorner());
        } else {
            /*
             * We want all horizontal grid cells which fall within the bounding
             * box
             */
            for (HorizontalCell gridCell : hGrid.getDomainObjects()) {
                if (bbox.contains(gridCell.getCentre())) {
                    horizontalPositions.add(gridCell.getCentre());
                }
            }
        }
        return horizontalPositions;
    }

    /**
     * Returns the {@link VariableMetadata} for a non-derived variable. This
     * guarantees that the cast to VM is permissible.
     * 
     * @param variableId
     *            The variable ID
     * @return The non-derived {@link VariableMetadata} of type VM
     * @throws VariableNotFoundException
     *             If the variable does not exist in this {@link Dataset}, or if
     *             it is a derived variable.
     */
    @SuppressWarnings("unchecked")
    private VM getNonDerivedVariableMetadata(String variableId) throws VariableNotFoundException {
        if (isDerivedVariable(variableId) != null) {
            throw new VariableNotFoundException(variableId + " is a derived variable");
        }
        /*
         * This cast is fine, because constructor of this class here ensures
         * that all non-derived variables are of type VM
         */
        return (VM) super.getVariableMetadata(variableId);
    }

    @Override
    public Class<MapFeature> getMapFeatureType(String variableId) {
        return MapFeature.class;
    }

    /**
     * Extracts data corresponding to a {@link HorizontalGrid}
     * 
     * @param metadata
     *            The {@link DiscreteLayeredVariableMetadata} object
     *            representing the variable to extract data for
     * @param tIndex
     *            The time index at which to extract data
     * @param zIndex
     *            The z index at which to extract data
     * @param targetGrid
     *            The {@link HorizontalGrid} to extract data onto
     * @param dataSource
     *            The {@link DataSource} used to extract data
     * @return An {@link Array2D} containing data which should map onto the
     *         requested {@link HorizontalGrid}
     * @throws DataReadingException
     *             If there is a problem reading the underlying data
     */
    protected abstract Array2D<Number> extractHorizontalData(VM metadata, int tIndex, int zIndex,
            HorizontalGrid targetGrid, DS dataSource) throws DataReadingException;

    /**
     * Extracts data corresponding to a vertical profile
     * 
     * @param metadata
     *            The {@link DiscreteLayeredVariableMetadata} object
     *            representing the variable to extract data for
     * @param zs
     *            A {@link List} of integer z-indices at which to extract data
     * @param tIndex
     *            The time index at which to extract data
     * @param hPos
     *            The {@link HorizontalPosition} at which to extract data
     * @param dataSource
     *            The {@link DataSource} used to extract data
     * @return An {@link Array1D} containing data which should map onto the
     *         requested list of indices
     * @throws DataReadingException
     *             If there is a problem reading the underlying data
     */
    protected abstract Array1D<Number> extractProfileData(VM metadata, List<Integer> zs,
            int tIndex, HorizontalPosition hPos, DS dataSource) throws DataReadingException;

    /**
     * Extracts data corresponding to a time series
     * 
     * @param metadata
     *            The {@link DiscreteLayeredVariableMetadata} object
     *            representing the variable to extract data for
     * @param ts
     *            A {@link List} of integer t-indices at which to extract data
     * @param zIndex
     *            The z-index at which to extract data
     * @param hPos
     *            The {@link HorizontalPosition} at which to extract data
     * @param dataSource
     *            The {@link DataSource} used to extract data
     * @return An {@link Array1D} containing data which should map onto the
     *         requested list of indices
     * @throws DataReadingException
     *             If there is a problem reading the underlying data
     */
    protected abstract Array1D<Number> extractTimeseriesData(VM metadata, List<Integer> ts,
            int zIndex, HorizontalPosition hPos, DS dataSource) throws DataReadingException;

    /**
     * Extract data at a point
     * 
     * @param metadata
     *            The {@link DiscreteLayeredVariableMetadata} object
     *            representing the variable to extract data for
     * @param t
     *            The t-index at which to extract data
     * @param z
     *            The z-index at which to extract data
     * @param hPos
     *            The {@link HorizontalPosition} at which to extract data
     * @param dataSource
     *            The {@link DataSource} used to extract data
     * @return The value of the data at the specified point
     * @throws DataReadingException
     *             If there is a problem reading the underlying data
     */
    protected abstract Number extractPoint(VM metadata, int t, int z, HorizontalPosition hPos,
            DS dataSource) throws DataReadingException;
}
