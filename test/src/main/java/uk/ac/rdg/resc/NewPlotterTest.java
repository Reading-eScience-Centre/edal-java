package uk.ac.rdg.resc;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;

import uk.ac.rdg.resc.edal.cdm.feature.NcGridSeriesFeatureCollection;
import uk.ac.rdg.resc.edal.coverage.DomainObjectValuePair;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell4D;
import uk.ac.rdg.resc.edal.coverage.grid.RegularGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.graphics.style.FeatureCollectionAndMemberName;
import uk.ac.rdg.resc.edal.graphics.style.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.graphics.style.Id2FeatureAndMember;
import uk.ac.rdg.resc.edal.graphics.style.IdToDataPoints;
import uk.ac.rdg.resc.edal.graphics.style.Image;
import uk.ac.rdg.resc.edal.graphics.style.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.style.Plotter;
import uk.ac.rdg.resc.edal.graphics.style.PlottingDatum;
import uk.ac.rdg.resc.edal.graphics.style.RasterPlotter;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.position.impl.VerticalPositionImpl;

public class NewPlotterTest {
    public static void main(String[] args) throws IOException, InstantiationException {
        final NcGridSeriesFeatureCollection featureCollection = new NcGridSeriesFeatureCollection(
                "testcollection", "Test Collection",
                "/home/guy/Data/OSTIA/20100715-UKMO-L4HRfnd-GLOB-v01-fv02-OSTIA.nc");
//                "/home/guy/Data/FOAM_ONE/FOAM_one.ncml");
        
        final GridSeriesFeature feature = featureCollection.getFeatureById("testcollection1");
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
        
        int width = 800;
        int height = 400;
        
        BoundingBox bbox = domain.getHorizontalGrid().getCoordinateExtent();
        RegularGrid targetDomain = new RegularGridImpl(new BoundingBoxImpl(new double[] { -180,
                -90, 0, 90 }, bbox.getCoordinateReferenceSystem()), width, height);

        Plotter plotter = new RasterPlotter();
        
        final String member = "analysed_sst";
        
        ImageLayer layer = new ImageLayer(plotter, member);
        
        Image image = new Image();
        image.addLayer(layer);
        
        Id2FeatureAndMember id2Feature = new Id2FeatureAndMember() {
            @Override
            public FeatureCollectionAndMemberName getFeatureAndMemberName(String id) {
                return new FeatureCollectionAndMemberName(featureCollection, member);
            }
        };
        
        GlobalPlottingParams params = new GlobalPlottingParams(width, height, bbox, null, null, vPos, tPos);

        ImageIO.write(image.render(params, id2Feature), "png", new File("/home/guy/0000points.png"));
        
    }
}
