/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.List;
import uk.ac.rdg.resc.edal.coverage.grid.Grid;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;

/**
 * Skeletal implementation of a GridValuesMatrix that holds data on disk.
 * All operations can be based on readBlock() without the loss of much
 * efficiency and the close() operation releases any resources (e.g. file handles)
 * @todo NEED TO MAKE SURE ALL GRIDVALUEMATRIXes are properly closed!  Can we simply
 * define that readBlock() will always return an in-memory GVM that does not need
 * to be closed?  Or should readBlock() return a different type?
 * @author Jon
 */
public abstract class DiskBasedGridValuesMatrix<E> extends AbstractGridValuesMatrix<E>
{
    
    public DiskBasedGridValuesMatrix(Grid grid) {
        super(grid);
    }
    
    @Override
    public E readPoint(int i, int j)
    {
        GridValuesMatrix<E> vals = this.readBlock(i, i, j, j);
        E val = vals.readPoint(i, j);
        vals.close();
        return val;
    }

    @Override
    public List<E> readScanline(int j, int imin, int imax)
    {
        GridValuesMatrix<E> vals = this.readBlock(imin, imax, j, j);
        return vals.getValues();
        // TODO what do we do about closing the GVM?  See comments above.
    }
    
    
}
