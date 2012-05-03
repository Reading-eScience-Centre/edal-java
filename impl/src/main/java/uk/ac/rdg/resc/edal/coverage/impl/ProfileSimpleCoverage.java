package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.ArrayList;
import java.util.List;

import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.ProfileCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.ProfileDomain;
import uk.ac.rdg.resc.edal.coverage.domain.impl.ProfileDomainImpl;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

public class ProfileSimpleCoverage<T> extends AbstractDiscreteSimpleCoverage<VerticalPosition, VerticalPosition, T> implements ProfileCoverage<T>{

    private List<T> values;
    private ScalarMetadata<T> metadata;
    private String description;
    private ProfileDomain domain;
    
    public ProfileSimpleCoverage(GridSeriesCoverage<T> coverage, List<T> values) {
        this.values = values;
        metadata = (ScalarMetadata<T>)coverage.getRangeMetadata(null);
        description = coverage.getDescription();
        List<Double> elevations = new ArrayList<Double>();
        VerticalAxis vAxis = coverage.getDomain().getVerticalAxis();
        if(vAxis != null) {
            for(int i=0; i<vAxis.size(); i++){
                elevations.add(vAxis.getCoordinateValue(i));
            }
        }
        domain = new ProfileDomainImpl(elevations, vAxis == null ? null : vAxis.getVerticalCrs());
    }

    @Override
    public ScalarMetadata<T> getRangeMetadata() {
        return metadata;
    }

    @Override
    public ProfileDomain getDomain() {
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
