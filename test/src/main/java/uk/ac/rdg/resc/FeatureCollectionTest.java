package uk.ac.rdg.resc;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import uk.ac.rdg.resc.edal.cdm.feature.NcGridSeriesFeatureCollection;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.grid.RegularGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.graphics.ColorPalette;
import uk.ac.rdg.resc.edal.graphics.MapPlotter;
import uk.ac.rdg.resc.edal.graphics.MapStyleDescriptor;
import uk.ac.rdg.resc.edal.graphics.PlotStyle;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.position.impl.VerticalPositionImpl;

public class FeatureCollectionTest {
    public static void main(String[] args) throws IOException, InstantiationException {
        NcGridSeriesFeatureCollection featureCollection = new NcGridSeriesFeatureCollection(
                "testcollection", "Test Collection",
//                "/home/guy/Data/POLCOMS_IRISH/polcoms_irish_hourly_20090320.nc");
                "/home/guy/Data/FOAM_ONE/FOAM_one.ncml");
        
        System.out.println(featureCollection.getId()+"=>"+featureCollection.getName()+":"+featureCollection.getFeatureIds());
        MapStyleDescriptor style = new MapStyleDescriptor();
        ColorPalette.loadPalettes(new File("/home/guy/Workspace/edal-java/ncwms/src/main/webapp/WEB-INF/conf/palettes/"));
//        System.out.println(ColorPalette.getAvailablePaletteNames());
//        style.setNumColourBands(20);
//        style.setColorPalette("greyscale");
        GridSeriesFeature feature = (GridSeriesFeature) featureCollection.getFeatureById("testcollection2");
        GridSeriesDomain domain = feature.getCoverage().getDomain();
        
        VerticalPosition vPos = null;
        try {
            vPos = new VerticalPositionImpl(domain.getVerticalAxis().getCoordinateValue(0),
                    domain.getVerticalCrs());
        } catch (NullPointerException e) {
        }
        TimePosition tPos = null;
        try {
            tPos = domain.getTimeAxis().getCoordinateValue(0);
        } catch (NullPointerException e) {
        }
        
        int width = 500;
        int height = 500;
        
        BoundingBox bbox = domain.getHorizontalGrid().getCoordinateExtent();
        RegularGrid simpleTargetDomain = new RegularGridImpl(bbox, width, height);
//        RegularGrid targetDomain = new RegularGridImpl(new BoundingBoxImpl(new double[] { -180,
//                -180, 180, 180 }, bbox.getCoordinateReferenceSystem()), width, height);
        RegularGrid targetDomain = new RegularGridImpl(new BoundingBoxImpl(new double[] { 0, 0,
                50, 50 }, bbox.getCoordinateReferenceSystem()), width, height);
        MapPlotter plotter = new MapPlotter(style, targetDomain);

        String member = "UV_MAG";
        PlotStyle plotStyle = PlotStyle.POINT;
        plotter.addToFrame(feature, member, vPos, tPos, "", plotStyle);
        
//        BoundingBox bbox = new BoundingBoxImpl(new double[]{-180,-45,-170,-15}, domain.getHorizontalCrs());
//        BufferedImage image = new BufferedImage(width*2, height*2, BufferedImage.TYPE_INT_ARGB);
//        Graphics2D graphics = image.createGraphics();
//        
//        RegularGrid targetDomain1 = new RegularGridImpl(new BoundingBoxImpl(new double[] {
//                bbox.getMinX(), bbox.getMinY(), bbox.getMinX() + 0.5 * bbox.getWidth(),
//                bbox.getMinY() + 0.5 * bbox.getHeight() }, bbox.getCoordinateReferenceSystem()),
//                width, height);
//        RegularGrid targetDomain2 = new RegularGridImpl(new BoundingBoxImpl(new double[] {
//                bbox.getMinX() + 0.5 * bbox.getWidth(), bbox.getMinY(), bbox.getMaxX(),
//                bbox.getMinY() + 0.5 * bbox.getHeight() }, bbox.getCoordinateReferenceSystem()),
//                width, height);
//        RegularGrid targetDomain3 = new RegularGridImpl(new BoundingBoxImpl(new double[] {
//                bbox.getMinX(), bbox.getMinY() + 0.5 * bbox.getHeight(),
//                bbox.getMinX() + 0.5 * bbox.getWidth(), bbox.getMaxY() },
//                bbox.getCoordinateReferenceSystem()), width, height);
//        RegularGrid targetDomain4 = new RegularGridImpl(new BoundingBoxImpl(new double[] {
//                bbox.getMinX() + 0.5 * bbox.getWidth(), bbox.getMinY() + 0.5 * bbox.getHeight(),
//                bbox.getMaxX(), bbox.getMaxY() }, bbox.getCoordinateReferenceSystem()), width,
//                height);
//        
//        MapPlotter plotter1 = new MapPlotter(style, targetDomain1);
//        GridFeature subFeature1 = feature.extractGridFeature(targetDomain1, vPos, tPos, CollectionUtils.setOf(member));
//        plotter1.addToFrame(subFeature1, member, vPos, tPos, null, plotStyle);
//        BufferedImage image1 = plotter1.getRenderedFrames().get(0);
//        
//        MapPlotter plotter2 = new MapPlotter(style, targetDomain2);
//        GridFeature subFeature2 = feature.extractGridFeature(targetDomain2, vPos, tPos, CollectionUtils.setOf(member));
//        plotter2.addToFrame(subFeature2, member, vPos, tPos, null, plotStyle);
//        BufferedImage image2 = plotter2.getRenderedFrames().get(0);
//        
//        MapPlotter plotter3 = new MapPlotter(style, targetDomain3);
//        GridFeature subFeature3 = feature.extractGridFeature(targetDomain3, vPos, tPos, CollectionUtils.setOf(member));
//        plotter3.addToFrame(subFeature3, member, vPos, tPos, null, plotStyle);
//        BufferedImage image3 = plotter3.getRenderedFrames().get(0);
//        
//        MapPlotter plotter4 = new MapPlotter(style, targetDomain4);
//        GridFeature subFeature4 = feature.extractGridFeature(targetDomain4, vPos, tPos, CollectionUtils.setOf(member));
//        plotter4.addToFrame(subFeature4, member, vPos, tPos, null, plotStyle);
//        BufferedImage image4 = plotter4.getRenderedFrames().get(0);
//        
//        graphics.drawImage(image4, 0, 0, null);
//        graphics.drawImage(image3, width, 0, null);
//        graphics.drawImage(image2, 0, height, null);
//        graphics.drawImage(image1, width, height, null);
        
        ImageIO.write(plotter.getRenderedFrames().get(0), "png", new File("/home/guy/00feature.png"));
        
    }
}
