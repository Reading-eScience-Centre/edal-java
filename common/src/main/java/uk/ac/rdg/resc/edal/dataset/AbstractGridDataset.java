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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.dataset.plugins.VariablePlugin;
import uk.ac.rdg.resc.edal.domain.MapDomain;
import uk.ac.rdg.resc.edal.domain.MapDomainImpl;
import uk.ac.rdg.resc.edal.domain.TrajectoryDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.MismatchedCrsException;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.metadata.GridVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Array4D;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;
import uk.ac.rdg.resc.edal.util.ValuesArray1D;

/**
 * A partial implementation of a {@link GridDataset}, using a
 * {@link GridDataSource} and a {@link DataReadingStrategy}.
 * 
 * @author Jon
 * @author Guy
 */
public abstract class AbstractGridDataset extends AbstractDataset implements GridDataset {
    private static final Logger log = LoggerFactory.getLogger(AbstractGridDataset.class);

    public AbstractGridDataset(String id, Collection<GridVariableMetadata> vars) {
        super(id, vars);
    }

    @Override
    public final MapFeature readMapData(Set<String> varIds, HorizontalGrid targetGrid, Double zPos,
            DateTime time) throws DataReadingException {
        GridDataSource dataSource = null;
        try {
            /*
             * Open the source of data
             */
            dataSource = openGridDataSource();

            Map<String, Array2D<Number>> values = new HashMap<String, Array2D<Number>>();
            Map<String, Parameter> parameters = new HashMap<String, Parameter>();

            /*
             * We need a vertical CRS. This should be the same for all variables
             * in this dataset, so we can set it from any one of them
             */
            VerticalCrs vCrs = null;
            StringBuilder id = new StringBuilder("uk.ac.rdg.resc.edal.feature.");
            id.append(System.currentTimeMillis());
            id.append(":");
            StringBuilder description = new StringBuilder("Map feature from variables:\n");

            /*
             * Keep a list of variable IDs which we need to generate data for
             * from a plugin
             */
            Map<String, VariablePlugin> varsToGenerate = new HashMap<String, VariablePlugin>();

            /*
             * If the user has passed in null for the variable IDs, they want
             * all variables returned
             */
            if (varIds == null) {
                varIds = getVariableIds();
            }

            for (String varId : varIds) {
                if (!getVariableMetadata(varId).isScalar()) {
                    /*
                     * Don't read map data for unplottable variables
                     */
                    continue;
                }

                id.append(varId);
                description.append(varId + "\n");

                /*
                 * We defer plugin-generated variables until after all other
                 * required variables have been read. This way, if any of the
                 * plugin-generated variables require data which we will read
                 * anyway, we don't have to read it twice.
                 */
                VariablePlugin derivingPlugin = isDerivedVariable(varId);
                if (derivingPlugin != null) {
                    /*
                     * Save the variable ID and continue on the outer loop
                     */
                    varsToGenerate.put(varId, derivingPlugin);
                    continue;
                }

                /*
                 * Do the actual data reading
                 */
                Array2D<Number> data = readHorizontalData(varId, targetGrid, zPos, time, dataSource);

                values.put(varId, data);
                /*
                 * We just use the existing parameter data, as it will be the
                 * same.
                 */
                parameters.put(varId, getVariableMetadata(varId).getParameter());
            }

            for (String derivedVarId : varsToGenerate.keySet()) {
                VariablePlugin plugin = varsToGenerate.get(derivedVarId);
                @SuppressWarnings("unchecked")
                Array2D<Number>[] pluginSourceData = new Array2D[plugin.usesVariables().length];
                VariableMetadata[] pluginSourceMetadata = new VariableMetadata[plugin
                        .usesVariables().length];
                /*
                 * Loop through the variable IDs required by this plugin,
                 * getting data and metadata
                 * 
                 * If we have already read the data, add it to the array,
                 * otherwise read the data first.
                 */
                for (int i = 0; i < pluginSourceData.length; i++) {
                    String pluginSourceVarId = plugin.usesVariables()[i];
                    if (values.containsKey(pluginSourceVarId)) {
                        pluginSourceData[i] = values.get(pluginSourceVarId);
                    } else {
                        pluginSourceData[i] = readHorizontalData(pluginSourceVarId, targetGrid,
                                zPos, time, dataSource);
                    }
                    pluginSourceMetadata[i] = getVariableMetadata(pluginSourceVarId);
                }

                values.put(derivedVarId, plugin.generateArray2D(derivedVarId, pluginSourceData));
                parameters.put(derivedVarId, getVariableMetadata(derivedVarId).getParameter());
            }

            /*
             * Release resources held by the DataSource
             */
            dataSource.close();

            /*
             * Construct the GridFeature from the t and z values, the horizontal
             * grid and the VariableMetadata objects
             */
            MapDomain domain = new MapDomainImpl(targetGrid, zPos, vCrs, time);
            if (time != null) {
                description.append("Time: " + time + "\n");
            }
            if (zPos != null) {
                description.append("Elevation: " + zPos);
            }

            MapFeature mapFeature = new MapFeature(UUID.nameUUIDFromBytes(id.toString().getBytes())
                    .toString(), "Extracted Map Feature", description.toString(), domain,
                    parameters, values);

            return mapFeature;
        } catch (IOException e) {
            throw new DataReadingException("Problem reading map feature", e);
        } finally {
            if (dataSource != null) {
                try {
                    dataSource.close();
                } catch (IOException e) {
                    log.error("Problem closing data source");
                }
            }
        }
    }

