package uk.ac.rdg.resc.edal.graphics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.TrajectoryCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.TrajectoryDomain;
import uk.ac.rdg.resc.edal.coverage.domain.impl.TrajectoryDomainImpl;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.RegularAxis;
import uk.ac.rdg.resc.edal.coverage.grid.RegularGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.BorderedGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.GridCoordinates2DImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.coverage.impl.TrajectoryCoverageImpl;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.impl.MetadataUtils;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.feature.impl.TrajectoryFeatureImpl;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs.PositiveDirection;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.position.impl.GeoPositionImpl;
import uk.ac.rdg.resc.edal.position.impl.HorizontalPositionImpl;
import uk.ac.rdg.resc.edal.position.impl.TimePositionJoda;
import uk.ac.rdg.resc.edal.position.impl.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.util.AbstractBigList;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Class for plotting data onto a map.
 * 
 * Methods in this class should map the data onto image co-ordinates and then
 * use methods in the {@link Frame} class to do the rendering
 * 
 * @author Guy Griffiths
 * 
 */
public class MapPlotter {
    private int width;
    private int height;
    private BoundingBox bbox;
    private MapStyleDescriptor style;
    private Map<TimePosition, Frame> frameData;

    public MapPlotter(MapStyleDescriptor style, int width, int height, BoundingBox bbox) {
        this.style = style;
        this.width = width;
        this.height = height;
        this.bbox = bbox;

        frameData = new HashMap<TimePosition, Frame>();
    }

    public MapPlotter(MapStyleDescriptor style, HorizontalGrid targetDomain) {
        this.style = style;
        this.width = targetDomain.getXAxis().size();
        this.height = targetDomain.getYAxis().size();
        this.bbox = targetDomain.getCoordinateExtent();

        frameData = new HashMap<TimePosition, Frame>();
    }

    /**
     * Adds data to a frame. The feature added need not conform to the bounds
     * set in this {@link MapPlotter}. If they do not, the appropriate method
     * will be used to extract the desired feature. Note that if many members of
     * the same feature are to be plotted, it may be better to extract the
     * appropriate sub-feature first, to avoid repeatedly doing so in this
     * routine
     * 
     * @param feature
     *            The feature containing the data to be plotted
     * @param memberName
     *            The member of the feature to be plotted
     * @param vPos
     *            The vertical position of the feature. This is only applicable
     *            if the feature is a {@link GridSeriesFeature} and you want
     *            automatic extraction to be performed
     * @param tPos
     *            The time position to be added. Each time this method is called
     *            with a different {@link TimePosition}, a frame will be
     *            generated for an animation. Each time it is called with a
     *            {@link TimePosition} which has already been used, it will add
     *            a layer to that frame (e.g. for plotting vector arrows on top
     *            of magnitude values)
     * @param label
     *            The label to be added to the frame. If it is null, no label is
     *            added
     * @param plotStyle
     *            The style of the plot
     */
    public void addToFrame(Feature feature, String memberName, VerticalPosition vPos,
            TimePosition tPos, String label, PlotStyle plotStyle) {
        Frame frame = frameData.get(tPos);
        if (frame == null) {
            frame = new Frame(width, height, label);
        }
        
        RangeMetadata metadata = MetadataUtils.getMetadataForFeatureMember(feature, memberName);
        if(metadata == null){
            throw new IllegalArgumentException("Member "+memberName+" does not exist.  Cannot plot.");
        }
        if(metadata instanceof ScalarMetadata){
            addScalarMemberToFrame(frame, feature, vPos, tPos, label, plotStyle, (ScalarMetadata) metadata, style.getScaleRange());
        } else {
            List<ScalarMetadata> representativeChildren = metadata.getRepresentativeChildren();
            if(representativeChildren != null){
                boolean plottedBoxfill = false;
                for(ScalarMetadata representativeChildMetadata : representativeChildren){
                    /*
                     * We need to supply a scale range for the contour plot.
                     */
                    PlotStyle defaultPlotStyle = PlotStyle.getDefaultPlotStyle(feature, representativeChildMetadata);
                    /*
                     * If we have already plotted something in boxfill style, we
                     * don't want to do so again (because it would obscure the
                     * original).  For the time being, we use the contour style instead
                     */
                    if(defaultPlotStyle == PlotStyle.BOXFILL){
                        if(plottedBoxfill){
                            plotStyle = PlotStyle.CONTOUR;
                        } else {
                            plottedBoxfill = true;
                        }
                    }
                    Extent<Float> contourScaleRange = style.getScaleRange();
                    if (plotStyle == PlotStyle.CONTOUR) {
                        contourScaleRange = GISUtils.estimateValueRange(feature, representativeChildMetadata.getName());
                    }
                    addScalarMemberToFrame(frame, feature, vPos, tPos, label, plotStyle, representativeChildMetadata, contourScaleRange);
                }
            }
        }

        if (!frameData.containsKey(tPos)) {
            frameData.put(tPos, frame);
        }
    }
    
