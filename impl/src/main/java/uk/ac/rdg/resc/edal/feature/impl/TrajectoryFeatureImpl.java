package uk.ac.rdg.resc.edal.feature.impl;

import uk.ac.rdg.resc.edal.coverage.TrajectoryCoverage;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;

public class TrajectoryFeatureImpl extends AbstractFeature implements TrajectoryFeature {

    private TrajectoryCoverage coverage;

    public TrajectoryFeatureImpl(String name, String id, String description,
            TrajectoryCoverage coverage, FeatureCollection<? extends Feature> parentCollection) {
        super(name, id, description, parentCollection);
        this.coverage = coverage;
    }

    @Override
    public TrajectoryCoverage getCoverage() {
        return coverage;
    }

}
