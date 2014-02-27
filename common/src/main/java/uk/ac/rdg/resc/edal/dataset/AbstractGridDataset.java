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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.dataset.plugins.VariablePlugin;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.GridDomain;
import uk.ac.rdg.resc.edal.domain.MapDomain;
import uk.ac.rdg.resc.edal.domain.MapDomainImpl;
import uk.ac.rdg.resc.edal.domain.SimpleGridDomain;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.domain.TrajectoryDomain;
import uk.ac.rdg.resc.edal.domain.VerticalDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.IncorrectDomainException;
import uk.ac.rdg.resc.edal.exceptions.MismatchedCrsException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxisImpl;
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
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;
import uk.ac.rdg.resc.edal.util.PlottingDomainParams;
import uk.ac.rdg.resc.edal.util.ValuesArray1D;

/**
 * A partial implementation of a {@link GridDataset}, using a
 * {@link GridDataSource} and a {@link DataReadingStrategy}.
 * 
 * @author Jon
 * @author Guy
 */
public abstract class AbstractGridDataset extends AbstractDataset {
    private static final Logger log = LoggerFactory.getLogger(AbstractGridDataset.class);

    public AbstractGridDataset(String id, Collection<GridVariableMetadata> vars) {
        super(id, vars);
    }

    @Override
    public GridFeature readFeature(String featureId) throws DataReadingException {
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

        try {
            GridDataSource gridDataSource = openGridDataSource();

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
            throw new DataReadingException("Problem reading the data from underlying storage");
        }
    }

