package uk.ac.rdg.resc.edal.coverage.domain.impl;

import java.util.Arrays;
import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.Domain;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * A list of {@link HorizontalPosition}s. Each will have a
 * {@link CoordinateReferenceSystem} associated with it. Generally, all
 * positions within the {@link Domain} will belong to the same
 * {@link CoordinateReferenceSystem}
 * 
 * @author Guy Griffiths
 */
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

    /**
     * Gets a list of all objects in the domain
     * 
     * @return a {@link List} of {@link HorizontalPosition}s
     */
    public List<HorizontalPosition> getDomainObjects() {
        return posList;
    }
}
