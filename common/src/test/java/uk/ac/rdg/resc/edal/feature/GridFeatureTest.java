/*******************************************************************************
 * Copyright (c) 2014 The University of Reading
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

package uk.ac.rdg.resc.edal.feature;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.domain.GridDomain;
import uk.ac.rdg.resc.edal.domain.SimpleGridDomain;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RegularGrid;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxisImpl;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Array4D;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

/**
 * Test class for {@link GridFeature}.
 * 
 * @author Guy
 */

public class GridFeatureTest {

    private static final String VAR_ID = "onlyvar";

    private GridFeature feature;

    private VerticalAxis vAxis;
    private TimeAxis tAxis;

    @Before
    public void setup() {
        HorizontalGrid hGrid = new RegularGridImpl(0, 0, 99, 99, null, 100, 100);
        vAxis = new VerticalAxisImpl("test_zaxis",
                CollectionUtils.listFromDoubleArray(new double[] { 0, 5, 10 }), null);
        List<DateTime> times = new ArrayList<>();
        for (int i = 1; i <= 31; i++) {
            times.add(new DateTime(2000, 1, i, 0, 0));
        }
        tAxis = new TimeAxisImpl("test_taxis", times);
        GridDomain domain = new SimpleGridDomain(hGrid, vAxis, tAxis);

        Map<String, Parameter> parameters = new HashMap<>();
        Map<String, Array4D<Number>> valuesMap = new HashMap<>();

        parameters.put(VAR_ID, new Parameter(VAR_ID, "The only variable",
                "This is the only variable in this feature", null, null));
        valuesMap.put(VAR_ID, new Array4D<Number>(31, 3, 100, 100) {
            @Override
            public Number get(int... coords) {
                return numberFromCoords(coords[0], coords[1], coords[2], coords[3]);
            }

            @Override
            public void set(Number value, int... coords) {
            }
        });

        feature = new GridFeature("test_feature", "Test Feature", "This is a test feature", domain,
                parameters, valuesMap);
    }

    @Test
    public void testExtractMapFeature() throws DataReadingException {
        int startX = 10;
        int startY = 20;
        int xSize = 10;
        int ySize = 10;

        RegularGrid targetGrid = new RegularGridImpl(startX - 0.5, startY - 0.5, startX + xSize
                + 0.5, startY + ySize + 0.5, null, xSize + 1, ySize + 1);

        /*
         * Useful check that the target grid is what we expect...
         */
//        for (int i = 0; i < targetGrid.getXSize(); i++) {
//            StringBuffer row = new StringBuffer();
//            for (int j = 0; j < targetGrid.getYSize(); j++) {
//                row.append(targetGrid.getXAxis().getCoordinateValue(i) + ","
//                        + targetGrid.getYAxis().getCoordinateValue(j) + "\t");
//            }
//            System.out.println(row.toString());
//        }

        for (int z = 0; z < vAxis.size(); z++) {
            double elevation = vAxis.getCoordinateValue(z);
            for (int t = 0; t < tAxis.size(); t++) {
                DateTime time = tAxis.getCoordinateValue(t);
                MapFeature mapFeature = feature
                        .extractMapFeature(null, targetGrid, elevation, time);
                Array2D<Number> values = mapFeature.getValues(VAR_ID);

                int xGlobal = startX;
                for (int xLocal = 0; xLocal < values.getXSize(); xLocal++) {
                    int yGlobal = startY;
                    for (int yLocal = 0; yLocal < values.getYSize(); yLocal++) {
                        Number number = values.get(yLocal, xLocal);
                        int[] c = coordsFromNumber(number);
                        assertEquals(c[0], t);
                        assertEquals(c[1], z);
                        assertEquals(c[2], yGlobal);
                        assertEquals(c[3], xGlobal);
                        yGlobal++;
                    }
                    xGlobal++;
                }
            }
        }

    }

    private static Number numberFromCoords(int t, int z, int y, int x) {
        return x + 1e3 * y + 1e6 * z + 1e9 * t;
    }

    private static int[] coordsFromNumber(Number n) {
        double v = n.doubleValue();
        int x = (int) (v % 1000);
        v -= x;
        v /= 1e3;
        int y = (int) (v % 1000);
        v -= y;
        v /= 1e3;
        int z = (int) (v % 1000);
        v -= z;
        v /= 1e3;
        int t = (int) (v % 1000);
        return new int[] { t, z, y, x };
    }
}
