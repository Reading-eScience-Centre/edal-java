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
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.RegularAxis;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularAxisImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.graphics.ImageProducer;
import uk.ac.rdg.resc.edal.util.Extents;

public class FeatureCollectionTest {
    public static void main(String[] args) throws IOException {
        NcGridSeriesFeatureCollection fs = new NcGridSeriesFeatureCollection("testcollection", "Test Collection",
                "/home/guy/Data/FOAM_ONE/FOAM_20100130.0.nc");

        for (String fId : fs.getFeatureIds()) {
//        String fId = "tmp";
            GridSeriesFeature<Float> feature = fs.getFeatureById(fId);
            double[] bbox = { -180.0, -90.0, 180.0, 90.0 };
            int width = 500;
            int height = 250;
            HorizontalGrid targetDomain = new RegularGridImpl(new BoundingBoxImpl(bbox, DefaultGeographicCRS.WGS84),
                    width, height);

            GridCoverage2D<Float> gridCoverage = feature.extractHorizontalGrid(0, 0, targetDomain);

            ImageProducer ip = new ImageProducer.Builder().height(height).width(width).transparent(true)
                    .backgroundColour(Color.BLACK).build();
            ip.addFrame(gridCoverage.getValues(), null);
            List<BufferedImage> images = ip.getRenderedFrames();
            ImageIO.write(images.get(0), "png", new File(fId + ".png"));
        }
    }
}
