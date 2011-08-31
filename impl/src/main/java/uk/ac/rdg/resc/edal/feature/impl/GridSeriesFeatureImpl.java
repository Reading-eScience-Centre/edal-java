package uk.ac.rdg.resc.edal.feature.impl;

import java.util.List;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.PointSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.PointSeriesSimpleCoverage;
import uk.ac.rdg.resc.edal.coverage.ProfileCoverage;
import uk.ac.rdg.resc.edal.coverage.ProfileSimpleCoverage;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Extents;

public class GridSeriesFeatureImpl extends AbstractFeature implements GridSeriesFeature<Float> {

    private FeatureCollection<? extends Feature> parentCollection;
    private GridSeriesCoverage<Float> coverage;

    public GridSeriesFeatureImpl(String name, String id, String description,
            FeatureCollection<? extends Feature> parentCollection, GridSeriesCoverage<Float> coverage) {
        super(name, id, description);
        this.parentCollection = parentCollection;
        this.coverage = coverage;
    }

    @Override
    public PointSeriesFeature<Float> extractPointSeriesFeature(HorizontalPosition pos, VerticalPosition z,
            Extent<TimePosition> tRange) {
        Extent<Integer> tExtent = coverage.getDomain().getTimeAxis().getIndexExtent();
        GridCell2D gridCell = coverage.getDomain().getHorizontalGrid().findContainingCell(pos);
        GridCoordinates gridCoords = gridCell.getGridCoordinates();
        int xIndex = gridCoords.getCoordinateValue(0);
        int yIndex = gridCoords.getCoordinateValue(1);
        int zIndex = coverage.getDomain().getVerticalAxis().findIndexOf(z.getZ());

        List<Float> values = coverage.evaluate(tExtent, Extents.newExtent(zIndex, zIndex), Extents.newExtent(yIndex,
                yIndex), Extents.newExtent(xIndex, xIndex));

        PointSeriesCoverage<Float> pointCoverage = new PointSeriesSimpleCoverage<Float>(coverage, values);
        // TODO Check whether we just want default values for name, id, etc.
        PointSeriesFeature<Float> feature = new PointSeriesFeatureImpl<Float>(getName(), getId(), getDescription(),
                pointCoverage, pos, z, parentCollection);
        return feature;
    }

    @Override
    public ProfileFeature<Float> extractProfileFeature(HorizontalPosition pos, TimePosition t) {

        Extent<Integer> vExtent = coverage.getDomain().getVerticalAxis().getIndexExtent();
        GridCell2D gridCell = coverage.getDomain().getHorizontalGrid().findContainingCell(pos);
        GridCoordinates gridCoords = gridCell.getGridCoordinates();
        int xIndex = gridCoords.getCoordinateValue(0);
        int yIndex = gridCoords.getCoordinateValue(1);
        int tIndex = coverage.getDomain().getTimeAxis().findIndexOf(t);

        List<Float> values = coverage.evaluate(Extents.newExtent(tIndex, tIndex), vExtent, Extents.newExtent(yIndex,
                yIndex), Extents.newExtent(xIndex, xIndex));
        ProfileCoverage<Float> profileCoverage = new ProfileSimpleCoverage<Float>(coverage, values);
        // TODO Check whether we just want default values for name, id, etc.
        ProfileFeature<Float> feature = new ProfileFeatureImpl<Float>(getName(), getId(), getDescription(),
                profileCoverage, pos, t, parentCollection);
        return feature;
    }

    @Override
    public GridSeriesCoverage<Float> getCoverage() {
        return coverage;
    }

    @Override
    public FeatureCollection<? extends Feature> getFeatureCollection() {
        return parentCollection;
    }

}
