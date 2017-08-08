/*******************************************************************************
 * Copyright (c) 2017 The University of Reading
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.DateTime;

import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayLong;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.NetcdfFileWriter.Version;
import ucar.nc2.Variable;
import uk.ac.rdg.resc.edal.domain.GridDomain;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.util.Array4D;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Provides methods for writing Features to NetCDF files. Currently fairly
 * limited in scope, it contains a single method which will write a GridFeature
 * on a rectilinear lat-lon grid to file.
 *
 * @author Guy Griffiths
 */
public class CdmGridFeatureWrite {
    /**
     * Writes a {@link GridFeature} to file
     * 
     * @param f
     *            The {@link GridFeature} to write. Can contain multiple
     *            variables.
     * @param outFile
     *            The {@link File} to write to.
     * @throws IOException
     *             - If there is a problem writing to the file location.
     * @throws InvalidRangeException
     *             - Usually indicative of a bug...
     */
    public static void gridFeatureToNetCDF(GridFeature f, File outFile) throws IOException,
            InvalidRangeException {
        /*
         * By default, this includes reasonable compression
         */
        NetcdfFileWriter fileWriter = NetcdfFileWriter.createNew(Version.netcdf4,
                outFile.getAbsolutePath());

        fileWriter.setFill(true);

        Set<String> outputVariables = new LinkedHashSet<>();
        outputVariables.addAll(f.getVariableIds());

        Map<Variable, Array> dataToWrite = new HashMap<>();
        ArrayList<Dimension> dims = null;

        GridDomain domain = f.getDomain();

        if (!GISUtils.isDefaultGeographicCRS(domain.getHorizontalGrid()
                .getCoordinateReferenceSystem())
                || !(domain.getHorizontalGrid() instanceof RectilinearGrid)) {
            throw new UnsupportedOperationException(
                    "Currently, writing only supports GridFeatures in CRS:84/EPSG:4326 with a RectlinearGrid");
        }
        RectilinearGrid hGrid = (RectilinearGrid) domain.getHorizontalGrid();
        int xSize = hGrid.getXSize();
        int ySize = hGrid.getYSize();
        int zSize = 1;
        int tSize = 1;
        boolean zPresent = false;
        boolean tPresent = false;
        VerticalAxis zAxis = domain.getVerticalAxis();
        TimeAxis tAxis = domain.getTimeAxis();

        /*
         * Define dimensions, adding z and t if required
         */
        dims = new ArrayList<Dimension>();
        if (tAxis != null) {
            tSize = tAxis.size();
            Dimension tDim = fileWriter.addDimension(null, "time", tAxis.size());
            dims.add(tDim);
            tPresent = true;
        }

        if (zAxis != null) {
            zSize = zAxis.size();
            Dimension zDim = fileWriter.addDimension(null, "z", zSize);
            dims.add(zDim);
            zPresent = true;
        }

        Dimension yDim = fileWriter.addDimension(null, "lat", ySize);
        Dimension xDim = fileWriter.addDimension(null, "lon", xSize);
        dims.add(yDim);
        dims.add(xDim);

        /*
         * Write coordinate variables
         */
        Variable latVar = fileWriter.addVariable(null, "lat", DataType.FLOAT, "lat");
        latVar.addAttribute(new Attribute("units", "degrees_north"));
        ArrayFloat.D1 latVals = new ArrayFloat.D1(ySize);
        int i = 0;
        for (Double latVal : hGrid.getYAxis().getCoordinateValues()) {
            latVals.set(i++, latVal.floatValue());
        }
        dataToWrite.put(latVar, latVals);

        Variable lonVar = fileWriter.addVariable(null, "lon", DataType.FLOAT, "lon");
        lonVar.addAttribute(new Attribute("units", "degrees_east"));
        ArrayFloat.D1 lonVals = new ArrayFloat.D1(xSize);
        i = 0;
        for (Double lonVal : hGrid.getXAxis().getCoordinateValues()) {
            lonVals.set(i++, lonVal.floatValue());
        }
        dataToWrite.put(lonVar, lonVals);

        if (zPresent) {
            Variable zVar = fileWriter.addVariable(null, "z", DataType.FLOAT, "z");
            zVar.addAttribute(new Attribute("units", zAxis.getVerticalCrs().getUnits()));
            zVar.addAttribute(new Attribute("positive",
                    zAxis.getVerticalCrs().isPositiveUpwards() ? "up" : "down"));
            ArrayFloat.D1 zVals = new ArrayFloat.D1(zSize);
            i = 0;
            for (Double zVal : zAxis.getCoordinateValues()) {
                zVals.set(i++, zVal.floatValue());
            }
            dataToWrite.put(zVar, zVals);
        }

        if (tPresent) {
            Variable tVar = fileWriter.addVariable(null, "time", DataType.LONG, "time");
            tVar.addAttribute(new Attribute("units", "seconds since 1970-1-1 0:0"));
            ArrayLong.D1 tVals = new ArrayLong.D1(tSize, false);
            i = 0;
            for (DateTime tVal : tAxis.getCoordinateValues()) {
                tVals.set(i++, (long) (tVal.toDate().getTime() / 1000L));
            }
            dataToWrite.put(tVar, tVals);
        }

        /*
         * Now write all data variables
         */
        for (String varId : outputVariables) {
            Array4D<Number> array4d = f.getValues(varId);

            /*
             * Pick the appropriately dimensioned array
             */
            ArrayFloat values;
            if (!zPresent && !tPresent) {
                values = new ArrayFloat.D2(ySize, xSize);
            } else if (zPresent && !tPresent) {
                values = new ArrayFloat.D3(zSize, ySize, xSize);
            } else if (!zPresent && tPresent) {
                values = new ArrayFloat.D3(tSize, ySize, xSize);
            } else {
                values = new ArrayFloat.D4(tSize, zSize, ySize, xSize);
            }
            Index index = values.getIndex();

            /*
             * Loop over all 4 possible dimensions. If z/t are not present,
             * their respective loops will only execute once.
             */
            for (int t = 0; t < tSize; t++) {
                for (int z = 0; z < zSize; z++) {
                    for (int y = 0; y < ySize; y++) {
                        for (int x = 0; x < xSize; x++) {
                            /*
                             * How to set the index depends on which dimensions
                             * are present.
                             */
                            if (!zPresent && !tPresent) {
                                index.set(y, x);
                            } else if (zPresent && !tPresent) {
                                index.set(z, y, x);
                            } else if (!zPresent && tPresent) {
                                index.set(t, y, x);
                            } else {
                                index.set(t, z, y, x);
                            }

                            Number number = array4d.get(t, z, y, x);
                            if (number == null) {
                                // We use NaN as a fill value
                                number = Float.NaN;
                            }
                            values.set(index, number.floatValue());
                        }
                    }
                }
            }

            Variable variable = fileWriter.addVariable(null, varId, DataType.FLOAT, dims);

            fileWriter.addVariableAttribute(variable, new Attribute("units", f.getParameter(varId)
                    .getUnits()));
            fileWriter.addVariableAttribute(variable, new Attribute("standard_name", f
                    .getParameter(varId).getStandardName()));
            fileWriter.addVariableAttribute(variable,
                    new Attribute("long_name", f.getParameter(varId).getDescription()));
            fileWriter.addVariableAttribute(variable, new Attribute("_FillValue", Float.NaN));

            dataToWrite.put(variable, values);

            for (Entry<Object, Object> entry : f.getFeatureProperties().entrySet()) {
                /*
                 * This is pretty unlikely to be called...
                 */
                if (!(entry.getKey() instanceof String)) {
                    continue;
                }
                Object value = entry.getValue();
                if (value instanceof String) {
                    fileWriter.addVariableAttribute(variable, new Attribute(
                            (String) entry.getKey(), (String) value));
                } else if (value instanceof Number) {
                    fileWriter.addVariableAttribute(variable, new Attribute(
                            (String) entry.getKey(), (Number) value));
                }
            }

        }

        /*
         * Add some global attributes
         */
        fileWriter.addGlobalAttribute("Conventions", "CF-1.6");
        fileWriter.addGlobalAttribute("CreatedBy", "EDAL Libraries");
        fileWriter.addGlobalAttribute("MoreInfo",
                "https://github.com/Reading-eScience-Centre/edal-java");

        /*
         * Finally actually create the file and write data to it
         */
        fileWriter.create();
        for (Entry<Variable, Array> entry : dataToWrite.entrySet()) {
            fileWriter.write(entry.getKey(), entry.getValue());
        }
        fileWriter.close();
    }
}
