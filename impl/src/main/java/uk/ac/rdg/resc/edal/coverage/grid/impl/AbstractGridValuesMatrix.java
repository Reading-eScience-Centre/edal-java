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
import uk.ac.rdg.resc.edal.util.AbstractBigList;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * Skeletal implementation of {@link GridValuesMatrix}.
 * @param <E> The type of the values contained in the grid
 * @author Jon
 */
public abstract class AbstractGridValuesMatrix<E> implements GridValuesMatrix<E>
{
    private final Grid grid;
    
    /**
     * Creates a GridValuesMatrix whose geometry is taken from the given Grid,
     * and whose values are taken from the given BigList.
     * @throws IllegalArgumentException if the sizes of the Grid and the BigList
     * do not match
     */
    public AbstractGridValuesMatrix(Grid grid)
    {
        this.grid = grid;
    }

    /**
     * Returns a BigList that uses this GridValuesMatrix to obtain values.
     * Note that none of the methods in this BigList close the parent
     * GridValuesMatrix, so users must be careful to close the GridValuesMatrix
     * when the BigList is no longer required.
     * @todo implement getAll() based on something more efficient than
     * repeated calls to get().
     * @return 
     */
    @Override
    public BigList<E> getValues() {
        return new AbstractBigList<E>()
        {
            @Override public E get(long index)
            {
                GridCoordinates2D coords = getCoords(index);
                E value = readPoint(coords.getXIndex(), coords.getYIndex());
                return value;
            }

            @Override public Class<E> getValueType()
            {
                return AbstractGridValuesMatrix.this.getValueType();
            }

            @Override public long sizeAsLong()
            {
                return AbstractGridValuesMatrix.this.size();
            }
        };
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
        return this.grid.size();
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
