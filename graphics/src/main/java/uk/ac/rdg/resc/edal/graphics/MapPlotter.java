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
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.VectorComponent.VectorDirection;
import uk.ac.rdg.resc.edal.coverage.metadata.VectorMetadata;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
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

        Number[] data;
        Frame frame = frameData.get(tPos);
        if (frame == null) {
            frame = new Frame(width, height, label);
        }
        
        /*
         * If our feature is not yet plottable, we need to know which members to
         * extract. This will be dependent on the type of value that memberName
         * has. If the member is scalar, it will just return a set containing
         * that member name
         */
        Set<String> memberNamesToExtract = getAllScalarChildrenOf(feature.getCoverage()
                .getRangeMetadata(), memberName);
        
        if(feature instanceof GridSeriesFeature){
            feature = ((GridSeriesFeature) feature).extractGridFeature(new RegularGridImpl(bbox, width,
                    height), vPos, tPos, memberNamesToExtract);
        }
        
        if (feature instanceof GridFeature) {
            GridFeature gridFeature = (GridFeature) feature;
            BoundingBox coordinateExtent = gridFeature.getCoverage().getDomain()
                    .getCoordinateExtent();
            int fWidth = gridFeature.getCoverage().getDomain().getXAxis().size();
            int fHeight = gridFeature.getCoverage().getDomain().getYAxis().size();
            if (fWidth != width || fHeight != height || !coordinateExtent.equals(bbox)) {
                /*
                 * The input feature is not suitable for this map. Convert it
                 */
                gridFeature = gridFeature.extractGridFeature(new RegularGridImpl(bbox, width,
                        height), memberNamesToExtract);
            }

            boolean scalarField = gridFeature.getCoverage().getScalarMemberNames().contains(memberName);
            
            if (scalarField) {
                /*
                 * We have a scalar field. We just want to plot it.
                 */
                data = getDataFromGridFeature(gridFeature, memberName);
                frame.addData(data, plotStyle);
            } else {
                RangeMetadata rangeMetadata = gridFeature.getCoverage().getRangeMetadata();
                if (rangeMetadata.getMemberNames().contains(memberName)) {
                    /*
                     * We have a direct child with this name. It is NOT a scalar
                     * field.
                     */
                    RangeMetadata memberMetadata = rangeMetadata.getMemberMetadata(memberName);
                    if(memberMetadata instanceof VectorMetadata){
                        VectorMetadata vectorMetadata = (VectorMetadata) memberMetadata;
                        String magnitudeMember = null;
                        String directionMember = null;
                        for(String vectorComponentMember : memberMetadata.getMemberNames()){
                            if(vectorMetadata.getMemberMetadata(vectorComponentMember).getDirection() == VectorDirection.DIRECTION)
                                directionMember = vectorComponentMember;
                            else if(vectorMetadata.getMemberMetadata(vectorComponentMember).getDirection() == VectorDirection.MAGNITUDE)
                                magnitudeMember = vectorComponentMember;
                            
                        }
                        if(magnitudeMember != null && directionMember != null){
                            data = getDataFromGridFeature(gridFeature, magnitudeMember);
                            frame.addData(data, PlotStyle.BOXFILL);
                            data = getDataFromGridFeature(gridFeature, directionMember);
                            frame.addData(data, PlotStyle.VECTOR);
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

//        } else if(feature instanceof GridSeriesFeature){
//            data = null;
//        } else if(feature instanceof PointSeriesFeature){
//            data = null;
        } else {
            throw new UnsupportedOperationException("Plotting of features of the type "
                    + feature.getClass() + " on a map is not yet supported");
        }

        if (!frameData.containsKey(tPos)) {
            frameData.put(tPos, frame);
        }
    }

    /*
     * This gets all scalar children of the desired member.
     */
    private Set<String> getAllScalarChildrenOf(RangeMetadata rangeMetadata, String memberName){
        Set<String> returnSet = new HashSet<String>();
        
        for(String subMember : rangeMetadata.getMemberNames()){
            RangeMetadata memberMetadata = rangeMetadata.getMemberMetadata(subMember);
            if(memberMetadata instanceof ScalarMetadata){
                returnSet.add(subMember);
            } else {
                returnSet.addAll(getAllScalarChildrenOf(memberMetadata, memberName));
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
        /*
         * TODO does this sort correctly if just one null value is present
         */
        Collections.sort(times);
//        if (times.size() == 1 && times.get(0) == null)
            for (TimePosition time : times) {
                images.add(frameData.get(time).renderLayers(style));
            }
        return images;
    }

    private Number[] getDataFromGridFeature(final GridFeature feature, String memberName) {
        GridValuesMatrix<?> gridVals = feature.getCoverage().getGridValues(memberName);

        Class<?> clazz = gridVals.getValueType();
        if (!Number.class.isAssignableFrom(clazz)) {
            throw new UnsupportedOperationException(
                    "Can only add frames from GridValuesMatrix objects which contain numbers");
        }

        Number[] data = new Number[width * height];

        for (int i = 0; i < data.length; i++) {
            /*
             * The image coordinate system has the vertical axis increasing
             * downward, but the data's coordinate system has the vertical axis
             * increasing upwards. The method below flips the axis
             */
            int dataIndex = getDataIndex(i, width, height);
            Number num = (Number) gridVals.getValues().get(dataIndex);
            if (num != null && (num.equals(Float.NaN) || num.equals(Double.NaN))) {
                num = null;
            }
            data[i] = num;
        }
        return data;
    }

    /**
     * Calculates the index of the data point in a data array that corresponds
     * with the given index in the image array, taking into account that the
     * vertical axis is flipped.
     */
    static int getDataIndex(int imageIndex, int width, int height) {
        int imageI = imageIndex % width;
        int imageJ = imageIndex / width;
        return getDataIndex(imageI, imageJ, width, height);
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
