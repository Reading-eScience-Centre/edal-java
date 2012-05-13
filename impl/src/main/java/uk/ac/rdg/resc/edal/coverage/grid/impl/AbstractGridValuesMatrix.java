/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.util.AbstractBigList;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * Skeletal implementation of {@link GridValuesMatrix}.
 * TODO: is it better to wrap a Grid or to implement AbstractGrid?
 * @param <E> The type of the values contained in the grid
 * @author Jon
 */
public abstract class AbstractGridValuesMatrix<E> extends AbstractGrid implements GridValuesMatrix<E>
{
    private final Class<E> valueType;
    
    /**
     * Creates a GridValuesMatrix whose geometry is taken from the given Grid.
     */
    public AbstractGridValuesMatrix(Class<E> valueType)
    {
        this.valueType = valueType;
    }
    
    @Override
    public final Class<E> getValueType()
    {
        return this.valueType;
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
    
}
