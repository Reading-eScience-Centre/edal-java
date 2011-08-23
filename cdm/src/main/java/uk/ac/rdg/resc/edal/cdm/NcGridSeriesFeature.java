package uk.ac.rdg.resc.edal.cdm;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.Coverage;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.impl.AbstractFeature;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

public class NcGridSeriesFeature extends AbstractFeature implements GridSeriesFeature<Double> {

    private FeatureCollection<? extends Feature> parentCollection;
    
    public NcGridSeriesFeature(String name, String id, String description, FeatureCollection<? extends Feature> parentCollection) {
        super(name, id, description);
        this.parentCollection = parentCollection;
        // TODO Auto-generated constructor stub
    }

    @Override
    public PointSeriesFeature<Double> extractPointSeriesFeature(HorizontalPosition pos, VerticalPosition z,
            Extent<TimePosition> tRange) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProfileFeature<Double> extractProfileFeature(HorizontalPosition pos, TimePosition t) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GridSeriesCoverage<Double> getCoverage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FeatureCollection<? extends Feature> getFeatureCollection() {
        return parentCollection;
    }

}
