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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.dataset.plugins.VariablePlugin;
import uk.ac.rdg.resc.edal.domain.MapDomain;
import uk.ac.rdg.resc.edal.domain.MapDomainImpl;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.metadata.GridVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.util.Array2D;

/**
 * A partial implementation of a {@link GridDataset}, using a
 * {@link GridDataSource} and a {@link DataReadingStrategy}.
 * 
 * @author Jon
 * @author Guy
 */
public abstract class AbstractGridDataset implements GridDataset {
    private static final Logger log = LoggerFactory.getLogger(AbstractGridDataset.class);
    private Map<String, VariableMetadata> vars;
    private List<VariablePlugin> plugins;

    public AbstractGridDataset(Map<String, GridVariableMetadata> vars) {
        this.vars = new HashMap<String, VariableMetadata>();
        this.plugins = new ArrayList<VariablePlugin>();
        for (Entry<String, GridVariableMetadata> entry : vars.entrySet()) {
            this.vars.put(entry.getKey(), entry.getValue());
            entry.getValue().setDataset(this);
        }
    }

    @Override
    public VariableMetadata getVariableMetadata(String variableId) {
        if (!vars.containsKey(variableId)) {
            log.error("Requested variable metadata for ID: " + variableId
                    + ", but this doesn't exist");
            throw new IllegalArgumentException(
                    "This dataset does not contain the specified variable (" + variableId + ")");
        }
        return vars.get(variableId);
    }

    @Override
    public Set<VariableMetadata> getTopLevelVariables() {
        Set<VariableMetadata> ret = new HashSet<VariableMetadata>();
        for (VariableMetadata metadata : vars.values()) {
            if (metadata.getParent() == null) {
                ret.add(metadata);
            }
        }
        return ret;
    }

    @Override
    public Set<String> getFeatureIds() {
        /*
         * There is one feature per variable
         */
        return vars.keySet();
    }

    @Override
    public Set<String> getVariableIds() {
        return vars.keySet();
    }

    @Override
    public final MapFeature readMapData(Set<String> varIds, HorizontalGrid targetGrid, Double zPos,
            DateTime time) throws IOException {
        /*
         * Open the source of data
         */
        GridDataSource dataSource = openGridDataSource();

        /*
         * The procedure below can be optimized: if we know that multiple
         * variables share the same source grid then we don't have to perform
         * the conversion from natural coordinates to grid indices multiple
         * times. HOWEVER, we might have to beware of this in the case of
         * aggregation, in which different variables may have different mappings
         * from time values to filename/tIndex.
         */
        Map<String, Array2D> values = new HashMap<String, Array2D>();
        Map<String, Parameter> parameters = new HashMap<String, Parameter>();

        /*
         * We need a vertical CRS. This should be the same for all variables in
         * this dataset, so we can set it from any one of them
         */
        VerticalCrs vCrs = null;
        StringBuilder id = new StringBuilder("uk.ac.rdg.resc.edal.feature."
                + System.currentTimeMillis() + ":");
        StringBuilder description = new StringBuilder("Map feature from variables:\n");

        /*
         * Keep a list of variable IDs which we need to generate data for from a
         * plugin
         */
        Map<String, VariablePlugin> varsToGenerate = new HashMap<String, VariablePlugin>();

        /*
         * If the user has passed in null for the variable IDs, they want all
         * variables returned
         */
        if (varIds == null) {
            varIds = getVariableIds();
        }

        /*
         * We use a label here so that we can use 'continue' from a nested loop
         */
        processAllVarsIds: for (String varId : varIds) {
            id.append(varId);
            description.append(varId + "\n");

            if (!getVariableMetadata(varId).isPlottable()) {
                /*
                 * Don't read map data for unplottable variables
                 */
                continue processAllVarsIds;
            }
            for (VariablePlugin plugin : plugins) {
                /*
                 * We defer plugin-generated variables until after all other
                 * required variables have been read. This way, if any of the
                 * plugin-generated variables require data which we will read
                 * anyway, we don't have to read it twice.
                 */
                if (Arrays.asList(plugin.providesVariables()).contains(varId)) {
                    /*
                     * Save the variable ID and continue on the outer loop
                     */
                    varsToGenerate.put(varId, plugin);
                    continue processAllVarsIds;
                }
            }

            /*
             * Do the actual data reading
             */
            Array2D data = readData(varId, targetGrid, zPos, time, dataSource);

            values.put(varId, data);
            /*
             * We just use the existing parameter data, as it is likely to be
             * the same.
             */
            parameters.put(varId, getVariableMetadata(varId).getParameter());
        }

        for (String derivedVarId : varsToGenerate.keySet()) {
            VariablePlugin plugin = varsToGenerate.get(derivedVarId);
            Array2D[] pluginSourceData = new Array2D[plugin.usesVariables().length];
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
                if (values.containsKey(pluginSourceVarId)) {
                    pluginSourceData[i] = values.get(pluginSourceVarId);
                } else {
                    pluginSourceData[i] = readData(pluginSourceVarId, targetGrid, zPos, time,
                            dataSource);
                }
                pluginSourceMetadata[i] = getVariableMetadata(pluginSourceVarId);
            }

            values.put(derivedVarId, plugin.generateArray2D(derivedVarId, pluginSourceData));
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
        MapDomain domain = new MapDomainImpl(targetGrid, zPos, vCrs, time);
        if (time != null) {
            description.append("Time: " + time + "\n");
        }
        if (zPos != null) {
            description.append("Elevation: " + zPos);
        }

        MapFeature mapFeature = new MapFeature(UUID.nameUUIDFromBytes(id.toString().getBytes())
                .toString(), "Extracted Map Feature", description.toString(), domain, parameters,
                values);

        return mapFeature;
    }

