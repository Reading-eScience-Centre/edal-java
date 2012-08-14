/**
 * 
 */
package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.HashMap;
import java.util.Map;

import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.domain.DiscreteDomain;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * An implementation of a coverage backed by a {@link Map} of {@link BigList}s
 * 
 * This can be used to implement an in-memory coverage with minimal effort. In
 * most cases it is sufficient to add the desired interface and implement the
 * constructor
 * 
 * @author Guy Griffiths
 * 
 */
public class AbstractBigListBackedCoverage<P, DO, GD extends DiscreteDomain<P, DO>> extends
        AbstractMultimemberDiscreteCoverage<P, DO, GD> {

    private Map<String, BigList<?>> memberName2Values;

    public AbstractBigListBackedCoverage(String description, GD domain) {
        super(description, domain);
        memberName2Values = new HashMap<String, BigList<?>>();
    }

    public void addMember(String memberName, GD domain, String description, Phenomenon parameter,
            Unit units, BigList<?> values, Class<?> valueType) {
        addMemberToMetadata(memberName, domain, description, parameter, units, valueType);
        memberName2Values.put(memberName, values);
    }

    @Override
    public BigList<?> getValuesList(final String memberName) {
        if (!memberName2Values.containsKey(memberName)) {
            throw new IllegalArgumentException(memberName + " is not contained in this coverage");
        }
        return memberName2Values.get(memberName);
    }
}
