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
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public class NcGridValuesMatrix4D extends AbstractGridValuesMatrix<Float> {

    private final GridAxis xAxis;
    private final GridAxis yAxis;
    private final GridAxis zAxis;
    private final GridAxis tAxis;
    private final NetcdfDataset nc;
    private final GridDatatype gridDatatype;

    public NcGridValuesMatrix4D(GridAxis xAxis, GridAxis yAxis, GridAxis zAxis, GridAxis tAxis,
            String location, String varId) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.zAxis = zAxis;
        this.tAxis = tAxis;
        try {
            this.nc = CdmUtils.openDataset(location);
            this.gridDatatype = CdmUtils.getGridDatatype(nc, varId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private RangesList getRangesList(final int imin, final int jmin, final int kmin,
            final int lmin, final int imax, final int jmax, final int kmax, final int lmax) {
        RangesList ranges = new RangesList(gridDatatype);
        ranges.setTRange(lmin, lmax);
        ranges.setZRange(kmin, kmax);
        ranges.setYRange(jmin, jmax);
        ranges.setXRange(imin, imax);
        return ranges;
    }

    private DataChunk readDataChunk(RangesList ranges) {
        try {
            return DataChunk.readDataChunk(gridDatatype.getVariable(), ranges);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public Class<Float> getValueType() {
        return Float.class;
    }

    @Override
    public void close() {
        CdmUtils.safelyClose(nc);
    }

    @Override
    public int getNDim() {
        return 4;
    }

    @Override
    public GridAxis doGetAxis(int n) {
        switch (n) {
        case 0:
            return xAxis;
        case 1:
            return yAxis;
        case 2:
            return zAxis;
        case 3:
            return tAxis;
        default:
            /*
             * We should never reach this code, because getAxis will already
             * have checked the bounds
             */
            throw new IllegalStateException("Axis index out of bounds");
        }
    }

    @Override
    protected Float doReadPoint(int[] coords) {
        RangesList ranges = this.getRangesList(coords[0], coords[1], coords[2], coords[3],
                coords[0], coords[1], coords[2], coords[3]);
        DataChunk dataChunk = readDataChunk(ranges);
        assert (dataChunk.size() == 1);
        final Index arrayIndex = dataChunk.getIndex();
        // Set index to zero in all directions
        arrayIndex.set(new int[arrayIndex.getRank()]);
        return dataChunk.readFloatValue(arrayIndex);
    }

    @Override
    protected GridValuesMatrix<Float> doReadBlock(final int[] mins, int[] maxes) {
        final int iSize = maxes[0] - mins[0] + 1;
        final int jSize = maxes[1] - mins[1] + 1;
        final int kSize = maxes[2] - mins[2] + 1;
        final int lSize = maxes[3] - mins[3] + 1;
        final long size = (long) iSize * jSize * kSize * lSize;
        final GridAxis newXAxis;
        if (this.getAxis(0) != null)
            newXAxis = new GridAxisImpl(this.getAxis(0).getName(), iSize);
        else
            newXAxis = null;
        final GridAxis newYAxis;
        if (this.getAxis(1) != null)
            newYAxis = new GridAxisImpl(this.getAxis(1).getName(), jSize);
        else
            newYAxis = null;
        final GridAxis newZAxis;
        if (this.getAxis(2) != null)
            newZAxis = new GridAxisImpl(this.getAxis(2).getName(), kSize);
        else
            newZAxis = null;
        final GridAxis newTAxis;
        if (this.getAxis(3) != null)
            newTAxis = new GridAxisImpl(this.getAxis(3).getName(), lSize);
        else
            newTAxis = null;

        // Read the data from disk into memory
        RangesList ranges = this.getRangesList(mins[0], mins[1], mins[2], mins[3], maxes[0],
                maxes[1], maxes[2], maxes[3]);
        final DataChunk dataChunk = readDataChunk(ranges);
        assert (size == dataChunk.size());

        // Return an in-memory GridValuesMatrix that wraps the DataChunk
        final int iIndexInArray = ranges.getXAxisIndex();
        final int jIndexInArray = ranges.getYAxisIndex();
        final int kIndexInArray = ranges.getZAxisIndex();
        final int lIndexInArray = ranges.getTAxisIndex();

        return new InMemoryGridValuesMatrix<Float>() {

            @Override
            public Class<Float> getValueType() {
                return Float.class;
            }

            @Override
            public int getNDim() {
                return 4;
            }

            @Override
            public GridAxis doGetAxis(int n) {
                switch (n) {
                case 0:
                    return newXAxis;
                case 1:
                    return newYAxis;
                case 2:
                    return newZAxis;
                case 3:
                    return newTAxis;
                default:
                    /*
                     * We should never reach this code, because getAxis will
                     * already have checked the bounds
                     */
                    throw new IllegalStateException("Axis index out of bounds");
                }
            }

            @Override
            protected Float doReadPoint(int[] coords) {
                Index arrayIndex = dataChunk.getIndex();
                if (iIndexInArray >= 0)
                    arrayIndex.setDim(iIndexInArray, coords[0]);
                if (jIndexInArray >= 0)
                    arrayIndex.setDim(jIndexInArray, coords[1]);
                if (kIndexInArray >= 0)
                    arrayIndex.setDim(kIndexInArray, coords[2]);
                if (lIndexInArray >= 0)
                    arrayIndex.setDim(lIndexInArray, coords[3]);
                return dataChunk.readFloatValue(arrayIndex);
            }
            
//            @Override
//            public GridValuesMatrix<Float> doReadBlock(final int[] mins, final int[] maxes) {
//                int[] sizes = new int[mins.length];
//                final GridAxis[] axes = new GridAxis[mins.length];
//                for (int i = 0; i < mins.length; i++) {
//                    sizes[i] = maxes[i] - mins[i] + 1;
//                    if (getAxis(i) == null)
//                        axes[i] = null;
//                    else
//                        axes[i] = new GridAxisImpl(this.getAxis(i).getName(), sizes[i]);
//                }
//
//                // This GridValuesMatrix wraps the parent one, without allocating new
//                // storage
//                return new InMemoryGridValuesMatrix<Float>() {
//                    @Override
//                    public Float doReadPoint(int[] indices) {
//                        for (int i = 0; i < indices.length; i++) {
//                            indices[i] += mins[i];
//                        }
//                        return NcGridValuesMatrix4D.this.readPoint(indices);
//                    }
//
//                    @Override
//                    public Class<Float> getValueType() {
//                        return NcGridValuesMatrix4D.this.getValueType();
//                    }
//
//                    @Override
//                    protected GridAxis doGetAxis(int n) {
//                        return axes[n];
//                    }
//
//                    @Override
//                    public int getNDim() {
//                        return NcGridValuesMatrix4D.this.getNDim();
//                    }
//                };
//            }
        };
    }
}
