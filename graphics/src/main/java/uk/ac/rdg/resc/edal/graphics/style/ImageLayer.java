package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.graphics.style.util.BilinearInterpolator;
import uk.ac.rdg.resc.edal.graphics.style.util.DataReadingTypes.PlotType;
import uk.ac.rdg.resc.edal.graphics.style.util.DataReadingTypes.SubsampleType;
import uk.ac.rdg.resc.edal.graphics.style.util.FeatureCollectionAndMemberName;
import uk.ac.rdg.resc.edal.graphics.style.util.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.graphics.style.util.Id2FeatureAndMember;
import uk.ac.rdg.resc.edal.graphics.style.util.PlottingDatum;
import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RegularAxis;
import uk.ac.rdg.resc.edal.grid.RegularGrid;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;

@XmlType(namespace = Image.NAMESPACE, name = "ImageLayerType")
public abstract class ImageLayer extends Drawable {
    
    protected interface DataReader {
        public List<PlottingDatum> getDataForLayerName(String layerId);
    }
    
    /*
     * The type of plot.  This determines how data will be extracted
     */
    private PlotType plotType;
    
    /*
     * For when the plot type is SUBSAMPLE
     */
    private int xSampleSize = 8;
    private int ySampleSize = 8;
    private SubsampleType subsampleType = SubsampleType.CLOSEST;

    @SuppressWarnings("unused")
    private ImageLayer(){}
    
    public ImageLayer(PlotType plotType) {
        this.plotType = plotType;
    }
    
