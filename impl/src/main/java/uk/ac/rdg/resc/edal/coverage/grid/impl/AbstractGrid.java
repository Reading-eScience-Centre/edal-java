package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.coverage.grid.Grid;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.coverage.grid.GridExtent;

/**
 * Partial implementation of a {@link Grid}
 * 
 * @author Guy Griffiths
 * 
 */
public abstract class AbstractGrid implements Grid {

    @Override
    public long size() {
        // We reuse code in GridExtentImpl to calculate the size
        return GridExtentImpl.convert(getGridExtent()).size();
    }

    @Override
    public GridExtent getGridExtent() {
        int[] low = new int[getNDim()];
        int[] high = new int[getNDim()];
        for (int i = 0; i < getNDim(); i++) {
            low[i] = getAxis(i).getIndexExtent().getLow();
            high[i] = getAxis(i).getIndexExtent().getHigh();
        }
        return new GridExtentImpl(new GridCoordinatesImpl(low), new GridCoordinatesImpl(high));
    }

    @Override
    public GridCoordinates getCoords(long index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Index must be between 0 and " + size());
        }
        int[] coords = new int[getNDim()];
        for (int i = 0; i < getNDim(); i++) {
            int size = 1;
            if (getAxis(i) != null) {
                size = getAxis(i).size();
            }
            coords[i] = (int) (index % size) + getMin(i);

            index = (index - coords[i]) / getAxisSize(i);
        }
        return new GridCoordinatesImpl(coords);
    }

    @Override
    public long getIndex(GridCoordinates coords) {
        long index = 0;
        for (int i = 0; i < getNDim(); i++) {
            if (!getAxis(i).getIndexExtent().contains(coords.getIndex(i))) {
                throw new IndexOutOfBoundsException("Index out of bounds on axis " + i);
            }

            int thisIndex = coords.getIndex(i) - getMin(i);

            int size = 1;
            for (int j = 0; j < i; j++) {
                size *= getAxisSize(j);
            }
            index += thisIndex * size;
        }
        return index;
    }

    private int getMin(int dim) {
        if (getAxis(dim) == null)
            return 0;
        return this.getAxis(dim).getIndexExtent().getLow();
    }

    private int getAxisSize(int dim) {
        if (getAxis(dim) == null)
            return 1;
        return this.getAxis(dim).size();
    }

    @Override
    public long getIndex(int... indices) {
        return getIndex(new GridCoordinatesImpl(indices));
    }
}
