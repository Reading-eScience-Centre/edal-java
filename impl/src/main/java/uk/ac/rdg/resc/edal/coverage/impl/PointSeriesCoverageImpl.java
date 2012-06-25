package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.HashMap;
import java.util.Map;

import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.PointSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.PointSeriesDomain;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * A mutable (only adding new members is supported) in-memory implementation of
 * {@link PointSeriesCoverage}
 * 
 * @author Guy Griffiths
 * 
 */
public class PointSeriesCoverageImpl extends
        AbstractMultimemberDiscreteCoverage<TimePosition, TimePosition, PointSeriesDomain>
        implements PointSeriesCoverage {

    private Map<String, BigList<?>> memberName2Values;

    public PointSeriesCoverageImpl(String description, PointSeriesDomain domain) {
        super(description, domain);
        memberName2Values = new HashMap<String, BigList<?>>();
    }

    public void addMember(String memberName, PointSeriesDomain domain, String description,
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
