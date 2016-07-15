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

import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

/**
 * Code used to generate the synthetic sea temperature data file.
 * 
 * Designed as a quick-and-dirty method for generating synthetic data, rather
 * than to be used as an example for EDAL. Included on github because it's here.
 * Just don't complain if it's not commented or doesn't demonstrate anything
 * EDAL-related...
 *
 * @author Guy Griffiths
 */
public class CreateSyntheticData {
    public static void main(String[] args) throws IOException, InvalidRangeException {
        URL resource = CreateSyntheticData.class.getResource("/synthetic_sea_temperature.nc");
        NetcdfFileWriter dataset = NetcdfFileWriter.openExisting(resource.getFile());
        Variable tempVar = dataset.findVariable("temperature");
        Array readDataBefore = tempVar.read();
        int[] shape = tempVar.getShape();
        ArrayFloat data = new ArrayFloat.D3(shape[0], shape[1], shape[2]);
        Index index = data.getIndex();
        for (int z = 0; z < shape[0]; z++) {
            for (int y = 0; y < shape[1]; y++) {
                for (int x = 0; x < shape[2]; x++) {
                    float value = (float) (274 - z + 0.0045f * Math.pow(
                            shape[1] / 2 - Math.abs(y - shape[1] / 2), 2));
                    index.set(z, y, x);
                    if (readDataBefore.getFloat(index) == -999f) {
                        data.setFloat(index, -999f);
                    } else {
                        data.setFloat(index, value);
                    }
                }
            }
        }
        dataset.write(tempVar, data);
        dataset.close();
    }
}
