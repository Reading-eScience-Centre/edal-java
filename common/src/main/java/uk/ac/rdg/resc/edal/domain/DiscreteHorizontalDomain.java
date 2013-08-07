package uk.ac.rdg.resc.edal.domain;

import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * Interface marking a domain which is both a {@link HorizontalDomain} and a
 * {@link DiscreteDomain}.
 * 
 * @author Guy
 */
public interface DiscreteHorizontalDomain<DO> extends DiscreteDomain<HorizontalPosition, DO>,
        HorizontalDomain {
    /**
     * Returns the size of this domain
     */
    public long size();
}
