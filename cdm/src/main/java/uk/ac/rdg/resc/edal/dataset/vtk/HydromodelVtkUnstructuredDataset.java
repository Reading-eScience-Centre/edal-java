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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.ac.rdg.resc.edal.dataset.HZTDataSource;
import uk.ac.rdg.resc.edal.dataset.HorizontalMesh4dDataset;
import uk.ac.rdg.resc.edal.dataset.vtk.HydromodelVtkDatasetFactory.TimestepInfo;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.metadata.HorizontalMesh4dVariableMetadata;

/**
 * In-memory implementation of a {@link HorizontalMesh4dDataset} to read the
 * hydromodel VTK format
 *
 * @author Guy Griffiths
 */
public class HydromodelVtkUnstructuredDataset extends HorizontalMesh4dDataset {
    private static final long serialVersionUID = 1L;
    private final OnDemandVtkMeshDataSource dataSource;

    public HydromodelVtkUnstructuredDataset(String id,
            Collection<HorizontalMesh4dVariableMetadata> vars, TimestepInfo[] timestepsInfo,
            Number[] zVals) {
        super(id, vars);
        dataSource = new OnDemandVtkMeshDataSource(timestepsInfo, zVals);
    }

    @Override
    protected HZTDataSource openDataSource() throws DataReadingException {
        return dataSource;
    }

    public static class OnDemandVtkMeshDataSource extends OnDemandVtkDataSource
            implements HZTDataSource {
        private TimestepInfo[] timesteps;
        private Number[] zVals;

        public OnDemandVtkMeshDataSource(TimestepInfo[] timesteps, Number[] zVals) {
            super();
            this.timesteps = timesteps;
            this.zVals = zVals;
        }

        @Override
        public List<Number> read(String variableId, List<MeshCoordinates3D> coordsToRead)
                throws DataReadingException {
            List<Number> ret = new ArrayList<>();

            if (variableId.equals(HydromodelVtkDatasetFactory.Z_VAR_ID)) {
                for (MeshCoordinates3D coord : coordsToRead) {
                    if (coord.h < 0 || coord.t < 0 || coord.z < 0) {
                        ret.add(null);
                    } else {
                        ret.add(zVals[coord.h]);
                    }
                }
            } else {
                Number[] lastData = null;
                int lastT = -1;
                for (MeshCoordinates3D coord : coordsToRead) {
                    if (coord.h < 0 || coord.t < 0 || coord.z < 0) {
                        ret.add(null);
                    } else {
                        if(lastT == coord.t) {
                            ret.add(lastData[coord.h]);
                        } else {
                            TimestepInfo timestepInfo = timesteps[coord.t];
                            try {
                                Number[] data1d = getData1D(timestepInfo, variableId);
                                lastT = coord.t;
                                lastData = data1d;
                                ret.add(data1d[coord.h]);
                            } catch (IOException e) {
                                throw new DataReadingException("Problem reading data file", e);
                            }
                        }
                    }
                }
            }

            return ret;
        }

        @Override
        public void close() throws DataReadingException {
        }
    }
}