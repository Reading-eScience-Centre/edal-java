package uk.ac.rdg.resc.edal;

import uk.ac.rdg.resc.edal.position.VerticalPosition;

public class VerticalExtent implements Extent<VerticalPosition> {

    private final VerticalPosition min;
    private final VerticalPosition max;
    
    public VerticalExtent(VerticalPosition min, VerticalPosition max) {
        if(max.getZ() < min.getZ())
            throw new IllegalArgumentException("Minimum value must be less than maximum value");
        this.min = min;
        this.max = max;
    }

    @Override
    public VerticalPosition getHigh() {
        return max;
    }

    @Override
    public VerticalPosition getLow() {
        return min;
    }

    @Override
    public boolean contains(VerticalPosition position) {
        return (position.getZ() >= min.getZ() && position.getZ() <= max.getZ());
    }

    @Override
    public boolean isEmpty() {
        return (min == null && max == null);
    }

}
