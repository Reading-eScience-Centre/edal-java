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
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import uk.ac.rdg.resc.edal.domain.MapDomain;
import uk.ac.rdg.resc.edal.domain.PointCollectionDomain;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.domain.TrajectoryDomain;
import uk.ac.rdg.resc.edal.domain.VerticalDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.IncorrectDomainException;
import uk.ac.rdg.resc.edal.exceptions.MismatchedCrsException;
import uk.ac.rdg.resc.edal.exceptions.VariableNotFoundException;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.feature.PointCollectionFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxisImpl;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.ValuesArray1D;

/**
 * A partial implementation of a 4-dimensional {@link Dataset} which handles the
 * use of plugins to generate values.
 * 
 * @param <DS>
 *            The type of {@link DataSource} which will be used to read the
 *            underlying data
 * 
 * @author Guy Griffiths
 * @author Jon Blower
 */
public abstract class HorizontallyDiscreteDataset<DS extends DataSource> extends AbstractDataset implements Serializable{
    private static final Logger log = LoggerFactory.getLogger(HorizontallyDiscreteDataset.class);

    public HorizontallyDiscreteDataset(String id, Collection<? extends VariableMetadata> vars) {
        super(id, vars);
    }

    /**
     * {@inheritDoc} By default we have one feature per variable. Subclasses can
     * override this method to implement a different scheme.
     */
    @Override
    public Set<String> getFeatureIds() {
        return getVariableIds();
    }

    /**
     * Extracts {@link MapFeature}s ready to be plotted on a map
     * 
     * @param varIds
     *            The IDs of the variables to extract from the dataset
     * @param domain
     *            The domain on which to extract the data.
     * @return A {@link List} of {@link MapFeature}s on the supplied domain
     * @throws DataReadingException
     *             if there is a problem reading the underlying data
     * @throws VariableNotFoundException
     *             if one or more of the supplied variable IDs does not exist in
     *             the dataset
     */
    public final List<MapFeature> extractMapFeatures(Set<String> varIds, final MapDomain domain)
            throws DataReadingException, VariableNotFoundException {
        /*
         * If the user has passed in null for the variable IDs, they want all
         * variables returned
         */
        if (varIds == null) {
            varIds = getVariableIds();
        }

        /*
         * Create a list so that we can add to it whilst looping over the
         * elements (to add required child members). The supplied Set may be
         * abstract, so this is the safest way.
         */
        List<String> variableIds = new ArrayList<String>(varIds);

        DS dataSource = null;
        try {
            /*
             * Open the source of data
             */
            dataSource = openDataSource();

            Map<String, Array2D<Number>> values = new HashMap<String, Array2D<Number>>();

            StringBuilder name = new StringBuilder("Map of ");

            VerticalCrs vCrs = null;
            for (int i = 0; i < variableIds.size(); i++) {
                String varId = variableIds.get(i);
                VariableMetadata metadata = getVariableMetadata(varId);
                if (!metadata.isScalar()) {
                    /*
                     * Don't read data for unplottable variables, but add any
                     * children
                     */
                    Set<VariableMetadata> children = getVariableMetadata(varId).getChildren();
                    for (VariableMetadata childMetadata : children) {
                        if (!variableIds.contains(childMetadata.getId())) {
                            variableIds.add(childMetadata.getId());
                        }
                    }
                    continue;
                }

                if (vCrs == null && metadata.getVerticalDomain() != null) {
                    vCrs = metadata.getVerticalDomain().getVerticalCrs();
                }

                name.append(varId + ", ");

                /*
                 * Do the actual data reading
                 */
                Array2D<Number> data = readHorizontalData(varId, domain, dataSource);

                values.put(varId, data);
            }

            name.delete(name.length() - 2, name.length() - 1);

            String description = generateDescription("Map of variables:", varIds);
            if (domain.getTime() != null) {
                description += "Time: " + domain.getTime() + "\n";
            }
            if (domain.getZ() != null) {
                description += "Elevation: " + domain.getZ();
            }

            domain.setVerticalCrs(vCrs);
            MapFeature mapFeature = new MapFeature(generateId(varIds, domain.getBoundingBox(),
                    null, null, null, domain.getZ(), domain.getTime()), name.toString(),
                    description, domain, getParameters(varIds), values);

            return Collections.singletonList(mapFeature);
        } catch (IOException e) {
            log.error("Problem reading data", e);
            throw new DataReadingException("Problem reading map feature", e);
        } finally {
            if (dataSource != null) {
                try {
                    dataSource.close();
                } catch (DataReadingException e) {
                    log.error("Problem closing data source");
                }
            }
        }
    }

