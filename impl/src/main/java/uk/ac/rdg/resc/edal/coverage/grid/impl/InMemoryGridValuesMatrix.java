/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;

/**
 * Skeletal implementation of a GridValuesMatrix that holds data in memory.
 * All operations can be based on readPoint() without the loss of much
 * efficiency and the close() operation has no effect.
 * @author Jon
 */
public abstract class InMemoryGridValuesMatrix<E> extends AbstractGridValuesMatrix<E>
{

    @Override
    public GridValuesMatrix<E> readBlock(final int imin, final int imax, final int jmin, final int jmax)
    {
        final int iSize = imax - imin + 1;
        final int jSize = jmax - jmin + 1;
        // The GridAxisImpl constructors will throw IllegalArgumentExceptions if
        // the sizes are less than 1.
        final GridAxis xAxis = new GridAxisImpl(this.getXAxis().getName(), iSize);
        final GridAxis yAxis = new GridAxisImpl(this.getYAxis().getName(), jSize);
        
        // This GridValuesMatrix wraps the parent one, without allocating new
        // storage
        return new InMemoryGridValuesMatrix<E>()
        {
            @Override
            public GridAxis getXAxis() { return xAxis; }

            @Override
            public GridAxis getYAxis() { return yAxis; }

            @Override
            public E readPoint(int i, int j) {
                i -= imin;
                j -= jmin;
                return InMemoryGridValuesMatrix.this.readPoint(i, j);
            }

            @Override
            public Class<E> getValueType() {
                return InMemoryGridValuesMatrix.this.getValueType();
            }
        };
    }

    @Override
    public void close() { /* Do nothing */ }
    
}
