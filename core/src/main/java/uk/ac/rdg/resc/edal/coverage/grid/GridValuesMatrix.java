/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.grid;

import uk.ac.rdg.resc.edal.util.BigList;

/**
 * A {@link Grid} that contains values
 * @param <E> The type of values in the matrix
 * @author Jon
 */
public interface GridValuesMatrix<E> extends Grid {
    
    /**
     * Reads a single point from the grid.  For disk-based GridValuesMatrixes
     * it will usually be more efficient to call readBlock() to read data in
     * bulk.
     * @throws IndexOutOfBoundsException if either i or j is out of bounds
     */
    public E readPoint(int i, int j);
    
    /**
     * Returns an in-memory GridValuesMatrix holding values from the given subset
     * of this object.
     */
    public GridValuesMatrix<E> readBlock(int imin, int imax, int jmin, int jmax);
    
    /**
     * Returns a representation of the values in this object as a BigList.
     * For disk-based GridValuesMatrixes, it will not usually be efficient to
     * use BigList.get() to read values.  Instead use BigList.getAll() or
     * this.readBlock().getValues() to get an in-memory structure.
     */
    public BigList<E> getValues();
    
    /**
     * @return the class which this {@link GridValuesMatrix} contains.
     */
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
