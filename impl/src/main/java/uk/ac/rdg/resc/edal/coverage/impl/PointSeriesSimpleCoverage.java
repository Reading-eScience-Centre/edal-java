package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.ArrayList;
import java.util.List;

import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.PointSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.domain.PointSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.domain.impl.PointSeriesDomainImpl;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.position.TimePosition;

public class PointSeriesSimpleCoverage<R> extends AbstractDiscreteSimpleCoverage<TimePosition, TimePosition, R> implements PointSeriesCoverage<R> {

    private List<R> values;
    private RangeMetadata metadata;
    private String description;
    private PointSeriesDomain domain;
    
    public PointSeriesSimpleCoverage(List<TimePosition> times, List<R> values, String description, RangeMetadata metadata) {
        if(values.size() != times.size())
            throw new IllegalArgumentException("Number of values must equal the number of times");
        this.values = values;
        this.metadata = metadata;
        this.description = description;
        domain = new PointSeriesDomainImpl(times);
    }
    
    public PointSeriesSimpleCoverage(GridSeriesCoverage<R> coverage, List<R> values) {
        this.values = values;
        metadata = coverage.getRangeMetadata(null);
        description = coverage.getDescription();
        List<TimePosition> times = new ArrayList<TimePosition>();
        TimeAxis tAxis = coverage.getDomain().getTimeAxis(); 
        for(int i=0; i<tAxis.size(); i++){
            times.add(tAxis.getCoordinateValue(i));
        }
        if(values.size() != times.size())
            throw new IllegalArgumentException("Number of values must equal the number of times");
        domain = new PointSeriesDomainImpl(times);
    }

    @Override
    protected RangeMetadata getRangeMetadata() {
        return metadata;
    }

    @Override
    public PointSeriesDomain getDomain() {
        return domain;
    }

    @Override
    public List<R> getValues() {
        return values;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
