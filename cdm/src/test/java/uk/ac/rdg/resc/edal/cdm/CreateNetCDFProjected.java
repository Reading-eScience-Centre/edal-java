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
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * This class is not part of the test suite, but may be run to generate the test
 * data used. It is difficult to get the src/test/resources directory in a
 * portable way, so this class doesn't attempt this. Test data should generally
 * only need generating once anyway.
 */

public class CreateNetCDFProjected {
    public static void main(String args[]) throws IOException {
        /*
         * The dataset to copy the projection stuff from.
         */
        NetcdfFileWriteable existingDataset = NetcdfFileWriteable.openExisting("/home/guy/Data/TestData/rotated.nc");
        
        String filename = "/home/guy/Data/TestData/test_proj.nc";
        NetcdfFileWriteable dataFile = null;
        try {
            
            Dimension rlonDimension = existingDataset.findDimension("rlon");
            Dimension rlatDimension = existingDataset.findDimension("rlat");
            
            Variable rpoleVariable = existingDataset.findVariable("rotated_pole");
            Variable rlonVariable = existingDataset.findVariable("rlon");
            Variable rlatVariable = existingDataset.findVariable("rlat");
            Variable navlatVariable = existingDataset.findVariable("nav_lat");
            Variable navlonVariable = existingDataset.findVariable("nav_lon");
            
            Array rpoleData = rpoleVariable.read();
            Array rlonData = rlonVariable.read();
            Array rlatData = rlatVariable.read();
            Array navlonData = navlonVariable.read();
            Array navlatData = navlatVariable.read();
            
            dataFile = NetcdfFileWriteable.createNew(filename, false);
            dataFile.addDimension(null, rlonDimension);
            dataFile.addDimension(null, rlatDimension);
            
            ArrayList<Dimension> dims = new ArrayList<Dimension>();
            dims.add(rlatDimension);
            dims.add(rlonDimension);
            
            dataFile.addVariable(null, rpoleVariable);
            dataFile.addVariable(null, rlonVariable);
            dataFile.addVariable(null, rlatVariable);
            dataFile.addVariable(null, navlonVariable);
            dataFile.addVariable(null, navlatVariable);
            
            dataFile.addAttribute(null, new Attribute("Conventions", "CF-1.0"));
            
            /*
             * Add extra variables here, including attributes
             */
            dataFile.addVariable("allx_u", DataType.FLOAT, dims);
            dataFile.addVariableAttribute("allx_u", "standard_name", "u-X-component");
            dataFile.addVariableAttribute("allx_u", "axis", "YX");
            dataFile.addVariableAttribute("allx_u", "coordinates", "nav_lon nav_lat");
            dataFile.addVariableAttribute("allx_u", "grid_mapping", "rotated_pole");
            
            dataFile.addVariable("allx_v", DataType.FLOAT, dims);
            dataFile.addVariableAttribute("allx_v", "standard_name", "v-X-component");
            dataFile.addVariableAttribute("allx_v", "axis", "YX");
            dataFile.addVariableAttribute("allx_v", "coordinates", "nav_lon nav_lat");
            dataFile.addVariableAttribute("allx_v", "grid_mapping", "rotated_pole");
            
            dataFile.addVariable("ally_u", DataType.FLOAT, dims);
            dataFile.addVariableAttribute("ally_u", "standard_name", "u-Y-component");
            dataFile.addVariableAttribute("ally_u", "axis", "YX");
            dataFile.addVariableAttribute("ally_u", "coordinates", "nav_lon nav_lat");
            dataFile.addVariableAttribute("ally_u", "grid_mapping", "rotated_pole");
            
            dataFile.addVariable("ally_v", DataType.FLOAT, dims);
            dataFile.addVariableAttribute("ally_v", "standard_name", "v-Y-component");
            dataFile.addVariableAttribute("ally_v", "axis", "YX");
            dataFile.addVariableAttribute("ally_v", "coordinates", "nav_lon nav_lat");
            dataFile.addVariableAttribute("ally_v", "grid_mapping", "rotated_pole");
            
            dataFile.create();
            
            dataFile.write("rotated_pole", rpoleData);
            
            dataFile.write("rlon", rlonData);
            dataFile.write("rlat", rlatData);
            dataFile.write("nav_lon", navlonData);
            dataFile.write("nav_lat", navlatData);

            ArrayFloat.D2 allxUData = new ArrayFloat.D2(rlatDimension.getLength(), rlonDimension.getLength());
            ArrayFloat.D2 allxVData = new ArrayFloat.D2(rlatDimension.getLength(), rlonDimension.getLength());
            ArrayFloat.D2 allyUData = new ArrayFloat.D2(rlatDimension.getLength(), rlonDimension.getLength());
            ArrayFloat.D2 allyVData = new ArrayFloat.D2(rlatDimension.getLength(), rlonDimension.getLength());

            for (int i = 0; i < rlatDimension.getLength(); i++) {
                for (int j = 0; j < rlonDimension.getLength(); j++) {
                    allxUData.set(i, j, 10f);
                    allxVData.set(i, j, 0f);
                    allyUData.set(i, j, 0f);
                    allyVData.set(i, j, 10f);
                }
            }
            
            dataFile.write("allx_u", allxUData);
            dataFile.write("allx_v", allxVData);
            dataFile.write("ally_u", allyUData);
            dataFile.write("ally_v", allyVData);
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
