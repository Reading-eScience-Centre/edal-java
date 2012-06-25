package uk.ac.rdg.resc.edal.feature.impl;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.PointSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.ProfileCoverage;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

/**
 * An implementation of a {@link GridSeriesFeature}
 * 
 * @author Guy Griffiths
 */
public class GridSeriesFeatureImpl extends AbstractFeature implements GridSeriesFeature {

    private final GridSeriesCoverage coverage;

    public GridSeriesFeatureImpl(String name, String id,
            FeatureCollection<? extends Feature> parentCollection, GridSeriesCoverage coverage) {
        super(name, id, coverage.getDescription(), parentCollection);
        this.coverage = coverage;
    }

    @Override
    public PointSeriesFeature extractPointSeriesFeature(HorizontalPosition pos, VerticalPosition z,
            Extent<TimePosition> tRange) {
        PointSeriesCoverage psCoverage = coverage.extractPointSeriesCoverage(pos, z, tRange,
                coverage.getMemberNames());
        return new PointSeriesFeatureImpl(getName() + " -> PointSeries", "PS-" + getId(),
                "Point series extraction of " + getDescription(), psCoverage, pos, z,
                getFeatureCollection());
    }

    @Override
    public ProfileFeature extractProfileFeature(HorizontalPosition pos, TimePosition t) {
        ProfileCoverage pCoverage = coverage.extractProfileCoverage(pos, t,
                coverage.getMemberNames());
        return new ProfileFeatureImpl(getName() + " -> Profile", "PF-" + getId(),
                "Profile extraction of " + getDescription(), pCoverage, pos, t,
                getFeatureCollection());
    }

    @Override
    public GridSeriesCoverage getCoverage() {
        return coverage;
    }

    @Override
    public GridFeature extractGridFeature(HorizontalGrid targetDomain, VerticalPosition zPos,
            TimePosition tPos) {
        GridCoverage2D gridCoverage = coverage.extractGridCoverage(targetDomain, zPos, tPos,
                coverage.getMemberNames());
        return new GridFeatureImpl(getName() + " -> GridFeature", "GF-" + getId(),
                getFeatureCollection(), gridCoverage);
    }
}
