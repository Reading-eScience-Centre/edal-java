package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.ArrayList;
import java.util.List;

import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.PointSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.PointSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.domain.impl.PointSeriesDomainImpl;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.position.TimePosition;

public class PointSeriesSimpleCoverage<T> extends AbstractDiscreteSimpleCoverage<TimePosition, TimePosition, T> implements PointSeriesCoverage<T> {

    private List<T> values;
    private ScalarMetadata<T> metadata;
    private String description;
    private PointSeriesDomain domain;
    
    public PointSeriesSimpleCoverage(GridSeriesCoverage<T> coverage, List<T> values) {
        this.values = values;
        // Can't enforce that GridSeriesCoverage<T>.getRangeMetadata() should return ScalarMetadata<T>
        metadata = (ScalarMetadata<T>)coverage.getRangeMetadata(null);
        description = coverage.getDescription();
        List<TimePosition> times = new ArrayList<TimePosition>();
        TimeAxis tAxis = coverage.getDomain().getTimeAxis(); 
        for(int i=0; i<tAxis.size(); i++){
            times.add(tAxis.getCoordinateValue(i));
        }
        domain = new PointSeriesDomainImpl(times);
    }

    @Override
    public ScalarMetadata<T> getRangeMetadata() {
        return metadata;
    }

    @Override
    public PointSeriesDomain getDomain() {
        return domain;
    }

    @Override
    public List<T> getValues() {
        return values;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