    private void addScalarMemberToFrame(Frame frame, Feature feature, VerticalPosition vPos,
            TimePosition tPos, String label, PlotStyle plotStyle, ScalarMetadata metadata, Extent<Float> contourScaleRange){
        String memberName = metadata.getName();
        if(plotStyle == PlotStyle.DEFAULT){
            plotStyle = PlotStyle.getDefaultPlotStyle(feature, metadata);
        }
        if (feature instanceof GridSeriesFeature) {
            addGridSeriesFeatureToFrame((GridSeriesFeature) feature, memberName, vPos, tPos, label,
                    plotStyle, frame, contourScaleRange);
        } else if (feature instanceof GridFeature) {
            addGridFeatureToFrame((GridFeature) feature, memberName, label, plotStyle, frame, contourScaleRange, false);
        } else if (feature instanceof PointSeriesFeature) {
            addPointSeriesFeatureToFrame((PointSeriesFeature) feature, memberName, tPos, label,
                    plotStyle, frame);
        } else if (feature instanceof ProfileFeature) {
            addProfileFeatureToFrame((ProfileFeature) feature, memberName, vPos, label, plotStyle,
                    frame);
        } else if (feature instanceof TrajectoryFeature) {
            addTrajectoryFeatureToFrame((TrajectoryFeature) feature, memberName, label, plotStyle,
                    frame);
        } else {
            throw new UnsupportedOperationException("Plotting of features of the type "
                    + feature.getClass() + " on a map is not yet supported");
        }
    }

    private void addGridSeriesFeatureToFrame(GridSeriesFeature feature, final String memberName,
            final VerticalPosition vPos, final TimePosition tPos, String label, PlotStyle plotStyle, Frame frame, Extent<Float> contourScaleRange) {
        RangeMetadata memberMetadata = MetadataUtils.getDescendantMetadata(feature.getCoverage()
                .getRangeMetadata(), memberName);
        Set<String> memberNamesToExtract = getAllScalarChildrenOf(memberMetadata);
        GridFeature gridFeature;
        if (plotStyle == PlotStyle.TRAJECTORY) {
            throw new UnsupportedOperationException(
                    "Cannot plot this type of feature as a trajectory");
        } else if (plotStyle == PlotStyle.GRIDPOINT) {
            final GridSeriesCoverage coverage = feature.getCoverage();
            addGridpoints(frame, feature.getCoverage().getDomain().getHorizontalGrid(), new Evaluator() {
                @Override
                public Object evaluate(HorizontalPosition hPos) {
                    return coverage.evaluate(new GeoPositionImpl(hPos, vPos, tPos), memberName);
                }
            });
        } else if (plotStyle == PlotStyle.POINT) {
            if (!(Number.class.isAssignableFrom(feature.getCoverage()
                    .getScalarMetadata(memberName).getValueType()))) {
                throw new UnsupportedOperationException(
                        "Cannot plot non-numerical data as coloured points");
            }
            final GridSeriesCoverage coverage = feature.getCoverage();
            addColouredPoints(frame, feature.getCoverage().getDomain().getHorizontalGrid(), new Evaluator() {
                @Override
                public Object evaluate(HorizontalPosition hPos) {
                    return coverage.evaluate(new GeoPositionImpl(hPos, vPos, tPos), memberName);
                }
            }, plotStyle);
        } else {
            gridFeature = feature.extractGridFeature(new RegularGridImpl(bbox, width, height),
                    vPos, tPos, memberNamesToExtract);
            addGridFeatureToFrame(gridFeature, memberName, label, plotStyle, frame, contourScaleRange, true);
        }
    }
    
