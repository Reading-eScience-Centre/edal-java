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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.NetcdfDataset;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.HZTDataSource;
import uk.ac.rdg.resc.edal.dataset.HorizontalMesh4dDataset;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.grid.HorizontalMesh;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.metadata.HorizontalMesh4dVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.GISUtils;
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
 * @author Jon
 */
public final class CdmUgridDatasetFactory extends CdmDatasetFactory {
    @Override
    protected Dataset generateDataset(String id, String location, NetcdfDataset nc)
            throws IOException {
        /*
         * Keep a list of non data variables. This will include the dummy
         * variable for UGRID along with all of the co-ordinate variables.
         * 
         * We can't just check if variables are co-ordinate variables or not,
         * because this seems to include variables which are not co-ordinate
         * variables (e.g. a measurement of depth which is not an axis...)
         */
        List<Variable> nonDataVars = new ArrayList<>();

        /*
         * First find the variable containing the "cf_role = mesh_topology"
         * attribute. This is what defines that we are working with a UGRID
         * dataset.
         */
        List<Variable> variables = nc.getVariables();
        Variable meshTopology = null;
        for (Variable var : variables) {
            Attribute cfRole = var.findAttribute("cf_role");
            if (cfRole != null && cfRole.isString()
                    && cfRole.getStringValue().equalsIgnoreCase("mesh_topology")) {
                meshTopology = var;
                break;
            }
        }

        if (meshTopology == null) {
            throw new DataReadingException("This is not a UGRID compliant dataset");
        }
        nonDataVars.add(meshTopology);

        /*
         * Check the attributes of the variable to ensure that it is UGRID
         * compliant
         */
        Attribute topologyDimAttr = meshTopology.findAttribute("topology_dimension");
        Attribute nodeCoordsAttr = meshTopology.findAttribute("node_coordinates");
        Attribute faceNodeConnectivityAttr = meshTopology.findAttribute("face_node_connectivity");
        String dummyVarName = meshTopology.getFullName();
        if (topologyDimAttr == null || topologyDimAttr.getNumericValue() == null) {
            throw new DataReadingException(
                    dummyVarName
                            + " variable must contain the integer attribute \"topology_dimension\" to be UGRID compliant");
        }
        if (nodeCoordsAttr == null || !nodeCoordsAttr.isString()) {
            throw new DataReadingException(
                    dummyVarName
                            + " variable must contain the string attribute \"node_coordinates\" to be UGRID compliant");
        }
        if (faceNodeConnectivityAttr == null || !faceNodeConnectivityAttr.isString()) {
            throw new DataReadingException(
                    dummyVarName
                            + " variable must contain the string attribute \"face_node_connectivity\" to be UGRID compliant");
        }

        Number topologyDim = topologyDimAttr.getNumericValue();
        if (topologyDim.intValue() != 2) {
            throw new DataReadingException("Currently only 2D unstructured grids are supported");
        }

        String[] nodeCoordsSplit = nodeCoordsAttr.getStringValue().split("\\s+");
        if (nodeCoordsSplit.length != 2) {
            throw new DataReadingException(
                    "Need exactly 2 coordinate variables to define the 2D mesh");
        }

        /*
         * Now find the coordinate variables and determine which is latitude and
         * which is longitude.
         */
        Variable coordVar1 = nc.findVariable(nodeCoordsSplit[0]);
        Variable coordVar2 = nc.findVariable(nodeCoordsSplit[1]);
        if (coordVar1 == null || coordVar2 == null) {
            throw new DataReadingException("Coordinate variables listed in " + dummyVarName
                    + ":node_coordinates must exist to be UGRID compliant");
        }
        if (!coordVar1.getDimensions().equals(coordVar2.getDimensions())
                || coordVar1.getDimensions().size() != 1) {
            throw new DataReadingException(
                    "Coordinate variables listed in "
                            + dummyVarName
                            + ":node_coordinates must share the same single dimension to be UGRID compliant");
        }
        Attribute cv1UnitsAttr = coordVar1.findAttribute("units");
        Attribute cv2UnitsAttr = coordVar2.findAttribute("units");
        if (cv1UnitsAttr == null || cv2UnitsAttr == null || !cv1UnitsAttr.isString()
                || !cv2UnitsAttr.isString()) {
            throw new DataReadingException("Coordinate variables listed in " + dummyVarName
                    + ":node_coordinates must both contain the \"units\" attribute");
        }

        String cv1Units = cv1UnitsAttr.getStringValue();
        String cv2Units = cv2UnitsAttr.getStringValue();
        Variable longitudeVar = null;
        Variable latitudeVar = null;
        if (cv1Units.equalsIgnoreCase("degrees_north") || cv1Units.equalsIgnoreCase("degree_north")
                || cv1Units.equalsIgnoreCase("degrees_N") || cv1Units.equalsIgnoreCase("degree_N")
                || cv1Units.equalsIgnoreCase("degreesN") || cv1Units.equalsIgnoreCase("degreeN")) {
            latitudeVar = coordVar1;
        } else if (cv1Units.equalsIgnoreCase("degrees_east")
                || cv1Units.equalsIgnoreCase("degree_east")
                || cv1Units.equalsIgnoreCase("degrees_E") || cv1Units.equalsIgnoreCase("degree_E")
                || cv1Units.equalsIgnoreCase("degreesE") || cv1Units.equalsIgnoreCase("degreeE")) {
            longitudeVar = coordVar1;
        }
        if (cv2Units.equalsIgnoreCase("degrees_north") || cv2Units.equalsIgnoreCase("degree_north")
                || cv2Units.equalsIgnoreCase("degrees_N") || cv2Units.equalsIgnoreCase("degree_N")
                || cv2Units.equalsIgnoreCase("degreesN") || cv2Units.equalsIgnoreCase("degreeN")) {
            latitudeVar = coordVar2;
        } else if (cv2Units.equalsIgnoreCase("degrees_east")
                || cv2Units.equalsIgnoreCase("degree_east")
                || cv2Units.equalsIgnoreCase("degrees_E") || cv2Units.equalsIgnoreCase("degree_E")
                || cv2Units.equalsIgnoreCase("degreesE") || cv2Units.equalsIgnoreCase("degreeE")) {
            longitudeVar = coordVar2;
        }
        if (longitudeVar == null || latitudeVar == null) {
            throw new DataReadingException(
                    "Currently only lat/lon coordinates for nodes are supported");
        }
        nonDataVars.add(longitudeVar);
        nonDataVars.add(latitudeVar);

        /*
         * Now ensure that the face_node_connectivity variable exists
         */
        Variable faceNodeConnectivity = nc.findVariable(faceNodeConnectivityAttr.getStringValue());
        if (faceNodeConnectivity == null) {
            throw new DataReadingException("Coordinate variable referenced in " + dummyVarName
                    + ":face_node_connectivity must exist to be UGRID compliant");
        }
        nonDataVars.add(faceNodeConnectivity);

        /*
         * Now we can read the node co-ordinates.
         */
        Dimension hDim = latitudeVar.getDimension(0);
        Array lonData = longitudeVar.read();
        Array latData = latitudeVar.read();
        List<HorizontalPosition> positions = new ArrayList<>();
        for (int i = 0; i < lonData.getSize(); i++) {
            /*
             * We have already checked that these two are both 1D and share the
             * same dimensions
             */
            positions.add(new HorizontalPosition(lonData.getDouble(i), latData.getDouble(i)));
        }

        /*
         * Find out if we have a fill value for the face node connectivity.
         * 
         * If so, we can have a variable number of edges per face
         */
        Integer fillValue = null;
        Attribute fillValueAttr = faceNodeConnectivity.findAttribute("_FillValue");
        if (fillValueAttr != null && fillValueAttr.getNumericValue() != null) {
            fillValue = fillValueAttr.getNumericValue().intValue();
        }

        /*
         * Find what index the face connectivity indices start from (i.e. 0- or
         * 1-based indices)
         */
        int connectionsStartFrom = 0;
        Attribute startIndexAttr = faceNodeConnectivity.findAttribute("start_index");
        if (startIndexAttr != null && startIndexAttr.getNumericValue() != null) {
            connectionsStartFrom = startIndexAttr.getNumericValue().intValue();
        }
        /*
         * Now read the face connectivity
         */
        List<int[]> connections = new ArrayList<>();
        Array faceNodeData = faceNodeConnectivity.read();
        Index index = faceNodeData.getIndex();
        int[] shape = faceNodeData.getShape();
        if (shape.length != 2) {
            throw new DataReadingException("Face node connectivity must be 2-dimensional");
        }
        int nFaces = shape[0];
        int nEdges = shape[1];
        for (int i = 0; i < nFaces; i++) {
            int nEdgesThisFace;
            if (fillValue != null) {
                /*
                 * Normally we will have nEdges for each face, but in actual
                 * fact this is a maximum, so we first calculate how many of the
                 * values are populated with non-fill values
                 */
                for (int j = 0; j < nEdges; j++) {
                    index.set(i, j);
                    if (fillValue == faceNodeData.getInt(index)) {
                        nEdgesThisFace = j;
                        break;
                    }
                }
                nEdgesThisFace = nEdges;
            } else {
                nEdgesThisFace = nEdges;
            }
            int[] face = new int[nEdgesThisFace];
            for (int j = 0; j < nEdgesThisFace; j++) {
                index.set(i, j);
                face[j] = faceNodeData.getInt(index);
            }
            connections.add(face);
        }

        HorizontalMesh hMesh = HorizontalMesh.fromConnections(positions, connections,
                connectionsStartFrom);

        List<CoordinateAxis> coordinateAxes = nc.getCoordinateAxes();
        /*
         * Now find the vertical co-ordinate
         * 
         * This is a coordinate variable and will either have units of pressure,
         * or the attribute "positive"
         */
        Dimension zDim = null;
        VerticalAxis zAxis = null;
        for (CoordinateAxis coordAxis : coordinateAxes) {
            if (coordAxis.getRank() != 1) {
                /*
                 * Vertical axes are 1D
                 */
                continue;
            }
            boolean zCoord = false;
            boolean positiveUp = false;
            Attribute positiveAttribute = coordAxis.findAttribute("positive");
            if (positiveAttribute != null && positiveAttribute.isString()) {
                String posStr = positiveAttribute.getStringValue();
                if (posStr.equalsIgnoreCase("up")) {
                    positiveUp = true;
                    zCoord = true;
                } else if (posStr.equalsIgnoreCase("down")) {
                    positiveUp = false;
                    zCoord = true;
                }
            }
            String units = coordAxis.getUnitsString();
            if (units != null && GISUtils.isPressureUnits(units)) {
                zCoord = true;
            }
            if (zCoord) {
                /*
                 * We have a 1D axis so this cast should be fine.
                 */
                zAxis = CdmUtils.createVerticalAxis((CoordinateAxis1D) coordAxis, positiveUp);
                nonDataVars.add(coordAxis);
                zDim = coordAxis.getDimension(0);
            }
        }

        /*
         * Now find the time co-ordinate
         */
        Dimension tDim = null;
        TimeAxis tAxis = null;
        for (CoordinateAxis coordAxis : coordinateAxes) {
            try {
                tAxis = CdmUtils.createTimeAxis(CoordinateAxis1DTime.factory(nc, coordAxis, null));
                nonDataVars.add(coordAxis);
                tDim = coordAxis.getDimension(0);
            } catch (Exception e) {
                /*
                 * If we can't create a time axis CoordinateAxis1DTime.factory()
                 * will throw an Exception. That's fine
                 */
            }
        }

        Collection<HorizontalMesh4dVariableMetadata> variableMetadata = new ArrayList<>();
        Map<String, int[]> varId2hztIndices = new HashMap<String, int[]>();
        for (Variable var : variables) {
            if (!nonDataVars.contains(var)) {
                int[] hztIndices = new int[] { -1, -1, -1 };
                /*
                 * We have a data variable - i.e. one which should be available
                 * in the Dataset
                 */
                Parameter parameter = getParameter(var);
                HorizontalMesh hDomain = null;
                if (var.getDimensions().contains(hDim)) {
                    hDomain = hMesh;
                    hztIndices[0] = var.getDimensions().indexOf(hDim);
                }
                VerticalAxis zDomain = null;
                if (var.getDimensions().contains(zDim)) {
                    zDomain = zAxis;
                    hztIndices[1] = var.getDimensions().indexOf(zDim);
                }
                TimeAxis tDomain = null;
                if (var.getDimensions().contains(tDim)) {
                    tDomain = tAxis;
                    hztIndices[2] = var.getDimensions().indexOf(tDim);
                }
                if (hDomain != null) {
                    variableMetadata.add(new HorizontalMesh4dVariableMetadata(parameter, hDomain,
                            zDomain, tDomain, true));
                    varId2hztIndices.put(parameter.getVariableId(), hztIndices);
                } else {
                    if (zDomain != null || tDomain != null) {
                        System.out.println(zDomain + "," + tDomain + ", but no h-domain");
                    }
                }
            }
        }
        return new CdmUgridDataset(id, location, variableMetadata, varId2hztIndices);
    }

