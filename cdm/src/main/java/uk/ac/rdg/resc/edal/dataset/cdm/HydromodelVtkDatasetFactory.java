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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
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
import uk.ac.rdg.resc.edal.grid.HorizontalMesh;
import uk.ac.rdg.resc.edal.metadata.HorizontalMesh4dVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

public class HydromodelVtkDatasetFactory extends DatasetFactory {

    @Override
    public Dataset createDataset(String id, String location) throws IOException, EdalException {
        try {
            File vtkXmlFile = new File(location);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(vtkXmlFile);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            /*
             * Parse the grid
             */
            String coords = xpath
                    .compile("VTKlist/VTKFile/UnstructuredGrid/Piece/Points/DataArray").evaluate(
                            doc);
            /*
             * Check if they're 2d / 3d points using attribute Necessary? Or
             * just use first 2?
             */
            String[] coordList = coords.split("\\n");
            List<HorizontalPosition> positions = new ArrayList<>();
            for (String coord : coordList) {
                String[] coordParts = coord.split("\\s+");
                positions.add(new HorizontalPosition(Double.parseDouble(coordParts[1]), Double
                        .parseDouble(coordParts[0])));
            }

            /*
             * Read the connectivity of the grid - needed to calculate the
             * boundary of the domain
             */
            NodeList connectionsNodes = (NodeList) xpath.compile(
                    "VTKlist/VTKFile/UnstructuredGrid/Piece/Cells/DataArray").evaluate(doc,
                    XPathConstants.NODESET);
            List<int[]> connections = new ArrayList<>();
            for (int i = 0; i < connectionsNodes.getLength(); i++) {
                Node variableData = connectionsNodes.item(i);
                if ("connectivity".equals(variableData.getAttributes().getNamedItem("Name")
                        .getNodeValue())) {
                    String[] links = variableData.getTextContent().split(" ");
                    for (int j = 0; j < links.length; j += 3) {
                        connections.add(new int[] { Integer.parseInt(links[j].trim()),
                                Integer.parseInt(links[j + 1].trim()),
                                Integer.parseInt(links[j + 2].trim()) });
                    }
                }
            }

            /*
             * Create the domain of the dataset
             */
            HorizontalMesh grid = HorizontalMesh.fromConnections(positions, connections, 0);

            /*
             * Read all of the value arrays.
             */
            NodeList values = (NodeList) xpath.compile(
                    "VTKlist/VTKFile/UnstructuredGrid/Piece/PointData/DataArray").evaluate(doc,
                    XPathConstants.NODESET);
            Set<HorizontalMesh4dVariableMetadata> vars = new HashSet<>();

            Map<String, Number[][][]> dataMap = new HashMap<>();
            for (int i = 0; i < values.getLength(); i++) {
                Number[][][] dataValues = new Number[1][1][positions.size()];

                Node variableData = values.item(i);
                String varId = variableData.getAttributes().getNamedItem("Name").getNodeValue();
                /*
                 * Cannot have commas in the variable name
                 * 
                 * TODO fix this at the Parameter level
                 */
                varId = varId.replace(",", ":");
                vars.add(new HorizontalMesh4dVariableMetadata(new Parameter(varId, "Variable",
                        "This is a test variable", "ppm", ""), grid, null, null, true));

                /*
                 * The data is a base 64 encoded string...
                 */
                String binaryData = variableData.getTextContent().substring(24);
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
                 * ...each set of 4 of which represent a 32-bit float stored in
                 * little endian form.
                 */
                int vals = 0;
                for (int j = 0; j < output.length; j += 4) {
                    byte[] valBytes = { output[j], output[j + 1], output[j + 2], output[j + 3] };
                    float value = ByteBuffer.wrap(valBytes).order(ByteOrder.LITTLE_ENDIAN)
                            .getFloat();
                    dataValues[0][0][vals] = value;
                    vals++;
                }
                dataMap.put(varId, dataValues);
            }
            return new HydromodelVtkDataset(id, vars, dataMap);
        } catch (XPathException | ParserConfigurationException | SAXException | DataFormatException e) {
            e.printStackTrace();
            throw new DataReadingException("Can't parse XML", e);
        }
    }

    /**
     * In-memory implementation of a {@link HorizontalMesh4dDataset} to read the
     * hydromodel VTK format
     *
     * @author Guy Griffiths
     */
    private static final class HydromodelVtkDataset extends HorizontalMesh4dDataset {
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