    private Array2D<Number> readHorizontalData(String varId, HorizontalGrid targetGrid, Double zPos,
            DateTime time, GridDataSource dataSource) throws IOException {
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
        Array2D<Number> data = getDataReadingStrategy().readMapData(dataSource, varId, tIndex, zIndex,
                domainMapper);
        return data;
    }

    private int getTimeIndex(DateTime time, TimeAxis tAxis, String varId) {
        int tIndex = 0;
        if (tAxis != null) {
            if(time == null) {
                time = GISUtils.getClosestToCurrentTime(tAxis.getCoordinateValues());
            }
            tIndex = tAxis.findIndexOf(time);
        }
        if (tIndex < 0) {
            throw new IllegalArgumentException(time
                    + " is not part of the temporal domain for the variable " + varId);
        }
        return tIndex;
    }

    private int getVerticalIndex(Double zPos, VerticalAxis zAxis, String varId) {
        int zIndex = 0;
        if (zAxis != null) {
            if(zPos == null) {
                zPos = GISUtils.getClosestElevationToSurface(zAxis);    
            }
            zIndex = zAxis.findIndexOf(zPos);
        }
        if (zIndex < 0) {
            throw new IllegalArgumentException(zPos
                    + " is not part of the vertical domain for the variable " + varId);
        }
        return zIndex;
    }

