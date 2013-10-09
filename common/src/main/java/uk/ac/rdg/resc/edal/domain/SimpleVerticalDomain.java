package uk.ac.rdg.resc.edal.domain;

import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * A simple {@link VerticalDomain} containing just an extent and a
 * {@link VerticalCrs}
 * 
 * @author Guy Griffiths
 */
public class SimpleVerticalDomain implements VerticalDomain {

    private final Extent<Double> extent;
    private final VerticalCrs crs;

    public SimpleVerticalDomain(Double min, Double max, VerticalCrs crs) {
        this.crs = crs;
        if(min == null || max == null) {
            extent = Extents.emptyExtent(Double.class);
        } else {
            extent = Extents.newExtent(min, max);
        }
    }

    @Override
    public boolean contains(Double position) {
        return extent.contains(position);
    }

    @Override
    public Extent<Double> getExtent() {
        return extent;
    }

    @Override
    public VerticalCrs getVerticalCrs() {
        return crs;
    }
}
