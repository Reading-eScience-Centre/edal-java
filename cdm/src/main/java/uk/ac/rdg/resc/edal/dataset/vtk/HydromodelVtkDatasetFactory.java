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

package uk.ac.rdg.resc.edal.dataset.vtk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.DataFormatException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.sis.referencing.CRS;
import org.joda.time.DateTime;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.cdm.RotatedOffsetProjection;
import uk.ac.rdg.resc.edal.domain.HorizontalDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.HorizontalMesh;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxisImpl;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.grid.VerticalAxisImpl;
import uk.ac.rdg.resc.edal.grid.cdm.CdmTransformedGrid;
import uk.ac.rdg.resc.edal.metadata.GridVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.HorizontalMesh4dVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.cdm.CdmUtils;

public class HydromodelVtkDatasetFactory extends DatasetFactory {
    public static final String Z_VAR_ID = "z";

    /**
     * The data extracted from each file. Applies to both Mesh and Grid files.
     */
    private static class FileData {
        HorizontalDomain domain;
        Number[] zVals;
        DateTime time;
        Set<String> vars;
        String varSuffix;

        public FileData(HorizontalDomain domain, Number[] zVals, DateTime time, Set<String> vars,
                String varSuffix) {
            super();
            this.domain = domain;
            this.zVals = zVals;
            this.time = time;
            this.vars = vars;
            this.varSuffix = varSuffix;
        }
    }

    private final XPath xpath;

    public HydromodelVtkDatasetFactory() {
        /*
         * XPath object will be needed for both types
         */
        XPathFactory xPathfactory = XPathFactory.newInstance();
        xpath = xPathfactory.newXPath();
    }

    @Override
    public Dataset createDataset(String id, String location, boolean forceRefresh)
            throws IOException, EdalException {
        List<File> files = CdmUtils.expandGlobExpression(location);

        Map<File, FileData> file2fileData = new HashMap<>();
        Boolean isGrid = null;

        Float fill1 = null;
        Float fill2 = null;
        for (File file : files) {
            /*
             * Parse each file and extract the relevant data
             */
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(file);

                if (fill1 == null) {
                    String fill1Str = xpath.evaluate("VTKFile/@no_data_1", doc);
                    if (fill1Str != null && !fill1Str.isEmpty()) {
                        fill1 = Float.parseFloat(fill1Str);
                    }
                }
                if (fill2 == null) {
                    String fill2Str = xpath.evaluate("VTKFile/@no_data_2", doc);
                    if (fill2Str != null && !fill2Str.isEmpty()) {
                        fill2 = Float.parseFloat(fill2Str);
                    }
                }

                String gridType = xpath.evaluate("VTKFile/@type", doc);

                Node node = (Node) xpath.evaluate("VTKFile/" + gridType, doc, XPathConstants.NODE);
                if (gridType.equals("UnstructuredGrid")) {
                    file2fileData.put(file, processUnstructuredFile(node));
                    if (isGrid == null) {
                        isGrid = false;
                    } else if (isGrid) {
                        throw new DataReadingException("The location: " + location
                                + " refers to a mixture of unstructured and rectlinear grid definitions");
                    }
                } else if (gridType.equals("RectilinearGrid")) {
                    file2fileData.put(file, processRectilinearFile(node));
                    if (isGrid == null) {
                        isGrid = true;
                    } else if (!isGrid) {
                        throw new DataReadingException("The location: " + location
                                + " refers to a mixture of unstructured and rectlinear grid definitions");
                    }
                } else {
                    throw new DataReadingException(
                            "Only \"RectilinearGrid\" and \"UnstructuredGrid\" types are supported for VTK files.  The supplied file: "
                                    + location + " has the type " + gridType);
                }
            } catch (ParserConfigurationException | SAXException | XPathExpressionException
                    | NumberFormatException | DataFormatException | FactoryException e) {
                /*
                 * TODO Might want to handle some of these separately
                 */
                throw new DataReadingException(
                        "Problem reading XML file: " + file.getAbsolutePath(), e);
            }
        }

