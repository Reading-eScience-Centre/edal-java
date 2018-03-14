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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
import org.geotoolkit.referencing.CRS;
import org.joda.time.DateTime;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.HZTDataSource;
import uk.ac.rdg.resc.edal.dataset.HorizontalMesh4dDataset;
import uk.ac.rdg.resc.edal.dataset.InMemoryMeshDataSource;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.HorizontalMesh;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.metadata.HorizontalMesh4dVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.cdm.CdmUtils;

public class HydromodelVtkDatasetFactory extends DatasetFactory {

    private static class MeshFileData {
        HorizontalMesh mesh;
        float[] zVals;
        DateTime time;
        Map<String, float[]> varData;

        public MeshFileData(HorizontalMesh mesh, float[] zVals, DateTime time,
                Map<String, float[]> varData) {
            super();
            this.mesh = mesh;
            this.zVals = zVals;
            this.time = time;
            this.varData = varData;
        }
    }

    private static class GridFileData {
        HorizontalGrid grid;
        VerticalAxis zAxis;
        DateTime time;
        Map<String, float[][][]> varData;

        public GridFileData(HorizontalGrid grid, VerticalAxis zAxis, DateTime time,
                Map<String, float[][][]> varData) {
            super();
            this.grid = grid;
            this.zAxis = zAxis;
            this.time = time;
            this.varData = varData;
        }
    }

    public static void main(String[] args) throws EdalException, IOException {
        HydromodelVtkDatasetFactory f = new HydromodelVtkDatasetFactory();
        f.createDataset("hh", "/home/guy/Data/hh/UnstructuredGrid/PP_Layer1_nivel-sim_1.xml");
    }

    private final XPath xpath;

    public HydromodelVtkDatasetFactory() {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        xpath = xPathfactory.newXPath();
    }

