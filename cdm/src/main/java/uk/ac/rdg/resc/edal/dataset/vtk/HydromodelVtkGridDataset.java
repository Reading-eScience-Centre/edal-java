/*******************************************************************************
 * Copyright (c) 2018 The University of Reading
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

package uk.ac.rdg.resc.edal.dataset.vtk;

import java.io.IOException;
import java.util.Collection;

import uk.ac.rdg.resc.edal.dataset.DataReadingStrategy;
import uk.ac.rdg.resc.edal.dataset.GridDataSource;
import uk.ac.rdg.resc.edal.dataset.GriddedDataset;
import uk.ac.rdg.resc.edal.dataset.HorizontalMesh4dDataset;
import uk.ac.rdg.resc.edal.dataset.vtk.HydromodelVtkDatasetFactory.TimestepInfo;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.metadata.GridVariableMetadata;
import uk.ac.rdg.resc.edal.util.Array4D;
import uk.ac.rdg.resc.edal.util.ValuesArray4D;

/**
 * In-memory implementation of a {@link HorizontalMesh4dDataset} to read the
 * hydromodel VTK format
 *
 * @author Guy Griffiths
 */
public class HydromodelVtkGridDataset extends GriddedDataset {
    private static final long serialVersionUID = 1L;
    private final GridDataSource dataSource;

    public HydromodelVtkGridDataset(String id, Collection<GridVariableMetadata> vars,
            final TimestepInfo[] timestepsInfo) {
        super(id, vars);
        dataSource = new OnDemandVtkGridDataSource(timestepsInfo);
    }

    @Override
    protected GridDataSource openDataSource() throws DataReadingException {
        return dataSource;
    }

    @Override
    protected DataReadingStrategy getDataReadingStrategy() {
        /*
         * We need to read an entire timestep at a time, after which all of the
         * 2/3D data is in memory. So reading that as a bounding box is the most
         * efficient, since it doesn't involve copying the data multiple times.
         */
        return DataReadingStrategy.BOUNDING_BOX;
    }

    private class OnDemandVtkGridDataSource extends OnDemandVtkDataSource implements GridDataSource {
        private TimestepInfo[] timesteps;

        public OnDemandVtkGridDataSource(TimestepInfo[] timesteps) {
            super();
            this.timesteps = timesteps;
        }

        @Override
        public Array4D<Number> read(String variableId, int tmin, int tmax, int zmin, int zmax,
                int ymin, int ymax, int xmin, int xmax) throws IOException, DataReadingException {
            int tSize = tmax - tmin + 1;
            int zSize = zmax - zmin + 1;
            int ySize = ymax - ymin + 1;
            int xSize = xmax - xmin + 1;

            GridVariableMetadata metadata = HydromodelVtkGridDataset.this
                    .getVariableMetadata(variableId);
            int yTotalSize = metadata.getHorizontalDomain().getYSize();
            int xTotalSize = metadata.getHorizontalDomain().getXSize();

            Array4D<Number> ret = new ValuesArray4D(tSize, zSize, ySize, xSize);
            for (int t = tmin; t <= tmax; t++) {
                TimestepInfo timestepInfo = timesteps[t];
                
                Number[] data1d = getData1D(timestepInfo, variableId);
                
                /*
                 * Reshape the 1D data into the 4D array
                 */
                for (int z = zmin; z <= zmax; z++) {
                    for (int y = ymin; y <= ymax; y++) {
                        for (int x = xmin; x <= xmax; x++) {
                            Number value = data1d[x + xTotalSize * y + xTotalSize * yTotalSize * z];
                            ret.set(value, t - tmin, z - zmin, y - ymin, x - xmin);
                        }
                    }
                }
            }
            return ret;
        }
    }
}