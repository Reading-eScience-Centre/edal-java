package uk.ac.rdg.resc.edal.cdm.coverage.grid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ucar.ma2.Index;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.cdm.util.CdmUtils;
import uk.ac.rdg.resc.edal.coverage.grid.Grid;
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridExtent;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.impl.DiskBasedGridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.impl.FloatListGridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.impl.GridCoordinates2DImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.GridExtentImpl;
import uk.ac.rdg.resc.edal.util.Extents;

public class NcGridValuesMatrix extends DiskBasedGridValuesMatrix<Float> {

    private String location;
    private String varId;
    private int zIndex;
    private int tIndex;
    private GridDatatype gridDatatype = null;
    
    public NcGridValuesMatrix(Grid grid, String location, String varId, int zIndex, int tIndex) {
        super(grid);
        this.location = location;
        this.varId = varId;
        this.zIndex = zIndex;
        this.tIndex = tIndex;
    }

    @Override
    public GridValuesMatrix<Float> readBlock(final int imin, final int imax, final int jmin, final int jmax) {
        NetcdfDataset ncDataset;
        if(gridDatatype == null){
            try {
                ncDataset = CdmUtils.openDataset(location);
                gridDatatype = CdmUtils.getGridDatatype(ncDataset, varId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        RangesList ranges = new RangesList(gridDatatype);
        ranges.setTRange(tIndex, tIndex);
        ranges.setZRange(zIndex, zIndex);
        ranges.setYRange(jmin, jmax);
        ranges.setXRange(imin, imax);
        
        final int iSize = imax - imin + 1;
        final int jSize = jmax - jmin + 1;
        final long size = (long) iSize * jSize;
        
        final int iIndexInArray = ranges.getXAxisIndex();
        final int jIndexInArray = ranges.getYAxisIndex();
        
        final DataChunk dataChunk;
        try
        {
            dataChunk = DataChunk.readDataChunk(gridDatatype.getVariable(), ranges);
        }
        catch(IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
        assert(size == dataChunk.size());
        
        final Index arrayIndex = dataChunk.getIndex();
        // Set the index to zero.  Is this necessary?
        arrayIndex.set(new int[arrayIndex.getRank()]);
        
        List<Float> valueList = new ArrayList<Float>();
        for (int index = 0; index < size; index++) {
            int i = index % iSize;
            int j = index / iSize;
            arrayIndex.setDim(iIndexInArray, i);
            arrayIndex.setDim(jIndexInArray, j);
            valueList.add(dataChunk.readFloatValue(arrayIndex));
        }

        final GridAxis xAxis = new GridAxis() {
            @Override
            public int size() {
                return 1+imax-imin;
            }
            
            @Override
            public String getName() {
                return getXAxis().getName();
            }
            
            @Override
            public Extent<Integer> getIndexExtent() {
                return Extents.newExtent(imin, imax);
            }
        };
        final GridAxis yAxis = new GridAxis() {
            @Override
            public int size() {
                return 1+jmax-jmin;
            }
            
            @Override
            public String getName() {
                return getYAxis().getName();
            }
            
            @Override
            public Extent<Integer> getIndexExtent() {
                return Extents.newExtent(jmin, jmax);
            }
        };
        
        Grid newGrid = new Grid() {
            @Override
            public long size() {
                return size;
            }
            
            @Override
            public GridAxis getYAxis() {
                return yAxis;
            }
            
            @Override
            public GridAxis getXAxis() {
                return xAxis;
            }
            
            @Override
            public long getIndex(int i, int j) {
                return i + iSize * j;
            }
            
            @Override
            public long getIndex(GridCoordinates2D coords) {
                return getIndex(coords.getXIndex(), coords.getYIndex());
            }
            
            @Override
            public GridExtent getGridExtent() {
                return new GridExtentImpl(Extents.newExtent(imin, imax), Extents.newExtent(jmin, jmax));
            }
            
            @Override
            public GridCoordinates2D getCoords(long index) {
                return new GridCoordinates2DImpl((int) index % iSize, (int) index / iSize);
            }
        };
        
        return new FloatListGridValuesMatrix(newGrid, valueList);
    }

    @Override
    public Class<Float> getValueType() {
        return Float.class;
    }

    @Override
    public void close() {

    }

}
