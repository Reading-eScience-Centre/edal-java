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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.dataset.plugins.VariablePlugin;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.GridDomain;
import uk.ac.rdg.resc.edal.domain.SimpleGridDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.MismatchedCrsException;
import uk.ac.rdg.resc.edal.exceptions.VariableNotFoundException;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.metadata.GridVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Array4D;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;
import uk.ac.rdg.resc.edal.util.ValuesArray1D;

/**
 * A partial implementation of a {@link Dataset} based on a 4D grid, using a
 * {@link GridDataSource} and a {@link DataReadingStrategy}.
 * 
 * @author Jon
 * @author Guy
 */
public abstract class GriddedDataset extends AbstractPluginEnabledDataset<GridDataSource> {
    private static final Logger log = LoggerFactory.getLogger(GriddedDataset.class);

    public GriddedDataset(String id, Collection<GridVariableMetadata> vars) {
        super(id, vars);
    }

    @Override
    public Class<GridFeature> getFeatureType(String variableId) {
        /*
         * All classes based on this class will have GridFeature as their
         * underlying data type
         */
        return GridFeature.class;
    }

    @Override
    public GridFeature readFeature(String featureId) throws DataReadingException,
            VariableNotFoundException {
        VariableMetadata variableMetadata = getVariableMetadata(featureId);
        if (!(variableMetadata instanceof GridVariableMetadata)) {
            /*
             * We have a variable which does not have a native grid which we can
             * read onto.
             */
            throw new DataReadingException(
                    "The feature "
                            + featureId
                            + " is not gridded.  It is probably a derived variable which is derived from variables with different grids");
        }
        GridVariableMetadata gridVariableMetadata = (GridVariableMetadata) variableMetadata;

        GridDataSource gridDataSource = null;
        try {
            gridDataSource = openDataSource();

            /*
             * Read the actual data. This method will recursively read any data
             * required for derived variables.
             */
            Array4D<Number> data = read4dData(featureId, gridDataSource, gridVariableMetadata);

            /*
             * Create a GridDomain from the GridVariableMetadata
             */
            GridDomain domain = new SimpleGridDomain(gridVariableMetadata.getHorizontalDomain(),
                    gridVariableMetadata.getVerticalDomain(),
                    gridVariableMetadata.getTemporalDomain());

            Map<String, Parameter> parameters = new HashMap<String, Parameter>();
            parameters.put(featureId, getVariableMetadata(featureId).getParameter());

            Map<String, Array4D<Number>> values = new HashMap<String, Array4D<Number>>();
            values.put(featureId, data);

            return new GridFeature(featureId, featureId + " data",
                    "The entire range of data for the variable: " + featureId, domain, parameters,
                    values);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DataReadingException("Problem reading the data from underlying storage", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataReadingException("Problem reading the data from underlying storage", e);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new DataReadingException("Problem reading the data from underlying storage", e);
        } finally {
            if (gridDataSource != null) {
                try {
                    gridDataSource.close();
                } catch (IOException e) {
                    log.error("Problem closing data source");
                }
            }
        }
    }

    /**
     * Reads entire 4D data from a variable.
     * 
     * @param varId
     *            The ID of the variable to read
     * @param gridDataSource
     *            A {@link GridDataSource} which can be used to access the data
     * @param metadata
     *            The {@link GridVariableMetadata} of the variable we are aiming
     *            to read. This will only be used if we are reading a derived
     *            variable, and it is used to check that all required variables
     *            share the same {@link GridDomain}.
     * @return An {@link Array4D} containing the read data.
     * @throws IOException
     *             If there is a problem reading the underlying data
     * @throws DataReadingException
     *             If the source variables' domains do not match the domain for
     *             a derived variable
     * @throws VariableNotFoundException
     *             If the requested variable is not found
     */
    private Array4D<Number> read4dData(final String varId, GridDataSource gridDataSource,
            final GridVariableMetadata metadata) throws IOException, DataReadingException,
            VariableNotFoundException {
        final VariablePlugin plugin = isDerivedVariable(varId);
        if (plugin == null) {
            /*
             * We have a non-derived variable - this means that
             * getVariableMetadata will return GridVariableMetadata
             */
            GridVariableMetadata variableMetadata = (GridVariableMetadata) getVariableMetadata(varId);

            /*
             * Find the grid size and read the data
             */
            int xSize = variableMetadata.getHorizontalDomain().getXSize();
            int ySize = variableMetadata.getHorizontalDomain().getYSize();
            int zSize = 1;
            if (variableMetadata.getVerticalDomain() != null) {
                zSize = variableMetadata.getVerticalDomain().size();
            }
            int tSize = 1;
            if (variableMetadata.getTemporalDomain() != null) {
                tSize = variableMetadata.getTemporalDomain().size();
            }

            return gridDataSource.read(varId, 0, tSize - 1, 0, zSize - 1, 0, ySize - 1, 0,
                    xSize - 1);
        } else {
            String[] requiredVariables = plugin.usesVariables();
            /*
             * Java generics type-erasure warning suppressor.
             */
            @SuppressWarnings("unchecked")
            final Array4D<Number>[] requiredData = new Array4D[requiredVariables.length];
            for (int i = 0; i < requiredVariables.length; i++) {
                VariableMetadata sourceMetadata = getVariableMetadata(requiredVariables[i]);
                if (sourceMetadata instanceof GridVariableMetadata) {
                    /*
                     * Compare domains to metadata
                     */
                } else {
                    throw new DataReadingException("The derived variable " + varId
                            + " has a different domain to one of its source variables: "
                            + requiredVariables[i]
                            + ".  This means that a GridFeature cannot be read.");
                }
                requiredData[i] = read4dData(requiredVariables[i], gridDataSource, metadata);
            }

            int tSize = requiredData[0].getTSize();
            int zSize = requiredData[0].getZSize();
            int ySize = requiredData[0].getYSize();
            int xSize = requiredData[0].getXSize();
            /*
             * Wrap the data in an anonymous Array4D.
             */
            return new Array4D<Number>(tSize, zSize, ySize, xSize) {
                @Override
                public Number get(int... coords) {
                    /*
                     * Use the metadata to get the horizontal position.
                     */
                    int xIndex = coords[3];
                    int yIndex = coords[2];
                    GridCell2D gridCell2D = metadata.getHorizontalDomain().getDomainObjects()
                            .get(yIndex, xIndex);
                    HorizontalPosition pos = gridCell2D == null ? null : gridCell2D.getCentre();

                    /*
                     * Set the source values
                     */
                    Number[] sourceValues = new Number[requiredData.length];
                    for (int i = 0; i < requiredData.length; i++) {
                        sourceValues[i] = requiredData[i].get(coords);
                    }
                    /*
                     * Generate the value
                     */
                    return plugin.getValue(varId, pos, sourceValues);
                }

                @Override
                public void set(Number value, int... coords) {
                    throw new UnsupportedOperationException("This Array4D is immutable");
                }
            };
        }
    }

    /**
     * Reads horizontal data for a non-derived variable
     * 
     * @param varId
     *            The ID of the variable to read
     * @param targetGrid
     *            The {@link HorizontalGrid} on which to read data
     * @param zPos
     *            The z-position to read at
     * @param time
     *            The time to read at
     * @param dataSource
     *            The {@link GridDataSource} to read data from
     * @return
     * @throws IOException
     *             If there is a problem opening the {@link GridDataSource}
     * @throws DataReadingException
     *             If there is a problem reading the data
     * @throws VariableNotFoundException
     */
    @Override
    protected Array2D<Number> readUnderlyingHorizontalData(String varId, HorizontalGrid targetGrid,
            Double zPos, DateTime time, GridDataSource dataSource) throws IOException,
            DataReadingException, VariableNotFoundException {
        /*
         * This cast will always work, because we only ever call this method for
         * non-derived variables - i.e. those whose metadata was provided in the
         * constructor (which constrains metadata to be GridVariableMetadata
         */
        GridVariableMetadata metadata = (GridVariableMetadata) getVariableMetadata(varId);

        /*
         * Get the domain of the grid
         */
        HorizontalGrid sourceGrid = metadata.getHorizontalDomain();
        VerticalAxis zAxis = metadata.getVerticalDomain();
        TimeAxis tAxis = metadata.getTemporalDomain();

        /*
         * All variables within this dataset should share the same vertical CRS
         * (even if they don't share the same values)
         */

        /*
         * Use these objects to convert natural coordinates to grid indices
         */
        int tIndex = getTimeIndex(time, tAxis, varId);
        int zIndex = getVerticalIndex(zPos, zAxis, varId);

        /*
         * Create a DomainMapper from the source and target grids
         */
        Domain2DMapper domainMapper = Domain2DMapper.forGrid(sourceGrid, targetGrid);

        /*
         * Now use the appropriate DataReadingStrategy to read data
         */
        Array2D<Number> data = getDataReadingStrategy().readMapData(dataSource, varId, tIndex,
                zIndex, domainMapper);
        return data;
    }

    /**
     * Reads profile data for a given non-derived variable
     * 
     * @param metadata
     *            The {@link GridVariableMetadata} representing the variable
     * @param zAxis
     *            The desired vertical axis of the data
     * @param bbox
     *            The {@link BoundingBox} within which to read profiles
     * @param targetT
     *            The target time at which to read profiles
     * @param tExtent
     *            The time {@link Extent} within which to read profiles
     * @param dataSource
     *            The {@link GridDataSource} to read from
     * @return A {@link Map} of unique profile locations to data for each
     * @throws IOException
     *             If there was a problem reading data from the
     *             {@link GridDataSource}
     */
    @Override
    protected Map<ProfileLocation, Array1D<Number>> readUnderlyingVerticalData(
            GridVariableMetadata metadata, VerticalAxis zAxis, BoundingBox bbox, DateTime targetT,
            Extent<DateTime> tExtent, GridDataSource dataSource) throws IOException,
            DataReadingException {
        String varId = metadata.getId();

        /*
         * Get the domain of the grid
         */
        HorizontalGrid hDomain = metadata.getHorizontalDomain();
        TimeAxis tAxis = metadata.getTemporalDomain();
        VerticalAxis variableZAxis = metadata.getVerticalDomain();

        /*
         * Find all of the horizontal positions which should be included
         */
        List<HorizontalPosition> horizontalPositions = new ArrayList<HorizontalPosition>();
        if (bbox == null) {
            bbox = hDomain.getBoundingBox();
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
            for (GridCell2D gridCell : hDomain.getDomainObjects()) {
                if (bbox.contains(gridCell.getCentre())) {
                    horizontalPositions.add(gridCell.getCentre());
                }
            }
        }

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

                GridCoordinates2D hIndices = hDomain.findIndexOf(hPos);

                int xIndex = hIndices.getX();
                int yIndex = hIndices.getY();

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
                int zMin;
                int zMax;
                if (zAxis.isAscending()) {
                    zMin = variableZAxis.findIndexOf(zAxis.getExtent().getLow());
                    zMax = variableZAxis.findIndexOf(zAxis.getExtent().getHigh());
                } else {
                    zMin = variableZAxis.findIndexOf(zAxis.getExtent().getHigh());
                    zMax = variableZAxis.findIndexOf(zAxis.getExtent().getLow());
                }

                /*
                 * Read the data and move it to a 1D Array
                 */
                Array4D<Number> data4d = dataSource.read(varId, tIndex, tIndex, zMin, zMax, yIndex,
                        yIndex, xIndex, xIndex);
                int zSize = zAxis.size();
                Array1D<Number> data = new ValuesArray1D(zSize);

                for (int i = 0; i < zSize; i++) {
                    Double zVal = zAxis.getCoordinateValue(i);
                    int zIndex = variableZAxis.findIndexOf(zVal);
                    if (zIndex < 0) {
                        throw new IllegalArgumentException("The z-axis for the variable " + varId
                                + " does not contain the position " + zVal
                                + " which was requested.");
                    }
                    data.set(data4d.get(new int[] { 0, zIndex - zMin, 0, 0 }), new int[] { i });
                }

                ret.put(location, data);

            }
        }

