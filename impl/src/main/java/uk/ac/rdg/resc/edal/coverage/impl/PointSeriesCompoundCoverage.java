package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.PointSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.Record;
import uk.ac.rdg.resc.edal.coverage.domain.PointSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.domain.impl.PointSeriesDomainImpl;
import uk.ac.rdg.resc.edal.position.TimePosition;

public class PointSeriesCompoundCoverage extends
        AbstractDiscreteCompoundCoverage<TimePosition, TimePosition> implements
        PointSeriesCoverage<Record> {

    private PointSeriesDomain domain;
    private Map<String, List<?>> memberValues;
    private Map<String, RangeMetadata> metadata;
    private String description;

    public PointSeriesCompoundCoverage(Map<String, List<?>> memberValues, List<TimePosition> times,
            Map<String, RangeMetadata> metadata, String description) {
        if (!memberValues.keySet().equals(metadata.keySet())) {
            throw new IllegalArgumentException(
                    "Must provide metadata corresponding to each member of the compound coverage");
        }
        metadata.put(null, new RangeMetadataImpl("Compound data", null, Unit.getUnit("N/A"), Record.class));
        domain = new PointSeriesDomainImpl(times);
        this.memberValues = memberValues;
        this.metadata = metadata;
        this.description = description;
    }

    @Override
    public PointSeriesDomain getDomain() {
        return domain;
    }

    @Override
    public List<?> getValues(String memberName) {
        List<?> ret = memberValues.get(memberName);
        if (ret == null) {
            /*
             * TODO is this fine, or should we just return null?
             */
            throw new IllegalArgumentException("The field " + memberName
                    + " is not present in this PointSeriesCoverage");
        }
        return ret;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Set<String> getMemberNames() {
        return memberValues.keySet();
    }

    @Override
    public RangeMetadata getRangeMetadata(String memberName) {
        RangeMetadata ret = metadata.get(memberName);
        if (ret == null) {
//            return new RangeMetadataImpl("Record containing multiple fields", Phenomenon.getPhenomenon("record"), Unit.getUnit("multiple"), Record.class);
            /*
             * TODO is this fine, or should we just return null?
             */
            throw new IllegalArgumentException("The field " + memberName
                    + " is not present in this PointSeriesCoverage");
        }
        return ret;
    }
}
