package uk.ac.rdg.resc;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;

import uk.ac.rdg.resc.edal.cdm.feature.NcGridSeriesFeatureCollection;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.graphics.ImageProducer;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.position.impl.GeoPositionImpl;
import uk.ac.rdg.resc.edal.position.impl.HorizontalPositionImpl;
import uk.ac.rdg.resc.edal.position.impl.VerticalPositionImpl;

public class FeatureCollectionTest {
    public static void main(String[] args) throws IOException {
        NcGridSeriesFeatureCollection fs = new NcGridSeriesFeatureCollection("testcollection", "Test Collection",
                "/home/guy/Data/FOAM_ONE/FOAM_one.ncml");
//        NcGridSeriesFeatureCollection fs = new NcGridSeriesFeatureCollection("testcollection", "Test Collection",
//            "/home/guy/Data/POLCOMS_IRISH/polcoms_irish_hourly_20090320.nc");

        for (String fId : fs.getFeatureIds()) {
//        String fId = "sst";
            GridSeriesFeature<?> feature = fs.getFeatureById(fId);
            double[] bbox = { -180.0, -90.0, 180.0, 90.0 };
//            double[] bbox = { -7.5, 50.0, -2.5, 57.0 };
//            double[] bbox = { -0.5, -45.0, 0.0, -44.5 };
            int width = 500;
            int height = 250;
//            int width = 3;
//            int height = 3;
            HorizontalGrid targetDomain = new RegularGridImpl(new BoundingBoxImpl(bbox, DefaultGeographicCRS.WGS84),width, height);

//            TimePosition time = feature.getCoverage().getDomain().getTimeAxis().getCoordinateValue(0);
//            ProfileFeature<Float> profile = feature.extractProfileFeature(new HorizontalPositionImpl(5.0, 40), time);
//            List<Float> profileValues = profile.getCoverage().getValues();
//            for(Float f:profileValues){
//                System.out.println(f);
//            }
            
//            feature.extractPointSeriesFeature(pos, new VerticalPositionImpl(0.0), tRange)
            
//            TimePosition tPos = feature.getCoverage().getDomain().getTimeAxis().getCoordinateValue(0);
//            VerticalPosition vPos = new VerticalPositionImpl(feature.getCoverage().getDomain().getVerticalAxis().getCoordinateValue(0));
//            System.out.println(tPos+","+vPos.getZ());
//            feature.getCoverage().getDomain().getVerticalAxis().getCoordinateValue(0);
            GridCoverage2D<?> gridCoverage = feature.extractHorizontalGrid(0, 0, targetDomain);
            System.out.println(gridCoverage.getRangeMetadata(null).getValueType());
//            System.out.println("VALUE-->"+gridCoverage.evaluate(new HorizontalPositionImpl(-0.25, -45)));
//            System.out.println("3dVAL-->"+feature.getCoverage().evaluate(new GeoPositionImpl(new HorizontalPositionImpl(-0.25, -45), vPos, tPos)));
            if(gridCoverage != null) {
                System.out.println(feature.getName());
            	System.out.println(fId+"-"+gridCoverage.getDescription()+"-"+feature.getName()+"-"+feature.getDescription());
	            
	//            HorizontalPosition hPos = new HorizontalPositionImpl(-5.0, 50.0);
	//            GridCoordinates2D coords = gridCoverage.getDomain().findContainingCell(hPos);
	//            GridCell2D cell = gridCoverage.getDomain().getGridCell(coords);
	//            System.out.println(cell.getCentre());
	            
	            ImageProducer ip = new ImageProducer.Builder().height(height).width(width).transparent(true)
	                    .backgroundColour(Color.BLACK).build();
	            ip.addFrame(gridCoverage, null);
	            List<BufferedImage> images = ip.getRenderedFrames();
	            ImageIO.write(images.get(0), "png", new File(fId + ".png"));
            }
        }
    }
}
