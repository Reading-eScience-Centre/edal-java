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

import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * This class is not part of the test suite, but can be used to generated useful
 * sample data.
 * 
 * It takes some data on a curvilinear grid
 * (src/test/resources/input-curvilinear.nc) and clears the variables, replacing
 * them with useful vector components. The input data may already be correct
 * (i.e the same as the output it creates), to save space. The point is that the
 * grid system is what we want, and so this program can be modified so that the
 * output contains whatever variables are desired. Paths etc. will need
 * changing.
 */
@SuppressWarnings("deprecation")
public class CreateNetCDFCurvilinear {
    public static void main(String args[]) throws IOException {
        /*
         * The dataset to copy the projection stuff from.
         */
        NetcdfFileWriteable existingDataset = NetcdfFileWriteable
                .openExisting("input-curvilinear.nc");

        String filename = "output-curvilinear.nc";
        NetcdfFileWriteable dataFile = null;
        try {

            Dimension etaDimension = existingDataset.findDimension("eta_rho");
            Dimension xiDimension = existingDataset.findDimension("xi_rho");

            Variable lonrhoVariable = existingDataset.findVariable("lon_rho");
            Variable latrhoVariable = existingDataset.findVariable("lat_rho");

            Array lonrhoData = lonrhoVariable.read();
            Array latrhoData = latrhoVariable.read();

            dataFile = NetcdfFileWriteable.createNew(filename, false);
            dataFile.addDimension(null, etaDimension);
            dataFile.addDimension(null, xiDimension);

            ArrayList<Dimension> dims = new ArrayList<Dimension>();
            dims.add(etaDimension);
            dims.add(xiDimension);

            dataFile.addVariable(null, lonrhoVariable);
            dataFile.addVariable(null, latrhoVariable);

            dataFile.addAttribute(null, new Attribute("Conventions", "CF-1.0"));

            /*
             * Add extra variables here, including attributes
             */
            dataFile.addVariable("allx_u", DataType.FLOAT, dims);
            dataFile.addVariableAttribute("allx_u", "standard_name", "u-X-component");
            dataFile.addVariableAttribute("allx_u", "coordinates", "lon_rho lat_rho");

            dataFile.addVariable("allx_v", DataType.FLOAT, dims);
            dataFile.addVariableAttribute("allx_v", "standard_name", "v-X-component");
            dataFile.addVariableAttribute("allx_u", "coordinates", "lon_rho lat_rho");

            dataFile.addVariable("ally_u", DataType.FLOAT, dims);
            dataFile.addVariableAttribute("ally_u", "standard_name", "u-Y-component");
            dataFile.addVariableAttribute("ally_u", "coordinates", "lon_rho lat_rho");

            dataFile.addVariable("ally_v", DataType.FLOAT, dims);
            dataFile.addVariableAttribute("ally_v", "standard_name", "v-Y-component");
            dataFile.addVariableAttribute("ally_u", "coordinates", "lon_rho lat_rho");

            dataFile.create();

            dataFile.write("lon_rho", lonrhoData);
            dataFile.write("lat_rho", latrhoData);

            ArrayFloat.D2 allxUData = new ArrayFloat.D2(etaDimension.getLength(),
                    xiDimension.getLength());
            ArrayFloat.D2 allxVData = new ArrayFloat.D2(etaDimension.getLength(),
                    xiDimension.getLength());
            ArrayFloat.D2 allyUData = new ArrayFloat.D2(etaDimension.getLength(),
                    xiDimension.getLength());
            ArrayFloat.D2 allyVData = new ArrayFloat.D2(etaDimension.getLength(),
                    xiDimension.getLength());

            for (int i = 0; i < etaDimension.getLength(); i++) {
                for (int j = 0; j < xiDimension.getLength(); j++) {
                    allxUData.set(i, j, i);
                    allxVData.set(i, j, 0f);
                    allyUData.set(i, j, 0f);
                    allyVData.set(i, j, j);
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
        System.out.println("Test file written to: " + dataFile.getLocation());
    }
}
