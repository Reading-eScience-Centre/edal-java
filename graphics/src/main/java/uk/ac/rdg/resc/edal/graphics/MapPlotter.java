package uk.ac.rdg.resc.edal.graphics;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.VectorComponent.VectorDirection;
import uk.ac.rdg.resc.edal.coverage.metadata.VectorMetadata;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Extents;

public class MapPlotter {
    private int width;
    private int height;
    private BoundingBox bbox;
    private MapStyleDescriptor style;
    private Map<TimePosition, Frame> frameData;

    public MapPlotter(MapStyleDescriptor style, int width, int height, BoundingBox bbox) {
        this.style = style;
        this.width = width;
        this.bbox = bbox;
        this.height = height;

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

        if (feature instanceof GridSeriesFeature) {
            if(plotStyle == PlotStyle.DEFAULT)
                plotStyle = PlotStyle.BOXFILL;
            addGridSeriesFeatureToFrame((GridSeriesFeature) feature, memberName, vPos, tPos, label,
                    plotStyle, frame);
        } else if (feature instanceof GridFeature) {
            if(plotStyle == PlotStyle.DEFAULT)
                plotStyle = PlotStyle.BOXFILL;
            addGridFeatureToFrame((GridFeature) feature, memberName, label, plotStyle, frame);
        } else if (feature instanceof PointSeriesFeature) {
            if(plotStyle == PlotStyle.DEFAULT)
                plotStyle = PlotStyle.POINT;
            addPointSeriesFeatureToFrame((PointSeriesFeature) feature, memberName, tPos, label,
                    plotStyle, frame);
        } else if (feature instanceof ProfileFeature) {
            if(plotStyle == PlotStyle.DEFAULT)
                plotStyle = PlotStyle.POINT;
            addProfileFeatureToFrame((ProfileFeature) feature, memberName, vPos, label, plotStyle,
                    frame);
        } else {
            throw new UnsupportedOperationException("Plotting of features of the type "
                    + feature.getClass() + " on a map is not yet supported");
        }
        
        if (!frameData.containsKey(tPos)) {
            frameData.put(tPos, frame);
        }
    }

    private void addGridSeriesFeatureToFrame(GridSeriesFeature feature, String memberName,
            VerticalPosition vPos, TimePosition tPos, String label, PlotStyle plotStyle, Frame frame) {
        Set<String> memberNamesToExtract = getAllScalarChildrenOf(feature.getCoverage()
                .getRangeMetadata().getMemberMetadata(memberName));
        GridFeature gridFeature;
        if (plotStyle == PlotStyle.TRAJECTORY) {
            throw new UnsupportedOperationException("Cannot plot this type of feature as a trajectory");
        } else if (plotStyle == PlotStyle.POINT || plotStyle == PlotStyle.GRID_POINTS) {
            /*
             * We are using a plot style which needs the entire grid. Usually
             * this will be lower resolution than the image anyway, so it's not
             * necessarily slow
             */
            HorizontalGrid horizontalGrid = feature.getCoverage().getDomain()
                    .getHorizontalGrid();
            gridFeature = feature.extractGridFeature(horizontalGrid, vPos,
                    tPos, memberNamesToExtract);
        } else {
            gridFeature = feature.extractGridFeature(new RegularGridImpl(
                    bbox, width, height), vPos, tPos, memberNamesToExtract);
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
                Set<String> memberNamesToExtract = getAllScalarChildrenOf(feature.getCoverage()
                        .getRangeMetadata().getMemberMetadata(memberName));
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
                    if(containingCell == null)
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
                HorizontalGrid targetDomain = new RegularGridImpl(bbox, width, height);
                HorizontalPosition hPos;
                HorizontalGrid hGrid = feature.getCoverage().getDomain();
                List<GridCoordinates2D> coords = new ArrayList<GridCoordinates2D>();
                List<Number> values = new ArrayList<Number>();
                for (GridCell2D gridCell : hGrid.getDomainObjects()) {
                    hPos = gridCell.getCentre();
                    GridCell2D containingCell = targetDomain.findContainingCell(hPos);
                    if(containingCell == null)
                        continue;
                    coords.add(containingCell.getGridCoordinates());
                    values.add((Number)feature.getCoverage().evaluate(hPos, memberName));
                }
                frame.addMultipointData(values, coords, plotStyle);
                frame.addGridPoints(coords);
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
        GridCell2D containingCell = targetDomain.findContainingCell(feature
                .getHorizontalPosition());
        /*
         * If the feature is outside of our box, don't do anything.
         */
        if (containingCell != null) {
            GridCoordinates2D gridCoordinates = containingCell.getGridCoordinates();
            if(plotStyle == PlotStyle.POINT){
    
                boolean scalarField = feature.getCoverage().getScalarMemberNames()
                        .contains(memberName);
                if (scalarField) {
                    ScalarMetadata scalarMetadata = feature.getCoverage()
                            .getScalarMetadata(memberName);
                    Class<?> clazz = scalarMetadata.getValueType();
                    Number value = null;
                    if (Number.class.isAssignableFrom(clazz)) {
                        /*
                         * We can plot non-numerical values of point series
                         * features. We just plot them as an out-of-range
                         * number.
                         */
                        value = (Number) feature.getCoverage()
                                .evaluate(tPos, memberName);
                    }
                    frame.addPointData(value, gridCoordinates, plotStyle);
                } else {
                    throw new UnsupportedOperationException(
                            "Plotting of non-scalar members of PointSeriesFeatures is not yet supported");
                }
            } else if(plotStyle == PlotStyle.GRID_POINTS){
                List<GridCoordinates2D> coord = new ArrayList<GridCoordinates2D>();
                frame.addGridPoints(coord);
            } else {
                throw new IllegalArgumentException("Cannot plot a PointSeriesFeature in the style "+plotStyle);
            }
        }
    }

    private void addProfileFeatureToFrame(ProfileFeature feature, String memberName,
            VerticalPosition vPos, String label, PlotStyle plotStyle, Frame frame) {
        HorizontalGrid targetDomain = new RegularGridImpl(bbox, width, height);
        GridCell2D containingCell = targetDomain.findContainingCell(feature
                .getHorizontalPosition());
        /*
         * If the feature is outside of our box, don't do anything.
         */
        if (containingCell != null) {
            GridCoordinates2D gridCoordinates = containingCell.getGridCoordinates();
            if(plotStyle == PlotStyle.POINT){
                
                boolean scalarField = feature.getCoverage().getScalarMemberNames()
                        .contains(memberName);
                if (scalarField) {
                    ScalarMetadata scalarMetadata = feature.getCoverage()
                            .getScalarMetadata(memberName);
                    Class<?> clazz = scalarMetadata.getValueType();
                    Number value = null;
                    if (Number.class.isAssignableFrom(clazz)) {
                        /*
                         * We can plot non-numerical values of point series
                         * features. We just plot them as an out-of-range
                         * number.
                         */
                        value = (Number) feature.getCoverage()
                                .evaluate(vPos, memberName);
                    }
                    frame.addPointData(value, gridCoordinates, plotStyle);
                } else {
                    throw new UnsupportedOperationException(
                            "Plotting of non-scalar members of ProfileFeatures is not yet supported");
                }
            } else if(plotStyle == PlotStyle.GRID_POINTS){
                List<GridCoordinates2D> coord = new ArrayList<GridCoordinates2D>();
                frame.addGridPoints(coord);
            } else {
                throw new IllegalArgumentException("Cannot plot a ProfileFeature in the style "+plotStyle);
            }
        }
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
}
