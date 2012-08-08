package uk.ac.rdg.resc.edal.coverage.domain.impl;

import java.util.List;

import uk.ac.rdg.resc.edal.coverage.domain.DiscreteDomain;

/**
 * A partial implementation of a domain whose domain objects are of the same
 * type as the objects used to identify them. This implies that
 * {@link DiscreteDomain#contains(Object)} will only return true if the exact
 * object is present i.e. not if the domain just spans a range containing that
 * object. This is implied by the fact that this class implements
 * {@link DiscreteDomain} with both parameters the same.
 * 
 * @author Guy Griffiths
 * 
 * @param <P>
 */
public abstract class AbstractPointDomain<P> implements DiscreteDomain<P, P> {

    private List<P> values;

    public AbstractPointDomain(List<P> values) {
        this.values = values;
    }
    
    @Override
    public boolean contains(P position) {
        return values.contains(position);
    }

    @Override
    public List<P> getDomainObjects() {
        return values;
    }

    @Override
    public long findIndexOf(P pos) {
        return values.indexOf(pos);
    }

    @Override
    public long size() {
        return values.size();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("unchecked")
        AbstractPointDomain<P> other = (AbstractPointDomain<P>) obj;
        if (values == null) {
            if (other.values != null)
                return false;
        } else if (!values.equals(other.values))
            return false;
        return true;
    }
}
