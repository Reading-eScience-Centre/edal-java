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

package uk.ac.rdg.resc.edal.dataset.cdm;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.util.cdm.CdmUtils;

/**
 * These tests perform some get methods of {@link RangeList}.
 * 
 * @author Nan Lin
 */

public class RangeListTest {
    private GridDataset gridDataset;
    private static final Range ZERO_RANGE;

    static {
        try {
            ZERO_RANGE = new Range(0, 0);
        } catch (InvalidRangeException ire) {
            throw new ExceptionInInitializerError(ire);
        }
    }

    /**
     * Initialize a grid dataset.
     * 
     * @throws IOException
     *             If there is a problem when open the netCDF file.
     * @throws EdalException
     *             If there is a problem when create the dataset.
     * */
    @Before
    public void setUp() throws IOException, EdalException {
        URL url = this.getClass().getResource("/rectilinear_test_data.nc");
        String location = url.getPath();
        NetcdfDataset nc = NetcdfDatasetAggregator.getDataset(location);
        gridDataset = CdmUtils.getGridDataset(nc);
    }

    /** 
     * Test {@link RangeList} get?AxisIndex and getRanges methods.
     */
    @Test
    public void test() {
        List<GridDatatype> gridVarLists = gridDataset.getGrids();
        /*
         * The order of 4D test data is first, T axis, second, Z axis, followed
         * by y and x axis.
         */
        for (GridDatatype gdt : gridVarLists) {
            RangesList gdtRange = new RangesList(gdt);
            assertEquals(0, gdtRange.getTAxisIndex());
            assertEquals(1, gdtRange.getZAxisIndex());
            assertEquals(2, gdtRange.getYAxisIndex());
            assertEquals(3, gdtRange.getXAxisIndex());
            List<Range> ranges = gdtRange.getRanges();
            for (Range r : ranges) {
                assertEquals(ZERO_RANGE, r);
            }
        }
    }
}
