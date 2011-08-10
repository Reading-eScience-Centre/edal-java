package uk.ac.rdg.resc.edal;

/**
 * An immutable implementation of {@link Extent} whose values are all
 * {@link Integer}s
 * 
 * @author Guy Griffiths
 * 
 */
public final class IntegerExtent implements Extent<Integer> {

    private final Integer max;
    private final Integer min;

    /**
     * Instantiate a new {@link IntegerExtent}. This object will be immutable
     * 
     * @param min
     *            the low value of the extent
     * @param max
     *            the high value of the extent
     */
    public IntegerExtent(Integer min, Integer max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public Integer getHigh() {
        return max;
    }

    @Override
    public Integer getLow() {
        return min;
    }

    @Override
    public boolean contains(Integer index) {
        return (index >= min && index <= max);
    }

}
