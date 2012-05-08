/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.coverage.grid.Grid;
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridExtent;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * Implementation of {@link GridValuesMatrix}.
 * @param <E> The type of the values contained in the grid
 * @author Jon
 */
public final class GridValuesMatrixImpl<E> implements GridValuesMatrix<E>
{
    
    private final Grid grid;
    private final BigList<E> values;
    
    /**
     * Creates a GridValuesMatrix whose geometry is taken from the given Grid,
     * and whose values are taken from the given BigList.
     * @throws IllegalArgumentException if the sizes of the Grid and the BigList
     * do not match
     */
    public GridValuesMatrixImpl(Grid grid, BigList<E> values)
    {
        if (grid.size() != values.sizeAsLong()) {
            throw new IllegalArgumentException("Sizes of grid and values don't match");
        }
        this.grid = grid;
        this.values = values;
    }

    @Override
    public BigList<E> getValues() {
        return this.values;
    }

    @Override
    public Class<E> getValueType() {
        return this.values.getValueType();
    }

    @Override
    public GridAxis getXAxis() {
        return this.grid.getXAxis();
    }

    @Override
    public GridAxis getYAxis() {
        return this.grid.getYAxis();
    }

    @Override
    public GridExtent getGridExtent() {
        return this.grid.getGridExtent();
    }

    @Override
    public long size() {
        return this.values.sizeAsLong();
    }

    @Override
    public GridCoordinates2D getCoords(long index) {
        return this.grid.getCoords(index);
    }

    @Override
    public long getIndex(GridCoordinates2D coords) {
        return this.grid.getIndex(coords);
    }

    @Override
    public long getIndex(int i, int j) {
        return this.grid.getIndex(i, j);
    }
    
}