    private static final class CdmUgridDataset extends HorizontalMesh4dDataset {
        private final String location;
        private final Map<String, int[]> varId2hztIndices;

        /**
         * Construct a new {@link CdmUgridDataset}
         * 
         * @param id
         *            The ID of the {@link CdmUgridDataset}
         * @param location
         *            The location of this dataset
         * @param vars
         *            A {@link Collection} of
         *            {@link HorizontalMesh4dVariableMetadata} representing the
         *            variables in this {@link CdmUgridDataset}
         * @param varId2hztIndices
         *            A {@link Map} of variable ID to the indices of the H, Z,
         *            and T dimensions
         */
        public CdmUgridDataset(String id, String location,
                Collection<HorizontalMesh4dVariableMetadata> vars,
                Map<String, int[]> varId2hztIndices) {
            super(id, vars);
            this.location = location;
            this.varId2hztIndices = varId2hztIndices;
        }

        @Override
        protected HZTDataSource openDataSource() throws DataReadingException {
            NetcdfDataset nc;
            try {
                nc = NetcdfDatasetAggregator.getDataset(location);
                synchronized (this) {
                    /*
                     * If the getGridDataset method runs concurrently on the
                     * same object, we can get a
                     * ConcurrentModificationException, so we synchronise this
                     * action to avoid the issue.
                     */
                    return new CdmMeshDataSource(nc, varId2hztIndices);
                }
            } catch (EdalException | IOException e) {
                throw new DataReadingException("Problem aggregating datasets", e);
            }
        }
    }
}