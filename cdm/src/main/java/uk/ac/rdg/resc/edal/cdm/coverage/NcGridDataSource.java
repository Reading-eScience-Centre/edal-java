/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.cdm.coverage;

import java.io.IOException;
import java.util.List;
import ucar.ma2.Index;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.cdm.util.CdmUtils;
import uk.ac.rdg.resc.edal.coverage.impl.GridDataSource;
import uk.ac.rdg.resc.edal.util.AbstractBigList;

/**
 * Simple class for reading from NetCDF grids
 * @author Jon
 */
final class NcGridDataSource extends GridDataSource<Float>
{
    private final NetcdfDataset nc;
    private final GridDatatype grid;
    private final int zIndex;
    private final int tIndex;

    public NcGridDataSource(String location, String memberName, int zIndex, int tIndex)
    {
        try
        {
            this.nc = NetcdfDataset.openDataset(location);
            this.grid = CdmUtils.getGridDatatype(nc, memberName);
            this.zIndex = zIndex;
            this.tIndex = tIndex;
        }
        catch(IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
    }

    // TODO: this seems like something of a palaver.  Is there some way of 
    // achieving the same result without loss of generality?  We need to take
    // into account:
    //    - whether the values in the NetCDF file are packed (hence the DataChunk abstraction)
    //    - the order of axes in the NetCDF file (hence the RangesList construct)
    @Override
    public List<Float> readBlock(int imin, int imax, int jmin, int jmax)
    {
        RangesList ranges = new RangesList(this.grid);
        ranges.setTRange(tIndex, tIndex);
        ranges.setZRange(zIndex, zIndex);
        ranges.setYRange(jmin, jmax);
        ranges.setXRange(imin, imax);
        
        final int iSize = imax - imin + 1;
        final int jSize = jmax - jmin + 1;
        final long size = (long)iSize * jSize;
        
        final int iIndexInArray = ranges.getXAxisIndex();
        final int jIndexInArray = ranges.getYAxisIndex();
        
        final DataChunk dataChunk;
        try
        {
            dataChunk = DataChunk.readDataChunk(grid.getVariable(), ranges);
        }
        catch(IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
        assert(size == dataChunk.size());
        
        final Index arrayIndex = dataChunk.getIndex();
        // Set the index to zero.  Is this necessary?
        arrayIndex.set(new int[arrayIndex.getRank()]);
        
        // Wrap the DataChunk as a List of Floats, returning data with the i
        // dimension varying fastest, irrespective of the order of data in
        // the data chunk.
        return new AbstractBigList<Float>()
        {
            @Override
            public Float get(long index)
            {
                int i = (int)(index % iSize);
                int j = (int)(index / iSize);
                arrayIndex.setDim(iIndexInArray, i);
                arrayIndex.setDim(jIndexInArray, j);
                return dataChunk.readFloatValue(arrayIndex);
            }

            @Override
            public long sizeAsLong() { return size; }

            @Override
            public Class<Float> getValueType() { return Float.class; }
        };
    }

    @Override
    public void close() {
        CdmUtils.safelyClose(nc);
    }

}
