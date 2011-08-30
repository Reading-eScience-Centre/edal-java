package uk.ac.rdg.resc.edal.feature.impl;

import uk.ac.rdg.resc.edal.coverage.ProfileCoverage;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;

public class ProfileFeatureImpl<R> extends AbstractFeature implements ProfileFeature<R> {

    private ProfileCoverage<R> coverage;
    private HorizontalPosition hPos;
    private TimePosition time;
    private FeatureCollection<? extends Feature> parentCollection;

    public ProfileFeatureImpl(String name, String id, String description, ProfileCoverage<R> coverage,
            HorizontalPosition hPos, TimePosition time, FeatureCollection<? extends Feature> parentCollection) {
        super(name, id, description);
        this.coverage = coverage;
        this.hPos = hPos;
        this.time = time;
        this.parentCollection = parentCollection;
    }

    @Override
    public ProfileCoverage<R> getCoverage() {
        return coverage;
    }

    @Override
    public HorizontalPosition getHorizontalPosition() {
        return hPos;
    }

    @Override
    public TimePosition getTime() {
        return time;
    }

    @Override
    public FeatureCollection<? extends Feature> getFeatureCollection() {
        return parentCollection;
    }

}
