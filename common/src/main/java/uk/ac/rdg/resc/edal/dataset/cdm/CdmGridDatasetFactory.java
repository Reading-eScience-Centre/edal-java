/*
 * Copyright (c) 2013 Reading e-Science Centre, University of Reading, UK
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of Reading e-Science Centre, University of Reading, UK, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.rdg.resc.edal.dataset.cdm;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset.Gridset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.GridDataset;
import uk.ac.rdg.resc.edal.dataset.GridMetadata;
import uk.ac.rdg.resc.edal.dataset.VariableMetadata;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

/**
 * {@link DatasetFactory} that creates {@link Dataset}s representing gridded
 * data read through the Unidata Common Data Model.
 * @author Jon
 */
public final class CdmGridDatasetFactory implements DatasetFactory<GridDataset>
{
    private static final Logger logger = LoggerFactory.getLogger(CdmGridDatasetFactory.class);

    @Override
    public GridDataset createDataset(String location, Map<String, Object> parameters) throws IOException
    {
        NetcdfDataset nc = null;
        try
        {
            // Open the dataset, using the cache for NcML aggregations
            nc = openDataset(location);
            ucar.nc2.dt.GridDataset gridDataset = CdmUtils.getGridDataset(nc);
            Map<String, VariableMetadata> vars = CollectionUtils.newHashMap();
            for (Gridset gridset : gridDataset.getGridsets())
            {
                GridCoordSystem coordSys = gridset.getGeoCoordSystem();
                // TODO: Create horizontal, vertical and temporal domains

                // Create a VariableMetadata object for each GridDatatype
                for (GridDatatype grid : gridset.getGrids())
                {
                    // Create VariableMetadata objects for each GridDatatype in the dataset
                    VariableMetadata vm = null; // TODO
                    vars.put(vm.getId(), vm);
                }
            }

            // TODO: look at variables and see whether we can create any derived
            // variables, or group them somehow.
            return new CdmGridDataset(location, vars);
        }
        finally
        {
            closeDataset(nc);
        }
    }
    
    private static final class CdmGridDataset implements GridDataset
    {
        private final String location;
        private final Map<String, VariableMetadata> vars;
        
        public CdmGridDataset(String location, Map<String, VariableMetadata> vars)
        {
            this.location = location;
            this.vars = vars;
        }

        // TODO: I'm no longer sure we need a concept of a "data grid"
        @Override
        public Set<String> getDataGridIds() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public GridMetadata getGridMetadata(String dataGridId) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Set<String> getFeatureIds() {
            // There is one feature per variable
            return this.vars.keySet();
        }

        @Override
        public Class<GridFeature> getFeatureType() {
            return GridFeature.class;
        }

        @Override
        public Set<String> getVariableIds() {
            return this.vars.keySet();
        }

        @Override
        public VariableMetadata getVariableMetadata(String variableId) {
            // TODO: what do we do if variableId doesn't match a variable?
            return this.vars.get(variableId);
        }

        @Override
        public Set<VariableMetadata> getTopLevelVariables() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public GridFeature readMapData(Set<String> varIds, DateTime time,
            VerticalPosition zPos, HorizontalGrid hGrid) throws IOException
        {
            NetcdfDataset nc = null;
            try
            {
                // Open the dataset, using the cache for NcML aggregations
                // TODO: will need to do something different for THREDDS since
                // we won't have a location
                nc = openDataset(location);
                for (String varId : varIds)
                {
                    VariableMetadata vm = this.vars.get(varId);
                    
                    // TODO: if this is a variable whose values are derived (rather than
                    // being read directly) we need to work out which of the underlying
                    // grids we're really going to read.

                    // Cast down the horizontal, vertical and temporal domain objects
                    // to HorizontalGrid, VerticalAxis and TimeAxis.  This should always
                    // be successful because we define GridDatasets to always use these
                    // domain subclasses.
                    HorizontalGrid sourceGrid = (HorizontalGrid) vm.getHorizontalDomain();
                    VerticalAxis   zAxis      = (VerticalAxis)   vm.getVerticalDomain();
                    TimeAxis       tAxis      = (TimeAxis)       vm.getTemporalDomain();
                    
                    // Convert from spatiotemporal coordinates to grid coordinates
                    
                    // Read data using appropriate strategy as Array<Number>
                    // Create new VariableMetadata object for new field
                    
                    // Build up the GridFeature object.
                }
                
                
                return null; // TODO
            }
            finally
            {
                closeDataset(nc);
            }
        }
        
        

        @Override
        public GridFeature readFeature(String featureId) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
    /**
     * Opens the NetCDF dataset at the given location, using the dataset
     * cache if {@code location} represents an NcML aggregation.  We cannot
     * use the cache for OPeNDAP or single NetCDF files because the underlying
     * data may have changed and the NetcdfDataset cache may cache a dataset
     * forever.  In the case of NcML we rely on the fact that server administrators
     * ought to have set a "recheckEvery" parameter for NcML aggregations that
     * may change with time.  It is desirable to use the dataset cache for NcML
     * aggregations because they can be time-consuming to assemble and we don't
     * want to do this every time a map is drawn.
     * @param location The location of the data: a local NetCDF file, an NcML
     * aggregation file or an OPeNDAP location, {@literal i.e.} anything that can be
     * passed to NetcdfDataset.openDataset(location).
     * @return a {@link NetcdfDataset} object for accessing the data at the
     * given location.
     * @throws IOException if there was an error reading from the data source.
     */
    private static NetcdfDataset openDataset(String location) throws IOException
    {
        boolean usedCache = false;
        NetcdfDataset nc;
        long start = System.nanoTime();
        // TODO: move isNcmlAggregation() to CdmUtils
        if (WmsUtils.isNcmlAggregation(location))
        {
            // We use the cache of NetcdfDatasets to read NcML aggregations
            // as they can be time-consuming to put together.  If the underlying
            // data can change we rely on the server admin setting the
            // "recheckEvery" parameter in the aggregation file.
            nc = NetcdfDataset.acquireDataset(location, null);
            usedCache = true;
        }
        else
        {
            // For local single files and OPeNDAP datasets we don't use the
            // cache, to ensure that we are always reading the most up-to-date
            // data.  There is a small possibility that the dataset cache will
            // have swallowed up all available file handles, in which case
            // the server admin will need to increase the number of available
            // handles on the server.
            nc = NetcdfDataset.openDataset(location);
        }
        long openedDS = System.nanoTime();
        String verb = usedCache ? "Acquired" : "Opened";
        logger.debug(verb + " NetcdfDataset in {} milliseconds", (openedDS - start) / 1.e6);
        return nc;
    }
    
    

    /** Closes the given dataset, logging any exceptions at debug level */
    private static void closeDataset(NetcdfDataset nc)
    {
        if (nc == null) return;
        try
        {
            nc.close();
            logger.debug("NetCDF file closed");
        }
        catch (IOException ex)
        {
            logger.error("IOException closing " + nc.getLocation(), ex);
        }
    }
    
}
