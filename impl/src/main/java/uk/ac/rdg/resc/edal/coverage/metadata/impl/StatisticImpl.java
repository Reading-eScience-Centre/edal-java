package uk.ac.rdg.resc.edal.coverage.metadata.impl;

import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.Statistic;
import uk.ac.rdg.resc.edal.coverage.metadata.StatisticsCollection;

public class StatisticImpl extends ScalarMetadataImpl implements Statistic {

    private StatisticType statisticType;

    public StatisticImpl(String name, String description, Phenomenon parameter, Unit units,
            Class<?> clazz, StatisticType statisticType) {
        super(name, description, parameter, units, clazz);
        this.statisticType = statisticType;
    }

    @Override
    public StatisticType getStatisticType() {
        return statisticType;
    }

    @Override
    public StatisticsCollection getParent() {
        return (StatisticsCollection) super.getParent();
    }

    @Override
    public void setParentMetadata(RangeMetadata parent) {
        if (!(parent instanceof StatisticsCollection)) {
            throw new IllegalArgumentException(
                    "Parent metadata of a statistic must be a StatisticCollection");
        }
        super.setParentMetadata(parent);
    }

    @Override
    public ScalarMetadata clone() throws CloneNotSupportedException {
        return new StatisticImpl(getName(), getDescription(), getParameter(), getUnits(),
                getValueType(), statisticType);
    }
}
