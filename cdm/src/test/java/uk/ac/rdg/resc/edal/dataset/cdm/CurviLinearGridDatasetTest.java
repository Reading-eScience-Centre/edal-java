package uk.ac.rdg.resc.edal.dataset.cdm;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import uk.ac.rdg.resc.edal.dataset.AbstractGridDataset;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.MapFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.metadata.GridVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.LonLatPosition;
import uk.ac.rdg.resc.edal.util.CurvilinearCoords;
import uk.ac.rdg.resc.edal.util.ValuesArray2D;

public class CurviLinearGridDatasetTest {
    private static final double delta = 1e-5;
    private Dataset dataset;
    private NetcdfFile cdf;
    private String location;
    // parameters about the used test dataset
    private int etaSize = 336;
    private int xiSize = 896;
    
    @Before
    public void setUp() throws Exception {
       URL url = this.getClass().getResource("/output-curvilinear.nc");
       
        //URL url = this.getClass().getResource("/rectilinear_test_data.nc");
        location = url.getPath();
        //"C:/Users/yv903893/workspace/edal-java/cdm/src/test/resources/output-curvilinear.nc"
        cdf =NetcdfFile.open(location);
        CdmGridDatasetFactory datasetFactory = new CdmGridDatasetFactory();
        dataset = datasetFactory.createDataset("testdataset", location);
    }
    
    @Test
    public void readData() throws IOException{
        Variable allxU =cdf.findVariable("ally_u");
        Variable allxV =cdf.findVariable("ally_v");
        ArrayFloat uValue = (ArrayFloat) allxU.read();
        ArrayFloat vValue = (ArrayFloat) allxV.read();
        for(int i=0; i<vValue.getSize(); i++)
            System.out.print(vValue.getFloat(i)+" ");
    }
    
    @Test
    public void testCurviLinearCoords() throws Exception{
        //for(String varId : dataset.getFeatureIds())
            //System.out.println(varId);
        //System.out.println(location);
        Variable lon_rho =cdf.findVariable("lon_rho");
        Variable lat_rho =cdf.findVariable("lat_rho");

        ArrayDouble lon_data = (ArrayDouble) lon_rho.read();
        ArrayDouble lat_data = (ArrayDouble) lat_rho.read();

        //int yySize =lon_data.getShape()[1]; //896
        //int xxSize =lon_data.getShape()[0]; //336
        ValuesArray2D lon_values =new ValuesArray2D(xiSize, etaSize);
        ValuesArray2D lat_values =new ValuesArray2D(xiSize, etaSize);
        for(int i=0; i<lon_data.getSize(); i++){
            int m = i % etaSize;
            int n = i / etaSize;
            int [] coords =new int[ ] {n, m};
            lon_values.set(lon_data.getDouble(i), coords);
            lat_values.set(lat_data.getDouble(i), coords);
        }
        assertEquals(lon_data.getDouble(etaSize), lon_values.get(1,0).doubleValue(), delta);
        assertEquals(lat_data.getDouble(etaSize), lat_values.get(1,0).doubleValue(), delta);
        CurvilinearCoords cCoords =new CurvilinearCoords(lon_values, lat_values);
        
        BoundingBox  expectedBbox = dataset.getVariableMetadata("allx_u").getHorizontalDomain().getBoundingBox();
        
        assertEquals(expectedBbox, cCoords.getBoundingBox());
        assertEquals(lon_data.getSize(), lon_values.size());
        assertEquals(etaSize, cCoords.getNi());
        assertEquals(xiSize, cCoords.getNj());
        
        int index =15000;
        LonLatPosition expectedPos =new LonLatPosition(lon_data.getDouble(index), lat_data.getDouble(index));
        int cCoords_i =index % etaSize;
        int cCoords_j =index / etaSize;
        //assertEquals(expectedPos, cCoords.getMidpoint(cCoords_i, cCoords_j));

        //dataset.supportsProfileFeatureExtraction(varId)
        //dataset.getVariableMetadata("allx_u").getHorizontalDomain().
        
        GridVariableMetadata metadata =(GridVariableMetadata) dataset.getVariableMetadata("allx_u");

        int xSize = metadata.getHorizontalDomain().getXSize();
        System.out.println(xSize);
        dataset.readFeature("allx_u");
        //System.out.println(metadata.getHorizontalDomain().getXSize());
        assertTrue(dataset.readFeature("allx_u") instanceof GridFeature);

    }

    @Test
    public void testCurviLinearDataset() throws DataReadingException, IOException{
        assertTrue(dataset instanceof AbstractGridDataset);
        GridFeature uValue = ((AbstractGridDataset) dataset).readFeature("ally_u");
        GridFeature vValue = ((AbstractGridDataset) dataset).readFeature("ally_v");
        Variable allxU =cdf.findVariable("ally_u");
        Variable allxV =cdf.findVariable("ally_v");
        ArrayFloat uValues = (ArrayFloat) allxU.read();
        ArrayFloat vValues = (ArrayFloat) allxV.read();
        
        for(int i=0; i< uValues.getSize(); i++){
            int m = i % xiSize;
            int n = i /xiSize;
            float f =uValue.getValues("ally_u").get(0,0,n,m).floatValue();
            float ff =vValue.getValues("ally_v").get(0,0,n,m).floatValue();
            assertEquals(uValues.getFloat(i), f, delta);
            assertEquals(vValues.getFloat(i), ff, delta);
        }
    }
}
