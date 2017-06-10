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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.CF;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.CoordinateAxis2D;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset.Gridset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridCoordSys;
import uk.ac.rdg.resc.edal.dataset.DataReadingStrategy;
import uk.ac.rdg.resc.edal.dataset.DataSource;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.DiscreteLayeredDataset;
import uk.ac.rdg.resc.edal.dataset.GridDataSource;
import uk.ac.rdg.resc.edal.dataset.GriddedDataset;
import uk.ac.rdg.resc.edal.dataset.HZTDataSource;
import uk.ac.rdg.resc.edal.dataset.HorizontalMesh4dDataset;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.geometry.Polygon;
import uk.ac.rdg.resc.edal.geometry.SimplePolygon;
import uk.ac.rdg.resc.edal.grid.DefinedStaggeredGrid;
import uk.ac.rdg.resc.edal.grid.DerivedStaggeredGrid;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.HorizontalMesh;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.grid.StaggeredHorizontalGrid;
import uk.ac.rdg.resc.edal.grid.StaggeredHorizontalGrid.SGridPadding;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxisImpl;
import uk.ac.rdg.resc.edal.metadata.DiscreteLayeredVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.GridVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.HorizontalMesh4dVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.util.Array4D;
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
 * @author Jon Blower
 */
public final class CdmGridDatasetFactory extends CdmDatasetFactory implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(CdmGridDatasetFactory.class);
    private static final String UNSTAGGERED_SUFFIX = ":face";

    @Override
    protected DiscreteLayeredDataset<? extends DataSource, ? extends DiscreteLayeredVariableMetadata> generateDataset(
            String id, String location, NetcdfDataset nc) throws IOException {
        if (isUgrid(nc)) {
            return generateUnstructuredGridDataset(id, location, nc);
        } else if (isSgrid(nc)) {
            return generateStaggeredGridDataset(id, location, nc);
        } else {
            return generateGridDataset(id, location, nc);
        }
    }

    private CdmGridDataset generateGridDataset(String id, String location, NetcdfDataset nc)
            throws IOException {
        /*
         * This is factored out since it is also used for extracting metadata
         * from the non-staggered parts of staggered grid datasets
         */
        List<GridVariableMetadata> vars = getNonStaggeredGriddedVariableMetadata(nc);

        CdmGridDataset cdmGridDataset = new CdmGridDataset(id, location, vars,
                CdmUtils.getOptimumDataReadingStrategy(nc));
        return cdmGridDataset;
    }

    private List<GridVariableMetadata> getNonStaggeredGriddedVariableMetadata(NetcdfDataset nc)
            throws DataReadingException, IOException {
        ucar.nc2.dt.GridDataset gridDataset = CdmUtils.getGridDataset(nc);
        List<GridVariableMetadata> vars = new ArrayList<>();

        for (Gridset gridset : gridDataset.getGridsets()) {
            GridCoordSystem coordSys = gridset.getGeoCoordSystem();
            HorizontalGrid hDomain = CdmUtils.createHorizontalGrid(coordSys);
            VerticalAxis zDomain = CdmUtils.createVerticalAxis(coordSys.getVerticalAxis(),
                    coordSys.isZPositive());
            TimeAxis tDomain = CdmUtils.createTimeAxis(coordSys.getTimeAxis1D());

            /*
             * Create a VariableMetadata object for each GridDatatype
             */
            for (GridDatatype grid : gridset.getGrids()) {
                VariableDS variable = grid.getVariable();
                Attribute gridAttribute = variable.findAttribute("grid");
                Attribute locationAttribute = variable.findAttribute("location");
                if (gridAttribute != null && locationAttribute != null) {
                    /*
                     * We have a staggered grid variable. We don't want to
                     * return this, since we're specifically looking for
                     * unstaggered ones.
                     */
                    continue;
                }

                Parameter parameter = getParameter(variable);
                GridVariableMetadata metadata = new GridVariableMetadata(parameter, hDomain,
                        zDomain, tDomain, true);
                vars.add(metadata);
            }
        }
        return vars;
    }

    private DiscreteLayeredDataset<? extends DataSource, ? extends DiscreteLayeredVariableMetadata> generateStaggeredGridDataset(
            String id, String location, NetcdfDataset nc) throws IOException {
        /*
         * Get the metadata for the non-staggered variables (if there are any).
         * 
         * This uses the NetCDF-Java libs to extract the grids. This approach
         * cannot be used for staggered grids - we essentially need to build the
         * grids up ourselves from scratch. The rest of this method does that.
         */
        List<GridVariableMetadata> varMetadata;
        try {
            varMetadata = getNonStaggeredGriddedVariableMetadata(nc);
        } catch (DataReadingException e) {
            /*
             * The most likely cause here is that we don't have any "grids" (in
             * the Unidata CDM sense) in the dataset.
             * 
             * This means that all of the data variables are defined on grids
             * which are staggered. That's a bit weird, but it's not actually a
             * problem.
             * 
             * If the DataReadingException was caused by something else, then
             * we'll hit problems further down anyway.
             */
            varMetadata = new ArrayList<>();
        }

        List<Variable> variables = nc.getVariables();

        /*
         * First loop through all variables to find:
         * 
         * Definitions of the staggered grids. In the (vast?) majority of cases,
         * there will only be one of these.
         * 
         * Coordinate variables which describe vertical and time axes
         * 
         * Coordinate variables which describe horizontal axes
         * 
         * Which variables are non-data variables and can be ignored when adding
         * variables to the dataset later. (i.e. all of the above things)
         */
        List<Variable> gridDefinitions = new ArrayList<>();
        Set<String> nonDataVariables = new HashSet<>();
        Map<String, ReferenceableAxis<?>> nonHorizontalAxes = new HashMap<>();
        Map<String, CoordinateAxis> horizontalCoordinateAxes = new HashMap<>();
        for (Variable var : variables) {
            /*
             * The staggered grid definition
             */
            Attribute cfRole = var.findAttribute("cf_role");
            if (cfRole != null && cfRole.isString()
                    && cfRole.getStringValue().equalsIgnoreCase("grid_topology")) {
                gridDefinitions.add(var);
                nonDataVariables.add(var.getFullName());
                continue;
            }

            if (var instanceof CoordinateAxis1DTime) {
                /*
                 * Time axis
                 */
                TimeAxis timeAxis = CdmUtils.createTimeAxis((CoordinateAxis1DTime) var);
                nonHorizontalAxes.put(var.getFullName(), timeAxis);
                nonDataVariables.add(var.getFullName());
            } else if (var instanceof CoordinateAxis1D) {
                /*
                 * Check if it's x, y, z, or t
                 */
                CoordinateAxis1D coordinateAxis1D = (CoordinateAxis1D) var;
                if (coordinateAxis1D.getAxisType() == AxisType.GeoZ
                        || coordinateAxis1D.getAxisType() == AxisType.Height
                        || coordinateAxis1D.getAxisType() == AxisType.Pressure
                        || coordinateAxis1D.getAxisType() == AxisType.RadialElevation) {
                    /*
                     * Create a vertical axis and store it
                     */
                    VerticalAxis zAxis = CdmUtils.createVerticalAxis(coordinateAxis1D,
                            isZPositive(coordinateAxis1D));
                    nonHorizontalAxes.put(var.getFullName(), zAxis);
                } else if (coordinateAxis1D.getAxisType() == AxisType.Time) {
                    /*
                     * Create a time axis and store it. Time axes don't always
                     * appear as CoordinateAxis1DTime
                     */
                    CoordinateAxis1DTime axis = CoordinateAxis1DTime.factory(nc, (VariableDS) var,
                            null);
                    TimeAxis timeAxis = CdmUtils.createTimeAxis(axis);
                    nonHorizontalAxes.put(var.getFullName(), timeAxis);
                } else if (coordinateAxis1D.getAxisType() == AxisType.GeoX
                        || coordinateAxis1D.getAxisType() == AxisType.GeoY
                        || coordinateAxis1D.getAxisType() == AxisType.Lon
                        || coordinateAxis1D.getAxisType() == AxisType.Lat) {
                    /*
                     * This is a 1D horizontal axis
                     */
                    horizontalCoordinateAxes.put(var.getFullName(), coordinateAxis1D);
                }

                nonDataVariables.add(var.getFullName());
            } else if (var instanceof CoordinateAxis2D) {
                /*
                 * This is a 2D horizontal axis
                 */
                CoordinateAxis2D coordinateAxis = (CoordinateAxis2D) var;
                horizontalCoordinateAxes.put(var.getFullName(), coordinateAxis);
                nonDataVariables.add(var.getFullName());
            }
        }

        Map<String, Map<String, StaggeredHorizontalGrid>> sgridDefinitions = new HashMap<>();
        for (Variable gridTopology : gridDefinitions) {
            /*
             * Now process the grid topologies, checking for the required
             * attributes:
             * 
             * cf_role, topology_dimension, node_dimensions, and face_dimensions
             * are required.
             * 
             * We only support 2D grids
             */
            Attribute nodeDimsAttr = gridTopology.findAttribute("node_dimensions");
            Attribute faceDimsAttr = gridTopology.findAttribute("face_dimensions");
            Attribute topology = gridTopology.findAttribute("topology_dimension");
            if (nodeDimsAttr == null || faceDimsAttr == null || topology == null) {
                /*
                 * Check for the mandatory attributes (many of which are not
                 * mandatory)
                 */
                throw new UnsupportedOperationException(
                        "Staggered grid definitions must contain the attributes: node_dimensions, face_dimensions, and topology_dimension");
            } else if (topology.getNumericValue() != null
                    && topology.getNumericValue().intValue() != 2) {
                throw new UnsupportedOperationException(
                        "Currently, only 2D Staggered grids are supported");
            }

            Attribute nodeCoordsAttr = gridTopology.findAttribute("node_coordinates");
            String nodeCoordsStr;
            if (nodeCoordsAttr != null) {
                nodeCoordsStr = nodeCoordsAttr.getStringValue();
            } else {
                /*
                 * We don't have explicitly defined node_coordinates. In this
                 * case, we assume that 1d coordinate variables are used. Since
                 * coordinate variables share their name with the dimension
                 * which defines them, we can equate the two.
                 */
                nodeCoordsStr = nodeDimsAttr.getStringValue();
            }

            /*
             * Now we will create StaggeredHorizontalGrids for each location
             * (face, edge1, edge2). The face grid must be explicitly defined,
             * but the edge1/2 grids have default values (matching the face
             * definitions)
             */
            Map<String, StaggeredHorizontalGrid> locations2Sgrids = new HashMap<>();

            /*
             * First create the grid for the nodes. Staggered grids will be
             * based on these.
             */
            String[] nodeCoordVals = nodeCoordsStr.split("\\s+");
            HorizontalGrid nodeGrid = getGridFromCoords(nodeCoordVals[0], nodeCoordVals[1],
                    horizontalCoordinateAxes, nc);

            /*
             * Now check to see if any other grids are explicitly defined. If
             * so, we'll use them, rather than approximating the staggering of
             * the node-based grid
             */
            Attribute faceCoordAttr = gridTopology.findAttribute("face_coordinates");
            HorizontalGrid faceGrid = null;
            if (faceCoordAttr != null) {
                String[] faceCoordVals = faceCoordAttr.getStringValue().split("\\s+");
                faceGrid = getGridFromCoords(faceCoordVals[0], faceCoordVals[1],
                        horizontalCoordinateAxes, nc);
            }

            Attribute edge1CoordAttr = gridTopology.findAttribute("edge1_coordinates");
            HorizontalGrid edge1Grid = null;
            if (edge1CoordAttr != null) {
                String[] edge1CoordVals = edge1CoordAttr.getStringValue().split("\\s+");
                edge1Grid = getGridFromCoords(edge1CoordVals[0], edge1CoordVals[1],
                        horizontalCoordinateAxes, nc);
            }

            Attribute edge2CoordAttr = gridTopology.findAttribute("edge2_coordinates");
            HorizontalGrid edge2Grid = null;
            if (edge2CoordAttr != null) {
                String[] edge2CoordVals = edge2CoordAttr.getStringValue().split("\\s+");
                edge2Grid = getGridFromCoords(edge2CoordVals[0], edge2CoordVals[1],
                        horizontalCoordinateAxes, nc);
            }

            /*
             * Find the padding on the faces. This must be defined. We will also
             * use these padding definitions to define the padding on the edges
             * if that's not explicitly defined.
             */
            Attribute edge1DimsAttr = gridTopology.findAttribute("edge1_dimensions");
            Attribute edge2DimsAttr = gridTopology.findAttribute("edge2_dimensions");

            String[] nodeDimVals = nodeDimsAttr.getStringValue().split("\\s+");
            Pattern p = Pattern.compile("(.*):(.*)\\(padding:(.*)\\)(.*):(.*)\\(padding:(.*)\\)");
            Matcher matcher = p.matcher(faceDimsAttr.getStringValue());
            if (matcher.find()) {
                SGridPadding xPadding;
                SGridPadding yPadding;

                String paddingDim1 = matcher.group(2).trim();
                String paddingDim2 = matcher.group(5).trim();

                SGridPadding padding1 = SGridPadding.fromString(matcher.group(3).trim());
                SGridPadding padding2 = SGridPadding.fromString(matcher.group(6).trim());
                if (paddingDim1.equalsIgnoreCase(nodeDimVals[0])
                        && paddingDim2.equalsIgnoreCase(nodeDimVals[1])) {
                    xPadding = padding1;
                    yPadding = padding2;
                } else if (paddingDim1.equalsIgnoreCase(nodeDimVals[1])
                        && paddingDim2.equalsIgnoreCase(nodeDimVals[0])) {
                    /*
                     * I don't *think* this should ever happen, but it's
                     * possible
                     */
                    xPadding = padding2;
                    yPadding = padding1;
                } else {
                    throw new IllegalArgumentException(
                            "face_dimensions needs to refer to the node dimensions");
                }

                /*
                 * If we have an explicitly-defined face grid, use that,
                 * otherwise derive the staggered grid from the original
                 */
                if (faceGrid != null) {
                    locations2Sgrids.put("face", new DefinedStaggeredGrid(faceGrid, nodeGrid,
                            xPadding, yPadding));
                } else {
                    locations2Sgrids.put("face", new DerivedStaggeredGrid(nodeGrid, xPadding,
                            yPadding));
                }

                /*
                 * If there are no edge dimensions defined, use the defaults
                 */
                if (edge1DimsAttr == null) {
                    /*
                     * If we have an explicitly-defined edge1 grid, use that,
                     * otherwise derive the staggered grid from the original
                     */
                    if (edge1Grid != null) {
                        locations2Sgrids.put("edge1", new DefinedStaggeredGrid(edge1Grid, nodeGrid,
                                SGridPadding.NO_OFFSET, yPadding));
                    } else {
                        locations2Sgrids.put("edge1", new DerivedStaggeredGrid(nodeGrid,
                                SGridPadding.NO_OFFSET, yPadding));
                    }
                }
                if (edge2DimsAttr == null) {
                    /*
                     * If we have an explicitly-defined edge2 grid, use that,
                     * otherwise derive the staggered grid from the original
                     */
                    if (edge2Grid != null) {
                        locations2Sgrids.put("edge2", new DefinedStaggeredGrid(edge2Grid, nodeGrid,
                                xPadding, SGridPadding.NO_OFFSET));
                    } else {
                        locations2Sgrids.put("edge2", new DerivedStaggeredGrid(nodeGrid, xPadding,
                                SGridPadding.NO_OFFSET));
                    }
                }
            } else {
                throw new IllegalArgumentException("face_dimensions is not properly formed: "
                        + faceDimsAttr.getStringValue());
            }

            /*
             * If there are edge dimensions defined, process the padding here
             */
            if (edge1DimsAttr != null) {
                p = Pattern.compile("(.*):(.*)\\s+(.*):(.*)\\(padding:(.*)\\)");
                matcher = p.matcher(edge1DimsAttr.getStringValue());
                if (matcher.find()) {
                    /*
                     * If we have an explicitly-defined edge1 grid, use that,
                     * otherwise derive the staggered grid from the original
                     */
                    if (edge1Grid != null) {
                        locations2Sgrids.put(
                                "edge1",
                                new DefinedStaggeredGrid(edge1Grid, nodeGrid,
                                        SGridPadding.NO_OFFSET, SGridPadding.fromString(matcher
                                                .group(5).trim())));
                    } else {
                        locations2Sgrids.put("edge1",
                                new DerivedStaggeredGrid(nodeGrid, SGridPadding.NO_OFFSET,
                                        SGridPadding.fromString(matcher.group(5).trim())));
                    }
                } else {
                    throw new IllegalArgumentException("edge1_dimensions is not properly formed: "
                            + edge1DimsAttr.getStringValue());
                }
            }

            if (edge2DimsAttr != null) {
                p = Pattern.compile("(.*):(.*)\\(padding:(.*)\\)(.*):(.*)");
                matcher = p.matcher(edge2DimsAttr.getStringValue());
                if (matcher.find()) {
                    /*
                     * If we have an explicitly-defined edge2 grid, use that,
                     * otherwise derive the staggered grid from the original
                     */
                    if (edge2Grid != null) {
                        locations2Sgrids.put("edge2", new DefinedStaggeredGrid(edge2Grid, nodeGrid,
                                SGridPadding.fromString(matcher.group(3).trim()),
                                SGridPadding.NO_OFFSET));
                    } else {
                        locations2Sgrids.put("edge2", new DerivedStaggeredGrid(nodeGrid,
                                SGridPadding.fromString(matcher.group(3).trim()),
                                SGridPadding.NO_OFFSET));
                    }
                } else {
                    throw new IllegalArgumentException("edge2_dimensions is not properly formed: "
                            + edge1DimsAttr.getStringValue());
                }
            }

            /*
             * Store the locations -> staggered grid definitions for this grid.
             */
            sgridDefinitions.put(gridTopology.getFullName(), locations2Sgrids);
        }

        /*
         * Now create the variable metadata for the staggered data variables
         * 
         * Often, these staggered variables will not be part of a GridDataset,
         * and so we won't be able to generate the RangesList for them in the
         * CdmGridDataSource. Therefore, here we manually create RangesList
         * objects for each of the variables and add them to a Map. When we
         * create a new CdmGridDataSource, this Map will be used to pre-load the
         * cache of RangesList to avoid the issue.
         */
        Map<String, RangesList> rangesList = new HashMap<>();
        /*
         * We are going to create dynamic variables which represent the
         * staggered variables interpolated onto their original grids. To do
         * this we need to save the paddings so that they can be passed to the
         * CdmSgridDataSource.
         */
        Map<String, SGridPadding[]> paddings = new HashMap<>();
        for (Variable var : nc.getVariables()) {
            if (nonDataVariables.contains(var.getFullName())) {
                /*
                 * Ignore any non-data variables
                 */
                continue;
            }
            Attribute gridAttribute = var.findAttribute("grid");
            Attribute locationAttribute = var.findAttribute("location");
            if (gridAttribute != null && locationAttribute != null) {
                /*
                 * We have a variable on the staggered grid
                 */
                String gridId = gridAttribute.getStringValue();

                Map<String, StaggeredHorizontalGrid> location2grid = sgridDefinitions.get(gridId);

                StaggeredHorizontalGrid staggeredGrid = location2grid.get(locationAttribute
                        .getStringValue());

                /*
                 * We reverse this list (in place), so we DO NOT want to just
                 * get a shallow copy of it. That can screw things up further
                 * down the line.
                 */
                List<Dimension> dimensions = new ArrayList<>(var.getDimensions());
                if (dimensions.size() < 2) {
                    /*
                     * Not a grid. This shouldn't actually happen, but if it
                     * does, we should catch it and ignore this variable.
                     */
                    log.error("The variable "
                            + var.getFullName()
                            + " links to an SGRID variable, but it is not gridded.  Ignoring this variable");
                    continue;
                }

                Collections.reverse(dimensions);

                VerticalAxis zAxis = null;
                TimeAxis tAxis = null;
                for (Dimension dim : dimensions.subList(2, dimensions.size())) {
                    ReferenceableAxis<?> axis = nonHorizontalAxes.get(dim.getFullName());
                    if (axis instanceof VerticalAxis) {
                        zAxis = (VerticalAxis) axis;
                    } else if (axis instanceof TimeAxis) {
                        tAxis = (TimeAxis) axis;
                    }
                }

                int x, y, z = -1, t = -1;
                if (tAxis == null) {
                    if (zAxis == null) {
                        x = 1;
                        y = 0;
                    } else {
                        x = 2;
                        y = 1;
                        z = 0;
                    }
                } else {
                    if (zAxis == null) {
                        x = 2;
                        y = 1;
                        t = 0;
                    } else {
                        x = 3;
                        y = 2;
                        z = 1;
                        t = 0;
                    }
                }

                int xSize = dimensions.get(dimensions.size() - 1 - x).getLength();
                int ySize = dimensions.get(dimensions.size() - 1 - y).getLength();
                if (xSize != staggeredGrid.getXSize() || ySize != staggeredGrid.getYSize()) {
                    /*
                     * The x and y dimensions of this variable do not match the
                     * staggered grid size. The likely cause of this is that the
                     * location attribute was specified incorrectly
                     */
                    log.error("The variable "
                            + var.getFullName()
                            + " is defined as being on "
                            + locationAttribute.getStringValue()
                            + " (relative to the unstaggered parent grid).  However, that staggered grid has the size "
                            + staggeredGrid.getXSize() + "x" + staggeredGrid.getYSize()
                            + ", and this variable has the size " + xSize + "x" + ySize
                            + ".  This variable will not be made available in the dataset " + id);
                    continue;
                }

                rangesList.put(var.getFullName(), new RangesList(x, y, z, t));

                GridVariableMetadata metadata = new GridVariableMetadata(getParameter(var),
                        staggeredGrid, zAxis, tAxis, true);
                varMetadata.add(metadata);

                /*
                 * Now create another version of this variable which will be
                 * interpolated onto the original unstaggered parent grid.
                 * 
                 * Note that for vectors, these variables will appear AFTER the
                 * original ones in the list. That means that they when
                 * processed as vectors, they will overwrite the original
                 * (staggered) variables for vectors (since they share the same
                 * standard names).
                 * 
                 * This is the desired behaviour.
                 */
                Parameter p = getParameter(var);
                Parameter unstaggeredParameter = new Parameter(p.getVariableId()
                        + UNSTAGGERED_SUFFIX, p.getTitle(), p.getDescription(), p.getUnits(),
                        p.getStandardName());
                GridVariableMetadata unstaggeredMetadata = new GridVariableMetadata(
                        unstaggeredParameter, staggeredGrid.getOriginalGrid(), zAxis, tAxis, true);
                paddings.put(
                        unstaggeredMetadata.getId(),
                        new SGridPadding[] { staggeredGrid.getXPadding(),
                                staggeredGrid.getYPadding() });
                varMetadata.add(unstaggeredMetadata);
            }
        }

        CdmSgridDataset cdmGridDataset = new CdmSgridDataset(id, location, varMetadata,
                CdmUtils.getOptimumDataReadingStrategy(nc), rangesList, paddings);
        return cdmGridDataset;
    }

    private boolean isZPositive(CoordinateAxis1D zAxis) {
        if (zAxis == null)
            return false;
        if (zAxis.getPositive() != null) {
            return zAxis.getPositive().equalsIgnoreCase(CF.POSITIVE_UP);
        }
        if (zAxis.getAxisType() == AxisType.Height)
            return true;
        return zAxis.getAxisType() != AxisType.Pressure;
    }

    private HorizontalGrid getGridFromCoords(String coord1, String coord2,
            Map<String, CoordinateAxis> horizontalCoordinateAxes, NetcdfDataset nc) {
        Collection<CoordinateAxis> axes = new ArrayList<>();
        axes.add(horizontalCoordinateAxes.get(coord1.trim()));
        axes.add(horizontalCoordinateAxes.get(coord2.trim()));
        CoordinateSystem coordSys = new CoordinateSystem(nc, axes, null);
        GridCoordSys gridCoordSys = new GridCoordSys(coordSys, null);
        return CdmUtils.createHorizontalGrid(gridCoordSys);
    }

    private CdmUgridDataset generateUnstructuredGridDataset(String id, String location,
            NetcdfDataset nc) throws IOException {
        /*
         * Keep a list of non data variables. This will include the dummy
         * variable for UGRID along with all of the co-ordinate variables, and
         * any data variables which are not only spatially-dependent
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
        Variable nodeCoordVar1 = nc.findVariable(nodeCoordsSplit[0]);
        Variable nodeCoordVar2 = nc.findVariable(nodeCoordsSplit[1]);
        if (nodeCoordVar1 == null || nodeCoordVar2 == null) {
            throw new DataReadingException("Coordinate variables listed in " + dummyVarName
                    + ":node_coordinates must exist to be UGRID compliant");
        }
        if (!nodeCoordVar1.getDimensions().equals(nodeCoordVar2.getDimensions())
                || nodeCoordVar1.getDimensions().size() != 1) {
            throw new DataReadingException(
                    "Coordinate variables listed in "
                            + dummyVarName
                            + ":node_coordinates must share the same single dimension to be UGRID compliant");
        }
        Attribute ncv1UnitsAttr = nodeCoordVar1.findAttribute("units");
        Attribute ncv2UnitsAttr = nodeCoordVar2.findAttribute("units");
        if (ncv1UnitsAttr == null || ncv2UnitsAttr == null || !ncv1UnitsAttr.isString()
                || !ncv2UnitsAttr.isString()) {
            throw new DataReadingException("Coordinate variables listed in " + dummyVarName
                    + ":node_coordinates must both contain the \"units\" attribute");
        }

        String ncv1Units = ncv1UnitsAttr.getStringValue();
        String ncv2Units = ncv2UnitsAttr.getStringValue();
        Variable nodeLongitudeVar = null;
        Variable nodeLatitudeVar = null;
        if (GISUtils.isLatitudeUnits(ncv1Units)) {
            nodeLatitudeVar = nodeCoordVar1;
        } else if (GISUtils.isLongitudeUnits(ncv1Units)) {
            nodeLongitudeVar = nodeCoordVar1;
        }
        if (GISUtils.isLatitudeUnits(ncv2Units)) {
            nodeLatitudeVar = nodeCoordVar2;
        } else if (GISUtils.isLongitudeUnits(ncv2Units)) {
            nodeLongitudeVar = nodeCoordVar2;
        }
        if (nodeLongitudeVar == null || nodeLatitudeVar == null) {
            throw new DataReadingException(
                    "Currently only lat/lon coordinates for nodes are supported");
        }
        nonDataVars.add(nodeLongitudeVar);
        nonDataVars.add(nodeLatitudeVar);

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
        Dimension nodeHDim = nodeLatitudeVar.getDimension(0);
        Array nodeLonData = nodeLongitudeVar.read();
        Array nodeLatData = nodeLatitudeVar.read();
        List<HorizontalPosition> nodePositions = new ArrayList<>();
        for (int i = 0; i < nodeLonData.getSize(); i++) {
            /*
             * We have already checked that these two are both 1D and share the
             * same dimensions
             */
            nodePositions.add(new HorizontalPosition(nodeLonData.getDouble(i), nodeLatData
                    .getDouble(i)));
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
        boolean ijSwap = false;
        if (nFaces < nEdges) {
            /*
             * The number of faces / number of edges per face have been
             * specified the wrong way around
             */
            int temp = nFaces;
            nFaces = nEdges;
            nEdges = temp;
            ijSwap = true;
        }
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
                if (!ijSwap) {
                    index.set(i, j);
                } else {
                    index.set(j, i);
                }
                face[j] = faceNodeData.getInt(index);
            }
            connections.add(face);
        }

        HorizontalMesh hNodeMesh = HorizontalMesh.fromConnections(nodePositions, connections,
                connectionsStartFrom);

        /*
         * We may also have a co-incident mesh based on the face coordinates,
         * which will require a similar treatment.
         */
        HorizontalMesh hFaceMesh = null;
        Dimension faceHDim = null;
        Attribute faceCoordsAttr = meshTopology.findAttribute("face_coordinates");
        if (faceCoordsAttr != null && faceCoordsAttr.isString()) {
            String[] faceCoordsSplit = faceCoordsAttr.getStringValue().split("\\s+");
            if (faceCoordsSplit.length == 2) {
                /*
                 * Now find the coordinate variables specifying the locations of
                 * the face centres and determine which is latitude and which is
                 * longitude.
                 */
                Variable faceCoordVar1 = nc.findVariable(faceCoordsSplit[0]);
                Variable faceCoordVar2 = nc.findVariable(faceCoordsSplit[1]);
                if (faceCoordVar1 == null || faceCoordVar2 == null) {
                    throw new DataReadingException("Coordinate variables listed in " + dummyVarName
                            + ":face_coordinates must exist to be UGRID compliant");
                }
                if (!faceCoordVar1.getDimensions().equals(faceCoordVar2.getDimensions())
                        || faceCoordVar1.getDimensions().size() != 1) {
                    throw new DataReadingException(
                            "Coordinate variables listed in "
                                    + dummyVarName
                                    + ":face_coordinates must share the same single dimension to be UGRID compliant");
                }
                Attribute fcv1UnitsAttr = faceCoordVar1.findAttribute("units");
                Attribute fcv2UnitsAttr = faceCoordVar2.findAttribute("units");
                if (fcv1UnitsAttr == null || fcv2UnitsAttr == null || !fcv1UnitsAttr.isString()
                        || !fcv2UnitsAttr.isString()) {
                    throw new DataReadingException("Coordinate variables listed in " + dummyVarName
                            + ":node_coordinates must both contain the \"units\" attribute");
                }

                String fcv1Units = fcv1UnitsAttr.getStringValue();
                String fcv2Units = fcv2UnitsAttr.getStringValue();
                Variable faceLongitudeVar = null;
                Variable faceLatitudeVar = null;
                if (GISUtils.isLatitudeUnits(fcv1Units)) {
                    faceLatitudeVar = faceCoordVar1;
                } else if (GISUtils.isLongitudeUnits(fcv1Units)) {
                    faceLongitudeVar = faceCoordVar1;
                }
                if (GISUtils.isLatitudeUnits(fcv2Units)) {
                    faceLatitudeVar = faceCoordVar2;
                } else if (GISUtils.isLongitudeUnits(fcv2Units)) {
                    faceLongitudeVar = faceCoordVar2;
                }
                if (faceLongitudeVar == null || faceLatitudeVar == null
                        || faceLatitudeVar.equals(faceLongitudeVar)) {
                    throw new DataReadingException(
                            "Currently only lat/lon coordinates for faces are supported");
                }
                faceHDim = faceLatitudeVar.getDimension(0);

                nonDataVars.add(faceLongitudeVar);
                nonDataVars.add(faceLatitudeVar);

                /*
                 * We can now generate the positions of all of the faces from
                 * faceLongitudeVar and faceLatitudeVar. However, we also want
                 * to know the cell bounds for each face.
                 * 
                 * We can get this from the combination of the face connectivity
                 * and the node locations
                 */
                faceNodeData = faceNodeConnectivity.read();
                index = faceNodeData.getIndex();

                if (nFaces != faceLatitudeVar.getSize()) {
                    throw new DataReadingException(
                            "Faces latitudes/longitudes dimensions do not have the same dimension as the face connectivity.");
                }

                List<HorizontalPosition> facePositions = new ArrayList<>();
                List<Polygon> faceBoundaries = new ArrayList<>();
                Array faceLatVals = faceLatitudeVar.read();
                Array faceLonVals = faceLongitudeVar.read();

                for (int i = 0; i < nFaces; i++) {
                    facePositions.add(new HorizontalPosition(faceLonVals.getDouble(i), faceLatVals
                            .getDouble(i)));
                    int nEdgesThisFace;
                    if (fillValue != null) {
                        /*
                         * Normally we will have nEdges for each face, but in
                         * actual fact this is a maximum, so we first calculate
                         * how many of the values are populated with non-fill
                         * values
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
                    /*
                     * Get the boundary for this cell
                     */
                    List<HorizontalPosition> facePoints = new ArrayList<>();
                    for (int j = 0; j < nEdgesThisFace; j++) {
                        if (!ijSwap) {
                            index.set(i, j);
                        } else {
                            index.set(j, i);
                        }
                        int nodeId = faceNodeData.getInt(index);
                        nodeId -= connectionsStartFrom;
                        facePoints.add(nodePositions.get(nodeId));
                    }
                    SimplePolygon faceBoundary = new SimplePolygon(facePoints);
                    faceBoundaries.add(faceBoundary);
                }

                /*
                 * Generate the HorizontalMesh for this grid
                 */
                hFaceMesh = HorizontalMesh.fromBounds(facePositions, faceBoundaries);
            }
        }

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
                if (coordAxis.getRank() == 1) {
                    /*
                     * We have a 1D axis so this cast should be fine.
                     */
                    zAxis = CdmUtils.createVerticalAxis((CoordinateAxis1D) coordAxis, positiveUp);
                    nonDataVars.add(coordAxis);
                    zDim = coordAxis.getDimension(0);
                } else if (coordAxis.getRank() == 2) {
                    /*
                     * We have a 2D depth axis. IF this is an independent
                     * z-dimension + a horizontal dimension we have a vertical
                     * axis where the actual depths depend on the horizontal
                     * position.
                     * 
                     * This doesn't fit nicely into our data model, but we can
                     * model it with a 1D vertical axis with units "level".
                     */
                    List<Dimension> dimensions = coordAxis.getDimensions();
                    boolean hasHDependency = false;
                    boolean hasSelfDependency = false;
                    for (Dimension dimension : dimensions) {
                        if (dimension.getFullName().equals(coordAxis.getFullName())) {
                            hasSelfDependency = true;
                            zDim = dimension;
                        }
                        if (dimension.equals(nodeHDim) || dimension.equals(faceHDim)) {
                            hasHDependency = true;
                        }
                    }
                    if (hasHDependency && hasSelfDependency) {
                        nonDataVars.add(coordAxis);
                        List<Double> values = new ArrayList<>();
                        for (int i = 0; i < zDim.getLength(); i++) {
                            values.add((double) i);
                        }
                        zAxis = new VerticalAxisImpl("level", values, new VerticalCrsImpl("level",
                                false, true, positiveUp));
                    }
                }
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
                int variableSpatialDimensions = 0;
                /*
                 * Pick which grid it uses - either the mandatory node-based
                 * grid, or the optional face-based grid (if it exists)
                 */
                if (var.getDimensions().contains(nodeHDim)) {
                    hDomain = hNodeMesh;
                    hztIndices[0] = var.getDimensions().indexOf(nodeHDim);
                    variableSpatialDimensions++;
                } else if (var.getDimensions().contains(faceHDim)) {
                    hDomain = hFaceMesh;
                    hztIndices[0] = var.getDimensions().indexOf(faceHDim);
                    variableSpatialDimensions++;
                }
                VerticalAxis zDomain = null;
                if (var.getDimensions().contains(zDim)) {
                    zDomain = zAxis;
                    hztIndices[1] = var.getDimensions().indexOf(zDim);
                    variableSpatialDimensions++;
                }
                TimeAxis tDomain = null;
                if (var.getDimensions().contains(tDim)) {
                    tDomain = tAxis;
                    hztIndices[2] = var.getDimensions().indexOf(tDim);
                    variableSpatialDimensions++;
                }
                if (hDomain != null) {
                    /*
                     * If we don't have a horizontal domain, we just ignore this
                     * variable.
                     */
                    if (var.getDimensions().size() <= variableSpatialDimensions) {
                        /*
                         * We make variables available if they are only
                         * spatially-dependent
                         */
                        variableMetadata.add(new HorizontalMesh4dVariableMetadata(parameter,
                                hDomain, zDomain, tDomain, true));
                        varId2hztIndices.put(parameter.getVariableId(), hztIndices);
                    }
                }
            }
        }
        return new CdmUgridDataset(id, location, variableMetadata, varId2hztIndices);
    }

    /**
     * Looks inside a NetCDF dataset to determine whether it follows the UGRID
     * conventions.
     * 
     * @param nc
     *            The dataset
     * @return <code>true</code> if this is a UGRID dataset
     */
    private static boolean isUgrid(NetcdfDataset nc) {
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
        return meshTopology != null;
    }

    /**
     * Looks inside a NetCDF dataset to determine whether it follows the SGRID
     * conventions.
     * 
     * @param nc
     *            The dataset
     * @return <code>true</code> if this is a SGRID dataset
     */
    private static boolean isSgrid(NetcdfDataset nc) {
        /*
         * First find the variable containing the "cf_role = grid_topology"
         * attribute. This is what defines that we are working with a UGRID
         * dataset.
         */
        List<Variable> variables = nc.getVariables();
        Variable gridTopology = null;
        for (Variable var : variables) {
            Attribute cfRole = var.findAttribute("cf_role");
            if (cfRole != null && cfRole.isString()
                    && cfRole.getStringValue().equalsIgnoreCase("grid_topology")) {
                gridTopology = var;
                break;
            }
        }
        return gridTopology != null;
    }

    private class CdmGridDataset extends GriddedDataset {
        protected final String location;
        private final DataReadingStrategy dataReadingStrategy;

        public CdmGridDataset(String id, String location, Collection<GridVariableMetadata> vars,
                DataReadingStrategy dataReadingStrategy) {
            super(id, vars);
            this.location = location;
            this.dataReadingStrategy = dataReadingStrategy;
            log.debug("Data reading strategy for "+id+": "+dataReadingStrategy);
        }

        @Override
        protected GridDataSource openDataSource() throws DataReadingException {
            NetcdfDataset nc = null;
            try {
                nc = CdmGridDatasetFactory.this.getNetcdfDatasetFromLocation(location, false);
                synchronized (this) {
                    /*
                     * If the getGridDataset method runs concurrently on the
                     * same object, we can get a
                     * ConcurrentModificationException, so we synchronise this
                     * action to avoid the issue.
                     */
                    return new CdmGridDataSource(nc);
                }
            } catch (EdalException | IOException e) {
                if (nc != null) {
                    NetcdfDatasetAggregator.releaseDataset(nc);
                }
                throw new DataReadingException("Problem aggregating datasets", e);
            }
        }

        @Override
        protected DataReadingStrategy getDataReadingStrategy() {
            return dataReadingStrategy;
        }
    }

    private final class CdmSgridDataset extends CdmGridDataset {
        private Map<String, RangesList> rangesList = null;
        private Map<String, SGridPadding[]> paddings;

        public CdmSgridDataset(String id, String location, Collection<GridVariableMetadata> vars,
                DataReadingStrategy dataReadingStrategy, Map<String, RangesList> rangesList,
                Map<String, SGridPadding[]> paddings) {
            super(id, location, vars, dataReadingStrategy);
            this.rangesList = rangesList;
            this.paddings = paddings;
        }

        @Override
        protected GridDataSource openDataSource() throws DataReadingException {
            NetcdfDataset nc = null;
            try {
                nc = CdmGridDatasetFactory.this.getNetcdfDatasetFromLocation(location, false);
                synchronized (this) {
                    /*
                     * If the getGridDataset method runs concurrently on the
                     * same object, we can get a
                     * ConcurrentModificationException, so we synchronise this
                     * action to avoid the issue.
                     * 
                     * Since we are dealing with an SGRID dataset, we have
                     * explicitly defined which index corresponds to which axis
                     * for the staggered variables
                     */
                    return new CdmSgridDataSource(nc, rangesList, paddings);
                }
            } catch (EdalException | IOException e) {
                if (nc != null) {
                    NetcdfDatasetAggregator.releaseDataset(nc);
                }
                throw new DataReadingException("Problem aggregating datasets", e);
            }
        }
    }

    private static final class CdmSgridDataSource implements GridDataSource {
        private CdmGridDataSource cdmGridDataSource;
        private Map<String, SGridPadding[]> paddings;

        public CdmSgridDataSource(NetcdfDataset nc, Map<String, RangesList> rangesList,
                Map<String, SGridPadding[]> paddings) throws IOException {
            cdmGridDataSource = new CdmGridDataSource(nc, rangesList);
            this.paddings = paddings;
        }

        @SuppressWarnings("incomplete-switch")
        @Override
        public Array4D<Number> read(String variableId, int tmin, int tmax, int zmin, int zmax,
                int ymin, int ymax, int xmin, int xmax) throws IOException, DataReadingException {
            if (variableId.endsWith(UNSTAGGERED_SUFFIX)) {
                /*
                 * We need to average the staggering
                 */
                SGridPadding xPadding = paddings.get(variableId)[0];
                SGridPadding yPadding = paddings.get(variableId)[1];

                /*
                 * First use the staggering information to set new limits
                 */
                switch (xPadding) {
                case NO_PADDING:
                case HIGH:
                    xmin -= 1;
                    break;
                case BOTH:
                case LOW:
                    xmax += 1;
                    break;
                }
                switch (yPadding) {
                case NO_PADDING:
                case HIGH:
                    ymin -= 1;
                    break;
                case BOTH:
                case LOW:
                    ymax += 1;
                    break;
                }

                /*
                 * Then remove the suffix from the variable and read the
                 * required data
                 */
                String origVarId = variableId.replace(UNSTAGGERED_SUFFIX, "");
                final Array4D<Number> origData = cdmGridDataSource.read(origVarId, tmin, tmax,
                        zmin, zmax, ymin, ymax, xmin, xmax);

                /*
                 * Now wrap the result in a 4D array which takes the appropriate
                 * average
                 */
                return new Array4D<Number>(tmax - tmin + 1, zmax - zmin + 1, ymax - ymin + 1, xmax
                        - xmin + 1) {
                    @Override
                    public Number get(int... coords) {
                        int x = coords[3];
                        int y = coords[2];
                        int numDimsToAverage = 0;

                        switch (xPadding) {
                        case NO_PADDING:
                        case HIGH:
                            x -= 1;
                            numDimsToAverage++;
                            break;
                        case BOTH:
                        case LOW:
                            x += 1;
                            numDimsToAverage++;
                            break;
                        }
                        switch (yPadding) {
                        case NO_PADDING:
                        case HIGH:
                            y -= 1;
                            numDimsToAverage++;
                            break;
                        case BOTH:
                        case LOW:
                            y += 1;
                            numDimsToAverage++;
                            break;
                        }
                        if (x < 0 || y < 0 || x >= origData.getXSize() || y >= origData.getYSize()) {
                            return null;
                        }

                        if (numDimsToAverage == 1) {
                            /*
                             * One of the dimensions has an offset - we are
                             * averaging 2 edges
                             */
                            Number v1 = origData.get(coords);
                            Number v2 = origData.get(coords[0], coords[1], y, x);
                            if (v1 == null || v2 == null) {
                                return null;
                            }
                            return (v1.doubleValue() + v2.doubleValue()) / 2.0;
                        } else if (numDimsToAverage == 2) {
                            /*
                             * Both of the dimensions has an offset - we are
                             * averaging 4 faces
                             */
                            Number v1 = origData.get(coords);
                            Number v2 = origData.get(coords[0], coords[1], coords[2], x);
                            Number v3 = origData.get(coords[0], coords[1], y, coords[3]);
                            Number v4 = origData.get(coords[0], coords[1], y, x);
                            if (v1 == null || v2 == null || v3 == null || v4 == null) {
                                return null;
                            }
                            return (v1.doubleValue() + v2.doubleValue() + v3.doubleValue() + v4
                                    .doubleValue()) / 4.0;
                        } else if (numDimsToAverage == 0) {
                            /*
                             * This was marked as a staggered grid, but had both
                             * axes set as NO_OFFSET. We shouldn't get here, but
                             * it's an easy case to handle if we do...
                             */
                            return origData.get(coords);
                        }
                        return null;
                    }

                    @Override
                    public void set(Number value, int... coords) {
                        throw new UnsupportedOperationException();
                    }
                };
            } else {
                return cdmGridDataSource.read(variableId, tmin, tmax, zmin, zmax, ymin, ymax, xmin,
                        xmax);
            }
        }

        @Override
        public void close() throws DataReadingException {
            cdmGridDataSource.close();
        }
    }

    private final class CdmUgridDataset extends HorizontalMesh4dDataset implements Serializable {
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
                nc = CdmGridDatasetFactory.this.getNetcdfDatasetFromLocation(location, false);
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
