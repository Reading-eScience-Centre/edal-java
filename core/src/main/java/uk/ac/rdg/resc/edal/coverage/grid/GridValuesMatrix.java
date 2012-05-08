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
    
    public BigList<E> getValues();
    
    public Class<E> getValueType();
    
}
