package uk.ac.rdg.resc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import uk.ac.rdg.resc.edal.cdm.feature.NcGridSeriesFeatureCollection;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.graphics.ImageGenerators;
import uk.ac.rdg.resc.edal.graphics.MapStyleDescriptor;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.position.impl.VerticalPositionImpl;

public class FeatureCollectionTest {
    public static void main(String[] args) throws IOException, InstantiationException {
        NcGridSeriesFeatureCollection featureCollection = new NcGridSeriesFeatureCollection(
                "testcollection", "Test Collection",
                "/home/guy/Data/POLCOMS_IRISH/polcoms_irish_hourly_20090320.nc");
        for (String featureId : featureCollection.getFeatureIds()) {
            System.out.println("Feature: " + featureId);
            GridSeriesFeature f = featureCollection.getFeatureById(featureId);
            System.out.println(f.getId());
//            if(!f.getName().equals("grid2")) continue;

            RangeMetadata metadata = f.getCoverage().getRangeMetadata();
            GridSeriesCoverage coverage = f.getCoverage();
            GridSeriesDomain domain = coverage.getDomain();
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
            HorizontalGrid targetGrid = new RegularGridImpl(domain.getHorizontalGrid().getCoordinateExtent(), 500, 500);
            GridFeature gridFeature = f.extractGridFeature(targetGrid, vPos, tPos, null); 
            for (String memberName : coverage.getMemberNames()) {
                MapStyleDescriptor style = new MapStyleDescriptor();
                BufferedImage image = ImageGenerators.plotFeature(gridFeature, memberName, style);
                ImageIO.write(image, "png", new File(f.getId() + "-" + memberName + "-output.png"));
            }
        }
    }
}