    private void addGridFeatureToFrame(GridFeature feature, final String memberName, String label,
            PlotStyle plotStyle, Frame frame, Extent<Float> contourScaleRange, boolean alreadyExtracted) {

        /*
         * First, make sure that we have a suitable grid feature.
         */
        if (plotStyle == PlotStyle.TRAJECTORY) {
            throw new UnsupportedOperationException(
                    "Cannot plot this type of feature as a trajectory");
        } else if (plotStyle == PlotStyle.GRIDPOINT) {
            final GridCoverage2D coverage = feature.getCoverage();
            addGridpoints(frame, feature.getCoverage().getDomain(), new Evaluator() {
                @Override
                public Object evaluate(HorizontalPosition hPos) {
                    return coverage.evaluate(hPos, memberName);
                }
            });
        } else if (plotStyle == PlotStyle.POINT) {
            if (!(Number.class.isAssignableFrom(feature.getCoverage()
                    .getScalarMetadata(memberName).getValueType()))) {
                throw new UnsupportedOperationException(
                        "Cannot plot non-numerical data as coloured points");
            }
            final GridCoverage2D coverage = feature.getCoverage();
            addColouredPoints(frame, feature.getCoverage().getDomain(), new Evaluator() {
                @Override
                public Object evaluate(HorizontalPosition hPos) {
                    return coverage.evaluate(hPos, memberName);
                }
            }, plotStyle);
        } else {
            /*
             * Otherwise we want the grid feature to have the same dimensions as
             * the image
             */
            BoundingBox coordinateExtent = feature.getCoverage().getDomain().getCoordinateExtent();
            int fWidth = feature.getCoverage().getDomain().getXAxis().size();
            int fHeight = feature.getCoverage().getDomain().getYAxis().size();
            if (fWidth != width || fHeight != height || !coordinateExtent.equals(bbox)) {
                /*
                 * The input feature is not the right size. Convert it
                 */
                RangeMetadata memberMetadata = MetadataUtils.getDescendantMetadata(feature
                        .getCoverage().getRangeMetadata(), memberName);
                Set<String> memberNamesToExtract = getAllScalarChildrenOf(memberMetadata);
                feature = feature.extractGridFeature(new RegularGridImpl(bbox, width, height),
                        memberNamesToExtract);
            }
            /*
             * We now have a grid feature suitable for plotting
             */
            Number[][] data = getDataFromGridFeature(feature, memberName);
            frame.addGriddedData(data, plotStyle, contourScaleRange);
        }

    }

    private interface Evaluator {
        Object evaluate(HorizontalPosition hPos);
    }

    private void addGridpoints(Frame frame, HorizontalGrid featureGrid, Evaluator evaluator){
        RegularGridImpl targetDomain = new RegularGridImpl(bbox, width, height);
        RegularAxis xAxis = targetDomain.getXAxis();
        RegularAxis yAxis = targetDomain.getYAxis();
        CoordinateReferenceSystem crs = bbox.getCoordinateReferenceSystem();
        Set<Long> neededIndices = new HashSet<Long>();
        for(double x : xAxis.getCoordinateValues()){
            for(double y : yAxis.getCoordinateValues()){
                long index = featureGrid.findIndexOf(new HorizontalPositionImpl(x, y, crs));
                if(index > 0){
                    neededIndices.add(index);
                }
            }                
        }
        Set<GridCoordinates2D> coords = new HashSet<GridCoordinates2D>();
        for(long index : neededIndices){
            GridCoordinates2D gridCoords = featureGrid.getCoords(index);
            HorizontalPosition hPos = featureGrid.getGridCell(gridCoords).getCentre();
            GridCell2D containingCell = targetDomain.findContainingCell(hPos);
            if(containingCell == null)
                continue;
            Object val = evaluator.evaluate(hPos);
            if (val == null || (Float.class.isAssignableFrom(val.getClass()) && Float.isNaN((Float)val))){
                continue;
            }
            coords.add(containingCell.getGridCoordinates());
        }
        frame.addGridPoints(coords);
        return;
    }