        /*
         * Set the array of fill values
         */
        float[] fillValues;
        if (fill1 != null) {
            if (fill1.equals(fill2) || fill2 == null) {
                fillValues = new float[] { fill1 };
            } else {
                fillValues = new float[] { fill1, fill2 };
            }
        } else if (fill2 != null) {
            fillValues = new float[] { fill1 };
        } else {
            fillValues = new float[0];
        }

        if (file2fileData.size() == 0) {
            throw new DataReadingException(
                    "The location: " + location + " does not refer to any valid VTK files");
        }

        /*
         * Check that data is consistent across all files
         */
        HorizontalDomain commonHDomain = null;
        Number[] commonZVals = null;
        Set<String> commonVars = null;
        for (FileData fileData : file2fileData.values()) {
            if (commonHDomain == null) {
                commonHDomain = fileData.domain;
            } else if (!commonHDomain.equals(fileData.domain)) {
                throw new DataReadingException(
                        "Files at " + location + " must all share the same horizontal domain");
            }

            if (commonZVals == null) {
                commonZVals = fileData.zVals;
            } else if (!Arrays.equals(commonZVals, fileData.zVals)) {
                throw new DataReadingException("Files at " + location
                        + " must all share the same domain (z domain differs)");
            }

            if (commonVars == null) {
                commonVars = fileData.vars;
            } else if (!commonVars.equals(fileData.vars)) {
                throw new DataReadingException(
                        "Files at " + location + " contain different variables.");
            }
        }

        List<DateTime> tVals = new ArrayList<>();
        AtomicInteger t = new AtomicInteger(0);
        TimestepInfo[] timestepsInfo = new TimestepInfo[file2fileData.size()];

