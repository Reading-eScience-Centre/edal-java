package uk.ac.rdg.resc.edal.feature.impl;

import uk.ac.rdg.resc.edal.coverage.ProfileCoverage;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;

/**
 * Implementation of a {@link ProfileFeature}
 * 
 * @author Guy Griffiths
 */
public class ProfileFeatureImpl extends AbstractFeature implements ProfileFeature {

    private final ProfileCoverage coverage;
    private final HorizontalPosition hPos;
    private final TimePosition time;

    public ProfileFeatureImpl(String name, String id, String description, ProfileCoverage coverage,
            HorizontalPosition hPos, TimePosition time,
            FeatureCollection<? extends Feature> parentCollection) {
        super(name, id, description, parentCollection);
        this.coverage = coverage;
        this.hPos = hPos;
        this.time = time;
    }

    @Override
    public ProfileCoverage getCoverage() {
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
}
