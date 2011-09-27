package uk.ac.rdg.resc.edal.coverage;

import java.util.ArrayList;
import java.util.List;

import uk.ac.rdg.resc.edal.coverage.domain.PointSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.domain.impl.PointSeriesDomainImpl;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.impl.AbstractDiscreteSimpleCoverage;
import uk.ac.rdg.resc.edal.position.TimePosition;

public class PointSeriesSimpleCoverage<R> extends AbstractDiscreteSimpleCoverage<TimePosition, TimePosition, R> implements PointSeriesCoverage<R> {

    private List<R> values;
    private RangeMetadata metadata;
    private String description;
    private PointSeriesDomain domain;
    
    public PointSeriesSimpleCoverage(GridSeriesCoverage<R> coverage, List<R> values) {
        super();
        this.values = values;
        metadata = coverage.getRangeMetadata(null);
        description = coverage.getDescription();
        List<TimePosition> times = new ArrayList<TimePosition>();
        TimeAxis tAxis = coverage.getDomain().getTimeAxis(); 
        for(int i=0; i<tAxis.size(); i++){
            times.add(tAxis.getCoordinateValue(i));
        }
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