        /*
         * Sort the values by time, check they have consistent data, and create
         * array of TimestepInfo for appropriate DataSources
         */
        file2fileData.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(new Comparator<FileData>() {
                    @Override
                    public int compare(FileData o1, FileData o2) {
                        /*
                         * Times cannot be null
                         */
                        return o1.time.compareTo(o2.time);
                    }
                })).forEach(entry -> {
                    FileData fileData = entry.getValue();

                    tVals.add(fileData.time);
                    timestepsInfo[t.getAndIncrement()] = new TimestepInfo(entry.getKey(),
                            fileData.varSuffix, fillValues);
                });

        TimeAxis tAxis = new TimeAxisImpl("time", tVals);

        if (isGrid) {
            /*
             * Now create metadata for all of the other variables
             */
            List<GridVariableMetadata> metadata = new ArrayList<>();
            for (String varId : commonVars) {
                GridVariableMetadata meta = new GridVariableMetadata(
                        new Parameter(varId, varId, "", "", ""), (HorizontalGrid) commonHDomain,
                        commonZVals == null ? null
                                : new VerticalAxisImpl("zAxis",
                                        VtkUtils.numberArrayToDoubleList(commonZVals),
                                        new VerticalCrsImpl("", false, true, true)),
                        tAxis, true);
                metadata.add(meta);
            }

            return new HydromodelVtkGridDataset(id, metadata, timestepsInfo);
        } else {
            /*
             * Add metadata for the elevation variable (which we will store in
             * memory)
             */
            List<HorizontalMesh4dVariableMetadata> metadata = new ArrayList<>();
            HorizontalMesh4dVariableMetadata zMeta = new HorizontalMesh4dVariableMetadata(
                    new Parameter(Z_VAR_ID, "Elevation", "", "m", "elevation"),
                    (HorizontalMesh) commonHDomain, null, null, true);
            metadata.add(zMeta);

            /*
             * Now create metadata for all of the other variables (which will be
             * read when required)
             */
            for (String varId : commonVars) {
                HorizontalMesh4dVariableMetadata meta = new HorizontalMesh4dVariableMetadata(
                        new Parameter(varId, varId, "", "", ""), (HorizontalMesh) commonHDomain,
                        null, tAxis, true);
                metadata.add(meta);
            }

            return new HydromodelVtkUnstructuredDataset(id, metadata, timestepsInfo, commonZVals);
        }
    }

    private FileData processUnstructuredFile(Node node) throws NumberFormatException,
            XPathExpressionException, DataFormatException, IOException, FactoryException {
        /*
         * First we get the CRS for the mesh
         */
        String wkt = xpath.evaluate("Projection", node);
        CoordinateReferenceSystem crs = CRS.fromWKT(wkt);

        /*
         * Now we construct the horizontal domain
         */
        Node verticesNode = (Node) xpath.evaluate("Piece/Points/DataArray", node,
                XPathConstants.NODE);

        Number[] verticesData = VtkUtils.parseDataArray(verticesNode, xpath);

        /*
         * Vertices are grouped into 3s - x,y,z. Extract the (x,y)s as
         * positions, and the zs as a separate array (to expose as elevation
         * data)
         */
        List<HorizontalPosition> positions = new ArrayList<>();
        Number[] zVals = new Number[verticesData.length / 3];
        int zi = 0;
        for (int i = 0; i < verticesData.length; i += 3) {
            HorizontalPosition pos = new HorizontalPosition(verticesData[i].doubleValue(),
                    verticesData[i + 1].doubleValue(), crs);
            /*
             * Convert to default CRS. Most operations are done in the default
             * CRS, so this will speed things up a great deal.
             */
            positions.add(GISUtils.transformPosition(pos, GISUtils.defaultGeographicCRS()));
            zVals[zi++] = verticesData[i + 2];
        }

        /*
         * Parse the offsets. These are the offsets within the connections list
         * where each cell starts
         */
        String offsetsStr = xpath.evaluate("Piece/Cells/DataArray[@Name='offsets']", node);
        String[] offsetsStrs = offsetsStr.split(" ");
        int[] offsets = new int[offsetsStrs.length];
        for (int i = 0; i < offsetsStrs.length; i++) {
            offsets[i] = Integer.parseInt(offsetsStrs[i].trim());
        }

        /*
         * Now parse the connections between nodes
         */
        String connectionsStr = xpath.evaluate("Piece/Cells/DataArray[@Name='connectivity']", node);
        String[] connectionsSplit = connectionsStr.split(" ");

        int start = 0;
        int ci = 0;
        List<int[]> connections = new ArrayList<>();
        for (int i = 0; i < offsets.length; i++) {
            int nConnections = offsets[i] - start;
            int[] nodeConnections = new int[nConnections];
            for (int j = 0; j < nConnections; j++) {
                nodeConnections[j] = Integer.parseInt(connectionsSplit[ci++].trim());
            }
            connections.add(nodeConnections);
            start = offsets[i];
        }

        /*
         * Finally create the horizontal domain
         */
        HorizontalMesh mesh = HorizontalMesh.fromConnections(positions, connections, 0);

        /*
         * Now parse the list of variables to determine timesteps and available
         * variables
         */
        NodeList vars = (NodeList) xpath.evaluate("Piece/PointData/DataArray", node,
                XPathConstants.NODESET);

        return parseVariableData(mesh, zVals, vars);
    }

    private FileData processRectilinearFile(Node node)
            throws XPathExpressionException, FactoryException, DataFormatException, IOException {
        NodeList coords = (NodeList) xpath.evaluate("Piece/Coordinates/DataArray", node,
                XPathConstants.NODESET);
        if (coords.getLength() != 3) {
            throw new DataReadingException(
                    "Coordinates element must contain 3 DataArrays - x, y, and z");
        }

        /*
         * Read the axis values. These are supplied as bounds, where the actual
         * positions are midway between each pair of bounds.
         */
        Number[] xBounds = VtkUtils.parseDataArray(coords.item(0), xpath);
        Number[] xVals = new Number[xBounds.length - 1];
        for (int i = 0; i < xVals.length; i++) {
            xVals[i] = (xBounds[i].doubleValue() + xBounds[i + 1].doubleValue()) / 2;
        }
        Number[] yBounds = VtkUtils.parseDataArray(coords.item(1), xpath);
        Number[] yVals = new Number[yBounds.length - 1];
        for (int i = 0; i < yVals.length; i++) {
            yVals[i] = (yBounds[i].doubleValue() + yBounds[i + 1].doubleValue()) / 2;
        }
        Number[] zBounds = VtkUtils.parseDataArray(coords.item(2), xpath);
        Number[] zVals = null;
        /*
         * The Hydromodel data is supposed to ignore z values as they are
         * meaningless, and each file will only contain 1 z coordinate (i.e. 2
         * bounds). This is essentially future-proofing it, in the case where
         * there *are* multiple z coordinates.
         */
        if (zBounds.length > 2) {
            zVals = new Number[zBounds.length - 1];
            for (int i = 0; i < zVals.length; i++) {
                zVals[i] = (zBounds[i].doubleValue() + zBounds[i + 1].doubleValue()) / 2;
            }
        }

        ReferenceableAxisImpl xAxis = new ReferenceableAxisImpl("x",
                VtkUtils.numberArrayToDoubleList(xVals), false);
        ReferenceableAxisImpl yAxis = new ReferenceableAxisImpl("y",
                VtkUtils.numberArrayToDoubleList(yVals), false);

        /*
         * Grid positions in Rectilinear VTK files need to be rotated and offset
         * to get them into a particular projection, defined by a WKT string.
         */
        String wkt = xpath.evaluate("Projection", node);
        CoordinateReferenceSystem crs = CRS.fromWKT(wkt);

        double xOff = Double.parseDouble(xpath.evaluate("Projection/@origin_x", node));
        double yOff = Double.parseDouble(xpath.evaluate("Projection/@origin_y", node));
        double angle = Double.parseDouble(xpath.evaluate("Projection/@angle", node));

        CdmTransformedGrid hGrid = new CdmTransformedGrid(
                new RotatedOffsetProjection(xOff, yOff, angle, crs), xAxis, yAxis);

        /*
         * Now parse the list of variables to determine timesteps and available
         * variables
         */
        NodeList vars = (NodeList) xpath.evaluate("Piece/CellData/DataArray", node,
                XPathConstants.NODESET);

        return parseVariableData(hGrid, zVals, vars);
    }

    private FileData parseVariableData(HorizontalDomain hDomain, Number[] zVals, NodeList varNodes)
            throws XPathExpressionException {
        Set<String> varIds = new LinkedHashSet<>();
        DateTime time = null;
        String varSuffix = null;
        for (int i = 0; i < varNodes.getLength(); i++) {
            Node var = varNodes.item(i);
            String varIdWithTime = xpath.evaluate("@Name", var);

            String varId = varIdWithTime.split(",")[0];
            varIds.add(varId);
            String[] split = varIdWithTime.split("_");
            String timeStr = split[split.length - 1];

            DateTime varTime = VtkUtils.dateTimeFromOLEAutomationString(timeStr);
            if (varTime == null) {
                throw new DataReadingException(timeStr
                        + " does not define a valid time - all variables must have a timestep defined.");
            }
            if (time == null) {
                time = varTime;
            } else {
                if (!time.equals(varTime)) {
                    throw new DataReadingException(
                            "The variable " + varId + " has the time value " + varTime
                                    + ", which is different to another variable in the same file");
                }
            }
            if (varSuffix == null) {
                varSuffix = "," + varIdWithTime.split(",")[1];
            } else {
                if (!varSuffix.equals("," + varIdWithTime.split(",")[1])) {
                    throw new DataReadingException(
                            "The variable " + varId + " has the time string " + varSuffix
                                    + ", which is different to another variable in the same file");
                }
            }
        }

        return new FileData(hDomain, zVals, time, varIds, varSuffix);
    }

    public static final class TimestepInfo {
        final File file;
        final String varSuffix;
        final float[] fillValues;

        public TimestepInfo(File file, String varSuffix, float... fillValues) {
            super();
            this.file = file;
            this.varSuffix = varSuffix;
            this.fillValues = fillValues;
        }
    }
}
