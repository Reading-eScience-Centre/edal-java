/*******************************************************************************
 * Copyright (c) 2016 The University of Reading
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

package uk.ac.rdg.resc.edal.examples;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.NetcdfFileWriter.Version;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Code used to generate the synthetic data file.
 * 
 * Designed as a quick-and-dirty method for generating synthetic data, rather
 * than to be used as an example for EDAL. Included on github because I wrote it
 * and there's no point in throwing it away. Just don't complain if it's not
 * commented or doesn't demonstrate anything EDAL-related...
 *
 * @author Guy Griffiths
 */
public class CreateSyntheticData {
    public static void main(String[] args) throws IOException, InvalidRangeException {
        URL landMaskResource = CreateSyntheticData.class.getResource("/synthetic_land_mask.nc");
        NetcdfDataset landMaskDataset = NetcdfDataset.openDataset(landMaskResource.getFile());
        Variable landMaskVar = landMaskDataset.findVariable("land_mask");
        Array landMaskData = landMaskVar.read();

        NetcdfFileWriter dataset = NetcdfFileWriter.createNew(Version.netcdf4_classic,
                "synthetic_data.nc");
        /*
         * Define the dimensions
         */
        Dimension timeDim = dataset.addDimension("time", 12);
        Dimension depthDim = dataset.addDimension("depth", 10);
        Dimension latDim = dataset.addDimension("lat", 180);
        Dimension lonDim = dataset.addDimension("lon", 360);

        /*
         * Add the co-ordinate variables
         */
        Variable timeVar = dataset.addVariable(timeDim.getFullName(), DataType.INT,
                Arrays.asList(timeDim));
        timeVar.addAttribute(new Attribute("units", "days since 2000-01-01 00:00:00"));
        timeVar.addAttribute(new Attribute("standard_name", "time"));
        timeVar.addAttribute(new Attribute("calendar", "gregorian"));
        ArrayInt.D1 timeData = new ArrayInt.D1(timeDim.getLength(), false);
        int day = 0;
        timeData.setInt(0, day);
        day += 31;
        timeData.setInt(1, day);
        day += 29;
        timeData.setInt(2, day);
        day += 31;
        timeData.setInt(3, day);
        day += 30;
        timeData.setInt(4, day);
        day += 31;
        timeData.setInt(5, day);
        day += 30;
        timeData.setInt(6, day);
        day += 31;
        timeData.setInt(7, day);
        day += 31;
        timeData.setInt(8, day);
        day += 30;
        timeData.setInt(9, day);
        day += 31;
        timeData.setInt(10, day);
        day += 30;
        timeData.setInt(11, day);

        Variable depthVar = dataset.addVariable(depthDim.getFullName(), DataType.FLOAT,
                Arrays.asList(depthDim));
        depthVar.addAttribute(new Attribute("units", "m"));
        depthVar.addAttribute(new Attribute("positive", "down"));
        depthVar.addAttribute(new Attribute("standard_name", "depth"));
        ArrayFloat.D1 depthData = new ArrayFloat.D1(depthDim.getLength());
        float depth = 0f;
        for (int z = 0; z < depthDim.getLength(); z++) {
            depthData.setFloat(z, depth += 10);
        }

        Variable latVar = dataset.addVariable(latDim.getFullName(), DataType.FLOAT,
                Arrays.asList(latDim));
        latVar.addAttribute(new Attribute("units", "degrees_north"));
        latVar.addAttribute(new Attribute("standard_name", "latitude"));
        ArrayFloat.D1 latData = new ArrayFloat.D1(latDim.getLength());
        float lat = -89.5f;
        for (int y = 0; y < latDim.getLength(); y++) {
            latData.setFloat(y, lat++);
        }

        Variable lonVar = dataset.addVariable(lonDim.getFullName(), DataType.FLOAT,
                Arrays.asList(lonDim));
        lonVar.addAttribute(new Attribute("units", "degrees_east"));
        lonVar.addAttribute(new Attribute("standard_name", "longitude"));
        ArrayFloat.D1 lonData = new ArrayFloat.D1(lonDim.getLength());
        float lon = -179.5f;
        for (int x = 0; x < lonDim.getLength(); x++) {
            lonData.setFloat(x, lon++);
        }

        List<Dimension> allDims = new ArrayList<>();
        allDims.add(timeDim);
        allDims.add(depthDim);
        allDims.add(latDim);
        allDims.add(lonDim);

        Variable tempVar = dataset.addVariable("temperature", DataType.FLOAT, allDims);
        tempVar.addAttribute(new Attribute("units", "K"));
        tempVar.addAttribute(new Attribute("standard_name", "sea_water_potential_temperature"));
        tempVar.addAttribute(new Attribute("ref", "http://www.uncertml.org/statistics/mean"));
        tempVar.addAttribute(new Attribute("_FillValue", -999f));
        ArrayFloat tempData = new ArrayFloat.D4(timeDim.getLength(), depthDim.getLength(),
                latDim.getLength(), lonDim.getLength());

        Variable tempErrorVar = dataset.addVariable("temperature_uncertainty", DataType.FLOAT,
                allDims);
        tempErrorVar.addAttribute(new Attribute("units", "K"));
        tempErrorVar
                .addAttribute(new Attribute("standard_name", "sea_water_potential_temperature"));
        tempErrorVar.addAttribute(new Attribute("ref",
                "http://www.uncertml.org/statistics/standard-deviation"));
        tempErrorVar.addAttribute(new Attribute("_FillValue", -999f));
        ArrayFloat tempErrorData = new ArrayFloat.D4(timeDim.getLength(), depthDim.getLength(),
                latDim.getLength(), lonDim.getLength());

        Variable tempUncertGroupVar = dataset.addVariable("temperature_stats", DataType.SHORT,
                new ArrayList<>());
        tempUncertGroupVar.addAttribute(new Attribute("ancillary_variables",
                "temperature temperature_uncertainty"));
        tempUncertGroupVar.addAttribute(new Attribute("ref",
                "http://www.uncertml.org/statistics/statistics-collection"));

        List<Dimension> currentDims = Arrays.asList(timeDim, latDim, lonDim);
        Variable currentXVar = dataset.addVariable("u", DataType.FLOAT, currentDims);
        currentXVar.addAttribute(new Attribute("units", "m/s"));
        currentXVar.addAttribute(new Attribute("standard_name", "eastward_sea_water_velocity"));
        currentXVar.addAttribute(new Attribute("_FillValue", -999f));
        ArrayFloat currentXData = new ArrayFloat.D3(timeDim.getLength(), latDim.getLength(),
                lonDim.getLength());

        Variable currentYVar = dataset.addVariable("v", DataType.FLOAT, currentDims);
        currentYVar.addAttribute(new Attribute("units", "m/s"));
        currentYVar.addAttribute(new Attribute("standard_name", "northward_sea_water_velocity"));
        currentYVar.addAttribute(new Attribute("_FillValue", -999f));
        ArrayFloat currentYData = new ArrayFloat.D3(timeDim.getLength(), latDim.getLength(),
                lonDim.getLength());

        Variable landUseVar = dataset.addVariable("land_cover", DataType.BYTE,
                Arrays.asList(latDim, lonDim));
        landUseVar.addAttribute(new Attribute("units", "Land Cover Class"));
        landUseVar.addAttribute(new Attribute("long_name", "land_cover"));
        landUseVar.addAttribute(new Attribute("flag_values", Arrays.asList(0, 1, 2, 3, 4)));
        landUseVar.addAttribute(new Attribute("flag_meanings",
                "Desert Grasslands Meadows Forest Ice"));
        landUseVar.addAttribute(new Attribute("flag_colors",
                "desert_sand granny_smith_apple emerald forest_green_traditional iceberg"));
        landUseVar.addAttribute(new Attribute("_FillValue", -1));
        ArrayByte landUseData = new ArrayByte.D2(latDim.getLength(), lonDim.getLength(), false);

        Index tempIndex = tempData.getIndex();
        Index tempErrorIndex = tempErrorData.getIndex();
        for (int t = 0; t < timeDim.getLength(); t++) {
            for (int z = 0; z < depthDim.getLength(); z++) {
                for (int y = 0; y < latDim.getLength(); y++) {
                    for (int x = 0; x < lonDim.getLength(); x++) {
                        double xFrac = ((double) x) / lonDim.getLength();
                        double yFrac = ((double) y) / latDim.getLength();

                        double xyTempComp = Math.sin(Math.PI * yFrac) * Math.sin(Math.PI * yFrac);
                        double zTempComp = -z / 75f;
                        double timeTempComp = -Math.abs(t - 6) / 36f;
                        float tempValue = (float) (278 + 25 * (xyTempComp + zTempComp + timeTempComp));
                        tempIndex.set(t, z, y, x);
                        tempErrorIndex.set(t, z, y, x);

                        double xyTempErrorComp = 2f - Math.sin(2 * Math.PI * xFrac)
                                * Math.sin(2 * Math.PI * xFrac) - Math.sin(Math.PI * yFrac)
                                * Math.sin(Math.PI * yFrac);
                        float tempErrorValue = (float) (xyTempErrorComp + zTempComp + timeTempComp);

                        if (isLand(x, y, z, landMaskData)) {
                            tempData.setFloat(tempIndex, -999f);
                            tempErrorData.setFloat(tempErrorIndex, -999f);
                        } else {
                            tempData.setFloat(tempIndex, tempValue);
                            tempErrorData.setFloat(tempErrorIndex, tempErrorValue);
                        }
                    }
                }
            }
        }

        Index currentXIndex = currentXData.getIndex();
        Index currentYIndex = currentYData.getIndex();
        for (int t = 0; t < timeDim.getLength(); t++) {
            for (int y = 0; y < latDim.getLength(); y++) {
                for (int x = 0; x < lonDim.getLength(); x++) {
                    currentXIndex.set(t, y, x);
                    currentYIndex.set(t, y, x);

                    float currentXValue = ((t + 20) / 10f) * (latData.getFloat(y) / 90f);
                    float currentYValue = ((t + 20) / 10f) * (lonData.getFloat(x) / 90f);
                    if (isLand(x, y, 0, landMaskData)) {
                        currentXData.set(currentXIndex, -999f);
                        currentYData.set(currentYIndex, -999f);
                    } else {
                        currentXData.set(currentXIndex, currentXValue);
                        currentYData.set(currentYIndex, currentYValue);
                    }
                }
            }
        }

        Index landUseIndex = landUseData.getIndex();
        for (int y = 0; y < latDim.getLength(); y++) {
            byte landUseValue = (byte) (5 * Math.abs(y - 90) / 92);
            for (int x = 0; x < lonDim.getLength(); x++) {
                landUseIndex.set(y, x);

                if (isLand(x, y, 0, landMaskData)) {
                    landUseData.set(landUseIndex, landUseValue);
                } else {
                    landUseData.set(landUseIndex, (byte) -1);
                }
            }
        }

        dataset.create();
        dataset.write(timeVar, timeData);
        dataset.write(depthVar, depthData);
        dataset.write(latVar, latData);
        dataset.write(lonVar, lonData);
        dataset.write(tempVar, tempData);
        dataset.write(tempErrorVar, tempErrorData);
        dataset.write(currentXVar, currentXData);
        dataset.write(currentYVar, currentYData);
        dataset.write(landUseVar, landUseData);
        dataset.close();
    }

    private static boolean isLand(int x, int y, int z, Array landMaskData) {
        Index lmIndex = landMaskData.getIndex();
        lmIndex.set(z, y, x);
        return landMaskData.getByte(lmIndex) == 1;
    }
}
