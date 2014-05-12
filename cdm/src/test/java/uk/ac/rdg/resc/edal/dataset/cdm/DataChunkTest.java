package uk.ac.rdg.resc.edal.dataset.cdm;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import ucar.ma2.Index;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.util.cdm.CdmUtils;

public class DataChunkTest {
    private GridDataset datasource;
    private static final double delta = 1e-5;
    private static final int ysize = 19;

    @Before
    public void setUp() throws Exception {
        URL url = this.getClass().getResource("/rectilinear_test_data.nc");
        String location = url.getPath();
        NetcdfDataset nc = CdmUtils.openDataset(location);
        datasource = CdmUtils.getGridDataset(nc);
    }

    @Test
    public void readDatachunkTest() throws IOException {
        GridDatatype gridDatatype = datasource.findGridDatatype("vLat");
        VariableDS var = gridDatatype.getVariable();
        RangesList range = new RangesList(gridDatatype);

        int tmax = 5;
        int zmax = 10;
        int ymax = 15;
        int xmax = 30;
        range.setTRange(0, tmax);
        range.setZRange(0, zmax);
        range.setYRange(0, ymax);
        range.setXRange(0, xmax);
        DataChunk datachunk = DataChunk.readDataChunk(var, range);

        assertEquals((tmax + 1) * (zmax + 1) * (ymax + 1) * (xmax + 1), datachunk.size());
        assertEquals(tmax + 1, datachunk.getIndex().getShape()[0]);
        assertEquals(zmax + 1, datachunk.getIndex().getShape()[1]);
        assertEquals(ymax + 1, datachunk.getIndex().getShape()[2]);
        assertEquals(xmax + 1, datachunk.getIndex().getShape()[3]);

        for (int i = 0; i < ymax; i++) {
            //sample data only change at the third dimension
            Index index = datachunk.getIndex().set2(i);
            float expectedLat = 100.0f * i / (ysize - 1);
            assertEquals(expectedLat, datachunk.readFloatValue(index), delta);
        }
    }

}
