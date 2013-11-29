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

package uk.ac.rdg.resc.edal.cdm;

import java.io.IOException;
import java.util.ArrayList;

import org.joda.time.DateTime;

import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

/**
 * This class is not part of the test suite, but may be run to generate the test
 * data used. It is difficult to get the src/test/resources directory in a
 * portable way, so this class doesn't attempt this. Test data should generally
 * only need generating once anyway.
 */
public class CreateNetCDF {
    public static void main(String args[]) {
        /*
         * We are writing 4D data, a 6 x 12 grid.
         */
        final int NLON = 36;
        final int NLAT = 19;
        final int NZ = 11;
        final int NT = 10;
        String filename = "test.nc";
        NetcdfFileWriteable dataFile = null;
        try {
            dataFile = NetcdfFileWriteable.createNew(filename, false);
            ArrayList<Dimension> dims = new ArrayList<Dimension>();
            Dimension latDim = dataFile.addDimension("latitude", NLAT);
            Dimension lonDim = dataFile.addDimension("longitude", NLON);
            Dimension depthDim = dataFile.addDimension("depth", NZ);
            Dimension timeDim = dataFile.addDimension("time", NT);
            /*
             * Define dimensions
             */
            dims.add(timeDim);
            dims.add(depthDim);
            dims.add(latDim);
            dims.add(lonDim);

            dataFile.addVariable("latitude", DataType.FLOAT, new Dimension[] { latDim });
            dataFile.addVariable("longitude", DataType.FLOAT, new Dimension[] { lonDim });
            dataFile.addVariable("depth", DataType.FLOAT, new Dimension[] { depthDim });
            dataFile.addVariable("time", DataType.INT, new Dimension[] { timeDim });
            /*
             * Define units attributes for coordinate vars.
             */
            dataFile.addVariableAttribute("longitude", "units", "degrees_east");
            dataFile.addVariableAttribute("longitude", "long_name", "longitude");
            dataFile.addVariableAttribute("longitude", "standard_name", "longitude");

            dataFile.addVariableAttribute("latitude", "units", "degrees_north");
            dataFile.addVariableAttribute("latitude", "long_name", "latitude");
            dataFile.addVariableAttribute("latitude", "standard_name", "latitude");

            dataFile.addVariableAttribute("depth", "units", "m");
            dataFile.addVariableAttribute("depth", "positive", "down");
            dataFile.addVariableAttribute("depth", "long_name", "depth");
            dataFile.addVariableAttribute("depth", "standard_name", "depth");

            dataFile.addVariableAttribute("time", "standard_name", "time");
            dataFile.addVariableAttribute("time", "units", "seconds since 1970-01-01 00:00:00");
            dataFile.addVariableAttribute("time", "calendar", "gregorian");

            /*
             * Add some 4D variables. These will just vary along one dimension
             * each for testing purposes
             */
            dataFile.addVariable("vLon", DataType.FLOAT, dims);
            dataFile.addVariable("vLat", DataType.FLOAT, dims);
            dataFile.addVariable("vDepth", DataType.FLOAT, dims);
            dataFile.addVariable("vTime", DataType.FLOAT, dims);

            dataFile.addVariable("northNComp", DataType.FLOAT, dims);
            dataFile.addVariable("northEComp", DataType.FLOAT, dims);
            dataFile.addVariableAttribute("northNComp", "standard_name", "northward_N");
            dataFile.addVariableAttribute("northEComp", "standard_name", "eastward_N");

            dataFile.addVariable("eastNComp", DataType.FLOAT, dims);
            dataFile.addVariable("eastEComp", DataType.FLOAT, dims);
            dataFile.addVariableAttribute("eastNComp", "standard_name", "northward_E");
            dataFile.addVariableAttribute("eastEComp", "standard_name", "eastward_E");

            dataFile.addVariable("northEastNComp", DataType.FLOAT, dims);
            dataFile.addVariable("northEastEComp", DataType.FLOAT, dims);
            dataFile.addVariableAttribute("northEastNComp", "standard_name", "northward_NE");
            dataFile.addVariableAttribute("northEastEComp", "standard_name", "eastward_NE");

            dataFile.addVariable("southEastNComp", DataType.FLOAT, dims);
            dataFile.addVariable("southEastEComp", DataType.FLOAT, dims);
            dataFile.addVariableAttribute("southEastNComp", "standard_name", "northward_SE");
            dataFile.addVariableAttribute("southEastEComp", "standard_name", "eastward_SE");

            dataFile.addVariable("southWestNComp", DataType.FLOAT, dims);
            dataFile.addVariable("southWestEComp", DataType.FLOAT, dims);
            dataFile.addVariableAttribute("southWestNComp", "standard_name", "northward_SW");
            dataFile.addVariableAttribute("southWestEComp", "standard_name", "eastward_SW");

            dataFile.addVariable("northWestNComp", DataType.FLOAT, dims);
            dataFile.addVariable("northWestEComp", DataType.FLOAT, dims);
            dataFile.addVariableAttribute("northWestNComp", "standard_name", "northward_NW");
            dataFile.addVariableAttribute("northWestEComp", "standard_name", "eastward_NW");

            /*
             * Write the data for the coordinate variables
             */
            ArrayFloat.D1 dataLat = new ArrayFloat.D1(latDim.getLength());
            ArrayFloat.D1 dataLon = new ArrayFloat.D1(lonDim.getLength());
            ArrayFloat.D1 dataDepth = new ArrayFloat.D1(depthDim.getLength());
            ArrayInt.D1 dataTime = new ArrayInt.D1(timeDim.getLength());
            for (int i = 0; i < latDim.getLength(); i++) {
                dataLat.set(i, -90f + (190f / NLAT) * i);
            }
            for (int i = 0; i < lonDim.getLength(); i++) {
                dataLon.set(i, -180f + (360f / NLON) * i);
            }
            for (int i = 0; i < depthDim.getLength(); i++) {
                dataDepth.set(i, 10 * i);
            }
            DateTime datetime = new DateTime(2000, 01, 01, 00, 00);
            for (int i = 0; i < timeDim.getLength(); i++) {
                dataTime.set(i, (int) (datetime.getMillis() / 1000));
                datetime = datetime.plusDays(1);
            }

            /*
             * Create the file
             */
            dataFile.create();
            dataFile.write("latitude", dataLat);
            dataFile.write("longitude", dataLon);
            dataFile.write("depth", dataDepth);
            dataFile.write("time", dataTime);

            /*
             * This is the data array we will write. It will just be filled with
             * a progression of numbers for this example.
             */
            ArrayFloat.D4 vLonDataOut = new ArrayFloat.D4(timeDim.getLength(),
                    depthDim.getLength(), latDim.getLength(), lonDim.getLength());
            ArrayFloat.D4 vLatDataOut = new ArrayFloat.D4(timeDim.getLength(),
                    depthDim.getLength(), latDim.getLength(), lonDim.getLength());
            ArrayFloat.D4 vDepthDataOut = new ArrayFloat.D4(timeDim.getLength(),
                    depthDim.getLength(), latDim.getLength(), lonDim.getLength());
            ArrayFloat.D4 vTimeDataOut = new ArrayFloat.D4(timeDim.getLength(),
                    depthDim.getLength(), latDim.getLength(), lonDim.getLength());

            ArrayFloat.D4 northNComp = new ArrayFloat.D4(timeDim.getLength(), depthDim.getLength(),
                    latDim.getLength(), lonDim.getLength());
            ArrayFloat.D4 northEComp = new ArrayFloat.D4(timeDim.getLength(), depthDim.getLength(),
                    latDim.getLength(), lonDim.getLength());

            ArrayFloat.D4 eastNComp = new ArrayFloat.D4(timeDim.getLength(), depthDim.getLength(),
                    latDim.getLength(), lonDim.getLength());
            ArrayFloat.D4 eastEComp = new ArrayFloat.D4(timeDim.getLength(), depthDim.getLength(),
                    latDim.getLength(), lonDim.getLength());

            ArrayFloat.D4 northEastNComp = new ArrayFloat.D4(timeDim.getLength(),
                    depthDim.getLength(), latDim.getLength(), lonDim.getLength());
            ArrayFloat.D4 northEastEComp = new ArrayFloat.D4(timeDim.getLength(),
                    depthDim.getLength(), latDim.getLength(), lonDim.getLength());

            ArrayFloat.D4 southEastNComp = new ArrayFloat.D4(timeDim.getLength(),
                    depthDim.getLength(), latDim.getLength(), lonDim.getLength());
            ArrayFloat.D4 southEastEComp = new ArrayFloat.D4(timeDim.getLength(),
                    depthDim.getLength(), latDim.getLength(), lonDim.getLength());

            ArrayFloat.D4 southWestNComp = new ArrayFloat.D4(timeDim.getLength(),
                    depthDim.getLength(), latDim.getLength(), lonDim.getLength());
            ArrayFloat.D4 southWestEComp = new ArrayFloat.D4(timeDim.getLength(),
                    depthDim.getLength(), latDim.getLength(), lonDim.getLength());

            ArrayFloat.D4 northWestNComp = new ArrayFloat.D4(timeDim.getLength(),
                    depthDim.getLength(), latDim.getLength(), lonDim.getLength());
            ArrayFloat.D4 northWestEComp = new ArrayFloat.D4(timeDim.getLength(),
                    depthDim.getLength(), latDim.getLength(), lonDim.getLength());

            /*
             * Create some pretend data. If this wasn't an example program, we
             * would have some real data to write, for example, model output.
             */
            for (int i = 0; i < lonDim.getLength(); i++) {
                float lon = 100.0f * i / (lonDim.getLength() - 1);
                for (int j = 0; j < latDim.getLength(); j++) {
                    float lat = 100.0f * j / (latDim.getLength() - 1);
                    for (int k = 0; k < depthDim.getLength(); k++) {
                        float depth = 100.0f * k / (depthDim.getLength() - 1);
                        for (int l = 0; l < timeDim.getLength(); l++) {
                            float time = 100.0f * l / (timeDim.getLength() - 1);
                            vLonDataOut.set(l, k, j, i, lon);
                            vLatDataOut.set(l, k, j, i, lat);
                            vDepthDataOut.set(l, k, j, i, depth);
                            vTimeDataOut.set(l, k, j, i, time);

                            northNComp.set(l, k, j, i, i);
                            northEComp.set(l, k, j, i, 0);

                            eastNComp.set(l, k, j, i, 0);
                            eastEComp.set(l, k, j, i, j);

                            southEastNComp.set(l, k, j, i, -i - j);
                            southEastEComp.set(l, k, j, i, i + j);

                            northWestNComp.set(l, k, j, i, i + j);
                            northWestEComp.set(l, k, j, i, -i - j);

                            northEastNComp.set(l, k, j, i, i - j + 100);
                            northEastEComp.set(l, k, j, i, i - j + 100);

                            southWestNComp.set(l, k, j, i, j - i - 100);
                            southWestEComp.set(l, k, j, i, j - i - 100);
                        }
                    }
                }
            }
            dataFile.write("vLon", vLonDataOut);
            dataFile.write("vLat", vLatDataOut);
            dataFile.write("vDepth", vDepthDataOut);
            dataFile.write("vTime", vTimeDataOut);

            dataFile.write("northNComp", northNComp);
            dataFile.write("northEComp", northEComp);
            dataFile.write("eastNComp", eastNComp);
            dataFile.write("eastEComp", eastEComp);
            dataFile.write("northEastNComp", northEastNComp);
            dataFile.write("northEastEComp", northEastEComp);
            dataFile.write("southEastNComp", southEastNComp);
            dataFile.write("southEastEComp", southEastEComp);
            dataFile.write("southWestNComp", southWestNComp);
            dataFile.write("southWestEComp", southWestEComp);
            dataFile.write("northWestNComp", northWestNComp);
            dataFile.write("northWestEComp", northWestEComp);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidRangeException e) {
            e.printStackTrace();
        } finally {
            if (null != dataFile) {
                try {
                    dataFile.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        System.out.println("Test file written to: " + dataFile.getLocation());
    }
}
