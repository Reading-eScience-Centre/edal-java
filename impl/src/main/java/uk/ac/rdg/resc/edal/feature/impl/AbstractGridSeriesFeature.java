package uk.ac.rdg.resc.edal.feature.impl;

import java.util.List;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.PointSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.PointSeriesSimpleCoverage;
import uk.ac.rdg.resc.edal.coverage.ProfileCoverage;
import uk.ac.rdg.resc.edal.coverage.ProfileSimpleCoverage;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Extents;

public abstract class AbstractGridSeriesFeature<R> extends AbstractFeature implements GridSeriesFeature<R> {

    private FeatureCollection<? extends Feature> parentCollection;
    private GridSeriesCoverage<R> coverage;

    public AbstractGridSeriesFeature(String name, String id, String description,
            FeatureCollection<? extends Feature> parentCollection, GridSeriesCoverage<R> coverage) {
        super(name, id, description);
        this.parentCollection = parentCollection;
        this.coverage = coverage;
    }

    @Override
    public PointSeriesFeature<R> extractPointSeriesFeature(HorizontalPosition pos, VerticalPosition z,
            Extent<TimePosition> tRange) {
        Extent<Integer> tExtent = coverage.getDomain().getTimeAxis().getIndexExtent();
        GridCoordinates2D gridCell = coverage.getDomain().getHorizontalGrid().findContainingCell(pos);
        int xIndex = gridCell.getXIndex();
        int yIndex = gridCell.getYIndex();
        int zIndex = coverage.getDomain().getVerticalAxis().findIndexOf(z.getZ());

        List<R> values = coverage.evaluate(tExtent, Extents.newExtent(zIndex, zIndex), Extents.newExtent(yIndex,
                yIndex), Extents.newExtent(xIndex, xIndex));

        PointSeriesCoverage<R> pointCoverage = new PointSeriesSimpleCoverage<R>(coverage, values);
        // TODO Check whether we just want default values for name, id, etc.
        PointSeriesFeature<R> feature = new PointSeriesFeatureImpl<R>(getName(), getId(), getDescription(),
                pointCoverage, pos, z, parentCollection);
        return feature;
    }

    @Override
    public ProfileFeature<R> extractProfileFeature(HorizontalPosition pos, TimePosition t) {
        Extent<Integer> vExtent = Extents.emptyExtent();
        try{
            vExtent = coverage.getDomain().getVerticalAxis().getIndexExtent();
        } catch (NullPointerException npe){
            // Do nothing
        }
        GridCoordinates2D gridCell = coverage.getDomain().getHorizontalGrid().findContainingCell(pos);
        int xIndex = gridCell.getXIndex();
        int yIndex = gridCell.getYIndex();
        int tIndex = coverage.getDomain().getTimeAxis().findIndexOf(t);

        List<R> values = coverage.evaluate(Extents.newExtent(tIndex, tIndex), vExtent, Extents.newExtent(yIndex,
                yIndex), Extents.newExtent(xIndex, xIndex));
        ProfileCoverage<R> profileCoverage = new ProfileSimpleCoverage<R>(coverage, values);
        // TODO Check whether we just want default values for name, id, etc.
        ProfileFeature<R> feature = new ProfileFeatureImpl<R>(getName(), getId(), getDescription(),
                profileCoverage, pos, t, parentCollection);
        return feature;
    }

    @Override
    public GridSeriesCoverage<R> getCoverage() {
        return coverage;
    }

    @Override
    public FeatureCollection<? extends Feature> getFeatureCollection() {
        return parentCollection;
    }
    
    @Override
    public GridCoverage2D<R> extractHorizontalGrid(TimePosition tPos, double zPos,
            HorizontalGrid targetDomain) {
        int tindex = getCoverage().getDomain().getTimeAxis().findIndexOf(tPos);
        int zindex = getCoverage().getDomain().getVerticalAxis().findIndexOf(zPos);
        return extractHorizontalGrid(tindex, zindex, targetDomain);
    }
}
