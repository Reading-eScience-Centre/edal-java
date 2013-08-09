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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.domain.MapDomain;
import uk.ac.rdg.resc.edal.domain.MapDomainImpl;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.metadata.GridVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.Parameter;
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
    private Map<String, ? extends GridVariableMetadata> vars;
    
    public AbstractGridDataset(Map<String, ? extends GridVariableMetadata> vars) {
        this.vars = vars;
        for(GridVariableMetadata metadata : vars.values()) {
            metadata.setDataset(this);
        }
    }

    @Override
    public GridVariableMetadata getVariableMetadata(String variableId) {
        if (!vars.containsKey(variableId)) {
            log.error("Requested variable metadata for ID: " + variableId
                    + ", but this doesn't exist");
            throw new IllegalArgumentException(
                    "This dataset does not contain the specified variable (" + variableId + ")");
        }
        return vars.get(variableId);
    }

    @Override
    public Set<GridVariableMetadata> getTopLevelVariables() {
        return new HashSet<GridVariableMetadata>(vars.values());
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
        StringBuilder id = new StringBuilder("uk.ac.rdg.resc.edal.feature.");
        StringBuilder description = new StringBuilder("Map feature from variables:\n");
        
        /*
         * If the user has passed in null for the variable IDs, they want all variables returned
         */
        if(varIds == null) {
            varIds = getVariableIds();
        }
        for (String varId : varIds) {
            id.append(varId);
            description.append(varId + "\n");

            GridVariableMetadata existingMetadata = getVariableMetadata(varId);

            /*
             * TODO: if this is a variable whose values are derived (rather than
             * being read directly) we need to work out which of the underlying
             * grids we're really going to read.
             */

            /*
             * Get the domain of the grid
             */
            HorizontalGrid sourceGrid = existingMetadata.getHorizontalDomain();
            VerticalAxis zAxis = existingMetadata.getVerticalDomain();
            TimeAxis tAxis = existingMetadata.getTemporalDomain();

            /*
             * All variables within this dataset should share the same vertical
             * CRS (even if they don't share the same values)
             */

            /*
             * Use these objects to convert natural coordinates to grid indices
             */
            int tIndex = tAxis.findIndexOf(time);
            if(tIndex < 0) {
                throw new IllegalArgumentException(time+" is not part of the temporal domain for the variable "+varId);
            }
            int zIndex = zAxis.findIndexOf(zPos);
            if(zIndex < 0) {
                throw new IllegalArgumentException(zPos+" is not part of the vertical domain for the variable "+varId);
            }
            
            /*
             * Create a PixelMap from the source and target grids
             */
            Domain2DMapper pixelMap = Domain2DMapper.forGrid(sourceGrid, targetGrid);

            /*
             * Now use the appropriate DataReadingStrategy to read data
             */
            Array2D data = getDataReadingStrategy().readMapData(dataSource, varId, tIndex, zIndex,
                    pixelMap);

            values.put(varId, data);
            /*
             * We just use the existing parameter data, as it is likely to be
             * the same.
             * 
             * TODO this may be different for derived variables, but we haven't
             * figured them out just yet
             */
            parameters.put(varId, existingMetadata.getParameter());
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

        MapFeature mapFeature = new MapFeature(UUID.nameUUIDFromBytes(id.toString().getBytes()).toString(),
                "Extracted Map Feature", description.toString(), domain, parameters, values);

        return mapFeature;
    }

    protected abstract GridDataSource openGridDataSource() throws IOException;

    protected abstract DataReadingStrategy getDataReadingStrategy();
}
