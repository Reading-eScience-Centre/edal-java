package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.util.AbstractBigList;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * Skeletal implementation of {@link GridValuesMatrix}.
 * 
 * @param <E>
 *            The type of the values contained in the grid
 * @author Jon
 * @author Guy Griffiths
 */
public abstract class AbstractGridValuesMatrix<E> extends AbstractGrid implements
        GridValuesMatrix<E> {

    @Override
    public final GridAxis getAxis(int n) {
        if (n >= getNDim() || n < 0) {
            throw new IllegalArgumentException("We only have " + getNDim()
                    + " axes, so you may only request from 0 to " + (getNDim() - 1));
        }
        return doGetAxis(n);
    }

    protected abstract GridAxis doGetAxis(int n);

    @Override
    public final E readPoint(int[] coords) {
        if (coords.length != getNDim()) {
            throw new IllegalArgumentException("Number of co-ordinates supplied (" + coords.length
                    + ") must be equal to the number of dimensions (" + getNDim() + ")");
        }
        return doReadPoint(coords);
    }

    protected abstract E doReadPoint(int[] coords);

    @Override
    public final GridValuesMatrix<E> readBlock(int[] mins, int[] maxes) {
        if (mins.length != getNDim() || mins.length != maxes.length) {
            throw new IllegalArgumentException("Number of minimum (" + mins.length
                    + ") and maximum (" + maxes.length
                    + ") values must both be equal to the number of dimensions (" + getNDim() + ")");
        }
        return doReadBlock(mins, maxes);
    }

    protected abstract GridValuesMatrix<E> doReadBlock(int[] mins, int[] maxes);

    /**
     * Returns a BigList that uses this GridValuesMatrix to obtain values. Note
     * that none of the methods in this BigList close the parent
     * GridValuesMatrix, so users must be careful to close the GridValuesMatrix
     * when the BigList is no longer required.
     * 
     * @todo implement getAll() based on something more efficient than repeated
     *       calls to get().
     * @return
     */
    @Override
    public BigList<E> getValues() {
        return new AbstractBigList<E>() {
            @Override
            public E get(long index) {
                GridCoordinates coords = getCoords(index);
                E value = readPoint(coords.getIndices());
                return value;
            }

            @Override
            public long sizeAsLong() {
                return AbstractGridValuesMatrix.this.size();
            }
        };
    }

}
