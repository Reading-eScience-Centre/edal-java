package uk.ac.rdg.resc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.util.FactoryException;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.UnitVocabulary;
import uk.ac.rdg.resc.edal.cdm.feature.NcGridSeriesFeatureCollection;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.graphics.MapRenderer;
import uk.ac.rdg.resc.edal.graphics.MapStyleDescriptor;
import uk.ac.rdg.resc.edal.graphics.MapStyleDescriptor.Style;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalCrs.PositiveDirection;
import uk.ac.rdg.resc.edal.position.impl.HorizontalPositionImpl;
import uk.ac.rdg.resc.edal.position.impl.TimePositionJoda;
import uk.ac.rdg.resc.edal.position.impl.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.position.impl.VerticalPositionImpl;
import uk.ac.rdg.resc.edal.util.Extents;

public class FeatureCollectionTest {
    public static void main(String[] args) throws IOException, NoSuchAuthorityCodeException, FactoryException, InstantiationException {
//        NcGridSeriesFeatureCollection fs = new NcGridSeriesFeatureCollection("testcollection",
//                "Test Collection", "/home/guy/Data/FOAM_ONE/FOAM_one.ncml");
//        // NcGridSeriesFeatureCollection fs = new
//        // NcGridSeriesFeatureCollection("testcollection", "Test Collection",
//        // "/home/guy/Data/POLCOMS_IRISH/polcoms_irish_hourly_20090320.nc");
//
////        for (String fId : fs.getFeatureIds()) {
////            System.out.println(fId);
////        }
//        String fId = "TMP";
//        GridSeriesFeature<?> feature = fs.getFeatureById(fId);
////        BoundingBox bbox = new BoundingBoxImpl(new double[]{ -18.0, -18.0, 18.0, 18.0 }, DefaultGeographicCRS.WGS84);
//        BoundingBox bbox = new BoundingBoxImpl(new double[]{ 0, -80.0, 360, 80.0 }, DefaultGeographicCRS.WGS84);
////        BoundingBox bbox = new BoundingBoxImpl(new double[]{ -4350000, -4350000, 8350000, 8350000 }, CRS.decode("EPSG:32661", true));
////        double[] bbox = { -9.9, -9.99, -0.1, -0.05 };
//        int width = 900;
//        int height = 900;
//        HorizontalGrid targetDomain = new RegularGridImpl(bbox, width, height);
//        feature.getCoverage().getDomain().getDomainObjects();
//        
//        GridCoverage2D<?> gridCoverage = feature.extractHorizontalGrid(0, 0, targetDomain);
//        if (gridCoverage != null) {
//            MapStyleDescriptor style = new MapStyleDescriptor();
//            style.setStyle(Style.POINT);
//            MapRenderer mapRenderer = new MapRenderer(style, width, height, bbox);
//            TimePosition okTime = feature.getCoverage().getDomain().getTimeAxis().getCoordinateValues().get(0);
//            VerticalCrs vCrs = new VerticalCrsImpl(Unit.getUnit("m", UnitVocabulary.UDUNITS),
//                    PositiveDirection.UP, false);
//            mapRenderer.addData(feature, okTime, new VerticalPositionImpl(-150.0, vCrs), null);
//            Extent<TimePositionJoda> tExtent = Extents.newExtent(new TimePositionJoda(0L),
//                    new TimePositionJoda());
////            for(int i=-90;i<90;i+=5){
////                for(int j=-45;j<45;j+=5){
////                    PointSeriesFeature<?> pointFeature = feature.extractPointSeriesFeature(
////                            new HorizontalPositionImpl(i, j, DefaultGeographicCRS.WGS84),
////                            new VerticalPositionImpl(0.0, vCrs), tExtent);
////                    mapRenderer.addData(pointFeature, okTime, new VerticalPositionImpl(0.0, vCrs), null);
////                }
////            }
//
//            List<BufferedImage> images = mapRenderer.getRenderedFrames();
//            ImageIO.write(images.get(0), "png", new File(fId + ".png"));
//        }
////        }
    }
}
