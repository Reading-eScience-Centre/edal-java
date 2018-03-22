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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.w3c.dom.Node;

public class VtkUtils {
    /**
     * Parses a <DataArray> node and returns an array of {@link Number}s of the
     * correct type (specified by the "type" attribute).
     * 
     * This assumes that binary data is compressed with zlib, and uses Little
     * Endian byte order.
     * 
     * Supports "ascii" and "binary" formats, and "Float32" and "Int32" data
     * types.
     * 
     * @param dataArrayNode
     *            The {@link Node} in the DOM
     * @param xpath
     *            An {@link XPath} object to evaluate expressions with. This
     *            will have already been created when finding the {@link Node},
     *            so there is no point instantiating a new one for this class
     * @return An array of {@link Number}s
     * @throws DataFormatException
     * @throws IOException
     * @throws XPathExpressionException
     */
    public static Number[] parseDataArray(Node dataArrayNode, XPath xpath)
            throws XPathExpressionException, DataFormatException, IOException {
        return parseDataArray(dataArrayNode, xpath, new float[0]);
    }

    /**
     * Parses a <DataArray> node and returns an array of {@link Number}s of the
     * correct type (specified by the "type" attribute).
     * 
     * This assumes that binary data is compressed with zlib, and uses Little
     * Endian byte order.
     * 
     * Supports "ascii" and "binary" formats, and "Float32" and "Int32" data
     * types.
     * 
     * @param dataArrayNode
     *            The {@link Node} in the DOM
     * @param xpath
     *            An {@link XPath} object to evaluate expressions with. This
     *            will have already been created when finding the {@link Node},
     *            so there is no point instantiating a new one for this class
     * @param fillVals
     *            An array of values which are considered to be fill values.
     *            These will be returned as <code>null</code>s.
     * @return An array of {@link Number}s
     * @throws DataFormatException
     * @throws IOException
     * @throws XPathExpressionException
     */
    public static Number[] parseDataArray(Node dataArrayNode, XPath xpath, float[] fillVals)
            throws DataFormatException, IOException, XPathExpressionException {
        String data = dataArrayNode.getTextContent();

        String format = xpath.evaluate("@format", dataArrayNode).trim();

        String type = xpath.evaluate("@type", dataArrayNode).trim();

        return parseDataString(data, format, type, fillVals);
    }

    public static Number[] parseDataString(String data, String format, String type,
            float[] fillVals) throws DataFormatException, IOException {
        final boolean float32;
        if (type.equalsIgnoreCase("Float32")) {
            float32 = true;
        } else if (type.equalsIgnoreCase("Int32")) {
            float32 = false;
        } else {
            throw new DataFormatException(
                    "Currently only \"Float32\" and \"Int32\" data types are supported");
        }
        if (format.equalsIgnoreCase("binary")) {
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

            byte[] output;
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
                    decodeBase64.length)) {
                byte[] buffer = new byte[1024];
                while (!decompresser.finished()) {
                    int count = decompresser.inflate(buffer);
                    outputStream.write(buffer, 0, count);
                }
                output = outputStream.toByteArray();
            }
            
            /*
             * ...each set of 4 of which represent a 32-bit float stored in
             * little endian form.
             */
            int vals = 0;
            Number[] values = new Number[output.length / 4];
            for (int i = 0; i < output.length; i += 4) {
                byte[] valBytes = { output[i], output[i + 1], output[i + 2], output[i + 3] };
                ByteBuffer value = ByteBuffer.wrap(valBytes).order(ByteOrder.LITTLE_ENDIAN);
                Float floatVal = value.getFloat();
                for (float fill : fillVals) {
                    if (fill == floatVal.floatValue()) {
                        floatVal = Float.NaN;
                        break;
                    }
                }
                values[vals++] = floatVal;
            }

            return values;
        } else if (format.equalsIgnoreCase("ascii")) {
            String[] dataParts = data.split(" ");
            Number[] values = new Number[dataParts.length];
            for (int i = 0; i < values.length; i++) {
                /*
                 * If we add support for more data types, change this to a
                 * switch
                 */
                if (float32) {
                    values[i] = Float.parseFloat(dataParts[i]);
                } else {
                    values[i] = Integer.parseInt(dataParts[i]);
                }
            }
            return values;
        } else {
            throw new DataFormatException(
                    "Can only process DataArrays with the format \"binary\" or \"ascii\"");
        }
    }

    public static DateTime dateTimeFromOLEAutomationString(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        /*
         * Convert from OLE Automation Date. This takes into account the fact
         * that MS treat 1900 as a leap year in that calculation.
         * 
         * See: https://stackoverflow.com/questions/10443325/how-to-convert-ole-
         * automation-date-to-readable-format-using-javascript
         */
        return new DateTime((Long.parseLong(dateStr) - 25569) * 24 * 3600 * 1000);
    }

    public static List<Double> numberArrayToDoubleList(Number[] na) {
        List<Double> ret = new ArrayList<>();
        for (Number n : na) {
            if (n == null) {
                ret.add(null);
            } else {
                ret.add(n.doubleValue());
            }
        }
        return ret;
    }
}
