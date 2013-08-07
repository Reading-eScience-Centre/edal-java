package uk.ac.rdg.resc.edal.util;

public abstract class Array4D implements Array<Number> {

    @Override
    public final int getNDim() {
        return 4;
    }

    @Override
    public Number get(int... coords) {
        if (coords == null || coords.length != 4) {
            throw new IllegalArgumentException("Wrong number of co-ordinates (" + coords.length
                    + ") for this Array (needs 4)");
        }
        return get(coords);
    }

    @Override
    public void set(Number value, int... coords) {
        if (coords.length != 4) {
            throw new IllegalArgumentException("Wrong number of co-ordinates (" + coords.length
                    + ") for this Array (needs 4)");
        }
        set(value, coords);
    }
    
    @Override
    public Class<Number> getValueClass() {
        return Number.class;
    }
}
