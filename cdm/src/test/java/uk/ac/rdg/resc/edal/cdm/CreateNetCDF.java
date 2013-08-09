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
            dims.add(lonDim);
            dims.add(latDim);
            dims.add(depthDim);
            dims.add(timeDim);

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
            ArrayFloat.D4 vLonDataOut = new ArrayFloat.D4(lonDim.getLength(), latDim.getLength(),
                    depthDim.getLength(), timeDim.getLength());
            ArrayFloat.D4 vLatDataOut = new ArrayFloat.D4(lonDim.getLength(), latDim.getLength(),
                    depthDim.getLength(), timeDim.getLength());
            ArrayFloat.D4 vDepthDataOut = new ArrayFloat.D4(lonDim.getLength(), latDim.getLength(),
                    depthDim.getLength(), timeDim.getLength());
            ArrayFloat.D4 vTimeDataOut = new ArrayFloat.D4(lonDim.getLength(), latDim.getLength(),
                    depthDim.getLength(), timeDim.getLength());

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
                            vLonDataOut.set(i, j, k, l, lon);
                            vLatDataOut.set(i, j, k, l, lat);
                            vDepthDataOut.set(i, j, k, l, depth);
                            vTimeDataOut.set(i, j, k, l, time);
                        }
                    }
                }
            }
            dataFile.write("vLon", vLonDataOut);
            dataFile.write("vLat", vLatDataOut);
            dataFile.write("vDepth", vDepthDataOut);
            dataFile.write("vTime", vTimeDataOut);
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
        System.out.println("Test file written to: "+dataFile.getLocation());
    }
}