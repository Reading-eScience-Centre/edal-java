/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.coverage.grid.Grid;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;

/**
 * Skeletal implementation of a GridValuesMatrix that holds data in memory.
 * All operations can be based on readPoint() without the loss of much
 * efficiency and the close() operation has no effect.
 * @author Jon
 */
public abstract class InMemoryGridValuesMatrix<E> extends AbstractGridValuesMatrix<E>
{
    
    public InMemoryGridValuesMatrix(Grid grid, Class<E> valueType) {
        super(grid, valueType);
    }

    @Override
    public GridValuesMatrix<E> readBlock(int imin, int imax, int jmin, int jmax) {
        // TODO: is there an easy way to abstract out the creation of a Grid
        // subset (i.e. axis names retained from parent Grid, axis sizes read
        // from imin,imax,jmin,jmax).  We would use this in readBlock() implementations
        // for both in-memory and grid-based GVMs.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() { /* Do nothing */ }
    
}
