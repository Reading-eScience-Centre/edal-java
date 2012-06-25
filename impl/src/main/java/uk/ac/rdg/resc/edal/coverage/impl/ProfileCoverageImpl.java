package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.HashMap;
import java.util.Map;

import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.ProfileCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.ProfileDomain;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * A mutable (only adding new members is supported) in-memory implementation of
 * {@link ProfileCoverage}
 * 
 * @author Guy Griffiths
 * 
 */
public class ProfileCoverageImpl extends
        AbstractMultimemberDiscreteCoverage<VerticalPosition, VerticalPosition, ProfileDomain>
        implements ProfileCoverage {

    private Map<String, BigList<?>> memberName2Values;

    public ProfileCoverageImpl(String description, ProfileDomain domain) {
        super(description, domain);
        memberName2Values = new HashMap<String, BigList<?>>();
    }

    public void addMember(String memberName, ProfileDomain domain, String description,
            Phenomenon parameter, Unit units, BigList<?> values) {
        addMemberToMetadata(memberName, domain, description, parameter, units);
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
