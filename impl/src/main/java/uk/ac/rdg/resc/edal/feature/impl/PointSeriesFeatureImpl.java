package uk.ac.rdg.resc.edal.feature.impl;

import uk.ac.rdg.resc.edal.coverage.PointSeriesCoverage;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

/**
 * An implementation of a {@link PointSeriesFeature}
 * 
 * @author Guy Griffiths
 */
public class PointSeriesFeatureImpl extends AbstractFeature implements PointSeriesFeature {

    private final PointSeriesCoverage coverage;
    private final HorizontalPosition hPos;
    private final VerticalPosition vPos;

    public PointSeriesFeatureImpl(String name, String id, String description,
            PointSeriesCoverage coverage, HorizontalPosition hPos, VerticalPosition vPos,
            FeatureCollection<? extends Feature> parentCollection) {
        super(name, id, description, parentCollection);
        this.coverage = coverage;
        this.hPos = hPos;
        this.vPos = vPos;
    }

    @Override
    public PointSeriesCoverage getCoverage() {
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
}