    private String generateId(Set<String> varIds, BoundingBox bbox, Extent<Double> zExtent,
            Extent<DateTime> tExtent, HorizontalPosition targetPos, Double targetZ,
            DateTime targetTime) {
        StringBuilder id = new StringBuilder("uk.ac.rdg.resc.edal.feature.");
        id.append(getId());
        id.append(":");
        for (String varId : varIds) {
            id.append(varId);
        }
        if (bbox != null) {
            id.append(bbox.toString());
        }
        if (zExtent != null) {
            id.append(zExtent.toString());
        }
        if (tExtent != null) {
            id.append(tExtent.toString());
        }
        if (targetPos != null) {
            id.append(targetPos.toString());
        }
        if (targetZ != null) {
            id.append(targetZ.toString());
        }
        if (targetTime != null) {
            id.append(targetTime.toString());
        }
        return UUID.nameUUIDFromBytes(id.toString().getBytes()).toString();
    }

    private Map<String, Parameter> getParameters(Set<String> varIds)
            throws VariableNotFoundException {
        Map<String, Parameter> parameters = new LinkedHashMap<String, Parameter>();
        for (String varId : varIds) {
            parameters.put(varId, getVariableMetadata(varId).getParameter());
        }
        return parameters;
    }

    private String generateDescription(String featureType, Set<String> varIds) {
        StringBuilder description = new StringBuilder(featureType + " from variables:\n");
        for (String varId : varIds) {
            description.append(varId + "\n");
        }

        return description.toString();
    }

    /**
     * Reads horizontal data for a variable, regardless of whether it is derived
     * or not
     * 
     * @param varId
     *            The ID of the variable to read
     * @param domain
     *            The {@link MapDomain} on which to read data
     * @param dataSource
     *            The {@link DS} to read data from
     * @return
     * @throws IOException
     *             If there is a problem opening the {@link DS}
     * @throws DataReadingException
     *             If there is a problem reading the data
     * @throws VariableNotFoundException
     */
    private Array2D<Number> readHorizontalData(String varId, final MapDomain domain, DS dataSource)
            throws IOException, DataReadingException, VariableNotFoundException {
        VariablePlugin plugin = isDerivedVariable(varId);
        if (plugin == null) {
            return readUnderlyingHorizontalData(varId, domain, dataSource);
        } else {
            @SuppressWarnings("unchecked")
            Array2D<Number>[] pluginSourceData = new Array2D[plugin.usesVariables().length];
            VariableMetadata[] pluginSourceMetadata = new VariableMetadata[plugin.usesVariables().length];
            /*
             * Loop through the variable IDs required by this plugin, getting
             * data and metadata
             * 
             * If we have already read the data, add it to the array, otherwise
             * read the data first.
             */
            for (int i = 0; i < pluginSourceData.length; i++) {
                String pluginSourceVarId = plugin.usesVariables()[i];
                pluginSourceData[i] = readHorizontalData(pluginSourceVarId, domain, dataSource);
                pluginSourceMetadata[i] = getVariableMetadata(pluginSourceVarId);
            }

            return plugin.generateArray2D(varId, new Array2D<HorizontalPosition>(domain.getYSize(),
                    domain.getXSize()) {
                @Override
                public HorizontalPosition get(int... coords) {
                    return domain.getDomainObjects().get(coords).getCentre();
                }

                @Override
                public void set(HorizontalPosition value, int... coords) {
                    throw new UnsupportedOperationException("This array is immutable");
                }
            }, pluginSourceData);
        }
    }

