package uk.ac.rdg.resc.edal.feature.impl;

import java.io.IOException;
import java.util.AbstractList;
import java.util.List;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.PointSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.ProfileCoverage;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.coverage.impl.GridCoverage2DImpl;
import uk.ac.rdg.resc.edal.coverage.impl.PointSeriesSimpleCoverage;
import uk.ac.rdg.resc.edal.coverage.impl.ProfileSimpleCoverage;
import uk.ac.rdg.resc.edal.coverage.util.DataReadingStrategy;
import uk.ac.rdg.resc.edal.coverage.util.PixelMap;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.ArrayBackedList;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.FastDoubleList;
import uk.ac.rdg.resc.edal.util.FastFloatList;

public class GridSeriesFeatureImpl<R> extends AbstractFeature implements GridSeriesFeature<R> {

    private FeatureCollection<? extends Feature> parentCollection;
    private GridSeriesCoverage<R> coverage;
    private DataReadingStrategy dataReadingStrategy;

    public GridSeriesFeatureImpl(String name, String id,
            FeatureCollection<? extends Feature> parentCollection, GridSeriesCoverage<R> coverage,
            DataReadingStrategy dataReadingStrategy) {
        super(name, id, coverage.getDescription());
        this.parentCollection = parentCollection;
        this.coverage = coverage;
        this.dataReadingStrategy = dataReadingStrategy;
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
        Extent<Integer> vExtent = Extents.emptyExtent(Integer.class);
        if(coverage.getDomain().getVerticalAxis() != null)
            vExtent = coverage.getDomain().getVerticalAxis().getIndexExtent();
        GridCoordinates2D gridCell = coverage.getDomain().getHorizontalGrid().findContainingCell(pos);
        List<R> values = null;
        if(gridCell != null){
            int xIndex = gridCell.getXIndex();
            int yIndex = gridCell.getYIndex();
            int tIndex = coverage.getDomain().getTimeAxis().findIndexOf(t);
            values = coverage.evaluate(Extents.newExtent(tIndex, tIndex), vExtent, Extents.newExtent(yIndex,
                    yIndex), Extents.newExtent(xIndex, xIndex));
        }
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
        int tindex = 0;
        int zindex = 0;
        TimeAxis tAxis = getCoverage().getDomain().getTimeAxis();
        if(tAxis != null)
            tindex = tAxis.findIndexOf(tPos);
        VerticalAxis vAxis = getCoverage().getDomain().getVerticalAxis();
        if(vAxis != null)
            zindex = vAxis.findIndexOf(zPos);
        return extractHorizontalGrid(tindex, zindex, targetDomain);
    }
    
    @Override
    public GridCoverage2D<R> extractHorizontalGrid(int tindex, int zindex,
            final HorizontalGrid targetDomain) {
        HorizontalGrid sourceGrid = coverage.getDomain().getHorizontalGrid();
        PixelMap pixelMap = new PixelMap(sourceGrid, targetDomain);

        List<R> dataList;
        if (pixelMap.isEmpty()) {
            /*
             * There is no overlap between the source data grid and the target
             * domain. Return a list of null values. It's very unlikely that the
             * target domain will be bigger than Integer.MAX_VALUE
             */
            dataList = new AbstractList<R>() {
                @Override
                public R get(int index) {
                    if (index < 0 || index >= (int) targetDomain.size())
                        throw new IndexOutOfBoundsException();
                    return null;
                }

                @Override
                public int size() {
                    return (int) targetDomain.size();
                }
            };
        } else {
            dataList = getDataList((int) targetDomain.size());
            try {
                dataReadingStrategy.readHorizontalData(tindex, zindex, coverage, pixelMap, dataList);
            } catch (IOException e) {
                // TODO deal with this better
                e.printStackTrace();
            }
        }
        return new GridCoverage2DImpl<R>(getCoverage().getRangeMetadata(null), getDescription(), targetDomain, dataList);
    }
    
    @SuppressWarnings("unchecked") 
    private List<R> getDataList(final int size) {
        Class<?> valueType = coverage.getRangeMetadata(null).getValueType(); 
        List<R> ret; 
        /*
         * These are unchecked conversions, but they will be fine (because
         * valueType==R)
         */
        if (valueType == Float.class) {
            ret = (List<R>) new FastFloatList(size);
        } else if (valueType ==  Double.class) {
            ret = (List<R>) new FastDoubleList(size);
        } else {
            /*
             * Worst case scenario. ArrayBackedList is different to ArrayList in
             * that it is not resizeable, and it's initial state is the correct
             * size filled with nulls
             */
            ret = new ArrayBackedList<R>(size);
        }
        return ret;
    }
}
