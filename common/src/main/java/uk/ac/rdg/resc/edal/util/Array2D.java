package uk.ac.rdg.resc.edal.util;

public abstract class Array2D implements Array<Number> {

    @Override
    public final int getNDim() {
        return 2;
    }

    @Override
    public Number get(int... coords) {
        if (coords == null || coords.length != 2) {
            throw new IllegalArgumentException("Wrong number of co-ordinates (" + coords.length
                    + ") for this Array (needs 2)");
        }
        return get(coords);
    }

    @Override
    public void set(Number value, int... coords) {
        if (coords.length != 2) {
            throw new IllegalArgumentException("Wrong number of co-ordinates (" + coords.length
                    + ") for this Array (needs 2)");
        }
        set(value, coords);
    }
    
    @Override
    public Class<Number> getValueClass() {
        return Number.class;
    }
}