    @Override
    public boolean supportsProfileFeatureExtraction(String varId) {
        try {
            /*
             * We support profile feature extraction if the given variable has a
             * vertical domain.
             * 
             * If there is a problem getting the metadata, assume that profiles
             * are not supported (most likely cause is that the variable ID is
             * not found)
             */
            return getVariableMetadata(varId).getVerticalDomain() != null;
        } catch (VariableNotFoundException e) {
            return false;
        }
    }

    @Override
    public List<? extends ProfileFeature> extractProfileFeatures(Set<String> varIds,
            BoundingBox bbox, Extent<Double> zExtent, Extent<DateTime> tExtent,
            final HorizontalPosition targetPos, DateTime targetTime) throws DataReadingException,
            UnsupportedOperationException, VariableNotFoundException {
        List<ProfileFeature> features = new ArrayList<>();
        /*
         * If the user has passed in null for the variable IDs, they want all
         * variables returned
         */
        if (varIds == null) {
            varIds = getVariableIds();
        }

        /*
         * Create a list so that we can add to it whilst looping over the
         * elements (to add required child members). The supplied Set may be
         * abstract, so this is the safest way.
         */
        List<String> variableIds = new ArrayList<String>(varIds);
        for (String varId : variableIds) {
            if (!supportsProfileFeatureExtraction(varId)) {
                throw new UnsupportedOperationException(
                        "Profile extraction not supported for the variable: " + varId);
            }
        }

        /*
         * Find a common z-axis
         */
        VerticalAxis zAxis = getVerticalAxis(varIds);

        if (zAxis == null) {
            /*
             * No z-axis within given limits - return empty collection
             */
            return features;
        }

        if (zExtent != null) {
            /*
             * The given z extent does not overlap with the z-extent for the
             * requested variables. Return an empty collection
             */
            if (!zExtent.intersects(zAxis.getExtent())) {
                return features;
            } else {
                List<Double> axisValues = zAxis.getCoordinateValues();
                List<Double> newValues = new ArrayList<>();
                for (Double testVal : axisValues) {
                    if (zExtent.contains(testVal)) {
                        newValues.add(testVal);
                    }
                }
                zAxis = new VerticalAxisImpl(zAxis.getName(), newValues, zAxis.getVerticalCrs());
            }
        }

        /*
         * Find a bounding box to extract all profiles from
         */
        if (bbox == null) {
            if (targetPos != null) {
                bbox = new BoundingBoxImpl(targetPos.getX(), targetPos.getY(), targetPos.getX(),
                        targetPos.getY(), targetPos.getCoordinateReferenceSystem());
            } else {
                bbox = null;
            }
        }

        DS dataSource = null;
        try {
            /*
             * Open the source of data
             */
            dataSource = openDataSource();

            /*
             * Store a map of unique profile locations to a variable IDs/values
             */
            Map<String, Map<ProfileLocation, Array1D<Number>>> allVariablesData = new HashMap<>();

            /*
             * Read all of the data from non-plugin variables. This loops over
             * all variables, and then ignores those which are plugin-derived.
             */
            for (int i = 0; i < variableIds.size(); i++) {
                String varId = variableIds.get(i);
                if (!getVariableMetadata(varId).isScalar()) {
                    /*
                     * Don't read data for unplottable variables, but add any
                     * children
                     */
                    Set<VariableMetadata> children = getVariableMetadata(varId).getChildren();
                    for (VariableMetadata childMetadata : children) {
                        if (!variableIds.contains(childMetadata.getId())) {
                            variableIds.add(childMetadata.getId());
                        }
                    }
                    continue;
                }

                /*
                 * Do the actual data reading
                 */
                Map<ProfileLocation, Array1D<Number>> data;
                try {
                    data = readVerticalData(varId, zAxis, bbox, targetTime, tExtent, dataSource);
                } catch (IOException e) {
                    throw new DataReadingException("Problem reading profile feature", e);
                }
                allVariablesData.put(varId, data);
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
                ProfileFeature feature = new ProfileFeature(generateId(varIds, bbox, zExtent,
                        tExtent, targetPos, null, targetTime), "Extracted Profile Feature",
                        generateDescription("Profile feature", varIds), zAxis, location.hPos,
                        location.time, getParameters(varIds), var2Values);
                features.add(feature);
            }

            if (targetPos != null) {
                Collections.sort(features, new Comparator<ProfileFeature>() {
                    @Override
                    public int compare(ProfileFeature o1, ProfileFeature o2) {
                        return Double.compare(
                                GISUtils.getDistSquared(o1.getHorizontalPosition(), targetPos),
                                GISUtils.getDistSquared(o2.getHorizontalPosition(), targetPos));
                    }
                });
            }
        } catch (DataReadingException e) {
            /*
             * Rethrow. Catch is just here to ensure that finally part gets
             * executed
             */
            throw e;
        } finally {
            if (dataSource != null) {
                try {
                    dataSource.close();
                } catch (DataReadingException e) {
                    log.error("Problem closing data source");
                }
            }
        }

        return features;
    }