    @Override
    public Dataset createDataset(String id, String location, boolean forceRefresh)
            throws IOException, EdalException {
        List<File> files = CdmUtils.expandGlobExpression(location);

        /*
         * We don't know which type of dataset this will be, so we store lists
         * for both types.
         * 
         * If they both get populated, throw an error.
         */
        List<MeshFileData> meshData = new ArrayList<>();
        List<GridFileData> gridData = new ArrayList<>();
        for (File file : files) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(file);

                String gridType = xpath.evaluate("VTKFile/@type", doc);

                Node node = (Node) xpath.evaluate("VTKFile/" + gridType, doc, XPathConstants.NODE);
                if (gridType.equals("UnstructuredGrid")) {
                    meshData.add(processUnstructuredFile(node));
                } else if (gridType.equals("RectlinearGrid")) {
                    gridData.add(createRectilinearDataset(node));
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
         * Now that all files have been processed, construct the appropriate
         * dataset
         */
        if (meshData.size() > 0 && gridData.size() > 0) {
            throw new DataReadingException("The location: " + location
                    + " refers to a mixture of unstructured and rectlinear grid definitions");
        } else if (meshData.size() == 0 && gridData.size() == 0) {
            throw new DataReadingException(
                    "The location: " + location + " does not refer to any valid VTK files");
        }

        if (meshData.size() > 0) {
            HorizontalMesh commonMesh = null;
            float[] commonZVals = null;
            Set<String> commonVars = null;
            /*
             * Check that all files contain consistent data
             */
            for (MeshFileData fileData : meshData) {
                if (commonMesh == null) {
                    commonMesh = fileData.mesh;
                } else if (!commonMesh.equals(fileData.mesh)) {
                    throw new DataReadingException(
                            "Files at " + location + " must all share the same mesh");
                }

                if (commonZVals == null) {
                    commonZVals = fileData.zVals;
                } else {
                    if (commonZVals.length != fileData.zVals.length) {
                        throw new DataReadingException("Files at " + location
                                + " must all share the same domain (size of z domain differs)");
                    }
                    for (int i = 0; i < commonZVals.length; i++) {
                        if (commonZVals[i] != fileData.zVals[i]) {
                            throw new DataReadingException("Files at " + location
                                    + " must all share the same domain (z domain differs)");
                        }
                    }
                }
                if (commonVars == null) {
                    commonVars = fileData.varData.keySet();
                } else {
                    if (!commonVars.equals(fileData.varData.keySet())) {
                        throw new DataReadingException(
                                "Files at " + location + " contain different variables.");
                    }
                }
            }

            /*
             * Now construct the dataset
             */

            /*
             * Sort by time
             */
            Collections.sort(meshData, new Comparator<MeshFileData>() {
                @Override
                public int compare(MeshFileData o1, MeshFileData o2) {
                    if (o1.time == null) {
                        /*
                         * This means we only have one file with no time stamp
                         * 
                         * TODO check what happens with multiple no time files.
                         */
                        return 0;
                    }
                    return o1.time.compareTo(o2.time);
                }
            });

            List<DateTime> tVals = new ArrayList<>();
            Map<String, Number[][][]> var2data = new HashMap<>();
            int t = 0;
            for (MeshFileData fileData : meshData) {
                tVals.add(fileData.time);
                Map<String, float[]> varData = fileData.varData;

                for (Entry<String, float[]> data : varData.entrySet()) {
                    if (!var2data.containsKey(data.getKey())) {
                        var2data.put(data.getKey(),
                                new Number[meshData.size()][1][data.getValue().length]);
                    }
                    float[] timestepValues = data.getValue();
                    for (int i = 0; i < timestepValues.length; i++) {
                        var2data.get(data.getKey())[t][0][i] = timestepValues[i];
                    }
                }
                t++;
            }

            List<HorizontalMesh4dVariableMetadata> metadata = new ArrayList<>();
            HorizontalMesh4dVariableMetadata zMeta = new HorizontalMesh4dVariableMetadata(
                    new Parameter("z", "Elevation", "", "m", "elevation"), commonMesh, null, null,
                    true);
            metadata.add(zMeta);

            TimeAxis tAxis = new TimeAxisImpl("time", tVals);
            for (String varId : var2data.keySet()) {
                HorizontalMesh4dVariableMetadata meta = new HorizontalMesh4dVariableMetadata(
                        new Parameter(varId, varId, "", "", ""), commonMesh, null, tAxis, true);
                metadata.add(meta);
            }

            return new HydromodelVtkDataset(id, metadata, var2data);
        } else {
            return null;
        }
    }

    private MeshFileData processUnstructuredFile(Node node) throws NumberFormatException,
            XPathExpressionException, DataFormatException, IOException, FactoryException {
        String vertices = xpath.evaluate("Piece/Points/DataArray", node);
        String wkt = xpath.evaluate("Projection", node);
        CoordinateReferenceSystem crs = CRS.parseWKT(wkt);

        float[] binaryData = parseBinaryData(vertices);

        List<HorizontalPosition> positions = new ArrayList<>();
        float[] zVals = new float[binaryData.length / 3];
        int zi = 0;
        for (int i = 0; i < binaryData.length; i += 3) {
            HorizontalPosition pos = new HorizontalPosition(binaryData[i], binaryData[i + 1], crs);
            /*
             * Convert to default CRS. Most operations are done in the default
             * CRS, so this will speed things up a great deal.
             */
            positions.add(GISUtils.transformPosition(pos, GISUtils.defaultGeographicCRS()));
            zVals[zi++] = binaryData[i + 2];
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

        HorizontalMesh mesh = HorizontalMesh.fromConnections(positions, connections, 0);

        NodeList vars = (NodeList) xpath.evaluate("Piece/PointData/DataArray", node,
                XPathConstants.NODESET);
        /*
         * TODO This could be factored out, since it will be the same for the
         * CellArray part of rectilinear grids
         */
        Map<String, float[]> var2Data = new HashMap<>();
        DateTime time = null;
        for (int i = 0; i < vars.getLength(); i++) {
            Node var = vars.item(i);
            String varIdWithTime = xpath.evaluate("@Name", var);
            String varId = varIdWithTime.split(",")[0];

            String[] split = varIdWithTime.split("_");
            /*
             * TODO deal with no time value
             */
            /*
             * Convert to OLE Automation Date. This takes into account the fact
             * that MS treat 1900 as a leap year in that calculation.
             * 
             * See:
             * https://stackoverflow.com/questions/10443325/how-to-convert-ole-
             * automation-date-to-readable-format-using-javascript
             */
            DateTime varTime = new DateTime(
                    (Long.parseLong(split[split.length - 1]) - 25569) * 24 * 3600 * 1000);
            if (time == null) {
                time = varTime;
            } else {
                if (!time.equals(varTime)) {
                    throw new DataReadingException(
                            "The variable " + varId + " has the time value " + varTime
                                    + ", which is different to another variable in the same file");
                }
            }

            String format = xpath.evaluate("@format", var).trim();
            if (!format.equals("binary")) {
                throw new DataReadingException("Expecting binary data for the variable " + varId
                        + ", but got data of the format: " + format);
            }
            String varDataStr = var.getTextContent();
            float[] varData = parseBinaryData(varDataStr);
            var2Data.put(varId, varData);
        }

        return new MeshFileData(mesh, zVals, time, var2Data);
    }

    private float[] parseBinaryData(String data) throws DataFormatException, IOException {
        /*
         * Ignore the header
         */
        String binaryData = data.substring(24);
        byte[] decodeBase64 = Base64.decodeBase64(binaryData);

        /*
         * ...which represents bytes compressed by zlib...
         */
        Inflater decompresser = new Inflater();
        decompresser.setInput(decodeBase64);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(decodeBase64.length);
        byte[] buffer = new byte[1024];
        while (!decompresser.finished()) {
            int count = decompresser.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();

        /*
         * ...each set of 4 of which represent a 32-bit float stored in little
         * endian form.
         */
        int vals = 0;
        float[] values = new float[output.length / 4];
        for (int j = 0; j < output.length; j += 4) {
            byte[] valBytes = { output[j], output[j + 1], output[j + 2], output[j + 3] };
            values[vals++] = ByteBuffer.wrap(valBytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        }

        return values;
    }

    private GridFileData createRectilinearDataset(Node node) {
        // TODO Auto-generated method stub
        return null;
    }

//            /*
//             * Parse the grid
//             */
//            String coords = xpath
//                    .compile("VTKlist/VTKFile/UnstructuredGrid/Piece/Points/DataArray").evaluate(
//                            doc);
//            /*
//             * Check if they're 2d / 3d points using attribute Necessary? Or
//             * just use first 2?
//             */
//            String[] coordList = coords.split("\\n");
//            List<HorizontalPosition> positions = new ArrayList<>();
//            for (String coord : coordList) {
//                String[] coordParts = coord.split("\\s+");
//                positions.add(new HorizontalPosition(Double.parseDouble(coordParts[1]), Double
//                        .parseDouble(coordParts[0])));
//            }
//
//            /*
//             * Read the connectivity of the grid - needed to calculate the
//             * boundary of the domain
//             */
//            NodeList connectionsNodes = (NodeList) xpath.compile(
//                    "VTKlist/VTKFile/UnstructuredGrid/Piece/Cells/DataArray").evaluate(doc,
//                    XPathConstants.NODESET);
//            List<int[]> connections = new ArrayList<>();
//            for (int i = 0; i < connectionsNodes.getLength(); i++) {
//                Node variableData = connectionsNodes.item(i);
//                if ("connectivity".equals(variableData.getAttributes().getNamedItem("Name")
//                        .getNodeValue())) {
//                    String[] links = variableData.getTextContent().split(" ");
//                    for (int j = 0; j < links.length; j += 3) {
//                        connections.add(new int[] { Integer.parseInt(links[j].trim()),
//                                Integer.parseInt(links[j + 1].trim()),
//                                Integer.parseInt(links[j + 2].trim()) });
//                    }
//                }
//            }
//
//            /*
//             * Create the domain of the dataset
//             */
//            HorizontalMesh grid = HorizontalMesh.fromConnections(positions, connections, 0);
//
//            /*
//             * Read all of the value arrays.
//             */
//            NodeList values = (NodeList) xpath.compile(
//                    "VTKlist/VTKFile/UnstructuredGrid/Piece/PointData/DataArray").evaluate(doc,
//                    XPathConstants.NODESET);
//            Set<HorizontalMesh4dVariableMetadata> vars = new HashSet<>();
//
//            Map<String, Number[][][]> dataMap = new HashMap<>();
//            for (int i = 0; i < values.getLength(); i++) {
//                Number[][][] dataValues = new Number[1][1][positions.size()];
//
//                Node variableData = values.item(i);
//                String varId = variableData.getAttributes().getNamedItem("Name").getNodeValue();
//                /*
//                 * Cannot have commas in the variable name
//                 * 
//                 * TODO fix this at the Parameter level
//                 */
//                varId = varId.replace(",", ":");
//                vars.add(new HorizontalMesh4dVariableMetadata(new Parameter(varId, "Variable",
//                        "This is a test variable", "ppm", ""), grid, null, null, true));
//
//                /*
//                 * The data is a base 64 encoded string...
//                 */
//                String binaryData = variableData.getTextContent().substring(24);
//                byte[] decodeBase64 = Base64.decodeBase64(binaryData);
//
//                /*
//                 * ...which represents bytes compressed by zlib...
//                 */
//                Inflater decompresser = new Inflater();
//                decompresser.setInput(decodeBase64);
//
//                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(decodeBase64.length);
//                byte[] buffer = new byte[1024];
//                while (!decompresser.finished()) {
//                    int count = decompresser.inflate(buffer);
//                    outputStream.write(buffer, 0, count);
//                }
//                outputStream.close();
//                byte[] output = outputStream.toByteArray();
//
//                /*
//                 * ...each set of 4 of which represent a 32-bit float stored in
//                 * little endian form.
//                 */
//                int vals = 0;
//                for (int j = 0; j < output.length; j += 4) {
//                    byte[] valBytes = { output[j], output[j + 1], output[j + 2], output[j + 3] };
//                    float value = ByteBuffer.wrap(valBytes).order(ByteOrder.LITTLE_ENDIAN)
//                            .getFloat();
//                    dataValues[0][0][vals] = value;
//                    vals++;
//                }
//                dataMap.put(varId, dataValues);
//            }
//            return new HydromodelVtkDataset(id, vars, dataMap);
//        } catch (XPathException | ParserConfigurationException | SAXException | DataFormatException e) {

    /**
     * In-memory implementation of a {@link HorizontalMesh4dDataset} to read the
     * hydromodel VTK format
     *
     * @author Guy Griffiths
     */
    private static final class HydromodelVtkDataset extends HorizontalMesh4dDataset {
        private static final long serialVersionUID = 1L;
        private final InMemoryMeshDataSource dataSource;

        public HydromodelVtkDataset(String id, Collection<HorizontalMesh4dVariableMetadata> vars,
                Map<String, Number[][][]> data) {
            super(id, vars);
            dataSource = new InMemoryMeshDataSource(data);
        }

        @Override
        protected HZTDataSource openDataSource() throws DataReadingException {
            return dataSource;
        }
    }
}