    @Override
    public final ProfileFeature readProfileData(Set<String> varIds, HorizontalPosition hPos,
            VerticalAxis zAxis, DateTime time) throws DataReadingException {
        GridDataSource dataSource = null;
        try {
            /*
             * Open the source of data
             */
            dataSource = openGridDataSource();

            Map<String, Array1D<Number>> values = new HashMap<String, Array1D<Number>>();
            Map<String, Parameter> parameters = new HashMap<String, Parameter>();

            StringBuilder id = new StringBuilder("uk.ac.rdg.resc.edal.feature.");
            id.append(System.currentTimeMillis());
            id.append(":");
            StringBuilder description = new StringBuilder("Profile feature from variables:\n");

            /*
             * Keep a list of variable IDs which we need to generate data for
             * from a plugin
             */
            Map<String, VariablePlugin> varsToGenerate = new HashMap<String, VariablePlugin>();

            /*
             * If the user has passed in null for the variable IDs, they want
             * all variables returned
             */
            if (varIds == null) {
                varIds = getVariableIds();
            }

            for (String varId : varIds) {
                if (!getVariableMetadata(varId).isScalar()) {
                    /*
                     * Don't read profile data for unplottable variables
                     */
                    continue;
                }

                id.append(varId);
                description.append(varId + "\n");

                /*
                 * We defer plugin-generated variables until after all other
                 * required variables have been read. This way, if any of the
                 * plugin-generated variables require data which we will read
                 * anyway, we don't have to read it twice.
                 */
                VariablePlugin derivingPlugin = isDerivedVariable(varId);
                if (derivingPlugin != null) {
                    /*
                     * Save the variable ID and continue on the outer loop
                     */
                    varsToGenerate.put(varId, derivingPlugin);
                    continue;
                }

                /*
                 * Do the actual data reading
                 */
                Array1D<Number> data = readVerticalData(varId, zAxis, hPos, time, dataSource);

                values.put(varId, data);
                /*
                 * We just use the existing parameter data, as it will be the
                 * same.
                 */
                parameters.put(varId, getVariableMetadata(varId).getParameter());
            }

            for (String derivedVarId : varsToGenerate.keySet()) {
                VariablePlugin plugin = varsToGenerate.get(derivedVarId);
                @SuppressWarnings("unchecked")
                Array1D<Number>[] pluginSourceData = new Array1D[plugin.usesVariables().length];
                VariableMetadata[] pluginSourceMetadata = new VariableMetadata[plugin
                        .usesVariables().length];
                /*
                 * Loop through the variable IDs required by this plugin,
                 * getting data and metadata
                 * 
                 * If we have already read the data, add it to the array,
                 * otherwise read the data first.
                 */
                for (int i = 0; i < pluginSourceData.length; i++) {
                    String pluginSourceVarId = plugin.usesVariables()[i];
                    if (values.containsKey(pluginSourceVarId)) {
                        pluginSourceData[i] = values.get(pluginSourceVarId);
                    } else {
                        pluginSourceData[i] = readVerticalData(pluginSourceVarId, zAxis, hPos,
                                time, dataSource);
                    }
                    pluginSourceMetadata[i] = getVariableMetadata(pluginSourceVarId);
                }

                values.put(derivedVarId, plugin.generateArray1D(derivedVarId, pluginSourceData));
                parameters.put(derivedVarId, getVariableMetadata(derivedVarId).getParameter());
                /*
                 * TODO This needs testing!
                 */
            }

            /*
             * Release resources held by the DataSource
             */
            dataSource.close();

            /*
             * Construct the GridFeature from the t and z values, the horizontal
             * grid and the VariableMetadata objects
             */
            description.append("Position: " + hPos + "\n");
            if (time != null) {
                description.append("Time: " + time + "\n");
            }

            ProfileFeature profileFeature = new ProfileFeature(UUID.nameUUIDFromBytes(
                    id.toString().getBytes()).toString(), "Extracted Profile Feature",
                    description.toString(), zAxis, hPos, time, parameters, values);

            return profileFeature;
        } catch (IOException e) {
            throw new DataReadingException("Problem reading profile feature", e);
        } finally {
            if (dataSource != null) {
                try {
                    dataSource.close();
                } catch (IOException e) {
                    log.error("Problem closing data source");
                }
            }
        }
    }