    @Override
    public Set<String> getFeatureIds() {
        /*
         * For a GridDataset, there is one feature per variable
         */
        return getVariableIds();
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
     */
    private Array4D<Number> read4dData(final String varId, GridDataSource gridDataSource,
            final GridVariableMetadata metadata) throws IOException, DataReadingException {
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
            int zSize = variableMetadata.getVerticalDomain().size();
            int tSize = variableMetadata.getTemporalDomain().size();

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

    @Override
    public final Collection<? extends DiscreteFeature<?, ?>> extractMapFeatures(Set<String> varIds,
            PlottingDomainParams params) throws DataReadingException {
        /*
         * If the user has passed in null for the variable IDs, they want all
         * variables returned
         */
        if (varIds == null) {
            varIds = getVariableIds();
        }
        /*
         * Create a list so that we can add to it whilst looping over the
         * elements (to add required child members)
         */
        List<String> variableIds = new ArrayList<String>(varIds);

        final HorizontalGrid targetGrid = params.getImageGrid();
        Double zPos = params.getTargetZ();

        DateTime time = params.getTargetT();

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
            StringBuilder name = new StringBuilder("Map of ");
            StringBuilder description = new StringBuilder("Map feature from variables:\n");

            /*
             * Keep a list of variable IDs which we need to generate data for
             * from a plugin
             */
            Map<String, VariablePlugin> varsToGenerate = new HashMap<String, VariablePlugin>();

            for (int i = 0; i < variableIds.size(); i++) {
                String varId = variableIds.get(i);
                if (!getVariableMetadata(varId).isScalar()) {
                    /*
                     * Don't read map data for unplottable variables
                     */
                    Set<VariableMetadata> children = getVariableMetadata(varId).getChildren();
                    for (VariableMetadata childMetadata : children) {
                        if (!variableIds.contains(childMetadata.getId())) {
                            variableIds.add(childMetadata.getId());
                        }
                    }
                    continue;
                }

                id.append(varId);
                name.append(varId + ", ");
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

                values.put(derivedVarId, plugin.generateArray2D(
                        derivedVarId,
                        new Array2D<HorizontalPosition>(targetGrid.getYSize(), targetGrid
                                .getXSize()) {
                            @Override
                            public HorizontalPosition get(int... coords) {
                                return targetGrid.getDomainObjects().get(coords).getCentre();
                            }

                            @Override
                            public void set(HorizontalPosition value, int... coords) {
                                throw new UnsupportedOperationException("This array is immutable");
                            }
                        }, pluginSourceData));
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
            name.delete(name.length() - 2, name.length() - 1);
            if (time != null) {
                description.append("Time: " + time + "\n");
            }
            if (zPos != null) {
                description.append("Elevation: " + zPos);
            }

            MapFeature mapFeature = new MapFeature(UUID.nameUUIDFromBytes(id.toString().getBytes())
                    .toString(), name.toString(), description.toString(), domain, parameters,
                    values);

            return CollectionUtils.setOf(mapFeature);
        } catch (IOException e) {
            log.error("Problem reading data", e);
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

    private Array2D<Number> readHorizontalData(String varId, HorizontalGrid targetGrid,
            Double zPos, DateTime time, GridDataSource dataSource) throws IOException {
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

    private static int getTimeIndex(DateTime time, TimeAxis tAxis, String varId) {
        int tIndex = 0;
        if (tAxis != null) {
            if (time == null) {
                time = GISUtils.getClosestToCurrentTime(tAxis);
            }
            tIndex = tAxis.findIndexOf(time);
        }
        if (tIndex < 0) {
            throw new IllegalArgumentException(time
                    + " is not part of the temporal domain for the variable " + varId);
        }
        return tIndex;
    }

    private static int getVerticalIndex(Double zPos, VerticalAxis zAxis, String varId) {
        int zIndex = 0;
        if (zAxis != null) {
            if (zPos == null) {
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

    private class ProfileLocation {
        HorizontalPosition hPos;
        DateTime time;

        public ProfileLocation(HorizontalPosition hPos, DateTime time) {
            this.hPos = hPos;
            this.time = time;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((hPos == null) ? 0 : hPos.hashCode());
            result = prime * result + ((time == null) ? 0 : time.hashCode());
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
            ProfileLocation other = (ProfileLocation) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (hPos == null) {
                if (other.hPos != null)
                    return false;
            } else if (!hPos.equals(other.hPos))
                return false;
            if (time == null) {
                if (other.time != null)
                    return false;
            } else if (!time.equals(other.time))
                return false;
            return true;
        }

        private AbstractGridDataset getOuterType() {
            return AbstractGridDataset.this;
        }
    };

    @Override
    public Collection<? extends ProfileFeature> extractProfileFeatures(Set<String> varIds,
            PlottingDomainParams params) throws DataReadingException {
        List<ProfileFeature> features = new ArrayList<>();
        /*
         * If the user has passed in null for the variable IDs, they want all
         * variables returned
         */
        if (varIds == null) {
            varIds = getVariableIds();
        }

        /*
         * Find a common z-axis
         */
        Extent<Double> zExtent = params.getZExtent();
        VerticalAxis zAxis;
        try {
            zAxis = getVerticalAxis(varIds);
        } catch (IncorrectDomainException e) {
            log.error("Cannot extract profiles for variables without vertical information", e);
            throw new DataReadingException("Problem finding common z-axis", e);
        }
        zAxis = limitZAxis(zAxis, zExtent);
        if (zAxis == null) {
            /*
             * No z-axis within given limits - return empty collection
             */
            return features;
        }

        /*
         * Find a bounding box to extract all profiles from
         */
        BoundingBox bbox;
        HorizontalPosition pos = params.getTargetHorizontalPosition();
        if (pos != null) {
            bbox = new BoundingBoxImpl(pos.getX(), pos.getY(), pos.getX(), pos.getY(),
                    pos.getCoordinateReferenceSystem());
        } else {
            bbox = params.getBbox();
        }

        /*
         * Find a time range to extract all profiles from
         */
        Extent<DateTime> timeExtent;
        if (params.getTargetT() != null) {
            timeExtent = Extents.newExtent(params.getTargetT(), params.getTargetT());
        } else {
            timeExtent = params.getTExtent();
        }

        GridDataSource dataSource = null;
        /*
         * Open the source of data
         */
        try {
            dataSource = openGridDataSource();
        } catch (IOException e) {
            throw new DataReadingException("Problem reading profile feature", e);
        }

        Map<String, Parameter> parameters = new HashMap<String, Parameter>();

        StringBuilder id = new StringBuilder("uk.ac.rdg.resc.edal.feature.");
        id.append(System.currentTimeMillis());
        id.append(":");
        StringBuilder description = new StringBuilder("Profile feature from variables:\n");

        /*
         * Keep a list of variable IDs which we need to generate data for from a
         * plugin
         */
        Map<String, VariablePlugin> varsToGenerate = new HashMap<String, VariablePlugin>();

        /*
         * Store a map of unique profile locations to a variable IDs/values
         */
        Map<String, Map<ProfileLocation, Array1D<Number>>> allVariablesData = new HashMap<>();

        /*
         * Read all of the data from non-plugin variables. This loops over all
         * variables, and then ignores those which are plugin-derived.
         */
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
             * We now know that this is not a derived variable, and hence will
             * have GridVariableMetadata
             */
            GridVariableMetadata gridVariableMetadata = (GridVariableMetadata) getVariableMetadata(varId);

            /*
             * Do the actual data reading
             */
            Map<ProfileLocation, Array1D<Number>> data;
            try {
                data = readVerticalData(gridVariableMetadata, zAxis, bbox, timeExtent, dataSource);
            } catch (IOException e) {
                throw new DataReadingException("Problem reading profile feature", e);
            }
            allVariablesData.put(varId, data);

            /*
             * We just use the existing parameter data, as it will be the same.
             */
            parameters.put(varId, getVariableMetadata(varId).getParameter());
        }

        /*
         * Now read the plugin-derived variables
         */
        for (String derivedVarId : varsToGenerate.keySet()) {
            VariablePlugin plugin = varsToGenerate.get(derivedVarId);
            List<Map<ProfileLocation, Array1D<Number>>> pluginSourceData = new ArrayList<>();
            int nSourceVars = plugin.usesVariables().length;
            VariableMetadata[] pluginSourceMetadata = new VariableMetadata[plugin.usesVariables().length];
            /*
             * Loop through the variable IDs required by this plugin, getting
             * data and metadata
             * 
             * If we have already read the data, add it to the array, otherwise
             * read the data first.
             */
            for (int i = 0; i < nSourceVars; i++) {
                String pluginSourceVarId = plugin.usesVariables()[i];
                if (allVariablesData.containsKey(pluginSourceVarId)) {
                    pluginSourceData.add(allVariablesData.get(pluginSourceVarId));
                } else {
                    /*
                     * This cast should be OK, but there may be issues if we
                     * have multiple nesting of plugins
                     * 
                     * TODO Either fix is or disallow multiple-plugin nesting
                     */
                    GridVariableMetadata variableMetadata = (GridVariableMetadata) getVariableMetadata(pluginSourceVarId);
                    try {
                        pluginSourceData.add(readVerticalData(variableMetadata, zAxis, bbox,
                                timeExtent, dataSource));
                    } catch (IOException e) {
                        log.error("Problem reading data", e);
                        throw new DataReadingException(
                                "Problem reading data to generate a plugin variable", e);
                    }
                }
                pluginSourceMetadata[i] = getVariableMetadata(pluginSourceVarId);
            }

            /*
             * Check that all plugin source variables share the same profile
             * locations
             */
            Set<ProfileLocation> profileLocations = new HashSet<>();
            for (int i = 0; i < nSourceVars; i++) {
                profileLocations = pluginSourceData.get(i).keySet();
                for (int j = i; j < nSourceVars; j++) {
                    Set<ProfileLocation> otherSourceValueLocations = pluginSourceData.get(j)
                            .keySet();
                    if (!profileLocations.equals(otherSourceValueLocations)) {
                        throw new DataReadingException(
                                "Cannot extract plugin-derived profiles for variables which have different spatio-temporal locations");
                    }
                }
            }

            Map<ProfileLocation, Array1D<Number>> locationsToValues = new HashMap<>();
            for (final ProfileLocation location : profileLocations) {
                @SuppressWarnings("unchecked")
                Array1D<Number>[] pluginSource = new Array1D[nSourceVars];
                for (int i = 0; i < nSourceVars; i++) {
                    pluginSource[i] = pluginSourceData.get(i).get(location);
                }
                Array1D<Number> values = plugin.generateArray1D(derivedVarId,
                        new Array1D<HorizontalPosition>(nSourceVars) {
                            @Override
                            public HorizontalPosition get(int... coords) {
                                return location.hPos;
                            }

                            @Override
                            public void set(HorizontalPosition value, int... coords) {
                                throw new UnsupportedOperationException("This array is immutable");
                            }
                        }, pluginSource);
                locationsToValues.put(location, values);
            }

            allVariablesData.put(derivedVarId, locationsToValues);
            parameters.put(derivedVarId, getVariableMetadata(derivedVarId).getParameter());
        }

        /*
         * We now have a map of variable ID -> locations -> values.
         * 
         * We want a map of location -> variable ID -> values.
         */
        Map<ProfileLocation, Map<String, Array1D<Number>>> location2Var2Values = new HashMap<>();
        for (Entry<String, Map<ProfileLocation, Array1D<Number>>> entry : allVariablesData
                .entrySet()) {
            Map<ProfileLocation, Array1D<Number>> varData = entry.getValue();
            for (ProfileLocation location : varData.keySet()) {
                if (!location2Var2Values.containsKey(location)) {
                    location2Var2Values.put(location, new HashMap<String, Array1D<Number>>());
                }
                Map<String, Array1D<Number>> map = location2Var2Values.get(location);
                map.put(entry.getKey(), varData.get(location));
            }
        }

        /*
         * Now for each location we can create a profile feature with all
         * available variables in
         */
        for (ProfileLocation location : location2Var2Values.keySet()) {
            Map<String, Array1D<Number>> var2Values = location2Var2Values.get(location);
            ProfileFeature feature = new ProfileFeature(UUID.nameUUIDFromBytes(
                    id.toString().getBytes()).toString(), "Extracted Profile Feature",
                    description.toString(), zAxis, location.hPos, location.time, parameters,
                    var2Values);
            features.add(feature);
        }

        return features;
    }

    private VerticalAxis getVerticalAxis(Set<String> varIds) throws IncorrectDomainException {
        VerticalAxis retAxis = null;
        Extent<Double> zExtent = null;
        VerticalCrs verticalCrs = null;
        /*
         * Loop through all variables and:
         * 
         * Make sure that all their vertical axes are equal
         * 
         * If individual variables only have an extent, store an extent which
         * covers them all.
         */
        for (String varId : varIds) {
            VariableMetadata variableMetadata = getVariableMetadata(varId);
            VerticalDomain verticalDomain = variableMetadata.getVerticalDomain();

            if (verticalDomain == null) {
                /*
                 * If any of our variables doesn't have a vertical domain, this
                 * will not work
                 */
                throw new IncorrectDomainException(
                        "All variables must have a vertical domain to extract profile features");
            }

            if (verticalDomain instanceof VerticalAxis) {
                VerticalAxis verticalAxis = (VerticalAxis) verticalDomain;
                if (retAxis == null) {
                    retAxis = verticalAxis;
                } else {
                    if (!retAxis.equals(verticalAxis)) {
                        throw new IncorrectDomainException(
                                "Variables must share a vertical axis to extract profile features");
                    }
                }
            } else {
                if (zExtent == null) {
                    zExtent = verticalDomain.getExtent();
                    verticalCrs = verticalDomain.getVerticalCrs();
                } else {
                    if (verticalCrs.equals(verticalDomain.getVerticalCrs())) {
                        zExtent = Extents.newExtent(
                                Math.min(verticalDomain.getExtent().getLow(), zExtent.getLow()),
                                Math.max(verticalDomain.getExtent().getHigh(), zExtent.getHigh()));
                    } else {
                        throw new IncorrectDomainException(
                                "Variables must share the same vertical CRS to extract profile features");
                    }
                }
            }
        }
        if (zExtent != null) {
            if (retAxis == null) {
                /*
                 * We have no axes, just an extent. Create a linear axis with
                 * 100 points
                 */
                List<Double> values = new ArrayList<Double>();
                for (int i = 0; i < 100; i++) {
                    values.add(zExtent.getLow() + i * (zExtent.getHigh() - zExtent.getLow()) / 99);
                }
                retAxis = new VerticalAxisImpl("Artificial z-axis", values, verticalCrs);
                return retAxis;
            } else {
                if (retAxis.getCoordinateExtent().equals(zExtent)) {
                    /*
                     * This is the case where we have a single axis defined over
                     * all required variables, as well as an extent which
                     * matches it
                     * 
                     * We can just use the axis as-is
                     */
                    return retAxis;
                } else {
                    /*
                     * The extents do not match - i.e. we don't have matching
                     * axes
                     */
                    throw new IncorrectDomainException(
                            "At least one variable has a vertical axis which is incompatible with the vertical axes of other variables");
                }
            }
        } else {
            return retAxis;
        }
    }

    /**
     * Limits a z-axis to include a range as tightly as possible
     * 
     * @param axis
     *            The axis to limit
     * @param limits
     *            The range to limit to
     * @return A new {@link VerticalAxis} which will extend by at most one point
     *         over each of the bounds provided by limits, or the original axis
     *         if limits is <code>null</code>
     */
    private static VerticalAxis limitZAxis(VerticalAxis axis, Extent<Double> limits) {
        if (limits == null) {
            return axis;
        }
        if (limits.getHigh() < axis.getCoordinateExtent().getLow()
                || limits.getLow() > axis.getCoordinateExtent().getHigh()) {
            return null;
        }

        int lowIndex = 0;
        for (int i = 0; i < axis.size(); i++) {
            Double axisValue = axis.getCoordinateValue(i);
            if (axisValue <= limits.getLow()) {
                lowIndex = i;
            } else {
                break;
            }
        }
        int highIndex = axis.size() - 1;
        for (int i = axis.size() - 1; i >= 0; i--) {
            Double axisValue = axis.getCoordinateValue(i);
            if (axisValue >= limits.getHigh()) {
                highIndex = i;
            } else {
                break;
            }
        }

        List<Double> values = new ArrayList<Double>();
        for (int i = lowIndex; i <= highIndex; i++) {
            values.add(axis.getCoordinateValue(i));
        }
        return new VerticalAxisImpl(axis.getName(), values, axis.getVerticalCrs());
    }

    /**
     * Reads profile data for a given variable
     * 
     * @param metadata
     *            The {@link GridVariableMetadata} representing the variable
     * @param zAxis
     *            The desired vertical axis of the data
     * @param bbox
     *            The {@link BoundingBox} within which to read profiles
     * @param tExtent
     *            The time {@link Extent} within which to read profiles
     * @param dataSource
     *            The {@link GridDataSource} to read from
     * @return A {@link Map} of unique profile locations to data for each
     * @throws IOException
     *             If there was a problem reading data from the
     *             {@link GridDataSource}
     */
    private Map<ProfileLocation, Array1D<Number>> readVerticalData(GridVariableMetadata metadata,
            VerticalAxis zAxis, BoundingBox bbox, Extent<DateTime> tExtent,
            GridDataSource dataSource) throws IOException {
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
        if (tExtent == null && tAxis != null) {
            tExtent = tAxis.getExtent();
        }
        if (tExtent != null) {
            if (tExtent.getLow().equals(tExtent.getHigh())) {
                times.add(tExtent.getLow());
            } else {
                for (DateTime time : tAxis.getCoordinateValues()) {
                    if (tExtent.contains(time)) {
                        times.add(time);
                    }
                }
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
                    /*
                     * TODO we need to test this
                     */
                }

                ret.put(location, data);

            }
        }

        return ret;
    }

    private class PointSeriesLocation {
        HorizontalPosition hPos;
        VerticalPosition elevation;

        public PointSeriesLocation(HorizontalPosition hPos, VerticalPosition elevation) {
            this.hPos = hPos;
            this.elevation = elevation;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((elevation == null) ? 0 : elevation.hashCode());
            result = prime * result + ((hPos == null) ? 0 : hPos.hashCode());
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
            PointSeriesLocation other = (PointSeriesLocation) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (elevation == null) {
                if (other.elevation != null)
                    return false;
            } else if (!elevation.equals(other.elevation))
                return false;
            if (hPos == null) {
                if (other.hPos != null)
                    return false;
            } else if (!hPos.equals(other.hPos))
                return false;
            return true;
        }

        private AbstractGridDataset getOuterType() {
            return AbstractGridDataset.this;
        }
    }

    @Override
    public Collection<? extends PointSeriesFeature> extractTimeseriesFeatures(Set<String> varIds,
            PlottingDomainParams params) throws DataReadingException {
        List<PointSeriesFeature> features = new ArrayList<>();
        /*
         * If the user has passed in null for the variable IDs, they want all
         * variables returned
         */
        if (varIds == null) {
            varIds = getVariableIds();
        }

        /*
         * Find a common time-axis
         */
        Extent<DateTime> tExtent = params.getTExtent();
        TimeAxis tAxis;
        try {
            tAxis = getTimeAxis(varIds);
        } catch (IncorrectDomainException e) {
            log.error("Cannot extract timeseries for variables without time information", e);
            throw new DataReadingException("Problem finding common t-axis", e);
        }
        tAxis = limitTAxis(tAxis, tExtent);
        if (tAxis == null) {
            /*
             * No time axis within given limits. Return empty collection
             */
            return features;
        }

        /*
         * Find a bounding box to extract all profiles from
         */
        BoundingBox bbox;
        HorizontalPosition pos = params.getTargetHorizontalPosition();
        if (pos != null) {
            bbox = new BoundingBoxImpl(pos.getX(), pos.getY(), pos.getX(), pos.getY(),
                    pos.getCoordinateReferenceSystem());
        } else {
            bbox = params.getBbox();
        }

        /*
         * Find a time range to extract all profiles from
         */
        Extent<Double> zExtent;
        if (params.getTargetZ() != null) {
            zExtent = Extents.newExtent(params.getTargetZ(), params.getTargetZ());
        } else {
            zExtent = params.getZExtent();
        }

        GridDataSource dataSource = null;
        /*
         * Open the source of data
         */
        try {
            dataSource = openGridDataSource();
        } catch (IOException e) {
            throw new DataReadingException("Problem reading timeseries feature", e);
        }

        Map<String, Parameter> parameters = new HashMap<String, Parameter>();

        StringBuilder id = new StringBuilder("uk.ac.rdg.resc.edal.feature.");
        id.append(System.currentTimeMillis());
        id.append(":");
        StringBuilder description = new StringBuilder("Point series feature from variables:\n");

        /*
         * Keep a list of variable IDs which we need to generate data for from a
         * plugin
         */
        Map<String, VariablePlugin> varsToGenerate = new HashMap<String, VariablePlugin>();

        /*
         * Store a map of unique profile locations to a variable IDs/values
         */
        Map<String, Map<PointSeriesLocation, Array1D<Number>>> allVariablesData = new HashMap<>();

        /*
         * Read all of the data from non-plugin variables. This loops over all
         * variables, and then ignores those which are plugin-derived.
         */
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
             * We now know that this is not a derived variable, and hence will
             * have GridVariableMetadata
             */
            GridVariableMetadata gridVariableMetadata = (GridVariableMetadata) getVariableMetadata(varId);

            /*
             * Do the actual data reading
             */
            Map<PointSeriesLocation, Array1D<Number>> data;
            try {
                data = readTemporalData(gridVariableMetadata, tAxis, bbox, zExtent, dataSource);
            } catch (IOException e) {
                log.error("Problem reading data", e);
                throw new DataReadingException("Problem reading timeseries feature", e);
            } catch (MismatchedCrsException e) {
                log.error("Problem reading data", e);
                throw new DataReadingException("Problem reading timeseries feature", e);
            }
            allVariablesData.put(varId, data);

            /*
             * We just use the existing parameter data, as it will be the same.
             */
            parameters.put(varId, getVariableMetadata(varId).getParameter());
        }

        /*
         * Now read the plugin-derived variables
         */
        for (String derivedVarId : varsToGenerate.keySet()) {
            VariablePlugin plugin = varsToGenerate.get(derivedVarId);
            List<Map<PointSeriesLocation, Array1D<Number>>> pluginSourceData = new ArrayList<>();
            int nSourceVars = plugin.usesVariables().length;
            VariableMetadata[] pluginSourceMetadata = new VariableMetadata[plugin.usesVariables().length];
            /*
             * Loop through the variable IDs required by this plugin, getting
             * data and metadata
             * 
             * If we have already read the data, add it to the array, otherwise
             * read the data first.
             */
            for (int i = 0; i < nSourceVars; i++) {
                String pluginSourceVarId = plugin.usesVariables()[i];
                if (allVariablesData.containsKey(pluginSourceVarId)) {
                    pluginSourceData.add(allVariablesData.get(pluginSourceVarId));
                } else {
                    /*
                     * This cast should be OK, but there may be issues if we
                     * have multiple nesting of plugins
                     * 
                     * TODO Either fix is or disallow multiple-plugin nesting
                     */
                    GridVariableMetadata variableMetadata = (GridVariableMetadata) getVariableMetadata(pluginSourceVarId);
                    try {
                        pluginSourceData.add(readTemporalData(variableMetadata, tAxis, bbox,
                                zExtent, dataSource));
                    } catch (IOException e) {
                        log.error("Problem reading data", e);
                        throw new DataReadingException(
                                "Problem reading data to generate a plugin variable", e);
                    } catch (MismatchedCrsException e) {
                        log.error("Problem reading data", e);
                        throw new DataReadingException(
                                "Problem reading data to generate a plugin variable", e);
                    }
                }
                pluginSourceMetadata[i] = getVariableMetadata(pluginSourceVarId);
            }

            /*
             * Check that all plugin source variables share the same timeseries
             * locations
             */
            Set<PointSeriesLocation> timeseriesLocations = new HashSet<>();
            for (int i = 0; i < nSourceVars; i++) {
                timeseriesLocations = pluginSourceData.get(i).keySet();
                for (int j = i; j < nSourceVars; j++) {
                    Set<PointSeriesLocation> otherSourceValueLocations = pluginSourceData.get(j)
                            .keySet();
                    if (!timeseriesLocations.equals(otherSourceValueLocations)) {
                        throw new DataReadingException(
                                "Cannot extract plugin-derived profiles for variables which have different spatio-temporal locations");
                    }
                }
            }

            Map<PointSeriesLocation, Array1D<Number>> locationsToValues = new HashMap<>();
            for (final PointSeriesLocation location : timeseriesLocations) {
                @SuppressWarnings("unchecked")
                Array1D<Number>[] pluginSource = new Array1D[nSourceVars];
                for (int i = 0; i < nSourceVars; i++) {
                    pluginSource[i] = pluginSourceData.get(i).get(location);
                }
                Array1D<Number> values = plugin.generateArray1D(derivedVarId,
                        new Array1D<HorizontalPosition>(nSourceVars) {
                            @Override
                            public HorizontalPosition get(int... coords) {
                                return location.hPos;
                            }

                            @Override
                            public void set(HorizontalPosition value, int... coords) {
                                throw new UnsupportedOperationException("This array is immutable");
                            }
                        }, pluginSource);
                locationsToValues.put(location, values);
            }

            allVariablesData.put(derivedVarId, locationsToValues);
            parameters.put(derivedVarId, getVariableMetadata(derivedVarId).getParameter());
        }

        /*
         * We now have a map of variable ID -> locations -> values.
         * 
         * We want a map of location -> variable ID -> values.
         */
        Map<PointSeriesLocation, Map<String, Array1D<Number>>> location2Var2Values = new HashMap<>();
        for (Entry<String, Map<PointSeriesLocation, Array1D<Number>>> entry : allVariablesData
                .entrySet()) {
            Map<PointSeriesLocation, Array1D<Number>> varData = entry.getValue();
            for (PointSeriesLocation location : varData.keySet()) {
                if (!location2Var2Values.containsKey(location)) {
                    location2Var2Values.put(location, new HashMap<String, Array1D<Number>>());
                }
                Map<String, Array1D<Number>> map = location2Var2Values.get(location);
                map.put(entry.getKey(), varData.get(location));
            }
        }

        /*
         * Now for each location we can create a profile feature with all
         * available variables in
         */
        for (PointSeriesLocation location : location2Var2Values.keySet()) {
            Map<String, Array1D<Number>> var2Values = location2Var2Values.get(location);
            PointSeriesFeature feature = new PointSeriesFeature(UUID.nameUUIDFromBytes(
                    id.toString().getBytes()).toString(), "Extracted Profile Feature",
                    description.toString(), tAxis, location.hPos, location.elevation, parameters,
                    var2Values);
            features.add(feature);
        }

        return features;
    }

    private TimeAxis getTimeAxis(Set<String> varIds) throws IncorrectDomainException {
        /*
         * TODO Test?
         */
        TimeAxis retAxis = null;
        Extent<DateTime> tExtent = null;
        Chronology chronology = null;
        /*
         * Loop through all variables and:
         * 
         * Make sure that all their vertical axes are equal
         * 
         * If individual variables only have an extent, store an extent which
         * covers them all.
         */
        for (String varId : varIds) {
            VariableMetadata variableMetadata = getVariableMetadata(varId);
            TemporalDomain temporalDomain = variableMetadata.getTemporalDomain();

            if (temporalDomain == null) {
                /*
                 * If any of our variables doesn't have a vertical domain, this
                 * will not work
                 */
                throw new IncorrectDomainException(
                        "All variables must have a temporal domain to extract point series features");
            }

            if (temporalDomain instanceof TimeAxis) {
                TimeAxis timeAxis = (TimeAxis) temporalDomain;
                if (retAxis == null) {
                    retAxis = timeAxis;
                } else {
                    if (!retAxis.equals(timeAxis)) {
                        throw new IncorrectDomainException(
                                "Variables must share a time axis to extract point series features");
                    }
                }
            } else {
                if (tExtent == null) {
                    tExtent = temporalDomain.getExtent();
                    chronology = temporalDomain.getChronology();
                } else {
                    if (chronology.equals(temporalDomain.getChronology())) {
                        tExtent = Extents.newExtent(
                                new DateTime(Math.min(temporalDomain.getExtent().getLow()
                                        .getMillis(), tExtent.getLow().getMillis())),
                                new DateTime(Math.max(temporalDomain.getExtent().getHigh()
                                        .getMillis(), tExtent.getHigh().getMillis())));
                    } else {
                        throw new IncorrectDomainException(
                                "Variables must share the same chronology to extract point series features");
                    }
                }
            }
        }
        if (tExtent != null) {
            if (retAxis == null) {
                /*
                 * We have no axes, just an extent. Create a linear axis with
                 * 100 points
                 */
                List<DateTime> values = new ArrayList<DateTime>();
                for (int i = 0; i < 100; i++) {
                    values.add(new DateTime(tExtent.getLow().getMillis() + i
                            * (tExtent.getHigh().getMillis() - tExtent.getLow().getMillis()) / 99,
                            chronology));
                }
                retAxis = new TimeAxisImpl("Artificial time-axis", values);
                return retAxis;
            } else {
                if (retAxis.getCoordinateExtent().equals(tExtent)) {
                    /*
                     * This is the case where we have a single axis defined over
                     * all required variables, as well as an extent which
                     * matches it
                     * 
                     * We can just use the axis as-is
                     */
                    return retAxis;
                } else {
                    /*
                     * The extents do not match - i.e. we don't have matching
                     * axes
                     */
                    throw new IncorrectDomainException(
                            "At least one variable has a time axis which is incompatible with the time axes of other variables");
                }
            }
        } else {
            return retAxis;
        }
    }

    /**
     * Limits a t-axis to include a range as tightly as possible
     * 
     * @param axis
     *            The axis to limit
     * @param limits
     *            The range to limit to
     * @return A new {@link TimeAxis} which will extend by at most one point
     *         over each of the bounds provided by limits, or the original axis
     *         if limits is <code>null</code>
     */
    private static TimeAxis limitTAxis(TimeAxis axis, Extent<DateTime> limits) {
        if (limits == null) {
            return axis;
        }
        if (limits.getHigh().isBefore(axis.getCoordinateExtent().getLow())
                || limits.getLow().isAfter(axis.getCoordinateExtent().getHigh())) {
            return null;
        }
        int lowIndex = 0;
        for (int i = 0; i < axis.size(); i++) {
            DateTime axisValue = axis.getCoordinateValue(i);
            if (axisValue.isBefore(limits.getLow()) || axisValue.isEqual(limits.getLow())) {
                lowIndex = i;
            } else {
                break;
            }
        }
        int highIndex = axis.size() - 1;
        for (int i = axis.size() - 1; i >= 0; i--) {
            DateTime axisValue = axis.getCoordinateValue(i);
            if (axisValue.isAfter(limits.getHigh()) || axisValue.isEqual(limits.getHigh())) {
                highIndex = i;
            } else {
                break;
            }
        }
        List<DateTime> values = new ArrayList<DateTime>();
        for (int i = lowIndex; i <= highIndex; i++) {
            values.add(axis.getCoordinateValue(i));
        }
        return new TimeAxisImpl(axis.getName(), values);
    }

    private Map<PointSeriesLocation, Array1D<Number>> readTemporalData(
            GridVariableMetadata metadata, TimeAxis tAxis, BoundingBox bbox,
            Extent<Double> zExtent, GridDataSource dataSource) throws IOException,
            MismatchedCrsException {
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
        if (zExtent == null && zAxis != null) {
            zExtent = zAxis.getExtent();
        }
        if (zExtent != null) {
            if (zExtent.getLow().equals(zExtent.getHigh())) {
                zVals.add(zExtent.getLow());
            } else {
                for (Double zVal : zAxis.getCoordinateValues()) {
                    if (zExtent.contains(zVal)) {
                        zVals.add(zVal);
                    }
                }
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

                int zIndex = getVerticalIndex(zVal, zAxis, varId);

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

    /*
     * TODO This is OK, and is possible a method of only AbstractGridDataset, or
     * maybe we can re-introduce a minimal GridDataset type which has this
     * method. Depends what we want to do with transects.
     */
    public TrajectoryFeature readTrajectoryData(Set<String> varIds, final TrajectoryDomain domain)
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

                values.put(
                        derivedVarId,
                        plugin.generateArray1D(derivedVarId,
                                new Array1D<HorizontalPosition>(domain.size()) {
                                    @Override
                                    public HorizontalPosition get(int... coords) {
                                        return domain.getDomainObjects().get(coords)
                                                .getHorizontalPosition();
                                    }

                                    @Override
                                    public void set(HorizontalPosition value, int... coords) {
                                        throw new UnsupportedOperationException(
                                                "This array is immutable");
                                    }
                                }, pluginSourceData));
                parameters.put(derivedVarId, getVariableMetadata(derivedVarId).getParameter());
            }

            /*
             * Release resources held by the DataSource
             */
            dataSource.close();

            /*
             * Construct the TrajectoryFeature from the t and z values, the
             * horizontal grid and the VariableMetadata objects
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

    /*
     * TODO like trajectory reading, do we still need this? Probably, but how to
     * best use it?
     */
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
            if (position.getVerticalPosition() != null) {
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

            return plugin.getValue(variableId, position, baseValues);
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

                Array4D<Number> readData = gridDataSource.read(variableId, t, t, z, z, xy.getY(),
                        xy.getY(), xy.getX(), xy.getX());
                return readData.get(0, 0, 0, 0);
            } catch (IOException e) {
                throw new DataReadingException("Problem reading data", e);
            }
        }
    }

    @Override
    public Class<GridFeature> getMapFeatureType(String variableId) {
        return GridFeature.class;
    }

    protected abstract GridDataSource openGridDataSource() throws IOException;

    protected abstract DataReadingStrategy getDataReadingStrategy();
}
