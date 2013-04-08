package uk.ac.rdg.resc.edal.coverage.metadata.impl;

import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.Statistic;
import uk.ac.rdg.resc.edal.coverage.metadata.StatisticsCollection;

public class StatisticCollectionImpl extends RangeMetadataImpl implements StatisticsCollection {

    private Phenomenon parameter;

    public StatisticCollectionImpl(String name, String description, Phenomenon parameter) {
        super(name, description);
        this.parameter = parameter;
    }

    public void addMember(Statistic metadata) {
        super.addMember(metadata);
    }
    
    @Override
    public Phenomenon getParameter() {
        return parameter;
    }
    
    @Override
    public RangeMetadata clone() throws CloneNotSupportedException {
        StatisticCollectionImpl clone = new StatisticCollectionImpl(getName(), getDescription(),
                parameter);
        for (RangeMetadata member : members.values()) {
            clone.addMember(member.clone());
        }
        return clone;
    }
}
