package uk.ac.rdg.resc;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;

import uk.ac.rdg.resc.edal.cdm.feature.NcGridSeriesFeatureCollection;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.RegularAxis;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularAxisImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.graphics.ImageProducer;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.impl.HorizontalPositionImpl;
import uk.ac.rdg.resc.edal.position.impl.VerticalPositionImpl;
import uk.ac.rdg.resc.edal.util.Extents;

public class FeatureCollectionTest {
    public static void main(String[] args) throws IOException {
        NcGridSeriesFeatureCollection fs = new NcGridSeriesFeatureCollection("testcollection", "Test Collection",
                "/home/guy/Data/FOAM_ONE/FOAM_one.ncml");
//        NcGridSeriesFeatureCollection fs = new NcGridSeriesFeatureCollection("testcollection", "Test Collection",
//            "/home/guy/Data/POLCOMS_IRISH/polcoms_irish_hourly_20090320.nc");

        for (String fId : fs.getFeatureIds()) {
//        String fId = "sst";
            System.out.println(fId);
            GridSeriesFeature<Float> feature = fs.getFeatureById(fId);
            double[] bbox = { -10.0, 50.0, 0.0, 60.0 };
            int width = 500;
            int height = 500;
            HorizontalGrid targetDomain = new RegularGridImpl(new BoundingBoxImpl(bbox, DefaultGeographicCRS.WGS84),width, height);

//            TimePosition time = feature.getCoverage().getDomain().getTimeAxis().getCoordinateValue(0);
//            ProfileFeature<Float> profile = feature.extractProfileFeature(new HorizontalPositionImpl(5.0, 40), time);
//            List<Float> profileValues = profile.getCoverage().getValues();
//            for(Float f:profileValues){
//                System.out.println(f);
//            }
            
//            feature.extractPointSeriesFeature(pos, new VerticalPositionImpl(0.0), tRange)
            
            GridCoverage2D<Float> gridCoverage = feature.extractHorizontalGrid(0, 0, targetDomain);
            
//            HorizontalPosition hPos = new HorizontalPositionImpl(-5.0, 50.0);
//            GridCoordinates2D coords = gridCoverage.getDomain().findContainingCell(hPos);
//            GridCell2D cell = gridCoverage.getDomain().getGridCell(coords);
//            System.out.println(cell.getCentre());
            
            ImageProducer ip = new ImageProducer.Builder().height(height).width(width).transparent(true)
                    .backgroundColour(Color.BLACK).build();
            ip.addFrame(gridCoverage.getValues(), null);
            List<BufferedImage> images = ip.getRenderedFrames();
            ImageIO.write(images.get(0), "png", new File(fId + ".png"));
        }
    }
}
