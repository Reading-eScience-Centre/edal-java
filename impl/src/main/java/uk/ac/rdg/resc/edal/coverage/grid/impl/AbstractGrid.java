package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.coverage.grid.Grid;
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridExtent;

/**
 * Abstract superclass that implements the {@link #size()} and
 * {@link #getGridExtent()} methods of a Grid
 * based upon the {@link GridAxis GridAxes} that are supplied by subclasses
 * @author Jon
 */
public abstract class AbstractGrid implements Grid
{
    /**
     * {@inheritDoc}
     * <p>This implementation uses the {@link #getGridExtent() GridEnvelope}
     * provided by subclasses.</p>
     */
    @Override
    public final long size() {
        // We reuse code in GridExtentImpl to calculate the size
        return GridExtentImpl.convert(getGridExtent()).size();
    }

    @Override
    public GridExtent getGridExtent() {
        return new GridExtentImpl(getXAxis().getIndexExtent(), getYAxis().getIndexExtent());
    }
    
    /**
     * Gets the long index of the given coordinates, assuming that the i
     * coordinate varies fastest
     */
    @Override
    public long getIndex(int i, int j) {
        // First remove the offsets in the i and j directions
        i -= getXMin();
        j -= getYMin();
        
        return j * (long)this.getXAxis().size() + i;
    }
    
    private int getXMin() { return this.getXAxis().getIndexExtent().getLow(); }
    private int getYMin() { return this.getYAxis().getIndexExtent().getLow(); }
    
    /**
     * Gets the long index of the given coordinates, assuming that the i
     * coordinate varies fastest
     */
    @Override
    public long getIndex(GridCoordinates2D coords) {
        return this.getIndex(coords.getXIndex(), coords.getYIndex());
    }
    
    /**
     * Gets the grid coordinates of the cell at the given index in the grid
     * (the index is constructed so that the i direction varies fastest).
     * @throws IndexOutOfBoundsException if index >= size()
     * @return 
     */
    @Override
    public GridCoordinates2D getCoords(long index) {
        if (index >= this.size()) {
            throw new IndexOutOfBoundsException();
        }
        
        // Calculate the indices assuming that the grid starts at (0,0)
        int xAxisSize = getXAxis().size();
        int i = (int)(index % xAxisSize);
        int j = (int)(index / xAxisSize);

        // Add the offsets (which will usually be zero)
        i += getXMin();
        j += getYMin();
        
        return new GridCoordinates2DImpl(i, j);
    }

}
