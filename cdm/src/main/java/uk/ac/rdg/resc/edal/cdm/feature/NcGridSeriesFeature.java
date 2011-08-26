package uk.ac.rdg.resc.edal.cdm.feature;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.cdm.coverage.NcGridSeriesCoverage;
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

public class NcGridSeriesFeature extends AbstractFeature implements GridSeriesFeature<Float> {

    private FeatureCollection<? extends Feature> parentCollection;
    private GridSeriesCoverage<Float> coverage;
    
    public NcGridSeriesFeature(String name, String id, String description, FeatureCollection<? extends Feature> parentCollection, GridSeriesCoverage<Float> coverage) {
        super(name, id, description);
        this.parentCollection = parentCollection;
        this.coverage = coverage;
    }

    @Override
    public PointSeriesFeature<Float> extractPointSeriesFeature(HorizontalPosition pos, VerticalPosition z,
            Extent<TimePosition> tRange) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProfileFeature<Float> extractProfileFeature(HorizontalPosition pos, TimePosition t) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NcGridSeriesCoverage getCoverage() {
        return (NcGridSeriesCoverage) coverage;
    }

    @Override
    public FeatureCollection<? extends Feature> getFeatureCollection() {
        return parentCollection;
    }

}