        return ret;
    }

    /**
     * Reads timeseries data for a given non-derived variable
     * 
     * @param metadata
     *            The {@link GridVariableMetadata} representing the variable
     * @param tAxis
     *            The desired time axis of the data
     * @param bbox
     *            The {@link BoundingBox} within which to read timeseries
     * @param targetZ
     *            The target depth to read timeseries at
     * @param zExtent
     *            The vertical {@link Extent} within which to read timeseries
     * @param dataSource
     *            The {@link GridDataSource} to read from
     * @return A {@link Map} of unique profile locations to data for each
     * @throws IOException
     *             If there was a problem reading data from the
     *             {@link GridDataSource}
     */
    @Override
    protected Map<PointSeriesLocation, Array1D<Number>> readUnderlyingTemporalData(
            GridVariableMetadata metadata, TimeAxis tAxis, BoundingBox bbox, Double targetZ,
            Extent<Double> zExtent, GridDataSource dataSource) throws IOException,
            MismatchedCrsException, DataReadingException {
        String varId = metadata.getId();

        /*
         * Get the domain of the grid
         */
        HorizontalGrid hDomain = metadata.getHorizontalDomain();
        VerticalAxis zAxis = metadata.getVerticalDomain();
        TimeAxis variableTAxis = metadata.getTemporalDomain();

        /*
         * Find all of the horizontal positions which should be included
         */
        List<HorizontalPosition> horizontalPositions = new ArrayList<HorizontalPosition>();
        if (bbox == null) {
            bbox = hDomain.getBoundingBox();
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
            for (GridCell2D gridCell : hDomain.getDomainObjects()) {
                if (bbox.contains(gridCell.getCentre())) {
                    horizontalPositions.add(gridCell.getCentre());
                }
            }
        }

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
         * Now read the data for each unique profile location.
         */
        Map<PointSeriesLocation, Array1D<Number>> ret = new HashMap<PointSeriesLocation, Array1D<Number>>();
        for (HorizontalPosition hPos : horizontalPositions) {
            for (Double zVal : zVals) {
                VerticalPosition zPos = null;
                if (zVal != null) {
                    zPos = new VerticalPosition(zVal, zAxis.getVerticalCrs());
                }
                PointSeriesLocation location = new PointSeriesLocation(hPos, zPos);

                GridCoordinates2D hIndices = hDomain.findIndexOf(hPos);

                int xIndex = hIndices.getX();
                int yIndex = hIndices.getY();

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
                int tMin = variableTAxis.findIndexOf(tAxis.getExtent().getLow());
                int tMax = variableTAxis.findIndexOf(tAxis.getExtent().getHigh());

                /*
                 * Read the data and move it to a 1D Array
                 */
                Array4D<Number> data4d = dataSource.read(varId, tMin, tMax, zIndex, zIndex, yIndex,
                        yIndex, xIndex, xIndex);
                int tSize = tAxis.size();
                Array1D<Number> data = new ValuesArray1D(tSize);

                for (int i = 0; i < tSize; i++) {
                    DateTime time = tAxis.getCoordinateValue(i);
                    int tIndex = variableTAxis.findIndexOf(time);
                    if (tIndex < 0) {
                        throw new IllegalArgumentException("The time-axis for the variable "
                                + varId + " does not contain the time " + time
                                + " which was requested.");
                    }
                    data.set(data4d.get(new int[] { tIndex - tMin, 0, 0, 0 }), new int[] { i });
                }

                ret.put(location, data);

            }
        }

        return ret;
    }

    @Override
    protected Number readUnderlyingPointData(String variableId, HorizontalPosition position,
            Double zVal, DateTime time, GridDataSource gridDataSource) throws DataReadingException,
            VariableNotFoundException {
        try {
            /*
             * We have a non-derived variable
             */
            /*
             * This cast is OK, since this is only called for non-derived
             * variables
             */
            GridVariableMetadata variableMetadata = (GridVariableMetadata) getVariableMetadata(variableId);
            GridCoordinates2D xy = variableMetadata.getHorizontalDomain().findIndexOf(position);
            if (xy == null) {
                return null;
            }

            VerticalAxis verticalDomain = variableMetadata.getVerticalDomain();
            int z = getVerticalIndex(zVal, verticalDomain, variableId);

            TimeAxis temporalDomain = variableMetadata.getTemporalDomain();
            int t = getTimeIndex(time, temporalDomain, variableId);

            Array4D<Number> readData = gridDataSource.read(variableId, t, t, z, z, xy.getY(),
                    xy.getY(), xy.getX(), xy.getX());
            return readData.get(0, 0, 0, 0);
        } catch (IOException e) {
            throw new DataReadingException("Problem reading data", e);
        }
    }

    @Override
    public Class<MapFeature> getMapFeatureType(String variableId) {
        return MapFeature.class;
    }
}
