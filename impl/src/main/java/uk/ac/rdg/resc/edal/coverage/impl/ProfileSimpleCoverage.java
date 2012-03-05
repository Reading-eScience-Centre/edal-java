package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.ProfileCoverage;
import uk.ac.rdg.resc.edal.coverage.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.domain.ProfileDomain;
import uk.ac.rdg.resc.edal.coverage.domain.impl.ProfileDomainImpl;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

public class ProfileSimpleCoverage<R> extends AbstractDiscreteSimpleCoverage<VerticalPosition, VerticalPosition, R> implements ProfileCoverage<R>{

    private List<R> values;
    private RangeMetadata metadata;
    private String description;
    private ProfileDomain domain;
    
    public ProfileSimpleCoverage(GridSeriesCoverage<R> coverage, List<R> values) {
        this.values = values;
        metadata = coverage.getRangeMetadata(null);
        description = coverage.getDescription();
        List<Double> elevations = new ArrayList<Double>();
        VerticalAxis vAxis = coverage.getDomain().getVerticalAxis();
        if(vAxis != null){
            elevations.addAll(vAxis.getCoordinateValues());
        }
        domain = new ProfileDomainImpl(elevations, vAxis == null ? null : vAxis.getVerticalCrs());
    }

    @Override
    protected RangeMetadata getRangeMetadata() {
        return metadata;
    }

    @Override
    public ProfileDomain getDomain() {
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