    @Override
    public BufferedImage drawImage(final GlobalPlottingParams params, final Id2FeatureAndMember id2Feature) {
        BufferedImage image = new BufferedImage(params.getWidth(), params.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        drawIntoImage(image, params, id2Feature);
        return image;
    }
    
    protected void drawIntoImage(BufferedImage image, final GlobalPlottingParams params,
            final Id2FeatureAndMember id2Feature) {
        drawIntoImage(image, new DataReader() {
            @Override
            public List<PlottingDatum> getDataForLayerName(String layerId) {
                FeatureCollectionAndMemberName featureAndMemberName = id2Feature
                        .getFeatureAndMemberName(layerId);
                return getDataFromFeatures(featureAndMemberName, params);
            }
        });
    }

    public PlotType getPlotType() {
        return plotType;
    }
    
    public void setXSampleSize(int xSampleSize) {
        this.xSampleSize = xSampleSize;
    }
    
    @XmlTransient
    public int getXSampleSize(){
        return xSampleSize;
    }
    
    public void setYSampleSize(int ySampleSize) {
        this.ySampleSize = ySampleSize;
    }
    
    @XmlTransient
    public int getYSampleSize(){
        return ySampleSize;
    }
    
    public void setSubsampleType(SubsampleType subsampleType) {
        this.subsampleType = subsampleType;
    }
    
    @XmlTransient
    public SubsampleType getSubsampleType() {
        return subsampleType;
    }
    
    protected abstract void drawIntoImage(BufferedImage image, DataReader dataReader);
    
    private List<PlottingDatum> getDataFromFeatures(
            FeatureCollectionAndMemberName featureAndMemberName, GlobalPlottingParams params) {
        FeatureCollection<? extends Feature> featureCollection = featureAndMemberName
                .getFeatureCollection();
        String member = featureAndMemberName.getMemberName();

        /*
         * We use a bordered grid to get the bounding box, so that we include
         * any features which are just outside the area of interest. This is
         * needed for plotting glyphs where the data point falls just outside
         * the bounding box, but where the glyph still needs drawing for tiled
         * maps
         */
        BoundingBox biggerBbox = new BorderedGrid(params.getBbox(), params.getWidth(),
                params.getHeight()).getCoordinateExtent();
        @SuppressWarnings("unchecked")
        Collection<Feature> features = (Collection<Feature>) featureCollection
                .findFeatures(biggerBbox, params.getZExtent(), params.getTExtent(),
                        CollectionUtils.setOf(member));
        List<PlottingDatum> data = new ArrayList<PlottingDatum>();
        for (Feature feature : features) {
            if (feature instanceof GridSeriesFeature) {
                data.addAll(getDataFromGridSeriesFeature((GridSeriesFeature) feature, member,
                        params));
            } else if (feature instanceof GridFeature) {
                data.addAll(getDataFromGridCoverage2D(((GridFeature) feature).getCoverage(), member, params));
            } else if (feature instanceof PointSeriesFeature) {
                data.addAll(getDataFromPointSeriesFeature((PointSeriesFeature) feature, member,
                        params));
            } else if (feature instanceof ProfileFeature) {
                data.addAll(getDataFromProfileFeature((ProfileFeature) feature, member, params));
            } else if (feature instanceof TrajectoryFeature) {
                data.addAll(getDataFromTrajectoryFeature((TrajectoryFeature) feature, member,
                        params));
            } else {
                throw new UnsupportedOperationException("Plotting of features of the type "
                        + feature.getClass() + " on a map is not yet supported");
            }
        }
        return data;
    }

    private List<PlottingDatum> getDataFromGridSeriesFeature(GridSeriesFeature gridSeriesFeature,
            String member, GlobalPlottingParams params) {
        /*
         * For a GridSeriesFeature, we just wrap the GridSeriesCoverage and use
         * the getDataFromGridCoverage2D method
         */
        VerticalPosition vPos = null;
        if (gridSeriesFeature.getCoverage().getDomain().getVerticalAxis() != null
                && params.getTargetZ() != null) {
            vPos = new VerticalPositionImpl(params.getTargetZ(), gridSeriesFeature.getCoverage()
                    .getDomain().getVerticalCrs());
        }
        
        if(getPlotType() == PlotType.SMOOTHED) {
            return getDataFromGridCoverage2D(
                    gridSeriesFeature.getCoverage().extractGridCoverage(
                            gridSeriesFeature.getCoverage().getDomain().getHorizontalGrid(), vPos,
                            params.getTargetT(), CollectionUtils.setOf(member)), member, params);
        } else {
            return getDataFromGridCoverage2D(new GridCoverage2DWrappedGridSeriesCoverage(
                    gridSeriesFeature.getCoverage(), vPos, params.getTargetT()), member, params);
        }
    }
    
    private List<PlottingDatum> getDataFromGridCoverage2D(GridCoverage2D coverage, String member,
            GlobalPlottingParams params) {
        List<PlottingDatum> plottingData = new ArrayList<PlottingDatum>();

        Set<String> members = CollectionUtils.setOf(member);

        switch (getPlotType()) {
        case RASTER:
        {
            /*
             * We want a value for every pixel in the image (aside from missing
             * data), so we:
             * 
             * Project the feature onto the target grid
             * 
             * Get all values from this newly projected feature
             */
            RegularGrid targetDomain = new RegularGridImpl(params.getBbox(), params.getWidth(),
                    params.getHeight());
            if (!coverage.getDomain().equals(targetDomain)) {
                coverage = coverage.extractGridCoverage(targetDomain, members);
            }
            
            List<DomainObjectValuePair<GridCell2D>> list = coverage.list();
            for (DomainObjectValuePair<GridCell2D> entry : list) {
                GridCoordinates2D gridCoordinates = entry.getDomainObject().getGridCoordinates();
                GridCoordinates2D coords = new GridCoordinates2DImpl(gridCoordinates.getXIndex(),
                        params.getHeight() - gridCoordinates.getYIndex() - 1);
                plottingData.add(new PlottingDatum(coords, (Number) entry.getValue().getValue(
                        member)));
            }
            break;
        }
        case SUBSAMPLE:
        {
            RegularGrid targetDomain = new RegularGridImpl(params.getBbox(), params.getWidth(),
                    params.getHeight());
            if (!coverage.getDomain().equals(targetDomain)) {
                coverage = coverage.extractGridCoverage(targetDomain, members);
            }
            
            int xss = getXSampleSize();
            int yss = getYSampleSize();
            for(int i = xss/2; i <= params.getWidth(); i+=xss) {
                for(int j = yss/2; j <= params.getHeight(); j+=yss) {
                    GridCell2D gridCell = coverage.getDomain().getGridCell(i, j);
                    Object value = coverage.evaluate(gridCell.getCentre(), member);
                    GridCoordinates2D coords = new GridCoordinates2DImpl(i, params.getHeight() - j + 1);
                    plottingData.add(new PlottingDatum(coords, (Number) value));
                }
            }
            /*
             * TODO add different subsampling methods (i.e. MEAN as well as CLOSEST)
             */
            break;
        }
        case GLYPH:
        {
            /*
             * We want a value for every point on the feature which falls within
             * the bounding box, plus a gutter, up to a maximum on one per
             * pixel, so we:
             * 
             * Calculate a list of indices in the source data which correspond
             * to each pixel on the target image, removing duplicates
             * 
             * Read the value from each of those indices. This is not the most
             * efficient method for small datasets (i.e. less points than
             * pixels), but it is good for larger ones.
             */
            
            /*
             * We use BorderedGrid here because this will get some data outside
             * of the bounding box, which is required when plotting glyphs.
             * 
             * This will correspond to one grid point per pixel.
             */
            BorderedGrid borderedTargetDomain = new BorderedGrid(params.getBbox(), params.getWidth(),
                    params.getHeight());
            RegularAxis xAxis = borderedTargetDomain.getXAxis();
            RegularAxis yAxis = borderedTargetDomain.getYAxis();
            CoordinateReferenceSystem crs = params.getBbox().getCoordinateReferenceSystem();
            HorizontalGrid featureGrid = coverage.getDomain();
            Set<Long> neededIndices = new LinkedHashSet<Long>();
            /*
             * Find out which indices in the source data are required to plot
             * every pixel in the target domain.
             */
            for (double x : xAxis.getCoordinateValues()) {
                for (double y : yAxis.getCoordinateValues()) {
                    long index = featureGrid.findIndexOf(new HorizontalPositionImpl(x, y, crs));
                    if (index > 0) {
                        neededIndices.add(index);
                    }
                }
            }
            
            /*
             * Now loop through all needed indices, and add all points which have an equivalent centre
             */
            for (long index : neededIndices) {
                GridCoordinates2D gridCoords = featureGrid.getCoords(index);
                HorizontalPosition hPos = featureGrid.getGridCell(gridCoords).getCentre();
                List<GridCell2D> containingCells = borderedTargetDomain.findAllContainingCells(hPos);
                for(GridCell2D containingCell : containingCells) {
                    if (containingCell == null)
                        continue;
                    Number val = (Number) coverage.evaluate(hPos, member);
                    if (val == null || Float.isNaN(val.floatValue())) {
                        continue;
                    }
                    GridCoordinates2D gridCoordinates = containingCell.getGridCoordinates();
                    
                    GridCoordinates2D coords = new GridCoordinates2DImpl(gridCoordinates.getXIndex(),
                            params.getHeight() - gridCoordinates.getYIndex() - 1);
                    plottingData.add(new PlottingDatum(coords, val));
                }
            }
            break;
        }
        case SMOOTHED:
        {
            HorizontalGrid featureHorizGrid = coverage.getDomain();
            if(featureHorizGrid instanceof RegularGrid) {
                RegularGrid featureGrid = (RegularGrid) featureHorizGrid;
                RegularAxis xAxis = featureGrid.getXAxis();
                RegularAxis yAxis = featureGrid.getYAxis();
                
                double[][] dataToInterpolate = new double[xAxis.size()][yAxis.size()];
                {
                    coverage.evaluate(new HorizontalPositionImpl(0.0, 0.0, DefaultGeographicCRS.WGS84));
                    GridValuesMatrix<? extends Object> gridValues = coverage.getGridValues(member);
                    BigList<? extends Object> values = gridValues.getValues();
                    
                    for(int i=0;i<xAxis.size();i++) {
                        for(int j=0;j<yAxis.size();j++) {
                            dataToInterpolate[i][j] = ((Number) values.get(gridValues
                                    .getIndex(i, j))).doubleValue(); 
                        }                    
                    }
                }
                /*
                 * Sod the apache commons maths, this is where you need to write an interpolation routine.
                 */
                BilinearInterpolator interpolator = new BilinearInterpolator(xAxis.getCoordinateValues(), yAxis.getCoordinateValues(), dataToInterpolate);
                
                RegularGridImpl targetDomain = new RegularGridImpl(params.getBbox(), params.getWidth(),
                        params.getHeight());
                for(int i=0;i<params.getWidth();i++){
                    double x = targetDomain.getXAxis().getCoordinateValue(i);
                    for(int j=0;j<params.getHeight();j++){
                        double y = targetDomain.getYAxis().getCoordinateValue(j);
                        double val = interpolator.getValue(x,y);
                        GridCoordinates2D coords = new GridCoordinates2DImpl(i, params.getHeight() - j - 1);
                        plottingData.add(new PlottingDatum(coords, val));
                    }
                }
            } else {
                throw new IllegalArgumentException("Only regular grids can be smoothed at present");
            }
            break;
        }
        default:
            /*
             * The plot type is either:
             * 
             * TRAJECTORY, which requires all data, even outside the bounding
             * box. This is not compatible with GridSeriesFeatures.
             * 
             * or
             * 
             * Something we haven't named yet.
             * 
             * Either way, throw an exception
             */
            throw new IllegalArgumentException(
                    "GridFeatures are incompatible with this plotting type: "+getPlotType());
        }

        return plottingData;
    }

    private List<PlottingDatum> getDataFromPointSeriesFeature(PointSeriesFeature feature,
            String member, GlobalPlottingParams params) {

        HorizontalGrid targetDomain = new BorderedGrid(params.getBbox(), params.getWidth(),
                params.getHeight());
        GridCell2D containingCell = targetDomain
                .findContainingCell(feature.getHorizontalPosition());
        /*
         * PointSeriesFeatures have a single point. The plotting style doesn't
         * affect how we extract the data
         * 
         * This will change if we want something like a Voronoi cell plotter for
         * in-situ data, but at the moment we don't.
         */
        if (containingCell != null) {
            GridCoordinates2D gridCoordinates = containingCell.getGridCoordinates();
            Number value = (Number) feature.getCoverage().evaluate(
                    GISUtils.getClosestTimeTo(params.getTargetT(), feature.getCoverage()
                            .getDomain().getTimes()), member);
            return Arrays.asList(new PlottingDatum(gridCoordinates, value));
        } else {
            return Collections.emptyList();
        }
    }

    private List<PlottingDatum> getDataFromProfileFeature(ProfileFeature feature, String member,
            GlobalPlottingParams params) {
        HorizontalGrid targetDomain = new BorderedGrid(params.getBbox(), params.getWidth(),
                params.getHeight());
        GridCell2D containingCell = targetDomain
                .findContainingCell(feature.getHorizontalPosition());
        /*
         * ProfileFeatures have a single point. The plotting style doesn't
         * affect how we extract the data
         * 
         * This will change if we want something like a Voronoi cell plotter for
         * in-situ data, but at the moment we don't.
         */
        if (containingCell != null) {
            GridCoordinates2D gridCoordinates = containingCell.getGridCoordinates();
            Number value = (Number) feature.getCoverage().evaluate(
                    GISUtils.getClosestElevationTo(params.getTargetZ(),
                            GISUtils.getVerticalAxis(feature)), member);
            return Arrays.asList(new PlottingDatum(gridCoordinates, value));
        } else {
            return Collections.emptyList();
        }
    }

    private List<PlottingDatum> getDataFromTrajectoryFeature(TrajectoryFeature feature,
            String member, GlobalPlottingParams params) {
        if (getPlotType() != PlotType.TRAJECTORY) {
            return Collections.emptyList();
        }
        RegularGrid targetDomain = new RegularGridImpl(params.getBbox(), params.getWidth(),
                params.getHeight());
        TrajectoryCoverage coverage = feature.getCoverage();
        List<GeoPosition> positions = coverage.getDomain().getDomainObjects();

        List<PlottingDatum> data = new ArrayList<PlottingDatum>();

        for (GeoPosition geoPos : positions) {
            HorizontalPosition pos = geoPos.getHorizontalPosition();

            /*
             * Usually we delegate to targetDomain.findContainingCell for this
             * kind of thing
             * 
             * However, for trajectory plots we want to know what the index is,
             * even if it's outside the targetDomain
             */
            if (pos.getCoordinateReferenceSystem() != targetDomain.getCoordinateReferenceSystem()) {
                pos = GISUtils.transformPosition(pos, targetDomain.getCoordinateReferenceSystem());
            }

            double fracAlongX = (pos.getX() - targetDomain.getXAxis().getCoordinateExtent()
                    .getLow())
                    / (targetDomain.getXAxis().getCoordinateExtent().getHigh() - targetDomain
                            .getXAxis().getCoordinateExtent().getLow());
            int xIndex = (int) (fracAlongX * params.getWidth());

            double fracAlongY = (pos.getY() - targetDomain.getYAxis().getCoordinateExtent()
                    .getLow())
                    / (targetDomain.getYAxis().getCoordinateExtent().getHigh() - targetDomain
                            .getYAxis().getCoordinateExtent().getLow());

            int yIndex = params.getHeight() - 1 - (int) (fracAlongY * params.getHeight());

            data.add(new PlottingDatum(new GridCoordinates2DImpl(xIndex, yIndex), (Number) coverage
                    .evaluate(geoPos, member)));
        }
        return data;
    }
}
