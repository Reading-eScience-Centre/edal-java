package uk.ac.rdg.resc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import ucar.ma2.InvalidRangeException;
import uk.ac.rdg.resc.edal.cdm.feature.EN3ProfileFeatureCollection;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.graphics.Charting;

public class EN3Test {
    public static void main(String[] args) throws IOException, InstantiationException, InvalidRangeException {
        EN3ProfileFeatureCollection en3Collection = new EN3ProfileFeatureCollection(
                "en3", "EN3 Collection", "/home/guy/Data/EN3/EN3_v1c_2007.12.nc");
        
        Set<String> featureIds = en3Collection.getFeatureIds();
//        System.out.println(featureIds);
        
        Feature feature = en3Collection.getFeatureById("prof123");
        if(feature instanceof ProfileFeature){
            ProfileFeature profileFeature = (ProfileFeature) feature;
            
            for(Double zVal : profileFeature.getCoverage().getDomain().getZValues()){
                System.out.println(zVal);
            }
            
            JFreeChart verticalProfilePlot = Charting.createVerticalProfilePlot(profileFeature, "TEMP");
            ChartUtilities.writeChartAsPNG(new FileOutputStream(new File("/home/guy/00en3test.png")), verticalProfilePlot, 500, 500);
        }
    }
}
