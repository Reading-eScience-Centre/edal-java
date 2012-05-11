/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    
    public InMemoryGridValuesMatrix(Grid grid) {
        super(grid);
    }

    @Override
    public List<E> readScanline(int j, int imin, int imax)
    {
        int n = imax - imin + 1;
        if (n < 0) throw new IllegalArgumentException("imax must be >= imin");
        if (n == 0) return Collections.emptyList();
        
        List<E> vals = new ArrayList<E>(n);
        for (int i = imin; i <= imax; i++)
        {
            E val = this.readPoint(i, j);
            vals.add(val);
        }
        return vals;
    }

    @Override
    public GridValuesMatrix<E> readBlock(int imin, int imax, int jmin, int jmax) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() { /* Do nothing */ }
    
}
