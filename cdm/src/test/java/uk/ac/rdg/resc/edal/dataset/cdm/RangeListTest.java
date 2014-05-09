package uk.ac.rdg.resc.edal.dataset.cdm;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.util.cdm.CdmUtils;

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

    @Before
    public void setUp() throws Exception {
        URL url = this.getClass().getResource("/rectilinear_test_data.nc");
        String location = url.getPath();
        NetcdfDataset nc = CdmUtils.openDataset(location);
        gridDataset = CdmUtils.getGridDataset(nc);
    }

    @Test
    public void test() {
        List<GridDatatype> gridVarLists = gridDataset.getGrids();

        for (GridDatatype gdt : gridVarLists) {
            RangesList gdtRange = new RangesList(gdt);
            assertEquals(0, gdtRange.getTAxisIndex());
            assertEquals(1, gdtRange.getZAxisIndex());
            assertEquals(2, gdtRange.getYAxisIndex());
            assertEquals(3, gdtRange.getXAxisIndex());
            List<Range> ranges = gdtRange.getRanges();
            for (Range r : ranges){
                assertEquals(ZERO_RANGE, r);
            }
        }
    }

}