    private Array1D<Number> readVerticalData(String varId, VerticalAxis zAxis, HorizontalPosition hPos,
            DateTime time, GridDataSource dataSource) throws IOException {
        /*
         * This conversion is safe, because we are not dealing with derived
         * variables, and all non-derived must have GridVariableMetadata
         * (specified in constructor)
         */
        GridVariableMetadata metadata = (GridVariableMetadata) getVariableMetadata(varId);

        /*
         * Get the domain of the grid
         */
        HorizontalGrid hDomain = metadata.getHorizontalDomain();
        TimeAxis tAxis = metadata.getTemporalDomain();
        VerticalAxis variableZAxis = metadata.getVerticalDomain();

        /*
         * Use these objects to convert natural coordinates to grid indices
         */
        GridCoordinates2D hIndices = hDomain.findIndexOf(hPos);
        int xIndex = hIndices.getX();
        int yIndex = hIndices.getY();

        int tIndex = getTimeIndex(time, tAxis, varId);

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
        int zMin = variableZAxis.findIndexOf(zAxis.getExtent().getLow());
        int zMax = variableZAxis.findIndexOf(zAxis.getExtent().getHigh());

        /*
         * Read the data and move it to a 1D Array
         */
        Array4D<Number> data4d = dataSource.read(varId, tIndex, tIndex, zMin, zMax, yIndex, yIndex, xIndex,
                xIndex);
        int zSize = zAxis.size();
        Array1D<Number> data = new ValuesArray1D(zSize);

        for (int i = 0; i < zSize; i++) {
            Double zVal = zAxis.getCoordinateValue(i);
            int zIndex = variableZAxis.findIndexOf(zVal);
            if (zIndex < 0) {
                throw new IllegalArgumentException("The z-axis for the variable " + varId
                        + " does not contain the position " + zVal + " which was requested.");
            }
            data.set(data4d.get(new int[] { 0, zIndex - zMin, 0, 0 }), new int[] { i });
            /*
             * TODO we need to test this
             */
        }

        return data;
    }

    @Override
    public PointSeriesFeature readTimeSeriesData(Set<String> varIds, HorizontalPosition hPos,
            VerticalPosition zPos, TimeAxis tAxis) throws DataReadingException {
        GridDataSource dataSource = null;
        try {
            /*
             * Open the source of data
             */
            dataSource = openGridDataSource();

            Map<String, Array1D<Number>> values = new HashMap<String, Array1D<Number>>();
            Map<String, Parameter> parameters = new HashMap<String, Parameter>();

            StringBuilder id = new StringBuilder("uk.ac.rdg.resc.edal.feature.");
            id.append(System.currentTimeMillis());
            id.append(":");
            StringBuilder description = new StringBuilder("Profile feature from variables:\n");

            /*
             * Keep a list of variable IDs which we need to generate data for
             * from a plugin
             */
            Map<String, VariablePlugin> varsToGenerate = new HashMap<String, VariablePlugin>();

            /*
             * If the user has passed in null for the variable IDs, they want
             * all variables returned
             */
            if (varIds == null) {
                varIds = getVariableIds();
            }

            for (String varId : varIds) {
                if (!getVariableMetadata(varId).isScalar()) {
                    /*
                     * Don't read profile data for unplottable variables
                     */
                    continue;
                }

                id.append(varId);
                description.append(varId + "\n");

                /*
                 * We defer plugin-generated variables until after all other
                 * required variables have been read. This way, if any of the
                 * plugin-generated variables require data which we will read
                 * anyway, we don't have to read it twice.
                 */
                VariablePlugin derivingPlugin = isDerivedVariable(varId);
                if (derivingPlugin != null) {
                    /*
                     * Save the variable ID and continue on the outer loop
                     */
                    varsToGenerate.put(varId, derivingPlugin);
                    continue;
                }

                /*
                 * Do the actual data reading
                 */
                Array1D<Number> data = readTemporalData(varId, tAxis, hPos, zPos, dataSource);

                values.put(varId, data);
                /*
                 * We just use the existing parameter data, as it will be the
                 * same.
                 */
                parameters.put(varId, getVariableMetadata(varId).getParameter());
            }

            for (String derivedVarId : varsToGenerate.keySet()) {
                VariablePlugin plugin = varsToGenerate.get(derivedVarId);
                @SuppressWarnings("unchecked")
                Array1D<Number>[] pluginSourceData = new Array1D[plugin.usesVariables().length];
                VariableMetadata[] pluginSourceMetadata = new VariableMetadata[plugin
                        .usesVariables().length];
                /*
                 * Loop through the variable IDs required by this plugin,
                 * getting data and metadata
                 * 
                 * If we have already read the data, add it to the array,
                 * otherwise read the data first.
                 */
                for (int i = 0; i < pluginSourceData.length; i++) {
                    String pluginSourceVarId = plugin.usesVariables()[i];
                    if (values.containsKey(pluginSourceVarId)) {
                        pluginSourceData[i] = values.get(pluginSourceVarId);
                    } else {
                        pluginSourceData[i] = readTemporalData(pluginSourceVarId, tAxis, hPos,
                                zPos, dataSource);
                    }
                    pluginSourceMetadata[i] = getVariableMetadata(pluginSourceVarId);
                }

                values.put(derivedVarId, plugin.generateArray1D(derivedVarId, pluginSourceData));
                parameters.put(derivedVarId, getVariableMetadata(derivedVarId).getParameter());
                /*
                 * TODO This needs testing!
                 */
            }

            /*
             * Release resources held by the DataSource
             */
            dataSource.close();

            /*
             * Construct the GridFeature from the t and z values, the horizontal
             * grid and the VariableMetadata objects
             */
            description.append("Position: " + hPos + "\n");
            if (zPos != null) {
                description.append("Elevation: " + zPos + "\n");
            }

            PointSeriesFeature pointSeriesFeature = new PointSeriesFeature(UUID.nameUUIDFromBytes(
                    id.toString().getBytes()).toString(), "Extracted Profile Feature",
                    description.toString(), tAxis, hPos, zPos, parameters, values);

            return pointSeriesFeature;
        } catch (IOException e) {
            throw new DataReadingException("Problem reading profile feature", e);
        } catch (MismatchedCrsException e) {
            throw new DataReadingException("Problem reading profile feature", e);
        } finally {
            if (dataSource != null) {
                try {
                    dataSource.close();
                } catch (IOException e) {
                    log.error("Problem closing data source");
                }
            }
        }
    }

