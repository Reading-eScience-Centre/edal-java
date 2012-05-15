package uk.ac.rdg.resc.edal.cdm.coverage.grid;

import java.io.IOException;

import ucar.ma2.Index;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.cdm.util.CdmUtils;
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.impl.AbstractGridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.impl.GridAxisImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.InMemoryGridValuesMatrix;

/**
 * Disk-based GridValuesMatrix that reads data from NetCDF files.
 * @author Jon
 */
public class NcGridValuesMatrix extends AbstractGridValuesMatrix<Float>
{

    private final int zIndex;
    private final int tIndex;
    private final GridAxis xAxis;
    private final GridAxis yAxis;
    private final NetcdfDataset nc;
    private final GridDatatype gridDatatype;
    
    public NcGridValuesMatrix(GridAxis xAxis, GridAxis yAxis, String location, String varId, int zIndex, int tIndex)
    {
        this.zIndex = zIndex;
        this.tIndex = tIndex;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        try {
            this.nc = CdmUtils.openDataset(location);
            this.gridDatatype = CdmUtils.getGridDatatype(nc, varId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Float readPoint(int i, int j)
    {
        RangesList ranges = this.getRangesList(i, i, j, j);
        DataChunk dataChunk = readDataChunk(ranges);
        assert(dataChunk.size() == 1);
        final Index arrayIndex = dataChunk.getIndex();
        // Set index to zero in all directions
        arrayIndex.set(new int[arrayIndex.getRank()]);
        return dataChunk.readFloatValue(arrayIndex);
    }

    @Override
    public GridValuesMatrix<Float> readBlock(final int imin, final int imax, final int jmin, final int jmax)
    {
        final int iSize = imax - imin + 1;
        final int jSize = jmax - jmin + 1;
        final long size = (long) iSize * jSize;
        final GridAxis newXAxis = new GridAxisImpl(this.getXAxis().getName(), iSize);
        final GridAxis newYAxis = new GridAxisImpl(this.getYAxis().getName(), jSize);
        
        // Read the data from disk into memory
        RangesList ranges = this.getRangesList(imin, imax, jmin, jmax);
        final DataChunk dataChunk = readDataChunk(ranges);
        assert(size == dataChunk.size());
        
        // Return an in-memory GridValuesMatrix that wraps the DataChunk
        final int iIndexInArray = ranges.getXAxisIndex();
        final int jIndexInArray = ranges.getYAxisIndex();
        
        return new InMemoryGridValuesMatrix<Float>()
        {
            @Override public Float readPoint(int i, int j)
            {
                // TODO: check that getIndex() returns a new index each time,
                // otherwise we may not be thread safe.
                Index arrayIndex = dataChunk.getIndex();
                arrayIndex.setDim(iIndexInArray, i);
                arrayIndex.setDim(jIndexInArray, j);
                return dataChunk.readFloatValue(arrayIndex);
            }

            @Override
            public GridAxis getXAxis() { return newXAxis; }

            @Override
            public GridAxis getYAxis() { return newYAxis; }

            @Override
            public Class<Float> getValueType() {
                return Float.class;
            }
        };
    }
    
    private RangesList getRangesList(final int imin, final int imax, final int jmin, final int jmax)
    {
        RangesList ranges = new RangesList(gridDatatype);
        ranges.setTRange(tIndex, tIndex);
        ranges.setZRange(zIndex, zIndex);
        ranges.setYRange(jmin, jmax);
        ranges.setXRange(imin, imax);
        return ranges;
    }
    
    private DataChunk readDataChunk(RangesList ranges)
    {
        try
        {
            return DataChunk.readDataChunk(gridDatatype.getVariable(), ranges);
        }
        catch(IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public GridAxis getXAxis() {
        return xAxis;
    }

    @Override
    public GridAxis getYAxis() {
        return yAxis;
    }

    @Override
    public void close() {
        CdmUtils.safelyClose(nc);
    }

    @Override
    public Class<Float> getValueType() {
        return Float.class;
    }

}