    private void addColouredPoints(Frame frame, HorizontalGrid featureGrid, Evaluator evaluator, PlotStyle plotStyle){
        RegularGrid targetDomain = new BorderedGrid(bbox, width, height);
        
        RegularAxis xAxis = targetDomain.getXAxis();
        RegularAxis yAxis = targetDomain.getYAxis();
        CoordinateReferenceSystem crs = bbox.getCoordinateReferenceSystem();
        Set<Long> neededIndices = new LinkedHashSet<Long>();
        for(double x : xAxis.getCoordinateValues()){
            for(double y : yAxis.getCoordinateValues()){
                long index = featureGrid.findIndexOf(new HorizontalPositionImpl(x, y, crs));
                if(index > 0){
                    neededIndices.add(index);
                }
            }                
        }
        List<GridCoordinates2D> coords = new ArrayList<GridCoordinates2D>();
        List<Number> values = new ArrayList<Number>();
        for(long index : neededIndices){
            GridCoordinates2D gridCoords = featureGrid.getCoords(index);
            HorizontalPosition hPos = featureGrid.getGridCell(gridCoords).getCentre();
            GridCell2D containingCell = targetDomain.findContainingCell(hPos);
            if(containingCell == null)
                continue;
            coords.add(containingCell.getGridCoordinates());
            values.add((Number) evaluator.evaluate(hPos));
        }
        frame.addMultipointData(values, coords, plotStyle);
    }

    private void addPointSeriesFeatureToFrame(PointSeriesFeature feature, String memberName,
            TimePosition tPos, String label, PlotStyle plotStyle, Frame frame) {
        HorizontalGrid targetDomain = new BorderedGrid(bbox, width, height);
        GridCell2D containingCell = targetDomain
                .findContainingCell(feature.getHorizontalPosition());
        /*
         * If the feature is outside of our box, don't do anything.
         */
        if (containingCell != null) {
            GridCoordinates2D gridCoordinates = containingCell.getGridCoordinates();
            if (plotStyle == PlotStyle.POINT) {

                boolean scalarField = feature.getCoverage().getScalarMemberNames()
                        .contains(memberName);
                if (scalarField) {
                    ScalarMetadata scalarMetadata = feature.getCoverage().getScalarMetadata(
                            memberName);
                    Class<?> clazz = scalarMetadata.getValueType();
                    Number value = null;
                    if (Number.class.isAssignableFrom(clazz)) {
                        /*
                         * We can plot non-numerical values of point series
                         * features. We just plot them as an out-of-range
                         * number.
                         */
                        value = (Number) feature.getCoverage().evaluate(tPos, memberName);
                    }
                    frame.addPointData(value, gridCoordinates, plotStyle);
                } else {
                    throw new UnsupportedOperationException(
                            "Plotting of non-scalar members of PointSeriesFeatures is not yet supported");
                }
            } else if (plotStyle == PlotStyle.GRIDPOINT) {
                List<GridCoordinates2D> coord = new ArrayList<GridCoordinates2D>();
                frame.addGridPoints(coord);
            } else {
                throw new IllegalArgumentException("Cannot plot a PointSeriesFeature in the style "
                        + plotStyle);
            }
        }
    }

    private void addProfileFeatureToFrame(ProfileFeature feature, String memberName,
            VerticalPosition vPos, String label, PlotStyle plotStyle, Frame frame) {
        HorizontalGrid targetDomain = new BorderedGrid(bbox, width, height);
        GridCell2D containingCell = targetDomain
                .findContainingCell(feature.getHorizontalPosition());
        /*
         * If the feature is outside of our box, don't do anything.
         */
        if (containingCell != null) {
            GridCoordinates2D gridCoordinates = containingCell.getGridCoordinates();
            if (plotStyle == PlotStyle.POINT) {

                boolean scalarField = feature.getCoverage().getScalarMemberNames()
                        .contains(memberName);
                if (scalarField) {
                    ScalarMetadata scalarMetadata = feature.getCoverage().getScalarMetadata(
                            memberName);
                    Class<?> clazz = scalarMetadata.getValueType();
                    Number value = null;
                    if (Number.class.isAssignableFrom(clazz)) {
                        /*
                         * We can plot non-numerical values of point series
                         * features. We just plot them as an out-of-range
                         * number.
                         */
                        value = (Number) feature.getCoverage().evaluate(vPos, memberName);
                    }
                    frame.addPointData(value, gridCoordinates, plotStyle);
                } else {
                    throw new UnsupportedOperationException(
                            "Plotting of non-scalar members of ProfileFeatures is not yet supported");
                }
            } else if (plotStyle == PlotStyle.GRIDPOINT) {
                List<GridCoordinates2D> coord = new ArrayList<GridCoordinates2D>();
                frame.addGridPoints(coord);
            } else {
                throw new IllegalArgumentException("Cannot plot a ProfileFeature in the style "
                        + plotStyle);
            }
        }
    }

