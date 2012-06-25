package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;

/**
 * Skeletal implementation of a GridValuesMatrix that holds data in memory. All
 * operations can be based on readPoint() without the loss of much efficiency
 * and the close() operation has no effect.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public abstract class InMemoryGridValuesMatrix<E> extends AbstractGridValuesMatrix<E> {

    @Override
    public GridValuesMatrix<E> doReadBlock(final int[] mins, final int[] maxes) {
        int[] sizes = new int[mins.length];
        final GridAxis[] axes = new GridAxis[mins.length];
        for (int i = 0; i < mins.length; i++) {
            sizes[i] = maxes[i] - mins[i] + 1;
            axes[i] = new GridAxisImpl(this.getAxis(i).getName(), sizes[i]);
        }

        // This GridValuesMatrix wraps the parent one, without allocating new
        // storage
        return new InMemoryGridValuesMatrix<E>() {
            @Override
            public E doReadPoint(int[] indices) {
                for (int i = 0; i < indices.length; i++) {
                    indices[i] -= mins[i];
                }
                return InMemoryGridValuesMatrix.this.readPoint(indices);
            }

            @Override
            public Class<E> getValueType() {
                return InMemoryGridValuesMatrix.this.getValueType();
            }

            @Override
            protected GridAxis doGetAxis(int n) {
                return axes[n];
            }

            @Override
            public int getNDim() {
                return InMemoryGridValuesMatrix.this.getNDim();
            }
        };
    }

    @Override
    public void close() { /* Do nothing */
    }
}