    private Array1D<Number> readTemporalData(String varId, TimeAxis tAxis, HorizontalPosition hPos,
            VerticalPosition zPos, GridDataSource dataSource) throws IOException,
            MismatchedCrsException {
        /*
         * This conversion is safe, because we are not dealing with derived
         * variables, and all non-derived must have GridVariableMetadata
         * (specified in constructor)
         */
        GridVariableMetadata metadata = (GridVariableMetadata) getVariableMetadata(varId);

        /*
         * Get the domain of the grid
         */
        HorizontalGrid hDomain = metadata.getHorizontalDomain();
        VerticalAxis zAxis = metadata.getVerticalDomain();
        TimeAxis variableTAxis = metadata.getTemporalDomain();
        if (zAxis != null && !zAxis.getVerticalCrs().equals(zPos.getCoordinateReferenceSystem())) {
            throw new MismatchedCrsException("Vertical CRSs must match to extract time series");
        }

        /*
         * Use these objects to convert natural coordinates to grid indices
         */
        GridCoordinates2D hIndices = hDomain.findIndexOf(hPos);
        int xIndex = hIndices.getX();
        int yIndex = hIndices.getY();

        int zIndex = getVerticalIndex(zPos.getZ(), zAxis, varId);

        /*
         * Now read the z-limits
         */
        if (variableTAxis == null) {
            throw new IllegalArgumentException("The variable " + varId
                    + " has no time axis, so a time series cannot be read.");
        }
        if (!variableTAxis.getChronology().equals(tAxis.getChronology())) {
            throw new IllegalArgumentException("The chronology of the variable " + varId
                    + " must match that of the domain you are trying to read.");
        }
        int tMin = variableTAxis.findIndexOf(tAxis.getExtent().getLow());
        int tMax = variableTAxis.findIndexOf(tAxis.getExtent().getHigh());

        /*
         * Read the data and move it to a 1D Array
         */
        Array4D<Number> data4d = dataSource.read(varId, tMin, tMax, zIndex, zIndex, yIndex, yIndex, xIndex,
                xIndex);
        int tSize = tAxis.size();
        Array1D<Number> data = new ValuesArray1D(tSize);

        for (int i = 0; i < tSize; i++) {
            DateTime time = tAxis.getCoordinateValue(i);
            int tIndex = variableTAxis.findIndexOf(time);
            if (tIndex < 0) {
                throw new IllegalArgumentException("The time-axis for the variable " + varId
                        + " does not contain the time " + time + " which was requested.");
            }
            data.set(data4d.get(new int[] { tIndex - tMin, 0, 0, 0 }), new int[] { i });
            /*
             * TODO we need to test this
             */
        }

        return data;
    }

