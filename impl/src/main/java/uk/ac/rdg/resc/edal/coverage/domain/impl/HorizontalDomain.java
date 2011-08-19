package uk.ac.rdg.resc.edal.coverage.domain.impl;

import java.util.Arrays;
import java.util.List;

import uk.ac.rdg.resc.edal.Domain;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * <p>
 * A list of {@link HorizontalPosition}s in a certain coordinate reference
 * system.
 * </p>
 * 
 * @author Guy Griffiths
 */
// TODO Check that this is OK and we do not need a CRS any more
public final class HorizontalDomain implements Domain<HorizontalPosition> {

    private final List<HorizontalPosition> posList;

    /**
     * Creates a HorizontalDomain from the given List of HorizontalPositions
     * with their coordinate reference system
     * 
     * @param list
     *            The x-y points to wrap as a HorizontalDomain
     * @return a new HorizontalDomain that wraps the given list of projection
     *         points
     */
    public HorizontalDomain(List<HorizontalPosition> posList) {
        this.posList = posList;
    }

    /**
     * Creates a HorizontalDomain containing a single point
     * 
     * @param point
     *            The HorizontalPosition to wrap
     * @return a new HorizontalDomain that wraps the given point
     */
    public HorizontalDomain(HorizontalPosition point) {
        this(Arrays.asList(point));
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        return posList.contains(position);
    }
}
