/*******************************************************************************
 * Copyright (c) 2015 The University of Reading
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.HZTDataSource;
import uk.ac.rdg.resc.edal.dataset.HorizontalMesh4dDataset;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.geometry.Polygon;
import uk.ac.rdg.resc.edal.geometry.SimplePolygon;
import uk.ac.rdg.resc.edal.grid.HorizontalMesh;
import uk.ac.rdg.resc.edal.metadata.HorizontalMesh4dVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

public class CfHorizontalMesh4dDatasetFactory extends DatasetFactory {

    @Override
    public Dataset createDataset(String id, String location) throws IOException, EdalException {
        NetcdfDataset nc = NetcdfDatasetAggregator.openAndAggregateDataset(location);
        List<Variable> variables = nc.getVariables();
        Variable latVar = null;
        Variable lonVar = null;
        Variable lonBoundsVar = null;
        Variable latBoundsVar = null;
        /*
         * Find lat / lon variables
         */
        List<Parameter> params = new ArrayList<>();
        for (Variable v : variables) {
            Attribute unitsAttr = v.findAttribute("units");
            if (unitsAttr != null) {
                String attrVal = unitsAttr.getValue(0).toString();
                if (attrVal.equalsIgnoreCase("degrees_north")
                        || attrVal.equalsIgnoreCase("degree_north")
                        || attrVal.equalsIgnoreCase("degrees_N")
                        || attrVal.equalsIgnoreCase("degree_N")
                        || attrVal.equalsIgnoreCase("degreesN")
                        || attrVal.equalsIgnoreCase("degreeN")) {
                    latVar = v;
                }
                if (attrVal.equalsIgnoreCase("degrees_east")
                        || attrVal.equalsIgnoreCase("degree_east")
                        || attrVal.equalsIgnoreCase("degrees_E")
                        || attrVal.equalsIgnoreCase("degree_E")
                        || attrVal.equalsIgnoreCase("degreesE")
                        || attrVal.equalsIgnoreCase("degreeE")) {
                    lonVar = v;
                }
            }
            if (v.getFullName().equalsIgnoreCase("bounds_lat")) {
                latBoundsVar = v;
            } else if (v.getFullName().equalsIgnoreCase("bounds_lon")) {
                lonBoundsVar = v;
            } else if (!v.equals(latVar) && !v.equals(lonVar)) {
                params.add(new Parameter(v.getFullName(), v.getFullName(), null, v
                        .findAttribute("units").getValue(0).toString(), null));
            }
        }

        /*
         * Read the lat/lon positions of the grid
         */
        List<HorizontalPosition> positions = new ArrayList<>();
        List<Polygon> boundaries = new ArrayList<>();

        int nCells = lonVar.getShape(0);
        Array latData = latVar.read();
        Array lonData = lonVar.read();
        Index llIndex = latData.getIndex();

        Array latBoundsData = latBoundsVar.read();
        Array lonBoundsData = lonBoundsVar.read();
        Index boundsIndex = latBoundsData.getIndex();
        for (int i = 0; i < nCells; i++) {
            llIndex.setDim(0, i);
            double lat = latData.getDouble(llIndex);
            double lon = lonData.getDouble(llIndex);
            positions.add(new HorizontalPosition(lon, lat));

            List<HorizontalPosition> bound = new ArrayList<>();
            for (int j = 0; j < latBoundsVar.getShape(1); j++) {
                boundsIndex.set(i, j);
                double latB = latBoundsData.getDouble(boundsIndex);
                double lonB = lonBoundsData.getDouble(boundsIndex);
                bound.add(new HorizontalPosition(lonB, latB));
            }
            boundaries.add(new SimplePolygon(bound));
        }

        HorizontalMesh grid = HorizontalMesh.fromBounds(positions, boundaries);
        Set<HorizontalMesh4dVariableMetadata> vars = new HashSet<>();
        for (Parameter p : params) {
            vars.add(new HorizontalMesh4dVariableMetadata(p, grid, null, null, true));
        }
        return new CfHorizontalMesh4dDataset(id, vars, location);
    }

    /**
     * Implementation of a {@link HorizontalMesh4dDataset} to read CF-NetCDF
     * unstructured grids
     *
     * @author Guy Griffiths
     */
    private static final class CfHorizontalMesh4dDataset extends HorizontalMesh4dDataset {
        private String location;

        public CfHorizontalMesh4dDataset(String id,
                Collection<HorizontalMesh4dVariableMetadata> vars, String location) {
            super(id, vars);
            this.location = location;
        }

        @Override
        protected HZTDataSource openDataSource() throws DataReadingException {
            try {
                return new CdmMeshDataSource(
                        NetcdfDatasetAggregator.openAndAggregateDataset(location));
            } catch (EdalException | IOException e) {
                throw new DataReadingException("Problem aggregating datasets", e);
            }
        }
    }
}