    @Override
    public TrajectoryFeature readTrajectoryData(Set<String> varIds, TrajectoryDomain domain)
            throws DataReadingException {
        GridDataSource dataSource = null;
        try {
            /*
             * Open the source of data
             */
            dataSource = openGridDataSource();

            Map<String, Array1D<Number>> values = new HashMap<String, Array1D<Number>>();
            Map<String, Parameter> parameters = new HashMap<String, Parameter>();

            StringBuilder id = new StringBuilder("uk.ac.rdg.resc.edal.feature.");
            id.append(System.currentTimeMillis());
            id.append(":");
            StringBuilder description = new StringBuilder("Trajectory feature from variables:\n");

            /*
             * Keep a list of variable IDs which we need to generate data for
             * from a plugin
             */
            Map<String, VariablePlugin> varsToGenerate = new HashMap<String, VariablePlugin>();

            /*
             * If the user has passed in null for the variable IDs, they want
             * all variables returned
             */
            if (varIds == null) {
                varIds = getVariableIds();
            }

            for (String varId : varIds) {
                if (!getVariableMetadata(varId).isScalar()) {
                    /*
                     * Don't read profile data for unplottable variables
                     */
                    continue;
                }

                id.append(varId);
                description.append(varId + "\n");

                /*
                 * We defer plugin-generated variables until after all other
                 * required variables have been read. This way, if any of the
                 * plugin-generated variables require data which we will read
                 * anyway, we don't have to read it twice.
                 */
                VariablePlugin derivingPlugin = isDerivedVariable(varId);
                if (derivingPlugin != null) {
                    /*
                     * Save the variable ID and continue on the outer loop
                     */
                    varsToGenerate.put(varId, derivingPlugin);
                    continue;
                }

                /*
                 * Do the actual data reading
                 */
                Array1D<Number> data = readMultiplePointData(varId, domain, dataSource);

                values.put(varId, data);
                /*
                 * We just use the existing parameter data, as it will be the
                 * same.
                 */
                parameters.put(varId, getVariableMetadata(varId).getParameter());
            }

            for (String derivedVarId : varsToGenerate.keySet()) {
                VariablePlugin plugin = varsToGenerate.get(derivedVarId);
                @SuppressWarnings("unchecked")
                Array1D<Number>[] pluginSourceData = new Array1D[plugin.usesVariables().length];
                VariableMetadata[] pluginSourceMetadata = new VariableMetadata[plugin
                        .usesVariables().length];
                /*
                 * Loop through the variable IDs required by this plugin,
                 * getting data and metadata
                 * 
                 * If we have already read the data, add it to the array,
                 * otherwise read the data first.
                 */
                for (int i = 0; i < pluginSourceData.length; i++) {
                    String pluginSourceVarId = plugin.usesVariables()[i];
                    if (values.containsKey(pluginSourceVarId)) {
                        pluginSourceData[i] = values.get(pluginSourceVarId);
                    } else {
                        pluginSourceData[i] = readMultiplePointData(pluginSourceVarId, domain,
                                dataSource);
                    }
                    pluginSourceMetadata[i] = getVariableMetadata(pluginSourceVarId);
                }

                values.put(derivedVarId, plugin.generateArray1D(derivedVarId, pluginSourceData));
                parameters.put(derivedVarId, getVariableMetadata(derivedVarId).getParameter());
                /*
                 * TODO This needs testing!
                 */
            }

            /*
             * Release resources held by the DataSource
             */
            dataSource.close();

            /*
             * Construct the TrajectoryFeature from the t and z values, the horizontal
             * grid and the VariableMetadata objects
             */
            TrajectoryFeature trajectoryFeature = new TrajectoryFeature(UUID.nameUUIDFromBytes(
                    id.toString().getBytes()).toString(), "Extracted Trajectory Feature",
                    description.toString(), domain, parameters, values);

            return trajectoryFeature;
        } catch (IOException e) {
            throw new DataReadingException("Problem reading trajectory feature", e);
        } finally {
            if (dataSource != null) {
                try {
                    dataSource.close();
                } catch (IOException e) {
                    log.error("Problem closing data source");
                }
            }
        }
    }

