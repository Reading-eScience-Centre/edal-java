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

package uk.ac.rdg.resc.edal.dataset.cdm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset.Gridset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.dataset.DataReadingStrategy;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.GridDataSource;
import uk.ac.rdg.resc.edal.dataset.GriddedDataset;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.metadata.GridVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.util.cdm.CdmUtils;

/**
 * {@link DatasetFactory} that creates {@link Dataset}s representing gridded
 * data read through the Unidata Common Data Model.
 * 
 * Although multiple instances of this {@link DatasetFactory} can be created,
 * all share a common cache of NetcdfDataset objects to speed up operations
 * where the same dataset is accessed multiple times. To avoid excess file
 * handles being open, this is a LRU cache which closes the datasets when they
 * expire.
 * 
 * @author Guy Griffiths
 * @author Jon Blower
 */
public final class CdmGridDatasetFactory extends CdmDatasetFactory {
    @Override
    protected Dataset generateDataset(String id, String location, NetcdfDataset nc)
            throws IOException {
        ucar.nc2.dt.GridDataset gridDataset = CdmUtils.getGridDataset(nc);
        List<GridVariableMetadata> vars = new ArrayList<>();

        for (Gridset gridset : gridDataset.getGridsets()) {
            GridCoordSystem coordSys = gridset.getGeoCoordSystem();
            HorizontalGrid hDomain = CdmUtils.createHorizontalGrid(coordSys);
            VerticalAxis zDomain = CdmUtils.createVerticalAxis(coordSys.getVerticalAxis(), coordSys.isZPositive());
            TimeAxis tDomain = CdmUtils.createTimeAxis(coordSys.getTimeAxis1D());

            /*
             * Create a VariableMetadata object for each GridDatatype
             */
            for (GridDatatype grid : gridset.getGrids()) {
                VariableDS variable = grid.getVariable();

                Parameter parameter = getParameter(variable);
                GridVariableMetadata metadata = new GridVariableMetadata(parameter, hDomain,
                        zDomain, tDomain, true);
                vars.add(metadata);
            }
        }

        CdmGridDataset cdmGridDataset = new CdmGridDataset(id, location, vars,
                CdmUtils.getOptimumDataReadingStrategy(nc));
        return cdmGridDataset;
    }

    private static final class CdmGridDataset extends GriddedDataset {
        private final String location;
        private final DataReadingStrategy dataReadingStrategy;

        public CdmGridDataset(String id, String location, Collection<GridVariableMetadata> vars,
                DataReadingStrategy dataReadingStrategy) {
            super(id, vars);
            this.location = location;
            this.dataReadingStrategy = dataReadingStrategy;
        }

        @Override
        protected GridDataSource openDataSource() throws DataReadingException {
            NetcdfDataset nc;
            try {
                nc = NetcdfDatasetAggregator.openAndAggregateDataset(location);
                synchronized (this) {
                    /*
                     * If the getGridDataset method runs concurrently on the
                     * same object, we can get a
                     * ConcurrentModificationException, so we synchronise this
                     * action to avoid the issue.
                     */
                    return new CdmGridDataSource(CdmUtils.getGridDataset(nc));
                }
            } catch (EdalException | IOException e) {
                throw new DataReadingException("Problem aggregating datasets", e);
            }
        }

        @Override
        protected DataReadingStrategy getDataReadingStrategy() {
            return dataReadingStrategy;
        }
    }
}