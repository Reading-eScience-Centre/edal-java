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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.dataset.plugins.VariablePlugin;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.GridDomain;
import uk.ac.rdg.resc.edal.domain.SimpleGridDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.exceptions.IncorrectDomainException;
import uk.ac.rdg.resc.edal.exceptions.VariableNotFoundException;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.grid.RectilinearGridImpl;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxisImpl;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxisImpl;
import uk.ac.rdg.resc.edal.metadata.GridVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Array4D;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;
import uk.ac.rdg.resc.edal.util.ValuesArray1D;

/**
 * A partial implementation of a {@link Dataset} based on a 4D grid, using a
 * {@link GridDataSource} and a {@link DataReadingStrategy}.
 * 
 * @author Guy Griffiths
 * @author Jon Blower
 */
public abstract class GriddedDataset extends
        DiscreteLayeredDataset<GridDataSource, GridVariableMetadata> {
    private static final Logger log = LoggerFactory.getLogger(GriddedDataset.class);
    private static final long serialVersionUID = 1L;

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
             * Create a GridDomain from the GridVariableMetadata
             */
            GridDomain domain = new SimpleGridDomain(gridVariableMetadata.getHorizontalDomain(),
                    gridVariableMetadata.getVerticalDomain(),
                    gridVariableMetadata.getTemporalDomain());

            /*
             * We want this feature to contain all the children of the requested
             * layer.
             */
            Set<String> variablesToRead = recursivelyGetChildIds(gridVariableMetadata, null);

            Map<String, Parameter> parameters = new LinkedHashMap<>();
            Map<String, Array4D<Number>> values = new HashMap<String, Array4D<Number>>();
            for (String variableId : variablesToRead) {
                VariableMetadata requiredMetadata = getVariableMetadata(variableId);
                if (!(requiredMetadata instanceof GridVariableMetadata)) {
                    /*
                     * We have a variable which does not have a native grid
                     * which we can read onto.
                     */
                    String message;
                    if (variableId.equals(featureId)) {
                        message = "The feature "
                                + variableId
                                + " is not gridded.  It is probably a derived variable which is derived from variables with different grids.";
                    } else {
                        message = "The feature "
                                + variableId
                                + " (which is a child variable of "
                                + featureId
                                + ") is not gridded.  It is probably a derived variable which is derived from variables with different grids.";
                    }
                    throw new DataReadingException(message);
                }
                /*
                 * Read the actual data. This method will recursively read any
                 * data required for derived variables.
                 */
                Array4D<Number> data = read4dData(variableId, gridDataSource,
                        (GridVariableMetadata) requiredMetadata);
                values.put(variableId, data);
                parameters.put(variableId, requiredMetadata.getParameter());
            }

            return new GridFeature(featureId, featureId + " data",
                    "The entire range of data for the variable: " + featureId, domain, parameters,
                    values);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new DataReadingException("Problem reading the data from underlying storage", e);
        } finally {
            if (gridDataSource != null) {
                try {
                    gridDataSource.close();
                } catch (DataReadingException e) {
                    log.error("Problem closing data source");
                }
            }
        }
    }

    /**
     * Extracts a 4d subset of data from the dataset. All variables must share
     * the same underlying grid, which must be rectilinear and CRS:84/EPSG:4326.
     * Whilst limiting scope, this is useful in a large number of situations.
     * 
     * @param variables
     *            The variable IDs to extract into a feature
     * @param hBox
     *            The {@link BoundingBox} in which to extract data. If this has
     *            zero height/width, data will be extracted at a point, with a
     *            horizontal domain consisting of 2 single-valued axes. Note
     *            that any extracted feature will use the underlying grid, so at
     *            least this {@link BoundingBox} will be extracted, but the
     *            actual area extracted may be larger, depending on where the
     *            cell boundaries lie.
     * @param zExtent
     *            The {@link Extent} in the z-direction to subset. If
     *            <code>null</code>, all available z-values (if there are any)
     *            will be extracted
     * @param tExtent
     *            The {@link Extent} in time to subset. If <code>null</code>,
     *            all available time values (if there are any) will be extracted
     * @return A new {@link GridFeature} whose domain
     */
    public GridFeature subsetFeatures(Set<String> variables, BoundingBox hBox,
            Extent<Double> zExtent, Extent<DateTime> tExtent) throws EdalException, IOException {
        /*
         * Open the GridDataSource as a resource, so it gets closed automatically.
         */
        try (GridDataSource dataSource = this.openDataSource()) {
            Map<String, Parameter> parameters = new HashMap<>();
            Map<String, Array4D<Number>> valuesMap = new HashMap<>();
            /*
             * Used to check that all variables share a common domain
             */
            RectilinearGrid commonGrid = null;
            VerticalAxis commonZAxis = null;
            TimeAxis commonTAxis = null;

            StringBuilder nameStr = new StringBuilder("Subset of: ");
            StringBuilder descriptionStr = new StringBuilder("Variables: ");
            /*
             * The domain of the final GridFeature
             */
            GridDomain outputDomain = null;
            /*
             * Extraction indices
             */
            int minX = -1;
            int maxX = -1;
            int minY = -1;
            int maxY = -1;
            int minZ = -1;
            int maxZ = -1;
            int minT = -1;
            int maxT = -1;
            for (String var : variables) {
                nameStr.append(var + ",");
                descriptionStr.append(var + ",");

                GridVariableMetadata metadata = this.getVariableMetadata(var);

                /*
                 * First check that we can do this subset.
                 */
                HorizontalGrid hGrid = metadata.getHorizontalDomain();
                if (!(hGrid instanceof RectilinearGrid)) {
                    throw new IncorrectDomainException(
                            "Feature subsetting is currently only supported for rectilinear grids");
                }
                RectilinearGrid grid = (RectilinearGrid) hGrid;
                if (commonGrid == null) {
                    commonGrid = grid;
                } else {
                    if (!commonGrid.equals(grid)) {
                        throw new IncorrectDomainException(
                                "All variables must be on the same horizontal grid");
                    }
                }
                VerticalAxis zAxis = metadata.getVerticalDomain();
                if (commonZAxis == null) {
                    commonZAxis = zAxis;
                } else {
                    if (!commonZAxis.equals(zAxis)) {
                        throw new IncorrectDomainException(
                                "All variables must share a common z axis");
                    }
                }
                TimeAxis tAxis = metadata.getTemporalDomain();
                if (commonTAxis == null) {
                    commonTAxis = tAxis;
                } else {
                    if (!commonTAxis.equals(tAxis)) {
                        throw new IncorrectDomainException(
                                "All variables must share a common time axis");
                    }
                }

                if (outputDomain == null) {
                    /*
                     * We only need to set the min/max vars once.
                     * 
                     * At the same time, we construct the output domain
                     */
                    ReferenceableAxis<Double> xAxis = grid.getXAxis();
                    if(xAxis.isAscending()) {
                        minX = xAxis.findIndexOf(hBox.getMinX());
                        maxX = xAxis.findIndexOf(hBox.getMaxX());
                    } else {
                        minX = xAxis.findIndexOf(hBox.getMaxX());
                        maxX = xAxis.findIndexOf(hBox.getMinX());
                    }
                    /*
                     * If either limit is out of range, set it correctly
                     */
                    if(minX < 0) {
                        minX = 0;
                    }
                    if(maxX < 0) {
                        maxX = xAxis.size() - 1;
                    }

                    ReferenceableAxis<Double> yAxis = grid.getYAxis();
                    if(yAxis.isAscending()) {
                        minY = yAxis.findIndexOf(hBox.getMinY());
                        maxY = yAxis.findIndexOf(hBox.getMaxY());
                    } else {
                        minY = yAxis.findIndexOf(hBox.getMaxY());
                        maxY = yAxis.findIndexOf(hBox.getMinY());
                    }
                    /*
                     * If either limit is out of range, set it correctly
                     */
                    if(minY < 0) {
                        minY = 0;
                    }
                    if(maxY < 0) {
                        maxY = yAxis.size() - 1;
                    }

                    if (zExtent != null) {
                        minZ = zAxis.findIndexOf(zExtent.getLow());
                        maxZ = zAxis.findIndexOf(zExtent.getHigh());
                    } else if (metadata.getVerticalDomain() != null) {
                        /*
                         * null extent means we want the entire range (which may
                         * be non-existent)
                         */
                        minZ = zAxis.findIndexOf(metadata.getVerticalDomain().getExtent().getLow());
                        maxZ = zAxis
                                .findIndexOf(metadata.getVerticalDomain().getExtent().getHigh());
                    }

                    if (tExtent != null) {
                        minT = tAxis.findIndexOf(tExtent.getLow());
                        if(minT == -1) {
                            minT = 0; 
                        }
                        maxT = tAxis.findIndexOf(tExtent.getHigh());
                        if(maxT == -1) {
                            maxT = tAxis.size() - 1;
                        }
                    } else if (metadata.getTemporalDomain() != null) {
                        /*
                         * null extent means we want the entire range (which may
                         * be non-existent)
                         */
                        minT = tAxis.findIndexOf(metadata.getTemporalDomain().getExtent().getLow());
                        maxT = tAxis
                                .findIndexOf(metadata.getTemporalDomain().getExtent().getHigh());
                    }

                    /*
                     * Now construct the subset domain
                     */
                    List<Double> xAxisOutputValues = new ArrayList<>();
                    List<Double> xAxisValues = xAxis.getCoordinateValues();
                    for (int i = minX; i <= maxX; i++) {
                        xAxisOutputValues.add(xAxisValues.get(i));
                    }
                    ReferenceableAxis<Double> xOutputAxis = new ReferenceableAxisImpl(
                            xAxis.getName(), xAxisOutputValues, xAxis.wraps());

                    List<Double> yAxisOutputValues = new ArrayList<>();
                    List<Double> yAxisValues = yAxis.getCoordinateValues();
                    for (int i = minY; i <= maxY; i++) {
                        yAxisOutputValues.add(yAxisValues.get(i));
                    }
                    ReferenceableAxis<Double> yOutputAxis = new ReferenceableAxisImpl(
                            yAxis.getName(), yAxisOutputValues, yAxis.wraps());

                    RectilinearGridImpl outputGrid = new RectilinearGridImpl(xOutputAxis,
                            yOutputAxis, grid.getCoordinateReferenceSystem());

                    VerticalAxisImpl outputZAxis = null;
                    if (zAxis != null) {
                        List<Double> zAxisOutputValues = new ArrayList<>();
                        List<Double> zAxisValues = zAxis.getCoordinateValues();
                        for (int i = minZ; i <= maxZ; i++) {
                            zAxisOutputValues.add(zAxisValues.get(i));
                        }
                        outputZAxis = new VerticalAxisImpl(zAxis.getName(), zAxisOutputValues,
                                zAxis.getVerticalCrs());
                    }

                    TimeAxisImpl outputTAxis = null;
                    if (tAxis != null) {
                        List<DateTime> tAxisOutputValues = new ArrayList<>();
                        List<DateTime> tAxisValues = tAxis.getCoordinateValues();
                        for (int i = minT; i <= maxT; i++) {
                            tAxisOutputValues.add(tAxisValues.get(i));
                        }
                        outputTAxis = new TimeAxisImpl(tAxis.getName(), tAxisOutputValues);
                    }
                    outputDomain = new SimpleGridDomain(outputGrid, outputZAxis, outputTAxis);
                }
                
                /*
                 * Remove trailing commas, and finalise the name/description
                 */
                nameStr.deleteCharAt(nameStr.length() - 1);
                descriptionStr.deleteCharAt(nameStr.length() - 1);
                descriptionStr.append(" extracted over the region: " + hBox.toString());
                if (zExtent != null) {
                    descriptionStr.append(", the vertical extent: " + zExtent.toString());
                }
                if (tExtent != null) {
                    descriptionStr.append(", the time extent: " + tExtent.toString());
                }

                /*
                 * Store the Parameter data + values
                 */
                parameters.put(var, metadata.getParameter());
                Array4D<Number> data = dataSource.read(var, minT, maxT, minZ, maxZ, minY, maxY,
                        minX, maxX);
                valuesMap.put(var, data);
            }

            return new GridFeature(id, nameStr.toString(), descriptionStr.toString(), outputDomain,
                    parameters, valuesMap);
        } catch (Exception e) {
            /*
             * Catch and rethrow any exceptions. This try-catch block is just to
             * auto-close the GridDataSource
             */
            log.error("Problem subsetting feature", e);
            throw e;
        }
    }

    private Set<String> recursivelyGetChildIds(VariableMetadata metadata, Set<String> ids) {
        if (ids == null) {
            ids = new LinkedHashSet<>();
        }
        ids.add(metadata.getId());
        for (VariableMetadata child : metadata.getChildren()) {
            ids = recursivelyGetChildIds(child, ids);
        }
        return ids;
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
                if (!(sourceMetadata instanceof GridVariableMetadata)
                        || !((GridVariableMetadata) sourceMetadata).getHorizontalDomain().equals(
                                metadata.getHorizontalDomain())) {
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
    protected Array2D<Number> extractHorizontalData(GridVariableMetadata metadata, int tIndex,
            int zIndex, HorizontalGrid targetGrid, GridDataSource dataSource) {
        HorizontalGrid sourceGrid = metadata.getHorizontalDomain();
        /*
         * Create a DomainMapper from the source and target grids
         */
        Domain2DMapper domainMapper = Domain2DMapper.forGrid(sourceGrid, targetGrid);

        /*
         * Now use the appropriate DataReadingStrategy to read data
         */
        Array2D<Number> data;
        try {
            data = getDataReadingStrategy().readMapData(dataSource, metadata.getId(), tIndex,
                    zIndex, domainMapper);
        } catch (IOException e) {
            throw new DataReadingException("Could not read underlying data", e);
        }
        return data;
    }

    @Override
    protected Array1D<Number> extractProfileData(GridVariableMetadata metadata, List<Integer> zs,
            int tIndex, HorizontalPosition hPos, GridDataSource dataSource)
            throws DataReadingException {
        HorizontalGrid hGrid = metadata.getHorizontalDomain();
        GridCoordinates2D hIndices = hGrid.findIndexOf(hPos);

        int xIndex = hIndices.getX();
        int yIndex = hIndices.getY();
        /*
         * Read the data and move it to a 1D Array
         */
        int zMin = Collections.min(zs);
        int zMax = Collections.max(zs);
        Array4D<Number> data4d;
        try {
            data4d = dataSource.read(metadata.getId(), tIndex, tIndex, zMin, zMax, yIndex, yIndex,
                    xIndex, xIndex);
        } catch (IOException e) {
            throw new DataReadingException("Cannot read data from underlying data source", e);
        }
        Array1D<Number> data = new ValuesArray1D(zs.size());

        int i = 0;
        for (Integer z : zs) {
            data.set(data4d.get(new int[] { 0, z - zMin, 0, 0 }), new int[] { i++ });
        }
        return data;
    }

    @Override
    protected Array1D<Number> extractTimeseriesData(GridVariableMetadata metadata,
            List<Integer> ts, int zIndex, HorizontalPosition hPos, GridDataSource dataSource)
            throws DataReadingException {
        HorizontalGrid hGrid = metadata.getHorizontalDomain();
        GridCoordinates2D hIndices = hGrid.findIndexOf(hPos);

        int xIndex = hIndices.getX();
        int yIndex = hIndices.getY();
        /*
         * Read the data and move it to a 1D Array
         */
        int tMin = Collections.min(ts);
        int tMax = Collections.max(ts);
        Array4D<Number> data4d;
        try {
            data4d = dataSource.read(metadata.getId(), tMin, tMax, zIndex, zIndex, yIndex, yIndex,
                    xIndex, xIndex);
        } catch (IOException e) {
            throw new DataReadingException("Cannot read data from underlying data source", e);
        }
        Array1D<Number> data = new ValuesArray1D(ts.size());

        int i = 0;
        for (Integer t : ts) {
            Number value = data4d.get(new int[] { t - tMin, 0, 0, 0 });
            data.set(value, new int[] { i++ });
        }
        return data;
    }

    @Override
    protected Number extractPoint(GridVariableMetadata metadata, int t, int z,
            HorizontalPosition hPos, GridDataSource dataSource) throws DataReadingException {
        HorizontalGrid hGrid = metadata.getHorizontalDomain();
        GridCoordinates2D hIndices = hGrid.findIndexOf(hPos);
        if (hIndices == null) {
            return null;
        }

        int xIndex = hIndices.getX();
        int yIndex = hIndices.getY();

        try {
            return dataSource.read(metadata.getId(), t, t, z, z, yIndex, yIndex, xIndex, xIndex)
                    .get(0, 0, 0, 0);
        } catch (IOException e) {
            throw new DataReadingException("Problem reading underlying data", e);
        }
    }

    protected abstract DataReadingStrategy getDataReadingStrategy();
}
