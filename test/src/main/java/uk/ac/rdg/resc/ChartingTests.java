package uk.ac.rdg.resc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import uk.ac.rdg.resc.edal.cdm.feature.NcGridSeriesFeatureCollection;
import uk.ac.rdg.resc.edal.coverage.grid.TimeAxis;
import uk.ac.rdg.resc.edal.coverage.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.exceptions.InvalidCrsException;
import uk.ac.rdg.resc.edal.exceptions.InvalidLineStringException;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.geometry.impl.LineString;
import uk.ac.rdg.resc.edal.graphics.Charting;
import uk.ac.rdg.resc.edal.graphics.ColorPalette;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.position.impl.HorizontalPositionImpl;
import uk.ac.rdg.resc.edal.position.impl.VerticalPositionImpl;
import uk.ac.rdg.resc.edal.util.Extents;

public class ChartingTests {

    /**
     * @param args
     * @throws IOException 
     * @throws InvalidLineStringException 
     * @throws InvalidCrsException 
     */
    public static void main(String[] args) throws IOException, InvalidCrsException, InvalidLineStringException {
        NcGridSeriesFeatureCollection featureCollection = new NcGridSeriesFeatureCollection(
                "testcollection", "Test Collection", "/home/guy/Data/FOAM_ONE/FOAM_one.ncml");
        System.out.println(featureCollection.getFeatureIds());
        GridSeriesFeature feature = (GridSeriesFeature) featureCollection.getFeatureById("grid1");
        System.out.println(feature.getCoverage().getScalarMemberNames());
        String memberName = "TMP";
        HorizontalPosition hPos = new HorizontalPositionImpl(0.0, 0.0,
                DefaultGeographicCRS.WGS84);
        TimeAxis timeAxis = feature.getCoverage().getDomain().getTimeAxis();
        VerticalAxis verticalAxis = feature.getCoverage().getDomain().getVerticalAxis();
        VerticalPosition zPos = new VerticalPositionImpl(verticalAxis.getCoordinateValue(0), feature.getCoverage()
                .getDomain().getVerticalCrs());
        TimePosition time = timeAxis.getCoordinateValue(0);
        
        PointSeriesFeature pointSeriesFeature = feature
                .extractPointSeriesFeature(hPos, zPos, timeAxis.getCoordinateExtent(), null);
        JFreeChart timeseriesPlot = Charting.createTimeseriesPlot(pointSeriesFeature, memberName);
        
        ProfileFeature profileFeature = feature.extractProfileFeature(hPos, time, null);
        JFreeChart verticalProfilePlot = Charting.createVerticalProfilePlot(profileFeature, memberName);
        
        LineString transectDomain = new LineString(
                "-38.671875 38.453125,-16.875 -28.34375,16.875 -54.359375,63.984375 -45.21875",
                DefaultGeographicCRS.WGS84, "1.3.0");
//        JFreeChart transectPlot = Charting.createTransectPlot(feature, memberName, transectDomain, zPos, time, "This is a copyrighted image");
        
        ColorPalette.loadPalettes(new File("/home/guy/Workspace/edal-java/ncwms/src/main/webapp/WEB-INF/conf/palettes/"));
        System.out.println(ColorPalette.getAvailablePaletteNames());
        
        JFreeChart verticalSectionChart = Charting.createVerticalSectionChart(feature, memberName, transectDomain,
                Extents.newExtent(273.0f, 310.0f), ColorPalette.get("rainbow"), 100, false, zPos, time);
        
        ChartUtilities.writeChartAsPNG(new FileOutputStream(new File("/home/guy/tsplot.png")), timeseriesPlot, 500, 400);
//        ChartUtilities.writeChartAsPNG(new FileOutputStream(new File("/home/guy/transplot.png")), transectPlot, 500, 400);
        ChartUtilities.writeChartAsPNG(new FileOutputStream(new File("/home/guy/vppplot.png")), verticalProfilePlot, 500, 400);
        ChartUtilities.writeChartAsPNG(new FileOutputStream(new File("/home/guy/vsplot.png")), verticalSectionChart, 500, 400);
    }

}
