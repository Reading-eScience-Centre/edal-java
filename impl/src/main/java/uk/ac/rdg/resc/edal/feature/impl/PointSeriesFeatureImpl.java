package uk.ac.rdg.resc.edal.feature.impl;

import uk.ac.rdg.resc.edal.coverage.PointSeriesCoverage;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

public class PointSeriesFeatureImpl<R> extends AbstractFeature implements PointSeriesFeature<R> {

    private PointSeriesCoverage<R> coverage;
    private HorizontalPosition hPos;
    private VerticalPosition vPos;
    private FeatureCollection<? extends Feature> parentCollection;

    public PointSeriesFeatureImpl(String name, String id, String description, PointSeriesCoverage<R> coverage,
            HorizontalPosition hPos, VerticalPosition vPos, FeatureCollection<? extends Feature> parentCollection) {
        super(name, id, description);
        this.coverage = coverage;
        this.hPos = hPos;
        this.vPos = vPos;
        this.parentCollection = parentCollection;
    }

    @Override
    public PointSeriesCoverage<R> getCoverage() {
        return coverage;
    }

    @Override
    public HorizontalPosition getHorizontalPosition() {
        return hPos;
    }

    @Override
    public VerticalPosition getVerticalPosition() {
        return vPos;
    }

    @Override
    public FeatureCollection<? extends Feature> getFeatureCollection() {
        return parentCollection;
    }

}
