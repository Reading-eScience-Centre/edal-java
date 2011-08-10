package uk.ac.rdg.resc.edal;

/**
 * An immutable implementation of {@link Extent} whose values are all
 * {@link Double}s
 * 
 * @author Guy Griffiths
 * 
 */
public final class GeneralExtent implements Extent<Double> {

    private final Double max;
    private final Double min;

    /**
     * Instantiate a new {@link GeneralExtent}. This object will be immutable
     * 
     * @param min
     *            the low value of the extent
     * @param max
     *            the high value of the extent
     */
    public GeneralExtent(Double min, Double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public Double getHigh() {
        return max;
    }

    @Override
    public Double getLow() {
        return min;
    }

    @Override
    public boolean contains(Double position) {
        return (position >= min && position <= max);
    }

}