    private void addTrajectoryFeatureToFrame(TrajectoryFeature feature, String memberName,
            String label, PlotStyle plotStyle, Frame frame) {

        if (plotStyle != PlotStyle.TRAJECTORY && plotStyle != PlotStyle.GRIDPOINT
                && plotStyle != PlotStyle.POINT) {
            throw new IllegalArgumentException("Cannot plot a TrajectoryFeature in the style "
                    + plotStyle);
        }

        RegularGrid targetDomain = new RegularGridImpl(bbox, width, height);
        TrajectoryCoverage coverage = feature.getCoverage();
        List<GeoPosition> positions = coverage.getDomain().getDomainObjects();
        List<GridCoordinates2D> coords = new ArrayList<GridCoordinates2D>();
        List<Number> values = new ArrayList<Number>();

        boolean numberField = false;
        boolean scalarField = coverage.getScalarMemberNames().contains(memberName);
        if (scalarField) {
            ScalarMetadata scalarMetadata = coverage.getScalarMetadata(memberName);
            Class<?> clazz = scalarMetadata.getValueType();
            if (Number.class.isAssignableFrom(clazz)) {
                numberField = true;
            }
        } else {
            throw new UnsupportedOperationException(
                    "Plotting of non-scalar members of TrajectoryFeatures is not yet supported");
        }


        for (GeoPosition geoPos : positions) {
            HorizontalPosition pos = geoPos.getHorizontalPosition();

            /*
             * Usually we delegate to targetDomain.findContainingCell for this kind
             * of thing
             * 
             * However, for trajectory plots we want to know what the
             * index is, even if it's outside the targetDomain
             */
            if (pos.getCoordinateReferenceSystem() != targetDomain.getCoordinateReferenceSystem()) {
                pos = GISUtils.transformPosition(pos, targetDomain.getCoordinateReferenceSystem());
            }

            double fracAlongX = (pos.getX() - targetDomain.getXAxis().getCoordinateExtent().getLow())
                    / (targetDomain.getXAxis().getCoordinateExtent().getHigh() - targetDomain
                            .getXAxis().getCoordinateExtent().getLow());
            int xIndex = (int) (fracAlongX * width);

            double fracAlongY = 1.0
                    - (pos.getY() - targetDomain.getYAxis().getCoordinateExtent().getLow())
                    / (targetDomain.getYAxis().getCoordinateExtent().getHigh() - targetDomain
                            .getYAxis().getCoordinateExtent().getLow());
            int yIndex = height - 1 - (int) (fracAlongY * height);

            coords.add(new GridCoordinates2DImpl(xIndex, yIndex));
            
            if (numberField) {
                values.add((Number) coverage.evaluate(geoPos, memberName));
            } else {
                values.add(null);
            }
        }

        frame.addMultipointData(values, coords, plotStyle);
    }
    
    /*
     * This gets all scalar children of the desired member.
     */
    private Set<String> getAllScalarChildrenOf(RangeMetadata rangeMetadata) {
        Set<String> returnSet = new HashSet<String>();

        if (rangeMetadata instanceof ScalarMetadata) {
            returnSet.add(rangeMetadata.getName());
        } else {
            for (String subMember : rangeMetadata.getMemberNames()) {
                RangeMetadata memberMetadata = rangeMetadata.getMemberMetadata(subMember);
                if (memberMetadata instanceof ScalarMetadata) {
                    returnSet.add(subMember);
                } else {
                    returnSet.addAll(getAllScalarChildrenOf(memberMetadata));
                }
            }
        }
        return returnSet;
    }

