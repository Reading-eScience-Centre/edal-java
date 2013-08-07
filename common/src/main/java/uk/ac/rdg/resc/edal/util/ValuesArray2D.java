package uk.ac.rdg.resc.edal.util;

import java.util.Iterator;

public class ValuesArray2D extends Array2D {

    private int[] shape = new int[2];
    private Double[][] data;

    private static final int X_IND = 1;
    private static final int Y_IND = 0;

    public ValuesArray2D(int ySize, int xSize) {
        if (xSize < 1 || ySize < 1) {
            throw new IllegalArgumentException("All dimension sizes must be at least 1");
        }
        shape[X_IND] = xSize;
        shape[Y_IND] = ySize;

        data = new Double[ySize][xSize];
    }

    @Override
    public int[] getShape() {
        return shape;
    }

    @Override
    public Iterator<Number> iterator() {
        return new Iterator<Number>() {
            private int xCounter = 0;
            private int yCounter = 0;

            boolean done = false;

            @Override
            public boolean hasNext() {
                return (!done);
            }

            @Override
            public Number next() {
                Number value = data[yCounter][xCounter];
                /*
                 * Increment the counters if necessary, resetting to zero if
                 * necessary
                 */
                if (xCounter++ >= shape[X_IND]) {
                    xCounter = 0;
                    if (yCounter++ >= shape[Y_IND]) {
                        yCounter = 0;
                        done = true;
                    }
                }
                return value;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove is not supported for this iterator");
            }
        };
    }

    @Override
    public Number get(int... coords) {
        if (coords.length != 2) {
            throw new IllegalArgumentException("Wrong number of co-ordinates (" + coords.length
                    + ") for this Array (needs 2)");
        }
        return data[coords[Y_IND]][coords[X_IND]];
    }

    @Override
    public void set(Number value, int... coords) {
        if (coords.length != 4) {
            throw new IllegalArgumentException("Wrong number of co-ordinates (" + coords.length
                    + ") for this Array (needs 2)");
        }
        data[coords[Y_IND]][coords[X_IND]] = (value == null) ? null
                : value.doubleValue();
    }

    @Override
    public long size() {
        return shape[X_IND] * shape[Y_IND];
    }
}
