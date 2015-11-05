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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;

import uk.ac.rdg.resc.edal.dataset.DataReadingStrategy;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.GridDataSource;
import uk.ac.rdg.resc.edal.dataset.GriddedDataset;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.grid.RegularAxisImpl;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.metadata.GridVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.util.Array4D;
import uk.ac.rdg.resc.edal.util.ValuesArray4D;

public class ModisLcDatasetFactory extends DatasetFactory {

    @Override
    public Dataset createDataset(String id, String location) throws IOException, EdalException {
        File file = new File(location);
        BufferedReader reader = new BufferedReader(new FileReader(file));

        int rows = Integer.parseInt(reader.readLine().split("\\s+")[1]);
        int cols = Integer.parseInt(reader.readLine().split("\\s+")[1]);

        double xstart = Double.parseDouble(reader.readLine().split("\\s+")[1]);
        double ystart = Double.parseDouble(reader.readLine().split("\\s+")[1]);
        double inc = Double.parseDouble(reader.readLine().split("\\s+")[1]);
        int nodata = Integer.parseInt(reader.readLine().split("\\s+")[1]);

        String[] data = reader.readLine().split("\\s+");
        reader.close();

        ValuesArray4D vals = new ValuesArray4D(1, 1, rows, cols);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                /*
                 * Add 1 because the first element is an empty string
                 */
                int index = 1 + c + r * cols;
                Integer value = Integer.parseInt(data[index]);
                if (value == nodata) {
                    value = null;
                }
                vals.set(value, 0, 0, rows - r - 1, c);
            }
        }

        RegularAxisImpl xAxis = new RegularAxisImpl("x", xstart, inc, cols, true);
        RegularAxisImpl yAxis = new RegularAxisImpl("y", ystart, inc, rows, true);

        GridVariableMetadata metadata = new GridVariableMetadata(new Parameter("land_cover",
                "Land Cover", "MODIS land cover", "MODIS", ""), new RegularGridImpl(xAxis, yAxis,
                DefaultGeographicCRS.WGS84), null, null, true);

        List<GridVariableMetadata> metadataCollection = Arrays.asList(metadata);
        return new ModisGridDataset(id, metadataCollection, vals);
    }

    private final class ModisGridDataset extends GriddedDataset {
        private ValuesArray4D data;

        public ModisGridDataset(String id, Collection<GridVariableMetadata> vars, ValuesArray4D data) {
            super(id, vars);
            this.data = data;
        }

        @Override
        protected GridDataSource openDataSource() throws DataReadingException {
            return new GridDataSource() {
                @Override
                public Array4D<Number> read(String variableId, final int tmin, int tmax,
                        final int zmin, int zmax, final int ymin, final int ymax, final int xmin,
                        final int xmax) throws IOException, DataReadingException {
                    return new Array4D<Number>(tmax - tmin + 1, zmax - zmin + 1, ymax - ymin + 1,
                            xmax - xmin + 1) {
                        @Override
                        public Number get(int... coords) {
                            int tIndex = tmin + coords[0];
                            int zIndex = zmin + coords[1];
                            int yIndex = ymin + coords[2];
                            int xIndex = xmin + coords[3];
                            return data.get(tIndex, zIndex, yIndex, xIndex);
                        }

                        @Override
                        public void set(Number value, int... coords) {
                            throw new UnsupportedOperationException();
                        }
                    };
                }

                @Override
                public void close() throws DataReadingException {
                }
            };
        }

        @Override
        protected DataReadingStrategy getDataReadingStrategy() {
            return DataReadingStrategy.PIXEL_BY_PIXEL;
        }
    }
}
