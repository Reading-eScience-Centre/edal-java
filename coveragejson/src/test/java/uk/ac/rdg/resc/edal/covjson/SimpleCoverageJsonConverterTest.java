package uk.ac.rdg.resc.edal.covjson;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.cdm.CdmGridDatasetFactory;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.Feature;

public class SimpleCoverageJsonConverterTest {
	
	private Feature<?> feature;
	
    @Before
    public void setup() throws IOException, EdalException {
        /*-
         * The test NetCDF dataset is 4D, and contains 4 variables:
         * 
         * vLon
         * vLat
         * vDepth
         * vTime
         * 
         * each of which covers [0,100] along one single dimension.
         * 
         * The latitude covers [-180,170] at a resolution of 10 degrees
         * The longitude covers [-90, 90] at a resolution of 10 degrees
         * The depth covers [0,100] at a resolution of 10m
         * The time covers 10 days starting from 01/01/2000 00:00:00
         */
    	
        URL url = this.getClass().getResource("/test.nc");
        String location = url.getPath();
        CdmGridDatasetFactory datasetFactory = new CdmGridDatasetFactory();
        Dataset dataset = datasetFactory.createDataset("testdataset", location);
        feature = dataset.readFeature(dataset.getFeatureIds().iterator().next());
    }
    
    @Test
    public void testConversion() {
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	new CoverageJsonConverterImpl().convertFeatureToJson(out, feature);
    	assertTrue(out.size() > 0);
    }
}
