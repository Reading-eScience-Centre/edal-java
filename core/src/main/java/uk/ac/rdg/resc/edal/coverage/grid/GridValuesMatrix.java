/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.grid;

import java.util.List;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * A {@link Grid} that contains values
 * @param <E> The type of values in the matrix
 * @author Jon
 */
public interface GridValuesMatrix<E> extends Grid {
    
    public E readPoint(int i, int j);
    
    public List<E> readScanline(int j, int imin, int imax);
    
    public GridValuesMatrix<E> readBlock(int imin, int imax, int jmin, int jmax);
    
    public BigList<E> getValues();
    
    public Class<E> getValueType();
    
    /**
     * Frees any resources associated with the grid.
     * <p>For disk-based data storage,
     * this will close any open file handles, after which the GridValuesMatrix
     * will no longer return any values.  The user must then retrieve a new
     * GridValuesMatrix from the coverage in question.</p>
     * <p>For in-memory data storage, the call to close() may be ignored.</p>
     */
    public void close();
    
}
