package uk.ac.rdg.resc.edal.feature.impl;

import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.GridFeature;

/**
 * An implementation of a {@link GridFeature}
 * 
 * @author Guy Griffiths
 */
public class GridFeatureImpl extends AbstractFeature implements GridFeature {

    private final GridCoverage2D coverage;

    public GridFeatureImpl(String name, String id,
            FeatureCollection<? extends Feature> parentCollection, GridCoverage2D coverage) {
        super(name, id, coverage.getDescription(), parentCollection);
        this.coverage = coverage;
    }

    @Override
    public GridCoverage2D getCoverage() {
        return coverage;
    }

    @Override
    public GridFeature extractGridFeature(HorizontalGrid targetDomain) {
        GridCoverage2D gridCoverage = coverage.extractGridCoverage(targetDomain,
                coverage.getMemberNames());
        return new GridFeatureImpl(getName() + " -> GridFeature", "GF-" + getId(),
                getFeatureCollection(), gridCoverage);
    }
}
