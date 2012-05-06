/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.ArrayList;
import java.util.List;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;

/**
 * Skeletal implementation of {@link GridValuesMatrix}.
 * @param <E> The type of the values contained in the grid
 * @author Jon
 */
public abstract class AbstractGridValuesMatrix<E> extends AbstractGrid implements GridValuesMatrix<E>
{

    /**
     * {@inheritDoc}
     * <p>This default implementation simply calls {@link #getValue(int, int)}
     * repeatedly to build up the list of values.  Subclasses may wish to provide
     * a more efficient implementation.</p>
     */
    @Override
    public List<E> getScanline(int j, int imin, int imax) {
        List<E> scanline = new ArrayList<E>(imax - imin + 1);
        for (int i = imin; i <= imax; i++) {
            scanline.add(this.getValue(i, j));
        }
        return scanline;
    }

    /**
     * {@inheritDoc}
     * <p>This default implementation simply calls {@link #getValue(int, int)}
     * repeatedly to build up the grid of values.  Subclasses may wish to provide
     * a more efficient implementation.</p>
     */
    @Override
    public GridValuesMatrix<E> getGrid(int imin, int imax, int jmin, int jmax) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