    public List<BufferedImage> getRenderedFrames() {
        /*
         * We have an animation and we want auto-scaling on it...
         * 
         * If we don't have an animation, auto scaling is already done by Frame
         */
        if (style.isAutoScale() && frameData.size() > 1) {
            List<Float> mins = new ArrayList<Float>();
            List<Float> maxes = new ArrayList<Float>();
            Extent<Float> frameRange = frameData.get(0).getAutoRange();
            mins.add(frameRange.getLow());
            maxes.add(frameRange.getHigh());
            style.setScaleRange(Extents.newExtent(Collections.min(mins), Collections.max(maxes)));
        }

        List<BufferedImage> images = new ArrayList<BufferedImage>();
        List<TimePosition> times = new ArrayList<TimePosition>(frameData.keySet());
        Collections.sort(times);
        for (TimePosition time : times) {
            images.add(frameData.get(time).renderLayers(style));
        }
        return images;
    }

    private Number[][] getDataFromGridFeature(final GridFeature feature, String memberName) {
        GridValuesMatrix<?> gridVals = feature.getCoverage().getGridValues(memberName);

        Class<?> clazz = gridVals.getValueType();
        if (!Number.class.isAssignableFrom(clazz)) {
            throw new UnsupportedOperationException(
                    "Can only add frames from GridValuesMatrix objects which contain numbers");
        }

        int width = feature.getCoverage().getDomain().getXAxis().size();
        int height = feature.getCoverage().getDomain().getYAxis().size();
        Number[][] data = new Number[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int dataIndex = getDataIndex(i, j, width, height);
                Number num = (Number) gridVals.getValues().get(dataIndex);
                if (num != null && (num.equals(Float.NaN) || num.equals(Double.NaN))) {
                    num = null;
                }
                data[i][j] = num;
            }
        }
        return data;
    }

    /**
     * Calculates the index of the data point in a data array that corresponds
     * with the given index in the image array, taking into account that the
     * vertical axis is flipped.
     */
    static int getDataIndex(int imageI, int imageJ, int width, int height) {
        int dataJ = height - imageJ - 1;
        int dataIndex = dataJ * width + imageI;
        return dataIndex;
    }

    public static void main(String[] args) throws InstantiationException, IOException {
        List<GeoPosition> positions = new ArrayList<GeoPosition>();
        final List<Float> values = new ArrayList<Float>();

//        double[] xs = new double[]{0,0,2,2,0,0,2.5,2.5,3,3,5,5,3.2,3.2,5,5,3,3,2.1,2.1,0};
//        double[] ys = new double[]{0,2,2,3,3,5,5,3.1,3.11,5,5,3.2,3.2,2,2,0,0,2,2,0,0};
        int SIZE = 100;
        double[] xs = new double[SIZE];
        double[] ys = new double[SIZE];
        for (int i = 0; i < SIZE; i++) {
            xs[i] = 5.1 * Math.cos(i * 2.0 * Math.PI / (SIZE - 1));
            ys[i] = 4.9 * Math.sin(i * 2.0 * Math.PI / (SIZE - 1));
        }

        /*
         * Now add some values
         */
        for (int i = 0; i < xs.length; i++) {
            positions.add(new GeoPositionImpl(new HorizontalPositionImpl(xs[i], ys[i],
                    DefaultGeographicCRS.WGS84), 0.0, new VerticalCrsImpl(Unit.getUnit("m"),
                    PositiveDirection.DOWN, false), new TimePositionJoda(i * 100000L)));
            values.add((float) i);
        }

        TrajectoryDomain domain = new TrajectoryDomainImpl(positions);
        TrajectoryCoverageImpl coverage = new TrajectoryCoverageImpl("coverage for trajectory",
                domain);
        coverage.addMember("test", domain, "desc", Phenomenon.getPhenomenon("test"),
                Unit.getUnit("testunit"), new AbstractBigList<Float>() {
                    @Override
                    public Float get(long index) {
                        return values.get((int) index);
                    }

                    @Override
                    public long sizeAsLong() {
                        return values.size();
                    }
                }, Float.class);
        MapStyleDescriptor style = new MapStyleDescriptor();
        TrajectoryFeature feature = new TrajectoryFeatureImpl("Trajectory feature", "tfeature",
                "long winded description", coverage, null);

        MapPlotter plotter = new MapPlotter(style, 500, 500, new BoundingBoxImpl(new double[] { -5,
                -5, 5, 5 }, DefaultGeographicCRS.WGS84));
        plotter.addToFrame(feature, "test", null, null, null, PlotStyle.TRAJECTORY);
        ImageIO.write(plotter.getRenderedFrames().get(0), "png", new File("/home/guy/00traj.png"));

    }
}
