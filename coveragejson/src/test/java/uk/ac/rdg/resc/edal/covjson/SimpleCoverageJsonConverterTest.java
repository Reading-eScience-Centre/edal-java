package uk.ac.rdg.resc.edal.covjson;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.dataset.DiscreteLayeredDataset;
import uk.ac.rdg.resc.edal.dataset.cdm.CdmGridDatasetFactory;
import uk.ac.rdg.resc.edal.dataset.plugins.VectorPlugin;
import uk.ac.rdg.resc.edal.domain.MapDomain;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.util.GISUtils;

public class SimpleCoverageJsonConverterTest {

    private Feature<?> feature;
    private MapFeature mapFeature;

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
        DiscreteLayeredDataset<?, ?> dataset = datasetFactory.createDataset("testdataset", location);
        dataset.addVariablePlugin(new VectorPlugin("vLon", "vLat", "Test Vector Field", true));
        feature = dataset.readFeature(dataset.getFeatureIds().iterator().next());

        // from CdmGridDatasetFactoryTest
        int xSize = 36;
        int ySize = 19;
        HorizontalGrid hGrid = new RegularGridImpl(-180.5, -90.5, 179.5, 90.5,
                GISUtils.defaultGeographicCRS(), xSize, ySize);
        DateTime time = new DateTime(2000, 01, 01, 00, 00);
        mapFeature = dataset.extractMapFeatures(dataset.getVariableIds(),
                new MapDomain(hGrid.getBoundingBox(), xSize, ySize, 0.0, time)).get(0);
    }

    @Test
    public void testConversion() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new CoverageJsonConverterImpl().convertFeatureToJson(out, feature);
        assertTrue(out.size() > 0);
    }

    @Test
    public void testMapFeatureConversion() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new CoverageJsonConverterImpl().convertFeatureToJson(out, mapFeature);
        assertTrue(out.size() > 0);
    }
}