    private Array2D readData(String varId, HorizontalGrid targetGrid, Double zPos, DateTime time,
            GridDataSource dataSource) throws IOException {
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
        int tIndex = 0;
        if (tAxis != null) {
            tAxis.findIndexOf(time);
        }
        if (tIndex < 0) {
            throw new IllegalArgumentException(time
                    + " is not part of the temporal domain for the variable " + varId);
        }
        int zIndex = 0;
        if (zAxis != null) {
            zAxis.findIndexOf(zPos);
        }
        if (zIndex < 0) {
            throw new IllegalArgumentException(zPos
                    + " is not part of the vertical domain for the variable " + varId);
        }

        /*
         * Create a DomainMapper from the source and target grids
         */
        Domain2DMapper domainMapper = Domain2DMapper.forGrid(sourceGrid, targetGrid);

        /*
         * Now use the appropriate DataReadingStrategy to read data
         */
        Array2D data = getDataReadingStrategy().readMapData(dataSource, varId, tIndex, zIndex,
                domainMapper);
        return data;
    }

    @Override
    public void addVariablePlugin(VariablePlugin plugin) {
        /*-
         * First check that the supplied plugin doesn't provide any variables
         * which either:
         * 
         * a) We already have in this dataset
         * b) Is generated by another plugin we already have
         */
        for (String generatedId : plugin.providesVariables()) {
            for (String alreadyHave : vars.keySet()) {
                if (alreadyHave.equals(generatedId)) {
                    throw new IllegalArgumentException(
                            "This dataset already contains the variable " + alreadyHave
                                    + " so this plugin cannot be added");
                }
            }
            for (VariablePlugin existingPlugin : plugins) {
                for (String alreadyHave : existingPlugin.providesVariables()) {
                    if (alreadyHave.equals(generatedId)) {
                        throw new IllegalArgumentException(
                                "This dataset already has a plugin which provides the variable "
                                        + alreadyHave + " so this plugin cannot be added");
                    }
                }
            }
        }
        /*
         * Now check that this dataset can supply all of the required variables.
         * 
         * At the same time, create an array of the VariableMetadata this plugin
         * uses for later use
         */
        VariableMetadata[] sourceMetadata = new VariableMetadata[plugin.usesVariables().length];
        int index = 0;
        for (String requiredId : plugin.usesVariables()) {
            if (!vars.keySet().contains(requiredId)) {
                throw new IllegalArgumentException("This plugin needs the variable " + requiredId
                        + ", but this dataset does not supply it.");
            }
            sourceMetadata[index++] = vars.get(requiredId);
        }

        plugins.add(plugin);

        /*-
         * The plugins have 2 functions:
         * 
         * 1) To generate data on-the-fly
         * 2) To insert metadata into the tree
         * 
         * For data, it's sufficient to store the plugin and use it when
         * required. For metadata, we immediately want to alter the metadata
         * tree and store the VariableMetadata in the map.
         * 
         * Do that here.
         */
        VariableMetadata[] variableMetadata = plugin.processVariableMetadata(sourceMetadata);

        for (VariableMetadata metadata : variableMetadata) {
            vars.put(metadata.getId(), metadata);
        }
    }

    protected abstract GridDataSource openGridDataSource() throws IOException;

    protected abstract DataReadingStrategy getDataReadingStrategy();
}