    @Override
    public final Number readSinglePoint(String variableId, HorizontalPosition position,
            Double zVal, DateTime time) throws DataReadingException {
        GridDataSource gridDataSource = null;
        try {
            gridDataSource = openGridDataSource();
            return readPointData(variableId, position, zVal, time, gridDataSource);
        } catch (IOException e) {
            throw new DataReadingException("Problem reading data", e);
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

    private final Array1D<Number> readMultiplePointData(String variableId, TrajectoryDomain domain,
            GridDataSource dataSource) throws DataReadingException {
        Array1D<Number> data = new ValuesArray1D(domain.size());
        Array<GeoPosition> domainObjects = domain.getDomainObjects();
        for (int i = 0; i < domain.size(); i++) {
            GeoPosition position = domainObjects.get(i);
            Double z = null;
            if(position.getVerticalPosition() != null) {
                z = position.getVerticalPosition().getZ();
            }
            Number value = readPointData(variableId, position.getHorizontalPosition(), z,
                    position.getTime(), dataSource);
            data.set(value, i);
        }
        return data;
    }

    private Number readPointData(String variableId, HorizontalPosition position, Double zVal,
            DateTime time, GridDataSource gridDataSource) throws DataReadingException {
        VariablePlugin plugin = isDerivedVariable(variableId);
        if (plugin != null) {
            /*
             * We have a derived variable - read all of the required variables
             * first.
             */
            String[] baseVariables = plugin.usesVariables();
            Number[] baseValues = new Number[baseVariables.length];

            for (int i = 0; i < baseVariables.length; i++) {
                /*
                 * Read all of the required base variables. By recursing this
                 * method, we safely cover the cases where derived variables are
                 * derived from other derived variables
                 */
                baseValues[i] = readSinglePoint(baseVariables[i], position, zVal, time);
            }

            return plugin.getValue(variableId, baseValues);
        } else {
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

                VerticalAxis verticalDomain = variableMetadata.getVerticalDomain();
                int z = getVerticalIndex(zVal, verticalDomain, variableId);

                TimeAxis temporalDomain = variableMetadata.getTemporalDomain();
                int t = getTimeIndex(time, temporalDomain, variableId);

                Array4D<Number> readData = gridDataSource.read(variableId, t, t, z, z, xy.getY(), xy.getY(), xy.getX(),
                        xy.getX());
                return readData.get(0, 0, 0, 0);
            } catch (IOException e) {
                throw new DataReadingException("Problem reading data", e);
            }
        }
    }

    protected abstract GridDataSource openGridDataSource() throws IOException;

    protected abstract DataReadingStrategy getDataReadingStrategy();
}
