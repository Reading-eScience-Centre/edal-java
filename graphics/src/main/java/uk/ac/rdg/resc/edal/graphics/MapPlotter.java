package uk.ac.rdg.resc.edal.graphics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.TrajectoryCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.TrajectoryDomain;
import uk.ac.rdg.resc.edal.coverage.domain.impl.TrajectoryDomainImpl;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.RegularGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.GridCoordinates2DImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.coverage.impl.TrajectoryCoverageImpl;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.VectorComponent;
import uk.ac.rdg.resc.edal.coverage.metadata.VectorComponent.VectorDirection;
import uk.ac.rdg.resc.edal.coverage.metadata.VectorMetadata;
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

        if(plotStyle == PlotStyle.DEFAULT){
            plotStyle = getDefaultPlotStyle(feature, memberName);
        }
        
        if (feature instanceof GridSeriesFeature) {
            addGridSeriesFeatureToFrame((GridSeriesFeature) feature, memberName, vPos, tPos, label,
                    plotStyle, frame);
        } else if (feature instanceof GridFeature) {
            addGridFeatureToFrame((GridFeature) feature, memberName, label, plotStyle, frame);
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

        if (!frameData.containsKey(tPos)) {
            frameData.put(tPos, frame);
        }
    }
    
    private PlotStyle getDefaultPlotStyle(Feature feature, String memberName) {
        /*
         * Start with BOXFILL as a default
         */
        PlotStyle plotStyle = PlotStyle.BOXFILL;
        
        /*
         * Now narrow down by feature type
         */
        if (feature instanceof GridSeriesFeature) {
            plotStyle = PlotStyle.BOXFILL;
        } else if (feature instanceof GridFeature) {
            plotStyle = PlotStyle.BOXFILL;
        } else if (feature instanceof PointSeriesFeature) {
            plotStyle = PlotStyle.POINT;
        } else if (feature instanceof ProfileFeature) {
            plotStyle = PlotStyle.POINT;
        } else if (feature instanceof TrajectoryFeature) {
            plotStyle = PlotStyle.TRAJECTORY;
        }
        /*
         * Now specialise by checking if we have numbers or only directional data
         */
        if (feature.getCoverage().getScalarMemberNames().contains(memberName)) {
            ScalarMetadata scalarMetadata = feature.getCoverage().getScalarMetadata(memberName);
            if (scalarMetadata.getValueType().equals(Object.class)) {
                /*
                 * We have non-numeric data. All we can do here is plot the grid
                 * points
                 */
                plotStyle = PlotStyle.GRID_POINTS;
            } else if (scalarMetadata instanceof VectorComponent
                    && ((VectorComponent) scalarMetadata).getDirection() == VectorDirection.DIRECTION) {
                /*
                 * We always plot direction fields with vector style
                 */
                plotStyle = PlotStyle.VECTOR;
            }
        }
        return plotStyle;
    }

    private void addGridSeriesFeatureToFrame(GridSeriesFeature feature, String memberName,
            VerticalPosition vPos, TimePosition tPos, String label, PlotStyle plotStyle, Frame frame) {
        RangeMetadata memberMetadata = MetadataUtils.getDescendentMetadata(feature.getCoverage()
                .getRangeMetadata(), memberName);
        Set<String> memberNamesToExtract = getAllScalarChildrenOf(memberMetadata);
        GridFeature gridFeature;
        if (plotStyle == PlotStyle.TRAJECTORY) {
            throw new UnsupportedOperationException(
                    "Cannot plot this type of feature as a trajectory");
        } else if (plotStyle == PlotStyle.POINT || plotStyle == PlotStyle.GRID_POINTS) {
            /*
             * We are using a plot style which needs the entire grid. Usually
             * this will be lower resolution than the image anyway, so it's not
             * necessarily slow
             */
            HorizontalGrid horizontalGrid = feature.getCoverage().getDomain().getHorizontalGrid();
            gridFeature = feature.extractGridFeature(horizontalGrid, vPos, tPos,
                    memberNamesToExtract);
        } else {
            gridFeature = feature.extractGridFeature(new RegularGridImpl(bbox, width, height),
                    vPos, tPos, memberNamesToExtract);
        }
        addGridFeatureToFrame(gridFeature, memberName, label, plotStyle, frame);
    }

    private void addGridFeatureToFrame(GridFeature feature, String memberName, String label,
            PlotStyle plotStyle, Frame frame) {

        /*
         * First, make sure that we have a suitable grid feature.
         */
        if (plotStyle == PlotStyle.TRAJECTORY) {
            throw new UnsupportedOperationException(
                    "Cannot plot this type of feature as a trajectory");
        } else if (plotStyle == PlotStyle.POINT || plotStyle == PlotStyle.GRID_POINTS) {
            /*
             * We are using a plot style which needs the entire grid. Usually
             * this will be lower resolution than the image anyway, so it's not
             * necessarily slow
             */
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
                RangeMetadata memberMetadata = MetadataUtils.getDescendentMetadata(feature
                        .getCoverage().getRangeMetadata(), memberName);
                Set<String> memberNamesToExtract = getAllScalarChildrenOf(memberMetadata);
                feature = feature.extractGridFeature(new RegularGridImpl(bbox, width, height),
                        memberNamesToExtract);
            }
        }

        /*
         * We now have a grid feature suitable for plotting
         */

        Number[][] data;
        boolean scalarField = feature.getCoverage().getScalarMemberNames().contains(memberName);

        if (scalarField) {
            /*
             * We have a scalar field. We just want to plot it.
             */
            if (plotStyle == PlotStyle.GRID_POINTS) {
                HorizontalGrid targetDomain = new RegularGridImpl(bbox, width, height);
                HorizontalPosition hPos;
                HorizontalGrid hGrid = feature.getCoverage().getDomain();
                List<GridCoordinates2D> coords = new ArrayList<GridCoordinates2D>();
                for (GridCell2D gridCell : hGrid.getDomainObjects()) {
                    hPos = gridCell.getCentre();
                    GridCell2D containingCell = targetDomain.findContainingCell(hPos);
                    Object val = feature.getCoverage().evaluate(hPos, memberName);
                    if (containingCell == null || val == null || val.equals(Float.NaN)
                            || val.equals(Double.NaN))
                        continue;
                    coords.add(containingCell.getGridCoordinates());
                }
                frame.addGridPoints(coords);
                return;
            } else if (plotStyle == PlotStyle.POINT) {
                if (!(Number.class.isAssignableFrom(feature.getCoverage()
                        .getScalarMetadata(memberName).getValueType()))) {
                    throw new UnsupportedOperationException(
                            "Cannot plot non-numerical data as coloured points");
                }
                /*
                 * The size of border to add to the target domain (this means
                 * that when tiled, point icons don't get cut off).
                 * 
                 * This needs to be at least half the size of the icon used to
                 * plot the point, but it doesn't matter if it's a bit large. We
                 * use 16, because an icon of 32x32 is pretty massive
                 */
                int extraPixels = 16;
                double xGrowth = ((double)extraPixels)/width;
                double yGrowth = ((double)extraPixels)/height;
                double xExtra = bbox.getWidth()*xGrowth;
                double yExtra = bbox.getHeight()*yGrowth;
                BoundingBox bboxBordered = new BoundingBoxImpl(new double[] {
                        bbox.getMinX() - xExtra, bbox.getMinY() - yExtra, bbox.getMaxX() + xExtra,
                        bbox.getMaxY() + yExtra }, bbox.getCoordinateReferenceSystem());
                RegularGrid targetDomain = new RegularGridImpl(bboxBordered, width+2*extraPixels, height+2*extraPixels);

                HorizontalPosition hPos;
                HorizontalGrid hGrid = feature.getCoverage().getDomain();
                List<GridCoordinates2D> coords = new ArrayList<GridCoordinates2D>();
                List<Number> values = new ArrayList<Number>();
                for (GridCell2D gridCell : hGrid.getDomainObjects()) {
                    hPos = gridCell.getCentre();

                    GridCell2D containingCell = targetDomain.findContainingCell(hPos);
                    if (containingCell == null)
                        continue;
                    GridCoordinates2D gridCoordinates = containingCell.getGridCoordinates();
                    coords.add(new GridCoordinates2DImpl(gridCoordinates.getXIndex() - extraPixels,
                            gridCoordinates.getYIndex() - extraPixels));
                    
                    values.add((Number) feature.getCoverage().evaluate(hPos, memberName));
                }
                frame.addMultipointData(values, coords, plotStyle);
                return;
            } else {
                data = getDataFromGridFeature(feature, memberName);
                frame.addGriddedData(data, plotStyle);
            }
        } else {
            /*
             * We have a non-scalar field. We ignore plotting style here and use
             * whatever is best.
             * 
             * TODO Is this the best behaviour?
             */
            RangeMetadata rangeMetadata = feature.getCoverage().getRangeMetadata();
            if (rangeMetadata.getMemberNames().contains(memberName)) {
                /*
                 * We have a direct child with this name. It is NOT a scalar
                 * field.
                 */
                RangeMetadata memberMetadata = rangeMetadata.getMemberMetadata(memberName);
                if (memberMetadata instanceof VectorMetadata) {
                    VectorMetadata vectorMetadata = (VectorMetadata) memberMetadata;
                    String magnitudeMember = null;
                    String directionMember = null;
                    for (String vectorComponentMember : memberMetadata.getMemberNames()) {
                        if (vectorMetadata.getMemberMetadata(vectorComponentMember).getDirection() == VectorDirection.DIRECTION)
                            directionMember = vectorComponentMember;
                        else if (vectorMetadata.getMemberMetadata(vectorComponentMember)
                                .getDirection() == VectorDirection.MAGNITUDE)
                            magnitudeMember = vectorComponentMember;

                    }
                    if (magnitudeMember != null && directionMember != null) {
                        data = getDataFromGridFeature(feature, magnitudeMember);
                        frame.addGriddedData(data, PlotStyle.BOXFILL);
                        data = getDataFromGridFeature(feature, directionMember);
                        frame.addGriddedData(data, PlotStyle.VECTOR);
                    } else {
                        throw new IllegalArgumentException(
                                "This vector is missing either a magnitude or a direction");
                    }
                } else {
                    throw new UnsupportedOperationException(
                            "Currently only scalar fields and vector fields can be plotted");
                }
            } else {
                throw new IllegalArgumentException(
                        "The member "
                                + memberName
                                + " is not present as either a scalar member of this coverage, or as a direct child");
            }
        }
    }

    private void addPointSeriesFeatureToFrame(PointSeriesFeature feature, String memberName,
            TimePosition tPos, String label, PlotStyle plotStyle, Frame frame) {
        HorizontalGrid targetDomain = new RegularGridImpl(bbox, width, height);
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
            } else if (plotStyle == PlotStyle.GRID_POINTS) {
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
        HorizontalGrid targetDomain = new RegularGridImpl(bbox, width, height);
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
            } else if (plotStyle == PlotStyle.GRID_POINTS) {
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

        if (plotStyle != PlotStyle.TRAJECTORY && plotStyle != PlotStyle.GRID_POINTS
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
