package uk.ac.rdg.resc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import uk.ac.rdg.resc.edal.cdm.feature.NcGridSeriesFeatureCollection;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.grid.RegularGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.graphics.MapPlotter;
import uk.ac.rdg.resc.edal.graphics.MapStyleDescriptor;
import uk.ac.rdg.resc.edal.graphics.PlotStyle;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.position.impl.VerticalPositionImpl;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

public class FeatureCollectionTest {
    public static void main(String[] args) throws IOException, InstantiationException {
        NcGridSeriesFeatureCollection featureCollection = new NcGridSeriesFeatureCollection(
                "testcollection", "Test Collection",
//                "/home/guy/Data/POLCOMS_IRISH/polcoms_irish_hourly_20090320.nc");
                "/home/guy/Data/FOAM_ONE/FOAM_one.ncml");
        
        System.out.println(featureCollection.getId()+"=>"+featureCollection.getName()+":"+featureCollection.getFeatureIds());
        MapStyleDescriptor style = new MapStyleDescriptor();
        GridSeriesFeature feature = (GridSeriesFeature) featureCollection.getFeatureById("testcollection2");
        GridSeriesDomain domain = feature.getCoverage().getDomain();
        
        RegularGrid targetDomain = new RegularGridImpl(domain.getHorizontalGrid().getCoordinateExtent(), 800, 400);
        MapPlotter plotter = new MapPlotter(style, targetDomain);
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
        
        String member = "UV_MAG";
        GridFeature subFeature = feature.extractGridFeature(feature.getCoverage().getDomain()
                .getHorizontalGrid(), vPos, tPos, CollectionUtils.setOf(member));
        
        plotter.addToFrame(subFeature, member, vPos, tPos, null, PlotStyle.BOXFILL);
        BufferedImage image = plotter.getRenderedFrames().get(0);
        ImageIO.write(image, "png", new File("/home/guy/00feature.png"));
        System.exit(0);
        
    }
}