    private VerticalAxis getVerticalAxis(Set<String> varIds) throws IncorrectDomainException,
            VariableNotFoundException {
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
     * Reads profile data for a given variable
     * 
     * @param varId
     *            The ID of the variable to read data for
     * @param zAxis
     *            The desired vertical axis of the data
     * @param bbox
     *            The {@link BoundingBox} within which to read profiles
     * @param targetT
     *            The target time at which to read profiles
     * @param tExtent
     *            The time {@link Extent} within which to read profiles
     * @param dataSource
     *            The {@link DS} to read from
     * @return A {@link Map} of unique profile locations to data for each
     * @throws IOException
     *             If there was a problem reading data from the {@link DS}
     * @throws VariableNotFoundException
     */
    private Map<ProfileLocation, Array1D<Number>> readVerticalData(String varId,
            VerticalAxis zAxis, BoundingBox bbox, DateTime targetT, Extent<DateTime> tExtent,
            DS dataSource) throws IOException, DataReadingException, VariableNotFoundException {
        VariablePlugin plugin = isDerivedVariable(varId);
        if (plugin == null) {
            /*
             * Cast to GridVariableMetadata is fine because all
             * non-plugin-derived variables are gridded
             */
            return readUnderlyingVerticalData(varId, zAxis, bbox, targetT, tExtent, dataSource);
        } else {
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
                try {
                    pluginSourceData.add(readVerticalData(pluginSourceVarId, zAxis, bbox, targetT,
                            tExtent, dataSource));
                } catch (IOException e) {
                    log.error("Problem reading data", e);
                    throw new DataReadingException(
                            "Problem reading data to generate a plugin variable", e);
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
                Array1D<Number> values = plugin.generateArray1D(varId,
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

            return locationsToValues;
        }
    }

    @Override
    public boolean supportsTimeseriesExtraction(String varId) {
        try {
            /*
             * We support timeseries extraction if the given variable has a
             * temporal domain.
             * 
             * If there is a problem getting the metadata, assume that
             * timeseries are not supported (most likely cause is that the
             * variable ID is not found)
             */
            return getVariableMetadata(varId).getTemporalDomain() != null;
        } catch (VariableNotFoundException e) {
            return false;
        }
    }

    @Override
    public List<? extends PointSeriesFeature> extractTimeseriesFeatures(Set<String> varIds,
            BoundingBox bbox, Extent<Double> zExtent, Extent<DateTime> tExtent,
            final HorizontalPosition targetPos, Double targetZ) throws DataReadingException,
            UnsupportedOperationException, VariableNotFoundException {
        List<PointSeriesFeature> features = new ArrayList<>();
        /*
         * If the user has passed in null for the variable IDs, they want all
         * variables returned
         */
        if (varIds == null) {
            varIds = getVariableIds();
        }

        /*
         * Create a list so that we can add to it whilst looping over the
         * elements (to add required child members). The supplied Set may be
         * abstract, so this is the safest way.
         */
        List<String> variableIds = new ArrayList<String>(varIds);
        for (String varId : variableIds) {
            if (!supportsTimeseriesExtraction(varId)) {
                throw new UnsupportedOperationException(
                        "Timeseries extraction not supported for the variable: " + varId);
            }
        }

        /*
         * Find a common time-axis
         */
        TimeAxis tAxis = getTimeAxis(varIds);

        if (tAxis == null) {
            /*
             * No time axis within given limits. Return empty collection
             */
            return features;
        }

        if (tExtent != null) {
            /*
             * The given time extent does not overlap with the time-extent for
             * the requested variables. Return an empty collection
             */
            if (!tExtent.intersects(tAxis.getExtent())) {
                return features;
            } else {
                List<DateTime> axisValues = tAxis.getCoordinateValues();
                List<DateTime> newValues = new ArrayList<>();
                for (DateTime testVal : axisValues) {
                    if (tExtent.contains(testVal)) {
                        newValues.add(testVal);
                    }
                }
                tAxis = new TimeAxisImpl(tAxis.getName(), newValues);
            }
        }

        /*
         * Find a bounding box to extract all profiles from
         */
        if (bbox == null) {
            if (targetPos != null) {
                bbox = new BoundingBoxImpl(targetPos.getX(), targetPos.getY(), targetPos.getX(),
                        targetPos.getY(), targetPos.getCoordinateReferenceSystem());
            }
        }

        DS dataSource = null;
        /*
         * Open the source of data
         */
        try {
            dataSource = openDataSource();

            /*
             * Store a map of unique point series locations to a variable
             * IDs/values
             */
            Map<String, Map<PointSeriesLocation, Array1D<Number>>> allVariablesData = new LinkedHashMap<>();

            /*
             * Read all of the data from non-plugin variables. This loops over
             * all variables, and then ignores those which are plugin-derived.
             */
            for (int i = 0; i < variableIds.size(); i++) {
                String varId = variableIds.get(i);
                if (!getVariableMetadata(varId).isScalar()) {
                    /*
                     * Don't read data for unplottable variables, but add any
                     * children
                     */
                    Set<VariableMetadata> children = getVariableMetadata(varId).getChildren();
                    for (VariableMetadata childMetadata : children) {
                        if (!variableIds.contains(childMetadata.getId())) {
                            variableIds.add(childMetadata.getId());
                        }
                    }
                    continue;
                }

                /*
                 * Do the actual data reading
                 */
                Map<PointSeriesLocation, Array1D<Number>> data;
                try {
                    data = readTemporalData(varId, tAxis, bbox, targetZ, zExtent, dataSource);
                } catch (IOException e) {
                    log.error("Problem reading data", e);
                    throw new DataReadingException("Problem reading timeseries feature", e);
                } catch (MismatchedCrsException e) {
                    log.error("Problem reading data", e);
                    throw new DataReadingException("Problem reading timeseries feature", e);
                }
                allVariablesData.put(varId, data);
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
                        location2Var2Values.put(location,
                                new LinkedHashMap<String, Array1D<Number>>());
                    }
                    Map<String, Array1D<Number>> map = location2Var2Values.get(location);
                    map.put(entry.getKey(), varData.get(location));
                }
            }

            /*
             * Now for each location we can create a point series feature with
             * all available variables in
             */
            for (PointSeriesLocation location : location2Var2Values.keySet()) {
                Map<String, Array1D<Number>> var2Values = location2Var2Values.get(location);
                PointSeriesFeature feature = new PointSeriesFeature(generateId(varIds, bbox,
                        zExtent, tExtent, targetPos, targetZ, null), "Extracted Profile Feature",
                        generateDescription("Point series", varIds), tAxis, location.hPos,
                        location.elevation, getParameters(varIds), var2Values);
                features.add(feature);
            }

            if (targetPos != null) {
                Collections.sort(features, new Comparator<PointSeriesFeature>() {
                    @Override
                    public int compare(PointSeriesFeature o1, PointSeriesFeature o2) {
                        return Double.compare(
                                GISUtils.getDistSquared(o1.getHorizontalPosition(), targetPos),
                                GISUtils.getDistSquared(o2.getHorizontalPosition(), targetPos));
                    }
                });
            }
        } catch (DataReadingException e) {
            throw e;
        } finally {
            if (dataSource != null) {
                try {
                    dataSource.close();
                } catch (DataReadingException e) {
                    log.error("Problem closing data source");
                }
            }
        }

        return features;
    }

    private TimeAxis getTimeAxis(Set<String> varIds) throws IncorrectDomainException,
            VariableNotFoundException {
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
     * Reads timeseries data for a given variable
     * 
     * @param varId
     *            The ID of the variable to read data for
     * @param tAxis
     *            The desired time axis of the data
     * @param bbox
     *            The {@link BoundingBox} within which to read timeseries
     * @param targetZ
     *            The target depth to read timeseries at
     * @param zExtent
     *            The vertical {@link Extent} within which to read timeseries
     * @param dataSource
     *            The {@link DS} to read from
     * @return A {@link Map} of unique profile locations to data for each
     * @throws IOException
     *             If there was a problem reading data from the {@link DS}
     * @throws VariableNotFoundException
     */
    private Map<PointSeriesLocation, Array1D<Number>> readTemporalData(String varId,
            TimeAxis tAxis, BoundingBox bbox, Double targetZ, Extent<Double> zExtent, DS dataSource)
            throws IOException, MismatchedCrsException, DataReadingException,
            VariableNotFoundException {
        VariablePlugin plugin = isDerivedVariable(varId);
        if (plugin == null) {
            return readUnderlyingTemporalData(varId, tAxis, bbox, targetZ, zExtent, dataSource);
        } else {
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
                try {
                    pluginSourceData.add(readTemporalData(pluginSourceVarId, tAxis, bbox, targetZ,
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
                Array1D<Number> values = plugin.generateArray1D(varId,
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

            return locationsToValues;
        }
    }

    /**
     * Extracts a {@link PointCollectionFeature} containing data from the given
     * variable IDs
     * 
     * @param varIds
     *            The variables to extract data from. If <code>null</code>, all
     *            available variables will be present in the returned
     *            {@link PointCollectionFeature}
     * @param domain
     *            The {@link PointCollectionDomain} over which to extract data
     * @return A {@link PointCollectionFeature} containing the extracted data
     * @throws DataReadingException
     *             If there is a problem reading the underlying data
     * @throws VariableNotFoundException
     *             If one or more of the supplied variable IDs is not present in
     *             this {@link HorizontallyDiscreteDataset}
     */
    public PointCollectionFeature extractPointCollection(Set<String> varIds,
            final PointCollectionDomain domain) throws DataReadingException,
            VariableNotFoundException {
        final Array1D<HorizontalPosition> domainObjects = domain.getDomainObjects();
        Map<String, Array1D<Number>> values = readMultiplePoints(varIds,
                new AbstractList<GeoPosition>() {
                    @Override
                    public GeoPosition get(int index) {
                        HorizontalPosition horizontalPosition = domainObjects.get(index);
                        return new GeoPosition(horizontalPosition, domain.getVerticalPosition(),
                                domain.getTime());
                    }

                    @Override
                    public int size() {
                        return (int) domainObjects.size();
                    }
                });
        String id = getId() + ":";
        if (varIds != null) {
            for (String varId : varIds) {
                id += varId;
            }
        }
        return new PointCollectionFeature(id, "Extracted point collection", generateDescription(
                "Point collection feature", varIds), domain, getParameters(varIds), values);
    }

    /**
     * Extracts a {@link TrajectoryFeature} containing data from the given
     * variable IDs
     * 
     * @param varIds
     *            The variables to extract data from. If <code>null</code>, all
     *            available variables will be present in the returned
     *            {@link TrajectoryFeature}
     * @param domain
     *            The {@link TrajectoryDomain} over which to extract data
     * @return A {@link TrajectoryFeature} containing the extracted data
     * @throws DataReadingException
     *             If there is a problem reading the underlying data
     * @throws VariableNotFoundException
     *             If one or more of the supplied variable IDs is not present in
     *             this {@link HorizontallyDiscreteDataset}
     */
    public TrajectoryFeature extractTrajectoryFeature(Set<String> varIds,
            final TrajectoryDomain domain) throws DataReadingException, VariableNotFoundException {
        final Array1D<GeoPosition> domainObjects = domain.getDomainObjects();
        Map<String, Array1D<Number>> values = readMultiplePoints(varIds,
                new AbstractList<GeoPosition>() {
                    @Override
                    public GeoPosition get(int index) {
                        return domainObjects.get(index);
                    }

                    @Override
                    public int size() {
                        return (int) domainObjects.size();
                    }
                });
        String id = getId() + ":";
        if (varIds != null) {
            for (String varId : varIds) {
                id += varId;
            }
        }
        return new TrajectoryFeature(id, "Extracted point collection", generateDescription(
                "Point collection feature", varIds), domain, getParameters(varIds), values);
    }

    /*
     * TODO This is OK, and is possible a method of only AbstractGridDataset, or
     * maybe we can re-introduce a minimal GridDataset type which has this
     * method. Depends what we want to do with transects.
     */
    private Map<String, Array1D<Number>> readMultiplePoints(Set<String> varIds,
            final List<GeoPosition> positions) throws DataReadingException,
            VariableNotFoundException {
        DS dataSource = null;
        try {
            /*
             * Open the source of data
             */
            dataSource = openDataSource();

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
                Array1D<Number> data = readMultiplePointData(varId, positions, dataSource);

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
                        pluginSourceData[i] = readMultiplePointData(pluginSourceVarId, positions,
                                dataSource);
                    }
                    pluginSourceMetadata[i] = getVariableMetadata(pluginSourceVarId);
                }

                values.put(derivedVarId, plugin.generateArray1D(derivedVarId,
                        new Array1D<HorizontalPosition>(positions.size()) {
                            @Override
                            public HorizontalPosition get(int... coords) {
                                return positions.get(coords[0]).getHorizontalPosition();
                            }

                            @Override
                            public void set(HorizontalPosition value, int... coords) {
                                throw new UnsupportedOperationException("This array is immutable");
                            }
                        }, pluginSourceData));
                parameters.put(derivedVarId, getVariableMetadata(derivedVarId).getParameter());
            }

            return values;
        } catch (DataReadingException e) {
            throw e;
        } finally {
            if (dataSource != null) {
                try {
                    dataSource.close();
                } catch (DataReadingException e) {
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
            Double zVal, DateTime time) throws DataReadingException, VariableNotFoundException {
        DS dataSource = null;
        try {
            dataSource = openDataSource();
            return readPointData(variableId, position, zVal, time, dataSource);
        } catch (DataReadingException e) {
            throw e;
        } finally {
            if (dataSource != null) {
                try {
                    dataSource.close();
                } catch (DataReadingException e) {
                    log.error("Problem closing data source");
                }
            }
        }
    }

    private final Array1D<Number> readMultiplePointData(String variableId,
            List<GeoPosition> positions, DS dataSource) throws DataReadingException,
            VariableNotFoundException {
        Array1D<Number> data = new ValuesArray1D(positions.size());
        for (int i = 0; i < positions.size(); i++) {
            GeoPosition position = positions.get(i);
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
            DateTime time, DS dataSource) throws DataReadingException, VariableNotFoundException {
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
                baseValues[i] = readUnderlyingPointData(baseVariables[i], position, zVal, time,
                        dataSource);
            }

            return plugin.getValue(variableId, position, baseValues);
        } else {
            /*
             * We have a non-derived variable
             */
            return readUnderlyingPointData(variableId, position, zVal, time, dataSource);
        }
    }

    protected static int getTimeIndex(DateTime time, TimeAxis tAxis, String varId) {
        int tIndex = 0;
        if (tAxis != null) {
            if (time == null) {
                time = GISUtils.getClosestToCurrentTime(tAxis);
            }
            tIndex = tAxis.findIndexOf(time);
        }
        if (tIndex < 0) {
            throw new IncorrectDomainException(time
                    + " is not part of the temporal domain for the variable " + varId);
        }
        return tIndex;
    }

    protected static int getVerticalIndex(Double zPos, VerticalAxis zAxis, String varId) {
        int zIndex = 0;
        if (zAxis != null) {
            if (zPos == null) {
                zPos = GISUtils.getClosestElevationToSurface(zAxis);
            }
            zIndex = zAxis.findIndexOf(zPos);
        }
        if (zIndex < 0) {
            throw new IncorrectDomainException(zPos
                    + " is not part of the vertical domain for the variable " + varId);
        }
        return zIndex;
    }

    protected class ProfileLocation {
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
            @SuppressWarnings("unchecked")
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

        private HorizontallyDiscreteDataset<DS> getOuterType() {
            return HorizontallyDiscreteDataset.this;
        }
    }

    protected class PointSeriesLocation {
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
            @SuppressWarnings("unchecked")
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

        private HorizontallyDiscreteDataset<DS> getOuterType() {
            return HorizontallyDiscreteDataset.this;
        }
    }

    /**
     * Reads a single point for a non-derived variable
     * 
     * @param variableId
     *            The ID of the variable to read
     * @param position
     *            The {@link HorizontalPosition} at which to read the data
     * @param zVal
     *            The z-position to read at
     * @param time
     *            The time to read at
     * @param dataSource
     *            The {@link DataSource} to read from
     * @return The value of the data, or <code>null</code> if there is no data
     *         there
     * @throws DataReadingException
     *             If there is a problem reading the data
     * @throws VariableNotFoundException
     *             If the requested variable is not present in the
     *             {@link Dataset}
     */
    protected abstract Number readUnderlyingPointData(String variableId,
            HorizontalPosition position, Double zVal, DateTime time, DS dataSource)
            throws DataReadingException, VariableNotFoundException;

    /**
     * Reads horizontal data for a non-derived variable
     * 
     * @param varId
     *            The ID of the variable to read
     * @param domain
     *            The {@link MapDomain} on which to read data
     * @param zPos
     *            The z-position to read at
     * @param time
     *            The time to read at
     * @param dataSource
     *            The {@link DS} to read data from
     * @return An {@link Array2D} containing the data corresponding to the
     *         supplied {@link MapDomain}
     * @throws DataReadingException
     *             If there is a problem reading the data
     * @throws VariableNotFoundException
     *             If the requested variable is not present in the
     *             {@link Dataset}
     */
    protected abstract Array2D<Number> readUnderlyingHorizontalData(String varId, MapDomain domain,
            DS dataSource) throws DataReadingException, VariableNotFoundException;

    /**
     * Reads profile data for a given non-derived variable
     * 
     * @param varId
     *            The of the variable to read data for
     * @param zAxis
     *            The desired vertical axis of the data
     * @param bbox
     *            The {@link BoundingBox} within which to read profiles
     * @param targetT
     *            The target time at which to read profiles
     * @param tExtent
     *            The time {@link Extent} within which to read profiles
     * @param dataSource
     *            The {@link DataSource} to read from
     * @return A {@link Map} of unique profile locations to data for each
     * @throws DataReadingException
     *             If there was a problem reading data from the
     *             {@link DataSource}
     * @throws VariableNotFoundException
     *             If the requested variable is not present in the
     *             {@link Dataset}
     */
    protected abstract Map<ProfileLocation, Array1D<Number>> readUnderlyingVerticalData(
            String varId, VerticalAxis zAxis, BoundingBox bbox, DateTime targetT,
            Extent<DateTime> tExtent, DS dataSource) throws DataReadingException,
            VariableNotFoundException;

    /**
     * Reads timeseries data for a given non-derived variable
     * 
     * @param varId
     *            The of the variable to read data for
     * @param tAxis
     *            The desired time axis of the data
     * @param bbox
     *            The {@link BoundingBox} within which to read timeseries
     * @param targetZ
     *            The target depth to read timeseries at
     * @param zExtent
     *            The vertical {@link Extent} within which to read timeseries
     * @param dataSource
     *            The {@link DataSource} to read from
     * @return A {@link Map} of unique profile locations to data for each
     * @throws DataReadingException
     *             If there was a problem reading data from the
     *             {@link DataSource}
     * @throws VariableNotFoundException
     *             If the requested variable is not present in the
     *             {@link Dataset}
     */
    protected abstract Map<PointSeriesLocation, Array1D<Number>> readUnderlyingTemporalData(
            String varId, TimeAxis tAxis, BoundingBox bbox, Double targetZ, Extent<Double> zExtent,
            DS dataSource) throws DataReadingException, VariableNotFoundException;

    /**
     * @return The {@link DataSource} from which to read data
     * @throws DataReadingException
     *             if there is a problem opening the {@link DataSource}
     */
    protected abstract DS openDataSource() throws DataReadingException;
}
